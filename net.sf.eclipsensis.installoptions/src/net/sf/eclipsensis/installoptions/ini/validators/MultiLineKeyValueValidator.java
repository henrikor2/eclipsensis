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
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.swt.SWT;

public class MultiLineKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        String value = TypeConverter.ESCAPED_STRING_CONVERTER.asString(keyValue.getValue());
        boolean hasErrors = false;
        boolean hasWarnings = false;
        boolean fixWarnings = (fixFlag & INILine.VALIDATE_FIX_WARNINGS) > 0;
        boolean fixErrors = (fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0;

        char[] chars = value.toCharArray();
        final StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        for (int i = 0; i < chars.length; i++) {
            buf.append(chars[i]);
            switch(chars[i]) {
                case SWT.CR:
                    if(i < chars.length-1) {
                        if(chars[i+1] == SWT.LF) {
                            buf.append(SWT.LF);
                            i++;
                            continue;
                        }
                    }
                    buf.append(SWT.LF);
                    if(!fixWarnings) {
                        hasWarnings = true;
                    }
                    break;
                case SWT.LF:
                    if(i > 0) {
                        if(chars[i-1] == SWT.CR) {
                            continue;
                        }
                    }
                    buf.insert(buf.length()-1, SWT.CR);
                    if(!fixErrors) {
                        hasErrors = true;
                    }
                    break;
                default:
                    break;
            }
        }
        if(hasErrors) {
            INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,
                                InstallOptionsPlugin.getFormattedString("missing.cr.error", //$NON-NLS-1$
                                        new Object[]{keyValue.getKey()}));
            problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.correct.line.delims")) { //$NON-NLS-1$
                @Override
                protected INIProblemFix[] createFixes()
                {
                    return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(TypeConverter.ESCAPED_STRING_CONVERTER.asType(buf.toString()))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                }
            });
            keyValue.addProblem(problem);
        }
        else if(hasWarnings) {
            INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING,
                    InstallOptionsPlugin.getFormattedString("missing.lf.warning", //$NON-NLS-1$
                            new Object[]{keyValue.getKey()}));
            problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.insert.missing.lf.chars")) { //$NON-NLS-1$
                @Override
                protected INIProblemFix[] createFixes()
                {
                    return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(TypeConverter.ESCAPED_STRING_CONVERTER.asType(buf.toString()))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                }
            });
            keyValue.addProblem(problem);
        }
        if(fixWarnings || fixErrors) {
            keyValue.setValue(TypeConverter.ESCAPED_STRING_CONVERTER.asType(buf.toString()));
        }
        return !hasErrors && !hasWarnings;
    }
}
