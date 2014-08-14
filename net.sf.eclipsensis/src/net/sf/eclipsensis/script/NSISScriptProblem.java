/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import java.io.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;

public class NSISScriptProblem implements Serializable
{
    private static final long serialVersionUID = -3563285678392628757L;

    public static final int TYPE_WARNING=0;
    public static final int TYPE_ERROR=1;

    private transient IPath mPath;
    private int mType;
    private String mText;
    private int mLine;
    private transient IMarker mMarker;

    public NSISScriptProblem(IPath path, int type, String text)
    {
       this(path, type, text, 1);
    }

    public NSISScriptProblem(IPath path, int type, String text, int line)
    {
        mPath = path;
        mType = type;
        mText = text;
        mLine = line;
    }

    public IMarker getMarker()
    {
        return mMarker;
    }

    public void setMarker(IMarker marker)
    {
        mMarker = marker;
    }

    public int getLine()
    {
        return mLine;
    }

    public Object getPath()
    {
        return mPath;
    }

    public String getText()
    {
        return mText;
    }

    public int getType()
    {
        return mType;
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeObject(mPath==null?null:mPath.toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        String s = (String)in.readObject();
        if(s != null) {
            mPath = new Path(s);
        }
        else {
            mPath = null;
        }
    }
}
