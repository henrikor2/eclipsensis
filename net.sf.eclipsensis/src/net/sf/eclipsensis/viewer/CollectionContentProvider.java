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

import java.util.Collection;


public class CollectionContentProvider extends EmptyContentProvider
{
    private boolean mReversed = false;

    public CollectionContentProvider()
    {
        super();
    }

    public CollectionContentProvider(boolean reversed)
    {
        super();
        mReversed = reversed;
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        if(inputElement != null && inputElement instanceof Collection<?>) {
            Object[] array = ((Collection<?>)inputElement).toArray();
            if(mReversed)
            {
                for(int i=0; i< array.length/2; i++)
                {
                    Object temp = array[i];
                    array[i] = array[array.length - i - 1];
                    array[array.length - i - 1] = temp;
                }
            }
            return array;
        }
        return super.getElements(inputElement);
    }
}