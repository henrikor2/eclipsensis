/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.*;

public class CompoundResourceBundle extends ResourceBundle
{
    private static final Locale EMPTY_LOCALE = new Locale("","",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private ResourceBundle[] mResourceBundles = null;

    public CompoundResourceBundle(String[] bundleNames)
    {
        this(CompoundResourceBundle.class.getClassLoader(), bundleNames);
    }

    public CompoundResourceBundle(ClassLoader loader, String[] bundleNames)
    {
        this(loader, Locale.getDefault(), bundleNames);
    }

    public CompoundResourceBundle(Locale locale, String[] bundleNames)
    {
        this(CompoundResourceBundle.class.getClassLoader(), locale, bundleNames);
    }

    public CompoundResourceBundle(ClassLoader loader, Locale locale, String[] bundleNames)
    {
        super();
        mResourceBundles = new ResourceBundle[bundleNames.length];
        for (int i = 0; i < bundleNames.length; i++) {
            try {
                mResourceBundles[i] = ResourceBundle.getBundle(bundleNames[i], locale, loader);
            } catch (MissingResourceException x) {
                mResourceBundles[i] = null;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#getKeys()
     */
    @Override
    public Enumeration<String> getKeys()
    {
        List<String> list = null;
        for(int i=0; i<mResourceBundles.length; i++) {
            if(mResourceBundles[i] != null) {
                if(list == null) {
                    list = Collections.list(mResourceBundles[i].getKeys());
                }
                else {
                    list.addAll(Collections.list(mResourceBundles[i].getKeys()));
                }
            }
        }
        if(list != null) {
            return Collections.enumeration(list);
        }
        else {
            return Collections.enumeration(Collections.<String>emptyList());
        }
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    @Override
    protected Object handleGetObject(String key)
    {
        for(int i=0; i<mResourceBundles.length; i++) {
            if(mResourceBundles[i] != null) {
                try {
                    return mResourceBundles[i].getObject(key);
                }
                catch(MissingResourceException mre) {
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#getLocale()
     */
    @Override
    public Locale getLocale()
    {
        for(int i=mResourceBundles.length-1; i>=0; i--) {
            if(mResourceBundles[i] != null) {
                try {
                    return mResourceBundles[i].getLocale();
                }
                catch(MissingResourceException mre) {
                }
            }
        }
        return EMPTY_LOCALE;
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#setParent(java.util.ResourceBundle)
     */
    @Override
    protected void setParent(ResourceBundle parent)
    {
    }
}