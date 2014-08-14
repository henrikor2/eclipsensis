/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public abstract class AbstractTemplateReaderWriter<T extends ITemplate>
{
    protected static final String TEMPLATE_ROOT = "templates"; //$NON-NLS-1$
    protected static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
    protected static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
    protected static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
    protected static final String DESCRIPTION_NODE= "description"; //$NON-NLS-1$

    /**
     * Reads templates from a stream and adds them to the templates.
     *
     * @param stream the file to read templates from
     * @return the read templates
     * @throws IOException if reading from the stream fails
     */
    public Collection<T> import$(File file) throws IOException
    {
        try {
            Collection<T> templates= new HashSet<T>();

            Document document= XMLUtil.loadDocument(file);

            NodeList elements= document.getElementsByTagName(TEMPLATE_ELEMENT);

            int count= elements.getLength();
            for (int i= 0; i != count; i++) {
                Node node= elements.item(i);
                T template = createTemplate();
                template.fromNode(node);
                templates.add(template);
            }

            return templates;
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }
        catch (SAXException e) {
            Throwable t= e.getCause();
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            else {
                throw new IOException(t.getMessage());
            }
        }
    }

    /**
     * Saves the templates as XML, encoded as UTF-8 onto the given byte stream.
     *
     * @param templates the templates to save
     * @param file the file to write the templates to in XML
     * @throws IOException if writing the templates fails
     */
    public void export(Collection<T> templates, File file) throws IOException
    {
        try {
            Document document= XMLUtil.newDocument();

            Node root= document.createElement(TEMPLATE_ROOT);
            document.appendChild(root);

            for (Iterator<T> iter=templates.iterator(); iter.hasNext(); ) {
                T template= iter.next();
                Node node= template.toNode(document);
                root.appendChild(node);
            }

            XMLUtil.saveDocument(document, file);
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }
        catch (TransformerException e) {
            if (e.getException() instanceof IOException) {
                throw (IOException) e.getException();
            }
            else {
                throw new IOException(e.getMessage());
            }
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    protected abstract T createTemplate();
}
