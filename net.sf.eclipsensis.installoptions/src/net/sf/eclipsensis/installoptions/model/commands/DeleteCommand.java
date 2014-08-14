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
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

import org.eclipse.gef.commands.Command;

public class DeleteCommand extends Command
{
    protected InstallOptionsWidget mChild;

    protected InstallOptionsDialog mParent;

    private InstallOptionsGuide mVerticalGuide, mHorizontalGuide;

    private int mVerticalAlign, mHorizontalAlign;

    protected int mIndex = -1;

    public DeleteCommand(InstallOptionsDialog parent, InstallOptionsWidget child)
    {
        mParent = parent;
        mChild = child;
        setLabel(InstallOptionsPlugin.getResourceString("delete.command.name")); //$NON-NLS-1$
    }

    protected void detachFromGuides(InstallOptionsWidget widget)
    {
        if (widget.getVerticalGuide() != null) {
            mVerticalGuide = widget.getVerticalGuide();
            mVerticalAlign = mVerticalGuide.getAlignment(widget);
            mVerticalGuide.detachWidget(widget);
        }
        if (widget.getHorizontalGuide() != null) {
            mHorizontalGuide = widget.getHorizontalGuide();
            mHorizontalAlign = mHorizontalGuide.getAlignment(widget);
            mHorizontalGuide.detachWidget(widget);
        }
    }

    @Override
    public void execute()
    {
        detachFromGuides(mChild);
        mIndex = mChild.getIndex();
        mParent.removeChild(mChild);
    }

    protected void reattachToGuides(InstallOptionsWidget widget)
    {
        if (mVerticalGuide != null) {
            mVerticalGuide.attachWidget(widget, mVerticalAlign);
        }
        if (mHorizontalGuide != null) {
            mHorizontalGuide.attachWidget(widget, mHorizontalAlign);
        }
    }

    @Override
    public void redo()
    {
        execute();
    }

    @Override
    public void undo()
    {
        mParent.addChild(mChild, mIndex);
        reattachToGuides(mChild);
    }

}
