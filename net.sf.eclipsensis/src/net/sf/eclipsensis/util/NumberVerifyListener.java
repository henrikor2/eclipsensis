/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import org.eclipse.swt.events.*;

public class NumberVerifyListener implements VerifyListener
{
    private boolean mAllowNegative = false;

    public NumberVerifyListener(boolean allowNegative)
    {
        this();
        mAllowNegative = allowNegative;
    }

    public NumberVerifyListener()
    {
    }

    public void verifyText(VerifyEvent e)
    {
        char[] chars = e.text.toCharArray();
        for(int i=0; i< chars.length; i++) {
            if(mAllowNegative && i == 0 && e.start == 0 && chars[i] == '-') {
                continue;
            }
            if(!Character.isDigit(chars[i])) {
                e.display.beep();
                e.doit = false;
                return;
            }
        }
    }
}
