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

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallLibraryDialog;
import net.sf.eclipsensis.wizard.util.NSISWizardUtil;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallLibrary extends AbstractNSISInstallItem
{
    private static final long serialVersionUID = -3834188758921066360L;

    public static final String TYPE = "Library"; //$NON-NLS-1$

    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.library.icon")); //$NON-NLS-1$

    static {
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.library.type.name"), IMAGE, NSISInstallLibrary.class); //$NON-NLS-1$
    }

    private String mName;

    private boolean mShared = true;

    private String mDestination = NSISKeywords.getInstance().getKeyword("$INSTDIR"); //$NON-NLS-1$

    private int mLibType = LIBTYPE_DLL;

    private boolean mProtected = true;

    private boolean mReboot = true;

    private boolean mRemoveOnUninstall = true;

    private boolean mRefreshShell = false;

    private boolean mUnloadLibraries = false;

    private boolean mIgnoreVersion = false;

    private boolean mX64 = false;

    @Override
    public String doValidate()
    {
        if (!IOUtility.isValidFile(IOUtility.decodePath(getName()))) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.name.error"); //$NON-NLS-1$
        }
        else if (!NSISWizardUtil.isValidNSISPathName(getSettings().getTargetPlatform(), getDestination())) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.destination.error"); //$NON-NLS-1$
        }
        else {
            return super.doValidate();
        }
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallLibraryDialog(wizard, this).open() == Window.OK;
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
        final NSISInstallLibrary other = (NSISInstallLibrary)obj;
        if (mDestination == null) {
            if (other.mDestination != null) {
                return false;
            }
        }
        else if (!mDestination.equals(other.mDestination)) {
            return false;
        }
        if (mLibType != other.mLibType) {
            return false;
        }
        if (mName == null) {
            if (other.mName != null) {
                return false;
            }
        }
        else if (!mName.equals(other.mName)) {
            return false;
        }
        if (mProtected != other.mProtected) {
            return false;
        }
        if (mReboot != other.mReboot) {
            return false;
        }
        if (mRefreshShell != other.mRefreshShell) {
            return false;
        }
        if (mRemoveOnUninstall != other.mRemoveOnUninstall) {
            return false;
        }
        if (mShared != other.mShared) {
            return false;
        }
        if (mUnloadLibraries != other.mUnloadLibraries) {
            return false;
        }
        return true;
    }

    public String getDestination()
    {
        return mDestination;
    }

    public String getDisplayName()
    {
        return mName;
    }

    public Image getImage()
    {
        return IMAGE;
    }

    public int getLibType()
    {
        return mLibType;
    }

    public String getName()
    {
        return mName;
    }

    public String getType()
    {
        return TYPE;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (mDestination == null?0:mDestination.hashCode());
        result = PRIME * result + mLibType;
        result = PRIME * result + (mName == null?0:mName.hashCode());
        result = PRIME * result + (mProtected?1231:1237);
        result = PRIME * result + (mReboot?1231:1237);
        result = PRIME * result + (mRefreshShell?1231:1237);
        result = PRIME * result + (mRemoveOnUninstall?1231:1237);
        result = PRIME * result + (mShared?1231:1237);
        result = PRIME * result + (mUnloadLibraries?1231:1237);
        return result;
    }

    public boolean isEditable()
    {
        return true;
    }

    public boolean isIgnoreVersion()
    {
        return mIgnoreVersion;
    }

    public boolean isProtected()
    {
        return mProtected;
    }

    public boolean isReboot()
    {
        return mReboot;
    }

    public boolean isRefreshShell()
    {
        return mRefreshShell;
    }

    public boolean isRemoveOnUninstall()
    {
        return mRemoveOnUninstall;
    }

    public boolean isShared()
    {
        return mShared;
    }

    public boolean isUnloadLibraries()
    {
        return mUnloadLibraries;
    }

    public boolean isX64()
    {
        return mX64;
    }

    public void setDestination(String destination)
    {
        if (!Common.stringsAreEqual(mDestination, destination)) {
            setDirty();
            mDestination = destination;
        }
    }

    public void setIgnoreVersion(boolean ignoreVersion)
    {
        if (mIgnoreVersion != ignoreVersion) {
            setDirty();
            mIgnoreVersion = ignoreVersion;
        }
    }

    public void setLibType(int libType)
    {
        int libType2 = libType;
        switch (libType2)
        {
            case LIBTYPE_DLL:
            case LIBTYPE_REGDLL:
            case LIBTYPE_TLB:
            case LIBTYPE_REGDLLTLB:
                break;
            case LIBTYPE_REGEXE:
                if(NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_42) >= 0) {
                    break;
                }
                //$FALL-THROUGH$
            default:
                libType2 = LIBTYPE_DLL;
        }
        if (mLibType != libType2) {
            setDirty();
            mLibType = libType2;
        }
    }

    public void setName(String name)
    {
        if (!Common.stringsAreEqual(mName, name)) {
            setDirty();
            mName = name;
        }
    }

    public void setProtected(boolean protected1)
    {
        if (mProtected != protected1) {
            setDirty();
            mProtected = protected1;
        }
    }

    public void setReboot(boolean reboot)
    {
        if (mReboot != reboot) {
            setDirty();
            mReboot = reboot;
        }
    }

    public void setRefreshShell(boolean refreshShell)
    {
        if (mRefreshShell != refreshShell) {
            setDirty();
            mRefreshShell = refreshShell;
        }
    }

    public void setRemoveOnUninstall(boolean removeOnUninstall)
    {
        if (mRemoveOnUninstall != removeOnUninstall) {
            setDirty();
            mRemoveOnUninstall = removeOnUninstall;
        }
    }

    public void setShared(boolean shared)
    {
        if (mShared != shared) {
            setDirty();
            mShared = shared;
        }
    }

    @Override
    public void setTargetPlatform(int targetPlatform)
    {
        super.setTargetPlatform(targetPlatform);
        setDestination(NSISWizardUtil.convertPath(targetPlatform, getDestination()));
        switch (targetPlatform)
        {
            case INSISWizardConstants.TARGET_PLATFORM_X64:
                setX64(true);
                break;
            case INSISWizardConstants.TARGET_PLATFORM_X86:
                setX64(false);
                break;
        }
    }

    public void setUnloadLibraries(boolean unloadLibraries)
    {
        if (mUnloadLibraries != unloadLibraries) {
            setDirty();
            mUnloadLibraries = unloadLibraries;
        }
    }

    public void setX64(boolean x64)
    {
        if (mX64 != x64) {
            setDirty();
            mX64 = x64;
        }
    }
}
