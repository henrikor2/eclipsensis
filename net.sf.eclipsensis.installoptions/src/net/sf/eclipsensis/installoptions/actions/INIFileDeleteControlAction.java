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

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.jface.dialogs.*;
import org.eclipse.ui.PlatformUI;

public class INIFileDeleteControlAction extends INIFileEditControlAction
{
    public INIFileDeleteControlAction(InstallOptionsSourceEditor editor)
    {
        super(editor);
        setText(InstallOptionsPlugin.getResourceString("delete.control.action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("delete.control.action.tooltip")); //$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.disabled.icon"))); //$NON-NLS-1$)
    }

    @Override
    protected boolean doRun2(INIFile iniFile, INISection section)
    {
        boolean showOnShift = InstallOptionsPlugin.getDefault().getPreferenceStore().getBoolean(IInstallOptionsConstants.PREFERENCE_DELETE_CONTROL_WARNING);
        if(!showOnShift || WinAPI.INSTANCE.getKeyState(WinAPI.VK_SHIFT)<0) {
            MessageDialogWithToggle dialog = new MessageDialogWithToggle(
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            mEditor.getPartName(),
                            InstallOptionsPlugin.getShellImage(),
                            InstallOptionsPlugin.getFormattedString("delete.control.action.warning", new String[] {section.getName()}), //$NON-NLS-1$
                            MessageDialog.WARNING,
                            new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0,
                            InstallOptionsPlugin.getResourceString("delete.control.warning.toggle.label"), showOnShift); //$NON-NLS-1$
            dialog.open();
            if(dialog.getReturnCode() == IDialogConstants.OK_ID) {
                InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(IInstallOptionsConstants.PREFERENCE_DELETE_CONTROL_WARNING,dialog.getToggleState());
            }
            else {
                return false;
            }
        }
        iniFile.removeChild(section);
        iniFile.update(INILine.VALIDATE_FIX_ERRORS);
        updateDocument(iniFile, section);
        return true;
    }
}
