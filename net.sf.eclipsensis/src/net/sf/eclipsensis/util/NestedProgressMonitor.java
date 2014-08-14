/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import org.eclipse.core.runtime.*;

public class NestedProgressMonitor extends ProgressMonitorWrapper
{
    private int mTicks;
    private double mScale;
    private double mUsed = 0.0;
    private boolean mCompleted = false;
    private boolean mHasSubTask = false;
    private String mParentTaskName;
    private String mPrefix;

    public NestedProgressMonitor(IProgressMonitor monitor, String parentTaskName, int ticks)
    {
        this(monitor, parentTaskName, parentTaskName, ticks);
    }

    public NestedProgressMonitor(IProgressMonitor monitor, String parentTaskName, String prefix, int ticks)
    {
        super(monitor);
        mParentTaskName = parentTaskName;
        mTicks = ticks;
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmpty(prefix)) {
            //Add an ellipse
            int n = 3;
            if(prefix.length() > n) {
                for(;n>0;n--) {
                    if(prefix.charAt(prefix.length()-(4-n)) != '.') {
                        break;
                    }
                }
            }
            buf.append(prefix);
            for(int i=0; i<n; i++) {
                buf.append('.');
            }
        }
        mPrefix = buf.toString();
    }

    @Override
    public void beginTask(String name, int totalWork)
    {
        mScale = totalWork <= 0 ? 0 : (double) mTicks / (double) totalWork;
        setTaskName(name);
    }

    @Override
    public void setTaskName(String name)
    {
        String name2 = name;
        if(!Common.isEmpty(name2)) {
            if(!Common.isEmpty(mPrefix)) {
                if(!mPrefix.substring(0,mPrefix.length()-3).equalsIgnoreCase(name2) && !mPrefix.regionMatches(true,0,name2,0,mPrefix.length())) {
                    name2 = mPrefix+name2;
                }
            }
            super.setTaskName(name2);
        }
    }

    @Override
    public void done()
    {
        double remaining = mTicks - mUsed;
        if (remaining > 0) {
            super.internalWorked(remaining);
        }
        if (mHasSubTask) {
            subTask(""); //$NON-NLS-1$
        }
        mTicks = 0;
        mUsed = 0;
        setTaskName(mParentTaskName);
    }

    @Override
    public void internalWorked(double work)
    {
        if (mCompleted) {
            return;
        }

        double realWork = mScale * work;
        super.internalWorked(realWork);
        mUsed += realWork;
        if (mUsed >= mTicks) {
            mCompleted = true;
        }
    }

    @Override
    public void subTask(String name)
    {
        mHasSubTask = true;
        super.subTask(name);
    }

    @Override
    public void worked(int work) {
        internalWorked(work);
    }
}
