/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import java.beans.*;
import java.util.*;

import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;

public class InstallOptionsGuide
{
    /**
     * Property used to notify mListeners when the parts attached to a guide are
     * changed
     */
    public static final String PROPERTY_CHILDREN = "net.sf.eclipsensis.installoptions.children_changed"; //$NON-NLS-1$

    /**
     * Property used to notify mListeners when the guide is re-positioned
     */
    public static final String PROPERTY_POSITION = "net.sf.eclipsensis.installoptions.position_changed"; //$NON-NLS-1$

    protected PropertyChangeSupport mListeners = new PropertyChangeSupport(this);

    private Map<InstallOptionsWidget, Integer> mMap;

    private int mPosition;

    private boolean mHorizontal;

    /**
     * Empty default constructor
     */
    public InstallOptionsGuide()
    {
        // empty constructor
    }

    /**
     * Constructor
     *
     * @param isHorizontal
     *            <code>true</code> if the guide is mHorizontal (i.e., placed
     *            on a vertical ruler)
     */
    public InstallOptionsGuide(boolean isHorizontal)
    {
        setHorizontal(isHorizontal);
    }

    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        mListeners.addPropertyChangeListener(listener);
    }

    /**
     * Attaches the given widget along the given edge to this guide. The
     * InstallOptionsElement is also updated to reflect this attachment.
     *
     * @param widget
     *            The widget that is to be attached to this guide; if the widget is
     *            already attached, its alignment is updated
     * @param alignment
     *            -1 is left or top; 0, center; 1, right or bottom
     */
    public void attachWidget(InstallOptionsWidget widget, int alignment)
    {
        if (getMap().containsKey(widget) && getAlignment(widget) == alignment) {
            return;
        }

        getMap().put(widget, new Integer(alignment));
        InstallOptionsGuide oldGuide = isHorizontal()?widget.getHorizontalGuide():widget
                .getVerticalGuide();
        if (oldGuide != null && oldGuide != this) {
            oldGuide.detachWidget(widget);
        }
        if (isHorizontal()) {
            widget.setHorizontalGuide(this);
        }
        else {
            widget.setVerticalGuide(this);
        }
        mListeners.firePropertyChange(PROPERTY_CHILDREN, null, widget);
    }

    /**
     * Detaches the given widget from this guide. The InstallOptionsElement is also updated
     * to reflect this change.
     *
     * @param widget
     *            the widget that is to be detached from this guide
     */
    public void detachWidget(InstallOptionsWidget widget)
    {
        if (getMap().containsKey(widget)) {
            getMap().remove(widget);
            if (isHorizontal()) {
                widget.setHorizontalGuide(null);
            }
            else {
                widget.setVerticalGuide(null);
            }
            mListeners.firePropertyChange(PROPERTY_CHILDREN, null, widget);
        }
    }

    public int getAlignment(InstallOptionsWidget widget)
    {
        if (getMap().get(widget) != null) {
            return (getMap().get(widget)).intValue();
        }
        return -2;
    }

    /**
     * @return The Map containing all the parts attached to this guide, and
     *         their alignments; the keys are IOElements and values are
     *         Integers
     */
    public Map<InstallOptionsWidget, Integer> getMap()
    {
        if (mMap == null) {
            mMap = new HashMap<InstallOptionsWidget, Integer>();
        }
        return mMap;
    }

    /**
     * @return the set of all the widgets attached to this guide; a set is used
     *         because a widget can only be attached to a guide along one edge.
     */
    public Set<InstallOptionsWidget> getWidgets()
    {
        return getMap().keySet();
    }

    /**
     * @return the mPosition/location of the guide (in pixels)
     */
    public int getPosition()
    {
        return mPosition;
    }

    /**
     * @return <code>true</code> if the guide is mHorizontal (i.e., placed on a
     *         vertical ruler)
     */
    public boolean isHorizontal()
    {
        return mHorizontal;
    }

    /**
     * @see PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        mListeners.removePropertyChangeListener(listener);
    }

    /**
     * Sets the orientation of the guide
     *
     * @param isHorizontal
     *            <code>true</code> if this guide is to be placed on a
     *            vertical ruler
     */
    public void setHorizontal(boolean isHorizontal)
    {
        mHorizontal = isHorizontal;
    }

    /**
     * Sets the location of the guide
     *
     * @param offset
     *            The location of the guide (in pixels)
     */
    public void setPosition(int offset)
    {
        if (mPosition != offset) {
            int oldValue = mPosition;
            mPosition = offset;
            mListeners.firePropertyChange(PROPERTY_POSITION, new Integer(
                    oldValue), new Integer(mPosition));
        }
    }
}
