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

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.RegistryValueSelectionDialog;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallRegistryValue;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.util.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class NSISInstallRegistryValueDialog extends NSISInstallRegistryKeyDialog
{
    static {
        cProperties.add("valueType"); //$NON-NLS-1$
        cProperties.add("value"); //$NON-NLS-1$
        cProperties.add("data"); //$NON-NLS-1$
    }

    public NSISInstallRegistryValueDialog(NSISWizard wizard, NSISInstallRegistryValue item)
    {
        super(wizard, item);
        mStore.setDefault("valueType",REG_SZ); //$NON-NLS-1$
    }

    @Override
    protected void browseRegistry()
    {
        RegistryValueSelectionDialog dialog = new RegistryValueSelectionDialog(getShell());
        dialog.setText(EclipseNSISPlugin.getResourceString("wizard.select.regval.message")); //$NON-NLS-1$
        String regKey = decodeRegKey();
        if(regKey != null) {
            dialog.setRegistryValue(new RegistryValue(regKey, mShellConstantConverter.decodeConstants(mStore.getString("value")))); //$NON-NLS-1$
        }
        if(dialog.open() == Window.OK) {
            RegistryValue regValue = dialog.getRegistryValue();
            encodeRegKey(regValue.getRegKey());
            mStore.setValue("value",mShellConstantConverter.encodeConstants(regValue.getValue()==null?"":regValue.getValue())); //$NON-NLS-1$ //$NON-NLS-2$
            int type;
            switch(regValue.getType()) {
                case WinAPI.REG_DWORD:
                    type = INSISWizardConstants.REG_DWORD;
                    break;
                case WinAPI.REG_BINARY:
                    type = INSISWizardConstants.REG_BIN;
                    break;
                case WinAPI.REG_EXPAND_SZ:
                    type = INSISWizardConstants.REG_EXPAND_SZ;
                    break;
                default:
                    type = INSISWizardConstants.REG_SZ;
            }
            mStore.setValue("valueType",type); //$NON-NLS-1$
            mStore.setValue("data",mShellConstantConverter.encodeConstants(regValue.getData()==null?"":regValue.getData())); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createControlContentsArea(Composite parent)
    {
        Composite composite = (Composite)super.createControlContentsArea(parent);
        final Composite c1 = new Composite(composite,SWT.NONE);
        c1.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        final StackLayout stackLayout1 = new StackLayout();
        c1.setLayout(stackLayout1);
        final Label l1 = NSISWizardDialogUtil.createLabel(c1,"wizard.value.label",true,null,true); //$NON-NLS-1$
        final Label l2 = NSISWizardDialogUtil.createLabel(c1,"wizard.value.label",true,null,false); //$NON-NLS-1$
        stackLayout1.topControl = (mStore.getInt("valueType")!=INSISWizardConstants.REG_SZ?l1:l2); //$NON-NLS-1$
        final Text text1 = NSISWizardDialogUtil.createText(composite,mStore.getString("value"),1,true, //$NON-NLS-1$
                null);
        final Combo combo = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.REG_VALUE_TYPES,mStore.getInt("valueType"), //$NON-NLS-1$
                true,"wizard.value.type.label",true,null,false); //$NON-NLS-1$

        final Composite c2 = new Composite(composite,SWT.NONE);
        c2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        final StackLayout stackLayout2 = new StackLayout();
        c2.setLayout(stackLayout2);
        final Label l3 = NSISWizardDialogUtil.createLabel(c2,"wizard.data.label",true,null,true); //$NON-NLS-1$
        final Label l4 = NSISWizardDialogUtil.createLabel(c2,"wizard.data.label",true,null,false); //$NON-NLS-1$
        stackLayout2.topControl = (mStore.getInt("valueType")==INSISWizardConstants.REG_DWORD || mStore.getString("value").equals("")?l3:l4); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        final Text text2 = NSISWizardDialogUtil.createText(composite,mStore.getString("data"),1, true, //$NON-NLS-1$
                null);
        text1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                String oldValue = mStore.getString("value"); //$NON-NLS-1$
                String newValue = text1.getText().trim();
                mStore.setValue("value",newValue); //$NON-NLS-1$
                if(!oldValue.equals(newValue) && (oldValue.equals("") || newValue.equals(""))) { //$NON-NLS-1$ //$NON-NLS-2$
                    stackLayout2.topControl = (mStore.getInt("valueType")==INSISWizardConstants.REG_DWORD || newValue.equals("")?l3:l4); //$NON-NLS-1$ //$NON-NLS-2$
                    c2.layout();
                }

                validate();
            }
        });
        text2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("data",text2.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        text2.addVerifyListener(new NumberVerifyListener() {
            @Override
            public void verifyText(VerifyEvent e)
            {
                int index = combo.getSelectionIndex();
                if(index == INSISWizardConstants.REG_DWORD) {
                    super.verifyText(e);
                }
                else if(index == INSISWizardConstants.REG_BIN) {
                    char[] chars = e.text.toCharArray();
                    for(int i=0; i< chars.length; i++) {
                        if(!Character.isDigit(chars[i]) && (chars[i]<'a' || chars[i]>'f') && (chars[i]<'A' || chars[i]>'F')) {
                            e.display.beep();
                            e.doit = false;
                            return;
                        }
                    }
                }
            }
        });

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int oldIndex = mStore.getInt("valueType"); //$NON-NLS-1$
                int index = combo.getSelectionIndex();
                mStore.setValue("valueType",index); //$NON-NLS-1$
                if(index == INSISWizardConstants.REG_DWORD) {
                    try {
                        Integer.parseInt(text2.getText());
                    }
                    catch(Exception ex) {
                        text2.setText(""); //$NON-NLS-1$
                    }
                }
                if(oldIndex != index) {
                    if(oldIndex == INSISWizardConstants.REG_SZ || index == INSISWizardConstants.REG_SZ) {
                        stackLayout1.topControl = (index == INSISWizardConstants.REG_SZ?l2:l1);
                        c1.layout();
                    }

                    if(oldIndex == INSISWizardConstants.REG_DWORD || index == INSISWizardConstants.REG_DWORD) {
                        stackLayout2.topControl = (mStore.getString("valueType").equals("")||index  == INSISWizardConstants.REG_DWORD?l3:l4); //$NON-NLS-1$ //$NON-NLS-2$
                        c2.layout();
                    }
                }

                validate();
            }
        });
        final IPropertyChangeListener listener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                String property = event.getProperty();
                if(property.equals("value")) { //$NON-NLS-1$
                    String value = (String)event.getNewValue();
                    if(!Common.stringsAreEqual(text1.getText(),value)) {
                        text1.setText(value);
                        validate();
                    }
                }
                else if(property.equals("valueType")) { //$NON-NLS-1$
                    int type = ((Integer)event.getNewValue()).intValue();
                    if(combo.getSelectionIndex() != type) {
                        combo.select(type);
                        validate();
                    }
                }
                else if(property.equals("data")) { //$NON-NLS-1$
                    String data = (String)event.getNewValue();
                    if(!Common.stringsAreEqual(text2.getText(),data)) {
                        text2.setText(data);
                        validate();
                    }
                }
            }
        };
        mStore.addPropertyChangeListener(listener);
        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mStore.removePropertyChangeListener(listener);
            }
        });
        return composite;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_regval_context"; //$NON-NLS-1$
    }

    @Override
    protected String checkForErrors()
    {
        String error = super.checkForErrors();
        if(Common.isEmpty(error)) {
            String value = mStore.getString("value"); //$NON-NLS-1$
            String data = mStore.getString("data"); //$NON-NLS-1$
            int type = mStore.getInt("valueType"); //$NON-NLS-1$

            if(type != INSISWizardConstants.REG_SZ && value.equals("")) { //$NON-NLS-1$
                return EclipseNSISPlugin.getResourceString("wizard.blank.reg.value.name"); //$NON-NLS-1$
            }
            switch(type) {
                case INSISWizardConstants.REG_DWORD:
                    if(Common.isEmpty(data)) {
                        return EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value"); //$NON-NLS-1$
                    }
                    break;
                case INSISWizardConstants.REG_SZ:
                    if(value.equals("") && data.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                        return EclipseNSISPlugin.getResourceString("wizard.invalid.reg.value"); //$NON-NLS-1$
                    }
            }
            return ""; //$NON-NLS-1$
        }
        return error;
    }
}
