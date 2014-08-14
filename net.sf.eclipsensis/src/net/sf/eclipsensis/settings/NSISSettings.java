/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Version;

public abstract class NSISSettings implements INSISSettingsConstants
{
    protected static final File PLUGIN_STATE_LOCATION = EclipseNSISPlugin.getPluginStateLocation();

    private boolean mHdrInfo = false;
    private boolean mLicense = false;
    private boolean mNoConfig = false;
    private boolean mNoCD = false;
    private int mVerbosity = INSISSettingsConstants.VERBOSITY_DEFAULT;
    private int mProcessPriority = INSISSettingsConstants.PROCESS_PRIORITY_DEFAULT;
    private int mCompressor = MakeNSISRunner.COMPRESSOR_DEFAULT;
    private boolean mSolidCompression = false;
    private List<String> mInstructions = null;
    private Map<String, String> mSymbols = null;

    public void load()
    {
        setHdrInfo(getBoolean(HDRINFO));
        setLicense(getBoolean(LICENSE));
        setNoConfig(getBoolean(NOCONFIG));
        setNoCD(getBoolean(NOCD));
        setVerbosity(getInt(VERBOSITY));
        setProcessPriority(getInt(PROCESS_PRIORITY));
        setCompressor(getInt(COMPRESSOR));
        setSolidCompression(getBoolean(SOLID_COMPRESSION));
        setInstructions(this.<List<String>>loadObject(INSTRUCTIONS));
        setSymbols(this.<Map<String,String>>loadObject(SYMBOLS));
        if(migrate(new Version(getString(PLUGIN_VERSION)))) {
            store();
        }
    }

    protected boolean migrate(Version settingsVersion)
    {
        if(EclipseNSISPlugin.getDefault().getVersion().compareTo(settingsVersion) > 0) {
            if(IPluginVersions.VERSION_0_9_5_1.compareTo(settingsVersion) > 0) {
                if(getVerbosity() == INSISSettingsConstants.VERBOSITY_ALL) {
                    setVerbosity(getDefaultVerbosity());
                    return true;
                }
            }
        }
        return false;
    }

    public void store()
    {
        setValue(HDRINFO,mHdrInfo);
        setValue(LICENSE, mLicense);
        setValue(NOCONFIG, mNoConfig);
        setValue(NOCD, mNoCD);
        setValue(VERBOSITY,mVerbosity);
        setValue(PROCESS_PRIORITY,mProcessPriority);
        setValue(COMPRESSOR, mCompressor);
        setValue(SOLID_COMPRESSION, mSolidCompression);
        setValue(PLUGIN_VERSION,EclipseNSISPlugin.getDefault().getVersion().toString());
        storeObject(SYMBOLS, mSymbols);
        storeObject(INSTRUCTIONS, mInstructions);
    }

    public boolean showStatistics()
    {
        return true;
    }

    public boolean getSolidCompression()
    {
        return mSolidCompression;
    }

    public boolean getDefaultSolidCompression()
    {
        return false;
    }

    public void setSolidCompression(boolean solidCompression)
    {
        mSolidCompression = solidCompression;
    }

    /**
     * @return Returns the compressor.
     */
    public int getCompressor()
    {
        return mCompressor;
    }

    /**
     * @return Returns the default compressor.
     */
    public int getDefaultCompressor()
    {
        return MakeNSISRunner.COMPRESSOR_DEFAULT;
    }

    /**
     * @param compressor The compressor to set.
     */
    public void setCompressor(int compressor)
    {
        mCompressor = compressor;
    }

    /**
     * @return Returns the headerInfo.
     */
    public boolean getHdrInfo()
    {
        return mHdrInfo;
    }

    /**
     * @return Returns the headerInfo.
     */
    public boolean getDefaultHdrInfo()
    {
        return false;
    }

    /**
     * @param headerInfo The headerInfo to set.
     */
    public void setHdrInfo(boolean headerInfo)
    {
        mHdrInfo = headerInfo;
    }

    /**
     * @return Returns the license.
     */
    public boolean getLicense()
    {
        return mLicense;
    }
    /**
     * @return Returns the default license.
     */
    public boolean getDefaultLicense()
    {
        return false;
    }

    /**
     * @param license The license to set.
     */
    public void setLicense(boolean license)
    {
        mLicense = license;
    }

    /**
     * @return Returns the noCD.
     */
    public boolean getNoCD()
    {
        return mNoCD;
    }

    /**
     * @return Returns the default noCD.
     */
    public boolean getDefaultNoCD()
    {
        return false;
    }

    /**
     * @param noCD The noCD to set.
     */
    public void setNoCD(boolean noCD)
    {
        mNoCD = noCD;
    }

    /**
     * @return Returns the noConfig.
     */
    public boolean getNoConfig()
    {
        return mNoConfig;
    }
    /**
     * @return Returns the default noConfig.
     */
    public boolean getDefaultNoConfig()
    {
        return false;
    }

    /**
     * @param noConfig The noConfig to set.
     */
    public void setNoConfig(boolean noConfig)
    {
        mNoConfig = noConfig;
    }

    /**
     * @return Returns the verbosity.
     */
    public int getVerbosity()
    {
        return mVerbosity;
    }

    /**
     * @return Returns the default verbosity.
     */
    public int getDefaultVerbosity()
    {
        return VERBOSITY_DEFAULT;
    }

    /**
     * @param verbosity The verbosity to set.
     */
    public void setVerbosity(int verbosity)
    {
        mVerbosity = verbosity;
    }

    /**
     * @return Returns the processPriority.
     */
    public int getProcessPriority()
    {
        return mProcessPriority;
    }

    /**
     * @return Returns the default processPriority.
     */
    public int getDefaultProcessPriority()
    {
        return PROCESS_PRIORITY_DEFAULT;
    }

    /**
     * @param processPriority The processPriority to set.
     */
    public void setProcessPriority(int processPriority)
    {
        mProcessPriority = processPriority;
    }

    /**
     * @return Returns the default instructions.
     */
    public List<String> getDefaultInstructions()
    {
        return new ArrayList<String>();
    }

    /**
     * @return Returns the instructions.
     */
    public List<String> getInstructions()
    {
        return (mInstructions !=null?new ArrayList<String>(mInstructions):new ArrayList<String>());
    }

    /**
     * @param instructions The instructions to set.
     */
    public void setInstructions(List<String> instructions)
    {
        mInstructions = (instructions==null?new ArrayList<String>():instructions);
    }

    /**
     * @return Returns the default symbols.
     */
    public Map<String, String> getDefaultSymbols()
    {
        return new LinkedHashMap<String, String>();
    }

    /**
     * @return Returns the symbols.
     */
    public Map<String, String> getSymbols()
    {
        return (mSymbols == null?new LinkedHashMap<String, String>():new LinkedHashMap<String, String>(mSymbols));
    }

    /**
     * @param symbols The symbols to set.
     */
    public void setSymbols(Map<String, String> symbols)
    {
        mSymbols = (symbols==null?new LinkedHashMap<String, String>():symbols);
    }

    public abstract String getString(String name);
    public abstract boolean getBoolean(String name);
    public abstract int getInt(String name);
    public abstract void setValue(String name, String value);
    public abstract void setValue(String name, boolean value);
    public abstract void setValue(String name, int value);
    public abstract void removeString(String name);
    public abstract void removeBoolean(String name);
    public abstract void removeInt(String name);
    public abstract void removeObject(String name);
    public abstract <T> void storeObject(String name, T object);
    public abstract <T> T loadObject(String name);
}
