/*****************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.actions.*;
import net.sf.eclipsensis.editor.outline.*;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.help.commands.*;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.startup.FileAssociationChecker;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dnd.IDragAndDropService;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

@SuppressWarnings("restriction")
public class NSISEditor extends TextEditor implements INSISConstants, INSISEditorPreferenceConstants, INSISHomeListener, ISelectionChangedListener, IProjectionListener
{
    private Set<NSISAction> mActions = new HashSet<NSISAction>();
    private ProjectionSupport mProjectionSupport;
    private NSISContentOutlinePage mOutlinePage;
    private NSISOutlineContentProvider mOutlineContentProvider;
    private Position mCurrentPosition = null;
    private Mutex mMutex = new Mutex();
    private HTMLExporter mHTMLExporter;
    private boolean mTextDragAndDropEnabled= false;
    private boolean mTextDragAndDropInstalled= false;
    private Object mTextDragAndDropToken;

    /**
     *
     */
    public NSISEditor()
    {
        super();
        FileAssociationChecker.checkFileAssociation(FILE_ASSOCIATION_ID);
        setHelpContextId(PLUGIN_CONTEXT_PREFIX + "nsis_editor_context"); //$NON-NLS-1$;
    }

    public void selectionChanged(SelectionChangedEvent event)
    {
        Object source = event.getSource();
        ISelection selection = event.getSelection();
        ISourceViewer sourceViewer = getSourceViewer();
        boolean acquiredMutex = mMutex.acquireWithoutBlocking(source);
        try {
            if(source.equals(sourceViewer) && selection instanceof ITextSelection) {
                IAction action = getAction(INSISEditorConstants.ADD_BLOCK_COMMENT);
                if(action != null) {
                    action.setEnabled(sourceViewer.getSelectedRange().y > 0);
                }

                if(mOutlineContentProvider != null) {
                    if(acquiredMutex) {
                        ITextSelection textSelection = (ITextSelection)selection;
                        IStructuredSelection sel = StructuredSelection.EMPTY;
                        NSISOutlineElement element = mOutlineContentProvider.findElement(textSelection.getOffset(),textSelection.getLength());
                        if(element != null) {
                            Position position = element.getPosition();
                            if(position.equals(mCurrentPosition)) {
                                return;
                            }
                            else {
                                if(mOutlinePage != null) {
                                    if(!mOutlinePage.isDisposed()) {
                                        sel = new StructuredSelection(element);
                                        mOutlinePage.setSelection(sel);
                                        return;
                                    }
                                    else {
                                        mOutlinePage = null;
                                    }
                                }

                                mCurrentPosition = position;
                                setHighlightRange(mCurrentPosition.getOffset(),
                                                mCurrentPosition.getLength(),
                                                false);
                            }
                        }
                    }
                    else {
                        return;
                    }
                }
                mCurrentPosition = null;
            }
            else if(source instanceof TreeViewer) {
                if (selection.isEmpty()) {
                    mCurrentPosition = null;
                    resetHighlightRange();
                }
                else {
                    NSISOutlineElement element = (NSISOutlineElement) ((IStructuredSelection) selection).getFirstElement();
                    Position position = element.getPosition();
                    if(mCurrentPosition == null || !position.equals(mCurrentPosition)) {
                        mCurrentPosition = position;
                        try {
                            boolean moveCursor = acquiredMutex;
                            //                        ISelection sel = getSelectionProvider().getSelection();
                            //                        if(sel != null && sel instanceof ITextSelection) {
                            //                            int offset = ((ITextSelection)sel).getOffset();
                            //                            if(position.includes(offset)) {
                            //                                moveCursor = false;
                            //                            }
                            //                        }

                            setHighlightRange(mCurrentPosition.getOffset(),
                                            mCurrentPosition.getLength(),
                                            moveCursor);
                        }
                        catch (IllegalArgumentException x) {
                            resetHighlightRange();
                        }
                    }

                    if(acquiredMutex) {
                        Position selectPosition = element.getSelectPosition();
                        if(selectPosition != null) {
                            sourceViewer.setSelectedRange(selectPosition.getOffset(),selectPosition.getLength());
                        }
                    }
                }
            }
        }
        finally {
            if(acquiredMutex) {
                mMutex.release(source);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
     */
    @Override
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] { NSIS_EDITOR_CONTEXT_ID });
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();

        mProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.task"); //$NON-NLS-1$
        mProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.bookmark"); //$NON-NLS-1$
        mProjectionSupport.install();
        NSISPreferences.getInstance().addListener(this);
        if(viewer.canDoOperation(ProjectionViewer.TOGGLE)) {
            viewer.doOperation(ProjectionViewer.TOGGLE);
        }
        mOutlineContentProvider = new NSISOutlineContentProvider(this);
        getSelectionProvider().addSelectionChangedListener(this);
        viewer.addPostSelectionChangedListener(this);
        updateAnnotations();
    }

    @Override
    protected void createActions()
    {
        super.createActions();
        ResourceBundle resourceBundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        IAction a= new TextOperationAction(resourceBundle, "content.assist.proposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
        a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction(INSISEditorConstants.CONTENT_ASSIST_PROPOSAL, a);

        a = new TextOperationAction(resourceBundle,"insert.template.",this,NSISSourceViewer.INSERT_TEMPLATE,true); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_TEMPLATE_COMMAND_ID);
        setAction(INSISEditorConstants.INSERT_TEMPLATE, a);

        a = new TextOperationAction(resourceBundle,"goto.help.",this,NSISSourceViewer.GOTO_HELP,true); //$NON-NLS-1$
        a.setActionDefinitionId(GOTO_HELP_COMMAND_ID);
        setAction(INSISEditorConstants.GOTO_HELP, a);

        TextOperationAction textAction = new TextOperationAction(resourceBundle,"sticky.help.",this,ISourceViewer.INFORMATION,true); //$NON-NLS-1$
        a = new NSISStickyHelpAction(resourceBundle,"sticky.help.",textAction); //$NON-NLS-1$
        a.setActionDefinitionId(STICKY_HELP_COMMAND_ID);
        setAction(INSISEditorConstants.STICKY_HELP, a);
        a = new NSISPopupStickyHelpAction(resourceBundle,"popup.sticky.help.",textAction); //$NON-NLS-1$
        setAction(INSISEditorConstants.POPUP_STICKY_HELP, a);

        a = new TextOperationAction(resourceBundle,"insert.file.",this,NSISSourceViewer.INSERT_FILE,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_FILE_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.file.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_FILE, a);

        a = new TextOperationAction(resourceBundle,"insert.directory.",this,NSISSourceViewer.INSERT_DIRECTORY,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_DIRECTORY_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.directory.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_DIRECTORY, a);

        a = new TextOperationAction(resourceBundle,"insert.color.",this,NSISSourceViewer.INSERT_COLOR,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_COLOR_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.color.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_COLOR, a);

        a = new TextOperationAction(resourceBundle,"insert.regfile.",this,NSISSourceViewer.IMPORT_REGFILE,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_REGFILE_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.regfile.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_REGFILE, a);

        a = new TextOperationAction(resourceBundle,"insert.regkey.",this,NSISSourceViewer.IMPORT_REGKEY,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_REGKEY_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.regkey.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_REGKEY, a);

        a = new TextOperationAction(resourceBundle,"insert.regval.",this,NSISSourceViewer.IMPORT_REGVAL,false); //$NON-NLS-1$
        a.setActionDefinitionId(INSERT_REGVAL_COMMAND_ID);
        a.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(resourceBundle.getString("insert.regval.image"))); //$NON-NLS-1$
        setAction(INSISEditorConstants.INSERT_REGVAL, a);

        a = new TextOperationAction(resourceBundle,"tabs.to.spaces.",this,NSISSourceViewer.TABS_TO_SPACES,false); //$NON-NLS-1$
        a.setActionDefinitionId(TABS_TO_SPACES_COMMAND_ID);
        setAction(INSISEditorConstants.TABS_TO_SPACES, a);

        a = new TextOperationAction(resourceBundle,"toggle.comment.",this,NSISSourceViewer.TOGGLE_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(TOGGLE_COMMENT_COMMAND_ID);
        setAction(INSISEditorConstants.TOGGLE_COMMENT, a);

        a = new TextOperationAction(resourceBundle,"add.block.comment.",this,NSISSourceViewer.ADD_BLOCK_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(ADD_BLOCK_COMMENT_COMMAND_ID);
        setAction(INSISEditorConstants.ADD_BLOCK_COMMENT, a);

        a = new TextOperationAction(resourceBundle,"remove.block.comment.",this,NSISSourceViewer.REMOVE_BLOCK_COMMENT,false); //$NON-NLS-1$
        a.setActionDefinitionId(REMOVE_BLOCK_COMMENT_COMMAND_ID);
        setAction(INSISEditorConstants.REMOVE_BLOCK_COMMENT, a);

        a= new TextOperationAction(resourceBundle, "projection.toggle.", this, ProjectionViewer.TOGGLE, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_TOGGLE, a);

        a = new TextOperationAction(resourceBundle, "projection.expand.all.", this, ProjectionViewer.EXPAND_ALL, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND_ALL);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_EXPAND_ALL, a);

        a= new TextOperationAction(resourceBundle, "projection.expand.", this, ProjectionViewer.EXPAND, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_EXPAND);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_EXPAND, a);

        a= new TextOperationAction(resourceBundle, "projection.collapse.", this, ProjectionViewer.COLLAPSE, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_COLLAPSE, a);

        a= new TextOperationAction(resourceBundle, "projection.collapse.all.", this, ProjectionViewer.COLLAPSE_ALL, true); //$NON-NLS-1$
        a.setActionDefinitionId(IFoldingCommandIds.FOLDING_COLLAPSE_ALL);
        a.setEnabled(true);
        setAction(INSISEditorConstants.FOLDING_COLLAPSE_ALL, a);
    }

    @Override
    protected void rulerContextMenuAboutToShow(IMenuManager menu) {
        super.rulerContextMenuAboutToShow(menu);
        IMenuManager foldingMenu= new MenuManager(EclipseNSISPlugin.getResourceString("folding.menu.label"), "net.sf.eclipsensis.projection"); //$NON-NLS-1$ //$NON-NLS-2$
        menu.appendToGroup(ITextEditorActionConstants.GROUP_RULERS, foldingMenu);

        IAction action= getAction(INSISEditorConstants.FOLDING_TOGGLE);
        foldingMenu.add(action);
        action= getAction(INSISEditorConstants.FOLDING_EXPAND_ALL);
        foldingMenu.add(action);
        action= getAction(INSISEditorConstants.FOLDING_COLLAPSE_ALL);
        foldingMenu.add(action);
    }

    @Override
    public void dispose()
    {
        mCurrentPosition = null;
        if (mOutlinePage != null) {
            mOutlinePage.setInput(null);
            mOutlinePage.dispose();
            mOutlinePage = null;
        }
        if (mOutlineContentProvider != null) {
            mOutlineContentProvider.inputChanged(getEditorInput(),null);
            mOutlineContentProvider.dispose();
            mOutlineContentProvider = null;
        }
        getSelectionProvider().removeSelectionChangedListener(this);
        ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
        viewer.removeProjectionListener(this);
        viewer.removePostSelectionChangedListener(this);
        NSISPreferences.getInstance().removeListener(this);
        super.dispose();
    }

    @Override
    public void doSetInput(IEditorInput input) throws CoreException
    {
        IEditorInput oldInput = getEditorInput();
        super.doSetInput(input);
        if (mOutlinePage != null) {
            mCurrentPosition = null;
            mOutlinePage.setInput(input);
        }
        else if (mOutlineContentProvider != null){
            mOutlineContentProvider.inputChanged(oldInput,null);
        }
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        menu.add(new Separator());
        NSISPopupStickyHelpAction a = (NSISPopupStickyHelpAction)getAction(INSISEditorConstants.POPUP_STICKY_HELP);
        if (a != null) {
            a.setClickPoint(getSourceViewer().getTextWidget().getDisplay().getCursorLocation());
        }
        addAction(menu, INSISEditorConstants.POPUP_STICKY_HELP);
        addAction(menu, INSISEditorConstants.CONTENT_ASSIST_PROPOSAL);
        addAction(menu, INSISEditorConstants.INSERT_TEMPLATE);
        menu.add(new Separator());
        addAction(menu, INSISEditorConstants.TABS_TO_SPACES);
        addAction(menu, INSISEditorConstants.TOGGLE_COMMENT);
        addAction(menu, INSISEditorConstants.ADD_BLOCK_COMMENT);

        IAction action = getAction(INSISEditorConstants.ADD_BLOCK_COMMENT);
        action.setEnabled(getSourceViewer().getSelectedRange().y > 0);

        addAction(menu, INSISEditorConstants.REMOVE_BLOCK_COMMENT);
        menu.add(new Separator());
        addAction(menu, INSISEditorConstants.INSERT_FILE);
        addAction(menu, INSISEditorConstants.INSERT_DIRECTORY);
        addAction(menu, INSISEditorConstants.INSERT_COLOR);
        addAction(menu, INSISEditorConstants.INSERT_REGFILE);
        addAction(menu, INSISEditorConstants.INSERT_REGKEY);
        addAction(menu, INSISEditorConstants.INSERT_REGVAL);
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
     */
    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
    {
        fAnnotationAccess= createAnnotationAccess();
        fOverviewRuler= createOverviewRuler(getSharedColors());

        NSISSourceViewer viewer= new NSISSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
        // ensure decoration support has been created and configured.
        SourceViewerDecorationSupport decorationSupport = getSourceViewerDecorationSupport(viewer);
        decorationSupport.setCharacterPairMatcher(new NSISCharacterPairMatcher());
        decorationSupport.setMatchingCharacterPainterPreferenceKeys(MATCHING_DELIMITERS,
                        MATCHING_DELIMITERS_COLOR);
        viewer.addProjectionListener(this);
        ISelectionChangedListener listener = new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event)
            {
                openCommandView();
            }
        };
        viewer.addSelectionChangedListener(listener);
        viewer.addPostSelectionChangedListener(listener);
        return viewer;
    }

    //This is copied from AbstractTextEditor so that we can support
    //text as well as custom drag & drop
    @Override
    protected void installTextDragAndDrop(ISourceViewer viewer)
    {
        if (mTextDragAndDropEnabled || viewer == null) {
            return;
        }

        if (mTextDragAndDropInstalled) {
            mTextDragAndDropEnabled= true;
            return;
        }

        final IDragAndDropService dndService= (IDragAndDropService)getSite().getService(IDragAndDropService.class);
        if (dndService == null) {
            return;
        }

        mTextDragAndDropEnabled= true;

        final StyledText st= viewer.getTextWidget();

        // Install drag source
        final ISelectionProvider selectionProvider= viewer.getSelectionProvider();
        final DragSource source= new DragSource(st, DND.DROP_COPY | DND.DROP_MOVE);
        source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        source.addDragListener(new DragSourceAdapter() {
            String mSelectedText;
            Point mSelection;
            @Override
            public void dragStart(DragSourceEvent event)
            {
                mTextDragAndDropToken= null;

                if (!mTextDragAndDropEnabled) {
                    event.doit= false;
                    event.image= null;
                    return;
                }

                try {
                    mSelection= st.getSelection();
                    int offset= st.getOffsetAtLocation(new Point(event.x, event.y));
                    Point p= st.getLocationAtOffset(offset);
                    if (p.x > event.x) {
                        offset--;
                    }
                    event.doit= offset > mSelection.x && offset < mSelection.y;

                    ISelection selection= selectionProvider.getSelection();
                    if (selection instanceof ITextSelection) {
                        mSelectedText= ((ITextSelection)selection).getText();
                    }
                    else {
                        mSelectedText= st.getSelectionText();
                    }
                } catch (IllegalArgumentException ex) {
                    event.doit= false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event)
            {
                event.data= mSelectedText;
                mTextDragAndDropToken= this; // Can be any non-null object
            }

            @Override
            public void dragFinished(DragSourceEvent event)
            {
                try {
                    if (event.detail == DND.DROP_MOVE && validateEditorInputState()) {
                        Point newSelection= st.getSelection();
                        int length= mSelection.y - mSelection.x;
                        int delta= 0;
                        if (newSelection.x < mSelection.x) {
                            delta= length;
                        }
                        st.replaceTextRange(mSelection.x + delta, length, ""); //$NON-NLS-1$

                        if (mTextDragAndDropToken == null) {
                            // Move in same editor - end compound change
                            IRewriteTarget target= (IRewriteTarget)getAdapter(IRewriteTarget.class);
                            if (target != null) {
                                target.endCompoundChange();
                            }
                        }

                    }
                } finally {
                    mTextDragAndDropToken= null;
                }
            }
        });

        // Install drag target
        DropTargetListener dropTargetListener= new DropTargetAdapter() {
            private Point mSelection;

            @Override
            public void dragEnter(DropTargetEvent event)
            {
                if(NSISCommandTransfer.INSTANCE.isSupportedType(event.currentDataType) ||
                                FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    //Don't want default feedback- we will do it ourselves
                    event.feedback = DND.FEEDBACK_NONE;
                    if (event.detail == DND.DROP_DEFAULT) {
                        event.detail = DND.DROP_COPY;
                    }
                }
                else {
                    mTextDragAndDropToken= null;
                    mSelection= st.getSelection();

                    if (!mTextDragAndDropEnabled)
                    {
                        event.detail= DND.DROP_NONE;
                        event.feedback= DND.FEEDBACK_NONE;
                        return;
                    }

                    if (event.detail == DND.DROP_DEFAULT) {
                        event.detail= DND.DROP_MOVE;
                    }
                }
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event)
            {
                if (NSISCommandTransfer.INSTANCE.isSupportedType(event.currentDataType) ||
                                FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    // Don't want default feedback- we will do it ourselves
                    event.feedback = DND.FEEDBACK_NONE;
                    if (event.detail == DND.DROP_DEFAULT) {
                        event.detail = DND.DROP_COPY;
                    }
                }
                else {
                    if (!mTextDragAndDropEnabled) {
                        event.detail= DND.DROP_NONE;
                        event.feedback= DND.FEEDBACK_NONE;
                        return;
                    }

                    if (event.detail == DND.DROP_DEFAULT) {
                        event.detail= DND.DROP_MOVE;
                    }
                }
            }

            @Override
            public void dragOver(DropTargetEvent event)
            {
                if (NSISCommandTransfer.INSTANCE.isSupportedType(event.currentDataType) ||
                                FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    // Don't want default feedback- we will do it ourselves
                    event.feedback = DND.FEEDBACK_NONE;
                    st.setFocus();
                    Point location = st.getDisplay().map(null, st, event.x, event.y);
                    location.x = Math.max(0, location.x);
                    location.y = Math.max(0, location.y);
                    int offset;
                    try {
                        offset = st.getOffsetAtLocation(new Point(location.x, location.y));
                    }
                    catch (IllegalArgumentException ex) {
                        try {
                            offset = st.getOffsetAtLocation(new Point(0, location.y));
                        }
                        catch (IllegalArgumentException ex2) {
                            offset = st.getCharCount();
                            Point maxLocation = st.getLocationAtOffset(offset);
                            if (location.y >= maxLocation.y) {
                                if (location.x < maxLocation.x) {
                                    offset = st.getOffsetAtLocation(new Point(location.x, maxLocation.y));
                                }
                            }
                        }
                    }
                    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                    offset = getCaretOffsetForInsertCommand(doc, offset);

                    st.setCaretOffset(offset);
                }
                else {
                    if (!mTextDragAndDropEnabled) {
                        event.feedback= DND.FEEDBACK_NONE;
                        return;
                    }

                    event.feedback |= DND.FEEDBACK_SCROLL;
                }
            }

            @Override
            public void drop(DropTargetEvent event)
            {
                if (NSISCommandTransfer.INSTANCE.isSupportedType(event.currentDataType)) {
                    insertCommand((NSISCommand)event.data, false);
                }
                else if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    int dropNSISFilesAction = NSISPreferences.getInstance().getPreferenceStore().getInt(DROP_EXTERNAL_FILES_ACTION);
                    switch(dropNSISFilesAction) {
                        case DROP_EXTERNAL_FILES_ASK:
                            MessageDialog dialog = new MessageDialog(getSite().getShell(),
                                            EclipseNSISPlugin.getResourceString("drop.external.files.ask.title"), //$NON-NLS-1$
                                            EclipseNSISPlugin.getShellImage(),
                                            EclipseNSISPlugin.getResourceString("drop.external.files.ask.message"), //$NON-NLS-1$
                                            MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL,
                                IDialogConstants.CANCEL_LABEL }, 0);
                            if(dialog.open() != 0) {
                                break;
                            }
                            //$FALL-THROUGH$
                        case DROP_EXTERNAL_FILES_OPEN_IN_EDITORS:
                            openFiles((String[])event.data);
                            return;
                        default:
                            break;
                    }
                    insertFiles((String[])event.data);
                }
                else {
                    try {
                        if (!mTextDragAndDropEnabled) {
                            return;
                        }

                        if (mTextDragAndDropToken != null && event.detail == DND.DROP_MOVE) {
                            // Move in same editor
                            int caretOffset= st.getCaretOffset();
                            if (mSelection.x <= caretOffset && caretOffset <= mSelection.y) {
                                event.detail= DND.DROP_NONE;
                                return;
                            }

                            // Start compound change
                            IRewriteTarget target= (IRewriteTarget)getAdapter(IRewriteTarget.class);
                            if (target != null) {
                                target.beginCompoundChange();
                            }
                        }

                        if (!validateEditorInputState()) {
                            event.detail= DND.DROP_NONE;
                            return;
                        }

                        String text= (String)event.data;
                        Point newSelection= st.getSelection();
                        st.insert(text);
                        st.setSelectionRange(newSelection.x, text.length());
                    }
                    finally {
                        mTextDragAndDropToken= null;
                    }
                }
            }
        };
        dndService.addMergedDropTarget(st, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY,
                        new Transfer[] {NSISCommandTransfer.INSTANCE,
                        FileTransfer.getInstance(),
                        TextTransfer.getInstance()},
                        dropTargetListener);

        mTextDragAndDropInstalled= true;
        mTextDragAndDropEnabled= true;
    }

    //This is copied from AbstractTextEditor so that we can support
    //text as well as custom drag & drop
    @Override
    protected void uninstallTextDragAndDrop(ISourceViewer viewer)
    {
        mTextDragAndDropEnabled= false;
    }
    /**
     * @param text
     * @param target
     */
    //    protected void initializeDragAndDrop(ISourceViewer viewer)
    //    {
    //        final StyledText text = viewer.getTextWidget();
    //        IDragAndDropService dndService= (IDragAndDropService)getSite().getService(IDragAndDropService.class);
    //        if(dndService != null) {
    //            int ops = DND.DROP_DEFAULT | DND.DROP_COPY;
    //            Transfer[] transfers = new Transfer[]{NSISCommandTransfer.INSTANCE,
    //                                                  FileTransfer.getInstance()};
    //
    //            final ITextEditorDropTargetListener listener = getTextEditorDropTargetListener();
    //
    //            if (listener != null) {
    //                ops |= DND.DROP_MOVE;
    //                transfers = (Transfer[])Common.joinArrays(new Object[] {transfers, listener.getTransfers()});
    //            }
    //
    //            DropTargetListener listener2 = new DropTargetListener() {
    //                public void dragEnter(DropTargetEvent e)
    //                {
    //                    if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType) ||
    //                       FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
    //                        //Don't want default feedback- we will do it ourselves
    //                        e.feedback = DND.FEEDBACK_NONE;
    //                        if (e.detail == DND.DROP_DEFAULT) {
    //                            e.detail = DND.DROP_COPY;
    //                        }
    //                    }
    //                    else if(listener != null) {
    //                        listener.dragEnter(e);
    //                    }
    //                }
    //
    //                public void dragOperationChanged(DropTargetEvent e)
    //                {
    //                    if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType) ||
    //                       FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
    //                        //Don't want default feedback- we will do it ourselves
    //                        e.feedback = DND.FEEDBACK_NONE;
    //                        if (e.detail == DND.DROP_DEFAULT) {
    //                            e.detail = DND.DROP_COPY;
    //                        }
    //                    }
    //                    else if(listener != null) {
    //                        listener.dragOperationChanged(e);
    //                    }
    //                }
    //
    //                public void dragOver(DropTargetEvent e)
    //                {
    //                    if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType) ||
    //                       FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
    //                        //Don't want default feedback- we will do it ourselves
    //                        e.feedback = DND.FEEDBACK_NONE;
    //                       text.setFocus();
    //                        Point location = text.getDisplay().map(null, text, e.x, e.y);
    //                        location.x = Math.max(0, location.x);
    //                        location.y = Math.max(0, location.y);
    //                        int offset;
    //                        try {
    //                            offset = text.getOffsetAtLocation(new Point(location.x, location.y));
    //                        }
    //                        catch (IllegalArgumentException ex) {
    //                            try {
    //                                offset = text.getOffsetAtLocation(new Point(0, location.y));
    //                            }
    //                            catch (IllegalArgumentException ex2) {
    //                                offset = text.getCharCount();
    //                                Point maxLocation = text.getLocationAtOffset(offset);
    //                                if (location.y >= maxLocation.y) {
    //                                    if (location.x < maxLocation.x) {
    //                                        offset = text.getOffsetAtLocation(new Point(location.x, maxLocation.y));
    //                                    }
    //                                }
    //                            }
    //                        }
    //                        IDocument doc = getDocumentProvider().getDocument(getEditorInput());
    //                        offset = getCaretOffsetForInsertCommand(doc, offset);
    //
    //                        text.setCaretOffset(offset);
    //                    }
    //                    else if(listener != null) {
    //                        listener.dragOver(e);
    //                    }
    //                }
    //
    //                public void drop(DropTargetEvent e)
    //                {
    //                    if(NSISCommandTransfer.INSTANCE.isSupportedType(e.currentDataType)) {
    //                        insertCommand((NSISCommand)e.data, false);
    //                    }
    //                    else if(FileTransfer.getInstance().isSupportedType(e.currentDataType)) {
    //                        insertFiles((String[])e.data);
    //                    }
    //                    else if(listener != null) {
    //                        listener.drop(e);
    //                    }
    //                }
    //
    //                public void dragLeave(DropTargetEvent e)
    //                {
    //                    if(listener != null) {
    //                        listener.dragLeave(e);
    //                    }
    //                }
    //
    //                public void dropAccept(DropTargetEvent e)
    //                {
    //                    if(listener != null) {
    //                        listener.dropAccept(e);
    //                    }
    //                }
    //            };
    //            dndService.addMergedDropTarget(text, DND.DROP_DEFAULT | DND.DROP_COPY,
    //                                           transfers,listener2);
    //        }
    //
    //
    //        IPreferenceStore store= getPreferenceStore();
    //        if (store != null && store.getBoolean(PREFERENCE_TEXT_DRAG_AND_DROP_ENABLED)) {
    //            installTextDragAndDrop(viewer);
    //        }
    //    }

    private void openFiles(String[] files)
    {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (int i = 0; i < files.length; i++) {
            try {
                IDE.openEditorOnFileStore(page, new LocalFile(new File(files[i])));
            }
            catch (PartInitException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    private void insertFiles(String[] files)
    {
        ISourceViewer viewer = getSourceViewer();
        if(!Common.isEmptyArray(files) && viewer != null) {
            StyledText styledText = viewer.getTextWidget();
            if(styledText != null && !styledText.isDisposed()) {
                Point sel = styledText.getSelection();
                try {
                    IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                    int offset = getCaretOffsetForInsertCommand(doc, sel.x);
                    styledText.setCaretOffset(offset);
                    StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                    String delim = doc.getLineDelimiter(styledText.getLineAtOffset(offset));
                    String fileKeyword = NSISKeywords.getInstance().getKeyword("File"); //$NON-NLS-1$
                    String recursiveKeyword = NSISKeywords.getInstance().getKeyword("/r"); //$NON-NLS-1$
                    RegistryImporter importer = null;
                    NSISEditorRegistryImportStrategy strategy = null;

                    for (int i = 0; i < files.length; i++) {
                        if(IOUtility.isValidFile(files[i])) {
                            if(files[i].regionMatches(true, files[i].length()-REG_FILE_EXTENSION.length(), REG_FILE_EXTENSION, 0, REG_FILE_EXTENSION.length())) {
                                if(importer == null || strategy == null) {
                                    importer = new RegistryImporter();
                                    strategy = new NSISEditorRegistryImportStrategy();
                                }
                                else {
                                    strategy.reset();
                                }
                                try {
                                    importer.importRegFile(styledText.getShell(), files[i], strategy);
                                    buf.append(strategy.getText()).append(delim);
                                    continue;
                                }
                                catch (Exception e) {
                                    EclipseNSISPlugin.getDefault().log(e);
                                }
                            }
                            buf.append(fileKeyword).append(" ").append( //$NON-NLS-1$
                                            IOUtility.resolveFileName(files[i], this)).append(
                                                            delim);
                        }
                        else {
                            buf.append(fileKeyword).append(" ").append(recursiveKeyword).append( //$NON-NLS-1$
                            " ").append(IOUtility.resolveFileName(files[i], this)).append(delim); //$NON-NLS-1$
                        }
                    }
                    String text = buf.toString();
                    doc.replace(offset, 0, text);
                    styledText.setCaretOffset(offset + text.length());
                }
                catch (Exception e) {
                    Common.openError(styledText.getShell(), e.getMessage(), EclipseNSISPlugin.getShellImage());
                    styledText.setSelection(sel);
                }
            }
        }
    }

    private int getCaretOffsetForInsertCommand(IDocument doc, int offset)
    {
        int offset2 = offset;
        if(doc != null) {
            ITypedRegion[][] regions = NSISTextUtility.getNSISLines(doc, offset2);
            if(Common.isEmptyArray(regions)) {
                try {
                    ITypedRegion partition = NSISTextUtility.getNSISPartitionAtOffset(doc, offset2);
                    if(partition.getType().equals(NSISPartitionScanner.NSIS_SINGLELINE_COMMENT) ||
                                    partition.getType().equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                        offset2 = partition.getOffset();
                        if(offset2 > 0) {
                            offset2--;
                            partition = NSISTextUtility.getNSISPartitionAtOffset(doc, offset2);
                            if(partition.getType().equals(NSISPartitionScanner.NSIS_SINGLELINE_COMMENT) ||
                                            partition.getType().equals(NSISPartitionScanner.NSIS_MULTILINE_COMMENT)) {
                                offset2 = offset2+1;
                            }
                            else {
                                int line1 = doc.getLineOfOffset(offset2);
                                int line2 = doc.getLineOfOffset(offset2+1);
                                if(line1 == line2) {
                                    offset2 = getCaretOffsetForInsertCommand(doc, offset2);
                                }
                                else {
                                    IRegion info  = doc.getLineInformation(line1);
                                    String s = doc.get(info.getOffset()+info.getLength()-1,1);
                                    if(s.charAt(0) == INSISConstants.LINE_CONTINUATION_CHAR) {
                                        offset2 = getCaretOffsetForInsertCommand(doc,info.getOffset()+info.getLength()-1);
                                    }
                                    else {
                                        offset2++;
                                    }
                                }
                            }
                        }
                    }
                }
                catch (BadLocationException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else {
                offset2 = regions[0][0].getOffset();
            }
        }
        return offset2;
    }

    private void insertCommand(NSISCommand command, boolean updateOffset)
    {
        ISourceViewer viewer = getSourceViewer();
        if(viewer != null) {
            StyledText styledText = viewer.getTextWidget();
            if(styledText != null && !styledText.isDisposed()) {
                if(command != null) {
                    NSISCommandResult result = null;
                    if (command.hasParameters()) {
                        NSISCommandDialog dlg = new NSISCommandDialog(styledText.getShell(), command);
                        int code = dlg.open();
                        if (code == Window.OK) {
                            result = dlg.getCommandResult();
                        }
                    }
                    else {
                        result = command.getResult();
                    }
                    if (result != null && !Common.isEmpty(result.getContent())) {
                        Point sel = styledText.getSelection();
                        try {
                            IDocument doc = getDocumentProvider().getDocument(getEditorInput());
                            if (updateOffset) {
                                int offset = getCaretOffsetForInsertCommand(doc, sel.x);
                                styledText.setCaretOffset(offset);
                                styledText.setFocus();
                            }
                            int offset = styledText.getCaretOffset();
                            doc.replace(offset, 0, result.getContent());
                            styledText.setCaretOffset(offset + result.getCursorPos());
                        }
                        catch (Exception e) {
                            Common.openError(styledText.getShell(), e.getMessage(), EclipseNSISPlugin.getShellImage());
                            if (updateOffset) {
                                styledText.setSelection(sel);
                            }
                        }
                    }
                }
            }
        }
    }

    public void insertCommand(NSISCommand command)
    {
        insertCommand(command, true);
    }

    public void projectionDisabled()
    {
    }

    public void projectionEnabled()
    {
        if(mOutlineContentProvider != null) {
            mOutlineContentProvider.refresh();
        }
    }

    /*
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int, int)
     */
    @Override
    protected void adjustHighlightRange(int offset, int length)
    {
        ISourceViewer viewer= getSourceViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }

    public Point getSelectedRange()
    {
        ISourceViewer sourceViewer = getSourceViewer();
        if(sourceViewer != null) {
            return sourceViewer.getSelectedRange();
        }
        else {
            return new Point(0,0);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class required)
    {
        ISourceViewer sourceViewer = getSourceViewer();
        if (IContentOutlinePage.class.equals(required)) {
            if (mOutlinePage == null || mOutlinePage.isDisposed()) {
                mCurrentPosition = null;
                mOutlinePage= new NSISContentOutlinePage(this);
                if (getEditorInput() != null) {
                    mOutlinePage.setInput(getEditorInput());
                }
            }
            return mOutlinePage;
        }

        if (mProjectionSupport != null) {
            Object adapter= mProjectionSupport.getAdapter(sourceViewer, required);
            if (adapter != null) {
                return adapter;
            }
        }

        return super.getAdapter(required);
    }

    /* (non-Javadoc)
     * Method declared on AbstractTextEditor
     */
    @Override
    protected void initializeEditor()
    {
        super.initializeEditor();
        IPreferenceStore preferenceStore = NSISPreferences.getInstance().getPreferenceStore();
        preferenceStore = new ChainedPreferenceStore(new IPreferenceStore[]{preferenceStore, EditorsUI.getPreferenceStore()});
        setPreferenceStore(preferenceStore);
        setSourceViewerConfiguration(new NSISEditorSourceViewerConfiguration(preferenceStore));
    }

    public void addAction(NSISAction action)
    {
        mActions.add(action);
    }

    public void removeAction(NSISAction action)
    {
        mActions.remove(action);
    }

    public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
    {
        try {
            if(monitor != null) {
                monitor.beginTask(EclipseNSISPlugin.getResourceString("updating.actions.message"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
            }
            updateActionsState();
        }
        finally {
            if(monitor != null) {
                monitor.done();
            }
        }
    }

    void updatePresentation()
    {
        try {
            ISourceViewer sourceViewer = getSourceViewer();
            if(sourceViewer instanceof NSISSourceViewer) {
                NSISSourceViewer viewer = (NSISSourceViewer)sourceViewer;
                if(viewer.mustProcessPropertyQueue()) {
                    viewer.processPropertyQueue();
                }
            }
        }
        catch(Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
        }
    }

    public void updateActionsState()
    {
        if(equals(getEditorSite().getPage().getActiveEditor())) {
            for(Iterator<NSISAction> iter=mActions.iterator(); iter.hasNext(); ) {
                IActionDelegate action = iter.next();
                if(action instanceof NSISScriptAction) {
                    ((NSISScriptAction)action).updateActionState();
                }
            }
        }
    }

    @Override
    protected String[] collectContextMenuPreferencePages()
    {
        String[] pages = {EDITOR_PREFERENCE_PAGE_ID, TEMPLATES_PREFERENCE_PAGE_ID, TASKTAGS_PREFERENCE_PAGE_ID};
        return (String[])Common.joinArrays(new Object[]{pages,super.collectContextMenuPreferencePages()});
    }

    @Override
    protected void performSaveAs(IProgressMonitor progressMonitor)
    {
        super.performSaveAs(progressMonitor);
        if(equals(getEditorSite().getPage().getActiveEditor())) {
            NSISAction[] actions = mActions.toArray(new NSISAction[mActions.size()]);
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] instanceof NSISScriptAction) {
                    ((NSISScriptAction)actions[i]).updateInput();
                }
            }
        }
        updateTaskTagMarkers(new NSISTaskTagUpdater());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorSaved()
     */
    @Override
    protected void editorSaved()
    {
        super.editorSaved();
        updateOutlinePage();
        updateActionsState();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation()
        {
            @Override
            protected void execute(IProgressMonitor monitor)
            {
                NSISTaskTagUpdater taskTagUpdater = new NSISTaskTagUpdater();
                updateTaskTagMarkers(taskTagUpdater);
            }
        };
        try {
            op.run(null);
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    private void openCommandView()
    {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if(activePage != null) {
                        IViewPart view = activePage.findView(COMMANDS_VIEW_ID);
                        if(view == null) {
                            activePage.showView(COMMANDS_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
                        }
                    }
                }
                catch(PartInitException pie) {
                    EclipseNSISPlugin.getDefault().log(pie);
                }
            }
        });
    }

    IAnnotationModel getAnnotationModel()
    {
        ISourceViewer viewer = getSourceViewer();
        if(viewer != null) {
            return viewer.getAnnotationModel();
        }
        return null;
    }

    /**
     * @param file
     */
    public void updateTaskTagMarkers(NSISTaskTagUpdater taskTagUpdater)
    {
        IEditorInput editorInput = getEditorInput();
        if(editorInput instanceof IFileEditorInput) {
            taskTagUpdater.updateTaskTags(((IFileEditorInput)editorInput).getFile(), getSourceViewer().getDocument());
        }
    }

    public void refreshOutline()
    {
        if(mOutlinePage != null) {
            mOutlinePage.refreshLabels();
        }
    }
    /**
     *
     */
    private void updateOutlinePage()
    {
        if (mOutlinePage != null) {
            mCurrentPosition = null;
            mOutlinePage.update();
        }
        else if(mOutlineContentProvider != null) {
            mOutlineContentProvider.refresh();
        }
    }

    private void updateAnnotations()
    {
        IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(this);
        if(!(input instanceof IFileEditorInput)){
            File file = new File(input.getPath().toOSString());
            if(IOUtility.isValidFile(file)) {
                MakeNSISResults results = NSISCompileTestUtility.INSTANCE.getCachedResults(file);
                if(results != null) {
                    NSISEditorUtilities.getMarkerAssistant(file).updateMarkers(this, results);
                }
            }
        }
    }

    /**
     * @return Returns the outlineContentProvider.
     */
    public NSISOutlineContentProvider getOutlineContentProvider()
    {
        return mOutlineContentProvider;
    }

    public void exportHTML()
    {
        if(isDirty()) {
            IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(this);
            if(!Common.openConfirm(getSourceViewer().getTextWidget().getShell(),
                            EclipseNSISPlugin.getFormattedString("export.html.save.confirmation", //$NON-NLS-1$
                                            new Object[] {input.getPath().lastSegment()}),
                                            EclipseNSISPlugin.getShellImage())) {
                return;
            }
            IProgressMonitor monitor = getProgressMonitor();
            doSave(monitor);
            if(monitor.isCanceled()) {
                return;
            }
        }
        if(mHTMLExporter == null) {
            mHTMLExporter = new HTMLExporter(this, getSourceViewer());
        }
        mHTMLExporter.exportHTML();
    }

    private class NSISStickyHelpAction extends TextEditorAction
    {
        private final TextOperationAction mTextOperationAction;
        private InformationPresenter mInformationPresenter;

        public NSISStickyHelpAction(ResourceBundle resourceBundle, String prefix, final TextOperationAction textOperationAction)
        {
            super(resourceBundle, prefix, NSISEditor.this);
            if (textOperationAction == null) {
                throw new IllegalArgumentException();
            }
            mTextOperationAction= textOperationAction;
        }

        private InformationPresenter getInformationPresenter()
        {
            if(mInformationPresenter == null) {
                mInformationPresenter = NSISEditorUtilities.createStickyHelpInformationPresenter();
            }
            return mInformationPresenter;
        }
        /*
         *  @see org.eclipse.jface.action.IAction#run()
         */
        @Override
        public void run()
        {
            if(NSISHelpURLProvider.getInstance().isNSISHelpAvailable()) {
                ISourceViewer sourceViewer = getSourceViewer();
                int offset = computeOffset(sourceViewer);
                if(offset == -1) {
                    mTextOperationAction.run();
                }
                else {
                    InformationPresenter informationPresenter = getInformationPresenter();
                    informationPresenter.install(sourceViewer);
                    informationPresenter.setOffset(offset); //wordRegion.getOffset());
                    informationPresenter.showInformation();
                }
            }
        }

        /**
         * @param sourceViewer
         * @return
         */
        protected int computeOffset(ISourceViewer sourceViewer)
        {
            return NSISTextUtility.computeOffset(sourceViewer,NSISTextUtility.COMPUTE_OFFSET_HOVER_LOCATION);
        }
    }

    private class NSISPopupStickyHelpAction extends NSISStickyHelpAction
    {
        private Point mClickPoint = null;
        public NSISPopupStickyHelpAction(ResourceBundle resourceBundle, String prefix, TextOperationAction textOperationAction)
        {
            super(resourceBundle, prefix, textOperationAction);
        }

        public void setClickPoint(Point p)
        {
            mClickPoint = p;
        }

        @Override
        protected int computeOffset(ISourceViewer sourceViewer)
        {
            if(mClickPoint != null && sourceViewer != null && sourceViewer.getTextWidget() != null) {
                Point p = sourceViewer.getTextWidget().toControl(mClickPoint);
                return NSISTextUtility.computeOffsetAtLocation(sourceViewer, p.x, p.y);
            }
            else {
                return -1;
            }
        }
    }
}
