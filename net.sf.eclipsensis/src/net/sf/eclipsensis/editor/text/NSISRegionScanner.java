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

import java.util.*;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;


public class NSISRegionScanner implements NSISScanner, INSISBackwardScanner
{
    private IDocument mDocument;
    private int mStartOffset;
    private int mOffset;
    private char[][] mDelimiters;
    private char[] mContent;


    public NSISRegionScanner(IDocument document)
    {
        mDocument = document;
        String[] delimiters= mDocument.getLegalLineDelimiters();
        if(!Common.isEmptyArray(delimiters)) {
            mDelimiters = new char[delimiters.length][];
            for (int i = 0; i < delimiters.length; i++) {
                mDelimiters[i] = delimiters[i].toCharArray();
            }

            Arrays.sort(mDelimiters,new Comparator<char[]>() {
                public int compare(char[] a, char[] b)
                {
                    return (b).length-(a).length;
                }
            });
        }
        else {
            mDelimiters = new char[0][];
        }
    }

    public NSISRegionScanner(IDocument document, IRegion region)
    {
        this(document);
        setRegion(region);
    }

    public int getPreviousCharacter(int count)
    {
        if(getOffset() >= (count+1)) {
            try {
                return mDocument.get(getOffset()-(count+1),1).charAt(0);
            }
            catch (BadLocationException e) {
            }
        }
        return -1;
    }

    /**
     * @param region
     */
    public void setRegion(IRegion region)
    {
        mStartOffset = region.getOffset();
        mOffset = 0;
        try {
            String content = mDocument.get(mStartOffset,region.getLength());
            mContent = content.toCharArray();
        }
        catch (BadLocationException e) {
            mContent = new char[0];
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISScanner#getOffset()
     */
    public int getOffset()
    {
        return mStartOffset+mOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.ICharacterScanner#getColumn()
     */
    public int getColumn()
    {
        return (mOffset >=0 && mOffset < mContent.length?mOffset:-1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.ICharacterScanner#getLegalLineDelimiters()
     */
    public char[][] getLegalLineDelimiters()
    {
        return mDelimiters;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.ICharacterScanner#read()
     */
    public int read()
    {
        try {
            if(mOffset < mContent.length) {
                return mContent[mOffset];
            }
            return EOF;
        }
        finally {
            mOffset++;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.ICharacterScanner#unread()
     */
    public void unread()
    {
        mOffset--;
    }

    public void reset()
    {
        mOffset = 0;
    }
}