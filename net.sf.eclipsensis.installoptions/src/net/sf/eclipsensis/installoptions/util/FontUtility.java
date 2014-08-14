/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class FontUtility
{
    private static final String UNKNOWN_FONT = "??"; //$NON-NLS-1$
    private static final String DEFAULT_DEFAULT_FONT_NAME = "MS Shell Dlg"; //$NON-NLS-1$
    public static final int DEFAULT_DEFAULT_FONT_SIZE = 8;
    public static final String DEFAULT_FONT_NAME;
    public static final int DEFAULT_FONT_SIZE;

    private static Font cDefaultFont;
    private static Font cInstallOptionsFont = null;
    private static Map<NSISLanguage,Font> cFontMap = new HashMap<NSISLanguage,Font> ();
    private static NSISSettings cNSISSettings = null;
    private static File cPropertiesFile = null;
    private static INSISConsole cNSISConsole = null;

    private static boolean cListening = false;

    private static INSISHomeListener cNSISHomeListener = new INSISHomeListener() {
        public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
        {
            if(EclipseNSISPlugin.getDefault().isConfigured()) {
                loadInstallOptionsFont();
                if(cInstallOptionsFont != null) {
                    NSISPreferences.getInstance().removeListener(cNSISHomeListener);
                }
            }
        }
    };

    static {
        String fontNameKey;
        String fontSizeKey;
        if(EclipseNSISPlugin.getDefault().isWin2K()) {
            fontNameKey = "default.font.name.win2k"; //$NON-NLS-1$
            fontSizeKey = "default.font.size.win2k"; //$NON-NLS-1$
        }
        else {
            fontNameKey = "default.font.name"; //$NON-NLS-1$
            fontSizeKey = "default.font.size"; //$NON-NLS-1$
        }
        DEFAULT_FONT_NAME = InstallOptionsPlugin.getResourceString(fontNameKey,DEFAULT_DEFAULT_FONT_NAME);
        int fontSize;
        try {
            fontSize = Integer.parseInt(InstallOptionsPlugin.getResourceString(fontSizeKey, Integer.toString(DEFAULT_DEFAULT_FONT_SIZE)));
        }
        catch (NumberFormatException e) {
            fontSize = DEFAULT_DEFAULT_FONT_SIZE;
        }
        DEFAULT_FONT_SIZE = fontSize;

        final Font[] f = new Font[1];
        final FontDescriptor fd = FontDescriptor.createFrom(DEFAULT_FONT_NAME,DEFAULT_FONT_SIZE,SWT.NORMAL);
        Runnable r = new Runnable() {
            public void run()
            {
                try {
                    f[0] = fd.createFont(Display.getDefault());
                }
                catch (DeviceResourceException e) {
                    InstallOptionsPlugin.getDefault().log(e);
                    f[0] = Display.getDefault().getSystemFont();
                }
            }
        };

        if(Display.getCurrent() != null) {
            r.run();
        }
        else {
            Display.getDefault().syncExec(r);
        }
        cDefaultFont = f[0];
        loadInstallOptionsFont();
        Display.getDefault().disposeExec(new Runnable() {
            public void run()
            {
                if(cDefaultFont != null && !cDefaultFont.isDisposed()) {
                    cDefaultFont.dispose();
                }
                for(Iterator<Font> iter = cFontMap.values().iterator(); iter.hasNext(); ) {
                    Font font = iter.next();
                    if(font != null && !font.isDisposed()) {
                        font.dispose();
                    }
                }
            }
        });
    }

    private FontUtility()
    {
    }

    private static Font loadInstallOptionsFont()
    {
        if(cInstallOptionsFont == null) {
            if (EclipseNSISPlugin.getDefault().isConfigured()) {
                cInstallOptionsFont = getFont(NSISLanguageManager.getInstance().getDefaultLanguage());
            }
            if(cInstallOptionsFont == null || cInstallOptionsFont == cDefaultFont) {
                if(!cListening) {
                    NSISPreferences.getInstance().addListener(cNSISHomeListener);
                    Display.getDefault().disposeExec(new Runnable() {
                        public void run()
                        {
                            NSISPreferences.getInstance().removeListener(cNSISHomeListener);
                            cListening = false;
                        }
                    });
                    cListening = true;
                }
            }
            else {
                if (cListening) {
                    NSISPreferences.getInstance().removeListener(cNSISHomeListener);
                    cListening = false;
                }
            }
        }
        return cInstallOptionsFont==null?cDefaultFont:cInstallOptionsFont;
    }

    public static Font getInstallOptionsFont()
    {
        return cInstallOptionsFont==null?loadInstallOptionsFont():cInstallOptionsFont;
    }

    public static Font getFont(NSISLanguage lang)
    {
        Font font = cFontMap.get(lang);
        if(font == null) {
            font = createFont(lang);
            if(font != null) {
                cFontMap.put(lang, font);
            }
            else {
                if(lang != null && !lang.equals(NSISLanguageManager.getInstance().getDefaultLanguage())) {
                    font = getInstallOptionsFont();
                }
                else {
                    font = cDefaultFont;
                }
            }
        }
        return font;
    }

    private synchronized static Font createFont(NSISLanguage lang)
    {
        Font font = null;
        if(EclipseNSISPlugin.getDefault().isConfigured() && lang != null) {
            try {
                if (cNSISSettings == null) {
                    cNSISSettings = new DummyNSISSettings();
                }
                if (cPropertiesFile == null) {
                    cPropertiesFile = File.createTempFile("font", ".properties");//$NON-NLS-1$ //$NON-NLS-2$
                    cPropertiesFile.deleteOnExit();
                }
                if (cNSISConsole == null) {
                    cNSISConsole = new NullNSISConsole();
                }
                Map<String,String> symbols = cNSISSettings.getSymbols();

                if(EclipseNSISPlugin.getDefault().isWinVista() && NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_21) >= 0) {
                    symbols.put("WINDOWS_VISTA",""); //$NON-NLS-1$ //$NON-NLS-2$
                }
                symbols.put("LANGUAGE", lang.getName()); //$NON-NLS-1$
                symbols.put("PROPERTIES_FILE", cPropertiesFile.getAbsolutePath()); //$NON-NLS-1$
                cNSISSettings.setSymbols(symbols);
                File fontScript = IOUtility.ensureLatest(InstallOptionsPlugin.getDefault().getBundle(), new Path("/font/getfont.nsi"), //$NON-NLS-1$
                                InstallOptionsPlugin.getPluginStateLocation());
                long timestamp = System.currentTimeMillis();
                MakeNSISResults results = MakeNSISRunner.compile(fontScript, cNSISSettings, cNSISConsole, new INSISConsoleLineProcessor() {
                    public NSISConsoleLine processText(String text)
                    {
                        return NSISConsoleLine.info(text);
                    }

                    public void reset()
                    {
                    }
                }, false);

                if (results != null) {
                    if (results.getReturnCode() == 0) {
                        File outfile = new File(InstallOptionsPlugin.getPluginStateLocation(), "getfont.exe"); //$NON-NLS-1$
                        if (IOUtility.isValidFile(outfile) && outfile.lastModified() > timestamp) {
                            MakeNSISRunner.testInstaller(outfile.getAbsolutePath(), null, true);
                            if (cPropertiesFile.exists()) {
                                Properties props = new Properties();
                                FileInputStream is = null;
                                try {
                                    is = new FileInputStream(cPropertiesFile);
                                    props.load(is);
                                    String fontName = props.getProperty("name"); //$NON-NLS-1$
                                    if (fontName == null) {
                                        fontName = DEFAULT_FONT_NAME;
                                    }
                                    else if (fontName.equals(UNKNOWN_FONT)) {
                                        fontName = InstallOptionsPlugin.getResourceString("unknown.font." + lang.getName().toLowerCase(), DEFAULT_FONT_NAME); //$NON-NLS-1$
                                    }
                                    int fontSize;
                                    String tmpFontSize = props.getProperty("size"); //$NON-NLS-1$
                                    if (tmpFontSize == null) {
                                        fontSize = DEFAULT_FONT_SIZE;
                                    }
                                    else {
                                        try {
                                            fontSize = Integer.parseInt(tmpFontSize);
                                        }
                                        catch (NumberFormatException e) {
                                            fontSize = DEFAULT_FONT_SIZE;
                                        }
                                    }
                                    final FontData fd = new FontData(fontName, fontSize, SWT.NORMAL);
                                    final Font[] f = {null};
                                    Runnable r = new Runnable() {
                                        public void run()
                                        {
                                            f[0] = new Font(Display.getDefault(), fd);
                                        }
                                    };
                                    if (Display.getCurrent() != null) {
                                        r.run();
                                    }
                                    else {
                                        Display.getDefault().syncExec(r);
                                    }
                                    font = f[0];
                                }
                                catch (Exception e) {
                                    InstallOptionsPlugin.getDefault().log(e);
                                    if (is != null) {
                                        try {
                                            is.close();
                                        }
                                        catch (Exception e1) {
                                            InstallOptionsPlugin.getDefault().log(e1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (IOException e1) {
                InstallOptionsPlugin.getDefault().log(e1);
            }
        }
        return font;
    }
}
