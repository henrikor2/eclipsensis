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

public class NSISHexNumberRule extends NSISWordPatternRule
{
    /**
     * @param detector
     * @param startSequence
     * @param endSequence
     * @param token
     */
    public NSISHexNumberRule(IToken token)
    {
        super(new IWordDetector(){
            /*
             * (non-Javadoc) Method declared on IWordDetector.
             */
            public boolean isWordStart(char character)
            {
                return isWordPart(character);
            }

            public boolean isWordPart(char character)
            {
                return (character >= '0' && character <= '9') ||
                       (character >= 'A' && character <= 'F') ||
                       (character >= 'a' && character <= 'f');
            }
        },"0x",null, token); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISWordPatternRule#testEndSequence(java.lang.String)
     */
    @Override
    protected boolean testEndSequence(String endSequence)
    {
        return (endSequence.length() > 0) && super.testEndSequence(endSequence);
    }
}
