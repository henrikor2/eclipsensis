/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.core.runtime.*;

public class NSISPluginManager implements INSISConstants
{
    public static final NSISPluginManager INSTANCE = new NSISPluginManager();
    private static final int NSIS_PLUGINS_EXTENSION_LENGTH = NSIS_PLUGINS_EXTENSION.length();
    private static final FileFilter PLUGIN_FILE_FILTER = new FileFilter() {
        public boolean accept(File pathname)
        {
            if(pathname.isFile()) {
                return pathname.getName().regionMatches(true,pathname.getName().length()-NSIS_PLUGINS_EXTENSION_LENGTH,
                                NSIS_PLUGINS_EXTENSION,0,NSIS_PLUGINS_EXTENSION_LENGTH);
            }
            return false;
        }

    };

    private Map<String, PluginInfo> mDefaultPluginsMap = null;
    private Map<File, Map<String, PluginInfo>> mCustomPluginsMap = new HashMap<File, Map<String, PluginInfo>>();
    private File mCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISPluginManager.class.getName()+".Plugins.ser"); //$NON-NLS-1$
    public static final Pattern PLUGIN_CALL_PATTERN=Pattern.compile("([a-z0-9\\$%\\'`\\-@\\{\\}~\\!#\\(\\)\\&_\\^\\+\\,\\=\\[\\]]+)::([a-z_][a-z0-9_]*)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private NSISPluginManager()
    {
    }

    void loadDefaultPlugins()
    {
        String nsisHome = "";
        NSISPreferences prefs = NSISPreferences.getInstance();
        if(prefs.getNSISHome() != null)
        {
            nsisHome = prefs.getNSISHome().getLocation().getAbsolutePath();
        }
        if(!Common.isEmpty(nsisHome)) {
            File nsisHomeDir = new File(nsisHome);
            if(IOUtility.isValidDirectory(nsisHomeDir)) {
                File nsisPluginsDir = new File(nsisHomeDir, NSIS_PLUGINS_LOCATION);
                if(IOUtility.isValidDirectory(nsisPluginsDir)) {
                    if(mCacheFile.exists()) {
                        try {
                            mDefaultPluginsMap = IOUtility.readObject(mCacheFile);
                        }
                        catch (Exception e) {
                            mDefaultPluginsMap = null;
                        }
                    }

                    boolean changed = false;
                    if(mDefaultPluginsMap == null) {
                        mDefaultPluginsMap = new CaseInsensitiveMap<PluginInfo>();
                        changed = true;
                    }

                    changed |= loadPlugins(nsisPluginsDir, mDefaultPluginsMap);

                    if(changed) {
                        try {
                            IOUtility.writeObject(mCacheFile, mDefaultPluginsMap);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void loadPlugins(File dir)
    {
        Map<String, PluginInfo> pluginsMap = mCustomPluginsMap.get(dir);
        if(pluginsMap == null) {
            pluginsMap = new CaseInsensitiveMap<PluginInfo>();
            mCustomPluginsMap.put(dir,pluginsMap);
        }
        loadPlugins(dir,pluginsMap);
    }

    private boolean loadPlugins(File dir, Map<String, PluginInfo> pluginsMap)
    {
        boolean changed = false;
        File[] pluginFiles = dir.listFiles(PLUGIN_FILE_FILTER);
        CaseInsensitiveSet set = new CaseInsensitiveSet();
        for (int i = 0; i < pluginFiles.length; i++) {
            String name = pluginFiles[i].getName();
            name = name.substring(0,name.length()-NSIS_PLUGINS_EXTENSION_LENGTH);
            set.add(name);
            PluginInfo pi = pluginsMap.get(name);
            if(pi == null || pi.getTimeStamp() != pluginFiles[i].lastModified()) {
                PluginInfo newPi = loadPluginInfo(name, pluginFiles[i]);
                if(newPi != null) {
                    pluginsMap.put(name, pi);
                    changed = true;
                }
                else if(pi != null) {
                    pluginsMap.remove(name);
                    changed = true;
                }
            }
        }
        if(pluginsMap.size() != pluginFiles.length) {
            for (Iterator<Map.Entry<String, PluginInfo>> iter = pluginsMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<String, PluginInfo> entry = iter.next();
                if(!set.contains(entry.getKey())) {
                    iter.remove();
                    changed = true;
                }
            }
        }

        return changed;
    }

    private PluginInfo loadPluginInfo(String name, File pluginFile)
    {
        try {
            String[] exports = WinAPI.INSTANCE.getPluginExports(pluginFile.getAbsolutePath());
            if(exports != null) {
                Arrays.sort(exports, String.CASE_INSENSITIVE_ORDER);
                return new PluginInfo(name, exports,
                                pluginFile.lastModified());
            }
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        return null;
    }

    public String[] getDefaultPluginNames()
    {
        return mDefaultPluginsMap == null?Common.EMPTY_STRING_ARRAY:mDefaultPluginsMap.keySet().toArray(Common.EMPTY_STRING_ARRAY);
    }

    public String[] getDefaultPluginExports(String name)
    {
        if (mDefaultPluginsMap != null) {
            PluginInfo pi = mDefaultPluginsMap.get(name);
            if (pi == null) {
                File pluginFile = new File(new File(NSISPreferences.getInstance().getNSISHome().getLocation(), NSIS_PLUGINS_LOCATION), name + NSIS_PLUGINS_EXTENSION);
                if (IOUtility.isValidFile(pluginFile)) {
                    pi = loadPluginInfo(name, pluginFile);
                    if (pi != null) {
                        mDefaultPluginsMap.put(name, pi);
                        JobScheduler jobScheduler = EclipseNSISPlugin.getDefault().getJobScheduler();
                        jobScheduler.cancelJobs(NSISPluginManager.class);
                        jobScheduler.scheduleJob(NSISPluginManager.class, EclipseNSISPlugin.getResourceString("saving.plugin.cache.job.name"), //$NON-NLS-1$
                                        new IJobStatusRunnable() {
                            public IStatus run(IProgressMonitor monitor)
                            {
                                monitor.beginTask(EclipseNSISPlugin.getResourceString("caching.plugins.task.name"), 1); //$NON-NLS-1$
                                try {
                                    if (!monitor.isCanceled()) {
                                        try {
                                            IOUtility.writeObject(mCacheFile, mDefaultPluginsMap);
                                            monitor.worked(1);
                                            return Status.OK_STATUS;
                                        }
                                        catch (Throwable t) {
                                            return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, t.getMessage(), t);
                                        }
                                    }
                                    else {
                                        return Status.CANCEL_STATUS;
                                    }
                                }
                                finally {
                                    monitor.done();
                                }
                            }
                        }
                        );
                    }
                }
            }
            if (pi != null) {
                return pi.getExports();
            }
        }
        return Common.EMPTY_STRING_ARRAY;
    }


    public String[] getPluginNames(File dir)
    {
        Map<String, PluginInfo> map = mCustomPluginsMap.get(dir);
        if(map == null) {
            loadPlugins(dir);
            map = mCustomPluginsMap.get(dir);
        }
        if(map != null) {
            return map.keySet().toArray(Common.EMPTY_STRING_ARRAY);
        }
        return null;
    }

    public String[] getPluginExports(File dir, String name)
    {
        Map<String, PluginInfo> map = mCustomPluginsMap.get(dir);
        if(map == null) {
            loadPlugins(dir);
            map = mCustomPluginsMap.get(dir);
        }
        if(map != null) {
            PluginInfo pi = map.get(name);
            if(pi == null) {
                File pluginFile = new File(dir,name+NSIS_PLUGINS_EXTENSION);
                if(IOUtility.isValidFile(pluginFile)) {
                    pi = loadPluginInfo(name, pluginFile);
                    map.put(pi.getName(),pi);
                }
            }
            if(pi != null) {
                return pi.getExports();
            }
        }
        return null;
    }

    private static class PluginInfo implements Serializable
    {
        private static final long serialVersionUID = 3815184021913290871L;

        private long mTimeStamp;
        private String mName;
        private String[] mExports;

        /**
         * @param name
         * @param exports
         * @param timeStamp
         */
        public PluginInfo(String name, String[] exports, long timeStamp)
        {
            mName = name;
            mExports = exports;
            mTimeStamp = timeStamp;
        }

        /**
         * @return Returns the exports.
         */
        public String[] getExports()
        {
            return mExports;
        }

        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return mName;
        }

        /**
         * @return Returns the timeStamp.
         */
        public long getTimeStamp()
        {
            return mTimeStamp;
        }
    }
}
