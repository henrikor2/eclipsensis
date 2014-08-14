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

public class NSISScriptFunction extends AbstractNSISScriptElementContainer
{
    private String mFunctionName;

    /**
     * @param name
     */
    public NSISScriptFunction(String name)
    {
        super("Function",name); //$NON-NLS-1$
        mFunctionName = name;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mFunctionName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mFunctionName = name;
        updateArgs(name);
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
        writer.println(getKeyword("FunctionEnd")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.AbstractNSISScriptElementContainer#validateElement(net.sf.eclipsensis.script.INSISScriptElement)
     */
    @Override
    protected void validateElement(INSISScriptElement element)
            throws InvalidNSISScriptElementException
    {
        if(element != null) {
            if(element instanceof NSISScriptInstruction || element instanceof NSISScriptLabel) {
                return;
            }
        }
        super.validateElement(element);
    }
}
