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

public class UneditableElementPropertySectionCreator extends WidgetPropertySectionCreator
{
    public UneditableElementPropertySectionCreator(InstallOptionsWidget widget)
    {
        super(widget);
    }

    @Override
    protected Control createAppearancePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        Composite composite = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        createTextSection(composite, InstallOptionsModel.PROPERTY_TEXT, widgetFactory, commandHelper, isTextPropertyMultiline());
        return composite;
    }

    @Override
    protected boolean shouldCreateAppearancePropertySection()
    {
        return true;
    }

    protected boolean isTextPropertyMultiline()
    {
        return false;
    }
}
