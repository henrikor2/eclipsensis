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

import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.util.*;

public class DropListStateKeyValueValidator extends ComboboxStateKeyValueValidator
{
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.ini.validators.IINIKeyValueValidator#validate(net.sf.eclipsensis.installoptions.ini.INIKeyValue)
     */
    @Override
    public boolean validate(INIKeyValue keyValue, int fixFlag)
    {
        String value = keyValue.getValue();
        return super.validate(keyValue, fixFlag) &&
               validateSelection(keyValue, (value != null && value.length() > 0?new String[]{value}:Common.EMPTY_STRING_ARRAY),
                                 fixFlag);
    }

    protected String getType()
    {
        return InstallOptionsModel.TYPE_DROPLIST;
    }

    protected boolean validateSelection(final INIKeyValue keyValue, String[] values, int fixFlag)
    {
        if(!Common.isEmptyArray(values)) {
            INIKeyValue[] keyValues = ((INISection)keyValue.getParent()).findKeyValues(InstallOptionsModel.PROPERTY_LISTITEMS);
            final String validValues;
            if(!Common.isEmptyArray(keyValues)) {
                Collection<String> allValues = new CaseInsensitiveSet(Common.tokenizeToList(keyValues[0].getValue(),IInstallOptionsConstants.LIST_SEPARATOR,false));
                if(!Common.isEmptyCollection(allValues)) {
                    List<String> valuesList = Common.makeList(values);
                    valuesList.removeAll(allValues);
                    if(valuesList.size() == 0) {
                        return true;
                    }
                    valuesList = Common.makeList(values);
                    valuesList.retainAll(allValues);
                    validValues = Common.flatten(valuesList, IInstallOptionsConstants.LIST_SEPARATOR);
                }
                else {
                    validValues = ""; //$NON-NLS-1$
                }
            }
            else {
                validValues = ""; //$NON-NLS-1$
            }
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                keyValue.setValue(validValues);
            }
            else {
                INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR, InstallOptionsPlugin.getFormattedString("valid.selection.error", //$NON-NLS-1$
                                        new String[]{InstallOptionsModel.PROPERTY_STATE,InstallOptionsModel.PROPERTY_LISTITEMS}));
                problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.invalid.selected.values")) { //$NON-NLS-1$
                    @Override
                    protected INIProblemFix[] createFixes()
                    {
                        return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(validValues)+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                    }
                });
                keyValue.addProblem(problem);
                return false;
            }
        }
        return true;
    }
}
