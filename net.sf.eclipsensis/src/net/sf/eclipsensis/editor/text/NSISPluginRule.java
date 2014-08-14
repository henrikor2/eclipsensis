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

import java.util.regex.Matcher;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.NSISPluginManager;

import org.eclipse.jface.text.rules.*;

public class NSISPluginRule implements IRule
{
    public static final String PLUGIN_CALL_VALID_CHARS="$%'`-@{}~!#()&^:"; //$NON-NLS-1$

    private IWordDetector mDetector;
    private IToken mToken;
    private StringBuffer mBuffer = new StringBuffer(""); //$NON-NLS-1$

    public NSISPluginRule(IWordDetector detector, IToken token)
    {
        mDetector = detector;
        mToken = token;
    }

    public IToken evaluate(ICharacterScanner scanner)
    {
        int c= scanner.read();
        if (c != ICharacterScanner.EOF && mDetector.isWordStart((char) c)) {
            int offset = ((NSISScanner)scanner).getOffset();
            mBuffer.setLength(0);

            mBuffer.append((char) c);
            while ((c = scanner.read()) != ICharacterScanner.EOF) {
                if(c == INSISConstants.LINE_CONTINUATION_CHAR) {
                    int c2 = scanner.read();
                    if(NSISTextUtility.delimitersDetected(scanner, c2)) {
                        continue;
                    }
                }
                if(mDetector.isWordPart((char) c)) {
                    mBuffer.append((char) c);
                }
                else {
                    break;
                }
            }
            scanner.unread();

            Matcher m = NSISPluginManager.PLUGIN_CALL_PATTERN.matcher(mBuffer.toString());
            if(m.matches()) {
                return mToken;
            }
            NSISTextUtility.unread(scanner, ((NSISScanner)scanner).getOffset()-offset);
        }

        scanner.unread();
        return Token.UNDEFINED;
    }
}
