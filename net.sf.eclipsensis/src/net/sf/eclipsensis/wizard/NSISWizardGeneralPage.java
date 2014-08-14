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

import java.util.ResourceBundle;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardGeneralPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardGeneral"; //$NON-NLS-1$

    private static final int APPNAME_CHECK=0x1;
    private static final int PUBURL_CHECK=0x10;
    private static final int INSTFILE_CHECK=0x100;
    private static final int INSTICON_CHECK=0x1000;
    private static final int UNINSTFILE_CHECK=0x10000;
    private static final int UNINSTICON_CHECK=0x100000;

    private static String[] cInstallFileErrors = {"empty.installer.file.error"}; //$NON-NLS-1$
    private static String[] cUninstallFileErrors = {"empty.uninstaller.file.error"}; //$NON-NLS-1$


    /**
     * @param pageName
     * @param title
     */
    public NSISWizardGeneralPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.general.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.general.description")); //$NON-NLS-1$
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

    @Override
    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();

            boolean b = ((flag & APPNAME_CHECK) == 0 || validateAppName()) &&
                   ((flag & PUBURL_CHECK) == 0 || validateEmptyOrValidURL(settings.getUrl(),null)) &&
                   ((flag & INSTFILE_CHECK) == 0 || validatePathName(IOUtility.decodePath(settings.getOutFile()),cInstallFileErrors)) &&
                   ((flag & INSTICON_CHECK) == 0 || validateEmptyOrValidFile(IOUtility.decodePath(settings.getIcon()),null)) &&
                   ((flag & UNINSTFILE_CHECK) == 0 || !settings.isCreateUninstaller() || validateFileName(IOUtility.decodePath(settings.getUninstallFile()),cUninstallFileErrors)) &&
                   ((flag & UNINSTICON_CHECK) == 0 || !settings.isCreateUninstaller() || validateEmptyOrValidFile(IOUtility.decodePath(settings.getUninstallIcon()),null));
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizgeneral_context"; //$NON-NLS-1$
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        createApplicationGroup(composite);
        createInstallerGroup(composite);
        createUninstallerGroup(composite);

        validatePage(VALIDATE_ALL);

        return composite;
    }

    private void createApplicationGroup(Composite parent)
    {
        Group appGroup = NSISWizardDialogUtil.createGroup(parent, 2, "application.group.label", null, true); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();
        final String programFiles =NSISKeywords.getInstance().getKeyword("$PROGRAMFILES"); //$NON-NLS-1$

        final Text appName = NSISWizardDialogUtil.createText(appGroup, settings.getName(), "application.name.label", true, null, isScriptWizard()); //$NON-NLS-1$
        appName.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               NSISWizardSettings settings = mWizard.getSettings();

               String newName = ((Text)e.widget).getText();
               String installDir = settings.getInstallDir();
               if(Common.isEmpty(installDir) || installDir.equalsIgnoreCase(new StringBuffer(programFiles).append("\\").append(settings.getName()).toString())) { //$NON-NLS-1$
                   settings.setInstallDir(new StringBuffer(programFiles).append("\\").append(newName).toString()); //$NON-NLS-1$
               }
               String startMenuGroup = settings.getStartMenuGroup();
               if(Common.isEmpty(startMenuGroup) || startMenuGroup.equalsIgnoreCase(settings.getName())) {
                   settings.setStartMenuGroup(newName);
               }
               settings.setName(newName);
               validateField(APPNAME_CHECK);
           }
        });

        final Text appVer = NSISWizardDialogUtil.createText(appGroup, settings.getVersion(), "application.version.label", true, null, false); //$NON-NLS-1$
        appVer.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setVersion(((Text)e.widget).getText());
           }
        });

        final Text pubName = NSISWizardDialogUtil.createText(appGroup, settings.getCompany(), "publisher.name.label", true, null, false); //$NON-NLS-1$
        pubName.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setCompany(((Text)e.widget).getText());
           }
        });

        final Text pubUrl = NSISWizardDialogUtil.createText(appGroup, settings.getUrl(), "publisher.url.label", true, null, false); //$NON-NLS-1$
        pubUrl.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setUrl(((Text)e.widget).getText());
               validateField(PUBURL_CHECK);
           }
        });

        final Combo targetPlatform;
        if(INSISVersions.VERSION_2_26.compareTo(NSISPreferences.getInstance().getNSISVersion()) <= 0) {
            targetPlatform = NSISWizardDialogUtil.createCombo(appGroup,NSISWizardDisplayValues.TARGET_PLATFORMS,
                        settings.getTargetPlatform(), true, "target.platform.label", true, null, false);  //$NON-NLS-1$
            targetPlatform.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mWizard.getSettings().setTargetPlatform(targetPlatform.getSelectionIndex());
                }
            });
        }
        else {
            targetPlatform = null;
            settings.setTargetPlatform(TARGET_PLATFORM_ANY);
        }

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                appName.setText(newSettings.getName());
                appVer.setText(newSettings.getVersion());
                pubName.setText(newSettings.getCompany());
                pubUrl.setText(newSettings.getUrl());


                if(targetPlatform != null && newSettings.getTargetPlatform() < targetPlatform.getItemCount()) {
                    targetPlatform.select(newSettings.getTargetPlatform());
                }
                else {
                    newSettings.setTargetPlatform(TARGET_PLATFORM_ANY);
                }
            }});
    }

    private void createInstallerGroup(final Composite parent)
    {
        final Group instGroup = NSISWizardDialogUtil.createGroup(parent, 3, "installer.group.label", null, true); //$NON-NLS-1$
        NSISWizardSettings settings = mWizard.getSettings();

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Text instFile = NSISWizardDialogUtil.createFileBrowser(instGroup, settings.getOutFile(), true,
                                   Common.loadArrayProperty(bundle,"installer.file.filternames"),  //$NON-NLS-1$
                                   Common.loadArrayProperty(bundle,"installer.file.filters"), "installer.file.label", //$NON-NLS-1$ //$NON-NLS-2$
                                   true, null,isScriptWizard());
        instFile.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setOutFile(((Text)e.widget).getText());
               validateField(INSTFILE_CHECK);
           }
        });

        final Text instIcon = NSISWizardDialogUtil.createImageBrowser(instGroup, settings.getIcon(), new Point(32,32),
                                          Common.loadArrayProperty(bundle,"installer.icon.filternames"),  //$NON-NLS-1$
                                          Common.loadArrayProperty(bundle,"installer.icon.filters"), "installer.icon.label", //$NON-NLS-1$ //$NON-NLS-2$
                                          true, null, false);
        instIcon.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setIcon(instIcon.getText());
               validateField(INSTICON_CHECK);
           }
        });
        NSISWizardDialogUtil.loadImage(instIcon);

        String[] installerTypeNames;
        if(NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_34) >= 0) {
            installerTypeNames = NSISWizardDisplayValues.INSTALLER_TYPE_NAMES;
        }
        else {
            installerTypeNames = (String[])Common.subArray(NSISWizardDisplayValues.INSTALLER_TYPE_NAMES, 0,
                                    NSISWizardDisplayValues.INSTALLER_TYPE_NAMES.length-1);
        }
        if(settings.getInstallerType() >= installerTypeNames.length ) {
            settings.setInstallerType(installerTypeNames.length-1);
        }
        final Button[] instTypes = NSISWizardDialogUtil.createRadioGroup(instGroup, installerTypeNames,
                                           settings.getInstallerType(),"installer.type.label", //$NON-NLS-1$
                                           true, null,false);
        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Button button = (Button)e.widget;
                if(button.getSelection()) {
                    Integer index = (Integer)button.getData();
                    mWizard.getSettings().setInstallerType(index.intValue());
                }
            }
        };
        for (int i = 0; i < instTypes.length; i++) {
            instTypes[i].addSelectionListener(sa);
        }

        final Button solidCompressor;
        NSISWizardDialogUtil.createLabel(instGroup,"compressor.label", true, null, false); //$NON-NLS-1$
        Composite composite = new Composite(instGroup,SWT.NONE);
        GridData gridData = new GridData(SWT.FILL,SWT.CENTER,true,false);
        gridData.horizontalSpan = 2;
        composite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Combo compressorType = NSISWizardDialogUtil.createCombo(composite, 2,
                        NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES,
                        NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES[settings.getCompressorType()],
                        true, true, null);

        String solidKeyword = NSISKeywords.getInstance().getKeyword("/SOLID"); //$NON-NLS-1$
        if(NSISKeywords.getInstance().isValidKeyword(solidKeyword)) {
            ((GridData)compressorType.getLayoutData()).horizontalSpan = 1;
            int index = compressorType.getSelectionIndex();
            solidCompressor = NSISWizardDialogUtil.createCheckBox(composite,"solid.compression.text",settings.isSolidCompression(), //$NON-NLS-1$
                                                     (index >= 0 && index != MakeNSISRunner.COMPRESSOR_DEFAULT),
                                                     null,false);
            solidCompressor.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mWizard.getSettings().setSolidCompression(((Button)e.widget).getSelection());
                }
            });
            ((GridData)solidCompressor.getLayoutData()).horizontalSpan = 1;
        }
        else {
            solidCompressor = null;
        }

        final Combo execLevel;
        if(INSISVersions.VERSION_2_21.compareTo(NSISPreferences.getInstance().getNSISVersion()) <= 0) {
            String[] execLevels = NSISWizardDisplayValues.EXECUTION_LEVELS;
            if(INSISVersions.VERSION_2_22.compareTo(NSISPreferences.getInstance().getNSISVersion()) > 0) {
                execLevels = (String[])Common.subArray(execLevels,0,execLevels.length - 1);
            }

            execLevel = NSISWizardDialogUtil.createCombo(instGroup,execLevels,settings.getExecutionLevel(),
                        true, "execution.level.label", true, null, false);  //$NON-NLS-1$
            execLevel.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mWizard.getSettings().setExecutionLevel(execLevel.getSelectionIndex());
                }
            });
        }
        else {
            execLevel = null;
            settings.setExecutionLevel(EXECUTION_LEVEL_NONE);
        }

        compressorType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                int index = ((Combo)e.widget).getSelectionIndex();
                mWizard.getSettings().setCompressorType(index);
                if(solidCompressor != null) {
                    solidCompressor.setEnabled(index >= 0 && index != MakeNSISRunner.COMPRESSOR_DEFAULT);
                }
            }
        });

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if(isCurrentPage()) {
                    if(execLevel != null) {
                        boolean visible = !mWizard.getSettings().isMultiUserInstallation();
                        if(visible != execLevel.isVisible()) {
                            execLevel.setVisible(visible);
                            ((GridData)execLevel.getLayoutData()).exclude = !visible;
                            Label l = (Label)execLevel.getData(NSISWizardDialogUtil.LABEL);
                            if(l != null) {
                                l.setVisible(visible);
                                ((GridData)l.getLayoutData()).exclude = !visible;
                            }
                            parent.layout(true);
                        }
                    }
                }
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                instFile.setText(newSettings.getOutFile());
                instIcon.setText(newSettings.getIcon());
                int n = newSettings.getInstallerType();
                if(!Common.isEmptyArray(instTypes)) {
                    for (int i = 0; i < instTypes.length; i++) {
                        instTypes[i].setSelection((i == n));
                    }
                }
                n = newSettings.getCompressorType();
                if(n >= 0 && n < NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES.length) {
                    compressorType.setText(NSISWizardDisplayValues.COMPRESSOR_TYPE_NAMES[n]);
                }
                else {
                    compressorType.clearSelection();
                    compressorType.setText(""); //$NON-NLS-1$
                }
                if(solidCompressor != null) {
                    solidCompressor.setSelection(newSettings.isSolidCompression());
                    int index = compressorType.getSelectionIndex();
                    solidCompressor.setEnabled(index >= 0 && index != MakeNSISRunner.COMPRESSOR_DEFAULT);
                }

                if(execLevel != null && newSettings.getExecutionLevel() < execLevel.getItemCount()) {
                    execLevel.select(newSettings.getExecutionLevel());
                }
                else {
                    newSettings.setExecutionLevel(EXECUTION_LEVEL_NONE);
                }
            }
        });
    }

    private void createUninstallerGroup(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group uninstGroup = NSISWizardDialogUtil.createGroup(parent, 3, "uninstaller.group.label", null, true); //$NON-NLS-1$
        final Button createUninst = NSISWizardDialogUtil.createCheckBox(uninstGroup,"create.uninstaller.label",settings.isCreateUninstaller(), //$NON-NLS-1$
                                        true, null, false);
        createUninst.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = createUninst.getSelection();
                mWizard.getSettings().setCreateUninstaller(selection);
                validatePage(VALIDATE_ALL);
            }
        });

        final MasterSlaveController m = new MasterSlaveController(createUninst);

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        final Text uninstFile = NSISWizardDialogUtil.createText(uninstGroup, settings.getUninstallFile(), "uninstaller.file.label", true, //$NON-NLS-1$
                            m,isScriptWizard());
        uninstFile.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setUninstallFile(((Text)e.widget).getText());
               validateField(UNINSTFILE_CHECK);
           }
        });

        final Text uninstIcon = NSISWizardDialogUtil.createImageBrowser(uninstGroup, settings.getUninstallIcon(), new Point(32,32),
                                          Common.loadArrayProperty(bundle,"uninstaller.icon.filternames"),  //$NON-NLS-1$
                                          Common.loadArrayProperty(bundle,"uninstaller.icon.filters"), "uninstaller.icon.label", //$NON-NLS-1$ //$NON-NLS-2$
                                          true, m, false);
        uninstIcon.addModifyListener(new ModifyListener(){
           public void modifyText(ModifyEvent e)
           {
               mWizard.getSettings().setUninstallIcon(uninstIcon.getText());
               validateField(UNINSTICON_CHECK);
           }
        });
        NSISWizardDialogUtil.loadImage(uninstIcon);

        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                createUninst.setSelection(newSettings.isCreateUninstaller());
                uninstFile.setText(newSettings.getUninstallFile());
                uninstIcon.setText(newSettings.getUninstallIcon());
                m.updateSlaves();
            }});
    }

    private boolean validateAppName()
    {
        if(!Common.isEmpty(mWizard.getSettings().getName())) {
            return true;
        }
        else {
            setErrorMessage(EclipseNSISPlugin.getResourceString("application.name.error")); //$NON-NLS-1$
            return false;
        }
    }
}
