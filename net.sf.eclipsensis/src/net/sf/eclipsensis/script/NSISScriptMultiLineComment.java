/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import net.sf.eclipsensis.util.Common;

public class NSISScriptMultiLineComment implements INSISScriptElement
{
    private String mText = ""; //$NON-NLS-1$

    /**
     * @param text
     */
    public NSISScriptMultiLineComment(String text)
    {
        setText(text);
    }

    /**
     * @return Returns the text.
     */
    public String getText()
    {
        return mText;
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text)
    {
        mText = text;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    public void write(NSISScriptWriter writer)
    {
        String[] lines = Common.formatLines(getText(),SCRIPT_MAX_LINE_LENGTH-2);
        if(!Common.isEmptyArray(lines)) {
            writer.print("/* "); //$NON-NLS-1$
            writer.print(lines[0]);
            for (int i = 1; i < lines.length; i++) {
                writer.print(" * "); //$NON-NLS-1$
                writer.println(lines[i]);
            }
        }
        writer.println(" */"); //$NON-NLS-1$
    }
}
