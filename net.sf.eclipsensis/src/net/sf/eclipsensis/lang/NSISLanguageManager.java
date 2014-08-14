/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.lang;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.core.runtime.IProgressMonitor;

public class NSISLanguageManager implements INSISHomeListener, IEclipseNSISService
{
    private static NSISLanguageManager cInstance = null;
    public static final String PROPERTY_LANGUAGES="net.sf.eclipsensis.languages"; //$NON-NLS-1$

    // !define\s+MUI_LANGNAME\s+(\S+|(")(?:\\?.)*?\2)
    private Pattern mDefineMUILangNamePattern = null;
    // !insertmacro\s+LANGFILE\s+(\S+|(")(?:\\?.)*?\2)\s+(\S+|(")(?:\\?.)*?\4)
    private Pattern mInsertMacroLangFilePattern = null;
    private Map<String, NSISLanguage> mLanguageMap = null;
    private List<NSISLanguage> mLanguages = null;
    private Map<String,String> mLocaleLanguageMap= null;
    private Map<String,String> mLanguageIdLocaleMap = null;
    private Integer mDefaultLanguageId = null;
    private PropertyChangeSupport mPropertyChangeSupport = null;
    private File mLangDir;
    private File mMuiLangDir;

    public static NSISLanguageManager getInstance()
    {
        return cInstance;
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            mLanguageMap = new CaseInsensitiveMap<NSISLanguage>();
            mLanguages = new ArrayList<NSISLanguage>();
            mPropertyChangeSupport = new PropertyChangeSupport(this);
            mDefineMUILangNamePattern = Pattern.compile(NSISKeywords.getInstance()
                            .getKeyword("!DEFINE") + "\\s+MUI_LANGNAME\\s+(\\S+|(\")(?:\\\\?.)*?\\2)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$ //$NON-NLS-2$
            mInsertMacroLangFilePattern = Pattern.compile(NSISKeywords.getInstance()
                            .getKeyword("!INSERTMACRO").toUpperCase() + "\\s+LANGFILE\\s+(\\S+|(\")(?:\\\\?.)*?\\2)\\s+(\\S+|(\")(?:\\\\?.)*?\\4)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                ResourceBundle bundle = ResourceBundle
                .getBundle(NSISLanguageManager.class.getName());
                mLocaleLanguageMap = Common.loadMapProperty(bundle,
                "locale.language.map"); //$NON-NLS-1$
                mLanguageIdLocaleMap = Common.loadMapProperty(bundle,
                "langid.locale.map"); //$NON-NLS-1$
                mDefaultLanguageId = Integer.valueOf(bundle
                                .getString("default.langid")); //$NON-NLS-1$
            }
            catch (Exception ex) {
                mDefaultLanguageId = new Integer(1033);
            }
            loadLanguages(monitor);
            NSISPreferences.getInstance().addListener(this);
            cInstance = this;
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISPreferences.getInstance().removeListener(this);
            mDefineMUILangNamePattern = null;
            mInsertMacroLangFilePattern = null;
            mLanguageMap = null;
            mLanguages = null;
            mLocaleLanguageMap= null;
            mLanguageIdLocaleMap = null;
            mDefaultLanguageId = null;
            mPropertyChangeSupport = null;
        }
    }

    public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
    {
        loadLanguages(monitor);
    }

    public void addPropertyChangedListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangedListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void loadLanguages(IProgressMonitor monitor)
    {
        try {
            if(monitor != null) {
                monitor.beginTask("", 100); //$NON-NLS-1$
                monitor.subTask(EclipseNSISPlugin.getResourceString("loading.languages.message")); //$NON-NLS-1$
            }
            mLanguageMap.clear();
            mLanguages.clear();
            mLangDir = null;
            mMuiLangDir = null;
            if(EclipseNSISPlugin.getDefault().isConfigured()) {
                File nsisHome = NSISPreferences.getInstance().getNSISHome().getLocation();
                if(nsisHome.exists()) {
                    mLangDir = new File(nsisHome,INSISConstants.LANGUAGE_FILES_LOCATION);
                    mMuiLangDir = new File(nsisHome,NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_30)>=0?
                                    INSISConstants.LANGUAGE_FILES_LOCATION:INSISConstants.MUI_LANGUAGE_FILES_LOCATION);
                    if(mLangDir.exists()) {
                        File[] langFiles = mLangDir.listFiles(new FileFilter() {
                            public boolean accept(File pathName)
                            {
                                return IOUtility.isValidFile(pathName) && pathName.getName().toLowerCase().endsWith(INSISConstants.LANGUAGE_FILES_EXTENSION);
                            }
                        });
                        for (int i = 0; i < langFiles.length; i++) {
                            NSISLanguage language = loadLanguage(langFiles[i]);
                            if (language != null) {
                                Integer langId = new Integer(language.getLangId());
                                String locale = Common.toString(mLanguageIdLocaleMap.get(langId.toString()),null);
                                if (locale != null) {
                                    mLanguageMap.put(locale, language);
                                }
                                mLanguageMap.put(language.getName(), language);
                                mLanguageMap.put(Integer.toString(langId), language);
                                mLanguages.add(language);
                            }
                            if(monitor != null) {
                                monitor.worked(2);
                            }
                        }
                    }
                }
            }
            mPropertyChangeSupport.firePropertyChange(PROPERTY_LANGUAGES,null,mLanguages);
        }
        finally {
            if(monitor != null) {
                monitor.done();
            }
        }
    }

    private String skipComments(BufferedReader br) throws IOException
    {
        String line = br.readLine();
        while(line != null) {
            line = line.trim();
            if(line.length() > 0) {
                char c=line.charAt(0);
                if(c != '#' && c != ';') {
                    break;
                }
            }
            line = br.readLine();
        }
        return line;
    }

    private NSISLanguage loadLanguage(File langFile)
    {
        NSISLanguage language = null;
        try {
            String filename = langFile.getName();
            if(filename.endsWith(INSISConstants.LANGUAGE_FILES_EXTENSION)) {
                String name = filename.substring(0,filename.length()-INSISConstants.LANGUAGE_FILES_EXTENSION.length());
                String displayName = name;
                int langId = 0;
                int codePage = 0;

                BufferedReader br = null;
                //1st non-comment line = header
                //2nd non-comment line = lang id
                //5th non-comment line = codepage
                int n;
                String line;
                try {
                    br = new BufferedReader(new UnicodeReader(new FileInputStream(langFile)));
                    n = 0;
                    line = skipComments(br);
                    outer: while (line != null) {
                        n++;
                        switch (n)
                        {
                            case 1: //header
                                break;
                            case 2: //lang id
                                langId = Integer.parseInt(line);
                                break;
                            case 3: //font
                            case 4: //font size
                                break;
                            case 5: //codepage
                                try
                                {
                                    codePage = Integer.parseInt(line);
                                }
                                catch(NumberFormatException nfe)
                                {
                                    codePage = 0;
                                }
                                break outer;
                        }
                        line = skipComments(br);
                    }
                }
                finally {
                    if(br != null) {
                        br.close();
                    }
                }
                if(IOUtility.isValidDirectory(mMuiLangDir)) {
                    File muiLangFile = new File(mMuiLangDir,name+INSISConstants.MUI_LANGUAGE_FILES_EXTENSION);
                    if(IOUtility.isValidFile(muiLangFile)) {
                        br = null;
                        try {
                            br = new BufferedReader(new FileReader(muiLangFile));
                            line = skipComments(br);
                            while (line != null) {
                                int m = line.indexOf(';');
                                if (m < 0) {
                                    m = line.indexOf('#');
                                }
                                if (m >= 0) {
                                    line = line.substring(0, m).trim();
                                }
                                if(NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_30) >= 0) {
                                    Matcher matcher = mInsertMacroLangFilePattern.matcher(line);
                                    if (matcher.matches()) {
                                        displayName = Common.maybeUnquote(matcher.group(3));
                                        break;
                                    }
                                }
                                else {
                                    Matcher matcher = mDefineMUILangNamePattern.matcher(line);
                                    if (matcher.matches()) {
                                        displayName = Common.maybeUnquote(matcher.group(1));
                                        break;
                                    }
                                }
                                line = skipComments(br);
                            }
                        }
                        finally {
                            if(br != null) {
                                br.close();
                            }
                        }
                    }
                }
                language = new NSISLanguage(name,displayName,langId, codePage);
            }
        }
        catch(Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
            language = null;
        }
        return language;
    }

    public List<NSISLanguage> getLanguages()
    {
        return new ArrayList<NSISLanguage>(mLanguages);
    }

    public NSISLanguage getDefaultLanguage()
    {
        NSISLanguage lang = null;
        //Try the user's lang id
        Version version = NSISPreferences.getInstance().getNSISVersion();
        int langId;
        if(version.compareTo(INSISVersions.VERSION_2_13) >= 0) {
            langId = WinAPI.INSTANCE.getUserDefaultUILanguage();
            lang = mLanguageMap.get(Integer.toString(langId));
        }
        if(lang == null) {
            langId = WinAPI.INSTANCE.getUserDefaultLangID();
            lang = mLanguageMap.get(Integer.toString(langId));
        }
        if(lang == null) {
            Locale locale = Locale.getDefault();
            lang = mLanguageMap.get(locale.toString());
            if(lang == null) {
                if(!Common.isEmpty(locale.getVariant())) {
                    lang = mLanguageMap.get(new Locale(locale.getLanguage(),locale.getCountry()).toString());
                }
                if (lang == null) {
                    //Try the user's language
                    lang = mLanguageMap.get(locale.getDisplayLanguage(Locale.US));
                    if (lang == null) {
                        //See if this is one of the specially mapped locales
                        lang = mLanguageMap.get(mLocaleLanguageMap.get(locale.toString()));
                        if (lang == null) {
                            //Try the default lang id
                            lang = mLanguageMap.get(mDefaultLanguageId.toString());
                            if (lang == null && mLanguages.size() > 0) {
                                //When all else fails, return the first one
                                lang = mLanguages.get(0);
                            }
                        }
                    }
                }
            }
        }

        return lang;
    }

    public Locale getDefaultLocale()
    {
        return getLocaleForLangId(mDefaultLanguageId.intValue());
    }

    public NSISLanguage getLanguage(String name)
    {
        return mLanguageMap.get(name);
    }

    public NSISLanguage getLanguage(int langId)
    {
        return mLanguageMap.get(Integer.toString(langId));
    }

    public NSISLanguage getLanguage(File langFile)
    {
        if(langFile != null && langFile.exists()) {
            if(langFile.getParent().equals(mLangDir)) {
                String name = langFile.getName();
                if(name.endsWith(INSISConstants.LANGUAGE_FILES_EXTENSION)) {
                    return getLanguage(name.substring(0,name.length()-INSISConstants.LANGUAGE_FILES_EXTENSION.length()));
                }
                else {
                    return null;
                }
            }
            return loadLanguage(langFile);
        }
        return null;
    }

    public Locale getLocaleForLangId(int langId)
    {
        Locale locale = null;
        int defaultLangId = mDefaultLanguageId.intValue();
        String strLangId = Integer.toString(langId);

        String localeName = mLanguageIdLocaleMap.get(strLangId);
        if(localeName == null && langId != defaultLangId) {
            locale = getLocaleForLangId(defaultLangId);
            mLanguageIdLocaleMap.put(strLangId,locale.toString());
        }
        if(localeName != null) {
            locale = parseLocale(localeName);
        }
        return locale;
    }

    private Locale parseLocale(String localeText)
    {
        Locale locale = null;
        StringTokenizer st = new StringTokenizer(localeText,"_"); //$NON-NLS-1$
        int n = st.countTokens();
        if(n > 0) {
            n = Math.min(n,3);
            String[] tokens = new String[n];
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = st.nextToken();
            }
            switch(n) {
                case 1:
                    locale = new Locale(tokens[0]);
                    break;
                case 2:
                    locale = new Locale(tokens[0],tokens[1]);
                    break;
                default:
                    locale = new Locale(tokens[0],tokens[1],tokens[2]);
            }
        }
        return locale;
    }
}
