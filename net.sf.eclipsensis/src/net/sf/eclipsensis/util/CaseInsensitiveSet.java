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

/**
 * An implementation of the Set interface that supports storing of
 * String values in case-insensitive form. The case in which the key is stored
 * is that in which it was originally added to the Set. The operation will
 * be ignored if the value is added subsequently in a different case.
 * <p>
 * The value may be <code>null</code>.
 *
 * @param <T> The types of values being stored in the Set.
 */
public class CaseInsensitiveSet extends AbstractSet<String> implements Serializable
{
    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * The mapping of the uppercase value to the original value.
     */
    private Map<String,String> mValueMap;

    /**
     * Constructs a new, empty set with default initial capacity.
     */
    public CaseInsensitiveSet()
    {
        mValueMap = new LinkedHashMap<String,String>();
    }

    /**
     * Constructs a new, empty set with the specified initial capacity.
     *
     * @param initialCapacity The initial capacity of the hash table.
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public CaseInsensitiveSet(int initialCapacity)
    {
        if (initialCapacity < 0)
        {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity); //$NON-NLS-1$
        }
        mValueMap = new LinkedHashMap<String,String>(initialCapacity);
    }

    /**
     * Constructs a new set containing the elements in the specified collection.
     *
     * @param c The collection whose elements are to be placed into this set.
     */
    public CaseInsensitiveSet(Collection<String> c)
    {
        this(c.size());
        for(Iterator<String> iter=c.iterator(); iter.hasNext(); ) {
            String value = iter.next();
            add(value);
        }
    }

    /**
     * Convert the value to uppercase.
     * @param value The original value.
     * @return The converted value.
     */
    private String toUpperCase(String value)
    {
        return (value !=null?value.toUpperCase():value);
    }

    @Override
    public int size()
    {
        return mValueMap.size();
    }

    @Override
    public boolean contains(Object value)
    {
        return (value != null && !(value instanceof String)?false:mValueMap.containsKey(toUpperCase((String)value)));
    }

    @Override
    public boolean add(String value)
    {
        String uppercaseValue = toUpperCase(value);
        if(!mValueMap.containsKey(uppercaseValue)) {
            mValueMap.put(uppercaseValue,value);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean remove(Object value)
    {
        if(value != null && !(value instanceof String))
        {
            return false;
        }
        String uppercaseValue = toUpperCase((String)value);
        if(mValueMap.containsKey(uppercaseValue)) {
            mValueMap.remove(uppercaseValue);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public Iterator<String> iterator()
    {
        return mValueMap.values().iterator();
    }
}
