/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.help;

import java.io.File;

import net.sf.eclipsensis.help.NSISLocalFileHandler;
import net.sf.eclipsensis.installoptions.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ide.IDE;

public class InstallOptionsLocalFileHandler extends NSISLocalFileHandler
{
    @Override
    protected String getEditorID(File file)
    {
        return IInstallOptionsConstants.INSTALLOPTIONS_DESIGN_EDITOR_ID;
    }

    @Override
    protected String getEditorID(IFile file)
    {
        try {
            String id = file.getPersistentProperty(IDE.EDITOR_KEY);
            if(id.equals(IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID)) {
                return id;
            }
        }
        catch (CoreException e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
        return IInstallOptionsConstants.INSTALLOPTIONS_DESIGN_EDITOR_ID;
    }
}
