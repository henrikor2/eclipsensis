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
import net.sf.eclipsensis.dialogs.RegistryKeySelectionDialog;
import net.sf.eclipsensis.help.NSISKeywords.ShellConstant;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.util.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstallRegistryKeyDialog extends AbstractNSISInstallItemDialog
{
    protected static List<String> cProperties = new ArrayList<String>();
    protected ShellConstantConverter mShellConstantConverter = new ShellConstantConverter();

    static {
        cProperties.add("rootKey"); //$NON-NLS-1$
        cProperties.add("subKey"); //$NON-NLS-1$
    }

    public NSISInstallRegistryKeyDialog(NSISWizard wizard, NSISInstallRegistryKey item)
    {
        this(wizard, (NSISInstallRegistryItem)item);
    }

    protected NSISInstallRegistryKeyDialog(NSISWizard wizard, NSISInstallRegistryItem item)
    {
        super(wizard, item);
        mStore.setDefault("rootKey",HKLM); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    @Override
    protected List<String> getProperties()
    {
        return cProperties;
    }

    protected ShellConstantConverter getShellConstantConverter()
    {
        return mShellConstantConverter;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = layout.marginWidth = 0;
        composite.setLayout(layout);
        Control control = createControlContentsArea(composite);
        control.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        Button b = new Button(composite,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("wizard.browse.registry.label")); //$NON-NLS-1$
        b.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                browseRegistry();
            }
        });
        return composite;
    }

    protected void browseRegistry()
    {
        String regKey = decodeRegKey();
        RegistryKeySelectionDialog dialog = new RegistryKeySelectionDialog(getShell());
        dialog.setText(EclipseNSISPlugin.getResourceString("wizard.select.regkey.message")); //$NON-NLS-1$
        if(regKey != null) {
            dialog.setRegKey(regKey);
        }
        if(dialog.open() == Window.OK) {
            encodeRegKey(dialog.getRegKey());
        }
    }

    protected void encodeRegKey(String regKey)
    {
        int rootKey = 0;
        String subKey = ""; //$NON-NLS-1$
        int n = regKey.indexOf("\\"); //$NON-NLS-1$
        if(n < 0) {
            rootKey = NSISWizardDisplayValues.getHKeyIndex(regKey);
        }
        else {
            rootKey = NSISWizardDisplayValues.getHKeyIndex(regKey.substring(0,n));
            subKey = regKey.substring(n+1);
        }
        if(subKey.startsWith("\\")) { //$NON-NLS-1$
            subKey = subKey.substring(1);
        }
        if(subKey.endsWith("\\")) { //$NON-NLS-1$
            subKey = subKey.substring(0,subKey.length()-1);
        }
        if(!Common.isEmpty(subKey)) {
            mShellConstantConverter.setShellContext(ShellConstant.CONTEXT_GENERAL);
            subKey = mShellConstantConverter.encodeConstants(subKey);
        }
        mStore.setValue("rootKey",rootKey); //$NON-NLS-1$
        mStore.setValue("subKey",subKey); //$NON-NLS-1$
    }

    protected String decodeRegKey()
    {
        String regKey = null;
        int n = mStore.getInt("rootKey"); //$NON-NLS-1$
        if(n >= 0 && n < NSISWizardDisplayValues.HKEY_NAMES.length) {
             StringBuffer buf = new StringBuffer(NSISWizardDisplayValues.HKEY_NAMES[n]);
             String subKey = mStore.getString("subKey"); //$NON-NLS-1$
             if(subKey != null) {
                 if(subKey.startsWith("\\")) { //$NON-NLS-1$
                     subKey = subKey.substring(1);
                 }
                 if(subKey.endsWith("\\")) { //$NON-NLS-1$
                     subKey = subKey.substring(0,subKey.length()-1);
                 }
                 if(!Common.isEmpty(subKey)) {
                     mShellConstantConverter.setShellContext(ShellConstant.CONTEXT_GENERAL);
                     buf.append("\\").append(mShellConstantConverter.decodeConstants(subKey)); //$NON-NLS-1$
                 }
             }
             regKey = buf.toString();
        }
        return regKey;
    }

    /**
     * @param parent
     * @return
     */
    protected Control createControlContentsArea(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        if(mStore.getInt("rootKey") >= NSISWizardDisplayValues.HKEY_NAMES.length) { //$NON-NLS-1$
            mStore.setValue("rootKey", -1); //$NON-NLS-1$
        }
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,NSISWizardDisplayValues.HKEY_NAMES,mStore.getInt("rootKey"), //$NON-NLS-1$
                            true,"wizard.root.key.label",true,null,false); //$NON-NLS-1$
        c1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mStore.setValue("rootKey",c1.getSelectionIndex()); //$NON-NLS-1$
                validate();
            }
        });
        final Text t = NSISWizardDialogUtil.createText(composite,mStore.getString("subKey"),"wizard.sub.key.label",true, //$NON-NLS-1$ //$NON-NLS-2$
                           null,true);
        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("subKey",t.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });

        final IPropertyChangeListener listener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                String property = event.getProperty();
                if(property.equals("rootKey")) { //$NON-NLS-1$
                    int rootKey = ((Integer)event.getNewValue()).intValue();
                    if(c1.getSelectionIndex() != rootKey) {
                        if(rootKey >= 0 && rootKey < NSISWizardDisplayValues.HKEY_NAMES.length) {
                            c1.select(rootKey);
                        }
                        else {
                            c1.deselectAll();
                        }
                        validate();
                    }
                }
                else if(property.equals("subKey")) { //$NON-NLS-1$
                    String subKey = (String)event.getNewValue();
                    if(!Common.stringsAreEqual(subKey,t.getText())) {
                        t.setText(subKey);
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
    protected boolean hasRequiredFields()
    {
        return true;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_regkeydlg_context"; //$NON-NLS-1$
    }

    @Override
    protected String checkForErrors()
    {
        int rootKey = mStore.getInt("rootKey"); //$NON-NLS-1$
        if(rootKey < 0 || rootKey >= NSISWizardDisplayValues.HKEY_NAMES.length) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.root.key"); //$NON-NLS-1$
        }
        else {
            String subKey = mStore.getString("subKey").trim(); //$NON-NLS-1$
            if(Common.isEmpty(subKey) || subKey.endsWith("\\") || subKey.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.sub.key"); //$NON-NLS-1$
            }
            else {
                return ""; //$NON-NLS-1$
            }
        }
    }
}
