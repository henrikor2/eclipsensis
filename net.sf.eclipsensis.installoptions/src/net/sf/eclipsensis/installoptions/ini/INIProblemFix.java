/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import org.eclipse.jface.text.Position;

public class INIProblemFix
{
    private INILine mLine;
    private String mText;
    private Position mPosition;

    public INIProblemFix(INILine line)
    {
        this(line,""); //$NON-NLS-1$
    }

    public INIProblemFix(String text)
    {
        this(null,text);
    }

    public INIProblemFix(INILine line, String text)
    {
        mLine = line;
        mText = text==null?"":text; //$NON-NLS-1$
    }

    public INILine getLine()
    {
        return mLine;
    }

    void setLine(INILine line)
    {
        mLine = line;
    }

    public Position getPosition()
    {
        return mPosition;
    }

    void setPosition(Position position)
    {
        mPosition = position;
    }

    public String getText()
    {
        return mText;
    }

    void setText(String text)
    {
        mText = text;
    }
}