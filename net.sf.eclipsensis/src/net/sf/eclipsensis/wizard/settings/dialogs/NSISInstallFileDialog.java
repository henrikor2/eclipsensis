/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallFile;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstallFileDialog extends AbstractNSISInstallItemDialog
{
    private static List<String> cProperties = new ArrayList<String>();

    static
    {
        cProperties.add("destination"); //$NON-NLS-1$
        cProperties.add("name"); //$NON-NLS-1$
        cProperties.add("overwriteMode"); //$NON-NLS-1$
        cProperties.add("nonFatal"); //$NON-NLS-1$
        cProperties.add("preserveAttributes"); //$NON-NLS-1$
    }

    public NSISInstallFileDialog(NSISWizard wizard, NSISInstallFile item)
    {
        super(wizard, item);
        mStore.setDefault("overwriteMode", OVERWRITE_ON); //$NON-NLS-1$
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_filedlg_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog
     * #getProperties()
     */
    @Override
    protected List<String> getProperties()
    {
        return cProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Text t = NSISWizardDialogUtil.createFileBrowser(composite, mStore.getString("name"), false, //$NON-NLS-1$
                Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),
                        "wizard.source.file.filternames"), //$NON-NLS-1$
                Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),
                        "wizard.source.file.filters"), //$NON-NLS-1$
                "wizard.source.file.label", true, null, true); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name", t.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite, NSISWizardUtil
                .getPathConstantsAndVariables(mWizard.getSettings().getTargetPlatform()), mStore
                .getString("destination"), //$NON-NLS-1$
                false, "wizard.destination.label", true, null, true); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination", c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        GridData gd = (GridData) c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite, NSISWizardDisplayValues.OVERWRITE_MODE_NAMES,
                mStore.getInt("overwriteMode"), //$NON-NLS-1$
                true, "wizard.overwrite.label", true, null, false); //$NON-NLS-1$
        gd = (GridData) c2.getLayoutData();
        c2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("overwriteMode", c2.getSelectionIndex()); //$NON-NLS-1$
            }
        });

        Composite composite2 = new Composite(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.horizontalSpan = 3;
        composite2.setLayoutData(gd);

        layout = new GridLayout(2, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Button b1 = NSISWizardDialogUtil.createCheckBox(composite2, "wizard.preserveattr.label", mStore //$NON-NLS-1$
                .getBoolean("preserveAttributes"), true, null, false); //$NON-NLS-1$
        gd = (GridData) b1.getLayoutData();
        gd.horizontalSpan = 1;
        gd.grabExcessHorizontalSpace = true;
        b1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("preserveAttributes", b1.getSelection()); //$NON-NLS-1$
            }
        });

        final Button b2 = NSISWizardDialogUtil.createCheckBox(composite2, "wizard.nonfatal.label", mStore //$NON-NLS-1$
                .getBoolean("nonFatal"), true, null, false); //$NON-NLS-1$
        gd = (GridData) b2.getLayoutData();
        gd.horizontalSpan = 1;
        gd.grabExcessHorizontalSpace = true;
        b2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("nonFatal", b2.getSelection()); //$NON-NLS-1$
            }
        });

        return composite;
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return true;
    }

    @Override
    protected String checkForErrors()
    {
        if (!IOUtility.isValidFile(IOUtility.decodePath(mStore.getString("name")))){ //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.name"); //$NON-NLS-1$
        }
        else if (!NSISWizardUtil.isValidNSISPathName(mWizard.getSettings().getTargetPlatform(), mStore
                .getString("destination"))){ //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.destination"); //$NON-NLS-1$
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }
}
