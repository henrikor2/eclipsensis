/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import java.util.Iterator;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.ui.rulers.DragGuidePolicy;

@SuppressWarnings("restriction")
public class InstallOptionsDragGuidePolicy extends DragGuidePolicy
{
    @Override
    protected boolean isMoveValid(int zoomedPosition)
    {
        if(zoomedPosition < 0) {
            return false;
        }
        GraphicalViewer viewer = (GraphicalViewer)getGuideEditPart().getViewer().getProperty(GraphicalViewer.class.toString());
        if(viewer == null) {
            return false;
        }
        if(super.isMoveValid(zoomedPosition)) {
            Iterator<?> i = getGuideEditPart().getRulerProvider().getAttachedEditParts(getHost().getModel(),
                    ((InstallOptionsRulerEditPart)getHost().getParent()).getDiagramViewer()).iterator();

            int delta = zoomedPosition - getGuideEditPart().getZoomedPosition();
            while (i.hasNext()) {
                InstallOptionsWidgetEditPart part = (InstallOptionsWidgetEditPart)i.next();
                if(((InstallOptionsWidget)part.getModel()).isLocked()) {
                    return false;
                }
                IFigure fig = part.getFigure();
                Rectangle bounds = fig.getBounds();
                if(getGuideEditPart().isHorizontal()) {
                    if(bounds.y+delta < 0) {
                        return false;
                    }
                }
                else {
                    if(bounds.x+delta < 0) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
