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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;

public class InstallOptionsGroupBox extends InstallOptionsUneditableElement
{
    private static final long serialVersionUID = -3886719827770413416L;

    protected InstallOptionsGroupBox(INISection section)
    {
        super(section);
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_GROUPBOX;
    }

    /**
     * @return
     */
    @Override
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("groupbox.text.default"); //$NON-NLS-1$
    }

    /**
     * @return
     */
    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,124,64);
    }
}
