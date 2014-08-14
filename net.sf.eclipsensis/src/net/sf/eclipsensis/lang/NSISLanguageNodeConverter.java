/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.lang;

import net.sf.eclipsensis.util.*;

import org.w3c.dom.*;

public class NSISLanguageNodeConverter extends AbstractNodeConverter<NSISLanguage>
{
    private static final String NAME_ATTR = "name"; //$NON-NLS-1$
    private static final String LANGUAGE_NODE = "language"; //$NON-NLS-1$

    @Override
    public NSISLanguage fromNode(Node node, Class<?> clasz)
    {
        if (NSISLanguage.class.isAssignableFrom(clasz)) {
            if (LANGUAGE_NODE.equals(node.getNodeName())) {
                NamedNodeMap attributes = node.getAttributes();
                String langName = XMLUtil.getStringValue(attributes, NAME_ATTR);
                return NSISLanguageManager.getInstance().getLanguage(langName);
            }
        }
        throw new IllegalArgumentException(clasz.getName());
    }

    public Node toNode(Document document, NSISLanguage object)
    {
        if(object != null) {
            Node node = document.createElement(LANGUAGE_NODE);
            XMLUtil.addAttribute(document, node, NAME_ATTR, (object).getName());
            return node;
        }
        throw new IllegalArgumentException(String.valueOf(object));
    }
}
