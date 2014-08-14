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
import net.sf.eclipsensis.util.IOUtility;

public class PathStateKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        boolean fixErrors = (fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0;
        if (keyValue.getValue() != null && keyValue.getValue().length() > 0) {
            if (!IOUtility.isValidPathName(keyValue.getValue()) && !IOUtility.isValidUNCName(keyValue.getValue())) {
                INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getResourceString("invalid.path.error")); //$NON-NLS-1$
                problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.clear.invalid.path")) { //$NON-NLS-1$
                            @Override
                            protected INIProblemFix[] createFixes()
                            {
                                return new INIProblemFix[]{new INIProblemFix(keyValue, keyValue.buildText("") //$NON-NLS-1$
                                        + (keyValue.getDelimiter() == null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                            }
                        });
                keyValue.addProblem(problem);
                if (fixErrors) {
                    keyValue.setValue(""); //$NON-NLS-1$
                }
                return false;
            }
        }
        return true;
    }
}
