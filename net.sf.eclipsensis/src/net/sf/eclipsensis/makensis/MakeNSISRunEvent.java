/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import org.eclipse.core.runtime.IPath;

public class MakeNSISRunEvent
{
    public static final int STARTED = 0;
    public static final int STOPPED = 1;
    public static final int CREATED_PROCESS = 2;
    public static final int COMPLETED_PROCESS = 3;

    private int mType;
    private IPath mScript;
    private Object mData;

    MakeNSISRunEvent(int type, IPath script)
    {
        this(type, script, null);
    }

    MakeNSISRunEvent(int type, IPath script, Object data)
    {
        mType = type;
        mScript = script;
        mData = data;
    }

    public Object getData()
    {
        return mData;
    }

    public IPath getScript()
    {
        return mScript;
    }

    public int getType()
    {
        return mType;
    }
}
