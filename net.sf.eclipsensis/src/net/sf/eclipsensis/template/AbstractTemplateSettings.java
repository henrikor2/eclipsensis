/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.lang.reflect.Array;
import java.text.Collator;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.TableResizer;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public abstract class AbstractTemplateSettings<T extends ITemplate> extends Composite
{
    private AbstractTemplateManager<T> mTemplateManager = null;

    private CheckboxTableViewer mTableViewer = null;
    private Button mAddButton;
    private Button mDuplicateButton;
    private Button mEditButton = null;
    private Button mImportButton = null;
    private Button mExportButton = null;
    private Button mRemoveButton = null;
    private Button mRestoreButton = null;
    private Button mRevertButton = null;
    private StyledText mDescriptionText = null;

    /**
     *
     */
    public AbstractTemplateSettings(Composite parent, int style, AbstractTemplateManager<T> manager)
    {
        super(parent, style);
        mTemplateManager = manager;
        createContents();
    }

    protected CheckboxTableViewer getTableViewer()
    {
        return mTableViewer;
    }

    protected AbstractTemplateManager<T> getTemplateManager()
    {
        return mTemplateManager;
    }

    protected void createContents()
    {
        GC gc = new GC(this);
        Font old = gc.getFont();
        gc.setFont(JFaceResources.getDialogFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.setFont(old);
        gc.dispose();

        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        this.setLayout(layout);

        Composite innerParent= new Composite(this, SWT.NONE);
        GridLayout innerLayout= new GridLayout();
        innerLayout.numColumns= 2;
        innerLayout.marginHeight= 0;
        innerLayout.marginWidth= 0;
        innerParent.setLayout(innerLayout);
        GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan= 2;
        innerParent.setLayoutData(gd);

        final Table table= new Table(innerParent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);

        GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint= fontMetrics.getAverageCharWidth()*3;
        data.heightHint= fontMetrics.getHeight()*10;
        table.setLayoutData(data);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn[] columns= {new TableColumn(table, SWT.NONE)};
        columns[0].setText(EclipseNSISPlugin.getResourceString("template.settings.name.label")); //$NON-NLS-1$

        mTableViewer= new CheckboxTableViewer(table);
        mTableViewer.setLabelProvider(new CollectionLabelProvider());
        mTableViewer.setContentProvider(new EmptyContentProvider() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
            @Override
            public Object[] getElements(Object inputElement)
            {
                if(inputElement != null && inputElement.equals(mTemplateManager)) {
                    return mTemplateManager.getTemplates().toArray();
                }
                return super.getElements(inputElement);
            }
        });

        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        mTableViewer.setSorter(new ViewerSorter(collator));

        ViewerFilter filter = new ViewerFilter() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element != null && mTemplateManager.getTemplateClass().isAssignableFrom(element.getClass())) {
                    T template = (T)element;
                    return template.isAvailable() && !template.isDeleted();
                }
                return true;
            }
        };
        mTableViewer.addFilter(filter);

        final INSISHomeListener nsisHomeListener = new INSISHomeListener() {
            public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
            {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        mTableViewer.refresh();
                        mTableViewer.setCheckedElements(getEnabledTemplates());
                    }
                });
            }
        };
        NSISPreferences.getInstance().addListener(nsisHomeListener);
        table.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                NSISPreferences.getInstance().removeListener(nsisHomeListener);
            }
        });

        mTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                edit();
            }
        });

        mTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                doSelectionChanged();
            }
        });

        mTableViewer.addCheckStateListener(new ICheckStateListener() {
            @SuppressWarnings("unchecked")
            public void checkStateChanged(CheckStateChangedEvent event) {
                T oldTemplate= (T)event.getElement();
                T newTemplate= (T)oldTemplate.clone();
                newTemplate.setEnabled(!oldTemplate.isEnabled());
                getTemplateManager().updateTemplate(oldTemplate, newTemplate);
                mTableViewer.refresh(true);
                mTableViewer.setSelection(new StructuredSelection(newTemplate));
                mTableViewer.setChecked(newTemplate, newTemplate.isEnabled());
                doSelectionChanged();
            }
        });

        Composite buttons= new Composite(innerParent, SWT.NONE);
        buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);

        createButtons(buttons);

        Label label= new Label(this, SWT.NONE);
        label.setText(EclipseNSISPlugin.getResourceString("template.settings.description.label")); //$NON-NLS-1$
        data= new GridData();
        data.horizontalSpan= 2;
        label.setLayoutData(data);

        mDescriptionText = new StyledText(this,SWT.BORDER|SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
        mDescriptionText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        mDescriptionText.setCursor(null);
        mDescriptionText.setCaret(null);
        data= new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalSpan= 2;
        data.heightHint= fontMetrics.getHeight()*5;
        mDescriptionText.setLayoutData(data);

        mTableViewer.setInput(mTemplateManager);
        mTableViewer.setCheckedElements(getEnabledTemplates());

        updateButtons();
        table.addControlListener(new TableResizer());

        Dialog.applyDialogFont(this);
    }

    private void createButtons(Composite parent)
    {
        if(canAdd()) {
            mAddButton= new Button(parent, SWT.PUSH);
            mAddButton.setText(EclipseNSISPlugin.getResourceString("template.settings.new.label")); //$NON-NLS-1$
            mAddButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            mAddButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event e) {
                    add();
                }
            });
        }
        else {
            mAddButton = null;
        }

        mDuplicateButton= new Button(parent, SWT.PUSH);
        mDuplicateButton.setText(EclipseNSISPlugin.getResourceString("template.settings.duplicate.label")); //$NON-NLS-1$
        mDuplicateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mDuplicateButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                duplicate();
            }
        });

        mEditButton= new Button(parent, SWT.PUSH);
        mEditButton.setText(EclipseNSISPlugin.getResourceString("template.settings.edit.label")); //$NON-NLS-1$
        mEditButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mEditButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                edit();
            }
        });

        mRemoveButton= new Button(parent, SWT.PUSH);
        mRemoveButton.setText(EclipseNSISPlugin.getResourceString("template.settings.remove.label")); //$NON-NLS-1$
        mRemoveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRemoveButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                remove();
            }
        });

        createSeparator(parent);

        mRestoreButton= new Button(parent, SWT.PUSH);
        mRestoreButton.setText(EclipseNSISPlugin.getResourceString("template.settings.restore.label")); //$NON-NLS-1$
        mRestoreButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRestoreButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                restoreDeleted();
            }
        });

        mRevertButton= new Button(parent, SWT.PUSH);
        mRevertButton.setText(EclipseNSISPlugin.getResourceString("template.settings.revert.label")); //$NON-NLS-1$
        mRevertButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mRevertButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                revert();
            }
        });

        createSeparator(parent);

        mImportButton= new Button(parent, SWT.PUSH);
        mImportButton.setText(EclipseNSISPlugin.getResourceString("template.settings.import.label")); //$NON-NLS-1$
        mImportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mImportButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                import$();
            }
        });

        mExportButton= new Button(parent, SWT.PUSH);
        mExportButton.setText(EclipseNSISPlugin.getResourceString("template.settings.export.label")); //$NON-NLS-1$
        mExportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mExportButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                export();
            }
        });
    }

    /**
     * Creates a separator between buttons
     * @param parent
     * @return
     */
    private Label createSeparator(Composite parent)
    {
        Label separator= new Label(parent, SWT.NONE);
        separator.setVisible(false);
        GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gd.heightHint= 4;
        separator.setLayoutData(gd);
        return separator;
    }

    @SuppressWarnings("unchecked")
    private T[] getEnabledTemplates()
    {
        List<T> enabled= new ArrayList<T>();
        Collection<T> coll = mTemplateManager.getTemplates();
        for (Iterator<T> iter = coll.iterator(); iter.hasNext(); ) {
            T template = iter.next();
            if (template.isEnabled() && !template.isDeleted()) {
                enabled.add(template);
            }
        }
        return enabled.toArray((T[])Array.newInstance(mTemplateManager.getTemplateClass(), enabled.size()));
    }

    private void doSelectionChanged()
    {
        updateViewerInput();
        updateButtons();
    }

    /**
     * Updates the description.
     */
    @SuppressWarnings("unchecked")
    protected void updateViewerInput()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        if (selection.size() == 1) {
            T template= (T) selection.getFirstElement();
            mDescriptionText.setText(template.getDescription());
        }
        else {
            mDescriptionText.setText(""); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("unchecked")
    private void duplicate()
    {
        T template = (T)((T)((IStructuredSelection)getTableViewer().getSelection()).getFirstElement()).clone();
        template.setName(EclipseNSISPlugin.getFormattedString("template.settings.duplicate.format",new Object[] {template.getName()})); //$NON-NLS-1$
        add(template);
        edit();
    }

    private void add()
    {
        T template = createTemplate(""); //$NON-NLS-1$
        Dialog dialog= createDialog(template);
        if (dialog.open() != Window.CANCEL) {
            add(template);
        }
    }

    /**
     * @param template
     */
    private void add(T template)
    {
        getTemplateManager().addTemplate(template);
        getTableViewer().refresh(true);
        getTableViewer().setChecked(template, template.isEnabled());
        getTableViewer().setSelection(new StructuredSelection(template));
    }

    /**
     * Updates the buttons.
     */
    @SuppressWarnings("unchecked")
    protected void updateButtons()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();
        int selectionCount= selection.size();
        int itemCount= mTableViewer.getTable().getItemCount();
        boolean canRestore= mTemplateManager.canRestore();
        boolean canRevert= false;
        for (Iterator<?> it= selection.iterator(); it.hasNext();) {
            if(mTemplateManager.canRevert((T)it.next())) {
                canRevert= true;
                break;
            }
        }

        mDuplicateButton.setEnabled(selectionCount == 1);
        mEditButton.setEnabled(selectionCount == 1);
        mExportButton.setEnabled(selectionCount > 0);
        mRemoveButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
        mRestoreButton.setEnabled(canRestore);
        mRevertButton.setEnabled(canRevert);
    }

    @SuppressWarnings("unchecked")
    private void edit()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        Object[] objects= selection.toArray();
        if (objects == null || objects.length != 1) {
            return;
        }

        T oldTemplate= (T)selection.getFirstElement();
        edit(oldTemplate);
    }

    @SuppressWarnings("unchecked")
    private void edit(T oldTemplate)
    {
        T newTemplate = (T)oldTemplate.clone();
        Dialog dialog= createDialog(newTemplate);
        if (dialog.open() == Window.OK) {
            if(updateTemplate(oldTemplate, newTemplate)) {
                mTableViewer.refresh(true);
                doSelectionChanged();
                mTableViewer.setChecked(newTemplate, newTemplate.isEnabled());
                mTableViewer.setSelection(new StructuredSelection(newTemplate));
            }
        }
    }

    private boolean updateTemplate(T oldTemplate, T template)
    {
        boolean createnew = false;
        if(!oldTemplate.getName().equals(template.getName())) {
            createnew = Common.openQuestion(null,EclipseNSISPlugin.getResourceString("template.rename.confirm"),getShellImage()); //$NON-NLS-1$
        }

        if(!createnew) {
            mTemplateManager.updateTemplate(oldTemplate,template);
        }
        else {
            mTemplateManager.addTemplate(template);
        }
        return true;
    }

    private void import$()
    {
        FileDialog dialog= new FileDialog(getShell());
        dialog.setText(EclipseNSISPlugin.getResourceString("template.settings.import.title")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("template.settings.import.extension")}); //$NON-NLS-1$
        String path= dialog.open();

        if (path == null) {
            return;
        }

        try {
            File file= new File(path);
            if (file.exists()) {
                Collection<T> coll = mTemplateManager.getReaderWriter().import$(file);
                if(!Common.isEmptyCollection(coll)) {
                    for (Iterator<T> iter=coll.iterator(); iter.hasNext(); ) {
                        T template = iter.next();
                        T oldTemplate = null;
                        if(template.getId() != null) {
                            oldTemplate = mTemplateManager.getTemplate(template.getId());
                        }
                        if(oldTemplate != null) {
                            mTemplateManager.updateTemplate(oldTemplate, template);
                        }
                        else {
                            mTemplateManager.addTemplate(template);
                        }
                    }

                    mTableViewer.refresh();
                    mTableViewer.setCheckedElements(getEnabledTemplates());
                    mTableViewer.setSelection(new StructuredSelection(coll.toArray()));
                    mTableViewer.getTable().setFocus();
                }
            }
        }
        catch (Exception e) {
            Common.openError(getShell(), e.getLocalizedMessage(), getShellImage());
        }
    }

    private void export()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();
        Collection<T> templates= Common.makeGenericList(mTemplateManager.getTemplateClass(), selection.toList());
        FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
        dialog.setText(EclipseNSISPlugin.getResourceString("template.settings.export.title")); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("template.settings.import.extension")}); //$NON-NLS-1$
        dialog.setFileName(EclipseNSISPlugin.getResourceString("template.settings.export.filename")); //$NON-NLS-1$
        String path= dialog.open();

        if (path == null) {
            return;
        }

        File file= new File(path);

        if (!file.exists() || Common.openConfirm(getShell(),EclipseNSISPlugin.getFormattedString("save.confirm",new Object[]{file.getAbsolutePath()}), getShellImage())) { //$NON-NLS-1$
            try {
                mTemplateManager.getReaderWriter().export(templates, file);
            }
            catch (Exception e) {
                Common.openError(getShell(),e.getLocalizedMessage(), getShellImage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void remove()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        if(!selection.isEmpty()) {
            Iterator<?> elements= selection.iterator();
            while (elements.hasNext()) {
                T template= (T) elements.next();
                mTemplateManager.removeTemplate(template);
            }

            mTableViewer.refresh(true);
        }
    }

    private void restoreDeleted()
    {
        mTemplateManager.restore();
        mTableViewer.refresh(true);
        mTableViewer.setCheckedElements(getEnabledTemplates());
        updateButtons();
    }

    @SuppressWarnings("unchecked")
    private void revert()
    {
        IStructuredSelection selection= (IStructuredSelection) mTableViewer.getSelection();

        if(!selection.isEmpty()) {
            ArrayList<T> list = new ArrayList<T>();
            for (Iterator<?> iter= selection.iterator(); iter.hasNext(); ) {
                T temp = mTemplateManager.revert((T) iter.next());
                if(temp != null) {
                    list.add(temp);
                }
            }

            resetViewer();
            mTableViewer.setSelection(new StructuredSelection(list));
            doSelectionChanged();
            mTableViewer.getTable().setFocus();
        }
    }

    public boolean performCancel()
    {
        mTemplateManager.discard();
        resetViewer();
        return true;
    }

    public void performDefaults()
    {
        mTemplateManager.resetToDefaults();
        resetViewer();
    }

    /**
     *
     */
    private void resetViewer()
    {
        mTableViewer.refresh(true);
        mTableViewer.setCheckedElements(getEnabledTemplates());
    }

    public boolean performOk()
    {
        try {
            mTemplateManager.save();
            return true;
        }
        catch (IOException e) {
            Common.openError(getShell(),e.getLocalizedMessage(), getShellImage());
            return false;
        }
    }

    protected abstract boolean canAdd();
    protected abstract T createTemplate(String name);
    protected abstract Dialog createDialog(T newTemplate);
    protected abstract Image getShellImage();
}
