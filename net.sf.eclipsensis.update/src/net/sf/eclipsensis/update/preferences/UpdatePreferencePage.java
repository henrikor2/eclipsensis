/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.preferences;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISVersions;
import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.update.net.DownloadSite;
import net.sf.eclipsensis.update.net.DownloadSiteSelectionDialog;
import net.sf.eclipsensis.update.net.NetworkUtil;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class UpdatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
        IUpdatePreferenceConstants
{
    private static final String LINK_TEXT;

    private boolean mRefreshedSites = false;
    private Map<DownloadSite, byte[]> mCachedAddresses = new HashMap<DownloadSite, byte[]>();
    private Text mNSISUpdateSite;
    private Text mSourceforgeMirror;
    private Button mIgnorePreview;
    private Button mAutoSelectMirror;
    private Button mManualSelectMirror;
    private String mLatestVersion = null;
    private static Pattern cIPAddressRegex = Pattern
            .compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"); //$NON-NLS-1$

    static
    {
        LINK_TEXT = new MessageFormat(EclipseNSISUpdatePlugin.getResourceString("preference.page.proxy.link.text")).format(new String[] { "org.eclipse.ui.net.NetPreferences" }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private ModifyListener mModifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e)
        {
            updateState();
        }
    };
    private Button mSelectSourceforgeMirror;

    @Override
    protected IPreferenceStore doGetPreferenceStore()
    {
        return EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Composite parent2 = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        parent2.setLayout(layout);

        Label l = new Label(parent2, SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("preference.page.header")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Group group1 = createSitesGroup(parent2);
        Group group2 = createOptionsGroup(parent2);
        Link link = new Link(parent2, SWT.WRAP);
        link.setText(LINK_TEXT);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (e.text != null)
                {
                    PreferencesUtil.createPreferenceDialogOn(getShell(), e.text, null, null);
                }
            }
        });
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        Point size1 = group1.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point size2 = group2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        gridData.widthHint = Math.max(size1.x, size2.x);
        link.setLayoutData(gridData);

        updateState();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent2,
                EclipseNSISUpdatePlugin.PLUGIN_CONTEXT_PREFIX + "nsis_update_prefs_context"); //$NON-NLS-1$
        return parent2;
    }

    private void loadDefaults()
    {
        IPreferenceStore prefs = getPreferenceStore();

        mNSISUpdateSite.setText(prefs.getDefaultString(NSIS_UPDATE_SITE));
        mSourceforgeMirror.setText(prefs.getDefaultString(SOURCEFORGE_MIRROR));
        boolean autoSelect = prefs.getDefaultBoolean(AUTOSELECT_SOURCEFORGE_MIRROR);
        mAutoSelectMirror.setSelection(autoSelect);
        mManualSelectMirror.setSelection(!autoSelect);
        updateMirrorSelector(!autoSelect);

        mIgnorePreview.setSelection(prefs.getDefaultBoolean(IGNORE_PREVIEW));
    }

    private void savePreferences()
    {
        IPreferenceStore prefs = getPreferenceStore();

        prefs.setValue(NSIS_UPDATE_SITE, mNSISUpdateSite.getText());
        prefs.setValue(SOURCEFORGE_MIRROR, mSourceforgeMirror.getText());
        prefs.setValue(AUTOSELECT_SOURCEFORGE_MIRROR, mAutoSelectMirror.getSelection());

        prefs.setValue(IGNORE_PREVIEW, mIgnorePreview.getSelection());
    }

    private Group createSitesGroup(Composite parent)
    {
        IPreferenceStore prefs = getPreferenceStore();

        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("sites.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(2, false));

        Label l = new Label(group, SWT.NONE);
        Font f = l.getFont();
        FontData[] fd = f.getFontData();
        for (int i = 0; i < fd.length; i++)
        {
            fd[i].setStyle(fd[i].getStyle() | SWT.BOLD);
        }
        final Font f2 = new Font(getShell().getDisplay(), fd);
        l.setFont(f2);
        l.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                f2.dispose();
            }
        });
        l.setText(EclipseNSISUpdatePlugin.getResourceString("sites.group.message")); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        l.setLayoutData(gridData);

        l = new Label(group, SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("nsis.update.site.label")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        mNSISUpdateSite = new Text(group, SWT.BORDER | SWT.SINGLE);
        mNSISUpdateSite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        mNSISUpdateSite.setText(prefs.getString(NSIS_UPDATE_SITE));
        mNSISUpdateSite.addModifyListener(mModifyListener);

        group = new Group(group, SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = 2;
        group.setLayoutData(gridData);
        group.setText(EclipseNSISUpdatePlugin.getResourceString("sourceforge.mirror.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(3, false));

        mAutoSelectMirror = new Button(group, SWT.RADIO);
        mAutoSelectMirror.setText(EclipseNSISUpdatePlugin.getResourceString("autoselect.sourceforge.mirror")); //$NON-NLS-1$
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 3;
        mAutoSelectMirror.setLayoutData(gridData);

        mManualSelectMirror = new Button(group, SWT.RADIO);
        mManualSelectMirror.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        boolean autoSelect = prefs.getBoolean(AUTOSELECT_SOURCEFORGE_MIRROR);
        mAutoSelectMirror.setSelection(autoSelect);
        mManualSelectMirror.setSelection(!autoSelect);

        mSourceforgeMirror = new Text(group, SWT.BORDER | SWT.SINGLE);
        mSourceforgeMirror.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        mSourceforgeMirror.setText(prefs.getString(SOURCEFORGE_MIRROR));

        mSourceforgeMirror.addModifyListener(mModifyListener);

        mSelectSourceforgeMirror = new Button(group, SWT.PUSH);
        mSelectSourceforgeMirror.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        mSelectSourceforgeMirror.setToolTipText(EclipseNSISUpdatePlugin
                .getResourceString("download.sites.dialog.title")); //$NON-NLS-1$

        updateMirrorSelector(!autoSelect);

        mSelectSourceforgeMirror.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
                    public void run()
                    {
                        List<DownloadSite> downloadSites = getDownloadSites(false);
                        if (!Common.isEmptyCollection(downloadSites))
                        {
                            DownloadSite selectedSite = null;
                            String str = mSourceforgeMirror.getText();
                            if (!Common.isEmpty(str))
                            {
                                selectedSite = getSelectedDownloadSite(downloadSites, str);
                            }
                            DownloadSiteSelectionDialog dialog = new DownloadSiteSelectionDialog(getShell(),
                                    downloadSites, selectedSite);
                            if (dialog.open() == Window.OK)
                            {
                                selectedSite = dialog.getSelectedSite();
                                try
                                {
                                    mSourceforgeMirror.setText(NSISUpdateURLs.getGenericDownloadURL(
                                            selectedSite.getName(), "1.0").getHost()); //$NON-NLS-1$
                                }
                                catch (IOException e1)
                                {
                                    EclipseNSISUpdatePlugin.getDefault().log(e1);
                                }
                            }
                        }
                        else
                        {
                            Common.openError(getShell(), EclipseNSISUpdatePlugin
                                    .getResourceString("no.sourceforge.mirrors.error"), //$NON-NLS-1$
                                    EclipseNSISUpdatePlugin.getShellImage());
                        }
                    }
                });
            }
        });

        SelectionAdapter adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                updateMirrorSelector(e.widget == mManualSelectMirror);
            }
        };
        mAutoSelectMirror.addSelectionListener(adapter);
        mManualSelectMirror.addSelectionListener(adapter);
        return group;
    }

    private void updateMirrorSelector(boolean flag)
    {
        mSourceforgeMirror.setEnabled(flag);
        mSelectSourceforgeMirror.setEnabled(flag);
    }

    private Group createOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        group.setText(EclipseNSISUpdatePlugin.getResourceString("update.options.group.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1, false));

        mIgnorePreview = new Button(group, SWT.CHECK);
        mIgnorePreview.setText(EclipseNSISUpdatePlugin.getResourceString("ignore.preview.label")); //$NON-NLS-1$
        mIgnorePreview.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        mIgnorePreview.setSelection(getPreferenceStore().getBoolean(IGNORE_PREVIEW));

        return group;
    }

    private void updateState()
    {
        setValid(validate());
    }

    private boolean validate()
    {
        final String nsisUpdateSite = mNSISUpdateSite.getText();
        if (nsisUpdateSite == null || nsisUpdateSite.trim().length() == 0)
        {
            setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("missing.nsis.update.site.error")); //$NON-NLS-1$
            return false;
        }
        if (!mAutoSelectMirror.getSelection() || mManualSelectMirror.getSelection())
        {
            final String site = mSourceforgeMirror.getText();
            if (site == null || site.trim().length() == 0)
            {
                setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("missing.sourceforge.mirror.error")); //$NON-NLS-1$
                return false;
            }
            if (site.indexOf('/') >= 0 || site.indexOf(':') >= 0)
            {
                setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("invalid.sourceforge.mirror.format.error")); //$NON-NLS-1$
                return false;
            }
            IPreferenceStore prefs = getPreferenceStore();
            if (prefs.getBoolean(AUTOSELECT_SOURCEFORGE_MIRROR)
                    || !Common.stringsAreEqual(site, prefs.getString(SOURCEFORGE_MIRROR)))
            {
                final boolean[] retValue = { true };
                BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
                    public void run()
                    {
                        List<DownloadSite> downloadSites = getDownloadSites(false);
                        if (getSelectedDownloadSite(downloadSites, site) == null)
                        {
                            if (!mRefreshedSites
                                    && Common.openQuestion(getShell(), EclipseNSISUpdatePlugin
                                            .getResourceString("invalid.sourceforge.mirror.message"), //$NON-NLS-1$
                                            EclipseNSISUpdatePlugin.getShellImage()))
                            {
                                downloadSites = getDownloadSites(true);
                                mRefreshedSites = true;
                                if (getSelectedDownloadSite(downloadSites, site) == null)
                                {
                                    retValue[0] = false;
                                }
                            }
                            else
                            {
                                retValue[0] = false;
                            }
                        }
                    }
                });
                if (!retValue[0])
                {
                    setErrorMessage(EclipseNSISUpdatePlugin.getResourceString("invalid.sourceforge.mirror.error")); //$NON-NLS-1$
                    return false;
                }
            }
        }
        setErrorMessage(null);
        return true;
    }

    private DownloadSite getSelectedDownloadSite(List<DownloadSite> downloadSites, String downloadSite)
    {
        byte[] ipAddress = parseIPAddress(downloadSite);
        for (Iterator<DownloadSite> iter = downloadSites.iterator(); iter.hasNext();)
        {
            DownloadSite site = iter.next();
            try
            {
                String sitehost = NSISUpdateURLs.getGenericDownloadURL(site.getName(), "1.0").getHost(); //$NON-NLS-1$
                if (ipAddress != null)
                {
                    byte[] siteAddress = mCachedAddresses.get(site);
                    if (siteAddress == null)
                    {
                        InetAddress address = InetAddress.getByName(sitehost);
                        siteAddress = address.getAddress();
                        mCachedAddresses.put(site, siteAddress);
                    }
                    if (Arrays.equals(ipAddress, siteAddress))
                    {
                        return site;
                    }

                }
                else if (downloadSite.equalsIgnoreCase(sitehost))
                {
                    return site;
                }
            }
            catch (IOException e1)
            {
            }
        }
        return null;
    }

    private byte[] parseIPAddress(String address)
    {
        try
        {
            Matcher matcher = cIPAddressRegex.matcher(address);
            if (matcher.matches())
            {
                InetAddress addr = InetAddress.getByName(address);
                return addr.getAddress();
            }
        }
        catch (Exception e)
        {
        }
        return null;
    }

    @Override
    public boolean performOk()
    {
        boolean ok = super.performOk();
        if (ok)
        {
            ok = validate();
            if (ok)
            {
                savePreferences();
            }
        }
        return ok;
    }

    @Override
    protected void performDefaults()
    {
        loadDefaults();
        super.performDefaults();
    }

    public void init(IWorkbench workbench)
    {
    }

    private List<DownloadSite> getDownloadSites(boolean forceRefresh)
    {
        List<DownloadSite> downloadSites = null;
        HttpURLConnection connection = null;
        try {
            if(mLatestVersion == null)
            {
                String version = INSISVersions.MINIMUM_VERSION
                        .toString();
                URL url = NSISUpdateURLs.getUpdateURL(version);
                connection = NetworkUtil
                        .makeConnection(new NullProgressMonitor(),
                                url, null);
                String[] result = NetworkUtil.getLatestVersion(connection);
                mLatestVersion = result[1];
            }
            downloadSites = NetworkUtil.getDownloadSites(mLatestVersion, new NullProgressMonitor(),
                            EclipseNSISUpdatePlugin.getResourceString("retrieving.sourceforge.mirrors"), null, forceRefresh); //$NON-NLS-1$
        }
        catch (IOException e) {
            EclipseNSISUpdatePlugin.getDefault().log(e);
        }
        finally
        {
            IOUtility.closeIO(connection);
        }
        return downloadSites;
    }
}
