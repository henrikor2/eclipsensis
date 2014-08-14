/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.viewer;

import org.eclipse.jface.viewers.*;

public abstract class ListViewerUpDownMover<S, T> extends StructuredViewerUpDownMover<S, T>
{
    private ListViewer mListViewer;

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#setInput(java.lang.Object)
     */
    @Override
    public void setViewer(StructuredViewer viewer)
    {
        mListViewer = (ListViewer)viewer;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#getInput()
     */
    @Override
    public StructuredViewer getViewer()
    {
        return mListViewer;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.util.UpDownMover#getSelectedIndices()
     */
    @Override
    protected int[] getSelectedIndices()
    {
        return mListViewer.getList().getSelectionIndices();
    }
}
