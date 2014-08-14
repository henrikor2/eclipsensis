/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.NSISSettings;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.*;
import org.eclipse.jface.viewers.IFilter;

public class NSISLaunchSettings extends NSISSettings
{
    public static final String SCRIPT = "script"; //$NON-NLS-1$
    public static final String RUN_INSTALLER = "runInstaller"; //$NON-NLS-1$

    private NSISSettings mParent;
    private String mScript = ""; //$NON-NLS-1$
    private boolean mRunInstaller = false;
    private ILaunchConfiguration mLaunchConfig;
    private IFilter mFilter = null;

    NSISLaunchSettings(NSISSettings parent)
    {
        this(parent, null);
    }

    NSISLaunchSettings(NSISSettings parent, ILaunchConfiguration launchConfig)
    {
        this(parent, launchConfig, null);
    }

    NSISLaunchSettings(NSISSettings parent, ILaunchConfiguration launchConfig, IFilter filter)
    {
        mParent = parent;
        setLaunchConfig(launchConfig);
        mFilter = filter;
        load();
    }

    public ILaunchConfiguration getLaunchConfig()
    {
        return mLaunchConfig;
    }

    public void setLaunchConfig(ILaunchConfiguration launchConfig)
    {
        mLaunchConfig = launchConfig;
    }

    public boolean getRunInstaller()
    {
        return mRunInstaller;
    }

    public void setRunInstaller(boolean runInstaller)
    {
        mRunInstaller = runInstaller;
    }

    public String getScript()
    {
        return mScript;
    }

    public void setScript(String script)
    {
        mScript = script;
    }

    @Override
    public void load()
    {
        mScript = getString(SCRIPT);
        mRunInstaller = getBoolean(RUN_INSTALLER);
        super.load();
    }

    @Override
    public void store()
    {
        String file = null;
        try {
            file = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(mScript);
        }
        catch (CoreException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        if(file != null) {
            File f = new File(file);
            if(f.exists()) {
                IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(f.toURI());
                if(!Common.isEmptyArray(files) && mLaunchConfig instanceof ILaunchConfigurationWorkingCopy) {
                    ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setMappedResources(files);
                }
            }
        }
        setValue(SCRIPT, mScript);
        setValue(RUN_INSTALLER, mRunInstaller);
        super.store();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String name)
    {
        if(mFilter == null || mFilter.select(name)) {
            boolean defaultValue = mParent.getBoolean(name);
            if(mLaunchConfig != null) {
                try {
                    return mLaunchConfig.getAttribute(name, defaultValue);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            return defaultValue;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getInt(java.lang.String)
     */
    @Override
    public int getInt(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            int defaultValue = mParent.getInt(name);
            if (mLaunchConfig != null) {
                try {
                    return mLaunchConfig.getAttribute(name, defaultValue);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            return defaultValue;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getString(java.lang.String)
     */
    @Override
    public String getString(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            String defaultValue = mParent.getString(name);
            if (mLaunchConfig != null) {
                try {
                    return mLaunchConfig.getAttribute(name, defaultValue);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            return defaultValue;
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, boolean)
     */
    @Override
    public void setValue(String name, boolean value)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, value);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, int)
     */
    @Override
    public void setValue(String name, int value)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, value);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, java.lang.String)
     */
    @Override
    public void setValue(String name, String value)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, value);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeBoolean(java.lang.String)
     */
    @Override
    public void removeBoolean(String name)
    {
        remove(name);
    }

    private void remove(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (String)null);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeInt(java.lang.String)
     */
    @Override
    public void removeInt(String name)
    {
        remove(name);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeString(java.lang.String)
     */
    @Override
    public void removeString(String name)
    {
        remove(name);
    }

    private List<String> storeSymbols(Map<String,String> map)
    {
        List<String> list = new ArrayList<String>();
        if (!Common.isEmptyMap(map)) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            for (Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<String, String> entry = iter.next();
                String key = entry.getKey();
                if (key != null) {
                    buf.append(key);
                }
                String value = entry.getValue();
                if (value != null) {
                    buf.append('\255').append(value);
                }
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    buf.setLength(0);
                }
            }
        }
        return list;
    }

    private Map<String, String> loadSymbols(List<String> list)
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        if (!Common.isEmptyCollection(list)) {
            for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
                String item = iter.next();
                if (item != null && item.length() > 0) {
                    String key;
                    String value;
                    int n = item.indexOf('\255');
                    if (n >= 0) {
                        key = item.substring(0, n);
                        value = item.substring(n + 1);
                    }
                    else {
                        key = item;
                        value = ""; //$NON-NLS-1$
                    }
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void storeObject(String name, T object)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null && mLaunchConfig.isWorkingCopy()) {
                if (object instanceof String) {
                    ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (String)object);
                }
                else if (object instanceof List<?>) {
                    ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (List<String>)object);
                }
                else if (object instanceof Map<?,?>) {
                    if (name.equals(SYMBOLS)) {
                        List<String> list = storeSymbols((Map<String, String>)object);
                        ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, list);
                    }
                    else {
                        ((ILaunchConfigurationWorkingCopy)mLaunchConfig).setAttribute(name, (Map<?,?>)object);
                    }
                }
                else if (object == null) {
                    remove(name);
                }
            }
        }
    }

    @Override
    public void removeObject(String name)
    {
        storeObject(name, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadObject(String name)
    {
        if (mFilter == null || mFilter.select(name)) {
            if (mLaunchConfig != null) {
                try {
                    Map<?,?> map = mLaunchConfig.getAttributes();
                    T object = (T) map.get(name);
                    if (name.equals(SYMBOLS) && object instanceof List<?>) {
                        object = (T) loadSymbols((List)object);
                    }
                    return object;
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    e.printStackTrace();
                }
            }
            return mParent.loadObject(name);
        }
        return null;
    }
}
