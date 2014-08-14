/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.pathrequest;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.editable.InstallOptionsEditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.*;

import org.eclipse.gef.tools.*;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public abstract class InstallOptionsPathRequestEditPart extends InstallOptionsEditableElementEditPart<TextCellEditor>
{
    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new PathRequestFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class<TextCellEditor> clasz, CellEditorLocator locator)
    {
        return new InstallOptionsPathRequestEditManager(part, clasz, locator);
    }

    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new PathRequestCellEditorLocator((PathRequestFigure)getFigure());
    }

    @Override
    protected Class<TextCellEditor> getCellEditorClass()
    {
        return TextCellEditor.class;
    }
}
