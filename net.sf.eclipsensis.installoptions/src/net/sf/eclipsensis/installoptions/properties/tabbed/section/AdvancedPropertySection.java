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

import net.sf.eclipsensis.installoptions.properties.*;
import net.sf.eclipsensis.installoptions.properties.tabbed.CustomTabbedPropertySheetPage;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.*;

public class AdvancedPropertySection extends AbstractPropertySection
{
    protected PropertySheetPage mPage;

    /**
     * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
     */
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage page)
    {
        super.createControls(parent, page);
        Composite composite = getWidgetFactory().createFlatFormComposite(parent);
        if(page instanceof CustomTabbedPropertySheetPage) {
            CommandStack stack = null;
            if(((CustomTabbedPropertySheetPage)page).getEditor() != null) {
                stack = ((CustomTabbedPropertySheetPage)page).getEditor().getEditDomain().getCommandStack();
            }
            mPage = new CustomPropertySheetPage();
            mPage.setRootEntry(new InstallOptionsPropertySheetEntry(stack));
       }
        else {
            mPage = new PropertySheetPage();
        }

        mPage.createControl(composite);
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        data.height = 100;
        data.width = 100;
        mPage.getControl().setLayoutData(data);
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setInput(IWorkbenchPart part, ISelection selection)
    {
        super.setInput(part, selection);
        mPage.selectionChanged(part, selection);
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.ISection#dispose()
     */
    @Override
    public void dispose()
    {
        super.dispose();

        if (mPage != null) {
            mPage.dispose();
            mPage = null;
        }
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
     */
    @Override
    public void refresh()
    {
        mPage.refresh();
    }

    /**
     * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
     */
    @Override
    public boolean shouldUseExtraSpace()
    {
        return true;
    }
}
