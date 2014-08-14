/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.lang.reflect.Method;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.winapi.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.widgets.*;

/*
 * The use of the Callback class is, strictly speaking, verboten.
 * However, we do what we have to do...
 */
@SuppressWarnings("restriction")
public class ControlSubclasser
{
    private static ILongPtr cNewProc;
    private static Map<IHandle, ControlInfo> cProcMap = new HashMap<IHandle, ControlInfo>(101);

    static {
        SubclassCallback subCallback = new SubclassCallback();
        final Callback callback = new Callback(subCallback,"windowProc",4); //$NON-NLS-1$
        Display.getDefault().disposeExec(new Runnable() {
            public void run()
            {
                callback.dispose();
            }
        });
        cNewProc = getCallbackAddress(callback);
    }

    private static ILongPtr getCallbackAddress(Callback callback)
    {
        ILongPtr address = WinAPI.ZERO_LONGPTR;
        if (callback != null)
        {
            try
            {
                Class<? extends Callback> clasz = callback.getClass();
                Method m = clasz.getMethod("getAddress");
                address = WinAPI.INSTANCE.createLongPtr(((Number) m.invoke(callback)));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return address;
    }

    private ControlSubclasser()
    {
    }

    public static void subclassControl(Control control, SWTControlFigure figure)
    {
        final IHandle handle = Common.getControlHandle(control);
        final ILongPtr oldProc = WinAPI.INSTANCE.getWindowLongPtr(handle, WinAPI.GWL_WNDPROC);
        cProcMap.put(handle,new ControlInfo(oldProc, figure));
        WinAPI.INSTANCE.setWindowLongPtr(handle, WinAPI.GWL_WNDPROC, cNewProc);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                WinAPI.INSTANCE.setWindowLongPtr(handle, WinAPI.GWL_WNDPROC, oldProc);
                cProcMap.remove(handle);
            }
        });
    }

    private static class SubclassCallback
    {
        public SubclassCallback()
        {
        }

        @SuppressWarnings("unused")
        public int windowProc(int hWnd, int msg, int wParam, int lParam)
        {
            return windowProc(WinAPI.INSTANCE.createHandle(hWnd), msg,
                            WinAPI.INSTANCE.createLongPtr(wParam),
                            WinAPI.INSTANCE.createLongPtr(lParam)).getValue().intValue();
        }

        @SuppressWarnings("unused")
        public long windowProc(long hWnd, long msg, long wParam, long lParam)
        {
            return windowProc(WinAPI.INSTANCE.createHandle(hWnd), (int)msg,
                            WinAPI.INSTANCE.createLongPtr(wParam),
                            WinAPI.INSTANCE.createLongPtr(lParam)).getValue().longValue();
        }

        public ILongPtr windowProc(IHandle hWnd, int msg, ILongPtr wParam, ILongPtr lParam)
        {
            ILongPtr res;

            switch (msg)
            {
                case WinAPI.WM_NCHITTEST:
                    res = WinAPI.INSTANCE.createLongPtr(WinAPI.HTTRANSPARENT);
                    break;
                case WinAPI.WM_SETFOCUS:
                case WinAPI.WM_KEYDOWN:
                case WinAPI.WM_CHAR:
                case WinAPI.WM_SYSCHAR:
                    res = WinAPI.ZERO_LONGPTR;
                    break;
                default:
                    try {
                        res=WinAPI.INSTANCE.callWindowProc(cProcMap.get(hWnd).oldProc,
                                        hWnd, msg, wParam, lParam);
                    }
                    catch(Throwable t) {
                        InstallOptionsPlugin.getDefault().log(t);
                        res = WinAPI.ZERO_LONGPTR;
                    }
            }

            return res;
        }
    }

    private static class ControlInfo
    {
        ILongPtr oldProc;
        @SuppressWarnings("unused")
        SWTControlFigure figure;

        public ControlInfo(ILongPtr oldProc, SWTControlFigure figure)
        {
            super();
            this.oldProc = oldProc;
            this.figure = figure;
        }
    }
}