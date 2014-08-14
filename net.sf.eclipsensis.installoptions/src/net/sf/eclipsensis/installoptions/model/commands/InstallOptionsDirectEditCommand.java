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

import org.eclipse.gef.commands.Command;

public class InstallOptionsDirectEditCommand extends Command
{
    private InstallOptionsWidget mWidget;
    private Object mProperty;
    private Object mOldValue;
    private Object mNewValue;

    public InstallOptionsDirectEditCommand(InstallOptionsWidget widget, Object property, Object value)
    {
        super();
        mWidget = widget;
        mProperty = property;
        mNewValue = value;
        mOldValue = mWidget.getPropertyValue(mProperty);
    }

    @Override
    public void execute() {
        mWidget.setPropertyValue(mProperty, mNewValue);
    }

    @Override
    public void undo() {
        mWidget.setPropertyValue(mProperty, mOldValue);
    }
}
