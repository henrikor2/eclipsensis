/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.picture;

import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.label.InstallOptionsLabelEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.UneditableElementDirectEditPolicy;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsPicture;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsPictureEditPart extends InstallOptionsLabelEditPart
{
    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        if(cIsNT) {
            //This is a hack because Windows NT Labels don't seem to respond to the
            //WM_PRINT message (see SWTControl.getImage(Control)
             return new NTPictureFigure(getInstallOptionsWidget());
        }
        else {
            return new PictureFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
    }

    @Override
    protected UneditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return null;
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator)
    {
        return null;
    }

    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return null;
    }

    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    private class NTPictureFigure extends NTFigure
    {
        protected ImageFigure mImageFigure;
        protected Image mImage;

        public NTPictureFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }

        @Override
        protected void createChildFigures()
        {
            mImageFigure = new ImageFigure();
            mImageFigure.setBorder(new DashedLineBorder());
            add(mImageFigure);
        }

        @Override
        protected void init(IPropertySource propertySource)
        {
            super.init(propertySource);
            setImage((Image)propertySource.getPropertyValue(InstallOptionsPicture.PROPERTY_IMAGE));
        }

        @Override
        protected void setChildConstraints(Rectangle rect)
        {
            setConstraint(mImageFigure, new Rectangle(0,0,rect.width,rect.height));
        }

        protected void setImage(Image image)
        {
            if(mImage != image) {
                mImage = image;
                refresh();
            }
        }

        @Override
        public void refresh()
        {
            super.refresh();
            mImageFigure.setImage(mImage);
        }
    }
}
