/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.CaseInsensitiveSet;

import org.eclipse.gef.EditPart;

public class InstallOptionsModelTypeDef
{
    private static final Class<?>[] cModelParamTypes = new Class[]{INISection.class};
    private String mType;
    private String mName;
    private String mDescription;
    private String mLargeIcon;
    private String mSmallIcon;
    private String mDisplayProperty;
    private Constructor<? extends InstallOptionsElement> mModelConstructor;
    private Constructor<? extends EditPart> mEditPartConstructor;
    private Set<String> mFlags = null;
    private Set<String> mSettings = null;

    InstallOptionsModelTypeDef(String type, String name, String description, String smallIcon, String largeIcon, String displayProperty, String model, String part)
    {
        mType = type;
        mName = InstallOptionsPlugin.getResourceString(name);
        mDescription = InstallOptionsPlugin.getResourceString(description);
        mSmallIcon = InstallOptionsPlugin.getResourceString(smallIcon);
        mLargeIcon = InstallOptionsPlugin.getResourceString(largeIcon);
        mDisplayProperty = displayProperty;
        mModelConstructor = createConstructor(model, cModelParamTypes);
        mEditPartConstructor = createConstructor(part, null);
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> createConstructor(String name, Class<?>[] paramTypes)
    {
        Constructor<T> constructor = null;

        try {
            Class<T> clasz = (Class<T>) Class.forName(name);
            constructor = clasz.getDeclaredConstructor(paramTypes);
        }
        catch (Exception e) {
            constructor = null;
            InstallOptionsPlugin.getDefault().log(e);
        }

        return constructor;
    }

    public String getDisplayProperty()
    {
        return mDisplayProperty;
    }

    public String getSmallIcon()
    {
        return mSmallIcon;
    }

    public String getType()
    {
        return mType;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public String getLargeIcon()
    {
        return mLargeIcon;
    }

    public String getName()
    {
        return mName;
    }

    public InstallOptionsElement createModel(INISection section)
    {
        InstallOptionsElement model = null;
        if(mModelConstructor != null) {
            try {
                model = mModelConstructor.newInstance(new Object[]{section});
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
                model = null;
            }
        }
        return model;
    }

    public EditPart createEditPart()
    {
        EditPart part = null;
        if(mEditPartConstructor != null) {
            try {
                part = mEditPartConstructor.newInstance((Object[])null);
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
                part = null;
            }
        }
        return part;
    }

    public Collection<String> getFlags()
    {
        return (mFlags == null?Collections.<String>emptySet():Collections.unmodifiableSet(mFlags));
    }

    void setFlags(Collection<String> flags)
    {
        if(mFlags == null) {
            if(flags == null) {
                return;
            }
            mFlags = new CaseInsensitiveSet();
        }
        mFlags.clear();
        if(flags != null) {
            mFlags.addAll(flags);
        }
    }

    public Collection<String> getSettings()
    {
        return (mSettings == null?Collections.<String>emptySet():Collections.unmodifiableSet(mSettings));
    }

    void setSettings(Collection<String> settings)
    {
        if(mSettings == null) {
            if(settings == null) {
                return;
            }
            mSettings = new CaseInsensitiveSet();
        }
        mSettings.clear();
        if(settings != null) {
            mSettings.addAll(settings);
        }
    }

}