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

import org.eclipse.jface.text.rules.*;

public class ExclusiveEndSequenceRule extends BeginningOfLineRule
{
    /**
     * @param startSequence
     * @param endSequence
     * @param token
     */
    public ExclusiveEndSequenceRule(String startSequence, String endSequence, IToken token)
    {
        this(startSequence, endSequence, token, false);
    }

    public ExclusiveEndSequenceRule(String startSequence, String endSequence, IToken token, boolean breaksOnEOF)
    {
        super(startSequence, endSequence, token, breaksOnEOF);
    }

    @Override
    protected boolean endSequenceDetected(ICharacterScanner scanner)
    {
        int offset1 = ((IInstallOptionsScanner)scanner).getOffset();
        if(super.endSequenceDetected(scanner)) {
            int offset2 = ((IInstallOptionsScanner)scanner).getOffset();
            for (int i = 0; i < fEndSequence.length; i++) {
                scanner.unread();
                int c = scanner.read();
                if((char)c == fEndSequence[fEndSequence.length - 1 - i]) {
                    scanner.unread();
                    offset2--;
                }
                else {
                    for(int j=offset2; j> offset1; j--) {
                        scanner.unread();
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
