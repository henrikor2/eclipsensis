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

import java.text.Collator;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallShortcut;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstallShortcutDialog extends AbstractNSISInstallItemDialog
{
    private static List<String> cProperties = new ArrayList<String>();

    static {
        cProperties.add("createInStartMenuGroup"); //$NON-NLS-1$
        cProperties.add("location"); //$NON-NLS-1$
        cProperties.add("name"); //$NON-NLS-1$
        cProperties.add("shortcutType"); //$NON-NLS-1$
        cProperties.add("path"); //$NON-NLS-1$
        cProperties.add("url"); //$NON-NLS-1$
    }

    public NSISInstallShortcutDialog(NSISWizard wizard, NSISInstallShortcut item)
    {
        super(wizard, item);
        mStore.setDefault("createInStartMenuGroup",true); //$NON-NLS-1$
        mStore.setDefault("shortcutType",SHORTCUT_URL); //$NON-NLS-1$
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_shortcutdlg_context"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    @Override
    protected List<String> getProperties()
    {
        return cProperties;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Combo c1;
        if(mWizard.getSettings().isCreateStartMenuGroup()) {
            boolean createInSMGroup = mStore.getBoolean("createInStartMenuGroup"); //$NON-NLS-1$
            Group group = NSISWizardDialogUtil.createGroup(composite, 2, "wizard.location.group.label", null, true); //$NON-NLS-1$
            final Button b1 = NSISWizardDialogUtil.createRadioButton(group, "wizard.smgroup.location.label",  //$NON-NLS-1$
                                                               createInSMGroup, true, null, false);
            b1.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    if(b1.getSelection()) {
                        mStore.setValue("createInStartMenuGroup", true); //$NON-NLS-1$
                        validate();
                    }
                }
            });
            final Button b2 = NSISWizardDialogUtil.createRadioButton(group, "wizard.other.location.label",  //$NON-NLS-1$
                    !createInSMGroup, true, null, false);
            b2.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    if(b2.getSelection()) {
                        mStore.setValue("createInStartMenuGroup", false); //$NON-NLS-1$
                        validate();
                    }
                }
            });
            ((GridData)b2.getLayoutData()).horizontalSpan = 1;
            MasterSlaveController mse = new MasterSlaveController(b2);
            c1 = NSISWizardDialogUtil.createCombo(group,null,"", //$NON-NLS-1$
                    false,null,true,mse,true);
            ((GridData)c1.getLayoutData()).horizontalSpan = 1;
            mse.updateSlaves();
        }
        else {
            mStore.setValue("createInStartMenuGroup", false); //$NON-NLS-1$
            c1 = NSISWizardDialogUtil.createCombo(composite,null,"", //$NON-NLS-1$
                    false,"wizard.location.label",true,null,true); //$NON-NLS-1$
        }
        ((GridData)c1.getLayoutData()).horizontalAlignment = GridData.FILL;
        List<String> input = Common.makeList(NSISWizardUtil.getPathConstantsAndVariables(mWizard.getSettings().getTargetPlatform()));
        ComboViewer cv = new ComboViewer(c1);
        cv.setContentProvider(new CollectionContentProvider());
        cv.setLabelProvider(new CollectionLabelProvider());
        Collator coll = Collator.getInstance();
        coll.setStrength(Collator.PRIMARY);
        cv.setSorter(new ViewerSorter(coll));
        cv.setInput(input);
        c1.setText(mStore.getString("location")); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("location",c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final Text t1 = NSISWizardDialogUtil.createText(composite,mStore.getString("name"),"wizard.name.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        t1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("name",t1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final Button[] radio = NSISWizardDialogUtil.createRadioGroup(composite,NSISWizardDisplayValues.SHORTCUT_TYPE_NAMES,mStore.getInt("shortcutType"), //$NON-NLS-1$
                            "wizard.shortcut.type.label",true,null,false); //$NON-NLS-1$
        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button b = (Button)e.widget;
                if(b.getSelection()) {
                    int n=-1;
                    if(b == radio[0]) {
                        n = 0;
                    }
                    else if(b == radio[1]) {
                        n = 1;
                    }
                    mStore.setValue("shortcutType",n); //$NON-NLS-1$
                    validate();
                }
            }
        };
        for (int i = 0; i < radio.length; i++) {
            radio[i].addSelectionListener(sa);
        }
        MasterSlaveController m1 = new MasterSlaveController(radio[SHORTCUT_URL]);
        MasterSlaveController m2 = new MasterSlaveController(radio[SHORTCUT_INSTALLELEMENT]);

        final Text t2 = NSISWizardDialogUtil.createText(composite,mStore.getString("url"),"wizard.url.label",true,m1,true); //$NON-NLS-1$ //$NON-NLS-2$
        t2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("url",t2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final Combo c2 = NSISWizardDialogUtil.createContentBrowser(composite, "wizard.path.label", mStore.getString("path"),//$NON-NLS-1$ //$NON-NLS-2$
                NSISWizardUtil.getPathConstantsAndVariables(mWizard.getSettings().getTargetPlatform()), mWizard, true, m2, true);

        c2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("path",c2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        m2.updateSlaves();
        m1.updateSlaves();

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
        if(mStore.getBoolean("createInStartMenuGroup")) { //$NON-NLS-1$
            if(!mWizard.getSettings().isCreateStartMenuGroup()) {
                return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.no.smgroup.error"); //$NON-NLS-1$
            }
        }
        else if(!NSISWizardUtil.isValidNSISPathName(mWizard.getSettings().getTargetPlatform(), mStore.getString("location"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.location"); //$NON-NLS-1$
        }
        if(!IOUtility.isValidFileName(mStore.getString("name"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.name"); //$NON-NLS-1$
        }
        int n = mStore.getInt("shortcutType"); //$NON-NLS-1$
        if((n == SHORTCUT_INSTALLELEMENT && !NSISWizardUtil.isValidNSISPathName(mWizard.getSettings().getTargetPlatform(), mStore.getString("path")))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.file"); //$NON-NLS-1$
        }
        else if((n == SHORTCUT_URL && !IOUtility.isValidURL(mStore.getString("url")))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.shortcut.url"); //$NON-NLS-1$
        }
        else {
            return ""; //$NON-NLS-1$
        }
    }
}
