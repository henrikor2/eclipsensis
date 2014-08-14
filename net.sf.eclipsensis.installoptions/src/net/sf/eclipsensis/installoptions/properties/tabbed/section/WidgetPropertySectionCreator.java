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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class WidgetPropertySectionCreator extends PropertySectionCreator
{
    public WidgetPropertySectionCreator(InstallOptionsWidget widget)
    {
        super(widget);
    }

    public InstallOptionsWidget getWidget()
    {
        return (InstallOptionsWidget)getElement();
    }

    public final Control createPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite parent2 = widgetFactory.createComposite(parent);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = layout.marginWidth = 0;
        parent2.setLayout(layout);
        if(shouldCreateAppearancePropertySection()) {
            Group group = widgetFactory.createGroup(parent2, InstallOptionsPlugin.getResourceString("appearance.section.label")); //$NON-NLS-1$
            group.setLayout(new GridLayout(1,false));
            group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            Control c = createAppearancePropertySection(group, widgetFactory, commandHelper);
            if(c != null) {
                c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            }
            else {
                group.dispose();
            }
        }
        if(shouldCreateOtherPropertySection()) {
            Group group = widgetFactory.createGroup(parent2, InstallOptionsPlugin.getResourceString("other.section.label")); //$NON-NLS-1$
            group.setLayout(new GridLayout(1,false));
            group.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            Control c = createOtherPropertySection(group, widgetFactory, commandHelper);
            if(c != null) {
                c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            }
            else {
                group.dispose();
            }
        }
        return parent2;
    }

    protected Control createAppearancePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite composite = widgetFactory.createComposite(parent);
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        return composite;
    }

    protected Control createOtherPropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite composite = widgetFactory.createComposite(parent);
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        return composite;
    }

    protected boolean shouldCreateAppearancePropertySection()
    {
        return false;
    }

    protected boolean shouldCreateOtherPropertySection()
    {
        return false;
    }
}
