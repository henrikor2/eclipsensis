/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import java.io.File;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.FileSelectionDialog;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.*;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

class NSISGeneralTab extends NSISTab
{
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{[^\\r\\n]+\\}"); //$NON-NLS-1$
    private static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    private static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("nsis.script.filtername")}; //$NON-NLS-1$

    private boolean mBuilder = false;

    public NSISGeneralTab()
    {
        this(false);
    }

    public NSISGeneralTab(boolean builder)
    {
        super();
        mBuilder = builder;
        if(mBuilder) {
            mSettings.setRunInstaller(false);
        }
    }

    @Override
    protected NSISSettingsEditorPage createPage()
    {
        return new LaunchSettingsEditorGeneralPage(mSettings);
    }

    @Override
    protected IFilter createSettingsFilter()
    {
        return new IFilter() {
            public boolean select(Object toTest)
            {
                return !INSISSettingsConstants.SYMBOLS.equals(toTest);
            }
        };
    }

    @Override
    public Image getImage()
    {
        return EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("nsis.general.tab.icon")); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        if (mBuilder) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_buildconfig_general_context"); //$NON-NLS-1$
        }
        else {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_launchconfig_general_context"); //$NON-NLS-1$
        }
    }

    public String getName()
    {
        return EclipseNSISPlugin.getResourceString("launchconfig.general.tab.name"); //$NON-NLS-1$
    }

    @Override
    public boolean canSave()
    {
        return ((LaunchSettingsEditorGeneralPage)mPage).isValid();
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig)
    {
        if(super.isValid(launchConfig)) {
            return ((LaunchSettingsEditorGeneralPage)mPage).isValid();
        }
        return false;
    }

    @Override
    public void settingsChanged()
    {
        super.settingsChanged();
        if(!((LaunchSettingsEditorGeneralPage)mPage).isValid()) {
            setErrorMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
        }
        else {
            setErrorMessage(null);
        }
    }

    class LaunchSettingsEditorGeneralPage extends NSISSettingsEditorGeneralPage
    {
        private IStringVariableManager mStringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
        private Text mScript = null;
        private Button mRunInstaller = null;
        private boolean mHandlingScriptChange = false;
        private IFilter mFilter = new IFilter() {
            public boolean select(Object toTest)
            {
                if(toTest instanceof IFile) {
                    String ext = ((IFile)toTest).getFileExtension();
                    return (ext != null && ext.equalsIgnoreCase(INSISConstants.NSI_EXTENSION));
                }
                return false;
            }
        };

        public LaunchSettingsEditorGeneralPage(NSISSettings settings)
        {
            super(settings);
        }

        boolean validateScript(String script)
        {
            try {
                boolean valid = checkExternalFile(mStringVariableManager.performStringSubstitution(script)) != null;
                if(!valid) {
                    //Check for variables
                    if(VARIABLE_PATTERN.matcher(script).find()) {
                        return true;
                    }
                }
                return valid;
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
                return false;
            }
        }

        IFile checkWorkspaceFile(String script)
        {
            if(!Common.isEmpty(script)) {
                IPath path = new Path(script);
                if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(path.getFileExtension())) {
                    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
                    if(file != null && file.exists()) {
                        return file;
                    }
                }
            }
            return null;
        }

        File checkExternalFile(String script)
        {
            if(!Common.isEmpty(script)) {
                File file = new File(script);
                if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(IOUtility.getFileExtension(file)) &&
                   IOUtility.isValidFile(file) && file.isAbsolute()) {
                    return file;
                }
            }
            return null;
        }

        public boolean isValid()
        {
            return validateScript(mScript.getText());
        }

        @Override
        public void reset()
        {
            NSISLaunchSettings settings = (NSISLaunchSettings)mSettings;
            mScript.setText(settings.getScript());
            if(!mBuilder) {
                mRunInstaller.setSelection(settings.getRunInstaller());
            }
            super.reset();
        }

        boolean handleScriptChange()
        {
            if(!mHandlingScriptChange) {
                try {
                    mHandlingScriptChange = true;
                    boolean state = false;
                    String script = mScript.getText();
                    if(!Common.isEmpty(script)) {
                        if(validateScript(script)) {
                            state = true;
                            setErrorMessage(null);
                        }
                        else {
                            setErrorMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
                        }
                    }
                    else if(getErrorMessage() == null) {
                        setErrorMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
                    }
                    enableControls(state);
                    return state;
                }
                finally {
                    fireChanged();
                    mHandlingScriptChange = false;
                }
            }
            return true;
        }

        @Override
        public void enableControls(boolean state)
        {
            if (!mBuilder) {
                mRunInstaller.setEnabled(state);
            }
            super.enableControls(state);
        }

        @Override
        public void setDefaults()
        {
            super.setDefaults();
            mScript.setText(""); //$NON-NLS-1$
            if (!mBuilder) {
                mRunInstaller.setSelection(false);
            }
        }

        @Override
        protected boolean performApply(NSISSettings settings)
        {
            if(super.performApply(settings)) {
                if (getControl() != null) {
                    NSISLaunchSettings settings2 = (NSISLaunchSettings)settings;
                    settings2.setScript(mScript.getText());
                    if (!mBuilder) {
                        settings2.setRunInstaller(mRunInstaller.getSelection());
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean canEnableControls()
        {
            return !Common.isEmpty(mScript.getText());
        }

        @Override
        protected Composite createMasterControl(Composite parent)
        {
            Composite composite = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(2,false);
            layout.marginWidth = 0;
            composite.setLayout(layout);

            Label label = new Label(composite, SWT.LEFT);
            label.setText(EclipseNSISPlugin.getResourceString("launchconfig.nsis.script.label")); //$NON-NLS-1$
            GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            label.setLayoutData(data);

            mScript = new Text(composite, SWT.BORDER);
            data = new GridData(SWT.FILL, SWT.CENTER, true, false);
            mScript.setLayoutData(data);

            mScript.setText(((NSISLaunchSettings)mSettings).getScript());
            mScript.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    handleScriptChange();
                }
            });

            Composite buttons = new Composite(composite,SWT.NONE);
            GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
            gridData.horizontalSpan = 3;
            buttons.setLayoutData(gridData);
            layout = new GridLayout(3,true);
            layout.marginHeight=0;
            layout.marginWidth=0;
            buttons.setLayout(layout);
            final Button workspaceButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.workspace.label"), ""); //$NON-NLS-1$  //$NON-NLS-2$
            workspaceButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseWorkspace(workspaceButton.getShell());
                }
            });
            workspaceButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            final Button filesystemButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.filesystem.label"), ""); //$NON-NLS-1$ //$NON-NLS-2$
            filesystemButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseFilesystem(filesystemButton.getShell());
                }
            });
            filesystemButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
            final Button variablesButton = createButton(buttons, EclipseNSISPlugin.getResourceString("launchconfig.browse.variables.label"), ""); //$NON-NLS-1$  //$NON-NLS-2$
            variablesButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    handleBrowseVariables(variablesButton.getShell());
                }
            });
            variablesButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));

            if (!mBuilder) {
                mRunInstaller = createCheckBox(composite, EclipseNSISPlugin.getResourceString("launchconfig.run.installer.label"), "", ((NSISLaunchSettings)mSettings).getRunInstaller()); //$NON-NLS-1$ //$NON-NLS-2$
                gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
                gridData.horizontalSpan = 2;
                mRunInstaller.setLayoutData(gridData);
            }
            return composite;
        }

        void handleBrowseWorkspace(Shell shell)
        {
            IFile ifile;
            try {
                ifile = checkWorkspaceFile(mStringVariableManager.performStringSubstitution(mScript.getText()));
            }
            catch (CoreException e) {
                ifile = null;
                EclipseNSISPlugin.getDefault().log(e);
            }
            FileSelectionDialog dialog = new FileSelectionDialog(shell,ifile,mFilter);
            dialog.setHelpAvailable(false);
            if(dialog.open() == Window.OK) {
                mScript.setText(mStringVariableManager.generateVariableExpression("workspace_loc",dialog.getFile().getFullPath().toString())); //$NON-NLS-1$
            }
        }

        void handleBrowseFilesystem(Shell shell)
        {
            FileDialog dialog = new FileDialog(shell,SWT.OPEN);
            dialog.setFilterExtensions(NSISGeneralTab.FILTER_EXTENSIONS);
            dialog.setFilterNames(NSISGeneralTab.FILTER_NAMES);

            File file;
            try {
                file = checkExternalFile(mStringVariableManager.performStringSubstitution(mScript.getText()));
            }
            catch (Exception e) {
                file = null;
                EclipseNSISPlugin.getDefault().log(e);
            }
            if(file != null) {
                dialog.setFileName(file.getName());
                dialog.setFilterPath(file.getParentFile().getAbsolutePath());
            }

            String filename = dialog.open();
            if(filename != null) {
                mScript.setText(filename);
            }
        }

        void handleBrowseVariables(Shell shell)
        {
            StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(shell);
            dialog.open();
            String variable = dialog.getVariableExpression();
            if (variable != null) {
                mScript.insert(variable);
            }
        }
    }
}