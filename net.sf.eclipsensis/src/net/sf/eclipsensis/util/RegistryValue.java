/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 ******************************************************************************/
package net.sf.eclipsensis.util;

import net.sf.eclipsensis.util.winapi.WinAPI;


public class RegistryValue
{
    private String mRegKey;
    private String mValue;
    private int mType = WinAPI.REG_SZ;
    private String mData = ""; //$NON-NLS-1$

    public RegistryValue(String regKey, String value)
    {
        mRegKey = regKey;
        setValue(value);
    }

    public RegistryValue(String regKey, String value, int type, String data)
    {
        this(regKey, value);
        setType(type);
        mData = data;
    }

    protected void set(String value, int type, byte[] data)
    {
        setValue(value);
        setType(type);
        switch(type) {
            case WinAPI.REG_BINARY:
                if(Common.isEmptyArray(data)) {
                    mData = null;
                }
                else {
                    mData = bytesToHexString(data);
                }
                break;
            case WinAPI.REG_DWORD:
                if(Common.isEmptyArray(data) || data.length > 8) {
                    mData = "0"; //$NON-NLS-1$
                }
                else {
                    mData = String.valueOf(Integer.parseInt(bytesToHexString((byte[])Common.flipArray(data)),16));
                }
                break;
            case WinAPI.REG_SZ:
            case WinAPI.REG_EXPAND_SZ:
            case WinAPI.REG_MULTI_SZ:
                if(Common.isEmptyArray(data)) {
                    mData = ""; //$NON-NLS-1$
                }
                else {
                    mData = new String(data,0,(data[data.length-1]==0?data.length-1:data.length));
                }
                break;
        }
    }

    /**
     * @param value
     */
    private void setValue(String value)
    {
        mValue = value;
    }

    /**
     * @param type
     */
    private void setType(int type)
    {
        switch(type) {
            case WinAPI.REG_SZ:
            case WinAPI.REG_BINARY:
            case WinAPI.REG_DWORD:
            case WinAPI.REG_EXPAND_SZ:
            case WinAPI.REG_MULTI_SZ:
                mType = type;
                break;
            default:
                mType = WinAPI.REG_SZ;
        }
    }

    private String bytesToHexString(byte[] data)
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int b = data[i]<0?256+data[i]:(int)data[i];
            int hi = b/16;
            int lo = b%16;
            buf.append((char)(hi>9?'a'+hi-10:'0'+hi-0));
            buf.append((char)(lo>9?'a'+lo-10:'0'+lo-0));
        }
        return buf.toString();
    }

    public String getData()
    {
        return mData;
    }

    public String getRegKey()
    {
        return mRegKey;
    }

    public int getType()
    {
        return mType;
    }

    public String getValue()
    {
        return mValue;
    }

    @Override
    public int hashCode()
    {
        int result = 31 + ((mRegKey == null)?0:mRegKey.hashCode());
        result = 31 * result + ((mValue == null)?0:mValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this != obj) {
            if(obj instanceof RegistryValue) {
                RegistryValue rv = (RegistryValue)obj;
                return Common.objectsAreEqual(mRegKey,rv.getRegKey()) && Common.stringsAreEqual(mValue,rv.getValue(),true);
            }
            return false;
        }
        return true;
    }
}
