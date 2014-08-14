/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Font;

public class CreateFromTemplateCommand extends org.eclipse.gef.commands.Command
{
    private InstallOptionsWidget[] mChildren;

    private Rectangle mRect;

    private InstallOptionsDialog mParent;

    public CreateFromTemplateCommand()
    {
        super(InstallOptionsPlugin.getResourceString("create.from.template.command.name")); //$NON-NLS-1$
    }

    @Override
    public boolean canExecute()
    {
        return mRect != null && mRect.x >= 0 && mRect.y >= 0;
    }

    @Override
    public void execute()
    {
        if (mRect != null) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int width, height;

            Dimension size = getParent().getDialogSize().getSize();
            for (int i = 0; i < mChildren.length; i++) {
                Position p = mChildren[i].toGraphical(mChildren[i].getPosition(),size,false);
                if(p.left < minX) {
                    minX = p.left;
                }
                if(p.top < minY) {
                    minY = p.top;
                }
                if(p.right > maxX) {
                    maxX = p.right;
                }
                if(p.bottom > maxY) {
                    maxY = p.bottom;
                }
            }
            width = maxX-minX+1;
            height = maxY-minY+1;
            Dimension delta = mRect.getLocation().getDifference(new Point(minX, minY));
            double scaleX = 1.0;
            double scaleY = 1.0;
            boolean needsScaling = false;
            if(!mRect.isEmpty()) {
                needsScaling = true;
                scaleX = (width == 0?0:(double)mRect.width/(double)width);
                scaleY = (height == 0?0:(double)mRect.height/(double)height);
            }
            for (int i = 0; i < mChildren.length; i++) {
                Position p = mChildren[i].toGraphical(mChildren[i].getPosition(),size,false);
                if(needsScaling) {
                    Dimension delta2 = new Dimension(delta.width + (int)((p.left-minX)*(scaleX-1)),
                                                     delta.height + (int)((p.top-minY)*(scaleY-1)));
                    p.move(delta2);
                    p.setSize(p.getSize().scale(scaleX, scaleY));
                }
                else {
                    p.move(delta);
                    p = mChildren[i].toModel(p,size,false);
                }
                mChildren[i].setPosition(p);
            }
        }
        redo();
    }

    public InstallOptionsDialog getParent()
    {
        return mParent;
    }

    @Override
    public void redo()
    {
        for (int i = 0; i < mChildren.length; i++) {
            mParent.addChild(mChildren[i]);
        }
    }

    public void setChildren(InstallOptionsWidget[] children)
    {
        mChildren = children;
    }

    public void setLocation(Rectangle r)
    {
        Font f = FontUtility.getInstallOptionsFont();
        mRect = FigureUtility.pixelsToDialogUnits(r,f);
    }

    public void setParent(InstallOptionsDialog newParent)
    {
        mParent = newParent;
    }

    @Override
    public void undo()
    {
        for (int i = 0; i < mChildren.length; i++) {
            mParent.removeChild(mChildren[i]);
        }
    }
}
