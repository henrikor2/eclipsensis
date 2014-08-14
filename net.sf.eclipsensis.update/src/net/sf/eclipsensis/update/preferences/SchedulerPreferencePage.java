/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import java.text.DateFormatSymbols;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.scheduler.*;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class SchedulerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IUpdatePreferenceConstants
{
    private static String[] cDaysOfWeek;

    private Button mAutoUpdate;
    private Button mStartup;
    private Button mDaily;
    private Button mWeekly;
    private Button mMonthly;
    private DateTime mDailyTime;
    private Combo mDayOfWeek;
    private DateTime mWeeklyTime;
    private Combo mDayOfMonth;
    private DateTime mMonthlyTime;
    private Button mNotify;
    private Button mDownload;
    private Button mInstall;

    static {
        DateFormatSymbols dfs = new DateFormatSymbols();
        cDaysOfWeek = new String[SchedulerConstants.DAYS_OF_WEEK.length];
        for (int i = 0; i < SchedulerConstants.DAYS_OF_WEEK.length; i++) {
            cDaysOfWeek[i]= dfs.getWeekdays()[SchedulerConstants.DAYS_OF_WEEK[i]];
        }
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore()
    {
        return EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Composite parent2 = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent2.setLayout(layout);

        mAutoUpdate = new Button(parent2,SWT.CHECK);
        mAutoUpdate.setText(EclipseNSISUpdatePlugin.getResourceString("auto.update.label")); //$NON-NLS-1$
        mAutoUpdate.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        createScheduleGroup(parent2);
        createActionGroup(parent2);

        loadPreferences();

        new Enabler(mAutoUpdate, new Control[] {mStartup,mDaily,mWeekly,mMonthly,
                    mNotify,mDownload,mInstall}).run();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent2,EclipseNSISUpdatePlugin.PLUGIN_CONTEXT_PREFIX+"nsis_scheduler_prefs_context"); //$NON-NLS-1$
        return parent2;
    }

    private void loadPreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();
        mAutoUpdate.setSelection(prefs.getBoolean(AUTO_UPDATE));

        int updateSchedule = SchedulerConstants.validateSchedule(prefs.getInt(UPDATE_SCHEDULE));

        mStartup.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_ON_STARTUP);

        mDaily.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_DAILY);
        setTime(mDailyTime, prefs.getInt(DAILY_TIME));

        mWeekly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_WEEKLY);
        mDayOfWeek.select(prefs.getInt(DAY_OF_WEEK));
        setTime(mWeeklyTime, prefs.getInt(WEEKLY_TIME));

        mMonthly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_MONTHLY);
        mDayOfMonth.select(prefs.getInt(DAY_OF_MONTH));
        setTime(mMonthlyTime, prefs.getInt(MONTHLY_TIME));

        int updateAction = SchedulerConstants.validateAction(prefs.getInt(UPDATE_ACTION));

        mNotify.setSelection(updateAction == SchedulerConstants.UPDATE_NOTIFY);
        mDownload.setSelection(updateAction == SchedulerConstants.UPDATE_DOWNLOAD);
        mInstall.setSelection(updateAction == SchedulerConstants.UPDATE_INSTALL);
    }

    private void loadDefaults()
    {
        IPreferenceStore prefs = getPreferenceStore();
        mAutoUpdate.setSelection(prefs.getDefaultBoolean(AUTO_UPDATE));

        int updateSchedule = prefs.getDefaultInt(UPDATE_SCHEDULE);
        mStartup.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_ON_STARTUP);

        mDaily.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_DAILY);
        setTime(mDailyTime, prefs.getDefaultInt(DAILY_TIME));

        mWeekly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_WEEKLY);
        mDayOfWeek.select(prefs.getDefaultInt(DAY_OF_WEEK));
        setTime(mWeeklyTime, prefs.getDefaultInt(WEEKLY_TIME));

        mMonthly.setSelection(updateSchedule == SchedulerConstants.SCHEDULE_MONTHLY);
        mDayOfMonth.select(prefs.getDefaultInt(DAY_OF_MONTH));
        setTime(mMonthlyTime, prefs.getDefaultInt(MONTHLY_TIME));

        int updateAction = prefs.getDefaultInt(UPDATE_ACTION);
        mNotify.setSelection(updateAction == SchedulerConstants.UPDATE_NOTIFY);
        mDownload.setSelection(updateAction == SchedulerConstants.UPDATE_DOWNLOAD);
        mInstall.setSelection(updateAction == SchedulerConstants.UPDATE_INSTALL);
    }

    private void savePreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();
        prefs.setValue(AUTO_UPDATE,mAutoUpdate.getSelection());

        int updateSchedule = (mStartup.getSelection()?SchedulerConstants.SCHEDULE_ON_STARTUP:
                              (mDaily.getSelection()?SchedulerConstants.SCHEDULE_DAILY:
                               (mWeekly.getSelection()?SchedulerConstants.SCHEDULE_WEEKLY:
                                SchedulerConstants.SCHEDULE_MONTHLY)));
        prefs.setValue(UPDATE_SCHEDULE, updateSchedule);

        prefs.setValue(DAILY_TIME,getTime(mDailyTime));

        prefs.setValue(DAY_OF_WEEK,mDayOfWeek.getSelectionIndex());
        prefs.setValue(WEEKLY_TIME,getTime(mWeeklyTime));

        prefs.setValue(DAY_OF_MONTH,mDayOfMonth.getSelectionIndex());
        prefs.setValue(MONTHLY_TIME,getTime(mMonthlyTime));

        int updateAction = (mNotify.getSelection()?SchedulerConstants.UPDATE_NOTIFY:
                            (mDownload.getSelection()?SchedulerConstants.UPDATE_DOWNLOAD:
                             SchedulerConstants.UPDATE_INSTALL));
        prefs.setValue(UPDATE_ACTION, updateAction);
    }

    private void createScheduleGroup(Composite parent)
    {
        GridLayout layout;
        Label l;
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.group.label")); //$NON-NLS-1$
        layout = new GridLayout(5,false);
        group.setLayout(layout);

        mStartup = new Button(group, SWT.RADIO);
        mStartup.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.startup.label")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,false);
        gridData.horizontalSpan = 5;
        mStartup.setLayoutData(gridData);

        mDaily = new Button(group, SWT.RADIO);
        mDaily.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.daily.label")); //$NON-NLS-1$
        mDaily.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.daily.time.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mDailyTime = createTimePicker(group);
        mDailyTime.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        Label l2 = new Label(group,SWT.NONE);
        l2.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        ((GridData)l2.getLayoutData()).horizontalSpan = 2;

        new Enabler(mDaily, new Control[] {l, mDailyTime});

        mWeekly = new Button(group, SWT.RADIO);
        mWeekly.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.weekly.label")); //$NON-NLS-1$
        mWeekly.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.weekly.day.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mDayOfWeek = new Combo(group, SWT.DROP_DOWN|SWT.READ_ONLY);
        mDayOfWeek.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        mDayOfWeek.setItems(cDaysOfWeek);
        mDayOfWeek.select(SchedulerConstants.DEFAULT_DAY_OF_WEEK);

        l2 = new Label(group,SWT.NONE);
        l2.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.weekly.time.label")); //$NON-NLS-1$
        l2.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mWeeklyTime = createTimePicker(group);
        mWeeklyTime.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));

        new Enabler(mWeekly, new Control[] {l, mDayOfWeek, l2, mWeeklyTime});

        mMonthly = new Button(group, SWT.RADIO);
        mMonthly.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.monthly.label")); //$NON-NLS-1$
        mMonthly.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

        l = new Label(group,SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.monthly.day.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mDayOfMonth = new Combo(group, SWT.DROP_DOWN|SWT.READ_ONLY);
        mDayOfMonth.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
        for(int i=0; i<SchedulerConstants.DAYS_OF_MONTH.length; i++) {
            mDayOfMonth.add(Integer.toString(SchedulerConstants.DAYS_OF_MONTH[i]));
        }
        mDayOfMonth.select(SchedulerConstants.DEFAULT_DAY_OF_MONTH);

        l2 = new Label(group,SWT.NONE);
        l2.setText(EclipseNSISUpdatePlugin.getResourceString("update.schedule.monthly.time.label")); //$NON-NLS-1$
        l2.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
        mMonthlyTime = createTimePicker(group);
        mMonthlyTime.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));

        new Enabler(mMonthly, new Control[] {l, mDayOfMonth, l2, mMonthlyTime});
    }

    private void createActionGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1,false));

        mNotify = new Button(group, SWT.RADIO);
        mNotify.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.notify.label")); //$NON-NLS-1$
        mNotify.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        mDownload = new Button(group, SWT.RADIO);
        mDownload.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.download.label")); //$NON-NLS-1$
        mDownload.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        mInstall = new Button(group, SWT.RADIO);
        mInstall.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.install.label")); //$NON-NLS-1$
        mInstall.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
    }

    private DateTime createTimePicker(Composite parent)
    {
        DateTime dt = new DateTime(parent,SWT.TIME|SWT.SHORT);
        setTime(dt, 0);
        return dt;
    }

    private void setTime(DateTime dt, int time)
    {
        int time2 = time;
        dt.setSeconds(time2 % 60);
        time2 /= 60;
        dt.setMinutes(time2 % 60);
        time2 /= 60;
        dt.setHours(time2);
    }

    private int getTime(DateTime dt)
    {
        return dt.getHours()*3600+dt.getMinutes()*60+dt.getSeconds();
    }

    @Override
    public boolean performOk()
    {
        boolean ok = super.performOk();
        if(ok) {
            savePreferences();
            Scheduler scheduler = Scheduler.getInstance();
            if(scheduler != null) {
                scheduler.scheduleUpdateJob();
            }
        }
        return ok;
    }

    @Override
    protected void performDefaults()
    {
        loadDefaults();
        Enabler enabler = Enabler.get(mAutoUpdate);
        if (enabler != null) {
            enabler.run();
        }
        super.performDefaults();
    }

    public void init(IWorkbench workbench)
    {
    }
}
