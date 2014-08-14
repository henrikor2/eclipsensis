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

import java.io.InputStream;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.startup.FileAssociationChecker;
import net.sf.eclipsensis.util.IOUtility;
import net.sf.eclipsensis.wizard.util.MasterSlaveController;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class NSISEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, INSISConstants, INSISEditorPreferenceConstants
{
    private static final String[][] cSyntaxStyleListModel = {
            {EclipseNSISPlugin.getResourceString("comments.label"),COMMENTS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("strings.label"),STRINGS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("commands.label"),COMMANDS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("installer.attributes.label"),INSTALLER_ATTRIBUTES_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("compiletime.commands.label"),COMPILETIME_COMMANDS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("callbacks.label"),CALLBACKS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("instructions.label"),INSTRUCTIONS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("instruction.parameters.label"),INSTRUCTION_PARAMETERS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("instruction.options.label"),INSTRUCTION_OPTIONS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("symbols.label"),SYMBOLS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("predefined.variables.label"),PREDEFINED_VARIABLES_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("plugins.label"),PLUGINS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("task.tags.label"),TASK_TAGS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("langstrings.label"),LANGSTRINGS_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("userdefined.variables.label"),USERDEFINED_VARIABLES_STYLE}, //$NON-NLS-1$
            {EclipseNSISPlugin.getResourceString("numbers.label"),NUMBERS_STYLE} //$NON-NLS-1$
    };
    private static final String[] cPreferenceKeys;

    private Map<String, NSISSyntaxStyle> mStyleMap = new HashMap<String, NSISSyntaxStyle>();
    private PreferenceStoreWrapper mPreferenceStore;
    private NSISSourceViewer mPreviewer;

    private Map<Button, String> mCheckBoxes= new HashMap<Button, String>();
    private SelectionListener mCheckBoxListener= new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
        }
        public void widgetSelected(SelectionEvent e) {
            Button button= (Button) e.widget;
            mPreferenceStore.setValue(mCheckBoxes.get(button), button.getSelection());
        }
    };

    private List mSyntaxStyleList;
    private ColorEditor mSyntaxColorEditor;
    private Button mStyleBold;
    private Button mStyleItalic;
    private Button mStyleUnderline;
    private Button mStyleStrikethrough;
    private Button mFileAssociationButton;
    private Combo mDropExternalFilesCombo;

    private ColorEditor mMatchingDelimsColorEditor;

    private MasterSlaveController mMasterSlaveController;

    static
    {
        Comparator<String[]> comparator = new Comparator<String[]>() {
            public int compare(String[] first, String[] second)
            {
                int n = first[0].compareToIgnoreCase(second[0]);
                if(n == 0) {
                    n = first[1].compareToIgnoreCase(second[1]);
                }
                return n;
            }
        };
        Arrays.sort(cSyntaxStyleListModel, comparator);
        cPreferenceKeys = new String[cSyntaxStyleListModel.length+2];
        int i = 0;
        for (; i < cSyntaxStyleListModel.length; i++) {
            cPreferenceKeys[i] = cSyntaxStyleListModel[i][1];
        }
        cPreferenceKeys[i++] = MATCHING_DELIMITERS;
        cPreferenceKeys[i++] = MATCHING_DELIMITERS_COLOR;
    }

    public NSISEditorPreferencePage()
    {
        super();
        setDescription(EclipseNSISPlugin.getResourceString("editor.preferences.description")); //$NON-NLS-1$
        setPreferenceStore(NSISPreferences.getInstance().getPreferenceStore());
        mPreferenceStore= new PreferenceStoreWrapper(getPreferenceStore());
        mPreferenceStore.load(cPreferenceKeys);
    }

    /*
     * @see IWorkbenchPreferencePage#init()
     */
    public void init(IWorkbench workbench) {
    }

    /*
     * @see PreferencePage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_editorprefs_context"); //$NON-NLS-1$
    }

    private void handleSyntaxStyleListSelection()
    {
        int i= mSyntaxStyleList.getSelectionIndex();
        String key= cSyntaxStyleListModel[i][1];
        NSISSyntaxStyle style = getStyle(key);
        mSyntaxColorEditor.setRGB(style.getForeground());
        mStyleBold.setSelection(style.isBold());
        mStyleItalic.setSelection(style.isItalic());
        mStyleUnderline.setSelection(style.isUnderline());
        mStyleStrikethrough.setSelection(style.isStrikethrough());
    }

    private NSISSyntaxStyle getStyle(String key)
    {
        NSISSyntaxStyle style = mStyleMap.get(key);
        if(style == null) {
            style = NSISSyntaxStyle.parse(mPreferenceStore.getString(key));
            mStyleMap.put(key,style);
        }
        return style;
    }

    private Composite createAppearanceGroup(Composite parent)
    {
        Composite appearanceComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        String label = EclipseNSISPlugin.getResourceString("show.matching.delimiters"); //$NON-NLS-1$
        Button cb = addCheckBox(appearanceComposite, label, MATCHING_DELIMITERS, 0);
        Label l = new Label(appearanceComposite, SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("matching.delimiters.color")); //$NON-NLS-1$
        GridData gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gd.horizontalIndent= 20;
        l.setLayoutData(gd);

        mMatchingDelimsColorEditor = new ColorEditor(appearanceComposite);
        Button button = mMatchingDelimsColorEditor.getButton();
        button.setLayoutData(new GridData());
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferenceConverter.setValue(mPreferenceStore, MATCHING_DELIMITERS_COLOR, mMatchingDelimsColorEditor.getRGB());
            }
        });
        mMasterSlaveController = new MasterSlaveController(cb);
        mMasterSlaveController.addSlave(l);
        mMasterSlaveController.addSlave(button);

        return appearanceComposite;
    }

    private Composite createOtherGroup(Composite parent)
    {
        Composite otherComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        otherComposite.setLayout(layout);

        Label l = new Label(otherComposite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("drop.external.files.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.BEGINNING,SWT.CENTER,false,false));

        mDropExternalFilesCombo = new Combo(otherComposite,SWT.DROP_DOWN|SWT.READ_ONLY);
        mDropExternalFilesCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        mDropExternalFilesCombo.add(EclipseNSISPlugin.getResourceString("drop.external.files.insert.label"),DROP_EXTERNAL_FILES_INSERT_AS_NSIS_COMMANDS); //$NON-NLS-1$
        mDropExternalFilesCombo.add(EclipseNSISPlugin.getResourceString("drop.external.files.edit.label"),DROP_EXTERNAL_FILES_OPEN_IN_EDITORS); //$NON-NLS-1$
        mDropExternalFilesCombo.add(EclipseNSISPlugin.getResourceString("drop.external.files.ask.label"),DROP_EXTERNAL_FILES_ASK); //$NON-NLS-1$

        mFileAssociationButton = new Button(otherComposite,SWT.CHECK);
        mFileAssociationButton.setText(EclipseNSISPlugin.getResourceString("check.editor.association.label")); //$NON-NLS-1$
        GridData data = new GridData(SWT.BEGINNING,SWT.CENTER,false,false);
        data.horizontalSpan = 2;
        mFileAssociationButton.setLayoutData(data);

        return otherComposite;
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
                boolean state = styleButton.getSelection();

                int i= mSyntaxStyleList.getSelectionIndex();
                String key= cSyntaxStyleListModel[i][1];
                if (key != null) {
                    NSISSyntaxStyle style = getStyle(key);
                    style.setStyle(styleFlag, state);
                    saveStyle(key, style);
                }
            }
        });
        return styleButton;
    }

    private Composite createSyntaxGroup(Composite parent)
    {
        Composite syntaxComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(1, false);
        syntaxComposite.setLayout(layout);

        Label l= new Label(syntaxComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("syntax.options")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        Composite listComposite= new Composite(syntaxComposite, SWT.NONE);
        layout= new GridLayout(2, false);
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        listComposite.setLayout(layout);
        listComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        mSyntaxStyleList= new List(listComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, true, true);
        gd.heightHint= convertHeightInCharsToPixels(9);
        gd.widthHint= convertWidthInCharsToPixels(30);
        mSyntaxStyleList.setLayoutData(gd);

        Composite stylesComposite= new Composite(listComposite, SWT.NONE);
        layout= new GridLayout(2, false);
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        l= new Label(stylesComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("color")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mSyntaxColorEditor= new ColorEditor(stylesComposite);
        Button foregroundColorButton= mSyntaxColorEditor.getButton();
        foregroundColorButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

        mSyntaxStyleList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSyntaxStyleListSelection();
            }
        });
        foregroundColorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int i= mSyntaxStyleList.getSelectionIndex();
                String key= cSyntaxStyleListModel[i][1];
                NSISSyntaxStyle style = getStyle(key);
                style.setForeground(mSyntaxColorEditor.getRGB());
                saveStyle(key, style);
            }
        });

        mStyleBold = makeStyleButton(stylesComposite, "bold", SWT.BOLD); //$NON-NLS-1$
        mStyleItalic = makeStyleButton(stylesComposite, "italic", SWT.ITALIC); //$NON-NLS-1$
        mStyleUnderline = makeStyleButton(stylesComposite, "underline", TextAttribute.UNDERLINE); //$NON-NLS-1$
        mStyleStrikethrough = makeStyleButton(stylesComposite, "strikethrough", TextAttribute.STRIKETHROUGH); //$NON-NLS-1$

        l= new Label(syntaxComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("preview")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Control previewer= createPreviewer(syntaxComposite);
        gd= new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint= convertWidthInCharsToPixels(20);
        gd.heightHint= convertHeightInCharsToPixels(12);
        previewer.setLayoutData(gd);

        return syntaxComposite;
    }

    private void saveStyle(String key, NSISSyntaxStyle style)
    {
        mPreferenceStore.setValue(key, style.toString());
        if(mPreviewer != null && mPreviewer.mustProcessPropertyQueue()) {
            mPreviewer.processPropertyQueue();
        }
    }

    private Control createPreviewer(Composite parent)
    {
        mPreviewer= new NSISSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        NSISTextUtility.hookSourceViewer(mPreviewer);
        SourceViewerConfiguration configuration= new NSISSourceViewerConfiguration(new ChainedPreferenceStore(new IPreferenceStore[]{mPreferenceStore,getPreferenceStore(), EditorsUI.getPreferenceStore()}));
        mPreviewer.configure(configuration);

        InputStream is = null;
        String content= ""; //$NON-NLS-1$
        try {
            is = getClass().getResourceAsStream("NSISPreview.txt"); //$NON-NLS-1$
            content= new String(IOUtility.loadContentFromStream(is));
        }
        catch(Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        finally {
            IOUtility.closeIO(is);
        }
        IDocument document= new Document(content);
        new NSISDocumentSetupParticipant().setup(document);
        mPreviewer.setDocument(document);
        mPreviewer.setEditable(false);

        return mPreviewer.getControl();
    }

    /*
     * @see PreferencePage#createContents(Composite)
     */
    @Override
    protected Control createContents(Composite parent)
    {
        Composite parent2 = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent2.setLayout(layout);

        Link link= new Link(parent2, SWT.NONE);
        link.setText(EclipseNSISPlugin.getResourceString("editor.preferences.note")); //$NON-NLS-1$
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.preferencePages.GeneralTextEditor", null, null); //$NON-NLS-1$
            }
        });

        Group group = new Group(parent2,SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("appearances.group.label")); //$NON-NLS-1$
        layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Composite c = createAppearanceGroup(group);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        group = new Group(parent2,SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("syntax.group.label")); //$NON-NLS-1$
        layout = new GridLayout(1,false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        c = createSyntaxGroup(group);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        group = new Group(parent2,SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("other.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1,false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        c = createOtherGroup(group);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        initialize();
        Dialog.applyDialogFont(parent2);
        return parent2;
    }

    private void initialize() {

        initializeFields();
        mFileAssociationButton.setSelection(FileAssociationChecker.getFileAssociationChecking(FILE_ASSOCIATION_ID));
        mDropExternalFilesCombo.select(NSISPreferences.getInstance().getPreferenceStore().getInt(DROP_EXTERNAL_FILES_ACTION));
        for (int i= 0; i < cSyntaxStyleListModel.length; i++) {
            mSyntaxStyleList.add(cSyntaxStyleListModel[i][0]);
        }
        mSyntaxStyleList.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (mSyntaxStyleList != null && !mSyntaxStyleList.isDisposed()) {
                    mSyntaxStyleList.select(0);
                    handleSyntaxStyleListSelection();
                }
            }
        });
    }

    private void initializeFields()
    {
        Iterator<Button> e= mCheckBoxes.keySet().iterator();
        while (e.hasNext()) {
            Button b= e.next();
            String key= mCheckBoxes.get(b);
            b.setSelection(mPreferenceStore.getBoolean(key));
        }
        mMatchingDelimsColorEditor.setRGB(PreferenceConverter.getColor(mPreferenceStore, MATCHING_DELIMITERS_COLOR));
        mMasterSlaveController.updateSlaves();
        mStyleMap.clear();
    }

    /*
     * @see PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        mPreferenceStore.update();
        NSISEditorUtilities.updatePresentations();
        NSISPreferences.getInstance().getPreferenceStore().setValue(DROP_EXTERNAL_FILES_ACTION,mDropExternalFilesCombo.getSelectionIndex());
        FileAssociationChecker.setFileAssociationChecking(FILE_ASSOCIATION_ID, mFileAssociationButton.getSelection());
        return super.performOk();
    }

    /*
     * @see PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {

        mPreferenceStore.loadDefaults();

        initializeFields();
        mFileAssociationButton.setSelection(true);
        mDropExternalFilesCombo.select(DROP_EXTERNAL_FILES_DEFAULT);
        handleSyntaxStyleListSelection();
        if(mPreviewer != null && mPreviewer.mustProcessPropertyQueue()) {
            mPreviewer.processPropertyQueue();
        }

        super.performDefaults();
    }

    /*
     * @see DialogPage#dispose()
     */
    @Override
    public void dispose() {

        if (mPreferenceStore != null) {
            mPreferenceStore.dispose();
            mPreferenceStore= null;
        }
        super.dispose();
    }

    private Button addCheckBox(Composite parent, String label, String key, int indentation) {
        Button checkBox= new Button(parent, SWT.CHECK);
        checkBox.setText(label);

        GridData gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gd.horizontalIndent= indentation;
        gd.horizontalSpan= 2;
        checkBox.setLayoutData(gd);
        checkBox.addSelectionListener(mCheckBoxListener);

        mCheckBoxes.put(checkBox, key);

        return checkBox;
    }
}
