/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util.winapi.x86;

import net.sf.eclipsensis.util.RegistryValue;
import net.sf.eclipsensis.util.winapi.*;

public final class WinAPI32 extends WinAPI
{
    static {
        System.loadLibrary("WinAPI"); //$NON-NLS-1$
        init();
    }

    @Override
    public ILongPtr callWindowProc(ILongPtr lpWndProc, IHandle hWnd, int Msg, ILongPtr wParam, ILongPtr lParam)
    {
        return new LongPtr32(CallWindowProc(((LongPtr32)lpWndProc).value, ((Handle32)hWnd).value, Msg,
                        ((LongPtr32)wParam).value, ((LongPtr32)lParam).value));
    }

    @Override
    public void drawWidgetThemeBackGround(IHandle hWnd, IHandle hDC, String theme, int partId, int stateId)
    {
        DrawWidgetThemeBackGround(((Handle32)hWnd).value, ((Handle32)hDC).value, theme, partId, stateId);
    }

    @Override
    public void drawWidgetThemeBorder(IHandle hWnd, IHandle hDC, String theme, int partId, int stateId)
    {
        DrawWidgetThemeBorder(((Handle32)hWnd).value, ((Handle32)hDC).value, theme, partId, stateId);
    }

    @Override
    public IHandle getDesktopWindow()
    {
        return new Handle32(GetDesktopWindow());
    }

    @Override
    public int getRegValuesCount(IHandle hKey)
    {
        return GetRegValuesCount(((Handle32)hKey).value);
    }

    @Override
    public int getWindowLong(IHandle hWnd, int nIndex)
    {
        return GetWindowLong(((Handle32)hWnd).value, nIndex);
    }

    @Override
    public ILongPtr getWindowLongPtr(IHandle hWnd, int nIndex)
    {
        return new LongPtr32(GetWindowLongPtr(((Handle32)hWnd).value, nIndex));
    }

    @Override
    public IHandle htmlHelp(IHandle hwndCaller, String pszFile, int uCommand, int dwData)
    {
        return new Handle32(HtmlHelp(((Handle32)hwndCaller).value, pszFile, uCommand, dwData));
    }

    @Override
    public boolean playSound(String pszFilename, IHandle hModule, int dwFlags)
    {
        return PlaySound(pszFilename, ((Handle32)hModule).value, dwFlags);
    }

    @Override
    public void regCloseKey(IHandle hKey)
    {
        RegCloseKey(((Handle32)hKey).value);
    }

    @Override
    public String regEnumKeyEx(IHandle hKey, int index, int subKeySize)
    {
        return RegEnumKeyEx(((Handle32)hKey).value, index, subKeySize);
    }

    @Override
    public boolean regEnumValue(IHandle hKey, int index, RegistryValue objRegValue)
    {
        return RegEnumValue(((Handle32)hKey).value, index, objRegValue);
    }

    @Override
    public String[] regGetSubKeys(IHandle hRootKey, String pszSubKey)
    {
        return RegGetSubKeys(((Handle32)hRootKey).value, pszSubKey);
    }

    @Override
    public boolean regKeyExists(IHandle hRootKey, String pszSubKey)
    {
        return RegKeyExists(((Handle32)hRootKey).value, pszSubKey);
    }

    @Override
    public IHandle regOpenKeyEx(IHandle hKey, String lpSubKey, int ulOptions, int regSam)
    {
        return new Handle32(RegOpenKeyEx(((Handle32)hKey).value, lpSubKey, ulOptions, regSam));
    }

    @Override
    public void regQueryInfoKey(IHandle hKey, int[] sizes)
    {
        RegQueryInfoKey(((Handle32)hKey).value, sizes);
    }

    @Override
    public String regQueryStrValue(IHandle hRootKey, String pszSubKey, String pszValue)
    {
        return RegQueryStrValue(((Handle32)hRootKey).value, pszSubKey, pszValue);
    }

    @Override
    public ILongPtr sendMessage(IHandle hWnd, int msg, ILongPtr wParam, ILongPtr lParam)
    {
        return new LongPtr32(SendMessage(((Handle32)hWnd).value, msg, ((LongPtr32)wParam).value,
                        ((LongPtr32)lParam).value));
    }

    @Override
    public boolean setLayeredWindowAttributes(IHandle hWnd, int red, int green, int blue, int bAlpha, int dwFlags)
    {
        return SetLayeredWindowAttributes(((Handle32)hWnd).value, red, green, blue, bAlpha, dwFlags);
    }

    @Override
    public int setWindowLong(IHandle hWnd, int nIndex, int dwNewLong)
    {
        return SetWindowLong(((Handle32)hWnd).value,nIndex,dwNewLong);
    }

    @Override
    public ILongPtr setWindowLongPtr(IHandle hWnd, int nIndex, ILongPtr dwNewLong)
    {
        return new LongPtr32(SetWindowLongPtr(((Handle32)hWnd).value,nIndex,((LongPtr32)dwNewLong).value));
    }

    @Override
    public IHandle createHandle(Number value)
    {
        return new Handle32(value.intValue());
    }

    @Override
    public ILongPtr createLongPtr(Number value)
    {
        return new LongPtr32(value.intValue());
    }

    private static native void init();

    private native int SetWindowLong(int hWnd, int nIndex, int dwNewLong);

    private native int GetWindowLong(int hWnd, int nIndex);

    private native int SetWindowLongPtr(int hWnd, int nIndex, int dwNewLong);

    private native int GetWindowLongPtr(int hWnd, int nIndex);

    private native boolean SetLayeredWindowAttributes(int hWnd, int red, int green, int blue,
                    int bAlpha, int dwFlags);

    private native int GetDesktopWindow();

    private native int HtmlHelp(int hwndCaller, String  pszFile, int uCommand, int dwData);

    private native int SendMessage(int hWnd, int msg, int wParam, int lParam);

    private native int CallWindowProc(int lpWndProc, int hWnd, int Msg, int wParam, int lParam);

    private native void DrawWidgetThemeBackGround(int hWnd, int hDC, String theme, int partId, int stateId);

    private native void DrawWidgetThemeBorder(int hWnd, int hDC, String theme, int partId, int stateId);

    private native boolean PlaySound(String pszFilename, int hModule, int dwFlags);

    private native String RegQueryStrValue(int hRootKey, String pszSubKey, String pszValue);

    private native String[] RegGetSubKeys(int hRootKey, String pszSubKey);

    private native boolean RegKeyExists(int hRootKey, String pszSubKey);

    private native int RegOpenKeyEx(int hKey, String lpSubKey, int ulOptions, int regSam);

    private native void RegCloseKey(int hKey);

    private native void RegQueryInfoKey(int hKey, int[] sizes);

    private native String RegEnumKeyEx(int hKey, int index, int subKeySize);

    private native int GetRegValuesCount(int hKey);

    private native boolean RegEnumValue(int hKey, int index, RegistryValue objRegValue);
}
