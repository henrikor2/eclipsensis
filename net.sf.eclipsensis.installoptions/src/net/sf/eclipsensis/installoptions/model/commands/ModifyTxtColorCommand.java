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
import net.sf.eclipsensis.installoptions.model.InstallOptionsLink;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.RGB;

public class ModifyTxtColorCommand extends Command
{
    private InstallOptionsLink mModel;
    private RGB mOldTxtColor;
    private RGB mNewTxtColor;

    public ModifyTxtColorCommand(InstallOptionsLink model, RGB newTxtColor)
    {
        mModel = model;
        mNewTxtColor = newTxtColor;
        mOldTxtColor = mModel.getTxtColor();
        setLabel(InstallOptionsPlugin.getFormattedString("modify.txtcolor.command.label", new Object[]{mModel.getType()})); //$NON-NLS-1$
    }

    @Override
    public void execute()
    {
        mModel.setTxtColor(mNewTxtColor);
    }

    @Override
    public void undo()
    {
        mModel.setTxtColor(mOldTxtColor);
    }
}
