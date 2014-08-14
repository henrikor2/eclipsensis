/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.search;

import java.io.*;
import java.net.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.IOUtility;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.FileLocator;

public class NSISHelpSearchManager implements INSISHelpSearchConstants
{
    private NSISHelpIndexer mStandardIndexer = null;
    private NSISHelpIndexer mStemmedIndexer = null;
    private String mSearchSyntaxURL;

    public NSISHelpSearchManager(File documentRoot)
    {
        File helpLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),INSISConstants.PLUGIN_HELP_LOCATION_PREFIX);
		mStandardIndexer = new NSISHelpIndexer(new File(helpLocation,
				STANDARD_INDEX_LOCATION), documentRoot, new StandardAnalyzer(
				Version.LUCENE_35));
		mStemmedIndexer = new NSISHelpIndexer(new File(helpLocation,
				STEMMED_INDEX_LOCATION), documentRoot, new StandardAnalyzer(
				Version.LUCENE_35));
        try {
            URL url = FileLocator.toFileURL(getClass().getResource("search_syntax.htm")); //$NON-NLS-1$
            try {
                File f = new File(new URI(url.toString()));
                if(f.exists()) {
                    mSearchSyntaxURL = IOUtility.getFileURLString(f);
                }
                else {
                    mSearchSyntaxURL = url.toString();
                }
            }
            catch(Exception ex) {
                mSearchSyntaxURL = url.toString();
            }
        }
        catch (IOException e) {
            mSearchSyntaxURL = null;
        }
    }

    public String getSearchSyntaxURL()
    {
        return mSearchSyntaxURL;
    }

    public void stop()
    {
        stopIndexing();
        stopSearching();
    }

    public void search(INSISHelpSearchRequester requester)
    {
        search(INSISHelpSearchConstants.INDEX_FIELD_CONTENTS, requester);
    }

    public void search(String field, INSISHelpSearchRequester requester)
    {
        NSISHelpSearcher searcher = (requester.useStemming()?mStemmedIndexer.getSearcher():mStandardIndexer.getSearcher());
        searcher.search(field, requester);
    }

    public void stopSearching()
    {
        mStandardIndexer.getSearcher().stopSearching();
        mStemmedIndexer.getSearcher().stopSearching();
    }

    public void stopIndexing()
    {
        mStandardIndexer.stopIndexing();
        mStemmedIndexer.stopIndexing();
    }

    public void indexHelp()
    {
        mStandardIndexer.indexHelp();
        mStemmedIndexer.indexHelp();
    }
}
