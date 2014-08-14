/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.io.*;
import java.net.*;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class NSISBrowserUtility
{
    public static final File COLORS_CSS_FILE;
    public static final Image HOME_IMAGE;
    public static final Image BACK_IMAGE;
    public static final Image DISABLED_BACK_IMAGE;
    public static final Image FORWARD_IMAGE;
    public static final Image DISABLED_FORWARD_IMAGE;
    public static final Image HTMLHELP_IMAGE;
    public static final Image HTMLHELP_DISABLED_IMAGE;
    public static final Set<String> HTML_EXTENSIONS;

    private static RGB cBrowserHelpBackground = null;
    private static RGB cBrowserHelpForeground = null;

    private static boolean cIsAvailable= false;
    private static boolean cAvailabilityChecked= false;

    static {
        BACK_IMAGE = loadImage(EclipseNSISPlugin.getResourceString("hoverhelp.back.icon")); //$NON-NLS-1$
        DISABLED_BACK_IMAGE = loadImage(EclipseNSISPlugin.getResourceString("hoverhelp.disabled.back.icon")); //$NON-NLS-1$
        FORWARD_IMAGE = loadImage(EclipseNSISPlugin.getResourceString("hoverhelp.forward.icon")); //$NON-NLS-1$
        DISABLED_FORWARD_IMAGE = loadImage(EclipseNSISPlugin.getResourceString("hoverhelp.disabled.forward.icon")); //$NON-NLS-1$
        HOME_IMAGE = loadImage(EclipseNSISPlugin.getResourceString("hoverhelp.home.icon")); //$NON-NLS-1$
        HTMLHELP_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("hoverhelp.htmlhelp.icon")); //$NON-NLS-1$
        HTMLHELP_DISABLED_IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("hoverhelp.disabled.htmlhelp.icon")); //$NON-NLS-1$
        HTML_EXTENSIONS = Collections.unmodifiableSet(new CaseInsensitiveSet(Common.loadListProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),
                                                        "hoverhelp.html.extensions"))); //$NON-NLS-1$

        File f = null;
        try {
            f = new File(new File(EclipseNSISPlugin.getPluginStateLocation(),"hoverhelp"),"colors.css"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            f = null;
        }

        COLORS_CSS_FILE = f;
    }

    public static boolean isBrowserAvailable(Composite parent)
    {
        if (!cAvailabilityChecked) {
            try {
                Browser browser= new Browser(parent, SWT.NONE);
                browser.dispose();
                cIsAvailable= true;
            }
            catch (SWTError er) {
                cIsAvailable= false;
            }
            finally {
                cAvailabilityChecked= true;
            }

        }

        return cIsAvailable;
    }

    private static Image loadImage(String file)
    {
        Image image = null;
        File f = null;
        try {
            f = IOUtility.ensureLatest(EclipseNSISPlugin.getDefault().getBundle(),
                                       new Path(file),
                                       new File(EclipseNSISPlugin.getPluginStateLocation(),EclipseNSISPlugin.getResourceString("hoverhelp.state.location"))); //$NON-NLS-1$
            image = new Image(Display.getCurrent(),f.getAbsolutePath());
            EclipseNSISPlugin.getImageManager().putImage(f.toURI().toURL(), image);
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        return image;
    }

    public static void updateColorStyles()
    {
        if(Display.getCurrent() != null && COLORS_CSS_FILE != null) {
            try {
                Display d = Display.getCurrent();
                RGB fg = d.getSystemColor(SWT.COLOR_INFO_FOREGROUND).getRGB();
                RGB bg = d.getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();
                if(!fg.equals(cBrowserHelpForeground) || !bg.equals(cBrowserHelpBackground)) {
                    StringBuffer buf = new StringBuffer("body { text: #"); //$NON-NLS-1$
                    buf.append(ColorManager.rgbToHex(fg)).append("; background-color: #").append( //$NON-NLS-1$
                    ColorManager.rgbToHex(bg)).append("}\n"); //$NON-NLS-1$

                    RGB bg2 = new RGB(Math.max(0,bg.red-8),Math.max(0,bg.green-8),Math.max(0,bg.blue-8));
                    buf.append("pre { background-color: #").append( //$NON-NLS-1$
                            ColorManager.rgbToHex(bg2)).append("}\n"); //$NON-NLS-1$
                    bg2 = new RGB(Math.max(0,bg.red-169),Math.max(0,bg.green-138),Math.max(0,bg.blue-102));
                    buf.append("a { color: #").append( //$NON-NLS-1$
                            ColorManager.rgbToHex(bg2)).append("}\n"); //$NON-NLS-1$
                    bg2 = new RGB(Math.max(0,bg.red-11),Math.max(0,bg.green-11),Math.max(0,bg.blue-11));
                    buf.append("a:hover { background-color: #").append( //$NON-NLS-1$
                            ColorManager.rgbToHex(bg2)).append("}\n"); //$NON-NLS-1$
                    IOUtility.writeContentToFile(COLORS_CSS_FILE, buf.toString().getBytes());

                    cBrowserHelpBackground = bg;
                    cBrowserHelpForeground = fg;
                }
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    public static void handleURL(String url, INSISBrowserKeywordURLHandler keywordURLHandler, INSISBrowserFileURLHandler fileURLHandler)
    {
        String url2 = url;
        if (url2.regionMatches(true, 0, NSISHelpURLProvider.KEYWORD_URI_SCHEME, 0, NSISHelpURLProvider.KEYWORD_URI_SCHEME.length())) {
            String keyword = url2.substring(NSISHelpURLProvider.KEYWORD_URI_SCHEME.length());
            keywordURLHandler.handleKeyword(keyword);
        }
        else if (url2.regionMatches(true, 0, NSISHelpURLProvider.HELP_URI_SCHEME, 0, NSISHelpURLProvider.HELP_URI_SCHEME.length())) {
            url2 = url2.substring(NSISHelpURLProvider.HELP_URI_SCHEME.length());
            NSISHelpURLProvider.getInstance().showHelp(url2);
        }
        else if (url2.regionMatches(true, 0, NSISHTMLHelp.FILE_URI_SCHEME, 0, NSISHTMLHelp.FILE_URI_SCHEME.length())) {
            try {
                fileURLHandler.handleFile(new File(new URI(url2)));
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        else {
            File f= new File(url2);
            if(IOUtility.isValidFile(f)) {
                fileURLHandler.handleFile(f);
            }
            else {
                Common.openExternalBrowser(url2);
            }
        }
    }

    public static final String ABOUT_BLANK = "about:blank"; //$NON-NLS-1$
}
