/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.Map;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.gef.requests.CreationFactory;
import org.w3c.dom.Node;

public class InstallOptionsElementFactory implements CreationFactory
{
    private static Map<String, InstallOptionsElementFactory> cCachedFactories = new CaseInsensitiveMap<InstallOptionsElementFactory>();

    public static InstallOptionsElementFactory getFactory(String type)
    {
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(type);
        if(typeDef != null) {
            synchronized(typeDef) {
                InstallOptionsElementFactory factory = cCachedFactories.get(typeDef.getType());
                if(factory == null) {
                    factory = new InstallOptionsElementFactory(typeDef);
                    cCachedFactories.put(typeDef.getType(), factory);
                }
                return factory;
            }
        }
        else {
            return getFactory(InstallOptionsModel.TYPE_UNKNOWN);
        }
    }

    public static InstallOptionsElement createFromNode(Node node)
    {
        if(node.getNodeName().equals(InstallOptionsWidget.NODE_NAME)) {
            String nodeType = node.getAttributes().getNamedItem(InstallOptionsWidget.TYPE_ATTRIBUTE).getNodeValue();
            InstallOptionsElementFactory factory = getFactory(nodeType);
            if(factory != null) {
                InstallOptionsElement element = (InstallOptionsElement)factory.getNewObject();
                if(element != null) {
                    element.fromNode(node);
                    return element;
                }
            }
        }
        return null;
    }

    private InstallOptionsModelTypeDef mTypeDef;

    /**
     *
     */
    private InstallOptionsElementFactory(InstallOptionsModelTypeDef typeDef)
    {
        super();
        mTypeDef = typeDef;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject()
    {
        return getNewObject(null);
    }

    public InstallOptionsElement getNewObject(INISection section)
    {
        return mTypeDef.createModel(section);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return mTypeDef.getType();
    }
}
