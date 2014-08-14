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

import org.eclipse.gef.commands.Command;

public class ModelCommandEvent
{
    private Object mModel;
    private Command mCommand;

    public ModelCommandEvent(Object model, Command command)
    {
        super();
        mModel = model;
        mCommand = command;
    }

    public Command getCommand()
    {
        return mCommand;
    }

    public Object getModel()
    {
        return mModel;
    }
}
