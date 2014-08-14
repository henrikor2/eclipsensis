/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;

public class InstallOptionsDialogLayer extends FreeformLayer implements IInstallOptionsConstants
{
    private List<IFigure> mChildren = new ArrayList<IFigure>();
    private Dimension mDialogSize = new Dimension(100,100);
    private boolean mShowDialogSize = false;

    @Override
    protected void paintFigure(Graphics graphics)
    {
        super.paintFigure(graphics);
        if(mShowDialogSize && !mDialogSize.equals(0,0)) {
            graphics.pushState();
            graphics.setForegroundColor(ColorConstants.blue);
            Dimension d = FigureUtility.dialogUnitsToPixels(mDialogSize,FontUtility.getInstallOptionsFont());
            graphics.drawRectangle(0,0,d.width,d.height);
            graphics.popState();
            graphics.restoreState();
        }
    }

    @Override
    public void add(IFigure child, Object constraint, int index)
    {
        mChildren.add(child);
        super.add(child, constraint, index);
    }

    @Override
    public void remove(IFigure child)
    {
        if ((child.getParent() == this) && getChildren().contains(child)) {
            mChildren.remove(child);
        }
        super.remove(child);
    }

    @Override
    protected void paintChildren(Graphics graphics)
    {
        IFigure child;

        Rectangle clip = Rectangle.SINGLETON;
        for (Iterator<IFigure> iter = mChildren.iterator(); iter.hasNext(); ) {
            child = iter.next();
            if (child.isVisible() && child.intersects(graphics.getClip(clip))) {
                graphics.pushState();
                graphics.clipRect(child.getBounds());
                child.paint(graphics);
                graphics.popState();
                graphics.restoreState();
            }
        }
    }

    public Dimension getDialogSize()
    {
        return mDialogSize;
    }

    public void setDialogSize(Dimension size)
    {
        if(!mDialogSize.equals(size)) {
            mDialogSize = size;
            repaint();
        }
    }

    public boolean isShowDialogSize()
    {
        return mShowDialogSize;
    }

    public void setShowDialogSize(boolean showDialogSize)
    {
        if(mShowDialogSize != showDialogSize) {
            mShowDialogSize = showDialogSize;
            repaint();
        }
    }

    @Override
    public IFigure findFigureAt(int x, int y, TreeSearch search)
    {
        IFigure figure = super.findFigureAt(x, y, search);
        if(figure instanceof IInstallOptionsFigure) {
            IInstallOptionsFigure ioFigure = ((IInstallOptionsFigure)figure);
            if(ioFigure.isClickThrough()) {
                Point p = new Point(x,y);
                translateToAbsolute(p);
                Point p2 = new Point();

                p2.setLocation(p);
                ioFigure.translateToRelative(p2);
                if(!ioFigure.hitTest(p2.x,p2.y)) {
                    figure = findFigureAt(x, y, new WrappedExclusionSearch(search, Collections.singleton(ioFigure)));
                    if(figure == null || figure == this) {
                        if(ioFigure.isDefaultClickThroughFigure()) {
                            figure = ioFigure;
                        }
                        else {
                            figure = null;
                        }
                    }
                }
            }
        }
        return figure;
    }

    private class WrappedExclusionSearch extends ExclusionSearch
    {
        private TreeSearch mDelegate;

        public WrappedExclusionSearch(TreeSearch delegate, Collection<IInstallOptionsFigure> exclusions)
        {
            super(exclusions);
            mDelegate = delegate;
        }

        @Override
        public boolean prune(IFigure f)
        {
            if(!mDelegate.prune(f)) {
                return super.prune(f);
            }
            return true;
        }
    }
}
