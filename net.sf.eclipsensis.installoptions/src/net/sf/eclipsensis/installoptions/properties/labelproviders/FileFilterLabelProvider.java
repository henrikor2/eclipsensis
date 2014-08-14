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

import net.sf.eclipsensis.installoptions.util.FileFilter;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

public class FileFilterLabelProvider extends LabelProvider implements ITableLabelProvider
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
        if(element instanceof FileFilter) {
            switch(columnIndex) {
                case 0:
                    return ((FileFilter)element).getDescription();
                case 1:
                    return ((FileFilter)element).getPatternString();
            }
        }
        return getText(element);
    }
}