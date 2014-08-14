/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardTemplatePage extends AbstractNSISWizardStartPage
{
    public static final String NAME = "nsisWizardTemplate"; //$NON-NLS-1$

    private NSISWizardTemplate mTemplate = null;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardTemplatePage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.template.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.template.description")); //$NON-NLS-1$
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return true;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_scrtmpltdlg_context"; //$NON-NLS-1$
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        mTemplate = mWizard.getTemplate();
        final Composite composite = new Composite(parent, SWT.NONE);

        final GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);
        ((GridLayout)composite.getLayout()).numColumns=2;

        final Text templateName = NSISWizardDialogUtil.createText(composite,mTemplate==null?"":mTemplate.getName(),"template.dialog.name.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        templateName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if(mTemplate != null) {
                    mTemplate.setName(((Text)e.widget).getText());
                    validatePage(VALIDATE_ALL);
                }
            }
        });

        final Text templateId;
        if(System.getProperty("manage.default.templates") != null) { //$NON-NLS-1$
            templateId = NSISWizardDialogUtil.createText(composite,mTemplate==null?"":mTemplate.getId(),"template.dialog.id.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
            templateId.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e)
                {
                    if(mTemplate != null) {
                        mTemplate.setId(((Text)e.widget).getText());
                        validatePage(VALIDATE_ALL);
                    }
                }
            });
        }
        else {
            templateId = null;
        }
        ((GridData)NSISWizardDialogUtil.createLabel(composite,"template.dialog.description.label", //$NON-NLS-1$
                true,null,false).getLayoutData()).horizontalSpan=2;

        final Text description = NSISWizardDialogUtil.createText(composite,mTemplate==null?"":mTemplate.getDescription(),SWT.BORDER|SWT.MULTI|SWT.WRAP,2,true,null); //$NON-NLS-1$
        Dialog.applyDialogFont(description);
        GridData data = (GridData)description.getLayoutData();
        data.horizontalAlignment=GridData.FILL;
        data.grabExcessHorizontalSpace=true;
        data.heightHint = Common.calculateControlSize(description,0,10).y;

        description.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if (mTemplate != null) {
                    mTemplate.setDescription(((Text)e.widget).getText());
                }
            }
        });

        final Button enabled = NSISWizardDialogUtil.createCheckBox(composite,"template.dialog.enabled.label",mTemplate != null?mTemplate.isEnabled():false,true,null,false); //$NON-NLS-1$
        data = (GridData)enabled.getLayoutData();
        data.horizontalSpan=2;
        enabled.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (mTemplate != null) {
                    mTemplate.setEnabled(((Button)e.widget).getSelection());
                }
            }
        });

        if(mWizard instanceof NSISTemplateWizard) {
            ((NSISTemplateWizard)mWizard).addTemplateListener(new INSISWizardTemplateListener() {
                public void templateChanged(NSISWizardTemplate oldTemplate, NSISWizardTemplate newTemplate)
                {
                    mTemplate = newTemplate;
                    if(mTemplate != null) {
                        templateName.setText(mTemplate.getName());
                        if(templateId != null) {
                            templateId.setText(mTemplate.getId());
                        }
                        description.setText(mTemplate.getDescription());
                        enabled.setSelection(mTemplate.isEnabled());
                    }
                    else {
                        templateName.setText(""); //$NON-NLS-1$
                        if(templateId != null) {
                            templateId.setText(""); //$NON-NLS-1$
                        }
                        description.setText(""); //$NON-NLS-1$
                        enabled.setSelection(false);
                    }
                    validatePage(VALIDATE_ALL);
                }
            });
        }
        validatePage(VALIDATE_ALL);

        return composite;
    }

    @Override
    public boolean validatePage(int flag)
    {
        boolean b = !Common.isEmpty(mTemplate != null?mTemplate.getName():""); //$NON-NLS-1$
        if(b) {
            if(System.getProperty("manage.default.templates") != null) { //$NON-NLS-1$
                b = b && !Common.isEmpty(mTemplate != null?mTemplate.getId():""); //$NON-NLS-1$
            }
            if(b) {
                setErrorMessage(null);
            }
            else {
                setErrorMessage(EclipseNSISPlugin.getResourceString("wizard.template.missing.id.error")); //$NON-NLS-1$
            }
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("wizard.template.missing.name.error")); //$NON-NLS-1$
        }
        setPageComplete(b);
        return b;
    }
}
