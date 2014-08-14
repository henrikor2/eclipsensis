/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class DirRequestPropertySectionCreator extends PathRequestPropertySectionCreator
{
    public DirRequestPropertySectionCreator(InstallOptionsDirRequest element)
    {
        super(element);
    }

    @Override
    protected Control createOtherPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite parent2 = (Composite)super.createOtherPropertySection(parent, widgetFactory, commandHelper);
        Text text = createTextSection(parent2, InstallOptionsModel.PROPERTY_TEXT, widgetFactory, commandHelper);
        //Move it to top
        if(text.getParent().equals(parent2)) {
            text.moveAbove(null);
            Object o = text.getData(LABEL);
            if(o != null && o instanceof Control) {
                ((Control)o).moveAbove(text);
            }
        }
        else {
            Control c = text;
            while(!c.getParent().equals(parent2)) {
                c = c.getParent();
            }
            c.moveAbove(null);
        }
        createTextSection(parent2, InstallOptionsModel.PROPERTY_ROOT, widgetFactory, commandHelper);
        return parent2;
    }
}
