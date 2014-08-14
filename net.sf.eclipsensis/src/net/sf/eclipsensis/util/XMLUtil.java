/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLUtil
{
    private static TransformerFactory cTransformerFactory = TransformerFactory.newInstance();
    private static DocumentBuilderFactory cDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

    private XMLUtil()
    {
    }

    public static void addAttribute(Document document, Node node, String name, String value)
    {
        if (value != null)
        {
            Attr attribute = document.createAttribute(name);
            attribute.setValue(value);
            node.getAttributes().setNamedItem(attribute);
        }
    }

    public static void removeValue(NamedNodeMap values, String name)
    {
        if (values.getNamedItem(name) != null)
        {
            values.removeNamedItem(name);
        }
    }

    public static String getStringValue(NamedNodeMap values, String name)
    {
        return getStringValue(values, name, null);
    }

    public static String getStringValue(NamedNodeMap values, String name, String defaultValue)
    {
        Node node = values.getNamedItem(name);
        return node == null ? defaultValue : node.getNodeValue();
    }

    public static boolean getBooleanValue(NamedNodeMap values, String name)
    {
        return getBooleanValue(values, name, false);
    }

    public static boolean getBooleanValue(NamedNodeMap values, String name, boolean defaultValue)
    {
        Node node = values.getNamedItem(name);
        return node == null ? defaultValue : Boolean.valueOf(node.getNodeValue()).booleanValue();
    }

    public static String readTextNode(Node item)
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        NodeList children2 = item.getChildNodes();
        if (children2 != null)
        {
            for (int k = 0; k < children2.getLength(); k++)
            {
                Node item2 = children2.item(k);
                if (item2 != null)
                {
                    buf.append(item2.getNodeValue());
                }
            }
        }
        return buf.toString();
    }

    public static int getIntValue(NamedNodeMap values, String name)
    {
        return getIntValue(values, name, 0);
    }

    public static int getIntValue(NamedNodeMap values, String name, int defaultValue)
    {
        Node node = values.getNamedItem(name);
        try
        {
            return node == null ? defaultValue : Integer.parseInt(node.getNodeValue());
        }
        catch (Exception e)
        {
            EclipseNSISPlugin.getDefault().log(e);
            return defaultValue;
        }
    }

    public static void saveDocument(Document doc, File file) throws TransformerException, IOException
    {
        OutputStream os = null;
        try
        {
            os = new BufferedOutputStream(new FileOutputStream(file));
            saveDocument(doc, os);
        }
        finally
        {
            IOUtility.closeIO(os);
        }
    }

    /**
     * @param doc
     * @param os
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static void saveDocument(Document doc, OutputStream os) throws TransformerConfigurationException,
            TransformerException
    {
        saveDocument(doc, os, true);
    }

    /**
     * @param doc
     * @param os
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public static void saveDocument(Document doc, OutputStream os, boolean indent)
            throws TransformerConfigurationException, TransformerException
    {
        StreamResult result = new StreamResult(os);
        Transformer transformer = cTransformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
        if (indent)
        {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        }
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
        DOMSource source = new DOMSource(doc);

        transformer.transform(source, result);
    }

    public static Document loadDocument(File file) throws IOException, SAXException, ParserConfigurationException
    {
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(new FileInputStream(file));
            return loadDocument(is);
        }
        finally
        {
            IOUtility.closeIO(is);
        }
    }

    public static Document loadDocument(InputStream is) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilder builder = cDocumentBuilderFactory.newDocumentBuilder();
        return builder.parse(new InputSource(is));
    }

    public static Document newDocument() throws ParserConfigurationException
    {
        return cDocumentBuilderFactory.newDocumentBuilder().newDocument();
    }

    public static Node findFirstChild(Node node)
    {
        return findFirstChild(node, null);
    }

    public static Node findFirstChild(Node node, String name)
    {
        NodeList childNodes = node.getChildNodes();
        int count = childNodes.getLength();
        for (int i = 0; i < count; i++)
        {
            Node child = childNodes.item(i);
            if (child instanceof Element && (name == null || child.getNodeName().equals(name)))
            {
                return child;
            }
        }
        return null;
    }

    public static Node[] findChildren(Node node)
    {
        return findChildren(node, null);
    }

    public static Node[] findChildren(Node node, String name)
    {
        List<Node> list = new ArrayList<Node>();
        NodeList childNodes = node.getChildNodes();
        int count = childNodes.getLength();
        for (int i = 0; i < count; i++)
        {
            Node child = childNodes.item(i);
            if (child instanceof Element && (name == null || child.getNodeName().equals(name)))
            {
                list.add(child);
            }
        }
        return list.toArray(new Node[list.size()]);
    }
}
