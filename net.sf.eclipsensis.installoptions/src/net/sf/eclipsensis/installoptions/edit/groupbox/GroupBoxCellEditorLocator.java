/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.groupbox;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsCellEditorLocator;
import net.sf.eclipsensis.installoptions.figures.GroupBoxFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Point;

public class GroupBoxCellEditorLocator extends InstallOptionsCellEditorLocator
{
    private static final int TEXT_OFFSET = 10;
    private static final int X_OFFSET = -4 + TEXT_OFFSET;
    private static final int W_OFFSET = 5 - TEXT_OFFSET;

    public GroupBoxCellEditorLocator(GroupBoxFigure groupBox)
    {
        super(groupBox);
    }

    @Override
    protected Rectangle transformLocation(Rectangle editArea, Point preferredSize)
    {
        return new Rectangle(editArea.x + X_OFFSET, editArea.y, editArea.width + W_OFFSET, preferredSize.y);
    }
}
