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
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.installoptions.properties.descriptors.PropertyDescriptorHelper;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.gef.commands.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class ListItemsPropertySectionCreator extends EditableElementPropertySectionCreator
{
    public ListItemsPropertySectionCreator(InstallOptionsListItems element)
    {
        super(element);
    }

    @Override
    protected Control createAppearancePropertySection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, InstallOptionsCommandHelper commandHelper)
    {
        Composite parent2 = (Composite)super.createAppearancePropertySection(parent, widgetFactory, commandHelper);
        createListItemsAndStateSection(parent2, widgetFactory, commandHelper);
        return parent2;
    }

    @Override
    protected boolean shouldCreateAppearancePropertySection()
    {
        return true;
    }

    protected CheckboxTableViewer createListItemsAndStateSection(Composite parent, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        final IPropertyDescriptor listItemsDescriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_LISTITEMS);
        final IPropertyDescriptor stateDescriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE);
        if(listItemsDescriptor != null && stateDescriptor != null) {
            final boolean[] nonUserChange = {false};

            Composite parent2 = widgetFactory.createGroup(parent, listItemsDescriptor.getDisplayName());
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.horizontalSpan = 2;
            parent2.setLayoutData(data);
            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.verticalSpacing = 0;
            gridLayout.marginHeight = 0;
            gridLayout.marginTop = 2;
            parent2.setLayout(gridLayout);

            final Table table = widgetFactory.createTable(parent2,SWT.FLAT|SWT.CHECK|SWT.MULTI);
            GC gc = new GC(table);
            gc.setFont(JFaceResources.getDialogFont());
            FontMetrics fontMetrics = gc.getFontMetrics();
            gc.dispose();
            data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = fontMetrics.getAverageCharWidth()*30;
            data.heightHint = fontMetrics.getHeight()*5;
            table.setLayoutData(data);

            final CheckboxTableViewer viewer = new CheckboxTableViewer(table);
            viewer.setContentProvider(new CollectionContentProvider());
            viewer.setLabelProvider(new LabelProvider());
            viewer.setComparer(new IElementComparer() {
                public boolean equals(Object a, Object b)
                {
                    if(a instanceof String && b instanceof String) {
                        return Common.stringsAreEqual((String)a, (String)b, true);
                    }
                    return Common.objectsAreEqual(a, b);
                }

                public int hashCode(Object element)
                {
                    Object element2 = element;
                    if(element2 != null) {
                        if(element2 instanceof String) {
                            element2 = ((String)element2).toLowerCase();
                        }
                        return element2.hashCode();
                    }
                    return 0;
                }

            });
            final InstallOptionsListItems widget = (InstallOptionsListItems)getWidget();
            final List<String> listItems = new ArrayList<String>(widget.getListItems());
            String[] state = Common.tokenize(widget.getState(), IInstallOptionsConstants.LIST_SEPARATOR);
            final ICellEditorValidator stateValidator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) stateDescriptor);
            viewer.addCheckStateListener(new ICheckStateListener() {
                public void checkStateChanged(CheckStateChangedEvent event)
                {
                    if(!nonUserChange[0]) {
                        boolean checked = event.getChecked();
                        String oldState = getWidget().getStringPropertyValue(InstallOptionsModel.PROPERTY_STATE);
                        String newState;
                        if(checked && !((InstallOptionsListItems)getWidget()).isMultiSelect()) {
                            String element = (String)event.getElement();
                            viewer.setCheckedElements(new String[] {element});
                            newState = element;
                        }
                        else {
                            newState = Common.flatten(viewer.getCheckedElements(),IInstallOptionsConstants.LIST_SEPARATOR);
                        }
                        if(!Common.stringsAreEqual(oldState, newState, true)) {
                            String error = stateValidator.isValid(newState);
                            if(Common.isEmpty(error)) {
                                commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_STATE,
                                                stateDescriptor.getDisplayName(), getWidget(), newState);
                            }
                            else {
                                Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                                viewer.setCheckedElements(Common.tokenize(oldState, IInstallOptionsConstants.LIST_SEPARATOR));
                            }
                        }
                    }
                }
            });

            viewer.setInput(listItems);
            viewer.setCheckedElements(state);
            final PropertyChangeListener listener = new PropertyChangeListener() {
                @SuppressWarnings("unchecked")
                public void propertyChange(PropertyChangeEvent evt)
                {
                    nonUserChange[0]=true;
                    try {
                        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
                            List <String>list = (List<String>)evt.getNewValue();
                            if (Common.isValid(viewer.getControl())) {
                                List<String> oldInput = (List<String>)viewer.getInput();
                                if(!Common.objectsAreEqual(list, oldInput)) {
                                    viewer.setInput(new ArrayList<String>(list));
                                    String state = ((InstallOptionsListItems)getWidget()).getState();
                                    viewer.setCheckedElements(Common.tokenize(state, IInstallOptionsConstants.LIST_SEPARATOR));
                                }
                            }
                        }
                        else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_STATE)) {
                            String state = (String)evt.getNewValue();
                            if (Common.isValid(viewer.getControl())) {
                                viewer.setCheckedElements(Common.tokenize(state, IInstallOptionsConstants.LIST_SEPARATOR));
                            }
                        }
                    }
                    finally {
                        nonUserChange[0]=false;
                    }
                }
            };
            widget.addPropertyChangeListener(listener);
            table.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    widget.removePropertyChangeListener(listener);
                }
            });

            Composite buttons = widgetFactory.createComposite(parent2);
            buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
            GridLayout layout = new GridLayout(2, true);
            layout.marginHeight = layout.marginWidth = 0;
            buttons.setLayout(layout);
            createListAndStateButtons(buttons, viewer, widgetFactory, commandHelper);

            CLabel l = widgetFactory.createCLabel(parent2, InstallOptionsPlugin.getResourceString("listitems.state.checked.items.message"), SWT.FLAT); //$NON-NLS-1$
            FontData[] fd = l.getFont().getFontData();
            for (int i = 0; i < fd.length; i++) {
                fd[i].setStyle(fd[i].getStyle()|SWT.BOLD);
                fd[i].setHeight((int)(fd[i].getHeight()*0.9));
            }
            final Font f = new Font(l.getDisplay(),fd);
            l.setFont(f);
            l.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    f.dispose();
                }
            });
            data = new GridData(SWT.FILL, SWT.FILL, true, false);
            data.horizontalSpan = 2;
            l.setLayoutData(data);

            return viewer;
        }
        return null;
    }

    protected void createListAndStateButtons(Composite buttons, final CheckboxTableViewer viewer, TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        final IPropertyDescriptor listItemsDescriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_LISTITEMS);
        final ICellEditorValidator listItemsValidator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) listItemsDescriptor);
        final IPropertyDescriptor stateDescriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_STATE);
        final ICellEditorValidator stateValidator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) stateDescriptor);

        final TextCellEditor textEditor = new TextCellEditor(viewer.getTable());
        ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                e.doit = e.text.indexOf(IInstallOptionsConstants.LIST_SEPARATOR) < 0;
                if(!e.doit) {
                    e.display.beep();
                }
            }
        });
        viewer.setColumnProperties(new String[]{"item"}); //$NON-NLS-1$
        viewer.setCellEditors(new CellEditor[]{textEditor});
        viewer.setCellModifier(new ICellModifier(){
            public boolean canModify(Object element, String property)
            {
                return true;
            }

            public Object getValue(Object element, String property)
            {
                return element;
            }

            @SuppressWarnings("unchecked")
            public void modify(Object element, String property, Object value)
            {
                if(value == null) {
                    Common.openError(viewer.getTable().getShell(),textEditor.getErrorMessage(), InstallOptionsPlugin.getShellImage());
                }
                else {
                    TableItem ti = (TableItem)element;
                    Table t = ti.getParent();
                    int n = t.getSelectionIndex();
                    String oldValue = null;
                    List<String> list = (List<String>)viewer.getInput();
                    if(n < list.size()) {
                        oldValue = list.set(n,String.valueOf(value));
                    }
                    else {
                        list.add(String.valueOf(value));
                    }
                    String error = listItemsValidator.isValid(list);
                    if(Common.isEmpty(error)) {
                        CompoundCommand command = commandHelper.createPropertyChangedCommand(InstallOptionsModel.PROPERTY_LISTITEMS,
                                        listItemsDescriptor.getDisplayName(), getWidget(), list);
                        List<String> oldState = Common.tokenizeToList(((InstallOptionsListItems)getWidget()).getState(), IInstallOptionsConstants.LIST_SEPARATOR);
                        if(Common.collectionContainsIgnoreCase(oldState, oldValue)) {
                            if(!Common.collectionContainsIgnoreCase(list, oldValue)) {
                                for (ListIterator<String> iter = oldState.listIterator(); iter.hasNext();) {
                                    String str = iter.next();
                                    if(Common.stringsAreEqual(str, oldValue, true)) {
                                        iter.set(String.valueOf(value));
                                        String newState = Common.flatten(oldState,IInstallOptionsConstants.LIST_SEPARATOR);
                                        error = stateValidator.isValid(newState);
                                        if(Common.isEmpty(error)) {
                                            CompoundCommand cmd = new ForwardUndoCompoundCommand(command.getLabel());
                                            cmd.add(commandHelper.createPropertyChangedCommand(InstallOptionsModel.PROPERTY_STATE,
                                                            stateDescriptor.getDisplayName(), getWidget(), newState));
                                            cmd.add(command);
                                            command = cmd;
                                        }
                                        else {
                                            command = null;
                                        }
                                        break;
                                    }
                                }

                            }
                        }
                        if(command != null && command.size() > 0) {
                            viewer.refresh(true);
                            commandHelper.execute(command);
                            viewer.refresh(true);
                            viewer.setSelection(new StructuredSelection(value));
                            return;
                        }
                    }
                    Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                    if(oldValue != null) {
                        list.set(n, oldValue);
                    }
                    else {
                        list.remove(n);
                    }
                }
            }
        });
        final Button add = widgetFactory.createButton(buttons,"",SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        add.setImage(CommonImages.ADD_ICON);
        add.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        add.addListener(SWT.Selection, new Listener() {
            @SuppressWarnings("unchecked")
            public void handleEvent(Event e) {
                List<String> list = (List<String>)viewer.getInput();
                if(list != null) {
                    int counter = 1;
                    String item = InstallOptionsPlugin.getFormattedString("default.listitem.label", new Object[]{new Integer(counter++)}); //$NON-NLS-1$

                    while(Common.collectionContainsIgnoreCase(list, item)) {
                        item = InstallOptionsPlugin.getFormattedString("default.listitem.label", new Object[]{new Integer(counter++)}); //$NON-NLS-1$
                    }
                    list.add(item);
                    String error = listItemsValidator.isValid(list);
                    if(Common.isEmpty(error)) {
                        commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_LISTITEMS,
                                        listItemsDescriptor.getDisplayName(), getWidget(), list);
                        viewer.refresh(false);
                        viewer.setSelection(new StructuredSelection(item));
                        viewer.editElement(item,0);
                        Text t = (Text)textEditor.getControl();
                        t.setSelection(item.length());
                    }
                    else {
                        Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                        list.remove(item);
                    }
                }
            }
        });

        final Button del = widgetFactory.createButton(buttons,"",SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        del.setImage(CommonImages.DELETE_ICON);
        del.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        del.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        del.addListener(SWT.Selection, new Listener() {
            @SuppressWarnings("unchecked")
            public void handleEvent(Event e) {
                List<String> list = (List<String>)viewer.getInput();
                if(list != null) {
                    IStructuredSelection selection= (IStructuredSelection) viewer.getSelection();
                    if(!selection.isEmpty()) {
                        for(Iterator<?> iter=selection.toList().iterator(); iter.hasNext(); ) {
                            list.remove(iter.next());
                        }
                        String error = listItemsValidator.isValid(list);
                        if(Common.isEmpty(error)) {
                            commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_LISTITEMS,
                                            listItemsDescriptor.getDisplayName(), getWidget(), list);
                            viewer.refresh(false);
                        }
                        else {
                            Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                            list.clear();
                            list.addAll(((InstallOptionsListItems)getWidget()).getListItems());
                            viewer.setSelection(selection);
                        }
                    }
                }
            }
        });
        del.setEnabled(!viewer.getSelection().isEmpty());

        final TableViewerUpDownMover<List<String>, String> mover = new TableViewerUpDownMover<List<String>, String>() {
            @Override
            @SuppressWarnings("unchecked")
            protected List<String> getAllElements()
            {
                return (List<String>)((TableViewer)getViewer()).getInput();
            }

            @Override
            protected void updateStructuredViewerInput(List<String> input, List<String> elements, List<String> move, boolean isDown)
            {
                String error = listItemsValidator.isValid(elements);
                if(Common.isEmpty(error)) {
                    commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_LISTITEMS,
                                    listItemsDescriptor.getDisplayName(), getWidget(), elements);
                }
                else {
                    Common.openError(viewer.getTable().getShell(), error, InstallOptionsPlugin.getShellImage());
                }
            }
        };
        mover.setViewer(viewer);

        final Button up = widgetFactory.createButton(buttons,"",SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        up.setImage(CommonImages.UP_ICON);
        up.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        up.setEnabled(mover.canMoveUp());
        up.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveUp();
            }
        });

        final Button down = widgetFactory.createButton(buttons,"",SWT.PUSH|SWT.FLAT); //$NON-NLS-1$
        down.setImage(CommonImages.DOWN_ICON);
        down.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        down.setEnabled(mover.canMoveDown());
        down.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveDown();
            }
        });


        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                del.setEnabled(!sel.isEmpty());
                up.setEnabled(mover.canMoveUp());
                down.setEnabled(mover.canMoveDown());
            }
        });
    }
}
