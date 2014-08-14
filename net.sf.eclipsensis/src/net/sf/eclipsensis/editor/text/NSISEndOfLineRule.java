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

public class NSISEndOfLineRule extends NSISSingleLineRule
{
    /**
     * @param startSequence
     * @param endSequence
     * @param successToken
     */
    public NSISEndOfLineRule(String startSequence, IToken successToken)
    {
        super(startSequence, null, successToken);
    }

    /**
     * @param startSequence
     * @param endSequence
     * @param successToken
     * @param breaksOnEOF
     */
    public NSISEndOfLineRule(String startSequence, IToken successToken, boolean breaksOnEOF)
    {
        super(startSequence, null, successToken, breaksOnEOF);
    }
}
