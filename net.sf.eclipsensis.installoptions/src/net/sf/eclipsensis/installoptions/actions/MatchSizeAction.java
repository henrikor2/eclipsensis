/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;

public class MatchSizeAction extends MatchWidthAction
{
    public static final String ID = "net.sf.eclipsensis.installoptions.match_size"; //$NON-NLS-1$

    public MatchSizeAction(IWorkbenchPart part)
    {
        super(part);
        setId(ID);
        setText(InstallOptionsPlugin.getResourceString("match.size.action.name")); //$NON-NLS-1$
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("match.size.icon")); //$NON-NLS-1$
        setImageDescriptor(imageDescriptor);
        setHoverImageDescriptor(imageDescriptor);
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("match.size.disabled.icon"))); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("match.size.action.tooltip")); //$NON-NLS-1$
    }

    @Override
    protected double getPreciseHeightDelta(PrecisionRectangle precisePartBounds, PrecisionRectangle precisePrimaryBounds)
    {
        return precisePrimaryBounds.preciseHeight - precisePartBounds.preciseHeight;
    }
}
