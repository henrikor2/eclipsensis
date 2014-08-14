/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.beans.*;
import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.util.NSISWizardUtil;

import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.*;

public class NSISWizardSettings extends AbstractNodeConvertible implements INSISWizardConstants
{
    public static final String NODE = "settings"; //$NON-NLS-1$
    public static final String CHILD_NODE = "attribute"; //$NON-NLS-1$

    public static final String NAME = "NAME"; //$NON-NLS-1$
    public static final String COMPANY = "COMPANY"; //$NON-NLS-1$
    public static final String VERSION = "VERSION"; //$NON-NLS-1$
    public static final String URL = "URL"; //$NON-NLS-1$
    public static final String OUT_FILE = "OUTFILE"; //$NON-NLS-1$
    public static final String COMPRESSOR_TYPE = "COMPRESSOR_TYPE"; //$NON-NLS-1$
    public static final String SOLID_COMPRESSION = "SOLID_COMPRESSION"; //$NON-NLS-1$
    public static final String INSTALLER_TYPE = "INSTALLER_TYPE"; //$NON-NLS-1$
    public static final String ICON = "ICON"; //$NON-NLS-1$
    public static final String SHOW_SPLASH = "SHOW_SPLASH"; //$NON-NLS-1$
    public static final String SPLASH_BMP = "SPLASH_BMP"; //$NON-NLS-1$
    public static final String SPLASH_WAV = "SPLASH_WAV"; //$NON-NLS-1$
    public static final String SPLASH_DELAY = "SPLASH_DELAY"; //$NON-NLS-1$
    public static final String FADE_IN_DELAY = "FADE_IN_DELAY"; //$NON-NLS-1$
    public static final String FADE_OUT_DELAY = "FADE_OUT_DELAY"; //$NON-NLS-1$
    public static final String SHOW_BACKGROUND = "SHOW_BACKGROUND"; //$NON-NLS-1$
    public static final String BG_TOP_COLOR = "BG_TOP_COLOR"; //$NON-NLS-1$
    public static final String BG_BOTTOM_COLOR = "BG_BOTTOM_COLOR"; //$NON-NLS-1$
    public static final String BG_TEXT_COLOR = "BG_TEXT_COLOR"; //$NON-NLS-1$
    public static final String BACKGROUND_BMP = "BACKGROUND_BMP"; //$NON-NLS-1$
    public static final String BACKGROUND_WAV = "BACKGROUND_WAV"; //$NON-NLS-1$
    public static final String SHOW_LICENSE = "SHOW_LICENSE"; //$NON-NLS-1$
    public static final String LICENSE_DATA = "LICENSE_DATA"; //$NON-NLS-1$
    public static final String LICENSE_BUTTON_TYPE = "LICENSE_BUTTON_TYPE"; //$NON-NLS-1$
    public static final String ENABLE_LANGUAGE_SUPPORT = "ENABLE_LANGUAGE_SUPPORT"; //$NON-NLS-1$
    public static final String LANGUAGES = "LANGUAGES"; //$NON-NLS-1$
    public static final String SELECT_LANGUAGE = "SELECT_LANGUAGE"; //$NON-NLS-1$
    public static final String DISPLAY_SUPPORTED_LANGUAGES = "DISPLAY_SUPPORTED_LANGUAGES"; //$NON-NLS-1$
    public static final String INSTALL_DIR = "INSTALL_DIR"; //$NON-NLS-1$
    public static final String CHANGE_INSTALL_DIR = "CHANGE_INSTALL_DIR"; //$NON-NLS-1$
    public static final String CREATE_START_MENU_GROUP = "CREATE_START_MENU_GROUP"; //$NON-NLS-1$
    public static final String START_MENU_GROUP = "START_MENU_GROUP"; //$NON-NLS-1$
    public static final String CHANGE_START_MENU_GROUP = "CHANGE_START_MENU_GROUP"; //$NON-NLS-1$
    public static final String DISABLE_START_MENU_SHORTCUTS = "DISABLE_START_MENU_SHORTCUTS"; //$NON-NLS-1$
    public static final String SHOW_INST_DETAILS = "SHOW_INST_DETAILS"; //$NON-NLS-1$
    public static final String RUN_PROGRAM_AFTER_INSTALL = "RUN_PROGRAM_AFTER_INSTALL"; //$NON-NLS-1$
    public static final String RUN_PROGRAM_AFTER_INSTALL_PARAMS = "RUN_PROGRAM_AFTER_INSTALL_PARAMS"; //$NON-NLS-1$
    public static final String OPEN_README_AFTER_INSTALL = "OPEN_README_AFTER_INSTALL"; //$NON-NLS-1$
    public static final String AUTO_CLOSE_INSTALLER = "AUTO_CLOSE_INSTALLER"; //$NON-NLS-1$
    public static final String SHOW_UNINST_DETAILS = "SHOW_UNINST_DETAILS"; //$NON-NLS-1$
    public static final String AUTO_CLOSE_UNINSTALLER = "AUTO_CLOSE_UNINSTALLER"; //$NON-NLS-1$
    public static final String CREATE_UNINSTALLER_START_MENU_SHORTCUT = "CREATE_UNINSTALLER_START_MENU_SHORTCUT"; //$NON-NLS-1$
    public static final String CREATE_UNINSTALLER_CONTROL_PANEL_ENTRY = "CREATE_UNINSTALLER_CONTROL_PANEL_ENTRY"; //$NON-NLS-1$
    public static final String SILENT_UNINSTALLER = "SILENT_UNINSTALLER"; //$NON-NLS-1$
    public static final String SELECT_COMPONENTS = "SELECT_COMPONENTS"; //$NON-NLS-1$

    public static final String TARGET_PLATFORM = "TARGET_PLATFORM"; //$NON-NLS-1$
    public static final String EXECUTION_LEVEL = "EXECUTION_LEVEL"; //$NON-NLS-1$

    public static final String CREATE_UNINSTALLER = "CREATE_UNINSTALLER"; //$NON-NLS-1$
    public static final String UNINSTALL_ICON = "UNINSTALL_ICON"; //$NON-NLS-1$
    public static final String UNINSTALL_FILE = "UNINSTALL_FILE"; //$NON-NLS-1$
    public static final String SAVE_EXTERNAL = "SAVE_EXTERNAL"; //$NON-NLS-1$
    public static final String SAVE_PATH = "SAVE_PATH"; //$NON-NLS-1$
    public static final String MAKE_PATHS_RELATIVE = "MAKE_PATHS_RELATIVE"; //$NON-NLS-1$
    public static final String COMPILE_SCRIPT = "COMPILE_SCRIPT"; //$NON-NLS-1$
    public static final String TEST_SCRIPT = "TEST_SCRIPT"; //$NON-NLS-1$

    public static final String MINIMUM_NSIS_VERSION = "MINIMUM_NSIS_VERSION"; //$NON-NLS-1$

    public static final String MULTIUSER_INSTALLATION = "MULTIUSER_INSTALLATION"; //$NON-NLS-1$
    public static final String MULTIUSER_EXEC_LEVEL = "MULTIUSER_EXEC_LEVEL"; //$NON-NLS-1$
    public static final String MULTIUSER_INSTALL_MODE = "MULTIUSER_INSTALL_MODE"; //$NON-NLS-1$
    public static final String MULTIUSER_INSTALL_MODE_REMEMBER = "MULTIUSER_INSTALL_MODE_REMEMBER"; //$NON-NLS-1$
    public static final String MULTIUSER_INSTALL_MODE_ASK = "MULTIUSER_INSTALL_MODE_ASK"; //$NON-NLS-1$

    public static final String INSTALLER = "INSTALLER"; //$NON-NLS-1$

    private static final long serialVersionUID = -3872062583870145866L;

    private transient PropertyChangeSupport mListeners;

    private String mName = EclipseNSISPlugin.getResourceString("wizard.default.name",""); //$NON-NLS-1$ //$NON-NLS-2$
    private String mCompany = ""; //$NON-NLS-1$
    private String mVersion = ""; //$NON-NLS-1$
    private String mUrl = ""; //$NON-NLS-1$
    private String mOutFile = EclipseNSISPlugin.getResourceString("wizard.default.installer",""); //$NON-NLS-1$ //$NON-NLS-2$
    private int mCompressorType = MakeNSISRunner.COMPRESSOR_DEFAULT;
    private boolean mSolidCompression = false;
    private int mInstallerType;
    private String mIcon = ""; //$NON-NLS-1$
    private boolean mShowSplash = false;
    private String mSplashBMP = ""; //$NON-NLS-1$
    private String mSplashWAV = ""; //$NON-NLS-1$
    private int mSplashDelay = 1000;
    private int mFadeInDelay = 600;
    private int mFadeOutDelay = 400;
    private boolean mShowBackground = false;
    private RGB mBGTopColor = new RGB(0x0,0x0,0x80);
    private RGB mBGBottomColor = ColorManager.BLACK;
    private RGB mBGTextColor = ColorManager.WHITE;
    private String mBackgroundBMP = ""; //$NON-NLS-1$
    private String mBackgroundWAV = ""; //$NON-NLS-1$
    private boolean mShowLicense = false;
    private String mLicenseData = ""; //$NON-NLS-1$
    private int mLicenseButtonType = LICENSE_BUTTON_CLASSIC;
    private boolean mEnableLanguageSupport = false;
    private List<NSISLanguage> mLanguages = new ArrayList<NSISLanguage>();
    private boolean mSelectLanguage = false;
    private boolean mDisplaySupportedLanguages = true;
    private String mInstallDir = new StringBuffer(NSISKeywords.getInstance().getKeyword("$PROGRAMFILES")).append("\\").append(mName).toString(); //$NON-NLS-1$ //$NON-NLS-2$
    private boolean mChangeInstallDir = true;
    private boolean mCreateStartMenuGroup = false;
    private String mStartMenuGroup = mName;
    private boolean mChangeStartMenuGroup = false;
    private boolean mDisableStartMenuShortcuts = false;
    private boolean mShowInstDetails = false;
    private String mRunProgramAfterInstall = ""; //$NON-NLS-1$
    private String mRunProgramAfterInstallParams = ""; //$NON-NLS-1$
    private String mOpenReadmeAfterInstall = ""; //$NON-NLS-1$
    private boolean mAutoCloseInstaller = false;
    private boolean mShowUninstDetails = false;
    private boolean mAutoCloseUninstaller = false;
    private boolean mCreateUninstallerStartMenuShortcut = true;
    private boolean mCreateUninstallerControlPanelEntry = true;
    private boolean mSilentUninstaller = false;
    private boolean mSelectComponents = false;

    private int mTargetPlatform = TARGET_PLATFORM_ANY;
    private int mExecutionLevel = EXECUTION_LEVEL_NONE;

    private boolean mCreateUninstaller = true;
    private String mUninstallIcon = ""; //$NON-NLS-1$
    private String mUninstallFile = EclipseNSISPlugin.getResourceString("wizard.default.uninstaller",""); //$NON-NLS-1$ //$NON-NLS-2$
    private boolean mSaveExternal = false;
    private String mSavePath = ""; //$NON-NLS-1$
    private boolean mMakePathsRelative = true;
    private boolean mCompileScript = true;
    private boolean mTestScript = false;

    private Version mMinimumNSISVersion = null;

    private boolean mMultiUserInstallation = false;
    private int mMultiUserExecLevel = MULTIUSER_EXEC_LEVEL_STANDARD;
    private int mMultiUserInstallMode = MULTIUSER_INSTALL_MODE_ALLUSERS;
    private boolean mMultiUserInstallModeRemember = false;
    private boolean mMultiUserInstallModeAsk = false;

    private INSISInstallElement mInstaller;

    private transient NSISWizard mWizard = null;

    public NSISWizardSettings()
    {
        this(false);
    }

    public NSISWizardSettings(boolean empty)
    {
        super();
        setInstallerType(NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_34) >= 0 ?
                            INSTALLER_TYPE_MUI2 :
                            INSTALLER_TYPE_MUI);
        if(!empty) {
            mInstaller = new NSISInstaller();
            mInstaller.setSettings(this);
            NSISSection section = (NSISSection)NSISInstallElementFactory.create(NSISSection.TYPE);
            section.setName(EclipseNSISPlugin.getResourceString("main.section.name")); //$NON-NLS-1$
            section.setDescription(EclipseNSISPlugin.getResourceString("main.section.description")); //$NON-NLS-1$
            section.setHidden(true);
            mInstaller.addChild(section);
        }
        else {
            mInstaller = null;
        }
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("wizard"); //$NON-NLS-1$
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if(mListeners == null) {
            mListeners = new PropertyChangeSupport(this);
        }
        mListeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if(mListeners != null) {
            mListeners.removePropertyChangeListener(listener);
        }
    }

    private void firePropertyChanged(String name, Object oldValue, Object newValue)
    {
        if(mListeners != null) {
            mListeners.firePropertyChange(name, oldValue, newValue);
        }
    }

   private void firePropertyChanged(String name, boolean oldValue, boolean newValue)
    {
        if(mListeners != null) {
            mListeners.firePropertyChange(name, oldValue, newValue);
        }
    }

    private void firePropertyChanged(String name, int oldValue, int newValue)
    {
        if(mListeners != null) {
            mListeners.firePropertyChange(name, oldValue, newValue);
        }
    }

    /**
     * @return Returns the wizard.
     */
    public NSISWizard getWizard()
    {
        return mWizard;
    }

    /**
     * @param wizard The wizard to set.
     */
    public void setWizard(NSISWizard wizard)
    {
        mWizard = wizard;
    }

    /**
     * @return Returns the autoCloseInstaller.
     */
    public boolean isAutoCloseInstaller()
    {
        return mAutoCloseInstaller;
    }

    /**
     * @param autoCloseInstaller The autoCloseInstaller to set.
     */
    public void setAutoCloseInstaller(boolean autoCloseInstaller)
    {
        boolean oldValue = mAutoCloseInstaller;
        mAutoCloseInstaller = autoCloseInstaller;
        firePropertyChanged(AUTO_CLOSE_INSTALLER, oldValue, autoCloseInstaller);
    }

    /**
     * @return Returns the bGBottomColor.
     */
    public RGB getBGBottomColor()
    {
        return mBGBottomColor;
    }

    /**
     * @param bottomColor The bGBottomColor to set.
     */
    public void setBGBottomColor(RGB bottomColor)
    {
        RGB oldValue = mBGBottomColor;
        mBGBottomColor = bottomColor;
        firePropertyChanged(BG_BOTTOM_COLOR, oldValue, bottomColor);
    }

    /**
     * @return Returns the showBackground.
     */
    public boolean isShowBackground()
    {
        return mShowBackground;
    }

    /**
     * @param showBackground The showBackground to set.
     */
    public void setShowBackground(boolean showBackground)
    {
        boolean oldValue = mShowBackground;
        mShowBackground = showBackground;
        firePropertyChanged(SHOW_BACKGROUND, oldValue, showBackground);
    }

    /**
     * @return Returns the bGImage.
     */
    public String getBackgroundBMP()
    {
        return mBackgroundBMP;
    }

    /**
     * @param image The bGImage to set.
     */
    public void setBackgroundBMP(String backgroundBMP)
    {
        String oldValue = mBackgroundBMP;
        mBackgroundBMP = backgroundBMP;
        firePropertyChanged(BACKGROUND_BMP, oldValue, backgroundBMP);
    }

    /**
     * @return Returns the backgroundWAV.
     */
    public String getBackgroundWAV()
    {
        return mBackgroundWAV;
    }

    /**
     * @param backgroundWAV The backgroundWAV to set.
     */
    public void setBackgroundWAV(String backgroundWAV)
    {
        String oldValue = mBackgroundWAV;
        mBackgroundWAV = backgroundWAV;
        firePropertyChanged(BACKGROUND_WAV, oldValue, backgroundWAV);
    }
    /**
     * @return Returns the bGTextColor.
     */
    public RGB getBGTextColor()
    {
        return mBGTextColor;
    }

    /**
     * @param textColor The bGTextColor to set.
     */
    public void setBGTextColor(RGB textColor)
    {
        RGB oldValue = mBGTextColor;
        mBGTextColor = textColor;
        firePropertyChanged(BG_TEXT_COLOR, oldValue, textColor);
    }

    /**
     * @return Returns the bGTopColor.
     */
    public RGB getBGTopColor()
    {
        return mBGTopColor;
    }

    /**
     * @param topColor The bGTopColor to set.
     */
    public void setBGTopColor(RGB topColor)
    {
        RGB oldValue = mBGTopColor;
        mBGTopColor = topColor;
        firePropertyChanged(BG_TOP_COLOR, oldValue, topColor);
    }

    /**
     * @return Returns the changeInstallDir.
     */
    public boolean isChangeInstallDir()
    {
        return mChangeInstallDir;
    }

    /**
     * @param changeInstallDir The changeInstallDir to set.
     */
    public void setChangeInstallDir(boolean changeInstallDir)
    {
        boolean oldValue = mChangeInstallDir;
        mChangeInstallDir = changeInstallDir;
        firePropertyChanged(CHANGE_INSTALL_DIR, oldValue, changeInstallDir);
    }

    /**
     * @return Returns the changeProgramGroup.
     */
    public boolean isChangeStartMenuGroup()
    {
        return mChangeStartMenuGroup;
    }

    /**
     * @param changeStartMenuGroup The changeStartMenuGroup to set.
     */
    public void setChangeStartMenuGroup(boolean changeStartMenuGroup)
    {
        boolean oldValue = mChangeStartMenuGroup;
        mChangeStartMenuGroup = changeStartMenuGroup;
        firePropertyChanged(CHANGE_START_MENU_GROUP, oldValue, changeStartMenuGroup);
    }

    public boolean isDisableStartMenuShortcuts()
    {
        return mDisableStartMenuShortcuts;
    }

    public void setDisableStartMenuShortcuts(boolean disableStartMenuShortcuts)
    {
        boolean oldValue = mDisableStartMenuShortcuts;
        mDisableStartMenuShortcuts = disableStartMenuShortcuts;
        firePropertyChanged(DISABLE_START_MENU_SHORTCUTS, oldValue, disableStartMenuShortcuts);
    }

    /**
     * @return Returns the company.
     */
    public String getCompany()
    {
        return mCompany;
    }

    /**
     * @param company The company to set.
     */
    public void setCompany(String company)
    {
        String oldValue = mCompany;
        mCompany = company;
        firePropertyChanged(COMPANY, oldValue, company);
    }

    /**
     * @return Returns the createStartMenuGroup.
     */
    public boolean isCreateStartMenuGroup()
    {
        return mCreateStartMenuGroup;
    }

    /**
     * @param createStartMenuGroup The createStartMenuGroup to set.
     */
    public void setCreateStartMenuGroup(boolean createStartMenuGroup)
    {
        boolean oldValue = mCreateStartMenuGroup;
        mCreateStartMenuGroup = createStartMenuGroup;
        firePropertyChanged(CREATE_START_MENU_GROUP, oldValue, createStartMenuGroup);
    }

    /**
     * @return Returns the createUninstaller.
     */
    public boolean isCreateUninstaller()
    {
        return mCreateUninstaller;
    }

    /**
     * @param createUninstaller The createUninstaller to set.
     */
    public void setCreateUninstaller(boolean createUninstaller)
    {
        boolean oldValue = mCreateUninstaller;
        mCreateUninstaller = createUninstaller;
        firePropertyChanged(CREATE_UNINSTALLER, oldValue, createUninstaller);
    }

    /**
     * @return Returns the fadeInTime.
     */
    public int getFadeInDelay()
    {
        return mFadeInDelay;
    }

    /**
     * @param fadeInDelay The fadeInDelay to set.
     */
    public void setFadeInDelay(int fadeInDelay)
    {
        int oldValue = mFadeInDelay;
        mFadeInDelay = fadeInDelay;
        firePropertyChanged(FADE_IN_DELAY, oldValue, fadeInDelay);
    }

    /**
     * @return Returns the fadeOutTime.
     */
    public int getFadeOutDelay()
    {
        return mFadeOutDelay;
    }

    /**
     * @param fadeOutDelay The fadeOutDelay to set.
     */
    public void setFadeOutDelay(int fadeOutDelay)
    {
        int oldValue = mFadeOutDelay;
        mFadeOutDelay = fadeOutDelay;
        firePropertyChanged(FADE_OUT_DELAY, oldValue, fadeOutDelay);
    }

    /**
     * @return Returns the installDir.
     */
    public String getInstallDir()
    {
        return mInstallDir;
    }

    /**
     * @param installDir The installDir to set.
     */
    public void setInstallDir(String installDir)
    {
        String oldValue = mInstallDir;
        mInstallDir = installDir;
        firePropertyChanged(INSTALL_DIR, oldValue, installDir);
    }

    /**
     * @return Returns the icon.
     */
    public String getIcon()
    {
        return mIcon;
    }

    /**
     * @param icon The icon to set.
     */
    public void setIcon(String icon)
    {
        String oldValue = mIcon;
        mIcon = icon;
        firePropertyChanged(ICON, oldValue, icon);
    }

    /**
     * @return Returns the installType.
     */
    public int getInstallerType()
    {
        return mInstallerType;
    }

    /**
     * @param installType The installType to set.
     */
    public void setInstallerType(int installerType)
    {
        int oldValue = mInstallerType;
        mInstallerType = installerType;
        firePropertyChanged(INSTALLER_TYPE, oldValue, installerType);
        updateMinimumNSISVersion();
    }

    /**
     * @return Returns the licenseButtonType.
     */
    public int getLicenseButtonType()
    {
        return mLicenseButtonType;
    }

    /**
     * @param licenseButtonType The licenseButtonType to set.
     */
    public void setLicenseButtonType(int licenseButtonType)
    {
        int oldValue = mLicenseButtonType;
        mLicenseButtonType = licenseButtonType;
        firePropertyChanged(LICENSE_BUTTON_TYPE, oldValue, licenseButtonType);
    }

    /**
     * @return Returns the licenseData.
     */
    public String getLicenseData()
    {
        return mLicenseData;
    }

    /**
     * @param licenseData The licenseData to set.
     */
    public void setLicenseData(String licenseData)
    {
        String oldValue = mLicenseData;
        mLicenseData = licenseData;
        firePropertyChanged(LICENSE_DATA, oldValue, licenseData);
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        String oldValue = mName;
        mName = name;
        firePropertyChanged(NAME, oldValue, name);
    }

    /**
     * @return Returns the outFile.
     */
    public String getOutFile()
    {
        return mOutFile;
    }

    /**
     * @param outFile The outFile to set.
     */
    public void setOutFile(String outFile)
    {
        String oldValue = mOutFile;
        mOutFile = outFile;
        firePropertyChanged(OUT_FILE, oldValue, outFile);
    }

    /**
     * @return Returns the startMenuGroup.
     */
    public String getStartMenuGroup()
    {
        return mStartMenuGroup;
    }

    /**
     * @param startMenuGroup The startMenuGroup to set.
     */
    public void setStartMenuGroup(String startMenuGroup)
    {
        String oldValue = mStartMenuGroup;
        mStartMenuGroup = startMenuGroup;
        firePropertyChanged(START_MENU_GROUP, oldValue, startMenuGroup);
    }

    /**
     * @return Returns the runProgramAfterInstall.
     */
    public String getRunProgramAfterInstall()
    {
        return mRunProgramAfterInstall;
    }

    /**
     * @param runAfterInstall The runProgramAfterInstall to set.
     */
    public void setRunProgramAfterInstall(String runProgramAfterInstall)
    {
        String oldValue = mRunProgramAfterInstall;
        mRunProgramAfterInstall = runProgramAfterInstall;
        firePropertyChanged(RUN_PROGRAM_AFTER_INSTALL, oldValue, runProgramAfterInstall);
    }

    /**
     * @return Returns the showInstDetails.
     */
    public boolean isShowInstDetails()
    {
        return mShowInstDetails;
    }

    /**
     * @param showInstDetails The showInstDetails to set.
     */
    public void setShowInstDetails(boolean showInstDetails)
    {
        boolean oldValue = mShowInstDetails;
        mShowInstDetails = showInstDetails;
        firePropertyChanged(SHOW_INST_DETAILS, oldValue, showInstDetails);
    }

    /**
     * @return Returns the showLicense.
     */
    public boolean isShowLicense()
    {
        return mShowLicense;
    }

    /**
     * @param showLicense The showLicense to set.
     */
    public void setShowLicense(boolean showLicense)
    {
        boolean oldValue = mShowLicense;
        mShowLicense = showLicense;
        firePropertyChanged(SHOW_LICENSE, oldValue, showLicense);
    }

    /**
     * @return Returns the splashBMP.
     */
    public String getSplashBMP()
    {
        return mSplashBMP;
    }

    /**
     * @param splashBMP The splashBMP to set.
     */
    public void setSplashBMP(String splashBMP)
    {
        String oldValue = mSplashBMP;
        mSplashBMP = splashBMP;
        firePropertyChanged(SPLASH_BMP, oldValue, splashBMP);
    }

    /**
     * @return Returns the splashTime.
     */
    public int getSplashDelay()
    {
        return mSplashDelay;
    }

    /**
     * @param splashDelay The splashDelay to set.
     */
    public void setSplashDelay(int splashDelay)
    {
        int oldValue = mSplashDelay;
        mSplashDelay = splashDelay;
        firePropertyChanged(SPLASH_DELAY, oldValue, splashDelay);
    }

    /**
     * @return Returns the splashWAV.
     */
    public String getSplashWAV()
    {
        return mSplashWAV;
    }

    /**
     * @param splashWAV The splashWAV to set.
     */
    public void setSplashWAV(String splashWAV)
    {
        String oldValue = mSplashWAV;
        mSplashWAV = splashWAV;
        firePropertyChanged(SPLASH_WAV, oldValue, splashWAV);
    }

    /**
     * @return Returns the uninstallFile.
     */
    public String getUninstallFile()
    {
        return mUninstallFile;
    }

    /**
     * @param uninstallFile The uninstallFile to set.
     */
    public void setUninstallFile(String uninstallFile)
    {
        String oldValue = mUninstallFile;
        mUninstallFile = uninstallFile;
        firePropertyChanged(UNINSTALL_FILE, oldValue, uninstallFile);
    }

    /**
     * @return Returns the uninstallIcon.
     */
    public String getUninstallIcon()
    {
        return mUninstallIcon;
    }

    /**
     * @param uninstallIcon The uninstallIcon to set.
     */
    public void setUninstallIcon(String uninstallIcon)
    {
        String oldValue = mUninstallIcon;
        mUninstallIcon = uninstallIcon;
        firePropertyChanged(UNINSTALL_ICON, oldValue, uninstallIcon);
    }

    /**
     * @return Returns the url.
     */
    public String getUrl()
    {
        return mUrl;
    }

    /**
     * @param url The url to set.
     */
    public void setUrl(String url)
    {
        String oldValue = mUrl;
        mUrl = url;
        firePropertyChanged(URL, oldValue, url);
    }

    /**
     * @return Returns the version.
     */
    public String getVersion()
    {
        return mVersion;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version)
    {
        String oldValue = mVersion;
        mVersion = version;
        firePropertyChanged(VERSION, oldValue, version);
    }

    /**
     * @return Returns the createMultilingual.
     */
    public boolean isEnableLanguageSupport()
    {
        return mEnableLanguageSupport;
    }

    /**
     * @param createMultilingual The createMultilingual to set.
     */
    public void setEnableLanguageSupport(boolean enableLanguageSupport)
    {
        boolean oldValue = mEnableLanguageSupport;
        mEnableLanguageSupport = enableLanguageSupport;
        firePropertyChanged(ENABLE_LANGUAGE_SUPPORT, oldValue, enableLanguageSupport);
    }

    /**
     * @return Returns the selectLanguage.
     */
    public boolean isSelectLanguage()
    {
        return mSelectLanguage;
    }

    /**
     * @param displaySupportedLanguages The displaySupportedLanguages to set.
     */
    public void setDisplaySupportedLanguages(boolean displaySupportedLanguages)
    {
        boolean oldValue = mDisplaySupportedLanguages;
        mDisplaySupportedLanguages = displaySupportedLanguages;
        firePropertyChanged(DISPLAY_SUPPORTED_LANGUAGES, oldValue, displaySupportedLanguages);
    }

    /**
     * @return Returns the displaySupportedLanguages.
     */
    public boolean isDisplaySupportedLanguages()
    {
        return mDisplaySupportedLanguages;
    }

    /**
     * @param selectLanguage The selectLanguage to set.
     */
    public void setSelectLanguage(boolean selectLanguage)
    {
        boolean oldValue = mSelectLanguage;
        mSelectLanguage = selectLanguage;
        firePropertyChanged(SELECT_LANGUAGE, oldValue, selectLanguage);
    }

    /**
     * @return Returns the languages.
     */
    public List<NSISLanguage> getLanguages()
    {
        return mLanguages;
    }

    /**
     * @param languages The languages to set.
     */
    public void setLanguages(List<NSISLanguage> languages)
    {
        if(mLanguages != languages) {
            mLanguages.clear();
            mLanguages.addAll(languages);
        }
        firePropertyChanged(LANGUAGES, mLanguages, languages);
    }

    /**
     * @return Returns the showSplash.
     */
    public boolean isShowSplash()
    {
        return mShowSplash;
    }

    /**
     * @param showSplash The showSplash to set.
     */
    public void setShowSplash(boolean showSplash)
    {
        boolean oldValue = mShowSplash;
        mShowSplash = showSplash;
        firePropertyChanged(SHOW_SPLASH, oldValue, showSplash);
    }

    /**
     * @return Returns the compileScript.
     */
    public boolean isCompileScript()
    {
        return mCompileScript;
    }

    /**
     * @param compileScript The compileScript to set.
     */
    public void setCompileScript(boolean compileScript)
    {
        boolean oldValue = mCompileScript;
        mCompileScript = compileScript;
        firePropertyChanged(COMPILE_SCRIPT, oldValue, compileScript);
    }

    /**
     * @return Returns the testScript.
     */
    public boolean isTestScript()
    {
        return mTestScript;
    }

    /**
     * @param testScript The testScript to set.
     */
    public void setTestScript(boolean testScript)
    {
        boolean oldValue = mTestScript;
        mTestScript = testScript;
        firePropertyChanged(TEST_SCRIPT, oldValue, testScript);
    }

    /**
     * @return Returns the makePathsRelative.
     */
    public boolean isMakePathsRelative()
    {
        return mMakePathsRelative;
    }

    /**
     * @param makePathsRelative The makePathsRelative to set.
     */
    public void setMakePathsRelative(boolean makePathsRelative)
    {
        boolean oldValue = mMakePathsRelative;
        mMakePathsRelative = makePathsRelative;
        firePropertyChanged(MAKE_PATHS_RELATIVE, oldValue, makePathsRelative);
    }

    public boolean isSaveExternal()
    {
        return mSaveExternal;
    }

    public void setSaveExternal(boolean saveExternal)
    {
        boolean oldValue = mSaveExternal;
        mSaveExternal = saveExternal;
        firePropertyChanged(SAVE_EXTERNAL, oldValue, saveExternal);
    }

    /**
     * @return Returns the savePath.
     */
    public String getSavePath()
    {
        return mSavePath;
    }

    /**
     * @param savePath The savePath to set.
     */
    public void setSavePath(String savePath)
    {
        String oldValue = mSavePath;
        mSavePath = savePath;
        firePropertyChanged(SAVE_PATH, oldValue, savePath);
    }
    /**
     * @return Returns the compressorType.
     */
    public int getCompressorType()
    {
        return mCompressorType;
    }

    /**
     * @param compressorType The compressorType to set.
     */
    public void setCompressorType(int compressorType)
    {
        int oldValue = mCompressorType;
        mCompressorType = compressorType;
        firePropertyChanged(COMPRESSOR_TYPE, oldValue, compressorType);
    }

    public boolean isSolidCompression()
    {
        return mSolidCompression;
    }

    public void setSolidCompression(boolean solidCompression)
    {
        boolean oldValue = mSolidCompression;
        mSolidCompression = solidCompression;
        firePropertyChanged(SOLID_COMPRESSION, oldValue, solidCompression);
    }

    public INSISInstallElement getInstaller()
    {
        return mInstaller;
    }

    public void setInstaller(INSISInstallElement installer)
    {
        INSISInstallElement oldValue = mInstaller;
        mInstaller = installer;
        firePropertyChanged(INSTALLER, oldValue, installer);
    }

    /**
     * @return Returns the autoCloseUninstaller.
     */
    public boolean isAutoCloseUninstaller()
    {
        return mAutoCloseUninstaller;
    }

    /**
     * @param autoCloseUninstaller The autoCloseUninstaller to set.
     */
    public void setAutoCloseUninstaller(boolean autoCloseUninstaller)
    {
        boolean oldValue = mAutoCloseUninstaller;
        mAutoCloseUninstaller = autoCloseUninstaller;
        firePropertyChanged(AUTO_CLOSE_UNINSTALLER, oldValue, autoCloseUninstaller);
    }

    /**
     * @return Returns the openReadmeAfterInstall.
     */
    public String getOpenReadmeAfterInstall()
    {
        return mOpenReadmeAfterInstall;
    }

    /**
     * @param openReadmeAfterInstall The openReadmeAfterInstall to set.
     */
    public void setOpenReadmeAfterInstall(String openReadmeAfterInstall)
    {
        String oldValue = mOpenReadmeAfterInstall;
        mOpenReadmeAfterInstall = openReadmeAfterInstall;
        firePropertyChanged(OPEN_README_AFTER_INSTALL, oldValue, openReadmeAfterInstall);
    }

    /**
     * @return Returns the runProgramAfterInstallParams.
     */
    public String getRunProgramAfterInstallParams()
    {
        return mRunProgramAfterInstallParams;
    }

    /**
     * @param runProgramAfterInstallParams The runProgramAfterInstallParams to set.
     */
    public void setRunProgramAfterInstallParams(String runProgramAfterInstallParams)
    {
        String oldValue = mRunProgramAfterInstallParams;
        mRunProgramAfterInstallParams = runProgramAfterInstallParams;
        firePropertyChanged(RUN_PROGRAM_AFTER_INSTALL_PARAMS, oldValue, runProgramAfterInstallParams);
    }

    /**
     * @return Returns the showUninstDetails.
     */
    public boolean isShowUninstDetails()
    {
        return mShowUninstDetails;
    }

    /**
     * @param showUninstDetails The showUninstDetails to set.
     */
    public void setShowUninstDetails(boolean showUninstDetails)
    {
        boolean oldValue = mShowUninstDetails;
        mShowUninstDetails = showUninstDetails;
        firePropertyChanged(SHOW_UNINST_DETAILS, oldValue, showUninstDetails);
    }

    /**
     * @return Returns the createUninstallerControlPanelEntry.
     */
    public boolean isCreateUninstallerControlPanelEntry()
    {
        return mCreateUninstallerControlPanelEntry;
    }

    /**
     * @param createUninstallerControlPanelEntry The createUninstallerControlPanelEntry to set.
     */
    public void setCreateUninstallerControlPanelEntry(
            boolean createUninstallerControlPanelEntry)
    {
        boolean oldValue = mCreateUninstallerControlPanelEntry;
        mCreateUninstallerControlPanelEntry = createUninstallerControlPanelEntry;
        firePropertyChanged(CREATE_UNINSTALLER_CONTROL_PANEL_ENTRY, oldValue, createUninstallerControlPanelEntry);
    }

    /**
     * @return Returns the createUninstallerStartMenuShortcut.
     */
    public boolean isCreateUninstallerStartMenuShortcut()
    {
        return mCreateUninstallerStartMenuShortcut;
    }

    /**
     * @param createUninstallerStartMenuShortcut The createUninstallerStartMenuShortcut to set.
     */
    public void setCreateUninstallerStartMenuShortcut(
            boolean createUninstallerStartMenuShortcut)
    {
        boolean oldValue = mCreateUninstallerStartMenuShortcut;
        mCreateUninstallerStartMenuShortcut = createUninstallerStartMenuShortcut;
        firePropertyChanged(CREATE_UNINSTALLER_START_MENU_SHORTCUT, oldValue, createUninstallerStartMenuShortcut);
    }

    /**
     * @return Returns the silentUninstaller.
     */
    public boolean isSilentUninstaller()
    {
        return mSilentUninstaller;
    }

    /**
     * @param silentUninstaller The silentUninstaller to set.
     */
    public void setSilentUninstaller(boolean silentUninstaller)
    {
        boolean oldValue = mSilentUninstaller;
        mSilentUninstaller = silentUninstaller;
        firePropertyChanged(SILENT_UNINSTALLER, oldValue, silentUninstaller);
    }

    /**
     * @return Returns the selectComponents.
     */
    public boolean isSelectComponents()
    {
        return mSelectComponents;
    }

    /**
     * @param selectComponents The selectComponents to set.
     */
    public void setSelectComponents(boolean selectComponents)
    {
        boolean oldValue = mSelectComponents;
        mSelectComponents = selectComponents;
        firePropertyChanged(SELECT_COMPONENTS, oldValue, selectComponents);
    }

    public int getTargetPlatform()
    {
        return mTargetPlatform;
    }

    public void setTargetPlatform(int targetPlatform)
    {
        int oldValue = mTargetPlatform;
        if(mTargetPlatform != targetPlatform) {
            mTargetPlatform = targetPlatform;
            setInstallDir(NSISWizardUtil.convertPath(targetPlatform, getInstallDir()));
            setRunProgramAfterInstall(NSISWizardUtil.convertPath(targetPlatform, getRunProgramAfterInstall()));
            setOpenReadmeAfterInstall(NSISWizardUtil.convertPath(targetPlatform, getOpenReadmeAfterInstall()));
            if(mInstaller != null) {
                mInstaller.setTargetPlatform(targetPlatform);
            }
        }
        firePropertyChanged(TARGET_PLATFORM, oldValue, targetPlatform);
    }

    public int getExecutionLevel()
    {
        return mExecutionLevel;
    }

    public void setExecutionLevel(int executionLevel)
    {
        int oldValue = mExecutionLevel;
        mExecutionLevel = executionLevel;
        firePropertyChanged(EXECUTION_LEVEL, oldValue, executionLevel);
    }

    @Override
    protected Object getNodeValue(Node node, String name, Class<?> clasz)
    {
        Object obj = super.getNodeValue(node, name, clasz);
        if(name.equals("installer")) { //$NON-NLS-1$
            NSISInstaller installer = (NSISInstaller)obj;
            if(installer != null) {
                installer.setSettings(this);
            }
        }
        else if(name.equals("languages")) { //$NON-NLS-1$
            if(obj instanceof Collection<?> && ((Collection<?>)obj).isEmpty()) {
                NamedNodeMap attr = node.getAttributes();
                if(attr != null) {
                    String langs = XMLUtil.getStringValue(attr, VALUE_ATTRIBUTE);
                    if(!Common.isEmpty(langs)) {
                        String[] langNames = Common.tokenize(langs, ',');
                        List<NSISLanguage> languages = new ArrayList<NSISLanguage>();
                        for (int i = 0; i < langNames.length; i++) {
                            NSISLanguage language = NSISLanguageManager.getInstance().getLanguage(langNames[i]);
                            if (language != null) {
                                languages.add(language);
                            }
                        }
                        obj = languages;
                    }
                }
            }
        }
        return obj;
    }

    public String getNodeName()
    {
        return NODE;
    }

    @Override
    protected String getChildNodeName()
    {
        return CHILD_NODE;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        NSISWizardSettings settings = (NSISWizardSettings)super.clone();
        settings.mBGTopColor = cloneRGB(mBGTopColor);
        settings.mBGBottomColor = cloneRGB(mBGBottomColor);
        settings.mBGTextColor = cloneRGB(mBGTextColor);
        settings.mLanguages = (mLanguages==null?null:new ArrayList<NSISLanguage>(mLanguages));
        settings.mWizard = null;
        settings.mInstaller = (INSISInstallElement)mInstaller.clone();
        settings.mInstaller.setSettings(settings);
        return settings;
    }

    /**
     * @return
     */
    private RGB cloneRGB(RGB rgb)
    {
        return (rgb==null?null:new RGB(rgb.red,rgb.green,rgb.blue));
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (mAutoCloseInstaller?1231:1237);
        result = PRIME * result + (mAutoCloseUninstaller?1231:1237);
        result = PRIME * result + ((mBGBottomColor == null)?0:mBGBottomColor.hashCode());
        result = PRIME * result + ((mBGTextColor == null)?0:mBGTextColor.hashCode());
        result = PRIME * result + ((mBGTopColor == null)?0:mBGTopColor.hashCode());
        result = PRIME * result + ((mBackgroundBMP == null)?0:mBackgroundBMP.hashCode());
        result = PRIME * result + ((mBackgroundWAV == null)?0:mBackgroundWAV.hashCode());
        result = PRIME * result + (mChangeInstallDir?1231:1237);
        result = PRIME * result + (mChangeStartMenuGroup?1231:1237);
        result = PRIME * result + ((mCompany == null)?0:mCompany.hashCode());
        result = PRIME * result + (mCompileScript?1231:1237);
        result = PRIME * result + mCompressorType;
        result = PRIME * result + (mCreateStartMenuGroup?1231:1237);
        result = PRIME * result + (mCreateUninstaller?1231:1237);
        result = PRIME * result + (mCreateUninstallerControlPanelEntry?1231:1237);
        result = PRIME * result + (mCreateUninstallerStartMenuShortcut?1231:1237);
        result = PRIME * result + (mDisableStartMenuShortcuts?1231:1237);
        result = PRIME * result + (mEnableLanguageSupport?1231:1237);
        result = PRIME * result + mFadeInDelay;
        result = PRIME * result + mFadeOutDelay;
        result = PRIME * result + ((mIcon == null)?0:mIcon.hashCode());
        result = PRIME * result + ((mInstallDir == null)?0:mInstallDir.hashCode());
        result = PRIME * result + ((mInstaller == null)?0:mInstaller.hashCode());
        result = PRIME * result + mInstallerType;
        result = PRIME * result + ((mLanguages == null)?0:mLanguages.hashCode());
        result = PRIME * result + mLicenseButtonType;
        result = PRIME * result + ((mLicenseData == null)?0:mLicenseData.hashCode());
        result = PRIME * result + (mMakePathsRelative?1231:1237);
        result = PRIME * result + ((mName == null)?0:mName.hashCode());
        result = PRIME * result + ((mOpenReadmeAfterInstall == null)?0:mOpenReadmeAfterInstall.hashCode());
        result = PRIME * result + ((mOutFile == null)?0:mOutFile.hashCode());
        result = PRIME * result + ((mRunProgramAfterInstall == null)?0:mRunProgramAfterInstall.hashCode());
        result = PRIME * result + ((mRunProgramAfterInstallParams == null)?0:mRunProgramAfterInstallParams.hashCode());
        result = PRIME * result + (mSaveExternal?1231:1237);
        result = PRIME * result + ((mSavePath == null)?0:mSavePath.hashCode());
        result = PRIME * result + (mSelectComponents?1231:1237);
        result = PRIME * result + (mSelectLanguage?1231:1237);
        result = PRIME * result + (mShowBackground?1231:1237);
        result = PRIME * result + (mShowInstDetails?1231:1237);
        result = PRIME * result + (mShowLicense?1231:1237);
        result = PRIME * result + (mShowSplash?1231:1237);
        result = PRIME * result + (mShowUninstDetails?1231:1237);
        result = PRIME * result + (mSilentUninstaller?1231:1237);
        result = PRIME * result + (mSolidCompression?1231:1237);
        result = PRIME * result + ((mSplashBMP == null)?0:mSplashBMP.hashCode());
        result = PRIME * result + mSplashDelay;
        result = PRIME * result + ((mSplashWAV == null)?0:mSplashWAV.hashCode());
        result = PRIME * result + ((mStartMenuGroup == null)?0:mStartMenuGroup.hashCode());
        result = PRIME * result + (mTestScript?1231:1237);
        result = PRIME * result + ((mUninstallFile == null)?0:mUninstallFile.hashCode());
        result = PRIME * result + ((mUninstallIcon == null)?0:mUninstallIcon.hashCode());
        result = PRIME * result + ((mUrl == null)?0:mUrl.hashCode());
        result = PRIME * result + ((mVersion == null)?0:mVersion.hashCode());
        result = PRIME * result + mTargetPlatform;
        result = PRIME * result + mExecutionLevel;
        result = PRIME * result + ((mMinimumNSISVersion == null)?0:mMinimumNSISVersion.hashCode());
        result = PRIME * result + (mMultiUserInstallation?1231:1237);
        result = PRIME * result + mMultiUserExecLevel;
        result = PRIME * result + mMultiUserInstallMode;
        result = PRIME * result + (mMultiUserInstallModeRemember?1231:1237);
        result = PRIME * result + (mMultiUserInstallModeAsk?1231:1237);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NSISWizardSettings other = (NSISWizardSettings)obj;

        if (mAutoCloseInstaller != other.mAutoCloseInstaller) {
            return false;
        }
        if (mAutoCloseUninstaller != other.mAutoCloseUninstaller) {
            return false;
        }
        if (!Common.objectsAreEqual(mBGBottomColor,other.mBGBottomColor)) {
                return false;
        }
        if (!Common.objectsAreEqual(mBGTextColor,other.mBGTextColor)) {
            return false;
        }
        if (!Common.objectsAreEqual(mBGTopColor,other.mBGTopColor)) {
            return false;
        }
        if (!Common.objectsAreEqual(mBackgroundBMP,other.mBackgroundBMP)) {
            return false;
        }
        if (!Common.objectsAreEqual(mBackgroundWAV,other.mBackgroundWAV)) {
            return false;
        }
        if (mChangeInstallDir != other.mChangeInstallDir) {
            return false;
        }
        if (mChangeStartMenuGroup != other.mChangeStartMenuGroup) {
            return false;
        }
        if (!Common.objectsAreEqual(mCompany,other.mCompany)) {
            return false;
        }
        if (mCompileScript != other.mCompileScript) {
            return false;
        }
        if (mCompressorType != other.mCompressorType) {
            return false;
        }
        if (mCreateStartMenuGroup != other.mCreateStartMenuGroup) {
            return false;
        }
        if (mCreateUninstaller != other.mCreateUninstaller) {
            return false;
        }
        if (mCreateUninstallerControlPanelEntry != other.mCreateUninstallerControlPanelEntry) {
            return false;
        }
        if (mCreateUninstallerStartMenuShortcut != other.mCreateUninstallerStartMenuShortcut) {
            return false;
        }
        if (mDisableStartMenuShortcuts != other.mDisableStartMenuShortcuts) {
            return false;
        }
        if (mEnableLanguageSupport != other.mEnableLanguageSupport) {
            return false;
        }
        if (mFadeInDelay != other.mFadeInDelay) {
            return false;
        }
        if (mFadeOutDelay != other.mFadeOutDelay) {
            return false;
        }
        if (!Common.objectsAreEqual(mIcon,other.mIcon)) {
            return false;
        }
        if (!Common.objectsAreEqual(mInstallDir,other.mInstallDir)) {
            return false;
        }
        if (!Common.objectsAreEqual(mInstaller,other.mInstaller)) {
            return false;
        }
        if (mInstallerType != other.mInstallerType) {
            return false;
        }
        if (!Common.objectsAreEqual(mLanguages,other.mLanguages)) {
            return false;
        }
        if (mLicenseButtonType != other.mLicenseButtonType) {
            return false;
        }
        if (!Common.objectsAreEqual(mLicenseData,other.mLicenseData)) {
            return false;
        }
        if (mMakePathsRelative != other.mMakePathsRelative) {
            return false;
        }
        if (!Common.objectsAreEqual(mName,other.mName)) {
            return false;
        }
        if (!Common.objectsAreEqual(mOpenReadmeAfterInstall,other.mOpenReadmeAfterInstall)) {
            return false;
        }
        if (!Common.objectsAreEqual(mOutFile,other.mOutFile)) {
            return false;
        }
        if (!Common.objectsAreEqual(mRunProgramAfterInstall,other.mRunProgramAfterInstall)) {
            return false;
        }
        if (!Common.objectsAreEqual(mRunProgramAfterInstallParams,other.mRunProgramAfterInstallParams)) {
            return false;
        }
        if (mSaveExternal != other.mSaveExternal) {
            return false;
        }
        if (!Common.objectsAreEqual(mSavePath,other.mSavePath)) {
            return false;
        }
        if (mSelectComponents != other.mSelectComponents) {
            return false;
        }
        if (mSelectLanguage != other.mSelectLanguage) {
            return false;
        }
        if (mShowBackground != other.mShowBackground) {
            return false;
        }
        if (mShowInstDetails != other.mShowInstDetails) {
            return false;
        }
        if (mShowLicense != other.mShowLicense) {
            return false;
        }
        if (mShowSplash != other.mShowSplash) {
            return false;
        }
        if (mShowUninstDetails != other.mShowUninstDetails) {
            return false;
        }
        if (mSilentUninstaller != other.mSilentUninstaller) {
            return false;
        }
        if (mSolidCompression != other.mSolidCompression) {
            return false;
        }
        if (!Common.objectsAreEqual(mSplashBMP,other.mSplashBMP)) {
            return false;
        }
        if (mSplashDelay != other.mSplashDelay) {
            return false;
        }
        if (!Common.objectsAreEqual(mSplashWAV,other.mSplashWAV)) {
            return false;
        }
        if (!Common.objectsAreEqual(mStartMenuGroup,other.mStartMenuGroup)) {
            return false;
        }
        if (mTestScript != other.mTestScript) {
            return false;
        }
        if (!Common.objectsAreEqual(mUninstallFile,other.mUninstallFile)) {
            return false;
        }
        if (!Common.objectsAreEqual(mUninstallIcon,other.mUninstallIcon)) {
            return false;
        }
        if (!Common.objectsAreEqual(mUrl,other.mUrl)) {
            return false;
        }
        if (!Common.objectsAreEqual(mVersion,other.mVersion)) {
            return false;
        }
        if(mTargetPlatform != other.mTargetPlatform) {
            return false;
        }
        if(mExecutionLevel != other.mExecutionLevel) {
            return false;
        }
        if (!Common.objectsAreEqual(mMinimumNSISVersion,other.mMinimumNSISVersion)) {
            return false;
        }
        if(mMultiUserInstallation != other.mMultiUserInstallation) {
            return false;
        }
        if(mMultiUserExecLevel != other.mMultiUserExecLevel) {
            return false;
        }
        if(mMultiUserInstallMode != other.mMultiUserInstallMode) {
            return false;
        }
        if(mMultiUserInstallModeRemember != other.mMultiUserInstallModeRemember) {
            return false;
        }
        if(mMultiUserInstallModeAsk != other.mMultiUserInstallModeAsk) {
            return false;
        }
        return true;
    }

    public Version getMinimumNSISVersion()
    {
        return mMinimumNSISVersion;
    }

    private void updateMinimumNSISVersion()
    {
        Version minimumNSISVersion = null;
        if(getInstallerType() == INSTALLER_TYPE_MUI2) {
            minimumNSISVersion = INSISVersions.VERSION_2_34;
            if(isMultiUserInstallation()) {
                minimumNSISVersion = INSISVersions.VERSION_2_35;
            }
        }
        setMinimumNSISVersion(minimumNSISVersion);
    }

    public void setMinimumNSISVersion(Version minimumNSISVersion)
    {
        Version oldValue = mMinimumNSISVersion;
        mMinimumNSISVersion = minimumNSISVersion;
        firePropertyChanged(MINIMUM_NSIS_VERSION, oldValue, minimumNSISVersion);
    }

    public boolean isMultiUserInstallation()
    {
        return mMultiUserInstallation;
    }

    public void setMultiUserInstallation(boolean multiUserInstallation)
    {
        boolean oldValue = mMultiUserInstallation;
        mMultiUserInstallation = multiUserInstallation;
        firePropertyChanged(MULTIUSER_INSTALLATION, oldValue, multiUserInstallation);
        updateMinimumNSISVersion();
    }

    public int getMultiUserExecLevel()
    {
        return mMultiUserExecLevel;
    }

    public void setMultiUserExecLevel(int multiUserExecLevel)
    {
        int oldValue = mMultiUserExecLevel;
        mMultiUserExecLevel = multiUserExecLevel;
        firePropertyChanged(MULTIUSER_EXEC_LEVEL, oldValue, multiUserExecLevel);
    }

    public int getMultiUserInstallMode()
    {
        return mMultiUserInstallMode;
    }

    public void setMultiUserInstallMode(int multiUserInstallMode)
    {
        int oldValue = mMultiUserInstallMode;
        mMultiUserInstallMode = multiUserInstallMode;
        firePropertyChanged(MULTIUSER_INSTALL_MODE, oldValue, multiUserInstallMode);
    }

    public boolean isMultiUserInstallModeRemember()
    {
        return mMultiUserInstallModeRemember;
    }

    public void setMultiUserInstallModeRemember(boolean multiUserInstallModeRemember)
    {
        boolean oldValue = mMultiUserInstallModeRemember;
        mMultiUserInstallModeRemember = multiUserInstallModeRemember;
        firePropertyChanged(MULTIUSER_INSTALL_MODE_REMEMBER, oldValue, multiUserInstallModeRemember);
    }

    public boolean isMultiUserInstallModeAsk()
    {
        return mMultiUserInstallModeAsk;
    }

    public void setMultiUserInstallModeAsk(boolean multiUserInstallModeAsk)
    {
        boolean oldValue = mMultiUserInstallModeAsk;
        mMultiUserInstallModeAsk = multiUserInstallModeAsk;
        firePropertyChanged(MULTIUSER_INSTALL_MODE_ASK, oldValue, multiUserInstallModeAsk);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        mDisplaySupportedLanguages = true;
        in.defaultReadObject();
    }
}
