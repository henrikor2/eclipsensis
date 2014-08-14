/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.uneditable;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.IUneditableElementFigure;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.viewers.*;

public abstract class InstallOptionsUneditableElementEditPart extends InstallOptionsWidgetEditPart
{
    @Override
    protected String getAccessibleControlEventResult()
    {
        return getInstallOptionsUneditableElement().getText();
    }

    @Override
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, createDirectEditPolicy());
    }

    /**
     * @return
     */
    protected UneditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new UneditableElementDirectEditPolicy();
    }

    protected InstallOptionsUneditableElement getInstallOptionsUneditableElement()
    {
        return (InstallOptionsUneditableElement)getModel();
    }

    protected Class<? extends CellEditor> getCellEditorClass()
    {
        return TextCellEditor.class;
    }

    @Override
    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_TEXT)) {
            IUneditableElementFigure figure2 = (IUneditableElementFigure)getFigure();
            figure2.setText((String)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }
}
