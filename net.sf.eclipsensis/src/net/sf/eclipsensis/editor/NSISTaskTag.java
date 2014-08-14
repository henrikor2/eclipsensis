/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.io.Serializable;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IMarker;

public class NSISTaskTag implements Serializable
{
    public static final String[] PRIORITY_LABELS;
    private static final long serialVersionUID = -5058661625944134857L;

    private String mTag;
    private int mPriority;
    private boolean mDefault = false;

    static {
        PRIORITY_LABELS = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"task.priority.labels"); //$NON-NLS-1$
    }

    /**
     *
     */
    public NSISTaskTag()
    {
        this("",IMarker.PRIORITY_NORMAL); //$NON-NLS-1$
    }

    /**
     * @param tag
     * @param priority
     */
    public NSISTaskTag(String tag, int priority)
    {
        super();
        mTag = tag;
        mPriority = priority;
    }

    /**
     * @param name
     * @param priority
     */
    public NSISTaskTag(NSISTaskTag taskTag)
    {
        this(taskTag.getTag(),taskTag.getPriority());
        setDefault(taskTag.isDefault());
    }

    /**
     * @return Returns the name.
     */
    public String getTag()
    {
        return mTag;
    }

    /**
     * @param name The name to set.
     */
    public void setTag(String name)
    {
        mTag = name;
    }

    /**
     * @return Returns the priority.
     */
    public int getPriority()
    {
        return mPriority;
    }

    /**
     * @param priority The priority to set.
     */
    public void setPriority(int priority)
    {
        mPriority = priority;
    }

    /**
     * @return Returns the default.
     */
    public boolean isDefault()
    {
        return mDefault;
    }

    /**
     * @param default1 The default to set.
     */
    public void setDefault(boolean default1)
    {
        mDefault = default1;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getTag();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof NSISTaskTag) {
            NSISTaskTag tag = (NSISTaskTag)obj;
            return mTag.equals(tag.getTag()) && (mPriority == tag.getPriority()) &&
                   (mDefault == tag.isDefault());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return mTag.hashCode()+mPriority+(mDefault?1:0);
    }
}
