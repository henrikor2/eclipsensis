/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.checkbox;

import net.sf.eclipsensis.installoptions.edit.button.ButtonCellEditorLocator;
import net.sf.eclipsensis.installoptions.figures.CheckBoxFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Point;

public class CheckBoxCellEditorLocator extends ButtonCellEditorLocator
{
    private static final int X_OFFSET = -4;
    private static final int W_OFFSET = -10;

    public CheckBoxCellEditorLocator(CheckBoxFigure button)
    {
        super(button);
    }

    @Override
    protected Rectangle transformLocation(Rectangle editArea, Point preferredSize)
    {
        CheckBoxFigure figure = (CheckBoxFigure)getFigure();
        return new Rectangle(editArea.x + X_OFFSET + (figure.isLeftText()?0:15),
                             editArea.y+(editArea.height-preferredSize.y)/2,
                             editArea.width + W_OFFSET, preferredSize.y);
    }
}

