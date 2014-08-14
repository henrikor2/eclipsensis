/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.editors;

import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;

/**
 * A cell editor that presents a list of items in a list box.
 */
public class ListCellEditor extends CellEditor
{
    private static final int DEFAULT_STYLE = SWT.SINGLE;

    private java.util.List<String> mItems;
    private String mSelection;
    private List mList;
    private boolean mCaseInsensitive = false;

    public ListCellEditor()
    {
        setStyle(DEFAULT_STYLE);
    }

    public ListCellEditor(Composite parent, java.util.List<String> items)
    {
        this(parent, items, DEFAULT_STYLE);
    }

    public ListCellEditor(Composite parent, java.util.List<String> items, int style)
    {
        super(parent, style);
        setItems(items);
    }

    public boolean isCaseInsensitive()
    {
        return mCaseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive)
    {
        mCaseInsensitive = caseInsensitive;
    }

    public java.util.List<String> getItems()
    {
        return (mItems == null?Collections.<String>emptyList():mItems);
    }

    public void setItems(java.util.List<String> items)
    {
        mItems = items;
        populateListItems();
    }

    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    @Override
    protected Control createControl(Composite parent)
    {
        mList = new List(parent, getStyle());
        mList.setFont(parent.getFont());

        mList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                keyReleaseOccured(e);
            }
        });

        mList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent event)
            {
                applyEditorValueAndDeactivate();
            }

            @Override
            public void widgetSelected(SelectionEvent event)
            {
                computeSelection();
            }
        });

        mList.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e)
            {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        mList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                ListCellEditor.this.focusLost();
            }
        });
        return mList;
    }


    /**
     *
     */
    private void computeSelection()
    {
        String oldSelection = mSelection;
        boolean oldIsValid = isValueValid();
        mSelection = Common.flatten(mList.getSelection(),IInstallOptionsConstants.LIST_SEPARATOR);
        boolean newIsValid = isCorrect(mSelection);
        if(!mSelection.equals(oldSelection)) {
            valueChanged(oldIsValid,newIsValid);
        }
    }

    @Override
    protected Object doGetValue()
    {
        return mSelection;
    }

    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    @Override
    protected void doSetFocus()
    {
        mList.setFocus();
    }

    @Override
    public LayoutData getLayoutData()
    {
        LayoutData layoutData = super.getLayoutData();
        if ((mList == null) || mList.isDisposed()) {
            layoutData.minimumWidth = 60;
        }
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(mList);
            layoutData.minimumWidth = (gc.getFontMetrics().getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    @Override
    protected void doSetValue(Object value)
    {
        java.util.List<String> list = Common.tokenizeToList((String)value,IInstallOptionsConstants.LIST_SEPARATOR,false);
        java.util.List<String> items = getItems();
        outer:
        for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
            String item = iter.next();
            for (Iterator<String> iterator = items.iterator(); iterator.hasNext();) {
                String item2 = iterator.next();
                if(Common.stringsAreEqual(item,item2,mCaseInsensitive)) {
                    iter.set(item2);
                    continue outer;
                }
            }
            iter.remove();
        }
        String[] selection = list.toArray(Common.EMPTY_STRING_ARRAY);
        mSelection = Common.flatten(selection,IInstallOptionsConstants.LIST_SEPARATOR);
        mList.setSelection(selection);
    }

    /**
     * Updates the list of choices for the combo box for the current control.
     */
    private void populateListItems()
    {
        if (mList != null && mItems != null) {
            mList.removeAll();
            for (int i = 0; i < mItems.size(); i++) {
                mList.add(mItems.get(i), i);
            }

            if(mSelection == null) {
                setValueValid(true);
                mSelection = ""; //$NON-NLS-1$
            }
            else {
                setValue(mSelection);
            }
        }
    }

    /**
     * Applies the currently selected value and deactiavates the cell editor
     */
    void applyEditorValueAndDeactivate()
    {
        //  must set the selection before getting value
        computeSelection();
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);
        if (!isValid) {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[]{newValue}));
        }
        fireApplyEditorValue();
        deactivate();
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#focusLost()
     */
    @Override
    protected void focusLost()
    {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
     */
    @Override
    protected void keyReleaseOccured(KeyEvent keyEvent)
    {
        if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
        else {
            super.keyReleaseOccured(keyEvent);
        }
    }
}
