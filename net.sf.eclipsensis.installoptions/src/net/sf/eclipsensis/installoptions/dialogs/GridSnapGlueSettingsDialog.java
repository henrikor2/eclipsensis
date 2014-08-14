/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dialogs;

import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsGridLayer;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class GridSnapGlueSettingsDialog extends Dialog implements IInstallOptionsConstants
{
    private GraphicalViewer mViewer;
    private Map<String, Object> mGridSettingsMap = new HashMap<String, Object>();
    private Map<String, Boolean> mSnapGlueSettingsMap = new HashMap<String, Boolean>();

    /**
     * @param parentShell
     */
    public GridSnapGlueSettingsDialog(Shell parentShell, GraphicalViewer viewer)
    {
        super(parentShell);
        setShellStyle(getShellStyle()|SWT.RESIZE);
        mViewer = viewer;
        loadViewerProperty(mGridSettingsMap, PREFERENCE_GRID_ORIGIN, SnapToGrid.PROPERTY_GRID_ORIGIN, GRID_ORIGIN_DEFAULT);
        loadViewerProperty(mGridSettingsMap, PREFERENCE_GRID_SPACING, SnapToGrid.PROPERTY_GRID_SPACING, GRID_SPACING_DEFAULT);
        loadViewerProperty(mGridSettingsMap, PREFERENCE_GRID_STYLE, InstallOptionsGridLayer.PROPERTY_GRID_STYLE, GRID_STYLE_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID, SnapToGrid.PROPERTY_GRID_ENABLED, SNAP_TO_GRID_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY, SnapToGeometry.PROPERTY_SNAP_ENABLED, SNAP_TO_GEOMETRY_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES, PROPERTY_SNAP_TO_GUIDES, SNAP_TO_GUIDES_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES, PROPERTY_GLUE_TO_GUIDES, GLUE_TO_GUIDES_DEFAULT);
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(InstallOptionsPlugin.getResourceString("grid.snap.glue.settings.dialog.name")); //$NON-NLS-1$
        newShell.setImage(InstallOptionsPlugin.getShellImage());
    }

    @SuppressWarnings("unchecked")
    private <T> T makeCopy(T o)
    {
        T o2 = o;
        if(o2 instanceof Point) {
            o2 = (T)new Point((Point)o2);
        }
        else if(o2 instanceof Dimension) {
            o2 = (T)new Dimension((Dimension)o2);
        }
        return o2;
    }

    @SuppressWarnings("unchecked")
    private <T> void loadViewerProperty(Map<String, ? super T> map, String mapName, String name, T defaultValue)
    {
        T o = null;
        try {
            o = (T)mViewer.getProperty(name);
        }
        catch(Exception e) {
        }
        if(o == null) {
            o = defaultValue;
        }
        map.put(mapName,makeCopy(o));
    }

    @SuppressWarnings("unchecked")
    private <T> void saveViewerProperty(Map<String, ? super T> map, String mapName, String name, T defaultValue)
    {
        T o = null;
        try {
            o = (T) map.get(mapName);
        }
        catch(Exception e) {
        }
        if(o == null) {
            o = defaultValue;
        }
        mViewer.setProperty(name,o);
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);

        new GridSettings(composite,mGridSettingsMap);
        new SnapGlueSettings(composite,mSnapGlueSettingsMap);

        initializeDialogUnits(composite);
        GridData data = (GridData)composite.getLayoutData();
        data.widthHint = convertWidthInCharsToPixels(50);
        return composite;
    }

    @Override
    protected void okPressed()
    {
        saveViewerProperty(mGridSettingsMap, PREFERENCE_GRID_ORIGIN, SnapToGrid.PROPERTY_GRID_ORIGIN, GRID_ORIGIN_DEFAULT);
        saveViewerProperty(mGridSettingsMap, PREFERENCE_GRID_SPACING, SnapToGrid.PROPERTY_GRID_SPACING, GRID_SPACING_DEFAULT);
        saveViewerProperty(mGridSettingsMap, PREFERENCE_GRID_STYLE, InstallOptionsGridLayer.PROPERTY_GRID_STYLE, GRID_STYLE_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID, SnapToGrid.PROPERTY_GRID_ENABLED, SNAP_TO_GRID_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY, SnapToGeometry.PROPERTY_SNAP_ENABLED, SNAP_TO_GEOMETRY_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES, PROPERTY_SNAP_TO_GUIDES, SNAP_TO_GUIDES_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES, PROPERTY_GLUE_TO_GUIDES, GLUE_TO_GUIDES_DEFAULT);
        super.okPressed();
    }
}
