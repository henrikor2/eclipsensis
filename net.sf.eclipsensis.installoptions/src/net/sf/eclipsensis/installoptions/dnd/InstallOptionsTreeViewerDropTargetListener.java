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
import org.eclipse.gef.commands.*;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.swt.dnd.*;

public class InstallOptionsTreeViewerDropTargetListener extends AbstractTransferDropTargetListener
{
    public InstallOptionsTreeViewerDropTargetListener(EditPartViewer viewer)
    {
        super(viewer, InstallOptionsTreeViewerTransfer.INSTANCE);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Request createTargetRequest()
    {
        ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
        request.setEditParts((List<EditPart>)InstallOptionsTreeViewerTransfer.INSTANCE.getObject());
        return request;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Command getCommand()
    {
        CompoundCommand command = new CompoundCommand();

        Iterator<EditPart> iter = ((List<EditPart>)InstallOptionsTreeViewerTransfer.INSTANCE.getObject()).iterator();

        Request  request = getTargetRequest();
        request.setType(isMove() ? RequestConstants.REQ_MOVE : RequestConstants.REQ_ORPHAN);

        while (iter.hasNext()) {
            EditPart editPart = iter.next();
            command.add(editPart.getCommand(request));
        }

        //If reparenting, add all editparts to target editpart.
        if (!isMove()) {
            request.setType(RequestConstants.REQ_ADD);
            if (getTargetEditPart() == null) {
                command.add(UnexecutableCommand.INSTANCE);
            }
            else {
                command.add(getTargetEditPart().getCommand(getTargetRequest()));
            }
        }
        return command;
    }

    protected String getCommandName() {
        if (isMove()) {
            return RequestConstants.REQ_MOVE;
        }
        return RequestConstants.REQ_ADD;
    }

    @Override
    protected Collection<EditPart> getExclusionSet() {
        List<EditPart> selection = Common.makeGenericList(EditPart.class, getViewer().getSelectedEditParts());
        List<EditPart> exclude = new ArrayList<EditPart>(selection);
        exclude.addAll(includeChildren(selection));
        return exclude;
    }

    @Override
    protected void handleDragOver()
    {
        if (InstallOptionsTreeViewerTransfer.INSTANCE.getViewer() != getViewer()) {
            getCurrentEvent().detail = DND.DROP_NONE;
            return;
        }
        getCurrentEvent().feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
        super.handleDragOver();
    }

    @SuppressWarnings("unchecked")
    protected EditPart getSourceEditPart()
    {
        List<EditPart> selection = (List<EditPart>)InstallOptionsTreeViewerTransfer.INSTANCE.getObject();
        if (selection == null
          || selection.isEmpty()) {
            return null;
        }
        return selection.get(0);
    }

    protected List<EditPart> includeChildren(List<EditPart> list)
    {
        List<EditPart> result = new ArrayList<EditPart>();
        for (int i = 0; i < list.size(); i++) {
            List<EditPart> children = Common.makeGenericList(EditPart.class, (list.get(i)).getChildren());
            result.addAll(children);
            result.addAll(includeChildren(children));
        }
        return result;
    }

    @Override
    public boolean isEnabled(DropTargetEvent event)
    {
        if (event.detail != DND.DROP_MOVE) {
            return false;
        }
        return super.isEnabled(event);
    }

    @SuppressWarnings("unchecked")
    protected boolean isMove()
    {
        EditPart source = getSourceEditPart();
        List<EditPart> selection = (List<EditPart>)InstallOptionsTreeViewerTransfer.INSTANCE.getObject();
        for (int i = 0; i < selection.size(); i++) {
            EditPart ep = selection.get(i);
            if (ep.getParent() != source.getParent()) {
                return false;
            }
        }
        return source.getParent() == getTargetEditPart();
    }

    @Override
    protected void updateTargetRequest()
    {
        ChangeBoundsRequest request = (ChangeBoundsRequest)getTargetRequest();
        request.setLocation(getDropLocation());
        request.setType(getCommandName());
    }
}
