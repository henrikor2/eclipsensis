/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallDirectoryDialog;
import net.sf.eclipsensis.wizard.util.NSISWizardUtil;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

public class NSISInstallDirectory extends AbstractNSISInstallItem implements INSISInstallFileSystemObject
{
    private static final long serialVersionUID = 3960745695250401464L;

    public static final String TYPE = "Folder"; //$NON-NLS-1$
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(
            EclipseNSISPlugin.getResourceString("wizard.directory.icon")); //$NON-NLS-1$
    private static final Image RECURSIVE_IMAGE = EclipseNSISPlugin.getImageManager().getImage(
            EclipseNSISPlugin.getResourceString("wizard.recursive.directory.icon")); //$NON-NLS-1$

    private String mName = null;
    private String mDestination = NSISKeywords.getInstance().getKeyword("$INSTDIR"); //$NON-NLS-1$
    private int mOverwriteMode = OVERWRITE_ON;
    private boolean mNonFatal = false;
    private boolean mPreserveAttributes = false;

    private boolean mRecursive = false;

    static
    {
        NSISInstallElementFactory.register(TYPE,
                EclipseNSISPlugin.getResourceString("wizard.directory.type.name"), IMAGE, NSISInstallDirectory.class); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return mName;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallDirectoryDialog(wizard, this).open() == Window.OK;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return mRecursive ? RECURSIVE_IMAGE : IMAGE;
    }

    /**
     * @return Returns the destination.
     */
    public String getDestination()
    {
        return mDestination;
    }

    /**
     * @param destination
     *            The destination to set.
     */
    public void setDestination(String destination)
    {
        if (!Common.stringsAreEqual(mDestination, destination))
        {
            setDirty();
            mDestination = destination;
        }
    }

    public boolean getNonFatal()
    {
        return mNonFatal;
    }

    public void setNonFatal(boolean nonFatal)
    {
        if (mNonFatal != nonFatal)
        {
            setDirty();
            mNonFatal = nonFatal;
        }
    }

    public boolean getPreserveAttributes()
    {
        return mPreserveAttributes;
    }

    public void setPreserveAttributes(boolean preserveAttributes)
    {
        if (mPreserveAttributes != preserveAttributes)
        {
            setDirty();
            mPreserveAttributes = preserveAttributes;
        }
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name)
    {
        if (!Common.stringsAreEqual(mName, name))
        {
            setDirty();
            mName = name;
        }
    }

    /**
     * @return Returns the overwriteMode.
     */
    public int getOverwriteMode()
    {
        return mOverwriteMode;
    }

    /**
     * @param overwriteMode
     *            The overwriteMode to set.
     */
    public void setOverwriteMode(int overwriteMode)
    {
        if (mOverwriteMode != overwriteMode)
        {
            setDirty();
            mOverwriteMode = overwriteMode;
        }
    }

    /**
     * @return Returns the recursive.
     */
    public boolean isRecursive()
    {
        return mRecursive;
    }

    /**
     * @param recursive
     *            The recursive to set.
     */
    public void setRecursive(boolean recursive)
    {
        if (mRecursive != recursive)
        {
            setDirty();
            mRecursive = recursive;
        }
    }

    @Override
    public String doValidate()
    {
        if (!IOUtility.isValidPath(IOUtility.decodePath(getName())))
        {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.directory.name.error"); //$NON-NLS-1$
        }
        else if (!NSISWizardUtil.isValidNSISPathName(getSettings().getTargetPlatform(), getDestination()))
        {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.directory.destination.error"); //$NON-NLS-1$
        }
        else
        {
            return super.doValidate();
        }
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (mDestination == null ? 0 : mDestination.hashCode());
        result = PRIME * result + (mName == null ? 0 : mName.hashCode());
        result = PRIME * result + mOverwriteMode;
        result = PRIME * result + (mRecursive ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final NSISInstallDirectory other = (NSISInstallDirectory) obj;
        if (mDestination == null)
        {
            if (other.mDestination != null)
            {
                return false;
            }
        }
        else if (!mDestination.equals(other.mDestination))
        {
            return false;
        }
        if (mName == null)
        {
            if (other.mName != null)
            {
                return false;
            }
        }
        else if (!mName.equals(other.mName))
        {
            return false;
        }
        if (mOverwriteMode != other.mOverwriteMode)
        {
            return false;
        }
        if (mRecursive != other.mRecursive)
        {
            return false;
        }
        return true;
    }

    @Override
    public void setTargetPlatform(int targetPlatform)
    {
        super.setTargetPlatform(targetPlatform);
        setDestination(NSISWizardUtil.convertPath(targetPlatform, getDestination()));
    }
}
