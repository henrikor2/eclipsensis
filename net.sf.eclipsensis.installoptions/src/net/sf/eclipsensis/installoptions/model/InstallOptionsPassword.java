/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.ini.INISection;

public class InstallOptionsPassword extends InstallOptionsText
{
    private static final long serialVersionUID = -8185800757229481950L;

    protected InstallOptionsPassword(INISection section)
    {
        super(section);
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_PASSWORD;
    }

    @Override
    protected String getDefaultState()
    {
        return ""; //$NON-NLS-1$
    }
}
