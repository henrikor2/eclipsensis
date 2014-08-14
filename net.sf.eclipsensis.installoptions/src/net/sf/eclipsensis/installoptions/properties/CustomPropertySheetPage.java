/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties;

import org.eclipse.ui.views.properties.*;

public class CustomPropertySheetPage extends PropertySheetPage
{
    private static final PropertySheetSorter cNonSorter = new PropertySheetSorter() {
        @Override
        public int compare(IPropertySheetEntry entryA, IPropertySheetEntry entryB)
        {
            return -1;
        }

        @Override
        public int compareCategories(String categoryA, String categoryB)
        {
            return -1;
        }
    };

    public CustomPropertySheetPage()
    {
        super();
        super.setSorter(cNonSorter);
    }

    @Override
    protected void setSorter(PropertySheetSorter sorter)
    {
    }
}
