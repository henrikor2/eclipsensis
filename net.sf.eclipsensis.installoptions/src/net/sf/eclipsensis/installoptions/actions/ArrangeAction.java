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

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.ArrangeCommand;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

public class ArrangeAction extends SelectionAction
{
    static final String GROUP = "net.sf.eclipsensis.installoptions.align."; //$NON-NLS-1$
    public static final String BRING_TO_FRONT_ID = GROUP+"bring.to.front"; //$NON-NLS-1$
    public static final String BRING_FORWARD_ID = GROUP+"bring.forward"; //$NON-NLS-1$
    public static final String SEND_TO_BACK_ID = GROUP+"send.to.back"; //$NON-NLS-1$
    public static final String SEND_BACKWARD_ID = GROUP+"send.backward"; //$NON-NLS-1$

    private int mType;
    /**
     * @param part
     */
    public ArrangeAction(IWorkbenchPart part, int type)
    {
        super(part);
        mType = type;
        setLazyEnablementCalculation(false);
        initUI();
    }

    /**
     * Initializes this action's text and images.
     */
    protected void initUI()
    {
        String id;
        switch(mType) {
            case IInstallOptionsConstants.ARRANGE_SEND_BACKWARD:
                id = SEND_BACKWARD_ID;
                break;
            case IInstallOptionsConstants.ARRANGE_SEND_TO_BACK:
                id = SEND_TO_BACK_ID;
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_FORWARD:
                id = BRING_FORWARD_ID;
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_TO_FRONT:
            default:
                id = BRING_TO_FRONT_ID;
                break;
        }

        setId(id);
        String prefix = id.substring(GROUP.length());
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".tooltip")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
    }

    public Command createArrangeCommand()
    {
        ArrangeCommand command = null;
        IWorkbenchPart part = getWorkbenchPart();
        if(part instanceof InstallOptionsDesignEditor) {
            InstallOptionsDesignEditor editor = (InstallOptionsDesignEditor)part;
            command = new ArrangeCommand(mType);
            GraphicalViewer viewer = editor.getGraphicalViewer();
            command.setParent((InstallOptionsDialog)viewer.getContents().getModel());
            List<?> selection = ((IStructuredSelection)viewer.getSelection()).toList();
            List<InstallOptionsWidget> modelSelection = new ArrayList<InstallOptionsWidget>();
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                EditPart element = (EditPart)iter.next();
                if(element instanceof InstallOptionsWidgetEditPart)
                {
                    modelSelection.add((InstallOptionsWidget) element.getModel());
                }
            }
            command.setSelection(modelSelection);
        }

        return command;
    }

    @Override
    protected boolean calculateEnabled() {
        Command cmd = createArrangeCommand();
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    @Override
    public void run() {
        execute(createArrangeCommand());
    }
}
