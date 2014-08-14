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
import net.sf.eclipsensis.dialogs.NSISConfigWizardDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class NSISConfigWizardAction extends Action
{
    public static final String ID = NSISConfigWizardAction.class.getName();

    public NSISConfigWizardAction()
    {
        super(EclipseNSISPlugin.getResourceString("nsis.config.wizard.action.name")); //$NON-NLS-1$
        setId(ID);
    }

    @Override
    public void run()
    {
        IWorkbench workbench = PlatformUI.getWorkbench();
        final Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        final NSISConfigWizardDialog[] wizardDialog = new NSISConfigWizardDialog[1];
        BusyIndicator.showWhile(shell.getDisplay(),new Runnable() {
            public void run()
            {
                try {
                    wizardDialog[0] = new NSISConfigWizardDialog(shell);
                    wizardDialog[0].create();
                }
                catch (Exception e) {
                    wizardDialog[0] = null;
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        });
        if(wizardDialog[0] != null) {
            wizardDialog[0].open();
        }
    }
}
