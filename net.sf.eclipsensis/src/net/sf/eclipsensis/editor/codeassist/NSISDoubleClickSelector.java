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


import net.sf.eclipsensis.editor.NSISCharacterPairMatcher;

import org.eclipse.jface.text.*;

public class NSISDoubleClickSelector extends NSISCharacterPairMatcher implements ITextDoubleClickStrategy
{
    /**
     *
     */
    public NSISDoubleClickSelector()
    {
        super();
        mAlwaysUsePrevChar = false;
    }

    /* (non-Javadoc)
     * Method declared on ITextDoubleClickStrategy
     */
    public void doubleClicked(ITextViewer text) {

        mPos= text.getSelectedRange().x;

        if (mPos >= 0) {
            mDocument = text.getDocument();

            if (!selectBracketBlock(text)) {
                selectWord(text);
            }
        }
    }

    /**
     * Select the word at the current selection. Return true if successful,
     * false otherwise.
     */
     protected boolean matchWord()
     {
        try {
            int pos= mPos;
            char c;

            while (pos >= 0) {
                c= mDocument.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                --pos;
            }

            mStartPos= pos;

            pos= mPos;
            int length= mDocument.getLength();

            while (pos < length) {
                c= mDocument.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                ++pos;
            }

            mEndPos= pos;

            return true;

        } catch (BadLocationException x) {
        }

        return false;
    }

    /**
     * Select the area between the selected bracket and the closing bracket. Return
     * true if successful.
     */
     protected boolean selectBracketBlock(ITextViewer text)
     {
        if (matchBracketsAt() || matchStringAt()) {

            if (mStartPos == mEndPos) {
                text.setSelectedRange(mStartPos, 0);
            }
            else {
                text.setSelectedRange(mStartPos + 1, mEndPos - mStartPos - 1);
            }

            return true;
        }
        return false;
    }

    /**
     * Select the word at the current selection.
     */
     protected void selectWord(ITextViewer text)
     {
        if (matchWord()) {

            if (mStartPos == mEndPos) {
                text.setSelectedRange(mStartPos, 0);
            }
            else {
                text.setSelectedRange(mStartPos + 1, mEndPos - mStartPos - 1);
            }
        }
    }
}
