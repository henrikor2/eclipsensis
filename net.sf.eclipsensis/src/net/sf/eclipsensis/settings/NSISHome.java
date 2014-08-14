/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.settings;

import java.io.File;

import net.sf.eclipsensis.util.*;

public class NSISHome
{
    private NSISExe mNSISExe;
    private File mLocation;

    public NSISHome(File location)
    {
        mLocation = location;
        mNSISExe = NSISValidator.findNSISExe(mLocation);
    }

    public NSISExe getNSISExe()
    {
        return mNSISExe;
    }
    public File getLocation()
    {
        return mLocation;
    }

    @Override
    public int hashCode()
    {
        return mLocation == null ? 0 : mLocation.hashCode();
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
        NSISHome other = (NSISHome) obj;
        if (mLocation == null)
        {
            if (other.mLocation != null)
            {
                return false;
            }
        }
        else if (!mLocation.equals(other.mLocation))
        {
            return false;
        }
        return true;
    }
}
