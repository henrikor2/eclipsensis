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
import java.net.*;
import java.util.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;

public class NSISHelpIndex implements Serializable
{
    private static final long serialVersionUID = 7257547506234414246L;

    private static final Comparator<int[]> cIndexIndexComparator = new Comparator<int[]>() {
        public int compare(int[] a, int[] b)
        {
            return a[0]-b[0];
        }
    };
    private static final Comparator<Object> cIndexEntryComparator = new Comparator<Object>() {
        public int compare(Object o1, Object o2)
        {
            return getString(o1).compareTo(getString(o2));
        }

        private String getString(Object o)
        {
            if(o instanceof String) {
                return (String)o;
            }
            else if(o instanceof NSISHelpIndexEntry) {
                return ((NSISHelpIndexEntry)o).getSortKey();
            }
            return null;
        }
    };

    private List<NSISHelpIndexEntry> mEntries = null;

    private transient Map<String, NSISHelpIndexEntry> mEntryMap = new CaseInsensitiveMap<NSISHelpIndexEntry>();
    private transient Map<String, String> mTitlemap = new CaseInsensitiveMap<String>();
    private int[][] mIndexIndex = null;

    private String getTitle(String url)
    {
        String url2 = url;
        int n = url2.lastIndexOf('#');
        if(n > 0) {
            url2 = url2.substring(0,n);
        }
        String title = ""; //$NON-NLS-1$
        if(mTitlemap.containsKey(url2)) {
            title = mTitlemap.get(url2);
        }
        else {
            Reader r = null;
            HTMLTitleParserCallback callback = null;
            try {
                URLConnection conn = new URL(url2).openConnection();
                r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                callback = new HTMLTitleParserCallback(r);
                NSISHelpURLProvider.HTML_PARSER.parse(r,callback,true);
            }
            catch (IOException e) {
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            finally {
                if(callback != null) {
                    title = callback.getTitle();
                }
                IOUtility.closeIO(r);
            }
            mTitlemap.put(url2,title);
        }
        return title;
    }

    NSISHelpIndex()
    {
    }

    void addEntry(String name, String url)
    {
        NSISHelpIndexEntry entry = mEntryMap.get(name);
        if(entry == null) {
            mEntryMap.put(name, new NSISHelpIndexEntry(name, url));
        }
        else {
            entry.addURL(url);
        }
    }

    public NSISHelpIndexEntry findEntry(String name)
    {
        String name2 = name;
        if(name2 != null && name2.length() > 0 && mIndexIndex != null) {
            name2 = name2.toLowerCase();
            int[] key = {name2.charAt(0),0};
            int n = Arrays.binarySearch(mIndexIndex,key,cIndexIndexComparator);
            if(n < 0) {
                n = -n-1;
                if(n >= mIndexIndex.length) {
                    return null;
                }
                if(n == 0) {
                    return mEntries.get(0);
                }
                else {
                    return mEntries.get(mIndexIndex[n][1]-1);
                }
            }
            int m = Collections.binarySearch(mEntries.subList(mIndexIndex[n][1],
                                                          (n < (mIndexIndex.length-1)?mIndexIndex[n+1][1]:mEntries.size())),
                                         name2, cIndexEntryComparator);
            if(m < 0) {
                m = mIndexIndex[n][1]-m-1;
                NSISHelpIndexEntry entry = mEntries.get(m);
                if(!entry.getSortKey().startsWith(name2)) {
                    if(m > mIndexIndex[n][1]) {
                        m--;
                    }
                    entry = mEntries.get(m);
                }
                return entry;
            }
            return mEntries.get(mIndexIndex[n][1]+m);
        }
        return null;
    }

    public List<NSISHelpIndexEntry> getEntries()
    {
        if(mEntries == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(mEntries);
    }

    void doneLoading()
    {
        if(mEntries != null) {
            mEntries.clear();
        }
        else {
            mEntries = new ArrayList<NSISHelpIndexEntry>();
        }
        mIndexIndex = null;
        mEntries.addAll(mEntryMap.values());
        mEntryMap.clear();
        Collections.sort(mEntries);
        int i = 0;
        List<int[]> indexIndex = new ArrayList<int[]>();
        int lastChar = 0;
        for (Iterator<NSISHelpIndexEntry> iter = mEntries.iterator(); iter.hasNext();) {
            NSISHelpIndexEntry entry = iter.next();
            entry.sort();
            char c = Character.toLowerCase(entry.getName().charAt(0));
            if(c > lastChar) {
                lastChar = c;
                indexIndex.add(new int[] {c,i});
            }
            i++;
        }
        mIndexIndex = indexIndex.toArray(new int[indexIndex.size()][]);
    }

    public class NSISHelpIndexEntry implements Serializable, Comparable<NSISHelpIndexEntry>
    {
        private static final long serialVersionUID = 460774407714231630L;

        private String mName;
        private List<NSISHelpIndexURL> mURLs = new ArrayList<NSISHelpIndexURL>();
        private String mSortKey;

        private NSISHelpIndexEntry(String name, String url)
        {
            mName = name;
            mURLs.add(new NSISHelpIndexURL(url));
            mSortKey = name.toLowerCase();
        }

        private void addURL(String url)
        {
            if(mURLs.size() == 1) {
                NSISHelpIndexURL url2 = mURLs.get(0);
                url2.setLocation(getTitle(url2.getURL()));
            }
            mURLs.add(new NSISHelpIndexURL(url,getTitle(url)));
        }

        public String getName()
        {
            return mName;
        }

        private String getSortKey()
        {
            return mSortKey;
        }

        public List<NSISHelpIndexURL> getURLs()
        {
            return Collections.unmodifiableList(mURLs);
        }

        public int compareTo(NSISHelpIndexEntry o)
        {
            return mSortKey.compareTo((o).mSortKey);
        }

        private void sort()
        {
            Collections.sort(mURLs);
        }

        @Override
        public int hashCode()
        {
            return mSortKey.hashCode();
        }

        @Override
        public String toString()
        {
            return mName;
        }

        @Override
        public boolean equals(Object other)
        {
            if(other != this) {
                if(other instanceof NSISHelpIndexEntry) {
                    return Common.stringsAreEqual(mSortKey, ((NSISHelpIndexEntry)other).mSortKey);
                }
                return false;
            }
            return true;
        }
    }

    public class NSISHelpIndexURL implements Serializable, Comparable<NSISHelpIndexURL>
    {
        private static final long serialVersionUID = -3957228848764619499L;
        private String mURL;
        private String mLocation;

        private NSISHelpIndexURL(String url)
        {
            mURL = url;
        }

        private NSISHelpIndexURL(String url, String location)
        {
            mURL = url;
            mLocation = location;
        }

        public String getLocation()
        {
            return mLocation;
        }

        private void setLocation(String location)
        {
            mLocation = location;
        }

        public String getURL()
        {
            return mURL;
        }

        public int compareTo(NSISHelpIndexURL o)
        {
            NSISHelpIndexURL url = o;
            if(!Common.stringsAreEqual(mLocation,url.mLocation)) {
                return (mLocation != null && url.mLocation != null?mLocation.compareTo(url.mLocation):(mLocation != null?1:-1));
            }
            return 0;
        }
    }

    private class HTMLTitleParserCallback extends ParserCallback
    {
        private StringBuffer mTitle = new StringBuffer(""); //$NON-NLS-1$
        private boolean mInTitle = false;
        private Reader mReader;

        private HTMLTitleParserCallback(Reader reader)
        {
            mReader = reader;
        }

        public String getTitle()
        {
            return mTitle.toString();
        }

        @Override
        public void handleEndTag(Tag t, int pos)
        {
            if(Tag.TITLE.equals(t) && mInTitle) {
                mInTitle = false;
                try {
                    mReader.close();
                }
                catch (IOException e) {
                }
            }
        }

        @Override
        public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
        {
            if(Tag.TITLE.equals(t) && !mInTitle) {
                mInTitle = true;
            }
        }

        @Override
        public void handleText(char[] data, int pos)
        {
            if(mInTitle) {
                if(mTitle.length() > 0) {
                    mTitle.append(INSISConstants.LINE_SEPARATOR);
                }
                mTitle.append(data);
            }
        }
    }
}
