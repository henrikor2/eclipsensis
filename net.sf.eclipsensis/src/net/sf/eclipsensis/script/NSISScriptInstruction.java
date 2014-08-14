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

public class NSISScriptInstruction extends AbstractNSISScriptElement
{
    private String mInstructionName = null;
    private Object mArg = null;

    /**
     * @param name
     */
    public NSISScriptInstruction(String name)
    {
        super(name);
        mInstructionName = name;
    }

    /**
     * @param name
     * @param arg
     */
    public NSISScriptInstruction(String name, Object arg)
    {
        super(name,arg);
        mInstructionName = name;
        mArg = arg;
    }

    /**
     * @return Returns the args.
     */
    public Object getArg()
    {
        return mArg;
    }

    /**
     * @param args The args to set.
     */
    public void setArg(Object args)
    {
        mArg = args;
        updateArgs();
    }

    /**
     *
     */
    private void updateArgs()
    {
        updateArgs(makeArray(mInstructionName,mArg));
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mInstructionName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mInstructionName = name;
        updateArgs();
    }
}
