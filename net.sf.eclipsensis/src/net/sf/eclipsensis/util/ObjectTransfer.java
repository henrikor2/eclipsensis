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

import org.eclipse.swt.dnd.*;

public abstract class ObjectTransfer extends ByteArrayTransfer
{
    private Object mObject;
    private long mStartTime;

    public Object getObject() {
        return mObject;
    }

    @Override
    public void javaToNative(Object object, TransferData transferData)
    {
        setObject(object);
        mStartTime = System.currentTimeMillis();
        if (transferData != null) {
            super.javaToNative(String.valueOf(mStartTime).getBytes(), transferData);
        }
    }

    @Override
    public Object nativeToJava(TransferData transferData)
    {
        byte[] bytes = (byte[])super.nativeToJava(transferData);
        //Now, only retain numeric bytes
        //This is a hack for Windows 98
        int i = 0;
        for ( ; i < bytes.length; i++) {
            if(!Character.isDigit((char)bytes[i])) {
                break;
            }
        }
        bytes = (byte[])Common.resizeArray(bytes,i);
        long startTime = Long.parseLong(new String(bytes));
        return (this.mStartTime == startTime?getObject():null);
    }

    public void setObject(Object obj)
    {
        mObject = obj;
    }
}
