/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ListItemsDialog extends Dialog
{
    private List<String> mValues;
    private String mType;
    private ICellEditorValidator mValidator;

    public ListItemsDialog(Shell parent, List<String> values, String type)
    {
        super(parent);
        setShellStyle(getShellStyle()|SWT.RESIZE);
        mValues = new ArrayList<String>(values);
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
        newShell.setText(InstallOptionsPlugin.getFormattedString("listitems.dialog.name", new String[]{mType})); //$NON-NLS-1$
        newShell.setImage(InstallOptionsPlugin.getShellImage());
    }

    public List<String> getValues()
    {
        return mValues;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        final Composite composite = (Composite)super.createDialogArea(parent);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;

        Table table = new Table(composite,SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
        initializeDialogUnits(table);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = convertWidthInCharsToPixels(40);
        data.heightHint = convertHeightInCharsToPixels(10);
        table.setLayoutData(data);
        table.setLinesVisible(true);
        new TableColumn(table,SWT.LEFT);

        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new LabelProvider());
        final TextCellEditor textEditor = new TextCellEditor(table);
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
                    Common.openError(getShell(),textEditor.getErrorMessage(), InstallOptionsPlugin.getShellImage());
                }
                else {
                    TableItem ti = (TableItem)element;
                    Table t = ti.getParent();
                    int n = t.getSelectionIndex();
                    List<String> list = (List<String>)viewer.getInput();
                    if(n < list.size()) {
                        list.set(n,(String) value);
                    }
                    else {
                        list.add((String) value);
                    }
                    viewer.refresh(true);
                    viewer.setSelection(new StructuredSelection(value));
                }
            }
        });

        final Composite buttons = new Composite(composite,SWT.NONE);
        buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttons.setLayout(layout);

        final Button add = new Button(buttons,SWT.PUSH);
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
                    viewer.refresh(false);
                    viewer.setSelection(new StructuredSelection(item));
                    viewer.editElement(item,0);
                    Text t = (Text)textEditor.getControl();
                    t.setSelection(item.length());
                }
            }
        });

        final Button del = new Button(buttons, SWT.PUSH);
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
                        viewer.refresh(false);
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
                (input).clear();
                (input).addAll(elements);
            }
        };
        mover.setViewer(viewer);

        final Button up = new Button(buttons,SWT.PUSH);
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

        final Button down = new Button(buttons, SWT.PUSH);
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

        table.addControlListener(new TableResizer());
        viewer.setInput(mValues);
        return composite;
    }

    @Override
    protected void okPressed()
    {
        ICellEditorValidator validator = getValidator();
        if(validator != null) {
            String error = validator.isValid(getValues());
            if(!Common.isEmpty(error)) {
                Common.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error, //$NON-NLS-1$
                                 InstallOptionsPlugin.getShellImage());
                return;
            }
        }
        super.okPressed();
    }
}