/*******************************************************************************
 * Copyright (c) 2007-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.script.NSISScriptProblem;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class NSISFileMarkerAssistant implements INSISMarkerAssistant, INSISConstants
{
    private static final Object cJobFamily = new Object();

    private IFile mFile;

    NSISFileMarkerAssistant(IFile file)
    {
        super();
        mFile = file;
    }

    public boolean hasMarkers()
    {
        if(mFile.exists()) {
            try {
                IMarker[] markers = mFile.findMarkers(INSISConstants.PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                return !Common.isEmptyArray(markers);
            }
            catch (CoreException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void clearMarkers()
    {
        WorkspaceModifyOperation op = new WorkspaceModifyOperation(mFile)
        {
            @Override
            protected void execute(IProgressMonitor monitor)
            {
                try {
                    mFile.deleteMarkers(INSISConstants.PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                }
                catch (CoreException ex) {
                    EclipseNSISPlugin.getDefault().log(ex);
                }
            }
        };
        try {
            op.run(null);
            NSISEditorUtilities.refreshEditorOutlines(mFile);
        }
        catch (Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
        }
    }

    public void updateMarkers(NSISEditor editor, MakeNSISResults results)
    {
        updateMarkers(results);
    }

    public void updateMarkers(final MakeNSISResults results)
    {
        if (!results.isCanceled()) {
            EclipseNSISPlugin.getDefault().getJobScheduler().scheduleJob(cJobFamily,EclipseNSISPlugin.getResourceString("updating.markers.job.name"),mFile, //$NON-NLS-1$
                new IJobStatusRunnable() {
                    public IStatus run(IProgressMonitor monitor)
                    {
                        final Exception[] ex = {null};
                        WorkspaceModifyOperation op = new WorkspaceModifyOperation(mFile)
                        {
                            @Override
                            protected void execute(IProgressMonitor monitor)
                            {
                                try {
                                    List<NSISScriptProblem> problems = results.getProblems();
                                    monitor.beginTask(EclipseNSISPlugin.getResourceString("updating.problem.markers.task.name"),1+(problems==null?0:problems.size())); //$NON-NLS-1$
                                    IPath path = mFile.getFullPath();
                                    IPath loc = mFile.getLocation();
                                    if (loc == null) {
                                        throw new CoreException(new Status(IStatus.ERROR, INSISConstants.PLUGIN_ID, IStatus.ERROR, EclipseNSISPlugin.getResourceString("local.filesystem.error"), null)); //$NON-NLS-1$
                                    }
                                    IDocument document = new FileDocument(loc.toFile());

                                    mFile.deleteMarkers(PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                                    if(monitor.isCanceled()) {
                                        return;
                                    }
                                    monitor.worked(1);
                                    if (problems != null && !Common.isEmptyCollection(problems)) {
                                        for(Iterator<NSISScriptProblem> iter = problems.iterator(); iter.hasNext(); ) {
                                            if(monitor.isCanceled()) {
                                                return;
                                            }
                                            NSISScriptProblem problem = iter.next();
                                            IPath p = (IPath)problem.getPath();
                                            if (p!= null && p.equals(path)) {
                                                IMarker marker = mFile.createMarker(PROBLEM_MARKER_ID);
                                                if(monitor.isCanceled()) {
                                                    return;
                                                }

                                                switch (problem.getType())
                                                {
                                                    case NSISScriptProblem.TYPE_ERROR:
                                                        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                                                        break;
                                                    case NSISScriptProblem.TYPE_WARNING:
                                                        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                                                        break;
                                                }
                                                marker.setAttribute(IMarker.MESSAGE, problem.getText());
                                                int line = problem.getLine();
                                                marker.setAttribute(IMarker.LINE_NUMBER, line > 0?line:1);
                                                if (line > 0) {
                                                    try {
                                                        IRegion region = document.getLineInformation(line - 1);
                                                        marker.setAttribute(IMarker.CHAR_START, region.getOffset());
                                                        marker.setAttribute(IMarker.CHAR_END, region.getOffset() + region.getLength());
                                                    }
                                                    catch (BadLocationException e) {
                                                    }
                                                }
                                                problem.setMarker(marker);
                                            }
                                            monitor.worked(1);
                                        }
                                    }
                                }
                                catch (CoreException e) {
                                    ex[0] = e;
                                }
                                finally {
                                    monitor.done();
                                }
                            }
                        };
                        try {
                            op.run(monitor);
                            NSISEditorUtilities.refreshEditorOutlines(mFile);
                        }
                        catch (Exception e) {
                            ex[0]= e;
                        }
                        return monitor.isCanceled()?Status.CANCEL_STATUS:(ex[0]==null?Status.OK_STATUS:new Status(IStatus.ERROR,PLUGIN_ID,IStatus.OK,ex[0].getMessage(),ex[0]));
                    }
                }
            );
        }
    }
}
