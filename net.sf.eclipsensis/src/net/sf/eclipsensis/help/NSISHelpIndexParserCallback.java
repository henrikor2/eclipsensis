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

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.*;

import net.sf.eclipsensis.util.IOUtility;

public class NSISHelpIndexParserCallback extends HTMLEditorKit.ParserCallback
{
    private static final String ATTR_VALUE_TEXT_SITEMAP="text/sitemap"; //$NON-NLS-1$
    private static final String ATTR_VALUE_LOCAL="Local"; //$NON-NLS-1$
    private static final String ATTR_VALUE_NAME="Name"; //$NON-NLS-1$

    private File mLocation;
    private boolean mCanProcess = false;
    private String mLocal = null;
    private String mName = null;
    private NSISHelpIndex mIndex;

    public NSISHelpIndexParserCallback(File location)
    {
        mLocation = location;
        mIndex = new NSISHelpIndex();
    }

    /* (non-Javadoc)
     * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleEndTag(javax.swing.text.html.HTML.Tag, int)
     */
    @Override
    public void handleEndTag(Tag t, int pos)
    {
        if(t.equals(Tag.OBJECT) && mCanProcess) {
            if(mLocal != null && mName != null) {
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
                mIndex.addEntry(mName, url);
            }
            mCanProcess = false;
            mLocal = null;
            mName = null;
        }
        else if(t.equals(Tag.HTML)) {
            mIndex.doneLoading();
        }
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
    }

    public NSISHelpIndex getIndex()
    {
        return mIndex;
    }
}
