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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.*;

public abstract class NSISInstallRegistryItem extends AbstractNSISInstallItem
{
    /**
     *
     */
    private static final long serialVersionUID = 6851931721096996387L;
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        makeDisplayName(buf);
        return buf.toString();
    }

    protected void makeDisplayName(StringBuffer buf)
    {
        int rootKey = getRootKey();
        if(rootKey >= 0) {
            buf.append(NSISWizardDisplayValues.HKEY_NAMES[rootKey]);
        }
        buf.append("\\").append(getSubKey()).toString(); //$NON-NLS-1$
    }

    /**
     * @return Returns the rootKey.
     */
    public int getRootKey()
    {
        int rootKey = getRootKeyInternal();
        if(rootKey >= NSISWizardDisplayValues.HKEY_NAMES.length) {
            rootKey = INSISWizardConstants.HKCU;
            setRootKeyInternal(rootKey);
        }
        return rootKey;
    }

    /**
     * @param rootKey The rootKey to set.
     */
    public void setRootKey(int rootKey)
    {
        int rootKey2 = rootKey;
        if(rootKey2 >= NSISWizardDisplayValues.HKEY_NAMES.length) {
            rootKey2 = INSISWizardConstants.HKCU;
        }
        setRootKeyInternal(rootKey2);
    }

    /**
     * @return Returns the subKey.
     */
    public String getSubKey()
    {
        return getSubKeyInternal();
    }

    /**
     * @param name The subKey to set.
     */
    public void setSubKey(String subKey)
    {
        setSubKeyInternal(subKey);
    }

    @Override
    public String doValidate()
    {
        if(getRootKey() < 0) {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.root.key.error"); //$NON-NLS-1$
        }
        else {
            String subKey = Common.trim(getSubKey());
            if(Common.isEmpty(subKey) || subKey.endsWith("\\") || subKey.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                return EclipseNSISPlugin.getResourceString("wizard.invalid.sub.key.error"); //$NON-NLS-1$
            }
            return super.doValidate();
        }
    }

    //Stupid hacks because serialization doesn't like it when a super class gets abstracted out
    //from an existing class.
    protected abstract int getRootKeyInternal();
    protected abstract void setRootKeyInternal(int rootKey);
    protected abstract String getSubKeyInternal();
    protected abstract void setSubKeyInternal(String subKey);
}
