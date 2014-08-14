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

import java.io.File;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.*;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModelFactory;

public class NSISAnnotationModelFactory extends ResourceMarkerAnnotationModelFactory
{
    @Override
    public IAnnotationModel createAnnotationModel(IPath location)
    {
        //First see if this is a workbench file
        IFile file= FileBuffers.getWorkspaceFileAtLocation(location);
        if(file == null) {
            try {
                File f = new File(location.toOSString());
                if(f.exists()) {
                    //This is an external file. Return a generic annotation model.
                    return new AnnotationModel();
                }
            }
            catch(Exception ex) {
            }
        }
        return super.createAnnotationModel(location);
    }
}
