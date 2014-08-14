/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.properties.validators.NumberCellEditorValidator;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.ui.views.properties.*;

public class DimensionPropertySource implements IPropertySource
{
    public static final String ID_WIDTH = "width"; //$NON-NLS-1$

    public static final String ID_HEIGHT = "height";//$NON-NLS-1$

    protected static IPropertyDescriptor[] mDescriptors;

    static {
        String propertyName = InstallOptionsPlugin.getResourceString("width.property.name"); //$NON-NLS-1$
        PropertyDescriptor widthProp = new TextPropertyDescriptor(ID_WIDTH, propertyName);
        widthProp.setValidator(new NumberCellEditorValidator(propertyName));
        propertyName = InstallOptionsPlugin.getResourceString("height.property.name"); //$NON-NLS-1$
        PropertyDescriptor heightProp = new TextPropertyDescriptor(ID_HEIGHT, propertyName);
        heightProp.setValidator(new NumberCellEditorValidator(propertyName));
        mDescriptors = new IPropertyDescriptor[]{widthProp,heightProp};
    }

    protected Dimension mDimension = null;

    public DimensionPropertySource(Dimension dimension)
    {
        this.mDimension = dimension.getCopy();
    }

    public Object getEditableValue()
    {
        return mDimension.getCopy();
    }

    public Object getPropertyValue(Object propName)
    {
        return getPropertyValue((String)propName);
    }

    public Object getPropertyValue(String propName)
    {
        if (ID_HEIGHT.equals(propName)) {
            return new Integer(mDimension.height).toString();
        }
        if (ID_WIDTH.equals(propName)) {
            return new Integer(mDimension.width).toString();
        }
        return null;
    }

    public void setPropertyValue(Object propName, Object value)
    {
        setPropertyValue((String)propName, value);
    }

    public void setPropertyValue(String propName, Object value)
    {
        if (ID_HEIGHT.equals(propName)) {
            Integer newInt = new Integer((String)value);
            mDimension.height = newInt.intValue();
        }
        if (ID_WIDTH.equals(propName)) {
            Integer newInt = new Integer((String)value);
            mDimension.width = newInt.intValue();
        }
    }

    public IPropertyDescriptor[] getPropertyDescriptors()
    {
        return mDescriptors;
    }

    public void resetPropertyValue(String propName)
    {
    }

    public void resetPropertyValue(Object propName)
    {
    }

    public boolean isPropertySet(Object propName)
    {
        return true;
    }

    public boolean isPropertySet(String propName)
    {
        if (ID_HEIGHT.equals(propName) || ID_WIDTH.equals(propName)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return new StringBuffer("(").append(mDimension.width).append(",").append(mDimension.height).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}