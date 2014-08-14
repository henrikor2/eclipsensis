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

import java.io.Serializable;

import org.eclipse.draw2d.geometry.*;

public class Position implements Cloneable, Serializable
{
    private static final long serialVersionUID = 2635176884349612233L;

    public int left;
    public int top;
    public int right;
    public int bottom;

    /**
     *
     */
    public Position()
    {
    }

    public Position(Rectangle rect)
    {
        this(rect.x,rect.y,rect.x+rect.width-1,rect.y+rect.height-1);
    }

    public Position(int left, int top, int right, int bottom)
    {
        this();
        set(left, top, right, bottom);
    }

    public void set(int left, int top, int right, int bottom)
    {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(Position pos)
    {
        if(pos != null) {
            set(pos.left,pos.top,pos.right,pos.bottom);
        }
    }

    public Position getCopy()
    {
        return new Position(left,top,right,bottom);
    }

    @Override
    protected Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return getCopy();
        }
    }

    public Rectangle getBounds()
    {
        return new Rectangle(left,top,right-left+1,bottom-top+1);
    }

    public Point getLocation()
    {
        return new Point(left,top);
    }

    public Dimension getSize()
    {
        return new Dimension(right-left+1,bottom-top+1);
    }

    public void setLocation(Point p)
    {
        setLocation(p.x,p.y);
    }

    public void setLocation(int x, int y)
    {
        right += (x-left);
        bottom += (y-top);
        left = x;
        top = y;
    }

    public void move(int x, int y)
    {
        setLocation(left+x,top+y);
    }

    public void move(Dimension offset)
    {
        move(offset.width, offset.height);
    }

    public void setSize(Dimension d)
    {
        setSize(d.width, d.height);
    }

    public void setSize(int width, int height)
    {
        right = left + width - 1;
        bottom = top + height - 1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Position) {
            Position p = (Position)obj;
            return (left == p.left && top == p.top && right == p.right && bottom == p.bottom);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return left+top+right+bottom;
    }

    @Override
    public String toString()
    {
        return new StringBuffer("Position(").append(left).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
            top).append(",").append(right).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
            bottom).append(")").toString(); //$NON-NLS-1$
    }
}
