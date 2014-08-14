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

public class NSISScriptLabel extends AbstractNSISScriptElement
{
    /**
     * @param name
     */
    public NSISScriptLabel(String name)
    {
        super(name.endsWith(":")?name:name+":"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    @Override
    public void write(NSISScriptWriter writer)
    {
        boolean b = writer.isIndenting();
        writer.setIndenting(false);
        super.write(writer);
        writer.setIndenting(b);
    }
}
