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


import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Manager for colors used in the NSIS plugin
 */
public class ColorManager
{
    public static final RGB GREEN = new RGB(0,0xff,0);
    public static final RGB BLUE = new RGB(0,0,0xff);
    public static final RGB RED = new RGB(0xff,0,0);
    public static final RGB WHITE = new RGB(0xff, 0xff, 0xff);
    public static final RGB BLACK = new RGB(0, 0, 0);

    private static ColorRegistry cColorRegistry;

    private static synchronized ColorRegistry getColorRegistry()
    {
        if(cColorRegistry == null) {
            if(Display.getCurrent() != null) {
                cColorRegistry = new ColorRegistry(Display.getCurrent());
            }
            else if(PlatformUI.isWorkbenchRunning()) {
                cColorRegistry = new ColorRegistry(PlatformUI.getWorkbench().getDisplay());
            }
            else {
                Display.getDefault().syncExec(new Runnable(){
                    public void run()
                    {
                        cColorRegistry = new ColorRegistry(Display.getDefault());
                    }
                });
            }
        }
        return cColorRegistry;
    }

    private ColorManager()
    {
    }

    public static Color getSystemColor(int colorId)
    {
        RGB rgb = getRGB(WinAPI.INSTANCE.getSysColor(colorId));
        //Need to flip
        int temp = rgb.red;
        rgb.red=rgb.blue;
        rgb.blue=temp;
        return getColor(rgb);
    }

    public static RGB getRGB(int pixel)
    {
        int pixel2 = pixel;
        int blue = pixel2 % 256;
        pixel2 /= 256;
        int green = pixel2 % 256;
        pixel2 /= 256;
        int red = pixel2 % 256;
        return new RGB(red,green,blue);
    }

    /**
     * Return the Color that is stored in the Color table as rgb.
     */
    public static Color getColor(RGB rgb)
    {
        Color color = null;
        if(rgb != null) {
            String rgbName = rgb.toString();
            synchronized(rgbName.intern()) {
                ColorRegistry colorRegistry = getColorRegistry();
                color = colorRegistry.get(rgbName);
                if (color == null) {
                    colorRegistry.put(rgbName, rgb);
                    color = colorRegistry.get(rgbName);
                }
            }
        }
        return color;
    }

    public static Color getNegativeColor(RGB rgb)
    {
        return getColor(new RGB(255 & ~rgb.red, 255 & ~rgb.green, 255 & ~rgb.blue));
    }

    public static Color getNegativeColor(Color color)
    {
        return getNegativeColor(color.getRGB());
    }

    /**
     * @param rgb
     * @return
     */
    public static RGB hexToRGB(String hexString)
    {
        RGB rgb = new RGB(0,0,0);
        rgb.red = Integer.parseInt(hexString.substring(0,2),16);
        rgb.green = Integer.parseInt(hexString.substring(2,4),16);
        rgb.blue = Integer.parseInt(hexString.substring(4,6),16);
        return rgb;
    }

    /**
     * @param rgb
     * @return
     */
    public static String rgbToHex(RGB rgb)
    {
        return new StringBuffer(Common.leftPad(Integer.toHexString(rgb.red),2,'0')).append(
                        Common.leftPad(Integer.toHexString(rgb.green),2,'0')).append(
                                        Common.leftPad(Integer.toHexString(rgb.blue),2,'0')).toString().toUpperCase();
    }
}