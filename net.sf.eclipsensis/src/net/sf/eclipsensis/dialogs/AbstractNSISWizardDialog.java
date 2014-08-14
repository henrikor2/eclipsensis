/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractNSISWizardDialog extends WizardDialog
{
    public AbstractNSISWizardDialog(Shell parentShell, IWizard wizard)
    {
        super(parentShell, wizard);
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setImage(EclipseNSISPlugin.getShellImage());
    }

    @Override
    public void create()
    {
        super.create();
        String helpContextId = getHelpContextId();
        if(helpContextId != null) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getContents(),helpContextId);
        }
    }

    protected abstract String getHelpContextId();
}
