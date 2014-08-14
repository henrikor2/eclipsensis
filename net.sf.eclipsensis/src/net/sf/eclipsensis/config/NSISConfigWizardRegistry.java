/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.config;

import java.util.*;

import net.sf.eclipsensis.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.*;

public class NSISConfigWizardRegistry implements IExtensionChangeHandler
{
    private static final String EXTENSION_POINT = "nsisConfigWizard"; //$NON-NLS-1$
    private static final String WIZARD = "wizard"; //$NON-NLS-1$

    public static final NSISConfigWizardRegistry INSTANCE = new NSISConfigWizardRegistry();

    private Map<String, List<NSISConfigWizardDescriptor>> mExtensions = new LinkedHashMap<String, List<NSISConfigWizardDescriptor>>();
    private Object mLock = new Object();

    private NSISConfigWizardRegistry()
    {
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
                tracker.unregisterHandler(NSISConfigWizardRegistry.this);
            }
        });
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
                List<NSISConfigWizardDescriptor> wizards = new ArrayList<NSISConfigWizardDescriptor>();
                for (int i = 0; i < elements.length; i++) {
                    if (WIZARD.equals(elements[i].getName())) {
                        try {
                            NSISConfigWizardDescriptor descriptor = new NSISConfigWizardDescriptor(elements[i]);
                            tracker.registerObject(extension, descriptor, IExtensionTracker.REF_WEAK);
                            wizards.add(descriptor);
                        }
                        catch (Exception e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                }
                mExtensions.put(extension.getUniqueIdentifier(), wizards);
            }
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        synchronized (mLock) {
            if (mExtensions.containsKey(extension.getUniqueIdentifier())) {
                mExtensions.remove(extension.getUniqueIdentifier());
                PlatformUI.getWorkbench().getExtensionTracker().unregisterObject(extension);
            }
        }
    }

    private IExtensionPoint getExtensionPointFilter()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(INSISConstants.PLUGIN_ID,EXTENSION_POINT);
    }

    public NSISConfigWizardDescriptor[] getWizardDescriptors()
    {
        synchronized (mLock) {
            List<NSISConfigWizardDescriptor> descriptors = new ArrayList<NSISConfigWizardDescriptor>();
            for (Iterator<String> iter = mExtensions.keySet().iterator(); iter.hasNext();) {
                String extensionId = iter.next();
                IExtension extension = getExtensionPointFilter().getExtension(extensionId);
                if (extension == null) {
                    iter.remove();
                }
                else {
                    descriptors.addAll(mExtensions.get(extensionId));
                }
            }
            return descriptors.toArray(new NSISConfigWizardDescriptor[descriptors.size()]);
        }
    }
}
