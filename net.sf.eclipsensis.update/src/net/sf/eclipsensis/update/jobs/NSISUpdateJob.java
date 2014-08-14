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

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.scheduler.Scheduler;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.swt.widgets.Display;

public abstract class NSISUpdateJob extends Job
{
    protected static final ISchedulingRule SCHEDULING_RULE = new ISchedulingRule() {
        public boolean contains(ISchedulingRule rule)
        {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule)
        {
            return rule == this;
        }
    };
    private NSISUpdateJobSettings mSettings;

    NSISUpdateJob(String name, NSISUpdateJobSettings settings)
    {
        super(name);
        mSettings = settings;
        setRule(SCHEDULING_RULE);
        if(!settings.isAutomated()) {
            setUser(true);
            setPriority(Job.INTERACTIVE);
        }
    }

    public NSISUpdateJobSettings getSettings()
    {
        return mSettings;
    }

    protected void handleException(final Exception e)
    {
        EclipseNSISUpdatePlugin.getDefault().log(e);
        if(!getSettings().isAutomated()) {
            displayExec(new Runnable() {
                public void run()
                {
                    Common.openError(Display.getCurrent().getActiveShell(), formatException(e), EclipseNSISUpdatePlugin.getShellImage());
                }
            });
        }
    }

    @Override
    public final boolean belongsTo(Object family)
    {
        return family == this || family == NSISUpdateJobSettings.JOB_FAMILY;
    }

    protected String formatException(Throwable e)
    {
        return e.getMessage();
    }

    @Override
    public final IStatus run(IProgressMonitor monitor)
    {
        IStatus status = doRun(monitor);
        if(shouldReschedule()) {
            Scheduler scheduler = Scheduler.getInstance();
            if(scheduler != null) {
                scheduler.scheduleUpdateJob();
            }
        }
        return status;
    }

    protected void displayExec(Runnable op)
    {
        if(getSettings().isAutomated()) {
            Display.getDefault().asyncExec(op);
        }
        else {
            Display.getDefault().syncExec(op);
        }
    }

    protected abstract boolean shouldReschedule();
    protected abstract IStatus doRun(IProgressMonitor monitor);
}
