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

import java.util.*;

import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.*;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;


public class InstallOptionsTreeViewerDragSourceListener extends AbstractTransferDragSourceListener
{
    private List<Object> mModelSelection;

    public InstallOptionsTreeViewerDragSourceListener(EditPartViewer viewer)
    {
        super(viewer, InstallOptionsTreeViewerTransfer.INSTANCE);
    }

    public void dragSetData(DragSourceEvent event)
    {
        event.data = getViewer().getSelectedEditParts();
    }

    @Override
    public void dragStart(DragSourceEvent event)
    {
        InstallOptionsTreeViewerTransfer.INSTANCE.setViewer(getViewer());
        List<EditPart> selection = Common.makeGenericList(EditPart.class, getViewer().getSelectedEditParts());
        InstallOptionsTreeViewerTransfer.INSTANCE.setObject(selection);
        saveModelSelection(selection);
    }

    @Override
    public void dragFinished(DragSourceEvent event)
    {
        InstallOptionsTreeViewerTransfer.INSTANCE.setObject(null);
        InstallOptionsTreeViewerTransfer.INSTANCE.setViewer(null);
        revertModelSelection();
    }

    protected void revertModelSelection()
    {
        List<EditPart> list = new ArrayList<EditPart>();
        for (int i = 0; i < mModelSelection.size(); i++) {
            list.add((EditPart) getViewer().getEditPartRegistry().get(mModelSelection.get(i)));
        }
        getViewer().setSelection(new StructuredSelection(list));
    }

    protected void saveModelSelection(List<?> editPartSelection)
    {
        mModelSelection = new ArrayList<Object>();
        for (int i = 0; i < editPartSelection.size(); i++) {
            EditPart editpart = (EditPart)editPartSelection.get(i);
            mModelSelection.add(editpart.getModel());
        }
    }
}
