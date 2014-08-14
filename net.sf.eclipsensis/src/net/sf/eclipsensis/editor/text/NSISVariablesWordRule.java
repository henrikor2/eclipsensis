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
import net.sf.eclipsensis.help.NSISKeywords;

import org.eclipse.jface.text.rules.*;

public class NSISVariablesWordRule implements IRule, INSISConstants
{
    private IWordDetector mWordDetector;
    private IToken mPredefinedVariablesToken;
    private IToken mVariablesToken;

    public NSISVariablesWordRule(IToken predefinedVariablesToken, IToken variablesToken)
    {
        mPredefinedVariablesToken = predefinedVariablesToken;
        mVariablesToken = variablesToken;
        mWordDetector = new NSISWordDetector(){
            /*
             * (non-Javadoc) Method declared on IWordDetector.
             */
            public boolean isWordStart(char character)
            {
                return (character == '$');
            }
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        StringBuffer buffer= new StringBuffer();
        int matchOffset = -1;
        NSISKeywords.VariableMatcher variableMatcher = NSISKeywords.getInstance().createVariableMatcher();
        int c= scanner.read();
        if (mWordDetector.isWordStart((char) c)) {
            IToken token = null;
            buffer.setLength(0);
            do {
                if(c == LINE_CONTINUATION_CHAR) {
                    int c2 = scanner.read();
                    if(NSISTextUtility.delimitersDetected(scanner, c2)) {
                        c = scanner.read();
                        continue;
                    }
                    else {
                        scanner.unread();
//                        break;
                    }
                }
                buffer.append((char) c);
                variableMatcher.setText(buffer.toString());
                if(variableMatcher.hasPotentialMatch()) {
                    if(variableMatcher.isMatch()) {
                        matchOffset = ((NSISScanner)scanner).getOffset()-1;
                    }
                }
                else {
                    if(matchOffset >= 0) {
                        break;
                    }
                }
                c= scanner.read();
            } while (c != ICharacterScanner.EOF && (mWordDetector.isWordPart((char) c) || c == LINE_CONTINUATION_CHAR));
            if(matchOffset >= 0) {
                token = mPredefinedVariablesToken;
                NSISTextUtility.unread(scanner,((NSISScanner)scanner).getOffset()-matchOffset-1);
            }
            else {
                scanner.unread();
                if(buffer.length() > 1) {
                    token = mVariablesToken;
                }
                else {
                    scanner.unread();
                    token = Token.UNDEFINED;
                }
            }

            return token;
        }

        scanner.unread();
        return Token.UNDEFINED;
    }
}
