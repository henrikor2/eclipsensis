/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 ******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.winapi.*;

import org.eclipse.swt.graphics.Image;

public class RegistryKey
{
    static final Image REGKEY_IMAGE;
    static final Image OPEN_REGKEY_IMAGE;

    static final Comparator<RegistryKey> SEARCH_COMPARATOR = new Comparator<RegistryKey>() {
        public int compare(RegistryKey o1, RegistryKey o2)
        {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
    };
    static final RegistryKey[] EMPTY_ARRAY = new RegistryKey[0];

    static {
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        REGKEY_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.key.image")); //$NON-NLS-1$
        OPEN_REGKEY_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.key.open.image")); //$NON-NLS-1$
    }

    protected IHandle mHandle = WinAPI.ZERO_HANDLE;
    protected String mName = ""; //$NON-NLS-1$
    protected int mChildCount = -1;
    protected RegistryKey[] mChildren = null;
    protected RegistryKey mParent = null;
    private String mString = null;
    private List<RegistryValue> mValues = new ArrayList<RegistryValue>();

    RegistryKey(RegistryKey parent, IHandle handle, String name)
    {
        this(parent, name);
        mHandle = handle;
    }

    public RegistryKey(RegistryKey parent, String name)
    {
        mParent = parent;
        setName(name);
    }

    public synchronized List<RegistryValue> getValues()
    {
        return Collections.unmodifiableList(mValues);
    }

    public synchronized void setValues(List<RegistryValue> values)
    {
        mValues.clear();
        mValues.addAll(values);
    }

    void setName(String name)
    {
        mName = name;
    }

    public Image getImage()
    {
        return REGKEY_IMAGE;
    }

    public Image getExpandedImage()
    {
        return OPEN_REGKEY_IMAGE;
    }

    public int getChildCount()
    {
        return mChildCount;
    }

    public RegistryKey[] getChildren()
    {
        return mChildren;
    }

    public String getName()
    {
        return mName;
    }

    public IHandle getHandle()
    {
        if(mHandle != null && WinAPI.ZERO_HANDLE.equals(mHandle)) {
            mHandle = WinAPI.INSTANCE.regOpenKeyEx(getParent().mHandle,getName(),0,WinAPI.KEY_QUERY_VALUE|WinAPI.KEY_ENUMERATE_SUB_KEYS);
        }
        return mHandle;
    }

    public RegistryKey getParent()
    {
        return mParent;
    }

    public int find(String name)
    {
        if(mChildCount < 0) {
            open();
        }
        return Arrays.binarySearch(mChildren,new RegistryKey(null,name),SEARCH_COMPARATOR);
    }

    public void open()
    {
        if(mChildCount < 0) {
            IHandle handle = getHandle();
            if(handle != null && !WinAPI.ZERO_HANDLE.equals(handle)) {
                int[] sizes = {0,0};
                WinAPI.INSTANCE.regQueryInfoKey(handle,sizes);
                mChildCount = sizes[0];
                if(mChildCount > 0) {
                    mChildren = new RegistryKey[mChildCount];
                    for (int i = 0; i < mChildren.length; i++) {
                        String subKey = WinAPI.INSTANCE.regEnumKeyEx(handle,i,sizes[1]);
                        mChildren[i] = new RegistryKey(this,subKey);
                    }
                    Arrays.sort(mChildren,SEARCH_COMPARATOR);
                }
                else {
                    mChildren = EMPTY_ARRAY;
                }
            }
            else {
                mChildCount = 0;
                mChildren = EMPTY_ARRAY;
            }
        }
    }

    public void close()
    {
        if(mChildCount > 0) {
            if(!Common.isEmptyArray(mChildren)) {
                for (int i=0; i<mChildren.length; i++) {
                    mChildren[i].close();
                }
            }
            mChildCount = 0;
            mChildren = null;
        }
        if(!(getParent() instanceof RegistryRoot) && mHandle != null && !WinAPI.ZERO_HANDLE.equals(mHandle)) {
            WinAPI.INSTANCE.regCloseKey(mHandle);
            mHandle = WinAPI.ZERO_HANDLE;
        }
    }

    @Override
    public int hashCode()
    {
        return mName.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o != this) {
            if(o instanceof RegistryKey) {
                RegistryKey r = (RegistryKey)o;
                if(Common.objectsAreEqual(getParent(),r.getParent())) {
                    return Common.stringsAreEqual(r.getName(),getName(),true);
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        if(mString == null) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            expandName(buf);
            mString = buf.toString();
        }
        return mString;
    }

    protected void expandName(StringBuffer buf)
    {
        if(mParent != null) {
            mParent.expandName(buf);
            if(buf.length() > 0) {
                buf.append("\\"); //$NON-NLS-1$
            }
        }
        buf.append(mName);
    }

    public RegistryKey getRootKey()
    {
        if(mParent instanceof RegistryRoot) {
            return this;
        }
        else {
            return mParent==null?null:mParent.getRootKey();
        }
    }
}
