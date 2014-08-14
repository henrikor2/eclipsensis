/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import java.io.*;
import java.util.*;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.*;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;

class DownloadURLsParserCallback extends ParserCallback
{
    private static File cCacheFolder = new File(EclipseNSISUpdatePlugin.getPluginStateLocation(),"resources"); //$NON-NLS-1$
    private static final String PROPERTIES_FILE_NAME = "siteimages.properties"; //$NON-NLS-1$
    private static final IPath cLocalPropertiesPath = new Path("/resources/"+PROPERTIES_FILE_NAME); //$NON-NLS-1$

    private static final String FORM_ACTION = "/settings/set_mirror"; //$NON-NLS-1$
    private static final String MIRROR = "mirror"; //$NON-NLS-1$
    private static final String AUTO_SELECT = "autoselect"; //$NON-NLS-1$
    private static final String TAG_LABEL = "label"; //$NON-NLS-1$
    private static final String INPUT_RADIO = "radio"; //$NON-NLS-1$

    private boolean mDone = false;
    private boolean mInForm = false;
    private boolean mInLI = false;
    private boolean mInLabel = false;

    private String[] mCurrentSite  = null;
    private List<String[]> mSites = new ArrayList<String[]>();
    private Properties mImageURLs = new Properties();

    private void loadImageURLs()
    {
        InputStream is = null;
        try {
            File file = IOUtility.ensureLatest(EclipseNSISUpdatePlugin.getDefault().getBundle(),cLocalPropertiesPath,cCacheFolder);
            NetworkUtil.downloadLatest(NSISUpdateURLs.getSiteImagesUpdateURL(),file);

            if(file != null && file.exists()) {
                is = new FileInputStream(file);
                mImageURLs.load(is);
            }
        }
        catch (IOException e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(is);
        }
    }

    public DownloadURLsParserCallback()
    {
        loadImageURLs();
    }

    @Override
    public void handleEndTag(Tag t, int pos)
    {
        if(!mDone) {
            if(t.equals(Tag.FORM)) {
                if(mInForm) {
                    mInForm = false;
                    mDone = true;
                }
            }
            else if(t.equals(Tag.LI)) {
                if(mInForm && mInLI) {
                    mInLI = false;
                    if(mCurrentSite != null) {
                        if(mCurrentSite[3] != null) {
                            mSites.add(mCurrentSite);
                        }
                        mCurrentSite = null;
                    }
                }
            }
            else if(t.toString().equals(TAG_LABEL)) {
                if(mInForm && mInLI) {
                    mInLabel = false;
                }
            }
        }
    }

    @Override
    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(!mDone) {
            if(t.equals(Tag.LI)) {
                if(mInForm) {
                    if(mInLI) {
                        handleEndTag(t, pos);
                    }
                    else {
                        handleStartTag(t, a, pos);
                    }
                }
            }
            else if(t.toString().equals(TAG_LABEL)) {
                if(mInForm && mInLI) {
                    if(mInLabel) {
                        handleEndTag(t, pos);
                    }
                    else {
                        handleStartTag(t, a, pos);
                    }
                }
            }
            else if(t.equals(Tag.INPUT)) {
                if(mInForm && mInLI && mCurrentSite != null) {
                    if(a.isDefined(Attribute.TYPE)) {
                        if(INPUT_RADIO.equalsIgnoreCase((String)a.getAttribute(Attribute.TYPE))) {
                            if(a.isDefined(Attribute.NAME)) {
                                if(MIRROR.equalsIgnoreCase((String)a.getAttribute(Attribute.NAME))) {
                                    if(a.isDefined(Attribute.VALUE)) {
                                        String[] names = Common.tokenize((String)a.getAttribute(Attribute.VALUE),',');
                                        for (int i = 0; i < names.length; i++)
                                        {
                                            mCurrentSite[0] = mImageURLs.getProperty(names[i]);
                                            if(mCurrentSite[0] != null)
                                            {
                                                mCurrentSite[3] = names[i];
                                                break;
                                            }
                                        }
                                        if (mCurrentSite[3] == null)
                                        {
                                            mCurrentSite[3] = names[0];
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
    {
        if(!mDone) {
            if(t.equals(Tag.FORM)) {
                if(!mInForm) {
                    if(a.isDefined(Attribute.ACTION)) {
                        String action = (String)a.getAttribute(Attribute.ACTION);
                        mInForm = FORM_ACTION.equalsIgnoreCase(action);
                    }
                }
            }
            else if(t.equals(Tag.LI)) {
                if(mInForm && !mInLI) {
                    String id = (String)a.getAttribute(Attribute.ID);
                    mInLI = !AUTO_SELECT.equalsIgnoreCase(id);
                    if(mInLI) {
                        mCurrentSite = new String[4];
                    }
                }
            }
            else if(t.toString().equals(TAG_LABEL)) {
                if(mInForm && mInLI) {
                    mInLabel = true;
                }
            }
        }
    }

    @Override
    public void handleText(char[] data, int pos)
    {
        if(!mDone) {
            if(mInForm) {
                String string = new String(data).trim();
                if(mInLabel) {
                    if(mCurrentSite != null) {
                        mCurrentSite[1] = string;
                    }
                }
                else if(mInLI) {
                    if(string.charAt(0) == '(' && string.charAt(string.length()-1) == ')')
                    {
                        string = string.substring(1,string.length()-1);
                    }
                    mCurrentSite[2] = string;
                }
            }
        }
    }

    public List<String[]> getSites()
    {
        return mSites;
    }

}