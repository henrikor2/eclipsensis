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

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.ui.views.properties.*;

public class InstallOptionsDirRequest extends InstallOptionsPathRequest
{
    private static final long serialVersionUID = 8683714309397828717L;
    private String mRoot;

    public boolean usesOtherTab()
    {
        return false;
    }

    protected InstallOptionsDirRequest(INISection section)
    {
        super(section);
    }

    @Override
    protected void init()
    {
        super.init();
        mRoot = ""; //$NON-NLS-1$
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_DIRREQUEST;
    }

    @Override
    public Object clone()
    {
        InstallOptionsDirRequest clone = (InstallOptionsDirRequest)super.clone();
        clone.setRoot(getRoot());
        return clone;
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_ROOT)) {
            list.add(InstallOptionsModel.PROPERTY_ROOT);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_ROOT)) {
            String propertyName = InstallOptionsPlugin.getResourceString("root.property.name"); //$NON-NLS-1$
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_ROOT, propertyName);
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
        if (InstallOptionsModel.PROPERTY_ROOT.equals(propName)) {
            return getRoot();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_ROOT)) {
            setRoot((String)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public String getRoot()
    {
        return mRoot;
    }

    public void setRoot(String root)
    {
        if(!mRoot.equals(root)) {
            String oldRoot = mRoot;
            mRoot = root;
            firePropertyChange(InstallOptionsModel.PROPERTY_ROOT, oldRoot, mRoot);
            setDirty(true);
        }
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new DirRequestPropertySectionCreator(this);
    }
}
