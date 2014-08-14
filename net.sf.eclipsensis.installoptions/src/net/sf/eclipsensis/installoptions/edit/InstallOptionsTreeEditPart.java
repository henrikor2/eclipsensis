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

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.*;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public class InstallOptionsTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener
{
    /**
     * Constructor initializes this with the given model.
     *
     * @param model
     *            Model for this.
     */
    public InstallOptionsTreeEditPart(Object model)
    {
        super(model);
    }

    @Override
    public void activate()
    {
        super.activate();
        InstallOptionsElement element = getInstallOptionsElement();
        if(element != null) {
            element.addPropertyChangeListener(this);
        }
    }

    /**
     * Creates and installs pertinent EditPolicies for this.
     */
    @Override
    protected void createEditPolicies()
    {
        EditPolicy component = new InstallOptionsEditPolicy();
        installEditPolicy(EditPolicy.COMPONENT_ROLE, component);
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new InstallOptionsTreeEditPolicy());
    }

    @Override
    public void deactivate()
    {
        InstallOptionsElement element = getInstallOptionsElement();
        if(element != null) {
            element.removePropertyChangeListener(this);
        }
        super.deactivate();
    }

    /**
     * Returns the model of this as a InstallOptionsElement.
     *
     * @return Model of this.
     */
    protected InstallOptionsElement getInstallOptionsElement()
    {
        return (InstallOptionsElement)getModel();
    }

    /**
     * Returns <code>null</code> as a Tree EditPart holds no children under
     * it.
     *
     * @return <code>null</code>
     */
    @Override
    protected List<InstallOptionsWidget> getModelChildren()
    {
        return Collections.emptyList();
    }

    public void propertyChange(final PropertyChangeEvent change)
    {
        if (change.getPropertyName().equals(InstallOptionsModel.PROPERTY_CHILDREN)) {
            Object oldValue = change.getOldValue();
            Object newValue = change.getNewValue();
            if(oldValue == null && newValue == null) {
                ISelection sel = getViewer().getSelection();
                refreshChildren();
                getViewer().setSelection(sel);
            }
            else {
                if (oldValue instanceof Integer) {
                    // new child
                    addChild(createChild(newValue), ((Integer)oldValue).intValue());
                }
                else {
                    EditPart editPart = (EditPart)getViewer().getEditPartRegistry().get(oldValue);
                    if(newValue == null) {
                        // remove child
                        removeChild(editPart);
                    }
                    else if (newValue instanceof Integer){
                        // reorder child
                        ISelection sel = getViewer().getSelection();
                        reorderChild(editPart, ((Integer)newValue).intValue());
                        getViewer().setSelection(sel);
                    }
                    else {
                        //replace child
                        replaceChild(editPart,createChild(newValue));
                    }
                }
            }
        }
        refreshVisuals();
    }

    @SuppressWarnings("unchecked")
    protected void replaceChild(EditPart oldChild, EditPart newChild)
    {
        Assert.isNotNull(oldChild);
        Assert.isNotNull(newChild);

        int index = getChildren().indexOf(oldChild);
        if (index < 0) {
            return;
        }
        fireRemovingChild(oldChild, index);
        if (isActive()) {
            oldChild.deactivate();
        }
        oldChild.removeNotify();
        removeChildVisual(oldChild);
        oldChild.setParent(null);
        getChildren().remove(oldChild);

        getChildren().add(index, newChild);
        newChild.setParent(this);
        addChildVisual(newChild, index);
        newChild.addNotify();

        if (isActive()) {
            newChild.activate();
        }
        fireChildAdded(newChild, index);
    }

    /**
     * Refreshes the Widget of this based on the property given to update. All
     * major properties are updated irrespective of the property input.
     *
     * @param property
     *            Property to be refreshed.
     */
    @Override
    protected void refreshVisuals()
    {
        if (getWidget() instanceof Tree) {
            for(Iterator<?> iter = getChildren().iterator(); iter.hasNext();) {
                ((InstallOptionsTreeEditPart)iter.next()).refreshVisuals();
            }
        }
        else {
            TreeItem item = (TreeItem)getWidget();
            if(item != null) {
                InstallOptionsElement element = getInstallOptionsElement();
                if(element != null) {
                    Image image = element.getIcon();
                    Image itemImage = item.getImage();
                    if (image != itemImage) {
                        if(image != null) {
                            image.setBackground(item.getParent().getBackground());
                        }
                        setWidgetImage(image);
                    }
                    String string = getInstallOptionsElement().toString();
                    if(string != null && !string.equals(item.getText())) {
                        setWidgetText(string);
                    }
                }
            }
        }
    }
}