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
import java.net.*;
import java.util.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.*;

import net.sf.eclipsensis.util.*;

public class NSISHelpTOCParserCallback extends HTMLEditorKit.ParserCallback
{
    private static final String ATTR_VALUE_TEXT_SITEMAP="text/sitemap"; //$NON-NLS-1$
    private static final String ATTR_VALUE_LOCAL="Local"; //$NON-NLS-1$
    private static final String ATTR_VALUE_NAME="Name"; //$NON-NLS-1$

    private File mLocation;
    private Map<String,List<String>> mTopicMap = null;
    private Map<String, String> mKeywordHelpMap = new CaseInsensitiveMap<String>();
    private boolean mCanProcess = false;
    private String mLocal = null;
    private String mName = null;
    private NSISHelpTOC mTOC = new NSISHelpTOC();
    private NSISHelpTOC.NSISHelpTOCNode mParentNode = null;
    private NSISHelpTOC.NSISHelpTOCNode mCurrentNode = null;

    public NSISHelpTOCParserCallback(File location, Map<String,List<String>> topicMap)
    {
        mLocation = location;
        mTopicMap = topicMap;
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleEndTag(javax.swing.text.html.HTML.Tag, int)
     */
    @Override
    public void handleEndTag(Tag t, int pos)
    {
        if(t.equals(Tag.OBJECT) && mCanProcess) {
            if(mLocal != null && mName != null) {
                if(mTopicMap.containsKey(mName)) {
                    List<String> keywords = mTopicMap.get(mName);
                    for (Iterator<String> iter = keywords.iterator(); iter.hasNext();) {
                        String keyword = iter.next();
                        if(NSISKeywords.getInstance().isValidKeyword(keyword)) {
                            mKeywordHelpMap.put(keyword, mLocal);
                        }
                    }
                }
                else if(NSISKeywords.getInstance().isValidKeyword(mName)) {
                    mKeywordHelpMap.put(mName,mLocal);
                }

                String url;
                try {
                    url = new URL(mLocal).toString();
                }
                catch(MalformedURLException mue) {
                    String suffix = null;
                    int n = mLocal.lastIndexOf('#');
                    if(n > 0) {
                        suffix = mLocal.substring(n);
                        mLocal = mLocal.substring(0,n);
                    }
                    File f = NSISHelpURLProvider.getInstance().translateCachedFile(new File(mLocation, mLocal));
                    url = IOUtility.getFileURLString(f);
                    if(suffix != null) {
                        url += suffix;
                    }
                }
                mCurrentNode = mTOC.createNode(mName, url);
                if(mParentNode != null) {
                    mParentNode.addNode(mCurrentNode);
                }
                else {
                    mTOC.addNode(mCurrentNode);
                }
            }
            mCanProcess = false;
            mLocal = null;
            mName = null;
        }
        else if(t.equals(Tag.UL)) {
            mCurrentNode = mParentNode;
            if(mParentNode != null) {
                mParentNode = mCurrentNode.getParent();
            }
        }
    }

    public NSISHelpTOC getTOC()
    {
        return mTOC;
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleSimpleTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
     */
    @Override
    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(mCanProcess && t.equals(Tag.PARAM)) {
            if(a.isDefined(Attribute.NAME) && a.isDefined(Attribute.VALUE)) {
                String name = (String)a.getAttribute(Attribute.NAME);
                String value = (String)a.getAttribute(Attribute.VALUE);
                if(ATTR_VALUE_LOCAL.equalsIgnoreCase(name)) {
                    mLocal = value;
                }
                else if(ATTR_VALUE_NAME.equalsIgnoreCase(name)) {
                    mName = value;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleStartTag(javax.swing.text.html.HTML.Tag, javax.swing.text.MutableAttributeSet, int)
     */
    @Override
    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(t.equals(Tag.OBJECT)) {
            if(a.isDefined(Attribute.TYPE)) {
                if(ATTR_VALUE_TEXT_SITEMAP.equalsIgnoreCase((String)a.getAttribute(Attribute.TYPE))) {
                    mCanProcess = true;
                }
            }
        }
        else if(t.equals(Tag.UL)) {
            mParentNode = mCurrentNode;
            mCurrentNode = null;
        }
    }

    /**
     * @return Returns the keywordMap.
     */
    public Map<String, String> getKeywordHelpMap()
    {
        return mKeywordHelpMap;
    }
}
