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

import org.eclipse.jface.text.rules.IWordDetector;

class InstallOptionsWordDetector implements IWordDetector
{
    private boolean mFoundNonSpace = false;
    private boolean mFoundWordEnd = false;
    private char mStartChar;
    private char mEndChar;

    InstallOptionsWordDetector(char startChar, char endChar)
    {
        super();
        mStartChar = startChar;
        mEndChar = endChar;
    }

    public boolean isWordStart(char c)
    {
        return c == mStartChar;
    }

    public boolean isWordPart(char c)
    {
        if(mFoundWordEnd) {
            reset();
        }
        else {
            if(mFoundNonSpace) {
                if(c == mEndChar) {
                    mFoundWordEnd = true;
                }
                return true;
            }
            else {
                if(c != ' ' && c != '\t') {
                    if(Character.isLetter(c)) {
                        mFoundNonSpace = true;
                    }
                    else {
                        reset();
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void reset()
    {
        mFoundWordEnd = false;
        mFoundNonSpace = false;
    }
}