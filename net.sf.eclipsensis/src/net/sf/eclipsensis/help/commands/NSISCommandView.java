/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.part.ViewPart;

public class NSISCommandView extends ViewPart implements INSISHomeListener
{
    private static final String KEY_NSIS_COMMAND_VIEW_FILTERS = "nsisCommandViewFilters";
    private static final String KEY_SHOW_FILTER = "showFilter";
    private static final String KEY_FILTER_INDEX = "filterIndex";

    private static final Image CATEGORY_IMAGE;
    private static final Image OPEN_CATEGORY_IMAGE;
    private static final Image COMMAND_IMAGE;
    private static final String DEFAULT_CATEGORY = EclipseNSISPlugin.getResourceString("other.category"); //$NON-NLS-1$
    private static Comparator<TreeNode> cComparator = new Comparator<TreeNode>() {
        public int compare(TreeNode node1, TreeNode node2)
        {
            return node1.getName().compareTo(node2.getName());
        }
    };

    private TreeNode mHierarchicalRootNode;
    private TreeNode mFlatRootNode;
    private TreeViewer mViewer;
    private IAction mFlatLayoutAction;
    private IAction mHierarchicalLayoutAction;
    private IAction mCollapseAllAction;
    private IAction mExpandAllAction;

    private ContentAssistCommandAdapter mContentAssistFindField = null;

    private boolean mFlatMode = false;
    private boolean mShowFilter = false;
    private Composite mFilterPanel;
    private MRUSet<FilterSetting> mSavedFilterSettings = new MRUSet<FilterSetting>(10);
    private FilterSetting mFilterSetting = null;
    private Composite mControl;

    static {
        final Image[] images = new Image[3];
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                images[0] = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("category.icon")); //$NON-NLS-1$
                images[1] = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("open.category.icon"));
                images[2] = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("command.icon"));
            }
        });
        CATEGORY_IMAGE = images[0];
        OPEN_CATEGORY_IMAGE = images[1];
        COMMAND_IMAGE = images[2];
    }

    @Override
    public void dispose()
    {
        NSISPreferences.getInstance().removeListener(this);
        super.dispose();
    }

    private void makeActions()
    {
        IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        mHierarchicalLayoutAction = new Action() {
            @Override
            public void run()
            {
                setFlatMode(false);
            }
        };
        mHierarchicalLayoutAction.setText(EclipseNSISPlugin.getResourceString("hierarchical.layout.action.text")); //$NON-NLS-1$
        mHierarchicalLayoutAction.setToolTipText(EclipseNSISPlugin.getResourceString("hierarchical.layout.action.tooltip"));
        mHierarchicalLayoutAction.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("hierarchical.layout.icon")));
        mHierarchicalLayoutAction.setChecked(!mFlatMode);
        tbm.add(mHierarchicalLayoutAction);
        menu.add(mHierarchicalLayoutAction);

        mFlatLayoutAction = new Action() {
            @Override
            public void run()
            {
                setFlatMode(true);
            }
        };
        mFlatLayoutAction.setText(EclipseNSISPlugin.getResourceString("flat.layout.action.text")); //$NON-NLS-1$
        mFlatLayoutAction.setToolTipText(EclipseNSISPlugin.getResourceString("flat.layout.action.tooltip"));
        mFlatLayoutAction.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("flat.layout.icon")));
        mFlatLayoutAction.setChecked(mFlatMode);
        tbm.add(mFlatLayoutAction);
        menu.add(mFlatLayoutAction);

        mExpandAllAction = new Action() {
            @Override
            public void run()
            {
                expandAll(true);
            }
        };
        mExpandAllAction.setText(EclipseNSISPlugin.getResourceString("expandall.text")); //$NON-NLS-1$
        mExpandAllAction.setToolTipText(EclipseNSISPlugin.getResourceString("expandall.tooltip"));
        mExpandAllAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPANDALL_ICON));
        mExpandAllAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPANDALL_DISABLED_ICON));
        mExpandAllAction.setEnabled(!mFlatMode);
        tbm.add(mExpandAllAction);

        mCollapseAllAction = new Action() {
            @Override
            public void run()
            {
                expandAll(false);
            }
        };
        mCollapseAllAction.setText(EclipseNSISPlugin.getResourceString("collapseall.text")); //$NON-NLS-1$
        mCollapseAllAction.setToolTipText(EclipseNSISPlugin.getResourceString("collapseall.tooltip"));
        mCollapseAllAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.COLLAPSEALL_ICON));
        mCollapseAllAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.COLLAPSEALL_DISABLED_ICON));
        mCollapseAllAction.setEnabled(!mFlatMode);
        tbm.add(mCollapseAllAction);

        IAction toggleFilterAction = new Action() {
            @Override
            public void run()
            {
                toggleFilter();
            }
        };
        toggleFilterAction.setText(EclipseNSISPlugin.getResourceString("togglefilter.action.text")); //$NON-NLS-1$
        toggleFilterAction.setToolTipText(EclipseNSISPlugin.getResourceString("togglefilter.action.tooltip"));
        toggleFilterAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.FILTER_ICON));
        toggleFilterAction.setEnabled(true);
        toggleFilterAction.setChecked(mShowFilter);
        tbm.add(toggleFilterAction);
    }

    @Override
    public void createPartControl(Composite parent)
    {
        mFlatMode = NSISPreferences.getInstance().getBoolean(INSISPreferenceConstants.NSIS_COMMAND_VIEW_FLAT_MODE);

        mControl = new Composite(parent,SWT.NONE);
        GridLayout l = new GridLayout(1,true);
        //        l.verticalSpacing = 0;
        mControl.setLayout(l);

        mFilterPanel = new Composite(mControl,SWT.NONE);
        mFilterPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        l = new GridLayout(3,false);
        l.marginWidth = l.marginHeight  = 0;
        mFilterPanel.setLayout(l);
        mFilterPanel.setVisible(mShowFilter);
        ((GridData)mFilterPanel.getLayoutData()).exclude = !mShowFilter;

        Label temp = new Label(mFilterPanel,SWT.NONE);
        temp.setText("Filter:");
        temp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        FieldDecoration dec= FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        Rectangle bounds = dec.getImage().getBounds();

        final Combo filterCombo = new Combo(mFilterPanel,SWT.BORDER|SWT.DROP_DOWN);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.horizontalIndent= bounds.width;
        filterCombo.setLayoutData(gd);
        final ComboViewer filterComboViewer = new ComboViewer(filterCombo);
        filterComboViewer.setContentProvider(new CollectionContentProvider(true));
        filterComboViewer.setInput(mSavedFilterSettings);
        filterComboViewer.setSelection(mFilterSetting==null?StructuredSelection.EMPTY:new StructuredSelection(mFilterSetting));

        final Button regexp = new Button(mFilterPanel,SWT.CHECK);
        regexp.setText("Regular Expression");
        regexp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        regexp.setSelection(mFilterSetting != null && mFilterSetting.regexp);

        temp = new Label(mFilterPanel,SWT.NONE);
        temp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Composite c = new Composite(mFilterPanel, SWT.NONE);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ((GridData)c.getLayoutData()).horizontalSpan = 2;
        l = new GridLayout(2,false);
        l.marginWidth = l.marginHeight  = 0;
        c.setLayout(l);

        final String helpText = "(* = any string, ? = any character, \\ = escape for literals: * ? \\)";
        final Label helpLabel = new Label(c,SWT.NONE);
        helpLabel.setText(helpText);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.horizontalIndent= bounds.width;
        helpLabel.setLayoutData(gd);

        c = new Composite(c, SWT.None);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        l = new GridLayout(2,true);
        l.marginWidth = l.marginHeight  = 0;
        c.setLayout(l);

        final Button clear = new Button(c,SWT.PUSH);
        clear.setText("Clear");
        clear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        final Button apply = new Button(c,SWT.PUSH);
        apply.setText("Apply");
        apply.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Tree tree = new Tree(mControl,SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.HIDE_SELECTION);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tree.setLinesVisible(false);
        mViewer = new TreeViewer(tree);
        mViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        mViewer.setContentProvider(new TreeContentProvider());
        mViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element)
            {
                if(element instanceof TreeNode) {
                    return ((TreeNode)element).getName();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element)
            {
                if(element instanceof TreeNode) {
                    if(((TreeNode)element).getCommand() != null) {
                        return COMMAND_IMAGE;
                    }
                    else {
                        return mViewer.getExpandedState(element)?OPEN_CATEGORY_IMAGE:CATEGORY_IMAGE;
                    }
                }
                return super.getImage(element);
            }
        });
        mViewer.addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event)
            {
                updateLabels(event);
            }

            /**
             * @param treeViewer
             * @param event
             */
            private void updateLabels(TreeExpansionEvent event)
            {
                final Object element = event.getElement();
                if(element instanceof TreeNode && ((TreeNode)element).getCommand() == null) {
                    mViewer.getTree().getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            mViewer.update(element,null);
                        }
                    });
                }
            }

            public void treeExpanded(TreeExpansionEvent event)
            {
                updateLabels(event);
            }
        });

        NSISPreferences.getInstance().addListener(this);
        mViewer.addDragSupport(DND.DROP_COPY,
                        new Transfer[]{NSISCommandTransfer.INSTANCE},
                        new DragSourceAdapter() {
            @Override
            public void dragStart(DragSourceEvent e)
            {
                IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                if (!(editor instanceof NSISEditor)) {
                    e.doit = false;
                }
                IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                if(sel == null || sel.isEmpty() || !(sel.getFirstElement() instanceof TreeNode) ||
                                ((TreeNode)sel.getFirstElement()).getCommand() == null) {
                    e.doit = false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent e)
            {
                IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                if(sel != null && !sel.isEmpty() && sel.getFirstElement() instanceof TreeNode &&
                                ((TreeNode)sel.getFirstElement()).getCommand() != null) {
                    e.data = ((TreeNode)sel.getFirstElement()).getCommand();
                }
                else {
                    e.data = null;
                }
            }
        }
        );
        mViewer.getTree().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if( (e.character == SWT.CR || e.character == SWT.LF) && e.stateMask == 0) {
                    insertCommand(mViewer.getSelection());
                }
            }
        });
        mViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                if(!insertCommand(event.getSelection())) {
                    if(event.getSelection() instanceof IStructuredSelection && !event.getSelection().isEmpty()) {
                        Object element = ((IStructuredSelection)event.getSelection()).getFirstElement();
                        if (element instanceof TreeNode) {
                            TreeNode node = (TreeNode)element;
                            mViewer.setExpandedState(node, !mViewer.getExpandedState(node));
                        }
                    }
                }
            }
        });

        final boolean[] stopListening = {false};

        filterCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if (!stopListening[0])
                {
                    mFilterSetting = createFilterSetting(filterCombo.getText(), regexp.getSelection());
                    apply.setEnabled(mFilterSetting != null);
                }
            }
        });
        filterCombo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                if(mContentAssistFindField == null)
                {
                    createFilterContentAssist(filterCombo, helpLabel);
                }
                mContentAssistFindField.setEnabled(regexp.getSelection());
            }
        });

        regexp.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                if(mContentAssistFindField == null)
                {
                    createFilterContentAssist(filterCombo, helpLabel);
                }
                mContentAssistFindField.setEnabled(regexp.getSelection());
            }
        });
        regexp.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if(mContentAssistFindField == null)
                {
                    createFilterContentAssist(filterCombo, helpLabel);
                }
                mContentAssistFindField.setEnabled(regexp.getSelection());
                mFilterPanel.layout(new Control[]{filterCombo, helpLabel});
                helpLabel.setVisible(!regexp.getSelection());
                if(mContentAssistFindField != null)
                {
                    mContentAssistFindField.setEnabled(regexp.getSelection());
                }
                if (!stopListening[0])
                {
                    mFilterSetting = createFilterSetting(filterCombo.getText(), regexp.getSelection());
                    apply.setEnabled(mFilterSetting != null);
                }
            }
        });

        clear.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                try
                {
                    stopListening[0] = true;
                    filterComboViewer.setSelection(StructuredSelection.EMPTY);
                    regexp.setSelection(false);
                    mFilterSetting = null;
                    updateInput();
                }
                finally
                {
                    stopListening[0] = false;
                }
            }
        });

        final Runnable applyFilterRunnable = new Runnable() {
            public void run()
            {
                updateInput();
                FilterSetting filterSetting = mFilterSetting;
                mSavedFilterSettings.add(filterSetting);
                filterComboViewer.refresh();
                filterComboViewer.setSelection(new StructuredSelection(filterSetting));
                filterComboViewer.getCombo().clearSelection();
            }
        };

        apply.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                applyFilterRunnable.run();
            }
        });

        filterCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if( (e.character == SWT.CR || e.character == SWT.LF) && e.stateMask == 0)
                {
                    if(apply.isEnabled())
                    {
                        applyFilterRunnable.run();
                    }
                }
            }
        });

        filterComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if(!event.getSelection().isEmpty())
                {
                    IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                    FilterSetting filterSetting = (FilterSetting) sel.getFirstElement();
                    try
                    {
                        stopListening[0] = true;
                        mFilterSetting = filterSetting;
                        regexp.setSelection(mFilterSetting.regexp);
                        apply.setEnabled(true);
                    }
                    finally
                    {
                        stopListening[0] = false;
                    }
                }
            }
        });
        makeActions();
        updateInput();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(mViewer.getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_cmdview_context");
    }

    private FilterSetting createFilterSetting(String filterText, boolean isRegexp)
    {
        String patternText = null;
        if (filterText != null && filterText.length() > 0)
        {
            if (isRegexp)
            {
                patternText = filterText;
            }
            else
            {
                StringBuffer buffer = new StringBuffer();
                boolean isEscaped= false;
                for (int i = 0; i < filterText.length(); i++) {
                    char c = filterText.charAt(i);
                    switch(c) {
                        // the backslash
                        case '\\':
                            // the backslash is escape char in string matcher
                            if (!isEscaped) {
                                isEscaped= true;
                            }
                            else {
                                buffer.append("\\\\");
                                isEscaped= false;
                            }
                            break;
                            // characters that need to be escaped in the regex.
                        case '(':
                        case ')':
                        case '{':
                        case '}':
                        case '.':
                        case '[':
                        case ']':
                        case '$':
                        case '^':
                        case '+':
                        case '|':
                            if (isEscaped) {
                                buffer.append("\\\\");
                                isEscaped= false;
                            }
                            buffer.append('\\');
                            buffer.append(c);
                            break;
                        case '?':
                            if (!isEscaped) {
                                buffer.append('.');
                            }
                            else {
                                buffer.append('\\');
                                buffer.append(c);
                                isEscaped= false;
                            }
                            break;
                        case '*':
                            if (!isEscaped) {
                                buffer.append(".*");
                            }
                            else {
                                buffer.append('\\');
                                buffer.append(c);
                                isEscaped= false;
                            }
                            break;
                        default:
                            if (isEscaped) {
                                buffer.append("\\\\");
                                isEscaped= false;
                            }
                            buffer.append(c);
                            break;
                    }
                }
                if (isEscaped) {
                    buffer.append("\\\\");
                    isEscaped= false;
                }
                patternText = buffer.toString();
            }
        }
        else
        {
            filterText = "";
        }
        if(mFilterSetting != null && Common.stringsAreEqual(filterText, mFilterSetting.text) && isRegexp == mFilterSetting.regexp)
        {
            return mFilterSetting;
        }
        else
        {
            try
            {
                return new FilterSetting(isRegexp,filterText,patternText == null?null:Pattern.compile(patternText, Pattern.CASE_INSENSITIVE));
            }
            catch (PatternSyntaxException e)
            {
                return null;
            }
        }
    }

    private void createFilterContentAssist(Combo filterCombo, Label helpLabel)
    {
        // Create the find content assist field
        ComboContentAdapter contentAdapter= new ComboContentAdapter();
        FindReplaceDocumentAdapterContentProposalProvider findProposer= new FindReplaceDocumentAdapterContentProposalProvider(true);
        mContentAssistFindField = new ContentAssistCommandAdapter(
                        filterCombo,
                        contentAdapter,
                        findProposer,
                        null,
                        new char[] {'\\', '[', '('},
                        true);
        mContentAssistFindField.setEnabled(false);
        GridData gd= (GridData)filterCombo.getLayoutData();
        FieldDecoration dec= FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        Rectangle bounds = dec.getImage().getBounds();
        gd.horizontalIndent= bounds.width;

        ((GridData)helpLabel.getLayoutData()).horizontalIndent = bounds.width;
    }

    private void updateIcon(TreeNode node)
    {
        mViewer.update(node,null);
        for(Iterator<TreeNode> iter = node.getChildren().iterator(); iter.hasNext(); ) {
            TreeNode childNode = iter.next();
            if(childNode.getCommand() == null) {
                updateIcon(childNode);
            }
        }
    }

    @Override
    public void setFocus()
    {
        mViewer.getControl().setFocus();
    }

    public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
    {
        Display.getDefault().asyncExec(new Runnable() {
            public void run()
            {
                updateInput();
            }
        });
    }

    private void updateInput()
    {
        if(mViewer != null) {
            Tree tree = mViewer.getTree();
            if(tree != null && !tree.isDisposed()) {
                NSISCommand[] commands;
                try {
                    commands = NSISCommandManager.getCommands();
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    commands = null;
                }
                mFlatRootNode = mFlatMode?new TreeNode(""):null; //$NON-NLS-1$
                mHierarchicalRootNode = mFlatMode?null:new TreeNode("");
                TreeNode rootNode = mFlatMode?mFlatRootNode:mHierarchicalRootNode;
                if (commands != null) {
                    for (int i = 0; i < commands.length; i++) {
                        if(mFilterSetting != null && mFilterSetting.pattern != null)
                        {
                            if(!mFilterSetting.pattern.matcher(commands[i].getName()).matches())
                            {
                                continue;
                            }
                        }
                        TreeNode parent = rootNode;

                        if (!mFlatMode) {
                            parent = findParent(parent, commands[i]);
                        }
                        parent.addChild(new TreeNode(commands[i].getName(),
                                        commands[i]));
                    }
                }
                rootNode.sort();
                updateInput(rootNode);
            }
        }
    }

    /**
     * @param rootNode
     */
    private void updateInput(TreeNode rootNode)
    {
        ISelection sel = mViewer.getSelection();
        Tree tree = mViewer.getTree();
        IHandle handle = Common.getControlHandle(tree);
        if(mFlatMode) {
            WinAPI.INSTANCE.setWindowLong(handle, WinAPI.GWL_STYLE, WinAPI.INSTANCE.getWindowLong(handle, WinAPI.GWL_STYLE) ^ (WinAPI.TVS_HASLINES  | WinAPI.TVS_HASBUTTONS));
        }
        else {
            WinAPI.INSTANCE.setWindowLong(handle, WinAPI.GWL_STYLE, WinAPI.INSTANCE.getWindowLong(handle, WinAPI.GWL_STYLE) | WinAPI.TVS_HASLINES  | WinAPI.TVS_HASBUTTONS);
        }
        mViewer.setInput(rootNode);
        if(!mFlatMode && rootNode.getCommand() == null) {
            updateIcon(rootNode);
        }
        if(sel != null && !sel.isEmpty()) {
            mViewer.setSelection(sel);
            sel = mViewer.getSelection();
            if(!sel.isEmpty()) {
                return;
            }
        }
        if(mViewer.getTree().getItemCount() > 0) {
            mViewer.getTree().showItem(mViewer.getTree().getItem(0));
        }
    }

    private void setFlatMode(boolean flatMode)
    {
        if(mFlatMode != flatMode) {
            TreeNode rootNode;
            if(mFlatMode) {
                boolean isNew = false;
                if(mHierarchicalRootNode == null) {
                    mHierarchicalRootNode = new TreeNode("");
                    isNew = true;
                }
                for(int i=0; i<mFlatRootNode.getChildren().size(); i++) {
                    TreeNode child = mFlatRootNode.getChildren().get(i);
                    mFlatRootNode.removeChild(child);
                    TreeNode parent = findParent(mHierarchicalRootNode, child.getCommand());
                    parent.addChild(child);
                    i--;
                }
                if (isNew) {
                    mHierarchicalRootNode.sort();
                }
                rootNode = mHierarchicalRootNode;
            }
            else {
                if(mFlatRootNode == null) {
                    mFlatRootNode = new TreeNode("");
                }
                moveCommandChild(mFlatRootNode,mHierarchicalRootNode);
                mFlatRootNode.sort();
                rootNode = mFlatRootNode;
            }
            mFlatMode = !mFlatMode;
            mFlatLayoutAction.setChecked(mFlatMode);
            mHierarchicalLayoutAction.setChecked(!mFlatMode);
            mExpandAllAction.setEnabled(!mFlatMode);
            mCollapseAllAction.setEnabled(!mFlatMode);
            NSISPreferences.getInstance().setValue(INSISPreferenceConstants.NSIS_COMMAND_VIEW_FLAT_MODE, mFlatMode);
            updateInput(rootNode);
        }
    }

    private boolean moveCommandChild(TreeNode target, TreeNode source)
    {
        if(source.getChildren().size() > 0) {
            for(int i=0; i<source.getChildren().size(); i++) {
                TreeNode child = source.getChildren().get(i);
                if(moveCommandChild(target, child)) {
                    i--;
                }
            }
            return false;
        }
        else {
            target.addChild(source);
            return true;
        }
    }

    private TreeNode findParent(TreeNode parent, NSISCommand cmd)
    {
        TreeNode parent2 = parent;
        String category = cmd.getCategory();
        if(Common.isEmpty(category)) {
            category = DEFAULT_CATEGORY;
        }
        String[] cats = Common.tokenize(category, '/');
        for(int j=0; j<cats.length; j++) {
            for (Iterator<TreeNode> iter = parent2.getChildren().iterator(); iter.hasNext();) {
                TreeNode node = iter.next();
                if(node.getName().equals(cats[j])) {
                    parent2 = node;
                    break;
                }
            }
            if(!parent2.getName().equals(cats[j])) {
                TreeNode node = new TreeNode(cats[j]);
                parent2.addChild(node);
                parent2 = node;
            }
        }
        return parent2;
    }

    /**
     * @param sel
     */
    private boolean insertCommand(ISelection sel)
    {
        if(sel instanceof IStructuredSelection && !sel.isEmpty()) {
            try {
                IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                if(editor instanceof NSISEditor) {
                    Object element = ((IStructuredSelection)sel).getFirstElement();
                    if(element instanceof TreeNode && ((TreeNode)element).getCommand() != null) {
                        ((NSISEditor)editor).insertCommand(((TreeNode)element).getCommand());
                        return true;
                    }
                }
            }
            catch(Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
            }
        }
        return false;
    }

    private void toggleFilter()
    {
        mShowFilter = !mShowFilter;
        mFilterPanel.setVisible(mShowFilter);
        ((GridData)mFilterPanel.getLayoutData()).exclude = !mShowFilter;
        mControl.layout(new Control[]{mFilterPanel});
        mFilterPanel.setFocus();
    }

    /**
     *
     */
    private void expandAll(boolean flag)
    {
        try {
            mViewer.getTree().setRedraw(false);
            if(flag) {
                mViewer.expandAll();
            }
            else {
                mViewer.collapseAll();
            }
            TreeNode rootNode = (TreeNode)mViewer.getInput();
            if(!mFlatMode && rootNode != null && rootNode.getCommand() == null) {
                updateIcon(rootNode);
            }
            IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
            if(!sel.isEmpty()) {
                mViewer.reveal(sel.getFirstElement());
            }
        }
        finally {
            mViewer.getTree().setRedraw(true);
        }
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);
        if (memento != null)
        {
            IMemento node = memento.getChild(KEY_NSIS_COMMAND_VIEW_FILTERS);
            if (node != null)
            {
                Boolean showFilter = node.getBoolean(KEY_SHOW_FILTER);
                mShowFilter = showFilter != null && showFilter.booleanValue();

                Integer filterIndex = node.getInteger(KEY_FILTER_INDEX);

                String text = node.getTextData();
                if (text != null)
                {
                    try
                    {
                        List<FilterSetting> list = IOUtility.readObject(new ByteDecodingInputStream(
                                        new ByteArrayInputStream(text.getBytes())));
                        mSavedFilterSettings.clear();
                        mSavedFilterSettings.addAll(list);
                        if (filterIndex != null)
                        {
                            FilterSetting filterSetting = list.get(filterIndex);
                            mFilterSetting = filterSetting;
                        }
                    }
                    catch (Exception e)
                    {
                        mFilterSetting = null;
                    }
                }
            }
        }
    }

    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        if (memento != null)
        {
            IMemento node = memento.createChild(KEY_NSIS_COMMAND_VIEW_FILTERS);
            node.putBoolean(KEY_SHOW_FILTER, mShowFilter);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                List<FilterSetting> list = new ArrayList<FilterSetting>(mSavedFilterSettings);
                IOUtility.writeObject(new ByteEncodingOutputStream(baos), list);
                baos.close();

                String text = baos.toString();
                node.putTextData(text);

                int n = list.indexOf(mFilterSetting);
                node.putInteger(KEY_FILTER_INDEX, n);
            }
            catch (Exception e)
            {
            }
        }
    }

    private class TreeNode
    {
        private TreeNode mParent;
        private String mName;
        private NSISCommand mCommand;
        private List<TreeNode> mChildren;

        public TreeNode(String name)
        {
            this(name, null);
        }

        public TreeNode(String name, NSISCommand data)
        {
            mName = name;
            mCommand = data;
        }

        public List<TreeNode> getChildren()
        {
            return mChildren==null?Collections.<TreeNode>emptyList():mChildren;
        }

        public NSISCommand getCommand()
        {
            return mCommand;
        }

        public String getName()
        {
            return mName;
        }

        public TreeNode getParent()
        {
            return mParent;
        }

        public void setParent(TreeNode parent)
        {
            if(mParent != null) {
                TreeNode oldParent = mParent;
                mParent = null;
                oldParent.removeChild(this);
            }
            mParent = parent;
            if(mParent != null) {
                mParent.addChild(this);
            }
        }

        public void addChild(TreeNode child)
        {
            if(mChildren == null) {
                mChildren = new ArrayList<TreeNode>();
            }
            if(!mChildren.contains(child)) {
                mChildren.add(child);
                child.setParent(this);
            }
        }

        public void removeChild(TreeNode child)
        {
            if(mChildren != null && mChildren.remove(child)) {
                child.setParent(null);
            }
        }

        public void sort()
        {
            if(mChildren != null) {
                Collections.sort(mChildren, cComparator);
                for (Iterator<TreeNode> iter = mChildren.iterator(); iter.hasNext();) {
                    iter.next().sort();
                }
            }
        }
    }

    private class TreeContentProvider extends EmptyContentProvider
    {
        @Override
        public Object[] getChildren(Object parentElement)
        {
            if(parentElement instanceof TreeNode) {
                List<TreeNode> children = ((TreeNode)parentElement).getChildren();
                return children==null?null:children.toArray();
            }
            return null;
        }

        @Override
        public Object getParent(Object element)
        {
            if(element instanceof TreeNode) {
                return ((TreeNode)element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element)
        {
            return !Common.isEmptyArray(getChildren(element));
        }

        @Override
        public Object[] getElements(Object inputElement)
        {
            if(inputElement == (mFlatMode?mFlatRootNode:mHierarchicalRootNode)) {
                return getChildren(inputElement);
            }
            return null;
        }
    }

    private static class ByteEncodingOutputStream extends FilterOutputStream
    {
        public ByteEncodingOutputStream(OutputStream out)
        {
            super(out);
        }

        @Override
        public void write(int b) throws IOException
        {
            String s = String.format("%02x",(byte)b);
            for(byte x : s.getBytes())
            {
                super.write(x);
            }
        }
    }

    private static class ByteDecodingInputStream extends FilterInputStream
    {
        public ByteDecodingInputStream(InputStream in)
        {
            super(in);
        }

        @Override
        public int available() throws IOException
        {
            return super.available()/2;
        }

        @Override
        public synchronized void mark(int readlimit)
        {
            super.mark(2*readlimit);
        }

        private byte decode(byte b1, byte b2)
        {
            byte[] bytes = { b1, b2};
            return (byte) Integer.parseInt(new String(bytes), 16);
        }

        @Override
        public int read() throws IOException
        {
            return decode((byte) super.read(), (byte) super.read());
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            byte[] b2 = new byte[2*b.length];

            int n = super.read(b2, 2*off, 2*len);
            int m = n/2;
            for(int i=0; i<m; i++)
            {
                b[i] = decode(b2[2*i],b2[2*i+1]);
            }

            return m;
        }

        @Override
        public long skip(long n) throws IOException
        {
            return super.skip(2*n);
        }
    }

    private static class FilterSetting implements Serializable
    {
        private static final long serialVersionUID = 1L;

        boolean regexp;
        String text;
        Pattern pattern;

        public FilterSetting(boolean regexp, String text, Pattern pattern)
        {
            super();
            this.regexp = regexp;
            this.text = text;
            this.pattern = pattern;
        }

        @Override
        public String toString()
        {
            return text;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (regexp ? 1231 : 1237);
            result = prime * result + (text == null ? 0 : text.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            FilterSetting other = (FilterSetting) obj;
            if (regexp != other.regexp)
            {
                return false;
            }
            if (text == null)
            {
                if (other.text != null)
                {
                    return false;
                }
            }
            else if (!text.equals(other.text))
            {
                return false;
            }
            return true;
        }
    }
}
