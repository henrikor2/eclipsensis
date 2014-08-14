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

public class NSISWordPatternRule extends NSISSingleLineRule
{
    protected IWordDetector mWordDetector;
    protected int mStartSequenceSize = 0;

    /**
     * @param startSequence
     * @param endSequence
     * @param successToken
     */
    public NSISWordPatternRule(IWordDetector wordDetector, String startSequence, String endSequence, IToken successToken)
    {
        super(startSequence, endSequence, successToken);
        mWordDetector = wordDetector;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
     */
    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        mStartSequenceSize = 0;
        return super.evaluate(scanner, resume);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISPatternRule#startSequenceDetected(org.eclipse.jface.text.rules.ICharacterScanner, char[][])
     */
    @Override
    protected boolean startSequenceDetected(ICharacterScanner scanner)
    {
        int n = ((NSISScanner)scanner).getOffset();
        boolean result = super.startSequenceDetected(scanner);
        if(result) {
            mStartSequenceSize = ((NSISScanner)scanner).getOffset() - n;
        }
        else {
            mStartSequenceSize = 0;
        }
        return result;
    }

    @Override
    protected boolean endSequenceDetected(ICharacterScanner scanner, boolean resume)
    {
        StringBuffer buffer = new StringBuffer();
        int offset = ((NSISScanner)scanner).getOffset();

        int c= scanner.read();

        boolean foundWordStart = false;
        while (true) {
            if(c == INSISConstants.LINE_CONTINUATION_CHAR) {
                int c2 = scanner.read();
                if(NSISTextUtility.delimitersDetected(scanner, c2)) {
                    c = scanner.read();
                    continue;
                }
                else {
                    scanner.unread();
                }
            }

            if(foundWordStart || resume) {
                if(!mWordDetector.isWordPart((char)c)) {
                    break;
                }
            }
            else {
                if(!mWordDetector.isWordStart((char)c)) {
                    break;
                }
                else {
                    foundWordStart = true;
                }
            }
            buffer.append((char)c);
            c= scanner.read();
        }
        scanner.unread();

        if(testEndSequence(buffer.toString())) {
            mStartSequenceSize = 0;
            return true;
        }
        else {
            NSISTextUtility.unread(scanner,(((NSISScanner)scanner).getOffset() - offset)+mStartSequenceSize);
            mStartSequenceSize = 0;
            return false;
        }
    }

    protected boolean testEndSequence(String endSequence)
    {
        if (endSequence.length() >= mEndSequence.length) {
            for (int i=mEndSequence.length - 1, j= endSequence.length() - 1; i >= 0; i--, j--) {
                if (mEndSequence[i] != endSequence.charAt(j)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
