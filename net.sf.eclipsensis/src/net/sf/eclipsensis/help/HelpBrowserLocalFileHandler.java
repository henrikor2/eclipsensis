/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.*;

public class HelpBrowserLocalFileHandler implements IExtensionChangeHandler, IHelpBrowserLocalFileHandler
{
    private static final String EXTENSION_POINT = "helpBrowserLocalFileHandler"; //$NON-NLS-1$
    private static final String HANDLER = "handler"; //$NON-NLS-1$
    private static final String HANDLER_EXTENSIONS = "extensions"; //$NON-NLS-1$
    private static final String HANDLER_CLASS = "class"; //$NON-NLS-1$

    private static final IHelpBrowserLocalFileHandler NULL_HANDLER = new IHelpBrowserLocalFileHandler() {
        public boolean handle(File file)
        {
            return false;
        }
    };

    public static final HelpBrowserLocalFileHandler INSTANCE = new HelpBrowserLocalFileHandler();

    private Map<String,List<HandlerDescriptor>> mExtensions = new LinkedHashMap<String,List<HandlerDescriptor>>();

    private Object mLock = new Object();

    private HelpBrowserLocalFileHandler()
    {
        super();
        final IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        loadExtensions(tracker);
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
        final BundleContext bundleContext = EclipseNSISPlugin.getDefault().getBundleContext();
        bundleContext.addBundleListener(new BundleListener() {
            public void bundleChanged(BundleEvent event)
            {
                if(event.getType() == BundleEvent.STOPPED ) {
                    bundleContext.removeBundleListener(this);
                }
                tracker.unregisterHandler(HelpBrowserLocalFileHandler.this);
            }
        });
    }

    public boolean handle(File file)
    {
        synchronized (mLock) {
            String ext = IOUtility.getFileExtension(file);
            if (!Common.isEmpty(ext)) {
                for (Iterator<String> iter = mExtensions.keySet().iterator(); iter.hasNext();) {
                    String extensionId = iter.next();
                    IExtension extension = getExtensionPointFilter().getExtension(extensionId);
                    if (extension == null) {
                        iter.remove();
                    }
                    else {
                        List<HandlerDescriptor> handlers = mExtensions.get(extensionId);
                        for (Iterator<HandlerDescriptor> iterator = handlers.iterator(); iterator.hasNext();) {
                            HandlerDescriptor desc = iterator.next();
                            if (desc.getExtensions().contains(ext)) {
                                try {
                                    return desc.getHandler().handle(file);
                                }
                                catch (Throwable e) {
                                    EclipseNSISPlugin.getDefault().log(e);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    private IExtensionPoint getExtensionPointFilter()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(INSISConstants.PLUGIN_ID,EXTENSION_POINT);
    }

    private void loadExtensions(IExtensionTracker tracker)
    {
        IExtensionPoint point = getExtensionPointFilter();
        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                addExtension(tracker, extensions[i]);
            }
        }
    }

    public void addExtension(IExtensionTracker tracker, IExtension extension)
    {
        synchronized (mLock) {
            if (!mExtensions.containsKey(extension.getUniqueIdentifier())) {
                IConfigurationElement[] elements = extension.getConfigurationElements();
                List<HandlerDescriptor> handlers = new ArrayList<HandlerDescriptor>();
                for (int i = 0; i < elements.length; i++) {
                    if (HANDLER.equals(elements[i].getName())) {
                        try {
                            HandlerDescriptor descriptor = new HandlerDescriptor(elements[i]);
                            tracker.registerObject(extension, descriptor, IExtensionTracker.REF_WEAK);
                            handlers.add(descriptor);
                        }
                        catch (Exception e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                }
                mExtensions.put(extension.getUniqueIdentifier(), handlers);
            }
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        synchronized (mLock) {
            if (mExtensions.containsKey(extension.getUniqueIdentifier())) {
                mExtensions.remove(extension.getUniqueIdentifier());
            }
        }
    }

    private class HandlerDescriptor
    {
        private IConfigurationElement mElement;

        private Set<String> mHandlerExtensions = new CaseInsensitiveSet();
        private IHelpBrowserLocalFileHandler mHandler = null;

        private HandlerDescriptor(IConfigurationElement element)
        {
            super();
            mHandlerExtensions.addAll(Common.tokenizeToList(element.getAttribute(HANDLER_EXTENSIONS), ','));
            mElement = element;
        }

        public Set<String> getExtensions()
        {
            return mHandlerExtensions;
        }

        public IHelpBrowserLocalFileHandler getHandler()
        {
            if(mHandler == null) {
                try {
                    mHandler = (IHelpBrowserLocalFileHandler)mElement.createExecutableExtension(HANDLER_CLASS);
                }
                catch (CoreException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    mHandler = NULL_HANDLER;
                    mHandlerExtensions.clear();
                }
            }
            return mHandler;
        }
    }
}
