/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.*;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.codeassist.NSISBrowserUtility;
import net.sf.eclipsensis.help.search.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class NSISHelpURLProvider implements INSISConstants, INSISKeywordsListener, IEclipseNSISService
{
    private static final Version HELP_URL_PROVIDER_VERSION = new Version("1.3"); //$NON-NLS-1$

    private static final String STATE_LOCATION = "stateLocation"; //$NON-NLS-1$
    private static final String VERSION = "version"; //$NON-NLS-1$
    private static final String INDEX = "index"; //$NON-NLS-1$
    private static final String TOC = "toc"; //$NON-NLS-1$
    private static final String HELP_URLS = "helpUrls"; //$NON-NLS-1$
    private static final String CHM_HELP_URLS = "chmHelpUrls"; //$NON-NLS-1$
    private static final String KEYWORD_HELP = "keywordHelp"; //$NON-NLS-1$

    public static final String KEYWORD_HELP_HTML_PREFIX;
    public static final String KEYWORD_HELP_HTML_SUFFIX="\n</body></html>"; //$NON-NLS-1$

    public static final String KEYWORD_URI_SCHEME="keyword:"; //$NON-NLS-1$
    public static final String HELP_URI_SCHEME="help:"; //$NON-NLS-1$

    private static NSISHelpURLProvider cInstance = null;

    private static final String NO_HELP_FILE=PLUGIN_HELP_LOCATION_PREFIX+"nohelp.html"; //$NON-NLS-1$
    private static final String CHMLINK_JS = "chmlink.js"; //$NON-NLS-1$

    private static final String NSIS_PLATFORM_PLUGIN_HELP_PREFIX = new StringBuffer("/").append( //$NON-NLS-1$
                    INSISConstants.PLUGIN_ID).append("/").append( //$NON-NLS-1$
                                    INSISConstants.NSIS_PLATFORM_HELP_DOCS_PREFIX).toString();
    private static final MessageFormat NSIS_PLATFORM_HELP_FORMAT = new MessageFormat(NSIS_PLATFORM_PLUGIN_HELP_PREFIX+"{0}"); //$NON-NLS-1$
    private static final Pattern NSIS_PLATFORM_HELP_PATTERN = Pattern.compile(new StringBuffer(".*/").append( //$NON-NLS-1$
                    INSISConstants.NSIS_PLATFORM_HELP_DOCS_PREFIX).append("(.*)").toString()); //$NON-NLS-1$
    static final MessageFormat NSIS_CHM_HELP_FORMAT = new MessageFormat("mk:@MSITStore:{0}::/{1}"); //$NON-NLS-1$

    static final ParserDelegator HTML_PARSER = new ParserDelegator();

    private String mStartPage = null;
    private String mCachedStartPage = null;
    private String mCHMStartPage = null;
    private NSISHelpTOC mTOC = null;
    private NSISHelpIndex mIndex = null;
    private Map<String, String> mHelpURLs = null;
    private Map<String, String> mCHMHelpURLs = null;
    private Map<String, String> mKeywordHelp = null;
    private Map<Version, String> mNSISContribPaths;
    private File mNSISHtmlHelpFile = null;
    private Collection<INSISHelpURLListener> mListeners = new LinkedHashSet<INSISHelpURLListener>();
    private NSISHelpSearchManager mSearchManager = null;

    private ResourceBundle mBundle;

    private boolean mNSISHelpAvailable;
    private File mCacheFile;
    private String mCachedHelpLocation;
    private File mCachedHelpDocsLocation;
    private File mNoHelpFile;

    private File mStateLocation;

    static {
        File styleSheet = null;
        try {
            styleSheet = IOUtility.ensureLatest(EclipseNSISPlugin.getDefault().getBundle(),
                            new Path(EclipseNSISPlugin.getResourceString("hoverhelp.style.sheet")), //$NON-NLS-1$
                            new File(EclipseNSISPlugin.getPluginStateLocation(),EclipseNSISPlugin.getResourceString("hoverhelp.state.location"))); //$NON-NLS-1$
        }
        catch (IOException e1) {}
        final StringBuffer htmlPrefix = new StringBuffer("<html>\n<head>\n"); //$NON-NLS-1$
        if(styleSheet != null) {
            htmlPrefix.append("<link rel=\"stylesheet\" href=\"").append(IOUtility.getFileURLString(styleSheet)).append( //$NON-NLS-1$
            "\" charset=\"ISO-8859-1\" type=\"text/css\">\n"); //$NON-NLS-1$
        }
        else {
            htmlPrefix.append("<style type=\"text/css\">\n").append( //$NON-NLS-1$
            ".heading { font-weight: bold; font-size: 120%; }\n").append(  //$NON-NLS-1$
            ".link { font-weight: bold; }\n</style>\n"); //$NON-NLS-1$
        }
        if(NSISBrowserUtility.COLORS_CSS_FILE != null) {
            htmlPrefix.append("<link rel=\"stylesheet\" href=\"").append( //$NON-NLS-1$
                            IOUtility.getFileURLString(NSISBrowserUtility.COLORS_CSS_FILE)).append(
                            "\" charset=\"ISO-8859-1\" type=\"text/css\">\n"); //$NON-NLS-1$
        }
        htmlPrefix.append("</head>\n<body>\n"); //$NON-NLS-1$
        KEYWORD_HELP_HTML_PREFIX = htmlPrefix.toString();
    }

    public static NSISHelpURLProvider getInstance()
    {
        return cInstance;
    }

    public NSISHelpURLProvider()
    {
        mStateLocation = EclipseNSISPlugin.getPluginStateLocation();
        mCacheFile = new File(mStateLocation, getClass().getName() + ".HelpURLs.ser"); //$NON-NLS-1$
        mCachedHelpLocation = new File(mStateLocation, PLUGIN_HELP_LOCATION_PREFIX).getAbsolutePath();
        mCachedHelpDocsLocation = new File(mStateLocation, PLUGIN_HELP_DOCS_LOCATION_PREFIX);
        mNoHelpFile = new File(mStateLocation.getAbsolutePath(),NO_HELP_FILE);
    }

    public void addListener(INSISHelpURLListener listener)
    {
        mListeners.add(listener);
    }

    public void removeListener(INSISHelpURLListener listener)
    {
        mListeners.remove(listener);
    }

    public File getNoHelpFile()
    {
        return mNoHelpFile;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            try {
                monitor.beginTask("",100); //$NON-NLS-1$
                monitor.subTask(EclipseNSISPlugin.getResourceString("loading.helpurls.message")); //$NON-NLS-1$
                try {
                    mBundle = ResourceBundle.getBundle(NSISHelpURLProvider.class
                                    .getName());
                }
                catch (MissingResourceException x) {
                    mBundle = null;
                }
                cInstance = this;
                File indexLocation = new File(mCachedHelpLocation,INSISHelpSearchConstants.STANDARD_INDEX_LOCATION);
                if(indexLocation.isFile()) {
                    indexLocation.delete();
                }
                if(!indexLocation.exists()) {
                    indexLocation.mkdirs();
                }
                mSearchManager = new NSISHelpSearchManager(mCachedHelpDocsLocation);
                mNSISContribPaths = new LinkedHashMap<Version, String>();
                monitor.worked(10);
                loadNSISContribPaths();
                monitor.worked(10);
                loadHelpURLs();
                monitor.worked(75);
                NSISKeywords.getInstance().addKeywordsListener(this);
                monitor.worked(5);
            }
            finally {
                monitor.done();
            }
        }
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            mSearchManager.stopIndexing();
            mSearchManager.stopSearching();
            mSearchManager = null;
            mStartPage = null;
            mCachedStartPage = null;
            mCHMStartPage = null;
            mTOC = null;
            mIndex = null;
            mHelpURLs = null;
            mCHMHelpURLs = null;
            mKeywordHelp = null;
            mNSISHelpAvailable = false;
            mNSISContribPaths = null;
            mBundle = null;
            NSISKeywords.getInstance().removeKeywordsListener(this);
        }
    }

    public NSISHelpSearchManager getSearchManager()
    {
        return mSearchManager;
    }

    public boolean isNSISHelpAvailable()
    {
        checkHelpFile();
        return mNSISHelpAvailable;
    }

    private void loadNSISContribPaths()
    {
        Map<Version, String> temp = new HashMap<Version, String>();
        List<Version> list = new ArrayList<Version>();
        for(Enumeration<String> e = mBundle.getKeys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            if(key.startsWith("nsis.contrib.path")) { //$NON-NLS-1$
                String[] tokens = Common.tokenize(key,'#');
                Version v;
                if(tokens.length > 1) {
                    v = new Version(tokens[1]);
                }
                else {
                    v = INSISVersions.MINIMUM_VERSION;
                }
                temp.put(v, key);
                list.add(v);
            }
        }

        Collections.sort(list);
        for(Iterator<Version> iter=list.iterator(); iter.hasNext(); ) {
            Version v = iter.next();
            mNSISContribPaths.put(v, mBundle.getString(temp.get(v)));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadHelpURLs()
    {
        mTOC = null;
        mIndex = null;
        mHelpURLs = null;
        mCHMHelpURLs = null;
        mStartPage = null;
        mCachedStartPage = null;
        mCHMStartPage = null;
        mKeywordHelp = null;
        mNSISHtmlHelpFile = null;
        mNSISHelpAvailable = false;

        try {
            String home = "";
            NSISPreferences prefs = NSISPreferences.getInstance();
            if(prefs.getNSISHome() != null)
            {
                home = prefs.getNSISHome().getLocation().getAbsolutePath();
            }
            if (!Common.isEmpty(home)) {
                mNSISHtmlHelpFile = new File(home, NSIS_CHM_HELP_FILE);
                if (IOUtility.isValidFile(mNSISHtmlHelpFile)) {
                    try {
                        String startPage = mBundle.getString("help.start.page"); //$NON-NLS-1$
                        mStartPage = NSIS_PLATFORM_HELP_FORMAT.format(new Object[]{startPage});
                        mCachedStartPage = convertHelpURLToCachedURL(mStartPage);
                        mCHMStartPage = NSIS_CHM_HELP_FORMAT.format(new Object[]{mNSISHtmlHelpFile.getAbsolutePath(), startPage});
                    }
                    catch (MissingResourceException mre) {
                    }

                    long cacheTimeStamp = 0;
                    if (mCacheFile.exists()) {
                        cacheTimeStamp = mCacheFile.lastModified();
                    }

                    long htmlHelpTimeStamp = mNSISHtmlHelpFile.lastModified();
                    if (htmlHelpTimeStamp == cacheTimeStamp) {
                        Object obj = null;
                        try {
                            obj = IOUtility.readObject(mCacheFile);
                        }
                        catch (Exception e) {
                            obj = null;
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                        if (obj != null && Map.class.isAssignableFrom(obj.getClass())) {
                            Map<String,Object> map = (Map<String,Object>)obj;
                            Version version = (Version)map.get(VERSION);
                            if(version != null && HELP_URL_PROVIDER_VERSION.equals(version)) {
                                String stateLocation = (String)map.get(STATE_LOCATION);
                                if(!Common.isEmpty(stateLocation) && mStateLocation.getAbsolutePath().equalsIgnoreCase(stateLocation)) {
                                    mTOC = (NSISHelpTOC)map.get(TOC);
                                    mIndex = (NSISHelpIndex)map.get(INDEX);
                                    mHelpURLs = (Map<String, String>)map.get(HELP_URLS);
                                    mCHMHelpURLs = (Map<String, String>)map.get(CHM_HELP_URLS);
                                    mKeywordHelp = (Map<String, String>)map.get(KEYWORD_HELP);
                                    mNSISHelpAvailable = true;
                                    return;
                                }
                            }
                        }
                    }

                    mSearchManager.stop();
                    if (mCacheFile.exists()) {
                        mCacheFile.delete();
                    }

                    Map<String, List<String>> topicMap = new CaseInsensitiveMap<List<String>>();

                    String[] mappedHelpTopics = Common.loadArrayProperty(mBundle, "mapped.help.topics"); //$NON-NLS-1$
                    if (!Common.isEmptyArray(mappedHelpTopics)) {
                        for (int i = 0; i < mappedHelpTopics.length; i++) {
                            String[] keywords = Common.loadArrayProperty(mBundle, mappedHelpTopics[i]);
                            if (!Common.isEmptyArray(keywords)) {
                                ArrayList<String> list = new ArrayList<String>();
                                for (int j = 0; j < keywords.length; j++) {
                                    keywords[j] = NSISKeywords.getInstance().getKeyword(keywords[j]);
                                    if (NSISKeywords.getInstance().isValidKeyword(keywords[j])) {
                                        list.add(keywords[j]);
                                    }
                                }
                                topicMap.put(mappedHelpTopics[i], list);
                            }
                        }
                    }

                    if (IOUtility.isValidFile(mCachedHelpDocsLocation)) {
                        mCachedHelpDocsLocation.delete();
                    }
                    if (!IOUtility.isValidDirectory(mCachedHelpDocsLocation)) {
                        mCachedHelpDocsLocation.mkdirs();
                    }
                    IOUtility.deleteDirectoryContents(mCachedHelpDocsLocation);

                    String[] tocAndIndex = new String[2];
                    WinAPI.INSTANCE.extractHtmlHelp(mNSISHtmlHelpFile.getAbsolutePath(), mCachedHelpDocsLocation.getAbsolutePath(), tocAndIndex);

                    if (!Common.isEmpty(tocAndIndex[0])) {
                        File tocFile = new File(tocAndIndex[0]);
                        if (tocFile.exists()) {
                            try {
                                NSISHelpTOCParserCallback parserCallback = new NSISHelpTOCParserCallback(mCachedHelpDocsLocation, topicMap);
                                HTML_PARSER.parse(new FileReader(tocFile), parserCallback, false);

                                mTOC = parserCallback.getTOC();
                                Map<String,String> keywordHelpMap = parserCallback.getKeywordHelpMap();
                                mHelpURLs = new CaseInsensitiveMap<String>();
                                mCHMHelpURLs = new CaseInsensitiveMap<String>();
                                if (!Common.isEmptyMap(keywordHelpMap)) {
                                    StringBuffer buf = new StringBuffer();
                                    String[] args = new String[]{null};
                                    StringBuffer chmBuf = new StringBuffer();
                                    String[] chmArgs = new String[]{mNSISHtmlHelpFile.getAbsolutePath(), null};
                                    for (Iterator<String> iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                                        buf.setLength(0);
                                        chmBuf.setLength(0);

                                        String keyword = iter.next();
                                        String location = keywordHelpMap.get(keyword);

                                        args[0] = location;
                                        mHelpURLs.put(keyword, NSIS_PLATFORM_HELP_FORMAT.format(args, buf, null).toString());

                                        chmArgs[1] = location;
                                        mCHMHelpURLs.put(keyword, NSIS_CHM_HELP_FORMAT.format(chmArgs, chmBuf, null).toString());
                                    }

                                    Map<String,String> urlContentsMap = new CaseInsensitiveMap<String>();
                                    List<String> processedFiles = new ArrayList<String>();
                                    CaseInsensitiveSet keywords = new CaseInsensitiveSet(keywordHelpMap.keySet());
                                    CaseInsensitiveMap<String> urlKeywordMap = new CaseInsensitiveMap<String>();
                                    for (Iterator<Map.Entry<String, String>> iter = keywordHelpMap.entrySet().iterator(); iter.hasNext();) {
                                        Map.Entry<String, String> entry = iter.next();
                                        urlKeywordMap.put(entry.getValue(),entry.getKey());
                                    }
                                    for (Iterator<String> iter = urlKeywordMap.keySet().iterator(); iter.hasNext();) {
                                        String url = iter.next();
                                        int n = url.indexOf('#');
                                        if (n > 1) {
                                            String htmlFile = url.substring(0, n).toLowerCase();
                                            if (!processedFiles.contains(htmlFile)) {
                                                processedFiles.add(htmlFile);
                                                NSISHelpFileParserCallback callback = new NSISHelpFileParserCallback(mNSISHtmlHelpFile,htmlFile + "#", keywords, urlKeywordMap, urlContentsMap); //$NON-NLS-1$
                                                HTML_PARSER.parse(new FileReader(new File(tocFile.getParentFile(), htmlFile)), callback, false);
                                            }
                                        }
                                    }

                                    mKeywordHelp = new CaseInsensitiveMap<String>();
                                    for (Iterator<String> iter = keywordHelpMap.keySet().iterator(); iter.hasNext();) {
                                        String keyword = iter.next();
                                        String url = keywordHelpMap.get(keyword);
                                        String help = urlContentsMap.get(url);
                                        if (help != null) {
                                            mKeywordHelp.put(keyword, help);
                                        }
                                    }
                                }

                                if (!Common.isEmpty(tocAndIndex[1])) {
                                    File indexFile = new File(tocAndIndex[1]);
                                    if (indexFile.exists()) {
                                        try {
                                            NSISHelpIndexParserCallback parserCallback2 = new NSISHelpIndexParserCallback(mCachedHelpDocsLocation);
                                            HTML_PARSER.parse(new FileReader(indexFile), parserCallback2, false);

                                            mIndex = parserCallback2.getIndex();
                                        }
                                        catch (Exception e) {
                                            EclipseNSISPlugin.getDefault().log(e);
                                        }
                                        indexFile.delete();
                                    }
                                }

                                Map<String,Object> map = new HashMap<String,Object>();
                                map.put(STATE_LOCATION, mStateLocation.getAbsolutePath());
                                map.put(VERSION, HELP_URL_PROVIDER_VERSION);
                                map.put(TOC, mTOC);
                                map.put(INDEX, mIndex);
                                map.put(HELP_URLS, mHelpURLs);
                                map.put(CHM_HELP_URLS, mCHMHelpURLs);
                                map.put(KEYWORD_HELP, mKeywordHelp);
                                IOUtility.writeObject(mCacheFile, map);
                                mCacheFile.setLastModified(htmlHelpTimeStamp);
                                mSearchManager.indexHelp();
                            }
                            catch (Exception e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                            tocFile.delete();
                        }

                        //Fix the chmlink.js
                        File chmlinkJs = new File(mCachedHelpDocsLocation, CHMLINK_JS);
                        if (chmlinkJs.exists()) {
                            chmlinkJs.delete();
                        }
                        PrintWriter writer = null;
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(CHMLINK_JS)));
                            writer = new PrintWriter(new BufferedWriter(new FileWriter(chmlinkJs)));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.println(line);
                            }
                        }
                        catch (IOException io) {
                            EclipseNSISPlugin.getDefault().log(io);
                        }
                        finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                }
                                catch (IOException e) {
                                }
                            }
                            if (writer != null) {
                                writer.close();
                            }
                        }
                    }
                    else if (!Common.isEmpty(tocAndIndex[1])) {
                        File indexFile = new File(tocAndIndex[1]);
                        if (indexFile.exists()) {
                            indexFile.delete();
                        }
                    }

                    mNSISHelpAvailable = true;
                }
            }
        }
        finally {
            if(!mNSISHelpAvailable) {
                if(IOUtility.isValidFile(mCacheFile)) {
                    mCacheFile.delete();
                }
                if(IOUtility.isValidDirectory(mCachedHelpDocsLocation)) {
                    IOUtility.deleteDirectory(mCachedHelpDocsLocation);
                }
                if(!IOUtility.isValidFile(mNoHelpFile)) {
                    if(IOUtility.isValidDirectory(mNoHelpFile)) {
                        mNoHelpFile.delete();
                    }
                    File parent = mNoHelpFile.getParentFile();
                    if(!IOUtility.isValidDirectory(parent)) {
                        if(IOUtility.isValidFile(parent)) {
                            parent.delete();
                        }
                        parent.mkdirs();
                    }
                    String text = EclipseNSISPlugin.getFormattedString("missing.chm.format", //$NON-NLS-1$
                                    new String[] {EclipseNSISPlugin.getResourceString("help.style")}); //$NON-NLS-1$
                    IOUtility.writeContentToFile(mNoHelpFile, text.getBytes());
                }
                mStartPage = mCachedStartPage = mCHMStartPage = IOUtility.getFileURLString(mNoHelpFile);
            }

            INSISHelpURLListener[] listeners = mListeners.toArray(new INSISHelpURLListener[mListeners.size()]);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].helpURLsChanged();
            }
        }
    }

    public String getKeywordHelp(String keyword)
    {
        checkHelpFile();
        return mKeywordHelp==null?null:mKeywordHelp.get(keyword);
    }

    public String getHelpStartPage()
    {
        checkHelpFile();
        return mStartPage;
    }

    public String getCachedHelpStartPage()
    {
        checkHelpFile();
        return mCachedStartPage;
    }

    public NSISHelpTOC getCachedHelpTOC()
    {
        checkHelpFile();
        return mTOC;
    }

    public NSISHelpIndex getCachedHelpIndex()
    {
        checkHelpFile();
        return mIndex;
    }

    public String getCHMHelpStartPage()
    {
        checkHelpFile();
        return mCHMStartPage;
    }

    public void keywordsChanged()
    {
        loadHelpURLs();
    }

    private void checkHelpFile()
    {
        boolean helpFileExists = IOUtility.isValidFile(mNSISHtmlHelpFile);
        if(mNSISHelpAvailable ^ helpFileExists) {
            loadHelpURLs();
        }
    }

    public String convertHelpURLToCHMHelpURL(String helpURL)
    {
        String chmHelpURL = null;
        if(!Common.isEmpty(helpURL)) {
            if(IOUtility.isValidFile(mNSISHtmlHelpFile)) {
                Matcher matcher = NSIS_PLATFORM_HELP_PATTERN.matcher(helpURL);
                if(matcher.matches()) {
                    if(matcher.groupCount() == 1) {
                        String link=matcher.group(1);
                        chmHelpURL = NSIS_CHM_HELP_FORMAT.format(new String[]{mNSISHtmlHelpFile.getAbsolutePath(),link});
                    }
                }
            }
        }
        return chmHelpURL;
    }

    public String getHelpURL(String keyWord, boolean useEclipseHelp)
    {
        if(!Common.isEmpty(keyWord)) {
            checkHelpFile();
            if(useEclipseHelp) {
                if(!mNSISHelpAvailable) {
                    return mStartPage;
                }
                if(mHelpURLs != null) {
                    return mHelpURLs.get(keyWord);
                }
            }
            else {
                if(!mNSISHelpAvailable) {
                    return mCHMStartPage;
                }
                if(mCHMHelpURLs != null) {
                    return mCHMHelpURLs.get(keyWord);
                }
            }
        }
        return null;
    }

    private String convertHelpURLToCachedURL(String url)
    {
        if(NSIS_PLATFORM_PLUGIN_HELP_PREFIX.regionMatches(true,0,url,0,NSIS_PLATFORM_PLUGIN_HELP_PREFIX.length())) {
            File f = new File(mCachedHelpDocsLocation,url.substring(NSIS_PLATFORM_PLUGIN_HELP_PREFIX.length()));
            return IOUtility.getFileURLString(f);
        }
        return url;
    }

    public File translateCachedFile(File file)
    {
        if(file != null && !file.exists()) {
            String path = file.getAbsolutePath();
            if(mCachedHelpLocation.regionMatches(true,0,path,0,mCachedHelpLocation.length())) {
                File file2 = new File(NSISPreferences.getInstance().getNSISHome().getLocation(),path.substring(mCachedHelpLocation.length()));
                if(file2.exists()) {
                    return file2;
                }
            }
        }
        return file;
    }

    public void showHelp()
    {
        checkHelpFile();
        String url = getHelpStartPage();
        if(!Common.isEmpty(url)) {
            if(NSISPreferences.getInstance().isUseEclipseHelp()) {
                showPlatformHelp(url);
                return;
            }
            else {
                if(NSISHTMLHelp.showHelp(convertHelpURLToCachedURL(url))) {
                    return;
                }
            }
        }
        url = getCHMHelpStartPage();
        openCHMHelpURL(url);
    }

    public void showPlatformHelp(String url)
    {
        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
    }

    public void showHelp(String file)
    {
        checkHelpFile();
        if(NSISPreferences.getInstance().isUseEclipseHelp()) {
            showPlatformHelp(NSIS_PLATFORM_HELP_FORMAT.format(new String[] {file}));
        }
        else if(!NSISHTMLHelp.showHelp(IOUtility.getFileURLString(new File(mCachedHelpDocsLocation,file)))) {
            openCHMHelpURL(NSIS_CHM_HELP_FORMAT.format(new String[] {mNSISHtmlHelpFile.getAbsolutePath(),file}));
        }
    }

    public boolean showHelpURL(String keyword)
    {
        checkHelpFile();
        String url = getHelpURL(keyword, true);
        if(!Common.isEmpty(url)) {
            if(NSISPreferences.getInstance().isUseEclipseHelp()) {
                showPlatformHelp(url);
                return true;
            }
            else if(NSISHTMLHelp.showHelp(convertHelpURLToCachedURL(url))) {
                return true;
            }
        }
        url = getHelpURL(keyword, false);
        if(!Common.isEmpty(url)) {
            openCHMHelpURL(url);
            return true;
        }

        return false;
    }

    /**
     * @param url
     */
    public void openCHMHelpURL(String url)
    {
        checkHelpFile();
        if(mNSISHelpAvailable) {
            WinAPI.INSTANCE.htmlHelp(WinAPI.INSTANCE.getDesktopWindow(),url,WinAPI.HH_DISPLAY_TOPIC,0);
        }
        else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    Common.openError(Display.getCurrent().getActiveShell(),
                                    EclipseNSISPlugin.getResourceString("missing.help.file.message"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                }
            });
        }
    }

    public String getNSISContribPath()
    {
        Version nsisVersion = NSISPreferences.getInstance().getNSISVersion();
        String nsisContribPath = null;
        for(Iterator<Version> iter=mNSISContribPaths.keySet().iterator(); iter.hasNext(); ) {
            Version v = iter.next();
            if(nsisVersion.compareTo(v) >= 0) {
                nsisContribPath = mNSISContribPaths.get(v);
            }
            else {
                break;
            }
        }
        return nsisContribPath;
    }
}
