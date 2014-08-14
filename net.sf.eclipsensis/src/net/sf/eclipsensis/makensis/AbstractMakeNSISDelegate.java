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

abstract class AbstractMakeNSISDelegate implements IMakeNSISDelegate
{
    private String mHwndLock = "lock"; //$NON-NLS-1$
    private long mHwnd = 0;

    public void startup()
    {
        synchronized(mHwndLock) {
            if(mHwnd <= 0) {
                mHwnd = init();
            }
        }
    }

    public void shutdown()
    {
        synchronized(mHwndLock) {
            if(mHwnd > 0) {
                destroy();
                mHwnd = 0;
            }
        }
    }

    public long getHwnd()
    {
        return mHwnd;
    }

    protected abstract long init();

    protected abstract void destroy();
}
