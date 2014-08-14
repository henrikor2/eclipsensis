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

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;

public class CustomComboBoxPropertyDescriptor extends ComboBoxPropertyDescriptor
{
    private Object[] mData;
    private String[] mDisplay;
    private int mDefault;

    public CustomComboBoxPropertyDescriptor(String id, String displayName, Object[] data, int default1)
    {
        this(id, displayName, data, data, default1);
    }

    public CustomComboBoxPropertyDescriptor(String id, String displayName, Object[] data, Object[] display, int default1)
    {
        super(id, displayName, new String[0]);
        mData = data;
        if(display.getClass().getComponentType().equals(String.class)) {
            mDisplay = (String[])display;
        }
        else {
            mDisplay = new String[display.length];
            for (int i = 0; i < display.length; i++) {
                mDisplay[i] = String.valueOf(display[i]);
            }
        }
        mDefault = default1;

        setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element)
            {
                if(element != null) {
                    for(int i=0; i<mData.length; i++) {
                        if(Common.objectsAreEqual(mData[i],element)) {
                            return mDisplay[i];
                        }
                    }
                    return mDisplay[mDefault];
                }
                return super.getText(element);
            }
        });
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent)
    {
        return new ComboBoxCellEditor(parent,mDisplay,SWT.READ_ONLY) {
            @Override
            protected Object doGetValue()
            {
                Integer i = (Integer)super.doGetValue();
                return mData[i.intValue()];
            }

            @Override
            protected void doSetValue(Object value)
            {
                int val = mDefault;
                for(int i=0; i<mData.length; i++) {
                    if(Common.objectsAreEqual(mData[i],value)) {
                        val = i;
                        break;
                    }
                }
                super.doSetValue(new Integer(val));
            }
        };
    }
}