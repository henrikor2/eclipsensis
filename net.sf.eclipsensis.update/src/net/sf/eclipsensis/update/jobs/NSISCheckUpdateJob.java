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

import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.net.NetworkUtil;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;

public class NSISCheckUpdateJob extends NSISHttpUpdateJob
{
    protected static final String NO_UPDATE = "0"; //$NON-NLS-1$
    protected static final String RELEASE_UPDATE = "1"; //$NON-NLS-1$
    protected static final String PREVIEW_UPDATE = "2"; //$NON-NLS-1$

    protected static final MessageFormat RELEASE_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("release.update.message")); //$NON-NLS-1$
    protected static final MessageFormat PREVIEW_UPDATE_MESSAGEFORMAT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("preview.update.message")); //$NON-NLS-1$

    public NSISCheckUpdateJob()
    {
        this(new NSISUpdateJobSettings());
    }

    public NSISCheckUpdateJob(NSISUpdateJobSettings settings)
    {
        this(settings, null);
    }

    public NSISCheckUpdateJob(NSISUpdateJobSettings settings, INSISUpdateJobRunner jobRunner)
    {
        super(EclipseNSISUpdatePlugin.getResourceString("check.update.message"), settings, jobRunner); //$NON-NLS-1$
    }

    @Override
    protected boolean shouldReschedule()
    {
        return getSettings().isAutomated() && ((getSettings().getAction() & SchedulerConstants.UPDATE_DOWNLOAD) == 0);
    }

    @Override
    protected URL getURL() throws IOException
    {
        Version version = NSISPreferences.getInstance().getNSISVersion();
        if(version != null) {
            if(!NSISValidator.isCVSVersion(version)) {
                String site = cPreferenceStore.getString(IUpdatePreferenceConstants.NSIS_UPDATE_SITE);
                if(!Common.isEmpty(site)) {
                    return NSISUpdateURLs.getUpdateURL(site, version.toString());
                }
            }
            else if(!getSettings().isAutomated()) {
                displayExec(new Runnable() {
                    public void run()
                    {
                        Common.openInformation(Display.getCurrent().getActiveShell(), EclipseNSISUpdatePlugin.getResourceString("update.title"),  //$NON-NLS-1$
                                EclipseNSISUpdatePlugin.getResourceString("update.cvs.version.message"), EclipseNSISUpdatePlugin.getShellImage()); //$NON-NLS-1$
                    }
                });
            }
        }
        return null;
    }

    @Override
    protected URL getDefaultURL() throws IOException
    {
        Version version = NSISPreferences.getInstance().getNSISVersion();
        if(version != null) {
            if(!NSISValidator.isCVSVersion(version)) {
                return NSISUpdateURLs.getUpdateURL(version.toString());
            }
        }
        return null;
    }

    @Override
    protected IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException
    {
        try {
            monitor.beginTask(getName(), 100);
            String[] result = NetworkUtil.getLatestVersion(conn);
            String type = result[0];
            String version = result[1];
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }
            monitor.worked(50);
            IStatus status = handleDownload(type, version);
            monitor.worked(50);
            if(status.isOK()) {
                return Status.OK_STATUS;
            }
            else {
                return status;
            }
        }
        finally {
            monitor.done();
        }
    }

    protected IStatus handleDownload(final String type, final String version)
    {
        if((RELEASE_UPDATE.equals(type) || (PREVIEW_UPDATE.equals(type) && !getSettings().isIgnorePreview()))) {
            displayExec(new Runnable() {
                public void run()
                {
                    MessageFormat mf;
                    if(RELEASE_UPDATE.equals(type)) {
                        mf = RELEASE_UPDATE_MESSAGEFORMAT;
                    }
                    else if (PREVIEW_UPDATE.equals(type)) {
                        mf = PREVIEW_UPDATE_MESSAGEFORMAT;
                    }
                    else {
                        return;
                    }
                    NSISUpdateJobSettings settings = getSettings();
                    boolean automated = settings.isAutomated();
                    boolean download = ((settings.getAction() & SchedulerConstants.UPDATE_DOWNLOAD) == SchedulerConstants.UPDATE_DOWNLOAD);
                    if(!download) {
                        automated = false;
                        download = Common.openQuestion(Display.getCurrent().getActiveShell(),
                                            mf.format(new String[] {version}),
                                            EclipseNSISUpdatePlugin.getShellImage());
                    }
                    if(download) {
                        settings = new NSISUpdateJobSettings(automated,settings.getAction());
                        INSISUpdateJobRunner jobRunner = getJobRunner();
                        NSISUpdateJob job = new NSISDownloadUpdateJob(version, settings, jobRunner);
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
        else {
            if(!getSettings().isAutomated()) {
                displayExec(new Runnable() {
                    public void run()
                    {
                        Common.openInformation(Display.getCurrent().getActiveShell(), EclipseNSISUpdatePlugin.getResourceString("update.title"),  //$NON-NLS-1$
                                EclipseNSISUpdatePlugin.getResourceString("no.update.message"), EclipseNSISUpdatePlugin.getShellImage()); //$NON-NLS-1$
                    }
                });
            }
        }

        return Status.OK_STATUS;
    }

    @Override
    protected String formatException(Throwable e)
    {
        return new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("check.update.error")).format(new String[]{e.getMessage()}); //$NON-NLS-1$
    }
}
