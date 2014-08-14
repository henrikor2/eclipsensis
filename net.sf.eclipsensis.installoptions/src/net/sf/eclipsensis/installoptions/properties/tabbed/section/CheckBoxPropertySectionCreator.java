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


import java.util.*;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class CheckBoxPropertySectionCreator extends UneditableElementPropertySectionCreator
{
    public CheckBoxPropertySectionCreator(InstallOptionsCheckBox checkbox)
    {
        super(checkbox);
    }

    @Override
    protected Control createAppearancePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite composite = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        InstallOptionsCheckBox checkbox = (InstallOptionsCheckBox)getWidget();
        Integer[] stateData = checkbox.getStateData();
        String[] stateDisplay = checkbox.getStateDisplay();
        Map<Integer,String> map = new LinkedHashMap<Integer,String>();
        for (int i = 0; i < Math.min(stateData.length,stateDisplay.length); i++) {
            map.put(stateData[i], stateDisplay[i]);
        }
        Integer defaultValue = null;
        if(checkbox.getStateDefault() < map.size()) {
            defaultValue = stateData[checkbox.getStateDefault()];
        }
        createComboSection(composite, InstallOptionsModel.PROPERTY_STATE, map, defaultValue, widgetFactory, commandHelper);
        return composite;
    }
}
