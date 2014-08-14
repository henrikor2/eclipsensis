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
import net.sf.eclipsensis.util.Common;

public class ToggleKeyValueValidator extends PositiveNumberKeyValueValidator
{
    @Override
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        boolean b = super.validate(keyValue, fixFlag);
        if(b) {
            final String value = keyValue.getValue();
            if(!Common.isEmpty(value)) {
                final int radix = getRadix(value);
                final String prefix = getPrefix(value,radix);
                int val = parseInt(value, radix);

                if(val == 0 || val == 1) {
                    return true;
                }
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    keyValue.setValue(formatInt(0,radix,prefix));
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,
                                                        InstallOptionsPlugin.getFormattedString("toggle.value.error", //$NON-NLS-1$
                                                                new String[]{keyValue.getKey()}));
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.set.valid.value")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(formatInt(0,radix,prefix))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                        }
                    });
                    keyValue.addProblem(problem);
                    return false;
                }
            }
        }
        return true;
    }

}
