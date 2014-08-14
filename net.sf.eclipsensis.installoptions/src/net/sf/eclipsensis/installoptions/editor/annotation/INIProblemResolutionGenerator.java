/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.annotation;

import java.util.*;

import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class INIProblemResolutionGenerator implements IMarkerResolutionGenerator2
{
    private static final IMarkerResolution[] EMPTY_RESOLUTION = new IMarkerResolution[0];
    public IMarkerResolution[] getResolutions(IMarker marker)
    {
        try {
            final String resolution = (String)marker.getAttribute(IINIProblemConstants.ATTR_RESOLUTION);
            final int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
            if(line >= 0 && resolution != null) {
                final IResource resource = marker.getResource();
                if(resource instanceof IFile) {
                    List<IEditorPart> editors = NSISEditorUtilities.findEditors(resource.getLocation());
                    IInstallOptionsEditor editor = null;
                    if(!Common.isEmptyCollection(editors)) {
                        for (Iterator<IEditorPart> iter = editors.iterator(); iter.hasNext();) {
                            IEditorPart element = iter.next();
                            if(element instanceof IInstallOptionsEditor) {
                                editor = (IInstallOptionsEditor)element;
                                break;
                            }
                        }
                    }
                    if(editor == null) {
                        return new IMarkerResolution[] {
                                new IMarkerResolution() {
                                    public String getLabel()
                                    {
                                        return resolution;
                                    }

                                    public void run(IMarker marker)
                                    {
                                        IInstallOptionsEditor editor;
                                        try {
                                            editor = (IInstallOptionsEditor)IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),(IFile)resource,
                                                                        IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID, true);
                                        }
                                        catch (PartInitException e) {
                                            InstallOptionsPlugin.getDefault().log(e);
                                            editor = null;
                                        }
                                        if(editor != null) {
                                            IEditorInput input = editor.getEditorInput();
                                            if(input instanceof InstallOptionsEditorInput) {
                                                IDocumentProvider provider = ((InstallOptionsEditorInput)input).getDocumentProvider();
                                                IDocument document = provider.getDocument(input);
                                                INIFile inifile = editor.getINIFile();
                                                List<INIProblem> problems = inifile.getProblems(line > 0);
                                                for (Iterator<INIProblem> iter = problems.iterator(); iter.hasNext();) {
                                                    INIProblem problem = iter.next();
                                                    if (problem.getLine() == line && Common.stringsAreEqual(problem.getFixDescription(),resolution)) {
                                                        problem.fix(document);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        };
                    }
                    else {
                        IEditorInput input = editor.getEditorInput();
                        if(input instanceof InstallOptionsEditorInput) {
                            IDocumentProvider provider = ((InstallOptionsEditorInput)input).getDocumentProvider();
                            final IDocument document = provider.getDocument(input);
                            INIFile inifile = editor.getINIFile();
                            List<INIProblem> problems = inifile.getProblems(line > 0);
                            for (Iterator<INIProblem> iter = problems.iterator(); iter.hasNext();) {
                                final INIProblem problem = iter.next();
                                if (problem.getLine() == line && Common.stringsAreEqual(problem.getFixDescription(),resolution)) {
                                    final IEditorPart fEditor = editor;
                                    return new IMarkerResolution[] {
                                            new IMarkerResolution() {
                                                public String getLabel()
                                                {
                                                    return resolution;
                                                }

                                                public void run(IMarker marker)
                                                {
                                                    fEditor.getEditorSite().getPage().activate(fEditor);
                                                    problem.fix(document);
                                                }
                                            }
                                    };
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
        return EMPTY_RESOLUTION;
    }

    public boolean hasResolutions(IMarker marker)
    {
        try {
            return !Common.isEmpty((String)marker.getAttribute(IINIProblemConstants.ATTR_RESOLUTION));
        }
        catch (CoreException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return false;
        }
    }
}
