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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.*;

public class DialogPropertySection extends InstallOptionsElementPropertySection
{
    @Override
    protected Control createSection(InstallOptionsElement element, Composite parent, TabbedPropertySheetPage page, InstallOptionsCommandHelper commandHelper)
    {
        if(element instanceof InstallOptionsDialog) {
            return new DialogPropertySectionCreator((InstallOptionsDialog)element).createPropertySection(parent, getWidgetFactory(), commandHelper);
        }
        return null;
    }

    private class DialogPropertySectionCreator extends PropertySectionCreator
    {
        public DialogPropertySectionCreator(InstallOptionsDialog dialog)
        {
            super(dialog);
        }

        public Control createPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
        {
            Group group = widgetFactory.createGroup(parent, InstallOptionsPlugin.getResourceString("appearance.section.label")); //$NON-NLS-1$
            group.setLayout(new GridLayout(2,false));
            group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

            createTextSection(group, InstallOptionsModel.PROPERTY_TITLE, widgetFactory, commandHelper);
            Map<Integer,String> map = new LinkedHashMap<Integer,String>();
            for (int i = 0; i < Math.min(InstallOptionsDialog.OPTION_DATA.length, InstallOptionsDialog.OPTION_DISPLAY.length); i++) {
                map.put(InstallOptionsDialog.OPTION_DATA[i],InstallOptionsDialog.OPTION_DISPLAY[i]);
            }
            Integer defaultValue = InstallOptionsDialog.OPTION_DATA[InstallOptionsDialog.DEFAULT_OPTION];

            createComboSection(group, InstallOptionsModel.PROPERTY_CANCEL_ENABLED, map, defaultValue, widgetFactory, commandHelper);
            createComboSection(group, InstallOptionsModel.PROPERTY_CANCEL_SHOW, map, defaultValue, widgetFactory, commandHelper);
            createComboSection(group, InstallOptionsModel.PROPERTY_BACK_ENABLED, map, defaultValue, widgetFactory, commandHelper);
            createTextSection(group, InstallOptionsModel.PROPERTY_CANCEL_BUTTON_TEXT, widgetFactory, commandHelper);
            createTextSection(group, InstallOptionsModel.PROPERTY_NEXT_BUTTON_TEXT, widgetFactory, commandHelper);
            createTextSection(group, InstallOptionsModel.PROPERTY_BACK_BUTTON_TEXT, widgetFactory, commandHelper);
            Text text = createTextSection(group, InstallOptionsModel.PROPERTY_RECT, widgetFactory, commandHelper);
            if(text != null) {
                text.addVerifyListener(getNumberVerifyListener());
            }
            createComboSection(group, InstallOptionsModel.PROPERTY_RTL, map, defaultValue, widgetFactory, commandHelper);
            return group;
        }
    }
}
