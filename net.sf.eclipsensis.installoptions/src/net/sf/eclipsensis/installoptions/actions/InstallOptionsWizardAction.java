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
import net.sf.eclipsensis.installoptions.wizard.InstallOptionsWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class InstallOptionsWizardAction extends Action
{
    public InstallOptionsWizardAction()
    {
        super("");//$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("wizard.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("wizard.disabled.icon"))); //$NON-NLS-1$
        setText(InstallOptionsPlugin.getResourceString("wizard.action.label")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("wizard.action.tooltip")); //$NON-NLS-1$
    }

    @Override
    public void run()
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Shell shell = workbench.getActiveWorkbenchWindow().getShell();
        final WizardDialog[] wizardDialog = new WizardDialog[1];
        BusyIndicator.showWhile(shell.getDisplay(),new Runnable() {
            public void run()
            {
                InstallOptionsWizard wizard = new InstallOptionsWizard();
                wizard.init(workbench, StructuredSelection.EMPTY);
                wizardDialog[0] = new WizardDialog(shell, wizard);
                wizardDialog[0].create();
            }
        });
        wizardDialog[0].open();
    }
}
