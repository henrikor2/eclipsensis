/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.wizard;

import java.text.MessageFormat;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class NSISUpdateWizardPage extends WizardPage
{
    public static final String NAME = "nsisUpdateWizardPage"; //$NON-NLS-1$
    private static final String LINK_TEXT;

    private int mAction = SchedulerConstants.DEFAULT_ACTION;
    private boolean mIgnorePreview = SchedulerConstants.DEFAULT_IGNORE_PREVIEW;

    static
    {
        LINK_TEXT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("wizard.proxy.link.text")).format(new String[] { "org.eclipse.ui.net.NetPreferences" }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public NSISUpdateWizardPage(int action, boolean ignorePreview)
    {
        super(NAME);
        mAction = action;
        mIgnorePreview = ignorePreview;
        setTitle(EclipseNSISUpdatePlugin.getResourceString("wizard.page.title")); //$NON-NLS-1$
        setDescription(EclipseNSISUpdatePlugin.getResourceString("wizard.page.description")); //$NON-NLS-1$
    }

    public int getAction()
    {
        return mAction;
    }

    public boolean isIgnorePreview()
    {
        return mIgnorePreview;
    }

    public void createControl(Composite parent)
    {
        Composite parent2 = new Composite(parent, SWT.NONE);
        parent2.setLayout(new GridLayout(1, false));

        Group group1 = createActionGroup(parent2);
        Group group2 = createOptionsGroup(parent2);
        Link link = new Link(parent2, SWT.WRAP);
        link.setText(LINK_TEXT);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (e.text != null)
                {
                    PreferencesUtil.createPreferenceDialogOn(getShell(), e.text, null, null).open();
                }
            }
        });
        Point size1 = group1.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point size2 = group2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        gridData.widthHint = Math.max(size1.x, size2.x);
        link.setLayoutData(gridData);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent2,
                EclipseNSISUpdatePlugin.PLUGIN_CONTEXT_PREFIX + "nsis_update_wizard_context"); //$NON-NLS-1$
        setControl(parent2);
    }

    private Group createActionGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));

        final Button notify = new Button(group, SWT.RADIO);
        notify.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.notify.label")); //$NON-NLS-1$
        notify.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        notify.setSelection(mAction == SchedulerConstants.UPDATE_NOTIFY);
        notify.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (notify.getSelection())
                {
                    mAction = SchedulerConstants.UPDATE_NOTIFY;
                }
            }
        });

        final Button download = new Button(group, SWT.RADIO);
        download.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.download.label")); //$NON-NLS-1$
        download.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        download.setSelection(mAction == SchedulerConstants.UPDATE_DOWNLOAD);
        download.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (download.getSelection())
                {
                    mAction = SchedulerConstants.UPDATE_DOWNLOAD;
                }
            }
        });

        final Button install = new Button(group, SWT.RADIO);
        install.setText(EclipseNSISUpdatePlugin.getResourceString("update.action.install.label")); //$NON-NLS-1$
        install.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        install.setSelection(mAction == SchedulerConstants.UPDATE_INSTALL);
        install.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (install.getSelection())
                {
                    mAction = SchedulerConstants.UPDATE_INSTALL;
                }
            }
        });

        return group;
    }

    private Group createOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.options.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));

        final Button ignorePreview = new Button(group, SWT.CHECK);
        ignorePreview.setText(EclipseNSISUpdatePlugin.getResourceString("ignore.preview.label")); //$NON-NLS-1$
        ignorePreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ignorePreview.setSelection(mIgnorePreview);
        ignorePreview.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mIgnorePreview = ignorePreview.getSelection();
            }
        });
        return group;
    }
}
