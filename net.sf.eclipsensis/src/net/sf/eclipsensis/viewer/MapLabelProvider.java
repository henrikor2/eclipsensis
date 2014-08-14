/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.viewer;

import java.util.Map;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;


public class MapLabelProvider extends LabelProvider implements ITableLabelProvider
{
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        if(element instanceof Map.Entry<?,?>) {
            switch(columnIndex) {
                case 0:
                    return String.valueOf(((Map.Entry<?,?>)element).getKey());
                case 1:
                    return String.valueOf(((Map.Entry<?,?>)element).getValue());
            }
        }
        return null;
    }

    @Override
    public String getText(Object element)
    {
        if(element instanceof Map.Entry<?,?>) {
            return getColumnText(element, 1);
        }
        return super.getText(element);
    }
}