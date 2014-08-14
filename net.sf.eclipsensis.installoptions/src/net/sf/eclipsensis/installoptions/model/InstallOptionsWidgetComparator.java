/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.Comparator;

public final class InstallOptionsWidgetComparator implements Comparator<InstallOptionsWidget>
{
    public static final Comparator<InstallOptionsWidget> INSTANCE = new InstallOptionsWidgetComparator(false);
    public static final Comparator<InstallOptionsWidget> REVERSE_INSTANCE = new InstallOptionsWidgetComparator(true);

    private boolean mReversed = false;

    public InstallOptionsWidgetComparator(boolean reversed)
    {
        super();
        mReversed = reversed;
    }

    public int compare(InstallOptionsWidget w1, InstallOptionsWidget w2)
    {
        return (mReversed?-1:1)*(w1.getIndex()-w2.getIndex());
    }
}