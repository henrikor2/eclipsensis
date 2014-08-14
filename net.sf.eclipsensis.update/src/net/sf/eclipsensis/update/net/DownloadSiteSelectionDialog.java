/*******************************************************************************
 * Copyright (c) 2005-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.update.net;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.update.EclipseNSISUpdatePlugin;
import net.sf.eclipsensis.update.jobs.NSISUpdateURLs;
import net.sf.eclipsensis.update.preferences.IUpdatePreferenceConstants;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.winapi.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class DownloadSiteSelectionDialog extends Dialog
{
    private static final String SAVE_PREFERRED = "savePreferred"; //$NON-NLS-1$
    private static final IPreferenceStore cPreferenceStore = EclipseNSISUpdatePlugin.getDefault().getPreferenceStore();

    /**
     *
     */
    private String mSettingsKey;
    private List<DownloadSite> mDownloadSites;
    private DownloadSite mSelectedSite = null;
    private Button mSavePreferred = null;
    private IDialogSettings mDialogSettings = null;
    private Image mDefaultSiteImage;

    public DownloadSiteSelectionDialog(Shell parentShell, List<DownloadSite> downloadSites, DownloadSite selectedSite)
    {
        this(parentShell, null, downloadSites, selectedSite);
    }

    public DownloadSiteSelectionDialog(Shell parentShell, String settingsKey, List<DownloadSite> downloadSites)
    {
        this(parentShell, settingsKey, downloadSites, null);
    }

    public DownloadSiteSelectionDialog(Shell parentShell, String settingsKey, List<DownloadSite> downloadSites, DownloadSite selectedSite)
    {
        super(parentShell);
        mSettingsKey = settingsKey;
        mDownloadSites = downloadSites;
        mSelectedSite = mDownloadSites.contains(selectedSite)?selectedSite:null;
        initDialogSettings();
    }

    private void initDialogSettings()
    {
        IDialogSettings pluginDialogSettings = EclipseNSISUpdatePlugin.getDefault().getDialogSettings();
        if(!Common.isEmpty(mSettingsKey)) {
            mDialogSettings = pluginDialogSettings.getSection(mSettingsKey);
            if(mDialogSettings == null) {
                mDialogSettings = pluginDialogSettings.addNewSection(mSettingsKey);
            }
        }
    }

    @Override
    protected void configureShell(Shell shell)
    {
        super.configureShell(shell);
        shell.setText(EclipseNSISUpdatePlugin.getResourceString("download.sites.dialog.title")); //$NON-NLS-1$
        shell.setImage(EclipseNSISUpdatePlugin.getShellImage());
    }

    private void makeLabel(Composite parent, Image image, String text, Color bgColor, MouseListener listener, MouseTrackListener listener2)
    {
        Composite parent2 = new Composite(parent,SWT.NONE);
        parent2.setBackground(bgColor);
        parent2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        parent2.addMouseListener(listener);
        if(listener2 != null) {
            parent2.addMouseTrackListener(listener2);
        }
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = layout.marginHeight = 0;
        parent2.setLayout(layout);

        Label l = new Label(parent2,SWT.NONE);
        l.setBackground(bgColor);
        if(image != null) {
            l.setImage(image);
        }
        if(text != null) {
            l.setText(text);
        }
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
        l.addMouseListener(listener);
        if(listener2 != null) {
            l.addMouseTrackListener(listener2);
        }
    }

    @Override
    protected void okPressed()
    {
        if(mDialogSettings != null) {
            mDialogSettings.put(SAVE_PREFERRED, mSavePreferred.getSelection());
            if(mSavePreferred.getSelection()) {
                try {
                    URL siteURL = NSISUpdateURLs.getDownloadURL(mSelectedSite.getName(),"1.0"); //$NON-NLS-1$
                    cPreferenceStore.setValue(IUpdatePreferenceConstants.AUTOSELECT_SOURCEFORGE_MIRROR, false);
                    cPreferenceStore.setValue(IUpdatePreferenceConstants.SOURCEFORGE_MIRROR, siteURL.getHost());
                }
                catch (IOException e) {
                    EclipseNSISUpdatePlugin.getDefault().log(e);
                }
            }
        }
        super.okPressed();
    }

    private Image getDefaultSiteImage()
    {
        if(mDefaultSiteImage == null) {
            mDefaultSiteImage = EclipseNSISUpdatePlugin.getImageDescriptor(EclipseNSISUpdatePlugin.getResourceString("default.download.site.icon")).createImage(getShell().getDisplay()); //$NON-NLS-1$
            getShell().addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    if(mDefaultSiteImage != null && !mDefaultSiteImage.isDisposed()) {
                        mDefaultSiteImage.dispose();
                    }
                }
            });
        }
        return mDefaultSiteImage;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);
        Label l = new Label(composite, SWT.NONE);
        l.setText(EclipseNSISUpdatePlugin.getResourceString("download.sites.dialog.header")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        ScrolledComposite scrolledComposite = new ScrolledComposite(composite,SWT.BORDER|SWT.V_SCROLL);
        final Color white = scrolledComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        scrolledComposite.setBackground(white);

        Composite composite2 = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(composite2);
        composite2.setBackground(white);
        GridLayout layout = new GridLayout(4,false);
        composite2.setLayout(layout);
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                widgetSelected(e);
                okPressed();
            }

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Button button = (Button)e.widget;
                if(button.getSelection()) {
                    mSelectedSite = (DownloadSite)button.getData();
                }
            }
        };
        for (ListIterator<DownloadSite> iter = mDownloadSites.listIterator(); iter.hasNext();) {
            DownloadSite site = iter.next();
            final Button button = new Button(composite2,SWT.RADIO);
            button.setBackground(white);
            button.setSelection(mSelectedSite==null?!iter.hasPrevious():site.equals(mSelectedSite));
            button.setData(site);
            button.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
            button.addSelectionListener(selectionAdapter);

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e)
                {
                    button.setFocus();
                }

                @Override
                public void mouseDoubleClick(MouseEvent e)
                {
                    mouseUp(e);
                    okPressed();
                }
            };

            MouseTrackAdapter mouseTrackAdapter = null;
            if(WinAPI.INSTANCE.areVisualStylesEnabled()) {
                mouseTrackAdapter = new MouseTrackAdapter() {
                    private void paint(int selectedStateId, int unselectedStateId)
                    {
                        GC gc = new GC(button);
                        IHandle handle = Common.getControlHandle(button);
                        IHandle handle2 = Common.getGraphicsHandle(gc);
                        if(button.getSelection()) {
                            WinAPI.INSTANCE.drawWidgetThemeBackGround(handle,handle2,"BUTTON",WinAPI.BP_RADIOBUTTON,selectedStateId); //$NON-NLS-1$
                        }
                        else {
                            WinAPI.INSTANCE.drawWidgetThemeBackGround(handle,handle2,"BUTTON",WinAPI.BP_RADIOBUTTON,unselectedStateId); //$NON-NLS-1$
                        }
                        gc.dispose();
                    }

                    @Override
                    public void mouseEnter(MouseEvent e)
                    {
                        paint(WinAPI.RBS_CHECKEDHOT,WinAPI.RBS_UNCHECKEDHOT);
                    }

                    @Override
                    public void mouseExit(MouseEvent e)
                    {
                        paint(WinAPI.RBS_CHECKEDNORMAL,WinAPI.RBS_UNCHECKEDNORMAL);
                    }
                };
            }

            Image image = site.getImage();
            if(image == null) {
                image = getDefaultSiteImage();
            }
            makeLabel(composite2, image, null, white, mouseAdapter, mouseTrackAdapter);
            makeLabel(composite2, null, site.getLocation(), white, mouseAdapter, mouseTrackAdapter);
            makeLabel(composite2, null, site.getContinent(), white, mouseAdapter, mouseTrackAdapter);

            if(iter.hasNext()) {
                l = new Label(composite2,SWT.HORIZONTAL|SWT.SEPARATOR);
                GridData data = new GridData(SWT.FILL,SWT.FILL,false,false);
                data.horizontalSpan = 4;
                l.setLayoutData(data);
            }
        }

        Point size = composite2.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        composite2.setSize(size);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = Math.min(size.y,300);
        data.widthHint = size.x;
        scrolledComposite.setLayoutData(data);

        if(mDialogSettings != null) {
            mSavePreferred = new Button(composite,SWT.CHECK);
            mSavePreferred.setText(EclipseNSISUpdatePlugin.getResourceString("download.sites.dialog.save.label")); //$NON-NLS-1$
            boolean b = true;
            if(mDialogSettings.get(SAVE_PREFERRED) != null) {
                b = mDialogSettings.getBoolean(SAVE_PREFERRED);
            }
            mSavePreferred.setSelection(b);
            mSavePreferred.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        }
        return composite;
    }

    public DownloadSite getSelectedSite()
    {
        return mSelectedSite;
    }
}