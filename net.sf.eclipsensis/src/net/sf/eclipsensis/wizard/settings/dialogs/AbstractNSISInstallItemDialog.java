/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractNSISInstallItemDialog extends StatusMessageDialog implements INSISWizardConstants
{
    protected INSISInstallElement mItem;
    protected IPreferenceStore mStore;
    protected NSISWizard mWizard;

    public AbstractNSISInstallItemDialog(NSISWizard wizard, INSISInstallElement item)
    {
        super(wizard.getShell());
        mWizard = wizard;
        mItem = item;
        mStore = new PreferenceStore();
        setTitle(EclipseNSISPlugin.getFormattedString("wizard.installitem.dialog.title.format", //$NON-NLS-1$
                new String[]{NSISInstallElementFactory.getTypeName(mItem.getType())}));
        Common.beanToStore(mItem, mStore, getProperties());
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Group group = new Group(composite,SWT.NONE);
        group.setLayout(new GridLayout(1,false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control control = createControlContents(group);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = convertWidthInCharsToPixels(80);
        control.setLayoutData(gd);

        NSISWizardDialogUtil.createRequiredFieldsLabel(composite);

        Dialog.applyDialogFont(composite);
        String helpContextId = getHelpContextId();
        if(helpContextId != null) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,helpContextId);
        }
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#create()
     */
    @Override
    public void create()
    {
        super.create();
        validate();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed()
    {
        if(validate()) {
            Common.storeToBean(mItem, mStore, getProperties());
            super.okPressed();
        }
    }

    protected final boolean validate()
    {
        DialogStatus status = getStatus();
        String error = checkForErrors();
        if(Common.isEmpty(error)) {
            status.setOK();
        }
        else {
            status.setError(error);
        }
        return status.getSeverity() == IStatus.OK;
    }

    protected abstract boolean hasRequiredFields();
    protected abstract String checkForErrors();
    protected abstract Control createControlContents(Composite parent);
    protected abstract List<String> getProperties();
    protected abstract String getHelpContextId();
}
