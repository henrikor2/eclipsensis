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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallShortcutDialog;
import net.sf.eclipsensis.wizard.util.NSISWizardUtil;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallShortcut extends AbstractNSISInstallItem
{
    private static final long serialVersionUID = 7567273788917909918L;
    public static final String STARTMENUGROUP_SHORTCUT_LOCATION;

    public static final String TYPE = "Shortcut"; //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.shortcut.icon")); //$NON-NLS-1$

    private boolean mCreateInStartMenuGroup = false;
    private String mName = null;
    private String mLocation = null;
    private String mUrl = null;
    private String mPath = null;
    private int mShortcutType = SHORTCUT_URL;

    static {
        STARTMENUGROUP_SHORTCUT_LOCATION = EclipseNSISPlugin.getResourceString("wizard.smgroup.shortcut.location","$SMPROGRAMS\\$StartMenuGroup"); //$NON-NLS-1$ //$NON-NLS-2$
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.shortcut.type.name"), IMAGE, NSISInstallShortcut.class); //$NON-NLS-1$
    }

    public boolean isCreateInStartMenuGroup()
    {
        return mCreateInStartMenuGroup;
    }

    public void setCreateInStartMenuGroup(boolean createInStartMenuGroup)
    {
        setLocationAndCreateInStartMenuGroup((createInStartMenuGroup?"":mLocation), createInStartMenuGroup); //$NON-NLS-1$
    }

    private void setLocationAndCreateInStartMenuGroup(String location, boolean createInStartMenuGroup)
    {
        if(mCreateInStartMenuGroup != createInStartMenuGroup) {
            setDirty();
            mCreateInStartMenuGroup = createInStartMenuGroup;
        }
        if(!Common.stringsAreEqual(mLocation, location)) {
            setDirty();
            mLocation = location;
        }
    }

    /**
     * @return Returns the path.
     */
    public String getPath()
    {
        return mPath;
    }
    /**
     * @param path The path to set.
     */
    public void setPath(String path)
    {
        if(!Common.stringsAreEqual(mPath, path)) {
            setDirty();
            mPath = path;
        }
    }
    /**
     * @return Returns the location.
     */
    public String getLocation()
    {
        if(mLocation != null && mLocation.startsWith(STARTMENUGROUP_SHORTCUT_LOCATION)) {
            mCreateInStartMenuGroup = true;
            mLocation = ""; //$NON-NLS-1$
        }
        return mLocation;
    }

    /**
     * @param location The location to set.
     */
    public void setLocation(String location)
    {
        if(location.startsWith(STARTMENUGROUP_SHORTCUT_LOCATION)) {
            setLocationAndCreateInStartMenuGroup("", true); //$NON-NLS-1$
        }
        else {
            setLocationAndCreateInStartMenuGroup(location, false);
        }
    }
    /**
     * @return Returns the uRL.
     */
    public String getUrl()
    {
        return mUrl;
    }
    /**
     * @param url The uRL to set.
     */
    public void setUrl(String url)
    {
        if(!Common.stringsAreEqual(mUrl, url)) {
            setDirty();
            mUrl = url;
        }
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return mName;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallShortcutDialog(wizard,this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
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
        if(!Common.stringsAreEqual(mName, name)) {
            setDirty();
            mName = name;
        }
    }

    /**
     * @return Returns the shortcutType.
     */
    public int getShortcutType()
    {
        return mShortcutType;
    }

    /**
     * @param shortcutType The shortcutType to set.
     */
    public void setShortcutType(int shortcutType)
    {
        if(mShortcutType != shortcutType) {
            setDirty();
            mShortcutType = shortcutType;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#setSettings(net.sf.eclipsensis.wizard.settings.NSISWizardSettings)
     */
    @Override
    public void setSettings(NSISWizardSettings settings)
    {
        super.setSettings(settings);
        if(settings != null && !Common.isEmpty(getSettings().getStartMenuGroup()) && Common.isEmpty(mLocation)) {
            setLocationAndCreateInStartMenuGroup(mLocation, true);
        }
    }
    @Override
    public String doValidate()
    {
        if(isCreateInStartMenuGroup()) {
            if(!getSettings().isCreateStartMenuGroup()) {
                return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.no.smgroup.error"); //$NON-NLS-1$
            }
        }
        else if(!NSISWizardUtil.isValidNSISPathName(getSettings().getTargetPlatform(), getLocation())) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.location.error"); //$NON-NLS-1$
        }
        if(!IOUtility.isValidFileName(getName())) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.name.error"); //$NON-NLS-1$
        }
        int n = getShortcutType();
        if((n == SHORTCUT_INSTALLELEMENT && !NSISWizardUtil.isValidNSISPathName(getSettings().getTargetPlatform(), getPath()))) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.file.error"); //$NON-NLS-1$
        }
        else if((n == SHORTCUT_URL && !IOUtility.isValidURL(getUrl()))) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.url.error"); //$NON-NLS-1$
        }
        else {
            return super.doValidate();
        }
    }

    @Override
    public void setTargetPlatform(int targetPlatform)
    {
        super.setTargetPlatform(targetPlatform);
        setLocation(NSISWizardUtil.convertPath(targetPlatform, getLocation()));
        setPath(NSISWizardUtil.convertPath(targetPlatform, getPath()));
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (mCreateInStartMenuGroup?1231:1237);
        result = PRIME * result + ((mLocation == null)?0:mLocation.hashCode());
        result = PRIME * result + ((mName == null)?0:mName.hashCode());
        result = PRIME * result + ((mPath == null)?0:mPath.hashCode());
        result = PRIME * result + mShortcutType;
        result = PRIME * result + ((mUrl == null)?0:mUrl.hashCode());
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
        final NSISInstallShortcut other = (NSISInstallShortcut)obj;
        if (mCreateInStartMenuGroup != other.mCreateInStartMenuGroup) {
            return false;
        }
        if (mLocation == null) {
            if (other.mLocation != null) {
                return false;
            }
        }
        else if (!mLocation.equals(other.mLocation)) {
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
        if (mPath == null) {
            if (other.mPath != null) {
                return false;
            }
        }
        else if (!mPath.equals(other.mPath)) {
            return false;
        }
        if (mShortcutType != other.mShortcutType) {
            return false;
        }
        if (mUrl == null) {
            if (other.mUrl != null) {
                return false;
            }
        }
        else if (!mUrl.equals(other.mUrl)) {
            return false;
        }
        return true;
    }
}
