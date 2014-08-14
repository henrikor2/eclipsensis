/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.IOException;

import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public abstract class AbstractTemplateDialog<T extends ITemplate> extends TrayDialog
{
    private T mOldTemplate = null;
    private T mTemplate = null;
    private AbstractTemplateManager<T> mTemplateManager = null;
    private Text mTemplateName = null;
    private Text mTemplateDescription = null;
    private Button mTemplateEnabled = null;
    private boolean mCreate;

    /**
     * @param parentShell
     */
    @SuppressWarnings("unchecked")
    public AbstractTemplateDialog(Shell parentShell, AbstractTemplateManager<T> templateManager, T template, boolean create)
    {
        super(parentShell);
        setCreate(create);
        if(isCreate()) {
            mOldTemplate = null;
            mTemplate = template;
        }
        else {
            mOldTemplate = template;
            mTemplate = (T)mOldTemplate.clone();
        }
        mTemplateManager = templateManager;
    }

    protected T getTemplate()
    {
        return mTemplate;
    }

    private void setCreate(boolean create)
    {
        mCreate = create;
    }

    protected boolean isCreate()
    {
        return mCreate;
    }

    @Override
    public void create()
    {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(mTemplateName.getText()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(getShellTitle());
        newShell.setImage(getShellImage());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        ((GridLayout)composite.getLayout()).numColumns=2;

        mTemplateName = NSISWizardDialogUtil.createText(composite,(mTemplate==null?"":mTemplate.getName()),"template.dialog.name.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        mTemplateName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(((Text)e.widget).getText()));
            }
        });

        Label l = NSISWizardDialogUtil.createLabel(composite,"template.dialog.description.label",true,null,false); //$NON-NLS-1$
        GridData data = (GridData)l.getLayoutData();
        data.horizontalSpan=2;

        mTemplateDescription = NSISWizardDialogUtil.createText(composite,(mTemplate==null?"":mTemplate.getDescription()),SWT.BORDER|SWT.MULTI|SWT.WRAP,2,true,null); //$NON-NLS-1$
        Dialog.applyDialogFont(mTemplateDescription);

        data = (GridData)mTemplateDescription.getLayoutData();
        data.heightHint = convertHeightInCharsToPixels(5);
        data.widthHint = convertWidthInCharsToPixels(60);

        mTemplateEnabled = NSISWizardDialogUtil.createCheckBox(composite,"template.dialog.enabled.label",(mTemplate==null?true:mTemplate.isEnabled()),true,null,false); //$NON-NLS-1$
        data = (GridData)mTemplateDescription.getLayoutData();
        data.horizontalSpan=2;
        return composite;
    }

    protected void createUpdateTemplate()
    {
        if(mTemplate == null) {
            mTemplate = createTemplate(mTemplateName.getText());
        }
        else {
            mTemplate.setName(mTemplateName.getText());
        }
        mTemplate.setDescription(mTemplateDescription.getText());
        mTemplate.setEnabled(mTemplateEnabled.getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed()
    {
        createUpdateTemplate();
        boolean ok;
        if(isCreate()) {
            ok = mTemplateManager.addTemplate(mTemplate);
        }
        else {
            ok = mTemplateManager.updateTemplate(mOldTemplate, mTemplate);
        }
        if(ok) {
            try {
                mTemplateManager.save();
                super.okPressed();
            }
            catch(IOException ioe) {
                Common.openError(getShell(),ioe.getMessage(), getShellImage());
            }
        }
    }

    protected abstract T createTemplate(String name);
    protected abstract String getShellTitle();
    protected abstract Image getShellImage();
}
