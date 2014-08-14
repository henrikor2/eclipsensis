/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.actions;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;

public class NSISExportHTMLAction extends NSISAction
{
    @Override
    public void run(IAction action)
    {
        if(mPlugin != null && mPlugin.isConfigured())
        {
            mEditor.exportHTML();
        }
        else
        {
            Common.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    EclipseNSISPlugin.getResourceString("unconfigured.confirm"), //$NON-NLS-1$
                    EclipseNSISPlugin.getShellImage());
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {

    }
}
