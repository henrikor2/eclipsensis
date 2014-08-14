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

import java.beans.PropertyChangeListener;
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.swt.graphics.Font;

/**
 *
 */
public class InstallOptionsResizableEditPolicy extends ResizableEditPolicy implements PropertyChangeListener
{
    private EditPart mEditPart;

    public InstallOptionsResizableEditPolicy(EditPart editPart)
    {
        super();
        mEditPart = editPart;
    }

    @Override
    public Command getCommand(Request request)
    {
        if((REQ_RESIZE.equals(request.getType()) || REQ_MOVE.equals(request.getType())) &&
                ((InstallOptionsWidget)mEditPart.getModel()).isLocked()) {
            return UnexecutableCommand.INSTANCE;
        }
        else {
            return super.getCommand(request);
        }
    }

    @Override
    public void showSourceFeedback(Request request)
    {
        if(!((InstallOptionsWidget)mEditPart.getModel()).isLocked()) {
            super.showSourceFeedback(request);
        }
    }


    @Override
    public void deactivate()
    {
        Object model = getHost().getModel();
        if(model instanceof InstallOptionsWidget) {
            ((InstallOptionsWidget)model).removePropertyChangeListener(this);
        }
        super.deactivate();
    }

    @Override
    public void activate()
    {
        super.activate();
        Object model = getHost().getModel();
        if(model instanceof InstallOptionsWidget) {
            ((InstallOptionsWidget)model).addPropertyChangeListener(this);
        }
    }


    public void propertyChange(java.beans.PropertyChangeEvent evt)
    {
        if(InstallOptionsWidget.PROPERTY_LOCKED.equals(evt.getPropertyName()) &&
           getHost().getSelected() != EditPart.SELECTED_NONE) {
            hideSelection();
            showSelection();
        }
    }

    @Override
    protected List<Handle> createSelectionHandles()
    {
        List<Handle> list = new ArrayList<Handle>();

        Object model = getHost().getModel();
        if(model instanceof InstallOptionsWidget && ((InstallOptionsWidget)model).isLocked()) {
            InstallOptionsHandleKit.addLockHandles((GraphicalEditPart)getHost(), list);
        }
        else {
            int directions = getResizeDirections();
            if(directions == 0) {
                InstallOptionsHandleKit.addNonResizableHandles((GraphicalEditPart)getHost(), list);
            }
            else {
                if (directions != -1) {
                    InstallOptionsHandleKit.addMoveHandle((GraphicalEditPart)getHost(), list);
                    if ((directions & PositionConstants.EAST) != 0) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.EAST);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.EAST);
                    }

                    if ((directions & PositionConstants.SOUTH_EAST) == PositionConstants.SOUTH_EAST) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.SOUTH_EAST);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.SOUTH_EAST);
                    }

                    if ((directions & PositionConstants.SOUTH) != 0) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.SOUTH);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.SOUTH);
                    }

                    if ((directions & PositionConstants.SOUTH_WEST) == PositionConstants.SOUTH_WEST) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.SOUTH_WEST);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                    PositionConstants.SOUTH_WEST);
                    }

                    if ((directions & PositionConstants.WEST) != 0) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.WEST);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                    PositionConstants.WEST);
                    }

                    if ((directions & PositionConstants.NORTH_WEST) == PositionConstants.NORTH_WEST) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.NORTH_WEST);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.NORTH_WEST);
                    }

                    if ((directions & PositionConstants.NORTH) != 0) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.NORTH);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.NORTH);
                    }

                    if ((directions & PositionConstants.NORTH_EAST) == PositionConstants.NORTH_EAST) {
                        InstallOptionsHandleKit.addResizableHandle((GraphicalEditPart)getHost(), list,
                                PositionConstants.NORTH_EAST);
                    }
                    else {
                        InstallOptionsHandleKit.addNonResizableHandle((GraphicalEditPart)getHost(), list,
                                    PositionConstants.NORTH_EAST);
                    }
                }
                else {
                    InstallOptionsHandleKit.addResizableHandles((GraphicalEditPart)getHost(), list);
                }
            }
        }
        return list;
    }

    @Override
    protected void showChangeBoundsFeedback(ChangeBoundsRequest request)
    {
        IFigure f = getDragSourceFeedbackFigure();
        if(f instanceof ResizeFeedbackFigure) {
            ResizeFeedbackFigure figure = (ResizeFeedbackFigure)f;
            figure.setResizeDirection(request.getResizeDirection());
        }
        super.showChangeBoundsFeedback(request);
    }

    /**
     * Creates the figure used for feedback.
     *
     * @return the new feedback figure
     */
    @Override
    protected IFigure createDragSourceFeedbackFigure()
    {
        IFigure figure = createFigure((GraphicalEditPart)getHost());

        figure.setBounds(getInitialFeedbackBounds());
        addFeedback(figure);
        return figure;
    }

    protected IFigure createFigure(GraphicalEditPart part)
    {

        Rectangle childBounds = part.getFigure().getBounds().getCopy();

        IFigure walker = part.getFigure().getParent();

        while (walker != ((GraphicalEditPart)part.getParent()).getFigure()) {
            walker.translateToParent(childBounds);
            walker = walker.getParent();
        }

        return new ResizeFeedbackFigure(childBounds);
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

    /**
     * Returns the layer used for displaying feedback.
     *
     * @return the feedback layer
     */
    protected IFigure getResizeFeedbackLayer()
    {
        return getLayer(InstallOptionsRootEditPart.RESIZE_FEEDBACK_LAYER);
    }

    /**
     * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#initialFeedbackRectangle()
     */
    @Override
    protected Rectangle getInitialFeedbackBounds()
    {
        return getHostFigure().getBounds();
    }

    private class ResizeFeedbackFigure extends RectangleFigure
    {
        private boolean mInit = false;
        private String mText = null;
        private InstallOptionsWidget mModel;
        private RectangleFigure mSizeFigure = null;
        private Viewport mViewport = null;
        private int mResizeDirection = 0;

        public ResizeFeedbackFigure(Rectangle bounds)
        {
            mModel = (InstallOptionsWidget)getHost().getModel();
            setXOR(true);
            setFill(true);
            setOpaque(true);
            setBackgroundColor(IInstallOptionsConstants.GHOST_FILL_COLOR);
            setForegroundColor(ColorConstants.white);
            setBounds(bounds);
            mInit = true;
        }

        public void setResizeDirection(int resizeDirection)
        {
            mResizeDirection = resizeDirection;
        }

        @Override
        public void setBounds(Rectangle rect)
        {
            if(mInit) {
                if(mResizeDirection == 0) {
                    if (mSizeFigure != null) {
                        mSizeFigure.setVisible(false);
                    }
                }
                else {
                    Position pos = mModel.toGraphical(mModel.toModel(new Position(rect)),false);
                    Dimension d = pos.getSize();
                    mText = new StringBuffer().append(d.width).append("x").append(d.height).toString(); //$NON-NLS-1$
                    Font f = getFont();
                    if (f != null) {
                        if(mViewport == null) {
                            FigureCanvas canvas = (FigureCanvas)getHost().getRoot().getViewer().getControl();
                            mViewport = canvas.getViewport();
                        }
                        Rectangle clientArea = mViewport.getClientArea();
                        Rectangle r = clientArea.getCopy().intersect(bounds);
                        Dimension dim = FigureUtilities.getTextExtents(mText, f);
                        dim.expand(4, 4);
                        IFigure sizeFigure = getSizeFigure();
                        int delX = (r.width - dim.width)/2;
                        int delY = (r.height - dim.height)/2;
                        if (delX >= 2 && delY >= 2) {
                            sizeFigure.setVisible(true);
                            sizeFigure.setBounds(new Rectangle(r.x + delX, r.y + delY, dim.width, dim.height));
                        }
                        else {
                            int x = -1;
                            int y = -1;
                            switch(mResizeDirection) {
                                case PositionConstants.NORTH_WEST:
                                {
                                    x = r.x - dim.width - 2;
                                    y = (r.y + delY);

                                    if(x < clientArea.x) {
                                        x = r.x + delX;
                                        y = r.y - dim.height - 2;
                                    }
                                    if(y < clientArea.y) {
                                        y = r.y + r.height + 2;
                                    }
                                    break;
                                }
                                case PositionConstants.NORTH:
                                {
                                    x = r.x + delX;
                                    y = r.y - dim.height - 8;
                                    if(y < clientArea.y) {
                                        y = r.y + r.height+2;
                                    }
                                    break;
                                }
                                case PositionConstants.NORTH_EAST:
                                {
                                    x = r.x + r.width + 2;
                                    y = (r.y + delY);

                                    if(x+dim.width > clientArea.x+clientArea.width) {
                                        x = r.x + delX;
                                        y = r.y - dim.height - 2;
                                    }
                                    if(y < clientArea.y) {
                                        y = r.y + r.height + 2;
                                    }
                                    break;
                                }
                                case PositionConstants.EAST:
                                {
                                    x = r.x + r.width + 8;
                                    y = (r.y + delY);

                                    if(x+dim.width > clientArea.x+clientArea.width) {
                                        x = r.x - dim.width - 2;
                                    }
                                    break;
                                }
                                case PositionConstants.SOUTH_EAST:
                                {
                                    x = r.x + r.width + 2;
                                    y = (r.y + delY);

                                    if(x+dim.width > clientArea.x+clientArea.width) {
                                        x = r.x + delX;
                                        y = r.y + r.height + 2;
                                    }
                                    if(y+dim.height > clientArea.y+clientArea.height) {
                                        y = r.y - dim.height - 2;
                                    }
                                    break;
                                }
                                case PositionConstants.SOUTH:
                                {
                                    x = r.x + delX;
                                    y = r.y + r.height + 8;
                                    if(y+dim.height > clientArea.y+clientArea.height) {
                                        y = r.y - dim.height - 2;
                                    }
                                    break;
                                }
                                case PositionConstants.SOUTH_WEST:
                                {
                                    x = r.x - dim.width - 2;
                                    y = (r.y + delY);

                                    if(x < clientArea.x) {
                                        x = r.x + delX;
                                        y = r.y + r.height + 2;
                                    }
                                    if(y+dim.height > clientArea.y+clientArea.height) {
                                        y = r.y - dim.height - 2;
                                    }
                                    break;
                                }
                                case PositionConstants.WEST:
                                {
                                    x = r.x - dim.width - 8;
                                    y = (r.y + delY);

                                    if(x < clientArea.x) {
                                        x = r.x + r.width + 2;
                                    }
                                    break;
                                }
                            }
                            if(x < 0 || y < 0 || (x+dim.width) > (clientArea.x+clientArea.width) || (y+dim.height) >= (clientArea.y+clientArea.height)) {
                                sizeFigure.setVisible(false);
                            }
                            else {
                                sizeFigure.setVisible(true);
                                sizeFigure.setBounds(new Rectangle(x, y, dim.width, dim.height));
                            }
                        }
                    }
                }
            }
            super.setBounds(rect);
        }

        private IFigure getSizeFigure()
        {
            if (mSizeFigure == null) {
                mSizeFigure = new RectangleFigure() {
                    @Override
                    public void paintClientArea(Graphics graphics)
                    {
                        super.paintClientArea(graphics);
                        if (mText != null) {
                            graphics.drawText(mText, getBounds().x+2, getBounds().y+2);
                        }
                    }
                };
                mSizeFigure.setForegroundColor(ColorConstants.tooltipForeground);
                mSizeFigure.setBackgroundColor(ColorConstants.tooltipBackground);
                mSizeFigure.setOutline(true);
                mSizeFigure.setFill(true);
                getResizeFeedbackLayer().add(mSizeFigure);
            }
            return mSizeFigure;
        }

        @Override
        public void removeNotify()
        {
            if(mSizeFigure != null) {
                getResizeFeedbackLayer().remove(mSizeFigure);
                mSizeFigure = null;
            }
            super.removeNotify();
        }
    }
}