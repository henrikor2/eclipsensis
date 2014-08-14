/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.util;

import java.lang.reflect.*;
import java.util.*;

import org.w3c.dom.*;

public class NodeConversionUtility
{
    private NodeConversionUtility()
    {
    }

    @SuppressWarnings("unchecked")
    public static final <S,T> T readArrayNode(Node node, Class<T> clasz)
    {
        if (clasz.isArray())
        {
            Class<S> clasz2 = (Class<S>) clasz.getComponentType();
            INodeConverter<? super S> nodeConverter = NodeConverterFactory.INSTANCE.getNodeConverter(clasz2);
            Node[] children = XMLUtil.findChildren(node);
            T array = (T) Array.newInstance(clasz2, children.length);
            for (int i = 0; i < children.length; i++)
            {
                Array.set(array, i, readComponentNode(children[i], nodeConverter, clasz2));
            }
            return array;
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    @SuppressWarnings("unchecked")
    private static <T> T readComponentNode(Node node, INodeConverter<T> nodeConverter, Class<? extends T> clasz)
    {
        if (!AbstractNodeConvertible.NULL_NODE.equals(node.getNodeName()))
        {
            if (nodeConverter != null)
            {
                return nodeConverter.fromNode(node);
            }
            else
            {
                INodeConverter<T> nodeConverter2 = (INodeConverter<T>) NodeConverterFactory.INSTANCE.getNodeConverter(node.getNodeName());
                if (nodeConverter2 != null)
                {
                    return nodeConverter2.fromNode(node);
                }
                throw new IllegalArgumentException(node.getNodeName());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static final <T> void createArrayNode(Document document, Node parent, Object value)
    {
        if (value.getClass().isArray())
        {
            Class<T> clasz = (Class<T>) value.getClass().getComponentType();
            INodeConverter<? super T> nodeConverter = NodeConverterFactory.INSTANCE.getNodeConverter(clasz);
            if (!Common.isEmptyArray(value))
            {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++)
                {
                    T obj = (T) Array.get(value, i);
                    createComponentNode(document, parent, nodeConverter, obj);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException(value.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    public static final <T extends Collection<Object>> T readCollectionNode(Node node, Class<T> clasz)
    {
        T collection = null;
        if (!Modifier.isAbstract(clasz.getModifiers()))
        {
            collection = Common.createDefaultObject(clasz);
        }
        else if (List.class.equals(clasz))
        {
            collection = (T) new ArrayList<Object>();
        }
        else if (Set.class.equals(clasz))
        {
            collection = (T) new HashSet<Object>();
        }
        if (collection != null)
        {
            Node[] children = XMLUtil.findChildren(node);
            for (int i = 0; i < children.length; i++)
            {
                collection.add(readComponentNode(children[i], null, Object.class));
            }
        }
        return collection;
    }

    public static final void createCollectionNode(Document document, Node parent, Collection<?> collection)
    {
        if (!Common.isEmptyCollection(collection))
        {
            for (Iterator<?> iterator = collection.iterator(); iterator.hasNext();)
            {
                createComponentNode(document, parent, null, iterator.next());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void createComponentNode(Document document, Node parent, INodeConverter<? super T> nodeConverter, T obj)
    {
        if (obj != null)
        {
            if (nodeConverter != null)
            {
                parent.appendChild(nodeConverter.toNode(document, obj));
            }
            else
            {
                INodeConverter<? super T> nodeConverter2 = NodeConverterFactory.INSTANCE.getNodeConverter((Class<T>)obj.getClass());
                if (nodeConverter2 != null)
                {
                    parent.appendChild(nodeConverter2.toNode(document, obj));
                }
                else
                {
                    throw new IllegalArgumentException(obj.getClass().getName());
                }
            }
        }
        else
        {
            parent.appendChild(document.createElement(AbstractNodeConvertible.NULL_NODE));
        }
    }
}
