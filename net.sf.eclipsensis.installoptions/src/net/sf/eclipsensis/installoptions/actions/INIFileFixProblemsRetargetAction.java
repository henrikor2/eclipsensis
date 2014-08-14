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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.ui.actions.RetargetAction;

public class INIFileFixProblemsRetargetAction extends RetargetAction
{
    public INIFileFixProblemsRetargetAction(String id)
    {
        super("",""); //$NON-NLS-1$ //$NON-NLS-2$
        String prefix;
        String id2 = id;
        if(INIFileFixProblemsAction.FIX_ALL_ID.equals(id2)) {
            prefix="fix.all"; //$NON-NLS-1$
        }
        else if(INIFileFixProblemsAction.FIX_WARNINGS_ID.equals(id2)) {
            prefix="fix.warnings"; //$NON-NLS-1$
        }
        else {
            id2 = INIFileFixProblemsAction.FIX_ERRORS_ID;
            prefix="fix.errors"; //$NON-NLS-1$
        }

        setId(id2);
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".action.tooltip")); //$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".action.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".action.disabled.icon"))); //$NON-NLS-1$
    }
}
