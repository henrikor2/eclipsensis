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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;

public class AddCommand extends org.eclipse.gef.commands.Command
{
    private InstallOptionsWidget mChild;

    private InstallOptionsDialog mParent;

    private int mIndex = -1;

    public AddCommand()
    {
        super(InstallOptionsPlugin.getResourceString("add.command.name")); //$NON-NLS-1$
    }

    @Override
    public void execute()
    {
        if (mIndex < 0) {
            mParent.addChild(mChild);
        }
        else {
            mParent.addChild(mChild, mIndex);
        }
    }

    public InstallOptionsDialog getParent()
    {
        return mParent;
    }

    @Override
    public void redo()
    {
        if (mIndex < 0) {
            mParent.addChild(mChild);
        }
        else {
            mParent.addChild(mChild, mIndex);
        }
    }

    public void setChild(InstallOptionsWidget widget)
    {
        mChild = widget;
    }

    public void setIndex(int i)
    {
        mIndex = i;
    }

    public void setParent(InstallOptionsDialog newParent)
    {
        mParent = newParent;
    }

    @Override
    public void undo()
    {
        mParent.removeChild(mChild);
    }

}