/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;

public class DistributeAction extends SelectionAction implements IInstallOptionsConstants
{
    static final String GROUP = "net.sf.eclipsensis.installoptions.distribute."; //$NON-NLS-1$
    public static final String HORIZONTAL_LEFT_EDGE_ID=GROUP+"horizontal.left"; //$NON-NLS-1$
    public static final String HORIZONTAL_CENTER_ID=GROUP+"horizontal.center"; //$NON-NLS-1$
    public static final String HORIZONTAL_RIGHT_EDGE_ID=GROUP+"horizontal.right"; //$NON-NLS-1$
    public static final String HORIZONTAL_BETWEEN_ID=GROUP+"horizontal.between"; //$NON-NLS-1$
    public static final String VERTICAL_TOP_EDGE_ID=GROUP+"vertical.top"; //$NON-NLS-1$
    public static final String VERTICAL_CENTER_ID=GROUP+"vertical.center"; //$NON-NLS-1$
    public static final String VERTICAL_BOTTOM_EDGE_ID=GROUP+"vertical.bottom"; //$NON-NLS-1$
    public static final String VERTICAL_BETWEEN_ID=GROUP+"vertical.between"; //$NON-NLS-1$

    private int mType;
    private Comparator<GraphicalEditPart> mComparator;

    public DistributeAction(IWorkbenchPart part, int type)
    {
        super(part);
        setLazyEnablementCalculation(false);
        mType = type;
        String id;
        switch(type) {
            case DISTRIBUTE_HORIZONTAL_LEFT_EDGE:
                id = HORIZONTAL_LEFT_EDGE_ID;
                break;
            case DISTRIBUTE_HORIZONTAL_RIGHT_EDGE:
                id = HORIZONTAL_RIGHT_EDGE_ID;
                break;
            case DISTRIBUTE_HORIZONTAL_CENTER:
                id = HORIZONTAL_CENTER_ID;
                break;
            case DISTRIBUTE_HORIZONTAL_BETWEEN:
                id = HORIZONTAL_BETWEEN_ID;
                break;
            case DISTRIBUTE_VERTICAL_TOP_EDGE:
                id = VERTICAL_TOP_EDGE_ID;
                break;
            case DISTRIBUTE_VERTICAL_BOTTOM_EDGE:
                id = VERTICAL_BOTTOM_EDGE_ID;
                break;
            case DISTRIBUTE_VERTICAL_CENTER:
                id = VERTICAL_CENTER_ID;
                break;
            case DISTRIBUTE_VERTICAL_BETWEEN:
            default:
                id = VERTICAL_BETWEEN_ID;
        }
        setId(id);
        String prefix = id.substring(GROUP.length());
        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".tooltip")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".icon")); //$NON-NLS-1$
        setHoverImageDescriptor(imageDescriptor);
        setImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".disabled.icon"))); //$NON-NLS-1$
        setEnabled(false);
        mComparator = new Comparator<GraphicalEditPart>() {
            public int compare(GraphicalEditPart o1, GraphicalEditPart o2)
            {
                Rectangle bounds1 = o1.getFigure().getBounds();
                Rectangle bounds2 = o2.getFigure().getBounds();
                double diff;
                switch(mType) {
                    case DISTRIBUTE_HORIZONTAL_LEFT_EDGE:
                        return bounds1.x-bounds2.x;
                    case DISTRIBUTE_HORIZONTAL_RIGHT_EDGE:
                        return bounds1.x+bounds1.width-bounds2.x-bounds2.width;
                    case DISTRIBUTE_HORIZONTAL_CENTER:
                        diff = midpoint(bounds1.x,bounds1.width)-midpoint(bounds2.x,bounds2.width);
                        return (diff < 0?-1:diff > 0?1:0);
                    case DISTRIBUTE_HORIZONTAL_BETWEEN:
                        return bounds1.x-bounds2.x;
                    case DISTRIBUTE_VERTICAL_TOP_EDGE:
                        return bounds1.y-bounds2.y;
                    case DISTRIBUTE_VERTICAL_BOTTOM_EDGE:
                        return bounds1.y+bounds1.height-bounds2.y-bounds2.height;
                    case DISTRIBUTE_VERTICAL_CENTER:
                        diff = midpoint(bounds1.y,bounds1.height)-midpoint(bounds2.y,bounds2.height);
                        return (diff < 0?-1:diff > 0?1:0);
                    case DISTRIBUTE_VERTICAL_BETWEEN:
                    default:
                        return bounds1.y-bounds2.y;
                }
            }
        };
    }

    private double midpoint(double p, double d)
    {
        return p+(d-1.0)/2.0;
    }

    @Override
    protected boolean calculateEnabled()
    {
        Command cmd = createDistributeCommand(getSelectedObjects());
        if (cmd == null) {
            return false;
        }
        return cmd.canExecute();
    }

    /**
     * Create a command to resize the selected objects.
     * @param objects The objects to be resized.
     * @return The command to resize the selected objects.
     */
    @SuppressWarnings("unchecked")
    private Command createDistributeCommand(List<?> objects) {
        if (objects.size() < 3) {
            return null;
        }
        if (!(objects.get(0) instanceof GraphicalEditPart)) {
            return null;
        }

        Collections.sort((List<GraphicalEditPart>)objects, mComparator);
        double sum = 0.0;
        Rectangle bounds1 = ((GraphicalEditPart)objects.get(objects.size()-1)).getFigure().getBounds();
        Rectangle bounds2 = ((GraphicalEditPart)objects.get(0)).getFigure().getBounds();
        double reference = 0;
        switch(mType) {
            case DISTRIBUTE_HORIZONTAL_LEFT_EDGE:
                reference = bounds2.x;
                sum = bounds1.x-reference;
                break;
            case DISTRIBUTE_HORIZONTAL_RIGHT_EDGE:
                reference = bounds2.x + bounds2.width -1;
                sum = bounds1.x+bounds1.width-1-reference;
                break;
            case DISTRIBUTE_HORIZONTAL_CENTER:
            {
                double midpoint1 = midpoint(bounds1.x,bounds1.width);
                reference = midpoint(bounds2.x,bounds2.width);
                sum = midpoint1-reference;
                break;
            }
            case DISTRIBUTE_HORIZONTAL_BETWEEN:
            {
                double right = reference = bounds2.x+bounds2.width-1;
                double left;
                for(int i=1; i<objects.size()-1; i++) {
                    Rectangle bounds = ((GraphicalEditPart)objects.get(i)).getFigure().getBounds();
                    left = bounds.x;
                    sum += (left-right+1);
                    right = bounds.x+bounds.width-1;
                }
                left = bounds1.x;
                sum += (left-right+1);
                break;
            }
            case DISTRIBUTE_VERTICAL_TOP_EDGE:
                reference = bounds2.y;
                sum = bounds1.y-reference;
                break;
            case DISTRIBUTE_VERTICAL_BOTTOM_EDGE:
                reference = bounds2.y+bounds2.height - 1;
                sum = bounds1.y+bounds1.height - 1 - reference;
                break;
            case DISTRIBUTE_VERTICAL_CENTER:
            {
                double midpoint1 = midpoint(bounds1.y,bounds1.height);
                reference = midpoint(bounds2.y,bounds2.height);
                sum = midpoint1-reference;
                break;
            }
            case DISTRIBUTE_VERTICAL_BETWEEN:
            default:
            {
                double bottom = reference = bounds2.y+bounds2.height-1;
                double top;
                for(int i=1; i<objects.size()-1; i++) {
                    Rectangle bounds = ((GraphicalEditPart)objects.get(i)).getFigure().getBounds();
                    top = bounds.y;
                    sum += (top-bottom +1);
                    bottom = bounds.y+bounds.height-1;
                }
                top = bounds1.y;
                sum += (top-bottom + 1);
            }
        }
        double mean = sum/(objects.size()-1);
        GraphicalEditPart part = null;
        ChangeBoundsRequest request = null;
        PrecisionPoint precisePoint = null;
        PrecisionRectangle precisePartBounds = null;
        Command cmd = null;
        CompoundCommand command = new CompoundCommand();

        for (int i = 1; i < objects.size()-1; i++) {
            part = (GraphicalEditPart)objects.get(i);
            request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
            precisePoint = new PrecisionPoint();
            precisePartBounds = new PrecisionRectangle(part.getFigure().getBounds().getCopy());
            part.getFigure().translateToAbsolute(precisePartBounds);
            switch(mType) {
                case DISTRIBUTE_HORIZONTAL_LEFT_EDGE:
                    precisePoint.preciseX = reference + i*mean - precisePartBounds.preciseX;
                    break;
                case DISTRIBUTE_HORIZONTAL_RIGHT_EDGE:
                    precisePoint.preciseX = reference + i*mean - (precisePartBounds.preciseX+precisePartBounds.preciseWidth-1);
                    break;
                case DISTRIBUTE_HORIZONTAL_CENTER:
                    precisePoint.preciseX = reference + i*mean - midpoint(precisePartBounds.preciseX,precisePartBounds.preciseWidth);
                    break;
                case DISTRIBUTE_HORIZONTAL_BETWEEN:
                    precisePoint.preciseX = reference + mean - precisePartBounds.preciseX;
                    reference = reference + mean + precisePartBounds.preciseWidth-1;
                    break;
                case DISTRIBUTE_VERTICAL_TOP_EDGE:
                    precisePoint.preciseY = reference + i*mean - precisePartBounds.preciseY;
                    break;
                case DISTRIBUTE_VERTICAL_BOTTOM_EDGE:
                    precisePoint.preciseY = reference + i*mean - (precisePartBounds.preciseY+precisePartBounds.preciseHeight-1);
                    break;
                case DISTRIBUTE_VERTICAL_CENTER:
                    precisePoint.preciseY = reference + i*mean - midpoint(precisePartBounds.preciseY,precisePartBounds.preciseHeight);
                    break;
                case DISTRIBUTE_VERTICAL_BETWEEN:
                default:
                    precisePoint.preciseY = reference + mean - precisePartBounds.preciseY;
                    reference = reference + mean + precisePartBounds.preciseHeight-1;
            }

            precisePoint.updateInts();

            request.setMoveDelta(precisePoint);

            cmd = part.getCommand(request);
            if (cmd != null) {
                command.add(cmd);
            }
        }

        return command;
    }

    @Override
    public void run() {
        execute(createDistributeCommand(getSelectedObjects()));
    }
}
