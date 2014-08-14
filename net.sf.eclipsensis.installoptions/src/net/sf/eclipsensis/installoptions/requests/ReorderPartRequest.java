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

import org.eclipse.gef.*;

public class ReorderPartRequest extends Request implements IInstallOptionsConstants
{
    private EditPart mEditPart;
    private int mNewIndex;

    /**
     *
     */
    public ReorderPartRequest(EditPart editPart, int newIndex)
    {
        super(REQ_REORDER_PART);
        mEditPart = editPart;
        mNewIndex = newIndex;
    }

    public EditPart getEditPart()
    {
        return mEditPart;
    }

    public int getNewIndex()
    {
        return mNewIndex;
    }
}
