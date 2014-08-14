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

import org.w3c.dom.*;

public class NodeConvertibleNodeConverter extends AbstractNodeConverter<INodeConvertible>
{
    @SuppressWarnings("unchecked")
    @Override
    public INodeConvertible fromNode(Node node, Class<?> clasz)
    {
        if(INodeConvertible.class.isAssignableFrom(clasz))
        {
            Class<? extends INodeConvertible> clasz2 = (Class<? extends INodeConvertible>) clasz;
            INodeConvertible nodeConvertible = Common.createDefaultObject(clasz2);
            if(Common.stringsAreEqual(node.getNodeName(), nodeConvertible.getNodeName())) {
                nodeConvertible.fromNode(node);
                return nodeConvertible;
            }
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    public Node toNode(Document document, INodeConvertible object)
    {
        if(object != null) {
            return object.toNode(document);
        }
        throw new IllegalArgumentException(String.valueOf(object));
    }
}
