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

public class LabelParam extends RegexpParam
{
    public LabelParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        mAllowBlank = true;
    }

    @Override
    protected String getRegexp()
    {
        return "0|\\+[1-9][0-9]*|\\-[1-9][0-9]*|[^\\+\\-0-9\\$\\!\"'`\\s].*"; //$NON-NLS-1$
    }

    @Override
    protected String getValidateErrorMessage()
    {
        return EclipseNSISPlugin.getResourceString("label.param.error"); //$NON-NLS-1$
    }
}
