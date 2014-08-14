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

import java.beans.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.installoptions.properties.descriptors.PropertyDescriptorHelper;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class ListboxPropertySectionCreator extends ListItemsPropertySectionCreator
{
    public ListboxPropertySectionCreator(InstallOptionsListbox element)
    {
        super(element);
    }

    @Override
    protected void createListAndStateButtons(Composite buttons, final CheckboxTableViewer viewer, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        super.createListAndStateButtons(buttons, viewer, widgetFactory, commandHelper);
        final IPropertyDescriptor stateDescriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE);
        final ICellEditorValidator stateValidator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) stateDescriptor);

        final Button selectAll = widgetFactory.createButton(buttons,"",SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        selectAll.setEnabled(((InstallOptionsListItems)getWidget()).isMultiSelect());
        selectAll.setImage(InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("select.all.icon"))); //$NON-NLS-1$
        selectAll.setToolTipText(InstallOptionsPlugin.getResourceString("select.all.tooltip")); //$NON-NLS-1$
        selectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        selectAll.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                List<?> list = (List<?>)viewer.getInput();
                if(list != null) {
                    Object[] checkedItems = viewer.getCheckedElements();
                    if(checkedItems != null && checkedItems.length == list.size()) {
                        return;
                    }
                    String state = Common.flatten(list,IInstallOptionsConstants.LIST_SEPARATOR);
                    String error = stateValidator.isValid(state);
                    if(Common.isEmpty(error)) {
                        commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_STATE, stateDescriptor.getDisplayName(), getWidget(), state);
                    }
                    else {
                        Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                    }
                }
            }
        });


        final Button deselectAll = widgetFactory.createButton(buttons,"",SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        deselectAll.setImage(InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("deselect.all.icon"))); //$NON-NLS-1$
        deselectAll.setToolTipText(InstallOptionsPlugin.getResourceString("deselect.all.tooltip")); //$NON-NLS-1$
        deselectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        deselectAll.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                String state = ""; //$NON-NLS-1$
                String error = stateValidator.isValid(state);
                if(Common.isEmpty(error)) {
                    commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_STATE, stateDescriptor.getDisplayName(), getWidget(), state);
                }
                else {
                    Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                }
            }
        });

        final PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
                    selectAll.setEnabled(((InstallOptionsListItems)getWidget()).isMultiSelect());
                }
            }
        };
        getWidget().addPropertyChangeListener(listener);
        buttons.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                getWidget().removePropertyChangeListener(listener);
            }
        });
    }
}
