/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.text;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;

public class BeginningOfLineRule extends SingleLineRule
{
    protected static final String DUMMY_START_SEQUENCE = "\0"; //$NON-NLS-1$
    public BeginningOfLineRule(String startSequence, String endSequence, IToken token)
    {
        this(startSequence, endSequence, token, false);
    }

    public BeginningOfLineRule(String startSequence, String endSequence, IToken token, boolean breaksOnEOF)
    {
        super((Common.isEmpty(startSequence)?DUMMY_START_SEQUENCE:startSequence), endSequence, token, (char)0, true, breaksOnEOF);
    }

    @Override
    protected IToken doEvaluate(ICharacterScanner scanner, boolean resume)
    {
        try {
            int n = 0;
            if(!resume) {
                IInstallOptionsScanner ioScanner = (IInstallOptionsScanner)scanner;
                IDocument doc = ioScanner.getDocument();
                int offset = ioScanner.getOffset();
                IRegion region = doc.getLineInformationOfOffset(offset);
                if(offset != region.getOffset()) {
                    char[] chars = doc.get(region.getOffset(),offset-region.getOffset()).toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        char c = chars[i];
                        if(!isSpace(c)) {
                            return Token.UNDEFINED;
                        }
                    }
                }
                int c = scanner.read();
                if(c == ICharacterScanner.EOF) {
                    scanner.unread();
                    return Token.EOF;
                }
                while(isSpace((char)c)) {
                    n++;
                    c = scanner.read();
                }
                scanner.unread();
            }
            IToken token = Token.UNDEFINED;
            if (resume) {
                if (endSequenceDetected(scanner)) {
                    token = fToken;
                }
                else {
                    scanner.unread();
                }
            }
            else {
                if(fStartSequence.length == 1 && fStartSequence[0] == DUMMY_START_SEQUENCE.charAt(0)) {
                    if (endSequenceDetected(scanner)) {
                        token = fToken;
                    }
                }
                else {
                    int c= scanner.read();
                    if (c == fStartSequence[0] &&
                        sequenceDetected(scanner, fStartSequence, false) &&
                        endSequenceDetected(scanner)) {
                                token = fToken;
                    }
                    else {
                        scanner.unread();
                    }
                }
            }

            if(token.isUndefined()) {
                while(n > 0) {
                    scanner.unread();
                    n--;
                }
            }
            return token;
        }
        catch (BadLocationException e) {
            return Token.UNDEFINED;
        }
    }

    protected boolean isSpace(char c)
    {
        return c == ' ' || c == '\t';
    }
}
