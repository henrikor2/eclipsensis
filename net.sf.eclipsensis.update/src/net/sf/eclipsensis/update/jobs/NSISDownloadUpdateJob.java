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

import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.net.*;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

class NSISDownloadUpdateJob extends NSISHttpUpdateJob
{
    protected static final File DOWNLOAD_FOLDER = EclipseNSISUpdatePlugin.getPluginStateLocation();
    protected static final MessageFormat INSTALL_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("install.update.prompt")); //$NON-NLS-1$

    private String mVersion;

    NSISDownloadUpdateJob(String version, NSISUpdateJobSettings settings, INSISUpdateJobRunner jobRunner)
    {
        super(new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.message")).format(new String[]{version}), settings, jobRunner); //$NON-NLS-1$
        mVersion = version;
    }

    @Override
    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated() && ((getSettings().getAction() & SchedulerConstants.UPDATE_INSTALL) == 0);
    }

    @Override
    protected URL getURL() throws IOException
    {
        if(cPreferenceStore.getBoolean(IUpdatePreferenceConstants.AUTOSELECT_SOURCEFORGE_MIRROR)) {
            return NSISUpdateURLs.getAutoDownloadURL(mVersion);
        }
        else {
            String site = cPreferenceStore.getString(IUpdatePreferenceConstants.SOURCEFORGE_MIRROR);
            if(!Common.isEmpty(site)) {
                return NSISUpdateURLs.getDownloadURL(site, mVersion);
            }
        }
        return null;
    }

    @Override
    protected URL getDefaultURL() throws IOException
    {
        return NSISUpdateURLs.getDownloadURL(mVersion);
    }

    @Override
    protected HttpURLConnection makeConnection(IProgressMonitor monitor, URL url, URL defaultURL) throws IOException
    {
        try {
            monitor.beginTask(getName(),100);
            return NetworkUtil.makeConnection(new NestedProgressMonitor(monitor,getName(),25), url, defaultURL);
        }
        catch (IOException ex) {
            final List<DownloadSite> downloadSites = NetworkUtil.getDownloadSites(mVersion, new NestedProgressMonitor(monitor,getName(),25),
                    EclipseNSISUpdatePlugin.getResourceString("download.update.retrieve.alternate.message"), getName()); //$NON-NLS-1$
            if (!Common.isEmptyCollection(downloadSites)) {
                String urlString = (url == null?"":url.toString()); //$NON-NLS-1$
                String defaultURLString = (defaultURL == null?"":defaultURL.toString()); //$NON-NLS-1$
                for (Iterator<DownloadSite> iter = downloadSites.iterator(); iter.hasNext();) {
                    DownloadSite site = iter.next();
                    String siteURLString = NSISUpdateURLs.getGenericDownloadURL(site.getName(), mVersion).toString();
                    if (siteURLString.equalsIgnoreCase(urlString) || siteURLString.equalsIgnoreCase(defaultURLString)) {
                        iter.remove();
                    }
                }
                while (!Common.isEmptyCollection(downloadSites)) {
                    DownloadSite site;
                    if (getSettings().isAutomated()) {
                        site = downloadSites.remove(0);
                    }
                    else {
                        final DownloadSite[] selectedSite = {null};
                        final int[] rv = new int[1];
                        displayExec(new Runnable() {
                            public void run()
                            {
                                DownloadSiteSelectionDialog dialog = new DownloadSiteSelectionDialog(Display.getDefault().getActiveShell(), NSISDownloadUpdateJob.this.getName(), downloadSites);
                                rv[0] = dialog.open();
                                if (rv[0] == Window.OK) {
                                    selectedSite[0] = dialog.getSelectedSite();
                                }
                            }
                        });
                        if (rv[0] == Window.CANCEL || selectedSite[0] == null) {
                            monitor.setCanceled(true);
                            return null;
                        }
                        site = selectedSite[0];
                        downloadSites.remove(site);
                    }
                    monitor.worked(1);
                    try {
                        URL siteURL = NSISUpdateURLs.getGenericDownloadURL(site.getName(), mVersion);
                        return NetworkUtil.makeConnection(new NestedProgressMonitor(monitor, getName(), 1), siteURL, null);
                    }
                    catch (Exception e) {
                        EclipseNSISUpdatePlugin.getDefault().log(IStatus.WARNING, e);
                    }
                }
            }
            throw ex;
        }
        finally {
            monitor.done();
        }
    }

    @Override
    protected IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException
    {
        try {
            monitor.beginTask(getName(),100);
            URL url = conn.getURL();
            String fileName = url.getPath();
            int index = fileName.lastIndexOf('/');
            if(index >= 0) {
                fileName = fileName.substring(index+1);
            }

            if(IOUtility.isValidFile(DOWNLOAD_FOLDER)) {
                DOWNLOAD_FOLDER.delete();
            }
            if(!IOUtility.isValidDirectory(DOWNLOAD_FOLDER)) {
                DOWNLOAD_FOLDER.mkdirs();
            }
            File setupExe = new File(DOWNLOAD_FOLDER,fileName);
            long timestamp = conn.getLastModified();
            if(setupExe.exists() && timestamp > setupExe.lastModified()) {
                setupExe.delete();
            }
            if(!setupExe.exists()) {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(setupExe);
                    IStatus status = NetworkUtil.download(conn, new NestedProgressMonitor(monitor, getName(), 99), getName(), os);
                    if(!status.isOK()) {
                        return status;
                    }
                }
                catch(Exception e) {
                    if(setupExe.exists()) {
                        IOUtility.closeIO(os);
                        os = null;
                        setupExe.delete();
                        IOException ioe;
                        if(e instanceof IOException) {
                            ioe = (IOException)e;
                        }
                        else {
                            ioe = (IOException)new IOException(e.getMessage()).initCause(e);
                        }
                        throw ioe;
                    }
                }
                finally {
                    IOUtility.closeIO(os);
                    if (monitor.isCanceled()) {
                        if(setupExe.exists()) {
                            setupExe.delete();
                        }
                        return Status.CANCEL_STATUS;
                    }
                    else if(setupExe.exists()) {
                        setupExe.setLastModified(timestamp);
                    }

                }
            }
            else {
                try {
                    //This is a hack, otherwise the messagedialog sometimes closes immediately
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                }
                finally {
                    monitor.worked(99);
                }
            }

            if(setupExe.exists()) {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                IStatus status = handleInstall(setupExe);
                if(!status.isOK()) {
                    return status;
                }
            }
            return Status.OK_STATUS;
        }
        finally {
            monitor.done();
        }
    }

    protected IStatus handleInstall(final File setupExe)
    {
        if(setupExe.exists()) {
            displayExec(new Runnable() {
                public void run()
                {
                    NSISUpdateJobSettings settings = getSettings();
                    boolean automated = settings.isAutomated();
                    boolean install = ((settings.getAction() & SchedulerConstants.UPDATE_INSTALL) == SchedulerConstants.UPDATE_INSTALL);
                    if(!install) {
                        automated = false;
                        install = Common.openQuestion(Display.getCurrent().getActiveShell(),
                                    INSTALL_UPDATE_MESSAGEFORMAT.format(new String[] {mVersion}),
                                    EclipseNSISUpdatePlugin.getShellImage());
                    }
                    if(install) {
                        settings = new NSISUpdateJobSettings(automated, settings.getAction());
                        INSISUpdateJobRunner jobRunner = getJobRunner();
                        NSISUpdateJob job = new NSISInstallUpdateJob(mVersion, setupExe, settings);
                        if(jobRunner == null) {
                            job.schedule();
                        }
                        else {
                            jobRunner.run(job);
                        }
                    }
                }
            });
        }
        return Status.OK_STATUS;
    }

    @Override
    protected String formatException(Throwable e)
    {
        return new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("download.update.error")).format(new String[] {mVersion,e.getMessage()}); //$NON-NLS-1$
    }
}
