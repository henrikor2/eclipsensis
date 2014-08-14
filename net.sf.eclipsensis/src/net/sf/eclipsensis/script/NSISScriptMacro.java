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

public class NSISScriptMacro extends AbstractNSISScriptElementContainer
{
    private String mMacroName;
    private Object mArg;
    /**
     * @param name
     * @param arg
     */
    public NSISScriptMacro(String name)
    {
        this(name, null);
    }

    /**
     * @param name
     * @param arg
     */
    public NSISScriptMacro(String name, Object arg)
    {
        this(makeArray(name, arg));
        mMacroName = name;
        mArg = arg;
    }

    private void updateArgs()
    {
        updateArgs(makeArray(mMacroName,mArg));
    }

    /**
     * @param name
     */
    private NSISScriptMacro(String[] args)
    {
        super("!macro",args); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    @Override
    public void write(NSISScriptWriter writer)
    {
        super.write(writer);
        writer.indent();
        writeElements(writer);
        writer.unindent();
        writer.println(getKeyword("!macroend")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.AbstractNSISScriptElementContainer#validateElement(net.sf.eclipsensis.script.INSISScriptElement)
     */
    @Override
    protected void validateElement(INSISScriptElement element)
            throws InvalidNSISScriptElementException
    {
        if(element instanceof NSISScriptMacro) {
            throw new InvalidNSISScriptElementException(element);
        }
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
