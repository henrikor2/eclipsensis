/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;
import net.sf.eclipsensis.installoptions.template.CreateFromTemplateCommand;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.*;
import org.eclipse.gef.rulers.RulerProvider;

public class InstallOptionsXYLayoutEditPolicy extends XYLayoutEditPolicy implements IInstallOptionsConstants
{
    private static final Command DUMMY_COMMAND = new Command() {
    };

    public InstallOptionsXYLayoutEditPolicy(XYLayout layout)
    {
        super();
        setXyLayout(layout);
    }

    @Override
    public Command getCommand(Request request)
    {
        if(request.getType().equals(IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE)) {
            return getCreateFromTemplateCommand((CreateRequest)request);
        }
        else {
            return super.getCommand(request);
        }
    }

    protected Command chainGuideAttachmentCommand(Request request,
            InstallOptionsWidget part, Command cmd, boolean horizontal)
    {
        Command result = cmd;

        // Attach to guide, if one is given
        Integer guidePos = (Integer)request.getExtendedData().get(
                horizontal?SnapToGuides.KEY_HORIZONTAL_GUIDE
                        :SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos != null) {
            int alignment = ((Integer)request.getExtendedData().get(
                    horizontal?SnapToGuides.KEY_HORIZONTAL_ANCHOR
                            :SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
            ChangeGuideCommand cgm = new ChangeGuideCommand(part, horizontal);
            cgm.setNewGuide(findGuideAt(guidePos.intValue(), horizontal),
                    alignment);
            result = result.chain(cgm);
        }

        return result;
    }

    protected Command chainGuideDetachmentCommand(Request request,
            InstallOptionsWidget part, Command cmd, boolean horizontal)
    {
        Command result = cmd;

        // Detach from guide, if none is given
        Integer guidePos = (Integer)request.getExtendedData().get(
                horizontal?SnapToGuides.KEY_HORIZONTAL_GUIDE
                        :SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos == null) {
            result = result.chain(new ChangeGuideCommand(part, horizontal));
        }

        return result;
    }

    @Override
    protected Command createAddCommand(EditPart child, Object constraint)
    {
        return null;
    }

    protected Command createAddCommand(Request request, EditPart childEditPart, Object constraint)
    {
        InstallOptionsWidget part = (InstallOptionsWidget)childEditPart.getModel();
        Position pos = (Position)constraint;

        AddCommand add = new AddCommand();
        add.setParent((InstallOptionsDialog)getHost().getModel());
        add.setChild(part);
        add.setLabel(InstallOptionsPlugin.getResourceString("add.command.label")); //$NON-NLS-1$
        add.setDebugLabel("InstallOptionsXYEP add subpart");//$NON-NLS-1$

        SetConstraintCommand setConstraint = new SetConstraintCommand(part, pos, null, null);
        setConstraint.setLabel(InstallOptionsPlugin.getResourceString("set.constaint.command.label")); //$NON-NLS-1$
        setConstraint.setDebugLabel("InstallOptionsXYEP setConstraint");//$NON-NLS-1$

        Command cmd = add.chain(setConstraint);
        cmd = chainGuideAttachmentCommand(request, part, cmd, true);
        cmd = chainGuideAttachmentCommand(request, part, cmd, false);
        cmd = chainGuideDetachmentCommand(request, part, cmd, true);
        return chainGuideDetachmentCommand(request, part, cmd, false);
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     *      java.lang.Object)
     */
    @Override
    protected Command createChangeConstraintCommand(EditPart child,
            Object constraint)
    {
        return null;
    }

    @Override
    protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint)
    {
        InstallOptionsWidget part = (InstallOptionsWidget)child.getModel();
        Point moveDelta;
        Dimension sizeDelta;
        if(request instanceof AlignmentRequest) {
            Position oldPos = part.toGraphical(part.getPosition());
            Position newPos = (Position)constraint;
            Dimension diff = oldPos.getLocation().getDifference(newPos.getLocation());
            moveDelta = new Point(diff.width,diff.height);
            sizeDelta = IInstallOptionsConstants.EMPTY_DIMENSION;
            if(moveDelta.equals(IInstallOptionsConstants.EMPTY_POINT) && sizeDelta.isEmpty()) {
                return DUMMY_COMMAND;
            }
        }
        else {
            moveDelta = request.getMoveDelta();
            sizeDelta = request.getSizeDelta();
        }
        if(part.isLocked()) {
            return UnexecutableCommand.INSTANCE;
        }
        Command result = new SetConstraintCommand(part, (Position)constraint,
                                                  moveDelta,
                                                  sizeDelta);

        EditPartViewer viewer = child.getViewer();
        boolean canGlue = Common.isTrue((Boolean)viewer.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY)) &&
                            Common.isTrue((Boolean)viewer.getProperty(PROPERTY_SNAP_TO_GUIDES)) &&
                            Common.isTrue((Boolean)viewer.getProperty(PROPERTY_GLUE_TO_GUIDES));
        if ((request.getResizeDirection() & PositionConstants.NORTH_SOUTH) != 0) {
            Integer guidePos = (canGlue?(Integer)request.getExtendedData().get(SnapToGuides.KEY_HORIZONTAL_GUIDE):null);
            if (guidePos != null) {
                result = chainGuideAttachmentCommand(request, part, result,
                        true);
            }
            else if (part.getHorizontalGuide() != null) {
                // SnapToGuides didn't provide a horizontal guide, but this part is attached
                // to a horizontal guide. Now we check to see if the part is attached to
                // the guide along the edge being resized. If that is the case, we need to
                // detach the part from the guide; otherwise, we leave it alone.
                int alignment = part.getHorizontalGuide().getAlignment(part);
                int edgeBeingResized = 0;
                if ((request.getResizeDirection() & PositionConstants.NORTH) != 0) {
                    edgeBeingResized = -1;
                }
                else {
                    edgeBeingResized = 1;
                }
                if (alignment == edgeBeingResized) {
                    result = result.chain(new ChangeGuideCommand(part, true));
                }
            }
        }

        if ((request.getResizeDirection() & PositionConstants.EAST_WEST) != 0) {
            Integer guidePos = (canGlue?(Integer)request.getExtendedData().get(SnapToGuides.KEY_VERTICAL_GUIDE):null);
            if (guidePos != null) {
                result = chainGuideAttachmentCommand(request, part, result,
                        false);
            }
            else if (part.getVerticalGuide() != null) {
                // SnapToGuides didn't provide a vertical guide, but this part is attached
                // to a vertical guide. Now we check to see if the part is attached to
                // the guide along the edge being resized. If that is the case, we need to
                // detach the part from the guide; otherwise, we leave it alone.
                int alignment = part.getVerticalGuide().getAlignment(part);
                int edgeBeingResized = 0;
                if ((request.getResizeDirection() & PositionConstants.WEST) != 0) {
                    edgeBeingResized = -1;
                }
                else {
                    edgeBeingResized = 1;
                }
                if (alignment == edgeBeingResized) {
                    result = result.chain(new ChangeGuideCommand(part, false));
                }
            }
        }

        if (request.getType().equals(REQ_MOVE_CHILDREN) ||
            request.getType().equals(REQ_ALIGN_CHILDREN)) {
            if(canGlue) {
                result = chainGuideAttachmentCommand(request, part, result, true);
                result = chainGuideAttachmentCommand(request, part, result, false);
                result = chainGuideDetachmentCommand(request, part, result, true);
                result = chainGuideDetachmentCommand(request, part, result, false);
            }
            else {
                result = result.chain(new ChangeGuideCommand(part, true));
                result = result.chain(new ChangeGuideCommand(part, false));
            }
        }
        return result;
    }

    @Override
    protected EditPolicy createChildEditPolicy(EditPart child)
    {
        return new InstallOptionsResizableEditPolicy(child);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createSizeOnDropFeedback(org.eclipse.gef.requests.CreateRequest)
     */
    @Override
    protected IFigure createSizeOnDropFeedback(CreateRequest createRequest)
    {
        IFigure figure;

        figure = new RectangleFigure();
        ((RectangleFigure)figure).setXOR(true);
        ((RectangleFigure)figure).setFill(true);
        figure.setBackgroundColor(IInstallOptionsConstants.GHOST_FILL_COLOR);
        figure.setForegroundColor(ColorConstants.white);

        addFeedback(figure);

        return figure;
    }

    protected InstallOptionsGuide findGuideAt(int pos, boolean horizontal)
    {
        RulerProvider provider = ((RulerProvider)getHost().getViewer()
                .getProperty(
                        horizontal?RulerProvider.PROPERTY_VERTICAL_RULER
                                :RulerProvider.PROPERTY_HORIZONTAL_RULER));
        return (InstallOptionsGuide)provider.getGuideAt(pos);
    }

    @Override
    protected Command getAddCommand(Request generic)
    {
        ChangeBoundsRequest request = (ChangeBoundsRequest)generic;
        List<?> editParts = request.getEditParts();
        CompoundCommand command = new CompoundCommand();
        command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$
        GraphicalEditPart childPart;
        Rectangle r;
        Object constraint;

        for (int i = 0; i < editParts.size(); i++) {
            childPart = (GraphicalEditPart)editParts.get(i);
            r = childPart.getFigure().getBounds().getCopy();
            //convert r to absolute from childpart figure
            childPart.getFigure().translateToAbsolute(r);
            r = request.getTransformedRectangle(r);
            //convert this figure to relative
            getLayoutContainer().translateToRelative(r);
            getLayoutContainer().translateFromParent(r);
            r.translate(getLayoutOrigin().getNegated());
            constraint = getConstraintFor(r);
            command.add(createAddCommand(generic, childPart, translateToModelConstraint(constraint)));
        }
        return command.unwrap();
    }

    @Override
    protected Object translateToModelConstraint(Object figureConstraint)
    {
        Rectangle r= (Rectangle)figureConstraint;
        Position p = new Position(r.x,r.y,r.x+r.width-1,r.y+r.height-1);
        return p;
    }

    /**
     * Override to return the <code>Command</code> to perform an {@link
     * RequestConstants#REQ_CLONE CLONE}. By default, <code>null</code> is
     * returned.
     *
     * @param request
     *            the Clone Request
     * @return A command to perform the Clone.
     */
    @Override
    protected Command getCloneCommand(ChangeBoundsRequest request)
    {
        CloneCommand clone = new CloneCommand();

        clone.setParent((InstallOptionsDialog)getHost().getModel());

        Iterator<?> i = request.getEditParts().iterator();
        GraphicalEditPart currPart = null;

        while (i.hasNext()) {
            currPart = (GraphicalEditPart)i.next();
            clone.addWidget((InstallOptionsWidget)currPart.getModel(),
                    (Rectangle)getConstraintForClone(currPart, request));
        }

        // Attach to horizontal guide, if one is given
        Integer guidePos = (Integer)request.getExtendedData().get(
                SnapToGuides.KEY_HORIZONTAL_GUIDE);
        if (guidePos != null) {
            int hAlignment = ((Integer)request.getExtendedData().get(
                    SnapToGuides.KEY_HORIZONTAL_ANCHOR)).intValue();
            clone.setGuide(findGuideAt(guidePos.intValue(), true), hAlignment,
                    true);
        }

        // Attach to vertical guide, if one is given
        guidePos = (Integer)request.getExtendedData().get(
                SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos != null) {
            int vAlignment = ((Integer)request.getExtendedData().get(
                    SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
            clone.setGuide(findGuideAt(guidePos.intValue(), false), vAlignment,
                    false);
        }

        return clone;
    }

    @Override
    protected Command getCreateCommand(CreateRequest request)
    {
        CreateCommand create = new CreateCommand();
        create.setParent((InstallOptionsDialog)getHost().getModel());
        InstallOptionsWidget newPart = (InstallOptionsWidget)request
                .getNewObject();
        create.setChild(newPart);
        Rectangle constraint = (Rectangle)getConstraintFor(request);
        create.setLocation(constraint);
        create.setLabel(InstallOptionsPlugin.getResourceString("create.command.label")); //$NON-NLS-1$

        Command cmd = chainGuideAttachmentCommand(request, newPart, create,
                true);
        return chainGuideAttachmentCommand(request, newPart, cmd, false);
    }

    protected Command getCreateFromTemplateCommand(CreateRequest request)
    {
        CreateFromTemplateCommand create = new CreateFromTemplateCommand();
        create.setParent((InstallOptionsDialog)getHost().getModel());
        InstallOptionsWidget[] newParts = (InstallOptionsWidget[])request.getNewObject();
        create.setChildren(newParts);
        Rectangle constraint = (Rectangle)getConstraintFor(request);
        create.setLocation(constraint);
        create.setLabel(InstallOptionsPlugin.getResourceString("create.from.template.command.label")); //$NON-NLS-1$
        return create;
    }

    @Override
    public EditPart getTargetEditPart(Request request)
    {
        if (IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE.equals(request.getType())) {
            return getHost();
        }
        return super.getTargetEditPart(request);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreationFeedbackOffset(org.eclipse.gef.requests.CreateRequest)
     */
    @Override
    protected Insets getCreationFeedbackOffset(CreateRequest request)
    {
        return new Insets();
    }

    @Override
    protected Command getDeleteDependantCommand(Request request)
    {
        return null;
    }

    /**
     * Returns the layer used for displaying feedback.
     *
     * @return the feedback layer
     */
    @Override
    protected IFigure getFeedbackLayer()
    {
        return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
    }

    @Override
    protected Command getOrphanChildrenCommand(Request request)
    {
        return null;
    }

    @Override
    public void showTargetFeedback(Request request) {
        if(IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE.equals(request.getType())) {
            showLayoutTargetFeedback(request);
            CreateRequest createReq = (CreateRequest)request;
            if (createReq.getSize() != null) {
                showSizeOnDropFeedback(createReq);
            }
        }
        else {
            super.showTargetFeedback(request);
        }
    }

    @Override
    public void eraseTargetFeedback(Request request)
    {
        if(IInstallOptionsConstants.REQ_CREATE_FROM_TEMPLATE.equals(request.getType())) {
            eraseLayoutTargetFeedback(request);
            eraseSizeOnDropFeedback(request);
        }
        else {
            super.eraseTargetFeedback(request);
        }
    }
}