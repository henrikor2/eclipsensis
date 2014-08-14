/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;

public abstract class SubstitutableParam extends PrefixableParam
{
    public static final String ATTR_ACCEPT_SYMBOL = "acceptSymbol"; //$NON-NLS-1$
    public static final String ATTR_ACCEPT_VAR = "acceptVar"; //$NON-NLS-1$
    private boolean mAcceptVar;
    private boolean mAcceptSymbol;

    public SubstitutableParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        NamedNodeMap attributes = node.getAttributes();
        mAcceptVar = XMLUtil.getBooleanValue(attributes, ATTR_ACCEPT_VAR,true);
        mAcceptSymbol = XMLUtil.getBooleanValue(attributes, ATTR_ACCEPT_SYMBOL,true);
    }

    public void setAcceptSymbol(boolean acceptSymbol)
    {
        mAcceptSymbol = acceptSymbol;
    }

    public void setAcceptVar(boolean acceptVar)
    {
        mAcceptVar = acceptVar;
    }

    public boolean isAcceptSymbol()
    {
        return mAcceptSymbol;
    }

    public boolean isAcceptVar()
    {
        return mAcceptVar;
    }
}
