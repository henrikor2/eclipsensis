/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 ******************************************************************************/
package net.sf.eclipsensis.util;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.winapi.WinAPI.HKEY;

import org.eclipse.swt.graphics.Image;

public class RegistryRoot extends RegistryKey
{
    private static final Image REGROOT_IMAGE;
    static final HKEY[] ROOT_KEYS = {HKEY.HKEY_CLASSES_ROOT, HKEY.HKEY_CURRENT_USER,
        HKEY.HKEY_LOCAL_MACHINE, HKEY.HKEY_USERS, HKEY.HKEY_CURRENT_CONFIG};

    static {
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        REGROOT_IMAGE = imageManager.getImage(EclipseNSISPlugin.getResourceString("registry.root.image")); //$NON-NLS-1$
    }

    public static String getRootKeyName(HKEY rootKey)
    {
        return rootKey.name();
    }

    public static HKEY getRootKey(String rootKey)
    {
        if (rootKey.equalsIgnoreCase("HKEY_CLASSES_ROOT") || rootKey.equalsIgnoreCase("HKCR")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_CLASSES_ROOT;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_CURRENT_CONFIG") || rootKey.equalsIgnoreCase("HKCC")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_CURRENT_CONFIG;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_CURRENT_USER") || rootKey.equalsIgnoreCase("HKCU")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_CURRENT_USER;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_DYN_DATA") || rootKey.equalsIgnoreCase("HKDD")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_DYN_DATA;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_LOCAL_MACHINE") || rootKey.equalsIgnoreCase("HKLM")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_LOCAL_MACHINE;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_PERFORMANCE_DATA") || rootKey.equalsIgnoreCase("HKPD")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_PERFORMANCE_DATA;
        }
        else if (rootKey.equalsIgnoreCase("HKEY_USERS") || rootKey.equalsIgnoreCase("HKU")) { //$NON-NLS-1$ //$NON-NLS-2$
            return HKEY.HKEY_USERS;
        }
        else {
            return null;
        }
    }

    public RegistryRoot()
    {
        super(null, null,null);
        setName(Common.getMyComputerLabel());
        mChildren = new RegistryKey[ROOT_KEYS.length];
        for (int i = 0; i < mChildren.length; i++) {
            mChildren[i] = new RegistryKey(this, ROOT_KEYS[i].getHandle(), getRootKeyName(ROOT_KEYS[i]));
        }
        mChildCount = mChildren.length;
    }

    @Override
    protected void expandName(StringBuffer buf)
    {
    }

    @Override
    public Image getImage()
    {
        return REGROOT_IMAGE;
    }

    @Override
    public Image getExpandedImage()
    {
        return REGROOT_IMAGE;
    }
}
