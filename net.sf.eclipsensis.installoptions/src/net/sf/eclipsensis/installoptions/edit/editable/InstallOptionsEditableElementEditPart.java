/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.editable;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.IEditableElementFigure;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.viewers.CellEditor;

public abstract class InstallOptionsEditableElementEditPart<T extends CellEditor> extends InstallOptionsWidgetEditPart
{
    @Override
    protected String getAccessibleControlEventResult()
    {
        return getInstallOptionsEditableElement().getState();
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
    protected EditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new EditableElementDirectEditPolicy();
    }

    protected InstallOptionsEditableElement getInstallOptionsEditableElement()
    {
        return (InstallOptionsEditableElement)getModel();
    }

    @Override
    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            IEditableElementFigure figure2 = (IEditableElementFigure)getFigure();
            figure2.setState((String)evt.getNewValue());
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }

    @Override
    protected final DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return creatDirectEditManager(part, getCellEditorClass(), locator);
    }

    @Override
    protected String getTypeName() {
        return null;
    }

    protected abstract Class<T> getCellEditorClass();
    protected abstract DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class<T> clasz, CellEditorLocator locator);
}
