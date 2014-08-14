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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Font;

public class CloneCommand extends Command
{

    private List<InstallOptionsElement> mWidgets, mNewTopLevelWidgets;

    private InstallOptionsDialog mParent;

    private Map<InstallOptionsWidget, Rectangle> mBounds;

    private Map<InstallOptionsWidget, Integer> mIndices;

    private ChangeGuideCommand mVerticalGuideCommand, mHorizontalGuideCommand;

    private InstallOptionsGuide mHorizontalGuide, mVerticalGuide;

    private int mHorizontalAlignment, mVerticalAlignment;

    private Font mFont;

    public CloneCommand()
    {
        super(InstallOptionsPlugin.getResourceString("clone.command.name")); //$NON-NLS-1$
        mWidgets = new LinkedList<InstallOptionsElement>();
        mFont = FontUtility.getInstallOptionsFont();
    }

    public void addWidget(InstallOptionsWidget widget, Rectangle newBounds)
    {
        mWidgets.add(widget);
        if (mBounds == null) {
            mBounds = new HashMap<InstallOptionsWidget, Rectangle>();
        }
        mBounds.put(widget, FigureUtility.pixelsToDialogUnits(newBounds,mFont));
    }

    public void addWidget(InstallOptionsWidget widget, int index)
    {
        mWidgets.add(widget);
        if (mIndices == null) {
            mIndices = new HashMap<InstallOptionsWidget, Integer>();
        }
        mIndices.put(widget, new Integer(index));
    }

    protected void cloneWidget(InstallOptionsElement oldWidget, InstallOptionsDialog newParent, Rectangle newBounds, int index)
    {
        InstallOptionsElement newWidget = null;

        if (oldWidget instanceof InstallOptionsWidget) {
            newWidget = (InstallOptionsWidget)((InstallOptionsWidget)oldWidget).clone();
            if (newBounds != null) {
                ((InstallOptionsWidget)newWidget).getPosition().setLocation(newBounds.getTopLeft());
            }

            if (index < 0) {
                newParent.addChild((InstallOptionsWidget)newWidget);
            }
            else {
                newParent.addChild((InstallOptionsWidget)newWidget, index);
            }
        }
        else if (oldWidget instanceof InstallOptionsDialog) {
            Iterator<InstallOptionsWidget> i = ((InstallOptionsDialog)oldWidget).getChildren().iterator();
            while (i.hasNext()) {
                cloneWidget(i.next(), (InstallOptionsDialog)newWidget, null, -1);
            }
        }

        if (newParent == mParent) {
            mNewTopLevelWidgets.add(newWidget);
        }
    }

    @Override
    public void execute()
    {
        mNewTopLevelWidgets = new LinkedList<InstallOptionsElement>();

        Iterator<InstallOptionsElement> i = mWidgets.iterator();

        InstallOptionsWidget widget = null;
        while (i.hasNext()) {
            widget = (InstallOptionsWidget)i.next();
            if (mBounds != null && mBounds.containsKey(widget)) {
                cloneWidget(widget, mParent, mBounds.get(widget), -1);
            }
            else if (mIndices != null && mIndices.containsKey(widget)) {
                cloneWidget(widget, mParent, null, mIndices.get(widget).intValue());
            }
            else {
                cloneWidget(widget, mParent, null, -1);
            }
        }

        if (mHorizontalGuide != null) {
            mHorizontalGuideCommand = new ChangeGuideCommand((InstallOptionsWidget)mWidgets.get(0),
                    true);
            mHorizontalGuideCommand.setNewGuide(mHorizontalGuide, mHorizontalAlignment);
            mHorizontalGuideCommand.execute();
        }

        if (mVerticalGuide != null) {
            mVerticalGuideCommand = new ChangeGuideCommand((InstallOptionsWidget)mWidgets.get(0),
                    false);
            mVerticalGuideCommand.setNewGuide(mVerticalGuide, mVerticalAlignment);
            mVerticalGuideCommand.execute();
        }
    }

    public void setParent(InstallOptionsDialog parent)
    {
        this.mParent = parent;
    }

    @Override
    public void redo()
    {
        for (Iterator<InstallOptionsElement> iter = mNewTopLevelWidgets.iterator(); iter.hasNext();) {
            mParent.addChild((InstallOptionsWidget)iter.next());
        }
        if (mHorizontalGuideCommand != null) {
            mHorizontalGuideCommand.redo();
        }
        if (mVerticalGuideCommand != null) {
            mVerticalGuideCommand.redo();
        }
    }

    public void setGuide(InstallOptionsGuide guide, int alignment, boolean isHorizontal)
    {
        if (isHorizontal) {
            mHorizontalGuide = guide;
            mHorizontalAlignment = alignment;
        }
        else {
            mVerticalGuide = guide;
            mVerticalAlignment = alignment;
        }
    }

    @Override
    public void undo()
    {
        if (mHorizontalGuideCommand != null) {
            mHorizontalGuideCommand.undo();
        }
        if (mVerticalGuideCommand != null) {
            mVerticalGuideCommand.undo();
        }
        for (Iterator<InstallOptionsElement> iter = mNewTopLevelWidgets.iterator(); iter.hasNext();) {
            mParent.removeChild((InstallOptionsWidget)iter.next());
        }
    }

}