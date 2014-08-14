/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities.util;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

import org.eclipse.jdt.launching.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Version;

public class Common
{
    public static final Integer ZERO = new Integer(0);

    private Common()
    {
        super();
    }

    public static boolean isEmpty(String str)
    {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isEmptyCollection(Collection<?> collection)
    {
        if(collection != null) {
            return collection.size() == 0;
        }
        return true;
    }

    public static boolean isEmptyArray(Object array)
    {
        if(array != null) {
            if(array.getClass().isArray()) {
                return Array.getLength(array) == 0;
            }
        }
        return true;
    }

    public static boolean isValidFile(File file)
    {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isValidDirectory(File file)
    {
        return file != null && file.exists() && file.isDirectory();
    }

    public static boolean stringsAreEqual(String str1, String str2)
    {
        return stringsAreEqual(str1, str2, false);
    }

    public static boolean objectsAreEqual(Object obj1, Object obj2)
    {
        return obj1 == null && obj2 == null ||
        obj1 !=null && obj2 != null && obj1.equals(obj2);
    }

    public static boolean stringsAreEqual(String str1, String str2, boolean ignoreCase)
    {
        return objectsAreEqual((ignoreCase?(str1==null?null:str1.toLowerCase()):str1),
                (ignoreCase?(str2==null?null:str2.toLowerCase()):str2));
    }

    public static List<String> tokenize(String text, char separator)
    {
        List<String> list = new ArrayList<String>();
        if(text != null && text.length() > 0) {
            char[] chars = text.toCharArray();
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (int i = 0; i < chars.length; i++) {
                if(chars[i] != separator) {
                    buf.append(chars[i]);
                }
                else {
                    list.add(buf.toString());
                    buf.delete(0,buf.length());
                }
            }
            list.add(buf.toString().trim());
        }
        return list;
    }

    public static <T> String flatten(Collection<T> collection, char separator)
    {
        StringBuilder buf = new StringBuilder(""); //$NON-NLS-1$
        if(!isEmptyCollection(collection))
        {
            Iterator<T> iter = collection.iterator();
            buf.append(String.valueOf(iter.next()));
            while(iter.hasNext()) {
                buf.append(separator).append(String.valueOf(iter.next()));
            }
        }
        return buf.toString();
    }

    public static Version parseVersion(String ver)
    {
        int[] parts={0,0,0};

        List<String> list = tokenize(ver,'.');
        int n = Math.min(parts.length,list.size());
        String temp=""; //$NON-NLS-1$
        for(int i=0; i<n; i++) {
            outer: {
            String token = list.get(i);
            char[] chars = token.toCharArray();
            for (int j = 0; j < chars.length; j++) {
                if(!Character.isDigit(chars[j])) {
                    parts[i] = j>0?Integer.parseInt(token.substring(0,j)):0;
                    temp=token.substring(j);
                    break outer;
                }
            }
            parts[i] = Integer.parseInt(token);
        }
        }
        StringBuffer buf = new StringBuffer(temp);
        for(int i=n; i<list.size(); i++) {
            buf.append(".").append(list.get(i)); //$NON-NLS-1$
        }

        return new Version(parts[0], parts[1], parts[2], buf.toString());
    }

    public static IVMInstall getVMInstall(Version minVersion)
    {
        IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
        if(vmInstall instanceof IVMInstall2) {
            if(parseVersion(((IVMInstall2)vmInstall).getJavaVersion()).compareTo(minVersion) >= 0) {
                return vmInstall;
            }
        }
        return null;
    }

    public static List<IVMInstall> getVMInstalls(Version minVersion, Version matchVersion)
    {
        List<IVMInstall> vmInstalls = new ArrayList<IVMInstall>();

        IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
        if(types != null) {
            for (int i = 0; i < types.length; i++) {
                IVMInstall[] installs = types[i].getVMInstalls();
                if(installs != null) {
                    for (int j = 0; j < installs.length; j++) {
                        if(installs[j] instanceof IVMInstall2) {
                            Version version = parseVersion(((IVMInstall2)installs[j]).getJavaVersion());
                            if(version.compareTo(minVersion) >= 0 &&
                                    version.getMajor() == matchVersion.getMajor() &&
                                    version.getMinor() == matchVersion.getMinor()) {
                                vmInstalls.add(installs[j]);
                            }
                        }
                    }
                }
            }
        }

        return vmInstalls;
    }

    public static List<IVMInstall> getVMInstalls(Version minVersion)
    {
        List<IVMInstall> vmInstalls = new ArrayList<IVMInstall>();

        IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
        if(types != null) {
            for (int i = 0; i < types.length; i++) {
                IVMInstall[] installs = types[i].getVMInstalls();
                if(installs != null) {
                    for (int j = 0; j < installs.length; j++) {
                        if(installs[j] instanceof IVMInstall2) {
                            Version version = parseVersion(((IVMInstall2)installs[j]).getJavaVersion());
                            if(minVersion == null || version.compareTo(minVersion) >= 0 ||
                                    version.getMajor() == minVersion.getMajor() &&
                                    version.getMinor() == minVersion.getMinor()) {
                                vmInstalls.add(installs[j]);
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(vmInstalls, new Comparator<IVMInstall>() {
            public int compare(IVMInstall o1, IVMInstall o2)
            {
                Version v1 = parseVersion(((IVMInstall2)o1).getJavaVersion());
                Version v2 = parseVersion(((IVMInstall2)o2).getJavaVersion());
                return v1.compareTo(v2);
            }
        });
        return vmInstalls;
    }

    public static Point calculateControlSize(Control control, int chars, int lines)
    {
        Point pt = new Point(0,0);
        GC gc = new GC(control);
        FontMetrics fontMetrics = gc.getFontMetrics();
        if(chars > 0) {
            pt.x = chars*fontMetrics.getAverageCharWidth();
        }
        if(lines > 0) {
            pt.y = lines*fontMetrics.getHeight();
        }
        gc.dispose();
        return pt;
    }
}
