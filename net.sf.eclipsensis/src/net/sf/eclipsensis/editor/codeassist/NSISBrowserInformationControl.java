/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.jdt.internal.ui.text.hover.BrowserInformationControl
 * Copyright (c) 2000-2010 IBM Corporation and others.
 *
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.io.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISExternalFileEditorInput;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.bindings.keys.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class NSISBrowserInformationControl implements IInformationControl, IInformationControlExtension,
IInformationControlExtension2, IInformationControlExtension3, DisposeListener
{
    private static final int BORDER = 1;
    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 250;
    private static final int MIN_WIDTH = 450;
    private static final int MIN_HEIGHT = 225;

    private Shell mShell;
    private ToolBar mToolBar;
    private Browser mBrowser;
    private boolean mBrowserHasContent;
    private int mMaxWidth = -1;
    private int mMaxHeight = -1;
    private boolean mHideScrollBars;
    private Listener mDeactivateListener;
    private ListenerList mFocusListeners = new ListenerList(ListenerList.IDENTITY);
    private String mKeyword = null;
    private ToolItem mBack = null;
    private ToolItem mForward = null;
    private ToolItem mHelp = null;
    private Stack<String> mBackKeywords = null;
    private Stack<String> mForwardKeywords = null;
    private boolean mCompleted = false;

    private INSISBrowserFileURLHandler mFileURLHandler = new INSISBrowserFileURLHandler() {
        public void handleFile(File file)
        {
            NSISBrowserInformationControl.this.handleFile(file);
        }
    };

    private INSISBrowserKeywordURLHandler mKeywordURLHandler = new INSISBrowserKeywordURLHandler() {
        public void handleKeyword(String keyword)
        {
            NSISBrowserInformationControl.this.gotoKeyword(keyword);

        }
    };

    public NSISBrowserInformationControl(final Shell parent, int shellStyle, int style)
    {
        GridLayout layout;

        mShell = new Shell(parent, SWT.TOOL | SWT.NO_FOCUS | SWT.ON_TOP | shellStyle);
        Display display = mShell.getDisplay();
        layout = new GridLayout(1, false);
        layout.marginHeight = layout.marginWidth = (shellStyle & SWT.NO_TRIM) == 0 ? 0 : BORDER;
        mShell.setLayout(layout);
        mShell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite composite = new Composite(mShell, SWT.NONE);
        layout = new GridLayout(3, false);
        layout.marginHeight = layout.marginWidth = layout.verticalSpacing = 0;

        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        // Browser field
        mBrowser = new Browser(composite, SWT.NONE);
        mHideScrollBars = (style & SWT.V_SCROLL) == 0 && (style & SWT.H_SCROLL) == 0;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 3;
        mBrowser.setLayoutData(gd);
        mBrowser.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        mBrowser.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        mBrowser.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e)
            {
                if (e.character == 0x1B)
                {// ESC
                    if (mShell != null && !mShell.isDisposed())
                    {
                        mShell.dispose();
                    }
                }
            }

            public void keyReleased(KeyEvent e)
            {
            }
        });
        hookLocationListener();
        // Replace browser's built-in context menu with none
        mBrowser.setMenu(new Menu(mShell, SWT.NONE));

        String statusText = null;
        boolean helpAvailable = NSISHelpURLProvider.getInstance().isNSISHelpAvailable();
        if (helpAvailable)
        {
            ParameterizedCommand command = NSISInformationUtility.getCommand(INSISConstants.GOTO_HELP_COMMAND_ID);
            List<KeySequence> list = new ArrayList<KeySequence>();
            List<int[]> list2 = new ArrayList<int[]>();
            if (command != null)
            {
                KeySequence[] sequences = NSISInformationUtility.getKeySequences(command);
                if (!Common.isEmptyArray(sequences))
                {
                    for (int i = 0; i < sequences.length; i++)
                    {
                        KeyStroke[] strokes = sequences[i].getKeyStrokes();
                        if (!Common.isEmptyArray(strokes) && strokes.length == 1)
                        {
                            list.add(sequences[i]);
                            list2.add(new int[] { strokes[0].getNaturalKey(), strokes[0].getModifierKeys() });
                        }
                    }
                }
                final int[][] keys = list2.toArray(new int[list2.size()][]);
                try
                {
                    statusText = NSISInformationUtility.buildStatusText(command.getCommand().getDescription(),
                                    list.toArray(new KeySequence[list.size()]));
                }
                catch (Exception e)
                {
                    statusText = null;
                }

                mBrowser.addKeyListener(new KeyListener() {
                    public void keyPressed(KeyEvent e)
                    {
                        if (!Common.isEmpty(mKeyword))
                        {
                            for (int i = 0; i < keys.length; i++)
                            {
                                if (e.keyCode == keys[i][0] && (e.stateMask & keys[i][1]) == e.stateMask)
                                {
                                    gotoHelp();
                                }
                            }
                        }
                    }

                    public void keyReleased(KeyEvent e)
                    {
                    }
                });
            }
        }

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
        separator.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 3;
        separator.setLayoutData(gd);

        createToolBar(composite, helpAvailable);
        if (statusText == null)
        {
            ((GridData) mToolBar.getLayoutData()).horizontalSpan = 2;
        }
        else
        {
            Label l = new Label(composite, SWT.NONE);
            FontData[] fd = l.getFont().getFontData();
            for (int i = 0; i < fd.length; i++)
            {
                fd[i].height *= 0.9;
            }
            final Font f = new Font(l.getDisplay(), fd);
            l.setFont(f);
            l.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    f.dispose();
                }
            });
            l.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            l.setText(statusText);
            gd = new GridData(SWT.END, SWT.CENTER, false, false);
            l.setLayoutData(gd);
        }
        Composite resizer = new Composite(composite, SWT.H_SCROLL | SWT.V_SCROLL);
        gd = new GridData(SWT.END, SWT.END, false, false);
        gd.widthHint = 0;
        gd.heightHint = 0;
        resizer.setLayoutData(gd);
        addDisposeListener(this);
    }

    public Shell getShell()
    {
        return mShell;
    }

    private void gotoHelp()
    {
        String keyword = mKeyword;
        Display.getDefault().asyncExec(new Runnable() {
            public void run()
            {
                if (mShell != null && !mShell.isDisposed())
                {
                    mShell.dispose();
                }
            }
        });
        NSISHelpURLProvider.getInstance().showHelpURL(keyword);
    }

    private boolean isValid(Image image)
    {
        return image != null && !image.isDisposed();
    }

    private void createToolBar(Composite displayArea, boolean helpAvailable)
    {
        if (isValid(NSISBrowserUtility.BACK_IMAGE) && isValid(NSISBrowserUtility.DISABLED_BACK_IMAGE)
                        && isValid(NSISBrowserUtility.FORWARD_IMAGE) && isValid(NSISBrowserUtility.DISABLED_FORWARD_IMAGE)
                        && isValid(NSISBrowserUtility.HOME_IMAGE))
        {
            mToolBar = new ToolBar(displayArea, SWT.FLAT);
            GridData data = new GridData(SWT.LEFT, SWT.FILL, true, false);
            mToolBar.setLayoutData(data);

            // Add a button to navigate backwards through previously visited
            // pages
            mBack = new ToolItem(mToolBar, SWT.NONE);
            mBack.setImage(NSISBrowserUtility.BACK_IMAGE);
            mBack.setDisabledImage(NSISBrowserUtility.DISABLED_BACK_IMAGE);
            mBack.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.back.text")); //$NON-NLS-1$

            // Add a button to navigate forward through previously visited pages
            mForward = new ToolItem(mToolBar, SWT.NONE);
            mForward.setImage(NSISBrowserUtility.FORWARD_IMAGE);
            mForward.setDisabledImage(NSISBrowserUtility.DISABLED_FORWARD_IMAGE);
            mForward.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.forward.text")); //$NON-NLS-1$

            // Add a button to go back to original page
            final ToolItem home = new ToolItem(mToolBar, SWT.NONE);
            home.setImage(NSISBrowserUtility.HOME_IMAGE);
            home.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.home.text")); //$NON-NLS-1$

            if (helpAvailable)
            {
                mHelp = new ToolItem(mToolBar, SWT.NONE);
                mHelp.setImage(NSISBrowserUtility.HTMLHELP_IMAGE);
                mHelp.setDisabledImage(NSISBrowserUtility.HTMLHELP_DISABLED_IMAGE);
                mHelp.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.htmlhelp.text")); //$NON-NLS-1$
            }
            else
            {
                mHelp = null;
            }

            Listener listener = new Listener() {
                public void handleEvent(Event event)
                {
                    ToolItem item = (ToolItem) event.widget;
                    if (item == home)
                    {
                        if (!Common.isEmptyCollection(mBackKeywords))
                        {
                            String oldKeyword = mKeyword;
                            String keyword = mBackKeywords.firstElement();
                            if (!Common.stringsAreEqual(oldKeyword, keyword))
                            {
                                if (setKeyword(keyword) && oldKeyword != null)
                                {
                                    mForwardKeywords.clear();
                                    mBackKeywords.push(oldKeyword);
                                }
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (item == mBack)
                    {
                        if (!Common.isEmptyCollection(mBackKeywords))
                        {
                            String oldKeyword = mKeyword;
                            String keyword = mBackKeywords.pop();
                            if (setKeyword(keyword) && oldKeyword != null)
                            {
                                mForwardKeywords.push(oldKeyword);
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (item == mForward)
                    {
                        if (!Common.isEmptyCollection(mForwardKeywords))
                        {
                            String oldKeyword = mKeyword;
                            String keyword = mForwardKeywords.pop();
                            if (setKeyword(keyword) && oldKeyword != null)
                            {
                                mBackKeywords.push(oldKeyword);
                            }
                        }
                        updateToolbarButtons();
                    }
                    else if (mHelp != null && item == mHelp)
                    {
                        gotoHelp();
                    }
                }
            };
            home.addListener(SWT.Selection, listener);
            mBack.addListener(SWT.Selection, listener);
            mForward.addListener(SWT.Selection, listener);
            mHelp.addListener(SWT.Selection, listener);

            mBackKeywords = new Stack<String>();
            mForwardKeywords = new Stack<String>();
            updateToolbarButtons();
        }
    }

    private void gotoKeyword(String keyword)
    {
        String oldKeyword = mKeyword;
        if (setKeyword(keyword))
        {
            if (oldKeyword != null && mBackKeywords != null)
            {
                mBackKeywords.push(oldKeyword);
            }
            if (mForwardKeywords != null)
            {
                mForwardKeywords.clear();
            }
            updateToolbarButtons();
        }
    }

    private boolean setKeyword(String keyword)
    {
        String help = NSISHelpURLProvider.getInstance().getKeywordHelp(keyword);
        if (!Common.isEmpty(help))
        {
            mKeyword = keyword;
            mBrowser.setText(help);
            updateToolbarButtons();
            return true;
        }
        return false;
    }

    private void updateToolbarButtons()
    {
        if (mBack != null)
        {
            mBack.setEnabled(!Common.isEmptyCollection(mBackKeywords));
        }
        if (mForward != null)
        {
            mForward.setEnabled(!Common.isEmptyCollection(mForwardKeywords));
        }
        if (mHelp != null)
        {
            mHelp.setEnabled(!Common.isEmpty(mKeyword));
        }
    }

    public void addKeyListener(KeyListener listener)
    {
        if (mBrowser != null)
        {
            mBrowser.addKeyListener(listener);
        }
    }

    public void removeKeyListener(KeyListener listener)
    {
        if (mBrowser != null)
        {
            mBrowser.removeKeyListener(listener);
        }
    }

    public void setInput(Object input)
    {
        if (input instanceof String)
        {
            setInformation((String) input);
        }
        else if (input instanceof NSISBrowserInformation)
        {
            NSISBrowserInformation info = (NSISBrowserInformation) input;
            setInformation(info.getContent());
            mKeyword = info.getKeyword();
            updateToolbarButtons();
        }
        else if (input instanceof INSISKeywordInformation)
        {
            setInput(new NSISBrowserInformationProvider()
            .getInformation(((INSISKeywordInformation) input).getKeyword()));
        }
        else if (input instanceof INSISInformation)
        {
            setInformation(((INSISInformation) input).getContent());
        }
    }

    public void setInformation(String content)
    {
        String content2 = content;
        mKeyword = null;
        mBrowserHasContent = content2 != null && content2.length() > 0;

        if (mBrowserHasContent)
        {
            int shellStyle = mShell.getStyle();
            boolean RTL = (shellStyle & SWT.RIGHT_TO_LEFT) != 0;

            String[] styles = null;
            if (RTL && !mHideScrollBars)
            {
                styles = new String[] { "direction:rtl" }; //$NON-NLS-1$
            }
            else if (RTL && mHideScrollBars)
            {
                styles = new String[] { "direction:rtl", "overflow:hidden" }; //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if (mHideScrollBars)
            {
                styles = new String[] { "overflow:hidden" }; //$NON-NLS-1$
            }

            if (styles != null)
            {
                StringBuffer buffer = new StringBuffer(content2);
                insertStyles(buffer, styles);
                content2 = buffer.toString();
            }
        }

        mBrowser.setText(content2);
        mBrowser.setSize(Math.min(200, mMaxWidth), Math.min(mMaxHeight, 50));
    }

    private void insertStyles(StringBuffer buffer, String[] styles)
    {
        if (Common.isEmptyArray(styles))
        {
            return;
        }

        StringBuffer styleBuf = new StringBuffer(" style=\"").append(styles[0]); //$NON-NLS-1$
        for (int i = 1; i < styles.length; i++)
        {
            styleBuf.append("; ").append(styles[i]); //$NON-NLS-1$
        }
        styleBuf.append('"');

        // Find insertion index
        int index = buffer.indexOf("<body "); //$NON-NLS-1$
        if (index == -1)
        {
            index = buffer.indexOf("<body>"); //$NON-NLS-1$
            if (index == -1)
            {
                return;
            }
        }

        buffer.insert(index + 5, styleBuf);
    }

    public void setVisible(boolean visible)
    {
        if (mShell == null || mShell.isVisible() == visible)
        {
            return;
        }

        if (!visible)
        {
            mShell.setVisible(false);
            return;
        }

        final Display display = mShell.getDisplay();

        // Make sure the display wakes from sleep after timeout:
        display.timerExec(100, new Runnable() {
            public void run()
            {
                mCompleted = true;
            }
        });

        while (!mCompleted)
        {
            // Drive the event loop to process the events required to load the
            // browser widget's contents:
            if (!display.readAndDispatch())
            {
                display.sleep();
            }
        }

        if (mShell == null || mShell.isDisposed())
        {
            return;
        }

        /*
         * Avoids flickering when replacing hovers, especially on Vista in
         * ON_CLICK mode. Causes flickering on GTK. Carbon does not care.
         */
        mShell.moveAbove(null);

        mShell.setVisible(visible);
        if (visible)
        {
            setFocus();
        }
    }

    public void dispose()
    {
        if (mShell != null && !mShell.isDisposed())
        {
            mShell.dispose();
        }
        else
        {
            widgetDisposed(null);
        }
    }

    public void widgetDisposed(DisposeEvent event)
    {
        mShell = null;
        mBrowser = null;
        mKeyword = null;
    }

    public void setSize(int width, int height)
    {
        mShell.setSize(Math.max(MIN_WIDTH, Math.min(width, mMaxWidth)), Math.max(MIN_HEIGHT, Math.min(height,
                        mMaxHeight)));
    }

    public void setLocation(Point location)
    {
        Rectangle trim = mShell.computeTrim(0, 0, 0, 0);
        Point browserLoc = mBrowser.getLocation();
        Point location2 = mToolBar == null ? browserLoc : mToolBar.getLocation();
        location.x += trim.x - browserLoc.x;
        location.y += trim.y - location2.y;
        mShell.setLocation(location);
    }

    public void setSizeConstraints(int maxWidth, int maxHeight)
    {
        mMaxWidth = Math.min(MAX_WIDTH, maxWidth);
        mMaxHeight = Math.min(MAX_HEIGHT, maxHeight);
    }

    public Point computeSizeHint()
    {
        return mShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    public Rectangle computeTrim()
    {
        return mShell.computeTrim(0, 0, 0, 0);
    }

    public Rectangle getBounds()
    {
        return mShell.getBounds();
    }

    public boolean restoresLocation()
    {
        return false;
    }

    public boolean restoresSize()
    {
        return false;
    }

    public void addDisposeListener(DisposeListener listener)
    {
        mShell.addDisposeListener(listener);
    }

    public void removeDisposeListener(DisposeListener listener)
    {
        mShell.removeDisposeListener(listener);
    }

    public void setForegroundColor(Color foreground)
    {
        mBrowser.setForeground(foreground);
    }

    public void setBackgroundColor(Color background)
    {
        mBrowser.setBackground(background);
    }

    public boolean isFocusControl()
    {
        return mBrowser.isFocusControl();
    }

    public void setFocus()
    {
        mShell.forceFocus();
        mBrowser.setFocus();
    }

    public void addFocusListener(final FocusListener listener)
    {
        mBrowser.addFocusListener(listener);

        if (mFocusListeners.isEmpty())
        {
            mDeactivateListener = new Listener() {
                public void handleEvent(Event event)
                {
                    Object[] listeners = mFocusListeners.getListeners();
                    for (int i = 0; i < listeners.length; i++)
                    {
                        ((FocusListener) listeners[i]).focusLost(new FocusEvent(event));
                    }
                }
            };
            mBrowser.getShell().addListener(SWT.Deactivate, mDeactivateListener);
        }
        mFocusListeners.add(listener);
    }

    public void removeFocusListener(FocusListener listener)
    {
        mBrowser.removeFocusListener(listener);

        mFocusListeners.remove(listener);
        if (mFocusListeners.isEmpty())
        {
            mBrowser.getShell().removeListener(SWT.Deactivate, mDeactivateListener);
            mDeactivateListener = null;
        }
    }

    public boolean hasContents()
    {
        return mBrowserHasContent;
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
        if (!HelpBrowserLocalFileHandler.INSTANCE.handle(f))
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
                            else
                            {
                                if (openInEditor(f))
                                {
                                    return;
                                }
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

    private boolean openInEditor(File file)
    {
        IEditorInput editorInput;
        IEditorDescriptor descriptor;
        IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
        IFile ifile = null;
        IFile[] ifiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI());
        if (!Common.isEmptyArray(ifiles))
        {
            ifile = ifiles[0];
        }
        if (ifile != null)
        {
            editorInput = new FileEditorInput(ifile);
            descriptor = registry.getDefaultEditor(ifile.getName());
        }
        else
        {
            editorInput = new NSISExternalFileEditorInput(file);
            descriptor = registry.getDefaultEditor(file.getName());
        }
        if (descriptor != null)
        {
            try
            {
                IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), editorInput,
                                descriptor.getId());
                return true;
            }
            catch (PartInitException e)
            {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
        return false;
    }
}
