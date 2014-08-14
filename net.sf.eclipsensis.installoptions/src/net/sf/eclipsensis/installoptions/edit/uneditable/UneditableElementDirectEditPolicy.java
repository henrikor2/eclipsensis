/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.uneditable;

import net.sf.eclipsensis.installoptions.model.InstallOptionsUneditableElement;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsUneditableElementCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

public class UneditableElementDirectEditPolicy extends DirectEditPolicy
{
    @Override
    protected Command getDirectEditCommand(DirectEditRequest edit)
    {
        String text = getDirectEditValue(edit);
        InstallOptionsUneditableElementEditPart control = (InstallOptionsUneditableElementEditPart)getHost();
        InstallOptionsUneditableElementCommand command = new InstallOptionsUneditableElementCommand((InstallOptionsUneditableElement)control.getModel(),text);
        return command;
    }

    /**
     * @param edit
     * @return
     */
    protected String getDirectEditValue(DirectEditRequest edit)
    {
        return (String)edit.getCellEditor().getValue();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.DirectEditPolicy#showCurrentEditValue(org.eclipse.gef.requests.DirectEditRequest)
     */
    @Override
    protected void showCurrentEditValue(DirectEditRequest request)
    {
    }
}
