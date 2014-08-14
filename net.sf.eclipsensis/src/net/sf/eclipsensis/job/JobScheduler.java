/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.job;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.ui.progress.UIJob;

public class JobScheduler
{
    private boolean mRunning = false;
    private Set<Object> mJobFamilies = new HashSet<Object>();

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

    public boolean isScheduled(Object family)
    {
        return !Common.isEmptyArray(Job.getJobManager().find(family));
    }

    public void scheduleUIJob(String name, IJobStatusRunnable runnable)
    {
        scheduleUIJob(name, (ISchedulingRule)null, runnable);
    }

    public void scheduleUIJob(String name, ISchedulingRule rule, IJobStatusRunnable runnable)
    {
        scheduleUIJob(null, name, rule, runnable);
    }

    public void scheduleUIJob(Object family, String name, final IJobStatusRunnable runnable)
    {
        scheduleUIJob(family, name, null, runnable);
    }

    public void scheduleUIJob(Object family, String name, ISchedulingRule rule, final IJobStatusRunnable runnable)
    {
        scheduleUIJob(family, name, null, runnable, 0L);
    }

    public void scheduleUIJob(Object family, String name, final IJobStatusRunnable runnable, long delay)
    {
        scheduleUIJob(family, name, null, runnable, delay);
    }

    public void scheduleUIJob(Object family, String name, ISchedulingRule rule, final IJobStatusRunnable runnable, long delay)
    {
        final Object jobFamily = (family == null?this:family);
        mJobFamilies.add(jobFamily);
        UIJob job = new UIJob(name) {
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
        };
        if(rule != null) {
            job.setRule(rule);
        }
        job.schedule(delay);
    }

    public void scheduleJob(String name, IJobStatusRunnable runnable)
    {
        scheduleJob(name, (ISchedulingRule)null, runnable);
    }

    public void scheduleJob(String name, ISchedulingRule rule, IJobStatusRunnable runnable)
    {
        scheduleJob(null, name, rule, runnable);
    }

    public void scheduleJob(Object family, String name, final IJobStatusRunnable runnable)
    {
        scheduleJob(family, name, runnable, 0L);
    }

    public void scheduleJob(Object family, String name, ISchedulingRule rule, final IJobStatusRunnable runnable)
    {
        scheduleJob(family, name, rule, runnable, 0L);
    }

    /**
     * @param family
     * @param name
     * @param runnable
     * @param delay
     */
    public void scheduleJob(Object family, String name, final IJobStatusRunnable runnable, long delay)
    {
        scheduleJob(family, name, null, runnable, delay);
    }

    /**
     * @param family
     * @param name
     * @param runnable
     * @param delay
     */
    public void scheduleJob(Object family, String name, ISchedulingRule rule, final IJobStatusRunnable runnable, long delay)
    {
        final Object jobFamily = (family == null?this:family);
        mJobFamilies.add(jobFamily);

        Job job = new Job(name) {
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
        };
        if(rule != null) {
            job.setRule(rule);
        }
        job.schedule(delay);
    }

    public void cancelJobs(Object family)
    {
        cancelJobs(family, true);
    }

    public void cancelJobs(Object family, boolean wait)
    {
        if(family != null && mRunning && mJobFamilies.contains(family)) {
            mJobFamilies.remove(family);
            Job.getJobManager().cancel(family);
            if (wait)
            {
                try
                {
                    Job.getJobManager().join(family, new NullProgressMonitor());
                }
                catch (Exception e)
                {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }
}
