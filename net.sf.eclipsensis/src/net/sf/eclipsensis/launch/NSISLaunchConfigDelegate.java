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

import java.io.*;
import java.text.MessageFormat;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.editor.NSISExternalFileEditorInput;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.*;
import org.eclipse.ui.console.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class NSISLaunchConfigDelegate implements ILaunchConfigurationDelegate
{
    private static final String BUILDER_LAUNCH_CONFIG_TYPE_ID = EclipseNSISPlugin.getBundleResourceString("%builder.launch.config.type.id"); //$NON-NLS-1$

    private static final String VALID_FILENAME_CHARS = ":\\.$-{}!()&^+,=[]"; //$NON-NLS-1$

    public void launch(ILaunchConfiguration configuration, String mode, final ILaunch launch, final IProgressMonitor monitor) throws CoreException
    {
        try {
            String taskName = EclipseNSISPlugin.getResourceString("launching.nsis.script.task.name"); //$NON-NLS-1$
            monitor.beginTask(taskName, 200);
            String script = null;
            boolean useConsole = true;
            File outputFile = null;
            String output = null;
            boolean append = false;
            boolean runInstaller = false;
            String encoding;

            ILaunchConfigurationType configType = configuration.getType();
            script = configuration.getAttribute(NSISLaunchSettings.SCRIPT, ""); //$NON-NLS-1$
            runInstaller = configType.getIdentifier().equals(BUILDER_LAUNCH_CONFIG_TYPE_ID)?false:configuration.getAttribute(NSISLaunchSettings.RUN_INSTALLER, false);
            useConsole = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
            output = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String)null);
            append = configuration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
            encoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null);

            monitor.worked(10);

            if (Common.isEmpty(script)) {
                throw new CoreException(new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID,IStatus.ERROR,EclipseNSISPlugin.getResourceString("launch.missing.script.error"),null)); //$NON-NLS-1$
            }
            IStringVariableManager stringVariableManager = VariablesPlugin.getDefault().getStringVariableManager();
            IPath path = null;
            if(!Common.isEmpty(script)) {
                script = stringVariableManager.performStringSubstitution(script);
                File file = new File(script);
                if(INSISConstants.NSI_EXTENSION.equalsIgnoreCase(IOUtility.getFileExtension(file)) &&
                   IOUtility.isValidFile(file) && file.isAbsolute()) {
                    path = new Path(file.getAbsolutePath());
                }
            }
            if(path == null) {
                throw new CoreException(new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID,IStatus.ERROR,EclipseNSISPlugin.getFormattedString("launch.invalid.script.error", script),null)); //$NON-NLS-1$
            }
            IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
            if(ifile != null) {
                path = ifile.getFullPath();
            }

            if (output != null) {
                output = stringVariableManager.performStringSubstitution(output);
            }

            monitor.worked(10);

            ifile = null;
            if (output != null) {
                ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(output));

                try {
                    if (ifile != null) {
                        if (append && ifile.exists()) {
                            ifile.appendContents(new ByteArrayInputStream(new byte[0]), true, true, new NullProgressMonitor());
                        }
                        else {
                            if (ifile.exists()) {
                                ifile.delete(true, new NullProgressMonitor());
                            }
                            ifile.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
                        }
                    }

                    outputFile = new File(output);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }

            monitor.worked(10);

            INSISConsole console;
            if (!useConsole) {
                if (outputFile == null) {
                    console = new NullNSISConsole();
                }
                else {
                    console = new FileNSISConsole(outputFile, append);
                }
            }
            else {
                if (outputFile == null) {
                    console = EclipseNSISPlugin.getDefault().getConsole();
                }
                else {
                    console = new CompoundNSISConsole(new INSISConsole[]{new FileNSISConsole(outputFile, append), EclipseNSISPlugin.getDefault().getConsole()});
                }
            }

            monitor.worked(10);

            String defaultEncoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
            if(encoding != null && !encoding.equals(defaultEncoding)) {
                console = new EncodingNSISConsole(console, encoding);
            }
            NSISSettings settings = new NSISLaunchSettings(NSISPreferences.getInstance(), configuration);
            final NSISLaunchProcess process = new NSISLaunchProcess(path, launch);
            launch.addProcess(process);

            monitor.worked(10);

            try {
                if (MakeNSISRunner.isCompiling()) {
                    NestedProgressMonitor subMonitor = new NestedProgressMonitor(monitor, taskName, 50);
                    try {
                        subMonitor.beginTask(EclipseNSISPlugin.getResourceString("launch.waiting.makensis.message"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                        while (MakeNSISRunner.isCompiling()) {
                            try {
                                Thread.sleep(50);
                                if (monitor.isCanceled()) {
                                    process.terminate();
                                    return;
                                }
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    finally {
                        subMonitor.done();
                    }
                }
                else {
                    monitor.worked(50);
                }
                if (monitor.isCanceled()) {
                    process.terminate();
                    return;
                }

                NestedProgressMonitor subMonitor = new NestedProgressMonitor(monitor, taskName, 50);
                try {
                    subMonitor.beginTask(EclipseNSISPlugin.getResourceString("launch.compiling.message"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    new Thread(new Runnable() {
                        public void run()
                        {
                            while(!process.isTerminated()) {
                                if(monitor.isCanceled()) {
                                    process.terminate();
                                    break;
                                }
                                try {
                                    Thread.sleep(10);
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },EclipseNSISPlugin.getResourceString("launch.monitor.thread.name")).start(); //$NON-NLS-1$
                }
                finally {
                    subMonitor.done();
                }
                MakeNSISResults results = MakeNSISRunner.compile(path, settings, console, new NSISConsoleLineProcessor(path));
                if (monitor.isCanceled()) {
                    process.terminate();
                    return;
                }
                monitor.worked(20);
                if(ifile != null) {
                    try {
                        ifile.refreshLocal(IResource.DEPTH_ZERO, new NestedProgressMonitor(monitor, taskName, 10));
                    }
                    catch (CoreException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
                if(results != null) {
                    if(useConsole && outputFile != null) {
                        String filename;
                        final IEditorInput editorInput;
                        IEditorDescriptor descriptor;
                        final IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
                        if(ifile != null) {
                            filename = ifile.getFullPath().toString();
                            editorInput = new FileEditorInput(ifile);
                            descriptor = registry.getDefaultEditor(ifile.getName());
                        }
                        else {
                            filename = outputFile.getAbsolutePath();
                            editorInput = new NSISExternalFileEditorInput(outputFile);
                            descriptor = registry.getDefaultEditor(outputFile.getName());
                        }
                        if(descriptor == null) {
                            descriptor = registry.findEditor("org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
                        }
                        if(descriptor == null) {
                            descriptor = registry.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
                        }
                        if (descriptor != null) {
                            final String editorId = descriptor.getId();
                            final IHyperlink hyperlink = new IHyperlink() {
                                public void linkEntered()
                                {
                                }

                                public void linkExited()
                                {
                                }

                                public void linkActivated()
                                {
                                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                                    try {
                                        IDE.openEditor(page, editorInput, editorId);
                                    }
                                    catch (PartInitException e) {
                                        EclipseNSISPlugin.getDefault().log(e);
                                    }
                                }
                            };
                            String message = MessageFormat.format(EclipseNSISPlugin.getResourceString("launch.console.redirect.message"), new Object[]{filename}); //$NON-NLS-1$
                            final NSISConsole nsisConsole = EclipseNSISPlugin.getDefault().getConsole();
                            final String ffilename;
                            if(encoding != null && !encoding.equals(defaultEncoding)) {
                                String temp;
                                try {
                                    message = new String(message.getBytes(), encoding);
                                    temp = new String(filename.getBytes(), encoding);
                                }
                                catch (Exception e) {
                                    temp = filename;
                                }
                                ffilename = temp;
                            }
                            else {
                                ffilename = filename;
                            }
                            nsisConsole.addPatternMatchListener(new IPatternMatchListener() {
                                String mPattern = escape(ffilename);

                                private String escape(String path)
                                {
                                    StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$
                                    if(path != null) {
                                        char[] chars = path.toCharArray();
                                        for (int i = 0; i < chars.length; i++) {
                                            switch(chars[i]) {
                                                case ' ':
                                                    buffer.append("\\x20"); //$NON-NLS-1$
                                                    break;
                                                case '\t':
                                                    buffer.append("\\t"); //$NON-NLS-1$
                                                    break;
                                                default:
                                                    if(VALID_FILENAME_CHARS.indexOf(chars[i]) >= 0) {
                                                        buffer.append('\\');
                                                    }
                                                    buffer.append(chars[i]);
                                            }
                                        }
                                    }
                                    return buffer.toString();
                                }

                                public String getPattern()
                                {
                                    return mPattern;
                                }

                                public void matchFound(PatternMatchEvent event)
                                {
                                    try {
                                        nsisConsole.addHyperlink(hyperlink, event.getOffset(), event.getLength());
                                        nsisConsole.removePatternMatchListener(this);
                                    } catch (BadLocationException e) {
                                    }
                                }

                                public int getCompilerFlags()
                                {
                                    return 0;
                                }

                                public String getLineQualifier()
                                {
                                    return null;
                                }

                                public void connect(TextConsole console)
                                {
                                }

                                public void disconnect() {
                                }
                            });
                            nsisConsole.appendLine(NSISConsoleLine.info(message));
                        }
                    }
                    monitor.worked(10);
                    if(results.getReturnCode() == MakeNSISResults.RETURN_SUCCESS && runInstaller) {
                        MakeNSISRunner.testInstaller(results.getOutputFileName(), console);
                    }
                    monitor.worked(10);
                }
            }
            finally {
                process.terminate();
            }
        }
        finally {
            monitor.done();
        }
    }
}
