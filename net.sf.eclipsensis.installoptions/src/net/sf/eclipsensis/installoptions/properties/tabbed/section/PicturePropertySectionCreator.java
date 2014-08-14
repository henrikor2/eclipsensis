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

import net.sf.eclipsensis.installoptions.model.InstallOptionsPicture;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class PicturePropertySectionCreator extends UneditableElementPropertySectionCreator
{
    public PicturePropertySectionCreator(InstallOptionsPicture picture)
    {
        super(picture);
    }

    @Override
    protected Control createAppearancePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        return null;
    }

    @Override
    protected boolean shouldCreateAppearancePropertySection()
    {
        return false;
    }

    @Override
    protected Control createOtherPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        return super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
    }

    @Override
    protected boolean shouldCreateOtherPropertySection()
    {
        return true;
    }

}
