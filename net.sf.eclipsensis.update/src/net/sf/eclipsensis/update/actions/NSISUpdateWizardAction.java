/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.actions;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.wizard.*;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class NSISUpdateWizardAction implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow mWindow;

    public void run(IAction action)
    {
        final Shell shell = mWindow.getShell();
        final NSISUpdateWizardDialog[] wizardDialog = new NSISUpdateWizardDialog[1];
        try {
            System.setProperty("net.sf.eclipsensis.config.IsConfiguring", String.valueOf(true)); //$NON-NLS-1$
            BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
                public void run() {
                    try {
                        NSISUpdateWizard wizard = new NSISUpdateWizard();
                        wizard.setValidateNSISConfig(false);
                        wizard.setWindowTitle(EclipseNSISUpdatePlugin
                                .getResourceString("wizard.window.title")); //$NON-NLS-1$
                        wizardDialog[0] = new NSISUpdateWizardDialog(shell,
                                wizard);
                        wizardDialog[0].create();
                    } catch (Exception e) {
                        wizardDialog[0] = null;
                        EclipseNSISUpdatePlugin.getDefault().log(e);
                    }
                }
            });
            if (wizardDialog[0] != null) {
                wizardDialog[0].open();
            }
        } finally {
            System.setProperty("net.sf.eclipsensis.config.IsConfiguring", String.valueOf(false)); //$NON-NLS-1$
        }
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
    }

    public void dispose()
    {
    }

    public void init(IWorkbenchWindow window)
    {
        this.mWindow = window;
    }
}