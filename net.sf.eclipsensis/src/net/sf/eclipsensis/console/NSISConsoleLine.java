/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import net.sf.eclipsensis.script.NSISScriptProblem;

import org.eclipse.core.runtime.IPath;

public class NSISConsoleLine
{
    public static final int TYPE_INFO = 0x1;
    public static final int TYPE_WARNING = 0x2;
    public static final int TYPE_ERROR = 0x4;

    private String mText = null;
    private int mType = TYPE_INFO;
    private IPath mSource = null;
    private int mLineNum = 0;
    private NSISScriptProblem mProblem = null;

    public static NSISConsoleLine info(String text)
    {
        return new NSISConsoleLine(text,TYPE_INFO);
    }

    public static NSISConsoleLine warning(String text)
    {
        return new NSISConsoleLine(text,TYPE_WARNING);
    }

    public static NSISConsoleLine error(String text)
    {
        return new NSISConsoleLine(text,TYPE_ERROR);
    }

    /**
     * @param string
     */
    public NSISConsoleLine(String text, int type)
    {
        mText = text;
        mType = type;
    }

    public NSISConsoleLine(String text, int type, IPath source, int lineNum)
    {
        this(text, type);
        setSource(source);
        setLineNum(lineNum);
    }

    @Override
    public String toString()
    {
        return mText;
    }

    /**
     * @return Returns the type.
     */
    public int getType()
    {
        return mType;
    }
    /**
     * @return Returns the source.
     */
    public IPath getSource()
    {
        return mSource;
    }
    /**
     * @param source The source to set.
     */
    public void setSource(IPath source)
    {
        mSource = source;
    }
    /**
     * @return Returns the lineNum.
     */
    public int getLineNum()
    {
        return mLineNum;
    }
    /**
     * @param lineNum The lineNum to set.
     */
    public void setLineNum(int lineNum)
    {
        mLineNum = lineNum;
    }

    public NSISScriptProblem getProblem()
    {
        return mProblem;
    }

    public void setProblem(NSISScriptProblem problem)
    {
        mProblem = problem;
    }
}
