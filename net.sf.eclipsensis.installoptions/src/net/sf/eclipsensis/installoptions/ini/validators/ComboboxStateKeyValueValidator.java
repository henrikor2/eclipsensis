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

public class ComboboxStateKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String error = InstallOptionsPlugin.getFormattedString("single.selection.error", //$NON-NLS-1$
                new String[]{InstallOptionsModel.PROPERTY_STATE});
        return validateSingleSelection(keyValue, error, fixFlag);
    }

    protected boolean validateSingleSelection(final INIKeyValue keyValue, String error, int fixFlag)
    {
        String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            final String[] array = Common.tokenize(value,IInstallOptionsConstants.LIST_SEPARATOR,false);
            if(!Common.isEmptyArray(array) && array.length > 1) {
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    keyValue.setValue(array[0]);
                }
                else {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, error);
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.selected.value")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(array[0])+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
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
