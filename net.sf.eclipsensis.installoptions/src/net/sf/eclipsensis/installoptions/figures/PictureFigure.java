/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import net.sf.eclipsensis.installoptions.model.InstallOptionsPicture;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class PictureFigure extends UneditableElementFigure
{
    private Image mImage;

    public PictureFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
        setBorder(new DashedLineBorder());
    }

    @Override
    protected void init(IPropertySource propertySource)
    {
        setImage((Image)propertySource.getPropertyValue(InstallOptionsPicture.PROPERTY_IMAGE));
        super.init(propertySource);
    }

    @Override
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Label label = new Label(parent, style);
        Image image = getImage();
        if(image != null) {
            label.setImage(image);
        }
        return label;
    }

    @Override
    public int getDefaultStyle()
    {
        return SWT.CENTER;
    }

    public Image getImage()
    {
        return mImage;
    }

    public void setImage(Image image)
    {
        mImage = image;
    }
}
