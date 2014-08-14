/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.util.List;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.TreeContainerEditPolicy;
import org.eclipse.gef.requests.*;

public class InstallOptionsTreeContainerEditPolicy extends TreeContainerEditPolicy
{
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.TreeContainerEditPolicy#getAddCommand(org.eclipse.gef.requests.ChangeBoundsRequest)
     */
    @Override
    protected Command getAddCommand(ChangeBoundsRequest request)
    {
        return null;
    }

    protected Command createCreateCommand(InstallOptionsWidget child, Rectangle r, int index, String label)
    {
        CreateCommand cmd = new CreateCommand();
        Rectangle rect;
        if (r == null) {
            rect = new Rectangle();
            rect.setSize(new Dimension(-1, -1));
        }
        else {
            rect = r;
        }
        cmd.setLocation(rect);
        cmd.setParent((InstallOptionsDialog)getHost().getModel());
        cmd.setChild(child);
        cmd.setLabel(label);
        if (index >= 0) {
            cmd.setIndex(index);
        }
        return cmd;
    }

    @Override
    protected Command getCreateCommand(CreateRequest request)
    {
        InstallOptionsWidget child = (InstallOptionsWidget)request.getNewObject();
        int index = findIndexOfTreeItemAt(request.getLocation());
        return createCreateCommand(child, null, index, "Create InstallOptionsWidget");//$NON-NLS-1$
    }

    @Override
    protected Command getMoveChildrenCommand(ChangeBoundsRequest request)
    {
        CompoundCommand command = new CompoundCommand();
        List<?> editparts = request.getEditParts();
        List<?> children = getHost().getChildren();
        int newIndex = findIndexOfTreeItemAt(request.getLocation());
        if(newIndex < 0) {
            newIndex = children.size();
        }

        for (int i = 0; i < editparts.size(); i++) {
            EditPart child = (EditPart)editparts.get(i);
            int tempIndex = newIndex;
            int oldIndex = children.indexOf(child);
            if (oldIndex == tempIndex || oldIndex + 1 == tempIndex) {
                command.add(UnexecutableCommand.INSTANCE);
                return command;
            }
            else if (oldIndex < tempIndex) {
                tempIndex--;
            }
            command.add(new ReorderPartCommand((InstallOptionsWidget)child.getModel(),
                    (InstallOptionsDialog)getHost().getModel(), oldIndex, tempIndex));
        }
        return command;
    }
}
