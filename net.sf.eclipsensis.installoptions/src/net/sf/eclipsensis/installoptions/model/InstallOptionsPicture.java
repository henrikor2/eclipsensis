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

import java.util.Collection;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;

import org.eclipse.swt.graphics.*;

public abstract class InstallOptionsPicture extends InstallOptionsUneditableElement
{
    /**
     *
     */
    private static final long serialVersionUID = 2617864864963263131L;
    public static final String PROPERTY_IMAGE = "Image"; //$NON-NLS-1$

    protected InstallOptionsPicture(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("SWTImageType"); //$NON-NLS-1$
        skippedProperties.add("fileExtension"); //$NON-NLS-1$
        skippedProperties.add("image"); //$NON-NLS-1$
    }

    @Override
    protected String getDefaultText()
    {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected Position getDefaultPosition()
    {
        Rectangle rect = getImage().getBounds();
        return new Position(0,0,rect.width-1,rect.height-1);
    }

    @Override
    public Object getPropertyValue(Object propName)
    {
        if(PROPERTY_IMAGE.equals(propName)) {
            return getImage();
        }
        else {
            return super.getPropertyValue(propName);
        }
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new PicturePropertySectionCreator(this);
    }

    public abstract int getSWTImageType();
    public abstract String getFileExtension();
    public abstract Image getImage();
}
