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
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallRegistryKeyDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallRegistryKey extends NSISInstallRegistryItem
{
    private static final long serialVersionUID = 1525071202238497310L;

    public static final String TYPE = "Registry Key"; //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.regkey.icon")); //$NON-NLS-1$

    private int mRootKey = HKLM;
    private String mSubKey = null;

    static {
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.regkey.type.name"), IMAGE, NSISInstallRegistryKey.class); //$NON-NLS-1$
    }

    @Override
    protected int getRootKeyInternal()
    {
        return mRootKey;
    }

    @Override
    protected String getSubKeyInternal()
    {
        return mSubKey;
    }

    @Override
    protected void setRootKeyInternal(int rootKey)
    {
        if(mRootKey != rootKey) {
            setDirty();
            mRootKey = rootKey;
        }
    }

    @Override
    protected void setSubKeyInternal(String subKey)
    {
        if(!Common.stringsAreEqual(mSubKey, subKey)) {
            setDirty();
            mSubKey = subKey;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    public Image getImage()
    {
        return IMAGE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallRegistryKeyDialog(wizard,this).open() == Window.OK;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + mRootKey;
        result = PRIME * result + ((mSubKey == null)?0:mSubKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NSISInstallRegistryKey other = (NSISInstallRegistryKey)obj;
        if (mRootKey != other.mRootKey) {
            return false;
        }
        if (mSubKey == null) {
            if (other.mSubKey != null) {
                return false;
            }
        }
        else if (!mSubKey.equals(other.mSubKey)) {
            return false;
        }
        return true;
    }
}
