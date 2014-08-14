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
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.gef.commands.Command;

public class ToggleLockCommand extends Command
{
    private boolean mShouldLock;
    private InstallOptionsWidget[] mWidgets;

    public ToggleLockCommand(InstallOptionsWidget[] widgets, boolean shouldLock)
    {
        mWidgets = widgets;
        mShouldLock = shouldLock;
        setLabel(InstallOptionsPlugin.getResourceString((mShouldLock?"lock.command.name":"unlock.command.name"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void execute()
    {
        setLocked(mShouldLock);
    }

    private void setLocked(boolean shouldLock)
    {
        for (int i = 0; i < mWidgets.length; i++) {
            mWidgets[i].setLocked(shouldLock);
        }
    }

    @Override
    public void undo()
    {
        setLocked(!mShouldLock);
    }
}
