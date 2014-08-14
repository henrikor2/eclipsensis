/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.internal.ui.rulers.GuideEditPart;
import org.eclipse.swt.graphics.Font;

@SuppressWarnings("restriction")
public class InstallOptionsGuideEditPart extends GuideEditPart
{
    /**
     * @param model
     */
    public InstallOptionsGuideEditPart(Object model)
    {
        super(model);
    }

    @Override
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new InstallOptionsDragGuidePolicy());
    }

    @Override
    public void updateLocationOfFigures(int position)
    {
        Font f = FontUtility.getInstallOptionsFont();
        int position2 = (isHorizontal()?FigureUtility.pixelsToDialogUnitsY(position,f):FigureUtility.pixelsToDialogUnitsX(position,f));
        position2 = (isHorizontal()?FigureUtility.dialogUnitsToPixelsY(position2,f):FigureUtility.dialogUnitsToPixelsX(position2,f));
        super.updateLocationOfFigures(position2);
    }
}
