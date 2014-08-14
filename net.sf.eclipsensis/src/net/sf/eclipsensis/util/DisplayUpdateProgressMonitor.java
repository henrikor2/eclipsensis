/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;

public class DisplayUpdateProgressMonitor implements IProgressMonitor, IProgressMonitorWithBlocking
{
    private IProgressMonitor mMonitor;

    public DisplayUpdateProgressMonitor(IProgressMonitor monitor)
    {
        mMonitor = monitor;
    }

    private void updateDisplay()
    {
        if(Display.getCurrent() != null) {
            Display.getCurrent().update();
        }
    }

    public void beginTask(String name, int totalWork)
    {
        mMonitor.beginTask(name, totalWork);
        updateDisplay();
    }

    public void done()
    {
        mMonitor.done();
        updateDisplay();
    }

    public void internalWorked(double work)
    {
        mMonitor.internalWorked(work);
        updateDisplay();
    }

    public boolean isCanceled()
    {
        return mMonitor.isCanceled();
    }

    public void setCanceled(boolean value)
    {
        mMonitor.setCanceled(value);
        updateDisplay();
    }

    public void setTaskName(String name)
    {
        mMonitor.setTaskName(name);
        updateDisplay();
    }

    public void subTask(String name)
    {
        mMonitor.subTask(name);
        updateDisplay();
    }

    public void worked(int work)
    {
        mMonitor.worked(work);
        updateDisplay();
    }

    public void setBlocked(IStatus reason)
    {
        if(mMonitor instanceof IProgressMonitorWithBlocking) {
            ((IProgressMonitorWithBlocking)mMonitor).setBlocked(reason);
            updateDisplay();
        }
    }

    public void clearBlocked()
    {
        if(mMonitor instanceof IProgressMonitorWithBlocking) {
            ((IProgressMonitorWithBlocking)mMonitor).clearBlocked();
            updateDisplay();
        }
    }
}
