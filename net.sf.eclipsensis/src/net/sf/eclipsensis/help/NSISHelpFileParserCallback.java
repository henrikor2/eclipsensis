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

import java.io.File;
import java.util.*;
import java.util.regex.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.*;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import net.sf.eclipsensis.util.IOUtility;

public class NSISHelpFileParserCallback extends ParserCallback
{
    private static final String ATTR_ONCLICK = "onclick"; //$NON-NLS-1$
    private static final String JAVASCRIPT_URI_SCHEME = "javascript:"; //$NON-NLS-1$
    private static final String NAV_MENU_MARKER = ">Previous</a>"; //$NON-NLS-1$

    private static final Set<Tag> HEADINGS = new HashSet<Tag>();
    private static final Pattern cOnClickPattern = Pattern.compile("parser\\(['\"]\\.\\./([\\.\\\\/a-z0-9_\\-\\s]+)['\"]\\)",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private Set<String> mKeywords;
    private Map<String, String> mURLKeywordsMap;
    private Map<String, String> mURLContentsMap;
    private String mPrefix;
    private StringBuffer mBuffer = new StringBuffer(""); //$NON-NLS-1$
    private boolean mCollecting = false;
    private boolean mHeading = false;
    private String mHref = null;
    private String mAnchor;
    private File mHelpFile;

    static {
        HEADINGS.add(Tag.H1);
        HEADINGS.add(Tag.H2);
        HEADINGS.add(Tag.H3);
        HEADINGS.add(Tag.H4);
        HEADINGS.add(Tag.H5);
        HEADINGS.add(Tag.H6);
    }

    public NSISHelpFileParserCallback(File helpFile, String prefix, Set<String> keywords, Map<String,String> urlKeywordsMap, Map<String, String> urlContentsMap)
    {
        super();
        mHelpFile = helpFile;
        mPrefix = prefix;
        mKeywords = keywords;
        mURLKeywordsMap = urlKeywordsMap;
        mURLContentsMap = urlContentsMap;
    }

    @Override
    public void handleEndTag(Tag t, int pos)
    {
        if(mCollecting) {
            if(t.equals(Tag.A) && mBuffer.length() == NSISHelpURLProvider.KEYWORD_HELP_HTML_PREFIX.length()) {
                return;
            }
            if(HEADINGS.contains(t)) {
                mBuffer.append("</p>"); //$NON-NLS-1$
                mHeading = false;
            }
            else if(t.equals(Tag.A)) {
                if(mHref != null) {
                    mBuffer.append("</a>"); //$NON-NLS-1$
                    if(mBuffer.length()>NAV_MENU_MARKER.length() && mBuffer.substring(mBuffer.length()-NAV_MENU_MARKER.length()).equals(NAV_MENU_MARKER)) {
                        int n = mBuffer.lastIndexOf("<p>"); //$NON-NLS-1$
                        if(n >= 0) {
                            mBuffer.setLength(n);
                        }
                        saveBuffer();
                    }
                }
                else {
                    mBuffer.append("</span>"); //$NON-NLS-1$
                }
                mHref = null;
            }
            else {
                mBuffer.append("</").append(t).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    private void saveBuffer()
    {
        mBuffer.append(NSISHelpURLProvider.KEYWORD_HELP_HTML_SUFFIX);
        mURLContentsMap.put(mAnchor,mBuffer.toString());
        mBuffer.setLength(0);
        mAnchor = null;
        mCollecting = false;
    }

    @Override
    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(mCollecting) {
            if(HEADINGS.contains(t)) {
                mBuffer.append("<p class=\"heading\">"); //$NON-NLS-1$
                mHeading = true;
            }
            else if(t.equals(Tag.A)) {
                mHref = null;
                if(a.isDefined(Attribute.HREF)) {
                    String href = a.getAttribute(Attribute.HREF).toString();
                    if(href.equals("#")) { //$NON-NLS-1$
                        if(a.isDefined(ATTR_ONCLICK)) {
                            String onClick = a.getAttribute(ATTR_ONCLICK).toString();
                            Matcher m = cOnClickPattern.matcher(onClick);
                            if(m.matches()) {
                                String file = m.group(1);
                                File f = new File(mHelpFile.getParent(),file);
                                if(f.exists()) {
                                    mHref = IOUtility.getFileURLString(f);
                                }
                            }
                        }
                    }
                    else {
                        mHref = href;
                    }
                }
                if(mHref == null) {
                    mBuffer.append("<span class=\"link\">"); //$NON-NLS-1$
                }
            }
            else {
                mBuffer.append("<").append(t); //$NON-NLS-1$
                if(a != null && a.getAttributeCount() > 0) {
                    for(Enumeration<?> e = a.getAttributeNames(); e.hasMoreElements(); ) {
                        Object name = e.nextElement();
                        Object value = a.getAttribute(name);
                        mBuffer.append(" ").append(name).append("=\"").append(value).append("\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                mBuffer.append(">"); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(t.equals(Tag.A)) {
            if(a != null && a.isDefined(Attribute.NAME)) {
                if(mCollecting) {
                    saveBuffer();
                }
                mAnchor = mPrefix+a.getAttribute(Attribute.NAME);
                if(mURLKeywordsMap.containsKey(mAnchor)) {
                    mHref = null;
                    mCollecting = true;
                    mBuffer.append(NSISHelpURLProvider.KEYWORD_HELP_HTML_PREFIX);
                    return;
                }
                else {
                    mAnchor = null;
                }
            }
        }
        handleSimpleTag(t,a,pos);
    }

    @Override
    public void handleText(char[] data, int pos)
    {
        if(mCollecting) {
            boolean isNewLine = false; //For some reason CR is being converted to NL by the parser.
                                       //So one needs to be dropped.
            boolean found = false;
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < data.length; i++) {
                if(isNewLine) {
                    isNewLine = false;
                    if(data[i] == '\n') {
                        continue;
                    }
                }
                isNewLine = (data[i] == '\n');
                if(mHeading && !found) {
                    found = (data[i]==' ');
                    continue;
                }
                buf.append(data[i]);
            }
            String text = buf.toString();
            if(mHref != null) {
                if(mKeywords.contains(text)) {
                    mHref=NSISHelpURLProvider.KEYWORD_URI_SCHEME+text;
                }
                else if(mURLKeywordsMap.containsKey(mHref)) {
                    mHref=NSISHelpURLProvider.KEYWORD_URI_SCHEME+mURLKeywordsMap.get(mHref);
                }
                else {
                    if(mHref.regionMatches(true,0, JAVASCRIPT_URI_SCHEME, 0, JAVASCRIPT_URI_SCHEME.length())) {
                        mHref = null;
                        mBuffer.append("<span class=\"link\">"); //$NON-NLS-1$
                    }
                    else if(mHref.indexOf(":") <= 0 && "./\\".indexOf(mHref.charAt(0)) < 0) { //$NON-NLS-1$ //$NON-NLS-2$
                        //This is a local URL
                        if(mHref.charAt(0) == '#') {
                            //It is on this page
                            mHref = mPrefix+mHref.substring(1);
                        }
                        mHref = NSISHelpURLProvider.HELP_URI_SCHEME+mHref;
                    }
                }

                if(mHref != null) {
                    mBuffer.append("<a href=\"").append(mHref).append("\">"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            mBuffer.append(text);
        }
    }
}
