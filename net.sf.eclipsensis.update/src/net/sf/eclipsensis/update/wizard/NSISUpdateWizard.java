/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.wizard;

import net.sf.eclipsensis.config.NSISConfigWizard;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.*;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.update.scheduler.SchedulerConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.WizardShellImageChanger;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class NSISUpdateWizard extends NSISConfigWizard
{
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 400;
    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 200;
    private static final Image cShellImage = EclipseNSISUpdatePlugin.getShellImage();

    private IPageChangedListener mPageChangedListener = null;
    private boolean mError = false;
    private int mAction;
    private boolean mIgnorePreview;

    public NSISUpdateWizard()
    {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle(EclipseNSISUpdatePlugin.getResourceString("update.config.wizard.title")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(EclipseNSISUpdatePlugin.getImageDescriptor(EclipseNSISUpdatePlugin.getResourceString("wizard.title.image"))); //$NON-NLS-1$
        initDialogSettings();
    }

    private void initDialogSettings()
    {
        IDialogSettings pluginDialogSettings = EclipseNSISUpdatePlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        IDialogSettings dialogSettings = pluginDialogSettings.getSection(name);
        if(dialogSettings == null) {
            dialogSettings = pluginDialogSettings.addNewSection(name);
        }
        try {
            mAction = SchedulerConstants.validateAction(dialogSettings.getInt(IUpdatePreferenceConstants.UPDATE_ACTION));
        }
        catch (Exception e) {
            mAction = SchedulerConstants.DEFAULT_ACTION;
        }

        if(dialogSettings.get(IUpdatePreferenceConstants.IGNORE_PREVIEW) == null) {
            mIgnorePreview = SchedulerConstants.DEFAULT_IGNORE_PREVIEW;
        }
        else {
            mIgnorePreview = dialogSettings.getBoolean(IUpdatePreferenceConstants.IGNORE_PREVIEW);
        }

        setDialogSettings(dialogSettings);
    }

    @Override
    public void createPageControls(Composite pageContainer)
    {
        super.createPageControls(pageContainer);
        Object data = pageContainer.getLayoutData();
        if(data instanceof GridData) {
            GridData d = (GridData)data;
            d.widthHint = SIZING_WIZARD_WIDTH;
            d.heightHint = SIZING_WIZARD_HEIGHT;
        }
    }

    @Override
    public void addPages()
    {
        addPage(new NSISUpdateWizardPage(mAction, mIgnorePreview));
    }

    @Override
    public int getPageCount()
    {
        int n = super.getPageCount();
        if(n == 0) {
            addPages();
            n = super.getPageCount();
        }
        return n;
    }

    @Override
    public IWizardPage[] getPages()
    {
        IWizardPage[] wizpages = super.getPages();
        if(Common.isEmptyArray(wizpages)) {
            addPages();
            wizpages = super.getPages();
        }
        return wizpages;
    }

    @Override
    public IWizardPage getStartingPage()
    {
        IWizardPage wizardPage = super.getStartingPage();
        if(wizardPage == null) {
            addPages();
            wizardPage = super.getStartingPage();
        }
        return wizardPage;
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer)
    {
        if(getContainer() instanceof IPageChangeProvider) {
            if(mPageChangedListener != null) {
                ((IPageChangeProvider)getContainer()).removePageChangedListener(mPageChangedListener);
            }
        }
        super.setContainer(wizardContainer);
        if(getContainer() instanceof IPageChangeProvider) {
            if(mPageChangedListener == null) {
                mPageChangedListener = new WizardShellImageChanger(this, cShellImage);
            }
            ((IPageChangeProvider)getContainer()).addPageChangedListener(mPageChangedListener);
        }
    }

    @Override
    public boolean doPerformFinish()
    {
        if(getPageCount() > 0) {
            IDialogSettings settings = getDialogSettings();
            NSISUpdateWizardPage wizpage = (NSISUpdateWizardPage)getPage(NSISUpdateWizardPage.NAME);
            mAction = wizpage.getAction();
            mIgnorePreview = wizpage.isIgnorePreview();
            settings.put(IUpdatePreferenceConstants.UPDATE_ACTION,mAction);
            settings.put(IUpdatePreferenceConstants.IGNORE_PREVIEW,mIgnorePreview);
        }

        NSISUpdateJobSettings settings = new NSISUpdateJobSettings(false,mAction, mIgnorePreview);
        NextUpdateJobRunner jobRunner = new NextUpdateJobRunner();
        NSISCheckUpdateJob job = new NSISCheckUpdateJob(settings, jobRunner);
        jobRunner.run(job);
        if(mError) {
            return false;
        }
        return true;
    }

    private class NextUpdateJobRunner implements INSISUpdateJobRunner
    {
        private void setPageError(final IWizardPage page, final String error)
        {
            getShell().getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    if(error != null) {
                        if(page instanceof WizardPage) {
                            ((WizardPage)page).setMessage(error, IMessageProvider.ERROR);
                        }
                        else {
                            Common.openError(getShell(),error,EclipseNSISUpdatePlugin.getShellImage());
                        }
                    }
                    else {
                        if(page instanceof WizardPage) {
                            ((WizardPage)page).setMessage(null, IMessageProvider.ERROR);
                        }
                    }
                }
            });
        }

        public void run(final NSISUpdateJob job)
        {
            final IWizardPage page = getContainer().getCurrentPage();
            try {
                mError = false;
                getContainer().run(true, true, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                    {
                        IStatus status = job.run(monitor);
                        mError = (status.getSeverity() == IStatus.ERROR);
                        if(mError) {
                            setPageError(page, status.getMessage());
                        }
                        else {
                            setPageError(page, null);
                        }
                    }
                });
            }
            catch (Exception e) {
                mError = true;
                setPageError(page, e.getMessage());
                EclipseNSISUpdatePlugin.getDefault().log(e);
            }
        }
    }
}
