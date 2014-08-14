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

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class NSISConfigWizardDescriptor implements IAdaptable, IPluginContribution
{
    private static final String WIZARD_ID = "id"; //$NON-NLS-1$
    private static final String WIZARD_NAME = "name"; //$NON-NLS-1$
    private static final String WIZARD_CLASS = "class"; //$NON-NLS-1$
    private static final String WIZARD_ICON = "icon"; //$NON-NLS-1$
    private static final String WIZARD_CAN_FINISH_EARLY = "canFinishEarly"; //$NON-NLS-1$
    private static final String WIZARD_DESCRIPTION = "description"; //$NON-NLS-1$

    private IConfigurationElement mConfigurationElement;
    private String mId;
    private ImageDescriptor mIcon;

    public NSISConfigWizardDescriptor(IConfigurationElement configurationElement)
    {
        mConfigurationElement = configurationElement;
        mId = configurationElement.getAttribute(WIZARD_ID);
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        if (adapter == IPluginContribution.class) {
            return this;
        }
        else if (adapter == IConfigurationElement.class) {
            return mConfigurationElement;
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public String getLocalId()
    {
        return getId();
    }

    public String getPluginId()
    {
        return (mConfigurationElement != null?mConfigurationElement.getContributor().getName():null);
    }

    public String getId()
    {
        return mId;
    }

    public String getName()
    {
        return mConfigurationElement.getAttribute(WIZARD_NAME);
    }

    public NSISConfigWizard createWizard() throws CoreException
    {
        return (NSISConfigWizard)mConfigurationElement.createExecutableExtension(WIZARD_CLASS);
    }

    public IConfigurationElement getConfigurationElement()
    {
        return mConfigurationElement;
    }

    public String getDescription()
    {
        IConfigurationElement[] children = mConfigurationElement.getChildren(WIZARD_DESCRIPTION);
        if (children.length > 0) {
            return children[0].getValue();
        }
        return "";//$NON-NLS-1$
    }

    public boolean canFinishEarly()
    {
        return Boolean.valueOf(mConfigurationElement.getAttribute(WIZARD_CAN_FINISH_EARLY)).booleanValue();
    }

    public ImageDescriptor getIcon()
    {
        if (mIcon == null) {
            String iconName = mConfigurationElement.getAttribute(WIZARD_ICON);
            if (iconName == null) {
                return null;
            }
            mIcon = AbstractUIPlugin.imageDescriptorFromPlugin(mConfigurationElement.getContributor().getName(), iconName);
        }
        return mIcon;
    }
}
