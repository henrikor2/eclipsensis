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

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;

import org.eclipse.core.runtime.preferences.*;

public class UpdatePreferenceInitializer extends AbstractPreferenceInitializer implements IUpdatePreferenceConstants
{
    @Override
    public void initializeDefaultPreferences()
    {
        IEclipsePreferences prefs = new DefaultScope().getNode(EclipseNSISUpdatePlugin.getDefault().getPluginId());

        prefs.putInt(PLUGIN_PREFERENCES_VERSION, 0);

        prefs.put(NSIS_UPDATE_SITE, NSISUpdateURLs.getDefaultUpdateSite());
        prefs.put(SOURCEFORGE_MIRROR, NSISUpdateURLs.getDefaultDownloadSite());
        prefs.putBoolean(AUTOSELECT_SOURCEFORGE_MIRROR, true);

        prefs.putBoolean(IGNORE_PREVIEW, SchedulerConstants.DEFAULT_IGNORE_PREVIEW);

        prefs.putBoolean(AUTO_UPDATE, SchedulerConstants.DEFAULT_AUTO_UPDATE);
        prefs.putInt(UPDATE_SCHEDULE, SchedulerConstants.DEFAULT_SCHEDULE);
        prefs.putInt(UPDATE_ACTION, SchedulerConstants.DEFAULT_ACTION);
        prefs.putInt(DAILY_TIME, SchedulerConstants.DEFAULT_TIME_OF_DAY);
        prefs.putInt(DAY_OF_WEEK, SchedulerConstants.DEFAULT_DAY_OF_WEEK);
        prefs.putInt(WEEKLY_TIME, SchedulerConstants.DEFAULT_TIME_OF_DAY);
        prefs.putInt(DAY_OF_MONTH, SchedulerConstants.DEFAULT_DAY_OF_MONTH);
        prefs.putInt(MONTHLY_TIME, SchedulerConstants.DEFAULT_TIME_OF_DAY);
    }
}
