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
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;


public class MaxLenKeyValueValidator extends PositiveNumberKeyValueValidator
{
    @Override
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        boolean b = super.validate(keyValue, fixFlag);
        if(b) {
            int maxValue = InstallOptionsModel.INSTANCE.getMaxLength();
            final String value = keyValue.getValue();
            final int radix = getRadix(value);
            final String prefix = getPrefix(value,radix);
            if(!Common.isEmpty(value)) {
                maxValue = parseInt(value, radix);
            }

            final int minValue;
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_MINLEN);
            if(!Common.isEmptyArray(keyValues)) {
                String value2 = keyValues[0].getValue();
                int val = 0;
                if(!Common.isEmpty(value2)) {
                    try {
                        int radix2 = getRadix(value2);
                        val = parseInt(value2, radix2);
                    }
                    catch(Exception e) {
                        val = 0;
                    }
                }
                minValue = val;
            }
            else {
                minValue = 0;
            }
            if(minValue > maxValue) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    keyValue.setValue(formatInt(minValue, radix, prefix));
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("maxlen.value.error",new Object[]{ //$NON-NLS-1$
                                                keyValue.getKey(),new Integer(minValue)}));
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.maxlen.value")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(formatInt(minValue, radix,prefix))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                        }
                    });
                    keyValue.addProblem(problem);
                    b = false;
                }
            }
        }
        return b;
    }
}
