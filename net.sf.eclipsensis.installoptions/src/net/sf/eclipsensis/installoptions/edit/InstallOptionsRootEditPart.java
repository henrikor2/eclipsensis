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

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.actions.ToggleGuideVisibilityAction;

import org.eclipse.draw2d.*;
import org.eclipse.gef.editparts.*;

public class InstallOptionsRootEditPart extends ScalableFreeformRootEditPart implements IInstallOptionsConstants
{
    public static final String RESIZE_FEEDBACK_LAYER = "ResizeFeedback"; //$NON-NLS-1$

    private PropertyChangeListener mGridListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (property.equals(InstallOptionsGridLayer.PROPERTY_GRID_STYLE)) {
                refreshGridLayer();
            }
        }
    };

    private PropertyChangeListener mGuidesListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (property.equals(ToggleGuideVisibilityAction.PROPERTY_GUIDE_VISIBILITY)) {
                refreshGuideLayer();
            }
        }
    };

    protected void refreshGuideLayer()
    {
        boolean visible = true;
        IFigure layer = getLayer(GUIDE_LAYER);
        Boolean val = (Boolean)getViewer().getProperty(ToggleGuideVisibilityAction.PROPERTY_GUIDE_VISIBILITY);
        if (val != null) {
            visible = val.booleanValue();
        }
        layer.setVisible(visible);
    }


    @Override
    protected void refreshGridLayer()
    {
        InstallOptionsGridLayer grid = (InstallOptionsGridLayer)getLayer(GRID_LAYER);
        String val = (String)getViewer().getProperty(InstallOptionsGridLayer.PROPERTY_GRID_STYLE);
        if (val != null) {
            grid.setStyle(val);
        }
        super.refreshGridLayer();
    }

    @Override
    protected void register()
    {
        if (getLayer(GRID_LAYER) != null) {
            getViewer().addPropertyChangeListener(mGridListener);
        }
        if (getLayer(GUIDE_LAYER) != null) {
            getViewer().addPropertyChangeListener(mGuidesListener);
        }
        super.register();
    }

    @Override
    protected void unregister()
    {
        getViewer().removePropertyChangeListener(mGridListener);
        super.unregister();
    }

    @Override
    protected GridLayer createGridLayer()
    {
        return new InstallOptionsGridLayer();
    }

    @Override
    protected void createLayers(LayeredPane layeredPane)
    {
        super.createLayers(layeredPane);
        layeredPane.add(new ResizeFeedbackLayer(), RESIZE_FEEDBACK_LAYER);
    }

    private class ResizeFeedbackLayer extends FreeformLayer
    {
        ResizeFeedbackLayer()
        {
            setEnabled(false);
        }
    }
}
