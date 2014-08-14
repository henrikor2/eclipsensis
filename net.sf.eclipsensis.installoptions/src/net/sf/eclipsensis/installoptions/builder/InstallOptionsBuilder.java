/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.builder;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class InstallOptionsBuilder extends IncrementalProjectBuilder implements IInstallOptionsConstants
{
    private static final Version cBuilderVersion;
    private static final Set<String> cExtensionsSet;
    private static final JobScheduler cJobScheduler = InstallOptionsPlugin.getDefault().getJobScheduler();

    private IDocumentProvider mDocumentProvider = new FileDocumentProvider();
    private INIFile mINIFile = new INIFile();
    private long mFullBuildTimestamp;
    private String mNSISVersion;

    static {
        cBuilderVersion = new Version(InstallOptionsPlugin.getResourceString("builder.version")); //$NON-NLS-1$
        cExtensionsSet = new CaseInsensitiveSet();
        for(int i=0; i<INI_EXTENSIONS.length; i++) {
            cExtensionsSet.add("."+INI_EXTENSIONS[i]); //$NON-NLS-1$
        }
    }

    @Override
    protected void clean(IProgressMonitor monitor)
    {
        try {
            String taskName = InstallOptionsPlugin.getResourceString("clean.task.name"); //$NON-NLS-1$
            monitor.beginTask(taskName,1);
            resetBuildTimestamp();
        }
        finally {
            monitor.done();
        }
    }

    @Override
    protected void startupOnInitialize()
    {
        super.startupOnInitialize();

        String nsisVersion;

        Version v = InstallOptionsModel.INSTANCE.getNSISVersion();
        nsisVersion = v == null ? null : v.toString();
        try {
            mNSISVersion = getProject().getPersistentProperty(PROJECTPROPERTY_NSIS_VERSION);
        }
        catch (CoreException e) {
            mNSISVersion = null;
        }

        if(!Common.stringsAreEqual(nsisVersion, mNSISVersion)) {
            resetNSISVersion(nsisVersion);
        }
        else {
            mFullBuildTimestamp = -1;
            String timestamp;
            try {
                timestamp = getProject().getPersistentProperty(RESOURCEPROPERTY_BUILD_TIMESTAMP);
            }
            catch (CoreException e) {
                timestamp = null;
            }
            if(!Common.isEmpty(timestamp)) {
                try {
                    mFullBuildTimestamp = Long.parseLong(timestamp);
                }
                catch(NumberFormatException nfe) {
                    mFullBuildTimestamp = -1;
                }
            }

            if(mFullBuildTimestamp < 0) {
                resetBuildTimestamp();
            }
        }

        final IModelListener modelListener = new IModelListener(){
            public void modelChanged()
            {
                Version v = NSISPreferences.getInstance().getNSISVersion();
                String nsisVersion = v == null? null : v.toString();
                if(!Common.stringsAreEqual(nsisVersion, mNSISVersion)) {
                    resetNSISVersion(nsisVersion);
                    buildProject(getProject(),FULL_BUILD,null);
                }
            }
        };
        InstallOptionsModel.INSTANCE.addModelListener(modelListener);

        getProject().getWorkspace().addResourceChangeListener(new IResourceChangeListener(){
            public void resourceChanged(IResourceChangeEvent event)
            {
                if(event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                    if(getProject() == event.getResource()) {
                        InstallOptionsModel.INSTANCE.removeModelListener(modelListener);
                        cJobScheduler.cancelJobs(new ProjectJobFamily(getProject()));
                        getProject().getWorkspace().removeResourceChangeListener(this);
                    }
                }
            }
        });
    }

    private void resetNSISVersion(String nsisVersion)
    {
        mNSISVersion = nsisVersion;
        try {
            getProject().setPersistentProperty(PROJECTPROPERTY_NSIS_VERSION, mNSISVersion);
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
        resetBuildTimestamp();
    }

    private void resetBuildTimestamp()
    {
        mFullBuildTimestamp = System.currentTimeMillis();
        try {
            getProject().setPersistentProperty(RESOURCEPROPERTY_BUILD_TIMESTAMP,Long.toString(mFullBuildTimestamp));
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
    {
        if (kind == FULL_BUILD) {
            fullBuild(monitor);
        }
        else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild(monitor);
            }
            else {
                if(!checkBuilderVersion(getProject())) {
                    fullBuild(monitor);
                }
                else {
                    incrementalBuild(delta, monitor);
                }
            }
        }
        return null;
    }

    private static String getExtension(String name)
    {
        int n = name.lastIndexOf('.');
        if(n > 0) {
            return name.substring(n);
        }
        else {
            return null;
        }
    }

    private void checkINI(IResource resource)
    {
        if (resource instanceof IFile && cExtensionsSet.contains(getExtension(resource.getName()))) {
            IFile file = (IFile) resource;
            try {
                String editorId = file.getPersistentProperty(IDE.EDITOR_KEY);
                if(editorId != null && (INSTALLOPTIONS_DESIGN_EDITOR_ID.equals(editorId) ||
                                        INSTALLOPTIONS_SOURCE_EDITOR_ID.equals(editorId))) {
                    long fileTimestamp = file.getLocalTimeStamp();
                    long buildTimestamp;
                    try {
                        buildTimestamp = Long.parseLong(file.getPersistentProperty(RESOURCEPROPERTY_BUILD_TIMESTAMP));
                    }
                    catch(Exception ex) {
                        buildTimestamp = -1;
                    }
                    if(buildTimestamp < mFullBuildTimestamp || buildTimestamp < fileTimestamp) {
                        IFileEditorInput input = new FileEditorInput(file);
                        mDocumentProvider.connect(input);
                        final IDocument document = mDocumentProvider.getDocument(input);
                        mINIFile.connect(document);
                        updateMarkers(file, mINIFile);
                        mINIFile.disconnect(document);
                        mDocumentProvider.disconnect(input);
                    }
                }
                else {
                    deleteMarkers(file);
                }
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    protected void fullBuild(final IProgressMonitor monitor)
    {
        try {
            monitor.beginTask(InstallOptionsPlugin.getResourceString("full.build.task.name"),100); //$NON-NLS-1$
            resetBuildTimestamp();
            monitor.worked(50);
            getProject().accept(new IResourceVisitor() {
                public boolean visit(IResource resource)
                {
                    if(monitor.isCanceled()) {
                        return false;
                    }
                    checkINI(resource);
                    return true;
                }
            });
            monitor.worked(45);
            getProject().setPersistentProperty(PROJECTPROPERTY_BUILDER_VERSION, cBuilderVersion.toString());
            monitor.worked(5);
        }
        catch (CoreException e) {
        }
        finally {
            monitor.done();
        }
    }

    protected void incrementalBuild(IResourceDelta delta, final IProgressMonitor monitor) throws CoreException
    {
        try {
            monitor.beginTask(InstallOptionsPlugin.getResourceString("full.build.task.name"),100); //$NON-NLS-1$
            delta.accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta)
                {
                    if(monitor.isCanceled()) {
                        return false;
                    }
                    IResource resource = delta.getResource();
                    switch (delta.getKind()) {
                        case IResourceDelta.ADDED:
                            checkINI(resource);
                            break;
                        case IResourceDelta.REMOVED:
                            break;
                        case IResourceDelta.CHANGED:
                            checkINI(resource);
                            break;
                    }
                    return true;
                }
            });
            monitor.worked(100);
        }
        finally {
            monitor.done();
        }
    }

    private void updateMarkers(final IFile file, final INIFile iniFile)
    {
        if(file != null) {
            WorkspaceModifyOperation op = new WorkspaceModifyOperation(file)
            {
                @Override
                protected void execute(IProgressMonitor monitor)throws CoreException
                {
                    try {
                        monitor.beginTask(InstallOptionsPlugin.getResourceString("updating.markers.task.name"), 2+(iniFile==null?0:iniFile.getProblems().length)); //$NON-NLS-1$
                        deleteMarkers(file);
                        monitor.worked(1);
                        if(iniFile != null) {
                            INIProblem[] problems = iniFile.getProblems();
                            for (int i = 0; i < problems.length; i++) {
                                IMarker marker = file.createMarker(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID);
                                marker.setAttribute(IMarker.SEVERITY,
                                        problems[i].getType()==INIProblem.TYPE_WARNING?
                                                IMarker.SEVERITY_WARNING:
                                                IMarker.SEVERITY_ERROR);
                                marker.setAttribute(IMarker.MESSAGE, problems[i].getMessage());
                                if(problems[i].getLine() >= 0) {
                                    marker.setAttribute(IMarker.LINE_NUMBER, problems[i].getLine());
                                }
                                if(problems[i].canFix()) {
                                    marker.setAttribute(IINIProblemConstants.ATTR_RESOLUTION, problems[i].getFixDescription());
                                }
                                marker.setAttribute(IDE.EDITOR_ID_ATTR,IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID);
                                monitor.worked(1);
                            }
                        }
                        try {
                            file.setPersistentProperty(IInstallOptionsConstants.RESOURCEPROPERTY_BUILD_TIMESTAMP, Long.toString(System.currentTimeMillis()));
                            monitor.worked(1);
                        }
                        catch (CoreException e) {
                            e.printStackTrace();
                        }
                    }
                    finally {
                        monitor.done();
                    }
                }
            };
            try {
                op.run(null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteMarkers(IFile file) throws CoreException
    {
        file.deleteMarkers(IInstallOptionsConstants.INSTALLOPTIONS_PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
    }

    public static void buildProject(final IProject project, final int kind, final Map<String, String> args)
    {
        cJobScheduler.scheduleJob(new ProjectJobFamily(project),
                                 InstallOptionsPlugin.getResourceString("full.build.job.name"), //$NON-NLS-1$
                                 new IJobStatusRunnable() {
                                    public IStatus run(IProgressMonitor monitor)
                                    {
                                        return internalBuildProject(project, kind, args, monitor);
                                    }
                                });
    }

    private static IStatus internalBuildProject(final IProject project, final int kind, final Map<String, String> args, IProgressMonitor monitor)
    {
        try {
            project.build(kind,INSTALLOPTIONS_BUILDER_ID,args,monitor);
            return Status.OK_STATUS;
        }
        catch (CoreException e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR,PLUGIN_ID,IStatus.OK,e.getMessage(),e);
        }
    }

    public static void buildWorkspace(final Map<?,?> args)
    {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        for (int i = 0; i < projects.length; i++) {
            if(projects[i].isOpen()) {
                checkBuildProject(projects[i]);
            }
        }
        workspace.addResourceChangeListener(new IResourceChangeListener(){
            public void resourceChanged(IResourceChangeEvent event)
            {
                if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                    IResourceDelta delta = event.getDelta();
                    // get deltas for the projects
                    IResourceDelta[] projectDeltas = delta.getAffectedChildren();
                    for (int i = 0; i < projectDeltas.length; i++) {
                        int kind = projectDeltas[i].getKind();
                        int flags = projectDeltas[i].getFlags();
                        if (kind == IResourceDelta.CHANGED && ((flags & IResourceDelta.OPEN) != 0)) {
                            IProject project = (IProject)projectDeltas[i].getResource();
                            if(project.isOpen()) {
                                checkBuildProject(project);
                            }
                            else {
                                cJobScheduler.cancelJobs(new ProjectJobFamily(project));
                            }
                        }
                    }
                }
            }
        });
    }

    private static void checkBuildProject(final IProject project)
    {
        cJobScheduler.scheduleJob(new ProjectJobFamily(project),
                    InstallOptionsPlugin.getFormattedString("building.project.format", new Object[]{project.getName()}), //$NON-NLS-1$
                    new IJobStatusRunnable() {
                        public IStatus run(final IProgressMonitor monitor)
                        {
                            try {
                                String taskName = InstallOptionsPlugin.getResourceString("build.project.task.name"); //$NON-NLS-1$
                                monitor.beginTask(taskName,1);
                                String nsisVersion = InstallOptionsModel.INSTANCE.getNSISVersion().toString();
                                if(project.hasNature(INSTALLOPTIONS_NATURE_ID)) {
                                    String buildNSISVersion = project.getPersistentProperty(PROJECTPROPERTY_NSIS_VERSION);
                                    String buildTimestamp = project.getPersistentProperty(RESOURCEPROPERTY_BUILD_TIMESTAMP);
                                    IProgressMonitor subMonitor = new NestedProgressMonitor(monitor,taskName,1);
                                    if(!checkBuilderVersion(project) || !nsisVersion.equals(buildNSISVersion) || Common.isEmpty(buildTimestamp)) {
                                        internalBuildProject(project, FULL_BUILD, null, subMonitor);
                                    }
                                    else {
                                        internalBuildProject(project, INCREMENTAL_BUILD, null, subMonitor);
                                    }
                                }
                                else {
                                    project.accept(new IResourceProxyVisitor(){
                                        private boolean found = false;
                                        public boolean visit(IResourceProxy proxy) throws CoreException
                                        {
                                            if(monitor.isCanceled()) {
                                                return false;
                                            }
                                            if(found) {
                                                return false;
                                            }
                                            if(proxy.getType() == IResource.FILE && cExtensionsSet.contains(getExtension(proxy.getName()))) {
                                                IFile file = (IFile)proxy.requestResource();
                                                String editorId = file.getPersistentProperty(IDE.EDITOR_KEY);
                                                if(editorId != null && (INSTALLOPTIONS_DESIGN_EDITOR_ID.equals(editorId) ||
                                                                        INSTALLOPTIONS_SOURCE_EDITOR_ID.equals(editorId))) {
                                                    InstallOptionsNature.addNature(file.getProject());
                                                    found = true;
                                                    return false;
                                                }
                                            }
                                            return true;
                                        }

                                    }, IContainer.EXCLUDE_DERIVED);
                                    monitor.worked(1);
                                }
                                if(monitor.isCanceled()) {
                                    return Status.CANCEL_STATUS;
                                }
                                return Status.OK_STATUS;
                            }
                            catch (CoreException e) {
                                e.printStackTrace();
                                return new Status(IStatus.ERROR,PLUGIN_ID,IStatus.OK,e.getMessage(),e);
                            }
                            finally {
                                monitor.done();
                            }
                        }
                    });
    }

    private static boolean checkBuilderVersion(IProject project) throws CoreException
    {
        String temp = project.getPersistentProperty(PROJECTPROPERTY_BUILDER_VERSION);
        Version builderVersion = (temp == null?Version.EMPTY_VERSION:new Version(temp));
        return (cBuilderVersion.compareTo(builderVersion) > 0);
    }

    private static class ProjectJobFamily
    {
        private IProject mProject;

        public ProjectJobFamily(IProject project)
        {
            mProject = project;
        }

        @Override
        public boolean equals(Object other)
        {
            if(other instanceof ProjectJobFamily) {
                return mProject.equals(((ProjectJobFamily)other).mProject);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return mProject.hashCode();
        }
    }
}
