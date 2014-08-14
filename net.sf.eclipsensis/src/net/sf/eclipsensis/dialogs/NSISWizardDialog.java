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

import net.sf.eclipsensis.wizard.NSISWizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Shell;

public class NSISWizardDialog extends AbstractNSISWizardDialog
{
    public NSISWizardDialog(Shell parentShell, IWizard wizard)
    {
        super(parentShell, wizard);
    }

    @Override
    protected String getHelpContextId()
    {
        return ((NSISWizard)getWizard()).getHelpContextId();
    }
}
