/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.util.*;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.*;

public class PreferenceStoreWrapper implements IPreferenceStore
{
    private IPreferenceStore mParentStore;
    private IPreferenceStore mInternalStore;
    private IPropertyChangeListener mPropertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event)
        {
            String name = event.getProperty();
            if(mInternalStore.contains(name)) {
                Object obj = event.getNewValue();
                if(obj != null) {
                    if(obj instanceof Long) {
                        long newValue = ((Long)obj).longValue();
                        long oldValue = mInternalStore.getLong(name);
                        if(newValue != oldValue) {
                            mInternalStore.setValue(name,newValue);
                        }
                    }
                    else if(obj instanceof Integer) {
                        int newValue = ((Integer)obj).intValue();
                        int oldValue = mInternalStore.getInt(name);
                        if(newValue != oldValue) {
                            mInternalStore.setValue(name,newValue);
                        }
                    }
                    else if(obj instanceof Float) {
                        float newValue = ((Float)obj).floatValue();
                        float oldValue = mInternalStore.getFloat(name);
                        if(newValue != oldValue) {
                            mInternalStore.setValue(name,newValue);
                        }
                    }
                    else if(obj instanceof Double) {
                        double newValue = ((Double)obj).doubleValue();
                        double oldValue = mInternalStore.getDouble(name);
                        if(newValue != oldValue) {
                            mInternalStore.setValue(name,newValue);
                        }
                    }
                    else if(obj instanceof Boolean) {
                        boolean newValue = ((Boolean)obj).booleanValue();
                        boolean oldValue = mInternalStore.getBoolean(name);
                        if(newValue != oldValue) {
                            mInternalStore.setValue(name,newValue);
                        }
                    }
                    else {
                        String newValue = obj.toString();
                        String oldValue = mInternalStore.getString(name);
                        if(newValue != oldValue) {
                            mInternalStore.setValue(name,newValue);
                        }
                    }
                }
            }
        }
    };
    private Map<String, Class<?>> mNamesMap = new HashMap<String, Class<?>>();
    /**
     * @param preferenceStore
     */
    public PreferenceStoreWrapper(IPreferenceStore preferenceStore)
    {
        mParentStore = preferenceStore;
        mInternalStore = new PreferenceStore();
        mParentStore.addPropertyChangeListener(mPropertyChangeListener);
    }

    public void load(String[] names)
    {
        for (int i = 0; i < names.length; i++) {
            checkString(names[i]);
        }
    }

    public void update()
    {
        for(Iterator<Map.Entry<String,Class<?>>> iter = mNamesMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String,Class<?>> entry = iter.next();
            String name = entry.getKey();
            Class<?> type = entry.getValue();
            if(type.equals(Boolean.class)) {
                mParentStore.setValue(name,mInternalStore.getBoolean(name));
            }
            else if(type.equals(Integer.class)) {
                mParentStore.setValue(name,mInternalStore.getInt(name));
            }
            else if(type.equals(Long.class)) {
                mParentStore.setValue(name,mInternalStore.getLong(name));
            }
            else if(type.equals(Float.class)) {
                mParentStore.setValue(name,mInternalStore.getFloat(name));
            }
            else if(type.equals(Double.class)) {
                mParentStore.setValue(name,mInternalStore.getDouble(name));
            }
            else {
                mParentStore.setValue(name,mInternalStore.getString(name));
            }
        }
    }

    public void loadDefaults()
    {
        for(Iterator<String> iter = mNamesMap.keySet().iterator(); iter.hasNext(); ) {
            mInternalStore.setToDefault(iter.next());
        }
    }

    public void dispose()
    {
        mParentStore.removePropertyChangeListener(mPropertyChangeListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener)
    {
        mInternalStore.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
     */
    public boolean contains(String name)
    {
        return mInternalStore.contains(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void firePropertyChangeEvent(String name, Object oldValue,
            Object newValue)
    {
        mInternalStore.firePropertyChangeEvent(name, oldValue, newValue);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String name)
    {
        checkBoolean(name);
        return mInternalStore.getBoolean(name);
    }

    /**
     * @param name
     */
    private void checkBoolean(String name)
    {
        if(!mInternalStore.contains(name) && mParentStore.contains(name)) {
            mInternalStore.setDefault(name,mParentStore.getDefaultBoolean(name));
            mInternalStore.setToDefault(name);
            mInternalStore.setValue(name,mParentStore.getBoolean(name));
            mNamesMap.put(name,Boolean.class);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
     */
    public boolean getDefaultBoolean(String name)
    {
        checkBoolean(name);
        return mInternalStore.getDefaultBoolean(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
     */
    public double getDefaultDouble(String name)
    {
        checkDouble(name);
        return mInternalStore.getDefaultDouble(name);
    }

    /**
     * @param name
     */
    private void checkDouble(String name)
    {
        if(!mInternalStore.contains(name) && mParentStore.contains(name)) {
            mInternalStore.setDefault(name,mParentStore.getDefaultDouble(name));
            mInternalStore.setToDefault(name);
            mInternalStore.setValue(name,mParentStore.getDouble(name));
            mNamesMap.put(name,Double.class);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
     */
    public float getDefaultFloat(String name)
    {
        checkFloat(name);
        return mInternalStore.getDefaultFloat(name);
    }

    /**
     * @param name
     */
    private void checkFloat(String name)
    {
        if(!mInternalStore.contains(name) && mParentStore.contains(name)) {
            mInternalStore.setDefault(name,mParentStore.getDefaultFloat(name));
            mInternalStore.setToDefault(name);
            mInternalStore.setValue(name,mParentStore.getFloat(name));
            mNamesMap.put(name,Float.class);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
     */
    public int getDefaultInt(String name)
    {
        checkInt(name);
        return mInternalStore.getDefaultInt(name);
    }

    /**
     * @param name
     */
    private void checkInt(String name)
    {
        if(!mInternalStore.contains(name) && mParentStore.contains(name)) {
            mInternalStore.setDefault(name,mParentStore.getDefaultInt(name));
            mInternalStore.setToDefault(name);
            mInternalStore.setValue(name,mParentStore.getInt(name));
            mNamesMap.put(name,Integer.class);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
     */
    public long getDefaultLong(String name)
    {
        checkLong(name);
        return mInternalStore.getDefaultLong(name);
    }

    /**
     * @param name
     */
    private void checkLong(String name)
    {
        if(!mInternalStore.contains(name) && mParentStore.contains(name)) {
            mInternalStore.setDefault(name,mParentStore.getDefaultLong(name));
            mInternalStore.setToDefault(name);
            mInternalStore.setValue(name,mParentStore.getLong(name));
            mNamesMap.put(name,Long.class);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
     */
    public String getDefaultString(String name)
    {
        checkString(name);
        return mInternalStore.getDefaultString(name);
    }

    /**
     * @param name
     */
    private void checkString(String name)
    {
        if(!mInternalStore.contains(name) && mParentStore.contains(name)) {
            mInternalStore.setDefault(name,mParentStore.getDefaultString(name));
            mInternalStore.setToDefault(name);
            mInternalStore.setValue(name,mParentStore.getString(name));
            mNamesMap.put(name,String.class);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
     */
    public double getDouble(String name)
    {
        checkDouble(name);
        return mInternalStore.getDouble(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
     */
    public float getFloat(String name)
    {
        checkFloat(name);
        return mInternalStore.getFloat(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
     */
    public int getInt(String name)
    {
        checkInt(name);
        return mInternalStore.getInt(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
     */
    public long getLong(String name)
    {
        checkLong(name);
        return mInternalStore.getLong(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
     */
    public String getString(String name)
    {
        checkString(name);
        return mInternalStore.getString(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
     */
    public boolean isDefault(String name)
    {
        return mInternalStore.isDefault(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
     */
    public boolean needsSaving()
    {
        return mInternalStore.needsSaving();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
     */
    public void putValue(String name, String value)
    {
        checkString(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.putValue(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener)
    {
        mInternalStore.removePropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
     */
    public void setDefault(String name, double value)
    {
        checkDouble(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setDefault(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
     */
    public void setDefault(String name, float value)
    {
        checkFloat(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setDefault(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
     */
    public void setDefault(String name, int value)
    {
        checkInt(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setDefault(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
     */
    public void setDefault(String name, long value)
    {
        checkLong(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setDefault(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
     */
    public void setDefault(String name, String value)
    {
        checkString(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setDefault(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
     */
    public void setDefault(String name, boolean value)
    {
        checkBoolean(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setDefault(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
     */
    public void setToDefault(String name)
    {
        if(!mInternalStore.contains(name)) {
            checkString(name);
        }
        if(mInternalStore.contains(name)) {
            mInternalStore.setToDefault(name);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
     */
    public void setValue(String name, double value)
    {
        checkDouble(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setValue(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
     */
    public void setValue(String name, float value)
    {
        checkFloat(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setValue(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
     */
    public void setValue(String name, int value)
    {
        checkInt(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setValue(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
     */
    public void setValue(String name, long value)
    {
        checkLong(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setValue(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
     */
    public void setValue(String name, String value)
    {
        checkString(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setValue(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
     */
    public void setValue(String name, boolean value)
    {
        checkBoolean(name);
        if(mInternalStore.contains(name)) {
            mInternalStore.setValue(name, value);
        }
    }
}
