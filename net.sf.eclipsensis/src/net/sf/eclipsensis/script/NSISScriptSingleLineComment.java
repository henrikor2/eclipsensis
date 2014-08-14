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

public class NSISScriptSingleLineComment extends NSISScriptMultiLineComment
{
    public static final String PREFIX_SEMICOLON = ";"; //$NON-NLS-1$
    public static final String PREFIX_HASH = "#"; //$NON-NLS-1$
    private String mPrefix = PREFIX_HASH;

    /**
     * @param text
     */
    public NSISScriptSingleLineComment(String text)
    {
        super(text);
    }

    /**
     * @param prefix
     * @param text
     */
    public NSISScriptSingleLineComment(String prefix, String text)
    {
        this(text);
        setPrefix(prefix);
    }

    /**
     * @return Returns the prefix.
     */
    public String getPrefix()
    {
        return mPrefix;
    }

    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix)
    {
        if(PREFIX_HASH.equals(prefix) || PREFIX_SEMICOLON.equals(prefix)) {
            mPrefix = prefix;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    @Override
    public void write(NSISScriptWriter writer)
    {
        String[] lines = Common.formatLines(getText(),SCRIPT_MAX_LINE_LENGTH-2);
        if(!Common.isEmptyArray(lines)) {
            for (int i = 0; i < lines.length; i++) {
                writer.print(mPrefix);
                writer.print(" "); //$NON-NLS-1$
                writer.println(lines[i]);
            }
        }
    }
}
