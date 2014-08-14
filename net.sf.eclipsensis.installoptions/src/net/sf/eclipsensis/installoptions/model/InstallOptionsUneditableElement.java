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
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.ui.views.properties.*;

public abstract class InstallOptionsUneditableElement extends InstallOptionsWidget
{
    /**
     *
     */
    private static final long serialVersionUID = -7057456623768374043L;
    private String mText;

    protected InstallOptionsUneditableElement(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("defaultText"); //$NON-NLS-1$
    }

    @Override
    protected void init()
    {
        super.init();
        mText = ""; //$NON-NLS-1$
    }

    /**
     * @param type
     */
    @Override
    protected void setDefaults()
    {
        super.setDefaults();
        mText = getDefaultText();
    }

    @Override
    public Object clone()
    {
        InstallOptionsUneditableElement clone = (InstallOptionsUneditableElement)super.clone();
        clone.setText(getText());
        return clone;
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            list.add(InstallOptionsModel.PROPERTY_TEXT);
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
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    @Override
    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_TEXT.equals(propName)) {
            return getText();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            setText((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String s)
    {
        if(!mText.equals(s)) {
            String oldText = mText;
            mText = s;
            firePropertyChange(InstallOptionsModel.PROPERTY_TEXT, oldText, mText);
            setDirty(true);
        }
    }

    protected String getDefaultText()
    {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new UneditableElementPropertySectionCreator(this);
    }
}