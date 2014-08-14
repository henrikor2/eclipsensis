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

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.Common;

public class TextStateKeyValueValidator extends MultiLineKeyValueValidator
{
    @Override
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        boolean hasProblems = false;
        if(!Common.isEmpty(value)) {
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_FLAGS);
            if(!Common.isEmptyArray(keyValues)) {
                String[] flags = Common.tokenize(keyValues[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR,false);
                if(!Common.isEmptyArray(flags)) {
                    for (int i = 0; i < flags.length; i++) {
                        if(flags[i].equalsIgnoreCase(InstallOptionsModel.FLAGS_MULTILINE)) {
                            if(!super.validate(keyValue, fixFlag)) {
                                hasProblems = true;
                            }
                        }
                        else if(flags[i].equalsIgnoreCase(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                            try {
                                Integer.parseInt(value);
                            }
                            catch(Exception ex) {
                                final StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                                char[] chars = value.toCharArray();
                                for (int j = 0; j < chars.length; j++) {
                                    if(Character.isDigit(chars[j])) {
                                        buf.append(chars[j]);
                                    }
                                }
                                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                                    keyValue.setValue(buf.toString());
                                }
                                else {
                                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,
                                                                                InstallOptionsPlugin.getFormattedString("text.state.only.numbers.error", //$NON-NLS-1$
                                                                                        new String[]{InstallOptionsModel.PROPERTY_STATE,
                                                                                        InstallOptionsModel.FLAGS_ONLY_NUMBERS}));
                                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.non.numeric.chars")) { //$NON-NLS-1$
                                        @Override
                                        protected INIProblemFix[] createFixes()
                                        {
                                            return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(buf.toString())+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                                        }
                                    });
                                    keyValue.addProblem(problem);
                                    hasProblems = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return !hasProblems;
    }
}
