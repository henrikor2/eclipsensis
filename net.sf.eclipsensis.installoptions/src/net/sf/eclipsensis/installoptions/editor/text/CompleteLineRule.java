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

import org.eclipse.jface.text.rules.IToken;

public class CompleteLineRule extends BeginningOfLineRule
{
    public CompleteLineRule(String startSequence, IToken token)
    {
        this(startSequence, token, false);
    }

    public CompleteLineRule(String startSequence, IToken token, boolean breaksOnEOF)
    {
        super(startSequence, null, token, breaksOnEOF);
    }
}
