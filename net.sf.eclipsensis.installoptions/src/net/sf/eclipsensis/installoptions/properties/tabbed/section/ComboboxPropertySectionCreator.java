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

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class ComboboxPropertySectionCreator extends ListItemsPropertySectionCreator
{
    public ComboboxPropertySectionCreator(InstallOptionsCombobox element)
    {
        super(element);
    }

    @Override
    protected CheckboxTableViewer createListItemsAndStateSection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        createTextSection(parent, InstallOptionsModel.PROPERTY_STATE, widgetFactory, commandHelper);
        return super.createListItemsAndStateSection(parent, widgetFactory, commandHelper);
    }

}
