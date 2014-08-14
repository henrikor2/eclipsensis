/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.template.NSISWizardTemplate;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;

public abstract class NSISWizard extends Wizard implements IAdaptable, INewWizard, INSISWizardConstants
{
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 500;
    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 500;

    private NSISWizardSettings mSettings = null;
    private List<INSISWizardSettingsListener> mSettingsListeners = new ArrayList<INSISWizardSettingsListener>();
    private IPageChangeProvider mPageChangeProvider;
    private AbstractNSISWizardPage mCurrentPage = null;
    private boolean mForcedCancel = false;

    /**
     * Constructor for NSISWizard.
     */
    public NSISWizard()
    {
        super();
        setTitleBarColor(ColorManager.WHITE);
    }

    void initSettings()
    {
        setSettings(new NSISWizardSettings());
    }

    void setCurrentPage(AbstractNSISWizardPage currentPage)
    {
        mCurrentPage = currentPage;
        if(mPageChangeProvider instanceof PageChangeProvider) {
            ((PageChangeProvider)mPageChangeProvider).firePageChanged();
        }
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if(IPageChangeProvider.class.equals(adapter)) {
            if(mPageChangeProvider == null) {
                mPageChangeProvider = new PageChangeProvider();
            }
            return mPageChangeProvider;
        }
        return null;
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer)
    {
        if(getContainer() == mPageChangeProvider) {
            mPageChangeProvider = null;
        }
        super.setContainer(wizardContainer);
        if(getContainer() instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider)getContainer();
            if(mPageChangeProvider instanceof PageChangeProvider) {
                List<IPageChangedListener> list = ((PageChangeProvider)mPageChangeProvider).getListeners();
                IPageChangedListener[] listeners = list.toArray(new IPageChangedListener[list.size()]);
                for (int i = 0; i < listeners.length; i++) {
                    pageChangeProvider.addPageChangedListener(listeners[i]);
                    list.remove(listeners[i]);
                }
                mPageChangeProvider = null;
            }
            mPageChangeProvider = pageChangeProvider;
        }
    }

    /**
     * Adding the page to the wizard.
     */
    @Override
    public final void addPages()
    {
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            initSettings();

            addStartPage();
            addPage(new NSISWizardGeneralPage());
            addPage(new NSISWizardAttributesPage());
            addPage(new NSISWizardPresentationPage());
            addPage(new NSISWizardContentsPage());
            addPage(new NSISWizardCompletionPage());
        }
        else {
            String error = EclipseNSISPlugin.getFormattedString("wizard.unconfigured.error", new Object[]{getWindowTitle()}); //$NON-NLS-1$
            Common.openError(getShell(), error, EclipseNSISPlugin.getShellImage());
            mForcedCancel = true;
            EclipseNSISPlugin.getDefault().getJobScheduler().scheduleUIJob("", new IJobStatusRunnable() { //$NON-NLS-1$
                public IStatus run(IProgressMonitor monitor)
                {
                    IWizardContainer container = getContainer();
                    if (container != null) {
                        container.getShell().close();
                    }
                    return Status.OK_STATUS;
                }
            });
        }
    }

    /**
     * @return Returns the settings.
     */
    public NSISWizardSettings getSettings()
    {
        return mSettings;
    }

    protected void setSettings(NSISWizardSettings settings)
    {
        NSISWizardSettings oldSettings = mSettings;
        if(oldSettings != null) {
            oldSettings.setWizard(null);
        }
        mSettings = settings;
        mSettings.setWizard(this);
        INSISWizardSettingsListener[] listeners = mSettingsListeners.toArray(new INSISWizardSettingsListener[mSettingsListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].settingsChanged(oldSettings, mSettings);
        }
        IWizardPage[] pages = getPages();
        if(!Common.isEmptyArray(pages)) {
            for (int i = 0; i < pages.length; i++) {
                ((AbstractNSISWizardPage)pages[i]).validatePage(VALIDATE_ALL);
            }
            getContainer().updateButtons();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page)
    {
        IWizardPage nextPage = super.getNextPage(page);
        if(mSettings.getInstallerType() == INSTALLER_TYPE_SILENT && nextPage != null &&
                nextPage.getName().equals(NSISWizardPresentationPage.NAME)) {
            nextPage = super.getNextPage(nextPage);
        }
        return nextPage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
     */
    @Override
    public IWizardPage getPreviousPage(IWizardPage page)
    {
        IWizardPage prevPage = super.getPreviousPage(page);
        if(mSettings.getInstallerType() == INSTALLER_TYPE_SILENT && prevPage != null &&
                prevPage.getName().equals(NSISWizardPresentationPage.NAME)) {
            prevPage = super.getNextPage(prevPage);
        }
        return prevPage;
    }

    @Override
    public void createPageControls(Composite pageContainer)
    {
        super.createPageControls(pageContainer);
        Object data = pageContainer.getLayoutData();
        if(data instanceof GridData) {
            GridData d = (GridData)data;
            d.widthHint = SIZING_WIZARD_WIDTH;
            d.heightHint = SIZING_WIZARD_HEIGHT + //Account for MultiUser group
            (INSISVersions.VERSION_2_35.compareTo(NSISPreferences.getInstance().getNSISVersion()) <= 0?60:0);
        }
    }

    public void addSettingsListener(INSISWizardSettingsListener listener)
    {
        mSettingsListeners.add(listener);
    }

    public void removeSettingsListener(INSISWizardSettingsListener listener)
    {
        mSettingsListeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        mForcedCancel = false;
    }

    protected abstract void addStartPage();

    protected NSISWizardTemplate mTemplate = null;

    /**
     * @return Returns the template.
     */
    protected NSISWizardTemplate getTemplate()
    {
        return mTemplate;
    }

    /**
     * @param template The template to set.
     */
    protected void setTemplate(NSISWizardTemplate template)
    {
        mTemplate = template;
    }

    protected boolean isForcedCancel()
    {
        return mForcedCancel;
    }

    public abstract String getHelpContextId();

    private class PageChangeProvider implements IPageChangeProvider
    {
        private List<IPageChangedListener> mListeners = new ArrayList<IPageChangedListener>();

        public void addPageChangedListener(IPageChangedListener listener)
        {
            if(!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }

        public List<IPageChangedListener> getListeners()
        {
            return mListeners;
        }

        public Object getSelectedPage()
        {
            return mCurrentPage;
        }

        public void removePageChangedListener(IPageChangedListener listener)
        {
            if(!mListeners.contains(listener)) {
                mListeners.add(listener);
            }
        }

        public void firePageChanged()
        {
            PageChangedEvent pageChangedEvent = new PageChangedEvent(this, mCurrentPage);
            IPageChangedListener[] listeners = mListeners.toArray(new IPageChangedListener[mListeners.size()]);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].pageChanged(pageChangedEvent);
            }
        }
    }
}