/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class NSISCommandDialog extends StatusMessageDialog
{
    private static final int MIN_WINDOW_HEIGHT = 400;
    private static final int MIN_WINDOW_WIDTH = 350;
    private static final String SETTING_COLLAPSE_HELP = "collapseHelp"; //$NON-NLS-1$
    private static Map<String, Map<String, Object>> cCommandStateMap;
    private static final Object JOB_FAMILY = new Object();
    private static final String STYLE_LINK;
    private static final String NEW_STYLE_LINK = "<style type=\"text/css\">body { text: #000000; background-color: #FFFFFF} pre { background-color: #F7F7F7} a { color: #567599} a:hover { background-color: #F4F4F4}</style>"; //$NON-NLS-1$

    private NSISCommand mCommand;

    private Control mParamEditorControl;
    private Map<String, Object> mSettings = null;
    private boolean mCollapseHelp = false;
    private IDialogSettings mDialogSettings = null;

    static
    {
        STYLE_LINK = new StringBuffer("<link rel=\"stylesheet\" href=\"").append( //$NON-NLS-1$
                        IOUtility.getFileURLString(NSISBrowserUtility.COLORS_CSS_FILE)).append(
                        "\" charset=\"ISO-8859-1\" type=\"text/css\">").toString();//$NON-NLS-1$
        final File stateLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),
        "net.sf.eclipsensis.help.commands.NSISCommandSettings.ser"); //$NON-NLS-1$
        EclipseNSISPlugin.getDefault().registerService(new IEclipseNSISService() {
            private boolean mStarted = false;

            public void start(IProgressMonitor monitor)
            {
                if (IOUtility.isValidFile(stateLocation))
                {
                    try
                    {
                        cCommandStateMap = IOUtility.readObject(stateLocation);
                    }
                    catch (Exception e)
                    {
                        EclipseNSISPlugin.getDefault().log(e);
                        cCommandStateMap = null;
                    }
                }
                if (cCommandStateMap == null)
                {
                    cCommandStateMap = new HashMap<String, Map<String, Object>>();
                }
                mStarted = true;
            }

            public void stop(IProgressMonitor monitor)
            {
                try
                {
                    IOUtility.writeObject(stateLocation, cCommandStateMap);
                }
                catch (Exception e)
                {
                    EclipseNSISPlugin.getDefault().log(e);
                }
                mStarted = false;
            }

            public boolean isStarted()
            {
                return mStarted;
            }
        });
    }

    private Listener mFilter = new Listener() {
        final JobScheduler jobScheduler = EclipseNSISPlugin.getDefault().getJobScheduler();

        private boolean isChildOf(Control parent, Control child)
        {
            Control child2 = child;
            while (child2 != null)
            {
                if (child2 == parent)
                {
                    return true;
                }
                child2 = child2.getParent();
            }
            return false;
        }

        public void handleEvent(Event event)
        {
            if (event.widget instanceof Control)
            {
                if (mParamEditorControl != null && !mParamEditorControl.isDisposed()
                                && isChildOf(mParamEditorControl, (Control) event.widget))
                {
                    jobScheduler.cancelJobs(JOB_FAMILY);
                    jobScheduler.scheduleUIJob(JOB_FAMILY, "NSISCommandDialog", new IJobStatusRunnable() { //$NON-NLS-1$
                        public IStatus run(IProgressMonitor monitor)
                        {
                            validate();
                            return Status.OK_STATUS;
                        }
                    });
                }
            }
        }
    };
    private INSISParamEditor mParamEditor;
    private NSISCommandResult mResult;
    private IDocument mCommandDoc = new Document();
    private boolean mRemember;
    private BrowserDialogTray mTray = null;

    public NSISCommandDialog(Shell parent, NSISCommand command)
    {
        super(parent);
        mCommand = command;
        setTitle(EclipseNSISPlugin.getResourceString("nsis.command.wizard.title")); //$NON-NLS-1$
        setShellImage(EclipseNSISPlugin.getShellImage());
        mSettings = cCommandStateMap.get(mCommand.getName());
        mRemember = mSettings != null;
        mParamEditor = mCommand.createEditor();
        setParamEditorState();
        IDialogSettings dialogSettings = EclipseNSISPlugin.getDefault().getDialogSettings();
        String name = getClass().getName();
        mDialogSettings = dialogSettings.getSection(name);
        if (mDialogSettings == null)
        {
            mDialogSettings = dialogSettings.addNewSection(name);
        }
        if (mDialogSettings.get(SETTING_COLLAPSE_HELP) != null)
        {
            mCollapseHelp = mDialogSettings.getBoolean(SETTING_COLLAPSE_HELP);
        }
        else
        {
            mCollapseHelp = false;
        }
    }

    @Override
    protected Point getInitialSize()
    {
        Point p = super.getInitialSize();
        if (p.y < MIN_WINDOW_HEIGHT)
        {
            p.y = MIN_WINDOW_HEIGHT;
        }
        int width = mParamEditorControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        Composite parent = mParamEditorControl.getParent();
        while (parent != null)
        {
            Layout layout = parent.getLayout();
            if (layout instanceof GridLayout)
            {
                GridLayout gridLayout = (GridLayout) layout;
                width += 2 * gridLayout.marginWidth;
            }
            parent = parent.getParent();
        }
        width = Math.max(width, MIN_WINDOW_WIDTH);
        return p;
    }

    @Override
    public void create()
    {
        super.create();

        getButton(IDialogConstants.OK_ID).setText(IDialogConstants.FINISH_LABEL);
        if (mParamEditor != null)
        {
            mParamEditor.initEditor();
        }
        validate();

        if (!mCollapseHelp && mTray != null)
        {
            openTray(mTray);
        }
        mParamEditorControl.forceFocus();
    }

    public NSISCommandResult getCommandResult()
    {
        return mResult;
    }

    @Override
    public boolean close()
    {
        mParamEditor = null;
        getShell().getDisplay().removeFilter(SWT.Modify, mFilter);
        getShell().getDisplay().removeFilter(SWT.Selection, mFilter);
        return super.close();
    }

    private void validate()
    {
        if (mParamEditor != null)
        {
            String error = mParamEditor.validate();
            if (error != null)
            {
                updateStatus(new DialogStatus(IStatus.ERROR, error));
                mResult = null;
                mCommandDoc.set(""); //$NON-NLS-1$
            }
            else
            {
                updateStatus(new DialogStatus(IStatus.OK, "")); //$NON-NLS-1$
                mResult = mCommand.getResultPreview(mParamEditor);
                mCommandDoc.set(mResult.getContent());
            }
        }
    }

    @Override
    protected void cancelPressed()
    {
        saveDialogSettings();
        super.cancelPressed();
    }

    private void saveDialogSettings()
    {
        mDialogSettings.put(SETTING_COLLAPSE_HELP, !(getTray() instanceof BrowserDialogTray));
    }

    @Override
    protected void okPressed()
    {
        saveDialogSettings();
        if (mRemember)
        {
            cCommandStateMap.put(mCommand.getName(), mSettings);
        }
        else
        {
            cCommandStateMap.remove(mCommand.getName());
        }
        super.okPressed();
    }

    @Override
    protected Control createHelpControl(Composite parent)
    {
        Control helpControl = super.createHelpControl(parent);
        Listener listener = new Listener() {
            public void handleEvent(Event event)
            {
                DialogTray tray = getTray();
                if (tray instanceof BrowserDialogTray)
                {
                    closeTray();
                    if (getShell() != null)
                    {
                        Control c = getShell().getDisplay().getFocusControl();
                        while (c != null)
                        {
                            if (c.isListening(SWT.Help))
                            {
                                c.notifyListeners(SWT.Help, new Event());
                                break;
                            }
                            c = c.getParent();
                        }
                    }
                }
            }
        };
        if (helpControl instanceof ToolBar)
        {
            ToolItem[] children = ((ToolBar) helpControl).getItems();
            if (children.length > 0)
            {
                children[0].addListener(SWT.Selection, listener);
            }
        }
        else
        {
            helpControl.addListener(SWT.Selection, listener);
        }
        return helpControl;
    }

    @Override
    protected void createControlAndMessageArea(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = layout.verticalSpacing = layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setLayout(new GridLayout(1, false));
        composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Color white = composite2.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        composite2.setBackground(white);

        Label titleHeader = new Label(composite2, SWT.NONE);
        titleHeader.setBackground(white);
        makeBold(titleHeader);
        titleHeader.setText(EclipseNSISPlugin.getResourceString("nsis.command.wizard.title")); //$NON-NLS-1$
        titleHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label titleSubHeader = new Label(composite2, SWT.WRAP);
        titleSubHeader.setBackground(white);
        titleSubHeader.setText(EclipseNSISPlugin.getResourceString("nsis.command.wizard.subtitle")); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.INDENT);
        titleSubHeader.setLayoutData(data);

        Label titleImage = new Label(composite, SWT.NONE);
        titleImage.setBackground(white);
        titleImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        titleImage.setImage(EclipseNSISPlugin.getImageManager().getImage(
                        EclipseNSISPlugin.getResourceString("command.wizard.title.image"))); //$NON-NLS-1$

        Label titleSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 2;
        titleSeparator.setLayoutData(data);

        super.createControlAndMessageArea(parent);
    }

    private Group wrapParamEditorControl(Composite parent)
    {
        Group g = new Group(parent, SWT.NONE);
        g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.marginTop = 5;
        g.setLayout(layout);
        mParamEditorControl.setParent(g);
        mParamEditorControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return g;
    }

    private NSISSourceViewer createPreviewer(Composite parent)
    {
        NSISSourceViewer previewer = new NSISSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL
                        | SWT.BORDER | SWT.WRAP);
        NSISTextUtility.hookSourceViewer(previewer);
        SourceViewerConfiguration configuration = new NSISSourceViewerConfiguration(
                        new ChainedPreferenceStore(new IPreferenceStore[] { NSISPreferences.getInstance().getPreferenceStore(),
                                        EditorsUI.getPreferenceStore() }));
        previewer.configure(configuration);
        new NSISDocumentSetupParticipant().setup(mCommandDoc);
        previewer.setDocument(mCommandDoc);
        previewer.setEditable(false);
        final StyledText textWidget = previewer.getTextWidget();
        textWidget.getCaret().setVisible(false);
        textWidget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                textWidget.setSelectionRange(e.x, 0);
            }
        });
        return previewer;
    }

    @Override
    protected Control createControl(Composite parent)
    {
        final Composite child = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = 0;
        child.setLayout(layout);
        child.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label l = new Label(child, SWT.None);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        l.setText(mCommand.getName());
        makeBold(l);

        ToolBar toolbar = new ToolBar(child, SWT.FLAT);
        ToolItem toolItem = new ToolItem(toolbar, SWT.PUSH);
        toolItem.setImage(EclipseNSISPlugin.getImageManager().getImage(
                        EclipseNSISPlugin.getResourceString("command.help.icon"))); //$NON-NLS-1$
        toolItem.setToolTipText(EclipseNSISPlugin.getResourceString("command.description.tooltip")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        toolbar.setLayoutData(gridData);
        toolbar.setCursor(toolbar.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        final Composite container = new Composite(child, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 2;
        container.setLayoutData(data);
        layout = new GridLayout(1, false);
        layout.marginHeight = layout.marginWidth = 0;
        container.setLayout(layout);
        mParamEditorControl = mParamEditor.createControl(container);
        final Group editorGroup;
        if (mParamEditorControl != null)
        {
            if (mParamEditorControl instanceof Group)
            {
                if (!Common.isEmpty(((Group) mParamEditorControl).getText()))
                {
                    editorGroup = wrapParamEditorControl(container);
                }
                else
                {
                    editorGroup = (Group) mParamEditorControl;
                    mParamEditorControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                    ((GridLayout) editorGroup.getLayout()).marginTop = 5;
                }
            }
            else
            {
                editorGroup = wrapParamEditorControl(container);
            }

            editorGroup.setText(EclipseNSISPlugin.getResourceString("nsis.command.parameters.label")); //$NON-NLS-1$

            if (mParamEditor.hasRequiredFields())
            {
                Composite reqFields = new Composite(editorGroup, SWT.NONE);
                data = new GridData(SWT.FILL, SWT.FILL, true, false);
                data.horizontalSpan = ((GridLayout) editorGroup.getLayout()).numColumns;
                reqFields.setLayoutData(data);
                layout = new GridLayout(1, false);
                layout.marginHeight = layout.marginWidth = 0;
                reqFields.setLayout(layout);
                NSISWizardDialogUtil.createRequiredFieldsLabel(reqFields);
            }

            container.addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent e)
                {
                    Point size = container.getSize();
                    if (editorGroup.isVisible())
                    {
                        e.gc.fillRectangle(0, 0, size.x, size.y);
                    }
                    else
                    {
                        Color fg = e.gc.getForeground();
                        e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
                        String text = EclipseNSISPlugin.getResourceString("revert.message"); //$NON-NLS-1$
                        Point stringSize = e.gc.stringExtent(text);
                        e.gc.drawText(text, (size.x - stringSize.x) / 2, (size.y - stringSize.y) / 2);
                        e.gc.setForeground(fg);
                    }
                }

            });
        }
        else
        {
            editorGroup = null;
        }

        Composite previewComposite = new Composite(child, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalSpan = 2;
        previewComposite.setLayoutData(data);

        layout = new GridLayout(1, false);
        layout.marginHeight = layout.marginWidth = 0;
        layout.marginTop = 5;
        layout.verticalSpacing = 0;
        previewComposite.setLayout(layout);

        l = new Label(previewComposite, SWT.LEFT);
        l.setText(EclipseNSISPlugin.getResourceString("preview")); //$NON-NLS-1$
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        l.setLayoutData(data);

        NSISSourceViewer previewer = createPreviewer(previewComposite);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        Point pt = Common.calculateControlSize(previewer.getControl(), 0, 3);
        data.heightHint = pt.y;
        previewer.getControl().setLayoutData(data);

        Composite buttonPanel = new Composite(child, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 2;
        buttonPanel.setLayoutData(data);
        layout = new GridLayout(3, false);
        layout.marginHeight = layout.marginWidth = 0;
        buttonPanel.setLayout(layout);
        final Button rememberButton = new Button(buttonPanel, SWT.CHECK);
        rememberButton.setText(EclipseNSISPlugin.getResourceString("nsis.command.wizard.remember.label")); //$NON-NLS-1$
        rememberButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        rememberButton.setSelection(mRemember);

        final Button revertButton = new Button(buttonPanel, SWT.PUSH);
        revertButton.setText(EclipseNSISPlugin.getResourceString("revert.label")); //$NON-NLS-1$
        revertButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                    public void run()
                    {
                        if (editorGroup != null)
                        {
                            editorGroup.setVisible(false);
                        }
                        mParamEditor.reset();
                        mParamEditor.initEditor();
                        if (editorGroup != null)
                        {
                            editorGroup.setVisible(true);
                        }
                    }
                });
            }
        });
        setButtonLayoutData(revertButton);
        revertButton.setEnabled(mRemember && !Common.isEmptyMap(mSettings));

        rememberButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mRemember = rememberButton.getSelection();
                setParamEditorState();
                revertButton.setEnabled(mRemember && !Common.isEmptyMap(mSettings));
            }
        });

        Button clearButton = new Button(buttonPanel, SWT.PUSH);
        clearButton.setText(EclipseNSISPlugin.getResourceString("clear.label")); //$NON-NLS-1$
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                    public void run()
                    {
                        mParamEditor.clear();
                    }
                });
            }
        });
        setButtonLayoutData(clearButton);

        if (NSISBrowserUtility.isBrowserAvailable(parent))
        {
            mTray = new BrowserDialogTray();
            mTray.setCurrentCommand(mCommand.getName());
            toolItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    openTray(mTray);
                }
            });
        }
        else
        {
            mCollapseHelp = true;
            toolbar.setVisible(false);
        }

        getShell().getDisplay().addFilter(SWT.Modify, mFilter);
        getShell().getDisplay().addFilter(SWT.Selection, mFilter);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(child,
                        INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_cmdwizard_context"); //$NON-NLS-1$
        return child;
    }

    @Override
    public void openTray(DialogTray tray) throws IllegalStateException, UnsupportedOperationException
    {
        DialogTray oldTray = getTray();
        if (oldTray != null)
        {
            if (!Common.objectsAreEqual(oldTray, tray))
            {
                closeTray();
            }
            else
            {
                return;
            }
        }
        super.openTray(tray);
    }

    private void makeBold(Control control)
    {
        FontData[] f = control.getFont().getFontData();
        for (int i = 0; i < f.length; i++)
        {
            f[i].setStyle(f[i].getStyle() | SWT.BOLD);
        }
        final Font font = new Font(control.getDisplay(), f);
        control.setFont(font);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                font.dispose();
            }
        });
    }

    /**
     *
     */
    private void setParamEditorState()
    {
        if (mRemember)
        {
            if (mSettings == null)
            {
                mSettings = new HashMap<String, Object>();
            }
            mParamEditor.setSettings(mSettings);
        }
        else
        {
            mParamEditor.setSettings(null);
        }
    }

    private class BrowserDialogTray extends DialogTray
    {
        private Browser mBrowser;
        private String mCurrentCommand = null;
        private ToolItem mBack = null;
        private ToolItem mForward = null;
        private Stack<String> mBackCommands = null;
        private Stack<String> mForwardCommands = null;
        private Image mCloseImage = null;
        private Image mCloseHotImage = null;

        private INSISBrowserFileURLHandler mFileURLHandler = new INSISBrowserFileURLHandler() {
            public void handleFile(File file)
            {
                BrowserDialogTray.this.handleFile(file);
            }
        };

        private INSISBrowserKeywordURLHandler mKeywordURLHandler = new INSISBrowserKeywordURLHandler() {
            public void handleKeyword(String keyword)
            {
                gotoCommand(keyword);

            }
        };

        @Override
        protected Control createContents(final Composite parent)
        {
            Composite contents = new Composite(parent, SWT.NONE) {
                @Override
                public Point computeSize(int wHint, int hHint, boolean changed)
                {
                    Point size = super.computeSize(wHint, hHint, changed);
                    if (wHint == SWT.DEFAULT && size.x < MIN_WINDOW_WIDTH)
                    {
                        size.x = MIN_WINDOW_WIDTH;
                    }
                    if (hHint == SWT.DEFAULT && size.y < MIN_WINDOW_HEIGHT)
                    {
                        size.y = MIN_WINDOW_HEIGHT;
                    }
                    return size;
                }
            };
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            contents.setLayoutData(gridData);
            GridLayout layout = new GridLayout(2, false);
            layout.marginHeight = layout.marginWidth = 2;
            contents.setLayout(layout);

            Composite composite = new Composite(contents, SWT.NONE);
            gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            composite.setLayoutData(gridData);
            layout = new GridLayout(2, false);
            layout.marginHeight = layout.marginWidth = 0;
            composite.setLayout(layout);
            Label l = new Label(composite, SWT.NONE);
            makeBold(l);
            l.setText(EclipseNSISPlugin.getResourceString("nsis.command.description.label")); //$NON-NLS-1$
            gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.horizontalIndent = 2;
            l.setLayoutData(gridData);
            createToolBar(composite);
            mBrowser = new Browser(composite, SWT.BORDER);
            gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.horizontalSpan = 2;
            initializeDialogUnits(mBrowser);
            gridData.heightHint = convertHeightInCharsToPixels(10);
            mBrowser.setLayoutData(gridData);
            mBrowser.setMenu(new Menu(getShell(), SWT.NONE));
            if (mCurrentCommand != null)
            {
                String help = getHelp(mCurrentCommand);
                mBrowser.setText(help);
            }
            hookLocationListener();
            return contents;
        }

        private boolean isValid(Image image)
        {
            return image != null && !image.isDisposed();
        }

        private void createCloseImages()
        {
            Display display = Display.getCurrent();
            int[] shape = new int[] { 1, 2, 3, 1, 5, 3, 6, 3, 8, 1, 10, 1, 10, 3, 8, 5, 8, 6, 10, 8, 10, 10, 8, 10, 6,
                            8, 5, 8, 3, 10, 1, 10, 1, 8, 3, 6, 3, 5, 1, 3 };

            /*
             * Use magenta as transparency color since it is used infrequently.
             */
            Color border = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            Color background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
            Color backgroundHot = new Color(display, new RGB(252, 160, 160));
            Color transparent = display.getSystemColor(SWT.COLOR_MAGENTA);

            PaletteData palette = new PaletteData(new RGB[] { transparent.getRGB(), border.getRGB(),
                            background.getRGB(), backgroundHot.getRGB() });
            ImageData data = new ImageData(12, 12, 8, palette);
            data.transparentPixel = 0;

            mCloseImage = new Image(display, data);
            mCloseImage.setBackground(transparent);
            GC gc = new GC(mCloseImage);
            gc.setBackground(background);
            gc.fillPolygon(shape);
            gc.setForeground(border);
            gc.drawPolygon(shape);
            gc.dispose();

            mCloseHotImage = new Image(display, data);
            mCloseHotImage.setBackground(transparent);
            gc = new GC(mCloseHotImage);
            gc.setBackground(backgroundHot);
            gc.fillPolygon(shape);
            gc.setForeground(border);
            gc.drawPolygon(shape);
            gc.dispose();

            backgroundHot.dispose();
        }

        private void createToolBar(Composite displayArea)
        {
            if (isValid(NSISBrowserUtility.BACK_IMAGE) && isValid(NSISBrowserUtility.DISABLED_BACK_IMAGE)
                            && isValid(NSISBrowserUtility.FORWARD_IMAGE) && isValid(NSISBrowserUtility.DISABLED_FORWARD_IMAGE)
                            && isValid(NSISBrowserUtility.HOME_IMAGE))
            {

                ToolBar toolBar = new ToolBar(displayArea, SWT.FLAT);
                toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

                // Add a button to go back to original page
                final ToolItem home = new ToolItem(toolBar, SWT.PUSH);
                home.setImage(NSISBrowserUtility.HOME_IMAGE);
                home.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.home.text")); //$NON-NLS-1$

                // Add a button to navigate backwards through previously visited
                // pages
                mBack = new ToolItem(toolBar, SWT.PUSH);
                mBack.setImage(NSISBrowserUtility.BACK_IMAGE);
                mBack.setDisabledImage(NSISBrowserUtility.DISABLED_BACK_IMAGE);
                mBack.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.back.text")); //$NON-NLS-1$

                // Add a button to navigate forward through previously visited
                // pages
                mForward = new ToolItem(toolBar, SWT.PUSH);
                mForward.setImage(NSISBrowserUtility.FORWARD_IMAGE);
                mForward.setDisabledImage(NSISBrowserUtility.DISABLED_FORWARD_IMAGE);
                mForward.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.forward.text")); //$NON-NLS-1$

                Listener listener = new Listener() {
                    public void handleEvent(Event event)
                    {
                        ToolItem item = (ToolItem) event.widget;
                        if (item == home)
                        {
                            if (!Common.isEmptyCollection(mBackCommands))
                            {
                                String oldKeyword = mCurrentCommand;
                                String keyword = mBackCommands.firstElement();
                                if (!Common.stringsAreEqual(oldKeyword, keyword))
                                {
                                    if (setCurrentCommand(keyword) && oldKeyword != null)
                                    {
                                        mForwardCommands.clear();
                                        mBackCommands.push(oldKeyword);
                                    }
                                }
                            }
                            updateToolbarButtons();
                        }
                        else if (item == mBack)
                        {
                            if (!Common.isEmptyCollection(mBackCommands))
                            {
                                String oldKeyword = mCurrentCommand;
                                String keyword = mBackCommands.pop();
                                if (setCurrentCommand(keyword) && oldKeyword != null)
                                {
                                    mForwardCommands.push(oldKeyword);
                                }
                            }
                            updateToolbarButtons();
                        }
                        else if (item == mForward)
                        {
                            if (!Common.isEmptyCollection(mForwardCommands))
                            {
                                String oldKeyword = mCurrentCommand;
                                String keyword = mForwardCommands.pop();
                                if (setCurrentCommand(keyword) && oldKeyword != null)
                                {
                                    mBackCommands.push(oldKeyword);
                                }
                            }
                            updateToolbarButtons();
                        }
                    }
                };
                home.addListener(SWT.Selection, listener);
                mBack.addListener(SWT.Selection, listener);
                mForward.addListener(SWT.Selection, listener);

                createCloseImages();
                toolBar.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e)
                    {
                        mCloseImage.dispose();
                        mCloseHotImage.dispose();
                    }
                });
                ToolItem close = new ToolItem(toolBar, SWT.PUSH);
                close.setImage(mCloseImage);
                close.setHotImage(mCloseHotImage);
                close.setToolTipText(EclipseNSISPlugin.getResourceString("close.tooltip")); //$NON-NLS-1$
                close.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event)
                    {
                        closeTray();
                    }
                });
                mBackCommands = new Stack<String>();
                mForwardCommands = new Stack<String>();
                updateToolbarButtons();
            }
        }

        private void gotoCommand(String command)
        {
            String oldCommand = mCurrentCommand;
            if (setCurrentCommand(command))
            {
                if (oldCommand != null && mBackCommands != null)
                {
                    mBackCommands.push(oldCommand);
                }
                if (mForwardCommands != null)
                {
                    mForwardCommands.clear();
                }
                updateToolbarButtons();
            }
        }

        private String getHelp(String command)
        {
            String help = NSISHelpURLProvider.getInstance().getKeywordHelp(command);
            if (!Common.isEmpty(help))
            {
                help = fixStyle(help);
            }
            return help;
        }

        private String fixStyle(String text)
        {
            String text2 = text;
            int n = text2.indexOf(STYLE_LINK);
            if (n >= 0)
            {
                text2 = new StringBuffer(text2.substring(0, n)).append(NEW_STYLE_LINK).append(
                                text2.substring(n + STYLE_LINK.length())).toString();
            }
            return text2;
        }

        private boolean setCurrentCommand(String command)
        {
            String help = getHelp(command);
            if (!Common.isEmpty(help))
            {
                mCurrentCommand = command;
                if (mBrowser != null)
                {
                    mBrowser.setText(help);
                }
                return true;
            }
            return false;
        }

        private void updateToolbarButtons()
        {
            if (mBack != null)
            {
                mBack.setEnabled(!Common.isEmptyCollection(mBackCommands));
            }
            if (mForward != null)
            {
                mForward.setEnabled(!Common.isEmptyCollection(mForwardCommands));
            }
        }

        private void hookLocationListener()
        {
            if (mBrowser != null && !mBrowser.isDisposed())
            {
                mBrowser.addLocationListener(new LocationAdapter() {
                    @Override
                    public void changing(LocationEvent event)
                    {
                        if (!NSISBrowserUtility.ABOUT_BLANK.equalsIgnoreCase(event.location))
                        {
                            try
                            {
                                NSISBrowserUtility.handleURL(event.location, mKeywordURLHandler, mFileURLHandler);
                            }
                            finally
                            {
                                event.doit = false;
                            }
                        }
                    }
                });
            }
        }

        private void handleFile(File f)
        {
            if (IOUtility.isValidDirectory(f))
            {
                try
                {
                    Program.launch(f.getCanonicalPath());
                }
                catch (IOException e)
                {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else
            {
                String home = "";
                NSISPreferences prefs = NSISPreferences.getInstance();
                if(prefs.getNSISHome() != null)
                {
                    home = prefs.getNSISHome().getLocation().getAbsolutePath();
                }
                if (home != null)
                {
                    try
                    {
                        if (f.getCanonicalPath().regionMatches(true, 0, home, 0, home.length()))
                        {
                            String ext = IOUtility.getFileExtension(f);
                            if (NSISBrowserUtility.HTML_EXTENSIONS != null
                                            && NSISBrowserUtility.HTML_EXTENSIONS.contains(ext))
                            {
                                NSISHTMLHelp.showHelp(IOUtility.getFileURLString(f));
                                return;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
                try
                {
                    Common.openExternalBrowser(IOUtility.getFileURLString(f));
                }
                catch (Exception e)
                {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }
}
