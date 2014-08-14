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

import java.text.MessageFormat;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.views.properties.*;

public class SetValueCommand extends Command
{
    protected Object mPropertyValue;
    protected Object mPropertyName;
    protected Object mUndoValue;
    protected boolean mResetOnUndo;
    protected IPropertySource mTarget;

    public SetValueCommand()
    {
        super(""); //$NON-NLS-1$
    }

    public SetValueCommand(String propLabel)
    {
        super(MessageFormat.format(InstallOptionsPlugin.getResourceString("set.value.command.name"), new Object[]{propLabel}).trim()); //$NON-NLS-1$
    }

    @Override
    public boolean canExecute()
    {
        return true;
    }

    @Override
    public void execute()
    {
        boolean wasPropertySet = getTarget().isPropertySet(mPropertyName);
        mUndoValue = getTarget().getPropertyValue(mPropertyName);
        if (mUndoValue instanceof IPropertySource) {
            mUndoValue = ((IPropertySource)mUndoValue).getEditableValue();
        }
        if (mPropertyValue instanceof IPropertySource) {
            mPropertyValue = ((IPropertySource)mPropertyValue).getEditableValue();
        }
        getTarget().setPropertyValue(mPropertyName, mPropertyValue);
        if (getTarget() instanceof IPropertySource2) {
            mResetOnUndo = !wasPropertySet && ((IPropertySource2)getTarget()).isPropertyResettable(mPropertyName);
        }
        else {
            mResetOnUndo = !wasPropertySet && getTarget().isPropertySet(mPropertyName);
        }
        if (mResetOnUndo) {
            mUndoValue = null;
        }
    }

    public IPropertySource getTarget()
    {
        return mTarget;
    }

    public void setTarget(IPropertySource target)
    {
        mTarget = target;
    }

    @Override
    public void redo()
    {
        execute();
    }

    public void setPropertyId(Object pName)
    {
        mPropertyName = pName;
    }

    public void setPropertyValue(Object val)
    {
        mPropertyValue = val;
    }

    @Override
    public void undo()
    {
        if (mResetOnUndo) {
            getTarget().resetPropertyValue(mPropertyName);
        }
        else {
            getTarget().setPropertyValue(mPropertyName, mUndoValue);
        }
    }
}
