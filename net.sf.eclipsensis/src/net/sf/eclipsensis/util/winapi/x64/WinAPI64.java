/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util.winapi.x64;

import net.sf.eclipsensis.util.RegistryValue;
import net.sf.eclipsensis.util.winapi.*;

public final class WinAPI64 extends WinAPI
{
    static {
        System.loadLibrary("WinAPI_x64"); //$NON-NLS-1$
        init();
    }

    @Override
    public ILongPtr callWindowProc(ILongPtr lpWndProc, IHandle hWnd, int Msg, ILongPtr wParam, ILongPtr lParam)
    {
        return new LongPtr64(CallWindowProc(((LongPtr64)lpWndProc).value, ((Handle64)hWnd).value, Msg,
                        ((LongPtr64)wParam).value, ((LongPtr64)lParam).value));
    }

    @Override
    public void drawWidgetThemeBackGround(IHandle hWnd, IHandle hDC, String theme, int partId, int stateId)
    {
        DrawWidgetThemeBackGround(((Handle64)hWnd).value, ((Handle64)hDC).value, theme, partId, stateId);
    }

    @Override
    public void drawWidgetThemeBorder(IHandle hWnd, IHandle hDC, String theme, int partId, int stateId)
    {
        DrawWidgetThemeBorder(((Handle64)hWnd).value, ((Handle64)hDC).value, theme, partId, stateId);
    }

    @Override
    public IHandle getDesktopWindow()
    {
        return new Handle64(GetDesktopWindow());
    }

    @Override
    public int getRegValuesCount(IHandle hKey)
    {
        return GetRegValuesCount(((Handle64)hKey).value);
    }

    @Override
    public int getWindowLong(IHandle hWnd, int nIndex)
    {
        return GetWindowLong(((Handle64)hWnd).value, nIndex);
    }

    @Override
    public ILongPtr getWindowLongPtr(IHandle hWnd, int nIndex)
    {
        return new LongPtr64(GetWindowLongPtr(((Handle64)hWnd).value, nIndex));
    }

    @Override
    public IHandle htmlHelp(IHandle hwndCaller, String pszFile, int uCommand, int dwData)
    {
        return new Handle64(HtmlHelp(((Handle64)hwndCaller).value, pszFile, uCommand, dwData));
    }

    @Override
    public boolean playSound(String pszFilename, IHandle hModule, int dwFlags)
    {
        return PlaySound(pszFilename, ((Handle64)hModule).value, dwFlags);
    }

    @Override
    public void regCloseKey(IHandle hKey)
    {
        RegCloseKey(((Handle64)hKey).value);
    }

    @Override
    public String regEnumKeyEx(IHandle hKey, int index, int subKeySize)
    {
        return RegEnumKeyEx(((Handle64)hKey).value, index, subKeySize);
    }

    @Override
    public boolean regEnumValue(IHandle hKey, int index, RegistryValue objRegValue)
    {
        return RegEnumValue(((Handle64)hKey).value, index, objRegValue);
    }

    @Override
    public String[] regGetSubKeys(IHandle hRootKey, String pszSubKey)
    {
        return RegGetSubKeys(((Handle64)hRootKey).value, pszSubKey);
    }

    @Override
    public boolean regKeyExists(IHandle hRootKey, String pszSubKey)
    {
        return RegKeyExists(((Handle64)hRootKey).value, pszSubKey);
    }

    @Override
    public IHandle regOpenKeyEx(IHandle hKey, String lpSubKey, int ulOptions, int regSam)
    {
        return new Handle64(RegOpenKeyEx(((Handle64)hKey).value, lpSubKey, ulOptions, regSam));
    }

    @Override
    public void regQueryInfoKey(IHandle hKey, int[] sizes)
    {
        RegQueryInfoKey(((Handle64)hKey).value, sizes);
    }

    @Override
    public String regQueryStrValue(IHandle hRootKey, String pszSubKey, String pszValue)
    {
        return RegQueryStrValue(((Handle64)hRootKey).value, pszSubKey, pszValue);
    }

    @Override
    public ILongPtr sendMessage(IHandle hWnd, int msg, ILongPtr wParam, ILongPtr lParam)
    {
        return new LongPtr64(SendMessage(((Handle64)hWnd).value, msg, ((LongPtr64)wParam).value,
                        ((LongPtr64)lParam).value));
    }

    @Override
    public boolean setLayeredWindowAttributes(IHandle hWnd, int red, int green, int blue, int bAlpha, int dwFlags)
    {
        return SetLayeredWindowAttributes(((Handle64)hWnd).value, red, green, blue, bAlpha, dwFlags);
    }

    @Override
    public int setWindowLong(IHandle hWnd, int nIndex, int dwNewLong)
    {
        return SetWindowLong(((Handle64)hWnd).value,nIndex,dwNewLong);
    }

    @Override
    public ILongPtr setWindowLongPtr(IHandle hWnd, int nIndex, ILongPtr dwNewLong)
    {
        return new LongPtr64(SetWindowLongPtr(((Handle64)hWnd).value,nIndex,((LongPtr64)dwNewLong).value));
    }

    @Override
    public IHandle createHandle(Number value)
    {
        return new Handle64(value.longValue());
    }

    @Override
    public ILongPtr createLongPtr(Number value)
    {
        return new LongPtr64(value.longValue());
    }

    private static native void init();

    private native int SetWindowLong(long hWnd, int nIndex, int dwNewLong);

    private native int GetWindowLong(long hWnd, int nIndex);

    private native long SetWindowLongPtr(long hWnd, int nIndex, long dwNewLong);

    private native long GetWindowLongPtr(long hWnd, int nIndex);

    private native boolean SetLayeredWindowAttributes(long hWnd, int red, int green, int blue,
                    int bAlpha, int dwFlags);

    private native long GetDesktopWindow();

    private native long HtmlHelp(long hwndCaller, String  pszFile, int uCommand, int dwData);

    private native long SendMessage(long hWnd, int msg, long wParam, long lParam);

    private native long CallWindowProc(long lpWndProc, long hWnd, int Msg, long wParam, long lParam);

    private native void DrawWidgetThemeBackGround(long hWnd, long hDC, String theme, int partId, int stateId);

    private native void DrawWidgetThemeBorder(long hWnd, long hDC, String theme, int partId, int stateId);

    private native boolean PlaySound(String pszFilename, long hModule, int dwFlags);

    private native String RegQueryStrValue(long hRootKey, String pszSubKey, String pszValue);

    private native String[] RegGetSubKeys(long hRootKey, String pszSubKey);

    private native boolean RegKeyExists(long hRootKey, String pszSubKey);

    private native long RegOpenKeyEx(long hKey, String lpSubKey, int ulOptions, int regSam);

    private native void RegCloseKey(long hKey);

    private native void RegQueryInfoKey(long hKey, int[] sizes);

    private native String RegEnumKeyEx(long hKey, int index, int subKeySize);

    private native int GetRegValuesCount(long hKey);

    private native boolean RegEnumValue(long hKey, int index, RegistryValue objRegValue);
}
