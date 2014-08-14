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

import org.w3c.dom.*;

public class PrimitivesNodeConverter extends AbstractNodeConverter<Object>
{
    private static final String VALUE_ATTR = "value"; //$NON-NLS-1$

    private Map<Class<? extends Object>,String> mClassNameMap = new HashMap<Class<? extends Object>,String>();

    @Override
    public void addNameClassMapping(String name, Class<? extends Object> clasz)
    {
        super.addNameClassMapping(name, clasz);
        mClassNameMap.put(clasz, name);
    }

    @Override
    public Object fromNode(Node node, Class<? extends Object> clasz)
    {
        NamedNodeMap attr = node.getAttributes();
        if(Integer.class.equals(clasz) || Integer.TYPE.equals(clasz)) {
            return Integer.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Long.class.equals(clasz) || Long.TYPE.equals(clasz)) {
            return Long.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Short.class.equals(clasz) || Short.TYPE.equals(clasz)) {
            return Short.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Byte.class.equals(clasz) || Byte.TYPE.equals(clasz)) {
            return Byte.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Double.class.equals(clasz) || Double.TYPE.equals(clasz)) {
            return Double.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Float.class.equals(clasz) || Float.TYPE.equals(clasz)) {
            return Float.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Boolean.class.equals(clasz) || Boolean.TYPE.equals(clasz)) {
            return Boolean.valueOf(XMLUtil.getStringValue(attr, VALUE_ATTR));
        }
        else if(Character.class.equals(clasz) || Character.TYPE.equals(clasz)) {
            return new Character(XMLUtil.getStringValue(attr, VALUE_ATTR).charAt(0));
        }
        throw new IllegalArgumentException(node.getNodeName());
    }

    public Node toNode(Document document, Object object)
    {
        String nodeName = mClassNameMap.get(object.getClass());
        if(nodeName != null) {
            Node node = document.createElement(nodeName);
            XMLUtil.addAttribute(document, node, VALUE_ATTR, String.valueOf(object));
            return node;
        }
        throw new IllegalArgumentException(object.getClass().getName());
    }
}
