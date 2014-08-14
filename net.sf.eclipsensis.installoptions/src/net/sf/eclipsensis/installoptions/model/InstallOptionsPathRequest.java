/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.validators.PathStateValidator;

import org.eclipse.ui.views.properties.*;

public abstract class InstallOptionsPathRequest extends InstallOptionsEditableElement
{
    /**
     *
     */
    private static final long serialVersionUID = 1868308753895206491L;

    protected InstallOptionsPathRequest(INISection section)
    {
        super(section);
    }

    @Override
    protected int getDefaultMaxLen()
    {
        return 260;
    }

    @Override
    protected Position getDefaultPosition()
    {
        return new Position(0,0,122,13);
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            String propertyName = InstallOptionsPlugin.getResourceString("state.property.name"); //$NON-NLS-1$;
            TextPropertyDescriptor descriptor = new TextPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE, propertyName);
            descriptor.setValidator(new PathStateValidator(propertyName));
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }
}
