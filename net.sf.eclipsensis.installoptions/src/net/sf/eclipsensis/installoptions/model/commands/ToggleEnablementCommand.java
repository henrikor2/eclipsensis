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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.commands.Command;

public class ToggleEnablementCommand extends Command
{
    private boolean mShouldEnable;
    private InstallOptionsWidget[] mWidgets;

    public ToggleEnablementCommand(InstallOptionsWidget[] widgets, boolean shouldEnable)
    {
        mWidgets = widgets;
        mShouldEnable = shouldEnable;
        setLabel(InstallOptionsPlugin.getResourceString((mShouldEnable?"enable.command.name":"disable.command.name"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void execute()
    {
        setEnablement(mShouldEnable);
    }

    private void setEnablement(boolean shouldEnable)
    {
        for (int i = 0; i < mWidgets.length; i++) {
            List<String> flags = new ArrayList<String>(mWidgets[i].getFlags());
            if(shouldEnable) {
                flags.remove(InstallOptionsModel.FLAGS_DISABLED);
            }
            else {
                if(!flags.contains(InstallOptionsModel.FLAGS_DISABLED)) {
                    flags.add(InstallOptionsModel.FLAGS_DISABLED);
                }
            }
            mWidgets[i].setFlags(flags);
        }
    }

    @Override
    public void undo()
    {
        setEnablement(!mShouldEnable);
    }
}
