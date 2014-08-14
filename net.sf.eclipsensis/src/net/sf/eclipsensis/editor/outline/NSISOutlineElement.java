/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;

/**
 * An outline element.
 */
public class NSISOutlineElement implements Serializable
{
    private static final long serialVersionUID = -7247998999735822359L;

    static final NSISOutlineContentResources.Type ROOT = null;

    private NSISOutlineContentResources.Type mType;
    private String mName;
    private transient Position mSelectPosition;
    private transient Position mPosition;
    private List<NSISOutlineElement> mChildren = new ArrayList<NSISOutlineElement>();
    private NSISOutlineElement mParent = null;

    /**
     * @param type
     * @param name
     * @param position
     */
    NSISOutlineElement(NSISOutlineContentResources.Type type, String name, Position position)
    {
        mType = type;
        mName = name;
        setPosition(position);
    }

    /**
     * @param type
     * @param name
     * @param position
     */
    NSISOutlineElement(NSISOutlineContentResources.Type type, String name, Position position, Position selectPosition)
    {
        this(type, name, position);
        setSelectPosition(selectPosition);
    }

    void setName(String name)
    {
        mName = name;
    }

    @Override
    public String toString()
    {
        if(isRoot()) {
            if(!Common.isEmpty(mName)) {
                return mName;
            }
            else {
                return "";
            }
        }
        else {
            if(!Common.isEmpty(mName)) {
                return new StringBuffer(getTypeName()).append(" ").append(mName).toString(); //$NON-NLS-1$
            }
            else {
                return getTypeName();
            }
        }
    }

    public boolean isRoot()
    {
        return mParent == null && ROOT == mType;
    }

    public boolean hasChildren()
    {
        return !Common.isEmptyCollection(mChildren);
    }

    /**
     * @return Returns the children.
     */
    public List<NSISOutlineElement> getChildren()
    {
        return mChildren;
    }
    /**
     * @return Returns the icon.
     */
    public Image getIcon()
    {
        return mType.getImage();
    }

    public NSISOutlineContentResources.Type getType()
    {
        return mType;
    }

    /**
     * @return Returns the name.
     */
    public String getTypeName()
    {
        return mType.getType();
    }
    /**
     * @return Returns the position.
     */
    public Position getPosition()
    {
        return mPosition;
    }

    /**
     * @param position The position to set.
     */
    void setPosition(Position position)
    {
        mPosition = position;
    }

    public void setSelectPosition(Position selectPosition)
    {
        mSelectPosition = selectPosition;
    }

    public Position getSelectPosition()
    {
        return mSelectPosition;
    }

    public void setParent(NSISOutlineElement parent)
    {
        if(mParent != null) {
            mParent.removeChild(this);
        }
        mParent = parent;
        if(mParent != null && !mParent.mChildren.contains(this)) {
            mParent.addChild(this);
        }
    }

    /**
     * @return Returns the parent.
     */
    public NSISOutlineElement getParent()
    {
        return mParent;
    }

    public void removeChild(NSISOutlineElement child)
    {
        if(child != null && mChildren.contains(child)) {
            if(child.mParent == this) {
                child.mParent = null;
            }
            mChildren.remove(child);
        }
    }

    public void addChild(NSISOutlineElement child)
    {
        if(child !=null && !mChildren.contains(child)) {
            mChildren.add(child);
            child.setParent(this);
        }
    }

    public void merge(Position position)
    {
        if(position != null) {
            int start = Math.min(mPosition.getOffset(),position.getOffset());
            int end = Math.max(mPosition.getOffset()+mPosition.getLength(),
                            position.getOffset()+position.getLength());
            mPosition.setOffset(start);
            mPosition.setLength(end-start);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException
    {
        stream.defaultWriteObject();
        writePosition(stream, mPosition);
        writePosition(stream, mSelectPosition);
    }

    private void writePosition(ObjectOutputStream stream, Position position) throws IOException
    {
        if(position != null)
        {
            stream.writeInt(position.offset);
            stream.writeInt(position.length);
            stream.writeBoolean(position.isDeleted);
        }
        else
        {
            stream.writeInt(-1);
        }
    }

    private Position readPosition(ObjectInputStream stream) throws IOException
    {
        int offset = stream.readInt();
        if(offset >= 0) {
            int length = stream.readInt();
            Position position = new Position(offset, length);
            position.isDeleted = stream.readBoolean();
            return position;
        }
        return null;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        mPosition = readPosition(stream);
        mSelectPosition = readPosition(stream);
    }
}