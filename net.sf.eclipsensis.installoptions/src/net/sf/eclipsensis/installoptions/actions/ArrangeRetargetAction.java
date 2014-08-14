/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.*;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.LabelRetargetAction;

public class ArrangeRetargetAction extends LabelRetargetAction
{
    public ArrangeRetargetAction(int type)
    {
        super(null,null);
        String id;
        switch(type) {
            case IInstallOptionsConstants.ARRANGE_SEND_BACKWARD:
                id = ArrangeAction.SEND_BACKWARD_ID;
                break;
            case IInstallOptionsConstants.ARRANGE_SEND_TO_BACK:
                id = ArrangeAction.SEND_TO_BACK_ID;
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_FORWARD:
                id = ArrangeAction.BRING_FORWARD_ID;
                break;
            case IInstallOptionsConstants.ARRANGE_BRING_TO_FRONT:
            default:
                id = ArrangeAction.BRING_TO_FRONT_ID;
                break;
        }

        setId(id);
        String prefix = id.substring(ArrangeAction.GROUP.length());
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".tooltip")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
    }
}
