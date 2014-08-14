/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities.util;

import java.io.File;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.*;
import org.eclipse.jdt.launching.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.*;

public abstract class AbstractToolsUtility implements IJavaLaunchConfigurationConstants
{
    protected File mToolsJar = null;
    protected boolean mVerbose = false;
    protected boolean mIgnoreErrors = false;
    protected IVMInstall mVMInstall;
    protected String mMainClassName;
    protected List<?> mSelection;

    public AbstractToolsUtility(IVMInstall vmInstall, String toolsJar, String mainClassName, List<?> selection)
    {
        mVMInstall = vmInstall;
        mToolsJar = new File(toolsJar);
        mMainClassName = mainClassName;
        mSelection = selection;
    }

    protected String maybeQuote(String str)
    {
        if(!Common.isEmpty(str)) {
            if(!str.startsWith("\"") || !str.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                char[] chars = str.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if(Character.isWhitespace(chars[i])) {
                        return new StringBuffer("\"").append(str).append("\"").toString(); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        return str;
    }

    public void setVerbose(boolean verbose)
    {
        mVerbose = verbose;
    }

    public void setIgnoreErrors(boolean ignoreErrors)
    {
        mIgnoreErrors = ignoreErrors;
    }

    protected void writeLogMessage(final MessageConsoleStream stream, final String message)
    {
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                stream.print(message);
            }
        });
    }

    public IStatus run(IProgressMonitor monitor)
    {
        try {
            IRuntimeClasspathEntry toolsEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(mToolsJar.getAbsolutePath()));
            toolsEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
            IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
            IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath,
                                                    IRuntimeClasspathEntry.STANDARD_CLASSES);
            List<String> classpath = new ArrayList<String>();
            classpath.add(toolsEntry.getMemento());
            classpath.add(systemLibsEntry.getMemento());

            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type = manager.getLaunchConfigurationType(ID_JAVA_APPLICATION);

            ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, getLaunchTitle());

            // specify a JRE
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(mVMInstall).toPortableString());

            // specify main type and program arguments
            workingCopy.setAttribute(ATTR_MAIN_TYPE_NAME, mMainClassName);
            workingCopy.setAttribute(ATTR_CLASSPATH, classpath);
            workingCopy.setAttribute(ATTR_DEFAULT_CLASSPATH, false);

            final MessageConsoleStream[] streams = new MessageConsoleStream[2];
            monitor.beginTask(getTaskName(), mSelection.size());
            final MessageConsole console = new MessageConsole(getConsoleTitle(),null);
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
                    consoleManager.addConsoles(new IConsole[]{console});
                    streams[0] = console.newMessageStream();

                    streams[1] = console.newMessageStream();
                    streams[1].setColor(JFaceResources.getColorRegistry().get("ERROR_COLOR")); //$NON-NLS-1$
                    consoleManager.showConsoleView(console);
                }
            });
            for(Iterator<?> iter=mSelection.iterator(); iter.hasNext(); ) {
                Object target = iter.next();
                try {
                    IStatus status = preProcess(target, monitor);
                    if(status.isOK()) {
                        monitor.subTask(getSubTaskName(target));
                        String arg = getProgramArguments(target);
                        workingCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, arg);
                        ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE,new SubProgressMonitor(monitor,1),false,false);
                        IProcess process = launch.getProcesses()[0];
                        IStreamsProxy proxy = process.getStreamsProxy();
                        IStreamMonitor stdout = proxy.getOutputStreamMonitor();
                        stdout.addListener(new IStreamListener(){
                            public void streamAppended(String text, IStreamMonitor monitor)
                            {
                                writeLogMessage(streams[0],text);
                            }
                        });
                        IStreamMonitor stderr = proxy.getErrorStreamMonitor();
                        stderr.addListener(new IStreamListener(){
                            public void streamAppended(String text, IStreamMonitor monitor)
                            {
                                writeLogMessage(streams[1],text);
                            }
                        });
                        while(!process.isTerminated()) {
                            Thread.sleep(100);
                        }
                        if(process.getExitValue() != 0) {
                            writeLogMessage(streams[1],getFailMessage(target));
                        }
                        else {
                            writeLogMessage(streams[0],getSuccessMessage(target));
                        }
                    }
                    else if(status.matches(IStatus.CANCEL)) {
                        if(!status.matches(IStatus.OK)) {
                            writeLogMessage(streams[1],getCancelMessage());
                            return Status.CANCEL_STATUS;
                        }
                    }
                    else {
                        writeLogMessage(streams[1], status.getMessage());
                        if(!mIgnoreErrors) {
                            return status;
                        }
                    }
                }
                catch (Throwable e) {
                    writeLogMessage(streams[1],getExceptionMessage(e));
                    if(!mIgnoreErrors) {
                        return createStatus(IStatus.ERROR,getExceptionMessage(e));
                    }
                }
                IStatus status = postProcess(target, monitor);
                if(status.matches(IStatus.CANCEL)) {
                    writeLogMessage(streams[1],getCancelMessage());
                    return Status.CANCEL_STATUS;
                }
                monitor.worked(1);
                if(iter.hasNext()) {
                    writeLogMessage(streams[0],"\n"); //$NON-NLS-1$
                }
            }
            return Status.OK_STATUS;
         }
        catch(Exception ex) {
            return createStatus(IStatus.ERROR,getExceptionMessage(ex));
        }
    }

    protected String getExceptionMessage(Throwable exception)
    {
        String message = null;
        if(exception != null) {
            message = exception.getMessage();
            if(message == null || message.length() == 0) {
                Throwable cause = exception.getCause();
                if(!exception.equals(cause)) {
                    message = getExceptionMessage(cause);
                }
                if(message == null) {
                    message = exception.getClass().getName();
                }
            }
        }
        return message;
    }

    protected IStatus preProcess(Object target, IProgressMonitor monitor)
    {
        if(monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

    protected IStatus postProcess(Object target, IProgressMonitor monitor)
    {
        if(monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }

    /**
     * @param message
     * @return
     */
    protected IStatus createStatus(int type, String message)
    {
        return new Status(type,getPlugin().getBundle().getSymbolicName(),-1,message,null);
    }

    protected abstract String getProgramArguments(Object target);
    protected abstract Plugin getPlugin();
    protected abstract String getSuccessMessage(Object target);
    protected abstract String getCancelMessage();
    protected abstract String getFailMessage(Object target);

    protected abstract String getConsoleTitle();

    protected abstract String getLaunchTitle();

    protected abstract String getTaskName();
    protected abstract String getSubTaskName(Object target);
}
