/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.text.Collator;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class NSISTaskTagsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private CheckboxTableViewer mTableViewer = null;
    private Button mEditButton = null;
    private Button mRemoveButton = null;
    private Button mCaseSensitiveButton = null;
    private Collection<NSISTaskTag> mOriginalTags = null;
    private Font mBoldFont = null;

    /**
     *
     */
    public NSISTaskTagsPreferencePage()
    {
        super();
        String descriptionText = EclipseNSISPlugin.getResourceString("task.tags.preferences.description"); //$NON-NLS-1$
        setDescription(descriptionText);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
     */
    @Override
    public void dispose()
    {
        if(mBoldFont != null) {
            mBoldFont.dispose();
        }
        super.dispose();
    }

    /*
     * @see PreferencePage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_tasktagprefs_context"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout= new GridLayout(2,false);
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        composite.setLayout(layout);
        Table table= new Table(composite, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
        GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint= convertWidthInCharsToPixels(65);
        data.heightHint= convertHeightInCharsToPixels(10);
        table.setLayoutData(data);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn[] columns = new TableColumn[2];
        columns[0] = new TableColumn(table, SWT.NONE);
        columns[0].setText(EclipseNSISPlugin.getResourceString("task.tag.label")); //$NON-NLS-1$

        columns[1] = new TableColumn(table, SWT.NONE);
        columns[1].setText(EclipseNSISPlugin.getResourceString("task.tag.priority.label")); //$NON-NLS-1$
        mTableViewer= new CheckboxTableViewer(table);
        mTableViewer.setLabelProvider(new TaskTagLabelProvider());
        mTableViewer.setContentProvider(new CollectionContentProvider());

        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        mTableViewer.setSorter(new ViewerSorter(collator));

        mTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                edit();
            }
        });

        mTableViewer.addCheckStateListener(new ICheckStateListener() {
            @SuppressWarnings("unchecked")
            public void checkStateChanged(CheckStateChangedEvent event) {
                NSISTaskTag taskTag= (NSISTaskTag)event.getElement();
                boolean checked = event.getChecked();
                if(checked) {
                    Collection<NSISTaskTag> taskTags = (Collection<NSISTaskTag>)mTableViewer.getInput();
                    for(Iterator<NSISTaskTag> iter=taskTags.iterator(); iter.hasNext(); ) {
                        NSISTaskTag t = iter.next();
                        if(!t.equals(taskTag) && t.isDefault()) {
                            t.setDefault(false);
                            mTableViewer.setChecked(t,false);
                            mTableViewer.refresh(t,true);
                            break;
                        }
                    }
                }
                taskTag.setDefault(checked);
                mTableViewer.setChecked(taskTag,checked);
                mTableViewer.refresh(taskTag,true);
                updateButtons();
            }
        });

        mTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                updateButtons();
            }
        });

        Composite buttons= new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setImage(CommonImages.ADD_ICON);
        addButton.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                add();
            }
        });

        mEditButton= new Button(buttons, SWT.PUSH);
        mEditButton.setImage(CommonImages.EDIT_ICON);
        mEditButton.setToolTipText(EclipseNSISPlugin.getResourceString("edit.tooltip")); //$NON-NLS-1$
        mEditButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mEditButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                edit();
            }
        });

        mRemoveButton= new Button(buttons, SWT.PUSH);
        mRemoveButton.setImage(CommonImages.DELETE_ICON);
        mRemoveButton.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        mRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRemoveButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                remove();
            }
        });

        mCaseSensitiveButton = new Button(composite, SWT.CHECK);
        mCaseSensitiveButton.setText(EclipseNSISPlugin.getResourceString("task.tags.case.sensitive.label")); //$NON-NLS-1$
        mCaseSensitiveButton.setSelection(NSISPreferences.getInstance().isCaseSensitiveTaskTags());
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = 2;

        Dialog.applyDialogFont(composite);
        FontData[] fd = table.getFont().getFontData();
        for (int i = 0; i < fd.length; i++) {
            fd[i].setStyle(fd[i].getStyle()|SWT.BOLD);
        }
        mBoldFont = new Font(getShell().getDisplay(),fd);

        mOriginalTags = NSISPreferences.getInstance().getTaskTags();
        Collection<NSISTaskTag> taskTags = NSISPreferences.getInstance().getTaskTags();
        mTableViewer.setInput(NSISPreferences.getInstance().getTaskTags());
        mTableViewer.setAllChecked(false);
        for (Iterator<NSISTaskTag> iter=taskTags.iterator(); iter.hasNext(); ) {
            NSISTaskTag t = iter.next();
            if(t.isDefault()) {
                mTableViewer.setChecked(t,true);
                break;
            }
        }

        updateButtons();
        table.addControlListener(new TableResizer());

        return composite;
    }

    /**
     * Updates the buttons.
     */
    protected void updateButtons()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();
        int selectionCount= selection.size();
        int itemCount= mTableViewer.getTable().getItemCount();
        boolean enabled = selectionCount == 1;
        mEditButton.setEnabled(enabled);
//        mEditButton.setImage(enabled?CommonImages.EDIT_ICON:CommonImages.EDIT_DISABLED_ICON);
        enabled = selectionCount > 0 && selectionCount <= itemCount;
        mRemoveButton.setEnabled(enabled);
//        mEditButton.setImage(enabled?CommonImages.DELETE_ICON:CommonImages.DELETE_DISABLED_ICON);
    }

    @SuppressWarnings("unchecked")
    private void edit()
    {
        IStructuredSelection sel = (IStructuredSelection)mTableViewer.getSelection();
        if(!sel.isEmpty() && sel.size() == 1) {
            NSISTaskTag oldTag = (NSISTaskTag)sel.getFirstElement();
            NSISTaskTag newTag = new NSISTaskTag(oldTag);
            HashSet<String> set = new HashSet<String>();
            Collection<NSISTaskTag> collection = (Collection<NSISTaskTag>)mTableViewer.getInput();
            for (Iterator<NSISTaskTag> iter = collection.iterator(); iter.hasNext();) {
                NSISTaskTag tag = iter.next();
                if(!tag.equals(newTag)) {
                    set.add(tag.getTag());
                }
            }
            NSISTaskTagDialog dialog = new NSISTaskTagDialog(getShell(),newTag);
            dialog.setExistingTags(set);
            if(dialog.open() == Window.OK) {
                collection.remove(oldTag);
                collection.add(newTag);
                mTableViewer.refresh(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void add()
    {
        NSISTaskTag tag = new NSISTaskTag();
        HashSet<String> set = new HashSet<String>();
        Collection<NSISTaskTag> collection = (Collection<NSISTaskTag>)mTableViewer.getInput();
        for (Iterator<NSISTaskTag> iter = collection.iterator(); iter.hasNext();) {
            NSISTaskTag element = iter.next();
            set.add(element.getTag());
        }
        NSISTaskTagDialog dialog = new NSISTaskTagDialog(getShell(),tag);
        dialog.setExistingTags(set);
        if(dialog.open() == Window.OK) {
            collection.add(tag);
            mTableViewer.refresh();
        }
    }

    @SuppressWarnings("unchecked")
    private void remove()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();
        if(!selection.isEmpty()) {
            Collection<NSISTaskTag> coll = (Collection<NSISTaskTag>)mTableViewer.getInput();
            for(Iterator<?> iter=selection.toList().iterator(); iter.hasNext(); ) {
                coll.remove(iter.next());
            }
            mTableViewer.refresh();
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean performOk()
    {
        if (super.performOk()) {
            Collection<NSISTaskTag> taskTags = (Collection<NSISTaskTag>)mTableViewer.getInput();
            boolean caseSensitive = mCaseSensitiveButton.getSelection();
            boolean different = (caseSensitive != NSISPreferences.getInstance().isCaseSensitiveTaskTags());
            if (!different) {
                if (taskTags.size() == mOriginalTags.size()) {
                    for (Iterator<NSISTaskTag> iter = taskTags.iterator(); iter.hasNext();) {
                        if (!mOriginalTags.contains(iter.next())) {
                            different = true;
                            break;
                        }
                    }
                }
                else {
                    different = true;
                }
            }
            if (different) {
                if (taskTags.size() > 0) {
                    boolean defaultFound = false;
                    for (Iterator<NSISTaskTag> iter = taskTags.iterator(); iter.hasNext();) {
                        NSISTaskTag element = iter.next();
                        if (element.isDefault()) {
                            defaultFound = true;
                            break;
                        }
                    }
                    if (!defaultFound) {
                        if (taskTags.size() == 1) {
                            NSISTaskTag taskTag = (NSISTaskTag)taskTags.toArray()[0];
                            taskTag.setDefault(true);
                            mTableViewer.setChecked(taskTag, true);
                        }
                        else {
                            Common.openError(getShell(), EclipseNSISPlugin.getResourceString("task.tag.dialog.missing.default"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                            return false;
                        }
                    }
                }
            }
            boolean updateTaskTags = false;
            if (different) {
                NSISPreferences.getInstance().setTaskTags(taskTags);
                NSISPreferences.getInstance().setCaseSensitiveTaskTags(caseSensitive);
                MessageDialog dialog = new MessageDialog(getShell(), EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                        EclipseNSISPlugin.getShellImage(), EclipseNSISPlugin.getResourceString("task.tags.settings.changed"), MessageDialog.QUESTION, //$NON-NLS-1$
                        new String[]{IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
                dialog.setBlockOnOpen(true);
                int rv = dialog.open();
                if (rv == 2) {
                    //Cancel
                    return false;
                }
                else {
                    updateTaskTags = (rv == 0);
                }
                NSISPreferences.getInstance().store();
            }
            if (updateTaskTags) {
                new NSISTaskTagUpdater().updateTaskTags();
                NSISEditorUtilities.updatePresentations();
                mOriginalTags.clear();
                mOriginalTags.addAll(taskTags);
            }
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults()
    {
        mTableViewer.setInput(NSISPreferences.getInstance().getDefaultTaskTags());
        mTableViewer.refresh(true);
        super.performDefaults();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    private class TaskTagLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider
    {
        /*
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            return null;
        }

        /*
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex)
        {
            NSISTaskTag tag = (NSISTaskTag) element;

            switch (columnIndex) {
                case 0:
                    if(tag.isDefault()) {
                        return EclipseNSISPlugin.getFormattedString("task.tag.default.format",  //$NON-NLS-1$
                                                                    new Object[]{tag.getTag()});
                    }
                    else {
                        return tag.getTag();
                    }
                case 1:
                    int n = tag.getPriority();
                    if(n >= 0 && n < NSISTaskTag.PRIORITY_LABELS.length) {
                        return NSISTaskTag.PRIORITY_LABELS[n];
                    }
                    //$FALL-THROUGH$
                default:
                    return ""; //$NON-NLS-1$
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element)
        {
            if(element instanceof NSISTaskTag) {
                if(((NSISTaskTag)element).isDefault()) {
                    return mBoldFont;
                }
            }
            return null;
        }
    }
}
