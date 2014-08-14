/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

public class NSISBrowserInformation implements INSISKeywordInformation
{
    private String mKeyword;
    private String mContent;

    public NSISBrowserInformation(String keyword, String content)
    {
        mKeyword = keyword;
        mContent = content;
    }

    public String getContent()
    {
        return mContent;
    }

    public String getKeyword()
    {
        return mKeyword;
    }
}
