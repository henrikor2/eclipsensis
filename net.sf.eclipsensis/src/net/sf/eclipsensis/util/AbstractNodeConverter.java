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

import java.util.*;

import org.w3c.dom.Node;

public abstract class AbstractNodeConverter<T> implements INodeConverter<T>
{
    private Map<String,Class<?>> mNameClassMap = new HashMap<String,Class<?>>();

    public Map<String,Class<?>> getNameClassMappings()
    {
        return Collections.unmodifiableMap(mNameClassMap);
    }

    public void addNameClassMapping(String name, Class<?> clasz)
    {
        mNameClassMap.put(name, clasz);
    }

    public T fromNode(Node node)
    {
        Class<?> clasz = mNameClassMap.get(node.getNodeName());
        if(clasz != null) {
            return fromNode(node, clasz);
        }
        throw new IllegalArgumentException(node.getNodeName());
    }

    public abstract T fromNode(Node node, Class<?> clasz);
}
