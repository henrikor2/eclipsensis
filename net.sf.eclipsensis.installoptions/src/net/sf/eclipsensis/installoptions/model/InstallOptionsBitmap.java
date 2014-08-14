/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.ini.INISection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class InstallOptionsBitmap extends InstallOptionsPicture
{
    private static final long serialVersionUID = 1L;

    public static final Image BITMAP_IMAGE = InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("bitmap.image")); //$NON-NLS-1$

    protected InstallOptionsBitmap(INISection section)
    {
        super(section);
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_BITMAP;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.model.InstallOptionsPicture#getImageName()
     */
    @Override
    public Image getImage()
    {
        return BITMAP_IMAGE;
    }

    @Override
    public String getFileExtension()
    {
        return ".bmp"; //$NON-NLS-1$
    }

    @Override
    public int getSWTImageType()
    {
        return SWT.IMAGE_BMP;
    }
}
