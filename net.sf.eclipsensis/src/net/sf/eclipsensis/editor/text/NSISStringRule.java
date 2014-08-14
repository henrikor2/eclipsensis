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


import org.eclipse.jface.text.rules.*;

public class NSISStringRule extends NSISSingleLineRule
{
    public NSISStringRule(char stringDelimiter, IToken successToken)
    {
        super(new String(new char[]{stringDelimiter}),
              new String(new char[]{stringDelimiter}), successToken);
    }

    @Override
    protected boolean postProcess(ICharacterScanner scanner, int c)
    {
        NSISTextUtility.stringEscapeSequencesDetected(scanner, c);
        return true;
    }
}
