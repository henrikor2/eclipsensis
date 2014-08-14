/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.listbox;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsCellEditorLocator;
import net.sf.eclipsensis.installoptions.figures.ListFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Point;

public class ListboxCellEditorLocator extends InstallOptionsCellEditorLocator
{
    public ListboxCellEditorLocator(ListFigure listbox)
    {
        super(listbox);
    }

    @Override
    protected Rectangle transformLocation(Rectangle editArea, Point preferredSize)
    {
        return editArea;
    }
}
