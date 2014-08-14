/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.gef.*;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.Action;

public class ToggleGridVisibilityAction extends Action
{
    private GraphicalViewer mDiagramViewer;

    /**
     * @param diagramViewer
     */
    public ToggleGridVisibilityAction(GraphicalViewer diagramViewer)
    {
        super(InstallOptionsPlugin.getResourceString("toggle.grid.label"), AS_CHECK_BOX); //$NON-NLS-1$
        this.mDiagramViewer = diagramViewer;
        setToolTipText(InstallOptionsPlugin.getResourceString("toggle.grid.tooltip")); //$NON-NLS-1$
        setId(GEFActionConstants.TOGGLE_GRID_VISIBILITY);
        setActionDefinitionId(GEFActionConstants.TOGGLE_GRID_VISIBILITY);
        setChecked(isChecked());
    }

    @Override
    public boolean isChecked()
    {
        Boolean val = (Boolean)mDiagramViewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE);
        if (val != null) {
            return val.booleanValue();
        }
        return false;
    }

    @Override
    public void run()
    {
        mDiagramViewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, (isChecked()?Boolean.FALSE:Boolean.TRUE));
    }
}
