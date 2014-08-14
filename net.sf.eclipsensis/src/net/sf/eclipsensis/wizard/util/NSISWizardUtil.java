/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import java.util.*;
import java.util.regex.*;

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.INSISWizardConstants;

public class NSISWizardUtil
{
    private static Pattern cValidNSISPrefixedPathNameSuffix = Pattern.compile("\\\\((((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2})\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]]\\\\?)+)?"); //$NON-NLS-1$

    private NSISWizardUtil()
    {
    }

    public static String convertPath(int targetPlatform, String path)
    {
        String path2 = path;
        switch(targetPlatform) {
            case INSISWizardConstants.TARGET_PLATFORM_X64:
                path2 = IOUtility.convertPathTo64bit(path2);
                break;
            case INSISWizardConstants.TARGET_PLATFORM_X86:
                path2 = IOUtility.convertPathTo32bit(path2);
                break;
        }
        return path2;
    }

    public static String[] getPathConstantsAndVariables(int targetPlatform)
    {
        List<String> list = new ArrayList<String>();
        String[] array = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.PATH_CONSTANTS_AND_VARIABLES);
        for (int i = 0; i < array.length; i++) {
            boolean exclude = false;
            switch(targetPlatform) {
                case INSISWizardConstants.TARGET_PLATFORM_X64:
                    exclude = IOUtility.is32BitPath(array[i]);
                    break;
                case INSISWizardConstants.TARGET_PLATFORM_X86:
                    exclude = IOUtility.is64BitPath(array[i]);
                    break;
            }
            if(!exclude) {
                list.add(array[i]);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static boolean isValidNSISPathName(int targetPlatform, String pathName)
    {
        if(pathName != null && pathName.length() > 0) {
            int n = pathName.indexOf('\\');
            String suffix = null;
            String prefix = null;
            if(n >= 1) {
                suffix = pathName.substring(n);
                prefix = pathName.substring(0,n);
            }
            else {
                prefix = pathName;
            }
            if(!Common.isEmpty(prefix) && prefix.startsWith("$")) { //$NON-NLS-1$
                String[] array = getPathConstantsAndVariables(targetPlatform);
                for(int i=0; i<array.length; i++) {
                    if(array[i].equalsIgnoreCase(prefix)) {
                        if(!Common.isEmpty(suffix)) {
                            Matcher matcher = NSISWizardUtil.cValidNSISPrefixedPathNameSuffix.matcher(suffix);
                            return matcher.matches();
                        }
                        return true;
                    }
                }
                return false;
            }
        }
        return IOUtility.isValidAbsolutePathName(pathName);
    }
}
