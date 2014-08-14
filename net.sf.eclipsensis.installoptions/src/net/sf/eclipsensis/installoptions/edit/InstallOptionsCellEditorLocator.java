/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public abstract class InstallOptionsCellEditorLocator implements CellEditorLocator
{
    private IInstallOptionsFigure mFigure;

    public InstallOptionsCellEditorLocator(IInstallOptionsFigure figure)
    {
        super();
        mFigure = figure;
    }

    public void relocate(CellEditor celleditor)
    {
        Control ctrl = celleditor.getControl();

        Rectangle editArea = getFigure().getDirectEditArea();
        getFigure().translateToAbsolute(editArea);
        Point preferredSize = ctrl.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        editArea = transformLocation(editArea, preferredSize);
        ctrl.setBounds(editArea.x, editArea.y, editArea.width, editArea.height);
    }

    protected IInstallOptionsFigure getFigure()
    {
        return mFigure;
    }

    protected abstract Rectangle transformLocation(Rectangle editArea, Point preferredSize);
}
