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

public class NSISWordRule extends WordRule implements INSISConstants
{
    /**
     * @param detector
     */
    public NSISWordRule(IWordDetector detector)
    {
        super(detector);
    }

    public NSISWordRule(IWordDetector detector, IToken defaultToken)
    {
        super(detector, defaultToken);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    @Override
    public IToken evaluate(ICharacterScanner scanner)
    {
        int offset = ((NSISScanner)scanner).getOffset();
        int c= scanner.read();
        if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
            if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {
                StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$
                buffer.append((char) c);
                while ((c = scanner.read()) != ICharacterScanner.EOF) {
                    if(c == LINE_CONTINUATION_CHAR) {
                        int c2 = scanner.read();
                        if(NSISTextUtility.delimitersDetected(scanner, c2)) {
                            continue;
                        }
                    }
                    if(fDetector.isWordPart((char) c)) {
                        buffer.append((char) c);
                    }
                    else {
                        break;
                    }
                }
                scanner.unread();

                IToken token= (IToken)fWords.get(buffer.toString());
                if (token != null) {
                    return token;
                }

                if (fDefaultToken.isUndefined()) {
                    NSISTextUtility.unread(scanner, ((NSISScanner)scanner).getOffset()-offset);
                }

                return fDefaultToken;
            }
        }

        scanner.unread();
        return Token.UNDEFINED;
    }
}
