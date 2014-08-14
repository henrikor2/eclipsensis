/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import net.sf.eclipsensis.EclipseNSISPlugin;

public class MakeNSISProcess
{
    private boolean mCanceled = false;
    private Process mProcess = null;

    MakeNSISProcess(Process process)
    {
        mProcess = process;
    }

    public Process getProcess()
    {
        return mProcess;
    }

    public synchronized void cancel()
    {
        try {
            mProcess.destroy();
            mCanceled = true;
        }
        catch (Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
        }
    }

    public synchronized boolean isCanceled()
    {
        return mCanceled;
    }
}
