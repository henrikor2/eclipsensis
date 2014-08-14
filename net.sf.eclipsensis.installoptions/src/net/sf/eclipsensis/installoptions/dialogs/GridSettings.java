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

import java.util.Map;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.geometry.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class GridSettings extends Composite implements IInstallOptionsConstants
{
    public static final String[][] GRID_STYLE_CONTENTS = {{GRID_STYLE_LINES,InstallOptionsPlugin.getResourceString("grid.settings.grid.style.lines.name")},{GRID_STYLE_DOTS,InstallOptionsPlugin.getResourceString("grid.settings.grid.style.dots.name")}}; //$NON-NLS-1$ //$NON-NLS-2$

    private Map<String,Object> mSettings = null;
    private Text mGridSpacingWidth;
    private Text mGridSpacingHeight;
    private Text mGridOriginX;
    private Text mGridOriginY;
    private ComboViewer mGridStyleViewer;

    private VerifyListener mNumberVerifyListener = new NumberVerifyListener(true);
    private VerifyListener mPositiveNumberVerifyListener = new NumberVerifyListener();

    /**
     * @param parent
     * @param style
     */
    public GridSettings(Composite parent, Map<String,Object> settings)
    {
        super(parent, SWT.NONE);
        initialize();
        setSettings(settings);
    }

    public void setSettings(Map<String,Object> settings)
    {
        mSettings = settings;
        Dimension d = null;
        try {
            d = (Dimension)mSettings.get(PREFERENCE_GRID_SPACING);
        }
        catch(Exception e) {
        }
        if(d == null) {
            d = new Dimension(GRID_SPACING_DEFAULT);
            mSettings.put(PREFERENCE_GRID_SPACING,d);
        }
        mGridSpacingWidth.setText(Integer.toString(d.width));
        mGridSpacingHeight.setText(Integer.toString(d.height));

        Point p = null;
        try {
            p = (Point)mSettings.get(PREFERENCE_GRID_ORIGIN);
        }
        catch(Exception e) {
        }
        if(p == null) {
            p = new Point(GRID_ORIGIN_DEFAULT);
            mSettings.put(PREFERENCE_GRID_ORIGIN,p);
        }
        mGridOriginX.setText(Integer.toString(p.x));
        mGridOriginY.setText(Integer.toString(p.y));

        String style = null;

        try {
            style = (String)mSettings.get(PREFERENCE_GRID_STYLE);
        }
        catch(Exception ex) {
            style = null;
        }
        if(style == null) {
            style = GRID_STYLE_LINES;
            mSettings.put(PREFERENCE_GRID_STYLE,style);
        }
        if(style.equals(GRID_STYLE_DOTS)) {
            mGridStyleViewer.setSelection(new StructuredSelection((Object)GRID_STYLE_CONTENTS[1]));
        }
        else {
            mGridStyleViewer.setSelection(new StructuredSelection((Object)GRID_STYLE_CONTENTS[0]));
        }
    }

    private void initialize()
    {
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        Group group = new Group(this,SWT.SHADOW_ETCHED_IN);
        group.setText(InstallOptionsPlugin.getResourceString("grid.settings.grid.group.name")); //$NON-NLS-1$
        gridLayout = new GridLayout(3,true);
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label l = new Label(group,SWT.NONE);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        l = new Label(group,SWT.NONE);
        l.setText(InstallOptionsPlugin.getResourceString("grid.settings.horizontal.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        l = new Label(group,SWT.NONE);
        l.setText(InstallOptionsPlugin.getResourceString("grid.settings.vertical.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        l = new Label(group,SWT.NONE);
        l.setText(InstallOptionsPlugin.getResourceString("grid.settings.grid.spacing.label")); //$NON-NLS-1$
        l.setToolTipText(InstallOptionsPlugin.getResourceString("grid.settings.grid.spacing.tooltip")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        mGridSpacingWidth = new Text(group,SWT.BORDER);
        mGridSpacingWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mGridSpacingWidth.addVerifyListener(mPositiveNumberVerifyListener);
        mGridSpacingWidth.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                String text = mGridSpacingWidth.getText();
                if(Common.isEmpty(text)) {
                    mGridSpacingWidth.setText(Integer.toString(((Dimension)mSettings.get(PREFERENCE_GRID_SPACING)).width));
                }
                else {
                    ((Dimension)mSettings.get(PREFERENCE_GRID_SPACING)).width = Integer.parseInt(text);
                }
            }
        });

        mGridSpacingHeight = new Text(group,SWT.BORDER);
        mGridSpacingHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mGridSpacingHeight.addVerifyListener(mPositiveNumberVerifyListener);
        mGridSpacingHeight.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                String text = mGridSpacingHeight.getText();
                if(Common.isEmpty(text)) {
                    mGridSpacingHeight.setText(Integer.toString(((Dimension)mSettings.get(PREFERENCE_GRID_SPACING)).height));
                }
                else {
                    ((Dimension)mSettings.get(PREFERENCE_GRID_SPACING)).height = Integer.parseInt(text);
                }
            }
        });

        l = new Label(group,SWT.NONE);
        l.setText(InstallOptionsPlugin.getResourceString("grid.settings.grid.origin.label")); //$NON-NLS-1$
        l.setToolTipText(InstallOptionsPlugin.getResourceString("grid.settings.grid.origin.tooltip")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        mGridOriginX = new Text(group,SWT.BORDER);
        mGridOriginX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mGridOriginX.addVerifyListener(mNumberVerifyListener);
        mGridOriginX.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                String text = mGridOriginX.getText();
                if(Common.isEmpty(text)) {
                    mGridOriginX.setText(Integer.toString(((Point)mSettings.get(PREFERENCE_GRID_ORIGIN)).x));
                }
                else {
                    ((Point)mSettings.get(PREFERENCE_GRID_ORIGIN)).x = Integer.parseInt(text);
                }
            }
        });


        mGridOriginY = new Text(group,SWT.BORDER);
        mGridOriginY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mGridOriginY.addVerifyListener(mNumberVerifyListener);
        mGridOriginY.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                String text = mGridOriginY.getText();
                if(Common.isEmpty(text)) {
                    mGridOriginY.setText(Integer.toString(((Point)mSettings.get(PREFERENCE_GRID_ORIGIN)).y));
                }
                else {
                    ((Point)mSettings.get(PREFERENCE_GRID_ORIGIN)).y = Integer.parseInt(text);
                }
            }
        });

        l = new Label(group,SWT.NONE);
        l.setText(InstallOptionsPlugin.getResourceString("grid.settings.grid.style.label")); //$NON-NLS-1$
        l.setToolTipText(InstallOptionsPlugin.getResourceString("grid.settings.grid.style.tooltip")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Combo mGridStyle = new Combo(group,SWT.READ_ONLY|SWT.DROP_DOWN);
        mGridStyle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mGridStyleViewer = new ComboViewer(mGridStyle);
        mGridStyleViewer.setContentProvider(new ArrayContentProvider());
        mGridStyleViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element)
            {
                return ((String[])element)[1];
            }

        });
        mGridStyleViewer.setInput(GRID_STYLE_CONTENTS);
        mGridStyleViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection ssel = (IStructuredSelection)event.getSelection();
                mSettings.put(PREFERENCE_GRID_STYLE,((String[])ssel.getFirstElement())[0]);
            }
        });
    }
}
