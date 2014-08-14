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

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class NSISLaunchShortcut implements ILaunchShortcut
{
    private ILaunchManager mLaunchManager;
    private ILaunchConfigurationType mConfigType;
    private IStringVariableManager mStringVariableManager;

    public NSISLaunchShortcut()
    {
        mLaunchManager = DebugPlugin.getDefault().getLaunchManager();
        mConfigType = mLaunchManager.getLaunchConfigurationType("net.sf.eclipsensis.launch.nsisLaunchConfigType"); //$NON-NLS-1$
        mStringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
    }

    public void launch(final ISelection selection, String mode)
    {
        if(mode.equals(ILaunchManager.RUN_MODE) && selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object obj = ((IStructuredSelection)selection).getFirstElement();
            if(obj instanceof IFile) {
                launch(((IFile)obj).getFullPath(), mode);
            }
        }
    }

    public void launch(final IEditorPart editor, String mode)
    {
        if(mode.equals(ILaunchManager.RUN_MODE) && editor != null) {
            IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(editor);
            if(input instanceof IFileEditorInput) {
                launch(((IFileEditorInput)input).getFile().getFullPath(), mode);
            }
            else {
                launch((input).getPath(), mode);
            }
        }
    }

    private void launch(IPath path, String mode)
    {
        try {
            IPath path2 = NSISCompileTestUtility.INSTANCE.getCompileScript(path);
            if (path2 != null && INSISConstants.NSI_EXTENSION.equalsIgnoreCase(path2.getFileExtension())) {
                ILaunchConfiguration config = findConfiguration(path2);
                if(config != null) {
                    DebugUITools.launch(config, mode);
                }
            }
        }
        catch(final Exception e) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    Common.openError(Display.getCurrent().getActiveShell(),e.getMessage(),EclipseNSISPlugin.getShellImage());
                }
            });
        }
    }

    private ILaunchConfiguration findConfiguration(IPath path)
    {
        String fullname = getLocation(path);
        List<ILaunchConfiguration> candidateConfigs = new ArrayList<ILaunchConfiguration>();
        try {
            if (mConfigType != null) {
                ILaunchConfiguration[] configs = mLaunchManager.getLaunchConfigurations(mConfigType);
                if (!Common.isEmptyArray(configs)) {
                    for (int i = 0; i < configs.length; i++) {
                        ILaunchConfiguration config = configs[i];
                        try {
                            String script = config.getAttribute(NSISLaunchSettings.SCRIPT, ""); //$NON-NLS-1$
                            if (!Common.isEmpty(script)) {
                                String fullname2 = getLocation(new Path(mStringVariableManager.performStringSubstitution(script)));
                                if (Common.stringsAreEqual(fullname, fullname2, true)) {
                                    candidateConfigs.add(config);
                                }
                            }
                        }
                        catch (CoreException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }

        if(candidateConfigs.size() > 0) {
            if(candidateConfigs.size() > 1) {
                return chooseConfiguration(candidateConfigs);
            }
            else {
                return candidateConfigs.get(0);
            }
        }
        else {
            return createConfiguration(path);
        }
    }

    private ILaunchConfiguration createConfiguration(IPath path)
    {
        NSISLaunchSettings settings;
        String script;
        if(path.getDevice() == null) {
            script = mStringVariableManager.generateVariableExpression("workspace_loc",path.toString()); //$NON-NLS-1$
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            NSISSettings properties = NSISProperties.getProperties(file);
            while(properties instanceof NSISProperties && ((NSISProperties)properties).getUseParent()) {
                properties = ((NSISProperties)properties).getParentSettings();
            }
            settings = new NSISLaunchSettings(properties);
        }
        else {
            script = path.toOSString();
            settings = new NSISLaunchSettings(NSISPreferences.getInstance());
        }
        settings.setScript(script);
        settings.setRunInstaller(false);

        ILaunchConfiguration config = null;
        ILaunchConfigurationWorkingCopy wc = null;
        try {
            wc = mConfigType.newInstance(null, mLaunchManager.generateUniqueLaunchConfigurationNameFrom(path.lastSegment()));
        } catch (CoreException exception) {
            reportCreatingConfiguration(exception);
            return null;
        }
        settings.setLaunchConfig(wc);
        settings.store();
        try {
            config = wc.doSave();
        } catch (CoreException exception) {
            reportCreatingConfiguration(exception);
            return null;
        }
        return config;
    }

    protected void reportCreatingConfiguration(final CoreException exception)
    {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                int mask = IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR;
                ErrorDialog dialog = new ErrorDialog(getShell(), EclipseNSISPlugin.getResourceString("launch.error.dialog.title"), EclipseNSISPlugin.getResourceString("launch.error.dialog.message"), exception.getStatus(), mask); //$NON-NLS-1$ //$NON-NLS-2$
                dialog.create();
                dialog.getShell().setImage(EclipseNSISPlugin.getShellImage());
                dialog.open();
            }
        });
    }

    private ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList)
    {
        IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setElements(configList.toArray());
        dialog.setTitle(EclipseNSISPlugin.getResourceString("launch.dialog.title")); //$NON-NLS-1$
        dialog.setMessage(EclipseNSISPlugin.getResourceString("launch.dialog.message")); //$NON-NLS-1$
        dialog.setMultipleSelection(false);
        int result = dialog.open();
        labelProvider.dispose();
        if (result == Window.OK) {
            return (ILaunchConfiguration) dialog.getFirstResult();
        }
        return null;
    }

    private Shell getShell()
    {
        Shell shell;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            shell = window.getShell();
        }
        else {
            shell = null;
        }
        return shell;
    }

    private String getLocation(IPath path)
    {
        String fullname = null;
        if(path.getDevice() == null) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            if(file != null) {
                IPath location = file.getLocation();
                if(location == null) {
                    throw new IllegalArgumentException(EclipseNSISPlugin.getResourceString("local.filesystem.error")); //$NON-NLS-1$
                }
                fullname = location.toOSString();
            }
        }
        else {
            fullname = path.toOSString();
        }
        return fullname;
    }
}
