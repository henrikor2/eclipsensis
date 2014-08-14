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

import java.io.File;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

public class InstallOptionsAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory
{
    private IEditorRegistry mEditorRegistry;

    public InstallOptionsAnnotationModelFactory()
    {
        IWorkbench workbench= PlatformUI.getWorkbench();
        mEditorRegistry = workbench.getEditorRegistry();
    }

    @Override
    public IAnnotationModel createAnnotationModel(IPath location)
    {
        String editorId = null;
        try {
            //First see if this is a workbench file
            IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
            editorId = file.getPersistentProperty(IDE.EDITOR_KEY);
        }
        catch(Exception e) {
            try {
                File f = new File(location.toOSString());
                if(f.exists()) {
                    //This is an external file. Check the default editor for it.
                    IEditorDescriptor descriptor= mEditorRegistry.getDefaultEditor(f.getName());
                    if (descriptor != null) {
                        editorId = descriptor.getId();
                    }
                }
            }
            catch(Exception ex) {
                editorId = null;
            }
        }
        if(IInstallOptionsConstants.INSTALLOPTIONS_DESIGN_EDITOR_ID.equals(editorId) ||
           IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID.equals(editorId)) {
            return new AnnotationModel();
        }
        return super.createAnnotationModel(location);
    }
}
