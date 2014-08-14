/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.util.winapi.x86;

import net.sf.eclipsensis.util.winapi.IHandle;

public class Handle32 implements IHandle
{
    int value;

    public Handle32(int value)
    {
        this.value = value;
    }

    public Number getValue()
    {
        return value;
    }

    public String toHexString()
    {
        return Integer.toHexString(value);
    }


    @Override
    public String toString()
    {
        return Integer.toString(value);
    }


    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Handle32 other = (Handle32) obj;
        if (value != other.value)
        {
            return false;
        }
        return true;
    }
}
