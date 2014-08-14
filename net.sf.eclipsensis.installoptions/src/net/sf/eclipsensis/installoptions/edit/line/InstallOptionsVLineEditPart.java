/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.line;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

public class InstallOptionsVLineEditPart extends InstallOptionsLineEditPart
{
    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("vline.type.name"); //$NON-NLS-1$
    }
}
