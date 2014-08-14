/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.dialogs.InstallOptionsWidgetEditorDialog;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.installoptions.ini.*;

import org.eclipse.jface.window.Window;

public class INIFileCreateControlAction extends INIFileAction
{
    /**
     * @param editor
     */
    public INIFileCreateControlAction(InstallOptionsSourceEditor editor)
    {
        super(editor);
        setText(InstallOptionsPlugin.getResourceString("create.control.action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("create.control.action.tooltip")); //$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.control.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.control.disabled.icon"))); //$NON-NLS-1$)
    }

    @Override
    public boolean doRun(INIFile iniFile)
    {
        return doRun(iniFile, getSection(iniFile));
    }

    protected INISection getSection(INIFile iniFile)
    {
        return null;
    }

    /**
     * @param iniFile
     */
    protected boolean doRun(INIFile iniFile, INISection section)
    {
        InstallOptionsWidgetEditorDialog dialog = new InstallOptionsWidgetEditorDialog(mEditor.getSite().getShell(), iniFile, section);
        if (dialog.open() == Window.OK) {
            updateDocument(iniFile, dialog.getSection());
            return true;
        }
        return false;
    }
}