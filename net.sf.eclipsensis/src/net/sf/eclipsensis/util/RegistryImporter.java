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

import java.io.*;
import java.util.Map;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.RegistryKeySelectionDialog;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.winapi.WinAPI;
import net.sf.eclipsensis.util.winapi.WinAPI.HKEY;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;

public class RegistryImporter
{
    public static final RegistryImporter INSTANCE = new RegistryImporter();

    private static final String REG_EXE = "reg.exe"; //$NON-NLS-1$

    private static final long DWORD_MAXVALUE = Long.parseLong("ffffffff", 16); //$NON-NLS-1$

    private static String[] cRegFileFilters = Common.tokenize(EclipseNSISPlugin.getResourceString("regfile.filters"),','); //$NON-NLS-1$
    private static String[] cRegFileFilterNames = Common.tokenize(EclipseNSISPlugin.getResourceString("regfile.filter.names"),','); //$NON-NLS-1$
    private static File cRegExe = null;

    private static Map<String, String> cRootKeyHandleMap = new CaseInsensitiveMap<String>();

    private boolean mShowMultiSZWarning = true;

    private String mRegKey = null;

    private static void putRootKeyHandle(String longName, String shortName, HKEY hKey)
    {
        String hexHandle = "0x"+hKey.getHandle().toHexString(); //$NON-NLS-1$
        cRootKeyHandleMap.put(longName, hexHandle);
        cRootKeyHandleMap.put(shortName, hexHandle);
    }

    static {
        putRootKeyHandle("HKEY_CLASSES_ROOT","HKCR",HKEY.HKEY_CLASSES_ROOT); //$NON-NLS-1$ //$NON-NLS-2$
        putRootKeyHandle("HKEY_CURRENT_USER","HKCU",HKEY.HKEY_CURRENT_USER); //$NON-NLS-1$ //$NON-NLS-2$
        putRootKeyHandle("HKEY_LOCAL_MACHINE","HKLM",HKEY.HKEY_LOCAL_MACHINE); //$NON-NLS-1$ //$NON-NLS-2$
        putRootKeyHandle("HKEY_USERS","HKU",HKEY.HKEY_USERS); //$NON-NLS-1$ //$NON-NLS-2$
        putRootKeyHandle("HKEY_PERFORMANCE_DATA","HKPD",HKEY.HKEY_PERFORMANCE_DATA); //$NON-NLS-1$ //$NON-NLS-2$
        putRootKeyHandle("HKEY_CURRENT_CONFIG","HKCC",HKEY.HKEY_CURRENT_CONFIG); //$NON-NLS-1$ //$NON-NLS-2$
        putRootKeyHandle("HKEY_DYN_DATA","HKDD",HKEY.HKEY_DYN_DATA); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public RegistryImporter()
    {
    }

    public String getRegKey()
    {
        return mRegKey;
    }

    public void setRegKey(String regKey)
    {
        mRegKey = regKey;
    }

    private File findRegExe(Shell shell)
    {
        File regExe = null;
        String pref = NSISPreferences.getInstance().getString(INSISPreferenceConstants.REG_EXE_LOCATION);
        if(!Common.isEmpty(pref)) {
            regExe = new File(pref);
            if(IOUtility.isValidFile(regExe)) {
                return regExe;
            }
            regExe = null;
        }
        String winDir = WinAPI.INSTANCE.getEnvironmentVariable("SystemRoot"); //$NON-NLS-1$
        if(winDir == null) {
            winDir = WinAPI.INSTANCE.getEnvironmentVariable("windir"); //$NON-NLS-1$
        }
        if(winDir != null) {
            File sys32Dir = new File(winDir,"system32"); //$NON-NLS-1$
            regExe = new File(sys32Dir,REG_EXE);
        }
        if(IOUtility.isValidFile(regExe)) {
            return regExe;
        }
        regExe = null;
        String path = WinAPI.INSTANCE.getEnvironmentVariable("PATH"); //$NON-NLS-1$
        if(!Common.isEmpty(path)) {
            String[] paths = Common.tokenize(path, File.pathSeparatorChar);
            if(!Common.isEmptyArray(paths)) {
                for (int i = 0; i < paths.length; i++) {
                    if(!paths[i].equalsIgnoreCase(winDir)) {
                        regExe = new File(paths[i],REG_EXE);
                        if(IOUtility.isValidFile(regExe)) {
                            return regExe;
                        }
                        regExe = null;
                    }
                }
            }
        }

        Common.openWarning(shell, EclipseNSISPlugin.getResourceString("insert.regkey.messagebox.title"), EclipseNSISPlugin.getResourceString("select.reg.exe.warning"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$ //$NON-NLS-2$
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFileName(REG_EXE);
        dialog.setText(EclipseNSISPlugin.getResourceString(EclipseNSISPlugin.getResourceString("select.reg.exe.message"))); //$NON-NLS-1$
        String file = dialog.open();
        if(file != null) {
            regExe = new File(file);
            if(IOUtility.isValidFile(regExe)) {
                return regExe;
            }
        }
        return null;
    }

    private File getRegExe(Shell shell)
    {
        if(!IOUtility.isValidFile(cRegExe)) {
            cRegExe = findRegExe(shell);
            NSISPreferences.getInstance().setValue(INSISPreferenceConstants.REG_EXE_LOCATION, (cRegExe==null?"":cRegExe.getAbsolutePath())); //$NON-NLS-1$
        }
        return cRegExe;
    }

    public void importRegKey(final Shell shell, final IRegistryImportStrategy callback)
    {
        final File regExe = getRegExe(shell);
        if(regExe == null) {
            Common.openError(shell, EclipseNSISPlugin.getResourceString("insert.regkey.messagebox.title"), EclipseNSISPlugin.getResourceString("missing.reg.exe.error"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
            final RegistryKeySelectionDialog dialog = new RegistryKeySelectionDialog(shell);
            if(mRegKey != null) {
                dialog.setRegKey(mRegKey);
            }
            dialog.setText(EclipseNSISPlugin.getResourceString("select.regkey.message")); //$NON-NLS-1$
            if(dialog.open() == Window.OK) {
                try {
                    final File regFile = File.createTempFile("exp", INSISConstants.REG_FILE_EXTENSION); //$NON-NLS-1$
                    BusyIndicator.showWhile(shell.getDisplay(),new Runnable() {
                        public void run()
                        {
                            if (IOUtility.isValidFile(regFile)) {
                                regFile.delete();
                            }
                            mRegKey = dialog.getRegKey();
                            String[] cmdArray = {regExe.getAbsolutePath(),"export", //$NON-NLS-1$
                                            mRegKey,
                                            regFile.getAbsolutePath()};
                            try {
                                Process p = Runtime.getRuntime().exec(cmdArray);
                                p.waitFor();
                            }
                            catch (Exception e) {
                                regFile.delete();
                            }
                        }
                    });
                    if (IOUtility.isValidFile(regFile)) {
                        importRegFile(shell, regFile.getAbsolutePath(), callback);
                    }
                    else {
                        throw new RuntimeException(EclipseNSISPlugin.getFormattedString("exec.reg.exe.error", //$NON-NLS-1$
                                        new String[]{regExe.getName()}));
                    }
                }
                catch (Exception e) {
                    Common.openError(shell, EclipseNSISPlugin.getResourceString("error.title"),  //$NON-NLS-1$
                                    e.getMessage(), EclipseNSISPlugin.getShellImage());
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    public void importRegFile(Shell shell, IRegistryImportStrategy callback)
    {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(cRegFileFilters);
        dialog.setFilterNames(cRegFileFilterNames);
        dialog.setText(EclipseNSISPlugin.getResourceString("insert.regfile.description")); //$NON-NLS-1$
        importRegFile(shell, dialog.open(), callback);
    }

    public void importRegFile(final Shell shell, final String filename, final IRegistryImportStrategy callback)
    {
        BusyIndicator.showWhile(shell.getDisplay(),new Runnable() {
            public void run()
            {
                if(!Common.isEmpty(filename)) {
                    File regFile = new File(filename);
                    if(IOUtility.isValidFile(regFile)) {
                        FileInputStream fis = null;
                        BufferedReader br = null;
                        try {
                            boolean isRegEdit4 = false;
                            boolean isRegEdit5 = false;

                            byte[] bytes = new byte[2];
                            fis = new FileInputStream(regFile);
                            int n = fis.read(bytes);
                            if(n < bytes.length) {
                                throw new RuntimeException(EclipseNSISPlugin.getResourceString("invalid.regfile.error")); //$NON-NLS-1$
                            }
                            if(bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE) {
                                isRegEdit5 = true;
                                fis.close();
                                fis = new FileInputStream(regFile);
                                br = new BufferedReader(new InputStreamReader(fis,"UTF-16")); //$NON-NLS-1$
                            }
                            else if(bytes[0] == 'R' && bytes[1] == 'E') {
                                fis.close();
                                fis = new FileInputStream(regFile);
                                isRegEdit4 = true;
                                br = new BufferedReader(new InputStreamReader(fis,"8859_1")); //$NON-NLS-1$
                            }
                            else {
                                throw new RuntimeException(EclipseNSISPlugin.getResourceString("invalid.regfile.error")); //$NON-NLS-1$
                            }

                            String line = br.readLine();
                            if(line != null) {
                                if ( !(isRegEdit4 && line.equals("REGEDIT4")) && //$NON-NLS-1$
                                                !(isRegEdit5 && line.equals("Windows Registry Editor Version 5.00"))) { //$NON-NLS-1$
                                    throw new RuntimeException(EclipseNSISPlugin.getResourceString("invalid.regfile.error")); //$NON-NLS-1$
                                }
                                String rootKey = null;
                                String subKey = null;
                                int count = 0;

                                while((line = br.readLine()) != null) {
                                    line = line.trim();
                                    if(line.length() == 0) {
                                        if(rootKey != null && subKey != null && count == 0) {
                                            callback.addRegistryKey(rootKey, subKey);
                                        }
                                        rootKey = null;
                                        subKey = null;
                                        count = 0;
                                    }
                                    else {
                                        if(rootKey == null) {
                                            if(line.charAt(0) == '[' && line.charAt(line.length()-1) == ']') {
                                                n = line.indexOf('\\');
                                                if(n > 1 && n < line.length()-2) {
                                                    rootKey = line.substring(1,n).toUpperCase();
                                                    subKey = line.substring(n+1,line.length()-1);

                                                    callback.beginRegistryKeySection(rootKey, subKey);
                                                    if(rootKey.charAt(0) == '-') {
                                                        rootKey = rootKey.substring(1);
                                                        callback.deleteRegistryKey(rootKey, subKey);
                                                        rootKey = null;
                                                        subKey = null;
                                                    }
                                                    continue;
                                                }
                                            }
                                        }
                                        else {
                                            if(line.charAt(line.length()-1) =='\\') {
                                                StringBuffer buf2 = new StringBuffer(line.substring(0,line.length()-1));
                                                line = br.readLine();
                                                while(line != null) {
                                                    line = line.trim();
                                                    if(line.charAt(line.length()-1) =='\\') {
                                                        buf2.append(line.substring(0,line.length()-1));
                                                        line = br.readLine();
                                                    }
                                                    else {
                                                        buf2.append(line);
                                                        line = buf2.toString();
                                                        break;
                                                    }
                                                }
                                            }
                                            if (line != null) {
                                                //Unescape \ character
                                                line = Common.replaceAll(line, "\\\\", "\\", false); //$NON-NLS-1$ //$NON-NLS-2$
                                                n = line.indexOf('=');
                                                if (n > 0) {
                                                    String valueName = line.substring(0, n); //remove the quotes
                                                    String value = line.substring(n + 1);
                                                    if (valueName.equals("@")) { //$NON-NLS-1$
                                                        valueName = ""; //$NON-NLS-1$
                                                    }
                                                    else if (!Common.isQuoted(valueName)) {
                                                        valueName = null;
                                                    }
                                                    else {
                                                        valueName = valueName.substring(1, valueName.length() - 1);
                                                    }
                                                    if (valueName != null) {
                                                        if (Common.isQuoted(value)) {
                                                            value = value.substring(1, value.length() - 1);
                                                            callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_SZ, value);
                                                            count++;
                                                            continue;
                                                        }
                                                        else {
                                                            if (value.equals("-")) { //$NON-NLS-1$
                                                                callback.deleteRegistryValue(rootKey, subKey, valueName);
                                                                continue;
                                                            }
                                                            else {
                                                                n = value.indexOf(':');
                                                                if (n > 0) {
                                                                    String valueType = value.substring(0, n);
                                                                    value = value.substring(n + 1);
                                                                    if (valueType.equals("dword")) { //$NON-NLS-1$
                                                                        //Validate that it is really a hex value
                                                                        long l = Long.parseLong(value, 16);
                                                                        if(l <= DWORD_MAXVALUE) {
                                                                            callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_DWORD, Long.toString(l));
                                                                            count++;
                                                                        }
                                                                        continue;
                                                                    }
                                                                    else if (valueType.equals("hex")) { //$NON-NLS-1$
                                                                        StringBuffer buf2 = new StringBuffer(""); //$NON-NLS-1$
                                                                        String[] values = Common.tokenize(value, ',');
                                                                        if (!Common.isEmptyArray(values)) {
                                                                            for (int i = 0; i < values.length; i++) {
                                                                                //Validate that it is really a hex value
                                                                                Integer.parseInt(values[i], 16);
                                                                                buf2.append(values[i]);
                                                                            }
                                                                        }
                                                                        callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_BINARY, buf2.toString());
                                                                        count++;
                                                                        continue;
                                                                    }
                                                                    else if (valueType.equals("hex(2)")) { //$NON-NLS-1$
                                                                        //Expandable String
                                                                        String[] values = Common.tokenize(value, ',');
                                                                        if (!Common.isEmptyArray(values) && values.length % 2 == 0) {
                                                                            int delta = isRegEdit4?1:2;
                                                                            bytes = new byte[values.length - delta]; //Last character is NULL
                                                                            for (int i = 0; i < bytes.length; i += delta) {
                                                                                if (isRegEdit4) {
                                                                                    bytes[i] = (byte)Integer.parseInt(values[i], 16);
                                                                                }
                                                                                else {
                                                                                    bytes[i] = (byte)Integer.parseInt(values[i + 1], 16);
                                                                                    bytes[i + 1] = (byte)Integer.parseInt(values[i], 16);
                                                                                }
                                                                            }
                                                                            callback.addRegistryValue(rootKey, subKey, valueName, WinAPI.REG_EXPAND_SZ, new String(bytes, (isRegEdit4?"8859_1":"UTF-16"))); //$NON-NLS-1$ //$NON-NLS-2$
                                                                            count++;
                                                                            continue;
                                                                        }
                                                                    }
                                                                    else if (valueType.equals("hex(7)")) { //$NON-NLS-1$
                                                                        if (mShowMultiSZWarning) {
                                                                            Common.openWarning(shell, EclipseNSISPlugin.getResourceString("warning.title"), //$NON-NLS-1$
                                                                                            EclipseNSISPlugin.getResourceString("reg.multistring.warning"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                                                            mShowMultiSZWarning = false;
                                                                        }
                                                                        continue;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        throw new RuntimeException(EclipseNSISPlugin.getResourceString("invalid.regfile.error")); //$NON-NLS-1$
                                    }
                                }
                            }
                            else {
                                throw new RuntimeException(EclipseNSISPlugin.getResourceString("invalid.regfile.error")); //$NON-NLS-1$
                            }
                        }
                        catch (Exception e) {
                            Common.openError(shell, EclipseNSISPlugin.getResourceString("error.title"),  //$NON-NLS-1$
                                            e.getMessage(), EclipseNSISPlugin.getShellImage());
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                        finally {
                            IOUtility.closeIO(br);
                            IOUtility.closeIO(fis);
                        }
                    }
                }
            }
        });
    }

    public static final String rootKeyNameToHandle(String rootKey)
    {
        String handle = cRootKeyHandleMap.get(rootKey);
        return handle==null?"":handle; //$NON-NLS-1$
    }

    public static interface IRegistryImportStrategy
    {
        public void reset();
        public void beginRegistryKeySection(String rootKey, String subKey);
        public void addRegistryKey(String rootKey, String subKey);
        public void deleteRegistryKey(String rootKey, String subKey);
        public void addRegistryValue(String rootKey, String subKey, String value, int type, String data);
        public void deleteRegistryValue(String rootKey, String subKey, String value);
    }
}
