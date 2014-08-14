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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.*;

import org.eclipse.gef.tools.*;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsGroupBoxEditPart extends InstallOptionsUneditableElementEditPart
{
    @Override
    protected String getDirectEditLabelProperty()
    {
        return "groupbox.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new GroupBoxFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    /**
     * @return
     */
    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("groupbox.type.name"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#creatDirectEditManager(net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart, java.lang.Class, org.eclipse.gef.tools.CellEditorLocator)
     */
    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return new InstallOptionsGroupBoxEditManager(part, locator);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#createCellEditorLocator(net.sf.eclipsensis.installoptions.figures.UneditableElementFigure)
     */
    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new GroupBoxCellEditorLocator((GroupBoxFigure)getFigure());
    }
}
