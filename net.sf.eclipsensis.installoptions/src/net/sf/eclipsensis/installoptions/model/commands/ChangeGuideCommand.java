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

import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.gef.commands.Command;

public class ChangeGuideCommand extends Command
{
    private InstallOptionsWidget mWidget;
    private InstallOptionsGuide mOldGuide, mNewGuide;
    private int mOldAlign, mNewAlign;
    private boolean mHorizontal;

    public ChangeGuideCommand(InstallOptionsWidget widget, boolean horizontalGuide)
    {
        super();
        this.mWidget = widget;
        mHorizontal = horizontalGuide;
    }

    protected void changeGuide(InstallOptionsGuide oldGuide, InstallOptionsGuide newGuide,
            int newAlignment)
    {
        if (oldGuide != null && oldGuide != newGuide) {
            oldGuide.detachWidget(mWidget);
        }
        // You need to re-attach the widget even if the oldGuide and the newGuide
        // are the same
        // because the alignment could have changed
        if (newGuide != null) {
            newGuide.attachWidget(mWidget, newAlignment);
        }
    }

    @Override
    public void execute()
    {
        // Cache the old values
        mOldGuide = mHorizontal?mWidget.getHorizontalGuide():mWidget.getVerticalGuide();
        if (mOldGuide != null) {
            mOldAlign = mOldGuide.getAlignment(mWidget);
        }

        redo();
    }

    @Override
    public void redo()
    {
        changeGuide(mOldGuide, mNewGuide, mNewAlign);
    }

    public void setNewGuide(InstallOptionsGuide guide, int alignment)
    {
        mNewGuide = guide;
        mNewAlign = alignment;
    }

    @Override
    public void undo()
    {
        changeGuide(mNewGuide, mOldGuide, mOldAlign);
    }

}