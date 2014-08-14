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
import net.sf.eclipsensis.installoptions.properties.descriptors.MultiLineTextPropertyDescriptor;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.*;

import org.eclipse.ui.views.properties.*;

public abstract class InstallOptionsEditableElement extends InstallOptionsWidget
{
    /**
     *
     */
    private static final long serialVersionUID = -9002023506652287815L;
    private String mText;
    private String mState;
    private String mMaxLen;
    private String mMinLen;
    private String mValidateText;

    protected InstallOptionsEditableElement(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("defaultMaxLen"); //$NON-NLS-1$
        skippedProperties.add("defaultMinLen"); //$NON-NLS-1$
        skippedProperties.add("defaultState"); //$NON-NLS-1$
    }

    @Override
    protected void init()
    {
        super.init();
        mText = ""; //$NON-NLS-1$
        mState = ""; //$NON-NLS-1$
        mMaxLen = ""; //$NON-NLS-1$
        mMinLen = ""; //$NON-NLS-1$
        mValidateText = ""; //$NON-NLS-1$
    }

    @Override
    protected void setDefaults()
    {
        super.setDefaults();
        mState = getDefaultState();
    }

    @Override
    public Object clone()
    {
        InstallOptionsEditableElement clone = (InstallOptionsEditableElement)super.clone();
        clone.setMaxLen(getMaxLen());
        clone.setMinLen(getMinLen());
        clone.setText(getText());
        clone.setState(getState());
        clone.setValidateText(getValidateText());
        return clone;
    }

    protected String getDefaultState()
    {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            list.add(InstallOptionsModel.PROPERTY_TEXT);
        }
        else if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            list.add(InstallOptionsModel.PROPERTY_STATE);
        }
        else if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_MAXLEN)) {
            list.add(InstallOptionsModel.PROPERTY_MAXLEN);
        }
        else if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_MINLEN)) {
            list.add(InstallOptionsModel.PROPERTY_MINLEN);
        }
        else if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_VALIDATETEXT)) {
            list.add(InstallOptionsModel.PROPERTY_VALIDATETEXT);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("text.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_TEXT, propertyName);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            String propertyName = InstallOptionsPlugin.getResourceString("state.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, propertyName);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_MAXLEN)) {
            String propertyName = InstallOptionsPlugin.getResourceString("maxlen.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_MAXLEN, propertyName);
            NumberCellEditorValidator validator = new NumberCellEditorValidator(propertyName,getDefaultMinLen(),getDefaultMaxLen(),true) {
                @Override
                public int getMinValue()
                {
                    String minLen = getMinLen();
                    try {
                        return Integer.parseInt(minLen);
                    }
                    catch(NumberFormatException nfe) {
                        return getDefaultMinLen();
                    }
                }
            };
            descriptor.setValidator(validator);
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_MINLEN)) {
            String propertyName = InstallOptionsPlugin.getResourceString("minlen.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_MINLEN, propertyName);
            NumberCellEditorValidator validator = new NumberCellEditorValidator(propertyName,getDefaultMinLen(),getDefaultMaxLen(),true) {
                @Override
                public int getMaxValue()
                {
                    String maxLen = getMaxLen();
                    try {
                        return Integer.parseInt(maxLen);
                    }
                    catch(NumberFormatException nfe) {
                        return getDefaultMaxLen();
                    }
                }
            };
            descriptor.setValidator(validator);
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_VALIDATETEXT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("validatetext.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new MultiLineTextPropertyDescriptor(this, InstallOptionsModel.PROPERTY_VALIDATETEXT, propertyName);
            descriptor.setValidator(new NSISEscapedStringLengthValidator(propertyName));
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    protected int getDefaultMinLen()
    {
        return 0;
    }

    protected int getDefaultMaxLen()
    {
        return InstallOptionsModel.INSTANCE.getMaxLength();
    }

    @Override
    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_TEXT.equals(propName)) {
            return getText();
        }
        if (InstallOptionsModel.PROPERTY_STATE.equals(propName)) {
            return getState();
        }
        if (InstallOptionsModel.PROPERTY_MAXLEN.equals(propName)) {
            return getMaxLen();
        }
        if (InstallOptionsModel.PROPERTY_MINLEN.equals(propName)) {
            return getMinLen();
        }
        if (InstallOptionsModel.PROPERTY_VALIDATETEXT.equals(propName)) {
            return getValidateText();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            setText((String)value);
        }
        else if(id.equals(InstallOptionsModel.PROPERTY_STATE)) {
            setState((String)value);
        }
        else if(id.equals(InstallOptionsModel.PROPERTY_MAXLEN)) {
            setMaxLen((String)value);
        }
        else if(id.equals(InstallOptionsModel.PROPERTY_MINLEN)) {
            setMinLen((String)value);
        }
        else if(id.equals(InstallOptionsModel.PROPERTY_VALIDATETEXT)) {
            setValidateText((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,122,13);
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String text)
    {
        if(!mText.equals(text)) {
            String oldText = mText;
            mText = text;
            firePropertyChange(InstallOptionsModel.PROPERTY_TEXT, oldText, mText);
            setDirty(true);
        }
    }

    public String getState()
    {
        return mState;
    }

    public void setState(String state)
    {
        if(!mState.equals(state)) {
            String oldState = mState;
            mState = state;
            firePropertyChange(InstallOptionsModel.PROPERTY_STATE, oldState, mState);
            setDirty(true);
        }
    }

    public String getMaxLen()
    {
        return mMaxLen;
    }

    public void setMaxLen(String maxLen)
    {
        if(!mMaxLen.equals(maxLen)) {
            String oldMaxLen = mMaxLen;
            mMaxLen = maxLen;
            firePropertyChange(InstallOptionsModel.PROPERTY_MAXLEN, oldMaxLen, mMaxLen);
            setDirty(true);
        }
    }

    public String getMinLen()
    {
        return mMinLen;
    }

    public void setMinLen(String minLen)
    {
        if(!mMinLen.equals(minLen)) {
            String oldMinLen = mMinLen;
            mMinLen = minLen;
            firePropertyChange(InstallOptionsModel.PROPERTY_MINLEN, oldMinLen, mMinLen);
            setDirty(true);
        }
    }

    public String getValidateText()
    {
        return mValidateText;
    }

    public void setValidateText(String validateText)
    {
        if(!mValidateText.equals(validateText)) {
            String oldValidateText = mValidateText;
            mValidateText = validateText;
            firePropertyChange(InstallOptionsModel.PROPERTY_VALIDATETEXT, oldValidateText, mValidateText);
            setDirty(true);
        }
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new EditableElementPropertySectionCreator(this);
    }
}
