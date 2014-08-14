/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModelTypeDef;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

public class LabelSectionDisplayTextProvider extends DefaultSectionDisplayTextProvider
{
    @Override
    protected String getDisplayText(InstallOptionsModelTypeDef typeDef, INISection section)
    {
        String text = super.getDisplayText(typeDef, section);
        return (shouldUnescape(section)?TypeConverter.ESCAPED_STRING_CONVERTER.asString(text):text);
    }

    protected boolean shouldUnescape(INISection section)
    {
        return true;
    }
}
