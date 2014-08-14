/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.wizard;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.*;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.template.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class InstallOptionsWizardPage extends WizardPage
{
    public static final String NAME = "installOptionsWizardPage"; //$NON-NLS-1$

    private static final String[] FILTER_EXTENSIONS = Common.loadArrayProperty(InstallOptionsPlugin.getDefault().getResourceBundle(),"ini.file.extensions"); //$NON-NLS-1$
    private static final String[] FILTER_NAMES = Common.loadArrayProperty(InstallOptionsPlugin.getDefault().getResourceBundle(),"ini.file.names"); //$NON-NLS-1$

    private String[] mEditorIds = {IInstallOptionsConstants.INSTALLOPTIONS_DESIGN_EDITOR_ID,
            IInstallOptionsConstants.INSTALLOPTIONS_SOURCE_EDITOR_ID};
    private boolean mCreateFromTemplate = false;
    private Button  mOpenFileCheckbox;
    private Combo mEditorIdCombo;

    private Button[] mSaveLocationTypes;
    private boolean mCheckOverwrite = false;
    private Text mSaveLocation;

    /**
     * Creates the page for the readme creation wizard.
     *
     * @param workbench  the workbench on which the page should be created
     * @param selection  the current selection
     */
    public InstallOptionsWizardPage()
    {
        super(NAME);
        this.setTitle(InstallOptionsPlugin.getResourceString("wizard.page.title")); //$NON-NLS-1$
        this.setDescription(InstallOptionsPlugin.getResourceString("wizard.page.description")); //$NON-NLS-1$
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),IInstallOptionsConstants.PLUGIN_CONTEXT_PREFIX+"installoptions_wizard_context"); //$NON-NLS-1$

        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        final Label l = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("wizard.page.header"), true, null, false); //$NON-NLS-1$
        l.setFont(JFaceResources.getBannerFont());
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label l2 = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("wizard.page.text"), true, null, false); //$NON-NLS-1$
        final GridData gridData = (GridData)l2.getLayoutData();
        Dialog.applyDialogFont(l2);
        gridData.widthHint = Common.calculateControlSize(l2,80,0).x;

        createTemplatesGroup(composite);

        createScriptSaveSettingsGroup(composite);

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

        NSISWizardDialogUtil.createRequiredFieldsLabel(composite);
        setPageComplete(validatePage());
    }

    private void createScriptSaveSettingsGroup(Composite parent)
    {
        Group g = new Group(parent,SWT.NONE);
        g.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        GridLayout layout = new GridLayout(1,false);
        g.setLayout(layout);

        Composite c = new Composite(g,SWT.NONE);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
        layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);
        mSaveLocationTypes = NSISWizardDialogUtil.createRadioGroup(c,new String[] {EclipseNSISPlugin.getResourceString("workspace.save.label"), //$NON-NLS-1$
                                                                      EclipseNSISPlugin.getResourceString("filesystem.save.label")}, //$NON-NLS-1$
                                                                      0,"save.label",true,null,false); //$NON-NLS-1$
        mSaveLocation = NSISWizardDialogUtil.createText(c, "","save.location.label",true,null,true); //$NON-NLS-1$ //$NON-NLS-2$
        ((GridData)mSaveLocation.getLayoutData()).horizontalSpan = 1;
        mSaveLocation.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                setPageComplete(validatePage());
                mCheckOverwrite = mSaveLocation.getText().length() > 0;
            }
        });
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mSaveLocation.setText(""); //$NON-NLS-1$
            }
        };
        mSaveLocationTypes[0].addSelectionListener(selectionAdapter);
        mSaveLocationTypes[1].addSelectionListener(selectionAdapter);

        Button b = new Button(c,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        b.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String savePath = mSaveLocation.getText();
                if(Common.isEmpty(savePath)) {
                    savePath = InstallOptionsPlugin.getResourceString("wizard.default.file.name"); //$NON-NLS-1$

                }
                if(mSaveLocationTypes[1].getSelection()) {
                    FileDialog dialog = new FileDialog(getShell(),SWT.SAVE);
                    dialog.setFileName(savePath);
                    dialog.setFilterExtensions(FILTER_EXTENSIONS);
                    dialog.setFilterNames(FILTER_NAMES);
                    dialog.setText(EclipseNSISPlugin.getResourceString("save.location.title")); //$NON-NLS-1$
                    savePath = dialog.open();
                    if(savePath != null) {
                        mSaveLocation.setText(savePath);
                    }
                }
                else {
                    SaveAsDialog dialog = new SaveAsDialog(getShell());
                    IPath path = new Path(savePath);
                    if(path.isAbsolute()) {
                        try {
                            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                            dialog.setOriginalFile(file);
                        }
                        catch (Exception e1) {
                        }
                    }
                    else {
                        dialog.setOriginalName(path.toString());
                    }
                    dialog.setTitle(EclipseNSISPlugin.getResourceString("save.location.title")); //$NON-NLS-1$
                    dialog.create();
                    dialog.setMessage(EclipseNSISPlugin.getResourceString("save.location.message")); //$NON-NLS-1$
                    int returnCode = dialog.open();
                    if(returnCode == Window.OK) {
                        mSaveLocation.setText(dialog.getResult().toString());
                        mCheckOverwrite = false;
                    }
                }
            }
        });

        c = new Composite(g,SWT.NONE);
        c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        layout = new GridLayout(2,false);
        layout.marginHeight = layout.marginWidth = 0;
        c.setLayout(layout);

        // open file for editing checkbox
        mOpenFileCheckbox = new Button(c,SWT.CHECK);
        mOpenFileCheckbox.setText(InstallOptionsPlugin.getResourceString("wizard.open.file.label")); //$NON-NLS-1$
        mOpenFileCheckbox.setSelection(true);
        mOpenFileCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        mEditorIdCombo = new Combo(c,SWT.DROP_DOWN|SWT.READ_ONLY);
        mEditorIdCombo.add(InstallOptionsPlugin.getResourceString("wizard.design.editor.label")); //$NON-NLS-1$
        mEditorIdCombo.add(InstallOptionsPlugin.getResourceString("wizard.source.editor.label")); //$NON-NLS-1$
        mEditorIdCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        mEditorIdCombo.select(0);

        MasterSlaveController msc = new MasterSlaveController(mOpenFileCheckbox);
        msc.addSlave(mEditorIdCombo);
    }

    private Group createTemplatesGroup(Composite parent)
    {
        Group group = NSISWizardDialogUtil.createGroup(parent, 1, null,null,false);
        ((GridLayout)group.getLayout()).makeColumnsEqualWidth = true;
        GridData data = (GridData)group.getLayoutData();
        data.grabExcessVerticalSpace = true;
        data.verticalAlignment = GridData.FILL;

        final Button b = NSISWizardDialogUtil.createCheckBox(group,"create.from.template.button.text",false,true,null,false); //$NON-NLS-1$

        MasterSlaveController m = new MasterSlaveController(b);
        SashForm form = new SashForm(group,SWT.HORIZONTAL);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        form.setLayoutData(data);

        MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public boolean canEnable(Control control)
            {
                return true;
            }

            public void enabled(Control control, boolean flag)
            {
                int id = (flag?SWT.COLOR_LIST_BACKGROUND:SWT.COLOR_WIDGET_BACKGROUND);
                control.setBackground(getShell().getDisplay().getSystemColor(id));
            }
        };

        Composite composite = new Composite(form,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        Label l = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("create.from.template.label"),b.getSelection(),m,true); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final List list = new List(composite,SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = Common.calculateControlSize(l,SWT.DEFAULT,6).y;
        list.setLayoutData(data);
        m.addSlave(list, mse);

        composite = new Composite(form,SWT.NONE);
        layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        l = NSISWizardDialogUtil.createLabel(composite,InstallOptionsPlugin.getResourceString("template.description.label"),true,m,false); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final StyledText t = new StyledText(composite,SWT.BORDER|SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = Common.calculateControlSize(t,SWT.DEFAULT,6).y;
        t.setLayoutData(data);
        t.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        t.setCursor(null);
        t.setCaret(null);
        m.addSlave(t, mse);

        final ListViewer viewer = new ListViewer(list);
        viewer.setContentProvider(new CollectionContentProvider());
        viewer.setLabelProvider(new CollectionLabelProvider());
        viewer.setInput(InstallOptionsTemplateManager.INSTANCE.getTemplates());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        viewer.setSorter(new ViewerSorter(collator));

        ViewerFilter filter = new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element)
            {
                if(element instanceof IInstallOptionsTemplate) {
                    IInstallOptionsTemplate template = (IInstallOptionsTemplate)element;
                    return template.isAvailable() && template.isEnabled() && !template.isDeleted();
                }
                return true;
            }
        };
        viewer.addFilter(filter);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                ISelection sel = event.getSelection();
                InstallOptionsWizard wizard = (InstallOptionsWizard)getWizard();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof IInstallOptionsTemplate) {
                        wizard.setTemplate((IInstallOptionsTemplate)obj);
                        t.setText(wizard.getTemplate().getDescription());
                    }
                }
                else {
                    wizard.setTemplate(null);
                }
                setPageComplete(validatePage());
            }
        });
        viewer.getList().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                if(canFlipToNextPage()) {
                    IWizardPage nextPage = getNextPage();
                    if(nextPage != null) {
                        getContainer().showPage(nextPage);
                    }
                }
            }
        });

        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mCreateFromTemplate = b.getSelection();
                setPageComplete(validatePage());
            }
        });

        m.updateSlaves();
        return group;
    }

    @Override
    public void setErrorMessage(String message)
    {
        super.setMessage(message,ERROR);
    }

    public boolean validatePage()
    {
        if((!mCreateFromTemplate || ((InstallOptionsWizard)getWizard()).getTemplate() != null)) {
            String pathname = mSaveLocation.getText();
            if(Common.isEmpty(pathname)) {
                setErrorMessage(EclipseNSISPlugin.getResourceString("empty.save.location.error")); //$NON-NLS-1$
                return false;
            }
            else if(Path.EMPTY.isValidPath(pathname)) {
                IPath path = new Path(pathname);
                path = path.removeLastSegments(1);
                if(mSaveLocationTypes[1].getSelection()) {
                    File file = new File(path.toOSString());
                    if(IOUtility.isValidDirectory(file)) {
                        setErrorMessage(null);
                        return true;
                    }
                }
                else {
                    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                    if(resource != null && (resource instanceof IFolder || resource instanceof IProject)) {
                        setErrorMessage(null);
                        return true;
                    }
                }
            }
            setErrorMessage(EclipseNSISPlugin.getFormattedString("invalid.save.location.error",new String[]{pathname})); //$NON-NLS-1$
            return false;
        }
        else {
            setErrorMessage(InstallOptionsPlugin.getResourceString("select.template.error")); //$NON-NLS-1$
            return false;
        }
    }

    public boolean finish()
    {
        IPath path = new Path(mSaveLocation.getText());
        if(Common.isEmpty(path.getFileExtension())) {
            path = path.addFileExtension(IInstallOptionsConstants.INI_EXTENSIONS[0]);
        }
        if(!path.isAbsolute()) {
            Common.openError(getShell(),InstallOptionsPlugin.getResourceString("absolute.save.path.error"),InstallOptionsPlugin.getShellImage()); //$NON-NLS-1$
            return false;
        }
        final boolean saveExternal = mSaveLocationTypes[1].getSelection();
        String pathString = saveExternal?path.toOSString():path.toString();
        final boolean exists;
        final File file;
        final IFile ifile;
        if(saveExternal) {
            ifile = null;
            file = new File(pathString);
            exists = file.exists();
        }
        else {
            file = null;
            ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            exists = ifile != null && ifile.exists();
            path = ifile != null?ifile.getLocation():null;
            if(path == null) {
                Common.openError(getShell(),EclipseNSISPlugin.getResourceString("local.filesystem.error"),InstallOptionsPlugin.getShellImage()); //$NON-NLS-1$
                return false;
            }
        }
        if(exists && mCheckOverwrite) {
            if(!Common.openQuestion(getShell(), EclipseNSISPlugin.getResourceString("question.title"), //$NON-NLS-1$
                    EclipseNSISPlugin.getFormattedString("save.path.question",new String[] {pathString}),  //$NON-NLS-1$
                    InstallOptionsPlugin.getShellImage())) {
                return false;
            }
            mCheckOverwrite = false;
        }
        java.util.List<IEditorPart> editors = NSISEditorUtilities.findEditors(path);
        if(!Common.isEmptyCollection(editors)) {
            java.util.List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();
            for (Iterator<IEditorPart> iter = editors.iterator(); iter.hasNext();) {
                IEditorPart editor = iter.next();
                if(editor.isDirty()) {
                    dirtyEditors.add(editor);
                }
            }
            if(dirtyEditors.size() > 0) {
                if(!Common.openConfirm(getShell(), EclipseNSISPlugin.getFormattedString("save.dirty.editor.confirm",new String[] {pathString}),  //$NON-NLS-1$
                    InstallOptionsPlugin.getShellImage())) {
                    return false;
                }
                for (Iterator<IEditorPart> iter = dirtyEditors.iterator(); iter.hasNext();) {
                    IEditorPart editor = iter.next();
                    editor.getSite().getPage().closeEditor(editor,false);
                    editors.remove(editor);
                }

                if(saveExternal) {
                    for (Iterator<IEditorPart> iter = editors.iterator(); iter.hasNext();) {
                        IEditorPart editor = iter.next();
                        editor.getSite().getPage().closeEditor(editor,false);
                    }
                }
            }
        }
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException
            {
                try {
                    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                    if(exists) {
                        if(saveExternal) {
                            if(file != null) {
                                file.delete();
                            }
                        }
                        else {
                            if(ifile != null) {
                                ifile.delete(true,true,null);
                            }
                        }
                    }
                    if(saveExternal) {
                        Writer writer = null;
                        try {
                            writer = new BufferedWriter(new FileWriter(file));
                            writer.write(getContents());
                        }
                        finally {
                            IOUtility.closeIO(writer);
                        }

                        if (file != null) {
                            IFile[] files = root.findFilesForLocationURI(file.toURI());
                            if (!Common.isEmptyArray(files)) {
                                for (int i = 0; i < files.length; i++) {
                                    files[i].refreshLocal(IResource.DEPTH_ZERO, null);
                                }
                            }
                        }
                    }
                    else {
                        if(ifile != null) {
                            ifile.create(new ByteArrayInputStream(getContents().getBytes()),true,null);
                        }
                    }
                }
                catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
        try {
            getContainer().run(true, false, op);
        }
        catch (InterruptedException e) {
            return false;
        }
        catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            Common.openError(getShell(), realException.getLocalizedMessage(), InstallOptionsPlugin.getShellImage());
            return false;
        }

        if (mOpenFileCheckbox.getSelection()) {
            final String editorId = mEditorIds[mEditorIdCombo.getSelectionIndex()];

            getShell().getDisplay().syncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try {
                        IEditorInput input;
                        if(saveExternal) {
                            input =new NSISExternalFileEditorInput(file);
                        }
                        else {
                            input = new FileEditorInput(ifile);
                        }
                        IDE.openEditor(page, input, editorId, true);
                    }
                    catch (PartInitException e) {
                    }
                }
            });
        }
        return true;
    }

    protected String getContents()
    {
        InstallOptionsDialog dialog = InstallOptionsDialog.loadINIFile(new INIFile());
        IInstallOptionsTemplate template = ((InstallOptionsWizard)getWizard()).getTemplate();
        if(template != null) {
            InstallOptionsWidget[] widgets = template.getWidgets();
            if(!Common.isEmptyArray(widgets)) {
                for (int i = 0; i < widgets.length; i++) {
                    dialog.addChild(widgets[i]);
                }
            }
        }
        INIFile iniFile = dialog.updateINIFile();
        int i=0;
        INILine line = new INILine("; "+InstallOptionsPlugin.getResourceString("wizard.file.header.comment")); //$NON-NLS-1$  //$NON-NLS-2$
        iniFile.addChild(i++,line);
        line = new INILine("; "+DateFormat.getDateTimeInstance().format(new Date())); //$NON-NLS-1$
        iniFile.addChild(i++,line);
        iniFile.update();

        return iniFile.toString();
    }
}
