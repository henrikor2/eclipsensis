/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.*;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.startup.FileAssociationChecker;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;

public class InstallOptionsPreferencePage extends PropertyPage implements IWorkbenchPreferencePage, IInstallOptionsConstants
{
    private Map<String, Boolean> mDisplaySettingsMap = new HashMap<String, Boolean>();
    private Map<String, Object> mGridSettingsMap = new HashMap<String, Object>();
    private Map<String, Boolean> mSnapGlueSettingsMap = new HashMap<String, Boolean>();
    private Map<String, DialogSize> mDialogSizesMap = new LinkedHashMap<String, DialogSize>();
    private DialogSize mDefaultDialogSize = null;
    private CheckboxTableViewer mDialogSizeViewer;
    private Button mEditDialogSize;
    private Button mRemoveDialogSize;
    private Button mShowRulers;
    private Button mShowGrid;
    private Button mShowGuides;
    private Button mShowDialogSize;
    private GridSettings mGridSettings;
    private SnapGlueSettings mSnapGlueSettings;
    private Map<String,NSISSyntaxStyle> mSyntaxStylesMap;
    private int mSyntaxStylesHashCode;
    private ListViewer mSyntaxStylesViewer;
    private InstallOptionsSourcePreviewer mPreviewer;
    private Object mData;
    private TabFolder mFolder;
    private Button mFileAssociation;
    private Button mAutosaveBeforePreview;

    /**
     *
     */
    public InstallOptionsPreferencePage()
    {
        super();
        setDescription(InstallOptionsPlugin.getResourceString("preference.page.description")); //$NON-NLS-1$
        loadPreferences();
    }

    private void loadPreferences()
    {
        // Ruler preference
        loadPreference(mDisplaySettingsMap, PREFERENCE_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_RULERS_DEFAULT);

        // Snap to Geometry preference
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GEOMETRY_DEFAULT);

        // Grid preferences
        loadPreference(mDisplaySettingsMap, PREFERENCE_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GRID_DEFAULT);
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GRID_DEFAULT);
        loadPreference(mGridSettingsMap, PREFERENCE_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                GRID_STYLE_DEFAULT);
        loadPreference(mGridSettingsMap, PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                GRID_ORIGIN_DEFAULT);
        loadPreference(mGridSettingsMap, PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                GRID_SPACING_DEFAULT);

        // Guides preferences
        loadPreference(mDisplaySettingsMap, PREFERENCE_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GUIDES_DEFAULT);
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GUIDES_DEFAULT);
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                GLUE_TO_GUIDES_DEFAULT);

        // Dialog size preferences
        loadPreference(mDisplaySettingsMap, PREFERENCE_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_DIALOG_SIZE_DEFAULT);

        mSyntaxStylesMap = NSISTextUtility.parseSyntaxStylesMap(getPreferenceStore().getString(PREFERENCE_SYNTAX_STYLES));
        mSyntaxStylesHashCode = mSyntaxStylesMap.hashCode();
    }

    @Override
    public void applyData(Object data)
    {
       mData = data;
       activateTab();
    }

    private <T> void loadPreference(Map<String, ? super T> map, String name, TypeConverter<T> converter, T defaultValue)
    {
        T o = null;
        try {
            IPreferenceStore store = getPreferenceStore();
            if(store.contains(name) || store.isDefault(name)) {
                o = converter.asType(store.getString(name));
            }
        }
        catch(Exception ex) {
            o = null;
        }
        if(o == null) {
            o = converter.makeCopy(defaultValue);
        }
        map.put(name,o);
    }

    private void savePreferences()
    {
        // Ruler preference
        savePreference(mDisplaySettingsMap, PREFERENCE_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_RULERS_DEFAULT);

        // Snap to Geometry preference
        savePreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GEOMETRY_DEFAULT);

        // Grid preferences
        savePreference(mDisplaySettingsMap, PREFERENCE_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GRID_DEFAULT);
        savePreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GRID_DEFAULT);
        savePreference(mGridSettingsMap, PREFERENCE_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                GRID_STYLE_DEFAULT);
        savePreference(mGridSettingsMap, PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                GRID_ORIGIN_DEFAULT);
        savePreference(mGridSettingsMap, PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                GRID_SPACING_DEFAULT);

        // Guides preferences
        savePreference(mDisplaySettingsMap, PREFERENCE_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GUIDES_DEFAULT);
        savePreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GUIDES_DEFAULT);
        savePreference(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                GLUE_TO_GUIDES_DEFAULT);

        // Dialog size preferences
        savePreference(mDisplaySettingsMap, PREFERENCE_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_DIALOG_SIZE_DEFAULT);

        DialogSizeManager.setDialogSizes(new ArrayList<DialogSize>(mDialogSizesMap.values()));
        DialogSizeManager.storeDialogSizes();

        int hashCode = mSyntaxStylesMap.hashCode();
        if(hashCode != mSyntaxStylesHashCode) {
            getPreferenceStore().setValue(PREFERENCE_SYNTAX_STYLES, NSISTextUtility.flattenSyntaxStylesMap(mSyntaxStylesMap));
            mSyntaxStylesHashCode = hashCode;
        }

        FileAssociationChecker.setFileAssociationChecking(FILE_ASSOCIATION_ID, mFileAssociation.getSelection());
        getPreferenceStore().setValue(PREFERENCE_AUTOSAVE_BEFORE_PREVIEW, mAutosaveBeforePreview.getSelection());
        InstallOptionsPlugin.getDefault().savePreferences();
    }

    @SuppressWarnings("unchecked")
    private <T> void savePreference(Map<String, ? super T> map, String name, TypeConverter<T> converter, T defaultValue)
    {
        T o = (T) map.get(name);
        if(o == null) {
            o = defaultValue;
        }
        getPreferenceStore().putValue(name,converter.asString(o));
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore()
    {
        return InstallOptionsPlugin.getDefault().getPreferenceStore();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent)
    {
        loadPreferences();
        loadDialogSizes();
        Composite parent2 = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent2.setLayout(layout);

        mFolder = new TabFolder(parent2, SWT.NONE);
        mFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        Dialog.applyDialogFont(mFolder);
        TabItem item = new TabItem(mFolder, SWT.NONE);
        item.setText(InstallOptionsPlugin.getResourceString("design.editor.tab.name")); //$NON-NLS-1$
        item.setControl(createDesignEditorTab(mFolder));
        item.setData(InstallOptionsDesignEditor.class);
        item = new TabItem(mFolder, SWT.NONE);
        item.setText(InstallOptionsPlugin.getResourceString("source.editor.tab.name")); //$NON-NLS-1$
        item.setControl(createSourceEditorTab(mFolder));
        item.setData(InstallOptionsSourceEditor.class);
        activateTab();

        mFileAssociation = new Button(parent2,SWT.CHECK);
        mFileAssociation.setText(InstallOptionsPlugin.getResourceString("check.default.editor.label")); //$NON-NLS-1$
        mFileAssociation.setSelection(FileAssociationChecker.getFileAssociationChecking(FILE_ASSOCIATION_ID));
        mFileAssociation.setLayoutData(new GridData(SWT.BEGINNING,SWT.CENTER,false,false));

        mAutosaveBeforePreview = new Button(parent2,SWT.CHECK);
        mAutosaveBeforePreview.setText(InstallOptionsPlugin.getResourceString("autosave.before.preview.label")); //$NON-NLS-1$
        mAutosaveBeforePreview.setSelection(getPreferenceStore().getBoolean(PREFERENCE_AUTOSAVE_BEFORE_PREVIEW));
        mAutosaveBeforePreview.setLayoutData(new GridData(SWT.BEGINNING,SWT.CENTER,false,false));

        return parent2;
    }

    private void activateTab()
    {
        if(mFolder != null && mData != null) {
            TabItem[] items = mFolder.getItems();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(items[i].getData() == mData) {
                        mFolder.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private Button makeStyleButton(Composite parent, String labelResource, final int styleFlag)
    {
        final Button styleButton = new Button(parent, SWT.CHECK);
        styleButton.setText(EclipseNSISPlugin.getResourceString(labelResource));
        GridData gd= new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
        gd.horizontalSpan= 2;
        styleButton.setLayoutData(gd);
        styleButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = (IStructuredSelection)mSyntaxStylesViewer.getSelection();
                if(!sel.isEmpty()) {
                    boolean state = styleButton.getSelection();
                    String key= (String)sel.getFirstElement();
                    NSISSyntaxStyle style = mSyntaxStylesMap.get(key);
                    style.setStyle(styleFlag, state);
                    mPreviewer.setSyntaxStyles(mSyntaxStylesMap);
                }
            }
        });
        return styleButton;
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),PLUGIN_CONTEXT_PREFIX+"installoptions_preferences_context"); //$NON-NLS-1$
    }

    private Control createSourceEditorTab(Composite parent)
    {
        Composite syntaxComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(1, false);
        syntaxComposite.setLayout(layout);

        Link link= new Link(syntaxComposite, SWT.NONE);
        link.setText(InstallOptionsPlugin.getResourceString("source.editor.preferences.description")); //$NON-NLS-1$
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.preferencePages.GeneralTextEditor", null, null); //$NON-NLS-1$
            }
        });
        link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        Label l= new Label(syntaxComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("syntax.options")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        Composite listComposite= new Composite(syntaxComposite, SWT.NONE);
        layout= new GridLayout(2, false);
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        listComposite.setLayout(layout);
        listComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        mSyntaxStylesViewer = new ListViewer(listComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, true, true);
        gd.heightHint= convertHeightInCharsToPixels(9);
        gd.widthHint= convertWidthInCharsToPixels(30);
        mSyntaxStylesViewer.getControl().setLayoutData(gd);
        mSyntaxStylesViewer.setContentProvider(new CollectionContentProvider());
        mSyntaxStylesViewer.setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element) {
                if(element instanceof String) {
                    String name = (String)element;
                    String label = InstallOptionsPlugin.getResourceString(name.toLowerCase()+".label",""); //$NON-NLS-1$ //$NON-NLS-2$
                    if(Common.isEmpty(label)) {
                        label = name;
                    }
                    return label;
                }
                return super.getText(element);
            }
        });

        Composite stylesComposite= new Composite(listComposite, SWT.NONE);
        layout= new GridLayout(2,false);
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        l= new Label(stylesComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("color")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        final ColorEditor ce= new ColorEditor(stylesComposite);
        Button foregroundColorButton= ce.getButton();
        foregroundColorButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        foregroundColorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = (IStructuredSelection)mSyntaxStylesViewer.getSelection();
                if(!sel.isEmpty()) {
                    String key= (String)sel.getFirstElement();
                    NSISSyntaxStyle style = mSyntaxStylesMap.get(key);
                    style.setForeground(ce.getRGB());
                    mPreviewer.setSyntaxStyles(mSyntaxStylesMap);
                }
            }
        });

        final Button styleBold = makeStyleButton(stylesComposite, "bold", SWT.BOLD); //$NON-NLS-1$
        final Button styleItalic = makeStyleButton(stylesComposite, "italic", SWT.ITALIC); //$NON-NLS-1$
        final Button styleUnderline = makeStyleButton(stylesComposite, "underline", TextAttribute.UNDERLINE); //$NON-NLS-1$
        final Button styleStrikethrough = makeStyleButton(stylesComposite, "strikethrough", TextAttribute.STRIKETHROUGH); //$NON-NLS-1$

        mSyntaxStylesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            private void enable(boolean flag)
            {
                ce.getButton().setEnabled(flag);
                styleItalic.setEnabled(flag);
                styleBold.setEnabled(flag);
                styleUnderline.setEnabled(flag);
                styleStrikethrough.setEnabled(flag);
            }

            public void selectionChanged(SelectionChangedEvent event)
            {
                ISelection sel = event.getSelection();
                if(sel.isEmpty()) {
                    enable(false);
                }
                else {
                    enable(true);
                    String key= (String)((IStructuredSelection)sel).getFirstElement();
                    NSISSyntaxStyle style = mSyntaxStylesMap.get(key);
                    ce.setRGB(style.getForeground());
                    styleBold.setSelection(style.isBold());
                    styleItalic.setSelection(style.isItalic());
                    styleUnderline.setSelection(style.isUnderline());
                    styleStrikethrough.setSelection(style.isStrikethrough());
                }
            }
        });

        l= new Label(syntaxComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("preview")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Set<String> input = mSyntaxStylesMap.keySet();
        mSyntaxStylesViewer.setInput(input);
        if(!Common.isEmptyCollection(input)) {
            mSyntaxStylesViewer.setSelection(new StructuredSelection(input.iterator().next()));
        }

        Control previewer= createPreviewer(syntaxComposite);
        gd= new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint= convertWidthInCharsToPixels(20);
        gd.heightHint= convertHeightInCharsToPixels(10);
        previewer.setLayoutData(gd);

        return syntaxComposite;
    }

    private Control createPreviewer(Composite parent)
    {
        mPreviewer= new InstallOptionsSourcePreviewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        NSISTextUtility.hookSourceViewer(mPreviewer);
        SourceViewerConfiguration configuration= new InstallOptionsSourceViewerConfiguration();
        mPreviewer.configure(configuration);

        String content= new String(IOUtility.loadContentFromStream(getClass().getResourceAsStream("InstallOptionsPreview.txt"))); //$NON-NLS-1$
        IDocument document= new Document(content);
        new InstallOptionsDocumentSetupParticipant().setup(document);
        mPreviewer.setDocument(document);
        mPreviewer.setEditable(false);

        return mPreviewer.getControl();
    }

    private Composite createDesignEditorTab(Composite parent)
    {
        Composite parent2 = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        parent2.setLayout(layout);

        Label l = new Label(parent2,SWT.WRAP);
        l.setText(InstallOptionsPlugin.getResourceString("design.editor.preferences.description")); //$NON-NLS-1$
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        l.setLayoutData(gd);

        createDisplayGroup(parent2);
        createDialogSizesGroup(parent2);
        mSnapGlueSettings = new SnapGlueSettings(parent2, mSnapGlueSettingsMap);
        mGridSettings = new GridSettings(parent2,mGridSettingsMap);
        return parent2;
    }

    private void loadDialogSizes()
    {
        List<DialogSize> list = DialogSizeManager.getDialogSizes();
        for (Iterator<DialogSize> iter = list.iterator(); iter.hasNext();) {
            DialogSize element = (iter.next()).getCopy();
            mDialogSizesMap.put(element.getName().toLowerCase(),element);
        }
    }

    private Button createButton(Composite parent, Image image, String tooltipResource, Listener selectionListener)
    {
        Button button = new Button(parent, SWT.PUSH);
        button.setImage(image);
        button.setToolTipText(EclipseNSISPlugin.getResourceString(tooltipResource));
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        button.addListener(SWT.Selection, selectionListener);
        return button;
    }

    /**
     * @param composite
     */
    private Control createDialogSizesGroup(final Composite composite)
    {
        final Group group = new Group(composite,SWT.SHADOW_ETCHED_IN);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = convertWidthInCharsToPixels(60);
        group.setLayoutData(gridData);
        GridLayout layout = new GridLayout(2,false);
        group.setLayout(layout);
        group.setText(InstallOptionsPlugin.getResourceString("dialog.sizes.group.name")); //$NON-NLS-1$

        Table table= new Table(group, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gridData);
        FontData[] fd = table.getFont().getFontData();
        for (int i = 0; i < fd.length; i++) {
            fd[i].setStyle(fd[i].getStyle()|SWT.BOLD);
        }
        final Font boldFont = new Font(table.getShell().getDisplay(),fd);
        table.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                boldFont.dispose();
            }
        });

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        final TableColumn[] columns = new TableColumn[3];
        columns[0] = new TableColumn(table, SWT.NONE);
        columns[0].setText(InstallOptionsPlugin.getResourceString("dialog.size.name.label")); //$NON-NLS-1$

        columns[1] = new TableColumn(table, SWT.NONE);
        columns[1].setText(InstallOptionsPlugin.getResourceString("dialog.size.width.label")); //$NON-NLS-1$

        columns[2] = new TableColumn(table, SWT.NONE);
        columns[2].setText(InstallOptionsPlugin.getResourceString("dialog.size.height.label")); //$NON-NLS-1$

        mDialogSizeViewer = new CheckboxTableViewer(table);
        DialogSizeLabelProvider provider = new DialogSizeLabelProvider();
        provider.setDefaultFont(boldFont);
        mDialogSizeViewer.setLabelProvider(provider);
        mDialogSizeViewer.setContentProvider(new CollectionContentProvider());
        mDialogSizeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                editDialogSize();
            }
        });

        final Composite buttons= new Composite(group, SWT.NONE);
        buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);

        createButton(buttons, CommonImages.ADD_ICON, "new.tooltip", //$NON-NLS-1$
                     new Listener() {
                         public void handleEvent(Event e) {
                             new DialogSizeDialog(getShell(),null).open();
                         }
                     });

        mEditDialogSize = createButton(buttons, CommonImages.EDIT_ICON, "edit.tooltip", //$NON-NLS-1$
                                       new Listener() {
                                           public void handleEvent(Event e) {
                                               editDialogSize();
                                           }
                                       });

        mRemoveDialogSize = createButton(buttons, CommonImages.DELETE_ICON, "remove.tooltip", //$NON-NLS-1$
                                         new Listener() {
                                             @SuppressWarnings("unchecked")
                                             public void handleEvent(Event e) {
                                                 IStructuredSelection selection= (IStructuredSelection) mDialogSizeViewer.getSelection();
                                                 if(!selection.isEmpty()) {
                                                     Collection<DialogSize> coll = (Collection<DialogSize>)mDialogSizeViewer.getInput();
                                                     for(Iterator<?> iter=selection.toList().iterator(); iter.hasNext(); ) {
                                                         DialogSize ds = (DialogSize)iter.next();
                                                         coll.remove(ds);
                                                         if(mDefaultDialogSize.equals(ds)) {
                                                             mDefaultDialogSize = null;
                                                         }
                                                     }
                                                     if(mDefaultDialogSize == null && coll.size() > 0) {
                                                         mDefaultDialogSize = coll.iterator().next();
                                                         mDefaultDialogSize.setDefault(true);
                                                     }
                                                     mDialogSizeViewer.refresh();
                                                     mDialogSizeViewer.setAllChecked(false);
                                                     mDialogSizeViewer.setChecked(mDefaultDialogSize,true);
                                                 }
                                             }
                                         });

        mDialogSizeViewer.addCheckStateListener(new ICheckStateListener() {
            @SuppressWarnings("unchecked")
            public void checkStateChanged(CheckStateChangedEvent event) {
                DialogSize dialogSize= (DialogSize)event.getElement();
                boolean checked = event.getChecked();
                Collection<DialogSize> dialogSizes = (Collection<DialogSize>)mDialogSizeViewer.getInput();
                if(dialogSizes.size() == 1) {
                    checked = true;
                }
                else {
                    for(Iterator<DialogSize> iter=dialogSizes.iterator(); iter.hasNext(); ) {
                        DialogSize ds = iter.next();
                        if(!ds.equals(dialogSize) && ds.isDefault() == checked) {
                            ds.setDefault(!checked);
                            mDialogSizeViewer.setChecked(ds,!checked);
                            mDialogSizeViewer.refresh(ds,true);
                            break;
                        }
                    }
                }
                dialogSize.setDefault(checked);
                mDialogSizeViewer.setChecked(dialogSize,checked);
                mDialogSizeViewer.refresh(dialogSize,true);
                updateButtons();
            }
        });

        mDialogSizeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                updateButtons();
            }
        });

        Label l = new Label(group,SWT.WRAP);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        l.setLayoutData(gridData);
        l.setText(InstallOptionsPlugin.getResourceString("dialog.size.group.footer")); //$NON-NLS-1$

        updateDialogSizeViewerInput();
        table.addControlListener(new TableResizer(new double[]{2,1,1}));

        return group;
    }

    private void updateDialogSizeViewerInput()
    {
        mDialogSizeViewer.setInput(mDialogSizesMap.values());
        mDialogSizeViewer.setAllChecked(false);
        boolean foundDefault = false;
        for (Iterator<DialogSize> iter=mDialogSizesMap.values().iterator(); iter.hasNext(); ) {
            DialogSize ds = iter.next();
            if(ds.isDefault()) {
                if(!foundDefault) {
                    mDialogSizeViewer.setChecked(ds,true);
                    mDefaultDialogSize = ds;
                    foundDefault = true;
                }
                else {
                    ds.setDefault(false);
                }
            }
        }

        updateButtons();
    }

    /**
     * @param tv
     */
    private void editDialogSize()
    {
        IStructuredSelection sel = (IStructuredSelection)mDialogSizeViewer.getSelection();
        if(!sel.isEmpty() && sel.size() == 1) {
            DialogSize ds = (DialogSize)sel.getFirstElement();
            new DialogSizeDialog(getShell(),ds).open();
        }
    }

    protected void updateButtons()
    {
        IStructuredSelection selection= (IStructuredSelection) mDialogSizeViewer.getSelection();
        int selectionCount= selection.size();
        int itemCount= mDialogSizeViewer.getTable().getItemCount();
        mEditDialogSize.setEnabled(selectionCount == 1);
        mRemoveDialogSize.setEnabled(selectionCount > 0 && selectionCount < itemCount);
    }

    /**
     * @param composite
     */
    private Control createDisplayGroup(Composite composite)
    {
        Group group = new Group(composite,SWT.SHADOW_ETCHED_IN);
        GridData gridData = new GridData(SWT.FILL,SWT.FILL,false,true);
        group.setLayoutData(gridData);
        group.setLayout(new GridLayout(2,false));
        group.setText(InstallOptionsPlugin.getResourceString("display.group.name")); //$NON-NLS-1$

        mShowRulers = new Button(group,SWT.CHECK);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        mShowRulers.setLayoutData(gridData);
        mShowRulers.setText(InstallOptionsPlugin.getResourceString("show.rulers.label")); //$NON-NLS-1$
        mShowRulers.setSelection((mDisplaySettingsMap.get(PREFERENCE_SHOW_RULERS)).booleanValue());
        mShowRulers.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mDisplaySettingsMap.put(PREFERENCE_SHOW_RULERS, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });

        mShowGrid = new Button(group,SWT.CHECK);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        mShowGrid.setLayoutData(gridData);
        mShowGrid.setText(InstallOptionsPlugin.getResourceString("show.grid.label")); //$NON-NLS-1$
        mShowGrid.setSelection((mDisplaySettingsMap.get(PREFERENCE_SHOW_GRID)).booleanValue());
        mShowGrid.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mDisplaySettingsMap.put(PREFERENCE_SHOW_GRID, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });

        mShowGuides = new Button(group,SWT.CHECK);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        mShowGuides.setLayoutData(gridData);
        mShowGuides.setText(InstallOptionsPlugin.getResourceString("show.guides.label")); //$NON-NLS-1$
        mShowGuides.setSelection((mDisplaySettingsMap.get(PREFERENCE_SHOW_GUIDES)).booleanValue());
        mShowGuides.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mDisplaySettingsMap.put(PREFERENCE_SHOW_GUIDES, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });

        mShowDialogSize = new Button(group,SWT.CHECK);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        mShowDialogSize.setLayoutData(gridData);
        mShowDialogSize.setText(InstallOptionsPlugin.getResourceString("show.dialog.size.label")); //$NON-NLS-1$
        mShowDialogSize.setSelection((mDisplaySettingsMap.get(PREFERENCE_SHOW_DIALOG_SIZE)).booleanValue());
        mShowDialogSize.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mDisplaySettingsMap.put(PREFERENCE_SHOW_DIALOG_SIZE, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });
        return group;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    @Override
    protected void performDefaults()
    {
        mDisplaySettingsMap.put(PREFERENCE_SHOW_RULERS,SHOW_RULERS_DEFAULT);
        mShowRulers.setSelection(SHOW_RULERS_DEFAULT.booleanValue());

        mDisplaySettingsMap.put(PREFERENCE_SHOW_GRID,SHOW_GRID_DEFAULT);
        mShowGrid.setSelection(SHOW_GRID_DEFAULT.booleanValue());

        mDisplaySettingsMap.put(PREFERENCE_SHOW_DIALOG_SIZE,SHOW_DIALOG_SIZE_DEFAULT);
        mShowDialogSize.setSelection(SHOW_DIALOG_SIZE_DEFAULT.booleanValue());

        mDisplaySettingsMap.put(PREFERENCE_SHOW_GUIDES,SHOW_GUIDES_DEFAULT);
        mShowGuides.setSelection(SHOW_GUIDES_DEFAULT.booleanValue());

        mDialogSizesMap.clear();
        List<DialogSize> list = DialogSizeManager.getPresetDialogSizes();
        for (Iterator<DialogSize> iter = list.iterator(); iter.hasNext();) {
            DialogSize element = iter.next();
            mDialogSizesMap.put(element.getName().toLowerCase(),element.getCopy());
        }
        updateDialogSizeViewerInput();

        mGridSettingsMap.put(PREFERENCE_GRID_STYLE, GRID_STYLE_DEFAULT);
        mGridSettingsMap.put(PREFERENCE_GRID_ORIGIN, new org.eclipse.draw2d.geometry.Point(GRID_ORIGIN_DEFAULT));
        mGridSettingsMap.put(PREFERENCE_GRID_SPACING, new Dimension(GRID_SPACING_DEFAULT));
        mGridSettings.setSettings(mGridSettingsMap);

        mSnapGlueSettingsMap.put(PREFERENCE_SNAP_TO_GEOMETRY, SNAP_TO_GEOMETRY_DEFAULT);
        mSnapGlueSettingsMap.put(PREFERENCE_SNAP_TO_GRID, SNAP_TO_GRID_DEFAULT);
        mSnapGlueSettingsMap.put(PREFERENCE_SNAP_TO_GUIDES, SNAP_TO_GUIDES_DEFAULT);
        mSnapGlueSettingsMap.put(PREFERENCE_GLUE_TO_GUIDES, GLUE_TO_GUIDES_DEFAULT);
        mSnapGlueSettings.setSettings(mSnapGlueSettingsMap);

        ISelection sel = mSyntaxStylesViewer.getSelection();
        mSyntaxStylesMap.clear();
        InstallOptionsPlugin.getDefault().setSyntaxStyles(mSyntaxStylesMap);
        mSyntaxStylesViewer.setInput(mSyntaxStylesMap.keySet());
        mSyntaxStylesViewer.setSelection(sel);
        mPreviewer.setSyntaxStyles(mSyntaxStylesMap);

        mFileAssociation.setSelection(CHECK_FILE_ASSOCIATION_DEFAULT);
        mAutosaveBeforePreview.setSelection(getPreferenceStore().getDefaultBoolean(PREFERENCE_AUTOSAVE_BEFORE_PREVIEW));
        super.performDefaults();
    }

    @Override
    public boolean performOk()
    {
        if(getPreferenceStore().contains(PREFERENCE_CHECK_EDITOR_ASSOCIATION)) {
            getPreferenceStore().setToDefault(PREFERENCE_CHECK_EDITOR_ASSOCIATION);
        }
        savePreferences();
        return super.performOk();
    }

    private class DialogSizeLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider
    {
        private Font mDefaultFont;


        public void setDefaultFont(Font defaultFont)
        {
            mDefaultFont = defaultFont;
        }

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
            DialogSize ds = (DialogSize) element;

            switch (columnIndex) {
                case 0:
                    return ds.getName();
                case 1:
                    return Integer.toString(ds.getSize().width);
                case 2:
                    return Integer.toString(ds.getSize().height);
                default:
                    return ""; //$NON-NLS-1$
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element)
        {
            if(element instanceof DialogSize) {
                if(((DialogSize)element).isDefault()) {
                    return mDefaultFont;
                }
            }
            return null;
        }
    }

    public class DialogSizeDialog extends Dialog
    {
        private DialogSize mOriginal;
        private DialogSize mCurrent;

        private VerifyListener mNumberVerifyListener = new NumberVerifyListener();

        /**
         * @param parentShell
         */
        public DialogSizeDialog(Shell parentShell, DialogSize dialogSize)
        {
            super(parentShell);
            setShellStyle(getShellStyle()|SWT.RESIZE);
            mOriginal = dialogSize;
            mCurrent = (dialogSize==null?new DialogSize("",false,new Dimension()): //$NON-NLS-1$
                                         dialogSize.getCopy());
        }

        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText((Common.isEmpty(mCurrent.getName())?InstallOptionsPlugin.getResourceString("dialog.size.dialog.add.title"):InstallOptionsPlugin.getResourceString("dialog.size.dialog.edit.title"))); //$NON-NLS-1$ //$NON-NLS-2$
            newShell.setImage(InstallOptionsPlugin.getShellImage());
        }

        @Override
        public void create()
        {
            super.create();
            updateOKButton();
        }

        /**
         *
         */
        private void updateOKButton()
        {
            getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(mCurrent.getName()) && mCurrent.getSize().width > 0 && mCurrent.getSize().height > 0);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void okPressed()
        {
            String oldName = (mOriginal == null?"":mOriginal.getName().toLowerCase()); //$NON-NLS-1$
            String newName = mCurrent.getName().toLowerCase();
            if(((mOriginal == null || !oldName.equals(newName)) && mDialogSizesMap.containsKey(newName))) {
                if(Common.openQuestion(getShell(),InstallOptionsPlugin.getResourceString("confirm.overwrite.title"), //$NON-NLS-1$
                        InstallOptionsPlugin.getFormattedString("dialog.size.overwrite.message",new Object[]{mCurrent.getName()}), //$NON-NLS-1$
                        InstallOptionsPlugin.getShellImage())) {
                    DialogSize old = mDialogSizesMap.remove(newName);
                    if(old.equals(mDefaultDialogSize)) {
                        mDefaultDialogSize = null;
                    }
                }
            }
            if(mOriginal == null) {
                mDialogSizesMap.put(newName,mCurrent);
            }
            else {
                mOriginal.setSize(mCurrent.getSize());
                mOriginal.setName(mCurrent.getName());
                if(!oldName.equals(newName)) {
                    mDialogSizesMap.remove(oldName);
                    mDialogSizesMap.put(newName,mOriginal);
                }
                mCurrent = mOriginal;
            }
            if(mDefaultDialogSize == null) {
                mDefaultDialogSize = mCurrent;
                mCurrent.setDefault(true);
                Collection<DialogSize> dialogSizes = (Collection<DialogSize>)mDialogSizeViewer.getInput();
                for(Iterator<DialogSize> iter=dialogSizes.iterator(); iter.hasNext(); ) {
                    DialogSize ds = iter.next();
                    if(!ds.equals(mDefaultDialogSize)) {
                        ds.setDefault(false);
                    }
                }
            }
            mDialogSizeViewer.refresh(true);
            mDialogSizeViewer.setAllChecked(false);
            mDialogSizeViewer.setChecked(mDefaultDialogSize,true);
            updateButtons();
            super.okPressed();
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite parent2 = (Composite)super.createDialogArea(parent);
            Composite composite = new Composite(parent2,SWT.NONE);
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout gridLayout = new GridLayout(2,false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            composite.setLayout(gridLayout);
            Label l = new Label(composite,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("dialog.size.dialog.name.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());

            final Text name = new Text(composite,SWT.BORDER);
            name.setText(mCurrent.getName());
            name.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    mCurrent.setName(name.getText());
                    updateOKButton();
                }}
            );
            initializeDialogUnits(name);
            GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
            data.widthHint = convertWidthInCharsToPixels(50);
            name.setLayoutData(data);

            l = new Label(composite,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("dialog.size.dialog.width.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());

            final Text width = new Text(composite,SWT.BORDER);
            width.setText(Integer.toString(mCurrent.getSize().width));
            width.addVerifyListener(mNumberVerifyListener);
            width.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    String text = width.getText();
                    mCurrent.getSize().width = (Common.isEmpty(text)?0:Integer.parseInt(text));
                    updateOKButton();
                }}
            );
            width.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    String text = width.getText();
                    if(Common.isEmpty(text)) {
                        width.setText(Integer.toString(mCurrent.getSize().width));
                    }
                }
            });
            data = new GridData();
            data.widthHint = convertWidthInCharsToPixels(5);
            width.setLayoutData(data);

            l = new Label(composite,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("dialog.size.dialog.height.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());

            final Text height = new Text(composite,SWT.BORDER);
            height.setText(Integer.toString(mCurrent.getSize().height));
            height.addVerifyListener(mNumberVerifyListener);
            height.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    String text = height.getText();
                    mCurrent.getSize().height = (Common.isEmpty(text)?0:Integer.parseInt(text));
                    updateOKButton();
                }}
            );
            height.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    String text = height.getText();
                    if(Common.isEmpty(text)) {
                        height.setText(Integer.toString(mCurrent.getSize().height));
                    }
                }
            });
            data = new GridData();
            data.widthHint = convertWidthInCharsToPixels(5);
            height.setLayoutData(data);

            return parent2;
        }
    }
}
