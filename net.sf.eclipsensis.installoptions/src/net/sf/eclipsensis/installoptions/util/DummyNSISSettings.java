/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import net.sf.eclipsensis.settings.NSISSettings;

public class DummyNSISSettings extends NSISSettings
{
    @Override
    public boolean showStatistics()
    {
        return false;
    }

    @Override
    public boolean getBoolean(String name)
    {
        return false;
    }

    @Override
    public int getInt(String name)
    {
        return 0;
    }

    @Override
    public String getString(String name)
    {
        return ""; //$NON-NLS-1$
    }

    @Override
    public <T> T loadObject(String name)
    {
        return null;
    }

    @Override
    public void removeBoolean(String name)
    {
    }

    @Override
    public void removeInt(String name)
    {
    }

    @Override
    public void removeString(String name)
    {
    }

    @Override
    public void removeObject(String name)
    {
    }

    @Override
    public void setValue(String name, boolean value)
    {
    }

    @Override
    public void setValue(String name, int value)
    {
    }

    @Override
    public void setValue(String name, String value)
    {
    }

    @Override
    public <T> void storeObject(String name, T object)
    {
    }

    public String getName()
    {
        return null;
    }
}
