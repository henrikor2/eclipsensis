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

public class NSISHelpSearchResult
{
    private String mTitle;
    private String mURL;
    private int mRank;

    NSISHelpSearchResult(String title, String url, int rank)
    {
        super();
        mTitle = title;
        mURL = url;
        mRank = rank;
    }

    public int getRank()
    {
        return mRank;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public String getURL()
    {
        return mURL;
    }

}
