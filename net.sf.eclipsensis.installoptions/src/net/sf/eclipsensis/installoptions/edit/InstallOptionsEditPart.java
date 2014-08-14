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

import java.beans.PropertyChangeListener;

import net.sf.eclipsensis.installoptions.model.InstallOptionsElement;
import net.sf.eclipsensis.installoptions.model.commands.IModelCommandListener;

import org.eclipse.gef.*;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

public abstract class InstallOptionsEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener
{
    private AccessibleEditPart mAccessible;

    @Override
    public void activate()
    {
        if (isActive()) {
            return;
        }
        super.activate();
        getInstallOptionsElement().addPropertyChangeListener(this);
    }

    @Override
    protected void createEditPolicies()
    {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new InstallOptionsEditPolicy());
    }

    abstract protected AccessibleEditPart createAccessible();

    /**
     * Makes the EditPart insensible to changes in the model by removing itself
     * from the model's list of mListeners.
     */
    @Override
    public void deactivate()
    {
        if (!isActive()) {
            return;
        }
        super.deactivate();
        getInstallOptionsElement().removePropertyChangeListener(this);
    }

    @Override
    protected AccessibleEditPart getAccessibleEditPart()
    {
        if (mAccessible == null) {
            mAccessible = createAccessible();
        }
        return mAccessible;
    }

    protected InstallOptionsElement getInstallOptionsElement()
    {
        return (InstallOptionsElement)getModel();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        ((InstallOptionsElement)getModel()).addModelCommandListener(getModelCommandListener());
    }

    private IModelCommandListener getModelCommandListener()
    {
        return (IModelCommandListener)((InstallOptionsEditDomain)getViewer().getEditDomain()).getAdapter(IModelCommandListener.class);
    }

    @Override
    public void removeNotify()
    {
        ((InstallOptionsElement)getModel()).removeModelCommandListener(getModelCommandListener());
        super.removeNotify();
    }

    protected abstract String getTypeName();
}