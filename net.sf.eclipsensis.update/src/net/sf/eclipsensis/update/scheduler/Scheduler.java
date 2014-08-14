/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.scheduler;

import java.lang.reflect.Constructor;
import java.util.Calendar;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateJobSettings;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class Scheduler implements IStartup, IUpdatePreferenceConstants
{
    private static Scheduler INSTANCE = null;

    private Job mScheduledJob = null;
    private boolean mStartedUp = false;
    private IPreferenceStore mPreferences;

    public static Scheduler getInstance()
    {
        return INSTANCE;
    }

    public Scheduler()
    {
        if(INSTANCE == null) {
            INSTANCE = this;
            mPreferences = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
        }
    }

    public void earlyStartup()
    {
        scheduleUpdateJob();
    }

    public void shutDown()
    {
        Job.getJobManager().cancel(NSISUpdateJobSettings.JOB_FAMILY);
        mScheduledJob = null;
    }

    public synchronized void scheduleUpdateJob()
    {
        if (INSTANCE == this) {
            long delay = -1;
            if (mPreferences.getBoolean(IUpdatePreferenceConstants.AUTO_UPDATE)) {
                int schedule = mPreferences.getInt(IUpdatePreferenceConstants.UPDATE_SCHEDULE);
                switch (schedule)
                {
                    case SchedulerConstants.SCHEDULE_ON_STARTUP:
                    {
                        if (!mStartedUp) {
                            delay = 0;
                        }
                        break;
                    }
                    case SchedulerConstants.SCHEDULE_DAILY:
                    {
                        delay = computeDelayForDailySchedule();
                        break;
                    }
                    case SchedulerConstants.SCHEDULE_WEEKLY:
                    {
                        delay = computeDelayForWeeklySchedule();
                        break;
                    }
                    case SchedulerConstants.SCHEDULE_MONTHLY:
                    {
                        delay = computeDelayForMonthlySchedule();
                        break;
                    }
                    default:
                        return;
                }
            }
            schedule(delay);
            if(delay >=0 && !mStartedUp) {
                mStartedUp = true;
            }
        }
    }

    private synchronized void schedule(long delay)
    {
        if(mScheduledJob != null) {
            Job.getJobManager().cancel(mScheduledJob);
            mScheduledJob = null;
        }
        if (delay >= 0) {
            int updateAction = mPreferences.getInt(IUpdatePreferenceConstants.UPDATE_ACTION);
            if(updateAction < SchedulerConstants.UPDATE_NOTIFY || updateAction > SchedulerConstants.UPDATE_INSTALL) {
                updateAction = SchedulerConstants.DEFAULT_ACTION;
            }
            boolean ignorePreview = mPreferences.getBoolean(IUpdatePreferenceConstants.IGNORE_PREVIEW);
            NSISUpdateJobSettings settings = new NSISUpdateJobSettings(true, updateAction, ignorePreview);
            mScheduledJob = createUpdateJob(settings);
            if (mScheduledJob != null) {
                mScheduledJob.schedule(delay);
            }
        }
    }

    /*
     * Loads the update job using reflection to avoid premature startup of the
     * EclipseNSIS plug-in.
     */
    private Job createUpdateJob(final NSISUpdateJobSettings settings)
    {
        //Create the job in the UI thread so that we don't deadlock the classloader
        final Job[] job = {null};
        Runnable r= new Runnable() {
            @SuppressWarnings("unchecked")
            public void run()
            {
                try {
                    Class<? extends Job> theClass = (Class<? extends Job>) Class.forName("net.sf.eclipsensis.update.jobs.NSISCheckUpdateJob"); //$NON-NLS-1$
                    Constructor<? extends Job> constructor = theClass.getConstructor(new Class[]{NSISUpdateJobSettings.class});
                    job[0] = constructor.newInstance(new Object[]{settings});
                }
                catch (Exception e) {
                    EclipseNSISUpdatePlugin.getDefault().log(e);
                    job[0] = null;
                }
            }
        };
        if(Display.getCurrent() != null) {
            r.run();
        }
        else {
            Display.getDefault().syncExec(r);
        }

        return job[0];
    }

    private long computeDelayForDailySchedule()
    {
        int targetTime = mPreferences.getInt(IUpdatePreferenceConstants.DAILY_TIME) % 86400;
        Calendar cal = Calendar.getInstance();
        int currentTime = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND);
        int currentMilliSec = cal.get(Calendar.MILLISECOND);

        if(currentTime == targetTime) {
            return 0;
        }

        return (targetTime - currentTime + (currentTime >= targetTime?86400:0)) * 1000 - currentMilliSec;
    }

    private long computeDelayForWeeklySchedule()
    {
        int m = mPreferences.getInt(IUpdatePreferenceConstants.DAY_OF_WEEK);
        if(m >= 0 && m < SchedulerConstants.DAYS_OF_WEEK.length) {
            int targetDay = SchedulerConstants.DAYS_OF_WEEK[m];
            int targetTime = mPreferences.getInt(IUpdatePreferenceConstants.WEEKLY_TIME) % 86400;

            Calendar cal = Calendar.getInstance();

            int currentDay = cal.get(Calendar.DAY_OF_WEEK);
            int currentTime = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND);
            int currentMilliSec = cal.get(Calendar.MILLISECOND);

            if(currentDay == targetDay && currentTime == targetTime) {
                return 0;
            }
            int dayDiff = targetDay - currentDay;
            if (targetDay < currentDay ||
                (targetDay == currentDay && targetTime < currentTime)) {
                dayDiff += 7;
            }
            return (dayDiff*86400 + targetTime - currentTime)*1000 - currentMilliSec;
        }
        return -1;
    }

    private long computeDelayForMonthlySchedule()
    {
        int m = mPreferences.getInt(IUpdatePreferenceConstants.DAY_OF_MONTH);
        if(m >= 0 && m < SchedulerConstants.DAYS_OF_MONTH.length) {
            long targetTime = mPreferences.getInt(IUpdatePreferenceConstants.MONTHLY_TIME);
            int targetDay = SchedulerConstants.DAYS_OF_MONTH[m];

            Calendar cal = Calendar.getInstance();

            long now = cal.getTimeInMillis();
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH);
            int currentDay = cal.get(Calendar.DAY_OF_MONTH);
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int currentTime = cal.get(Calendar.HOUR_OF_DAY)*3600 + cal.get(Calendar.MINUTE)*60 + cal.get(Calendar.SECOND);


            if(currentDay == (targetDay>maxDay?maxDay:targetDay) && currentTime == targetTime) {
                return 0;
            }
            cal.set(Calendar.MILLISECOND,0);
            cal.set(Calendar.SECOND,(int)(targetTime % 60));
            targetTime /= 60;
            cal.set(Calendar.MINUTE,(int)(targetTime % 60));
            targetTime /= 60;
            cal.set(Calendar.HOUR_OF_DAY,(int)targetTime);

            cal.set(Calendar.DAY_OF_MONTH, (targetDay>maxDay?maxDay:targetDay));
            targetTime = cal.getTimeInMillis();

            if (targetTime < now) {
                int targetYear = currentYear;
                int targetMonth = currentMonth + 1;
                if (targetMonth > cal.getMaximum(Calendar.MONTH)) {
                    targetYear++;
                    targetMonth = cal.getMinimum(Calendar.MONTH);
                }
                cal.set(Calendar.YEAR, targetYear);
                cal.set(Calendar.MONTH, targetMonth);
                maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, (targetDay > maxDay?maxDay:targetDay));
                targetTime = cal.getTimeInMillis();
            }
            if(targetTime >= now) {
                return targetTime-now;
            }
        }
        return -1;
    }
}
