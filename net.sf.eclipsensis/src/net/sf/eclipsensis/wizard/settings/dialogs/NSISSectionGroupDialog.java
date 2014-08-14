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

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISSectionGroup;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISSectionGroupDialog extends AbstractNSISInstallItemDialog
{
    private static List<String> cProperties = new ArrayList<String>();

    static {
        cProperties.add("bold"); //$NON-NLS-1$
        cProperties.add("caption"); //$NON-NLS-1$
        cProperties.add("description"); //$NON-NLS-1$
        cProperties.add("defaultExpanded"); //$NON-NLS-1$
    }

    public NSISSectionGroupDialog(NSISWizard wizard, NSISSectionGroup item)
    {
        super(wizard, item);
        mStore.setDefault("bold",false); //$NON-NLS-1$
        mStore.setDefault("defaultExpanded",false); //$NON-NLS-1$
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_sectiongrpdlg_context"; //$NON-NLS-1$
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
        Dialog.applyDialogFont(composite);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = Common.calculateControlSize(composite,60,0).x;
        composite.setLayoutData(gd);

        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Text t = NSISWizardDialogUtil.createText(composite,mStore.getString("caption"), //$NON-NLS-1$
                        "wizard.caption.label",true,null,true); //$NON-NLS-1$
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("caption",t.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        t.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                buf.append(text.substring(0,e.start)).append(e.text).append(text.substring(e.end));
                text = buf.toString();
                if(text.length() > 0) {
                    char c = text.charAt(0);
                    if(text.length()>=3 && text.substring(0,3).equalsIgnoreCase("un.") || //$NON-NLS-1$
                                    Character.isWhitespace(c) || c == '!') {
                        e.display.beep();
                        e.doit = false;
                        return;
                    }
                }
            }
        });
        if(mWizard.getSettings().getInstallerType() == INSISWizardConstants.INSTALLER_TYPE_MUI ||
                        mWizard.getSettings().getInstallerType() == INSISWizardConstants.INSTALLER_TYPE_MUI2) {
            Label l = NSISWizardDialogUtil.createLabel(composite, "wizard.description.label", true, null, false); //$NON-NLS-1$
            ((GridData)l.getLayoutData()).horizontalSpan = 2;
            final Text t2 = NSISWizardDialogUtil.createText(composite, mStore.getString("description"), SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL, 1, true, null); //$NON-NLS-1$
            Dialog.applyDialogFont(t2);
            gd = (GridData)t2.getLayoutData();
            gd.horizontalSpan = 2;
            gd.verticalSpan = 4;
            gd.verticalAlignment = GridData.FILL;
            gd.grabExcessVerticalSpace = true;
            gd.heightHint = convertHeightInCharsToPixels(5);
            t2.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e)
                {
                    mStore.setValue("description", t2.getText().trim()); //$NON-NLS-1$
                    validate();
                }
            });
            int textLimit;
            try {
                textLimit = Integer.parseInt(NSISPreferences.getInstance().getNSISHome().getNSISExe().getDefinedSymbol("NSIS_MAX_STRLEN")); //$NON-NLS-1$
            }
            catch (Exception ex) {
                textLimit = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
            }
            t2.setTextLimit(textLimit);
        }
        Composite composite2 = new Composite(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
        gd.horizontalSpan = 2;
        composite2.setLayoutData(gd);

        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Button cb1 = NSISWizardDialogUtil.createCheckBox(composite2,"wizard.bold.label",mStore.getBoolean("bold"),true,null,false); //$NON-NLS-1$ //$NON-NLS-2$
        cb1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("bold",cb1.getSelection()); //$NON-NLS-1$
            }
        });
        ((GridData)cb1.getLayoutData()).horizontalSpan = 1;
        final Button cb2 = NSISWizardDialogUtil.createCheckBox(composite2,"wizard.default.expanded.label",mStore.getBoolean("defaultExpanded"),true,null,false); //$NON-NLS-1$ //$NON-NLS-2$
        cb2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("defaultExpanded",cb2.getSelection()); //$NON-NLS-1$
            }
        });
        ((GridData)cb2.getLayoutData()).horizontalSpan = 1;

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
        if(Common.isEmpty(mStore.getString("caption"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.missing.sectiongroup.caption"); //$NON-NLS-1$
        }
        else {
            return ""; //$NON-NLS-1$
        }
    }
}
