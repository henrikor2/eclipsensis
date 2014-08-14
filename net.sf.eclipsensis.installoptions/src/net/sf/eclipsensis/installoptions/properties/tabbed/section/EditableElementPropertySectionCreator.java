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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class EditableElementPropertySectionCreator extends WidgetPropertySectionCreator
{
    public EditableElementPropertySectionCreator(InstallOptionsEditableElement element)
    {
        super(element);
    }

    @Override
    protected Control createOtherPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite parent2 = (Composite)super.createOtherPropertySection(parent, widgetFactory, commandHelper);
        Text text = createTextSection(parent2, InstallOptionsModel.PROPERTY_MINLEN, widgetFactory, commandHelper);
        if(text != null) {
            text.addVerifyListener(getNumberVerifyListener());
        }

        text = createTextSection(parent2, InstallOptionsModel.PROPERTY_MAXLEN, widgetFactory, commandHelper);
        if(text != null) {
            text.addVerifyListener(getNumberVerifyListener());
        }

        text = createTextSection(parent2, InstallOptionsModel.PROPERTY_VALIDATETEXT, widgetFactory, commandHelper, true);
        if(text != null) {
            GC gc = new GC(text);
            gc.setFont(JFaceResources.getDialogFont());
            FontMetrics fontMetrics = gc.getFontMetrics();
            gc.dispose();
            GridData data = (GridData)text.getLayoutData();
            data.heightHint = fontMetrics.getHeight()*2;
        }
        return parent2;
    }

    @Override
    protected boolean shouldCreateOtherPropertySection()
    {
        return true;
    }
}
