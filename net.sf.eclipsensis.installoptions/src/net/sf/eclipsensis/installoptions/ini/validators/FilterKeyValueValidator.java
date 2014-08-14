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
import net.sf.eclipsensis.util.Common;

public class FilterKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            final int n = Common.tokenize(value,IInstallOptionsConstants.LIST_SEPARATOR,false).length;
            if(n%2 != 0) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    if(n == 0) {
                        buf.append(InstallOptionsPlugin.getResourceString("filter.files.label")); //$NON-NLS-1$
                    }
                    else {
                        buf.append(value);
                    }
                    keyValue.setValue(buf.append(IInstallOptionsConstants.LIST_SEPARATOR).append(InstallOptionsPlugin.getResourceString("filter.files.wildcard")).toString()); //$NON-NLS-1$
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,
                                                            InstallOptionsPlugin.getFormattedString("filter.value.error", //$NON-NLS-1$
                                                                    new Object[]{keyValue.getKey()}));
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.filter.value")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            StringBuffer buf = new StringBuffer(keyValue.getText());
                            if(n == 0) {
                                buf.append(InstallOptionsPlugin.getResourceString("filter.files.label")); //$NON-NLS-1$
                            }
                            buf.append(IInstallOptionsConstants.LIST_SEPARATOR).append(InstallOptionsPlugin.getResourceString("filter.files.wildcard")); //$NON-NLS-1$
                            if(keyValue.getDelimiter()!=null) {
                                buf.append(keyValue.getDelimiter());
                            }
                            return new INIProblemFix[] {new INIProblemFix(keyValue,buf.toString())};
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
