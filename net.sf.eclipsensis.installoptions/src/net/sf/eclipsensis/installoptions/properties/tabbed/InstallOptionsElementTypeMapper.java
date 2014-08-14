/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

public class InstallOptionsElementTypeMapper implements ITypeMapper
{
    public Class<?> mapType(Object object)
    {
        Class<?> type = object.getClass();
        if (object instanceof EditPart) {
            type = ((EditPart) object).getModel().getClass();
        }
        return type;
    }
}
