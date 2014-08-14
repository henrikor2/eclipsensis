/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import net.sf.eclipsensis.editor.text.*;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

public class NSISCharacterPairMatcher implements ICharacterPairMatcher
{
    protected IDocument mDocument;
    protected int mPos;
    protected int mStartPos;
    protected int mEndPos;
    protected static final char[] BRACKETS = { '{', '}', '(', ')', '[', ']' };
    private int mAnchor;
    protected boolean mAlwaysUsePrevChar = true;

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#dispose()
     */
    public void dispose()
    {
        clear();
        mDocument= null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#clear()
     */
    public void clear()
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#match(org.eclipse.jface.text.IDocument, int)
     */
    public IRegion match(IDocument document, int pos)
    {
        mPos= pos;

        if (mPos >= 0) {
            mDocument= document;

            if (mDocument != null) {
                if ( (matchBracketsAt() || matchStringAt()) && mStartPos != mEndPos) {
                    return new Region(mStartPos, mEndPos - mStartPos + 1);
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.ICharacterPairMatcher#getAnchor()
     */
    public int getAnchor()
    {
        return mAnchor;
    }

    protected boolean matchStringAt()
    {
        mStartPos= -1;
        mEndPos= -1;

        // get the chars preceding and following the start mPosition
        try {
            ITypedRegion typedRegion = NSISTextUtility.getNSISPartitionAtOffset(mDocument,mPos);
            String type = typedRegion.getType();
            int offset = typedRegion.getOffset();
            if(type.equals(NSISPartitionScanner.NSIS_STRING)) {
                int endOffset = offset+typedRegion.getLength();

                if(mPos == offset+1) {
                    mStartPos = mPos - 1;
                    mEndPos = endOffset-1;
                    mAnchor = LEFT;
                    return true;
                }
                else if(!mAlwaysUsePrevChar && mPos == (endOffset-1)) {
                    mStartPos = offset;
                    mEndPos = mPos;
                    mAnchor = RIGHT;
                    return true;
                }
            }
            else if(type.equals(IDocument.DEFAULT_CONTENT_TYPE) && mAlwaysUsePrevChar && mPos == offset) {
                ITypedRegion typedRegion2 = NSISTextUtility.getNSISPartitionAtOffset(mDocument, mPos-1);
                if(typedRegion2.getType().equals(NSISPartitionScanner.NSIS_STRING)) {
                    offset = typedRegion2.getOffset();
                    mStartPos = offset;
                    mEndPos = offset + typedRegion2.getLength();
                    mAnchor = RIGHT;
                    return true;
                }
            }
        }
        catch (BadLocationException x) {
        }
        return false;
    }

    /**
     * Match the brackets at the current selection. Return true if successful,
     * false otherwise.
     */
    protected boolean matchBracketsAt()
    {
        char prevChar, nextChar;

        int i;
        int bracketIndex1= BRACKETS.length;
        int bracketIndex2= BRACKETS.length;

        mStartPos= -1;
        mEndPos= -1;

        try {
            prevChar= mDocument.getChar(Math.max(mPos - 1,0));
            nextChar= (mAlwaysUsePrevChar?prevChar:mDocument.getChar(mPos));

            for (i= 0; i < BRACKETS.length; i= i + 2) {
                if (prevChar == BRACKETS[i]) {
                    mStartPos= mPos - 1;
                    bracketIndex1= i;
                    break;
                }
            }
            for (i= 1; i < BRACKETS.length; i= i + 2) {
                if (nextChar == BRACKETS[i]) {
                    mEndPos= mPos - (mAlwaysUsePrevChar?1:0);
                    bracketIndex2= i;
                    break;
                }
            }

            if (mStartPos > -1 && bracketIndex1 < bracketIndex2) {
                mAnchor= LEFT;
                mEndPos= searchForClosingBracket(mStartPos, BRACKETS[bracketIndex1], BRACKETS[bracketIndex1 + 1], mDocument);
                if (mEndPos > -1) {
                    return true;
                }
                else {
                    mStartPos= -1;
                }
            }
            else if (mEndPos > -1) {
                mAnchor= RIGHT;
                mStartPos= searchForOpenBracket(mEndPos, BRACKETS[bracketIndex2 - 1], BRACKETS[bracketIndex2], mDocument);
                if (mStartPos > -1) {
                    return true;
                }
                else {
                    mEndPos= -1;
                }
            }

        }
        catch (BadLocationException x) {
        }

        return false;
    }

    /**
     * Returns the mPosition of the closing bracket after startPosition.
     * @returns the location of the closing bracket.
     * @param startPosition - the beginning mPosition
     * @param openBracket - the character that represents the open bracket
     * @param closeBracket - the character that represents the close bracket
     * @param document - the document being searched
     */
    protected int searchForClosingBracket(int startPosition, char openBracket, char closeBracket, IDocument document) throws BadLocationException
    {
        int stack= 1;
        int closePosition= startPosition + 1;
        int length= document.getLength();
        char nextChar;

        while (closePosition < length && stack > 0) {
            nextChar= document.getChar(closePosition);
            if (nextChar == openBracket && nextChar != closeBracket) {
                stack++;
            }
            else if (nextChar == closeBracket) {
                stack--;
            }
            closePosition++;
        }

        if (stack == 0) {
            return closePosition - 1;
        }
        else {
            return -1;
        }
    }

    /**
     * Returns the mPosition of the open bracket before startPosition.
     * @returns the location of the starting bracket.
     * @param startPosition - the beginning mPosition
     * @param openBracket - the character that represents the open bracket
     * @param closeBracket - the character that represents the close bracket
     * @param document - the document being searched
     */
    protected int searchForOpenBracket(int startPosition, char openBracket, char closeBracket, IDocument document) throws BadLocationException
    {
        int stack= 1;
        int openPos= startPosition - 1;
        char nextChar;

        while (openPos >= 0 && stack > 0) {
            nextChar= document.getChar(openPos);
            if (nextChar == closeBracket && nextChar != openBracket) {
                stack++;
            }
            else if (nextChar == openBracket) {
                stack--;
            }
            openPos--;
        }

        if (stack == 0) {
            return openPos + 1;
        }
        else {
            return -1;
        }
    }
}
