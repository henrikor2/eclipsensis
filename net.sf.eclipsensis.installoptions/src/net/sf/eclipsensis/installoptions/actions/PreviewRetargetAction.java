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

import org.eclipse.ui.actions.RetargetAction;

public class PreviewRetargetAction extends RetargetAction
{

    public PreviewRetargetAction(int type)
    {
        super(null,null);
        String resource;
        switch(type) {
            case IInstallOptionsConstants.PREVIEW_CLASSIC:
                setId(PreviewAction.PREVIEW_CLASSIC_ID);
                resource = "preview.action.classic.label"; //$NON-NLS-1$
                break;
            default:
                setId(PreviewAction.PREVIEW_MUI_ID);
                resource = "preview.action.mui.label"; //$NON-NLS-1$
        }
        String label = InstallOptionsPlugin.getResourceString(resource);
        setText(label);
        setToolTipText(label);
    }

}
