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

import net.sf.eclipsensis.INSISConstants;

import org.eclipse.jface.text.rules.*;

public class NSISPatternRule implements IPredicateRule, INSISConstants
{
    protected IToken mSuccessToken = null;
    protected char[] mStartSequence = null;
    protected char[] mEndSequence = null;

    protected boolean mBreaksOnEOL = false;
    protected boolean mBreaksOnEOF = false;

    /**
     * @param successToken
     * @param startSequence
     * @param endSequence
     */
    public NSISPatternRule(String startSequence, String endSequence, IToken successToken)
    {
        mSuccessToken = successToken;
        mStartSequence = startSequence.toCharArray();
        mEndSequence = (endSequence==null?new char[0]:endSequence.toCharArray());
    }

    /**
     * @param successToken
     * @param startSequence
     * @param endSequence
     * @param breaksOnEOL
     */
    public NSISPatternRule(String startSequence, String endSequence, IToken successToken, boolean breaksOnEOL)
    {
        this(startSequence, endSequence, successToken);
        mBreaksOnEOL = breaksOnEOL;
    }

    /**
     * @param successToken
     * @param startSequence
     * @param endSequence
     * @param breaksOnEOL
     * @param breaksOnEOF
     */
    public NSISPatternRule(String startSequence, String endSequence, IToken successToken, boolean breaksOnEOL, boolean breaksOnEOF)
    {
        this(startSequence, endSequence, successToken, breaksOnEOL);
        mBreaksOnEOF = breaksOnEOF;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken()
    {
        return mSuccessToken;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        if (resume) {
            if (endSequenceDetected(scanner, resume)) {
                return mSuccessToken;
            }
        }
        else {
            if (startSequenceDetected(scanner)) {
                if (endSequenceDetected(scanner, resume)) {
                    return mSuccessToken;
                }
            }
        }

        return Token.UNDEFINED;
    }

    protected boolean startSequenceDetected(ICharacterScanner scanner)
    {
        int c= scanner.read();
        if (c == mStartSequence[0]) {
            if (NSISTextUtility.sequenceDetected(scanner, mStartSequence, true, false)) {
                return true;
            }
        }
        scanner.unread();
        return false;
    }

    protected boolean preProcess(ICharacterScanner scanner, int c)
    {
        return true;
    }

    protected boolean postProcess(ICharacterScanner scanner, int c)
    {
        return true;
    }

    protected boolean endSequenceDetected(ICharacterScanner scanner, boolean resume) {
        int c;
        while ((c= scanner.read()) != ICharacterScanner.EOF) {
            if(!preProcess(scanner, c)) {
                scanner.unread();
                return false;
            }
            if(c == LINE_CONTINUATION_CHAR) {
                int c2 = scanner.read();
                if(NSISTextUtility.delimitersDetected(scanner, c2)) {
                    continue;
                }
                else {
                    scanner.unread();
                }
            }

            if (mEndSequence.length > 0 && c == mEndSequence[0]) {
                // Check if the specified end sequence has been found.
                if (NSISTextUtility.sequenceDetected(scanner, mEndSequence, true, true)) {
                    return true;
                }
            }
            else if (mBreaksOnEOL) {
                // Check for end of line since it can be used to terminate the pattern.
                if(NSISTextUtility.delimitersDetected(scanner, c)) {
                    return true;
                }
            }
            if(!postProcess(scanner, c)) {
                scanner.unread();
                return false;
            }
        }
        if (mBreaksOnEOF) {
            return true;
        }
        scanner.unread();
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        return evaluate(scanner, false);
    }
}
