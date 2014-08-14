/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.ReorderPartCommand;
import net.sf.eclipsensis.installoptions.requests.ReorderPartRequest;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

public class InstallOptionsDialogEditPolicy extends ContainerEditPolicy implements IInstallOptionsConstants
{
    @Override
    protected Command getCreateCommand(CreateRequest request)
    {
        return null;
    }

    @Override
    public Command getCommand(Request request)
    {
        if(REQ_REORDER_PART.equals(request.getType())) {
            return getReorderPartCommand((ReorderPartRequest)request);
        }
        else {
            return super.getCommand(request);
        }
    }

    protected Command getReorderPartCommand(ReorderPartRequest request)
    {
        EditPart editpart = request.getEditPart();
        List<?> children = getHost().getChildren();
        int newIndex = request.getNewIndex();
        //Below is because children order is reversed in this edit part
        int oldIndex = (children.size()-1)-children.indexOf(editpart);
        if (oldIndex == newIndex) {
            return UnexecutableCommand.INSTANCE;
        }
        return new ReorderPartCommand((InstallOptionsWidget)editpart.getModel(),
                (InstallOptionsDialog)getHost().getModel(), oldIndex, newIndex);
    }
}
