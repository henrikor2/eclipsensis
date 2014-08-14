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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.CutCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;

public class CutAction extends SelectionAction
{
    /**
     * @param part
     */
    public CutAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(true);
    }

    /**
     * Initializes this action's text and images.
     */
    @Override
    protected void init()
    {
        super.init();
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText(InstallOptionsPlugin.getResourceString("cut.action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("cut.action.tooltip")); //$NON-NLS-1$
        setId(ActionFactory.CUT.getId());
        setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        setEnabled(false);
    }

    public Command createCutCommand(List<?> objects) {
        if (objects.isEmpty()) {
            return null;
        }

        CutCommand cutCommand = null;
        InstallOptionsDialog dialog = (InstallOptionsDialog)getWorkbenchPart().getAdapter(InstallOptionsDialog.class);
        if(dialog != null) {
            cutCommand = new CutCommand();
            cutCommand.setParent(dialog);
            //cutCommand.setParent(objects.get(0));
            for (Iterator<?> iter = objects.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if(object instanceof InstallOptionsWidgetEditPart) {
                    cutCommand.addWidget((InstallOptionsWidget)((InstallOptionsWidgetEditPart)object).getModel());
                }
                else {
                    return null;
                }
            }
        }

        return cutCommand;
    }

    @Override
    protected boolean calculateEnabled() {
        Command cmd = createCutCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    @Override
    public void run() {
        execute(createCutCommand(getSelectedObjects()));
    }
}
