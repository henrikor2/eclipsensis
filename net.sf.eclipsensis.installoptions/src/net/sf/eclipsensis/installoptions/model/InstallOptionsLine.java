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

import java.util.Collection;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;

public abstract class InstallOptionsLine extends InstallOptionsWidget
{
    /**
     *
     */
    private static final long serialVersionUID = -1423121089228222673L;

    public InstallOptionsLine(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("flags"); //$NON-NLS-1$
        skippedProperties.add("horizontal"); //$NON-NLS-1$
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new WidgetPropertySectionCreator(this) { };
    }

    public abstract boolean isHorizontal();
}
