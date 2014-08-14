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

public class InstallOptionsUneditableElementCommand extends InstallOptionsDirectEditCommand
{
    public InstallOptionsUneditableElementCommand(InstallOptionsUneditableElement uneditable, String text)
    {
        super(uneditable, InstallOptionsModel.PROPERTY_TEXT, (text != null?text:"")); //$NON-NLS-1$
        setLabel(InstallOptionsPlugin.getFormattedString("uneditable.element.command.label",  //$NON-NLS-1$
                                                         new Object[]{uneditable.getType()}));
    }
}
