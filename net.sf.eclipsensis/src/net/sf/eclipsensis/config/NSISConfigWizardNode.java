/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.config;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPluginContribution;

public class NSISConfigWizardNode implements IWizardNode, IPluginContribution
{
    private NSISConfigSelectionPage mParentPage;
    private NSISConfigWizardDescriptor mDescriptor;
    protected NSISConfigWizard mWizard;

    public NSISConfigWizardNode(NSISConfigSelectionPage parentPage, NSISConfigWizardDescriptor descriptor)
    {
        super();
        mParentPage = parentPage;
        mDescriptor = descriptor;
    }

    public void dispose()
    {
    }

    public Point getExtent()
    {
        return new Point(-1,-1);
    }

    public IWizard getWizard()
    {
        if (mWizard != null) {
            return mWizard; // we've already created it
        }

        final NSISConfigWizard[] wizard = new NSISConfigWizard[1];
        final IStatus statuses[] = new IStatus[1];

        Shell shell = mParentPage.getShell();
        BusyIndicator.showWhile(shell.getDisplay(),
                new Runnable() {
                    public void run() {
                        SafeRunner.run(new SafeRunnable() {
                            @Override
                            public void handleException(Throwable e) {
                                statuses[0] = new Status(IStatus.ERROR, getPluginId(), IStatus.OK,
                                        e.getMessage() == null ? "" : e.getMessage(), e); //$NON-NLS-1$,
                            }

                            public void run() {
                                try {
                                    wizard[0] = mDescriptor.createWizard();
                                } catch (CoreException e) {
                                    statuses[0] = e.getStatus();
                                }
                            }
                        });
                    }
                });

        if (statuses[0] != null) {
            mParentPage.setMessage(statuses[0].getMessage(), IMessageProvider.ERROR);
            Common.openError(shell, statuses[0].getMessage(), EclipseNSISPlugin.getShellImage());
            return null;
        }

        mWizard = wizard[0];
        return mWizard;
    }

    public boolean isContentCreated()
    {
        return mWizard != null;
    }

    public NSISConfigWizardDescriptor getDescriptor()
    {
        return mDescriptor;
    }

    public String getLocalId()
    {
        IPluginContribution contribution = (IPluginContribution) mDescriptor.getAdapter(IPluginContribution.class);
        if (contribution != null) {
            return contribution.getLocalId();
        }
        return mDescriptor.getLocalId();
    }

    public String getPluginId()
    {
        IPluginContribution contribution = (IPluginContribution) mDescriptor.getAdapter(IPluginContribution.class);
        if (contribution != null) {
            return contribution.getPluginId();
        }
        return mDescriptor.getPluginId();
    }
}
