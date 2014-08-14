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

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.w3c.dom.Node;

public class VarStringParam extends RegexpParam
{
    public VarStringParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        setAcceptVar(false);
    }

    @Override
    protected String getRegexp()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(isAcceptSymbol()) {
            buf.append(getSymbolRegexp()).append("|"); //$NON-NLS-1$
        }
        buf.append("[0-9a-z_]+"); //$NON-NLS-1$
        return buf.toString();
    }

    @Override
    protected String getValidateErrorMessage()
    {
        return EclipseNSISPlugin.getResourceString("var.string.param.error"); //$NON-NLS-1$
    }

    @Override
    public boolean verifyText(String text)
    {
        if(text != null && text.length() > 0) {
            if((isAcceptSymbol() && text.charAt(0)=='$')) {
                return true;
            }
            for(int i=0; i<text.length(); i++) {
                char c = Character.toLowerCase(text.charAt(i));
                if((c < '0' || c > '9') && (c < 'a' || c > 'z') && c != '_') {
                    return false;
                }
            }
        }
        return super.verifyText(text);
    }
}
