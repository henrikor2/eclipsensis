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

public class NSISScriptInsertMacro extends AbstractNSISScriptElement
{
    private String mMacroName = null;
    private Object mArg = null;

    /**
     * @param name
     */
    public NSISScriptInsertMacro(String name)
    {
        this(name, null);
    }

    /**
     * @param name
     * @param arg
     */
    private NSISScriptInsertMacro(String[] args)
    {
        super("!insertmacro", args); //$NON-NLS-1$
    }

    /**
     * @param name
     * @param arg
     */
    public NSISScriptInsertMacro(String name, Object arg)
    {
        this(makeArray(name,arg));
        mMacroName = name;
        mArg = arg;
    }

    private void updateArgs()
    {
        updateArgs(makeArray(mMacroName,mArg));
    }

    /**
     * @return Returns the arg.
     */
    public Object getArg()
    {
        return mArg;
    }

    /**
     * @param arg The arg to set.
     */
    public void setArg(Object arg)
    {
        mArg = arg;
        updateArgs();
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mMacroName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mMacroName = name;
        updateArgs();
    }
}
