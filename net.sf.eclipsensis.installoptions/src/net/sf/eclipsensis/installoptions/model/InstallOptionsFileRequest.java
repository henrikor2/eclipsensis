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

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.editors.FileFilterCellEditor;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsFileRequest extends InstallOptionsPathRequest
{
    private static final long serialVersionUID = -1414856427625675866L;

    public  static final char FILTER_SEPARATOR = ';';

    public static final TypeConverter<List<FileFilter>> FILEFILTER_LIST_CONVERTER = new TypeConverter<List<FileFilter>>(){
        @Override
        public String asString(List<FileFilter> o)
        {
            return Common.flatten(o.toArray(new FileFilter[o.size()]),IInstallOptionsConstants.LIST_SEPARATOR);
        }

        @Override
        public List<FileFilter> asType(String s)
        {
            List<FileFilter> list = new ArrayList<FileFilter>();
            String[] tokens = Common.tokenize(s,IInstallOptionsConstants.LIST_SEPARATOR,false);
            for (int i = 0; i < (tokens.length-1); i+= 2) {
                String description = tokens[i];
                String[] temp = Common.tokenize(tokens[i+1],FILTER_SEPARATOR,false);
                FilePattern[] patterns = new FilePattern[temp.length];
                for (int j = 0; j < patterns.length; j++) {
                    patterns[j] = new FilePattern(temp[j]);
                }
                list.add(new FileFilter(description, patterns));
            }
            return list;
        }

        @Override
        public List<FileFilter> makeCopy(List<FileFilter> o)
        {
            List<FileFilter> list = new ArrayList<FileFilter>();
            for(Iterator<FileFilter> iter=o.iterator(); iter.hasNext(); ) {
                list.add((FileFilter) iter.next().clone());
            }
            return list;
        }
    };

    public static final LabelProvider FILTER_LABEL_PROVIDER = new LabelProvider(){
        @Override
        @SuppressWarnings("unchecked")
        public String getText(Object element)
        {
            if(element instanceof List) {
                return FILEFILTER_LIST_CONVERTER.asString((List<FileFilter>) element);
            }
            else {
                return super.getText(element);
            }
        }
    };

    private List<FileFilter> mFilter;

    protected InstallOptionsFileRequest(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("text"); //$NON-NLS-1$
    }

    @Override
    protected void init()
    {
        super.init();
        mFilter = new ArrayList<FileFilter>();
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_FILEREQUEST;
    }

    @Override
    public Object clone()
    {
        InstallOptionsFileRequest clone = (InstallOptionsFileRequest)super.clone();
        clone.mFilter = new ArrayList<FileFilter>();
        clone.setFilter(mFilter);
        return clone;
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if (setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_FILTER)) {
            list.add(InstallOptionsModel.PROPERTY_FILTER);
        }
        else if(!setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            super.addPropertyName(list, setting);
        }
    }

    @Override
    protected TypeConverter<?> loadTypeConverter(String property, Object value)
    {
        if(property.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            return FILEFILTER_LIST_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new PathRequestPropertySectionCreator(this);
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            String propertyName = InstallOptionsPlugin.getResourceString("filter.property.name"); //$NON-NLS-1$
            PropertyDescriptor descriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_FILTER, propertyName){
                @Override
                public CellEditor createPropertyEditor(Composite parent)
                {
                    final FileFilterCellEditor editor = new FileFilterCellEditor(InstallOptionsFileRequest.this, parent);
                    editor.setValidator(getValidator());
                    final PropertyChangeListener listener = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt)
                        {
                            if(evt.getPropertyName().equals(getId())) {
                                editor.setValue(evt.getNewValue());
                            }
                        }
                    };
                    InstallOptionsFileRequest.this.addPropertyChangeListener(listener);
                    editor.getControl().addDisposeListener(new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e)
                        {
                            InstallOptionsFileRequest.this.removePropertyChangeListener(listener);
                        }
                    });
                    return editor;
                }
            };
            descriptor.setLabelProvider(FILTER_LABEL_PROVIDER);
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
        if (InstallOptionsModel.PROPERTY_FILTER.equals(propName)) {
            return getFilter();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_FILTER)) {
            setFilter((List<FileFilter>)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    public List<FileFilter> getFilter()
    {
        return mFilter;
    }

    public void setFilter(List<FileFilter> filter)
    {
        if(!mFilter.equals(filter)) {
            List<FileFilter> oldFilter = mFilter;
            mFilter = FILEFILTER_LIST_CONVERTER.makeCopy(filter);
            firePropertyChange(InstallOptionsModel.PROPERTY_FILTER, oldFilter, mFilter);
            setDirty(true);
        }
    }
}
