/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.*;

public class PreviewSubMenuManager extends MenuManager
{
    public PreviewSubMenuManager(PreviewRetargetAction[] actions)
    {
        super(InstallOptionsPlugin.getResourceString("preview.submenu.name")); //$NON-NLS-1$
        for (int i = 0; i < actions.length; i++) {
            add(actions[i]);
        }
    }

    @Override
    public void fill(Menu parent, int index)
    {
        super.fill(parent, index);
        MenuItem item = getMenu().getParentItem();
        if(item.getImage() == null) {
            item.setImage(InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("preview.icon"))); //$NON-NLS-1$
        }
    }
}
