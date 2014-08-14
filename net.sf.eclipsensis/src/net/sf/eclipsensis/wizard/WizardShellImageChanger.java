/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class WizardShellImageChanger implements IPageChangedListener
{
    private IWizard mWizard;
    private Image mImage;

    private Image mOldImage;
    private Image[] mOldImages;

    public WizardShellImageChanger(IWizard wizard, Image image)
    {
        mWizard = wizard;
        mImage = image;
    }

    private boolean contains(IWizardPage[] pages, Object selectedPage)
    {
        for (int i = 0; i < pages.length; i++) {
            if(pages[i] != null && pages[i].equals(selectedPage)) {
                return true;
            }
        }
        return false;
    }
    public void pageChanged(PageChangedEvent event)
    {
        Shell shell = mWizard.getContainer().getShell();
        Image image = shell.getImage();
        if(contains(mWizard.getPages(), event.getSelectedPage())) {
            if(image != mImage) {
                mOldImage = image;
                mOldImages = shell.getImages();
                shell.setImage(mImage);
            }
        }
        else {
            if(image == mImage) {
                shell.setImage(mOldImage);
                if(mOldImages != null) {
                    shell.setImages(mOldImages);
                }
            }
        }
    }
}
