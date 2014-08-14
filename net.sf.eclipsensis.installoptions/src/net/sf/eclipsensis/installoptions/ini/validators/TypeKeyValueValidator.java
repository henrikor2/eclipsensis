/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini.validators;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;

public class TypeKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        if(value.length() > 0 && value.indexOf(' ') < 0 && value.indexOf('\t') < 0) {
            InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(value);
            if(typeDef != null) {
                return true;
            }
        }
        if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
            keyValue.setValue(InstallOptionsModel.TYPE_UNKNOWN);
        }
        else {
            INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING,
                                            InstallOptionsPlugin.getFormattedString("type.value.warning", //$NON-NLS-1$
                                                    new Object[]{InstallOptionsModel.PROPERTY_TYPE}));
            problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.set.valid.type.value")) { //$NON-NLS-1$
                @Override
                protected INIProblemFix[] createFixes()
                {
                    return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(InstallOptionsModel.TYPE_UNKNOWN)+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                }
            });
            keyValue.addProblem(problem);
        }
        return false;
    }
}
