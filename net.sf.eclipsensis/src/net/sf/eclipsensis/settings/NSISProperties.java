/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class NSISProperties extends NSISSettings implements INSISConstants
{
    public static final String USE_PARENT = "useParent"; //$NON-NLS-1$
    public static final String USE_GLOBALS = "useGlobals"; //$NON-NLS-1$
    private static Map<String, NSISProperties> cPropertiesCache = new HashMap<String, NSISProperties>();
    private static Map<String, QualifiedName> cQualifiedNames = new HashMap<String, QualifiedName>();

    private IResource mResource = null;
    private boolean mUseParent = true;

    private NSISSettings mParentSettings;

    static {
        cQualifiedNames.put(USE_GLOBALS, new QualifiedName(PLUGIN_ID,USE_GLOBALS));
        cQualifiedNames.put(USE_PARENT, new QualifiedName(PLUGIN_ID,USE_PARENT));
        cQualifiedNames.put(HDRINFO, new QualifiedName(PLUGIN_ID,HDRINFO));
        cQualifiedNames.put(VERBOSITY, new QualifiedName(PLUGIN_ID,VERBOSITY));
        cQualifiedNames.put(PROCESS_PRIORITY, new QualifiedName(PLUGIN_ID,PROCESS_PRIORITY));
        cQualifiedNames.put(LICENSE, new QualifiedName(PLUGIN_ID,LICENSE));
        cQualifiedNames.put(NOCONFIG, new QualifiedName(PLUGIN_ID,NOCONFIG));
        cQualifiedNames.put(NOCD, new QualifiedName(PLUGIN_ID,NOCD));
        cQualifiedNames.put(COMPRESSOR, new QualifiedName(PLUGIN_ID,COMPRESSOR));
        cQualifiedNames.put(SOLID_COMPRESSION, new QualifiedName(PLUGIN_ID,SOLID_COMPRESSION));
        cQualifiedNames.put(INSTRUCTIONS, new QualifiedName(PLUGIN_ID,INSTRUCTIONS));
        cQualifiedNames.put(SYMBOLS, new QualifiedName(PLUGIN_ID,SYMBOLS));
    }

    public static synchronized NSISProperties getProperties(IResource resource)
    {
        if (resource != null) {
            String fileName = null;
            IPath location = resource.getLocation();
            if (location != null) {
                fileName = location.toString();
            }
            if (fileName == null || !cPropertiesCache.containsKey(fileName)) {
                NSISProperties props = new NSISProperties(resource);
                props.load();
                if (fileName != null) {
                    cPropertiesCache.put(fileName, props);
                }
                return props;
            }
            return cPropertiesCache.get(fileName);
        }
        return null;
    }

    protected NSISProperties(IResource resource)
    {
        mResource = resource;
        if(mResource instanceof IProject) {
            mParentSettings = NSISPreferences.getInstance();
        }
        else if(mResource instanceof IFile || mResource instanceof IFolder) {
            mParentSettings = getProperties(resource.getParent());
        }
        else {
            throw new IllegalArgumentException(String.valueOf(resource.getFullPath()));
        }
    }

    public IResource getResource()
    {
        return mResource;
    }

    @Override
    public void load()
    {
        String temp = getPersistentProperty(getQualifiedName(USE_PARENT));
        if(temp == null) {
            temp = getPersistentProperty(getQualifiedName(USE_GLOBALS));
            if(temp != null) {
                setPersistentProperty(getQualifiedName(USE_GLOBALS), null);
                setPersistentProperty(getQualifiedName(USE_PARENT), temp);
            }
        }
        setUseParent((temp == null || Boolean.valueOf(temp).booleanValue()));
        if(!getUseParent()) {
            super.load();
        }
    }

    @Override
    public void store()
    {
        setValue(USE_PARENT,getUseParent());
        if(getUseParent()) {
            setHdrInfo(getDefaultHdrInfo());
            setLicense(getDefaultLicense());
            setNoConfig(getDefaultNoConfig());
            setNoCD(getDefaultNoCD());
            setVerbosity(getDefaultVerbosity());
            setProcessPriority(getDefaultProcessPriority());
            setCompressor(getDefaultCompressor());
            setSolidCompression(getDefaultSolidCompression());
            setSymbols(getDefaultSymbols());
            setInstructions(getDefaultInstructions());
        }
        super.store();
    }

    @Override
    public int getCompressor()
    {
        if(getUseParent()) {
            return mParentSettings.getCompressor();
        }
        else {
            return super.getCompressor();
        }
    }

    @Override
    public boolean getSolidCompression()
    {
        if(getUseParent()) {
            return mParentSettings.getSolidCompression();
        }
        else {
            return super.getSolidCompression();
        }
    }

    @Override
    public boolean getHdrInfo()
    {
        if(getUseParent()) {
            return mParentSettings.getHdrInfo();
        }
        else {
            return super.getHdrInfo();
        }
    }

    @Override
    public List<String> getInstructions()
    {
        if(getUseParent()) {
            return mParentSettings.getInstructions();
        }
        else {
            return super.getInstructions();
        }
    }

    @Override
    public boolean getLicense()
    {
        if(getUseParent()) {
            return mParentSettings.getLicense();
        }
        else {
            return super.getLicense();
        }
    }

    @Override
    public boolean getNoCD()
    {
        if(getUseParent()) {
            return mParentSettings.getNoCD();
        }
        else {
            return super.getNoCD();
        }
    }

    @Override
    public boolean getNoConfig()
    {
        if(getUseParent()) {
            return mParentSettings.getNoConfig();
        }
        else {
            return super.getNoConfig();
        }
    }

    @Override
    public Map<String,String> getSymbols()
    {
        if(getUseParent()) {
            return mParentSettings.getSymbols();
        }
        else {
            return super.getSymbols();
        }
    }

    @Override
    public int getVerbosity()
    {
        if(getUseParent()) {
            return mParentSettings.getVerbosity();
        }
        else {
            return super.getVerbosity();
        }
    }

    @Override
    public int getProcessPriority()
    {
        if(getUseParent()) {
            return mParentSettings.getProcessPriority();
        }
        else {
            return super.getProcessPriority();
        }
    }

    private static synchronized QualifiedName getQualifiedName(String name)
    {
        QualifiedName qname = cQualifiedNames.get(name);
        if(qname == null) {
            qname = new QualifiedName(PLUGIN_ID,name);
            cQualifiedNames.put(name,qname);
        }
        return qname;
    }

    public boolean getUseParent()
    {
        return mUseParent;
    }

    /**
     * @param useParent The useParent to set.
     */
    public void setUseParent(boolean useParent)
    {
        mUseParent = useParent;
    }

    private String getPersistentProperty(QualifiedName qname)
    {
        String value = null;
        try {
            value = mResource.getPersistentProperty(qname);
        }
        catch(CoreException ex){ }
        return value;
    }

    private void setPersistentProperty(QualifiedName qname, String value)
    {
        try {
            mResource.setPersistentProperty(qname, value);
        }
        catch(CoreException ex){
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String name)
    {
        String value = getPersistentProperty(getQualifiedName(name));
        return (value !=null && Boolean.valueOf(value).booleanValue());
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getInt(java.lang.String)
     */
    @Override
    public int getInt(String name)
    {
        String value = getPersistentProperty(getQualifiedName(name));
        if(value != null) {
            try {
                return Integer.parseInt(value);
            }
            catch(NumberFormatException nfe) {
                return 0;
            }
        }
        else {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getString(java.lang.String)
     */
    @Override
    public String getString(String name)
    {
        return getPersistentProperty(getQualifiedName(name));
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, boolean)
     */
    @Override
    public void setValue(String name, boolean value)
    {
        setPersistentProperty(getQualifiedName(name), Boolean.toString(value));
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, int)
     */
    @Override
    public void setValue(String name, int value)
    {
        setPersistentProperty(getQualifiedName(name), Integer.toString(value));
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, java.lang.String)
     */
    @Override
    public void setValue(String name, String value)
    {
        setPersistentProperty(getQualifiedName(name), value);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeBoolean(java.lang.String)
     */
    @Override
    public void removeBoolean(String name)
    {
        setPersistentProperty(getQualifiedName(name), null);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeInt(java.lang.String)
     */
    @Override
    public void removeInt(String name)
    {
        setPersistentProperty(getQualifiedName(name), null);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeString(java.lang.String)
     */
    @Override
    public void removeString(String name)
    {
        setPersistentProperty(getQualifiedName(name), null);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#loadObject(java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T loadObject(String name)
    {
        QualifiedName qname = getQualifiedName(name);
        ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
        synchronizer.add(qname);
        InputStream is = null;
        try {
            byte[] bytes = synchronizer.getSyncInfo(qname,mResource);
            if(!Common.isEmptyArray(bytes)) {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                is = new BufferedInputStream(bais);
                T object = (T) IOUtility.readObject(is);
                return object;
            }
        }
        catch(Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(is);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#storeObject(java.lang.String, java.lang.Object)
     */
    @Override
    public <T> void storeObject(String name, T object)
    {
        try {
            QualifiedName qname = getQualifiedName(name);
            ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
            synchronizer.add(qname);
            if(object != null) {
                OutputStream os = null;
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    os = new BufferedOutputStream(baos);
                    IOUtility.writeObject(os,object);
                    IOUtility.closeIO(os);
                    synchronizer.setSyncInfo(qname,mResource,baos.toByteArray());
                    return;
                }
                catch(IOException ioe) {
                    EclipseNSISPlugin.getDefault().log(ioe);
                }
                finally {
                    IOUtility.closeIO(os);
                }
            }
            synchronizer.setSyncInfo(qname,mResource,null);
        }
        catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    @Override
    public void removeObject(String name)
    {
        storeObject(name, null);
    }

    public NSISSettings getParentSettings()
    {
        return mParentSettings;
    }
}