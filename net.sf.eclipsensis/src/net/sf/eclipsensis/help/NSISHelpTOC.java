/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.Serializable;
import java.util.*;

import net.sf.eclipsensis.util.CaseInsensitiveMap;

public class NSISHelpTOC implements Serializable
{
    private static final long serialVersionUID = -7260019864838868104L;

    private Map<String, NSISHelpTOCNode> mNodeMap = null;
    private List<NSISHelpTOCNode> mChildren = null;

    NSISHelpTOC()
    {
    }

    NSISHelpTOCNode createNode(String name, String url)
    {
        return new NSISHelpTOCNode(name, url);
    }

    public List<NSISHelpTOCNode> getChildren()
    {
        return (mChildren != null?Collections.unmodifiableList(mChildren):null);
    }

    private void mapNode(NSISHelpTOCNode node)
    {
        if(mNodeMap == null) {
            mNodeMap = new CaseInsensitiveMap<NSISHelpTOCNode>();
        }
        String url = node.getURL();
        mNodeMap.put(url,node);
        mNodeMap.put(node.getName(),node);

        int n = url.lastIndexOf('#');
        if(n > 0) {
            String suffix = url.substring(n+1);
            url = url.substring(0,n);
            n = url.lastIndexOf('.');
            int m = url.lastIndexOf('/');
            String ext = ""; //$NON-NLS-1$
            if(n > m) {
                ext = url.substring(n);
                url = url.substring(0,n);
            }
            if(url.length() > suffix.length() && url.regionMatches(true,url.length()-suffix.length(),suffix,0,suffix.length())) {
                url = url+ext;
                if(!mNodeMap.containsKey(url)) {
                    mNodeMap.put(url, node);
                }
            }
        }
    }

    public NSISHelpTOCNode getNode(String urlOrName)
    {
        return mNodeMap.get(urlOrName);
    }

    void addNode(NSISHelpTOCNode node)
    {
        if(node.getOwner() != this) {
            throw new IllegalArgumentException();
        }
        if(mChildren == null) {
            mChildren = new ArrayList<NSISHelpTOCNode>();
        }
        mChildren.add(node);
        mapNode(node);
    }

    public class NSISHelpTOCNode implements Serializable
    {
        private static final long serialVersionUID = 6411295289602629504L;

        private String mName;
        private String mURL;
        private List<NSISHelpTOCNode> mNodeChildren;
        private NSISHelpTOCNode mParent = null;

        private NSISHelpTOCNode(String name, String url)
        {
            super();
            mName = name;
            mURL = url;
        }

        void addNode(NSISHelpTOCNode node)
        {
            if(node.getOwner() != NSISHelpTOC.this) {
                throw new IllegalArgumentException();
            }
            if(mNodeChildren == null) {
                mNodeChildren = new ArrayList<NSISHelpTOCNode>();
            }
            mNodeChildren.add(node);
            mapNode(node);
            node.setParent(this);
        }

        public NSISHelpTOC getOwner()
        {
            return NSISHelpTOC.this;
        }

        public List<NSISHelpTOCNode> getChildren()
        {
            return (mNodeChildren != null?Collections.unmodifiableList(mNodeChildren):null);
        }

        public String getName()
        {
            return mName;
        }

        public NSISHelpTOCNode getParent()
        {
            return mParent;
        }

        private void setParent(NSISHelpTOCNode parent)
        {
            mParent = parent;
        }

        public String getURL()
        {
            return mURL;
        }

        @Override
        public String toString()
        {
            return new StringBuffer(mName).append(" - ").append(mURL).toString(); //$NON-NLS-1$
        }
    }
}
