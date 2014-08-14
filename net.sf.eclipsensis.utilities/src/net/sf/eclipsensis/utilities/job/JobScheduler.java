/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.utilities.job;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.ui.progress.UIJob;

public class JobScheduler
{
    private boolean mRunning = false;
    private Set<Object> mJobFamilies = new HashSet<Object>();

    public JobScheduler()
    {
        super();
    }

    public void start()
    {
        if(!mRunning) {
            mRunning = true;
        }
    }

    public void stop()
    {
        if(mRunning) {
            mRunning = false;
            IJobManager manager = Job.getJobManager();
            if(manager != null) {
                for(Iterator<Object> iter = mJobFamilies.iterator(); iter.hasNext(); ) {
                    manager.cancel(iter.next());
                    iter.remove();
                }
            }
        }
    }

    public void scheduleUIJob(String name, IJobStatusRunnable runnable)
    {
        scheduleJob(null, name, runnable);
    }

    public void scheduleUIJob(Object family, String name, final IJobStatusRunnable runnable)
    {
        final Object jobFamily = (family == null?this:family);

        new UIJob(name) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor)
            {
                return runnable.run(monitor);
            }

            @Override
            public boolean belongsTo(Object family)
            {
                return jobFamily.equals(family);
            }

        }.schedule();
    }

    public void scheduleJob(String name, IJobStatusRunnable runnable)
    {
        scheduleJob(null, runnable);
    }

    public void scheduleJob(Object family, String name, final IJobStatusRunnable runnable)
    {
        final Object jobFamily = (family == null?this:family);

        new Job(name) {
            @Override
            public IStatus run(IProgressMonitor monitor)
            {
                return runnable.run(monitor);
            }

            @Override
            public boolean belongsTo(Object family)
            {
                return jobFamily.equals(family);
            }

        }.schedule();
    }

    public void cancelJobs(Object family)
    {
        if(family != null && mRunning && mJobFamilies.contains(family)) {
            mJobFamilies.remove(family);
            Job.getJobManager().cancel(family);
        }
    }
}
