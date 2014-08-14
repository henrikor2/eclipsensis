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


public class DefaultTextProcessor implements INSISTextProcessor, INSISConstants
{
    protected ICharacterScanner mScanner;
    protected StringBuffer mBuffer = new StringBuffer(""); //$NON-NLS-1$

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.codeassist.NSISTextUtility.INSISTextProcessor#setScanner(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public void setScanner(ICharacterScanner scanner)
    {
        mScanner = scanner;
        mBuffer.setLength(0);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.INSISTextProcessor#createToken()
     */
    public IToken createToken()
    {
        return new Token(mBuffer.toString());
    }
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.INSISTextProcessor#isValid(int)
     */
    public boolean isValid(int c)
    {
        mBuffer.append((char)c);
        return true;
    }
}