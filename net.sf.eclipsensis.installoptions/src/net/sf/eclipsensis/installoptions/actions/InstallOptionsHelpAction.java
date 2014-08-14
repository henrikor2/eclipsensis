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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

public class InstallOptionsHelpAction extends Action
{
    public static final String PLUGIN_HELP_URL = new StringBuffer("/").append(IInstallOptionsConstants.PLUGIN_ID).append( //$NON-NLS-1$
                                                 "/help/overview.html").toString(); //$NON-NLS-1$

    public InstallOptionsHelpAction()
    {
        super(InstallOptionsPlugin.getResourceString("help.action.label")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("help.action.tooltip")); //$NON-NLS-1$
        ImageDescriptor desc = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("help.action.icon")); //$NON-NLS-1$
        setImageDescriptor(desc);
        setHoverImageDescriptor(desc);
        desc = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("help.action.disabled.icon")); //$NON-NLS-1$
        setDisabledImageDescriptor(desc);
    }

    @Override
    public void run()
    {
        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(
                PLUGIN_HELP_URL);
    }
}
