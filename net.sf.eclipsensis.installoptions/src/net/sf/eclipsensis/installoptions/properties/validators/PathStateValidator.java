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
import net.sf.eclipsensis.util.IOUtility;

public class PathStateValidator extends NSISStringLengthValidator
{
    public PathStateValidator(String propertyName)
    {
        super(propertyName);
    }

    @Override
    public String isValid(Object value)
    {
        String error = super.isValid(value);
        if(error==null) {
            String val = (String)value;
            if (val != null && val.length() > 0) {
                if (!IOUtility.isValidPathName(val) && !IOUtility.isValidUNCName(val)) {
                    error = InstallOptionsPlugin.getResourceString("invalid.path.error"); //$NON-NLS-1$
                }
            }
        }
        return error;
    }
}
