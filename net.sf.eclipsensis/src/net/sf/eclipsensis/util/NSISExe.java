/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 *******************************************************************************/

package net.sf.eclipsensis.util;

import java.io.File;
import java.util.Properties;

import net.sf.eclipsensis.INSISVersions;
import net.sf.eclipsensis.settings.NSISPreferences;

public class NSISExe
{
    private File mFile;
    private Version mVersion;
    private Properties mDefinedSymbols;
    private boolean mUnicode = false;
    private boolean mSolidCompressionSupported = false;
    private boolean mProcessPrioritySupported = false;

    public NSISExe(File file, Version version, Properties definedSymbols)
    {
        mFile = file;
        mVersion = version;
        mDefinedSymbols = definedSymbols;
        mUnicode = mDefinedSymbols.containsKey(NSISPreferences.NSIS_UNICODE_SUPPORT);
        mSolidCompressionSupported = mVersion.compareTo(INSISVersions.VERSION_2_07) >=0 && mDefinedSymbols.containsKey(NSISPreferences.NSIS_CONFIG_COMPRESSION_SUPPORT);
        mProcessPrioritySupported = mVersion.compareTo(INSISVersions.VERSION_2_24) >=0;
    }

    public File getFile()
    {
        return mFile;
    }

    public Version getVersion()
    {
        return mVersion;
    }

    public Properties getDefinedSymbols()
    {
        return mDefinedSymbols;
    }

    public String getDefinedSymbol(String name)
    {
        return mDefinedSymbols.getProperty(name);
    }

    public boolean isUnicode()
    {
        return mUnicode;
    }

    public boolean isSolidCompressionSupported()
    {
        return mSolidCompressionSupported;
    }

    public boolean isProcessPrioritySupported()
    {
        return mProcessPrioritySupported;
    }
}
