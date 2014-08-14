/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.wizard;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class NSISUpdateWizardDialog extends WizardDialog
{
    public NSISUpdateWizardDialog(Shell shell, NSISUpdateWizard wizard)
    {
        super(shell, wizard);
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setImage(EclipseNSISUpdatePlugin.getShellImage());
    }
}