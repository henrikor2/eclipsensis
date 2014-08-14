/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.File;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISExternalFileEditorInput;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class NSISLocalFileHandler implements IHelpBrowserLocalFileHandler
{
    public boolean handle(final File file)
    {
        final IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI());
        final boolean[] result = {true};
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    if(Common.isEmptyArray(files)) {
                        IDE.openEditor(page, createExternalFileEditorInput(file), getEditorID(file));
                    }
                    else {
                        IDE.openEditor(page, new FileEditorInput(files[0]), getEditorID(files[0]));
                    }
                    page.getWorkbenchWindow().getShell().forceActive();
                    result[0] = true;
                }
                catch (PartInitException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    result[0] = false;
                }
            }
        });
        return result[0];
    }

    protected String getEditorID(IFile file)
    {
        return INSISConstants.EDITOR_ID;
    }

    protected String getEditorID(File file)
    {
        return INSISConstants.EDITOR_ID;
    }

    protected NSISExternalFileEditorInput createExternalFileEditorInput(final File file)
    {
        return new NSISExternalFileEditorInput(file);
    }
}
