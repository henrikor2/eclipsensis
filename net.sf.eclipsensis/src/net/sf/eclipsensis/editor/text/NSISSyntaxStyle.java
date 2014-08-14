/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;


import net.sf.eclipsensis.util.*;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


public class NSISSyntaxStyle implements Cloneable
{
    private RGB mForeground = null;
    private RGB mBackground = null;
    private boolean mBold;
    private boolean mItalic;
    private boolean mUnderline;
    private boolean mStrikethrough;

    public NSISSyntaxStyle(RGB foreground, RGB background, boolean bold,
                           boolean italic, boolean underline, boolean strikeThrough)
    {
        mForeground = foreground;
        mBackground = background;
        mBold = bold;
        mItalic = italic;
        mUnderline = underline;
        mStrikethrough = strikeThrough;
    }

    @Override
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return new NSISSyntaxStyle(mForeground,mBackground,mBold,mItalic,mUnderline,mStrikethrough);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof NSISSyntaxStyle) {
            NSISSyntaxStyle style = (NSISSyntaxStyle)obj;
            if(mBold == style.mBold && mItalic == style.mItalic &&
               mUnderline == style.mUnderline && mStrikethrough == style.mStrikethrough) {
                return rgbsAreEqual(mForeground,style.mForeground) &&
                       rgbsAreEqual(mBackground,style.mBackground);
            }
        }
        return false;
    }

    private boolean rgbsAreEqual(RGB rgb1, RGB rgb2)
    {
        if(rgb1 == null && rgb2 == null) {
            return true;
        }
        else if(rgb1 != null && rgb2 != null) {
            return rgb1.equals(rgb2);
        }
        else {
            return false;
        }
    }
    @Override
    public int hashCode()
    {
        int hashCode = 0;
        if(mForeground != null) {
            hashCode += mForeground.hashCode();
        }
        if(mBackground != null) {
            hashCode += mBackground.hashCode();
        }
        hashCode += (mBold?1 << 8:0);
        hashCode += (mItalic?1 << 4:0);
        hashCode += (mUnderline?1 << 2:0);
        hashCode += (mStrikethrough?1 << 2:0);
        return hashCode;
    }

    private NSISSyntaxStyle()
    {
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        RGB rgb = mForeground;
        if(rgb != null) {
            buf.append(StringConverter.asString(rgb));
        }
        buf.append("|"); //$NON-NLS-1$
        rgb = mBackground;
        if(rgb != null) {
            buf.append(StringConverter.asString(rgb));
        }
        buf.append("|"); //$NON-NLS-1$
        buf.append(StringConverter.asString(mBold));
        buf.append("|"); //$NON-NLS-1$
        buf.append(StringConverter.asString(mItalic));
        buf.append("|"); //$NON-NLS-1$
        buf.append(StringConverter.asString(mUnderline));
        buf.append("|"); //$NON-NLS-1$
        buf.append(StringConverter.asString(mStrikethrough));

        return buf.toString();
    }

    public static NSISSyntaxStyle parse(String text)
    {
        NSISSyntaxStyle style = new NSISSyntaxStyle();
        String[] tokens = Common.tokenize(text,'|');
        int len = tokens.length;
        if(len > 0) {
            if(!Common.isEmpty(tokens[0])) {
                style.mForeground = StringConverter.asRGB(tokens[0]);
            }
            if(len > 1) {
                if(!Common.isEmpty(tokens[1])) {
                    style.mBackground = StringConverter.asRGB(tokens[1]);
                }
                if(len > 2) {
                    style.mBold = StringConverter.asBoolean(tokens[2]);
                    if(len > 3) {
                        style.mItalic = StringConverter.asBoolean(tokens[3]);
                        if(len > 4) {
                            style.mUnderline = StringConverter.asBoolean(tokens[4]);
                        }
                        if(len > 5) {
                            style.mStrikethrough = StringConverter.asBoolean(tokens[5]);
                        }
                    }
                }
            }
        }
        return style;
    }

    public TextAttribute createTextAttribute()
    {
        int style = (mBold?SWT.BOLD:0) | (mItalic?SWT.ITALIC:0) |
                    (mUnderline?TextAttribute.UNDERLINE:0) |
                    (mStrikethrough?TextAttribute.STRIKETHROUGH:0);
        return new TextAttribute(ColorManager.getColor(mForeground),
                                 ColorManager.getColor(mBackground),
                                 style);
    }

    public RGB getBackground()
    {
        return mBackground;
    }

    public void setBackground(RGB background)
    {
        mBackground = background;
    }

    public boolean isBold()
    {
        return mBold;
    }

    public void setBold(boolean bold)
    {
        mBold = bold;
    }

    public RGB getForeground()
    {
        return mForeground;
    }

    public void setForeground(RGB foreground)
    {
        mForeground = foreground;
    }

    public boolean isItalic()
    {
        return mItalic;
    }

    public void setItalic(boolean italic)
    {
        mItalic = italic;
    }

    public boolean isStrikethrough()
    {
        return mStrikethrough;
    }

    public void setStrikethrough(boolean strikethrough)
    {
        mStrikethrough = strikethrough;
    }

    public boolean isUnderline()
    {
        return mUnderline;
    }

    public void setUnderline(boolean underline)
    {
        mUnderline = underline;
    }

    public void setStyle(int style, boolean flag)
    {
        switch(style) {
            case SWT.BOLD:
                setBold(flag);
                break;
            case SWT.ITALIC:
                setItalic(flag);
                break;
            case TextAttribute.UNDERLINE:
                setUnderline(flag);
                break;
            case TextAttribute.STRIKETHROUGH:
                setStrikethrough(flag);
                break;
        }
    }
}