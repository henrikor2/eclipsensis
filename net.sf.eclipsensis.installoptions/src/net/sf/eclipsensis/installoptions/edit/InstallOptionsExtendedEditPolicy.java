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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;

public abstract class InstallOptionsExtendedEditPolicy extends GraphicalEditPolicy
{
    public static final String ROLE = "ExtendedEditPolicy"; //$NON-NLS-1$
    private EditPart mEditPart;

    public InstallOptionsExtendedEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }


    @Override
    public Command getCommand(Request request)
    {
        if (IInstallOptionsConstants.REQ_EXTENDED_EDIT.equals(request.getType())) {
            return getExtendedEditCommand((ExtendedEditRequest)request);
        }
        else if (RequestConstants.REQ_OPEN.equals(request.getType())) {
            return getExtendedEditCommand(new ExtendedEditRequest(mEditPart));
        }
        else {
            return super.getCommand(request);
        }
    }

    @Override
    public boolean understandsRequest(Request request)
    {
        if (IInstallOptionsConstants.REQ_EXTENDED_EDIT.equals(request.getType())||
            RequestConstants.REQ_OPEN.equals(request.getType())) {
            InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(((InstallOptionsWidget)mEditPart.getModel()).getType());
            return (typeDef != null && typeDef.getSettings().contains(getExtendedEditProperty()));
        }
        return super.understandsRequest(request);
    }


    protected abstract String getExtendedEditProperty();
    protected abstract Command getExtendedEditCommand(ExtendedEditRequest request);
}
