/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.link;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.ModifyTxtColorCommand;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.RGB;

public class InstallOptionsLinkExtendedEditPolicy extends InstallOptionsExtendedEditPolicy
{
    public InstallOptionsLinkExtendedEditPolicy(EditPart editPart)
    {
        super(editPart);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.InstallOptionsExtendedEditPolicy#getExtendedEditCommand(net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest)
     */
    @Override
    protected Command getExtendedEditCommand(ExtendedEditRequest request)
    {
        ModifyTxtColorCommand command = new ModifyTxtColorCommand((InstallOptionsLink)request.getEditPart().getModel(),
                                                                              (RGB)request.getNewValue());
        return command;
    }

    @Override
    protected String getExtendedEditProperty()
    {
        return InstallOptionsModel.PROPERTY_TXTCOLOR;
    }
}
