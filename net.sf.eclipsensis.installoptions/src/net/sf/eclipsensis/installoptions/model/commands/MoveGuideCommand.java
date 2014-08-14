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

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Font;

public class MoveGuideCommand extends Command
{
    private int mPositionDelta;
    private int mGuideOldPosition;
    private Map<InstallOptionsWidget, Position> mOldPositions = new HashMap<InstallOptionsWidget, Position>();
    private InstallOptionsGuide mGuide;

    public MoveGuideCommand(InstallOptionsGuide guide, int positionDelta)
    {
        super(InstallOptionsPlugin.getResourceString("move.guide.command.name")); //$NON-NLS-1$
        this.mGuide = guide;
        mPositionDelta = positionDelta;
    }

    @Override
    public void execute()
    {
        boolean isHorizontal = mGuide.isHorizontal();
        mGuideOldPosition = mGuide.getPosition();
        Font f = FontUtility.getInstallOptionsFont();

        int guidePos = (isHorizontal?FigureUtility.dialogUnitsToPixelsY(mGuideOldPosition,f):FigureUtility.dialogUnitsToPixelsX(mGuideOldPosition,f)) + mPositionDelta;
        guidePos = (isHorizontal?FigureUtility.pixelsToDialogUnitsY(guidePos,f):FigureUtility.pixelsToDialogUnitsX(guidePos,f));
        mGuide.setPosition(guidePos);
        Iterator<InstallOptionsWidget> iter = mGuide.getWidgets().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = iter.next();
            Position pos = widget.getPosition();
            mOldPositions.put(widget,pos);
            pos = widget.toGraphical(pos, false);
            int alignment = mGuide.getAlignment(widget);
            if (mGuide.isHorizontal()) {
                pos.setLocation(pos.left,calculatePosition(guidePos,alignment, pos));
            }
            else {
                pos.setLocation(calculatePosition(guidePos,alignment, pos),pos.top);
            }
            pos = widget.toModel(pos, false);
            widget.setPosition(pos);
        }
    }

    @Override
    public boolean canExecute()
    {
        boolean isHorizontal = mGuide.isHorizontal();
        Font f = FontUtility.getInstallOptionsFont();
        int guidePos = (isHorizontal?FigureUtility.dialogUnitsToPixelsY(mGuide.getPosition(),f):FigureUtility.dialogUnitsToPixelsX(mGuide.getPosition(),f)) + mPositionDelta;
        if(guidePos < 0) {
            return false;
        }
        Iterator<InstallOptionsWidget> iter = mGuide.getWidgets().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = iter.next();
            Position pos = widget.toGraphical(widget.getPosition(), false);
            int alignment = mGuide.getAlignment(widget);
            int position = calculatePosition(guidePos, alignment, pos);
            if(position < 0) {
                return false;
            }
        }
        return super.canExecute();
    }

    @Override
    public void undo()
    {
        mGuide.setPosition(mGuideOldPosition);
        Iterator<InstallOptionsWidget> iter = mGuide.getWidgets().iterator();
        while (iter.hasNext()) {
            InstallOptionsWidget widget = iter.next();
            widget.setPosition(mOldPositions.get(widget));
        }
    }

    private int calculatePosition(int guidePos, int alignment, Position pos)
    {
        int position;
        int dim = (mGuide.isHorizontal()?pos.getSize().height:pos.getSize().width);
        switch(alignment) {
            case -1:
                position = guidePos;
                break;
            case 0:
                position = guidePos-(dim-1)/2;
                break;
            case 1:
                position = guidePos-dim+1;
                break;
            default:
                position = (mGuide.isHorizontal()?pos.getBounds().y:pos.getBounds().x);
        }
        return position;
    }
}