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

import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

public class ExtendedEditAction extends SelectionAction
{
    public static final String ID = "net.sf.eclipsensis.installoptions.extended_edit"; //$NON-NLS-1$
    private ExtendedEditRequest mRequest = new ExtendedEditRequest();

    public ExtendedEditAction(IWorkbenchPart part) {
        super(part);
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().size() == 1 && (getSelectedObjects().get(0) instanceof EditPart)) {
            EditPart part = (EditPart)getSelectedObjects().get(0);
            return part.understandsRequest(mRequest);
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public void run() {
        try {
            EditPart part = (EditPart)getSelectedObjects().get(0);
            mRequest.setEditPart(part);
            part.performRequest(mRequest);
        }
        catch (ClassCastException e) {
            Display.getCurrent().beep();
        }
        catch (IndexOutOfBoundsException e) {
            Display.getCurrent().beep();
        }
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
     */
    @Override
    protected void init() {
        super.init();
        setId(ID);
    }


}
