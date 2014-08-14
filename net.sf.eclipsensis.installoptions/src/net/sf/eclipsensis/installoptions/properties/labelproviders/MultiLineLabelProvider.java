/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.labelproviders;

import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.jface.viewers.LabelProvider;

public class MultiLineLabelProvider extends LabelProvider
{
    public static final MultiLineLabelProvider INSTANCE = new MultiLineLabelProvider();

    private MultiLineLabelProvider()
    {
        super();
    }

    @Override
    public String getText(Object element)
    {
        if(element instanceof String) {
            return TypeConverter.ESCAPED_STRING_CONVERTER.asString((String) element);
        }
        else {
            return super.getText(element);
        }
    }
}