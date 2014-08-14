/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.net.*;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;

public abstract class AbstractTemplateManager<T extends ITemplate>
{
    private File mUserTemplatesStore;
    private URL mDefaultTemplatesStore;

    private List<T> mTemplates;
    private Map<String, T> mDefaultTemplatesMap;
    private AbstractTemplateReaderWriter<T> mReaderWriter;

    public AbstractTemplateManager()
    {
        String fileName = getClass().getName()+".Templates.ser"; //$NON-NLS-1$

        mDefaultTemplatesStore = getPlugin().getBundle().getResource(getTemplatesPath().append(fileName).makeAbsolute().toString());

        File parentFolder = getPlugin().getStateLocation().toFile();
        File location = null;
        if(parentFolder != null) {
            location = new File(parentFolder,getTemplatesPath().toString());
            if(IOUtility.isValidFile(location)) {
                location.delete();
            }
            if(!location.exists()) {
                location.mkdirs();
            }
        }

        mUserTemplatesStore = new File(location,fileName);

        mReaderWriter = createReaderWriter();

        try {
            mDefaultTemplatesMap = loadDefaultTemplateStore();
        }
        catch (Exception e1) {
            EclipseNSISPlugin.getDefault().log(e1);
            mDefaultTemplatesMap = new HashMap<String, T>();
        }
        finally {
            if(mDefaultTemplatesMap == null) {
                mDefaultTemplatesMap = new HashMap<String, T>();
            }
        }

        mTemplates = new ArrayList<T>();
        loadTemplates();
    }

    /**
     *
     */
    protected final void loadTemplates()
    {
        mTemplates.clear();
        Map<String, T> map = new HashMap<String, T>();
        if(mDefaultTemplatesMap != null) {
            for (Iterator<T> iter = mDefaultTemplatesMap.values().iterator(); iter.hasNext();) {
                T template = iter.next();
                map.put(template.getName(),template);
            }
        }

        try {
            List<T> list = loadUserTemplateStore();
            if(list != null) {
                for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
                    T template = iter.next();
                    if(template.getType() == ITemplate.TYPE_CUSTOM) {
                        map.remove(template.getName());
                    }
                    mTemplates.add(template);
                }
            }
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        mTemplates.addAll(map.values());
    }

    public AbstractTemplateReaderWriter<T> getReaderWriter()
    {
        return mReaderWriter;
    }

    protected Map<String, T> loadDefaultTemplateStore() throws IOException, ClassNotFoundException
    {
        Map<String, T> map = null;
        if(mDefaultTemplatesStore != null) {
            InputStream stream = mDefaultTemplatesStore.openStream();
            map = IOUtility.readObject(stream, getClass().getClassLoader());
        }

        return  map;
    }

    @SuppressWarnings("unchecked")
    protected List<T> loadUserTemplateStore() throws IOException, ClassNotFoundException
    {
        List<T> list = null;
        if(IOUtility.isValidFile(mUserTemplatesStore)) {
            Object obj = IOUtility.readObject(mUserTemplatesStore, getClass().getClassLoader());
            if(obj instanceof Map<?, ?>) {
                //migrate
                Map<String, T> defaults = new HashMap<String, T>();
                if(mDefaultTemplatesMap != null) {
                    for (Iterator<T> iter = mDefaultTemplatesMap.values().iterator(); iter.hasNext();) {
                        T template = iter.next();
                        defaults.put(template.getName(),template);
                    }
                }
                list = new ArrayList<T>();
                for(Iterator<T> iter = ((Map<String, T>)obj).values().iterator(); iter.hasNext(); ) {
                    T template = iter.next();
                    switch(template.getType()) {
                        case ITemplate.TYPE_DEFAULT:
                            continue;
                        case ITemplate.TYPE_CUSTOM:
                            T t = defaults.get(template.getName());
                            if(t == null) {
                                template.setType(ITemplate.TYPE_USER);
                            }
                            else {
                                template.setId(t.getId());
                                if(templatesAreEqual(t,template)) {
                                   continue;
                                }
                            }
                            //$FALL-THROUGH$
                        case ITemplate.TYPE_USER:
                            list.add(template);
                    }
                }
                IOUtility.writeObject(mUserTemplatesStore, list);
            }
            else if(obj instanceof List<?>) {
                list = (List<T>)obj;
            }
            else if(obj instanceof Collection<?>) {
                list = new ArrayList<T>((Collection<T>)obj);
            }
        }
        return list;
    }

    protected URL getDefaultTemplatesStore()
    {
        return mDefaultTemplatesStore;
    }

    protected File getUserTemplatesStore()
    {
        return mUserTemplatesStore;
    }

    public T getTemplate(String id)
    {
        for (Iterator<T> iter = mTemplates.iterator(); iter.hasNext();) {
            T template = iter.next();
            if(Common.stringsAreEqual(template.getId(),id)) {
                return template;
            }
        }
        return null;
    }

    public Collection<T> getTemplates()
    {
        return Collections.unmodifiableCollection(mTemplates);
    }

    public Collection<T> getDefaultTemplates()
    {
        return Collections.unmodifiableCollection(mDefaultTemplatesMap.values());
    }

    public boolean addTemplate(final T template)
    {
        checkClass(template);
        template.setId(null);
        template.setType(ITemplate.TYPE_USER);
        template.setDeleted(false);
        mTemplates.add(template);
        return true;
    }

    protected boolean containsTemplate(T template)
    {
        for (Iterator<T> iter = mTemplates.iterator(); iter.hasNext();) {
            T t = iter.next();
            if(templatesAreEqual(t,template)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean removeTemplate(T template)
    {
        checkClass(template);
        if(containsTemplate(template)) {
            switch(template.getType()) {
                case ITemplate.TYPE_DEFAULT:
                case ITemplate.TYPE_CUSTOM:
                    T t = (T)template.clone();
                    t.setType(ITemplate.TYPE_CUSTOM);
                    t.setDeleted(true);
                    mTemplates.add(t);
                    //$FALL-THROUGH$
                case ITemplate.TYPE_USER:
                    mTemplates.remove(template);
            }
            return true;
        }
        return false;
    }

    public boolean updateTemplate(T oldTemplate, final T template)
    {
        checkClass(oldTemplate);
        checkClass(template);
        if(containsTemplate(oldTemplate)) {
            mTemplates.remove(oldTemplate);

            if(oldTemplate.getType() != ITemplate.TYPE_USER) {
                T defaultTemplate = mDefaultTemplatesMap.get(oldTemplate.getId());
                if(defaultTemplate != null) {
                    template.setType(templatesAreEqual(template,defaultTemplate)?ITemplate.TYPE_DEFAULT:ITemplate.TYPE_CUSTOM);
                }
                else {
                    template.setType(ITemplate.TYPE_USER);
                }
            }
            template.setDeleted(false);
            mTemplates.add(template);
            return true;
        }
        return false;
    }

    public void restore()
    {
        for (Iterator<T> iter = mTemplates.iterator(); iter.hasNext();) {
            restore(iter.next());
        }
    }

    protected boolean restore(T template)
    {
        checkClass(template);
        if(template.getType() == ITemplate.TYPE_CUSTOM && template.isDeleted()) {
            template.setDeleted(false);
            T defaultTemplate = mDefaultTemplatesMap.get(template.getId());
            if(templatesAreEqual(template,defaultTemplate)) {
                template.setType(ITemplate.TYPE_DEFAULT);
            }
            return true;
        }
        return false;
    }

    public boolean canRestore()
    {
        for (Iterator<T> iter = mTemplates.iterator(); iter.hasNext();) {
            T template = iter.next();
            checkClass(template);
            if(template.getType() == ITemplate.TYPE_CUSTOM && template.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    public void resetToDefaults()
    {
        mTemplates.clear();
        mTemplates.addAll(mDefaultTemplatesMap.values());
    }

    public T revert(T template)
    {
        checkClass(template);
        if(template.getType() == ITemplate.TYPE_CUSTOM) {
            T defaultTemplate = mDefaultTemplatesMap.get(template.getId());
            checkClass(defaultTemplate);
            mTemplates.remove(template);
            mTemplates.add(defaultTemplate);
            return defaultTemplate;
        }
        return null;
    }

    public boolean canRevert(T template)
    {
        checkClass(template);
        return (containsTemplate(template) && (template.getType() == ITemplate.TYPE_CUSTOM));
    }

    public void discard()
    {
        loadTemplates();
    }

    /**
     * @throws IOException
     *
     */
    public void save() throws IOException
    {
        Map<String, T> map = new LinkedHashMap<String, T>();
        for (Iterator<T> iter = mTemplates.iterator(); iter.hasNext();) {
            T template = iter.next();
            checkClass(template);

            if(System.getProperty("manage.default.templates") != null) { //$NON-NLS-1$
                template.setType(ITemplate.TYPE_DEFAULT);
                map.put(template.getId(),template);
            }
            else {
                if(template.getType() != ITemplate.TYPE_DEFAULT) {
                    map.put(template.getName(),template);
                }
            }
        }
        if(System.getProperty("manage.default.templates") != null) { //$NON-NLS-1$
            mDefaultTemplatesMap.clear();
            mDefaultTemplatesMap.putAll(map);
            mTemplates.clear();
            URL fileURL = FileLocator.toFileURL(mDefaultTemplatesStore);
            File file;
            try
            {
                file = new File(fileURL.toURI());
            }
            catch(URISyntaxException e)
            {
                file = new File(fileURL.getPath());
            }
            IOUtility.writeObject(file, new HashMap<Object, T>(mDefaultTemplatesMap));
        }
        else {
            IOUtility.writeObject(mUserTemplatesStore, new ArrayList<T>(map.values()));
        }
    }

    private final void checkClass(T template)
    {
        if(template != null && !getTemplateClass().isAssignableFrom(template.getClass())) {
            throw new IllegalArgumentException(template.getClass().getName());
        }
    }

    private boolean templatesAreEqual(T t1, T t2)
    {
        checkClass(t1);
        checkClass(t2);
        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        return t1.isEqualTo(t2);
    }

    protected abstract Plugin getPlugin();
    protected abstract Image getShellImage();
    protected abstract IPath getTemplatesPath();
    protected abstract Class<T> getTemplateClass();
    protected abstract AbstractTemplateReaderWriter<T> createReaderWriter();
}
