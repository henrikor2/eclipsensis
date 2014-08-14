/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.picture;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

public class InstallOptionsIconEditPart extends InstallOptionsPictureEditPart
{
    @Override
    protected String getDirectEditLabelProperty()
    {
        return "icon.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("icon.type.name"); //$NON-NLS-1$
    }
}