/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;

public class DashedLineBorder extends LineBorder
{
    public static final int[] DASHES = {16,8};

    public DashedLineBorder()
    {
        super(ColorManager.getColor(ColorManager.BLACK),1);
    }

    @Override
    public void paint(IFigure figure, Graphics graphics, Insets insets)
    {
        int oldStyle = graphics.getLineStyle();
        graphics.setLineStyle(SWT.LINE_CUSTOM);
        graphics.setLineDash(DASHES);
        super.paint(figure, graphics, insets);
        graphics.setLineStyle(oldStyle);
    }
}
