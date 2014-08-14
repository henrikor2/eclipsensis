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
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.Command;

public class SetConstraintCommand extends Command
{
    private Position mNewPos;
    private Position mOldPos;
    private Position mGraphicalPos;
    private InstallOptionsWidget mModel;

    public SetConstraintCommand(InstallOptionsWidget model, Position pos, Point moveDelta, Dimension sizeDelta)
    {
        super();
        mModel = model;
        mOldPos = mModel.getPosition().getCopy();
        Position oldPos = mModel.toGraphical(mOldPos,false);
        mGraphicalPos = pos;
        Position pos2 = mModel.toGraphical(mModel.toModel(pos),false);

        if(moveDelta != null) {
            int left = pos2.left;
            int top = pos2.top;

            if(moveDelta.x == 0) {
                left = oldPos.left;
            }
            if(moveDelta.y == 0) {
                top = oldPos.top;
            }
            if(left != pos2.left || top != pos2.top) {
                pos2.setLocation(left, top);
            }
        }

        if(sizeDelta != null) {
            Dimension newSize = pos2.getSize();
            Dimension oldSize = oldPos.getSize();
            int width = newSize.width;
            int height = newSize.width;

            if(sizeDelta.width == 0) {
                width = oldSize.width;
            }
            if(sizeDelta.height == 0) {
                height = oldSize.height;
            }
            if(width != newSize.width || height != newSize.width) {
                pos2.setSize(width, height);
            }
        }
        mNewPos = mModel.toModel(pos2,false);
    }

    @Override
    public void execute()
    {
        mModel.setPosition(mNewPos);
    }

    @Override
    public boolean canExecute()
    {
        return mGraphicalPos.left >= 0 && mGraphicalPos.top >= 0;
    }

    @Override
    public String getLabel()
    {
        if (mOldPos.getSize().equals(mNewPos.getSize())) {
            return InstallOptionsPlugin.getResourceString("set.constraint.command.location.name"); //$NON-NLS-1$
        }
        return InstallOptionsPlugin.getResourceString("set.constraint.command.resize.name"); //$NON-NLS-1$
    }

    @Override
    public void undo()
    {
        mModel.setPosition(mOldPos);
    }
}