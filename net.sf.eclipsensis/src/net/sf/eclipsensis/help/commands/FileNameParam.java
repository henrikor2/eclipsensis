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

public class FileNameParam extends RegexpParam
{
    public FileNameParam(Node node)
    {
        super(node);
    }

    @Override
    protected String getRegexp()
    {
        return "(\\.?[a-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"; //$NON-NLS-1$
    }

    @Override
    protected String getValidateErrorMessage()
    {
        return EclipseNSISPlugin.getResourceString("file.name.param.error"); //$NON-NLS-1$
    }
}
