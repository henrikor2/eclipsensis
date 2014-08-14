/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.combobox;

import net.sf.eclipsensis.installoptions.figures.ComboboxFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;

public class ComboboxCellEditorLocator implements CellEditorLocator
{
    private ComboboxFigure mCombobox;

    public ComboboxCellEditorLocator(ComboboxFigure combobox)
    {
        setCombobox(combobox);
    }

    public void relocate(CellEditor celleditor)
    {
        Combo combo = (Combo)celleditor.getControl();

        Rectangle rect = mCombobox.getDirectEditArea();
        mCombobox.translateToAbsolute(rect);
        Point p = combo.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        int height = rect.height - p.y;
        int itemHeight = combo.getItemHeight();
        int itemCount = Math.max(1,(int)Math.ceil(height/itemHeight));
        if(itemCount*itemHeight < height) {
            itemCount++;
        }
        combo.setVisibleItemCount(itemCount);
        combo.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    protected ComboboxFigure getCombobox() {
        return mCombobox;
    }

    protected void setCombobox(ComboboxFigure combobox)
    {
        mCombobox = combobox;
    }
}
