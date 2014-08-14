/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.beans.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.tabbed.section.*;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class InstallOptionsListbox extends InstallOptionsListItems
{
    private static final long serialVersionUID = -3774074381295970839L;

    protected InstallOptionsListbox(INISection section)
    {
        super(section);
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_LISTBOX;
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_STATE)) {
            SelectListItemsPropertyDescriptor descriptor = new SelectListItemsPropertyDescriptor();
            addPropertyChangeListener(descriptor);
            return descriptor;
        }
        return super.createPropertyDescriptor(name);
    }

    @Override
    protected IPropertySectionCreator createPropertySectionCreator()
    {
        return new ListboxPropertySectionCreator(this);
    }

    @Override
    public void setFlags(List<String> flags)
    {
        if(!flags.contains(InstallOptionsModel.FLAGS_MULTISELECT)&&
           !flags.contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT)) {
            String state = getState();
            int n = state.indexOf(IInstallOptionsConstants.LIST_SEPARATOR);
            if(n >= 0) {
                state = state.substring(0,n);
                fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE, state));
            }
        }
        super.setFlags(flags);
    }

    private String validateState(String state)
    {
        Collection<String> listItems = new CaseInsensitiveSet(getListItems());
        List<String> selected = Common.tokenizeToList(state,IInstallOptionsConstants.LIST_SEPARATOR,false);
        selected.retainAll(listItems);
        return Common.flatten(selected.toArray(),IInstallOptionsConstants.LIST_SEPARATOR);
    }

    @Override
    public void setListItems(List<String> listItems)
    {
        super.setListItems(listItems);
        String oldState = getState();
        String newState = validateState(oldState);
        if(!Common.stringsAreEqual(newState,oldState)) {
            fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE, newState));
        }
    }

    @Override
    public boolean isMultiSelect()
    {
        return hasFlag(InstallOptionsModel.FLAGS_MULTISELECT) ||
               hasFlag(InstallOptionsModel.FLAGS_EXTENDEDSELECT);
    }

    protected class SelectListItemsPropertyDescriptor extends PropertyDescriptor implements PropertyChangeListener
    {
        private SelectListItemsCellEditor mEditor;
        private DisposeListener mListener = new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mEditor = null;
            }
         };

        public SelectListItemsPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_STATE, InstallOptionsPlugin.getResourceString("state.property.name")); //$NON-NLS-1$
            setValidator(new NSISStringLengthValidator(getDisplayName()));
        }

        @SuppressWarnings("unchecked")
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_LISTITEMS)) {
                setListItems((List<String>)evt.getNewValue());
            }
            else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
                List<String> list = (List<String>)evt.getNewValue();
                setMultiSelect(list.contains(InstallOptionsModel.FLAGS_MULTISELECT)||
                               list.contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT));
            }
        }

        public void setListItems(List<String> listItems)
        {
            if(mEditor != null) {
                mEditor.setListItems(listItems);
            }
        }

        public void setMultiSelect(boolean multiSelect)
        {
            if(mEditor != null) {
                mEditor.setMultiSelect(multiSelect);
            }
        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            if(mEditor == null) {
                mEditor = new SelectListItemsCellEditor(parent,getListItems(),
                        (getFlags().contains(InstallOptionsModel.FLAGS_MULTISELECT)||
                         getFlags().contains(InstallOptionsModel.FLAGS_EXTENDEDSELECT)));
                ICellEditorValidator validator = getValidator();
                if(validator != null) {
                    mEditor.setValidator(validator);
                }
                mEditor.getControl().addDisposeListener(mListener);
            }
            return mEditor;
        }
    }

    protected class SelectListItemsCellEditor extends DialogCellEditor implements PropertyChangeListener
    {
        private boolean mMultiSelect = false;
        private List<String> mListItems;

        protected SelectListItemsCellEditor(Composite parent, List<String> listItems, boolean multiSelect)
        {
            super(parent);
            InstallOptionsListbox.this.addPropertyChangeListener(this);
            setListItems(listItems);
            setMultiSelect(multiSelect);
        }

        @Override
        public void dispose()
        {
            InstallOptionsListbox.this.removePropertyChangeListener(this);
            super.dispose();
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_STATE)) {
                setValue(evt.getNewValue());
            }
        }

        public void setListItems(List<String> listItems)
        {
            mListItems = listItems;
        }

        public void setMultiSelect(boolean multiSelect)
        {
            mMultiSelect = multiSelect;
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow)
        {
            Object oldValue = getValue();
            List<String> selected = Common.tokenizeToList((String)oldValue,IInstallOptionsConstants.LIST_SEPARATOR,false);
            SelectListItemsDialog dialog = new SelectListItemsDialog(cellEditorWindow.getShell(), mListItems, selected, mMultiSelect, getType());
            dialog.setValidator(getValidator());
            int result = dialog.open();
            return (result == Window.OK?Common.flatten(dialog.getSelection().toArray(),IInstallOptionsConstants.LIST_SEPARATOR):oldValue);
        }
    }

    protected class SelectListItemsDialog extends Dialog
    {
        private List<String> mValues;
        private List<String> mSelection;
        private boolean mMultiSelect;
        private String mType;
        private ICellEditorValidator mValidator;
        private TableViewer mViewer;

        public SelectListItemsDialog(Shell parent, List<String> values, List<String> selection, boolean multiSelect, String type)
        {
            super(parent);
            setShellStyle(getShellStyle()|SWT.RESIZE);
            mValues = new ArrayList<String>(values);
            mSelection = new ArrayList<String>(selection);
            mMultiSelect = multiSelect;
            mType = type;
        }

        public ICellEditorValidator getValidator()
        {
            return mValidator;
        }

        public void setValidator(ICellEditorValidator validator)
        {
            mValidator = validator;
        }
        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getFormattedString("select.listitems.dialog.name", new String[]{mType})); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getShellImage());
        }

        public List<String> getSelection()
        {
            return mSelection;
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            final Composite composite = (Composite)super.createDialogArea(parent);
            GridLayout layout = (GridLayout)composite.getLayout();
            layout.numColumns = 2;
            layout.makeColumnsEqualWidth = false;

            Table table = new Table(composite,SWT.BORDER | (mMultiSelect?SWT.MULTI:SWT.SINGLE) | SWT.FULL_SELECTION | SWT.V_SCROLL);
            initializeDialogUnits(table);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = convertWidthInCharsToPixels(40);
            data.heightHint = convertHeightInCharsToPixels(10);
            table.setLayoutData(data);
            table.setLinesVisible(true);
            new TableColumn(table,SWT.LEFT);
            table.addControlListener(new TableResizer());

            mViewer = new TableViewer(table);
            mViewer.setContentProvider(new CollectionContentProvider());
            mViewer.setLabelProvider(new LabelProvider());
            mViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    mSelection.clear();
                    mSelection.addAll(Common.makeGenericList(String.class, sel.toList()));
                }
            });
            mViewer.setComparer(new IElementComparer() {
                public boolean equals(Object a, Object b)
                {
                    if(a instanceof String && b instanceof String) {
                        return Common.stringsAreEqual((String)a, (String)b, true);
                    }
                    return Common.objectsAreEqual(a, b);
                }

                public int hashCode(Object element)
                {
                    return (element==null?0:(element instanceof String?((String)element).toLowerCase().hashCode():element.hashCode()));
                }
            });
            mViewer.getTable().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e)
                {
                    okPressed();
                }
            });

            Composite buttons = new Composite(composite,SWT.NONE);
            buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            layout = new GridLayout(1,false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            buttons.setLayout(layout);

            Button selectAll = new Button(buttons,SWT.PUSH);
            selectAll.setText(InstallOptionsPlugin.getResourceString("select.all.label")); //$NON-NLS-1$
            selectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            selectAll.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mViewer.setSelection(new StructuredSelection(mValues));
                    mViewer.getTable().setFocus();
                }
            });
            selectAll.setEnabled(mMultiSelect);

            Button deselectAll = new Button(buttons,SWT.PUSH);
            deselectAll.setText(InstallOptionsPlugin.getResourceString("deselect.all.label")); //$NON-NLS-1$
            deselectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            deselectAll.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mViewer.setSelection(StructuredSelection.EMPTY);
                    mViewer.getTable().setFocus();
                }
            });

            mViewer.setInput(mValues);

            return composite;
        }

        @Override
        public void create()
        {
            super.create();
            // Set the initial selection here because of Windows bug which creates blank rows
            // if the selection is set in createDialogArea
            mViewer.setSelection(new StructuredSelection(mSelection));
        }

        @Override
        protected void okPressed()
        {
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                String error = validator.isValid(getSelection());
                if(!Common.isEmpty(error)) {
                    Common.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error, //$NON-NLS-1$
                                     InstallOptionsPlugin.getShellImage());
                    return;
                }
            }
            super.okPressed();
        }
    }
}

