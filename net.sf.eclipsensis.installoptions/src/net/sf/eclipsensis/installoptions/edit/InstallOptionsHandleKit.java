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

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.handles.*;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.graphics.*;

public class InstallOptionsHandleKit
{
    public static void addResizableHandle(GraphicalEditPart part, List<Handle> handles, int direction)
    {
        handles.add(createResizableHandle(part, direction));
    }

    public static void addResizableHandle(GraphicalEditPart part, List<Handle> handles, int direction,
                                 DragTracker tracker, Cursor cursor)
    {
        handles.add(createResizableHandle(part, direction, tracker, cursor));
    }

    public static void addResizableHandles(GraphicalEditPart part, List<Handle> handles)
    {
        addMoveHandle(part, handles);
        handles.add(createResizableHandle(part, PositionConstants.EAST));
        handles.add(createResizableHandle(part, PositionConstants.SOUTH_EAST));
        handles.add(createResizableHandle(part, PositionConstants.SOUTH));
        handles.add(createResizableHandle(part, PositionConstants.SOUTH_WEST));
        handles.add(createResizableHandle(part, PositionConstants.WEST));
        handles.add(createResizableHandle(part, PositionConstants.NORTH_WEST));
        handles.add(createResizableHandle(part, PositionConstants.NORTH));
        handles.add(createResizableHandle(part, PositionConstants.NORTH_EAST));
    }

    public static void addLockHandles(GraphicalEditPart part, List<Handle> handles)
    {
        handles.add(new MoveHandle(part) {
            @Override
            protected DragTracker createDragTracker()
            {
                return null;
            }

            @Override
            protected void initialize()
            {
                super.initialize();
                setCursor(null);
            }
        });
        handles.add(createLockHandle(part, PositionConstants.EAST));
        handles.add(createLockHandle(part, PositionConstants.SOUTH_EAST));
        handles.add(createLockHandle(part, PositionConstants.SOUTH));
        handles.add(createLockHandle(part, PositionConstants.SOUTH_WEST));
        handles.add(createLockHandle(part, PositionConstants.WEST));
        handles.add(createLockHandle(part, PositionConstants.NORTH_WEST));
        handles.add(createLockHandle(part, PositionConstants.NORTH));
        handles.add(createLockHandle(part, PositionConstants.NORTH_EAST));
    }

    static Handle createLockHandle(GraphicalEditPart owner, int direction)
    {
        InstallOptionsLockHandle handle = new InstallOptionsLockHandle(owner, direction);
        return handle;
    }

    static Handle createResizableHandle(GraphicalEditPart owner, int direction)
    {
        ResizeHandle handle = new ResizeHandle(owner, direction);
        return handle;
    }

    static Handle createResizableHandle(GraphicalEditPart owner, int direction, DragTracker tracker,
                               Cursor cursor)
    {
        ResizeHandle handle = new ResizeHandle(owner, direction);
        handle.setDragTracker(tracker);
        handle.setCursor(cursor);
        return handle;
    }

    public static void addCornerHandles(GraphicalEditPart part, List<Handle> handles,
            DragTracker tracker, Cursor cursor)
    {
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_EAST, tracker, cursor));
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_WEST, tracker, cursor));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_WEST, tracker, cursor));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_EAST, tracker, cursor));
    }

    public static void addCornerHandles(GraphicalEditPart part, List<Handle> handles)
    {
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_EAST));
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_WEST));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_WEST));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_EAST));
    }

    public static void addNonResizableHandle(GraphicalEditPart part, List<Handle> handles, int direction)
    {
        handles.add(createNonResizableHandle(part, direction));
    }

    public static void addNonResizableHandle(GraphicalEditPart part, List<Handle> handles, int direction, DragTracker tracker, Cursor cursor)
    {
        handles.add(createNonResizableHandle(part, direction, tracker, cursor));
    }

    public static void addNonResizableHandles(GraphicalEditPart part, List<Handle> handles)
    {
        addMoveHandle(part, handles);
        addCornerHandles(part, handles);
    }

    public static void addNonResizableHandles(GraphicalEditPart part, List<Handle> handles, DragTracker tracker, Cursor cursor)
    {
        addMoveHandle(part, handles, tracker, cursor);
        addCornerHandles(part, handles, tracker, cursor);
    }

    static Handle createNonResizableHandle(GraphicalEditPart owner, int direction)
    {
        ResizeHandle handle = new ResizeHandle(owner, direction);
        handle.setCursor(Cursors.SIZEALL);
        handle.setDragTracker(new DragEditPartsTracker(owner));
        return handle;
    }

    static Handle createNonResizableHandle(GraphicalEditPart owner, int direction, DragTracker tracker, Cursor cursor)
    {
        ResizeHandle handle = new ResizeHandle(owner, direction);
        handle.setCursor(cursor);
        handle.setDragTracker(tracker);
        return handle;
    }

    public static void addMoveHandle(GraphicalEditPart f, List<Handle> handles)
    {
        handles.add(moveHandle(f));
    }

    public static void addMoveHandle(GraphicalEditPart f, List<Handle> handles, DragTracker tracker,
                                     Cursor cursor)
    {
        handles.add(moveHandle(f, tracker, cursor));
    }

    public static Handle moveHandle(GraphicalEditPart owner)
    {
        return new MoveHandle(owner);
    }

    public static Handle moveHandle(GraphicalEditPart owner, DragTracker tracker,
                                    Cursor cursor)
    {
        MoveHandle moveHandle = new MoveHandle(owner);
        moveHandle.setDragTracker(tracker);
        moveHandle.setCursor(cursor);
        return moveHandle;
    }

    private static class InstallOptionsLockHandle extends AbstractHandle
    {
        private static Image cLockImage = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("lock.handle.icon")); //$NON-NLS-1$

        public InstallOptionsLockHandle(GraphicalEditPart owner, int direction)
        {
            super();
            setOwner(owner);
            setLocator(new RelativeHandleLocator(owner.getFigure(), direction));
            setOpaque(false);
            setLayoutManager(new XYLayout());
            IFigure imageFigure = new ImageFigure(cLockImage);
            add(imageFigure);
            setConstraint(imageFigure, new Rectangle(cLockImage.getBounds().x,
                                                     cLockImage.getBounds().y,
                                                     cLockImage.getBounds().width,
                                                     cLockImage.getBounds().height));

        }

        @Override
        protected DragTracker createDragTracker()
        {
            return null;
        }
    }
}
