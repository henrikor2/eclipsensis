/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

public class CompoundNSISConsole implements INSISConsole
{
    private INSISConsole[] mChildren;

    public CompoundNSISConsole(INSISConsole[] children)
    {
        super();
        mChildren = children;
    }

    public void appendLine(NSISConsoleLine line)
    {
        if(!Common.isEmptyArray(mChildren)) {
            for (int i = 0; i < mChildren.length; i++) {
                try {
                    mChildren[i].appendLine(line);
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    public void clearConsole()
    {
        for (int i = 0; i < mChildren.length; i++) {
            try {
                mChildren[i].clearConsole();
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

}
