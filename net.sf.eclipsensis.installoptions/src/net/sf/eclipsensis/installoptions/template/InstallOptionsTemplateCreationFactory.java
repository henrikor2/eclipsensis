/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import org.eclipse.gef.requests.CreationFactory;

public class InstallOptionsTemplateCreationFactory implements CreationFactory
{
    public static final Object TYPE = IInstallOptionsTemplate.class;
    private IInstallOptionsTemplate mTemplate;

    public InstallOptionsTemplateCreationFactory(IInstallOptionsTemplate template)
    {
        super();
        mTemplate = template;
    }

    public Object[] getNewObjects()
    {
        return mTemplate.getWidgets();
    }

    public Object getNewObject()
    {
        return getNewObjects();
    }

    public Object getObjectType()
    {
        return TYPE;
    }
}
