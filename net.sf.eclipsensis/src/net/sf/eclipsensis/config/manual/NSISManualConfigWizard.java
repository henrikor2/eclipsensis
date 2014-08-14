/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.config.manual;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.config.NSISConfigWizard;
import net.sf.eclipsensis.wizard.WizardShellImageChanger;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class NSISManualConfigWizard extends NSISConfigWizard
{
    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 400;
    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 200;
    private static final Image cShellImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("manual.config.wizard.icon")); //$NON-NLS-1$
    private IPageChangedListener mPageChangedListener;

    public NSISManualConfigWizard()
    {
        super();
        setNeedsProgressMonitor(false);
        setWindowTitle(EclipseNSISPlugin.getResourceString(EclipseNSISPlugin.getResourceString("manual.config.wizard.title"))); //$NON-NLS-1$
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
    protected boolean doPerformFinish()
    {
        return ((NSISManualConfigWizardPage)getPages()[0]).performFinish();
    }

    @Override
    public final void addPages()
    {
        addPage(new NSISManualConfigWizardPage());
    }
}
