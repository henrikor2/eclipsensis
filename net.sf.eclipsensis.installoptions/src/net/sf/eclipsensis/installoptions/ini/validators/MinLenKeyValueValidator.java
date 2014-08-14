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


public class MinLenKeyValueValidator extends PositiveNumberKeyValueValidator
{
    @Override
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        boolean b = super.validate(keyValue, fixFlag);
        if(b) {
            int minValue = 0;
            final String value = keyValue.getValue();
            final int radix = getRadix(value);
            final String prefix = getPrefix(value,radix);
            if(!Common.isEmpty(value)) {
                minValue = parseInt(value, radix);
            }

            final int maxValue;
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_MAXLEN);
            if(!Common.isEmptyArray(keyValues)) {
                String value2 = keyValues[0].getValue();
                int val = InstallOptionsModel.INSTANCE.getMaxLength();
                if(!Common.isEmpty(value2)) {
                    try {
                        int radix2 = getRadix(value2);
                        val = parseInt(value2, radix2);
                    }
                    catch(Exception e) {
                        val = InstallOptionsModel.INSTANCE.getMaxLength();
                    }
                }
                maxValue = val;
            }
            else {
                maxValue = InstallOptionsModel.INSTANCE.getMaxLength();
            }
            if(minValue > maxValue) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    keyValue.setValue(formatInt(maxValue, radix, prefix));
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("minlen.value.error",new Object[]{ //$NON-NLS-1$
                                                keyValue.getKey(),new Integer(maxValue)}));
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.minlen.value")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(formatInt(maxValue, radix, prefix))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
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
