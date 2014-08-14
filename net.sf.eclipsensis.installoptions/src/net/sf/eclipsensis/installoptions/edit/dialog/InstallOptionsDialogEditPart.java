/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.*;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.*;

public class InstallOptionsDialogEditPart extends InstallOptionsEditPart implements LayerConstants, IInstallOptionsConstants
{
    private FreeformLayout mLayout = new FreeformLayout();
    private PropertyChangeListener mPropertyChangeListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent event)
        {
            String property = event.getPropertyName();
            if(property.equals(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE)) {
                DialogSize d = (DialogSize)event.getNewValue();
                getInstallOptionsDialog().setDialogSize(d);
                InstallOptionsDialogLayer fig = (InstallOptionsDialogLayer)getFigure();
                if(fig != null) {
                    fig.setDialogSize(d.getSize());
                }
                List<?> children = getChildren();
                for (Iterator<?> iter = children.iterator(); iter.hasNext();) {
                    InstallOptionsWidgetEditPart element = (InstallOptionsWidgetEditPart)iter.next();
                    IFigure figure = element.getFigure();
                    if(figure != null) {
                        InstallOptionsWidget widget = element.getInstallOptionsWidget();
                        Rectangle bounds = widget.toGraphical(widget.getPosition(),d.getSize()).getBounds();
                        setLayoutConstraint(element, figure, bounds);
                    }
                }
            }
            else if(property.equals(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE)) {
                Boolean b = (Boolean)event.getNewValue();
                getInstallOptionsDialog().setShowDialogSize(b.booleanValue());
                InstallOptionsDialogLayer fig = (InstallOptionsDialogLayer)getFigure();
                if(fig != null) {
                    fig.setShowDialogSize(b.booleanValue());
                }
            }
        }
    };

    protected InstallOptionsDialog getInstallOptionsDialog()
    {
        return (InstallOptionsDialog)getModel();
    }

    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (InstallOptionsDialog.PROPERTY_SELECTION.equals(prop)) {
            List<InstallOptionsWidget> modelSelection = (List<InstallOptionsWidget>)evt.getNewValue();
            List<EditPart> selection = new ArrayList<EditPart>();
            for (Iterator<InstallOptionsWidget> iter = modelSelection.iterator(); iter.hasNext();) {
                InstallOptionsWidget element = iter.next();
                selection.add((EditPart) getViewer().getEditPartRegistry().get(element));
            }
            getViewer().setSelection(new StructuredSelection(selection));
            getViewer().getControl().forceFocus();
        }
        else if (InstallOptionsModel.PROPERTY_RTL.equals(prop)) {
            refreshDiagram();
        }
        else if (InstallOptionsModel.PROPERTY_CHILDREN.equals(prop)) {
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            List<EditPart> selection = null;
            int index = -1;
            if(oldValue instanceof InstallOptionsWidget && newValue instanceof InstallOptionsWidget) {
                //Replaced child
                if(getViewer() != null) {
                    ISelection sel = getViewer().getSelection();
                    if(sel instanceof IStructuredSelection) {
                        selection = new ArrayList<EditPart>(((IStructuredSelection)sel).toList());
                        for (Iterator<EditPart> iter = selection.iterator(); iter.hasNext();) {
                            EditPart part = iter.next();
                            if(oldValue.equals(part.getModel())) {
                                index = selection.indexOf(part);
                                break;
                            }
                        }
                        if(index < 0) {
                            selection = null;
                        }
                    }
                }
            }
            //This bit is in here to correct z-ordering of children.
            List<InstallOptionsWidget> modelChildren = getModelChildren();
            List<?> children = getChildren();
            int n = Math.min(modelChildren.size(), children.size());
            int i=0;
            for(; i<n; i++) {
                InstallOptionsWidget model = modelChildren.get(i);
                EditPart part = (EditPart)children.get(i);
                if(model != part.getModel()) {
                    break;
                }
            }
            refreshChildren();
            if(i < n) {
                for(int j=i; j<children.size(); j++) {
                    GraphicalEditPart part = (GraphicalEditPart)children.get(j);
                    IFigure fig = part.getFigure();
                    LayoutManager layout = getContentPane().getLayoutManager();
                    Object constraint = null;
                    if (layout != null) {
                        constraint = layout.getConstraint(fig);
                    }
                    getContentPane().remove(fig);
                    getContentPane().add(fig);
                    setLayoutConstraint(part, fig, constraint);
                }
            }
            if(selection != null && index >= 0) {
                selection.remove(index);
                EditPart part = (EditPart)getViewer().getEditPartRegistry().get(newValue);
                if(part != null) {
                    selection.add(index,part);
                    final IStructuredSelection sel = new StructuredSelection(selection);
                    final Display display = getViewer().getControl().getDisplay();
                    display.asyncExec(new Runnable() {
                        public void run()
                        {
                            Control c = display.getFocusControl();
                            try {
                                getViewer().setSelection(sel);
                                getViewer().getControl().forceFocus();
                            }
                            finally {
                                if(c != null && !c.isDisposed()) {
                                    c.forceFocus();
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Returns the Children of this through the model.
     *
     * @return Children of this as a List.
     */
    @Override
    protected List<InstallOptionsWidget> getModelChildren()
    {
        List<InstallOptionsWidget> list = new ArrayList<InstallOptionsWidget>(getInstallOptionsDialog().getChildren());
        Collections.reverse(list);
        return list;
    }

    @Override
    protected AccessibleEditPart createAccessible()
    {
        return new AccessibleGraphicalEditPart() {
            @Override
            public void getName(AccessibleEvent e)
            {
                e.result = InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
            }
        };
    }

    /**
     * Installs EditPolicies specific to this.
     */
    @Override
    protected void createEditPolicies()
    {
        super.createEditPolicies();

        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE,
                        new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.CONTAINER_ROLE, new InstallOptionsDialogEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new InstallOptionsXYLayoutEditPolicy((XYLayout)getContentPane().getLayoutManager()));

        installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    public void refreshDiagram()
    {
        InstallOptionsWidgetEditPart child;

        for (Iterator<?> iter = getChildren().iterator(); iter.hasNext(); ) {
            child = (InstallOptionsWidgetEditPart)iter.next();
            ((IInstallOptionsFigure)child.getFigure()).refresh();
        }
    }

    /**
     * Returns a Figure to represent this.
     *
     * @return Figure.
     */
    @Override
    protected IFigure createFigure()
    {
        InstallOptionsDialogLayer f = new InstallOptionsDialogLayer();
        f.setDialogSize(getInstallOptionsDialog().getDialogSize().getSize());
        f.setShowDialogSize(getInstallOptionsDialog().isShowDialogSize());
        f.setLayoutManager(mLayout);
        f.setBorder(new MarginBorder(5));
        return f;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if (adapter == SnapToHelper.class) {
            List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();

            Boolean val = (Boolean)getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
            if (val != null && val.booleanValue()) {
                val = (Boolean)getViewer().getProperty(PROPERTY_SNAP_TO_GUIDES);
                if (val != null && val.booleanValue()) {
                    snapStrategies.add(new SnapToGuides(this));
                }
            }

            val = (Boolean)getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGeometry(this));
            }

            val = (Boolean)getViewer().getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE);
            if (val != null && val.booleanValue()) {
                val = (Boolean)getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
                if (val != null && val.booleanValue()) {
                    snapStrategies.add(new InstallOptionsSnapToGrid(this));
                }
            }

            if (snapStrategies.size() == 0) {
                return null;
            }
            if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }

            SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        getViewer().setProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE,
                        getInstallOptionsDialog().getDialogSize().getCopy());
        getViewer().setProperty(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE,
                        Boolean.valueOf(getInstallOptionsDialog().isShowDialogSize()));
        getViewer().addPropertyChangeListener(mPropertyChangeListener);
    }

    @Override
    public void removeNotify()
    {
        getViewer().removePropertyChangeListener(mPropertyChangeListener);
        super.removeNotify();
    }

    @Override
    public DragTracker getDragTracker(Request req)
    {
        if (req instanceof SelectionRequest
                        && ((SelectionRequest)req).getLastButtonPressed() == 3) {
            return new DeselectAllTracker(this);
        }
        return new MarqueeDragTracker();
    }

    @Override
    protected void refreshVisuals()
    {
        getFigure().setLayoutManager(mLayout);
        List<?> children = getChildren();
        for (Iterator<?> iter = children.iterator(); iter.hasNext();) {
            EditPart part = (EditPart)iter.next();
            part.refresh();
        }
    }

    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
    }
}