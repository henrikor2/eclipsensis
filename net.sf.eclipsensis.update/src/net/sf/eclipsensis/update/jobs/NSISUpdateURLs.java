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
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;

public class NSISUpdateURLs
{
    private static final String cDefaultDownloadSite;
    private static final String cDefaultUpdateSite;
    private static final MessageFormat cUpdateURLFormat;
    private static final MessageFormat cDownloadURLFormat;
    private static final MessageFormat cGenericDownloadURLFormat;
    private static final MessageFormat cAutoDownloadURLFormat;
    private static final MessageFormat cSelectDownloadURLFormat;
    private static final URL cSiteImagesUpdateURL;

    static {
        String className = NSISUpdateURLs.class.getName();
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(className);
        } catch (MissingResourceException x) {
            bundle = null;
        }

        cDefaultUpdateSite = readBundle(bundle, "default.update.site"); //$NON-NLS-1$
        cDefaultDownloadSite = readBundle(bundle, "default.download.site"); //$NON-NLS-1$
        cUpdateURLFormat = readBundleFormat(bundle, "update.url.format"); //$NON-NLS-1$
        cDownloadURLFormat = readBundleFormat(bundle, "download.url.format"); //$NON-NLS-1$
        cGenericDownloadURLFormat = readBundleFormat(bundle, "generic.download.url.format"); //$NON-NLS-1$
        cAutoDownloadURLFormat = readBundleFormat(bundle, "auto.download.url.format"); //$NON-NLS-1$
        cSelectDownloadURLFormat = readBundleFormat(bundle, "select.download.url"); //$NON-NLS-1$
        cSiteImagesUpdateURL = readBundleURL(bundle,"site.images.update.url"); //$NON-NLS-1$
    }

    private NSISUpdateURLs()
    {
    }

    private static String readBundle(ResourceBundle bundle, String key)
    {
        String string = null;
        if(bundle != null) {
            try {
                string = bundle.getString(key);
            }
            catch (Exception e) {
                EclipseNSISUpdatePlugin.getDefault().log(e);
                string = null;
            }
        }
        return string;
    }

    private static MessageFormat readBundleFormat(ResourceBundle bundle, String key)
    {
        MessageFormat format = null;
        try {
            format = new MessageFormat(readBundle(bundle, key));
        }
        catch (Exception e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
            format = null;
        }
        return format;
    }

    private static URL readBundleURL(ResourceBundle bundle, String key)
    {
        URL url = null;
        try {
            url = new URL(readBundle(bundle, key));
        }
        catch (Exception e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
            url = null;
        }
        return url;
    }

    public static String getDefaultDownloadSite()
    {
        return cDefaultDownloadSite;
    }

    public static String getDefaultUpdateSite()
    {
        return cDefaultUpdateSite;
    }

    public static synchronized URL getUpdateURL(String site, String version) throws IOException
    {
        return new URL(cUpdateURLFormat.format(new String[] {site, version}));
    }

    public static synchronized URL getDownloadURL(String site, String version) throws IOException
    {
        return new URL(cDownloadURLFormat.format(new String[] {site, version}));
    }

    public static synchronized URL getGenericDownloadURL(String site, String version) throws IOException
    {
        return new URL(cGenericDownloadURLFormat.format(new String[] {site, version}));
    }

    public static synchronized URL getAutoDownloadURL(String version) throws IOException
    {
        return new URL(cAutoDownloadURLFormat.format(new String[] {version}));
    }

    public static synchronized URL getSelectDownloadURL(String version) throws IOException
    {
        return new URL(cSelectDownloadURLFormat.format(new String[] {version}));
    }

    public static synchronized URL getSiteImagesUpdateURL()
    {
        return cSiteImagesUpdateURL;
    }

    public static synchronized URL getUpdateURL(String version) throws IOException
    {
        return getUpdateURL(cDefaultUpdateSite, version);
    }

    public static synchronized URL getDownloadURL(String version) throws IOException
    {
        return getDownloadURL(cDefaultDownloadSite, version);
    }
}
