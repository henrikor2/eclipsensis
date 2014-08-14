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

import java.io.IOException;
import java.net.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.net.NetworkUtil;
import net.sf.eclipsensis.util.NestedProgressMonitor;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;

public abstract class NSISHttpUpdateJob extends NSISUpdateJob
{
    protected static final IPreferenceStore cPreferenceStore = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();

    private INSISUpdateJobRunner mJobRunner = null;

    protected NSISHttpUpdateJob(String name, NSISUpdateJobSettings settings, INSISUpdateJobRunner jobRunner)
    {
        super(name, settings);
        mJobRunner = jobRunner;
    }

    public INSISUpdateJobRunner getJobRunner()
    {
        return mJobRunner;
    }

    @Override
    protected final IStatus doRun(IProgressMonitor monitor)
    {
        monitor.beginTask(getName(), 110);
        try {
            URL url = null;
            URL defaultUrl = null;
            try {
                url = getURL();
            }
            catch (IOException e) {
                handleException(e);
                return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
            }
            try {
                defaultUrl = getDefaultURL();
            }
            catch (IOException e) {
                handleException(e);
                return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
            }
            if(url == null) {
                url = defaultUrl;
                defaultUrl = null;
            }
            else if(defaultUrl != null && url.toString().equals(defaultUrl.toString())) {
                defaultUrl = null;
            }

            if (url != null || defaultUrl != null) {
                try {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    monitor.worked(5);
                    HttpURLConnection conn = null;
                    try {
                        IProgressMonitor subMonitor = new NestedProgressMonitor(monitor,getName(),5);
                        conn = makeConnection(subMonitor, url, defaultUrl);
                        if(monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }
                        subMonitor = new NestedProgressMonitor(monitor,getName(),100);
                        IStatus status = handleConnection(conn, subMonitor);
                        if(!status.isOK()) {
                            return status;
                        }
                    }
                    finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                }
                catch (Exception e) {
                    handleException(e);
                    return new Status(IStatus.ERROR, EclipseNSISUpdatePlugin.getDefault().getPluginId(), IStatus.ERROR, e.getMessage(), e);
                }
            }
            return Status.OK_STATUS;
        }
        finally {
            monitor.done();
        }
    }

    /**
     * @param monitor
     * @param url
     * @param defaultURL
     * @param conn
     * @return
     * @throws IOException
     */
    protected HttpURLConnection makeConnection(IProgressMonitor monitor, URL url, URL defaultURL) throws IOException
    {
        return NetworkUtil.makeConnection(monitor, url, defaultURL);
    }

    protected final void setSystemProperty(String name, String value)
    {
        if (value == null) {
            try {
                System.getProperties().remove(name);
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
            }
        }
        else {
            System.setProperty(name, value);
        }
    }

    protected abstract URL getDefaultURL() throws IOException;
    protected abstract URL getURL() throws IOException;
    protected abstract IStatus handleConnection(HttpURLConnection conn, IProgressMonitor monitor) throws IOException;
}
