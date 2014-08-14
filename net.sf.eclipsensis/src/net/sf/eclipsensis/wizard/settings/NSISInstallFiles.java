/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISInstallFilesDialog;
import net.sf.eclipsensis.wizard.util.NSISWizardUtil;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.FileDialog;

public class NSISInstallFiles extends AbstractNSISInstallGroup implements INSISInstallFileSystemObject
{
    public static class FileItem extends AbstractNSISInstallItem
    {
        private static final long serialVersionUID = 3744853352840436396L;

        public static final String FILEITEM_TYPE = "File Item"; //$NON-NLS-1$

        private static final Image FILEITEM_IMAGE = EclipseNSISPlugin.getImageManager().getImage(
                EclipseNSISPlugin.getResourceString("wizard.file.icon")); //$NON-NLS-1$

        static
        {
            NSISInstallElementFactory.register(FILEITEM_TYPE,
                    EclipseNSISPlugin.getResourceString("wizard.fileitem.type.name"), FILEITEM_IMAGE, FileItem.class); //$NON-NLS-1$
        }

        private String mName = null;

        @Override
        public String doValidate()
        {
            if (!IOUtility.isValidFile(IOUtility.decodePath(getName())))
            {
                return EclipseNSISPlugin.getResourceString("wizard.invalid.file.name.error"); //$NON-NLS-1$
            }
            else
            {
                return super.doValidate();
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * net.sf.eclipsensis.wizard.settings.INSISInstallElement#edit(org.eclipse
         * .swt.widgets.Composite)
         */
        public boolean edit(NSISWizard wizard)
        {
            FileDialog dialog = new FileDialog(wizard.getShell(), SWT.OPEN | SWT.PRIMARY_MODAL);
            ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
            dialog.setText(EclipseNSISPlugin.getResourceString("wizard.fileitem.dialog.title")); //$NON-NLS-1$
            dialog.setFilterNames(Common.loadArrayProperty(bundle, "wizard.source.file.filternames")); //$NON-NLS-1$
            dialog.setFilterExtensions(Common.loadArrayProperty(bundle, "wizard.source.file.filters")); //$NON-NLS-1$
            if (!Common.isEmpty(mName))
            {
                dialog.setFileName(mName);
            }
            String newFilename = dialog.open();
            if (newFilename != null && !newFilename.equalsIgnoreCase(mName))
            {
                if (getParent() != null)
                {
                    FileItem fi = new FileItem();
                    fi.setName(newFilename);
                    if (!getParent().canAddChild(fi))
                    {
                        Common
                                .openError(
                                        wizard.getShell(),
                                        EclipseNSISPlugin
                                                .getFormattedString(
                                                        "duplicate.child.error", new Object[] { getParent().getDisplayName(), fi.getDisplayName() }), //$NON-NLS-1$
                                        EclipseNSISPlugin.getShellImage());
                        return false;
                    }
                }
                mName = newFilename;
                return true;
            }
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof FileItem)
            {
                return ((FileItem) obj).mName.equalsIgnoreCase(mName);
            }
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName
         * ()
         */
        public String getDisplayName()
        {
            return getName();
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
         */
        public Image getImage()
        {
            return FILEITEM_IMAGE;
        }

        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return mName;
        }

        /*
         * (non-Javadoc)
         *
         * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
         */
        public String getType()
        {
            return FILEITEM_TYPE;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return mName.hashCode();
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
         */
        public boolean isEditable()
        {
            return true;
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
    }

    private static final long serialVersionUID = 1293912008528238512L;

    public static final String TYPE = "File Set"; //$NON-NLS-1$

    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(
            EclipseNSISPlugin.getResourceString("wizard.files.icon")); //$NON-NLS-1$

    public static final char SEPARATOR = '\0';
    static
    {
        NSISInstallElementFactory.register(TYPE,
                EclipseNSISPlugin.getResourceString("wizard.files.type.name"), IMAGE, NSISInstallFiles.class); //$NON-NLS-1$
        // Let's register File Item as well
        FileItem.class.getName();
    }

    private String mDestination = NSISKeywords.getInstance().getKeyword("$INSTDIR"); //$NON-NLS-1$

    private int mOverwriteMode = OVERWRITE_ON;

    private boolean mNonFatal = false;

    private boolean mPreserveAttributes = false;

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("files"); //$NON-NLS-1$
    }

    @Override
    public String doValidate()
    {
        if (!NSISWizardUtil.isValidNSISPathName(getSettings().getTargetPlatform(), getDestination()))
        {
            return EclipseNSISPlugin.getResourceString("wizard.invalid.fileset.destination.error"); //$NON-NLS-1$
        }
        else
        {
            return super.doValidate();
        }
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISInstallFilesDialog(wizard, this).open() == Window.OK;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final NSISInstallFiles other = (NSISInstallFiles) obj;
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
        if (mOverwriteMode != other.mOverwriteMode)
        {
            return false;
        }
        return true;
    }

    /**
     * @return Returns the destination.
     */
    public String getDestination()
    {
        return mDestination;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return TYPE;
    }

    /**
     * @return Returns the filenames.
     */
    public String getFiles()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if (getChildren().length > 0)
        {
            Iterator<INSISInstallElement> iter = getChildrenIterator();
            buf.append(((FileItem) iter.next()).getName());
            while (iter.hasNext())
            {
                buf.append(SEPARATOR).append(((FileItem) iter.next()).getName());
            }
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
    }

    /**
     * @return Returns the overwriteMode.
     */
    public int getOverwriteMode()
    {
        return mOverwriteMode;
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

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + (mDestination == null ? 0 : mDestination.hashCode());
        result = PRIME * result + mOverwriteMode;
        return result;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes
     * ()
     */
    @Override
    public void setChildTypes()
    {
        clearChildTypes();
        addChildType(FileItem.FILEITEM_TYPE);
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

    /**
     * @param filenames
     *            The filenames to set.
     */
    public void setFiles(String filenames)
    {
        String[] files = Common.tokenize(filenames, SEPARATOR);
        CaseInsensitiveSet newFiles = new CaseInsensitiveSet(Arrays.asList(files));
        for (Iterator<INSISInstallElement> iter = getChildrenIterator(); iter.hasNext();)
        {
            FileItem item = (FileItem) iter.next();
            if (!newFiles.contains(item.getName()))
            {
                iter.remove();
                setDirty();
            }
            else
            {
                newFiles.remove(item.getName());
            }
        }
        for (Iterator<String> iter = newFiles.iterator(); iter.hasNext();)
        {
            FileItem fi = new FileItem();
            fi.setName(iter.next());
            addChild(fi);
            setDirty();
        }
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

    @Override
    public void setTargetPlatform(int targetPlatform)
    {
        super.setTargetPlatform(targetPlatform);
        setDestination(NSISWizardUtil.convertPath(targetPlatform, getDestination()));
    }

}
