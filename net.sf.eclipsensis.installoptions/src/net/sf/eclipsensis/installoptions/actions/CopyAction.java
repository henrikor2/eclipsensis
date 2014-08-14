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
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.commands.CopyCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;

public class CopyAction extends SelectionAction
{
    /**
     * @param part
     */
    public CopyAction(IWorkbenchPart part)
    {
        super(part);
        setLazyEnablementCalculation(false);
    }

    /**
     * Initializes this action's text and images.
     */
    @Override
    protected void init()
    {
        super.init();
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        setText(InstallOptionsPlugin.getResourceString("copy.action.label")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("copy.action.tooltip")); //$NON-NLS-1$
        setId(ActionFactory.COPY.getId());
        setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        setEnabled(false);
    }

    public Command createCopyCommand(List<?> objects) {
        if (objects.isEmpty()) {
            return null;
        }

        CopyCommand copyCommand = new CopyCommand();
        for (Iterator<?> iter = objects.iterator(); iter.hasNext();) {
            Object object = iter.next();
            if(object instanceof InstallOptionsWidgetEditPart) {
                copyCommand.addWidget((InstallOptionsWidget)((InstallOptionsWidgetEditPart)object).getModel());
            }
            else {
                return null;
            }
        }

        return copyCommand;
    }

    @Override
    protected boolean calculateEnabled() {
        Command cmd = createCopyCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    @Override
    public void run() {
        createCopyCommand(getSelectedObjects()).execute();
    }
}
