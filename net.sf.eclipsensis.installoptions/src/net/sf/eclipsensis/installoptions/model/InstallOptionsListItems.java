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
import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.dialogs.ListItemsDialog;
import net.sf.eclipsensis.installoptions.properties.labelproviders.ListLabelProvider;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public abstract class InstallOptionsListItems extends InstallOptionsEditableElement
{
    /**
     *
     */
    private static final long serialVersionUID = -5321738343148820961L;
    protected static LabelProvider cListItemsLabelProvider = new ListLabelProvider();
    private List<String> mListItems;

    protected InstallOptionsListItems(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("multiSelect"); //$NON-NLS-1$
        skippedProperties.add("text"); //$NON-NLS-1$
        skippedProperties.add("maxLen"); //$NON-NLS-1$
    }

    @Override
    protected void init()
    {
        super.init();
        mListItems = new ArrayList<String>();
    }

    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,99,99);
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            list.add(InstallOptionsModel.PROPERTY_LISTITEMS);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    @Override
    protected TypeConverter<?> loadTypeConverter(String property, Object value)
    {
        if(InstallOptionsModel.PROPERTY_LISTITEMS.equals(property)) {
            return TypeConverter.STRING_LIST_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            return new ListItemsPropertyDescriptor();
        }
        return super.createPropertyDescriptor(name);
    }

    @Override
    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_LISTITEMS.equals(propName)) {
            return getListItems();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
            setListItems((List<String>)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public List<String> getListItems()
    {
        return (mListItems == null?Collections.<String>emptyList():mListItems);
    }

    public void setListItems(List<String> listItems)
    {
        if(!mListItems.equals(listItems)) {
            List<String> oldListItems = mListItems;
            mListItems = new ArrayList<String>(listItems);
            firePropertyChange(InstallOptionsModel.PROPERTY_LISTITEMS, oldListItems, mListItems);
            setDirty(true);
        }
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new ListItemsPropertySectionCreator(this);
    }

    @Override
    public Object clone()
    {
        InstallOptionsListItems clone = (InstallOptionsListItems)super.clone();
        clone.setListItems(new ArrayList<String>(mListItems));
        return clone;
    }

    public boolean isMultiSelect()
    {
        return false;
    }

    protected class ListItemsPropertyDescriptor extends PropertyDescriptor
    {
        public ListItemsPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_LISTITEMS, InstallOptionsPlugin.getResourceString("listitems.property.name")); //$NON-NLS-1$
            setLabelProvider(cListItemsLabelProvider);
            setValidator(new NSISStringLengthValidator(getDisplayName()));
        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            final ListItemsCellEditor cellEditor = new ListItemsCellEditor(parent);
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                cellEditor.setValidator(validator);
            }
            return cellEditor;
        }
    }

    protected class ListItemsCellEditor extends DialogCellEditor implements PropertyChangeListener
    {
        protected ListItemsCellEditor(Composite parent)
        {
            super(parent);
            InstallOptionsListItems.this.addPropertyChangeListener(this);
        }

        @Override
        public void dispose()
        {
            InstallOptionsListItems.this.removePropertyChangeListener(this);
            super.dispose();
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
                setValue(evt.getNewValue());
            }
        }

        @Override
        protected void updateContents(Object value)
        {
            Label label = getDefaultLabel();
            if (label != null) {
                label.setText(cListItemsLabelProvider.getText(value));
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object openDialogBox(Control cellEditorWindow)
        {
            Object oldValue = getValue();
            ListItemsDialog dialog = new ListItemsDialog(cellEditorWindow.getShell(), (List<String>)oldValue, getType());
            dialog.setValidator(getValidator());
            int result = dialog.open();
            return (result == Window.OK?dialog.getValues():oldValue);
        }
    }
}

