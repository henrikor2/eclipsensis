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

import net.sf.eclipsensis.installoptions.util.TypeConverter;

public class NSISEscapedStringLengthValidator extends NSISStringLengthValidator
{
    public NSISEscapedStringLengthValidator(String propertyName)
    {
        super(propertyName);
    }

    @Override
    public String isValid(String value)
    {
        return super.isValid(TypeConverter.ESCAPED_STRING_CONVERTER.asString(value));
    }
}
