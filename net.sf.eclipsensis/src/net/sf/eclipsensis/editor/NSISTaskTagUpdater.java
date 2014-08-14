/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

public class NSISTaskTagUpdater implements INSISConstants
{
    private FileDocumentProvider mDocumentProvider = new FileDocumentProvider();

    public void updateTaskTags(IFile file)
    {
        try {
            FileEditorInput input = new FileEditorInput(file);
            mDocumentProvider.connect(input);
            IDocument document = mDocumentProvider.getDocument(input);
            NSISTextUtility.setupPartitioning(document);
            updateTaskTags(file, document);
            TextUtilities.removeDocumentPartitioners(document);
            mDocumentProvider.disconnect(input);
        }
        catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    public void updateTaskTags(IFile file, IDocument document)
    {
        try {
            file.getWorkspace().deleteMarkers(file.findMarkers(TASK_MARKER_ID,true,IResource.DEPTH_ZERO));
            ITypedRegion[] typedRegions = NSISTextUtility.getNSISPartitions(document);
            if(!Common.isEmptyArray(typedRegions)) {
                NSISRegionScanner scanner = new NSISRegionScanner(document);
                NSISTaskTagRule taskTagRule = new NSISTaskTagRule();
                for (int i = 0; i < typedRegions.length; i++) {
                    String type = typedRegions[i].getType();
                    if(NSISPartitionScanner.NSIS_MULTILINE_COMMENT.equals(type) ||
                       NSISPartitionScanner.NSIS_SINGLELINE_COMMENT.equals(type)) {
                        scanner.setRegion(typedRegions[i]);
                        LinkedHashMap<Region, IToken> map = new LinkedHashMap<Region, IToken>();
                        while(true) {
                            int offset = scanner.getOffset();
                            IToken token = taskTagRule.evaluate(scanner);
                            if(token.isUndefined()) {
                                int c = scanner.read();
                                if(c == ICharacterScanner.EOF) {
                                    break;
                                }
                            }
                            else {
                                map.put(new Region(offset,(scanner.getOffset()-offset)),token);
                            }
                        }
                        if(map.size() > 0) {
                            Region[] regions = map.keySet().toArray(new Region[map.size()]);
                            for (int j = 0; j < regions.length; j++) {
                                try {
                                    int line = document.getLineOfOffset(regions[j].getOffset());
                                    IRegion lineRegion = NSISTextUtility.intersection(typedRegions[i],document.getLineInformation(line));
                                    int start = regions[j].getOffset();
                                    int lineEnd = lineRegion.getOffset()+lineRegion.getLength();

                                    while(j < (regions.length-1) && (lineEnd > regions[j+1].getOffset())) {
                                        createTaskMarker(map, regions[j], file, document, line, start,regions[j+1].getOffset()-start);
                                        j++;
                                        start = regions[j].getOffset();
                                    }
                                    createTaskMarker(map, regions[j], file, document, line, start,lineEnd-start);
                            }
                                catch (BadLocationException e1) {
                                    EclipseNSISPlugin.getDefault().log(e1);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }


    private void createTaskMarker(Map<Region, IToken> regionMap, IRegion region, IFile file, IDocument document, int line, int start, int length) throws BadLocationException, CoreException
    {
        IToken token = regionMap.get(region);
        NSISTaskTag taskTag = (NSISTaskTag)token.getData();
        String message = document.get(start,length).trim();

        IMarker marker = file.createMarker(TASK_MARKER_ID);
        marker.setAttribute(IMarker.LINE_NUMBER,line+1);
        marker.setAttribute(IMarker.PRIORITY,taskTag.getPriority());
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.USER_EDITABLE,false);
        marker.setAttribute(IMarker.CHAR_START,start);
        marker.setAttribute(IMarker.CHAR_END,start+message.length());
    }

    public void updateTaskTags()
    {
        final String taskName = EclipseNSISPlugin.getResourceString("task.tags.job.title"); //$NON-NLS-1$
        EclipseNSISPlugin.getDefault().getJobScheduler().scheduleJob(getClass(), taskName,
            new IJobStatusRunnable() {
                public IStatus run(final IProgressMonitor monitor)
                {
                    try {
                        String mainTaskName = EclipseNSISPlugin.getResourceString("task.tags.scan.task.name"); //$NON-NLS-1$
                        monitor.beginTask(mainTaskName,2);
                        final String[] extensions = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"nsis.extensions"); //$NON-NLS-1$
                        for (int i = 0; i < extensions.length; i++) {
                            extensions[i]=extensions[i].toLowerCase();
                        }
                        final HashMap<IResource, IDocument> filesMap = new HashMap<IResource, IDocument>();
                        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
                        for (int i = 0; i < windows.length; i++) {
                            if(monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }
                            IWorkbenchPage[] pages = windows[i].getPages();
                            for (int j = 0; j < pages.length; j++) {
                                if(monitor.isCanceled()) {
                                    return Status.CANCEL_STATUS;
                                }
                                IEditorReference[] editorRefs = pages[i].getEditorReferences();
                                for (int k = 0; k < editorRefs.length; k++) {
                                    if(monitor.isCanceled()) {
                                        return Status.CANCEL_STATUS;
                                    }
                                    if(INSISConstants.EDITOR_ID.equals(editorRefs[k].getId())) {
                                        NSISEditor editor = (NSISEditor)editorRefs[k].getEditor(false);
                                        if(editor != null && !editor.isDirty()) {
                                            IEditorInput editorInput = editor.getEditorInput();
                                            if(editorInput instanceof IFileEditorInput) {
                                                IFile file = ((IFileEditorInput)editorInput).getFile();
                                                IDocument document = editor.getDocumentProvider().getDocument(editorInput);
                                                filesMap.put(file,document);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {

                            public boolean visit(IResource resource)
                            {
                                if(!monitor.isCanceled()) {
                                    if(resource instanceof IFile && !filesMap.containsKey(resource)) {
                                        String ext = resource.getFileExtension();
                                        if(!Common.isEmpty(ext)) {
                                            for (int i = 0; i < extensions.length; i++) {
                                                if(ext.toLowerCase().equals(extensions[i])) {
                                                    filesMap.put(resource,null);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    else if(resource instanceof IContainer) {
                                        return true;
                                    }
                                }
                                return false;
                            }

                        });
                        if(monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }

                        String taskName2 = EclipseNSISPlugin.getResourceString("task.tags.update.task.name"); //$NON-NLS-1$
                        NestedProgressMonitor subMonitor = new NestedProgressMonitor(monitor,mainTaskName,1);
                        try {
                            subMonitor.beginTask(taskName2,filesMap.size());
                            for (Iterator<IResource> iter = filesMap.keySet().iterator(); iter.hasNext();) {
                                if(monitor.isCanceled()) {
                                    return Status.CANCEL_STATUS;
                                }
                                IFile file = (IFile)iter.next();
                                IDocument document = filesMap.get(file);
                                subMonitor.subTask(EclipseNSISPlugin.getFormattedString("task.tags.update.file.task.name",new String[]{file.getFullPath().toString()})); //$NON-NLS-1$
                                if(document == null) {
                                    updateTaskTags(file);
                                }
                                else {
                                    updateTaskTags(file,document);
                                }
                                subMonitor.worked(1);
                            }
                        }
                        finally {
                            subMonitor.done();
                        }
                        if(monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        monitor.worked(1);
                    }
                    catch(CoreException ce) {
                        EclipseNSISPlugin.getDefault().log(ce);
                    }
                    finally {
                        monitor.done();
                    }
                    return Status.OK_STATUS;
                }
            });
    }
}
