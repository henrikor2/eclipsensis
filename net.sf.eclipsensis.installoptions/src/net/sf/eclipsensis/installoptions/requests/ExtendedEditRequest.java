/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.requests;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.LocationRequest;

public class ExtendedEditRequest extends LocationRequest
{
    private EditPart mEditPart;
    private Object mNewValue;

    public ExtendedEditRequest()
    {
        super(IInstallOptionsConstants.REQ_EXTENDED_EDIT);
    }

    public ExtendedEditRequest(EditPart editPart)
    {
        this();
        setEditPart(editPart);
    }

    public EditPart getEditPart()
    {
        return mEditPart;
    }

    public void setEditPart(EditPart editPart)
    {
        mEditPart = editPart;
    }

    public Object getNewValue()
    {
        return mNewValue;
    }

    public void setNewValue(Object newValue)
    {
        mNewValue = newValue;
    }
}
