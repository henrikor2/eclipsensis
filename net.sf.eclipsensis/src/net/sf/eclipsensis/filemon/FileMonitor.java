/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.filemon;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.IOUtility;

public class FileMonitor
{
    public static final long POLL_INTERVAL;
    public static final int FILE_MODIFIED = 0;
    public static final int FILE_DELETED = 1;
    public static final int FILE_CREATED = 2;

    private Object mLock = new Object();

    public static final FileMonitor INSTANCE = new FileMonitor();

    private Timer mTimer;
    private Map<File, FileChangeRegistryEntry> mRegistry = new LinkedHashMap<File, FileChangeRegistryEntry>();
    private FileChangeTimerTask mTask;

    static {
        long interval;
        try {
            interval = Long.parseLong(EclipseNSISPlugin.getResourceString("file.change.monitor.poll.interval","500")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch(Throwable t) {
            interval = 500;
        }
        POLL_INTERVAL = interval;
    }

    private FileMonitor()
    {
        super();
    }

    public boolean start()
    {
        synchronized (mLock) {
            if (mTimer == null && mTask == null) {
                mTimer = new Timer(true);
                mTask = new FileChangeTimerTask();
                mTimer.schedule(mTask, 0, POLL_INTERVAL);
                return true;
            }
        }
        return false;
    }

    public boolean stop()
    {
        synchronized (mLock) {
            if (mTimer != null && mTask != null) {
                mTimer.cancel();
                mTimer = null;
                mTask.cancel();
                mTask = null;
                return true;
            }
        }
        return false;
    }

    public void register(File file, IFileChangeListener listener)
    {
        FileChangeRegistryEntry entry = mRegistry.get(file);
        if(entry == null) {
            entry = new FileChangeRegistryEntry();
            if(IOUtility.isValidFile(file)) {
                entry.lastModified = file.lastModified();
            }
            else {
                entry.lastModified = -1;
            }
            mRegistry.put(file,entry);
        }
        for(Iterator<WeakReference<IFileChangeListener>> iter=entry.listeners.iterator(); iter.hasNext(); ) {
            if(iter.next().get() == listener) {
                return;
            }
        }
        entry.listeners.add(new WeakReference<IFileChangeListener>(listener));
    }

    public void unregister(File file, IFileChangeListener listener)
    {
        FileChangeRegistryEntry entry = mRegistry.get(file);
        if(entry != null) {
            for(Iterator<WeakReference<IFileChangeListener>> iter=entry.listeners.iterator(); iter.hasNext(); ) {
                if(iter.next().get() == listener) {
                    iter.remove();
                    if(entry.listeners.isEmpty()) {
                        if(!entry.removed) {
                            mRegistry.remove(file);
                            entry.removed = true;
                        }
                        return;
                    }
                }
            }
        }
    }

    public void unregister(File file)
    {
        FileChangeRegistryEntry entry = mRegistry.get(file);
        if(entry != null && !entry.removed) {
            mRegistry.remove(file);
            entry.removed = true;
        }
    }

    private class FileChangeTimerTask extends TimerTask
    {
        private boolean mCanceled = false;

        @Override
        public boolean cancel()
        {
            if(super.cancel()) {
                mCanceled = true;
                return true;
            }
            return false;
        }

        @Override
        public void run()
        {
            Thread.currentThread().setName(EclipseNSISPlugin.getResourceString("file.monitor.thread.name")); //$NON-NLS-1$
            File[] files = mRegistry.keySet().toArray(new File[mRegistry.size()]);
            for(int i=0; i<files.length; i++) {
                if(mCanceled) {
                    return;
                }
                File file = files[i];
                FileChangeRegistryEntry entry = mRegistry.get(file);
                if(!IOUtility.isValidFile(file)) {
                    /* Sleep 50 ms & see if the file shows up again- i.e.,
                     * we caught the event in the middle of a move operation. */
                    try {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    if(!IOUtility.isValidFile(file)) {
                        if(entry.lastModified != -1) {
                            /* Yup, it's really gone. Bummer. */
                            entry.lastModified = -1;
                            fireChanged(FILE_DELETED, file, entry);
                        }
                        continue;
                    }
                }
                long lastModified = file.lastModified();
                if(lastModified != entry.lastModified) {
                    int event = (entry.lastModified == -1?FILE_CREATED:FILE_MODIFIED);
                    entry.lastModified = lastModified;
                    fireChanged(event, file, entry);
                }
                if(!entry.removed && entry.listeners.isEmpty()) {
                    mRegistry.remove(file);
                    entry.removed = true;
                }
            }
        }

        private void fireChanged(int type, File file, FileChangeRegistryEntry entry)
        {
            List<WeakReference<IFileChangeListener>> listeners = new ArrayList<WeakReference<IFileChangeListener>>(entry.listeners);
            for (WeakReference<IFileChangeListener> weakref : listeners) {
                IFileChangeListener listener = weakref.get();
                if(listener == null) {
                    entry.listeners.remove(weakref);
                }
                else {
                    listener.fileChanged(type, file);
                }
            }
        }
    }

    private class FileChangeRegistryEntry
    {
        long lastModified;
        List<WeakReference<IFileChangeListener>> listeners = new ArrayList<WeakReference<IFileChangeListener>>();
        boolean removed = false;
    }
}
