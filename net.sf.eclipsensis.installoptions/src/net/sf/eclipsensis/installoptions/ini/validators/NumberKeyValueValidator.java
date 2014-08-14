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

public class NumberKeyValueValidator implements IINIKeyValueValidator
{
    public boolean validate(final INIKeyValue keyValue, int fixFlag)
    {
        final String value = keyValue.getValue();
        if(!Common.isEmpty(value)) {
            final int radix = getRadix(value);
            final String prefix = getPrefix(value,radix);
            try {
                final int i = parseInt(value, radix);
                if(i < 0 && !isNegativeAllowed()) {
                    keyValue.setValue(formatInt(-i,radix,prefix));
                    boolean b = validate(keyValue, fixFlag);
                    if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                        return b;
                    }
                    else {
                        keyValue.setValue(value);
                        if(b) {
                            INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("positive.numeric.value.error",new String[]{keyValue.getKey()})); //$NON-NLS-1$
                            problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.make.numeric.value.positive")) { //$NON-NLS-1$
                                @Override
                                protected INIProblemFix[] createFixes()
                                {
                                    return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(formatInt(-i,radix,prefix))+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                                }
                            });
                            keyValue.addProblem(problem);
                        }
                        return false;
                    }
                }
            }
            catch(Exception e) {
                final StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                char[] chars = value.toCharArray();
                for (int i = (radix == 16?2:0); i < chars.length; i++) {
                    if(buf.length() == 0 && isNegativeAllowed() && chars[i] == '-') {
                        buf.append('-');
                        continue;
                    }
                    if(Character.isDigit(chars[i]) || (radix == 16 && isHexChar(chars[i]))) {
                        buf.append(chars[i]);
                    }
                }
                if(buf.length() > 0 && radix == 16) {
                    buf.insert(0,prefix);
                }

                keyValue.setValue(buf.toString());
                boolean b = validate(keyValue, fixFlag);
                if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                    return b;
                }
                else {
                    keyValue.setValue(value);
                    if(b) {
                        INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("numeric.value.error",new String[]{keyValue.getKey()})); //$NON-NLS-1$
                        problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.replace.valid.numeric.value")) { //$NON-NLS-1$
                            @Override
                            protected INIProblemFix[] createFixes()
                            {
                                return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText(buf.toString())+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$
                            }
                        });
                        keyValue.addProblem(problem);
                    }
                    return false;
                }
            }
        }
        else if(!isEmptyAllowed()){
            keyValue.setValue("0"); //$NON-NLS-1$
            boolean b = validate(keyValue, fixFlag);
            if((fixFlag & INILine.VALIDATE_FIX_ERRORS) > 0) {
                return b;
            }
            else {
                keyValue.setValue(value);
                if(b) {
                    INIProblem problem = new INIProblem(INIProblem.TYPE_ERROR,InstallOptionsPlugin.getFormattedString("numeric.value.error",new String[]{keyValue.getKey()})); //$NON-NLS-1$
                    problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.set.valid.numeric.value")) { //$NON-NLS-1$
                        @Override
                        protected INIProblemFix[] createFixes()
                        {
                            return new INIProblemFix[] {new INIProblemFix(keyValue,keyValue.buildText("0")+(keyValue.getDelimiter()==null?"":keyValue.getDelimiter()))}; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    });
                    keyValue.addProblem(problem);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * @param value
     * @return
     */
    protected int getRadix(final String value)
    {
        return value.regionMatches(true,0,"0x",0,2)?16:10; //$NON-NLS-1$
    }

    protected boolean isHexChar(char c)
    {
        char c2 = Character.toLowerCase(c);
        return (c2 >= 'a' && c2 <= 'f');
    }

    protected boolean isEmptyAllowed()
    {
        return true;
    }

    protected boolean isNegativeAllowed()
    {
        return true;
    }

    protected String getPrefix(String value, int radix)
    {
        switch(radix) {
            case 16:
                return value.substring(0,2);
        }
        return ""; //$NON-NLS-1$
    }

    protected int parseInt(String value, int radix)
    {
        String value2 = value;
        switch(radix) {
            case 16:
                value2 = value2.substring(2);
        }
        return (int)Long.parseLong(value2,radix);
    }

    protected String formatInt(int value, int radix, String prefix)
    {
        switch(radix) {
            case 16:
                return prefix+Integer.toHexString(value);
        }
        return Integer.toString(value,radix);
    }
}
