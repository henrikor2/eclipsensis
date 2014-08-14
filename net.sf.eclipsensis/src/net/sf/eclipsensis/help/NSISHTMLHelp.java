/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.*;
import java.io.InputStream;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.*;
import net.sf.eclipsensis.help.NSISHelpIndex.*;
import net.sf.eclipsensis.help.NSISHelpTOC.NSISHelpTOCNode;
import net.sf.eclipsensis.help.search.*;
import net.sf.eclipsensis.help.search.parser.NSISHelpSearchQueryParser;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.apache.lucene.search.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class NSISHTMLHelp extends ViewPart implements INSISConstants
{
    public static final String ECLIPSENSIS_URI_SCHEME = "eclipsensis:"; //$NON-NLS-1$
    public static final String FILE_URI_SCHEME = "file:"; //$NON-NLS-1$
    private static final Pattern cFileUriPattern = Pattern.compile("^file:///?(.*)$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static final Pattern cW3CFileUriPattern = Pattern.compile("^file://([^/].*)?$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static String cFirstPage = null;
    private static final String IMAGE_LOCATION_FORMAT = EclipseNSISPlugin
            .getResourceString("help.browser.throbber.icon.format"); //$NON-NLS-1$
    private static final int IMAGE_COUNT = Integer.parseInt(EclipseNSISPlugin
            .getResourceString("help.browser.throbber.icon.count")); //$NON-NLS-1$
    private static final String HIGHLIGHT_JS_CONTENT;
    private static final String ENCODING_SCHEME = System.getProperty("file.encoding"); //$NON-NLS-1$

    private boolean mShowNav;
    private boolean mSynched;

    private Browser mBrowser;
    private ProgressBar mProgressBar;
    private Label mStatusText;
    private Canvas mThrobber;

    private Image[] mThrobberImages;
    private ToolItem mBackButton;
    private ToolItem mForwardButton;
    private ToolItem mHomeButton;
    private ToolItem mStopButton;
    private ToolItem mRefreshButton;
    private ToolItem mShowHideNavButton;
    private ToolItem mSynchedButton;

    private String mStartPage;
    private int mThrobberImageIndex;
    private boolean mBusy;
    private INSISHelpURLListener mHelpURLListener = new INSISHelpURLListener() {
        public void helpURLsChanged()
        {
            Runnable runnable = new Runnable() {
                public void run()
                {
                    String location = null;
                    ISelection sel = mContentsViewer.getSelection();
                    if (sel != null && !sel.isEmpty())
                    {
                        NSISHelpTOCNode node = (NSISHelpTOCNode) ((IStructuredSelection) sel).getFirstElement();
                        NSISHelpTOC toc = NSISHelpURLProvider.getInstance().getCachedHelpTOC();
                        NSISHelpTOCNode node2 = toc.getNode(node.getName());
                        if (node2 != null)
                        {
                            location = node2.getURL();
                        }
                        else
                        {
                            node2 = toc.getNode(node.getURL());
                            if (node2 != null)
                            {
                                location = node2.getURL();
                            }
                        }
                    }
                    if (location == null)
                    {
                        location = mBrowser.getUrl();
                        if (location != null)
                        {
                            try
                            {
                                URI uri = new URI(location);
                                try
                                {
                                    new File(uri);
                                }
                                catch (IllegalArgumentException iae)
                                {
                                    try
                                    {
                                        location = uri.toURL().toString();
                                    }
                                    catch (MalformedURLException e)
                                    {
                                        location = null;
                                    }
                                }
                            }
                            catch (URISyntaxException e)
                            {
                                location = null;
                            }
                        }
                        if (location != null)
                        {
                            File f = new File(location);
                            if (f.exists())
                            {
                                location = IOUtility.getFileURLString(f);
                            }
                            else
                            {
                                location = null;
                            }
                        }
                    }
                    if (location != null)
                    {
                        cFirstPage = location;
                    }
                    init();
                }
            };
            if (Display.getCurrent() != null)
            {
                runnable.run();
            }
            else
            {
                Display.getDefault().syncExec(runnable);
            }
        }
    };
    private TreeViewer mContentsViewer;
    private SashForm mSashForm;
    private ToolItem mSeparator;
    private ListViewer mIndexViewer;
    private TableViewer mSearchViewer;
    private TabFolder mNavigationPane;
    private ToolBar mToolBar;
    private boolean mNonUserChange = false;
    private SearchingDialog mSearchingDialog;
    private MessageFormat mHighlightJSPrefix = null;
    private String mHighlightJS = null;

    static
    {
        String highlighJSContent = null;
        InputStream is = null;
        try
        {
            is = NSISHTMLHelp.class.getResourceAsStream("highlight.js"); //$NON-NLS-1$
            highlighJSContent = new String(IOUtility.loadContentFromStream(is));
        }
        catch (Exception ex)
        {
            highlighJSContent = null;
            EclipseNSISPlugin.getDefault().log(ex);
        }
        finally
        {
            IOUtility.closeIO(is);
        }
        HIGHLIGHT_JS_CONTENT = highlighJSContent;
    }

    public static boolean showHelp(final String url)
    {
        final boolean[] result = { false };
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run()
            {
                try
                {
                    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    NSISHTMLHelp htmlHelp = (NSISHTMLHelp) activePage.findView(HTMLHELP_ID);
                    if (htmlHelp == null)
                    {
                        cFirstPage = url;
                        htmlHelp = (NSISHTMLHelp) activePage.showView(HTMLHELP_ID);
                        result[0] = htmlHelp.isActivated();
                    }
                    else
                    {
                        activePage.activate(htmlHelp);
                        result[0] = htmlHelp.isActivated();
                        if (result[0])
                        {
                            cFirstPage = url;
                            htmlHelp.openHelp();
                        }
                    }
                }
                catch (PartInitException pie)
                {
                    result[0] = false;
                    EclipseNSISPlugin.getDefault().log(pie);
                }
            }
        });
        return result[0];
    }

    public NSISHTMLHelp()
    {
        if (HIGHLIGHT_JS_CONTENT != null)
        {
            mHighlightJSPrefix = new MessageFormat("var keywords = new Array({0});\r\nvar regex = new Array({1});\r\n"); //$NON-NLS-1$
        }
    }

    private Browser getBrowser()
    {
        return mBrowser;
    }

    private boolean isActivated()
    {
        return getBrowser() != null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        mShowNav = NSISPreferences.getInstance().getBoolean(INSISPreferenceConstants.NSIS_HELP_VIEW_SHOW_NAV);
        mSynched = NSISPreferences.getInstance().getBoolean(INSISPreferenceConstants.NSIS_HELP_VIEW_SYNCHED);

        initResources();

        mSashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        createNavigationPane(mSashForm);
        Composite composite = new Composite(mSashForm, SWT.NONE);
        composite.setLayout(new FillLayout());
        mSashForm.setWeights(new int[] { 1, 3 });
        try
        {
            mBrowser = new Browser(composite, SWT.BORDER);
        }
        catch (SWTError e)
        {
            mBrowser = null;
            if (mSashForm != null)
            {
                mSashForm.dispose();
            }
            parent.setLayout(new FillLayout());
            Label label = new Label(parent, SWT.CENTER | SWT.WRAP);
            label.setText(EclipseNSISPlugin.getResourceString("help.browser.create.error")); //$NON-NLS-1$
            parent.layout(true);
            return;
        }

        parent.setLayout(new FormLayout());
        createToolBar(parent);
        createThrobber(parent);
        createStatusArea(parent);
        Label l = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(mThrobber, 5, SWT.DEFAULT);
        l.setLayoutData(data);

        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.top = new FormAttachment(l, 0, SWT.DEFAULT);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(mStatusText, -5, SWT.DEFAULT);
        mSashForm.setLayoutData(data);

        mBrowser.addLocationListener(new LocationListener() {
            public void changed(LocationEvent event)
            {
                String location = event.location;
                synch(location);
            }

            public void changing(LocationEvent event)
            {
                if (!Common.isEmpty(event.location))
                {
                    File f = null;
                    if (isFileURI(event.location))
                    {
                        try
                        {
                            String location = encodeFileURI(fixFileURI(event.location));
                            URI url = new URI(location);
                            if (url.getFragment() != null)
                            {
                                int n = location.lastIndexOf('#');
                                if (n >= 0)
                                {
                                    url = new URI(location.substring(0, n));
                                }
                            }
                            f = new File(url);
                        }
                        catch (Exception e)
                        {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                    }
                    else if (event.location.regionMatches(true, 0, ECLIPSENSIS_URI_SCHEME, 0, ECLIPSENSIS_URI_SCHEME
                            .length()))
                    {
                        String action = event.location.substring(ECLIPSENSIS_URI_SCHEME.length());
                        if (action.equals(NSISHelpProducer.CONFIGURE))
                        {
                            IJobStatusRunnable runnable = new IJobStatusRunnable() {
                                public IStatus run(IProgressMonitor monitor)
                                {
                                    new NSISConfigWizardDialog(getSite().getShell()).open();
                                    return Status.OK_STATUS;
                                }
                            };
                            EclipseNSISPlugin.getDefault().getJobScheduler().scheduleUIJob(NSISHTMLHelp.class,
                                    EclipseNSISPlugin.getResourceString("configure.nsis.job.name"), runnable); //$NON-NLS-1$
                            event.doit = false;
                        }
                    }
                    else
                    {
                        f = new File(event.location);
                    }
                    File f2 = NSISHelpURLProvider.getInstance().translateCachedFile(f);
                    if (f2 != null && f2.exists())
                    {
                        event.doit = !HelpBrowserLocalFileHandler.INSTANCE.handle(f2);
                    }
                    if (event.doit)
                    {
                        // File has been translated and not handled.
                        // Handle it manually
                        if (f2 != null && f2.isDirectory())
                        {
                            event.doit = false;
                            try
                            {
                                Program.launch(f2.getCanonicalPath());
                            }
                            catch (IOException e)
                            {
                                EclipseNSISPlugin.getDefault().log(e);
                                mBrowser.setUrl(IOUtility.getFileURLString(f2));
                            }
                        }
                        else if (!Common.objectsAreEqual(f, f2))
                        {
                            event.doit = false;
                            mBrowser.setUrl(IOUtility.getFileURLString(f2));
                        }
                    }
                }
            }
        });

        init();
        NSISHelpURLProvider.getInstance().addListener(mHelpURLListener);
    }

    private void init()
    {
        Composite parent = mBrowser.getParent();
        NSISHelpTOC toc = NSISHelpURLProvider.getInstance().getCachedHelpTOC();
        if (toc == null)
        {
            mShowHideNavButton.dispose();
            mShowHideNavButton = null;
            mSynchedButton.dispose();
            mSynchedButton = null;
            mSeparator.dispose();
            mSeparator = null;
            if (mSashForm.getMaximizedControl() != parent)
            {
                mSashForm.setMaximizedControl(parent);
            }
            mContentsViewer.setInput(null);
            mIndexViewer.setInput(null);
        }
        else
        {
            if (mShowHideNavButton == null)
            {
                createNavToolItems();
            }
            if (mShowNav)
            {
                if (mSashForm.getMaximizedControl() != null)
                {
                    mSashForm.setMaximizedControl(null);
                }
            }
            else
            {
                if (mSashForm.getMaximizedControl() != parent)
                {
                    mSashForm.setMaximizedControl(parent);
                }
            }
            mContentsViewer.setInput(toc);
            NSISHelpIndex index = NSISHelpURLProvider.getInstance().getCachedHelpIndex();
            TabItem indexTabItem = null;
            TabItem[] tabItems = mNavigationPane.getItems();
            for (int i = 0; i < tabItems.length; i++)
            {
                if (Common.objectsAreEqual(tabItems[i].getControl(), mIndexViewer.getControl().getParent()))
                {
                    indexTabItem = tabItems[i];
                    break;
                }
            }
            if (index == null)
            {
                if (indexTabItem != null)
                {
                    indexTabItem.dispose();
                }
                mIndexViewer.setInput(null);
            }
            else
            {
                if (indexTabItem == null)
                {
                    TabItem tabItem = new TabItem(mNavigationPane, SWT.NONE, 1);
                    tabItem.setText(EclipseNSISPlugin.getResourceString("help.browser.index.tab.label")); //$NON-NLS-1$
                    tabItem.setControl(mIndexViewer.getControl().getParent());
                }
                mIndexViewer.setInput(index);
                try
                {
                    mNonUserChange = true;
                    mIndexViewer.setSelection(new StructuredSelection(index.getEntries().get(0)));
                    mIndexViewer.getList().setTopIndex(mIndexViewer.getList().getSelectionIndex());
                }
                finally
                {
                    mNonUserChange = false;
                }
            }
        }
        mSearchingDialog.reset();
        mSearchViewer.setInput(null);
        Table table = mSearchViewer.getTable();
        table.setSortColumn(null);
        table.setSortDirection(SWT.NONE);

        openHelp();
    }

    /**
     * @param toc
     * @param location
     */
    private void synch(String location)
    {
        if (mSynched && mContentsViewer != null)
        {
            String url = location;
            try
            {
                new URL(url);
            }
            catch (MalformedURLException mue)
            {
                String suffix = ""; //$NON-NLS-1$
                int n = url.lastIndexOf('#');
                if (n > 0)
                {
                    suffix = url.substring(n);
                    url = url.substring(0, n);
                }
                File f = new File(url);
                url = IOUtility.getFileURLString(f) + suffix;

            }
            NSISHelpTOC toc = NSISHelpURLProvider.getInstance().getCachedHelpTOC();
            NSISHelpTOCNode node = toc == null ? null : toc.getNode(url);
            if (node == null)
            {
                int n = url.lastIndexOf('#');
                if (toc != null && n >= 0)
                {
                    url = url.substring(0, n + 1);
                    node = toc.getNode(url);
                    if (node == null)
                    {
                        url = url.substring(0, n);
                        node = toc.getNode(url);
                    }
                }
                else
                {
                    node = toc == null ? null : toc.getNode(url + "#"); //$NON-NLS-1$
                }
            }
            if (node != null)
            {
                ISelection sel = mContentsViewer.getSelection();
                if (sel.isEmpty() || !Common.objectsAreEqual(node, ((StructuredSelection) sel).getFirstElement()))
                {
                    mContentsViewer.setSelection(new StructuredSelection(node));
                }
            }
            else
            {
                mContentsViewer.setSelection(StructuredSelection.EMPTY);
            }
        }
    }

    @Override
    public void dispose()
    {
        NSISHelpURLProvider.getInstance().removeListener(mHelpURLListener);
        super.dispose();
    }

    /**
     * Loads the resources
     */
    private void initResources()
    {
        if (mThrobberImages == null)
        {
            MessageFormat mf = new MessageFormat(IMAGE_LOCATION_FORMAT);
            mThrobberImages = new Image[IMAGE_COUNT];
            for (int i = 0; i < IMAGE_COUNT; ++i)
            {
                mThrobberImages[i] = EclipseNSISPlugin.getImageManager().getImage(
                        mf.format(new Object[] { new Integer(i) }));
            }
        }
    }

    private void resizeColumn(TableColumn column)
    {
        Table table = column.getParent();
        GC gc = new GC(table);
        int width = gc.stringExtent(column.getText()).x + 16;
        if (table.getSortColumn() == column && table.getSortDirection() != SWT.NONE)
        {
            width += 26;
        }
        gc.dispose();
        if (column.getWidth() < width)
        {
            column.setWidth(width);
        }
    }

    private void createNavigationPane(Composite parent)
    {
        mNavigationPane = new TabFolder(parent, SWT.NONE);

        TabItem item = new TabItem(mNavigationPane, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("help.browser.contents.tab.label")); //$NON-NLS-1$
        item.setControl(createContentsTab(mNavigationPane));

        item = new TabItem(mNavigationPane, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("help.browser.index.tab.label")); //$NON-NLS-1$
        item.setControl(createIndexTab(mNavigationPane));

        item = new TabItem(mNavigationPane, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString("help.browser.search.tab.label")); //$NON-NLS-1$
        item.setControl(createSearchTab(mNavigationPane));
    }

    private Composite createSearchTab(Composite parent)
    {
        Composite searchComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        searchComposite.setLayout(layout);

        Composite composite = new Composite(searchComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);

        Label l = new Label(composite, SWT.WRAP);
        l.setText(EclipseNSISPlugin.getResourceString("help.browser.search.box.label")); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        l.setLayoutData(data);

        final String searchSyntaxURL = NSISHelpURLProvider.getInstance().getSearchManager().getSearchSyntaxURL();
        if (searchSyntaxURL != null)
        {
            ToolBar toolbar = new ToolBar(composite, SWT.FLAT);
            toolbar.setCursor(toolbar.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            ToolItem toolItem = new ToolItem(toolbar, SWT.PUSH);
            Image image = EclipseNSISPlugin.getImageManager().getImage(
                    EclipseNSISPlugin.getResourceString("help.small.icon")); //$NON-NLS-1$
            toolItem.setImage(image);
            toolItem.setToolTipText(EclipseNSISPlugin.getResourceString("help.browser.search.syntax.tooltip")); //$NON-NLS-1$
            toolItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    mBrowser.setUrl(searchSyntaxURL);
                }
            });
            toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        }
        else
        {
            data.horizontalSpan = 2;
        }

        composite = new Composite(searchComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);

        final Text searchText = new Text(composite, SWT.BORDER);
        searchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        final Button b = new Button(composite, SWT.ARROW | SWT.RIGHT);
        b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        final Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
        SelectionAdapter menuAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                MenuItem mi = (MenuItem) e.widget;
                searchText.insert(mi.getText());
            }
        };
        String[] labels = { " AND ", " OR ", " NOT " }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < labels.length; i++)
        {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText(labels[i]);
            mi.addSelectionListener(menuAdapter);
        }

        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (menu.isVisible())
                {
                    menu.setVisible(false);
                }
                else
                {
                    Rectangle rect = b.getBounds();
                    Point pt = new Point(rect.x + rect.width, rect.y);
                    pt = b.getParent().toDisplay(pt);
                    menu.setLocation(pt.x, pt.y);
                    menu.setVisible(true);
                }
            }
        });

        composite = new Composite(searchComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
        layout = new GridLayout(2, true);
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = 40;
        composite.setLayout(layout);

        final Button listTopics = new Button(composite, SWT.PUSH);
        listTopics.setText(EclipseNSISPlugin.getResourceString("help.browser.list.topics.label")); //$NON-NLS-1$
        listTopics.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        listTopics.setEnabled(false);

        searchText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                listTopics.setEnabled(!Common.isEmpty(searchText.getText()));
            }
        });

        Button display = new Button(composite, SWT.PUSH);
        display.setText(EclipseNSISPlugin.getResourceString("help.browser.display.label")); //$NON-NLS-1$
        display.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        composite = new Composite(searchComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        layout = new GridLayout(3, false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        l = new Label(composite, SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("help.browser.search.results.label")); //$NON-NLS-1$
        data = new GridData(SWT.FILL, SWT.FILL, false, false);
        data.widthHint = 70;
        l.setLayoutData(data);

        l = new Label(composite, SWT.NONE);
        l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final MessageFormat foundFormat = new MessageFormat(EclipseNSISPlugin
                .getResourceString("help.browser.search.results.count.format")); //$NON-NLS-1$
        final Integer[] foundArgs = { Common.ZERO };
        final Label found = new Label(composite, SWT.NONE);
        found.setText(foundFormat.format(foundArgs));
        data = new GridData(SWT.FILL, SWT.FILL, false, false);
        data.widthHint = 120;
        found.setLayoutData(data);

        final Table table = new Table(searchComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL
                | SWT.H_SCROLL);
        final TableColumn titleColumn = new TableColumn(table, SWT.NONE);
        titleColumn.setText(EclipseNSISPlugin.getResourceString("help.browser.search.title.label")); //$NON-NLS-1$
        final TableColumn rankColumn = new TableColumn(table, SWT.NONE);
        rankColumn.setText(EclipseNSISPlugin.getResourceString("help.browser.search.rank.label")); //$NON-NLS-1$
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final TableResizer tableResizer = new TableResizer(new double[] { 4, 1 });
        table.addControlListener(tableResizer);

        final Comparator<NSISHelpSearchResult> sortComparator = new Comparator<NSISHelpSearchResult>() {
            public int compare(NSISHelpSearchResult r1, NSISHelpSearchResult r2)
            {
                TableColumn sortCol = table.getSortColumn();
                int sortDir = table.getSortDirection();
                int cmp;
                if (sortCol == titleColumn)
                {
                    cmp = r1.getTitle().compareTo(r2.getTitle());
                }
                else
                {
                    cmp = r1.getRank() - r2.getRank();
                }
                return sortDir == SWT.DOWN ? -cmp : cmp;
            }
        };

        SelectionListener colummSortListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                NSISHelpSearchResult[] results = (NSISHelpSearchResult[]) mSearchViewer.getInput();
                if (!Common.isEmptyArray(results))
                {
                    TableColumn col = (TableColumn) e.widget;
                    Table t = col.getParent();
                    TableColumn sortCol = t.getSortColumn();
                    if (sortCol == col)
                    {
                        int sortDir = t.getSortDirection();
                        t.setSortDirection(sortDir == SWT.UP ? SWT.DOWN : SWT.UP);
                    }
                    else
                    {
                        t.setSortColumn(col);
                        t.setSortDirection(SWT.UP);
                    }

                    Arrays.sort(results, sortComparator);
                    resizeColumn(col);
                    mSearchViewer.refresh();
                }
            }
        };

        titleColumn.addSelectionListener(colummSortListener);
        rankColumn.addSelectionListener(colummSortListener);

        mSearchViewer = new TableViewer(table);
        mSearchViewer.setContentProvider(new ArrayContentProvider());
        mSearchViewer.setLabelProvider(new CollectionLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex)
            {
                if (element instanceof NSISHelpSearchResult)
                {
                    switch (columnIndex)
                    {
                        case 0:
                            return ((NSISHelpSearchResult) element).getTitle();
                        case 1:
                            return String.valueOf(((NSISHelpSearchResult) element).getRank());
                    }
                }
                return super.getColumnText(element, columnIndex);
            }
        });

        final Button searchPrevious = new Button(searchComposite, SWT.CHECK);
        searchPrevious.setText(EclipseNSISPlugin.getResourceString("help.browser.search.previous.results.label")); //$NON-NLS-1$
        searchPrevious.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final Button useStemming = new Button(searchComposite, SWT.CHECK);
        useStemming.setSelection(true);
        useStemming.setText(EclipseNSISPlugin.getResourceString("help.browser.search.stemmed.label")); //$NON-NLS-1$
        useStemming.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final Button searchTitles = new Button(searchComposite, SWT.CHECK);
        searchTitles.setText(EclipseNSISPlugin.getResourceString("help.browser.search.titles.label")); //$NON-NLS-1$
        searchTitles.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        mSearchingDialog = new SearchingDialog(getSite().getShell()) {
            @Override
            public void searchCompleted(final NSISHelpSearchResult[] results, Collection<String> highlightTerms)
            {
                super.searchCompleted(results, highlightTerms);
                getSite().getShell().getDisplay().asyncExec(new Runnable() {
                    public void run()
                    {
                        int size = results == null ? 0 : results.length;
                        foundArgs[0] = size == 0 ? Common.ZERO : new Integer(size);
                        found.setText(foundFormat.format(foundArgs));
                        Table table = mSearchViewer.getTable();
                        table.setSortColumn(Common.isEmptyArray(results) ? null : rankColumn);
                        table.setSortDirection(Common.isEmptyArray(results) ? SWT.NONE : SWT.UP);
                        resizeColumn(rankColumn);
                        mSearchViewer.setInput(results);
                    }
                });
            }

            @Override
            public int open()
            {
                int rv = super.open();
                NSISHelpURLProvider.getInstance().getSearchManager().search(
                        searchTitles.getSelection() ? INSISHelpSearchConstants.INDEX_FIELD_TITLE : null, this);
                return rv;
            }
        };
        final Runnable displaySearchRunnable = new Runnable() {
            public void run()
            {
                ISelection sel = mSearchViewer.getSelection();
                if (!sel.isEmpty() && sel instanceof IStructuredSelection)
                {
                    if (mHighlightJSPrefix != null && HIGHLIGHT_JS_CONTENT != null)
                    {
                        if (mHighlightJS == null)
                        {
                            Collection<String> terms = mSearchingDialog.getTerms();
                            if (!Common.isEmptyCollection(terms))
                            {
                                StringBuffer keywords = new StringBuffer(""); //$NON-NLS-1$
                                StringBuffer regex = new StringBuffer(""); //$NON-NLS-1$
                                for (Iterator<String> iter = terms.iterator(); iter.hasNext();)
                                {
                                    String term = iter.next();
                                    boolean isRegex = false;
                                    if (term.startsWith(NSISHelpSearchQueryParser.REGEX_PREFIX))
                                    {
                                        isRegex = true;
                                        term = term.substring(NSISHelpSearchQueryParser.REGEX_PREFIX.length())
                                                .replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                    if (keywords.length() > 0)
                                    {
                                        keywords.append(","); //$NON-NLS-1$
                                        regex.append(","); //$NON-NLS-1$
                                    }
                                    keywords.append("\"").append(term.replaceAll("\\\"", "\\\\\"")).append("\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                    regex.append(isRegex);
                                }

                                final StringBuffer js = new StringBuffer(""); //$NON-NLS-1$
                                mHighlightJSPrefix.format(new String[] { keywords.toString(), regex.toString() }, js,
                                        null);
                                js.append(HIGHLIGHT_JS_CONTENT);
                                mHighlightJS = js.toString();
                            }
                        }
                        final String url = ((NSISHelpSearchResult) ((IStructuredSelection) sel).getFirstElement())
                                .getURL();
                        if (mHighlightJS != null)
                        {
                            final String urlFile;
                            String url2 = decode(url);
                            int n = url2.lastIndexOf('#');
                            if (n < 0)
                            {
                                urlFile = url2;
                            }
                            else
                            {
                                urlFile = url2.substring(0, n);
                            }
                            mBrowser.addLocationListener(new LocationAdapter() {
                                @Override
                                public void changed(LocationEvent event)
                                {
                                    String location = event.location;
                                    if (!Common.isEmpty(location))
                                    {
                                        if (!isFileURI(location))
                                        {
                                            location.replace("+", " "); //$NON-NLS-1$ //$NON-NLS-2$
                                            // This is a windows file name
                                            location = decode(IOUtility.getFileURLString(new File(location)));
                                        }
                                        else
                                        {
                                            location = fixFileURI(location);
                                        }
                                        String file;
                                        int n = location.lastIndexOf('#');
                                        if (n < 0)
                                        {
                                            file = location;
                                        }
                                        else
                                        {
                                            file = location.substring(0, n);
                                        }
                                        if (!urlFile.equalsIgnoreCase(file))
                                        {
                                            mBrowser.removeLocationListener(this);
                                        }
                                        else
                                        {
                                            mBrowser.execute(mHighlightJS);
                                        }
                                    }
                                }
                            });
                        }
                        mBrowser.setUrl(url);
                    }
                }
            }
        };
        display.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                displaySearchRunnable.run();
            }
        });
        mSearchViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                displaySearchRunnable.run();
            }
        });

        final Runnable searchRunnable = new Runnable() {
            public void run()
            {
                mSearchingDialog.setUseStemming(useStemming.getSelection());
                mSearchingDialog.setSearchPrevious(searchPrevious.getSelection());
                mSearchingDialog.setSearchText(searchText.getText());
                mSearchingDialog.open();
            }
        };
        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.character == SWT.CR || e.character == SWT.LF)
                {
                    searchRunnable.run();
                    searchText.selectAll();
                }
            }
        });

        listTopics.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                searchRunnable.run();
            }
        });

        return searchComposite;
    }

    private Composite createIndexTab(Composite parent)
    {
        Composite indexComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        indexComposite.setLayout(layout);

        Label l = new Label(indexComposite, SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("help.browser.index.keyword.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        final Text indexText = new Text(indexComposite, SWT.BORDER);
        indexText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        List list = new List(indexComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mIndexViewer = new ListViewer(list);
        mIndexViewer.setContentProvider(new EmptyContentProvider() {
            @Override
            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof NSISHelpIndex)
                {
                    return ((NSISHelpIndex) inputElement).getEntries().toArray();
                }
                return null;
            }
        });
        mIndexViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element)
            {
                if (element instanceof NSISHelpIndexEntry)
                {
                    return ((NSISHelpIndexEntry) element).getName();
                }
                return super.getText(element);
            }
        });
        mIndexViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                if (!sel.isEmpty())
                {
                    Object element = sel.getFirstElement();
                    if (element instanceof NSISHelpIndexEntry)
                    {
                        displayIndexEntry((NSISHelpIndexEntry) element);
                    }
                }
            }
        });
        mIndexViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if (!mNonUserChange)
                {
                    IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                    if (!sel.isEmpty())
                    {
                        Object element = sel.getFirstElement();
                        if (element instanceof NSISHelpIndexEntry)
                        {
                            try
                            {
                                mNonUserChange = true;
                                indexText.setText(((NSISHelpIndexEntry) element).getName());
                            }
                            finally
                            {
                                mNonUserChange = false;
                            }
                        }
                    }
                }
            }
        });
        indexText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                if (!mNonUserChange)
                {
                    String text = indexText.getText();
                    NSISHelpIndexEntry entry = ((NSISHelpIndex) mIndexViewer.getInput()).findEntry(text);
                    if (entry != null)
                    {
                        try
                        {
                            mNonUserChange = true;
                            mIndexViewer.setSelection(new StructuredSelection(entry));
                            mIndexViewer.getList().setTopIndex(mIndexViewer.getList().getSelectionIndex());
                        }
                        finally
                        {
                            mNonUserChange = false;
                        }
                    }
                }
            }
        });
        Button b = new Button(indexComposite, SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("help.browser.display.label")); //$NON-NLS-1$
        b.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection sel = (IStructuredSelection) mIndexViewer.getSelection();
                if (!sel.isEmpty())
                {
                    Object element = sel.getFirstElement();
                    if (element instanceof NSISHelpIndexEntry)
                    {
                        displayIndexEntry((NSISHelpIndexEntry) element);
                    }
                }
            }
        });
        return indexComposite;
    }

    private Composite createContentsTab(Composite parent)
    {
        Composite contentsComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        contentsComposite.setLayout(layout);
        Tree tree = new Tree(contentsComposite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mContentsViewer = new TreeViewer(tree);
        final ITreeContentProvider contentProvider = new EmptyContentProvider() {
            @Override
            public Object[] getChildren(Object parentElement)
            {
                if (parentElement instanceof NSISHelpTOC)
                {
                    return ((NSISHelpTOC) parentElement).getChildren().toArray();
                }
                else if (parentElement instanceof NSISHelpTOCNode)
                {
                    return ((NSISHelpTOCNode) parentElement).getChildren().toArray();
                }
                return null;
            }

            @Override
            public Object getParent(Object element)
            {
                if (element instanceof NSISHelpTOCNode)
                {
                    return ((NSISHelpTOCNode) element).getParent();
                }
                return null;
            }

            @Override
            public boolean hasChildren(Object element)
            {
                if (element instanceof NSISHelpTOC)
                {
                    return !Common.isEmptyCollection(((NSISHelpTOC) element).getChildren());
                }
                else if (element instanceof NSISHelpTOCNode)
                {
                    return !Common.isEmptyCollection(((NSISHelpTOCNode) element).getChildren());
                }
                return false;
            }

            @Override
            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof NSISHelpTOC)
                {
                    return getChildren(inputElement);
                }
                return null;
            }
        };
        mContentsViewer.setContentProvider(contentProvider);
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();
        final Image helpClosed = imageManager.getImage(EclipseNSISPlugin.getResourceString("help.closed.icon")); //$NON-NLS-1$
        final Image helpOpen = imageManager.getImage(EclipseNSISPlugin.getResourceString("help.open.icon")); //$NON-NLS-1$
        final Image helpPage = imageManager.getImage(EclipseNSISPlugin.getResourceString("help.page.icon")); //$NON-NLS-1$
        mContentsViewer.setLabelProvider(new LabelProvider() {
            @Override
            public Image getImage(Object element)
            {
                if (element instanceof NSISHelpTOCNode)
                {
                    NSISHelpTOCNode node = (NSISHelpTOCNode) element;
                    if (Common.isEmptyCollection(node.getChildren()))
                    {
                        return helpPage;
                    }
                    else
                    {
                        if (mContentsViewer.getExpandedState(element))
                        {
                            return helpOpen;
                        }
                        else
                        {
                            return helpClosed;
                        }
                    }
                }
                return super.getImage(element);
            }

            @Override
            public String getText(Object element)
            {
                if (element instanceof NSISHelpTOCNode)
                {
                    return ((NSISHelpTOCNode) element).getName();
                }
                return super.getText(element);
            }
        });
        mContentsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if (!event.getSelection().isEmpty())
                {
                    Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    if (element instanceof NSISHelpTOCNode)
                    {
                        String url = ((NSISHelpTOCNode) element).getURL();
                        if (!Common.stringsAreEqual(url, mBrowser.getUrl(), true))
                        {
                            mBrowser.setUrl(url);
                        }
                    }
                }
            }
        });
        mContentsViewer.addTreeListener(new ITreeViewerListener() {
            public void treeCollapsed(TreeExpansionEvent event)
            {
                updateLabels(mContentsViewer, event);
            }

            /**
             * @param treeViewer
             * @param event
             */
            private void updateLabels(final TreeViewer treeViewer, TreeExpansionEvent event)
            {
                final Object element = event.getElement();
                if (element instanceof NSISHelpTOCNode)
                {
                    treeViewer.getTree().getDisplay().asyncExec(new Runnable() {
                        public void run()
                        {
                            treeViewer.update(element, null);
                        }
                    });
                }
            }

            public void treeExpanded(TreeExpansionEvent event)
            {
                updateLabels(mContentsViewer, event);
            }
        });
        return contentsComposite;
    }

    private void displayIndexEntry(NSISHelpIndexEntry entry)
    {
        java.util.List<NSISHelpIndexURL> urls = entry.getURLs();
        if (urls.size() > 0)
        {
            NSISHelpIndexURL url = null;
            if (urls.size() > 1)
            {
                NSISHelpIndexEntryDialog dialog = new NSISHelpIndexEntryDialog(getSite().getShell(), entry);
                if (dialog.open() == Window.OK)
                {
                    url = dialog.getURL();
                }
            }
            else
            {
                url = urls.get(0);
            }
            if (url != null)
            {
                mBrowser.setUrl(url.getURL());
            }
        }
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, Image icon)
    {
        return createToolItem(bar, tooltip, icon, bar.getItemCount());
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, Image icon, int index)
    {
        return createToolItem(bar, tooltip, icon, index, SWT.PUSH);
    }

    private ToolItem createToolItem(ToolBar bar, String tooltip, Image icon, int index, int style)
    {
        ToolItem item = new ToolItem(bar, style);
        item.setToolTipText(EclipseNSISPlugin.getResourceString(tooltip));
        item.setImage(icon);
        return item;
    }

    private void createToolBar(Composite parent)
    {
        mToolBar = new ToolBar(parent, SWT.FLAT);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 5);
        mToolBar.setLayoutData(data);

        createNavToolItems();

        // Add a button to navigate backwards through previously visited pages
        mBackButton = createToolItem(mToolBar, "help.browser.back.tooltip", //$NON-NLS-1$
                CommonImages.BROWSER_BACK_ICON);

        // Add a button to navigate forward through previously visited pages
        mForwardButton = createToolItem(mToolBar, "help.browser.forward.tooltip", //$NON-NLS-1$
                CommonImages.BROWSER_FORWARD_ICON);

        // Add a separator
        new ToolItem(mToolBar, SWT.SEPARATOR);

        // Add a button to abort web page loading
        mStopButton = createToolItem(mToolBar, "help.browser.stop.tooltip", //$NON-NLS-1$
                CommonImages.BROWSER_STOP_ICON);

        // Add a button to refresh the current web page
        mRefreshButton = createToolItem(mToolBar, "help.browser.refresh.tooltip", //$NON-NLS-1$
                CommonImages.BROWSER_REFRESH_ICON);

        // Add a button to navigate to the Home page
        mHomeButton = createToolItem(mToolBar, "help.browser.home.tooltip", //$NON-NLS-1$
                CommonImages.BROWSER_HOME_ICON);

        Listener listener = new Listener() {
            public void handleEvent(Event event)
            {
                ToolItem item = (ToolItem) event.widget;
                if (item == mBackButton)
                {
                    mBrowser.back();
                }
                else if (item == mForwardButton)
                {
                    mBrowser.forward();
                }
                else if (item == mStopButton)
                {
                    mBrowser.stop();
                }
                else if (item == mRefreshButton)
                {
                    mBrowser.refresh();
                }
                else if (item == mHomeButton)
                {
                    cFirstPage = null;
                    openHelp();
                }
            }
        };
        mBackButton.addListener(SWT.Selection, listener);
        mForwardButton.addListener(SWT.Selection, listener);
        mStopButton.addListener(SWT.Selection, listener);
        mRefreshButton.addListener(SWT.Selection, listener);
        mHomeButton.addListener(SWT.Selection, listener);
    }

    /**
     *
     */
    private void createNavToolItems()
    {
        // Add a button to show/hide navigation pane
        final String showNavToolTip = EclipseNSISPlugin.getResourceString("help.browser.shownav.tooltip"); //$NON-NLS-1$
        final String hideNavToolTip = EclipseNSISPlugin.getResourceString("help.browser.hidenav.tooltip"); //$NON-NLS-1$
        mShowHideNavButton = createToolItem(mToolBar, (mShowNav ? hideNavToolTip : showNavToolTip),
                (mShowNav ? CommonImages.BROWSER_HIDENAV_ICON : CommonImages.BROWSER_SHOWNAV_ICON), 0);

        // Add a button to sync browser with contents
        mSynchedButton = createToolItem(mToolBar, "help.browser.synced.tooltip", //$NON-NLS-1$
                CommonImages.BROWSER_SYNCED_ICON, 1, SWT.CHECK);
        mSynchedButton.setSelection(mSynched);
        mSynchedButton.setEnabled(mShowNav);

        // Add a separator
        mSeparator = new ToolItem(mToolBar, SWT.SEPARATOR, 2);
        Listener listener = new Listener() {
            public void handleEvent(Event event)
            {
                ToolItem item = (ToolItem) event.widget;
                if (item == mShowHideNavButton)
                {
                    if (mSashForm.getMaximizedControl() == null)
                    {
                        mShowNav = false;
                        mSashForm.setMaximizedControl(mBrowser.getParent());
                        item.setImage(CommonImages.BROWSER_SHOWNAV_ICON);
                        item.setToolTipText(showNavToolTip);
                    }
                    else
                    {
                        mShowNav = true;
                        mSashForm.setMaximizedControl(null);
                        item.setImage(CommonImages.BROWSER_HIDENAV_ICON);
                        item.setToolTipText(hideNavToolTip);
                    }
                    mSynchedButton.setEnabled(mShowNav);
                    NSISPreferences.getInstance().setValue(INSISPreferenceConstants.NSIS_HELP_VIEW_SHOW_NAV, mShowNav);
                }
                else if (item == mSynchedButton)
                {
                    mSynched = mSynchedButton.getSelection();
                    NSISPreferences.getInstance().setValue(INSISPreferenceConstants.NSIS_HELP_VIEW_SYNCHED, mSynched);
                    synch(mBrowser.getUrl());
                }
            }
        };
        mShowHideNavButton.addListener(SWT.Selection, listener);
        mSynchedButton.addListener(SWT.Selection, listener);
    }

    /**
     * @param displayArea
     */
    private void createThrobber(Composite displayArea)
    {
        FormData data;
        final Rectangle rect = mThrobberImages[0].getBounds();
        mThrobber = new Canvas(displayArea, SWT.NONE);
        data = new FormData();
        data.width = rect.width;
        data.height = rect.height;
        data.top = new FormAttachment(0, 5);
        data.right = new FormAttachment(100, -5);
        mThrobber.setLayoutData(data);

        mThrobber.addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event e)
            {
                Point pt = ((Canvas) e.widget).getSize();
                e.gc.drawImage(mThrobberImages[mThrobberImageIndex], 0, 0, rect.width, rect.height, 0, 0, pt.x, pt.y);
            }
        });
        mThrobber.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event e)
            {
                cFirstPage = null;
                openHelp();
            }
        });

        final Display display = displayArea.getDisplay();
        display.asyncExec(new Runnable() {
            public void run()
            {
                if (mThrobber.isDisposed())
                {
                    return;
                }
                if (isBusy())
                {
                    mThrobberImageIndex++;
                    if (mThrobberImageIndex == mThrobberImages.length)
                    {
                        mThrobberImageIndex = 1;
                    }
                    mThrobber.redraw();
                }
                display.timerExec(100, this);
            }
        });
    }

    private boolean isBusy()
    {
        return mBusy;
    }

    private void setBusy(boolean busy)
    {
        mBusy = busy;
        if (mStopButton != null && !mStopButton.isDisposed())
        {
            mStopButton.setEnabled(busy);
        }
    }

    private void createStatusArea(Composite composite)
    {
        // Add a label for displaying status messages as they are received from
        // the control
        mStatusText = new Label(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        // Add a progress bar to display downloading progress information
        mProgressBar = new ProgressBar(composite, SWT.BORDER);

        FormData data = new FormData();
        data.left = new FormAttachment(0, 5);
        data.right = new FormAttachment(mProgressBar, 0, SWT.DEFAULT);
        data.bottom = new FormAttachment(100, -5);
        mStatusText.setLayoutData(data);

        data = new FormData();
        data.right = new FormAttachment(100, -5);
        data.bottom = new FormAttachment(100, -5);
        mProgressBar.setLayoutData(data);

        mBrowser.addStatusTextListener(new StatusTextListener() {
            public void changed(StatusTextEvent event)
            {
                mStatusText.setText(event.text);
            }
        });

        mBrowser.addProgressListener(new ProgressListener() {
            public void changed(ProgressEvent event)
            {
                int ratio;
                if (event.total == 0)
                {
                    ratio = 0;
                    setBusy(false);
                }
                else
                {
                    ratio = event.current * 100 / event.total;
                    setBusy(event.current != event.total);
                }
                mProgressBar.setSelection(ratio);
                if (!isBusy())
                {
                    mThrobberImageIndex = 0;
                    mThrobber.redraw();
                }
            }

            public void completed(ProgressEvent event)
            {
                mProgressBar.setSelection(0);
                setBusy(false);
                mThrobberImageIndex = 0;
                mBackButton.setEnabled(mBrowser.isBackEnabled());
                mForwardButton.setEnabled(mBrowser.isForwardEnabled());
                mThrobber.redraw();
            }
        });
    }

    private String decode(String url)
    {
        try
        {
            return URLDecoder.decode(url, ENCODING_SCHEME);
        }
        catch (UnsupportedEncodingException e)
        {
            return url;
        }
    }

    private void openHelp()
    {
        if (isActivated())
        {
            if (!EclipseNSISPlugin.getDefault().isConfigured())
            {
                mBrowser.setText(EclipseNSISPlugin.getFormattedString("unconfigured.browser.help.format", //$NON-NLS-1$
                        new String[] { NSISHelpProducer.STYLE, ECLIPSENSIS_URI_SCHEME, NSISHelpProducer.CONFIGURE }));
            }
            else
            {
                mStartPage = NSISHelpURLProvider.getInstance().getCachedHelpStartPage();
                if (mStartPage == null)
                {
                    mStartPage = "about:blank"; //$NON-NLS-1$
                }
                String newUrl = decode(cFirstPage == null ? mStartPage : cFirstPage);
                String oldUrl = mBrowser.getUrl();
                if (!Common.isEmpty(oldUrl))
                {
                    String newUrl2;
                    int m = newUrl.lastIndexOf('#');
                    if (m > 0)
                    {
                        newUrl2 = newUrl.substring(0, m);
                    }
                    else
                    {
                        newUrl2 = newUrl;
                    }
                    String oldUrl2;
                    int n = oldUrl.lastIndexOf('#');
                    if (n > 0)
                    {
                        oldUrl2 = oldUrl.substring(0, n);
                    }
                    else
                    {
                        oldUrl2 = oldUrl;
                    }
                    if (Common.stringsAreEqual(newUrl2, oldUrl2, true))
                    {
                        mBrowser.refresh();
                    }
                }
                mBrowser.setUrl(newUrl);
                cFirstPage = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        if (isActivated())
        {
            mBrowser.setFocus();
        }
    }

    private boolean isFileURI(String location)
    {
        return location.regionMatches(true, 0, FILE_URI_SCHEME, 0, FILE_URI_SCHEME.length());
    }

    private String fixFileURI(String location)
    {
        Matcher matcher = cW3CFileUriPattern.matcher(location);
        if(matcher.matches())
        {
            return "file:///" + matcher.group(1).replace("+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        return location;
    }

    private String encodeFileURI(String location) throws IOException
    {
        Matcher matcher = cFileUriPattern.matcher(location);
        if(matcher.matches())
        {
            return "file:///" + URLEncoder.encode(matcher.group(1),System.getProperty("file.encoding")).replace("+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        return location;
    }

    private class NSISHelpIndexEntryDialog extends Dialog
    {
        private NSISHelpIndexEntry mEntry;
        private NSISHelpIndexURL mURL;

        public NSISHelpIndexEntryDialog(Shell parent, NSISHelpIndexEntry entry)
        {
            super(parent);
            mEntry = entry;
            mURL = mEntry.getURLs().get(0);
        }

        public NSISHelpIndexURL getURL()
        {
            return mURL;
        }

        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(EclipseNSISPlugin.getResourceString("help.browser.index.entry.dialog.title")); //$NON-NLS-1$
            newShell.setImage(EclipseNSISPlugin.getShellImage());
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent)
        {
            super.createButtonsForButtonBar(parent);
            getButton(IDialogConstants.OK_ID)
                    .setText(EclipseNSISPlugin.getResourceString("help.browser.display.label")); //$NON-NLS-1$
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite parent2 = (Composite) super.createDialogArea(parent);
            Label l = new Label(parent2, SWT.NONE);
            l.setText(EclipseNSISPlugin.getResourceString("help.browser.index.entry.dialog.header")); //$NON-NLS-1$
            l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            Table table = new Table(parent2, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
            initializeDialogUnits(table);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = convertWidthInCharsToPixels(80);
            data.heightHint = convertHeightInCharsToPixels(10);
            table.setLayoutData(data);

            TableColumn col = new TableColumn(table, SWT.NONE);
            col.setText(EclipseNSISPlugin.getResourceString("help.browser.title.label")); //$NON-NLS-1$
            col = new TableColumn(table, SWT.NONE);
            col.setText(EclipseNSISPlugin.getResourceString("help.browser.location.label")); //$NON-NLS-1$

            table.setLinesVisible(false);
            table.setHeaderVisible(true);

            table.addControlListener(new TableResizer(new double[] { 1, 1 }));

            final TableViewer viewer = new TableViewer(table);
            viewer.setContentProvider(new EmptyContentProvider() {
                @Override
                public Object[] getElements(Object inputElement)
                {
                    if (inputElement instanceof NSISHelpIndexEntry)
                    {
                        return ((NSISHelpIndexEntry) inputElement).getURLs().toArray();
                    }
                    return super.getElements(inputElement);
                }
            });
            viewer.setLabelProvider(new CollectionLabelProvider() {
                @Override
                public String getColumnText(Object element, int columnIndex)
                {
                    if (element instanceof NSISHelpIndexURL)
                    {
                        switch (columnIndex)
                        {
                            case 0:
                                return mEntry.getName();
                            case 1:
                                return ((NSISHelpIndexURL) element).getLocation();
                        }
                    }
                    return super.getColumnText(element, columnIndex);
                }
            });
            viewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event)
                {
                    updateURL(event.getSelection());
                }
            });
            viewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event)
                {
                    if (updateURL(event.getSelection()))
                    {
                        okPressed();
                    }
                }
            });
            viewer.setInput(mEntry);
            viewer.setSelection(new StructuredSelection(mURL));
            return parent2;
        }

        private boolean updateURL(ISelection sel)
        {
            if (!sel.isEmpty() && sel instanceof IStructuredSelection)
            {
                IStructuredSelection ssel = (IStructuredSelection) sel;
                mURL = (NSISHelpIndexURL) ssel.getFirstElement();
                return true;
            }
            return false;
        }
    }

    private static final String SEARCHING_DIALOG_TITLE = EclipseNSISPlugin.getResourceString("searching.dialog.title"); //$NON-NLS-1$
    private static final String SEARCHING_DIALOG_MESSAGE = EclipseNSISPlugin
            .getResourceString("searching.dialog.message"); //$NON-NLS-1$

    private class SearchingDialog extends Dialog implements INSISHelpSearchRequester
    {
        private String mSearchText = ""; //$NON-NLS-1$
        private boolean mCanceled = false;
        private Query mQuery = null;
        private boolean mSearchPrevious = false;
        private Collection<String> mTerms = new CaseInsensitiveSet();
        private boolean mUseStemming = false;

        protected SearchingDialog(Shell parentShell)
        {
            super(parentShell);
            setBlockOnOpen(false);
        }

        @Override
        protected Point getInitialSize()
        {
            Point size = super.getInitialSize();
            GC gc = new GC(getShell());
            int width = gc.stringExtent(SEARCHING_DIALOG_TITLE).x + 75;
            gc.dispose();
            if (size.x < width)
            {
                size.x = width;
            }
            return size;
        }

        public Filter getFilter()
        {
            if (mSearchPrevious && mQuery != null)
            {
                return new QueryFilter(mQuery);
            }
            else
            {
                return null;
            }
        }

        public void setUseStemming(boolean useStemming)
        {
            mUseStemming = useStemming;
        }

        public boolean useStemming()
        {
            return mUseStemming;
        }

        public void setSearchPrevious(boolean searchPrevious)
        {
            mSearchPrevious = searchPrevious;
        }

        public void queryParsed(Query query)
        {
            mQuery = query;
        }

        public void setSearchText(String searchText)
        {
            mSearchText = searchText;
        }

        public String getSearchText()
        {
            return mSearchText;
        }

        public boolean isCanceled()
        {
            return mCanceled;
        }

        public Collection<String> getTerms()
        {
            return mTerms;
        }

        public void reset()
        {
            mUseStemming = false;
            mSearchPrevious = false;
            mQuery = null;
            mSearchText = ""; //$NON-NLS-1$
            mCanceled = false;
            mTerms.clear();
        }

        public void searchCompleted(final NSISHelpSearchResult[] results, Collection<String> highlightTerms)
        {
            getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run()
                {
                    close();
                }
            });
            mHighlightJS = null;
            if (!Common.isEmptyArray(results))
            {
                if (!mSearchPrevious)
                {
                    mTerms.clear();
                }
                if (highlightTerms != null)
                {
                    mTerms.addAll(highlightTerms);
                }
            }
        }

        @Override
        protected void cancelPressed()
        {
            mCanceled = true;
            super.cancelPressed();
        }

        @Override
        public int open()
        {
            mCanceled = false;
            return super.open();
        }

        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(SEARCHING_DIALOG_TITLE);
            newShell.setImage(EclipseNSISPlugin.getShellImage());
        }

        @Override
        protected Control createButtonBar(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(1, false);
            layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
            layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
            layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
            layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
            composite.setLayout(layout);
            GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, true);
            composite.setLayoutData(data);
            composite.setFont(parent.getFont());

            createButton(composite, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

            return composite;
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite composite = (Composite) super.createDialogArea(parent);
            Label l = new Label(composite, SWT.NONE);
            l.setText(SEARCHING_DIALOG_MESSAGE);
            l.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
            return composite;
        }

    }
}
