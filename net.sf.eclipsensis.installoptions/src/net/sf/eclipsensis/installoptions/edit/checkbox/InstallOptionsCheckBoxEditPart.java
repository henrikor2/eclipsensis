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

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.edit.button.InstallOptionsButtonEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.tools.*;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsCheckBoxEditPart extends InstallOptionsButtonEditPart
{
    private IExtendedEditSupport mExtendedEditSupport = new IExtendedEditSupport() {
        private Object mNewValue;
        public boolean performExtendedEdit()
        {
            InstallOptionsCheckBox model = (InstallOptionsCheckBox)getModel();
            Integer state = model.getState();
            if(Common.objectsAreEqual(state,InstallOptionsModel.STATE_CHECKED)) {
                mNewValue = InstallOptionsModel.STATE_UNCHECKED;
            }
            else {
                mNewValue = InstallOptionsModel.STATE_CHECKED;
            }
            return true;
        }

        public Object getNewValue()
        {
            return mNewValue;
        }

    };

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class key)
    {
        if(IExtendedEditSupport.class.equals(key)) {
            return mExtendedEditSupport;
        }
        return super.getAdapter(key);
    }

    @Override
    protected String getDirectEditLabelProperty()
    {
        return "checkbox.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new CheckBoxFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new CheckBoxCellEditorLocator((CheckBoxFigure)figure);
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return new InstallOptionsCheckBoxEditManager(part, locator);
    }

    @Override
    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(InstallOptionsExtendedEditPolicy.ROLE, new InstallOptionsCheckBoxExtendedEditPolicy(this));
    }

    @Override
    protected String getExtendedEditLabelProperty()
    {
        return "checkbox.extended.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("checkbox.type.name"); //$NON-NLS-1$
    }

    @Override
    protected void handleFlagAdded(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_RIGHT)) {
            ((CheckBoxFigure)getFigure()).setLeftText(true);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagAdded(flag);
        }
    }

    @Override
    protected void handleFlagRemoved(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_RIGHT)) {
            ((CheckBoxFigure)getFigure()).setLeftText(false);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagRemoved(flag);
        }
    }

    @Override
    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_STATE)) {
            CheckBoxFigure figure2 = (CheckBoxFigure)getFigure();
            figure2.setState(InstallOptionsModel.STATE_CHECKED.equals(evt.getNewValue()));
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }
}
