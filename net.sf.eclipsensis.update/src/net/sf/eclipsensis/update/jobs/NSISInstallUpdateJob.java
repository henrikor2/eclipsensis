/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.jobs;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.filemon.FileMonitor;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;

class NSISInstallUpdateJob extends NSISUpdateJob
{
    public static final int INSTALL_ERROR = -1;
    public static final int INSTALL_SUCCESS = 0;
    public static final int INSTALL_CANCEL = 1;
    public static final int INSTALL_ABORTED = 2;

    private static MessageFormat cNotifyFormat = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.complete.message")); //$NON-NLS-1$
    private static MessageFormat cAutoNotifyFormat = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("auto.install.complete.message")); //$NON-NLS-1$

    private String mVersion;
    private File mSetupExe;

    NSISInstallUpdateJob(String version, File setupExe, NSISUpdateJobSettings settings)
    {
        super(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("installing.update.message")).format(new String[]{version}), settings); //$NON-NLS-1$
        mVersion = version;
        mSetupExe = setupExe;
    }

    @Override
    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated();
    }

    @Override
    protected IStatus doRun(final IProgressMonitor monitor)
    {
        if(IOUtility.isValidFile(mSetupExe)) {
            monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);
            boolean fileMonStopped = false;
            try {
                String nsisHome = "";
                final NSISPreferences prefs = NSISPreferences.getInstance();
                if(prefs.getNSISHome() != null)
                {
                    nsisHome = prefs.getNSISHome().getLocation().getAbsolutePath();
                }
                NSISUpdateJobSettings settings = getSettings();
                if(!Common.isEmpty(nsisHome)) {
                    fileMonStopped = FileMonitor.INSTANCE.stop();
                }
                final List<String> cmd = new ArrayList<String>();
                if(EclipseNSISPlugin.getDefault().isWinVista())
                {
                    cmd.add("cmd.exe"); //$NON-NLS-1$
                    cmd.add("/c"); //$NON-NLS-1$
                }
                cmd.add(mSetupExe.getAbsolutePath());
                boolean install = (settings.getAction() & SchedulerConstants.UPDATE_INSTALL) == SchedulerConstants.UPDATE_INSTALL;
                if(install) {
                    cmd.add("/S"); //Silent //$NON-NLS-1$
                    if(!Common.isEmpty(nsisHome)) {
                        cmd.add("/D="+nsisHome); //$NON-NLS-1$
                    }
                }
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                int rv = INSTALL_SUCCESS;
                final boolean[] terminated = { false };
                try {
                    final Process p = Runtime.getRuntime().exec(cmd.toArray(Common.EMPTY_STRING_ARRAY));
                    new Thread(new Runnable() {
                        public void run()
                        {
                            while(!terminated[0]) {
                                if(monitor.isCanceled()) {
                                    displayExec(new Runnable() {
                                        public void run()
                                        {
                                            Common.openWarning(Display.getCurrent().getActiveShell(),
                                                            EclipseNSISUpdatePlugin.getResourceString("cancel.not.supported.message"),  //$NON-NLS-1$
                                                            EclipseNSISUpdatePlugin.getShellImage());
                                        }
                                    });
                                    return;
                                }
                                try {
                                    Thread.sleep(10);
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },EclipseNSISUpdatePlugin.getResourceString("install.thread.name")).start(); //$NON-NLS-1$
                    rv = p.waitFor();
                }
                catch (Exception e) {
                    EclipseNSISUpdatePlugin.getDefault().log(e);
                    rv = INSTALL_ERROR;
                }
                finally {
                    terminated[0] = true;
                }
                switch(rv) {
                    case INSTALL_SUCCESS:
                        mSetupExe.delete();
                        final String newNSISHome = NSISValidator.getRegistryNSISHome();
                        if (!Common.isEmpty(newNSISHome)) {
                            if (nsisHome == null || !newNSISHome.equalsIgnoreCase(nsisHome)) {
                                monitor.setTaskName(EclipseNSISUpdatePlugin.getResourceString("configuring.eclipsensis.task.name")); //$NON-NLS-1$
                                Display.getDefault().syncExec(new Runnable() {
                                    public void run()
                                    {
                                        prefs.setNSISHome(newNSISHome);
                                        prefs.store();
                                    }
                                });
                            }
                        }
                        if (install) {
                            final MessageFormat format = settings.isAutomated()?cAutoNotifyFormat:cNotifyFormat;
                            displayExec(new Runnable() {
                                public void run()
                                {
                                    Common.openInformation(Display.getCurrent().getActiveShell(), EclipseNSISUpdatePlugin.getResourceString("update.title"), //$NON-NLS-1$
                                                    format.format(new String[]{mVersion}), EclipseNSISUpdatePlugin.getShellImage());
                                }
                            });
                        }
                        break;
                    case INSTALL_CANCEL:
                        return Status.CANCEL_STATUS;
                    case INSTALL_ABORTED:
                        throw new RuntimeException(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.aborted.error")).format(new String[]{mVersion})); //$NON-NLS-1$
                    case INSTALL_ERROR:
                    default:
                        throw new RuntimeException(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.exec.error")).format(new String[]{mVersion})); //$NON-NLS-1$
                }
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
                return new Status(IStatus.ERROR,EclipseNSISUpdatePlugin.getDefault().getPluginId(),IStatus.ERROR,e.getMessage(),e);
            }
            finally {
                monitor.done();
                if(fileMonStopped) {
                    FileMonitor.INSTANCE.start();
                }
            }
        }
        if (monitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;
    }
}
