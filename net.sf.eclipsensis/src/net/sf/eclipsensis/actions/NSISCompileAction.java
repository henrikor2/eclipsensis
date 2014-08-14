/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.NSISCompileTestUtility;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class NSISCompileAction extends NSISScriptAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    final public void run(IAction action) {
        if(mPlugin != null && NSISPreferences.getInstance().getNSISHome() != null) {
            action.setEnabled(false);
            if(!NSISCompileTestUtility.INSTANCE.compile(getInput(), shouldTest())) {
                mAction.setEnabled(isEnabled());
            }
        }
    }

    @Override
    protected void started(IPath script)
    {
        if(mAction != null && mAction.isEnabled()) {
            mAction.setEnabled(false);
        }
    }

    @Override
    protected void stopped(IPath script, MakeNSISResults results)
    {
        if(mAction != null && !mAction.isEnabled()) {
            mAction.setEnabled(true);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            return !MakeNSISRunner.isCompiling();
        }
        return false;
    }

    protected boolean shouldTest()
    {
        return false;
    }
}