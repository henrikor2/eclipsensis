/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;


import net.sf.eclipsensis.settings.*;

import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

abstract class NSISTab extends AbstractLaunchConfigurationTab implements INSISSettingsEditorPageListener
{
    protected NSISSettingsEditorPage mPage;
    protected NSISLaunchSettings mSettings;

    public NSISTab()
    {
        mSettings = new NSISLaunchSettings(NSISPreferences.getInstance(),null,createSettingsFilter());
        mPage = createPage();
    }

    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        Control control = mPage.create(composite);
        control.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

        setControl(composite);
        mPage.addListener(this);
        Dialog.applyDialogFont(parent);
}

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
    {
        ILaunchConfiguration config = mSettings.getLaunchConfig();
        try {
            mSettings.setLaunchConfig(null);
            mSettings.load();
            mSettings.setLaunchConfig(configuration);
            mPage.performApply();
            mSettings.store();
        }
        finally {
            mSettings.setLaunchConfig(config);
            mSettings.load();
        }
    }

    public void initializeFrom(ILaunchConfiguration configuration)
    {
        mSettings.setLaunchConfig(configuration);
        mSettings.load();
        mPage.reset();
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration)
    {
        ILaunchConfiguration config = mSettings.getLaunchConfig();
        try {
            mSettings.setLaunchConfig(configuration);
            mPage.performApply();
            mSettings.store();
        }
        finally {
            mSettings.setLaunchConfig(config);
        }
    }

    public void settingsChanged()
    {
        updateLaunchConfigurationDialog();
    }

    protected ControlAdapter createTableControlListener(final ControlAdapter controlAdapter)
    {
        return new ControlAdapter() {
            boolean ok=false;
            @Override
            public void controlResized(ControlEvent e)
            {
                final Table table = (Table)e.widget;
                if(table.getShell().isVisible()) {
                    //Really dumb hack because LaunchConfigurationDialog
                    //keeps resizing the dialog with normal use of TableResizer
                    if(!ok) {
                        table.removeControlListener(this);
                        final Point p = table.getSize();
                        table.getShell().getDisplay().asyncExec(new Runnable() {
                            public void run()
                            {
                                if(!table.isDisposed()) {
                                    p.x -= 19;
                                    table.setSize(p);
                                    p.x += 19;
                                    table.setSize(p);
                                    table.addControlListener(controlAdapter);
                                }
                            }
                        });
                        ok = true;
                    }
                }
            }
        };
    }

    protected abstract IFilter createSettingsFilter();
    protected abstract NSISSettingsEditorPage createPage();
}