/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.validators;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NumberCellEditorValidator implements ICellEditorValidator
{
    private String mPropertyName;
    private int mMinValue;
    private int mMaxValue;
    private boolean mBlankAllowed;

    public NumberCellEditorValidator(String propertyName)
    {
        this(propertyName,0,Integer.MAX_VALUE,false);
    }

    public NumberCellEditorValidator(String propertyName, int minValue, int maxValue, boolean blankAllowed)
    {
        mPropertyName = propertyName;
        mMinValue = minValue;
        mMaxValue = maxValue;
        mBlankAllowed = blankAllowed;
    }

    public String isValid(Object value)
    {
        try {
            if(!isBlankAllowed() || !Common.isEmpty((String)value)) {
                int val = Integer.parseInt((String)value);
                int minValue = getMinValue();
                if(val < minValue) {
                    return InstallOptionsPlugin.getFormattedString("number.minvalue.error.message",new Object[]{mPropertyName,new Integer(minValue)}); //$NON-NLS-1$
                }
                int maxValue = getMaxValue();
                if(val > maxValue) {
                    return InstallOptionsPlugin.getFormattedString("number.maxvalue.error.message",new Object[]{mPropertyName,new Integer(maxValue)}); //$NON-NLS-1$
                }
            }
            return null;
        }
        catch (NumberFormatException exc) {
            return InstallOptionsPlugin.getFormattedString("number.error.message",new String[] {mPropertyName}); //$NON-NLS-1$
        }
    }

    public boolean isBlankAllowed()
    {
        return mBlankAllowed;
    }

    public int getMinValue()
    {
        return mMinValue;
    }

    public int getMaxValue()
    {
        return mMaxValue;
    }
}