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
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.DialogSize;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IUpdate;

public class SetDialogSizeAction extends Action implements IUpdate
{
    private IEditorPart mEditor = null;
    private DialogSize mDialogSize = null;
    /**
     * @param text
     */
    public SetDialogSizeAction(DialogSize dialogSize)
    {
        super((dialogSize==null?InstallOptionsPlugin.getResourceString("empty.dialog.size.name"):InstallOptionsPlugin.getFormattedString("set.dialog.size.action.name.format",  //$NON-NLS-1$ //$NON-NLS-2$
                                                    new Object[]{dialogSize.getName(),
                                                                 new Integer(dialogSize.getSize().width),
                                                                 new Integer(dialogSize.getSize().height)})),AS_CHECK_BOX);
        mDialogSize = dialogSize;
    }

    public void setEditor(IEditorPart editor)
    {
        mEditor = editor;
    }

    @Override
    public void run()
    {
        if(isEnabled()) {
            ((InstallOptionsDesignEditor)mEditor).getGraphicalViewer().setProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE,mDialogSize.getCopy());
        }
    }

    @Override
    public boolean isEnabled()
    {
        if (mDialogSize != null && mEditor instanceof InstallOptionsDesignEditor &&
                !((InstallOptionsDesignEditor)mEditor).isDisposed() && ((InstallOptionsDesignEditor)mEditor).getGraphicalViewer() != null) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    public void update()
    {
        super.setEnabled(isEnabled());
        if (mDialogSize != null && mEditor instanceof InstallOptionsDesignEditor &&
                !((InstallOptionsDesignEditor)mEditor).isDisposed() && ((InstallOptionsDesignEditor)mEditor).getGraphicalViewer() != null) {
            DialogSize d = (DialogSize)((InstallOptionsDesignEditor)mEditor).getGraphicalViewer().getProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE);
            setChecked(mDialogSize.equals(d));
        }
    }
}
