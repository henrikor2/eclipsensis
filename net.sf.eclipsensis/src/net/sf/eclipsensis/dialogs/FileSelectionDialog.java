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
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.viewer.EmptyContentProvider;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class FileSelectionDialog extends TitleAreaDialog
{
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final int VIEWER_WIDTH = 200;
    private static final int VIEWER_HEIGHT = 300;

    private IFilter mFilter = null;
    private IContainer mContainer = null;
    private IFile mFile = null;
    private String mDialogTitle;
    private String mDialogHeader;
    private String mDialogMessage;

    public FileSelectionDialog(Shell parentShell, IResource resource, IFilter filter)
    {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        if(resource instanceof IFile) {
            mFile = (IFile)resource;
            mContainer = mFile.getParent();
        }
        else {
            mFile = null;
            mContainer = (IContainer)resource;
        }
        mFilter = filter;
    }

    public FileSelectionDialog(Shell parentShell, IResource resource)
    {
        this(parentShell, resource, null);
    }

    public FileSelectionDialog(Shell parentShell, IFilter filter)
    {
        this(parentShell, null, filter);
    }

    public FileSelectionDialog(Shell parentShell)
    {
        this(parentShell, null, null);
    }

    public IFile getFile()
    {
        return mFile;
    }

    public String getDialogHeader()
    {
        return mDialogHeader;
    }

    public void setDialogHeader(String dialogHeader)
    {
        mDialogHeader = dialogHeader;
    }

    public String getDialogMessage()
    {
        return mDialogMessage;
    }

    public void setDialogMessage(String dialogMessage)
    {
        mDialogMessage = dialogMessage;
    }

    public String getDialogTitle()
    {
        return mDialogTitle;
    }

    public void setDialogTitle(String dialogTitle)
    {
        mDialogTitle = dialogTitle;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(mDialogTitle==null?EclipseNSISPlugin.getResourceString("fileselection.dialog.title"):mDialogTitle); //$NON-NLS-1$
        shell.setImage(EclipseNSISPlugin.getShellImage());
    }

    @Override
    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);
        setTitle(mDialogHeader==null?EclipseNSISPlugin.getResourceString("fileselection.dialog.header"):mDialogHeader); //$NON-NLS-1$
        setTitleImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("file.selection.dialog.icon"))); //$NON-NLS-1$
        setMessage(mDialogMessage==null?EclipseNSISPlugin.getResourceString("fileselection.dialog.message"):mDialogMessage); //$NON-NLS-1$

        Button button = getButton(IDialogConstants.OK_ID);
        if(button != null) {
            button.setEnabled(mFile != null);
        }
        return contents;
    }
    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite parent2 = (Composite)super.createDialogArea(parent);
        Composite composite = new Composite(parent2,SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        GridLayout layout = new GridLayout(1,true);
        composite.setLayout(layout);

        SashForm form = new SashForm(composite,SWT.HORIZONTAL);
        form.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

        layout = new GridLayout(1,true);
        form.setLayout(layout);

        composite = new Composite(form,SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(1,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("fileselection.parent.folder.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        Tree tree = new Tree(composite,SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
        gridData.widthHint = VIEWER_WIDTH;
        gridData.heightHint = VIEWER_HEIGHT;
        tree.setLayoutData(gridData);
        final TreeViewer tv = new TreeViewer(tree);
        tv.setContentProvider(new ContainerContentProvider());
        tv.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        tv.setSorter(new ViewerSorter());


        composite = new Composite(form,SWT.None);
        composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        layout = new GridLayout(1,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("fileselection.file.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        Table table = new Table(composite,SWT.SINGLE|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER|SWT.FULL_SELECTION);
        gridData = new GridData(SWT.FILL,SWT.FILL,true,true);
        gridData.widthHint = VIEWER_WIDTH;
        gridData.heightHint = VIEWER_HEIGHT;
        table.setLayoutData(gridData);
        table.setLinesVisible(false);
        final TableViewer tv2 = new TableViewer(table);
        tv2.setContentProvider(new FilesContentProvider());
        tv2.setLabelProvider(new FilesLabelProvider());
        tv2.setSorter(new ViewerSorter());
        if(mFilter != null) {
            tv2.addFilter(new ViewerFilter() {
                @Override
                public boolean select(Viewer viewer, Object parentElement, Object element)
                {
                    return mFilter.select(element);
                }
            });
        }

        tv.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                tv2.setInput(selection.getFirstElement()); // allow null
            }
        });
        tv.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object item = ((IStructuredSelection) selection)
                            .getFirstElement();
                    if (tv.getExpandedState(item)) {
                        tv.collapseToLevel(item, 1);
                    }
                    else {
                        tv.expandToLevel(item, 1);
                    }
                }
            }
        });

        tv2.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if(selection != null && !selection.isEmpty()) {
                    mFile = (IFile)selection.getFirstElement();
                }
                else {
                    mFile = null;
                }
                Button button = getButton(IDialogConstants.OK_ID);
                if(button != null) {
                    button.setEnabled(selection != null && !selection.isEmpty());
                }
            }
        });
        tv2.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                if(mFile != null) {
                    setReturnCode(Window.OK);
                    close();
                }
            }
        });

        tv.setInput(ResourcesPlugin.getWorkspace());
        if(mContainer != null) {
            tv.setSelection(new StructuredSelection(mContainer));
            if(mFile != null) {
                tv2.setSelection(new StructuredSelection(mFile));
            }
            else {
                tv2.setSelection(new StructuredSelection());
            }
        }
        return parent2;
    }

    private class ContainerContentProvider extends EmptyContentProvider
    {
        @Override
        public Object[] getChildren(Object element)
        {
            if (element instanceof IWorkspace) {
                return ((IWorkspace) element).getRoot().getProjects();
            }
            else if (element instanceof IContainer) {
                IContainer container = (IContainer) element;
                if (container.isAccessible()) {
                    try {
                        List<IResource> children = new ArrayList<IResource>();
                        IResource[] members = container.members();
                        for (int i = 0; i < members.length; i++) {
                            if (members[i].getType() != IResource.FILE) {
                                children.add(members[i]);
                            }
                        }
                        return children.toArray();
                    }
                    catch (CoreException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
            return EMPTY_ARRAY;
        }

        @Override
        public Object[] getElements(Object element)
        {
            return getChildren(element);
        }

        @Override
        public Object getParent(Object element)
        {
            if (element instanceof IResource) {
                return ((IResource) element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element)
        {
            return getChildren(element).length > 0;
        }
    }

    private class FilesLabelProvider extends LabelProvider implements ITableLabelProvider
    {
        private ILabelProvider mLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();

        public Image getColumnImage(Object element, int columnIndex)
        {
            return mLabelProvider.getImage(element);
        }

        public String getColumnText(Object element, int columnIndex)
        {
            return mLabelProvider.getText(element);
        }
    }

    private class FilesContentProvider extends EmptyContentProvider
    {
        @Override
        public Object[] getElements(Object element)
        {
            if (element instanceof IContainer) {
                IContainer container = (IContainer) element;
                if (container.isAccessible()) {
                    try {
                        List<IResource> children = new ArrayList<IResource>();
                        IResource[] members = container.members();
                        for (int i = 0; i < members.length; i++) {
                            if (members[i].getType() == IResource.FILE) {
                                children.add(members[i]);
                            }
                        }
                        return children.toArray();
                    }
                    catch (CoreException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
            return EMPTY_ARRAY;
        }
    }
}
