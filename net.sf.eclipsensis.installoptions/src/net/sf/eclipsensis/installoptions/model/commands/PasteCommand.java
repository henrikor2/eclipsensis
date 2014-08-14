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
import net.sf.eclipsensis.installoptions.actions.Clipboard;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.commands.Command;

public class PasteCommand extends Command
{
    private InstallOptionsDialog mParent;
    private List<InstallOptionsWidget> mSelection = null;
    private Rectangle mPasteBounds;
    private List<InstallOptionsWidget> mPasteList = new ArrayList<InstallOptionsWidget>();
    private Rectangle mClientArea;

    public PasteCommand()
    {
        super(InstallOptionsPlugin.getResourceString("paste.command.name")); //$NON-NLS-1$
    }

    public void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
    }

    public void setSelection(List<InstallOptionsWidget> selection)
    {
        mSelection = (selection != null?selection:Collections.<InstallOptionsWidget>emptyList());
    }

    public void setClientArea(org.eclipse.swt.graphics.Rectangle clientArea)
    {
        mClientArea = FigureUtility.pixelsToDialogUnits(new Rectangle(clientArea.x,clientArea.y,clientArea.width,clientArea.height),FontUtility.getInstallOptionsFont());
    }

    @Override
    public void execute()
    {
        CopyCommand.CopyContents mCopyContents = (CopyCommand.CopyContents)Clipboard.getDefault().getContents();
        if(mCopyContents != null) {
            mPasteBounds = new Rectangle(mCopyContents.getBounds());
            mPasteList.clear();
            for (Iterator<InstallOptionsWidget> iter = mCopyContents.getChildren().iterator(); iter.hasNext();) {
                mPasteList.add((InstallOptionsWidget) (iter.next()).clone());
            }
        }
        redo();
    }

    private void calculatePasteBounds(Dimension size)
    {
        Point p;
        if(mClientArea != null) {
            p = new Point((mClientArea.width < mPasteBounds.width?mClientArea.x:mClientArea.x+(mClientArea.width-mPasteBounds.width)/2-1),
                                (mClientArea.height < mPasteBounds.height?mClientArea.y:mClientArea.y+(mClientArea.height-mPasteBounds.height)/2)-1);
        }
        else {
            p = new Point(mPasteBounds.x + 5, mPasteBounds.y + 5);
        }
        int delX = p.x-mPasteBounds.x;
        int delY = p.y-mPasteBounds.y;
        for (Iterator<InstallOptionsWidget> iter = mPasteList.iterator(); iter.hasNext();) {
            InstallOptionsWidget model = iter.next();
            Position pos = model.getPosition();
            pos = model.toGraphical(pos, size);
            if(!model.isLocked()) {
                pos.setLocation(pos.left+delX,pos.top+delY);
            }
            pos = model.toModel(pos, size);
            model.getPosition().set(pos.left,pos.top,pos.right,pos.bottom);
        }
        mPasteBounds.x = p.x;
        mPasteBounds.y = p.y;
    }

    @Override
    public void redo()
    {
        calculatePasteBounds(mParent.getDialogSize().getSize());
        for (Iterator<InstallOptionsWidget> iter = mPasteList.iterator(); iter.hasNext();) {
            mParent.addChild(iter.next());
        }
        List<InstallOptionsWidget> list = mParent.getChildren();
        mPasteList.retainAll(list);
        mParent.setSelection(mPasteList);
    }

    @Override
    public void undo()
    {
        for (Iterator<InstallOptionsWidget> iter = mPasteList.iterator(); iter.hasNext();) {
            mParent.removeChild(iter.next());
        }
        if(mSelection != null) {
            mParent.setSelection(mSelection);
        }
    }
}