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
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.rulers.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.graphics.Font;

public class CreateGuideCommand extends Command
{
    private InstallOptionsGuide mGuide;

    private InstallOptionsRuler mParent;

    private int mPosition;

    public CreateGuideCommand(InstallOptionsRuler parent, int position)
    {
        super(InstallOptionsPlugin.getResourceString("create.guide.command.name")); //$NON-NLS-1$
        this.mParent = parent;
        this.mPosition = position;
    }

    @Override
    public boolean canUndo()
    {
        return true;
    }

    @Override
    public void execute()
    {
        if (mGuide == null) {
            mGuide = new InstallOptionsGuide(!mParent.isHorizontal());
        }
        Font f = FontUtility.getInstallOptionsFont();
        mGuide.setPosition(mGuide.isHorizontal()?FigureUtility.pixelsToDialogUnitsY(mPosition,f):FigureUtility.pixelsToDialogUnitsX(mPosition,f));
        mParent.addGuide(mGuide);
    }

    @Override
    public void undo()
    {
        mParent.removeGuide(mGuide);
    }
}