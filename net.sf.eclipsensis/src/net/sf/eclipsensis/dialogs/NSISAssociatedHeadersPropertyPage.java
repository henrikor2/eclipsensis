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

import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class NSISAssociatedHeadersPropertyPage extends NSISSettingsEditorPage
{
    private TableViewer mViewer;
    private Button mReassociateHeaderWarning;
    private Collection<IFile> mOriginalHeaders;
    private HashSet<IFile> mHeaders;
    private NSISHeaderAssociationManager mHeaderAssociationManager = NSISHeaderAssociationManager.getInstance();

    public NSISAssociatedHeadersPropertyPage(NSISSettings settings)
    {
        super("headers", settings); //$NON-NLS-1$
    }

    @Override
    public boolean canEnableControls()
    {
        return true;
    }

    @Override
    public void enableControls(boolean state)
    {
    }

    @Override
    public boolean supportsEnablement()
    {
        return false;
    }

    @Override
    public Control createControl(final Composite parent)
    {
        mOriginalHeaders = mHeaderAssociationManager.getAssociatedHeaders((IFile)((NSISProperties)mSettings).getResource());
        mHeaders = new HashSet<IFile>();
        initHeaders();
        final IFilter filter = new IFilter() {
            public boolean select(Object toTest)
            {
                if(toTest instanceof IFile) {
                    String ext = ((IFile)toTest).getFileExtension();
                    if (ext != null && ext.equalsIgnoreCase(INSISConstants.NSH_EXTENSION)) {
                        return mHeaders != null && !mHeaders.contains(toTest);
                    }
                }
                return false;
            }
        };

        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        composite.setLayout(layout);

        Label l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("associated.headers.title")); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
        data.horizontalSpan = 2;
        l.setLayoutData(data);

        Table table = new Table(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn column = new TableColumn(table,SWT.LEFT,0);
        column.setText(EclipseNSISPlugin.getResourceString("associated.headers.column.label")); //$NON-NLS-1$
        table.addControlListener(new TableResizer());

        mViewer = new TableViewer(table);
        mViewer.setContentProvider(new CollectionContentProvider());
        mViewer.setLabelProvider(new CollectionLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex)
            {
                if(element instanceof IFile) {
                    return ((IFile)element).getFullPath().toString();
                }
                return null;
            }
        });
        mViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2)
            {
                if(e1 instanceof IFile && e2 instanceof IFile) {
                    return ((IFile)e1).getFullPath().toString().compareTo(((IFile)e2).getFullPath().toString());
                }
                return super.compare(viewer, e1, e2);
            }
        });
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.verticalSpan = 2;
        table.setLayoutData(data);
        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setImage(CommonImages.ADD_ICON);
        addButton.setToolTipText(EclipseNSISPlugin.getResourceString("add.associated.header.toolip")); //$NON-NLS-1$
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                FileSelectionDialog dialog = new FileSelectionDialog(parent.getShell(), ((NSISProperties)mSettings).getResource().getParent(), filter);
                dialog.setDialogMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
                dialog.setHelpAvailable(false);
                if (dialog.open() == Window.OK) {
                    IFile file = dialog.getFile();
                    IFile script = mHeaderAssociationManager.getAssociatedScript(file);
                    if(script != null && !script.equals(((NSISProperties)mSettings).getResource()) &&
                            mReassociateHeaderWarning.getSelection()) {

                        MessageDialogWithToggle dlg = new MessageDialogWithToggle(parent.getShell(),
                                EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                EclipseNSISPlugin.getShellImage(),
                                EclipseNSISPlugin.getFormattedString("associated.header.warning", //$NON-NLS-1$
                                        new String[] {file.getFullPath().toString(),script.getFullPath().toString()}),
                                MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0,
                                EclipseNSISPlugin.getResourceString("associated.header.toggle.message"),false); //$NON-NLS-1$
                        dlg.open();
                        if(dialog.getReturnCode() == IDialogConstants.OK_ID) {
                            mReassociateHeaderWarning.setSelection(!dlg.getToggleState());
                        }
                        else {
                            return;
                        }
                    }
                    if(!mHeaders.contains(file)) {
                        mHeaders.add(file);
                        mViewer.refresh(false);
                    }
                }
            }
        });
        addButton.setLayoutData(new GridData(SWT.FILL,SWT.TOP,false,false));

        final Button removeButton = new Button(composite, SWT.PUSH);
        removeButton.setImage(CommonImages.DELETE_ICON);
        removeButton.setToolTipText(EclipseNSISPlugin.getResourceString("remove.associated.header.toolip")); //$NON-NLS-1$
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0)
            {
                IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                if(!sel.isEmpty()) {
                    mHeaders.removeAll(sel.toList());
                    mViewer.refresh(false);
                }
            }
        });
        data = new GridData(SWT.FILL,SWT.TOP,false,false);
        data.verticalSpan = 2;
        removeButton.setLayoutData(data);

        Composite c = new Composite(composite,SWT.NONE);
        data = new GridData(SWT.FILL,SWT.FILL,true,false);
        c.setLayoutData(data);
        layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        c.setLayout(layout);

        mReassociateHeaderWarning = new Button(c,SWT.CHECK);
        mReassociateHeaderWarning.setLayoutData(new GridData(SWT.FILL,SWT.TOP,false,false));
        mReassociateHeaderWarning.setSelection(NSISPreferences.getInstance().getPreferenceStore().getBoolean(INSISPreferenceConstants.WARN_REASSOCIATE_HEADER));
        l = new Label(c,SWT.WRAP);
        l.setText(EclipseNSISPlugin.getResourceString("show.associated.header.warning.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,true,false));

        mViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
            }
        });

        mViewer.setInput(mHeaders);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_assochdrproperties_context"); //$NON-NLS-1$
        return composite;
    }

    /**
     *
     */
    private void initHeaders()
    {
        mHeaders.clear();
        for (Iterator<IFile> iter = mOriginalHeaders.iterator(); iter.hasNext();) {
            IFile header = iter.next();
            if(IOUtility.isValidFile(header)) {
                mHeaders.add(header);
            }
        }
    }

    @Override
    public void reset()
    {
        if(mViewer != null && mHeaders != null) {
            initHeaders();
            mViewer.refresh(false);
        }
        if(mReassociateHeaderWarning != null) {
            mReassociateHeaderWarning.setSelection(NSISPreferences.getInstance().getPreferenceStore().getBoolean(INSISPreferenceConstants.WARN_REASSOCIATE_HEADER));
        }
    }

    @Override
    public void setDefaults()
    {
        if(mViewer != null && mHeaders != null) {
            mHeaders.clear();
            mViewer.refresh(false);
        }
        if(mReassociateHeaderWarning != null) {
            mReassociateHeaderWarning.setSelection(true);
        }
    }

    @Override
    protected boolean performApply(NSISSettings settings)
    {
        IFile file = (IFile)((NSISProperties)mSettings).getResource();
        Set<IFile> removedHeaders = new HashSet<IFile>(mOriginalHeaders);
        removedHeaders.removeAll(mHeaders);
        Set<IFile> addedHeaders = new HashSet<IFile>(mHeaders);
        addedHeaders.removeAll(mOriginalHeaders);
        for (Iterator<IFile> iter = removedHeaders.iterator(); iter.hasNext();) {
            mHeaderAssociationManager.disassociateFromScript(iter.next());
        }
        for (Iterator<IFile> iter = addedHeaders.iterator(); iter.hasNext();) {
            mHeaderAssociationManager.associateWithScript(iter.next(), file);
        }
        NSISPreferences.getInstance().getPreferenceStore().setValue(INSISPreferenceConstants.WARN_REASSOCIATE_HEADER, mReassociateHeaderWarning.getSelection());
        return true;
    }
}
