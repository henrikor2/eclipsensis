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

public class PathRequestPropertySectionCreator extends EditableElementPropertySectionCreator
{
    public PathRequestPropertySectionCreator(InstallOptionsPathRequest element)
    {
        super(element);
    }

    @Override
    protected Control createAppearancePropertySection(Composite parent, final TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        Composite parent2 = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        createTextSection(parent2, InstallOptionsModel.PROPERTY_STATE, widgetFactory, commandHelper);
        return parent2;
    }

    @Override
    protected boolean shouldCreateAppearancePropertySection()
    {
        return true;
    }

    @Override
    protected boolean shouldCreateOtherPropertySection()
    {
        return true;
    }
}
