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

import net.sf.eclipsensis.INSISVersions;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.descriptors.*;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.*;

public class InstallOptionsLink extends InstallOptionsLabel
{
    private static final long serialVersionUID = -6806110942403416762L;
    public static final RGB DEFAULT_TXTCOLOR = new RGB(0,0,255);
    private static ILabelProvider cLabelProvider = new LabelProvider(){
        @Override
        public String getText(Object element)
        {
            RGB rgb = null;
            if(element instanceof RGB) {
                rgb = (RGB)element;
            }
            else if(element == null) {
                rgb = DEFAULT_TXTCOLOR;
            }
            else {
                return super.getText(element);
            }
            String s = TypeConverter.RGB_CONVERTER.asString(rgb);
            if(rgb.equals(DEFAULT_TXTCOLOR)) {
                s = InstallOptionsPlugin.getFormattedString("link.default.value.format", new String[] {s}); //$NON-NLS-1$
            }
            return s;
        }
    };

    private String mState;
    private RGB mTxtColor;
    private boolean mMultiLine;

    protected InstallOptionsLink(INISection section)
    {
        super(section);
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_LINK;
    }

    @Override
    protected void init()
    {
        super.init();
        mState = ""; //$NON-NLS-1$
        mMultiLine = checkMultiLine();
    }

    /**
     * @return
     */
    @Override
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("link.text.default"); //$NON-NLS-1$
    }

    /**
     * @return
     */
    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,15,9);
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

    public RGB getTxtColor()
    {
        return mTxtColor;
    }

    public void setTxtColor(RGB txtColor)
    {
        if(!Common.objectsAreEqual(mTxtColor,txtColor)) {
            RGB oldTxtColor = mTxtColor;
            mTxtColor = (DEFAULT_TXTCOLOR.equals(txtColor)?null:txtColor);
            firePropertyChange(InstallOptionsModel.PROPERTY_TXTCOLOR, oldTxtColor, txtColor);
            setDirty(true);
        }
    }

    @Override
    protected TypeConverter<?> loadTypeConverter(String property, Object value)
    {
        if(property.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            return TypeConverter.RGB_CONVERTER;
        }
        else {
            return super.loadTypeConverter(property, value);
        }
    }

    @Override
    protected void addPropertyName(List<String> list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            list.add(InstallOptionsModel.PROPERTY_STATE);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            list.add(InstallOptionsModel.PROPERTY_TXTCOLOR);
        }
        else {
            super.addPropertyName(list, setting);
        }
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            String propertyName = InstallOptionsPlugin.getResourceString("state.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, propertyName);
            descriptor.setValidator(new NSISStringLengthValidator(propertyName));
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            CustomColorPropertyDescriptor descriptor = new CustomColorPropertyDescriptor(this, InstallOptionsModel.PROPERTY_TXTCOLOR, InstallOptionsPlugin.getResourceString("txtcolor.property.name")); //$NON-NLS-1$;
            descriptor.setDefaultColor(DEFAULT_TXTCOLOR);
            descriptor.setLabelProvider(cLabelProvider);
            return descriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_TEXT)) {
            MultiLineTextPropertyDescriptor descriptor = (MultiLineTextPropertyDescriptor)super.createPropertyDescriptor(name);
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        else {
            return super.createPropertyDescriptor(name);
        }
    }

    @Override
    public Object clone()
    {
        InstallOptionsLink clone = (InstallOptionsLink)super.clone();
        clone.setState(getState());
        if(mTxtColor != null) {
            clone.setTxtColor(new RGB(mTxtColor.red,mTxtColor.green,mTxtColor.blue));
        }
        return clone;
    }

    @Override
    public Object getPropertyValue(Object propName)
    {
        if (InstallOptionsModel.PROPERTY_STATE.equals(propName)) {
            return getState();
        }
        if (InstallOptionsModel.PROPERTY_TXTCOLOR.equals(propName)) {
            return getTxtColor();
        }
        return super.getPropertyValue(propName);
    }

    @Override
    public void setPropertyValue(Object id, Object value)
    {
        if(id.equals(InstallOptionsModel.PROPERTY_STATE)) {
            setState((String)value);
        }
        else if(id.equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            setTxtColor((RGB)value);
        }
        else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new LinkPropertySectionCreator(this);
    }

    @Override
    public boolean isMultiLine()
    {
        return mMultiLine;
    }

    private void setMultiLine(boolean multiLine)
    {
        if(mMultiLine != multiLine) {
            mMultiLine = multiLine;
            firePropertyChange(InstallOptionsModel.PROPERTY_MULTILINE,
                    multiLine?Boolean.FALSE:Boolean.TRUE,
                    multiLine?Boolean.TRUE:Boolean.FALSE);
        }
    }

    @Override
    public void modelChanged()
    {
        super.modelChanged();
        setMultiLine(checkMultiLine());
    }

    /**
     * @return
     */
    private boolean checkMultiLine()
    {
        Version version = NSISPreferences.getInstance().getNSISVersion();
        return (version != null && version.compareTo(INSISVersions.VERSION_2_26) >= 0 );
    }
}
