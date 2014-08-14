/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.actions;

import net.sf.eclipsensis.actions.NSISAction;
import net.sf.eclipsensis.update.jobs.NSISCheckUpdateJob;

import org.eclipse.jface.action.IAction;

public class NSISUpdateAction extends NSISAction
{
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action)
    {
        new NSISCheckUpdateJob().schedule();
    }
}
