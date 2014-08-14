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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.*;

public abstract class NSISConfigWizard extends Wizard
{
    private boolean mValidateNSISConfig = true;

    public NSISConfigWizard()
    {
        super();
        setTitleBarColor(ColorManager.WHITE);
    }

    public boolean isValidateNSISConfig()
    {
        return mValidateNSISConfig;
    }

    public void setValidateNSISConfig(boolean validateNSISConfig)
    {
        mValidateNSISConfig = validateNSISConfig;
    }

    @Override
    public final boolean performFinish()
    {
        if(doPerformFinish()) {
            if(isValidateNSISConfig()) {
                if(!EclipseNSISPlugin.getDefault().isConfigured()) {
                    ((WizardPage)getContainer().getCurrentPage()).setMessage(EclipseNSISPlugin.getResourceString("config.failure.message"), IMessageProvider.ERROR); //$NON-NLS-1$
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected abstract boolean doPerformFinish();
}
