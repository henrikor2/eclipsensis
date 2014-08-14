/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Collection;

import net.sf.eclipsensis.util.AbstractNodeConvertible;

import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Node;

public class DownloadSite extends AbstractNodeConvertible
{
    private static final long serialVersionUID = -6504957629669082738L;

    private File mImageFile;
    private String mLocation;
    private String mContinent;
    private String mName;

    public DownloadSite()
    {
        this(null, null, null, null);
    }

    DownloadSite(File imageFile, String location, String continent, String name)
    {
        mImageFile = imageFile;
        mLocation = location;
        mContinent = continent;
        mName = name;
    }

    public String getContinent()
    {
        return mContinent;
    }

    public Image getImage()
    {
        return NetworkUtil.getImageFromCache(mImageFile);
    }

    public File getImageFile()
    {
        return mImageFile;
    }

    public String getLocation()
    {
        return mLocation;
    }

    public String getName()
    {
        return mName;
    }

    @Override
    protected String getChildNodeName()
    {
        return "attribute"; //$NON-NLS-1$
    }

    public String getNodeName()
    {
        return "downloadsite"; //$NON-NLS-1$
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("image"); //$NON-NLS-1$
    }

    @Override
    protected void propertyFromNode(Node childNode, PropertyDescriptor propertyDescriptor)
    {
        String propertyName = propertyDescriptor.getName();
        if ("imageFile".equals(propertyName)) //$NON-NLS-1$
        {
            mImageFile = (File) getNodeValue(childNode, propertyDescriptor.getName(), File.class);
        }
        else if ("location".equals(propertyName)) //$NON-NLS-1$
        {
            mLocation = (String) getNodeValue(childNode, propertyDescriptor.getName(), String.class);
        }
        else if ("name".equals(propertyName)) //$NON-NLS-1$
        {
            mName = (String) getNodeValue(childNode, propertyDescriptor.getName(), String.class);
        }
        else if ("continent".equals(propertyName)) //$NON-NLS-1$
        {
            mContinent = (String) getNodeValue(childNode, propertyDescriptor.getName(), String.class);
        }
        else
        {
            super.propertyFromNode(childNode, propertyDescriptor);
        }
    }

    @Override
    protected boolean isConvertibleAttributeType(Class<?> clasz)
    {
        if(File.class.equals(clasz))
        {
            return true;
        }
        return super.isConvertibleAttributeType(clasz);
    }

    @Override
    protected Object convertFromString(String string, Class<?> clasz)
    {
        if (File.class.equals(clasz))
        {
            return new File(string);
        }
        return super.convertFromString(string, clasz);
    }

    @Override
    protected String convertToString(String name, Object obj)
    {
        if (obj instanceof File)
        {
            return ((File) obj).getAbsolutePath();
        }
        return super.convertToString(name, obj);
    }

    @Override
    public int hashCode()
    {
        int result = 31 + (mContinent == null ? 0 : mContinent.hashCode());
        result = 31 * result + (mImageFile == null ? 0 : mImageFile.hashCode());
        result = 31 * result + (mLocation == null ? 0 : mLocation.hashCode());
        result = 31 * result + (mName == null ? 0 : mName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        DownloadSite other = (DownloadSite) obj;
        if (mContinent == null)
        {
            if (other.mContinent != null)
            {
                return false;
            }
        }
        else if (!mContinent.equals(other.mContinent))
        {
            return false;
        }
        if (mImageFile == null)
        {
            if (other.mImageFile != null)
            {
                return false;
            }
        }
        else if (!mImageFile.equals(other.mImageFile))
        {
            return false;
        }
        if (mLocation == null)
        {
            if (other.mLocation != null)
            {
                return false;
            }
        }
        else if (!mLocation.equals(other.mLocation))
        {
            return false;
        }
        if (mName == null)
        {
            if (other.mName != null)
            {
                return false;
            }
        }
        else if (!mName.equals(other.mName))
        {
            return false;
        }
        return true;
    }
}