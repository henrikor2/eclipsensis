/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

public abstract class AbstractNSISScriptElement implements INSISScriptElement
{
    protected String mName = null;
    protected String[] mArgs = null;

    /**
     * @param name
     */
    public AbstractNSISScriptElement(String name)
    {
        this(name, null);
    }

    /**
     * @param name
     * @param arg
     */
    public AbstractNSISScriptElement(String name, Object arg)
    {
        mName = name;
        updateArgs(arg);
    }

    /**
     * @param arg
     */
    protected void updateArgs(Object arg)
    {
        if (arg != null)
        {
            if (arg instanceof String[])
            {
                mArgs = (String[]) arg;
            }
            else
            {
                mArgs = new String[] { arg.toString() };
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis
     * .script.NSISScriptWriter)
     */
    public void write(NSISScriptWriter writer)
    {
        writer.printValue(getKeyword(mName));
        if (!Common.isEmptyArray(mArgs))
        {
            for (int i = 0; i < mArgs.length; i++)
            {
                if (mArgs[i] != null)
                {
                    writer.print(" "); //$NON-NLS-1$
                    writer.printValue(mArgs[i]);
                }
            }
        }
        writer.println();
    }

    protected static String[] makeArray(String name, Object arg)
    {
        String[] array = null;
        if (arg != null)
        {
            if (arg instanceof String[])
            {
                String[] strings = (String[]) arg;
                array = new String[1 + strings.length];
                array[0] = name;
                System.arraycopy(strings, 0, array, 1, strings.length);
            }
            else
            {
                array = new String[] { name, arg.toString() };
            }
        }
        else
        {
            array = new String[] { name };
        }
        return array;
    }

    protected static String getKeyword(String keyword)
    {
        return NSISKeywords.getInstance().getKeyword(keyword);
    }
}
