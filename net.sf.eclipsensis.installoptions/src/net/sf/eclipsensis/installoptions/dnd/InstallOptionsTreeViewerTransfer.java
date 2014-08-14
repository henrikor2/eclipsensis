/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dnd;

import net.sf.eclipsensis.util.ObjectTransfer;

import org.eclipse.gef.EditPartViewer;

public class InstallOptionsTreeViewerTransfer extends ObjectTransfer
{
    public static final InstallOptionsTreeViewerTransfer INSTANCE = new InstallOptionsTreeViewerTransfer();
    private static final String[] TYPE_NAMES = new String[]{"Local Transfer"//$NON-NLS-1$
                                                            + System.currentTimeMillis()
                                                            + ":" + INSTANCE.hashCode()};//$NON-NLS-1$
    private static final int[] TYPE_IDS = new int[] {registerType(TYPE_NAMES[0])};

    private static EditPartViewer viewer;

    private InstallOptionsTreeViewerTransfer()
    {
    }

    @Override
    protected int[] getTypeIds()
    {
        return TYPE_IDS;
    }

    @Override
    protected String[] getTypeNames()
    {
        return TYPE_NAMES;
    }

    public EditPartViewer getViewer()
    {
        return viewer;
    }

    public void setViewer(EditPartViewer epv)
    {
        viewer = epv;
    }
}
