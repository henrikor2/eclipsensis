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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;

public class DefaultSectionDisplayTextProvider implements IINISectionDisplayTextProvider
{
    protected static final String MISSING_DISPLAY_NAME = InstallOptionsPlugin.getResourceString("missing.outline.display.name"); //$NON-NLS-1$

    public String formatDisplayText(String type, INISection section)
    {
        String type2 = type;
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(type2);
        if(typeDef != null) {
            String displayText = getDisplayText(typeDef, section);
            if(!typeDef.getName().equals(InstallOptionsModel.TYPE_UNKNOWN)) {
                type2 = typeDef.getName();
            }
            return InstallOptionsPlugin.getFormattedString("source.outline.display.name.format",  //$NON-NLS-1$
                    new String[]{section.getName(), type2, (Common.isEmpty(displayText)?MISSING_DISPLAY_NAME:displayText)});
        }
        return null;
    }

    /**
     * @param typeDef
     * @param section
     * @return
     */
    protected String getDisplayText(InstallOptionsModelTypeDef typeDef, INISection section)
    {
        String displayName = ""; //$NON-NLS-1$
        INIKeyValue[] values = section.findKeyValues(typeDef.getDisplayProperty());
        if(!Common.isEmptyArray(values)) {
            displayName = values[0].getValue();
        }
        return displayName;
    }
}
