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
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.wizard.WizardShellImageChanger;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class NSISConfigSelectionWizard extends Wizard
{
    private static final Image cShellImage = EclipseNSISPlugin.getShellImage();

    private IPageChangedListener mPageChangedListener;

    private NSISConfigSelectionPage mMainPage;
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 400;
    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 200;

    public NSISConfigSelectionWizard()
    {
        super();
        setTitleBarColor(ColorManager.WHITE);
        setWindowTitle(EclipseNSISPlugin.getResourceString("config.selection.wizard.title")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
        setForcePreviousAndNextButtons(true);
        mPageChangedListener = new WizardShellImageChanger(this, cShellImage);
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
    public void setContainer(IWizardContainer wizardContainer)
    {
        if(getContainer() instanceof IPageChangeProvider) {
            ((IPageChangeProvider)getContainer()).removePageChangedListener(mPageChangedListener);
        }
        super.setContainer(wizardContainer);
        if(getContainer() instanceof IPageChangeProvider) {
            ((IPageChangeProvider)getContainer()).addPageChangedListener(mPageChangedListener);
        }
    }

    @Override
    public void addPages()
    {
        mMainPage = new NSISConfigSelectionPage();
        addPage(mMainPage);
    }

    @Override
    public boolean performFinish()
    {
        if (getContainer().getCurrentPage() == mMainPage) {
            if (mMainPage.canFinishEarly()) {
                IWizard wizard = mMainPage.getSelectedNode().getWizard();
                wizard.setContainer(getContainer());
                return wizard.performFinish();
            }
        }
        return true;
    }

    @Override
    public boolean canFinish()
    {
        if (getContainer().getCurrentPage() == mMainPage) {
            if (mMainPage.canFinishEarly()) {
                return true;
            }
        }
        return super.canFinish();
    }
}
