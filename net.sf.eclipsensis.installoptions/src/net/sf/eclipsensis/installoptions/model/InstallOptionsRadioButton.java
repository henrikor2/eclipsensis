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

public class InstallOptionsRadioButton extends InstallOptionsCheckBox
{
    private static final long serialVersionUID = 1L;

    private static final String[] STATE_DISPLAY = {InstallOptionsPlugin.getResourceString("state.default"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.unselected"), //$NON-NLS-1$
                                 InstallOptionsPlugin.getResourceString("state.selected")}; //$NON-NLS-1$

    protected InstallOptionsRadioButton(INISection section)
    {
        super(section);
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_RADIOBUTTON;
    }

    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,76,11);
    }

    /**
     * @return
     */
    @Override
    public String[] getStateDisplay()
    {
        return STATE_DISPLAY;
    }

    @Override
    protected String getDefaultText()
    {
        return InstallOptionsPlugin.getResourceString("radiobutton.text.default"); //$NON-NLS-1$
    }
}
