/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini.validators;

import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

public class INIKeyValueValidatorRegistry
{
    private static Map<String, IINIKeyValueValidator> mRegistry = new CaseInsensitiveMap<IINIKeyValueValidator>();

    static {
        init();
    }

    @SuppressWarnings("unchecked")
    private static void init()
    {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(INIKeyValueValidatorRegistry.class.getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }

        if(bundle != null) {
            HashMap<Class<? extends IINIKeyValueValidator>, IINIKeyValueValidator> map = new HashMap<Class<? extends IINIKeyValueValidator>, IINIKeyValueValidator>();
            for(Enumeration<String> e=bundle.getKeys(); e.hasMoreElements();) {
                try {
                    IINIKeyValueValidator validator;

                    String key = e.nextElement();
                    String className = bundle.getString(key);
                    Class<? extends IINIKeyValueValidator> clasz = (Class<? extends IINIKeyValueValidator>) Class.forName(className);
                    if(map.containsKey(clasz)) {
                        validator = map.get(clasz);
                    }
                    else {
                        Constructor<? extends IINIKeyValueValidator> c = clasz.getConstructor((Class[])null);
                        validator = c.newInstance((Object[])null);
                        map.put(clasz,validator);
                    }
                    mRegistry.put(key,validator);
                }
                catch(Exception ex) {
                    InstallOptionsPlugin.getDefault().log(ex);
                }
            }
        }
    }

    private INIKeyValueValidatorRegistry()
    {
    }

    public static IINIKeyValueValidator getKeyValueValidator(String name)
    {
        return mRegistry.get(name);
    }
}
