/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.line;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsLine;

import org.eclipse.gef.tools.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public abstract class InstallOptionsLineEditPart extends InstallOptionsWidgetEditPart
{
    @Override
    protected String getDirectEditLabelProperty()
    {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return null;
    }

    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return null;
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        InstallOptionsLine line = (InstallOptionsLine)getInstallOptionsWidget();
        return new LineFigure((Composite)getViewer().getControl(), line, line.isHorizontal()?SWT.HORIZONTAL:SWT.VERTICAL);
    }

    @Override
    protected String getAccessibleControlEventResult()
    {
        return ""; //$NON-NLS-1$
    }
}
