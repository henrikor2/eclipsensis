/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.labelproviders;

import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.LabelProvider;

public class ListLabelProvider extends LabelProvider
{
    @Override
    public String getText(Object element)
    {
        if(element instanceof List<?>) {
            return Common.flatten(((List<?>)element).toArray(),IInstallOptionsConstants.LIST_SEPARATOR);
        }
        else {
            return super.getText(element);
        }
    }
}