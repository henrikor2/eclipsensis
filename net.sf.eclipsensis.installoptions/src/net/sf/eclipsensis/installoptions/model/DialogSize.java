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

import org.eclipse.draw2d.geometry.Dimension;

public class DialogSize
{
    private String mName;
    private boolean mDefault;
    private Dimension mSize;

    public DialogSize(String name, boolean isDefault, Dimension size)
    {
        super();
        mName = name;
        mSize = size;
        mDefault = isDefault;
    }

    public DialogSize getCopy()
    {
        return new DialogSize(mName, mDefault, mSize.getCopy());
    }

    public boolean isDefault()
    {
        return mDefault;
    }

    public void setDefault(boolean default1)
    {
        mDefault = default1;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public Dimension getSize()
    {
        return mSize;
    }

    public void setSize(Dimension size)
    {
        mSize = size;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof DialogSize) {
            DialogSize ds = (DialogSize)o;
            return mName.equals(ds.getName()) && mSize.equals(ds.getSize());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return mName.hashCode()+mSize.hashCode();
    }
}