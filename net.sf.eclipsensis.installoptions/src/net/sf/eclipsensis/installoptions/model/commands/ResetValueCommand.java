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

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.views.properties.*;

public class ResetValueCommand extends Command
{
    /** the property that has to be reset */
    protected Object propertyName;
    /** the current non-default value of the property */
    protected Object undoValue;
    /** the property source whose property has to be reset */
    protected IPropertySource target;

    /**
     * Default Constructor: Sets the label for the Command
     * @since 3.1
     */
    public ResetValueCommand()
    {
        super(InstallOptionsPlugin.getResourceString("reset.value.command.name")); //$NON-NLS-1$
    }

    /**
     * Returns <code>true</code> IFF:<br>
     * 1) the target and property have been specified<br>
     * 2) the property has a default value<br>
     * 3) the value set for that property is not the default
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    @Override
    public boolean canExecute()
    {
        boolean answer = false;
        if (target != null && propertyName != null) {
            answer = target.isPropertySet(propertyName);
            if (target instanceof IPropertySource2) {
                answer = answer && (((IPropertySource2)target).isPropertyResettable(propertyName));
            }
        }
        return answer;
    }

    /**
     * Caches the undo value and invokes redo()
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute()
    {
        undoValue = target.getPropertyValue(propertyName);
        if (undoValue instanceof IPropertySource) {
            undoValue = ((IPropertySource)undoValue).getEditableValue();
        }
        redo();
    }

    /**
     * Sets the IPropertySource.
     * @param propSource the IPropertySource whose property has to be reset
     */
    public void setTarget(IPropertySource propSource)
    {
        target = propSource;
    }

    /**
     * Resets the specified property on the specified IPropertySource
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo()
    {
        target.resetPropertyValue(propertyName);
    }

    /**
     * Sets the property that is to be reset.
     * @param pName the property to be reset
     */
    public void setPropertyId(Object pName)
    {
        propertyName = pName;
    }

    /**
     * Restores the non-default value that was reset.
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo()
    {
        target.setPropertyValue(propertyName, undoValue);
    }
}
