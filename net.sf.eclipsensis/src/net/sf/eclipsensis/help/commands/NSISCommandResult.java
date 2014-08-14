/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

public class NSISCommandResult
{
    private String mContent;
    private int mCursorPos;

    public NSISCommandResult(String content, int cursorPos)
    {
        super();
        mContent = content;
        mCursorPos = cursorPos;
    }

    public String getContent()
    {
        return mContent;
    }

    public int getCursorPos()
    {
        return mCursorPos;
    }
}
