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

public class NSISScriptlet extends AbstractNSISScriptElementContainer
{
    /**
     * @param name
     * @param arg
     */
    public NSISScriptlet()
    {
        super(""); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    @Override
    public void write(NSISScriptWriter writer)
    {
        writeElements(writer);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.AbstractNSISScriptElementContainer#validateElement(net.sf.eclipsensis.script.INSISScriptElement)
     */
    @Override
    protected void validateElement(INSISScriptElement element)
            throws InvalidNSISScriptElementException
    {
    }
}
