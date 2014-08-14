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
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;

import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

public class RevertToSavedAction extends EditorPartAction
{
    public RevertToSavedAction(IEditorPart editor)
    {
        super(editor);
        setLazyEnablementCalculation(false);
    }

    @Override
    protected boolean calculateEnabled()
    {
        return getEditorPart().isDirty();
    }

    @Override
    protected void init()
    {
        setText(InstallOptionsPlugin.getResourceString("revert.action.label")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("revert.action.tooltip")); //$NON-NLS-1$
        setId(ActionFactory.REVERT.getId());
    }

    @Override
    public void run()
    {
        ((InstallOptionsDesignEditor)getEditorPart()).doRevertToSaved();
    }
}
