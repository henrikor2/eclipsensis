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

public class NSISScriptUndef extends AbstractNSISScriptElement
{
    private String mSymbolName = null;

    /**
     * @param name
     */
    public NSISScriptUndef(String name) {
        super("!undef",name); //$NON-NLS-1$
        mSymbolName = name;
    }

    private void updateArgs()
    {
        updateArgs(mSymbolName);
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mSymbolName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mSymbolName = name;
        updateArgs();
    }
}
