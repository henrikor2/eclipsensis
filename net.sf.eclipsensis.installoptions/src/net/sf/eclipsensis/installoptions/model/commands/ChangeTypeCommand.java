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

public class ChangeTypeCommand extends DeleteCommand
{
    private InstallOptionsWidget mNewChild;

    public ChangeTypeCommand(InstallOptionsDialog parent, InstallOptionsWidget oldChild, InstallOptionsWidget newChild)
    {
        super(parent, oldChild);
        mNewChild = newChild;
        setLabel(InstallOptionsPlugin.getFormattedString("change.type.command.format", new String[] {mNewChild.getType()})); //$NON-NLS-1$
    }

    public InstallOptionsWidget getNewChild()
    {
        return mNewChild;
    }

    @Override
    public void redo()
    {
        detachFromGuides(mChild);
        mIndex = mChild.getIndex();
        mParent.replaceChild(mChild, mNewChild);
        Position oldPos = mChild.getPosition();
        Position newPos = mNewChild.getPosition();
        if(oldPos != null && newPos != null && oldPos.equals(newPos)) {
            reattachToGuides(mNewChild);
        }
    }

    @Override
    public void execute()
    {
        redo();
    }

    @Override
    public void undo()
    {
        Position oldPos = mChild.getPosition();
        Position newPos = mNewChild.getPosition();
        if(oldPos != null && newPos != null && oldPos.equals(newPos)) {
            detachFromGuides(mNewChild);
        }
        mParent.replaceChild(mNewChild, mChild);
        reattachToGuides(mChild);
    }
}
