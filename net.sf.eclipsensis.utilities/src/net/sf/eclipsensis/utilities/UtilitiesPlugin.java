/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.utilities.job.JobScheduler;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UtilitiesPlugin extends AbstractUIPlugin
{
    //The shared instance.
    private static UtilitiesPlugin cPlugin;
    private ResourceBundle mResourceBundle;
    private Image mShellImage;
    private JobScheduler mJobScheduler = new JobScheduler();
    private File mToolsVMCacheFile;
    private Properties mToolsVMCache = new Properties();

    /**
     * The constructor.
     */
    public UtilitiesPlugin()
    {
        cPlugin = this;
        try {
            mResourceBundle = ResourceBundle.getBundle("net.sf.eclipsensis.utilities.UtilitiesPluginResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            mResourceBundle = null;
        }
    }
    /**
     * Returns the shared instance.
     */
    public static UtilitiesPlugin getDefault()
    {
        return cPlugin;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = getDefault().getResourceBundle();
        try {
            return bundle != null ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static String getFormattedString(String key, Object[] args)
    {
        return MessageFormat.format(getResourceString(key),args);
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return mResourceBundle;
    }

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        mToolsVMCacheFile = new File(getStateLocation().toFile(),"net.sf.eclipsensis.utilities.tools-vms-cache.properties");
        mJobScheduler.start();
        URL entry = getBundle().getEntry(getResourceString("utilities.icon")); //$NON-NLS-1$
        getImageRegistry().put("utilities.icon", ImageDescriptor.createFromURL(entry)); //$NON-NLS-1$
        mShellImage = getImageRegistry().get("utilities.icon"); //$NON-NLS-1$

        if(mToolsVMCacheFile.exists() && mToolsVMCacheFile.isFile())
        {
            BufferedInputStream is = null;
            try
            {
                is = new BufferedInputStream(new FileInputStream(mToolsVMCacheFile));
                mToolsVMCache.load(is);
            }
            finally
            {
                if(is != null)
                {
                    is.close();
                }
            }
        }
    }

    public Image getShellImage()
    {
        return mShellImage;
    }

    public JobScheduler getJobScheduler()
    {
        return mJobScheduler;
    }

    public String getCachedVMName(String toolsJarName)
    {
        return mToolsVMCache.getProperty(toolsJarName);
    }

    public void setCachedVMName(String toolsJarName, String vmName)
    {
        mToolsVMCache.setProperty(toolsJarName, vmName);
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        if(mToolsVMCacheFile.exists() && mToolsVMCacheFile.isFile())
        {
            BufferedOutputStream os = null;
            try
            {
                os = new BufferedOutputStream(new FileOutputStream(mToolsVMCacheFile));
                mToolsVMCache.store(os, null);
            }
            finally
            {
                if(os != null)
                {
                    os.close();
                }
            }
        }
        mJobScheduler.stop();
        mShellImage = null;
        super.stop(context);
    }
}
