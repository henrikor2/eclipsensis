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
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.*;
import net.sf.eclipsensis.wizard.settings.NSISInstallLibrary;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInstallLibraryDialog extends AbstractNSISInstallItemDialog
{
    private static final String TLB_EXTENSION = ".tlb"; //$NON-NLS-1$
    private static final String EXE_EXTENSION = ".exe"; //$NON-NLS-1$

    private static List<String> cProperties = new ArrayList<String>();

    static {
        cProperties.add("destination"); //$NON-NLS-1$
        cProperties.add("name"); //$NON-NLS-1$
        cProperties.add("shared"); //$NON-NLS-1$
        cProperties.add("libType"); //$NON-NLS-1$
        cProperties.add("protected"); //$NON-NLS-1$
        cProperties.add("reboot"); //$NON-NLS-1$
        cProperties.add("refreshShell"); //$NON-NLS-1$
        cProperties.add("unloadLibraries"); //$NON-NLS-1$
        cProperties.add("removeOnUninstall"); //$NON-NLS-1$
        cProperties.add("ignoreVersion"); //$NON-NLS-1$
        cProperties.add("x64"); //$NON-NLS-1$
    }

    public NSISInstallLibraryDialog(NSISWizard wizard, NSISInstallLibrary item)
    {
        super(wizard, item);
        mStore.setDefault("shared", true); //$NON-NLS-1$
        mStore.setDefault("libType", LIBTYPE_DLL); //$NON-NLS-1$
        mStore.setDefault("protected", true); //$NON-NLS-1$
        mStore.setDefault("reboot", true); //$NON-NLS-1$
        mStore.setDefault("refreshShell", false); //$NON-NLS-1$
        mStore.setDefault("unloadLibraries", false); //$NON-NLS-1$
        mStore.setDefault("removeOnUninstall", true); //$NON-NLS-1$
        mStore.setDefault("ignoreVersion", false); //$NON-NLS-1$
        mStore.setDefault("x64", false); //$NON-NLS-1$
    }

    @Override
    protected String checkForErrors()
    {
        if (!IOUtility.isValidFile(IOUtility.decodePath(mStore.getString("name")))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.name"); //$NON-NLS-1$
        }
        else if (!NSISWizardUtil.isValidNSISPathName(mWizard.getSettings().getTargetPlatform(), mStore.getString("destination"))) { //$NON-NLS-1$
            return EclipseNSISPlugin.getResourceString("wizard.invalid.file.destination"); //$NON-NLS-1$
        }
        else {
            return ""; //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final boolean supportsExeCom = NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_42) >= 0;

        String[] filterNames = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(), "wizard.library.filternames"); //$NON-NLS-1$
        String[] filters = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(), "wizard.library.filters"); //$NON-NLS-1$
        if(supportsExeCom) {
            filterNames = (String[]) Common.joinArrays(new Object[]{Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(), "wizard.library.filternames.exe"),filterNames}); //$NON-NLS-1$
            filters = (String[]) Common.joinArrays(new Object[]{Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(), "wizard.library.filters.exe"),filters}); //$NON-NLS-1$
        }

        final Text t = NSISWizardDialogUtil.createFileBrowser(composite, mStore.getString("name"), false, //$NON-NLS-1$
                filterNames,
                filters,
                "wizard.library.label", true, null, true); //$NON-NLS-1$
        final Combo c1 = NSISWizardDialogUtil.createCombo(composite,
                NSISWizardUtil.getPathConstantsAndVariables(mWizard.getSettings().getTargetPlatform()),
                mStore.getString("destination"), //$NON-NLS-1$
                false, "wizard.destination.label", true, null, true); //$NON-NLS-1$
        c1.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("destination", c1.getText().trim()); //$NON-NLS-1$
                validate();
            }
        });
        GridData gd = (GridData)c1.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;

        String[] libTypes = NSISWizardDisplayValues.LIBTYPES;
        if(!supportsExeCom) {
            libTypes = (String[]) Common.subArray(libTypes, 0, libTypes.length-1);
        }

        int libType = mStore.getInt("libType"); //$NON-NLS-1$
        if(libType >= libTypes.length) {
            libType = 0;
        }
        final Combo c2 = NSISWizardDialogUtil.createCombo(composite, libTypes, libType,
                true, "wizard.lib.type.label", true, null, false); //$NON-NLS-1$
        c2.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mStore.setValue("libType", c2.getSelectionIndex()); //$NON-NLS-1$
                validate();
            }
        });
        gd = (GridData)c2.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;

        t.addModifyListener(new ModifyListener() {
            private boolean fixLibType(String name, String extension, int libType)
            {
                if (name.regionMatches(true, name.length() - extension.length(), extension, 0, extension.length())) {
                    if (c2.getSelectionIndex() != libType) {
                        c2.select(libType);
                        return true;
                    }
                }
                return false;
            }

            public void modifyText(ModifyEvent e)
            {
                String name = t.getText().trim();
                mStore.setValue("name", name); //$NON-NLS-1$
                if (!fixLibType(name, TLB_EXTENSION, INSISWizardConstants.LIBTYPE_TLB)) {
                    if (supportsExeCom) {
                        fixLibType(name, EXE_EXTENSION, INSISWizardConstants.LIBTYPE_REGEXE);
                    }
                }
                validate();
            }
        });

        final Button cb1 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.shared.library.label", mStore.getBoolean("shared"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("shared", cb1.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        final Button cb2 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.upgrade.reboot.label", mStore.getBoolean("reboot"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("reboot", cb2.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        final Button cb3 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.protected.library.label", mStore.getBoolean("protected"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("protected", cb3.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        boolean flag = NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_26) >= 0;

        if (flag && mWizard.getSettings().getTargetPlatform() == INSISWizardConstants.TARGET_PLATFORM_ANY) {
            final Button cb4 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.x64.library.label", mStore.getBoolean("x64"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
            cb4.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mStore.setValue("x64", cb4.getSelection()); //$NON-NLS-1$
                    validate();
                }
            });
        }

        final Button cb5 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.refresh.shell.label", mStore.getBoolean("refreshShell"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb5.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("refreshShell", cb5.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        final Button cb6 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.unload.libraries.label", mStore.getBoolean("unloadLibraries"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        cb6.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mStore.setValue("unloadLibraries", cb6.getSelection()); //$NON-NLS-1$
                validate();
            }
        });

        if (flag) {
            final Button cb7 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.ignore.version.label", mStore.getBoolean("x64"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
            cb7.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mStore.setValue("ignoreVersion", cb7.getSelection()); //$NON-NLS-1$
                    validate();
                }
            });
        }

        if (mWizard.getSettings().isCreateUninstaller()) {
            final Button cb8 = NSISWizardDialogUtil.createCheckBox(composite, "wizard.remove.on.uninstall.label", mStore.getBoolean("removeOnUninstall"), true, null, false); //$NON-NLS-1$ //$NON-NLS-2$
            cb8.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mStore.setValue("removeOnUninstall", cb8.getSelection()); //$NON-NLS-1$
                    validate();
                }
            });
        }

        return composite;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_librarydlg_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.dialogs.AbstractNSISInstallItemDialog#getProperties()
     */
    @Override
    protected List<String> getProperties()
    {
        return cProperties;
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return true;
    }
}
