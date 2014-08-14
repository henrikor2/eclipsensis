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
import net.sf.eclipsensis.installoptions.editor.IInstallOptionsEditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

public class SwitchEditorAction extends Action
{
    public static final String ID = "net.sf.eclipsensis.installoptions.switch_editor"; //$NON-NLS-1$

    private IInstallOptionsEditor mEditor;
    private String mSwitchToEditorId;

    public SwitchEditorAction(IInstallOptionsEditor editor, String switchToEditorId, String text)
    {
        super(text);
        mEditor = editor;
        mSwitchToEditorId = switchToEditorId;
        setId(ID);
        setToolTipText(text);
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.icon"))); //$NON-NLS-1$
        setActionDefinitionId(IInstallOptionsConstants.SWITCH_EDITOR_COMMAND_ID);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("switch.editor.disabled.icon"))); //$NON-NLS-1$
    }

    @Override
    public void run()
    {
        if(mEditor.canSwitch()) {
            BusyIndicator.showWhile(null,new Runnable(){
                public void run()
                {
                    IWorkbenchPage page = mEditor.getSite().getPage();
                    IEditorInput input = mEditor.getEditorInput();
                    mEditor.prepareForSwitch();
                    page.closeEditor(mEditor,false);
                    try {
                        page.openEditor(input,mSwitchToEditorId);
                        if(input instanceof IFileEditorInput) {
                            try {
                                ((IFileEditorInput)input).getFile().setPersistentProperty(IDE.EDITOR_KEY,mSwitchToEditorId);
                            }
                            catch (CoreException e1) {
                                InstallOptionsPlugin.getDefault().log(e1);
                            }
                        }
                    }
                    catch (PartInitException e) {
                        InstallOptionsPlugin.getDefault().log(e);
                    }
                }
            });
        }
    }
}