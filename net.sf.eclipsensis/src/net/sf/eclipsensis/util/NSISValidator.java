/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.File;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.winapi.WinAPI;

public class NSISValidator implements INSISConstants
{
    private static final Pattern cVersionPattern = Pattern.compile("v?(\\d+(?:\\.\\d+){0,3}(?:\\-?[A-Za-z]+\\d*)?)"); //$NON-NLS-1$
    private static final Pattern cCVSVersionPattern = Pattern.compile("v?([0-3][0-9]-[a-zA-Z]{3}-20[0-9]{2})\\.cvs"); //$NON-NLS-1$
    private static final SimpleDateFormat cCVSDateFormat;
    private static final SimpleDateFormat cCVSEnglishDateFormat;
    public static final String NSIS_VERSION="NSIS_VERSION"; //$NON-NLS-1$
    public static final String DEFINED_SYMBOLS_PREFIX = "Defined symbols: "; //$NON-NLS-1$
    private static Map<Version, Date> cVersionDateMap;

    static {
        TimeZone tz = TimeZone.getTimeZone("GMT"); //$NON-NLS-1$
        cCVSDateFormat = new SimpleDateFormat("dd-MMM-yyyy"); //$NON-NLS-1$
        cCVSDateFormat.setTimeZone(tz);
        if(Locale.getDefault().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            cCVSEnglishDateFormat = null;
        }
        else {
            cCVSEnglishDateFormat = new SimpleDateFormat("dd-MMM-yyyy",Locale.ENGLISH); //$NON-NLS-1$
            cCVSEnglishDateFormat.setTimeZone(tz);
        }


        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NSISValidator.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }
        Map<String, String> map = Common.loadMapProperty(bundle,"version.dates"); //$NON-NLS-1$
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //$NON-NLS-1$
        sdf.setTimeZone(tz);
        cVersionDateMap = new LinkedHashMap<Version, Date>();
        for(Iterator<String> iter=map.keySet().iterator(); iter.hasNext(); ) {
            String key = iter.next();
            String value = map.get(key);
            Version v = new Version(key);
            Date d;
            try {
                d = sdf.parse(value);
            }
            catch (ParseException e) {
                EclipseNSISPlugin.getDefault().log(e);
                d = new Date(0);
            }
            cVersionDateMap.put(v,d);
        }
    }

    private NSISValidator()
    {
    }

    public static boolean isCVSVersion(Version v)
    {
        try {
            String version = "v" + v.toString(); //$NON-NLS-1$
            return cCVSVersionPattern.matcher(version).matches();
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            return false;
        }
    }

    public static String getRegistryNSISHome()
    {
        String nsisHome = WinAPI.INSTANCE.regQueryStrValue(INSISConstants.NSIS_REG_ROOTKEY.getHandle(),
                        INSISConstants.NSIS_REG_SUBKEY,INSISConstants.NSIS_REG_VALUE);
        if(Common.isEmpty(nsisHome) && EclipseNSISPlugin.getDefault().isX64())
        {
            int regView = WinAPI.INSTANCE.getRegView();
            try
            {
                WinAPI.INSTANCE.setRegView(WinAPI.KEY_WOW64_32KEY);
                nsisHome = WinAPI.INSTANCE.regQueryStrValue(INSISConstants.NSIS_REG_ROOTKEY.getHandle(),
                                INSISConstants.NSIS_REG_SUBKEY,INSISConstants.NSIS_REG_VALUE);
            }
            finally
            {
                WinAPI.INSTANCE.setRegView(regView);
            }
        }
        return nsisHome;
    }

    public static NSISExe findNSISExe(File nsisHome)
    {
        if(IOUtility.isValidDirectory(nsisHome)) {
            File file = new File(nsisHome,MAKENSIS_EXE);
            if(IOUtility.isValidFile(file)) {
                String exeName = file.getAbsoluteFile().getAbsolutePath();
                String[] output = MakeNSISRunner.runProcessWithOutput(exeName,
                                new String[]{MakeNSISRunner.MAKENSIS_HDRINFO_OPTION},
                                file.getParentFile(), 1);

                Properties definedSymbols = loadNSISDefinedSymbols(output);
                Version version = getNSISVersion(definedSymbols);

                if(version.compareTo(INSISVersions.MINIMUM_VERSION) >= 0)
                {
                    return new NSISExe(file, version, definedSymbols);
                }
            }
        }
        return null;
    }

    private static Properties loadNSISDefinedSymbols(String[] output)
    {
        Properties props = new Properties();
        if(!Common.isEmptyArray(output)) {
            for (int i = 0; i < output.length; i++) {
                if(output[i].startsWith(DEFINED_SYMBOLS_PREFIX)) {
                    StringTokenizer st = new StringTokenizer(output[i].substring(DEFINED_SYMBOLS_PREFIX.length()),","); //$NON-NLS-1$
                    while(st.hasMoreTokens()) {
                        String token = st.nextToken();
                        int n = token.indexOf('=');
                        if(n>0 && token.length() > n+1) {
                            props.put(token.substring(0,n).trim(),token.substring(n+1).trim());
                        }
                        else {
                            props.setProperty(token,Boolean.TRUE.toString());
                        }
                    }
                }
            }
        }
        return props;
    }

    private static Version getNSISVersion(Properties definedSymbols)
    {
        Version version = null;
        if(definedSymbols != null)
        {
            String ver = definedSymbols.getProperty(NSIS_VERSION);
            if (ver != null)
            {
                Matcher matcher = cVersionPattern.matcher(ver);
                if (matcher.matches())
                {
                    version = new Version(matcher.group(1));
                }
                else
                {
                    matcher = cCVSVersionPattern.matcher(ver);
                    if (matcher.matches())
                    {
                        Date cvsDate;
                        try
                        {
                            cvsDate = cCVSDateFormat.parse(matcher.group(1));
                        }
                        catch (ParseException e)
                        {
                            if (cCVSEnglishDateFormat != null)
                            {
                                try
                                {
                                    cvsDate = cCVSEnglishDateFormat.parse(matcher.group(1));
                                }
                                catch (ParseException e1)
                                {
                                    EclipseNSISPlugin.getDefault().log(e1);
                                    cvsDate = new Date(0);
                                }
                            }
                            else
                            {
                                EclipseNSISPlugin.getDefault().log(e);
                                cvsDate = new Date(0);
                            }
                        }

                        for (Iterator<Version> iter = cVersionDateMap.keySet().iterator(); iter.hasNext();)
                        {
                            Version v = iter.next();
                            Date d = cVersionDateMap.get(v);
                            if (cvsDate.compareTo(d) >= 0)
                            {
                                version = v;
                            }
                            else
                            {
                                break;
                            }
                        }
                        if (version != null)
                        {
                            version = new Version(version, ver.substring(1));
                        }
                    }
                }
            }
        }

        return version == null?Version.EMPTY_VERSION:version;
    }

}
