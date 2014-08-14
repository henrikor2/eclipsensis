/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.settings.NSISPreferences;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.osgi.framework.Bundle;

public class IOUtility
{
    public static final IOUtility INSTANCE = new IOUtility();

    private static final Map<String, String> cPathMappingx86tox64;
    private static final Map<String, String> cPathMappingx64tox86;
    private static final String cPathSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
    private static final String cOnePathLevelUp = ".." + cPathSeparator; //$NON-NLS-1$
    private static Pattern cValidUNCName = Pattern.compile("\\\\{0,2}((((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2})\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]]\\\\?)+)?"); //$NON-NLS-1$
    private static Pattern cValidPathName = Pattern.compile("([A-Za-z]:)?\\\\?((((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2})\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]]\\\\?)+)?"); //$NON-NLS-1$
    private static Pattern cValidPathSpec = Pattern.compile("([A-Za-z]:)?\\\\?((((\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2})\\\\)*(\\.?[A-Za-z0-9\\*\\?\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]]\\\\?)+)?"); //$NON-NLS-1$
    private static Pattern cValidFileName = Pattern.compile("(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+"); //$NON-NLS-1$
    private static Pattern cValidURL = Pattern.compile("(?:(?:ftp|https?):\\/\\/)?(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|edu|biz|org|gov|int|info|mil|net|name|museum|coop|aero|[a-z][a-z])\\b(?:\\d+)?(?:\\/[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]*(?:[.,?]+[^;\"'<>()\\[\\]{}\\s\\x7f-\\xff]+)*)?"); //$NON-NLS-1$
    private static Pattern cValidAbsolutePathName = Pattern.compile("[A-Za-z]:\\\\((((\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]])+|\\.{1,2})\\\\)*(\\.?[A-Za-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\x20\\+\\,\\=\\[\\]]\\\\?)+)?"); //$NON-NLS-1$

    private static HashMap<BundleResource, Long> cBundleResources = new HashMap<BundleResource, Long>();

    public static final String FILE_URL_PREFIX = "file:///"; //$NON-NLS-1$

    private IOUtility()
    {
    }

    static {
        cPathMappingx86tox64 = new CaseInsensitiveMap<String>(Common.loadMapProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"path.mapping.x86.to.x64")); //$NON-NLS-1$
        cPathMappingx64tox86 = new CaseInsensitiveMap<String>(Common.loadMapProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"path.mapping.x64.to.x86")); //$NON-NLS-1$
    }

    public static boolean is64BitPath(String path)
    {
        if(!Common.isEmpty(path)) {
            String[] p = Common.tokenize(path,cPathSeparator.charAt(0));
            if(p.length > 0) {
                return cPathMappingx64tox86.containsKey(p[0]);
            }
        }
        return false;
    }

    public static boolean is32BitPath(String path)
    {
        if(!Common.isEmpty(path)) {
            String[] p = Common.tokenize(path,cPathSeparator.charAt(0));
            if(p.length > 0) {
                return cPathMappingx86tox64.containsKey(p[0]);
            }
        }
        return false;
    }

    public static String convertPathTo64bit(String path)
    {
        if(!Common.isEmpty(path)) {
            String[] p = Common.tokenize(path,cPathSeparator.charAt(0));
            if(p.length > 0) {
                if(cPathMappingx86tox64.containsKey(p[0])) {
                    p[0] = cPathMappingx86tox64.get(p[0]);
                    return Common.flatten(p,cPathSeparator.charAt(0));
                }
            }
        }
        return path;
    }

    public static String convertPathTo32bit(String path)
    {
        if(!Common.isEmpty(path)) {
            String[] p = Common.tokenize(path,cPathSeparator.charAt(0));
            if(p.length > 0) {
                if(cPathMappingx64tox86.containsKey(p[0])) {
                    p[0] = cPathMappingx64tox86.get(p[0]);
                    return Common.flatten(p,cPathSeparator.charAt(0));
                }
            }
        }
        return path;
    }

    public static String resolveFileName(String fileName, NSISEditor editor)
    {
        String fileName2 = fileName;
        String newFileName = IOUtility.encodePath(fileName2);
        if(editor != null && newFileName.equalsIgnoreCase(fileName2)) {
            IEditorInput editorInput = editor.getEditorInput();
            if(editorInput instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput)editorInput).getFile();
                if(file != null) {
                    fileName2 = makeRelativeLocation(file, fileName2);
                }
            }
            else if(editorInput instanceof ILocationProvider) {
                File f = new File(((ILocationProvider)editorInput).getPath(editorInput).toOSString());
                fileName2 = makeRelativeLocation(f, fileName2);
            }
        }
        else {
            fileName2 = newFileName;
        }
        return Common.maybeQuote(Common.escapeQuotes(fileName2));
    }

    public static boolean deleteDirectory(File directory)
    {
        if(deleteDirectoryContents(directory)) {
            return directory.delete();
        }
        return true;
    }

    public static boolean deleteDirectoryContents(File directory)
    {
        if(isValidDirectory(directory)) {
            File[] files = directory.listFiles();
            if(!Common.isEmptyArray(files)) {
                for (int i = 0; i < files.length; i++) {
                    if(files[i].isDirectory()) {
                        if(!deleteDirectory(files[i])) {
                            return false;
                        }
                    }
                    else {
                        if(!files[i].delete()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static String getFileExtension(File file)
    {
        String name = file.getName();
        return getFileExtension(name);
    }

    /**
     * @param filename
     * @return
     */
    public static String getFileExtension(String filename)
    {
        if (filename != null) {
            int n = filename.lastIndexOf('.');
            if (n >= 0) {
                return filename.substring(n + 1);
            }
        }
        return null;
    }

    public static String encodePath(String path)
    {
        String path2 = path;
        if(!Common.isEmpty(path2)) {
            String nsisdirKeyword = NSISKeywords.getInstance().getKeyword("${NSISDIR}"); //$NON-NLS-1$
            String nsisHome = "";
            NSISPreferences prefs = NSISPreferences.getInstance();
            if(prefs.getNSISHome() != null)
            {
                nsisHome = prefs.getNSISHome().getLocation().getAbsolutePath().toLowerCase();
            }
            if(path2.toLowerCase().startsWith(nsisHome)) {
                path2 = nsisdirKeyword + path2.substring(nsisHome.length());
            }
        }
        return path2;
    }

    public static String decodePath(String path)
    {
        String path2 = path;
        String nsisdirKeyword = NSISKeywords.getInstance().getKeyword("${NSISDIR}").toLowerCase(); //$NON-NLS-1$
        String nsisHome = "";
        NSISPreferences prefs = NSISPreferences.getInstance();
        if(prefs.getNSISHome() != null)
        {
            nsisHome = prefs.getNSISHome().getLocation().getAbsolutePath();
        }
        if(path2.toLowerCase().startsWith(nsisdirKeyword))
        {
            path2 = nsisHome + path2.substring(nsisdirKeyword.length());
        }
        return path2;
    }

    public static <T> T readObject(File file) throws IOException, ClassNotFoundException
    {
        return readObject(file, null);
    }

    public static <T> T readObject(File file, ClassLoader classLoader) throws IOException, ClassNotFoundException
    {
        return readObject(new BufferedInputStream(new FileInputStream(file)), classLoader);
    }

    public static <T> T readObject(InputStream inputStream) throws IOException, ClassNotFoundException
    {
        return readObject(inputStream, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(InputStream inputStream, final ClassLoader classLoader) throws IOException, ClassNotFoundException
    {
        InputStream inputStream2 = inputStream;
        ObjectInputStream ois = null;
        try {
            if(!(inputStream2 instanceof BufferedInputStream)) {
                inputStream2 = new BufferedInputStream(inputStream2);
            }
            ois = new ObjectInputStream(inputStream2){

                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
                {
                    if(classLoader != null) {
                        try {
                            return Class.forName(desc.getName(), false, classLoader);
                        }
                        catch(ClassNotFoundException e) {}
                    }
                    return super.resolveClass(desc);
                }
            };
            return (T)ois.readObject();
        }
        finally {
            closeIO(ois);
            closeIO(inputStream2);
        }
    }

    public static void closeIO(Object object)
    {
        if(object != null) {
            try {
                if(object instanceof InputStream) {
                    InputStream is = (InputStream)object;
                    try {
                        int count = 0;
                        while(count < 100 && is.available() > 0) {
                            try {
                                Thread.sleep(10);
                            }
                            catch (InterruptedException e1) {
                            }
                            count++;
                        }
                    }
                    catch (IOException e) {
                    }
                    is.close();
                }
                else if(object instanceof OutputStream) {
                    OutputStream os = (OutputStream)object;
                    try {
                        os.flush();
                    }
                    catch (IOException e) {
                    }
                    os.close();
                }
                else if(object instanceof Reader) {
                    Reader r = (Reader)object;
                    try {
                        int count = 0;
                        while(count < 100 && r.ready()) {
                            try {
                                Thread.sleep(10);
                            }
                            catch (InterruptedException e1) {
                                EclipseNSISPlugin.getDefault().log(e1);
                            }
                            count++;
                        }
                    }
                    catch (IOException e) {
                    }
                    r.close();
                }
                else if(object instanceof Writer) {
                    Writer w = (Writer)object;
                    try {
                        w.flush();
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    w.close();
                }
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    public static void writeObject(File file, Object object) throws IOException
    {
        writeObject(new BufferedOutputStream(new FileOutputStream(file)), object);
    }

    public static void writeObject(OutputStream outputStream, Object object) throws IOException
    {
        OutputStream outputStream2 = outputStream;
        if(object != null) {
            ObjectOutputStream oos = null;

            try {
                if(!(outputStream2 instanceof BufferedOutputStream)) {
                    outputStream2 = new BufferedOutputStream(outputStream2);
                }
                oos = new ObjectOutputStream(outputStream2);
                oos.writeObject(object);
            }
            finally {
                closeIO(oos);
                closeIO(outputStream2);
            }
        }
    }

    public static boolean isValidAbsolutePathName(String pathName)
    {
        return isValidPathNameOrSpec(pathName, cValidAbsolutePathName);
    }

    public static boolean isValidPathName(String pathName)
    {
        return isValidPathNameOrSpec(pathName, cValidPathName);
    }

    public static boolean isValidPathSpec(String pathName)
    {
        return isValidPathNameOrSpec(pathName, cValidPathSpec);
    }

    private static boolean isValidPathNameOrSpec(String pathName, Pattern path)
    {
        if(pathName != null && pathName.length() > 0) {
            Matcher matcher = path.matcher(pathName);
            return matcher.matches();
        }
        return false;
    }

    public static boolean isValidFileName(String fileName)
    {
        if(fileName != null && fileName.length() > 0) {
            Matcher matcher = cValidFileName.matcher(fileName);
            return matcher.matches();
        }
        return false;
    }

    public static boolean isValidUNCName(String name)
    {
        if(name != null && name.length() > 0) {
            Matcher matcher = cValidUNCName.matcher(name);
            return matcher.matches();
        }
        return false;
    }

    public static boolean isValidFile(String fileName)
    {
        return isValidFile(new File(fileName));
    }

    public static boolean isValidFile(File file)
    {
        return file != null && file.exists() && file.isFile();
    }

    public static boolean isValidDirectory(File file)
    {
        return file != null && file.exists() && file.isDirectory();
    }

    public static boolean isValidPath(String pathName)
    {
        File file = new File(pathName);
        return isValidDirectory(file);
    }

    public static boolean isValidURL(String url)
    {
        if(url != null && url.length() > 0) {
            Matcher matcher = cValidURL.matcher(url);
            return matcher.matches();
        }
        return false;
    }

    public static String makeRelativeLocation(IResource resource, String pathname)
    {
        if(resource instanceof IContainer || resource instanceof IFile) {
            return makeRelativeLocation((resource instanceof IContainer?(IContainer)resource:((IFile)resource).getParent()).getLocation(), pathname);
        }
        return pathname;
    }

    public static String makeRelativeLocation(File file, String pathname)
    {
        if(file != null) {
            String filepath = file.isFile()?file.getParent():file.getPath();
            if(filepath != null) {
                return makeRelativeLocation(new Path(filepath), pathname);
            }
        }
        return pathname;
    }

    private static String makeRelativeLocation(IPath reference, String pathname)
    {
        if(!Common.isEmpty(pathname)) {
            IPath childPath = new Path(pathname);
            if(reference != null && reference.isAbsolute() && childPath.isAbsolute()) {
                if(Common.stringsAreEqual(reference.getDevice(), childPath.getDevice(), true)) {
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    int l1 = reference.segmentCount();
                    int l2 = childPath.segmentCount();
                    int n = Math.min(l1,l2);

                    int i=0;
                    for(; i<n; i++) {
                        if(!reference.segment(i).equalsIgnoreCase(childPath.segment(i))) {
                            break;
                        }
                    }

                    if(i > 0) {
                        for(int j=i; j<l1; j++) {
                            buf.append(cOnePathLevelUp);
                        }
                        for(int j=i; j<l2-1; j++) {
                            buf.append(childPath.segment(j)).append(cPathSeparator);
                        }
                        buf.append(childPath.lastSegment());
                        childPath = new Path(buf.toString());
                    }
                }
            }
            return childPath.toOSString();
        }
        return pathname;
    }

    public static void writeContentToFile(File file, byte[] content)
    {
        if(!file.canWrite()) {
            file.delete();
        }
        ByteBuffer buf = ByteBuffer.wrap(content);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.getChannel().write(buf);
        }
        catch (FileNotFoundException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    public static byte[] loadContentFromFile(File file) {
        byte[] bytes = new byte[0];
        if(file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                bytes = readChannel(fis.getChannel());
            }
            catch (FileNotFoundException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            finally {
                if(fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
        }
        return bytes;
    }

    public static byte[] loadContentFromStream(InputStream stream)
    {
        try {
            return readChannel(Channels.newChannel(stream));
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
            return new byte[0];
        }
    }

    public static byte[] readChannel(ReadableByteChannel channel) throws IOException
    {
        byte[] bytes = new byte[0];
        ByteBuffer buf = ByteBuffer.allocateDirect(8192);
        int numread = channel.read(buf);
        while(numread > 0) {
            buf.rewind();
            int n = bytes.length;
            bytes = (byte[])Common.resizeArray(bytes, n+numread);
            buf.get(bytes,n,numread);
            buf.rewind();
            numread = channel.read(buf);
        }
        return bytes;
    }

    public static File ensureLatest(Bundle bundle, IPath source, File destFolder) throws IOException
    {
        if(IOUtility.isValidFile(destFolder)) {
            destFolder.delete();
        }
        if(!destFolder.exists()) {
            destFolder.mkdirs();
        }
        BundleResource key = new BundleResource(bundle,source);
        long lastModified;
        Long l = cBundleResources.get(key);
        if(l != null) {
            lastModified = l.longValue();
        }
        else {
            lastModified = bundle.getLastModified();
            try {
                IPath relative = source.makeRelative();
                URL url = FileLocator.resolve(bundle.getEntry("/")); //$NON-NLS-1$
                if (url.getProtocol().equalsIgnoreCase("file")) { //$NON-NLS-1$
                    File original = new File(url.getFile(), relative.toString());
                    if (isValidFile(original)) {
                        lastModified = original.lastModified();
                    }
                }
                else if (url.getProtocol().equals("jar")) { //$NON-NLS-1$
                    JarURLConnection conn = (JarURLConnection)url.openConnection();
                    JarFile jarFile = conn.getJarFile();
                    JarEntry entry = jarFile.getJarEntry(relative.toString());
                    if (entry != null && !entry.isDirectory()) {
                        lastModified = entry.getTime();
                    }
                }
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }

        File destFile = new File(destFolder,source.lastSegment());
        if(IOUtility.isValidDirectory(destFile)) {
            deleteDirectory(destFile);
        }
        else if(IOUtility.isValidFile(destFile)) {
            if (destFile.lastModified() >= lastModified) {
                return destFile;
            }
            destFile.delete();
        }
        URL url = bundle.getEntry(source.toString());
        InputStream is = null;
        byte[] data;
        try {
            is = new BufferedInputStream(url.openStream());
            data = loadContentFromStream(is);
        }
        finally {
            closeIO(is);
        }
        writeContentToFile(destFile,data);
        destFile.setLastModified(lastModified);
        cBundleResources.put(key, new Long(lastModified));
        return destFile;
    }

    public static final String getFileURLString(File file)
    {
        try {
            String url = file.toURI().toURL().toString();
            for(int i=0; i<FILE_URL_PREFIX.length(); i++) {
                if(url.charAt(i) != FILE_URL_PREFIX.charAt(i) && Character.toLowerCase(url.charAt(i)) != FILE_URL_PREFIX.charAt(i)) {
                    url = new StringBuffer(url.substring(0,i)).append(FILE_URL_PREFIX.substring(i)).append(url.substring(i)).toString();
                    break;
                }
            }
            return url;
        }
        catch (MalformedURLException e) {
            EclipseNSISPlugin.getDefault().log(e);
            return null;
        }
    }

    private static class BundleResource
    {
        private Bundle mBundle;
        private IPath mResource;

        public BundleResource(Bundle bundle, IPath resource)
        {
            mBundle = bundle;
            mResource = resource;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof BundleResource) {
                BundleResource other = (BundleResource)obj;
                return mBundle.equals(other.mBundle) && mResource.equals(other.mResource);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return mBundle.hashCode() << 16 + mResource.hashCode();
        }
    }

    public static boolean isValidFile(IFile file)
    {
        if (file != null) {
            if (!file.exists()) {
                IProject project = file.getProject();
                if (project.isOpen()) {
                    return false;
                }
                else {
                    return project.exists();
                }
            }
            return true;
        }
        return false;
    }
}
