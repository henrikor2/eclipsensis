/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import net.sf.eclipsensis.dialogs.NSISConfigWizardDialog;
import net.sf.eclipsensis.util.Common;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;

public class NSISLiveHelpAction implements ILiveHelpAction
{
    private String mData = null;

    /* (non-Javadoc)
     * @see org.eclipse.help.ILiveHelpAction#setInitializationString(java.lang.String)
     */
    public void setInitializationString(String data)
    {
        mData = data;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                if(NSISHelpProducer.CONFIGURE.equals(mData)) {
                    new NSISConfigWizardDialog(Display.getDefault().getActiveShell()).open();
                }
                else if(!Common.isEmpty(mData)) {
                    String chmURL = NSISHelpURLProvider.getInstance().convertHelpURLToCHMHelpURL(mData);
                    if(!Common.isEmpty(chmURL)) {
                        NSISHelpURLProvider.getInstance().openCHMHelpURL(chmURL);
                    }
                }
            }
        });
    }
}
