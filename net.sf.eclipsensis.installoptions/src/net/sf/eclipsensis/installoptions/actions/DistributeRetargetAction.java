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

public class DistributeRetargetAction extends LabelRetargetAction implements IInstallOptionsConstants
{
    public DistributeRetargetAction(int type)
    {
        super(null,null);
        String id;
        switch(type) {
            case DISTRIBUTE_HORIZONTAL_LEFT_EDGE:
                id = DistributeAction.HORIZONTAL_LEFT_EDGE_ID;
                break;
            case DISTRIBUTE_HORIZONTAL_RIGHT_EDGE:
                id = DistributeAction.HORIZONTAL_RIGHT_EDGE_ID;
                break;
            case DISTRIBUTE_HORIZONTAL_CENTER:
                id = DistributeAction.HORIZONTAL_CENTER_ID;
                break;
            case DISTRIBUTE_HORIZONTAL_BETWEEN:
                id = DistributeAction.HORIZONTAL_BETWEEN_ID;
                break;
            case DISTRIBUTE_VERTICAL_TOP_EDGE:
                id = DistributeAction.VERTICAL_TOP_EDGE_ID;
                break;
            case DISTRIBUTE_VERTICAL_BOTTOM_EDGE:
                id = DistributeAction.VERTICAL_BOTTOM_EDGE_ID;
                break;
            case DISTRIBUTE_VERTICAL_CENTER:
                id = DistributeAction.VERTICAL_CENTER_ID;
                break;
            case DISTRIBUTE_VERTICAL_BETWEEN:
            default:
                id = DistributeAction.VERTICAL_BETWEEN_ID;
        }
        setId(id);
        String prefix = id.substring(DistributeAction.GROUP.length());
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".tooltip")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
    }
}
