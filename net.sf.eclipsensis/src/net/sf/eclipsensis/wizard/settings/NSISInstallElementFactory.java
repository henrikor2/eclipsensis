/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.lang.reflect.Constructor;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Node;

public class NSISInstallElementFactory
{
    private static final String TYPE_ALIASES = "type.aliases"; //$NON-NLS-1$
    private static final String PRELOAD_INSTALLELEMENTS = "preload.installelements"; //$NON-NLS-1$
    private static final String VALID_TYPES = "valid.types"; //$NON-NLS-1$

    private static final ResourceBundle cBundle;
    private static final Map<String, String> cTypeAliases = new HashMap<String, String>();
    private static final Set<String> cValidTypes = new HashSet<String>();
    private static final Map<String, NSISInstallElementDescriptor<? extends INSISInstallElement>> cElementMap = new HashMap<String, NSISInstallElementDescriptor<? extends INSISInstallElement>>();
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static INSISHomeListener cNSISHomeListener  = new INSISHomeListener() {
        public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
        {
            loadTypes(monitor);
        }
    };

    static {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(NSISInstallElementFactory.class.getName());
        }
        catch (MissingResourceException x) {
            bundle = null;
        }
        cBundle = bundle;
        if(cBundle != null) {
            String typeAliasesList;
            try {
                typeAliasesList = cBundle.getString(TYPE_ALIASES);
            }
            catch(MissingResourceException mre) {
                typeAliasesList = null;
            }
            String[] typeAliases = Common.tokenize(typeAliasesList, ',');
            for (int i = 0; i < typeAliases.length; i++) {
                int n = typeAliases[i].indexOf('=');
                if(n > 0 && n < typeAliases[i].length()-1) {
                    String type = typeAliases[i].substring(0,n);
                    String alias = typeAliases[i].substring(n+1);
                    cTypeAliases.put(type, alias);
                    cTypeAliases.put(alias, type);
                }
            }
        }
        EclipseNSISPlugin.getDefault().registerService(new IEclipseNSISService() {
            private boolean mStarted = false;

            public void start(IProgressMonitor monitor)
            {
                loadTypes(monitor);
                NSISPreferences.getInstance().addListener(cNSISHomeListener);
                mStarted = true;
            }

            public void stop(IProgressMonitor monitor)
            {
                mStarted = false;
                NSISPreferences.getInstance().removeListener(cNSISHomeListener);
            }

            public boolean isStarted()
            {
                return mStarted;
            }
        });
        if(cBundle != null) {
            String classList;
            try {
                classList = cBundle.getString(PRELOAD_INSTALLELEMENTS);
            }
            catch(MissingResourceException mre) {
                classList = null;
            }
            String[] classes = Common.tokenize(classList, ',');
            if(!Common.isEmptyArray(classes)) {
                for (int i = 0; i < classes.length; i++) {
                    try {
                        Class.forName(classes[i]);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String getAlias(String type)
    {
        return cTypeAliases.get(type);
    }

    private static void loadTypes(IProgressMonitor monitor)
    {
        Version nsisVersion = NSISPreferences.getInstance().getNSISVersion();
        cValidTypes.clear();
        if(cBundle != null) {
            Version maxVersion = null;
            String validTypes = null;
            for(Enumeration<String> e=cBundle.getKeys(); e.hasMoreElements();) {
                String key = e.nextElement();
                if(key.startsWith(VALID_TYPES)) {
                    int n = key.indexOf('#');
                    Version version = n >= 0?new Version(key.substring(n+1)):INSISVersions.MINIMUM_VERSION;
                    if(nsisVersion.compareTo(version) >= 0) {
                        if(maxVersion == null || version.compareTo(maxVersion) > 0) {
                            maxVersion = version;
                            validTypes = cBundle.getString(key);
                        }
                    }
                }
            }
            if(validTypes != null) {
                cValidTypes.addAll(Common.tokenizeToList(validTypes, ','));
            }
        }
    }

    private NSISInstallElementFactory()
    {
    }

    public static void register(String type, String typeName, Image image, Class<? extends INSISInstallElement> clasz)
    {
        if(!cElementMap.containsKey(type)) {
            try {
                NSISInstallElementDescriptor<? extends INSISInstallElement> descriptor = createDescriptor(clasz, typeName, image);
                cElementMap.put(type, descriptor);
            }
            catch(Exception ex) {
            }
        }
    }

    private static <T extends INSISInstallElement > NSISInstallElementDescriptor<T> createDescriptor(Class<T> clasz, String typeName,Image image) throws NoSuchMethodException {
        return new NSISInstallElementDescriptor<T>(clasz, typeName, image);
    }

    public static void unregister(String type, Class<? extends INSISInstallElement> clasz)
    {
        if(cElementMap.containsKey(type) && cElementMap.get(type).getElementClass().equals(clasz)) {
            cElementMap.remove(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends INSISInstallElement> NSISInstallElementDescriptor<T> getDescriptor(String type)
    {
        String type2 = type;
        if(!cValidTypes.contains(type2)) {
            type2 = cTypeAliases.get(type2);
            if(type2 == null || !cValidTypes.contains(type2)) {
                return null;
            }
        }
        return (NSISInstallElementDescriptor<T>)cElementMap.get(type2);
    }

    public static INSISInstallElement create(String type)
    {
        NSISInstallElementDescriptor<? extends INSISInstallElement> descriptor = getDescriptor(type);
        if(descriptor != null) {
            try {
                return descriptor.getConstructor().newInstance(EMPTY_OBJECT_ARRAY);
            }
            catch (Exception e) {
            }
        }
        return null;
    }

    public static INSISInstallElement createFromNode(Node node)
    {
        return createFromNode(node,null);
    }

    public static INSISInstallElement createFromNode(Node node, String type)
    {
        if(node.getNodeName().equals(INSISInstallElement.NODE)) {
            String nodeType = node.getAttributes().getNamedItem(INSISInstallElement.TYPE_ATTRIBUTE).getNodeValue();
            if(Common.isEmpty(type) || nodeType.equals(type)) {
                INSISInstallElement element = create(nodeType);
                if(element != null) {
                    element.fromNode(node);
                    return element;
                }
            }
        }
        return null;
    }

    public static Image getImage(String type)
    {
        NSISInstallElementDescriptor<? extends INSISInstallElement> descriptor = getDescriptor(type);
        if(descriptor != null) {
            return descriptor.getImage();
        }
        return null;
    }

    public static String getTypeName(String type)
    {
        NSISInstallElementDescriptor<? extends INSISInstallElement> descriptor = getDescriptor(type);
        if(descriptor != null) {
            return descriptor.getTypeName();
        }
        return null;
    }

    public static boolean isValidType(String type)
    {
        return cValidTypes.contains(type) || !cValidTypes.contains(null) && cValidTypes.contains(cTypeAliases.get(type));
    }

    static void setImage(String type, Image image)
    {
        NSISInstallElementDescriptor<? extends INSISInstallElement> descriptor = getDescriptor(type);
        if(descriptor != null) {
            descriptor.setImage(image);
        }
    }

    static void setTypeName(String type, String typeName)
    {
        NSISInstallElementDescriptor<? extends INSISInstallElement> descriptor = getDescriptor(type);
        if(descriptor != null) {
            descriptor.setTypeName(typeName);
        }
    }

    private static class NSISInstallElementDescriptor<T extends INSISInstallElement>
    {
        public String mTypeName;
        public Image mImage;
        public Class<T> mElementClass;
        public Constructor<T> mConstructor;

        public NSISInstallElementDescriptor(Class<T> clasz, String typeName, Image image) throws SecurityException, NoSuchMethodException, IllegalArgumentException
        {
            mElementClass = clasz;
            mConstructor = clasz.getConstructor(EMPTY_CLASS_ARRAY);
            mTypeName = typeName;
            mImage = image;
        }

        /**
         * @return Returns the class.
         */
        public Class<T> getElementClass()
        {
            return mElementClass;
        }

        /**
         * @return Returns the constructor.
         */
        public Constructor<T> getConstructor()
        {
            return mConstructor;
        }

        public String getTypeName()
        {
            return mTypeName;
        }

        /**
         * @return Returns the image.
         */
        public Image getImage()
        {
            return mImage;
        }

        public void setImage(Image image)
        {
            mImage = image;
        }

        public void setTypeName(String typeName)
        {
            mTypeName = typeName;
        }
    }
}
