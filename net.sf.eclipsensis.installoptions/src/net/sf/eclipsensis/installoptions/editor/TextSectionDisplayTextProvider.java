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

import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class TextSectionDisplayTextProvider extends LabelSectionDisplayTextProvider
{
    @Override
    protected boolean shouldUnescape(INISection section)
    {
        INIKeyValue[] values = section.findKeyValues(InstallOptionsModel.PROPERTY_FLAGS);
        if(!Common.isEmptyArray(values)) {
            List<String> flags = Common.tokenizeToList(values[0].getValue(), IInstallOptionsConstants.LIST_SEPARATOR,false);
            return flags.contains(InstallOptionsModel.FLAGS_MULTILINE);
        }
        return false;
    }
}
