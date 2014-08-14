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

import java.util.Calendar;

public class SchedulerConstants
{
    public static final boolean DEFAULT_AUTO_UPDATE = false;
    public static final boolean DEFAULT_IGNORE_PREVIEW = false;

    public static final int SCHEDULE_ON_STARTUP = 0;
    public static final int SCHEDULE_DAILY = 1;
    public static final int SCHEDULE_WEEKLY = 2;
    public static final int SCHEDULE_MONTHLY = 3;
    public static final int DEFAULT_SCHEDULE = SCHEDULE_ON_STARTUP;

    public static final int UPDATE_NOTIFY = 0;
    public static final int UPDATE_DOWNLOAD = 1;
    public static final int UPDATE_INSTALL = 3;
    public static final int DEFAULT_ACTION = UPDATE_NOTIFY;

    public static final int DEFAULT_TIME_OF_DAY = 0;

    public static final int[] DAYS_OF_WEEK;
    public static final int DEFAULT_DAY_OF_WEEK = 0;

    public static final int[] DAYS_OF_MONTH;
    public static final int DEFAULT_DAY_OF_MONTH = 0;

    static {
        Calendar cal = Calendar.getInstance();

        DAYS_OF_WEEK = new int[cal.getMaximum(Calendar.DAY_OF_WEEK)-cal.getMinimum(Calendar.DAY_OF_WEEK)+1];
        int firstDay = cal.getFirstDayOfWeek();
        for(int i=0; i<DAYS_OF_WEEK.length; i++) {
            DAYS_OF_WEEK[i]= firstDay++;
            if(firstDay > cal.getMaximum(Calendar.DAY_OF_WEEK)) {
                firstDay = cal.getMinimum(Calendar.DAY_OF_WEEK);
            }
        }

        DAYS_OF_MONTH = new int[cal.getMaximum(Calendar.DAY_OF_MONTH)-cal.getMinimum(Calendar.DAY_OF_MONTH)+1];
        int day = cal.getMinimum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < DAYS_OF_MONTH.length; i++) {
            DAYS_OF_MONTH[i] = day++;
        }
    }

    public static int validateSchedule(int schedule)
    {
        switch(schedule) {
            case SCHEDULE_ON_STARTUP:
            case SCHEDULE_DAILY:
            case SCHEDULE_WEEKLY:
            case SCHEDULE_MONTHLY:
                return schedule;
            default:
                return DEFAULT_SCHEDULE;
        }
    }

    public static int validateAction(int action)
    {
        switch(action) {
            case UPDATE_NOTIFY:
            case UPDATE_DOWNLOAD:
            case UPDATE_INSTALL:
                return action;
            default:
                return DEFAULT_ACTION;
        }
    }
}
