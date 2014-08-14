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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;

public class NSISCancelAction extends NSISScriptAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        try {
            MakeNSISRunner.cancel();
        }
        catch(Exception ex)
        {
            EclipseNSISPlugin.getDefault().log(ex);
        }
    }

    @Override
    protected void started(IPath script)
    {
        if(mAction != null && !mAction.isEnabled()) {
            mAction.setEnabled(true);
        }
    }

    @Override
    protected void stopped(IPath script, MakeNSISResults results)
    {
        if(mAction != null && mAction.isEnabled()) {
            mAction.setEnabled(false);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.actions.NSISAction#isEnabled()
     */
    @Override
    public boolean isEnabled()
    {
        if(super.isEnabled()) {
            return MakeNSISRunner.isCompiling();
        }
        return false;
    }
}