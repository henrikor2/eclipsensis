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
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;

public class CutCommand extends CopyCommand
{
    private InstallOptionsDialog mParent;
    private Stack<CutInfo> mUndoStack = new Stack<CutInfo>();
    private List<InstallOptionsWidget> mOriginals = new ArrayList<InstallOptionsWidget>();

    public CutCommand()
    {
        super(InstallOptionsPlugin.getResourceString("cut.command.name")); //$NON-NLS-1$
    }

    @Override
    public void addWidget(InstallOptionsWidget widget)
    {
        mOriginals.add(widget);
        super.addWidget(widget);
    }

    public void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
    }

    @Override
    public void execute()
    {
        Collections.sort(mOriginals, InstallOptionsWidgetComparator.REVERSE_INSTANCE);
        Collections.sort(mCopies, InstallOptionsWidgetComparator.REVERSE_INSTANCE);
        super.execute();
    }

    @Override
    public void redo()
    {
        super.redo();
        for (Iterator<InstallOptionsWidget> iter = mOriginals.iterator(); iter.hasNext();) {
            CutInfo cutInfo = new CutInfo(iter.next());
            cutInfo.cut();
            mUndoStack.push(cutInfo);
        }
    }
    @Override
    public void undo()
    {
        while(mUndoStack.size() > 0) {
            CutInfo cutInfo = mUndoStack.pop();
            cutInfo.uncut();
        }
        super.undo();
    }

    private class CutInfo
    {
        InstallOptionsWidget mElement;
        InstallOptionsGuide mVerticalGuide;
        int mVerticalAlign;
        InstallOptionsGuide mHorizontalGuide;
        int mHorizontalAlign;

        public CutInfo(InstallOptionsWidget element)
        {
            mElement = element;
            mVerticalGuide = mElement.getVerticalGuide();
            if(mVerticalGuide != null) {
                mVerticalAlign = mVerticalGuide.getAlignment(element);
            }
            mHorizontalGuide = mElement.getHorizontalGuide();
            if(mHorizontalGuide != null) {
                mHorizontalAlign = mHorizontalGuide.getAlignment(element);
            }
        }

        public void cut()
        {
            if(mVerticalGuide != null) {
                mVerticalGuide.detachWidget(mElement);
            }
            if(mHorizontalGuide != null) {
                mHorizontalGuide.detachWidget(mElement);
            }
            mParent.removeChild(mElement.getIndex());
        }

        public void uncut()
        {
            mParent.addChild(mElement,mElement.getIndex());
            if(mVerticalGuide != null) {
                mVerticalGuide.attachWidget(mElement, mVerticalAlign);
            }
            if(mHorizontalGuide != null) {
                mHorizontalGuide.attachWidget(mElement, mHorizontalAlign);
            }
        }
    }
}
