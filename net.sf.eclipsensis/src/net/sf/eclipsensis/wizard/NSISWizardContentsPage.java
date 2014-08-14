/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.beans.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;
import net.sf.eclipsensis.viewer.StructuredViewerUpDownMover;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardContentsPage extends AbstractNSISWizardPage implements INSISConstants
{
    public static final String NAME = "nsisWizardContents"; //$NON-NLS-1$

    private static final int[] cDeleteConfirmButtonIds = {IDialogConstants.YES_ID, IDialogConstants.YES_TO_ALL_ID, IDialogConstants.NO_ID, IDialogConstants.NO_TO_ALL_ID};

    private static final String cDeleteConfirmTitle = EclipseNSISPlugin.getResourceString("delete.confirmation.title"); //$NON-NLS-1$

    private static final String cDeleteConfirmMessageFormat = EclipseNSISPlugin.getResourceString("delete.confirmation.message"); //$NON-NLS-1$

    private TreeViewer mContentsTreeViewer;

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardContentsPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.contents.title"), //$NON-NLS-1$
                        EclipseNSISPlugin.getResourceString("wizard.contents.description")); //$NON-NLS-1$
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return false;
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizcontents_context"; //$NON-NLS-1$
    }

    public void refresh()
    {
        if(mContentsTreeViewer != null) {
            mContentsTreeViewer.refresh();
        }
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        final Composite composite = new Composite(parent, SWT.NONE);

        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();

        Label contentsLabel = NSISWizardDialogUtil.createLabel(composite,"wizard.contents.text",true,null,false); //$NON-NLS-1$
        Dialog.applyDialogFont(contentsLabel);
        final GridData gridData = (GridData)contentsLabel.getLayoutData();
        gridData.widthHint = Common.calculateControlSize(contentsLabel,80,0).x;
        composite.addListener (SWT.Resize,  new Listener () {
            boolean init = false;

            public void handleEvent (Event e) {
                if(init) {
                    Point size = composite.getSize();
                    gridData.widthHint = size.x - 2*layout.marginWidth;
                    composite.layout();
                }
                else {
                    init=true;
                }
            }
        });

        Group contentsGroup = NSISWizardDialogUtil.createGroup(composite,1,"",null,false); //$NON-NLS-1$
        GridData gd = (GridData)contentsGroup.getLayoutData();
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = GridData.FILL;

        final ToolBar contentsToolbar = createToolBar(contentsGroup, bundle);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 1;
        contentsToolbar.setLayoutData(gd);

        final ToolItem addToolItem = contentsToolbar.getItem(0);
        final ToolItem editToolItem = contentsToolbar.getItem(1);
        final ToolItem deleteToolItem = contentsToolbar.getItem(2);

        Composite composite2 = new Composite(contentsGroup,SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 1;
        composite2.setLayoutData(gd);

        GridLayout layout2 = new GridLayout(2,false);
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        composite2.setLayout(layout2);

        final Tree contentsTree = new Tree(composite2,SWT.MULTI|SWT.BORDER);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 1;
        contentsTree.setLayoutData(gd);

        updateSelectComponents();
        mContentsTreeViewer = new TreeViewer(contentsTree);
        mContentsTreeViewer.setLabelProvider(new NSISInstallElementLabelProvider());
        mContentsTreeViewer.setContentProvider(new NSISInstallElementTreeContentProvider(settings));
        mContentsTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        mContentsTreeViewer.setInput(settings);
        ColumnViewerToolTipSupport.enableFor(mContentsTreeViewer);

        final StructuredViewerUpDownMover<NSISWizardSettings, INSISInstallElement> mover = new StructuredViewerUpDownMover<NSISWizardSettings, INSISInstallElement>() {
            private TreeViewer mTreeViewer = null;

            @Override
            public void setViewer(StructuredViewer viewer)
            {
                mTreeViewer = (TreeViewer)viewer;
            }

            @Override
            public StructuredViewer getViewer()
            {
                return mTreeViewer;
            }

            @Override
            protected int[] getSelectedIndices()
            {
                List<INSISInstallElement> list = getSelectionList(mTreeViewer);
                INSISInstallElement parent = getSelectionParent(list);
                if(parent != null) {
                    List<INSISInstallElement> allElements = getAllElements(parent);
                    int[] selectedIndices = new int[list.size()];
                    int i=0;
                    for(Iterator<INSISInstallElement> iter=list.iterator(); iter.hasNext(); ) {
                        selectedIndices[i++] = allElements.indexOf(iter.next());
                    }

                    return selectedIndices;
                }
                return new int[0];
            }

            private List<INSISInstallElement> getAllElements(INSISInstallElement parent)
            {
                if(parent != null) {
                    INSISInstallElement[] children = parent.getChildren();
                    if(!Common.isEmptyArray(children)) {
                        return Arrays.asList(children);
                    }
                }
                return Collections.<INSISInstallElement>emptyList();
            }

            @Override
            protected List<INSISInstallElement> getAllElements()
            {
                return getAllElements(getSelectionParent(getSelectionList(mTreeViewer)));
            }

            @Override
            protected void updateStructuredViewerInput(NSISWizardSettings input, List<INSISInstallElement> elements, List<INSISInstallElement> move, boolean isDown)
            {
                INSISInstallElement parent = getSelectionParent(move);
                if(parent != null && parent instanceof AbstractNSISInstallGroup) {
                    parent.removeAllChildren();
                    for (Iterator<INSISInstallElement> iter = elements.iterator(); iter.hasNext();) {
                        INSISInstallElement element = iter.next();
                        parent.addChild(element);
                    }
                    mTreeViewer.refresh(parent,true);
                    expandGroup(mTreeViewer, (AbstractNSISInstallGroup)parent);
                }
            }

            /**
             * @param viewer
             */
            @Override
            protected void refreshViewer(StructuredViewer viewer, List<INSISInstallElement> elements, List<INSISInstallElement> move, boolean isDown)
            {
            }
        };
        mover.setViewer(mContentsTreeViewer);

        Composite composite3 = new Composite(composite2,SWT.NONE);
        gd = new GridData();
        composite3.setLayoutData(gd);

        layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        composite3.setLayout(layout2);

        final Button upButton = new Button(composite3,SWT.PUSH);
        upButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        upButton.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        upButton.setEnabled(mover.canMoveUp());
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveUp();
            }
        });

        final Button downButton = new Button(composite3,SWT.PUSH);
        downButton.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
        downButton.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        downButton.setEnabled(mover.canMoveDown());
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mover.moveDown();
            }
        });

        mContentsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                enableItems(event.getSelection(),addToolItem,editToolItem,deleteToolItem);
                upButton.setEnabled(mover.canMoveUp());
                downButton.setEnabled(mover.canMoveDown());
            }
        });

        mContentsTreeViewer.addTreeListener(new ITreeViewerListener() {
            public void setState(TreeExpansionEvent event, boolean state)
            {
                Object element = event.getElement();
                if(element instanceof AbstractNSISInstallGroup) {
                    ((AbstractNSISInstallGroup)element).setExpanded(state);
                }
            }

            public void treeCollapsed(TreeExpansionEvent event)
            {
                setState(event, false);
            }

            public void treeExpanded(TreeExpansionEvent event)
            {
                setState(event, true);
            }
        });

        configureDND(mContentsTreeViewer);

        SelectionAdapter editSelectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                ISelection sel = mContentsTreeViewer.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        editElement(composite, mContentsTreeViewer, (INSISInstallElement)obj);
                        updateSelectComponents();
                        checkUnselectedSections();
                    }
                }
            }
        };

        SelectionAdapter deleteSelectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                ISelection sel = mContentsTreeViewer.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    deleteElements(mContentsTreeViewer, sel);
                    updateSelectComponents();
                }
            }
        };

        final SelectionAdapter addSelectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                MenuItem mi = (MenuItem)e.widget;
                String type = (String)mi.getData();
                INSISInstallElement element = NSISInstallElementFactory.create(type);
                if(element != null) {
                    try {
                        if(element.isEditable()) {
                            ISelection se = mContentsTreeViewer.getSelection();
                            if(!se.isEmpty() && se instanceof IStructuredSelection) {
                                Object obj = ((IStructuredSelection)se).getFirstElement();
                                if(obj instanceof INSISInstallElement) {
                                    INSISInstallElement parent = (INSISInstallElement)obj;
                                    if(element.edit(mWizard)) {
                                        if(parent.addChild(element)) {
                                            mContentsTreeViewer.refresh(parent,true);
                                            mContentsTreeViewer.reveal(element);
                                            if(element.hasChildren()) {
                                                mContentsTreeViewer.expandToLevel(element,AbstractTreeViewer.ALL_LEVELS);
                                            }
                                            updateSelectComponents();
                                            setPageComplete(validatePage(VALIDATE_ALL));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception ex) {
                        delayedValidateAfterError(ex.getLocalizedMessage(),2000);
                    }
                }
            }
        };

        final Menu addPopupMenu = new Menu(getShell(),SWT.POP_UP);
        addToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Rectangle rect = addToolItem.getBounds();
                Point pt = new Point (rect.x, rect.y + rect.height);
                pt = contentsToolbar.toDisplay (pt);
                updateAddMenu(addPopupMenu, mContentsTreeViewer.getSelection(),addSelectionAdapter);
                addPopupMenu.setLocation(pt.x, pt.y);
                addPopupMenu.setVisible(true);
            }
        });
        editToolItem.addSelectionListener(editSelectionAdapter);
        deleteToolItem.addSelectionListener(deleteSelectionAdapter);
        contentsToolbar.getItem(3).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mContentsTreeViewer.expandAll();
            }
        });
        contentsToolbar.getItem(4).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mContentsTreeViewer.collapseAll();
            }
        });

        final Menu itemPopupMenu = createMenu(bundle);
        final Menu addDropdownMenu = new Menu(getShell(),SWT.DROP_DOWN);

        final MenuItem addMenuItem = itemPopupMenu.getItem(0);
        final MenuItem editMenuItem = itemPopupMenu.getItem(1);
        final MenuItem deleteMenuItem = itemPopupMenu.getItem(2);

        addMenuItem.setMenu(addDropdownMenu);
        editMenuItem.addSelectionListener(editSelectionAdapter);
        deleteMenuItem.addSelectionListener(deleteSelectionAdapter);

        contentsTree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                ISelection sel = mContentsTreeViewer.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    IStructuredSelection ssel = (IStructuredSelection)sel;
                    Object obj = ssel.getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        editElement(composite, mContentsTreeViewer, (INSISInstallElement)obj);
                        updateSelectComponents();
                        checkUnselectedSections();
                    }
                }
            }
        });

        contentsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if(e.button == 3) {
                    TreeItem ti = contentsTree.getItem(new Point(e.x,e.y));
                    if(ti != null) {
                        ISelection sel = mContentsTreeViewer.getSelection();
                        enableItems(sel,addMenuItem,editMenuItem,deleteMenuItem);
                        updateAddMenu(addDropdownMenu, sel,addSelectionAdapter);
                        Point pt = contentsTree.toDisplay(e.x,e.y);
                        itemPopupMenu.setLocation(pt.x, pt.y);
                        itemPopupMenu.setVisible(true);
                    }
                }
            }
        });

        contentsTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.character == SWT.DEL) {
                    if(e.stateMask == 0) {
                        ISelection sel = mContentsTreeViewer.getSelection();
                        if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                            if(canDeleteElements((IStructuredSelection)sel)) {
                                deleteElements(mContentsTreeViewer, sel);
                                updateSelectComponents();
                            }
                        }

                    }
                }
            }
        });

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                checkUnselectedSections();
            }
        });

        final PropertyChangeListener propertyListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(NSISWizardSettings.TARGET_PLATFORM.equals(evt.getPropertyName())) {
                    mContentsTreeViewer.refresh(true);
                }
            }
        };
        settings.addPropertyChangeListener(propertyListener);
        final INSISWizardSettingsListener listener = new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, final NSISWizardSettings newSettings)
            {
                if(oldSettings != null) {
                    oldSettings.removePropertyChangeListener(propertyListener);
                }
                mContentsTreeViewer.setInput(newSettings);
                mContentsTreeViewer.expandToLevel(newSettings.getInstaller(), AbstractTreeViewer.ALL_LEVELS);
                newSettings.addPropertyChangeListener(propertyListener);
            }
        };
        mWizard.addSettingsListener(listener);

        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                mWizard.removeSettingsListener(listener);
                mWizard.getSettings().removePropertyChangeListener(propertyListener);
            }
        });

        setPageComplete(validatePage(VALIDATE_ALL));

        return composite;
    }

    private void expandGroup(TreeViewer tv, AbstractNSISInstallGroup group)
    {
        if(tv.getExpandedState(group) != group.isExpanded()) {
            tv.setExpandedState(group,group.isExpanded());
        }
        INSISInstallElement[] children = group.getChildren();
        if(!Common.isEmptyArray(children)) {
            for (int i = 0; i < children.length; i++) {
                if(children[i] instanceof AbstractNSISInstallGroup) {
                    expandGroup(tv, (AbstractNSISInstallGroup)children[i]);
                }
            }
        }
    }

    private void configureDND(final TreeViewer tv)
    {
        Transfer[] types = new Transfer[] {ContentsTransfer.INSTANCE};
        int operations = DND.DROP_MOVE | DND.DROP_COPY;
        tv.addDragSupport(operations, types, new DragSourceListener() {
            public void dragFinished(DragSourceEvent event)
            {
            }

            public void dragSetData(DragSourceEvent event)
            {
                List<INSISInstallElement> selection = getSelectionList(tv);
                INSISInstallElement parent = getSelectionParent(selection);
                if(parent == null) {
                    event.data = null;
                }
                else {
                    event.data = new ContentsTransferData(parent,selection);
                }
            }

            public void dragStart(DragSourceEvent event)
            {
                List<INSISInstallElement> selection = getSelectionList(tv);
                INSISInstallElement parent = getSelectionParent(selection);
                if(parent == null) {
                    event.doit = false;
                }
            }
        });
        tv.addDropSupport(operations, new Transfer[] {ContentsTransfer.INSTANCE,FileTransfer.getInstance()}, new DropTargetAdapter() {
            private int determineOperation(String[] files, String[] types, INSISInstallElement target, DropTargetEvent event, int defaultFeedback)
            {
                if(target == null) {
                    return DND.DROP_NONE;
                }
                int operation = DND.DROP_COPY;
                boolean filesOnly = true;
                for (int i = 0; i < files.length; i++) {
                    if(IOUtility.isValidFile(files[i])) {
                        if(files[i].regionMatches(true, files[i].length()-REG_FILE_EXTENSION.length(), REG_FILE_EXTENSION, 0, REG_FILE_EXTENSION.length())) {
                            types[i] = NSISInstallRegistryValue.TYPE;
                        }
                        else {
                            types[i] = NSISInstallFile.TYPE;
                        }
                    }
                    else {
                        //Directory
                        filesOnly = false;
                        types[i] = NSISInstallDirectory.TYPE;
                    }
                }
                NSISInstallFiles.FileItem fileItem = null;
                if(filesOnly) {
                    fileItem = new NSISInstallFiles.FileItem();
                }
                for (int i = 0; i < types.length; i++) {
                    if(!target.acceptsChildType(types[i])) {
                        if(types[i].equals(NSISInstallRegistryValue.TYPE)) {
                            types[i] = NSISInstallFile.TYPE;
                            if(target.acceptsChildType(types[i])) {
                                continue;
                            }
                        }
                        if(fileItem != null && types[i].equals(NSISInstallFile.TYPE) && filesOnly) {
                            fileItem.setName(files[i]);
                            if(target.canAddChild(fileItem)) {
                                types[i] = NSISInstallFiles.FileItem.FILEITEM_TYPE;
                                continue;
                            }
                        }
                        operation = DND.DROP_NONE;
                        break;
                    }
                }
                return operation;
            }

            private int determineOperation(List<INSISInstallElement> selection, INSISInstallElement oldParent, INSISInstallElement newParent, DropTargetEvent event, int defaultFeedback)
            {
                int operation = DND.DROP_NONE;
                if(newParent != null) {
                    boolean shift = WinAPI.INSTANCE.getKeyState(WinAPI.VK_SHIFT) < 0;
                    boolean ctrl = WinAPI.INSTANCE.getKeyState(WinAPI.VK_CTRL) < 0;
                    if(ctrl && shift) {
                        operation = DND.DROP_NONE;
                    }
                    else {
                        if(ctrl) {
                            operation = DND.DROP_COPY;
                        }
                        else if(shift) {
                            operation = DND.DROP_MOVE;
                        }
                        else {
                            operation = DND.DROP_DEFAULT;
                        }
                    }
                    if(newParent != oldParent || operation == DND.DROP_COPY) {
                        for (Iterator<INSISInstallElement> iter = selection.iterator(); iter.hasNext();) {
                            if(!newParent.canAddChild(iter.next())) {
                                operation = DND.DROP_NONE;
                                break;
                            }
                        }
                    }
                    if(operation != DND.DROP_NONE && event != null) {
                        event.feedback |= defaultFeedback;
                    }
                }
                return operation;
            }

            @Override
            public void dragOver(DropTargetEvent event)
            {
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
                int detail = DND.DROP_NONE;
                if (event.item instanceof TreeItem) {
                    Tree tree = tv.getTree();
                    TreeItem item = (TreeItem)event.item;
                    Object data = item.getData();
                    if(data instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)data;
                        if(ContentsTransfer.INSTANCE.isSupportedType(event.currentDataType)) {
                            ContentsTransferData transferData = (ContentsTransferData)ContentsTransfer.INSTANCE.nativeToJava(event.currentDataType);
                            if(transferData.parent != null) {
                                Point pt = tree.getDisplay().map(null, tree, event.x, event.y);
                                Rectangle bounds = item.getBounds();
                                if (pt.y < bounds.y + bounds.height/3) {
                                    detail = determineOperation(transferData.selection, transferData.parent, element.getParent(), event, DND.FEEDBACK_INSERT_BEFORE);
                                }
                                else if (pt.y > bounds.y + 2*bounds.height/3) {
                                    detail = determineOperation(transferData.selection, transferData.parent, element.getParent(), event, DND.FEEDBACK_INSERT_AFTER);
                                }
                                else {
                                    detail = determineOperation(transferData.selection, transferData.parent, element, event, DND.FEEDBACK_SELECT);
                                }
                            }
                        }
                        else if(FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                            Object transferData = FileTransfer.getInstance().nativeToJava(event.currentDataType);
                            if(transferData instanceof String[]) {
                                String[] files = (String[])transferData;
                                String[] types = new String[files.length];
                                Point pt = tree.getDisplay().map(null, tree, event.x, event.y);
                                Rectangle bounds = item.getBounds();
                                if (pt.y < bounds.y + bounds.height/3) {
                                    detail = determineOperation(files, types, element.getParent(), event, DND.FEEDBACK_INSERT_BEFORE);
                                }
                                else if (pt.y > bounds.y + 2*bounds.height/3) {
                                    detail = determineOperation(files, types, element.getParent(), event, DND.FEEDBACK_INSERT_AFTER);
                                }
                                else {
                                    detail = determineOperation(files, types, element, event, DND.FEEDBACK_SELECT);
                                }
                            }
                        }
                    }
                }
                event.detail = detail;
            }

            private void doDrop(List<INSISInstallElement> selection, int detail, INSISInstallElement oldParent, INSISInstallElement newParent, int index)
            {
                int index2 = index;
                try {
                    for (Iterator<INSISInstallElement>  iter = selection.iterator(); iter.hasNext();) {
                        INSISInstallElement el = iter.next();
                        switch(detail) {
                            case DND.DROP_COPY:
                                el = (INSISInstallElement)el.clone();
                                //$FALL-THROUGH$
                            case DND.DROP_DEFAULT:
                            case DND.DROP_MOVE:
                                if(oldParent == newParent) {
                                    int n = oldParent.indexOf(el);
                                    if(n < index2) {
                                        index2--;
                                    }
                                    oldParent.removeChild(el);
                                }
                                newParent.addChild(index2++,el);
                        }
                    }
                    setPageComplete(validatePage(VALIDATE_ALL));
                }
                catch(Exception ex) {
                    delayedValidateAfterError(ex.getLocalizedMessage(),2000);
                }
                finally {
                    tv.refresh(true);
                    if(newParent instanceof AbstractNSISInstallGroup) {
                        ((AbstractNSISInstallGroup)newParent).setExpanded(true);
                        expandGroup(tv, (AbstractNSISInstallGroup)newParent);
                    }
                }
            }

            private void doDrop(String[] files, String[] types, INSISInstallElement target, int index)
            {
                int index2 = index;
                try {
                    RegistryImporter importer = null;
                    RegistryImportStrategy strategy = null;

                    for (int i=0; i<files.length; i++) {
                        INSISInstallElement el = null;
                        if(types[i].equals(NSISInstallFile.TYPE)) {
                            el = new NSISInstallFile();
                            ((NSISInstallFile)el).setName(files[i]);
                        }
                        else if(types[i].equals(NSISInstallDirectory.TYPE)) {
                            el = new NSISInstallDirectory();
                            ((NSISInstallDirectory)el).setName(files[i]);
                        }
                        else if(types[i].equals(NSISInstallFiles.FileItem.FILEITEM_TYPE)) {
                            el = new NSISInstallFiles.FileItem();
                            ((NSISInstallFiles.FileItem)el).setName(files[i]);
                        }
                        else if(types[i].equals(NSISInstallRegistryValue.TYPE)) {
                            try {
                                if(importer == null || strategy == null) {
                                    importer = new RegistryImporter();
                                    strategy = new RegistryImportStrategy();
                                }
                                else {
                                    strategy.reset();
                                }
                                importer.importRegFile(getShell(), files[i], strategy);
                                List<NSISInstallRegistryItem> list = strategy.getRegistryItems();
                                if(!Common.isEmptyCollection(list)) {
                                    for (Iterator<NSISInstallRegistryItem> iter = list.iterator(); iter.hasNext();) {
                                        NSISInstallRegistryItem element = iter.next();
                                        target.addChild(index2++, element);
                                    }
                                }
                                continue;
                            }
                            catch(Exception ex) {
                            }
                            el = new NSISInstallFile();
                            ((NSISInstallFile)el).setName(files[i]);
                        }
                        if(el != null) {
                            target.addChild(index2++,el);
                        }
                    }
                    setPageComplete(validatePage(VALIDATE_ALL));
                }
                catch(Exception ex) {
                    delayedValidateAfterError(ex.getLocalizedMessage(),2000);
                }
                finally {
                    tv.refresh(true);
                    if(target instanceof AbstractNSISInstallGroup) {
                        ((AbstractNSISInstallGroup)target).setExpanded(true);
                        expandGroup(tv, (AbstractNSISInstallGroup)target);
                    }
                }
            }

            @Override
            public void drop(DropTargetEvent event)
            {
                int detail = DND.DROP_NONE;

                if (event.item instanceof TreeItem) {
                    Tree tree = tv.getTree();
                    TreeItem item = (TreeItem)event.item;
                    Object data = item.getData();
                    if(data instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)data;
                        if (ContentsTransfer.INSTANCE.isSupportedType(event.currentDataType)) {
                            if (event.data instanceof ContentsTransferData) {
                                ContentsTransferData transferData = (ContentsTransferData)event.data;
                                if (transferData.parent != null) {
                                    Point pt = tree.getDisplay().map(null, tree, event.x, event.y);
                                    Rectangle bounds = item.getBounds();
                                    if (pt.y < bounds.y + bounds.height / 3) {
                                        detail = determineOperation(transferData.selection, transferData.parent, element.getParent(), null, 0);
                                        if (detail != DND.DROP_NONE) {
                                            doDrop(transferData.selection, detail, transferData.parent, element.getParent(), element.getParent().indexOf(element));
                                        }
                                    }
                                    else if (pt.y > bounds.y + 2 * bounds.height / 3) {
                                        detail = determineOperation(transferData.selection, transferData.parent, element.getParent(), null, 0);
                                        if (detail != DND.DROP_NONE) {
                                            doDrop(transferData.selection, detail, transferData.parent, element.getParent(), element.getParent().indexOf(element) + 1);
                                        }
                                    }
                                    else {
                                        detail = determineOperation(transferData.selection, transferData.parent, element, null, 0);
                                        if (detail != DND.DROP_NONE) {
                                            doDrop(transferData.selection, detail, transferData.parent, element, element.getChildCount());
                                        }
                                    }
                                }
                            }
                        }
                        else if(FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                            if(event.data instanceof String[]) {
                                String[] files = (String[])event.data;
                                String[] types = new String[files.length];
                                Point pt = tree.getDisplay().map(null, tree, event.x, event.y);
                                Rectangle bounds = item.getBounds();
                                if (pt.y < bounds.y + bounds.height/3) {
                                    detail = determineOperation(files, types, element.getParent(), event, DND.FEEDBACK_INSERT_BEFORE);
                                    if(detail != DND.DROP_NONE) {
                                        doDrop(files, types, element.getParent(), element.getParent().indexOf(element));
                                    }
                                }
                                else if (pt.y > bounds.y + 2*bounds.height/3) {
                                    detail = determineOperation(files, types, element.getParent(), event, DND.FEEDBACK_INSERT_AFTER);
                                    if(detail != DND.DROP_NONE) {
                                        doDrop(files, types, element.getParent(), element.getParent().indexOf(element)+1);
                                    }
                                }
                                else {
                                    detail = determineOperation(files, types, element, event, DND.FEEDBACK_SELECT);
                                    if(detail != DND.DROP_NONE) {
                                        doDrop(files, types, element, element.getChildCount());
                                    }
                                }
                            }
                        }
                    }
                }
                event.detail = detail;
            }
        });
    }

    private List<INSISInstallElement> getSelectionList(TreeViewer tv)
    {
        List<INSISInstallElement> list = new ArrayList<INSISInstallElement>();
        ISelection sel = tv.getSelection();
        if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
            for(Object o : ((IStructuredSelection)sel).toList())
            {
                list.add((INSISInstallElement) o);
            }
        }
        return list;
    }

    private INSISInstallElement getSelectionParent(List<INSISInstallElement> list)
    {
        INSISInstallElement parent = null;
        if(!Common.isEmptyCollection(list)) {
            for(Iterator<INSISInstallElement> iter=list.iterator(); iter.hasNext(); ) {
                INSISInstallElement element = iter.next();
                if(parent != null) {
                    if(!parent.equals(element.getParent())) {
                        return null;
                    }
                }
                else {
                    parent = element.getParent();
                    if(parent == null) {
                        return null;
                    }
                }
            }
        }
        return parent;
    }

    private boolean shouldSelectComponents(INSISInstallElement item)
    {
        if(item instanceof NSISSection) {
            return !((NSISSection)item).isHidden();
        }
        else if(item instanceof NSISSectionGroup) {
            INSISInstallElement[] items = item.getChildren();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(shouldSelectComponents(items[i])) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void updateSelectComponents()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if(settings.getInstallerType() != INSISWizardConstants.INSTALLER_TYPE_SILENT) {
            INSISInstallElement[] items = settings.getInstaller().getChildren();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(shouldSelectComponents(items[i])) {
                        settings.setSelectComponents(true);
                        return;
                    }
                }
            }
        }
        settings.setSelectComponents(false);
    }

    private void editElement(Composite composite, TreeViewer tv, INSISInstallElement element)
    {
        try {
            if(element.edit(mWizard)) {
                tv.refresh(element, true);
                if(element.hasChildren()) {
                    tv.expandToLevel(element,AbstractTreeViewer.ALL_LEVELS);
                }
                setPageComplete(validatePage(VALIDATE_ALL));
            }
        }
        catch(Exception ex) {
            delayedValidateAfterError(ex.getLocalizedMessage(),2000);
        }
    }

    /**
     * @param tv
     * @param sel
     */
    private void deleteElements(final TreeViewer tv, ISelection sel)
    {
        if(sel instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection)sel;
            if(!ssel.isEmpty()) {
                try {
                    int buttonId = -1;
                    for(Iterator<?> iter = ssel.iterator(); iter.hasNext(); ) {
                        INSISInstallElement element = (INSISInstallElement)iter.next();
                        if(element.hasChildren()) {
                            if(buttonId == IDialogConstants.NO_TO_ALL_ID) {
                                continue;
                            }
                            else if(buttonId != IDialogConstants.YES_TO_ALL_ID) {
                                int index = new MessageDialog(getShell(),cDeleteConfirmTitle,EclipseNSISPlugin.getShellImage(),
                                                MessageFormat.format(cDeleteConfirmMessageFormat,new Object[]{element.getDisplayName()}), MessageDialog.QUESTION,
                                                new String[]{IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL}, 0).open();
                                if(index >= 0) {
                                    buttonId = cDeleteConfirmButtonIds[index];
                                }
                                else {
                                    return;
                                }

                                if(buttonId == IDialogConstants.NO_ID || buttonId == IDialogConstants.NO_TO_ALL_ID) {
                                    continue;
                                }
                            }
                        }
                        INSISInstallElement parent = element.getParent();
                        if(parent != null) {
                            parent.removeChild(element);
                            tv.refresh(parent,true);
                        }
                    }
                    setPageComplete(validatePage(VALIDATE_ALL));
                }
                catch(Exception ex) {
                    delayedValidateAfterError(ex.getLocalizedMessage(),2000);
                }
                finally {
                    tv.refresh(false);
                }
            }
        }
    }

    /**
     * @param errorMessage
     */
    private void delayedValidateAfterError(String errorMessage, final int delay)
    {
        setErrorMessage(errorMessage);
        final Display display = getShell().getDisplay();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(delay);
                }
                catch(InterruptedException ie) {
                }
                display.asyncExec(new Runnable() {
                    public void run()
                    {
                        setPageComplete(validatePage(VALIDATE_ALL));
                    }
                });
            }
        },EclipseNSISPlugin.getResourceString("wizard.contents.validator.thread.name")).start(); //$NON-NLS-1$
    }

    private void enableItem(Item item, boolean state)
    {
        if(item instanceof ToolItem) {
            ((ToolItem)item).setEnabled(state);
        }
        else if(item instanceof MenuItem) {
            ((MenuItem)item).setEnabled(state);
        }
    }

    private void enableItems(ISelection sel, Item addItem, Item editItem, Item deleteItem)
    {
        if(sel.isEmpty()) {
            enableItem(addItem, false);
            enableItem(editItem, false);
            enableItem(deleteItem, false);
        }
        else if(sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)sel;
            if(selection.size() > 1) {
                enableItem(addItem, false);
                enableItem(editItem, false);
            }
            else {
                Object obj = selection.getFirstElement();
                if(obj instanceof INSISInstallElement) {
                    INSISInstallElement element = (INSISInstallElement)obj;
                    enableItem(addItem, !Common.isEmptyArray(element.getChildTypes()));
                    enableItem(editItem, element.isEditable());
                }
            }
            enableItem(deleteItem, canDeleteElements(selection));
        }
    }

    private boolean isAncestorOf(INSISInstallElement first, INSISInstallElement second)
    {
        INSISInstallElement second2 = second;
        while(!first.equals(second2)) {
            second2 = second2.getParent();
            if(second2 == null) {
                return false;
            }
        }
        return true;
    }
    /**
     * @param selection
     * @return
     */
    private boolean canDeleteElements(IStructuredSelection selection)
    {
        Set<INSISInstallElement> elements = new HashSet<INSISInstallElement>();
        if(selection.size() > 0) {
            if(selection.size() > 1) {
                for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                    Object obj = iter.next();
                    if(obj instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)obj;
                        if(!element.isRemovable()) {
                            return false;
                        }
                        else {
                            for (Iterator<INSISInstallElement> iterator = elements.iterator(); iterator.hasNext();) {
                                INSISInstallElement element2 = iterator.next();
                                if(isAncestorOf(element,element2) || isAncestorOf(element2, element)) {
                                    return false;
                                }
                            }
                            elements.add(element);
                        }
                    }
                }
                return true;
            }
            else {
                Object obj = selection.getFirstElement();
                if(obj instanceof INSISInstallElement) {
                    return ((INSISInstallElement)obj).isRemovable();
                }
            }
        }
        return false;
    }

    private void updateAddMenu(Menu addPopupMenu, ISelection selection, SelectionAdapter adapter)
    {
        if(!selection.isEmpty()) {
            if(selection instanceof IStructuredSelection) {
                IStructuredSelection ssel = (IStructuredSelection)selection;
                if(ssel.size() == 1) {
                    Object obj = ssel.getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)obj;
                        String[] childTypes = element.getChildTypes();
                        if(!Common.isEmptyArray(childTypes)) {
                            int n = addPopupMenu.getItemCount();
                            for(int i=0; i<n; i++) {
                                addPopupMenu.getItem(0).dispose();
                            }

                            for (int i = 0; i < childTypes.length; i++) {
                                MenuItem mi = new MenuItem(addPopupMenu,SWT.PUSH);
                                mi.addSelectionListener(adapter);
                                mi.setData(childTypes[i]);
                                mi.setText(NSISInstallElementFactory.getTypeName(childTypes[i]));
                                mi.setImage(NSISInstallElementFactory.getImage(childTypes[i]));
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * @param composite
     * @param bundle
     */
    private ToolBar createToolBar(Composite parent, ResourceBundle bundle)
    {
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

        int[] styles = {SWT.DROP_DOWN,SWT.PUSH,SWT.PUSH,SWT.PUSH,SWT.PUSH};
        Image[] images = {CommonImages.ADD_ICON,CommonImages.EDIT_ICON,CommonImages.DELETE_ICON,CommonImages.EXPANDALL_ICON,CommonImages.COLLAPSEALL_ICON};
        Image[] disabledImages = {CommonImages.ADD_DISABLED_ICON,CommonImages.EDIT_DISABLED_ICON,CommonImages.DELETE_DISABLED_ICON,CommonImages.EXPANDALL_DISABLED_ICON,CommonImages.COLLAPSEALL_DISABLED_ICON};
        String[] tooltips = {"add.tooltip","edit.tooltip","delete.tooltip","expandall.tooltip","collapseall.tooltip"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        boolean[] state = {false,false,false,true,true};
        for (int i = 0; i < styles.length; i++) {
            ToolItem ti = new ToolItem(toolbar, styles[i]);
            ti.setImage(images[i]);
            ti.setDisabledImage(disabledImages[i]);
            ti.setToolTipText(bundle.getString(tooltips[i]));
            ti.setEnabled(state[i]);
        }
        return toolbar;
    }

    /**
     * @param composite
     * @param bundle
     */
    private Menu createMenu(ResourceBundle bundle)
    {
        Menu menu = new Menu(getShell(), SWT.POP_UP);

        int[] styles = {SWT.CASCADE,SWT.PUSH,SWT.PUSH};
        Image[] images = {CommonImages.ADD_ICON,CommonImages.EDIT_ICON,CommonImages.DELETE_ICON};
        String[] tooltips = {"add.tooltip","edit.tooltip","delete.tooltip"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < styles.length; i++) {
            MenuItem mi = new MenuItem(menu, styles[i]);
            mi.setImage(images[i]);
            mi.setText(bundle.getString(tooltips[i]));
            mi.setEnabled(false);
        }
        return menu;
    }

    private void checkUnselectedSections()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if(Common.isEmpty(getMessage()) || getMessageType() != ERROR) {
            if (settings.getInstallerType() == INSISWizardConstants.INSTALLER_TYPE_SILENT) {
                INSISInstallElement[] items = settings.getInstaller().getChildren();
                if (!Common.isEmptyArray(items)) {
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] instanceof NSISSection) {
                            if (((NSISSection)items[i]).isDefaultUnselected()) {
                                setMessage(EclipseNSISPlugin.getResourceString("silent.unselected.sections.warning"), WARNING); //$NON-NLS-1$
                                return;
                            }
                        }
                        else if (items[i] instanceof NSISSectionGroup) {
                            INSISInstallElement[] items2 = items[i].getChildren();
                            if (!Common.isEmptyArray(items2)) {
                                for (int j = 0; j < items2.length; j++) {
                                    if (items2[j] instanceof NSISSection) {
                                        if (((NSISSection)items2[j]).isDefaultUnselected()) {
                                            setMessage(EclipseNSISPlugin.getResourceString("silent.unselected.sections.warning"), WARNING); //$NON-NLS-1$
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                setMessage(EclipseNSISPlugin.getResourceString("wizard.contents.description")); //$NON-NLS-1$
            }
        }
    }

    @Override
    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();

            List<INSISInstallElement> changedElements = new ArrayList<INSISInstallElement>();
            String error = settings.getInstaller().validate(changedElements);
            if(mContentsTreeViewer != null && changedElements.size() > 0) {
                mContentsTreeViewer.update(changedElements.toArray(),null);
            }
            setErrorMessage(error);
            boolean b = error == null;
            setPageComplete(b);
            checkUnselectedSections();
            return b;
        }
    }

    private static class RegistryImportStrategy implements RegistryImporter.IRegistryImportStrategy
    {
        private List<NSISInstallRegistryItem> mRegistryItems = new ArrayList<NSISInstallRegistryItem>();

        public void reset()
        {
            mRegistryItems.clear();
        }

        public List<NSISInstallRegistryItem> getRegistryItems()
        {
            return mRegistryItems;
        }

        public void addRegistryKey(String rootKey, String subKey)
        {
            NSISInstallRegistryKey regKey = new NSISInstallRegistryKey();
            regKey.setRootKey(NSISWizardDisplayValues.getHKeyIndex(rootKey));
            regKey.setSubKey(subKey);
            mRegistryItems.add(regKey);
        }

        public void addRegistryValue(String rootKey, String subKey, String value, int type, String data)
        {
            int type2 = type;
            switch(type2) {
                case WinAPI.REG_BINARY:
                    type2 = REG_BIN;
                    break;
                case WinAPI.REG_DWORD:
                    type2 = REG_DWORD;
                    break;
                case WinAPI.REG_EXPAND_SZ:
                    type2 = REG_EXPAND_SZ;
                    break;
                case WinAPI.REG_SZ:
                    type2 = REG_SZ;
                    break;
                default:
                    return;
            }
            NSISInstallRegistryValue regVal = new NSISInstallRegistryValue();
            regVal.setRootKey(NSISWizardDisplayValues.getHKeyIndex(rootKey));
            regVal.setSubKey(subKey);
            regVal.setValue(value);
            regVal.setData(data);
            regVal.setValueType(type2);
            mRegistryItems.add(regVal);
        }

        public void beginRegistryKeySection(String rootKey, String subKey)
        {
        }

        public void deleteRegistryKey(String rootKey, String subKey)
        {
        }

        public void deleteRegistryValue(String rootKey, String subKey, String value)
        {
        }
    }

    private class ContentsTransferData
    {
        INSISInstallElement parent;
        List<INSISInstallElement> selection;

        public ContentsTransferData(INSISInstallElement parent, List<INSISInstallElement> selection)
        {
            this.parent = parent;
            this.selection = selection;
        }
    }

    private static class ContentsTransfer extends ObjectTransfer
    {
        public static final ContentsTransfer INSTANCE = new ContentsTransfer();
        private static final String[] TYPE_NAMES = {new StringBuffer("NSIS Wizard contents transfer").append( //$NON-NLS-1$
                        System.currentTimeMillis()).append(
                        ":").append(INSTANCE.hashCode()).toString()};//$NON-NLS-1$
        private static final int[] TYPEIDS = {registerType(TYPE_NAMES[0])};

        private ContentsTransfer()
        {
        }

        @Override
        protected int[] getTypeIds()
        {
            return TYPEIDS;
        }

        @Override
        protected String[] getTypeNames()
        {
            return TYPE_NAMES;
        }
    }
}
