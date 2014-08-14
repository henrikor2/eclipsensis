/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.startup;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The activator class controls the plug-in life cycle
 */
public class EclipseNSISStartup extends AbstractUIPlugin
{
    // The plug-in ID
    public static final String PLUGIN_ID = "net.sf.eclipsensis.startup"; //$NON-NLS-1$

    // The shared instance
    private static EclipseNSISStartup cPlugin;

    private BundleContext mBundleContext;
    private ResourceBundle mResourceBundle;
    private IEclipsePreferences mPreferences;

    /**
     * The constructor
     */
    public EclipseNSISStartup()
    {
        try {
            mResourceBundle = ResourceBundle.getBundle("net.sf.eclipsensis.startup.EclipseNSISStartupMessages"); //$NON-NLS-1$
        }
        catch(MissingResourceException mre) {
            mResourceBundle = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception
    {
        cPlugin = this;
        mBundleContext = context;
        String name = (String)context.getBundle().getHeaders().get("Bundle-Name"); //$NON-NLS-1$
        mPreferences = new InstanceScope().getNode(name);
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        cPlugin = null;
        mBundleContext = null;
    }

    BundleContext getBundleContext()
    {
        return mBundleContext;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static EclipseNSISStartup getDefault()
    {
        return cPlugin;
    }

    public ResourceBundle getResourceBundle()
    {
        return mResourceBundle;
    }

    public static String getResourceString(String key)
    {
        EclipseNSISStartup plugin = getDefault();
        if(plugin != null) {
            ResourceBundle bundle = plugin.getResourceBundle();
            try {
                return (bundle != null) ? bundle.getString(key) : key;
            }
            catch (MissingResourceException e) {
            }
        }
        return key;
    }

    public void savePreferences()
    {
        try {
            mPreferences.flush();
        }
        catch (BackingStoreException e) {
            log(e);
        }
    }

    public void log(Throwable t)
    {
        ILog log = getLog();
        if(log != null) {
            IStatus status;
            if(t instanceof CoreException) {
                status = ((CoreException)t).getStatus();
            }
            else {
                String message = t.getMessage();
                status = new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR, message==null?t.getClass().getName():message,t);
            }
            log.log(status);
        }
        else {
            t.printStackTrace();
        }
    }
}
