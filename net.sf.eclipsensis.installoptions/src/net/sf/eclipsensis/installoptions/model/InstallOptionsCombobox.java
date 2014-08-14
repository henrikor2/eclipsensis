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

import java.beans.*;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.editors.CustomComboBoxCellEditor;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsCombobox extends InstallOptionsListItems
{
    private static final long serialVersionUID = 5374900660429140670L;

    protected InstallOptionsCombobox(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("stateReadOnly"); //$NON-NLS-1$
        skippedProperties.remove("maxLen"); //$NON-NLS-1$
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_COMBOBOX;
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            ComboStatePropertyDescriptor descriptor = new ComboStatePropertyDescriptor();
            if(isStateReadOnly()) {
                descriptor.setStyle(SWT.READ_ONLY);
            }
            else {
                addPropertyChangeListener(descriptor);
            }
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        if(isStateReadOnly()) {
            return super.createPropertySectionCreator();
        }
        else {
            return new ComboboxPropertySectionCreator(this);
        }
    }

    protected boolean isStateReadOnly()
    {
        return false;
    }

    protected class ComboStatePropertyDescriptor extends PropertyDescriptor implements PropertyChangeListener
    {
        private CustomComboBoxCellEditor mEditor;
        private DisposeListener mListener = new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mEditor = null;
            }
         };
        private int mStyle = SWT.NONE;

        public ComboStatePropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$
            setValidator(new NSISStringLengthValidator(getDisplayName()));
        }

        public int getStyle()
        {
            return mStyle;
        }

        public void setStyle(int style)
        {
            mStyle = style;
        }

        @SuppressWarnings("unchecked")
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
                setListItems((List<String>)evt.getNewValue());
            }

        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            if(mEditor == null) {
                mEditor = new CustomComboBoxCellEditor(parent, getListItems(), mStyle);
                mEditor.setCaseInsensitive(true);
                mEditor.getControl().addDisposeListener(mListener);
            }
            return mEditor;
        }

        public void setListItems(List<String> listItems)
        {
            if(mEditor != null) {
                mEditor.setItems(listItems);
            }
        }
    }
}

