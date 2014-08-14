/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model.commands;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.geometry.Rectangle;

public class CreateCommand extends org.eclipse.gef.commands.Command
{
    private InstallOptionsWidget mChild;

    private Rectangle mRect;

    private InstallOptionsDialog mParent;

    private int mIndex = -1;

    public CreateCommand()
    {
        super(InstallOptionsPlugin.getResourceString("create.command.name")); //$NON-NLS-1$
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
            Position p  = mChild.getPosition();
            p.setLocation(mRect.getLocation());
            if (!mRect.isEmpty()) {
                p.setSize(mRect.getSize());
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
        mParent.addChild(mChild, mIndex);
    }

    public void setChild(InstallOptionsWidget child)
    {
        mChild = child;
    }

    public void setIndex(int index)
    {
        this.mIndex = index;
    }

    public void setLocation(Rectangle r)
    {
        mRect = FigureUtility.pixelsToDialogUnits(r,FontUtility.getInstallOptionsFont());
    }

    public void setParent(InstallOptionsDialog newParent)
    {
        mParent = newParent;
    }

    @Override
    public void undo()
    {
        mParent.removeChild(mChild);
    }
}
