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

public interface IUpdatePreferenceConstants
{
    public static final int PLUGIN_PREFERENCES_VERSION_NUMBER = 1;

    public static final String PLUGIN_PREFERENCES_VERSION = "pluginPreferencesVersion"; //$NON-NLS-1$

    public static final String NSIS_UPDATE_SITE = "nsisUpdateSite"; //$NON-NLS-1$
    public static final String AUTOSELECT_SOURCEFORGE_MIRROR = "autoselectSourceforgeMirror"; //$NON-NLS-1$
    public static final String SOURCEFORGE_MIRROR = "sourceforgeMirror"; //$NON-NLS-1$

    public static final String IGNORE_PREVIEW = "ignorePreview"; //$NON-NLS-1$

    public static final String AUTO_UPDATE = "autoUpdate"; //$NON-NLS-1$
    public static final String UPDATE_SCHEDULE = "updateSchedule"; //$NON-NLS-1$
    public static final String UPDATE_ACTION = "updateAction"; //$NON-NLS-1$
    public static final String DAILY_TIME = "dailyTime"; //$NON-NLS-1$
    public static final String DAY_OF_WEEK = "dayOfWeek"; //$NON-NLS-1$
    public static final String WEEKLY_TIME = "weeklyTime"; //$NON-NLS-1$
    public static final String DAY_OF_MONTH = "dayOfMonth"; //$NON-NLS-1$
    public static final String MONTHLY_TIME = "monthlyTime"; //$NON-NLS-1$
}
