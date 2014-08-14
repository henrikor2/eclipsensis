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

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities.PositionMarker;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.script.NSISScriptProblem;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

public class NSISExternalFileMarkerAssistant implements INSISMarkerAssistant
{
    private File mFile;

    NSISExternalFileMarkerAssistant(File file)
    {
        super();
        mFile = file;
    }

    public boolean hasMarkers()
    {
        MakeNSISResults results = NSISCompileTestUtility.INSTANCE.getCachedResults(mFile);
        if(results != null) {
            return results.getProblems().size() > 0;
        }
        return false;
    }

    public void clearMarkers()
    {
        if(IOUtility.isValidFile(mFile)) {
            NSISCompileTestUtility.INSTANCE.removeCachedResults(mFile);
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    updateMarkers(null);
                    NSISEditorUtilities.refreshEditorOutlines(new Path(mFile.getAbsolutePath()));
                }
            });
        }
    }

    public void updateMarkers(MakeNSISResults results)
    {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IEditorReference[] editorRefs = pages[j].getEditorReferences();
                for (int k = 0; k < editorRefs.length; k++) {
                    if(INSISConstants.EDITOR_ID.equals(editorRefs[k].getId())) {
                        NSISEditor editor = (NSISEditor)editorRefs[k].getEditor(false);
                        if(editor != null) {
                            IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(editor);
                            if(!(input instanceof IFileEditorInput)) {
                                if(mFile.getAbsolutePath().equalsIgnoreCase(input.getPath().toOSString())) {
                                    updateMarkers(editor, results);
                                    editor.updateActionsState();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateMarkers(final NSISEditor editor, MakeNSISResults results)
    {
        IAnnotationModel model = editor.getAnnotationModel();
        if(model instanceof AnnotationModel) {
            AnnotationModel annotationModel = (AnnotationModel)model;
            annotationModel.removeAllAnnotations();
            if (results != null) {
                List<NSISScriptProblem> problems = results.getProblems();
                if (!Common.isEmptyCollection(problems)) {
                    IEditorInput editorInput = editor.getEditorInput();
                    IFile file = null;
                    if(editorInput instanceof IFileEditorInput) {
                        file = ((IFileEditorInput)editorInput).getFile();
                    }
                    IDocument doc = editor.getDocumentProvider().getDocument(editorInput);
                    for (Iterator<NSISScriptProblem> iter = problems.iterator(); iter.hasNext();) {
                        NSISScriptProblem problem = iter.next();
                        int line = problem.getLine();
                        if (line >= 0) {
                            try {
                                String name;
                                int severity;
                                if (problem.getType() == NSISScriptProblem.TYPE_ERROR) {
                                    name = INSISConstants.ERROR_ANNOTATION_NAME;
                                    severity = IMarker.SEVERITY_ERROR;
                                }
                                else if (problem.getType() == NSISScriptProblem.TYPE_WARNING) {
                                    name = INSISConstants.WARNING_ANNOTATION_NAME;
                                    severity = IMarker.SEVERITY_WARNING;
                                }
                                else {
                                    continue;
                                }
                                IRegion region = doc.getLineInformation(line > 0?line - 1:1);

                                Position position = new Position(region.getOffset(), (line > 0?region.getLength():0));
                                problem.setMarker(new PositionMarker(file,severity,problem.getText(),position));
                                annotationModel.addAnnotation(new Annotation(name, false, problem.getText()), position);
                            }
                            catch (BadLocationException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                    }
                }
            }
        }
        NSISEditorUtilities.refreshOutline(editor);
    }
}
