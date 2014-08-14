/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed;

import net.sf.eclipsensis.installoptions.editor.InstallOptionsDesignEditor;

import org.eclipse.ui.views.properties.tabbed.*;

public class CustomTabbedPropertySheetPage extends TabbedPropertySheetPage
{
    private InstallOptionsDesignEditor mEditor;

    public CustomTabbedPropertySheetPage(ITabbedPropertySheetPageContributor contributor)
    {
        super(contributor);
        if(contributor instanceof InstallOptionsDesignEditor) {
            mEditor = (InstallOptionsDesignEditor)contributor;
        }
    }

    public InstallOptionsDesignEditor getEditor()
    {
        return mEditor;
    }
}
