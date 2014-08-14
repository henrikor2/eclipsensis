/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util.winapi;

import java.net.Authenticator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.RegistryValue;
import net.sf.eclipsensis.util.winapi.x64.WinAPI64;
import net.sf.eclipsensis.util.winapi.x86.WinAPI32;

public abstract class WinAPI
{
    public static final WinAPI INSTANCE;

    static
    {
        if(EclipseNSISPlugin.getDefault().isX64())
        {
            INSTANCE = new WinAPI64();
        }
        else
        {
            INSTANCE = new WinAPI32();
        }
    }

    public static final IHandle ZERO_HANDLE = INSTANCE.createHandle(0);
    public static final ILongPtr ZERO_LONGPTR = INSTANCE.createLongPtr(0);

    public static enum HKEY
    {
        HKEY_CLASSES_ROOT(INSTANCE.createHandle(0x80000000)),
        HKEY_CURRENT_USER(INSTANCE.createHandle(0x80000001)),
        HKEY_LOCAL_MACHINE(INSTANCE.createHandle(0x80000002)),
        HKEY_USERS(INSTANCE.createHandle(0x80000003)),
        HKEY_PERFORMANCE_DATA(INSTANCE.createHandle(0x80000004)),
        HKEY_CURRENT_CONFIG(INSTANCE.createHandle(0x80000005)),
        HKEY_DYN_DATA(INSTANCE.createHandle(0x80000006));

        private IHandle mHandle;
        public IHandle getHandle()
        {
            return mHandle;
        }

        private HKEY(IHandle handle)
        {
            mHandle = handle;
        }
    }


    public static final int GWL_EXSTYLE = 0xffffffec;
    public static final int GWL_STYLE = 0xfffffff0;
    public static final int GWL_WNDPROC = 0xfffffffc;

    public static final int WM_NCHITTEST = 0x84;
    public static final int WM_SETFOCUS = 0x7;
    public static final int WM_KEYDOWN = 0x100;
    public static final int WM_CHAR = 0x102;
    public static final int WM_SYSCHAR = 0x106;

    public static final int LWA_COLORKEY = 1;
    public static final int LWA_ALPHA = 2;

    public static final int WS_EX_LAYERED = 0x80000;
    public static final int WS_EX_LAYOUTRTL = 0x00400000;

    public static final int HH_DISPLAY_TOPIC = 0x0;

    public static final int HTTRANSPARENT = 0xffffffff;

    public static final int REG_SZ = 1;
    public static final int REG_EXPAND_SZ = 2;
    public static final int REG_BINARY = 3;
    public static final int REG_DWORD = 4;
    public static final int REG_MULTI_SZ = 7;

    public static final int KEY_QUERY_VALUE = 0x0001;
    public static final int KEY_ENUMERATE_SUB_KEYS = 0x0008;
    public static final int KEY_WOW64_32KEY = 0x0200;
    public static final int KEY_WOW64_64KEY = 0x0100;

    public static final int BS_LEFTTEXT = 0x20;
    public static final int CB_SHOWDROPDOWN = 0x14f;
    public static final int CB_GETDROPPEDSTATE = 0x157;

    public static final int WM_PRINT = 0x317;
    public static final int PRF_NONCLIENT = 0x2;
    public static final int PRF_CLIENT = 0x4;
    public static final int PRF_ERASEBKGND = 0x8;
    public static final int PRF_CHILDREN = 0x10;

    public static final int EP_EDITTEXT = 1;
    public static final int ETS_NORMAL = 1;
    public static final int ETS_DISABLED = 4;
    public static final int ETS_READONLY = 6;

    public static final int LVP_LISTITEM = 1;
    public static final int LIS_NORMAL = 1;
    public static final int LIS_DISABLED = 4;

    public static final int BP_RADIOBUTTON = 2;
    public static final int RBS_UNCHECKEDNORMAL = 1;
    public static final int RBS_UNCHECKEDHOT = 2;
    public static final int RBS_CHECKEDNORMAL = 5;
    public static final int RBS_CHECKEDHOT = 6;

    public static final int TVS_HASBUTTONS = 0x1;
    public static final int TVS_HASLINES = 0x2;

    public static final int COLOR_GRAYTEXT = 0x11;
    public static final int COLOR_3DHILIGHT = 0x14;

    public static final int WS_HSCROLL = 0x100000;
    public static final int WS_VSCROLL = 0x200000;

    public static final int LB_SETHORIZONTALEXTENT = 0x194;

    public static final int SM_CXVSCROLL = 0x2;
    public static final int SM_CYVSCROLL = 0x14;
    public static final int SM_CYHSCROLL = 0x3;

    public static final int SND_SYNC = 0x0;
    public static final int SND_ASYNC = 0x1;
    public static final int SND_NODEFAULT = 0x2;
    public static final int SND_LOOP = 0x8;
    public static final int SND_PURGE = 0x40;
    public static final int SND_FILENAME = 0x20000;

    public static final int FILE_ATTRIBUTE_ARCHIVE = 0x20;
    public static final int FILE_ATTRIBUTE_DIRECTORY = 0x10;
    public static final int FILE_ATTRIBUTE_HIDDEN = 0x2;
    public static final int FILE_ATTRIBUTE_NORMAL = 0x80;
    public static final int FILE_ATTRIBUTE_READONLY = 0x1;
    public static final int FILE_ATTRIBUTE_SYSTEM = 0x4;

    public static final int VK_SHIFT = 0x10;
    public static final int VK_CTRL = 0x11;
    public static final int VK_ALT = 0x12;

    public abstract int setWindowLong(IHandle hWnd, int nIndex, int dwNewLong);
    public abstract int getWindowLong(IHandle hWnd, int nIndex);
    public abstract ILongPtr setWindowLongPtr(IHandle hWnd, int nIndex, ILongPtr dwNewLong);
    public abstract ILongPtr getWindowLongPtr(IHandle hWnd, int nIndex);
    public abstract boolean setLayeredWindowAttributes(IHandle hWnd, int red, int green, int blue,
                    int bAlpha, int dwFlags);

    public abstract IHandle getDesktopWindow();

    public abstract IHandle htmlHelp(IHandle hwndCaller, String  pszFile, int uCommand, int dwData);

    public abstract ILongPtr sendMessage(IHandle hWnd, int msg, ILongPtr wParam, ILongPtr lParam);

    public abstract ILongPtr callWindowProc(ILongPtr lpWndProc, IHandle hWnd, int Msg, ILongPtr wParam, ILongPtr lParam);

    public abstract void drawWidgetThemeBackGround(IHandle hWnd, IHandle hDC, String theme, int partId, int stateId);

    public abstract void drawWidgetThemeBorder(IHandle hWnd, IHandle hDC, String theme, int partId, int stateId);

    public abstract boolean playSound(String pszFilename, IHandle hModule, int dwFlags);

    public abstract String regQueryStrValue(IHandle hRootKey, String pszSubKey, String pszValue);

    public abstract String[] regGetSubKeys(IHandle hRootKey, String pszSubKey);

    public abstract boolean regKeyExists(IHandle hRootKey, String pszSubKey);

    public abstract IHandle regOpenKeyEx(IHandle hKey, String lpSubKey, int ulOptions, int regSam);

    public abstract void regCloseKey(IHandle hKey);

    public abstract void regQueryInfoKey(IHandle hKey, int[] sizes);

    public abstract String regEnumKeyEx(IHandle hKey, int index, int subKeySize);

    public abstract int getRegValuesCount(IHandle hKey);

    public abstract boolean regEnumValue(IHandle hKey, int index, RegistryValue objRegValue);

    public abstract IHandle createHandle(Number value);

    public abstract ILongPtr createLongPtr(Number value);

    public final boolean areVisualStylesEnabled()
    {
        return AreVisualStylesEnabled();
    }

    public final void extractHtmlHelp(String pszFile, String pszFolder, String[] tocAndIndex)
    {
        ExtractHtmlHelp(pszFile, pszFolder, tocAndIndex);
    }

    public final String getEnvironmentVariable(String name)
    {
        return GetEnvironmentVariable(name);
    }

    public final int getFileAttributes(String pszFilename)
    {
        return GetFileAttributes(pszFilename);
    }

    public final short getKeyState(int nVirtKey)
    {
        return GetKeyState(nVirtKey);
    }

    public final Object getObjectFieldValue(Object object, String field, String signature)
    {
        return GetObjectFieldValue(object, field, signature);
    }

    public final String[] getPluginExports(String pszPluginFile)
    {
        return GetPluginExports(pszPluginFile);
    }

    public final int getRegView()
    {
        return GetRegView();
    }

    public final String getShellFolder(int id)
    {
        return GetShellFolder(id);
    }

    public final String getShortPathName(String longPathName)
    {
        return GetShortPathName(longPathName);
    }

    public final int getSysColor(int index)
    {
        return GetSysColor(index);
    }

    public final int getSystemMetrics(int nIndex)
    {
        return GetSystemMetrics(nIndex);
    }

    public final int getUserDefaultLangID()
    {
        return GetUserDefaultLangID();
    }

    public final int getUserDefaultUILanguage()
    {
        return GetUserDefaultUILanguage();
    }

    public final String loadResourceString(String pszFilename, int id, int lcid)
    {
        return LoadResourceString(pszFilename, id, lcid);
    }

    public final boolean setFileAttributes(String pszFilename, int dwAttributes)
    {
        return SetFileAttributes(pszFilename, dwAttributes);
    }

    public final void setIntFieldValue(Object object, String field, int value)
    {
        SetIntFieldValue(object, field, value);
    }

    public final void setRegView(int regView)
    {
        SetRegView(regView);
    }

    public final boolean validateWildcard(String wildcard)
    {
        return ValidateWildcard(wildcard);
    }

    public final String strftime(String format)
    {
        return Strftime(format);
    }

    public final Authenticator getDefaultAuthenticator()
    {
        return GetDefaultAuthenticator();
    }

    private native int GetUserDefaultLangID();

    private native int GetUserDefaultUILanguage();

    private native void ExtractHtmlHelp(String pszFile, String pszFolder, String[] tocAndIndex);

    private native String[] GetPluginExports(String pszPluginFile);

    private native boolean AreVisualStylesEnabled();

    private native int GetSysColor(int index);

    private native int GetSystemMetrics (int nIndex);

    private native Object GetObjectFieldValue(Object object, String field, String signature);

    private native void SetIntFieldValue(Object object, String field, int value);

    private native String GetEnvironmentVariable(String name);

    private native String GetShellFolder(int id);

    private native String GetShortPathName(String longPathName);

    private native int GetFileAttributes(String pszFilename);

    private native boolean SetFileAttributes(String pszFilename, int dwAttributes);

    private native short GetKeyState(int nVirtKey);

    private native boolean ValidateWildcard(String wildcard);

    private native void SetRegView(int regView);

    private native int GetRegView();

    private native String LoadResourceString(String pszFilename, int id, int lcid);

    private native String Strftime(String format);

    private native Authenticator GetDefaultAuthenticator();
}
