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

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.wizard.NSISWizard;

import org.eclipse.swt.graphics.Image;

public class NSISInstaller extends AbstractNSISInstallGroup
{
    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.installer.icon")); //$NON-NLS-1$
    public static final String TYPE = "Installer"; //$NON-NLS-1$
    private String mFormat;

    private static final long serialVersionUID = 3601773736043608518L;

    static {
        NSISInstallElementFactory.register(TYPE, EclipseNSISPlugin.getResourceString("wizard.installer.type.name"), IMAGE, NSISInstaller.class); //$NON-NLS-1$
    }

    public NSISInstaller()
    {
        super();
        mFormat = EclipseNSISPlugin.getResourceString("wizard.installer.format"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes()
     */
    @Override
    public void setChildTypes()
    {
        clearChildTypes();
        addChildType(NSISSection.TYPE);
        addChildType(NSISSectionGroup.TYPE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return MessageFormat.format(mFormat,new Object[]{getSettings().getName()});
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isRemovable()
     */
    @Override
    public boolean isRemovable()
    {
        return false;
    }

    public boolean edit(NSISWizard wizard)
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((mFormat == null)?0:mFormat.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NSISInstaller other = (NSISInstaller)obj;
        if (mFormat == null) {
            if (other.mFormat != null) {
                return false;
            }
        }
        else if (!mFormat.equals(other.mFormat)) {
            return false;
        }
        return true;
    }
}