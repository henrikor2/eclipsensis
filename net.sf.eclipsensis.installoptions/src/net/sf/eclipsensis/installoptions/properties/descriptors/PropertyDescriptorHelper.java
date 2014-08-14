/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.installoptions.properties.descriptors;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * @author Sunil.Kamath
 *
 */
public class PropertyDescriptorHelper
{
    private PropertyDescriptorHelper()
    {
    }

    public static ICellEditorValidator getCellEditorValidator(PropertyDescriptor descriptor)
    {
        return (ICellEditorValidator)Common.getObjectFieldValue(descriptor, "validator", ICellEditorValidator.class); //$NON-NLS-1$
    }
}
