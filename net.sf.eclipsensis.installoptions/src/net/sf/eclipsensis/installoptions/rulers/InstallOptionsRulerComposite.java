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

import java.beans.*;
import java.util.ArrayList;

import net.sf.eclipsensis.installoptions.editor.InstallOptionsGraphicalViewer;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.gef.*;
import org.eclipse.gef.internal.ui.rulers.*;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class InstallOptionsRulerComposite extends Composite
{
    private EditDomain mRulerEditDomain;
    private GraphicalViewer mLeft, mTop;
    private FigureCanvas mEditor;
    private InstallOptionsGraphicalViewer mViewer;
    private Font mFont;
    private Listener mLayoutListener;
    private PropertyChangeListener mPropertyListener;
    private boolean mLayingOut = false;
    private boolean mIsRulerVisible = true;
    private boolean mNeedToLayout = false;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            layout(false);
        }
    };

    /**
     * Constructor
     *
     * @param parent    a widget which will be the parent of the new instance (cannot be null)
     * @param style     the style of widget to construct
     * @see Composite#Composite(org.eclipse.swt.widgets.Composite, int)
     */
    public InstallOptionsRulerComposite(Composite parent, int style) {
        super(parent, style);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                disposeResources();
            }
        });
    }

    @SuppressWarnings("restriction")
    private GraphicalViewer createRulerContainer(InstallOptionsDialog dialog, int orientation)
    {
        RulerViewer viewer = new RulerViewer(dialog);
        final boolean isHorizontal = orientation == PositionConstants.NORTH
        || orientation == PositionConstants.SOUTH;

        // Finish initializing the viewer
        viewer.setRootEditPart(new RulerRootEditPart(isHorizontal));
        viewer.setEditPartFactory(new InstallOptionsRulerEditPartFactory(mViewer));
        viewer.createControl(this);
        ((GraphicalEditPart)viewer.getRootEditPart()).getFigure()
        .setBorder(new RulerBorder(isHorizontal));
        viewer.setProperty(GraphicalViewer.class.toString(), mViewer);

        // Configure the viewer's control
        FigureCanvas canvas = (FigureCanvas)viewer.getControl();
        canvas.setScrollBarVisibility(FigureCanvas.NEVER);
        if (mFont == null) {
            FontData[] data = canvas.getFont().getFontData();
            for (int i = 0; i < data.length; i++) {
                data[i].setHeight(data[i].getHeight() - 1);
            }
            mFont = new Font(Display.getCurrent(), data);
        }
        canvas.setFont(mFont);
        if (isHorizontal) {
            canvas.getViewport().setHorizontalRangeModel(
                            mEditor.getViewport().getHorizontalRangeModel());
        } else {
            canvas.getViewport().setVerticalRangeModel(
                            mEditor.getViewport().getVerticalRangeModel());
        }

        // Add the viewer to the rulerEditDomain
        if (mRulerEditDomain == null) {
            mRulerEditDomain = new EditDomain();
            mRulerEditDomain.setCommandStack(mViewer.getEditDomain().getCommandStack());
        }
        mRulerEditDomain.addViewer(viewer);

        return viewer;
    }

    private void disposeResources() {
        if (mViewer != null) {
            mViewer.removePropertyChangeListener(mPropertyListener);
        }
        if (mFont != null) {
            mFont.dispose();
        }
        // layoutListener is not being removed from the scroll bars because they are already
        // disposed at this point.
    }

    private void disposeRulerViewer(GraphicalViewer viewer) {
        if (viewer == null) {
            return;
        }
        /*
         * There's a tie from the editor's range model to the RulerViewport (via a listener)
         * to the RulerRootEditPart to the RulerViewer.  Break this tie so that the viewer
         * doesn't leak and can be garbage collected.
         */
        RangeModel rModel = new DefaultRangeModel();
        Viewport port = ((FigureCanvas)viewer.getControl()).getViewport();
        port.setHorizontalRangeModel(rModel);
        port.setVerticalRangeModel(rModel);
        mRulerEditDomain.removeViewer(viewer);
        viewer.getControl().dispose();
    }

    private void doLayout() {
        if (mLeft == null && mTop == null) {
            Rectangle area = getClientArea();
            if (!mEditor.getBounds().equals(area)) {
                mEditor.setBounds(area);
            }
            return;
        }

        int leftWidth, rightWidth, topHeight, bottomHeight;
        leftWidth = mLeft == null ? 0
                        : mLeft.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        rightWidth = 0;
        topHeight = mTop == null ? 0
                        : mTop.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        bottomHeight = 0;

        Point size = getSize();
        Point editorSize = new Point(size.x - (leftWidth + rightWidth),
                        size.y - (topHeight + bottomHeight));
        if (!mEditor.getSize().equals(editorSize)) {
            mEditor.setSize(editorSize);
        }
        Point editorLocation = new Point(leftWidth, topHeight);
        if (!mEditor.getLocation().equals(editorLocation)) {
            mEditor.setLocation(editorLocation);
        }

        int vBarWidth = 0, hBarHeight = 0;
        Rectangle trim = mEditor.computeTrim(0, 0, 0, 0);
        /*
         * Fix for Bug# 67554
         * Motif leaves a few pixels of space around the Canvas which
         * can cause the rulers to misaligned.
         */
        if (mEditor.getVerticalBar().getVisible()) {
            vBarWidth = trim.width + ("motif".equals(SWT.getPlatform()) ? trim.x * 2 : 0); //$NON-NLS-1$
        }
        if (mEditor.getHorizontalBar().getVisible()) {
            hBarHeight = trim.height + ("motif".equals(SWT.getPlatform()) ? trim.y * 2 : 0); //$NON-NLS-1$
        }

        if (mLeft != null) {
            Rectangle leftBounds = new Rectangle(
                            0, topHeight - 1, leftWidth, editorSize.y - hBarHeight);
            if (!mLeft.getControl().getBounds().equals(leftBounds)) {
                mLeft.getControl().setBounds(leftBounds);
            }
        }
        if (mTop != null) {
            Rectangle topBounds = new Rectangle(
                            leftWidth - 1, 0, editorSize.x - vBarWidth, topHeight);
            if (!mTop.getControl().getBounds().equals(topBounds)) {
                mTop.getControl().setBounds(topBounds);
            }
        }
    }

    private GraphicalViewer getRulerContainer(int orientation) {
        GraphicalViewer result = null;
        switch(orientation) {
            case PositionConstants.NORTH:
                result = mTop;
                break;
            case PositionConstants.WEST:
                result = mLeft;
        }
        return result;
    }

    /**
     * @see org.eclipse.swt.widgets.Composite#layout(boolean)
     */
    @Override
    public void layout(boolean change) {
        if (!mLayingOut && !isDisposed()) {
            checkWidget();
            if (change || mNeedToLayout) {
                mNeedToLayout = false;
                mLayingOut = true;
                doLayout();
                mLayingOut = false;
            }
        }
    }

    /**
     * Creates rulers for the given graphical viewer.
     * <p>
     * The primaryViewer or its Control cannot be <code>null</code>.  The primaryViewer's
     * Control should be a FigureCanvas and a child of this Composite.  This method should
     * only be invoked once.
     * <p>
     * To create ruler(s), simply add the RulerProvider(s) (with the right key:
     * RulerProvider.PROPERTY_HORIZONTAL_RULER or RulerProvider.PROPERTY_VERTICAL_RULER)
     * as a property on the given viewer.  It can be done after this method is invoked.
     * RulerProvider.PROPERTY_RULER_VISIBILITY can be used to show/hide the rulers.
     *
     * @param   primaryViewer   The graphical viewer for which the rulers have to be created
     */
    public void setGraphicalViewer(InstallOptionsGraphicalViewer primaryViewer) {
        // pre-conditions
        Assert.isNotNull(primaryViewer);
        Assert.isNotNull(primaryViewer.getControl());
        if(mViewer != null) {
            disposeRulerViewer(mTop);
            mTop = null;
            disposeRulerViewer(mLeft);
            mLeft = null;
        }

        mViewer = primaryViewer;
        mEditor = (FigureCanvas)mViewer.getControl();

        // layout whenever the scrollbars are shown or hidden, and whenever the RulerComposite
        // is resized
        mLayoutListener = new Listener() {
            public void handleEvent(Event event) {
                layout(true);
            }
        };
        addListener(SWT.Resize, mLayoutListener);
        mEditor.getHorizontalBar().addListener(SWT.Show, mLayoutListener);
        mEditor.getHorizontalBar().addListener(SWT.Hide, mLayoutListener);
        mEditor.getVerticalBar().addListener(SWT.Show, mLayoutListener);
        mEditor.getVerticalBar().addListener(SWT.Hide, mLayoutListener);

        mPropertyListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String property = evt.getPropertyName();
                if (RulerProvider.PROPERTY_HORIZONTAL_RULER.equals(property)) {
                    setRuler(mViewer.getDialog(),(RulerProvider)mViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER),
                                    PositionConstants.NORTH);
                }
                else if (RulerProvider.PROPERTY_VERTICAL_RULER.equals(property)) {
                    setRuler(null, (RulerProvider)mViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER),
                                    PositionConstants.WEST);
                }
                else if (RulerProvider.PROPERTY_RULER_VISIBILITY.equals(property)) {
                    setRulerVisibility(((Boolean)mViewer.getProperty(
                                    RulerProvider.PROPERTY_RULER_VISIBILITY)).booleanValue());
                }
            }
        };
        mViewer.addPropertyChangeListener(mPropertyListener);
        Boolean rulerVisibility = (Boolean)mViewer.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
        if (rulerVisibility != null) {
            setRulerVisibility(rulerVisibility.booleanValue());
        }
        setRuler(mViewer.getDialog(),
                        (RulerProvider)mViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER), PositionConstants.NORTH);
        setRuler(null, (RulerProvider)mViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER), PositionConstants.WEST);
    }

    private void setRuler(InstallOptionsDialog dialog, RulerProvider provider, int orientation) {
        Object ruler = null;
        if (mIsRulerVisible && provider != null) {
            // provider.getRuler() might return null (at least the API does not prevent that)
            ruler = provider.getRuler();
        }

        if (ruler == null) {
            // Ruler is not visible or is not present
            setRulerContainer(null, orientation);
            // Layout right-away to prevent an empty control from showing
            layout(true);
            return;
        }

        GraphicalViewer container = getRulerContainer(orientation);
        if (container == null) {
            container = createRulerContainer(dialog, orientation);
            setRulerContainer(container, orientation);
        }
        if (container.getContents() != ruler) {
            container.setContents(ruler);
            mNeedToLayout = true;
            Display.getCurrent().asyncExec(mRunnable);
        }
    }

    private void setRulerContainer(GraphicalViewer container, int orientation) {
        if (orientation == PositionConstants.NORTH) {
            if (mTop == container) {
                return;
            }
            disposeRulerViewer(mTop);
            mTop = container;
        } else if (orientation == PositionConstants.WEST) {
            if (mLeft == container) {
                return;
            }
            disposeRulerViewer(mLeft);
            mLeft = container;
        }
    }

    private void setRulerVisibility(boolean isVisible) {
        if (mIsRulerVisible != isVisible) {
            mIsRulerVisible = isVisible;
            if (mViewer != null) {
                setRuler(mViewer.getDialog(), (RulerProvider)mViewer.getProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER), PositionConstants.NORTH);
                setRuler(null, (RulerProvider)mViewer.getProperty(RulerProvider.PROPERTY_VERTICAL_RULER), PositionConstants.WEST);
            }
        }
    }

    private static class RulerBorder
    extends AbstractBorder
    {
        private static final Insets H_INSETS = new Insets(0, 1, 0, 0);
        private static final Insets V_INSETS = new Insets(1, 0, 0, 0);
        private boolean horizontal;
        /**
         * Constructor
         *
         * @param isHorizontal  whether or not the ruler being bordered is horizontal or not
         */
        public RulerBorder(boolean isHorizontal) {
            horizontal = isHorizontal;
        }
        /**
         * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
         */
        public Insets getInsets(IFigure figure) {
            return horizontal ? H_INSETS : V_INSETS;
        }
        /**
         * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure, org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
         */
        public void paint(IFigure figure, Graphics graphics, Insets insets)
        {
            graphics.pushState();
            graphics.setForegroundColor(ColorConstants.buttonDarker);
            if (horizontal)
            {
                graphics.drawLine(figure.getBounds().getTopLeft(),
                                figure.getBounds().getBottomLeft()
                                .translate(new org.eclipse.draw2d.geometry.Point(0, -4)));
            }
            else {
                graphics.drawLine(figure.getBounds().getTopLeft(),
                                figure.getBounds().getTopRight()
                                .translate(new org.eclipse.draw2d.geometry.Point(-4, 0)));
            }
            graphics.popState();
            graphics.restoreState();
        }
    }

    private static class RulerViewer extends InstallOptionsGraphicalViewer
    {
        /**
         * Constructor
         */
        public RulerViewer(InstallOptionsDialog dialog)
        {
            super(dialog);
            init();
        }
        /**
         * @see org.eclipse.gef.EditPartViewer#appendSelection(org.eclipse.gef.EditPart)
         */
        @Override
        public void appendSelection(EditPart editpart)
        {
            EditPart editpart2 = editpart;
            if (editpart2 instanceof RootEditPart) {
                editpart2 = ((RootEditPart)editpart2).getContents();
            }
            setFocus(editpart2);
            super.appendSelection(editpart2);
        }
        /**
         * @see org.eclipse.gef.GraphicalViewer#findHandleAt(org.eclipse.draw2d.geometry.Point)
         */
        @Override
        @SuppressWarnings({ "restriction", "unchecked" })
        public Handle findHandleAt(org.eclipse.draw2d.geometry.Point p) {
            final GraphicalEditPart gep =
                (GraphicalEditPart)findObjectAtExcluding(p, new ArrayList<Object>());
            if (!(gep instanceof GuideEditPart)) {
                return null;
            }
            return new Handle() {
                public DragTracker getDragTracker() {
                    return ((GuideEditPart)gep).getDragTracker(null);
                }
                public org.eclipse.draw2d.geometry.Point getAccessibleLocation() {
                    return null;
                }
            };
        }
        /**
         * @see org.eclipse.gef.ui.parts.AbstractEditPartViewer#init()
         */
        @Override
        @SuppressWarnings("restriction")
        protected void init() {
            setContextMenu(new RulerContextMenuProvider(this));
            setKeyHandler(new RulerKeyHandler(this));
        }
        /**
         * Requests to reveal a ruler are ignored since that causes undesired scrolling to
         * the origin of the ruler
         *
         * @see org.eclipse.gef.EditPartViewer#reveal(org.eclipse.gef.EditPart)
         */
        @Override
        public void reveal(EditPart part) {
            if (part != getContents()) {
                super.reveal(part);
            }
        }
        /**
         * @see org.eclipse.gef.EditPartViewer#setContents(org.eclipse.gef.EditPart)
         */
        @Override
        public void setContents(EditPart editpart) {
            super.setContents(editpart);
            setFocus(getContents());
        }
        protected static class RulerKeyHandler extends GraphicalViewerKeyHandler {
            /**
             * Constructor
             *
             * @param viewer    The viewer for which this handler processes keyboard input
             */
            public RulerKeyHandler(GraphicalViewer viewer) {
                super(viewer);
            }
            /**
             * @see org.eclipse.gef.KeyHandler#keyPressed(org.eclipse.swt.events.KeyEvent)
             */
            @Override
            @SuppressWarnings("restriction")
            public boolean keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.DEL) {
                    // If a guide has focus, delete it
                    if (getFocusEditPart() instanceof GuideEditPart) {
                        RulerEditPart parent =
                            (RulerEditPart)getFocusEditPart().getParent();
                        getViewer().getEditDomain().getCommandStack().execute(
                                        parent.getRulerProvider().getDeleteGuideCommand(
                                                        getFocusEditPart().getModel()));
                        event.doit = false;
                        return true;
                    }
                    return false;
                } else if ((event.stateMask & SWT.ALT) != 0
                                && event.keyCode == SWT.ARROW_UP) {
                    // ALT + UP_ARROW pressed
                    // If a guide has focus, give focus to the ruler
                    EditPart parent = getFocusEditPart().getParent();
                    if (parent instanceof RulerEditPart) {
                        navigateTo(getFocusEditPart().getParent(), event);
                    }
                    return true;
                }
                return super.keyPressed(event);
            }
        }
    }

}
