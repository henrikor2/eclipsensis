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

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

public class NSISInstallElementLabelProvider extends CellLabelProvider
{
    private static Image cErrorImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("error.decoration.icon")); //$NON-NLS-1$
    private static ImageData cErrorImageData = cErrorImage.getImageData();

    public NSISInstallElementLabelProvider()
    {
        super();
    }

    public NSISInstallElementLabelProvider(boolean withErrors)
    {
        this();
    }

    public Image getImage(Object element) {
        if(element instanceof INSISInstallElement) {
            Image image = ((INSISInstallElement)element).getImage();
            if(((INSISInstallElement)element).getError() != null) {
                image = decorateImage(image, (INSISInstallElement)element);
            }
            return image;
        }
        else {
            return null;
        }
    }

    public String getText(Object element) {
        if(element instanceof INSISInstallElement) {
            return ((INSISInstallElement)element).getDisplayName();
        }
        else {
            return Common.toString(element,""); //$NON-NLS-1$
        }
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        if(element instanceof INSISInstallElement) {
            return false;
        }
        else {
            return super.isLabelProperty(element, property);
        }
    }

    private Image decorateImage(final Image image, INSISInstallElement element)
    {
        String name = Integer.toString(image.hashCode())+"$error"; //$NON-NLS-1$
        Image image2 = EclipseNSISPlugin.getImageManager().getImage(name);
        if(image2 == null) {
            EclipseNSISPlugin.getImageManager().putImageDescriptor(name,
                    new CompositeImageDescriptor(){
                        @Override
                        protected void drawCompositeImage(int width, int height)
                        {
                            drawImage(image.getImageData(),0,0);
                            drawImage(cErrorImageData,0,getSize().y-cErrorImageData.height);
                        }

                        @Override
                        protected Point getSize()
                        {
                            return new Point(image.getBounds().width,image.getBounds().height);
                        }
                    });
            image2 = EclipseNSISPlugin.getImageManager().getImage(name);
        }
        return image2;
    }

    @Override
    public String getToolTipText(Object element)
    {
        return (element instanceof INSISInstallElement?((INSISInstallElement)element).getError():null);
    }

    @Override
    public Image getToolTipImage(Object element)
    {
        return (element instanceof INSISInstallElement?(((INSISInstallElement)element).getError()!= null?cErrorImage:null):null);
    }

    @Override
    public boolean useNativeToolTip(Object object)
    {
        return false;
    }

    @Override
    public void update(ViewerCell cell)
    {
        cell.setImage(getImage(cell.getElement()));
        cell.setText(getText(cell.getElement()));
    }
}
