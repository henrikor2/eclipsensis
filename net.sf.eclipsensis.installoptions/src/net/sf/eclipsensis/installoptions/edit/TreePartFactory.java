/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogTreeEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.gef.*;

public class TreePartFactory implements EditPartFactory
{
    public static final TreePartFactory INSTANCE = new TreePartFactory();

    private TreePartFactory()
    {
    }

    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof InstallOptionsDialog) {
            return new InstallOptionsDialogTreeEditPart(model);
        }
        else {
            return new InstallOptionsTreeEditPart(model);
        }
    }
}
