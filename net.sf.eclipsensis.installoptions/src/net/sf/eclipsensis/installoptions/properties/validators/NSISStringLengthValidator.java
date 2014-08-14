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

import java.util.Collection;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class NSISStringLengthValidator implements ICellEditorValidator
{
    private String mPropertyName;

    /**
     *
     */
    public NSISStringLengthValidator(String propertyName)
    {
        super();
        mPropertyName = propertyName;
    }

    public String isValid(String value)
    {
        int maxLen = InstallOptionsModel.INSTANCE.getMaxLength();
        if(value.length() > maxLen)  {
            return InstallOptionsPlugin.getFormattedString("property.maxlength.error",new Object[]{mPropertyName,new Integer(maxLen)}); //$NON-NLS-1$
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
     */
    public String isValid(Object value)
    {
        if(value instanceof String) {
            return isValid((String)value);
        }
        else if(value instanceof Collection<?>) {
            return isValid(Common.flatten(((Collection<?>)value).toArray(),IInstallOptionsConstants.LIST_SEPARATOR));
        }
        return null;
    }
}
