/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.descriptors;

import java.beans.*;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.editors.CustomColorCellEditor;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CustomColorPropertyDescriptor extends PropertyDescriptor implements PropertyChangeListener
{
    private InstallOptionsElement mSource;
    private RGB mColor = null;
    private RGB mDefaultColor = null;
    private PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * @param id
     * @param displayName
     */
    public CustomColorPropertyDescriptor(InstallOptionsElement source, Object id, String displayName)
    {
        super(id, displayName);
        mSource = source;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
            RGB color = (RGB)evt.getNewValue();
            setColor(color);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void setColor(RGB color)
    {
        if (!Common.objectsAreEqual(mColor,color)) {
            RGB oldColor = mColor;
            mColor = color;
            mPropertyChangeSupport.firePropertyChange(InstallOptionsModel.PROPERTY_TXTCOLOR, oldColor, color);
        }
    }

    public RGB getColor()
    {
        return mColor;
    }

    public RGB getDefaultColor()
    {
        return mDefaultColor;
    }

    public void setDefaultColor(RGB defaultColor)
    {
        mDefaultColor = defaultColor;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent)
    {
        final CustomColorCellEditor editor = new CustomColorCellEditor(parent);
        final PropertyChangeListener listener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
                    editor.setValue(evt.getNewValue());
                }
            }
        };
        addPropertyChangeListener(listener);
        mSource.addPropertyChangeListener(listener);
        if(mDefaultColor != null) {
            editor.setDefaultColor(mDefaultColor);
        }
        if(getLabelProvider() != null) {
            editor.setLabelProvider(getLabelProvider());
        }
        parent.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                removePropertyChangeListener(listener);
                mSource.removePropertyChangeListener(listener);
            }
        });
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
