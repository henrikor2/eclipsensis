/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.io.File;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.actions.*;
import net.sf.eclipsensis.installoptions.actions.MatchSizeAction;
import net.sf.eclipsensis.installoptions.actions.RevertToSavedAction;
import net.sf.eclipsensis.installoptions.builder.InstallOptionsNature;
import net.sf.eclipsensis.installoptions.dialogs.GridSnapGlueSettingsDialog;
import net.sf.eclipsensis.installoptions.dnd.*;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.edit.dialog.InstallOptionsDialogEditPart;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.tabbed.CustomTabbedPropertySheetPage;
import net.sf.eclipsensis.installoptions.rulers.*;
import net.sf.eclipsensis.installoptions.template.*;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.startup.FileAssociationChecker;
import net.sf.eclipsensis.template.AbstractTemplateSettings;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.winapi.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.parts.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.*;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.palette.*;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.customize.PaletteSettingsDialog;
import org.eclipse.gef.ui.parts.*;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.views.palette.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

public class InstallOptionsDesignEditor extends EditorPart implements INSISHomeListener, IInstallOptionsEditor, CommandStackListener, ISelectionListener, ITabbedPropertySheetPageContributor
{
    private boolean mDisposed = false;
    private DefaultEditDomain mEditDomain;
    private GraphicalViewer mGraphicalViewer;
    private ActionRegistry mActionRegistry;
    private SelectionSynchronizer mSynchronizer;
    private List<String> mSelectionActions = new ArrayList<String>();
    private List<String> mStackActions = new ArrayList<String>();
    private List<String> mPropertyActions = new ArrayList<String>();
    private PaletteViewerProvider mPaletteProvider;
    private FlyoutPaletteComposite mPalette;
    private CustomPalettePage mPalettePage;
    private boolean mSwitching = false;
    private INIFile mINIFile;
    private KeyHandler mSharedKeyHandler;
    private boolean mCreatedEmptyPart = true;
    private IModelListener mModelListener = new IModelListener()
    {
        public void modelChanged()
        {
            GraphicalViewer viewer = getGraphicalViewer();
            if(viewer != null) {
                ISelection sel = viewer.getSelection();
                viewer.setSelection(sel);
            }
            InstallOptionsDialog dialog = getInstallOptionsDialog();
            dialog.modelChanged();
            if(dialog.canUpdateINIFile()) {
                dialog.updateINIFile();
                mINIFile.updateDocument();
            }
            mINIFile.validate(true);
            if(mINIFile.hasErrors()) {
                IWorkbenchWindow window = InstallOptionsDesignEditor.this.getSite().getWorkbenchWindow();
                if(PlatformUI.getWorkbench().getActiveWorkbenchWindow() == window) {
                    if(window.getPartService().getActivePart() == InstallOptionsDesignEditor.this) {
                        checkPerformSwitch();
                    }
                }
            }
        }
    };

    private int mErrorCorrectionOnSave;
    private PaletteRoot mRoot;
    private Font mInstallOptionsFont;

    private OutlinePage mOutlinePage;
    private boolean mEditorSaving = false;

    private IWindowListener mWindowListener = new IWindowListener() {
        public void windowActivated(IWorkbenchWindow window)
        {
            if(window == InstallOptionsDesignEditor.this.getSite().getWorkbenchWindow()) {
                if(window.getPartService().getActivePart() == InstallOptionsDesignEditor.this) {
                    checkPerformSwitch();
                }
            }
        }

        public void windowDeactivated(IWorkbenchWindow window)
        {
        }

        public void windowClosed(IWorkbenchWindow window)
        {
        }

        public void windowOpened(IWorkbenchWindow window)
        {
        }
    };

    private IPartListener mPartListener = new IPartListener() {
        // If an open, unsaved file was deleted, query the user to either do a
        // "Save As"
        // or close the editor.
        public void partActivated(IWorkbenchPart part)
        {
            if (part != InstallOptionsDesignEditor.this) {
                return;
            }
            boolean exists = false;
            Object source = ((IInstallOptionsEditorInput)getEditorInput()).getSource();
            if(source instanceof IFile) {
                exists = ((IFile)source).exists();
            }
            else if(source instanceof IPath) {
                exists = new File(((IPath)source).toOSString()).exists();
            }
            if (!exists) {
                Shell shell = getSite().getShell();
                String title = InstallOptionsPlugin.getResourceString("file.deleted.error.title"); //$NON-NLS-1$
                String message = InstallOptionsPlugin.getResourceString("file.deleted.error.message"); //$NON-NLS-1$
                String[] buttons = {InstallOptionsPlugin.getResourceString("save.button.name"), InstallOptionsPlugin.getResourceString("close.button.name")}; //$NON-NLS-1$ //$NON-NLS-2$
                MessageDialog dialog = new MessageDialog(shell, title, InstallOptionsPlugin.getShellImage(),
                                message, MessageDialog.QUESTION, buttons, 0);
                if (dialog.open() == 0) {
                    if (!performSaveAs()) {
                        partActivated(part);
                    }
                }
                else {
                    closeEditor(false);
                }
            }
            checkPerformSwitch();
        }

        public void partBroughtToTop(IWorkbenchPart part)
        {
        }

        public void partClosed(IWorkbenchPart part)
        {
            if (part != InstallOptionsDesignEditor.this) {
                return;
            }
            IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
            window.getSelectionService().removeSelectionListener(InstallOptionsDesignEditor.this);
            part.getSite().getWorkbenchWindow().getWorkbench().removeWindowListener(mWindowListener);
        }

        public void partDeactivated(IWorkbenchPart part)
        {
        }

        public void partOpened(final IWorkbenchPart part)
        {
            if (part != InstallOptionsDesignEditor.this) {
                return;
            }
            IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
            window.getSelectionService().addSelectionListener(InstallOptionsDesignEditor.this);
            part.getSite().getWorkbenchWindow().getWorkbench().addWindowListener(mWindowListener);
        }
    };

    private InstallOptionsDialog mInstallOptionsDialog = null;

    private Object mJobFamily = new Object();
    private ISchedulingRule mSchedulingRule = new ISchedulingRule() {
        public boolean contains(ISchedulingRule rule)
        {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule)
        {
            return rule == this;
        }
    };
    private IJobStatusRunnable mJobStatusRunnable = new IJobStatusRunnable() {
        public IStatus run(IProgressMonitor monitor)
        {
            try {
                GraphicalViewer viewer = getGraphicalViewer();
                if(viewer != null) {
                    EditPart editPart = viewer.getContents();
                    if(editPart instanceof InstallOptionsDialogEditPart) {
                        viewer.setContents(null);
                        viewer.setContents(editPart);
                    }
                    if(mRulerComposite != null && !mRulerComposite.isDisposed()) {
                        mRulerComposite.setGraphicalViewer((InstallOptionsGraphicalViewer)viewer);
                    }
                }
                return Status.OK_STATUS;
            }
            catch (Exception e) {
                return new Status(IStatus.ERROR,IInstallOptionsConstants.PLUGIN_ID, IStatus.ERROR,
                                e.getMessage(), e);
            }
        }
    };

    private boolean mSavePreviouslyNeeded = false;

    private ResourceTracker mResourceListener = new ResourceTracker();

    private InstallOptionsRulerComposite mRulerComposite;

    protected static final String PALETTE_DOCK_LOCATION = "PaletteDockLocation"; //$NON-NLS-1$

    protected static final String PALETTE_SIZE = "PaletteSize"; //$NON-NLS-1$

    protected static final String PALETTE_STATE = "PaletteState"; //$NON-NLS-1$

    protected static final int DEFAULT_PALETTE_SIZE = 130;
    private static IPreferenceStore cPreferenceStore = InstallOptionsPlugin.getDefault().getPreferenceStore();

    static {
        cPreferenceStore.setDefault(PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
    }

    public InstallOptionsDesignEditor()
    {
        mINIFile = new INIFile();
        mINIFile.setValidateFixMode(INILine.VALIDATE_FIX_ERRORS);
        FileAssociationChecker.checkFileAssociation(FILE_ASSOCIATION_ID);
        setEditDomain(new InstallOptionsEditDomain(this));
    }

    public String getContributorId()
    {
        return TABBED_PROPERTIES_CONTRIBUTOR_ID;
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
     */
    @Override
    protected void firePropertyChange(int property)
    {
        super.firePropertyChange(property);
        updateActions(mPropertyActions);
    }

    /**
     * Lazily creates and returns the action registry.
     * @return the action registry
     */
    public ActionRegistry getActionRegistry()
    {
        if (mActionRegistry == null) {
            mActionRegistry = new ActionRegistry();
        }
        return mActionRegistry;
    }

    /**
     * Returns the command stack.
     * @return the command stack
     */
    protected CommandStack getCommandStack()
    {
        return getEditDomain().getCommandStack();
    }

    /**
     * Returns the edit domain.
     * @return the edit domain
     */
    public DefaultEditDomain getEditDomain()
    {
        return mEditDomain;
    }

    /**
     * Returns the graphical viewer.
     * @return the graphical viewer
     */
    public GraphicalViewer getGraphicalViewer()
    {
        return mGraphicalViewer;
    }

    /**
     * Returns the list of {@link IAction IActions} dependent on property changes in the
     * Editor.  These actions should implement the {@link UpdateAction} interface so that they
     * can be updated in response to property changes.  An example is the "Save" action.
     * @return the list of property-dependant actions
     */
    protected List<String> getPropertyActions()
    {
        return mPropertyActions;
    }

    /**
     * Returns the list of {@link IAction IActions} dependent on changes in the workbench's
     * {@link ISelectionService}. These actions should implement the {@link UpdateAction}
     * interface so that they can be updated in response to selection changes.  An example is
     * the Delete action.
     * @return the list of selection-dependant actions
     */
    protected List<String> getSelectionActions()
    {
        return mSelectionActions;
    }

    /**
     * Returns the selection syncronizer object. The synchronizer can be used to sync the
     * selection of 2 or more EditPartViewers.
     * @return the syncrhonizer
     */
    protected SelectionSynchronizer getSelectionSynchronizer()
    {
        if (mSynchronizer == null) {
            mSynchronizer = new SelectionSynchronizer();
        }
        return mSynchronizer;
    }

    /**
     * Returns the list of {@link IAction IActions} dependant on the CommmandStack's state.
     * These actions should implement the {@link UpdateAction} interface so that they can be
     * updated in response to command stack changes.  An example is the "undo" action.
     * @return the list of stack-dependant actions
     */
    protected List<String> getStackActions()
    {
        return mStackActions;
    }

    /**
     * Hooks the GraphicalViewer to the rest of the Editor.  By default, the viewer
     * is added to the SelectionSynchronizer, which can be used to keep 2 or more
     * EditPartViewers in sync.  The viewer is also registered as the ISelectionProvider
     * for the Editor's PartSite.
     */
    protected void hookGraphicalViewer()
    {
        getSelectionSynchronizer().addViewer(getGraphicalViewer());
        getSite().setSelectionProvider(getGraphicalViewer());
        getGraphicalViewer().addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged(SelectionChangedEvent event)
            {
                showPropertiesView();
            }
        });
    }

    /**
     * Sets the site and input for this editor then creates and initializes the actions.
     * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
    {
        setSite(site);
        setInput(input);
        getCommandStack().addCommandStackListener(this);
        ((IContextService)site.getService(IContextService.class)).activateContext(EDITING_INSTALLOPTIONS_DESIGN_CONTEXT_ID);
        initializeActionRegistry();
    }

    /**
     * Initializes the ActionRegistry.  This registry may be used by {@link
     * ActionBarContributor ActionBarContributors} and/or {@link ContextMenuProvider
     * ContextMenuProviders}.
     * <P>This method may be called on Editor creation, or lazily the first time {@link
     * #getActionRegistry()} is called.
     */
    protected void initializeActionRegistry()
    {
        createActions();
        updateActions(mPropertyActions);
        updateActions(mStackActions);
    }

    /**
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        if(!mCreatedEmptyPart) {
            // If not the active editor, ignore selection changed.
            IEditorPart activeEditor = getSite().getPage().getActiveEditor();
            if(activeEditor != null) {
                Object adapter = activeEditor.getAdapter(getClass());
                if (this.equals(adapter)) {
                    updateActions(mSelectionActions);
                }
            }
        }
    }

    /**
     * Sets the ActionRegistry for this EditorPart.
     * @param registry the registry
     */
    public void setActionRegistry(ActionRegistry registry)
    {
        mActionRegistry = registry;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        if(!mCreatedEmptyPart) {
            getGraphicalViewer().getControl().setFocus();
        }
    }

    /**
     * Sets the graphicalViewer for this EditorPart.
     * @param viewer the graphical viewer
     */
    protected void setGraphicalViewer(GraphicalViewer viewer)
    {
        this.mGraphicalViewer = viewer;
        getEditDomain().addViewer(viewer);
        getEditDomain().setPaletteRoot(getPaletteRoot());
    }

    /**
     * A convenience method for updating a set of actions defined by the given List of action
     * IDs. The actions are found by looking up the ID in the {@link #getActionRegistry()
     * action registry}. If the corresponding action is an {@link UpdateAction}, it will have
     * its <code>update()</code> method called.
     * @param actionIds the list of IDs to update
     */
    protected void updateActions(List<String> actionIds)
    {
        ActionRegistry registry = getActionRegistry();
        Iterator<String> iter = actionIds.iterator();
        while (iter.hasNext()) {
            IAction action = registry.getAction(iter.next());
            if (action instanceof UpdateAction) {
                ((UpdateAction)action).update();
            }
        }
    }

    public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
    {
        Font font = FontUtility.getInstallOptionsFont();
        if(font != null && font != mInstallOptionsFont) {
            mInstallOptionsFont = FontUtility.getInstallOptionsFont();
            JobScheduler scheduler = InstallOptionsPlugin.getDefault().getJobScheduler();
            scheduler.scheduleUIJob(mJobFamily,InstallOptionsPlugin.getResourceString("refresh.design.editor.job.name"), //$NON-NLS-1$
                            mSchedulingRule,mJobStatusRunnable);
        }
    }

    /**
     * @see GraphicalEditor#createPartControl(Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        if(!mINIFile.hasErrors()) {
            mInstallOptionsFont = FontUtility.getInstallOptionsFont();
            mPalette = new FlyoutPaletteComposite(parent, SWT.NONE, getSite().getPage(),
                            getPaletteViewerProvider(), getPalettePreferences());
            createGraphicalViewer(mPalette);
            mPalette.setGraphicalControl(getGraphicalControl());
            if (mPalettePage != null) {
                mPalette.setExternalViewer(mPalettePage.getPaletteViewer());
                mPalettePage = null;
            }
            InstallOptionsModel.INSTANCE.addModelListener(mModelListener);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,PLUGIN_CONTEXT_PREFIX+"installoptions_designeditor_context"); //$NON-NLS-1$
            mCreatedEmptyPart = false;
            NSISPreferences.getInstance().addListener(this);
        }
    }

    /**
     * Returns the palette viewer provider that is used to create palettes for the view and
     * the flyout.  Creates one if it doesn't already exist.
     *
     * @return  the PaletteViewerProvider that can be used to create PaletteViewers for
     *          this editor
     * @see #createPaletteViewerProvider()
     */
    protected final PaletteViewerProvider getPaletteViewerProvider()
    {
        if (mPaletteProvider == null) {
            mPaletteProvider = createPaletteViewerProvider();
        }
        return mPaletteProvider;
    }

    /**
     * Sets the edit domain for this editor.
     *
     * @param   editDomain  The new EditDomain
     */
    protected void setEditDomain(DefaultEditDomain editDomain)
    {
        mEditDomain = editDomain;
    }

    protected void closeEditor(boolean save)
    {
        getSite().getPage().closeEditor(this, save);
    }

    public void commandStackChanged(EventObject event)
    {
        if (isDirty()) {
            if (!savePreviouslyNeeded()) {
                setSavePreviouslyNeeded(true);
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        }
        else {
            setSavePreviouslyNeeded(false);
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        updateActions(mStackActions);
    }

    protected void configureGraphicalViewer()
    {
        getGraphicalViewer().getControl().setBackground(ColorConstants.listBackground);
        InstallOptionsGraphicalViewer viewer = (InstallOptionsGraphicalViewer)getGraphicalViewer();

        InstallOptionsRootEditPart root = new InstallOptionsRootEditPart();
        viewer.setRootEditPart(root);

        viewer.setEditPartFactory(GraphicalPartFactory.INSTANCE);
        ContextMenuProvider provider = new InstallOptionsDesignMenuProvider(this,//viewer,
                        getActionRegistry());
        viewer.setContextMenu(provider);
        ((IEditorSite)getSite()).registerContextMenu("net.sf.eclipsensis.installoptions.editor.installoptionseditor.contextmenu", //$NON-NLS-1$
                        provider, viewer, false);
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer).setParent(getCommonKeyHandler()));
        IFile file = null;
        Object source = ((IInstallOptionsEditorInput)getEditorInput()).getSource();
        if(source instanceof IFile) {
            file = (IFile)source;
        }
        loadProperties(file);

        // Actions
        IAction showRulers = new ToggleRulerVisibilityAction(getGraphicalViewer());
        getActionRegistry().registerAction(showRulers);

        IAction showGrid = new ToggleGridVisibilityAction(getGraphicalViewer());
        getActionRegistry().registerAction(showGrid);

        IAction showGuides = new ToggleGuideVisibilityAction(getGraphicalViewer());
        getActionRegistry().registerAction(showGuides);
    }

    protected CustomPalettePage createPalettePage()
    {
        return new CustomPalettePage(getPaletteViewerProvider());
    }

    protected PaletteViewerProvider createPaletteViewerProvider()
    {
        return new PaletteViewerProvider(getEditDomain()) {
            @Override
            protected void configurePaletteViewer(PaletteViewer viewer)
            {
                super.configurePaletteViewer(viewer);
                viewer.setContextMenu(new CustomPaletteContextMenuProvider(viewer));
                viewer.addDragSourceListener(new InstallOptionsTemplateTransferDragSourceListener(viewer));
            }

            @Override
            public PaletteViewer createPaletteViewer(Composite parent)
            {
                PaletteViewer paletteViewer = super.createPaletteViewer(parent);
                paletteViewer.setPaletteViewerPreferences(new PaletteViewerPreferences(cPreferenceStore));
                return paletteViewer;
            }
        };
    }

    @Override
    public void dispose()
    {
        NSISPreferences.getInstance().removeListener(this);
        InstallOptionsModel.INSTANCE.removeModelListener(mModelListener);
        boolean hasErrors = mINIFile.hasErrors();
        IInstallOptionsEditorInput input = (IInstallOptionsEditorInput)getEditorInput();
        IFile file = null;
        Object source = input.getSource();
        if(source instanceof IFile) {
            file = (IFile)source;
            file.getWorkspace().removeResourceChangeListener(mResourceListener);
        }
        IDocumentProvider provider = input.getDocumentProvider();
        if(provider != null) {
            IDocument doc = provider.getDocument(input);
            if(doc != null) {
                if(isSwitching()) {
                    updateDocument();
                }
                input.getDocumentProvider().disconnect(input);
                mINIFile.disconnect(doc);
            }
        }

        getSite().getWorkbenchWindow().getPartService().removePartListener(mPartListener);
        mPartListener = null;
        if(!hasErrors && file != null) {
            saveProperties(file);
        }
        getCommandStack().removeCommandStackListener(this);
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        getEditDomain().setActiveTool(null);
        ((InstallOptionsEditDomain)getEditDomain()).setFile((IFile)null);
        getActionRegistry().dispose();
        super.dispose();
        mDisposed = true;
    }

    public boolean isDisposed()
    {
        return mDisposed;
    }

    protected void handleExceptionOnSave(IInstallOptionsEditorInput input, IDocumentProvider p,
                    CoreException exception, IProgressMonitor progressMonitor)
    {
        try {
            ++mErrorCorrectionOnSave;

            Shell shell= getSite().getShell();

            boolean isSynchronized= false;

            if (p instanceof IDocumentProviderExtension3)  {
                IDocumentProviderExtension3 p3= (IDocumentProviderExtension3) p;
                isSynchronized= p3.isSynchronized(input);
            } else  {
                long modifiedStamp= p.getModificationStamp(input);
                long synchStamp= p.getSynchronizationStamp(input);
                isSynchronized= modifiedStamp == synchStamp;
            }

            if (mErrorCorrectionOnSave == 1 && !isSynchronized) {

                String title= InstallOptionsPlugin.getResourceString("outofsync.error.save.title"); //$NON-NLS-1$
                String msg= InstallOptionsPlugin.getResourceString("outofsync.error.save.message"); //$NON-NLS-1$

                if (Common.openQuestion(shell, title, msg, InstallOptionsPlugin.getShellImage())) {
                    performSave(input, p, true, progressMonitor);
                }
                else {
                    if (progressMonitor != null) {
                        progressMonitor.setCanceled(true);
                    }
                }
            }
            else {
                String title= InstallOptionsPlugin.getResourceString("error.save.title"); //$NON-NLS-1$
                String msg= InstallOptionsPlugin.getFormattedString("error.save.message", new Object[] { exception.getMessage() }); //$NON-NLS-1$
                ErrorDialog.openError(shell, title, msg, exception.getStatus());

                if (progressMonitor != null) {
                    progressMonitor.setCanceled(true);
                }
            }
        }
        finally {
            --mErrorCorrectionOnSave;
        }
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor)
    {
        try {
            IInstallOptionsEditorInput input = (IInstallOptionsEditorInput)getEditorInput();
            if(input != null) {
                IDocumentProvider provider = input.getDocumentProvider();
                if(provider != null) {
                    updateDocument();
                    performSave(input, provider, true, progressMonitor);
                }
            }
        }
        catch (Exception e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
    }

    protected void editorSaved(IInstallOptionsEditorInput input)
    {
        Object source = input.getSource();
        if(source instanceof IFile) {
            IFile file = (IFile)source;
            saveProperties(file);
        }
    }

    public void doRevertToSaved()
    {
        try {
            IInstallOptionsEditorInput input = (IInstallOptionsEditorInput)getEditorInput();
            if(input != null) {
                IDocumentProvider provider = input.getDocumentProvider();
                if(provider != null) {
                    performRevert(input, provider);
                }
            }
        }
        catch (Exception e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
    }

    protected void performRevert(IInstallOptionsEditorInput input, IDocumentProvider provider)
    {
        if (provider == null) {
            return;
        }

        try {
            mEditorSaving = true;
            provider.aboutToChange(getEditorInput());
            provider.resetDocument(getEditorInput());
            editorSaved(input);
        }
        catch (CoreException x) {
            IStatus status= x.getStatus();
            if (status == null || status.getSeverity() != IStatus.CANCEL ) {
                Shell shell= getSite().getShell();
                String title= InstallOptionsPlugin.getResourceString("error.revert.title"); //$NON-NLS-1$
                String msg= InstallOptionsPlugin.getResourceString("error.revert.message"); //$NON-NLS-1$
                ErrorDialog.openError(shell, title, msg, x.getStatus());
            }
        }
        finally {
            provider.changed(getEditorInput());
            mEditorSaving = false;
            loadInstallOptionsDialog();
            getCommandStack().flush();
        }
    }

    protected void performSave(IInstallOptionsEditorInput input, IDocumentProvider provider, boolean overwrite, IProgressMonitor progressMonitor)
    {
        if (provider == null) {
            return;
        }

        try {
            mEditorSaving = true;
            provider.aboutToChange(input);
            provider.saveDocument(progressMonitor, input, provider.getDocument(input), overwrite);
            editorSaved(input);
            getCommandStack().markSaveLocation();
        }
        catch (CoreException x)
        {
            IStatus status= x.getStatus();
            if (status == null || status.getSeverity() != IStatus.CANCEL) {
                handleExceptionOnSave(input, provider, x, progressMonitor);
            }
        }
        finally {
            provider.changed(input);
            mEditorSaving = false;
        }
    }

    private void updateDocument()
    {
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(dialog != null && dialog.canUpdateINIFile() && getCommandStack().isDirty()) {
            dialog.updateINIFile();
            mINIFile.updateDocument();
        }
    }

    @Override
    public void doSaveAs()
    {
        performSaveAs();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class type)
    {
        if(type == getClass()) {
            return this;
        }
        if (type == InstallOptionsDialog.class) {
            return mInstallOptionsDialog;
        }
        if (type == IContentOutlinePage.class) {
            mOutlinePage = new OutlinePage(new CustomTreeViewer());
            return mOutlinePage;
        }
        if (type == PalettePage.class) {
            if (mPalette == null) {
                mPalettePage = createPalettePage();
                return mPalettePage;
            }
            return createPalettePage();
        }
        if (type == org.eclipse.ui.views.properties.IPropertySheetPage.class) {
            return new CustomTabbedPropertySheetPage(this);
        }
        if (type == EditDomain.class) {
            return getEditDomain();
        }
        if (type == GraphicalViewer.class) {
            return getGraphicalViewer();
        }
        if (type == CommandStack.class) {
            return getCommandStack();
        }
        if (type == ActionRegistry.class) {
            return getActionRegistry();
        }
        if (type == EditPart.class && getGraphicalViewer() != null) {
            return getGraphicalViewer().getRootEditPart();
        }
        if (type == IFigure.class && getGraphicalViewer() != null) {
            return ((GraphicalEditPart)getGraphicalViewer().getRootEditPart()).getFigure();
        }
        return super.getAdapter(type);
    }

    protected Control getGraphicalControl()
    {
        return mRulerComposite;
    }

    /**
     * Returns the KeyHandler with common bindings for both the Outline and
     * Graphical Views. For example, delete is a common action.
     */
    protected KeyHandler getCommonKeyHandler()
    {
        if (mSharedKeyHandler == null) {
            mSharedKeyHandler = new KeyHandler();
            mSharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
                            getActionRegistry().getAction(ActionFactory.DELETE.getId()));
            mSharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
                            getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
        }
        return mSharedKeyHandler;
    }

    private InstallOptionsDialog getInstallOptionsDialog()
    {
        return mInstallOptionsDialog;
    }

    protected FlyoutPreferences getPalettePreferences()
    {
        return new FlyoutPreferences() {
            public int getDockLocation()
            {
                return cPreferenceStore.getInt(PALETTE_DOCK_LOCATION);
            }

            public int getPaletteState()
            {
                return cPreferenceStore.getInt(PALETTE_STATE);
            }

            public int getPaletteWidth()
            {
                return cPreferenceStore.getInt(PALETTE_SIZE);
            }

            public void setDockLocation(int location)
            {
                cPreferenceStore.setValue(PALETTE_DOCK_LOCATION, location);
            }

            public void setPaletteState(int state)
            {
                cPreferenceStore.setValue(PALETTE_STATE, state);
            }

            public void setPaletteWidth(int width)
            {
                cPreferenceStore.setValue(PALETTE_SIZE, width);
            }
        };
    }

    protected PaletteRoot getPaletteRoot()
    {
        if (mRoot == null) {
            mRoot = InstallOptionsPaletteProvider.createPalette(getGraphicalViewer());
        }
        return mRoot;
    }

    public void gotoMarker(IMarker marker)
    {
    }


    protected void initializeGraphicalViewer()
    {
        mPalette.hookDropTargetListener(getGraphicalViewer());
        getGraphicalViewer().setContents(getInstallOptionsDialog());
        getGraphicalViewer().addDropTargetListener((TransferDropTargetListener)new InstallOptionsTemplateTransferDropTargetListener(getGraphicalViewer()));
    }

    protected void createActions()
    {
        ActionRegistry registry = getActionRegistry();
        IAction action;

        action = new PreviewAction(PREVIEW_CLASSIC, this);
        registry.registerAction(action);

        action = new PreviewAction(PREVIEW_MUI, this);
        registry.registerAction(action);

        action = new RefreshDiagramAction(this);
        registry.registerAction(action);

        action = new ToggleDialogSizeVisibilityAction(this);
        registry.registerAction(action);

        action = new ToggleEnablementAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new ToggleLockAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new Action(InstallOptionsPlugin.getResourceString("grid.snap.glue.action.name")) { //$NON-NLS-1$
            @Override
            public void run()
            {
                if(getEditorInput() != null) {
                    new GridSnapGlueSettingsDialog(getSite().getShell(),getGraphicalViewer()).open();
                }
            }
        };
        action.setId(IInstallOptionsConstants.GRID_SNAP_GLUE_SETTINGS_ACTION_ID);
        action.setToolTipText(InstallOptionsPlugin.getResourceString("grid.snap.glue.action.tooltip")); //$NON-NLS-1$
        registry.registerAction(action);

        action = new UndoAction(this);
        registry.registerAction(action);
        getStackActions().add(action.getId());

        action = new RedoAction(this);
        registry.registerAction(action);
        getStackActions().add(action.getId());

        action = new SelectAllAction(this);
        registry.registerAction(action);

        action = new DeleteAction((IWorkbenchPart)this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new SaveAction(this);
        registry.registerAction(action);
        getPropertyActions().add(action.getId());

        action = new RevertToSavedAction(this);
        registry.registerAction(action);
        getPropertyActions().add(action.getId());

        action = new PrintAction(this);
        registry.registerAction(action);

        action = new CutAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new CopyAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new CreateTemplateAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, ARRANGE_SEND_BACKWARD);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, ARRANGE_SEND_TO_BACK);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, ARRANGE_BRING_FORWARD);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new ArrangeAction(this, ARRANGE_BRING_TO_FRONT);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_HORIZONTAL_LEFT_EDGE);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_HORIZONTAL_RIGHT_EDGE);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_HORIZONTAL_CENTER);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_HORIZONTAL_BETWEEN);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_VERTICAL_TOP_EDGE);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_VERTICAL_BOTTOM_EDGE);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_VERTICAL_CENTER);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new DistributeAction(this, DISTRIBUTE_VERTICAL_BETWEEN);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new PasteAction(this);
        getSelectionActions().add(action.getId());
        registry.registerAction(action);

        action = new MatchWidthAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new MatchHeightAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new MatchSizeAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new DirectEditAction((IWorkbenchPart)this);
        String label = InstallOptionsPlugin.getResourceString("direct.edit.label"); //$NON-NLS-1$
        action.setText(label);
        action.setToolTipText(label);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new ExtendedEditAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,
                        PositionConstants.LEFT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.RIGHT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.TOP);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.BOTTOM);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.CENTER);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart)this,PositionConstants.MIDDLE);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new SwitchEditorAction(this, INSTALLOPTIONS_SOURCE_EDITOR_ID,InstallOptionsPlugin.getResourceString("switch.source.editor.action.name")); //$NON-NLS-1$
        registry.registerAction(action);
        ((IHandlerService)getEditorSite().getService(IHandlerService.class)).activateHandler(action.getActionDefinitionId(),new ActionHandler(action));

        final Shell shell;
        if (getGraphicalViewer() != null) {
            shell= getGraphicalViewer().getControl().getShell();
        }
        else {
            shell= null;
        }
        action = new Action(){
            @Override
            public void run() {
                String[] preferencePages= {IInstallOptionsConstants.INSTALLOPTIONS_PREFERENCE_PAGE_ID};
                if (preferencePages.length > 0 && (shell == null || !shell.isDisposed())) {
                    PreferencesUtil.createPreferenceDialogOn(shell, preferencePages[0], preferencePages, InstallOptionsDesignEditor.class).open();
                }
            }
        };
        action.setId("net.sf.eclipsensis.installoptions.design_editor_prefs"); //$NON-NLS-1$
        action.setText(InstallOptionsPlugin.getResourceString("preferences.action.name")); //$NON-NLS-1$
        action.setToolTipText(InstallOptionsPlugin.getResourceString("preferences.action.tooltip")); //$NON-NLS-1$
        registry.registerAction(action);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse.swt.widgets.Composite)
     */
    protected void createGraphicalViewer(Composite parent)
    {
        mRulerComposite = new InstallOptionsRulerComposite(parent, SWT.NONE);
        GraphicalViewer viewer = new InstallOptionsGraphicalViewer(mInstallOptionsDialog);
        viewer.createControl(mRulerComposite);
        setGraphicalViewer(viewer);
        configureGraphicalViewer();
        hookGraphicalViewer();
        initializeGraphicalViewer();
        mRulerComposite.setGraphicalViewer((InstallOptionsGraphicalViewer)getGraphicalViewer());
        getGraphicalViewer().getControl().setBackground(getGraphicalViewer().getControl().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    protected FigureCanvas getEditor()
    {
        return (FigureCanvas)getGraphicalViewer().getControl();
    }

    @Override
    public boolean isDirty()
    {
        return isSaveOnCloseNeeded();
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    @Override
    public boolean isSaveOnCloseNeeded()
    {
        IInstallOptionsEditorInput input = (IInstallOptionsEditorInput)getEditorInput();
        return input != null && input.getDocumentProvider().canSaveDocument(input) || getCommandStack().isDirty();
    }

    private <T> T loadPreference(String name, TypeConverter<T> converter, T defaultValue)
    {
        T o = null;
        try {
            o = converter.asType(cPreferenceStore.getString(name));
        }
        catch(Exception ex) {
            o = null;
        }
        if(o == null) {
            o = converter.makeCopy(defaultValue);
        }
        return o;
    }

    private <T> T loadFileProperty(IFile file, QualifiedName name, TypeConverter<T> converter, T defaultValue)
    {
        T defaultValue2 = loadPreference(name.getLocalName(),converter, defaultValue);
        T o = null;
        if(file != null) {
            try {
                o = converter.asType(file.getPersistentProperty(name), defaultValue2);
            }
            catch (Exception e) {
                o = null;
            }
        }
        if(o == null) {
            o = defaultValue2;
        }
        return o;
    }

    protected void loadProperties(IFile file)
    {
        // Ruler properties
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(dialog != null) {
            GraphicalViewer viewer = getGraphicalViewer();

            if(viewer != null) {
                InstallOptionsRuler ruler = dialog.getRuler(PositionConstants.WEST);
                RulerProvider provider = null;
                if (ruler != null) {
                    provider = new InstallOptionsRulerProvider(ruler);
                }
                viewer.setProperty(RulerProvider.PROPERTY_VERTICAL_RULER,provider);

                ruler = dialog.getRuler(PositionConstants.NORTH);
                provider = null;
                if (ruler != null) {
                    provider = new InstallOptionsRulerProvider(ruler);
                }
                viewer.setProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER, provider);

                viewer.setProperty(RulerProvider.PROPERTY_RULER_VISIBILITY,
                                loadFileProperty(file, FILEPROPERTY_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                                                SHOW_RULERS_DEFAULT));

                // Snap to Geometry property
                viewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED,
                                loadFileProperty(file, FILEPROPERTY_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                                                SNAP_TO_GEOMETRY_DEFAULT));

                // Grid properties
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED,
                                loadFileProperty(file, FILEPROPERTY_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                                                SNAP_TO_GRID_DEFAULT));
                viewer.setProperty(InstallOptionsGridLayer.PROPERTY_GRID_STYLE,
                                loadFileProperty(file, FILEPROPERTY_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                                                GRID_STYLE_DEFAULT));
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_ORIGIN,
                                loadFileProperty(file, FILEPROPERTY_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                                                GRID_ORIGIN_DEFAULT));
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING,
                                loadFileProperty(file, FILEPROPERTY_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                                                GRID_SPACING_DEFAULT));
                viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE,
                                loadFileProperty(file, FILEPROPERTY_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                                                SHOW_GRID_DEFAULT));

                // Guides properties
                viewer.setProperty(PROPERTY_SNAP_TO_GUIDES,
                                loadFileProperty(file, FILEPROPERTY_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                                                SNAP_TO_GUIDES_DEFAULT));
                viewer.setProperty(PROPERTY_GLUE_TO_GUIDES,
                                loadFileProperty(file, FILEPROPERTY_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                                                GLUE_TO_GUIDES_DEFAULT));
                viewer.setProperty(ToggleGuideVisibilityAction.PROPERTY_GUIDE_VISIBILITY,
                                loadFileProperty(file, FILEPROPERTY_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                                                SHOW_GUIDES_DEFAULT));
            }

            DialogSize dialogSize = null;
            String dialogSizeName = loadFileProperty(file, FILEPROPERTY_DIALOG_SIZE_NAME, TypeConverter.STRING_CONVERTER, null);
            if(dialogSizeName == null) {
                Dimension dim = loadFileProperty(file, FILEPROPERTY_DIALOG_SIZE,TypeConverter.DIMENSION_CONVERTER,null);
                if(dim != null) {
                    dialogSize = DialogSizeManager.getDialogSize(dim);
                }
            }
            if(dialogSize == null) {
                if(dialogSizeName != null) {
                    dialogSize = DialogSizeManager.getDialogSize(dialogSizeName);
                }
                if(dialogSize == null) {
                    dialogSize = DialogSizeManager.getDefaultDialogSize();
                }
            }
            dialog.setDialogSize(dialogSize);
            dialog.setShowDialogSize(loadFileProperty(file, FILEPROPERTY_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                            SHOW_DIALOG_SIZE_DEFAULT).booleanValue());
        }
    }

    protected boolean performSaveAs()
    {
        Shell shell = getSite().getWorkbenchWindow().getShell();
        SaveAsDialog dialog = new SaveAsDialog(shell);
        IInstallOptionsEditorInput input = (IInstallOptionsEditorInput)getEditorInput();
        IFile original = input instanceof IFileEditorInput?((IFileEditorInput)input).getFile():null;
        if(original != null) {
            dialog.setOriginalFile(original);
        }
        else {
            dialog.setOriginalName(input.getPath().lastSegment());
        }
        dialog.create();

        IDocumentProvider provider= input.getDocumentProvider();
        if (provider == null) {
            // editor has programmatically been  closed while the dialog was open
            return false;
        }

        if (provider.isDeleted(input) && original != null) {
            String message= InstallOptionsPlugin.getFormattedString("warning.save.delete", new Object[] { original.getName() }); //$NON-NLS-1$
            dialog.setErrorMessage(null);
            dialog.setMessage(message, IMessageProvider.WARNING);
        }

        if (dialog.open() == Window.CANCEL) {
            return false;
        }
        IPath path = dialog.getResult();
        if (path == null) {
            return false;
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IFile file = workspace.getRoot().getFile(path);
        final IEditorInput newInput= new FileEditorInput(file);

        boolean success= false;
        if(!file.exists()) {
            try {
                updateDocument();
                provider.aboutToChange(newInput);
                provider.saveDocument(new NullProgressMonitor(), newInput, provider.getDocument(input), true);
                saveProperties(file);
                success= true;
            }
            catch (CoreException x) {
                IStatus status= x.getStatus();
                if (status == null || status.getSeverity() != IStatus.CANCEL) {
                    String title= InstallOptionsPlugin.getResourceString("error.saveas.title"); //$NON-NLS-1$
                    String msg= InstallOptionsPlugin.getFormattedString("error.save.message", new Object[] { x.getMessage() }); //$NON-NLS-1$

                    if (status != null) {
                        switch (status.getSeverity()) {
                            case IStatus.INFO:
                                Common.openInformation(shell, title, msg, InstallOptionsPlugin.getShellImage());
                                break;
                            case IStatus.WARNING:
                                Common.openWarning(shell, title, msg, InstallOptionsPlugin.getShellImage());
                                break;
                            default:
                                Common.openError(shell, title, msg, InstallOptionsPlugin.getShellImage());
                        }
                    } else {
                        Common.openError(shell, title, msg, InstallOptionsPlugin.getShellImage());
                    }
                }
            } finally {
                provider.changed(newInput);
            }

            if(success) {
                try {
                    superSetInput(createInput(file));
                    getCommandStack().markSaveLocation();
                }
                catch (Exception e) {
                    InstallOptionsPlugin.getDefault().log(e);
                }
            }
        }
        return true;
    }

    /**
     * @param file
     * @return
     */
    private IEditorInput createInput(IFile file)
    {
        return new FileEditorInput(file);
    }

    private boolean savePreviouslyNeeded()
    {
        return mSavePreviouslyNeeded;
    }

    private <T> void saveFileProperty(IFile file, QualifiedName name, TypeConverter<T> converter, T value, T defaultValue)
    {
        try {
            file.setPersistentProperty(name, converter.asString(value, defaultValue));
        }
        catch (Exception e) {
            InstallOptionsPlugin.getDefault().log(e);
        }
    }

    protected void saveProperties(IFile file)
    {
        InstallOptionsDialog dialog = getInstallOptionsDialog();
        if(dialog != null && file.exists()) {
            GraphicalViewer viewer = getGraphicalViewer();
            saveFileProperty(file, FILEPROPERTY_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY),
                            SHOW_RULERS_DEFAULT);

            // Snap to Geometry property
            saveFileProperty(file, FILEPROPERTY_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED),
                            SNAP_TO_GEOMETRY_DEFAULT);

            // Grid properties
            saveFileProperty(file, FILEPROPERTY_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(SnapToGrid.PROPERTY_GRID_ENABLED),
                            SNAP_TO_GRID_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                            (String)viewer.getProperty(InstallOptionsGridLayer.PROPERTY_GRID_STYLE),
                            GRID_STYLE_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                            (org.eclipse.draw2d.geometry.Point)viewer.getProperty(SnapToGrid.PROPERTY_GRID_ORIGIN),
                            GRID_ORIGIN_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                            (Dimension)viewer.getProperty(SnapToGrid.PROPERTY_GRID_SPACING),
                            GRID_SPACING_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE),
                            SHOW_GRID_DEFAULT);

            // Guides properties
            saveFileProperty(file, FILEPROPERTY_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(PROPERTY_SNAP_TO_GUIDES),
                            SNAP_TO_GUIDES_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(PROPERTY_GLUE_TO_GUIDES),
                            GLUE_TO_GUIDES_DEFAULT);
            saveFileProperty(file, FILEPROPERTY_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                            (Boolean)viewer.getProperty(ToggleGuideVisibilityAction.PROPERTY_GUIDE_VISIBILITY),
                            SHOW_GUIDES_DEFAULT);

            saveFileProperty(file, FILEPROPERTY_DIALOG_SIZE_NAME,TypeConverter.STRING_CONVERTER,
                            dialog.getDialogSize().getName(),DEFAULT_DIALOG_SIZE.getName());
            saveFileProperty(file, FILEPROPERTY_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                            Boolean.valueOf(dialog.isShowDialogSize()),
                            SHOW_DIALOG_SIZE_DEFAULT);
        }
    }

    @Override
    public void setInput(IEditorInput input)
    {
        superSetInput(input);
        IEditorInput input2 = getEditorInput();
        if(input2 != null) {
            IDocument document = ((IInstallOptionsEditorInput)input2).getDocumentProvider().getDocument(input2);
            mINIFile.connect(document);
            loadInstallOptionsDialog();
        }
    }

    private void loadInstallOptionsDialog()
    {
        InstallOptionsDialog dialog;
        if(!mINIFile.hasErrors()) {
            dialog = InstallOptionsDialog.loadINIFile(mINIFile);
        }
        else {
            dialog = new InstallOptionsDialog(null);
        }

        setInstallOptionsDialog(dialog);
        if (!mEditorSaving) {
            IFile file = null;
            Object source = ((IInstallOptionsEditorInput)getEditorInput()).getSource();
            if(source instanceof IFile) {
                file = (IFile)source;
            }
            loadProperties(file);
            if (getGraphicalViewer() != null) {
                getGraphicalViewer().setContents(dialog);
            }
            if (mOutlinePage != null) {
                mOutlinePage.setContents(dialog);
            }
        }
    }

    private void setInstallOptionsDialog(InstallOptionsDialog dialog)
    {
        mInstallOptionsDialog = dialog;
    }

    private void setSavePreviouslyNeeded(boolean value)
    {
        mSavePreviouslyNeeded = value;
    }

    protected void superSetInput(IEditorInput input)
    {
        IEditorInput input2 = input;
        IEditorInput oldInput = getEditorInput();
        if (oldInput != null) {
            ((InstallOptionsEditDomain)getEditDomain()).setFile((IFile)null);
            Object source = ((IInstallOptionsEditorInput)oldInput).getSource();
            if(source instanceof IFile) {
                IFile file = (IFile)source;
                file.getWorkspace().removeResourceChangeListener(mResourceListener);
            }
            ((IInstallOptionsEditorInput)oldInput).getDocumentProvider().disconnect(oldInput);
        }
        if(input2 != null) {
            try {
                if(!(input2 instanceof IInstallOptionsEditorInput)) {
                    if(input2 instanceof IFileEditorInput) {
                        IFile file = ((IFileEditorInput)input2).getFile();
                        InstallOptionsNature.addNature(file.getProject());
                        input2 = new InstallOptionsEditorInput((IFileEditorInput)input2);
                    }
                    else if (input2 instanceof IPathEditorInput){
                        input2 = new InstallOptionsExternalFileEditorInput((IPathEditorInput)input2);
                    }
                    else {
                        input2 = new InstallOptionsExternalFileEditorInput((IPathEditorInput)input2.getAdapter(IPathEditorInput.class));
                    }
                    ((IInstallOptionsEditorInput)input2).getDocumentProvider().connect(input2);
                }
                else {
                    IInstallOptionsEditorInput input3 = (IInstallOptionsEditorInput)input2;
                    input3.getDocumentProvider().connect(input3);
                    input3.completedSwitch();
                }
            }
            catch (CoreException e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }

        super.setInput(input2);

        input2 = getEditorInput();
        if (input2 != null) {
            Object source = ((IInstallOptionsEditorInput)input2).getSource();
            if(source instanceof IFile) {
                ((IFile)source).getWorkspace().addResourceChangeListener(mResourceListener);
                ((InstallOptionsEditDomain)getEditDomain()).setFile((IFile)source);
                setPartName(((IFile)source).getName());
            }
            else if(source instanceof IPath) {
                File file = new File(((IPath)source).toOSString());
                ((InstallOptionsEditDomain)getEditDomain()).setFile(file);
                setPartName(file.getName());
            }
        }
    }

    @Override
    protected void setSite(IWorkbenchPartSite site)
    {
        super.setSite(site);
        getSite().getWorkbenchWindow().getPartService().addPartListener(mPartListener);
    }

    private boolean isSwitching()
    {
        return mSwitching;
    }

    public boolean canSwitch()
    {
        return true;
    }

    public INIFile getINIFile()
    {
        return mINIFile;
    }

    public void prepareForSwitch()
    {
        if(!mSwitching) {
            mSwitching = true;
            ((IInstallOptionsEditorInput)getEditorInput()).prepareForSwitch();
        }
    }

    private void showPropertiesView()
    {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                try {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if(activePage != null) {
                        IViewPart view = activePage.findView(IPageLayout.ID_PROP_SHEET);
                        if(view == null) {
                            activePage.showView(IPageLayout.ID_PROP_SHEET, null, IWorkbenchPage.VIEW_VISIBLE);
                        }
                        else {
                            activePage.bringToTop(view);
                        }
                    }
                }
                catch(PartInitException pie) {
                    InstallOptionsPlugin.getDefault().log(pie);
                }
            }
        });
    }

    private void checkPerformSwitch()
    {
        if(mINIFile.hasErrors() && !isSwitching()) {
            prepareForSwitch();
            getSite().getShell().getDisplay().asyncExec(new Runnable(){
                public void run()
                {
                    String name = null;
                    Object source = ((IInstallOptionsEditorInput)getEditorInput()).getSource();
                    if(source instanceof IFile) {
                        name = ((IFile)source).getName();
                    }
                    else if(source instanceof IPath) {
                        name = ((IPath)source).lastSegment();
                    }
                    Common.openError(getSite().getShell(),EclipseNSISPlugin.getResourceString("error.title"), //$NON-NLS-1$
                                    InstallOptionsPlugin.getFormattedString("editor.switch.error", //$NON-NLS-1$
                                                    new String[]{name}),
                                                    InstallOptionsPlugin.getShellImage());
                    getActionRegistry().getAction(SwitchEditorAction.ID).run();
                }
            });
        }
    }

    protected class CustomPalettePage extends PaletteViewerPage
    {
        /**
         * Constructor
         * @param provider  the provider used to create a PaletteViewer
         */
        public CustomPalettePage(PaletteViewerProvider provider)
        {
            super(provider);
        }
        /**
         * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
         */
        @Override
        public void createControl(Composite parent)
        {
            super.createControl(parent);
            if (mPalette != null) {
                mPalette.setExternalViewer(viewer);
            }
        }
        /**
         * @see org.eclipse.ui.part.IPage#dispose()
         */
        @Override
        public void dispose()
        {
            if (mPalette != null) {
                mPalette.setExternalViewer(null);
            }
            super.dispose();
        }
        /**
         * @return  the PaletteViewer created and displayed by this page
         */
        public PaletteViewer getPaletteViewer()
        {
            return viewer;
        }
    }

    private class OutlinePage extends ContentOutlinePage
    {
        private PageBook mPageBook;

        private Control mOutline;

        private Canvas mOverview;

        private IAction mShowOutlineAction, mShowOverviewAction;

        static final int ID_OUTLINE = 0;

        static final int ID_OVERVIEW = 1;

        private Thumbnail mThumbnail;

        private DisposeListener mDisposeListener;

        public OutlinePage(final EditPartViewer viewer)
        {
            super(viewer);
        }

        @Override
        public void init(IPageSite pageSite)
        {
            super.init(pageSite);
            ActionRegistry registry = getActionRegistry();
            IActionBars bars = pageSite.getActionBars();
            String id = ActionFactory.UNDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.REDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.CUT.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.COPY.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.PASTE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.DELETE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            bars.updateActionBars();
        }

        protected void configureOutlineViewer()
        {
            getViewer().setEditDomain(getEditDomain());
            getViewer().setEditPartFactory(TreePartFactory.INSTANCE);
            ContextMenuProvider provider = new InstallOptionsDesignMenuProvider(getViewer(),
                            getActionRegistry());
            getViewer().setContextMenu(provider);
            getSite().registerContextMenu(
                            "net.sf.eclipsensis.installoptions.editor.outline.contextmenu", //$NON-NLS-1$
                            provider, getSite().getSelectionProvider());
            getViewer().setKeyHandler(getCommonKeyHandler());
            IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
            mShowOutlineAction = new Action() {
                @Override
                public void run()
                {
                    showPage(ID_OUTLINE);
                }
            };
            mShowOutlineAction.setDescription(InstallOptionsPlugin.getResourceString("show.outline.action.description")); //$NON-NLS-1$
            mShowOutlineAction.setToolTipText(InstallOptionsPlugin.getResourceString("show.outline.action.tooltip")); //$NON-NLS-1$
            mShowOutlineAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("outline.icon"))); //$NON-NLS-1$
            tbm.add(mShowOutlineAction);
            mShowOverviewAction = new Action() {
                @Override
                public void run()
                {
                    showPage(ID_OVERVIEW);
                }
            };
            mShowOverviewAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("overview.icon"))); //$NON-NLS-1$
            mShowOverviewAction.setDescription(InstallOptionsPlugin.getResourceString("show.overview.action.description")); //$NON-NLS-1$
            mShowOverviewAction.setToolTipText(InstallOptionsPlugin.getResourceString("show.overview.action.tooltip")); //$NON-NLS-1$
            tbm.add(mShowOverviewAction);
            showPage(ID_OUTLINE);
        }

        @Override
        public void createControl(Composite parent)
        {
            mPageBook = new PageBook(parent, SWT.NONE);
            mOutline = getViewer().createControl(mPageBook);
            IHandle handle = Common.getControlHandle(mOutline);
            if(mOutline instanceof Tree) {
                WinAPI.INSTANCE.setWindowLong(handle, WinAPI.GWL_STYLE, WinAPI.INSTANCE.getWindowLong(handle, WinAPI.GWL_STYLE) ^ (WinAPI.TVS_HASLINES  | WinAPI.TVS_HASBUTTONS));
            }
            mOverview = new Canvas(mPageBook, SWT.NONE);
            mPageBook.showPage(mOutline);
            configureOutlineViewer();
            hookOutlineViewer();
            initializeOutlineViewer();
            PlatformUI.getWorkbench().getHelpSystem().setHelp(mPageBook,IInstallOptionsConstants.PLUGIN_CONTEXT_PREFIX + "installoptions_designoutline_context"); //$NON-NLS-1$
        }

        @Override
        public void dispose()
        {
            unhookOutlineViewer();
            if (mThumbnail != null) {
                mThumbnail.deactivate();
                mThumbnail = null;
            }
            super.dispose();
            InstallOptionsDesignEditor.this.mOutlinePage = null;
        }

        @Override
        public Control getControl()
        {
            return mPageBook;
        }

        protected void hookOutlineViewer()
        {
            getSelectionSynchronizer().addViewer(getViewer());
        }

        protected void initializeOutlineViewer()
        {
            setContents(getInstallOptionsDialog());
        }

        protected void initializeOverview()
        {
            LightweightSystem lws = new LightweightSystem(mOverview);
            RootEditPart rep = getGraphicalViewer().getRootEditPart();
            if (rep instanceof ScalableFreeformRootEditPart) {
                ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart)rep;
                mThumbnail = new ScrollableThumbnail((Viewport)root.getFigure());
                mThumbnail.setBorder(new MarginBorder(3));
                mThumbnail.setSource(root
                                .getLayer(LayerConstants.PRINTABLE_LAYERS));
                lws.setContents(mThumbnail);
                mDisposeListener = new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        if (mThumbnail != null) {
                            mThumbnail.deactivate();
                            mThumbnail = null;
                        }
                    }
                };
                getEditor().addDisposeListener(mDisposeListener);
            }
        }

        public void setContents(Object contents)
        {
            getViewer().setContents(contents);
        }

        protected void showPage(int id)
        {
            if (id == ID_OUTLINE) {
                mShowOutlineAction.setChecked(true);
                mShowOverviewAction.setChecked(false);
                mPageBook.showPage(mOutline);
                if (mThumbnail != null) {
                    mThumbnail.setVisible(false);
                }
            }
            else if (id == ID_OVERVIEW) {
                if (mThumbnail == null) {
                    initializeOverview();
                }
                mShowOutlineAction.setChecked(false);
                mShowOverviewAction.setChecked(true);
                mPageBook.showPage(mOverview);
                mThumbnail.setVisible(true);
            }
        }

        protected void unhookOutlineViewer()
        {
            getSelectionSynchronizer().removeViewer(getViewer());
            if (mDisposeListener != null && getEditor() != null && !getEditor().isDisposed()) {
                getEditor().removeDisposeListener(mDisposeListener);
            }
        }
    }

    // This class listens to changes to the file system in the workspace, and
    // makes changes accordingly.
    // 1) An open, saved file gets deleted -> close the editor
    // 2) An open file gets renamed or moved -> change the editor's input
    // accordingly
    private class ResourceTracker implements IResourceChangeListener, IResourceDeltaVisitor
    {
        public void resourceChanged(IResourceChangeEvent event)
        {
            IResourceDelta delta = event.getDelta();
            try {
                if (delta != null) {
                    delta.accept(this);
                }
            }
            catch (CoreException exception) {
                // What should be done here?
            }
        }

        public boolean visit(IResourceDelta delta)
        {
            if (delta == null
                            || !delta.getResource().equals(
                                            ((IFileEditorInput)getEditorInput()).getFile())) {
                return true;
            }

            if (delta.getKind() == IResourceDelta.REMOVED) {
                Display display = getSite().getShell().getDisplay();
                if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) {
                    // If the file was deleted
                    // NOTE: The case where an open, unsaved file is deleted is
                    // being handled by the
                    // PartListener added to the Workbench in the initialize()
                    // method.
                    display.asyncExec(new Runnable() {
                        public void run()
                        {
                            if (!isDirty()) {
                                closeEditor(false);
                            }
                        }
                    });
                }
                else { // else if it was moved or renamed
                    final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getMovedToPath());
                    display.asyncExec(new Runnable() {
                        public void run()
                        {
                            superSetInput(createInput(newFile));
                        }
                    });
                }
            }
            else if (delta.getKind() == IResourceDelta.CHANGED) {
                if (!mEditorSaving) {
                    if(Common.isEmptyArray(delta.getMarkerDeltas())) {
                        // the file was overwritten somehow (could have been
                        // replaced by another
                        // version in the respository)
                        final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getFullPath());
                        Display display = getSite().getShell().getDisplay();
                        display.asyncExec(new Runnable() {
                            public void run()
                            {
                                setInput(createInput(newFile));
                                getCommandStack().flush();
                            }
                        });
                    }
                }
            }
            return false;
        }
    }

    private class CustomTreeViewer extends TreeViewer
    {
        public CustomTreeViewer()
        {
            super();
            addDragSourceListener(new InstallOptionsTreeViewerDragSourceListener(this));
            addDropTargetListener(new InstallOptionsTreeViewerDropTargetListener(this));
        }

        @Override
        public void addDragSourceListener(TransferDragSourceListener listener)
        {
            if(listener instanceof InstallOptionsTreeViewerDragSourceListener) {
                super.addDragSourceListener(listener);
            }
        }

        @Override
        public void addDropTargetListener(TransferDropTargetListener listener)
        {
            if(listener instanceof InstallOptionsTreeViewerDropTargetListener) {
                super.addDropTargetListener(listener);
            }
        }
    }

    private class CustomPaletteContextMenuProvider extends PaletteContextMenuProvider
    {
        public CustomPaletteContextMenuProvider(PaletteViewer palette)
        {
            super(palette);
        }

        @Override
        public void buildContextMenu(IMenuManager menu)
        {
            super.buildContextMenu(menu);
            IContributionItem[] items = menu.getItems();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(items[i] instanceof ActionContributionItem) {
                        IAction action = ((ActionContributionItem)items[i]).getAction();
                        if(action.getClass().equals(SettingsAction.class)) {
                            menu.remove(items[i]);
                            break;
                        }
                    }
                }
            }
            menu.appendToGroup(GEFActionConstants.GROUP_REST, new PaletteSettingsAction(getPaletteViewer()));

            EditPart selectedPart = (EditPart)getPaletteViewer().getSelectedEditParts().get(0);
            Object model = selectedPart.getModel();
            if (model instanceof CombinedTemplateCreationEntry) {
                final Object template = ((CombinedTemplateCreationEntry)model).getTemplate();
                if(template instanceof IInstallOptionsTemplate) {
                    menu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new EditTemplateAction((IInstallOptionsTemplate)template));
                    menu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new DeleteTemplateAction((IInstallOptionsTemplate)template));
                }
            }
        }

    }

    private class PaletteSettingsAction extends SettingsAction
    {
        private PaletteViewer mPaletteViewer;

        public PaletteSettingsAction(PaletteViewer palette)
        {
            super(palette);
            mPaletteViewer = palette;
        }

        @Override
        public void run()
        {
            Dialog settings = new CustomPaletteSettingsDialog(mPaletteViewer.getControl().getShell(),
                            mPaletteViewer.getPaletteViewerPreferences());
            settings.open();
        }
    }

    private class PaletteViewerPreferences extends DefaultPaletteViewerPreferences
    {
        public PaletteViewerPreferences(IPreferenceStore store)
        {
            super(store);
            getPreferenceStore().setDefault(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED, true);
        }

        public void setUnloadCreationToolWhenFinished(boolean value)
        {
            getPreferenceStore().setValue(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED, value);
        }

        public boolean getUnloadCreationToolWhenFinished()
        {
            return getPreferenceStore().getBoolean(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED);
        }

        @Override
        protected void handlePreferenceStorePropertyChanged(String property)
        {
            if(property.equals(PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)) {
                firePropertyChanged(property,
                                (getUnloadCreationToolWhenFinished()?Boolean.TRUE:Boolean.FALSE));
            }
            else {
                super.handlePreferenceStorePropertyChanged(property);
            }
        }
    }

    private class CustomPaletteSettingsDialog extends PaletteSettingsDialog
    {
        protected static final String CACHE_UNLOAD_CREATION_TOOL_WHEN_FINISHED = "unload creation tool when finished"; //$NON-NLS-1$
        protected static final int UNLOAD_CREATION_TOOL_WHEN_FINISHED_ID = CLIENT_ID + 1;

        private org.eclipse.gef.ui.palette.PaletteViewerPreferences mPrefs;
        private AbstractTemplateSettings<IInstallOptionsTemplate> mTemplateSettings;

        public CustomPaletteSettingsDialog(Shell parentShell, org.eclipse.gef.ui.palette.PaletteViewerPreferences prefs)
        {
            super(parentShell, prefs);
            mPrefs = prefs;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void cacheSettings()
        {
            super.cacheSettings();
            if(mPrefs instanceof PaletteViewerPreferences) {
                settings.put(CACHE_UNLOAD_CREATION_TOOL_WHEN_FINISHED, ((PaletteViewerPreferences)mPrefs).getUnloadCreationToolWhenFinished()?Boolean.TRUE:Boolean.FALSE);
            }
        }

        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getResourceString("settings.dialog.title")); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getShellImage());
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
            layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
            layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            applyDialogFont(composite);

            TabFolder mFolder = new TabFolder(parent, SWT.NONE);
            mFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
            Dialog.applyDialogFont(mFolder);
            TabItem item = new TabItem(mFolder, SWT.NONE);
            item.setText(InstallOptionsPlugin.getResourceString("palette.settings.tab.name")); //$NON-NLS-1$
            item.setControl(createPaletteTab(mFolder));
            item = new TabItem(mFolder, SWT.NONE);
            item.setText(InstallOptionsPlugin.getResourceString("templates.settings.tab.name")); //$NON-NLS-1$
            item.setControl(createTemplatesTab(mFolder));

            return parent;
        }

        private Control createTemplatesTab(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(1,false));

            mTemplateSettings = new AbstractTemplateSettings<IInstallOptionsTemplate>(composite, SWT.NONE, InstallOptionsTemplateManager.INSTANCE) {
                @Override
                protected boolean canAdd()
                {
                    return false;
                }

                @Override
                protected IInstallOptionsTemplate createTemplate(String name)
                {
                    return new InstallOptionsTemplate2(name);
                }

                @Override
                protected Dialog createDialog(final IInstallOptionsTemplate template)
                {
                    InstallOptionsTemplateDialog dialog = new InstallOptionsTemplateDialog(getShell(), template) {
                        @Override
                        protected void okPressed()
                        {
                            createUpdateTemplate();
                            IInstallOptionsTemplate t = getTemplate();
                            if(template != t) {
                                template.setName(t.getName());
                                template.setDescription(t.getDescription());
                                template.setEnabled(t.isEnabled());
                            }
                            setReturnCode(OK);
                            close();
                        }
                    };
                    return dialog;
                }

                @Override
                protected Image getShellImage()
                {
                    return InstallOptionsPlugin.getShellImage();
                }
            };
            mTemplateSettings.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

            Button b = new Button(composite,SWT.PUSH);
            b.setText(InstallOptionsPlugin.getResourceString("restore.defaults.label")); //$NON-NLS-1$
            b.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false, false));
            b.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mTemplateSettings.performDefaults();
                }
            });
            return composite;
        }

        private Control createPaletteTab(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);

            GridLayout layout = (GridLayout)composite.getLayout();

            Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
            GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            data.horizontalSpan = layout.numColumns;
            label.setLayoutData(data);

            Control child = createCreationToolOptions(composite);
            data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            data.horizontalSpan = layout.numColumns;
            data.horizontalIndent = 5;
            child.setLayoutData(data);

            return composite;
        }

        /**
         * @param composite
         * @return
         */
        private Control createCreationToolOptions(Composite composite)
        {
            Composite composite2 = new Composite(composite, SWT.NONE);
            composite2.setLayout(new GridLayout(1,false));

            Label label = new Label(composite2, SWT.NONE);
            label.setText(InstallOptionsPlugin.getResourceString("creation.tools.options")); //$NON-NLS-1$
            GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
            label.setLayoutData(data);

            Button b = createButton(composite2, UNLOAD_CREATION_TOOL_WHEN_FINISHED_ID,
                            InstallOptionsPlugin.getResourceString("unload.creation.tool.when.finished.label"), //$NON-NLS-1$
                            SWT.CHECK,null);
            ((GridData)b.getLayoutData()).horizontalIndent = 5;
            b.setSelection(((PaletteViewerPreferences)mPrefs).getUnloadCreationToolWhenFinished());
            return composite2;
        }

        @Override
        protected void restoreSettings()
        {
            super.restoreSettings();
            if(mPrefs instanceof PaletteViewerPreferences) {
                ((PaletteViewerPreferences)mPrefs).setUnloadCreationToolWhenFinished(((Boolean)settings.get(CACHE_UNLOAD_CREATION_TOOL_WHEN_FINISHED)).booleanValue());
            }
        }

        @Override
        protected void handleCancelPressed()
        {
            if(mTemplateSettings.performCancel()) {
                super.handleCancelPressed();
            }
        }

        @Override
        protected void okPressed()
        {
            if(mTemplateSettings.performOk()) {
                super.okPressed();
            }
        }

        @Override
        protected void buttonPressed(int buttonId)
        {
            if(buttonId == UNLOAD_CREATION_TOOL_WHEN_FINISHED_ID) {
                Button b = getButton(buttonId);
                handleUnloadCreationToolWhenFinishedChanged(b.getSelection());
            }
            else {
                super.buttonPressed(buttonId);
            }
        }

        private void handleUnloadCreationToolWhenFinishedChanged(boolean value)
        {
            if(mPrefs instanceof PaletteViewerPreferences) {
                ((PaletteViewerPreferences)mPrefs).setUnloadCreationToolWhenFinished(value);
            }
        }
    }

}