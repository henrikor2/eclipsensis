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

import java.beans.*;
import java.io.File;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SaveAsDialog;

public class NSISWizardCompletionPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardCompletion"; //$NON-NLS-1$

    private static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    private static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("nsis.script.filtername")}; //$NON-NLS-1$
    private static final int PROGRAM_FILE_CHECK=0x1;
    private static final int README_FILE_CHECK=0x10;
    private static final int SAVE_PATH_CHECK=0x100;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardCompletionPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.completion.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.completion.description")); //$NON-NLS-1$
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return isScriptWizard();
    }

    private boolean validateField(int flag)
    {
        if(validatePage(flag)) {
            return validatePage(VALIDATE_ALL & ~flag);
        }
        else {
            return false;
        }
    }

    private boolean validateSavePath()
    {
        String pathname = mWizard.getSettings().getSavePath();
        if(Common.isEmpty(pathname)) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("empty.save.location.error")); //$NON-NLS-1$
            return false;
        }
        else if(Path.EMPTY.isValidPath(pathname)) {
            IPath path = new Path(pathname);
            path = path.removeLastSegments(1);
            if(mWizard.getSettings().isSaveExternal()) {
                File file = new File(path.toOSString());
                if(IOUtility.isValidDirectory(file)) {
                    return true;
                }
            }
            else {
                IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                if(resource != null && (resource instanceof IFolder || resource instanceof IProject)) {
                    return true;
                }
            }
        }
        setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.save.location.error",new String[]{pathname})); //$NON-NLS-1$
        return false;
    }

    @Override
    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();

            boolean b = ((flag & PROGRAM_FILE_CHECK) == 0 || validateNSISPath(settings.getRunProgramAfterInstall()))&&
                        ((flag & README_FILE_CHECK) == 0 || validateNSISPath(settings.getOpenReadmeAfterInstall()))&&
                        ((flag & SAVE_PATH_CHECK) == 0 || validateSavePath());
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    /**
     * @return
     */
    private boolean validateNSISPath(String pathname)
    {
        boolean b = Common.isEmpty(pathname) || NSISWizardUtil.isValidNSISPathName(mWizard.getSettings().getTargetPlatform(),pathname);
        if(!b) {
            setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.nsis.pathname.error",new String[]{pathname})); //$NON-NLS-1$
        }
        return b;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizcomplete_context"; //$NON-NLS-1$
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        createMiscInstallerSettingsGroup(composite);
        createPostInstallationActionsGroup(composite);
        createMiscUninstallerSettingsGroup(composite);
        createScriptSaveSettingsGroup(composite);

        validatePage(VALIDATE_ALL);

        return composite;
    }

    /**
     * @param composite
     */
    private void createMiscInstallerSettingsGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group miscGroup = NSISWizardDialogUtil.createGroup(parent, 2, "miscellaneous.installer.settings.group.label",null,false); //$NON-NLS-1$
        ((GridLayout)miscGroup.getLayout()).makeColumnsEqualWidth = true;
        final Button showInstDetails = NSISWizardDialogUtil.createCheckBox(miscGroup, "show.installer.details.label", //$NON-NLS-1$
                              settings.isShowInstDetails(),(settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        GridData data = (GridData)showInstDetails.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        showInstDetails.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setShowInstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button autoclose = NSISWizardDialogUtil.createCheckBox(miscGroup, "autoclose.installer.label", //$NON-NLS-1$
                settings.isAutoCloseInstaller(),(settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        data = (GridData)autoclose.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        autoclose.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setAutoCloseInstaller(((Button)e.widget).getSelection());
            }
        });

        final Button uninstShortcut = NSISWizardDialogUtil.createCheckBox(miscGroup, "uninstaller.shortcut.startmenu.label", //$NON-NLS-1$
                                        settings.isCreateUninstallerStartMenuShortcut(),
                                        settings.isCreateUninstaller(), null, false);

        uninstShortcut.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCreateUninstallerStartMenuShortcut(((Button)e.widget).getSelection());
            }
        });

        final Button uninstControlPanel = NSISWizardDialogUtil.createCheckBox(miscGroup, "uninstaller.entry.control.panel.label", //$NON-NLS-1$
                                        settings.isCreateUninstallerControlPanelEntry(),
                                        settings.isCreateUninstaller(), null, false);
        uninstControlPanel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCreateUninstallerControlPanelEntry(((Button)e.widget).getSelection());
            }
        });

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                NSISWizardSettings settings = mWizard.getSettings();

                boolean b = settings.getInstallerType() != INSTALLER_TYPE_SILENT;
                showInstDetails.setEnabled(b);
                autoclose.setEnabled(b);
                b = settings.isCreateUninstaller();
                uninstShortcut.setEnabled(b);
                uninstControlPanel.setEnabled(b);
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                showInstDetails.setSelection(newSettings.isShowInstDetails());
                showInstDetails.setEnabled(newSettings.getInstallerType() != INSTALLER_TYPE_SILENT);
                autoclose.setSelection(newSettings.isAutoCloseInstaller());
                autoclose.setEnabled(newSettings.getInstallerType() != INSTALLER_TYPE_SILENT);
                uninstShortcut.setSelection(newSettings.isCreateUninstallerStartMenuShortcut());
                uninstShortcut.setEnabled(newSettings.isCreateUninstaller());
                uninstControlPanel.setSelection(newSettings.isCreateUninstallerControlPanelEntry());
                uninstControlPanel.setEnabled(newSettings.isCreateUninstaller());
            }});
    }

    /**
     * @param composite
     */
    private void createPostInstallationActionsGroup(Composite parent)
    {
        final Group postInstGroup = NSISWizardDialogUtil.createGroup(parent, 3, "post.installation.actions.group.label",null,false); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();

        String[] pathConstantsAndVariables = NSISWizardUtil.getPathConstantsAndVariables(settings.getTargetPlatform());

        final Combo runProg = NSISWizardDialogUtil.createContentBrowser(postInstGroup, "run.program.label", settings.getRunProgramAfterInstall(), //$NON-NLS-1$
                                                                   pathConstantsAndVariables, mWizard, true, null, false);
        final Text runParams = NSISWizardDialogUtil.createText(postInstGroup,settings.getRunProgramAfterInstallParams(),"run.params.label",true,null,false); //$NON-NLS-1$
        runParams.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setRunProgramAfterInstallParams(((Text)e.widget).getText());
                validateField(PROGRAM_FILE_CHECK);
            }
        });

        Label l = (Label)runParams.getData(NSISWizardDialogUtil.LABEL);
        if(l != null) {
            ((GridData)l.getLayoutData()).horizontalIndent=8;
        }

        runProg.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setRunProgramAfterInstall(runProg.getText());
                boolean b = validatePage(PROGRAM_FILE_CHECK);
                runParams.setEnabled(b);
                if(b) {
                    validatePage(VALIDATE_ALL & ~PROGRAM_FILE_CHECK);
                }
            }
        });

        final Combo openReadme = NSISWizardDialogUtil.createContentBrowser(postInstGroup, "open.readme.label", settings.getOpenReadmeAfterInstall(), //$NON-NLS-1$
                                                                   pathConstantsAndVariables, mWizard, true, null, false);
        openReadme.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setOpenReadmeAfterInstall(openReadme.getText());
                validateField(README_FILE_CHECK);
            }
        });

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if(isCurrentPage()) {
                    NSISWizardSettings settings = mWizard.getSettings();
                    runProg.setText(settings.getRunProgramAfterInstall());
                    openReadme.setText(settings.getOpenReadmeAfterInstall());
                }
            }
        });

        final PropertyChangeListener propertyListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(NSISWizardSettings.TARGET_PLATFORM.equals(evt.getPropertyName())) {
                    NSISWizardSettings settings = (NSISWizardSettings)evt.getSource();
                    String[] pathConstantsAndVariables = NSISWizardUtil.getPathConstantsAndVariables(((Integer)evt.getNewValue()).intValue());
                    NSISWizardDialogUtil.populateCombo(runProg,pathConstantsAndVariables,settings.getRunProgramAfterInstall());
                    NSISWizardDialogUtil.populateCombo(openReadme,pathConstantsAndVariables,settings.getOpenReadmeAfterInstall());
                }
            }
        };
        settings.addPropertyChangeListener(propertyListener);
        final INSISWizardSettingsListener listener = new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                if(oldSettings != null) {
                    oldSettings.removePropertyChangeListener(propertyListener);
                }
                runProg.setText(newSettings.getRunProgramAfterInstall());
                runParams.setText(newSettings.getRunProgramAfterInstallParams());
                openReadme.setText(newSettings.getOpenReadmeAfterInstall());
                newSettings.addPropertyChangeListener(propertyListener);
            }
        };
        mWizard.addSettingsListener(listener);
        postInstGroup.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mWizard.removeSettingsListener(listener);
                mWizard.getSettings().removePropertyChangeListener(propertyListener);
            }
        });
    }

    /**
     * @param composite
     */
    private void createScriptSaveSettingsGroup(Composite parent)
    {
        final Group saveGroup = NSISWizardDialogUtil.createGroup(parent, 1, "script.save.settings.group.label",null,true); //$NON-NLS-1$
        saveGroup.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        GridLayout layout = new GridLayout(1,false);
        saveGroup.setLayout(layout);

        Composite c = new Composite(saveGroup,SWT.None);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        final Button[] saveTypes = NSISWizardDialogUtil.createRadioGroup(c,new String[] {EclipseNSISPlugin.getResourceString("workspace.save.label"), //$NON-NLS-1$
                                                              EclipseNSISPlugin.getResourceString("filesystem.save.label")}, //$NON-NLS-1$
                                              mWizard.getSettings().isSaveExternal()?1:0,"save.label",true,null,false); //$NON-NLS-1$
        final Text saveLocation = NSISWizardDialogUtil.createText(c, mWizard.getSettings().getSavePath().toString(),"save.location.label",true,null,isScriptWizard()); //$NON-NLS-1$
        ((GridData)saveLocation.getLayoutData()).horizontalSpan = 1;
        saveLocation.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mWizard.getSettings().setSavePath(((Text)e.widget).getText());
                validateField(SAVE_PATH_CHECK);
                if(isScriptWizard()) {
                    ((NSISScriptWizard)mWizard).setCheckOverwrite(mWizard.getSettings().getSavePath().length() > 0);
                }
            }
        });
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean saveExternal = saveTypes[1].getSelection();
                if(saveExternal != mWizard.getSettings().isSaveExternal()) {
                    mWizard.getSettings().setSaveExternal(saveExternal);
                    saveLocation.setText(""); //$NON-NLS-1$
                }
            }
        };
        saveTypes[0].addSelectionListener(selectionAdapter);
        saveTypes[1].addSelectionListener(selectionAdapter);

        Button browseSave = new Button(c,SWT.PUSH);
        browseSave.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        browseSave.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        browseSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String savePath = mWizard.getSettings().getSavePath();
                if(Common.isEmpty(savePath)) {
                    savePath = EclipseNSISPlugin.getResourceString("default.save.name"); //$NON-NLS-1$
                }
                if(mWizard.getSettings().isSaveExternal()) {
                    FileDialog dialog = new FileDialog(getShell(),SWT.SAVE);
                    dialog.setFileName(savePath);
                    dialog.setFilterExtensions(FILTER_EXTENSIONS);
                    dialog.setFilterNames(FILTER_NAMES);
                    dialog.setText(EclipseNSISPlugin.getResourceString("save.location.title")); //$NON-NLS-1$
                    savePath = dialog.open();
                    if(savePath != null) {
                        saveLocation.setText(savePath);
                    }
                }
                else {
                    SaveAsDialog dialog = new SaveAsDialog(getShell());
                    IPath path = new Path(savePath);
                    if(path.isAbsolute()) {
                        try {
                            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                            dialog.setOriginalFile(file);
                        }
                        catch (Exception e1) {
                        }
                    }
                    else {
                        dialog.setOriginalName(path.toString());
                    }
                    dialog.setTitle(EclipseNSISPlugin.getResourceString("save.location.title")); //$NON-NLS-1$
                    dialog.create();
                    dialog.setMessage(EclipseNSISPlugin.getResourceString("save.location.message")); //$NON-NLS-1$
                    int returnCode = dialog.open();
                    if(returnCode == Window.OK) {
                        saveLocation.setText(dialog.getResult().toString());
                        if(isScriptWizard()) {
                            ((NSISScriptWizard)mWizard).setCheckOverwrite(false);
                        }
                    }
                }
            }
        });

        final Button relativePaths = NSISWizardDialogUtil.createCheckBox(saveGroup, "make.paths.relative.label", //$NON-NLS-1$
                mWizard.getSettings().isMakePathsRelative(),true, null, false);
        relativePaths.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMakePathsRelative(((Button)e.widget).getSelection());
            }
        });

        c = new Composite(saveGroup,SWT.None);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        layout = new GridLayout(2,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        final Button compile = NSISWizardDialogUtil.createCheckBox(c, "compile.label", //$NON-NLS-1$
                                        mWizard.getSettings().isCompileScript(),
                                        true, null, false);
        ((GridData)compile.getLayoutData()).horizontalSpan = 1;
        compile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setCompileScript(((Button)e.widget).getSelection());
            }
        });

        MasterSlaveController m = new MasterSlaveController(compile);
        final Button testInstaller = NSISWizardDialogUtil.createCheckBox(c, "test.label", //$NON-NLS-1$
                mWizard.getSettings().isTestScript(),
                compile.getSelection(), m, false);
        ((GridData)testInstaller.getLayoutData()).horizontalSpan = 1;
        testInstaller.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setTestScript(((Button)e.widget).getSelection());
            }
        });

        if(isScriptWizard()) {
            final NSISScriptWizard scriptWizard = (NSISScriptWizard)mWizard;
            final Button button = NSISWizardDialogUtil.createCheckBox(saveGroup,"save.wizard.template.label",scriptWizard.isSaveAsTemplate(),true,null,false); //$NON-NLS-1$
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    scriptWizard.setSaveAsTemplate(button.getSelection());
                }
            });
            scriptWizard.addSettingsListener(new INSISWizardSettingsListener() {
                public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
                {
                    scriptWizard.setSaveAsTemplate(false);
                    button.setSelection(false);
                }
            });
        }

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                saveLocation.setText(newSettings.getSavePath().toString());
                saveTypes[0].setSelection(!newSettings.isSaveExternal());
                saveTypes[1].setSelection(newSettings.isSaveExternal());
                relativePaths.setSelection(newSettings.isMakePathsRelative());
                compile.setSelection(newSettings.isCompileScript());
                testInstaller.setSelection(newSettings.isTestScript());
            }
       });
    }

    /**
     * @param composite
     */
    private void createMiscUninstallerSettingsGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        final Group miscUninstGroup = NSISWizardDialogUtil.createGroup(parent, 2, "miscellaneous.uninstaller.settings.group.label",null,false); //$NON-NLS-1$
        ((GridLayout)miscUninstGroup.getLayout()).makeColumnsEqualWidth = true;

        final Button silentUninst = NSISWizardDialogUtil.createCheckBox(miscUninstGroup, "silent.uninstaller", //$NON-NLS-1$
                settings.isSilentUninstaller(),true, null, false);
        silentUninst.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setSilentUninstaller(((Button)e.widget).getSelection());
            }
        });
        silentUninst.setEnabled(settings.isCreateUninstaller());
        final MasterSlaveController m = new MasterSlaveController(silentUninst,true);
        MasterSlaveEnabler enabler = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                return mWizard.getSettings().isCreateUninstaller();
            }

            public void enabled(Control control, boolean flag) { }
        };

        final Button showUninstDetails = NSISWizardDialogUtil.createCheckBox(miscUninstGroup, "show.uninstaller.details.label", //$NON-NLS-1$
                              settings.isShowUninstDetails(),true, m, false);
        GridData data = (GridData)showUninstDetails.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        showUninstDetails.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setShowUninstDetails(((Button)e.widget).getSelection());
            }
        });

        final Button autocloseUninst = NSISWizardDialogUtil.createCheckBox(miscUninstGroup, "autoclose.uninstaller.label", //$NON-NLS-1$
                settings.isAutoCloseUninstaller(),true, m, false);
        data = (GridData)autocloseUninst.getLayoutData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        autocloseUninst.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setAutoCloseUninstaller(((Button)e.widget).getSelection());
            }
        });
        m.setEnabler(showUninstDetails, enabler);
        m.setEnabler(autocloseUninst, enabler);
        m.updateSlaves();

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                NSISWizardSettings settings = mWizard.getSettings();
                silentUninst.setEnabled(settings.isCreateUninstaller());
                m.updateSlaves();
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                silentUninst.setEnabled(newSettings.isCreateUninstaller());
                silentUninst.setSelection(newSettings.isSilentUninstaller());
                showUninstDetails.setSelection(newSettings.isShowUninstDetails());
                autocloseUninst.setSelection(newSettings.isAutoCloseUninstaller());
                m.updateSlaves();
            }
        });
    }
}
