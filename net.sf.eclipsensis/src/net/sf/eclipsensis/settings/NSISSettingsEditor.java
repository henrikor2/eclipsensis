/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public abstract class NSISSettingsEditor implements INSISSettingsEditorPageListener
{
    private NSISSettings mSettings = null;
    private Collection<NSISSettingsEditorPage> mPages = new ArrayList<NSISSettingsEditorPage>();
    private TabFolder mFolder = null;
    private boolean mEnabledState;

    public void settingsChanged()
    {
        boolean enabledState = getEnabledState();
        if(enabledState != mEnabledState) {
            mEnabledState = enabledState;
            enableControls();
        }
    }

    /**
     * @return
     */
    private boolean getEnabledState()
    {
        boolean enabledState = true;
        for (Iterator<NSISSettingsEditorPage> iter=mPages.iterator(); iter.hasNext(); ) {
            NSISSettingsEditorPage page = iter.next();
            if(page != null && page.supportsEnablement()) {
                enabledState = enabledState && page.canEnableControls();
            }
        }
        return enabledState;
    }

    private void enableControls()
    {
        for (Iterator<NSISSettingsEditorPage> iter=mPages.iterator(); iter.hasNext(); ) {
            NSISSettingsEditorPage page = iter.next();
            if(page != null && page.supportsEnablement()) {
                page.enableControls(mEnabledState);
                TabItem tabItem = (TabItem)mFolder.getData(page.getName());
                if(tabItem != null && !tabItem.isDisposed()) {
                    tabItem.getControl().setEnabled(mEnabledState);
                }
            }
        }
    }

    protected void addPage(TabFolder folder, String text, String tooltip, NSISSettingsEditorPage page)
    {
        page.addListener(this);
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(EclipseNSISPlugin.getResourceString(text));
        item.setToolTipText(EclipseNSISPlugin.getResourceString(tooltip));
        item.setControl(page.create(folder));
        mPages.add(page);
        folder.setData(page.getName(), item);
    }

    public Control createControl(Composite parent)
    {
        final TabFolder folder = new TabFolder(parent, SWT.NONE);

        addPages(folder);

        mEnabledState = getEnabledState();

        folder.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                try {
                    TabItem item = folder.getSelection()[0];
                    if(!item.getControl().isEnabled()) {
                        folder.setSelection(0);
                    }
                }
                catch(Exception ex) {
                    EclipseNSISPlugin.getDefault().log(ex);
                }
            }
        });

        Dialog.applyDialogFont(folder);
        mFolder = folder;
        enableControls();
        return mFolder;
    }

    /**
     * @param folder
     */
    protected void addPages(final TabFolder folder)
    {
        NSISSettingsEditorGeneralPage page = createGeneralPage();
        addPage(folder, "general.tab.text", "general.tab.tooltip", page); //$NON-NLS-1$ //$NON-NLS-2$
        folder.setData(page.getName(),null); //Don't want to disable this tab ever
        addPage(folder, "symbols.tab.text", "symbols.tab.tooltip", createSymbolsPage()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected NSISSettingsEditorSymbolsPage createSymbolsPage()
    {
        return new NSISSettingsEditorSymbolsPage(getSettings());
    }

    protected abstract NSISSettingsEditorGeneralPage createGeneralPage();

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public final boolean performApply()
    {
        for (Iterator<NSISSettingsEditorPage> iter=mPages.iterator(); iter.hasNext(); ) {
            NSISSettingsEditorPage page = iter.next();
            if(page != null) {
                if(!page.performApply()) {
                    return false;
                }
            }
        }
        getSettings().store();
        return true;
    }

    public final void performDefaults()
    {
        for (Iterator<NSISSettingsEditorPage> iter=mPages.iterator(); iter.hasNext(); ) {
            NSISSettingsEditorPage page = iter.next();
            if(page != null) {
                page.setDefaults();
            }
        }
    }

    public boolean isValid()
    {
        return true;
    }

    /**
     * @return Returns the settings.
     */
    public NSISSettings getSettings()
    {
        if(mSettings == null) {
            mSettings = loadSettings();
        }
        return mSettings;
    }

    protected abstract NSISSettings loadSettings();
}
