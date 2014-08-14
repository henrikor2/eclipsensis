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

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractNSISWizardStartPage extends AbstractNSISWizardPage
{
    private static final Image cShellImage = EclipseNSISPlugin.getShellImage();

    private Image mOldShellImage = null;
    private Image[] mOldShellImages = null;

    public AbstractNSISWizardStartPage(String pageName, String title, String description)
    {
        super(pageName, title, description);
        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                Shell shell = getWizard().getContainer().getShell();
                if(isCurrentPage()) {
                    Image image = shell.getImage();
                    if(image != cShellImage) {
                        mOldShellImage = image;
                        mOldShellImages = shell.getImages();
                        shell.setImage(cShellImage);
                    }
                }
                else if(isPreviousPage()) {
                    if(!(getSelectedPage() instanceof AbstractNSISWizardPage)) {
                        shell.setImage(mOldShellImage);
                        if(mOldShellImages != null) {
                            shell.setImages(mOldShellImages);
                        }
                        mOldShellImage = null;
                        mOldShellImages = null;
                    }
                }
            }
        });
    }
}
