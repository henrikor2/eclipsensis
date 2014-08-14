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

import java.util.regex.Pattern;

import org.w3c.dom.Node;

public abstract class RegexpParam extends StringParam
{
    protected Pattern mPattern;

    public RegexpParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        mPattern = Pattern.compile(getRegexp(),Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected final String validateText(String text)
    {
        if((isAllowBlank() && text.length() == 0) || mPattern.matcher(text).matches()) {
            return null;
        }
        return getValidateErrorMessage();
    }

    protected abstract String getValidateErrorMessage();
    protected abstract String getRegexp();

    /**
     * @return
     */
    protected String getSymbolRegexp()
    {
        return "\\$\\{[0-9a-z_\\-\\.]+\\}"; //$NON-NLS-1$
    }

    /**
     * @return
     */
    protected String getVarRegexp()
    {
        return "\\$[0-9a-z_]+"; //$NON-NLS-1$
    }
}
