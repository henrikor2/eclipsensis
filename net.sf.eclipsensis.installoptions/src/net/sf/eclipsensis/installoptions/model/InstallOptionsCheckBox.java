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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.descriptors.CustomComboBoxPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class InstallOptionsCheckBox extends InstallOptionsUneditableElement
{
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_STATE = 0;
    private static final Integer[] STATE_DATA = {InstallOptionsModel.STATE_DEFAULT,
                                                 InstallOptionsModel.STATE_UNCHECKED,
                                                 InstallOptionsModel.STATE_CHECKED};
    private static final String[] STATE_DISPLAY = {InstallOptionsPlugin.getResourceString("state.default"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.unchecked"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.checked")}; //$NON-NLS-1$

    private Integer mState;

    protected InstallOptionsCheckBox(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("stateDefault"); //$NON-NLS-1$
        skippedProperties.add("stateDisplay"); //$NON-NLS-1$
        skippedProperties.add("stateData"); //$NON-NLS-1$
    }

    @Override
    protected void init()
    {
        super.init();
        mState = null;
    }

    @Override
    public Object clone()
    {
        InstallOptionsCheckBox clone = (InstallOptionsCheckBox)super.clone();
        clone.setState(getState());
        return clone;
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            list.add(InstallOptionsModel.PROPERTY_STATE);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    @Override
    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_STATE.equals(propName)) {
            return getState();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_STATE)) {
            setState((Integer)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public Integer getState()
    {
        return mState;
    }

    public void setState(Integer state)
    {
        if(!Common.objectsAreEqual(mState,state)) {
            Integer oldState = mState;
            mState = state;
            firePropertyChange(InstallOptionsModel.PROPERTY_STATE, oldState, mState);
            setDirty(true);
        }
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_CHECKBOX;
    }

    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,65,10);
    }

    @Override
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("checkbox.text.default"); //$NON-NLS-1$
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            String propertyName = InstallOptionsPlugin.getResourceString("state.property.name"); //$NON-NLS-1$
            CustomComboBoxPropertyDescriptor descriptor = new CustomComboBoxPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE,
                    propertyName, getStateData(), getStateDisplay(), getStateDefault());
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    /**
     * @return
     */
    public int getStateDefault()
    {
        return DEFAULT_STATE;
    }

    /**
     * @return
     */
    public String[] getStateDisplay()
    {
        return STATE_DISPLAY;
    }

    /**
     * @return
     */
    public Integer[] getStateData()
    {
        return STATE_DATA;
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new CheckBoxPropertySectionCreator(this);
    }

    @Override
    protected TypeConverter<?> loadTypeConverter(String property, Object value)
    {
        if (InstallOptionsModel.PROPERTY_STATE.equals(property)) {
            if(value instanceof String) {
                if(((String)value).regionMatches(true,0,"0x",0,2)) { //$NON-NLS-1$
                    return TypeConverter.HEX_CONVERTER;
                }
            }
            return TypeConverter.INTEGER_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }
}
