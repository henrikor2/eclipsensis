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

public class NSISTextProcessorRule implements IRule, INSISConstants
{
    private INSISTextProcessor mTextProcessor;

    /**
     * @param textProcessor
     */
    public void setTextProcessor(INSISTextProcessor textProcessor)
    {
        mTextProcessor = textProcessor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        mTextProcessor.setScanner(scanner);
        int c;
        while(true) {
            c = scanner.read();
            if(c == ICharacterScanner.EOF) {
                scanner.unread();
                break;
            }
            if(c == LINE_CONTINUATION_CHAR) {
                int c2 = scanner.read();
                if(NSISTextUtility.delimitersDetected(scanner,c2)) {
                    continue;
                }
                else {
                    scanner.unread();
                }
            }
            if(!mTextProcessor.isValid(c)) {
                scanner.unread();
                break;
            }
        }

        return mTextProcessor.createToken();
    }
}