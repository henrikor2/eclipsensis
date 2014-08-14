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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;

public class PositionPropertySource implements IPropertySource
{
    public static final String ID_LEFT = InstallOptionsModel.PROPERTY_LEFT;
    public static final String ID_TOP = InstallOptionsModel.PROPERTY_TOP;
    public static final String ID_RIGHT = InstallOptionsModel.PROPERTY_RIGHT;
    public static final String ID_BOTTOM = InstallOptionsModel.PROPERTY_BOTTOM;

    private static Map<String, IntegerCellEditorValidator> cValidators = new HashMap<String, IntegerCellEditorValidator>();

    static {
        cValidators.put(ID_LEFT, new IntegerCellEditorValidator(InstallOptionsPlugin.getResourceString("left.property.name"))); //$NON-NLS-1$
        cValidators.put(ID_TOP, new IntegerCellEditorValidator(InstallOptionsPlugin.getResourceString("top.property.name"))); //$NON-NLS-1$
        cValidators.put(ID_RIGHT, new IntegerCellEditorValidator(InstallOptionsPlugin.getResourceString("right.property.name"))); //$NON-NLS-1$
        cValidators.put(ID_BOTTOM, new IntegerCellEditorValidator(InstallOptionsPlugin.getResourceString("bottom.property.name"))); //$NON-NLS-1$
    }

    private IPropertyDescriptor[] mDescriptors;

    private void createDescriptors()
    {
        IntegerCellEditorValidator validator = cValidators.get(ID_LEFT);
        PropertyDescriptor leftProp = new CustomTextPropertyDescriptor(ID_LEFT, validator.getPropertyName());
        leftProp.setValidator(validator);

        validator = cValidators.get(ID_TOP);
        PropertyDescriptor topProp = new CustomTextPropertyDescriptor(ID_TOP, validator.getPropertyName());
        topProp.setValidator(validator);

        validator = cValidators.get(ID_RIGHT);
        PropertyDescriptor rightProp = new CustomTextPropertyDescriptor(ID_RIGHT, validator.getPropertyName());
        rightProp.setValidator(validator);

        validator = cValidators.get(ID_BOTTOM);
        PropertyDescriptor bottomProp = new CustomTextPropertyDescriptor(ID_BOTTOM, validator.getPropertyName());
        bottomProp.setValidator(validator);

        mDescriptors = new IPropertyDescriptor[]{leftProp, topProp, rightProp, bottomProp};
    }

    protected InstallOptionsWidget mWidget = null;
    protected Position mPosition = null;

    public PositionPropertySource(InstallOptionsWidget widget)
    {
        mWidget = widget;
        mPosition = mWidget.getPosition().getCopy();
        createDescriptors();
    }

    public Object getEditableValue()
    {
        return mPosition.getCopy();
    }

    public Object getPropertyValue(Object propName)
    {
        return getPropertyValue((String)propName);
    }

    public Object getPropertyValue(String propName)
    {
        if (ID_TOP.equals(propName)) {
            return new Integer(mPosition.top).toString();
        }
        if (ID_LEFT.equals(propName)) {
            return new Integer(mPosition.left).toString();
        }
        if (ID_RIGHT.equals(propName)) {
            return new Integer(mPosition.right).toString();
        }
        if (ID_BOTTOM.equals(propName)) {
            return new Integer(mPosition.bottom).toString();
        }
        return null;
    }

    public void setPropertyValue(Object propName, Object value)
    {
        setPropertyValue((String)propName, value);
    }

    public void setPropertyValue(String propName, Object value)
    {
        if (ID_TOP.equals(propName)) {
            mPosition.top = Integer.parseInt((String)value);
        }
        if (ID_LEFT.equals(propName)) {
            mPosition.left = Integer.parseInt((String)value);
        }
        if (ID_RIGHT.equals(propName)) {
            mPosition.right = Integer.parseInt((String)value);
        }
        if (ID_BOTTOM.equals(propName)) {
            mPosition.bottom = Integer.parseInt((String)value);
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
        return isPropertySet((String)propName);
    }

    public boolean isPropertySet(String propName)
    {
        return (ID_TOP.equals(propName) || ID_LEFT.equals(propName) ||
            ID_RIGHT.equals(propName) || ID_BOTTOM.equals(propName));
    }

    @Override
    public String toString()
    {
        return new StringBuffer("(").append(mPosition.left).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
            mPosition.top).append(",").append(mPosition.right).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
            mPosition.bottom).append(")").toString(); //$NON-NLS-1$
    }

    private static class IntegerCellEditorValidator implements ICellEditorValidator
    {
        private String mPropertyName;

        public IntegerCellEditorValidator(String propertyName)
        {
            super();
            mPropertyName = propertyName;
        }

        public String getPropertyName()
        {
            return mPropertyName;
        }

        public String isValid(Object value)
        {
            try {
                Integer.parseInt((String)value);
                return null;
            }
            catch (NumberFormatException nfe){
                return InstallOptionsPlugin.getFormattedString("number.error.message",new String[] {mPropertyName}); //$NON-NLS-1$
            }
        }
    }

    private class CustomTextPropertyDescriptor extends TextPropertyDescriptor
    {
        public CustomTextPropertyDescriptor(Object id, String displayName)
        {
            super(id, displayName);
        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            if(mWidget.isLocked()) {
                return null;
            }
            else {
                return super.createPropertyEditor(parent);
            }
        }

    }
}