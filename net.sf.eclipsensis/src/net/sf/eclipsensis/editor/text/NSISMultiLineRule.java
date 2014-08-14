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

import org.eclipse.jface.text.rules.IToken;

public class NSISMultiLineRule extends NSISPatternRule
{
    /**
     * @param startSequence
     * @param endSequence
     * @param successToken
     */
    public NSISMultiLineRule(String startSequence, String endSequence, IToken successToken)
    {
        this(startSequence, endSequence, successToken,true);
    }

    /**
     * @param startSequence
     * @param endSequence
     * @param successToken
     * @param breaksOnEOL
     * @param breaksOnEOF
     */
    public NSISMultiLineRule(String startSequence, String endSequence,
            IToken successToken, boolean breaksOnEOF)
    {
        super(startSequence, endSequence, successToken, false, breaksOnEOF);
    }
}
