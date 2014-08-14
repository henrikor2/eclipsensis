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

import org.eclipse.gef.rulers.RulerProvider;

public class InstallOptionsRuler
{
    public static final String PROPERTY_CHILDREN = "net.sf.eclipsensis.installoptions.children_changed"; //$NON-NLS-1$

    public static final String PROPERTY_UNIT = "net.sf.eclipsensis.installoptions.units_changed"; //$NON-NLS-1$

    protected PropertyChangeSupport mListeners = new PropertyChangeSupport(this);

    private int mUnit;

    private boolean mHorizontal;

    private List<InstallOptionsGuide> mGuides = new ArrayList<InstallOptionsGuide>();

    public InstallOptionsRuler(boolean isHorizontal)
    {
        this(isHorizontal, RulerProvider.UNIT_PIXELS);
    }

    public InstallOptionsRuler(boolean isHorizontal, int unit)
    {
        mHorizontal = isHorizontal;
        setUnit(unit);
    }

    public void addGuide(InstallOptionsGuide guide)
    {
        if (!mGuides.contains(guide)) {
            guide.setHorizontal(!isHorizontal());
            mGuides.add(guide);
            mListeners.firePropertyChange(PROPERTY_CHILDREN, null, guide);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        mListeners.addPropertyChangeListener(listener);
    }

    // the returned list should not be modified
    public List<InstallOptionsGuide> getGuides()
    {
        return mGuides;
    }

    public int getUnit()
    {
        return mUnit;
    }

    public boolean isHidden()
    {
        return false;
    }

    public boolean isHorizontal()
    {
        return mHorizontal;
    }

    public void removeGuide(InstallOptionsGuide guide)
    {
        if (mGuides.remove(guide)) {
            mListeners.firePropertyChange(PROPERTY_CHILDREN, null, guide);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        mListeners.removePropertyChangeListener(listener);
    }

    public void setHidden(boolean isHidden)
    {
    }

    public void setUnit(int newUnit)
    {
        if (mUnit != newUnit) {
            int oldUnit = mUnit;
            mUnit = newUnit;
            mListeners.firePropertyChange(PROPERTY_UNIT, oldUnit, newUnit);
        }
    }
}
