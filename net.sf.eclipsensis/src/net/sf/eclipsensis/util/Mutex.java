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

public class Mutex
{
    private static int cCounter = 0;
    private Object mOwner = null;
    private int mLockCount = 0;
    private final int mId = newId();

    public int getId()
    {
        return mId;
    }

    static int newId()
    {
        synchronized(Mutex.class) {
            cCounter++;
            return cCounter;
        }
    }

    protected Object getOwner()
    {
        return mOwner;
    }

    /**
     * Acquire the mutex. The mutex can be acquired multiple times
     * by the same thread, provided that it is released as many
     * times as it is acquired. The calling thread blocks until
     * it has acquired the mutex. (There is no timeout).
     *
     * @see release
     * @see acquireWithoutBlocking
     */
    public synchronized void acquire(Object owner, long timeout) throws InterruptedException
    {
        while( !acquireWithoutBlocking(owner)) {
            wait(timeout);
        }
    }

    /**
     * Attempts to acquire the mutex. Returns false (and does not
     * block) if it can't get it.
     *
     * @see release
     * @see acquire
     */
    public synchronized boolean acquireWithoutBlocking(Object owner)
    {
        // Try to get the mutex. Return true if you got it.
        if( mOwner == null ) {
            mOwner = owner;
            mLockCount = 1;
            return true;
        }

        if( mOwner == owner ) {
            ++mLockCount;
            return true;
        }

        return false;
    }

    /**
     * Release the mutex. The mutex has to be released as many times
     * as it was acquired to actually unlock the resource. The mutex
     * must be released by the thread that acquired it
     */
     public synchronized void release(Object owner)
     {
         if( mOwner == owner ) {
             if( --mLockCount <= 0 ) {
                 mOwner = null;
                 notifyAll();
             }
         }
     }
}