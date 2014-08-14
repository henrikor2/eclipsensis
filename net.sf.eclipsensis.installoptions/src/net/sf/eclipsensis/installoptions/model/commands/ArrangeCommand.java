/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.commands.Command;

public class ArrangeCommand extends Command
{
    private int mType;

    private InstallOptionsDialog mParent;
    private List<InstallOptionsWidget> mSelection;
    private List<InstallOptionsWidget> mOldChildren;

    public ArrangeCommand(int type)
    {
        mType = type;
        String name;
        switch(mType) {
            case IInstallOptionsConstants.ARRANGE_SEND_BACKWARD:
                name = "send.backward.command.name"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.ARRANGE_SEND_TO_BACK:
                name = "send.to.back.command.name"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_FORWARD:
                name = "bring.forward.command.name"; //$NON-NLS-1$
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_TO_FRONT:
            default:
                name = "bring.to.front.command.name"; //$NON-NLS-1$
                break;
        }
        setLabel(InstallOptionsPlugin.getResourceString(name));
    }

    @Override
    public boolean canExecute()
    {
        if(mParent != null) {
            if(!Common.isEmptyCollection(mSelection)) {
                return mParent.canMove(mType, mSelection);
            }
        }
        return false;
    }

    @Override
    public void execute()
    {
        if(mParent != null) {
            mOldChildren = new ArrayList<InstallOptionsWidget>(mParent.getChildren());
            redo();
        }
    }

    public void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
    }

    public void setSelection(List<InstallOptionsWidget> selection)
    {
        mSelection = selection;
    }


    @Override
    public void redo()
    {
        if(mParent != null) {
            mParent.setSelection(mSelection);
            mParent.move(mType);
        }
    }

    @Override
    public void undo()
    {
        if(mParent != null) {
            mParent.setChildren(mOldChildren);
            mParent.setSelection(mSelection);
        }
    }
}