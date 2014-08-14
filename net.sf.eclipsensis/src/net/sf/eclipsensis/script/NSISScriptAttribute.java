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

public class NSISScriptAttribute extends AbstractNSISScriptElement
{
    private String mAttributeName = null;
    private Object mAttributeArgs = null;
    /**
     * @param name
     */
    public NSISScriptAttribute(String name)
    {
        super(name);
        mAttributeName = name;
    }

    /**
     * @param name
     * @param arg
     */
    public NSISScriptAttribute(String name, Object args)
    {
        super(name,args);
        mAttributeName = name;
        mAttributeArgs = args;
    }

    /**
     * @return Returns the args.
     */
    public Object getArgs()
    {
        return mAttributeArgs;
    }

    /**
     * @param args The args to set.
     */
    public void setArgs(Object args)
    {
        mAttributeArgs = args;
        updateArgs();
    }

    /**
     *
     */
    private void updateArgs()
    {
        updateArgs(makeArray(mAttributeName,mAttributeArgs));
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mAttributeName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mAttributeName = name;
        updateArgs();
    }
}
