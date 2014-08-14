/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.parser.ParserDelegator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.util.NestedProgressMonitor;
import net.sf.eclipsensis.util.NodeConversionUtility;
import net.sf.eclipsensis.util.XMLUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NetworkUtil
{
    private static final String PREFERENCE_IMAGE_CACHE_REFRESH_TIMESTAMP = "imageCacheRefreshTimestamp"; //$NON-NLS-1$
    private static final String PREFERENCE_CACHED_DOWNLOAD_SITES = "cachedDownloadSites"; //$NON-NLS-1$
    private static final String PREFERENCE_DOWNLOAD_SITES_REFRESH_TIMESTAMP = "downloadSitesRefreshTimestamp"; //$NON-NLS-1$
    private static final String PREFERENCE_DOWNLOAD_SITES_VERSION = "downloadSitesVersion"; //$NON-NLS-1$
    private static final int DOWNLOAD_BUFFER_SIZE = 32768;
    private static MessageFormat cConnectionFormat = new MessageFormat(EclipseNSISUpdatePlugin
            .getResourceString("http.connect.message")); //$NON-NLS-1$
    private static Map<File, Image> cImageCache = new HashMap<File, Image>();
    private static long cImageCacheRefreshTimestamp = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore()
            .getLong(PREFERENCE_IMAGE_CACHE_REFRESH_TIMESTAMP);
    private static final File IMAGE_CACHE_FOLDER = new File(EclipseNSISUpdatePlugin.getPluginStateLocation(),
            "imageCache"); //$NON-NLS-1$
    private static List<DownloadSite> cDownloadSites;
    private static long cDownloadSitesRefreshTimestamp;
    private static String cDownloadSitesVersion;

    static
    {
        String xml = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().getString(
                PREFERENCE_CACHED_DOWNLOAD_SITES);
        try
        {
            if (!Common.isEmpty(xml))
            {
                Document doc = XMLUtil.loadDocument(new ByteArrayInputStream(xml.getBytes()));
                cDownloadSites = Common.makeGenericList(DownloadSite.class, NodeConversionUtility.readCollectionNode(doc.getDocumentElement(), List.class));
            }
        }
        catch (Exception e)
        {
            cDownloadSites = null;
            EclipseNSISUpdatePlugin.getDefault().log(IStatus.WARNING, e);
        }
        if (cDownloadSites == null)
        {
            cDownloadSites = new ArrayList<DownloadSite>();
            cDownloadSitesRefreshTimestamp = 0;
            cDownloadSitesVersion = "0"; //$NON-NLS-1$
        }
        else
        {
            cDownloadSitesRefreshTimestamp = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().getLong(
                    PREFERENCE_DOWNLOAD_SITES_REFRESH_TIMESTAMP);
            cDownloadSitesVersion = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().getString(
                    PREFERENCE_DOWNLOAD_SITES_VERSION);
        }
    }

    private NetworkUtil()
    {
    }

    public static HttpURLConnection makeConnection(IProgressMonitor monitor, URL url, URL defaultURL)
            throws IOException
    {
        try
        {
            URL url2 = url;
            monitor.beginTask(cConnectionFormat.format(new String[] { url2.getHost() }), 100);
            HttpURLConnection conn = null;
            int responseCode;
            try
            {
                conn = (HttpURLConnection) url2.openConnection();
                responseCode = conn.getResponseCode();
            }
            catch (IOException e)
            {
                if (defaultURL != null)
                {
                    responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
                }
                else
                {
                    throw e;
                }
            }
            if (responseCode >= 400)
            {
                if (defaultURL != null)
                {
                    monitor.worked(50);
                    url2 = defaultURL;
                    monitor.setTaskName(cConnectionFormat.format(new String[] { url2.getHost() }));
                    conn = (HttpURLConnection) url2.openConnection();
                    responseCode = conn.getResponseCode();
                }
                if (responseCode >= 400)
                {
                    String message = conn == null?null:conn.getResponseMessage();
                    if (Common.isEmpty(message))
                    {
                        message = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("http.error")).format(new Object[] { new Integer(responseCode) }); //$NON-NLS-1$
                    }
                    throw new IOException(message);
                }
            }
            return conn;
        }
        finally
        {
            monitor.done();
        }
    }

    public static IStatus download(HttpURLConnection conn, IProgressMonitor monitor, String name, OutputStream os)
            throws IOException
    {
        int length = 0;
        String[] args = null;

        MessageFormat mf = new MessageFormat(EclipseNSISUpdatePlugin
                .getResourceString("download.update.progress.format")); //$NON-NLS-1$
        if (monitor != null)
        {
            length = conn.getContentLength();
            if (length <= 0)
            {
                monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
            }
            else
            {
                args = new String[] { "0" }; //$NON-NLS-1$
                monitor.beginTask(mf.format(args), 101);
            }
            monitor.worked(1);
            if (monitor.isCanceled())
            {
                return Status.CANCEL_STATUS;
            }
        }
        BufferedInputStream is = null;
        try
        {
            is = new BufferedInputStream(conn.getInputStream());

            ByteBuffer buf = ByteBuffer.allocateDirect(DOWNLOAD_BUFFER_SIZE);
            ReadableByteChannel channel = Channels.newChannel(is);
            WritableByteChannel fileChannel = Channels.newChannel(os);
            int worked = 0;
            int totalread = 0;
            int numread = channel.read(buf);
            while (numread >= 0)
            {
                if (monitor != null && monitor.isCanceled())
                {
                    return Status.CANCEL_STATUS;
                }
                totalread += numread;
                if (buf.position() >= buf.limit())
                {
                    buf.flip();
                    fileChannel.write(buf);

                    if (monitor != null && length > 0 && args != null)
                    {
                        int newWorked = Math.round(totalread * 100 / length);
                        args[0] = Integer.toString(newWorked);
                        monitor.setTaskName(mf.format(args));
                        monitor.worked(newWorked - worked);
                        worked = newWorked;
                    }

                    buf.rewind();
                }
                numread = channel.read(buf);
            }
            if (buf.position() > 0)
            {
                buf.flip();
                fileChannel.write(buf);
            }
            if (monitor != null && length > 0 && args != null)
            {
                args[0] = "100"; //$NON-NLS-1$
                monitor.setTaskName(mf.format(args));
                monitor.worked(100 - worked);
            }
            fileChannel.close();
            channel.close();
        }
        finally
        {
            IOUtility.closeIO(is);
            IOUtility.closeIO(os);
            if (monitor != null)
            {
                if (monitor.isCanceled())
                {
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
            }
        }

        return Status.OK_STATUS;
    }

    public static String getContent(HttpURLConnection conn) throws IOException
    {
        ByteArrayOutputStream os = null;
        try
        {
            os = new ByteArrayOutputStream(DOWNLOAD_BUFFER_SIZE);
            NetworkUtil.download(conn, null, null, os);
        }
        catch (Exception e)
        {
            IOException ioe;
            if (e instanceof IOException)
            {
                ioe = (IOException) e;
            }
            else
            {
                ioe = (IOException) new IOException(e.getMessage()).initCause(e);
            }
            throw ioe;
        }
        finally
        {
            IOUtility.closeIO(os);
        }

        String content;
        try
        {
            content = os.toString(conn.getContentEncoding());
        }
        catch (Exception ex)
        {
            content = os.toString();
        }
        return content;
    }

    public static boolean downloadLatest(URL url, File targetFile)
    {
        boolean ok = false;
        boolean downloaded = false;
        FileOutputStream fos = null;
        HttpURLConnection conn2 = null;
        long timestamp = 0;
        try
        {
            conn2 = makeConnection(new NullProgressMonitor(), url, null);
            timestamp = conn2.getLastModified();
            if (timestamp > (targetFile.exists() ? targetFile.lastModified() : 0))
            {
                if (targetFile.exists())
                {
                    targetFile.delete();
                }
                ok = false;
                fos = new FileOutputStream(targetFile);
                NetworkUtil.download(conn2, null, null, fos);
                ok = true;
                downloaded = true;
            }
        }
        catch (Exception e)
        {
            EclipseNSISPlugin.getDefault().log(e);
            ok = false;
        }
        finally
        {
            IOUtility.closeIO(fos);
            if (downloaded)
            {
                targetFile.setLastModified(timestamp);
            }
            if (conn2 != null)
            {
                conn2.disconnect();
            }
        }
        return ok;
    }

    public static String[] getLatestVersion(HttpURLConnection conn) throws IOException
    {
        InputStream is = null;
        String type = null;
        String version = ""; //$NON-NLS-1$
        try {
            is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            if(line != null) {
                String[] tokens = Common.tokenize(line, '|');
                if(tokens.length > 0) {
                    type = tokens[0];
                    if(tokens.length > 1) {
                        version = tokens[1];
                    }
                }
            }
        }
        finally {
            IOUtility.closeIO(is);
        }
        return new String[]{type,version};
    }

    public static synchronized List<DownloadSite> getDownloadSites(String version, IProgressMonitor monitor, String taskName, String parentTaskName)
    {
        return getDownloadSites(version, monitor, taskName, parentTaskName, false);
    }

    public static synchronized List<DownloadSite> getDownloadSites(String version, IProgressMonitor monitor, String taskName, String parentTaskName,
            boolean forceRefresh)
    {
        if (!forceRefresh && Common.stringsAreEqual(cDownloadSitesVersion,version))
        {
            long now = System.currentTimeMillis();
            if (now - cDownloadSitesRefreshTimestamp <= 86400000)
            {
                return cDownloadSites;
            }
        }
        try
        {
            monitor.beginTask(taskName, 100);
            cDownloadSites = new ArrayList<DownloadSite>();
            HttpURLConnection conn2 = null;
            String content = null;
            try
            {
                //if(NSISPreferences)
                conn2 = makeConnection(new NestedProgressMonitor(monitor, taskName, parentTaskName, 25), NSISUpdateURLs
                        .getSelectDownloadURL(version), null);
                content = getContent(conn2);
                monitor.worked(25);
            }
            catch (IOException ioe)
            {
                return null;
            }
            finally
            {
                if (conn2 != null)
                {
                    conn2.disconnect();
                }
            }
            if (content != null)
            {
                // BIG HACK - Replace with proper lenient XHTML parser if
                // available
                // HTML Parser does not like XHTML
                int n = content.indexOf("<html"); //$NON-NLS-1$
                if (n >= 0)
                {
                    content = content.substring(n);
                }
                content = content.replaceAll("\\s*/>", ">"); //$NON-NLS-1$ //$NON-NLS-2$

                ParserDelegator parserDelegator = new ParserDelegator();
                DownloadURLsParserCallback callback = new DownloadURLsParserCallback();
                try
                {
                    parserDelegator.parse(new StringReader(content), callback, true);
                }
                catch (IOException e1)
                {
                    return null;
                }
                List<String[]> sites = callback.getSites();
                if (sites.size() > 0)
                {
                    long now = System.currentTimeMillis();
                    boolean refreshImageCache = false;
                    if (now - cImageCacheRefreshTimestamp > 86400000)
                    {
                        // Refresh once a day
                        refreshImageCache = true;
                        cImageCacheRefreshTimestamp = now;
                        EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().setValue(
                                PREFERENCE_IMAGE_CACHE_REFRESH_TIMESTAMP, cImageCacheRefreshTimestamp);
                    }
                    if (!IOUtility.isValidDirectory(IMAGE_CACHE_FOLDER))
                    {
                        IMAGE_CACHE_FOLDER.mkdirs();
                    }
                    int count = 0;
                    for (Iterator<String[]> iter = sites.iterator(); iter.hasNext();)
                    {
                        String[] element = iter.next();
                        if (element != null && element.length == 4)
                        {
                            File imageFile = null;
                            try
                            {
                                if (!Common.isEmpty(element[0]))
                                {
                                    URL imageURL = new URL(element[0]);
                                    String path = imageURL.getPath();
                                    n = path.lastIndexOf("/"); //$NON-NLS-1$
                                    if (n >= 0)
                                    {
                                        path = path.substring(n + 1);
                                    }
                                    if (!Common.isEmpty(path))
                                    {
                                        imageFile = new File(IMAGE_CACHE_FOLDER, path);
                                        if (!imageFile.exists() || refreshImageCache)
                                        {
                                            NetworkUtil.downloadLatest(imageURL, imageFile);
                                            if (!imageFile.exists())
                                            {
                                                cImageCache.remove(imageFile);
                                            }
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                EclipseNSISUpdatePlugin.getDefault().log(IStatus.WARNING, e);
                            }
                            cDownloadSites.add(new DownloadSite(imageFile, element[1], element[2], element[3]));
                        }
                        monitor.worked(50 * (++count / sites.size()));
                    }
                }
            }

            return cDownloadSites;
        }
        finally
        {
            try
            {
                Document doc = XMLUtil.newDocument();
                Node parent = doc.createElement("downloadsites"); //$NON-NLS-1$
                doc.appendChild(parent);
                NodeConversionUtility.createCollectionNode(doc, parent, cDownloadSites);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                XMLUtil.saveDocument(doc, baos, false);
                String xml = baos.toString();
                EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().setValue(PREFERENCE_CACHED_DOWNLOAD_SITES,
                        xml);
                cDownloadSitesRefreshTimestamp = System.currentTimeMillis();
                cDownloadSitesVersion = version;
                EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().setValue(
                        PREFERENCE_DOWNLOAD_SITES_REFRESH_TIMESTAMP, cDownloadSitesRefreshTimestamp);
                EclipseNSISUpdatePlugin.getDefault().getPreferenceStore().setValue(
                        PREFERENCE_DOWNLOAD_SITES_VERSION, version);
            }
            catch (Exception e)
            {
                EclipseNSISUpdatePlugin.getDefault().log(IStatus.WARNING, e);
            }
            monitor.done();
        }
    }

    static Image getImageFromCache(File imageFile)
    {
        Image image;
        image = cImageCache.get(imageFile);
        if (image == null)
        {
            if (imageFile != null && imageFile.exists())
            {
                final ImageData imageData = new ImageData(imageFile.getAbsolutePath());
                final Image[] imageArray = new Image[1];
                Display.getDefault().syncExec(new Runnable() {
                    public void run()
                    {
                        try
                        {
                            imageArray[0] = new Image(Display.getDefault(), imageData);
                        }
                        catch (Exception ex)
                        {
                            imageArray[0] = null;
                        }
                    }
                });
                image = imageArray[0];
                if (image != null)
                {
                    cImageCache.put(imageFile, image);
                }
            }
        }
        return image;
    }
}
