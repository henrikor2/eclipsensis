/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.Serializable;
import java.util.*;

public final class MRUSet<E> extends AbstractSet<E> implements Serializable
{
    private static final long serialVersionUID = -1L;
    private MRUMap<E,E> mBackingMap;

    public MRUSet(int maxSize)
    {
        mBackingMap = new MRUMap<E,E>(maxSize);
    }

    public MRUSet(int maxSize, Set<E> set)
    {
        this(maxSize);
        addAll(set);
    }

    @Override
    public boolean add(E val)
    {
        E old = mBackingMap.put(val, val);
        return Common.objectsAreEqual(val, old);
    }

    @Override
    public Iterator<E> iterator()
    {
        return mBackingMap.values().iterator();
    }

    @Override
    public int size()
    {
        return mBackingMap.size();
    }
}