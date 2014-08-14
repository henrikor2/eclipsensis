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
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

public class CopyCommand extends Command
{
    protected List<InstallOptionsWidget> mCopies;
    protected CopyContents mNewContents = new CopyContents();
    private Rectangle mBounds;
    private int mMinX = Integer.MAX_VALUE;
    private int mMaxX = Integer.MIN_VALUE;
    private int mMinY = Integer.MAX_VALUE;
    private int mMaxY = Integer.MIN_VALUE;

    public CopyCommand()
    {
        this(InstallOptionsPlugin.getResourceString("copy.command.name")); //$NON-NLS-1$
    }

    protected CopyCommand(String label)
    {
        super(label);
        mCopies = new ArrayList<InstallOptionsWidget>();
    }

    public void addWidget(InstallOptionsWidget widget)
    {
        if(!widget.isLocked()) {
            Rectangle bounds = widget.getPosition().getBounds();
            mMinX = Math.min(mMinX,bounds.x);
            mMaxX = Math.max(mMaxX,bounds.x+bounds.width-1);
            mMinY = Math.min(mMinY,bounds.y);
            mMaxY = Math.max(mMaxY,bounds.y+bounds.height-1);
        }

        mCopies.add((InstallOptionsWidget) widget.clone());
    }

    @Override
    public void execute()
    {
        mBounds = new Rectangle(mMinX,mMinY,mMaxX-mMinX+1,mMaxY-mMinY+1);
        redo();
    }

    @Override
    public void redo()
    {
        Clipboard clipboard = Clipboard.getDefault();
        clipboard.setContents(mNewContents);
    }

    public class CopyContents
    {
        public Rectangle getBounds()
        {
            return mBounds;
        }

        public List<InstallOptionsWidget> getChildren()
        {
            return mCopies;
        }
    }
}