/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.ui.internal.console.IOConsolePartition
 *
 *******************************************************************************/
package net.sf.eclipsensis.console;

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class NSISConsolePartition implements ITypedRegion
{
    public static final String PARTITION_TYPE = INSISConstants.PLUGIN_ID + ".nsis_console_partition_type"; //$NON-NLS-1$
    private StringBuffer mBuffer;
    private String mType;
    private int mOffset;
    private NSISConsoleOutputStream mOutputStream;
    private int mLength;

    public NSISConsolePartition(NSISConsoleOutputStream outputStream, int length)
    {
        mOutputStream = outputStream;
        mLength = length;
        mType = PARTITION_TYPE;
    }

    public void insert(String s, int insertOffset)
    {
        mBuffer.insert(insertOffset, s);
        mLength += s.length();
    }

    public void delete(int delOffset, int delLength)
    {
        mBuffer.delete(delOffset, delOffset+delLength);
        mLength -= delLength;
    }

    public String getType()
    {
        return mType;
    }

    public int getLength()
    {
        return mLength;
    }

    public int getOffset()
    {
        return mOffset;
    }

    public void setOffset(int offset)
    {
        mOffset = offset;
    }

    public void setLength(int length)
    {
        mLength = length;
    }

    public String getString()
    {
        return mBuffer != null ? mBuffer.toString() : ""; //$NON-NLS-1$
    }

    public StyleRange getStyleRange(int rangeOffset, int rangeLength)
    {
        return new StyleRange(rangeOffset, rangeLength, getColor(), null, getFontStyle());
    }

    private int getFontStyle()
    {
        return mOutputStream.getFontStyle();
    }

    public Color getColor()
    {
        return mOutputStream.getColor();
    }

    public void clearBuffer()
    {
        mBuffer = null;
    }

    NSISConsoleOutputStream getStream()
    {
        return mOutputStream;
    }
}