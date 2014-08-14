/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;

public class InstallOptionsEditableElementCommand extends InstallOptionsDirectEditCommand
{
    public InstallOptionsEditableElementCommand(InstallOptionsEditableElement editable, String state)
    {
        super(editable, InstallOptionsModel.PROPERTY_STATE, (state != null?state:"")); //$NON-NLS-1$
        setLabel(InstallOptionsPlugin.getFormattedString("editable.element.command.label",  //$NON-NLS-1$
                                                         new Object[]{editable.getType()}));
    }
}
