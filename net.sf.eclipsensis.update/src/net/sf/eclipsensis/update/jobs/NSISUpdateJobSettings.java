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

import net.sf.eclipsensis.update.scheduler.SchedulerConstants;

public class NSISUpdateJobSettings
{
    private boolean mAutomated = false;
    private int mAction = SchedulerConstants.UPDATE_NOTIFY;
    private boolean mIgnorePreview = false;
    public static final Object JOB_FAMILY = new Object();

    public NSISUpdateJobSettings()
    {
        this(false,SchedulerConstants.UPDATE_NOTIFY,false);
    }

    public NSISUpdateJobSettings(boolean automated, int action, boolean ignorePreview)
    {
        this(automated, action);
        mIgnorePreview = ignorePreview;
    }

    public NSISUpdateJobSettings(boolean automated, int action)
    {
        mAutomated = automated;
        mAction = action;
    }

    public boolean isAutomated()
    {
        return mAutomated;
    }

    public int getAction()
    {
        return mAction;
    }

    public boolean isIgnorePreview()
    {
        return mIgnorePreview;
    }
}
