/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.beans.*;
import java.io.File;
import java.text.Collator;
import java.util.Locale;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardAttributesPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardAttributes"; //$NON-NLS-1$

    private static final int INSTDIR_CHECK = 0x1;
    private static final int SMGRP_CHECK = 0x10;
    private static final int LANG_CHECK = 0x100;
    private static final int MULTIUSER_CHECK = 0x1000;
    private static String[] cInstallDirErrors = { "empty.installation.directory.error" }; //$NON-NLS-1$

    private static Collator cLanguageCollator = Collator.getInstance(Locale.US);

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardAttributesPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.attributes.title"), //$NON-NLS-1$
                EclipseNSISPlugin.getResourceString("wizard.attributes.description")); //$NON-NLS-1$
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return isScriptWizard();
    }

    private boolean validateField(int flag)
    {
        if (validatePage(flag))
        {
            return validatePage(VALIDATE_ALL & ~flag);
        }
        else
        {
            return false;
        }
    }

    private boolean validateStartMenuGroup()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if (settings.isCreateStartMenuGroup())
        {
            String startMenuGroup = settings.getStartMenuGroup();
            String[] parts = Common.tokenize(startMenuGroup, File.separatorChar);
            if (Common.isEmptyArray(parts))
            {
                setErrorMessage(EclipseNSISPlugin.getFormattedString(
                        "invalid.start.menu.group.error", new String[] { startMenuGroup })); //$NON-NLS-1$
                return false;
            }
            for (int i = 0; i < parts.length; i++)
            {
                if (!IOUtility.isValidFileName(parts[i]))
                {
                    setErrorMessage(EclipseNSISPlugin.getFormattedString(
                            "invalid.start.menu.group.error", new String[] { startMenuGroup })); //$NON-NLS-1$
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateMultiUserInstallation()
    {
        return true;
    }

    private boolean validateLanguages()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if (settings.isEnableLanguageSupport() && settings.getLanguages().size() == 0)
        {
            setErrorMessage(EclipseNSISPlugin.getResourceString("invalid.languages.error")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private boolean isMultiUser()
    {
        NSISWizardSettings settings = mWizard.getSettings();
        return settings.getInstallerType() == INSTALLER_TYPE_MUI2 && settings.isMultiUserInstallation();
    }

    @Override
    public boolean validatePage(int flag)
    {
        if (isTemplateWizard())
        {
            return true;
        }
        else
        {
            String dir = mWizard.getSettings().getInstallDir();
            boolean b = ((flag & INSTDIR_CHECK) == 0 || (isMultiUser() ? validateFolderName(dir, cInstallDirErrors)
                    : validateNSISPathName(dir, cInstallDirErrors)))
                    && ((flag & SMGRP_CHECK) == 0 || validateStartMenuGroup())
                    && ((flag & LANG_CHECK) == 0 || validateLanguages())
                    && ((flag & MULTIUSER_CHECK) == 0 || validateMultiUserInstallation());
            setPageComplete(b);
            if (b)
            {
                setErrorMessage(null);
            }
            return b;
        }
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_wizattrib_context"; //$NON-NLS-1$
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        createInstallationDirectoryGroup(composite);
        createStartMenuGroupGroup(composite);

        if (INSISVersions.VERSION_2_35.compareTo(NSISPreferences.getInstance().getNSISVersion()) <= 0)
        {
            createMultiUserGroup(composite);
        }
        else
        {
            mWizard.getSettings().setMultiUserInstallation(false);
        }

        createLanguagesGroup(composite);

        validatePage(VALIDATE_ALL);
        return composite;
    }

    private void createMultiUserGroup(Composite parent)
    {
        final Group multiUserGroup = NSISWizardDialogUtil.createGroup(parent, 2, "MultiUser Installation", null, false); //$NON-NLS-1$
        GridData data = (GridData) multiUserGroup.getLayoutData();
        data.verticalAlignment = SWT.FILL;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        NSISWizardSettings settings = mWizard.getSettings();

        Composite composite = new Composite(multiUserGroup, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Button multiUser = NSISWizardDialogUtil.createCheckBox(composite, EclipseNSISPlugin
                .getResourceString("enable.multiuser.label"), //$NON-NLS-1$
                settings.isMultiUserInstallation(), settings.getInstallerType() == INSTALLER_TYPE_MUI2, null, false);
        multiUser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = multiUser.getSelection();
                mWizard.getSettings().setMultiUserInstallation(selection);
                validateField(MULTIUSER_CHECK);
            }
        });

        final MasterSlaveController m = new MasterSlaveController(multiUser);

        boolean enabled = multiUser.isEnabled() && multiUser.getSelection();

        final Combo execLevel = NSISWizardDialogUtil.createCombo(composite,
                NSISWizardDisplayValues.MULTIUSER_EXEC_LEVELS, settings.getMultiUserExecLevel(), true,
                EclipseNSISPlugin.getResourceString("multiuser.exec.level.label"), //$NON-NLS-1$
                enabled, m, false);
        ((GridData) execLevel.getLayoutData()).horizontalAlignment = SWT.FILL;
        ((GridData) execLevel.getLayoutData()).grabExcessHorizontalSpace = true;

        final Group instModeGroup = NSISWizardDialogUtil.createGroup(multiUserGroup, 1, "Installation Mode", m, false); //$NON-NLS-1$
        data = (GridData) instModeGroup.getLayoutData();
        data.verticalAlignment = SWT.FILL;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;
        data.horizontalSpan = 1;

        MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag)
            {
            }

            public boolean canEnable(Control c)
            {
                return isMultiUser();
            }
        };
        m.addSlave(execLevel, mse);

        enabled = enabled && execLevel.getSelectionIndex() != MULTIUSER_EXEC_LEVEL_STANDARD;

        mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag)
            {
            }

            public boolean canEnable(Control c)
            {
                return isMultiUser() && mWizard.getSettings().getMultiUserExecLevel() != MULTIUSER_EXEC_LEVEL_STANDARD;
            }
        };

        execLevel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMultiUserExecLevel(execLevel.getSelectionIndex());
                m.updateSlaves();
                validateField(MULTIUSER_CHECK);
            }
        });

        final Combo instMode = NSISWizardDialogUtil.createCombo(instModeGroup,
                NSISWizardDisplayValues.MULTIUSER_INSTALL_MODES, settings.getMultiUserInstallMode(), true,
                EclipseNSISPlugin.getResourceString("multiuser.install.mode.label"), enabled, m, false); //$NON-NLS-1$
        ((GridData) instMode.getLayoutData()).grabExcessHorizontalSpace = true;
        ((GridData) instMode.getLayoutData()).horizontalAlignment = SWT.FILL;
        instMode.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMultiUserInstallMode(instMode.getSelectionIndex());
                validateField(MULTIUSER_CHECK);
            }
        });
        m.addSlave(instMode, mse);

        composite = new Composite(instModeGroup, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);

        layout = new GridLayout(2, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Button instModeRemember = NSISWizardDialogUtil.createCheckBox(composite, EclipseNSISPlugin
                .getResourceString("multiuser.install.mode.remember.label"), settings.isMultiUserInstallModeRemember(), //$NON-NLS-1$
                enabled, m, false);
        ((GridData) instModeRemember.getLayoutData()).horizontalSpan = 1;
        instModeRemember.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMultiUserInstallModeRemember(instModeRemember.getSelection());
                validateField(MULTIUSER_CHECK);
            }
        });
        m.addSlave(instModeRemember, mse);

        final Button instModeAsk = NSISWizardDialogUtil.createCheckBox(composite, EclipseNSISPlugin
                .getResourceString("multiuser.install.mode.ask.label"), settings.isMultiUserInstallModeAsk(), //$NON-NLS-1$
                enabled, m, false);
        ((GridData) instModeAsk.getLayoutData()).horizontalSpan = 1;
        instModeAsk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setMultiUserInstallModeAsk(instModeAsk.getSelection());
                validateField(MULTIUSER_CHECK);
            }
        });
        m.addSlave(instModeAsk, mse);

        m.updateSlaves();
        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if (isCurrentPage())
                {
                    NSISWizardDialogUtil.setEnabled(multiUser,
                            mWizard.getSettings().getInstallerType() == INSTALLER_TYPE_MUI2);
                    m.updateSlaves();
                }
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                multiUser.setSelection(settings.isMultiUserInstallation());
                execLevel.select(settings.getMultiUserExecLevel());
                instMode.select(settings.getMultiUserInstallMode());
                instModeRemember.setSelection(settings.isMultiUserInstallModeRemember());
                instModeAsk.setSelection(settings.isMultiUserInstallModeAsk());
                NSISWizardDialogUtil.setEnabled(multiUser, settings.getInstallerType() == INSTALLER_TYPE_MUI2);
                m.updateSlaves();
            }
        });
    }

    private void createLanguagesGroup(Composite parent)
    {
        final Group langGroup = NSISWizardDialogUtil
        .createGroup(parent, 1, "language.support.group.label", null, false); //$NON-NLS-1$
        GridData data = (GridData) langGroup.getLayoutData();
        data.verticalAlignment = SWT.FILL;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        NSISWizardSettings settings = mWizard.getSettings();

        final Button enableLangSupport = NSISWizardDialogUtil.createCheckBox(langGroup,
                "enable.language.support.label", settings.isEnableLanguageSupport(), true, null, false); //$NON-NLS-1$
        enableLangSupport.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = enableLangSupport.getSelection();
                mWizard.getSettings().setEnableLanguageSupport(selection);
                validateField(LANG_CHECK);
            }
        });

        final MasterSlaveController m = new MasterSlaveController(enableLangSupport);

        final Composite listsComposite = new Composite(langGroup, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        listsComposite.setLayoutData(data);

        GridLayout layout = new GridLayout(2, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        listsComposite.setLayout(layout);

        java.util.List<NSISLanguage> selectedLanguages = settings.getLanguages();
        if (selectedLanguages.isEmpty())
        {
            NSISLanguage defaultLanguage = NSISLanguageManager.getInstance().getDefaultLanguage();
            if (defaultLanguage != null)
            {
                selectedLanguages.add(defaultLanguage);
            }
        }
        java.util.List<NSISLanguage> availableLanguages = NSISLanguageManager.getInstance().getLanguages();
        availableLanguages.removeAll(selectedLanguages);

        Composite leftComposite = new Composite(listsComposite, SWT.NONE);
        leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        leftComposite.setLayout(layout);

        ((GridData) NSISWizardDialogUtil.createLabel(leftComposite, "available.languages.label", //$NON-NLS-1$
                true, m, false).getLayoutData()).horizontalSpan = 2;

        final List availableLangList = new List(leftComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        Dialog.applyDialogFont(availableLangList);
        data.heightHint = Common.calculateControlSize(availableLangList, 0, 12).y;
        availableLangList.setLayoutData(data);
        m.addSlave(availableLangList);

        final ListViewer availableLangViewer = new ListViewer(availableLangList);
        CollectionContentProvider collectionContentProvider = new CollectionContentProvider();
        availableLangViewer.setContentProvider(collectionContentProvider);
        availableLangViewer.setInput(availableLanguages);
        availableLangViewer.setSorter(new ViewerSorter(cLanguageCollator));

        final Composite buttonsComposite1 = new Composite(leftComposite, SWT.NONE);
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttonsComposite1.setLayout(layout);
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonsComposite1.setLayoutData(data);

        final Button allRightButton = new Button(buttonsComposite1, SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        allRightButton.setLayoutData(data);
        allRightButton.setImage(EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("all.right.icon"))); //$NON-NLS-1$
        allRightButton.setToolTipText(EclipseNSISPlugin.getResourceString("all.right.tooltip")); //$NON-NLS-1$

        final Button rightButton = new Button(buttonsComposite1, SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        rightButton.setLayoutData(data);
        rightButton.setImage(EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("right.icon"))); //$NON-NLS-1$
        rightButton.setToolTipText(EclipseNSISPlugin.getResourceString("right.tooltip")); //$NON-NLS-1$

        final Button leftButton = new Button(buttonsComposite1, SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        leftButton.setLayoutData(data);
        leftButton.setImage(EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("left.icon"))); //$NON-NLS-1$
        leftButton.setToolTipText(EclipseNSISPlugin.getResourceString("left.tooltip")); //$NON-NLS-1$

        final Button allLeftButton = new Button(buttonsComposite1, SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        allLeftButton.setLayoutData(data);
        allLeftButton.setImage(EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("all.left.icon"))); //$NON-NLS-1$
        allLeftButton.setToolTipText(EclipseNSISPlugin.getResourceString("all.left.tooltip")); //$NON-NLS-1$

        Composite rightComposite = new Composite(listsComposite, SWT.NONE);
        rightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        rightComposite.setLayout(layout);

        ((GridData) NSISWizardDialogUtil.createLabel(rightComposite, "selected.languages.label", //$NON-NLS-1$
                true, m, isScriptWizard()).getLayoutData()).horizontalSpan = 2;

        final List selectedLangList = new List(rightComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        Dialog.applyDialogFont(selectedLangList);
        data.heightHint = Common.calculateControlSize(selectedLangList, 0, 12).y;
        selectedLangList.setLayoutData(data);
        m.addSlave(selectedLangList);

        final ListViewer selectedLangViewer = new ListViewer(selectedLangList);
        selectedLangViewer.setContentProvider(collectionContentProvider);
        selectedLangViewer.setInput(selectedLanguages);

        final ListViewerUpDownMover<java.util.List<NSISLanguage>, NSISLanguage> mover = new ListViewerUpDownMover<java.util.List<NSISLanguage>, NSISLanguage>() {
            @Override
            @SuppressWarnings("unchecked")
            protected java.util.List<NSISLanguage> getAllElements()
            {
                return (java.util.List<NSISLanguage>) ((ListViewer) getViewer()).getInput();
            }

            @Override
            protected void updateStructuredViewerInput(java.util.List<NSISLanguage> input, java.util.List<NSISLanguage> elements, java.util.List<NSISLanguage> move,
                    boolean isDown)
            {
                (input).clear();
                (input).addAll(elements);
            }
        };

        mover.setViewer(selectedLangViewer);

        final Composite buttonsComposite2 = new Composite(rightComposite, SWT.NONE);
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttonsComposite2.setLayout(layout);
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        buttonsComposite2.setLayoutData(data);

        final Button upButton = new Button(buttonsComposite2, SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        upButton.setLayoutData(data);
        upButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        upButton.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        m.addSlave(upButton);

        final Button downButton = new Button(buttonsComposite2, SWT.PUSH);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        downButton.setLayoutData(data);
        downButton.setImage(EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
        downButton.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        m.addSlave(downButton);

        Composite langOptions = langGroup;
        boolean showSupportedLangOption = NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_26) >= 0;
        if(showSupportedLangOption)
        {
            langOptions = new Composite(langGroup, SWT.None);
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            langOptions.setLayout(layout);
            data = new GridData(SWT.FILL, SWT.FILL, true, false);
            langOptions.setLayoutData(data);
        }

        final Button selectLang = NSISWizardDialogUtil.createCheckBox(langOptions,
                "select.language.label", settings.isSelectLanguage(), true, m, false); //$NON-NLS-1$

        final Button displaySupported;
        if (showSupportedLangOption) {
            ((GridData)selectLang.getLayoutData()).horizontalSpan = 1;
            displaySupported = NSISWizardDialogUtil
                    .createCheckBox(
                            langOptions,
                            "display.supported.languages.label", settings.isDisplaySupportedLanguages(),  //$NON-NLS-1$
                            true, m, false);
            ((GridData)displaySupported.getLayoutData()).horizontalSpan = 1;
            displaySupported.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    mWizard.getSettings().setDisplaySupportedLanguages(
                            displaySupported.getSelection());
                }
            });
        }
        else
        {
            displaySupported = null;
        }

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag)
            {
            }

            @SuppressWarnings("unchecked")
            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if (control == allRightButton)
                {
                    return settings.isEnableLanguageSupport() && availableLangViewer.getList().getItemCount() > 0;
                }
                else if (control == rightButton)
                {
                    return settings.isEnableLanguageSupport() && !availableLangViewer.getSelection().isEmpty();
                }
                else if (control == allLeftButton)
                {
                    return settings.isEnableLanguageSupport() && selectedLangViewer.getList().getItemCount() > 0;
                }
                else if (control == leftButton)
                {
                    return settings.isEnableLanguageSupport() && !selectedLangViewer.getSelection().isEmpty();
                }
                else if (control == upButton)
                {
                    return settings.isEnableLanguageSupport() && mover.canMoveUp();
                }
                else if (control == downButton)
                {
                    return settings.isEnableLanguageSupport() && mover.canMoveDown();
                }
                else if (control == selectLang)
                {
                    java.util.List<NSISLanguage> selectedLanguages = (java.util.List<NSISLanguage>) selectedLangViewer.getInput();
                    return settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isEnableLanguageSupport()
                    && selectedLanguages.size() > 1;
                }
                else if (control == displaySupported && displaySupported != null)
                {
                    java.util.List<NSISLanguage> selectedLanguages = (java.util.List<NSISLanguage>) selectedLangViewer.getInput();
                    return settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isEnableLanguageSupport()
                    && selectedLanguages.size() > 1 && selectLang.getSelection();
                }
                else
                {
                    return true;
                }
            }
        };
        m.addSlave(rightButton, mse);
        m.addSlave(allRightButton, mse);
        m.addSlave(leftButton, mse);
        m.addSlave(allLeftButton, mse);
        m.setEnabler(upButton, mse);
        m.setEnabler(downButton, mse);
        m.setEnabler(selectLang, mse);
        if (displaySupported != null)
        {
            m.setEnabler(displaySupported, mse);
        }

        selectLang.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setSelectLanguage(selectLang.getSelection());
                if (displaySupported != null) {
                    displaySupported.setEnabled(mse.canEnable(displaySupported));
                }
            }
        });

        final Runnable langRunnable = new Runnable() {
            public void run()
            {
                availableLangViewer.refresh(false);
                selectedLangViewer.refresh(false);
                allRightButton.setEnabled(mse.canEnable(allRightButton));
                allLeftButton.setEnabled(mse.canEnable(allLeftButton));
                rightButton.setEnabled(mse.canEnable(rightButton));
                leftButton.setEnabled(mse.canEnable(leftButton));
                upButton.setEnabled(mse.canEnable(upButton));
                downButton.setEnabled(mse.canEnable(downButton));
                selectLang.setEnabled(mse.canEnable(selectLang));
                if (displaySupported != null) {
                    displaySupported.setEnabled(mse.canEnable(displaySupported));
                }
                setPageComplete(validateField(LANG_CHECK));
            }
        };

        rightButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                moveAcross(availableLangViewer, selectedLangViewer, Common.makeGenericList(NSISLanguage.class,((IStructuredSelection) availableLangViewer
                        .getSelection()).toList()));
                langRunnable.run();
            }
        });
        allRightButton.addSelectionListener(new SelectionAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent se)
            {
                moveAcross(availableLangViewer, selectedLangViewer, (java.util.List<NSISLanguage>) availableLangViewer.getInput());
                langRunnable.run();
            }
        });
        leftButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                moveAcross(selectedLangViewer, availableLangViewer, Common.makeGenericList(NSISLanguage.class,((IStructuredSelection) selectedLangViewer
                        .getSelection()).toList()));
                langRunnable.run();
            }
        });
        allLeftButton.addSelectionListener(new SelectionAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void widgetSelected(SelectionEvent se)
            {
                moveAcross(selectedLangViewer, availableLangViewer, (java.util.List<NSISLanguage>) selectedLangViewer.getInput());
                langRunnable.run();
            }
        });
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                mover.moveUp();
                langRunnable.run();
            }
        });
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                mover.moveDown();
                langRunnable.run();
            }
        });

        availableLangViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                rightButton.setEnabled(mse.canEnable(rightButton));
                allRightButton.setEnabled(mse.canEnable(allRightButton));
            }
        });
        availableLangViewer.getList().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection) availableLangViewer.getSelection();
                if (!sel.isEmpty())
                {
                    moveAcross(availableLangViewer, selectedLangViewer, Common.makeGenericList(NSISLanguage.class,sel.toList()));
                    selectedLangViewer.reveal(sel.getFirstElement());
                    langRunnable.run();
                }
            }
        });
        selectedLangViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                leftButton.setEnabled(mse.canEnable(leftButton));
                allLeftButton.setEnabled(mse.canEnable(allLeftButton));
                upButton.setEnabled(mse.canEnable(upButton));
                downButton.setEnabled(mse.canEnable(downButton));
                selectLang.setEnabled(mse.canEnable(selectLang));
                if (displaySupported != null)
                {
                    displaySupported
                            .setEnabled(mse.canEnable(displaySupported));
                }
            }
        });
        selectedLangViewer.getList().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection) selectedLangViewer.getSelection();
                if (!sel.isEmpty())
                {
                    moveAcross(selectedLangViewer, availableLangViewer, Common.makeGenericList(NSISLanguage.class,sel.toList()));
                    availableLangViewer.reveal(sel.getFirstElement());
                    langRunnable.run();
                }
            }
        });

        m.updateSlaves();

        listsComposite.addListener(SWT.Resize, new Listener() {
            boolean init = false;

            public void handleEvent(Event e)
            {
                if (!init)
                {
                    // Stupid hack so that the height hint doesn't get changed
                    // on the first resize,
                    // i.e., when the page is drawn for the first time.
                    init = true;
                }
                else
                {
                    Point size = listsComposite.getSize();
                    GridLayout layout = (GridLayout) listsComposite.getLayout();
                    int heightHint = size.y - 2 * layout.marginHeight;
                    ((GridData) availableLangList.getLayoutData()).heightHint = heightHint;
                    ((GridData) selectedLangList.getLayoutData()).heightHint = heightHint;
                    int totalWidth = size.x - 2 * layout.marginWidth - 3 * layout.horizontalSpacing;
                    int listWidth = (int) (totalWidth * 0.4);
                    int buttonWidth = (int) (0.5 * (totalWidth - 2 * listWidth));
                    size = availableLangList.computeSize(listWidth, SWT.DEFAULT);
                    int delta = 0;
                    if (size.x > listWidth)
                    {
                        delta = size.x - listWidth;
                        listWidth = listWidth - delta;
                    }
                    ((GridData) availableLangList.getLayoutData()).widthHint = listWidth;
                    ((GridData) buttonsComposite1.getLayoutData()).widthHint = totalWidth - 2 * (listWidth + delta)
                    - buttonWidth;
                    ((GridData) selectedLangList.getLayoutData()).widthHint = listWidth;
                    ((GridData) buttonsComposite2.getLayoutData()).widthHint = buttonWidth;
                    listsComposite.layout();
                }
            }
        });

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if (isCurrentPage())
                {
                    selectLang.setEnabled(mse.canEnable(selectLang));
                    if (displaySupported != null)
                    {
                        displaySupported
                                .setEnabled(mse.canEnable(displaySupported));
                    }
                }
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                enableLangSupport.setSelection(newSettings.isEnableLanguageSupport());
                m.updateSlaves();
                selectLang.setSelection(newSettings.isSelectLanguage());
                if (displaySupported != null)
                {
                    displaySupported.setSelection(newSettings.isDisplaySupportedLanguages());
                }
                java.util.List<NSISLanguage> selectedLanguages = newSettings.getLanguages();
                java.util.List<NSISLanguage> availableLanguages = NSISLanguageManager.getInstance().getLanguages();
                if (selectedLanguages.isEmpty())
                {
                    NSISWizardWelcomePage welcomePage = (NSISWizardWelcomePage) mWizard
                    .getPage(NSISWizardWelcomePage.NAME);
                    if (welcomePage != null)
                    {
                        if (!welcomePage.isCreateFromTemplate())
                        {
                            NSISLanguage defaultLanguage = NSISLanguageManager.getInstance().getDefaultLanguage();
                            if (defaultLanguage != null && availableLanguages.contains(defaultLanguage))
                            {
                                selectedLanguages.add(defaultLanguage);
                            }
                        }
                    }
                }
                selectedLangViewer.setInput(selectedLanguages);
                availableLanguages.removeAll(selectedLanguages);
                availableLangViewer.setInput(availableLanguages);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void moveAcross(ListViewer fromLV, ListViewer toLV, java.util.List<NSISLanguage> move)
    {
        java.util.List<NSISLanguage> from = (java.util.List<NSISLanguage>) fromLV.getInput();
        java.util.List<NSISLanguage> to = (java.util.List<NSISLanguage>) toLV.getInput();
        to.addAll(move);
        from.removeAll(move);
        fromLV.refresh(false);
        toLV.refresh(false);
    }

    /**
     * @param composite
     */
    private void createInstallationDirectoryGroup(Composite parent)
    {
        Group instDirGroup = NSISWizardDialogUtil.createGroup(parent, 2,
                "installation.directory.group.label", null, false); //$NON-NLS-1$

        NSISWizardSettings settings = mWizard.getSettings();

        ((GridData) NSISWizardDialogUtil.createLabel(instDirGroup,
                "installation.directory.label", true, null, isScriptWizard()).getLayoutData()).horizontalSpan = 1; //$NON-NLS-1$

        final Composite instDirComposite = new Composite(instDirGroup, SWT.NONE);
        instDirComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final StackLayout instDirLayout = new StackLayout();
        instDirComposite.setLayout(instDirLayout);
        final Combo instDirCombo = NSISWizardDialogUtil.createCombo(instDirComposite, 1, NSISWizardUtil
                .getPathConstantsAndVariables(settings.getTargetPlatform()), settings.getInstallDir(), false, true,
                null);
        final Text instDirText = NSISWizardDialogUtil.createText(instDirComposite, settings.getInstallDir(), 1, true,
                null);
        instDirLayout.topControl = instDirCombo;

        Runnable r = new Runnable() {
            private String mInstDirParent = ""; //$NON-NLS-1$

            private void updateInstDir(NSISWizardSettings settings)
            {
                Control topControl;
                if (isMultiUser())
                {
                    if (settings.getInstallDir().startsWith(mInstDirParent))
                    {
                        String instDir = settings.getInstallDir().substring(mInstDirParent.length());
                        settings.setInstallDir(instDir);
                        instDirText.setText(instDir);
                    }
                    topControl = instDirText;
                }
                else
                {
                    if (!settings.getInstallDir().startsWith(mInstDirParent))
                    {
                        String instDir = mInstDirParent + settings.getInstallDir();
                        settings.setInstallDir(instDir);
                        instDirCombo.setText(instDir);
                    }
                    topControl = instDirCombo;
                }
                if (instDirLayout.topControl != topControl)
                {
                    instDirLayout.topControl = topControl;
                    instDirComposite.layout(true);
                }
                validateField(INSTDIR_CHECK);
            }

            public void run()
            {
                final PropertyChangeListener propertyListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if (NSISWizardSettings.INSTALLER_TYPE.equals(evt.getPropertyName())
                                || NSISWizardSettings.MULTIUSER_INSTALLATION.equals(evt.getPropertyName()))
                        {
                            updateInstDir(mWizard.getSettings());
                        }
                        else if (NSISWizardSettings.INSTALL_DIR.equals(evt.getPropertyName()))
                        {
                            if (!isMultiUser())
                            {
                                setInstDirParent(mWizard.getSettings());
                            }
                        }
                    }
                };
                final INSISWizardSettingsListener settingsListener = new INSISWizardSettingsListener() {
                    public void settingsChanged(NSISWizardSettings oldSettings, final NSISWizardSettings newSettings)
                    {
                        if (oldSettings != null)
                        {
                            oldSettings.removePropertyChangeListener(propertyListener);
                        }
                        setInstDirParent(newSettings);
                        if (newSettings != null)
                        {
                            newSettings.addPropertyChangeListener(propertyListener);
                        }
                        updateInstDir(newSettings);
                    }
                };
                mWizard.addSettingsListener(settingsListener);
                mWizard.getSettings().addPropertyChangeListener(propertyListener);
                instDirCombo.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        mWizard.getSettings().removePropertyChangeListener(propertyListener);
                        mWizard.removeSettingsListener(settingsListener);
                    }
                });
                setInstDirParent(mWizard.getSettings());
                updateInstDir(mWizard.getSettings());
            }

            private void setInstDirParent(NSISWizardSettings settings)
            {
                mInstDirParent = ""; //$NON-NLS-1$
                if (settings != null)
                {
                    String instDir = settings.getInstallDir();
                    if (!Common.isEmpty(instDir))
                    {
                        int n = instDir.lastIndexOf('\\');
                        if (n > 0 && n < instDir.length() - 1)
                        {
                            mInstDirParent = instDir.substring(0, n + 1);
                        }
                    }
                }
            }
        };

        r.run();

        GridData gd = (GridData) instDirCombo.getLayoutData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        instDirCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                String text = ((Combo) e.widget).getText();
                mWizard.getSettings().setInstallDir(text);
                validateField(INSTDIR_CHECK);
            }
        });
        instDirText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text) e.widget).getText();
                mWizard.getSettings().setInstallDir(text);
                validateField(INSTDIR_CHECK);
            }
        });

        final Button changeInstDir = NSISWizardDialogUtil.createCheckBox(instDirGroup,
                "change.installation.directory.label", //$NON-NLS-1$
                settings.isChangeInstallDir(), (settings.getInstallerType() != INSTALLER_TYPE_SILENT), null, false);
        changeInstDir.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setChangeInstallDir(((Button) e.widget).getSelection());
            }
        });

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                if (isCurrentPage())
                {
                    NSISWizardSettings settings = mWizard.getSettings();
                    instDirCombo.setText(settings.getInstallDir());
                    changeInstDir.setEnabled(settings.getInstallerType() != INSTALLER_TYPE_SILENT);
                }
            }
        });

        final PropertyChangeListener propertyListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (NSISWizardSettings.TARGET_PLATFORM.equals(evt.getPropertyName()))
                {
                    NSISWizardDialogUtil.populateCombo(instDirCombo, NSISWizardUtil
                            .getPathConstantsAndVariables(((Integer) evt.getNewValue()).intValue()),
                            ((NSISWizardSettings) evt.getSource()).getInstallDir());
                }
            }
        };
        settings.addPropertyChangeListener(propertyListener);
        final INSISWizardSettingsListener listener = new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                if (oldSettings != null)
                {
                    oldSettings.removePropertyChangeListener(propertyListener);
                }
                instDirCombo.setText(newSettings.getInstallDir());
                changeInstDir.setSelection(newSettings.isChangeInstallDir());
                changeInstDir.setEnabled(newSettings.getInstallerType() != INSTALLER_TYPE_SILENT);
                newSettings.addPropertyChangeListener(propertyListener);
            }
        };
        mWizard.addSettingsListener(listener);

        instDirGroup.addDisposeListener(new DisposeListener() {
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
    private void createStartMenuGroupGroup(Composite parent)
    {
        Group startMenuGroup = NSISWizardDialogUtil.createGroup(parent, 1, "startmenu.group.group.label", null, false); //$NON-NLS-1$

        NSISWizardSettings settings = mWizard.getSettings();

        final Button createStartMenu = NSISWizardDialogUtil.createCheckBox(startMenuGroup,
                "create.startmenu.group.label", settings.isCreateStartMenuGroup(), //$NON-NLS-1$
                true, null, false);
        createStartMenu.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = createStartMenu.getSelection();
                mWizard.getSettings().setCreateStartMenuGroup(selection);
                validateField(SMGRP_CHECK);
                NSISWizardContentsPage page = (NSISWizardContentsPage) mWizard.getPage(NSISWizardContentsPage.NAME);
                if (page != null)
                {
                    page.setPageComplete(page.validatePage(VALIDATE_ALL));
                    page.refresh();
                    mWizard.getContainer().updateButtons();
                }
            }
        });

        final MasterSlaveController m = new MasterSlaveController(createStartMenu);

        final Text startMenu = NSISWizardDialogUtil.createText(startMenuGroup, settings.getStartMenuGroup(),
                "startmenu.group.label", //$NON-NLS-1$
                true, m, isScriptWizard());
        startMenu.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text) e.widget).getText();
                mWizard.getSettings().setStartMenuGroup(text);
                validateField(SMGRP_CHECK);
            }
        });

        Composite composite = new Composite(startMenuGroup, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);

        final Button changeStartMenu = NSISWizardDialogUtil.createCheckBox(composite, "change.startmenu.group.label", //$NON-NLS-1$
                settings.isChangeStartMenuGroup(), (settings.getInstallerType() != INSTALLER_TYPE_SILENT), m, false);
        ((GridData) changeStartMenu.getLayoutData()).horizontalSpan = 1;
        changeStartMenu.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setChangeStartMenuGroup(((Button) e.widget).getSelection());
            }
        });
        final MasterSlaveController m2 = new MasterSlaveController(changeStartMenu);
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag)
            {
                m2.updateSlaves(flag);
            }

            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if (changeStartMenu == control)
                {
                    return settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isCreateStartMenuGroup();
                }
                else
                {
                    return true;
                }
            }
        };

        final Button disableShortcuts = NSISWizardDialogUtil.createCheckBox(composite,
                "disable.startmenu.shortcuts.label", //$NON-NLS-1$
                settings.isDisableStartMenuShortcuts(), changeStartMenu.isEnabled(), m2, false);
        ((GridData) disableShortcuts.getLayoutData()).horizontalSpan = 1;
        disableShortcuts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setDisableStartMenuShortcuts(((Button) e.widget).getSelection());
            }
        });

        m.setEnabler(changeStartMenu, mse);
        m.updateSlaves();

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                startMenu.setText(mWizard.getSettings().getStartMenuGroup());
                changeStartMenu.setEnabled(mse.canEnable(changeStartMenu));
                disableShortcuts.setEnabled(changeStartMenu.isEnabled());
            }
        });

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                createStartMenu.setSelection(newSettings.isCreateStartMenuGroup());
                startMenu.setText(newSettings.getStartMenuGroup());
                changeStartMenu.setSelection(newSettings.isChangeStartMenuGroup());
                changeStartMenu.setEnabled(newSettings.getInstallerType() != INSTALLER_TYPE_SILENT);
                disableShortcuts.setSelection(newSettings.isDisableStartMenuShortcuts());
                disableShortcuts.setEnabled(changeStartMenu.isEnabled());

                m.updateSlaves();
            }
        });
    }
}
