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

import java.io.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.job.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class NSISPreferencePage    extends NSISSettingsPage implements INSISPreferenceConstants
{
    public static final List<String> NSIS_HOMES;

    private static final List<String> cInternalNSISHomes;
    private static File cNSISHomesListFile = new File(EclipseNSISPlugin.getPluginStateLocation(),
                    NSISPreferencePage.class.getName()+".NSISHomesList.ser"); //$NON-NLS-1$
    private static IJobStatusRunnable cSaveNSISHomesRunnable = new IJobStatusRunnable() {
        public IStatus run(IProgressMonitor monitor)
        {
            try {
                IOUtility.writeObject(cNSISHomesListFile,cInternalNSISHomes);
                return Status.OK_STATUS;
            }
            catch (IOException e) {
                EclipseNSISPlugin.getDefault().log(e);
                return new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR,e.getMessage(),e);
            }
        }
    };
    private static Map<File, long[]> cSolidCompressionMap = new HashMap<File, long[]>();
    private static Map<File, long[]> cProcessPriorityMap = new HashMap<File, long[]>();
    private static final String[] cAutoShowConsoleText;
    private static final String[] cBeforeCompileSaveText;

    static {
        Collection<String> nsisHomes = loadNSISHomes();
        cInternalNSISHomes = (List<String>)nsisHomes;
        NSIS_HOMES = Collections.unmodifiableList(cInternalNSISHomes);

        cAutoShowConsoleText = new String[AUTO_SHOW_CONSOLE_ARRAY.length];
        for (int i = 0; i < AUTO_SHOW_CONSOLE_ARRAY.length; i++) {
            cAutoShowConsoleText[i] = EclipseNSISPlugin.getResourceString("auto.show.console."+AUTO_SHOW_CONSOLE_ARRAY[i]); //$NON-NLS-1$
        }

        cBeforeCompileSaveText = new String[BEFORE_COMPILE_SAVE_ARRAY.length];
        for (int i = 0; i < BEFORE_COMPILE_SAVE_ARRAY.length; i++) {
            cBeforeCompileSaveText[i] = EclipseNSISPlugin.getResourceString("before.compile.save."+BEFORE_COMPILE_SAVE_ARRAY[i]); //$NON-NLS-1$
        }
    }

    private static Collection<String> loadNSISHomes()
    {
        Collection<String> nsisHomes;
        if(cNSISHomesListFile.exists()) {
            try {
                nsisHomes = IOUtility.readObject(cNSISHomesListFile);
                if(!(nsisHomes instanceof List<?>)) {
                    nsisHomes = new ArrayList<String>(nsisHomes);
                }
            }
            catch (Exception e1) {
                nsisHomes = new ArrayList<String>();
            }
        }
        else {
            nsisHomes = new ArrayList<String>();
        }
        return nsisHomes;
    }

    public static boolean addNSISHome(String nsisHome)
    {
        return addNSISHome(cInternalNSISHomes,nsisHome);
    }

    private static boolean addNSISHome(List<String> nsisHomesList, String nsisHome)
    {
        if(nsisHomesList.size() > 0) {
            if(nsisHomesList.get(0).equalsIgnoreCase(nsisHome)) {
                return false;
            }
            removeNSISHome(nsisHomesList, nsisHome);
        }
        nsisHomesList.add(0,nsisHome);
        return true;
    }

    public static boolean removeNSISHome(String nsisHome)
    {
        return removeNSISHome(cInternalNSISHomes, nsisHome);
    }

    private static boolean removeNSISHome(List<String> nsisHomesList, String nsisHome)
    {
        for(Iterator<String> iter=nsisHomesList.iterator(); iter.hasNext(); ) {
            if(iter.next().equalsIgnoreCase(nsisHome)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    public static void saveNSISHomes()
    {
        JobScheduler scheduler = EclipseNSISPlugin.getDefault().getJobScheduler();
        scheduler.cancelJobs(NSISPreferencePage.class);
        scheduler.scheduleJob(NSISPreferencePage.class, EclipseNSISPlugin.getResourceString("preferences.save.nsis.homes.job.name"), cSaveNSISHomesRunnable); //$NON-NLS-1$
    }

    public static void show()
    {
        PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        PREFERENCE_PAGE_ID, null, null).open();
    }

    @Override
    protected String getContextId()
    {
        return PLUGIN_CONTEXT_PREFIX + "nsis_prefs_context"; //$NON-NLS-1$
    }

    @Override
    protected String getPageDescription()
    {
        return EclipseNSISPlugin.getResourceString("preferences.header.text"); //$NON-NLS-1$
    }

    @Override
    protected NSISSettingsEditor createSettingsEditor()
    {
        return new PreferencesEditor();
    }


    private class PreferencesEditor extends NSISSettingsEditor
    {
        private NSISExe mNSISExe = null;

        @Override
        public boolean isValid()
        {
            return mNSISExe != null;
        }

        @Override
        protected NSISSettingsEditorGeneralPage createGeneralPage()
        {
            return new PreferencesEditorGeneralPage(getSettings());
        }

        @Override
        protected NSISSettings loadSettings()
        {
            NSISHome home = NSISPreferences.getInstance().getNSISHome();
            mNSISExe = home==null?null:home.getNSISExe();
            return NSISPreferences.getInstance();
        }

        private class PreferencesEditorGeneralPage extends NSISSettingsEditorGeneralPage
        {
            private ComboViewer mNSISHome = null;
            private Button mUseEclipseHelp = null;
            private Combo mAutoShowConsole = null;
            private Button mNotifyMakeNSISChanged = null;
            private boolean mNSISHomeDirty = false;
            private boolean mHandlingNSISHomeChange = false;
            private Button mWarnProcessPriority = null;
            private Combo mBeforeCompileSave = null;

            public PreferencesEditorGeneralPage(NSISSettings settings)
            {
                super(settings);
            }

            protected boolean isSolidCompressionSupported()
            {
                if(mNSISExe != null && mNSISExe.getVersion().compareTo(INSISVersions.VERSION_2_07) >= 0) {
                    File exeFile = mNSISExe.getFile();
                    if(IOUtility.isValidFile(exeFile)) {
                        long[] data = cSolidCompressionMap.get(exeFile);
                        if(data != null) {
                            if(data[0] == exeFile.lastModified() && data[1] == exeFile.length()) {
                                return data[2] == 1;
                            }
                        }
                        else {
                            data = new long[3];
                        }
                        data[0] = exeFile.lastModified();
                        data[1] = exeFile.length();
                        data[2] = 0;
                        Properties definedSymbols = mNSISExe.getDefinedSymbols();
                        if(definedSymbols.containsKey(NSISPreferences.NSIS_CONFIG_COMPRESSION_SUPPORT)) {
                            data[2] = 1;
                        }
                        cSolidCompressionMap.put(exeFile,data);
                        return data[2] == 1;
                    }
                }
                return false;
            }

            @Override
            protected boolean isProcessPrioritySupported()
            {
                if(mNSISExe != null && mNSISExe.getVersion().compareTo(INSISVersions.VERSION_2_24) >= 0) {
                    File exeFile = mNSISExe.getFile();
                    if(IOUtility.isValidFile(exeFile)) {
                        long[] data = cProcessPriorityMap.get(exeFile);
                        if(data != null) {
                            if(data[0] == exeFile.lastModified() && data[1] == exeFile.length()) {
                                return true;
                            }
                        }
                        else {
                            data = new long[2];
                        }
                        data[0] = exeFile.lastModified();
                        data[1] = exeFile.length();
                        cProcessPriorityMap.put(exeFile,data);
                        return true;
                    }
                }
                return false;
            }

            private boolean handleNSISHomeChange(boolean eraseInvalid)
            {
                if(mNSISHomeDirty && !mHandlingNSISHomeChange) {
                    try {
                        mHandlingNSISHomeChange = true;
                        boolean state = false;
                        String nsisHome = mNSISHome.getCombo().getText();
                        if(!Common.isEmpty(nsisHome)) {
                            if(nsisHome.endsWith("\\") && !nsisHome.endsWith(":\\")) { //$NON-NLS-1$ //$NON-NLS-2$
                                nsisHome = nsisHome.substring(0,nsisHome.length()-1);
                                mNSISHome.getCombo().setText(nsisHome);
                            }
                            NSISExe nsisExe = NSISValidator.findNSISExe(new File(nsisHome));
                            if (nsisExe == null) {
                                if(eraseInvalid) {
                                    Common.openError(getShell(),
                                                    EclipseNSISPlugin.getResourceString("invalid.nsis.home.message"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                    mNSISHome.getCombo().setText(""); //$NON-NLS-1$
                                    mNSISHome.getCombo().forceFocus();
                                    mNSISHomeDirty = false;
                                }
                                mNSISExe = null;
                                mSolidCompression.setVisible(false);
                                setProcessPriorityVisible(false);
                            }
                            else {
                                state = true;
                                mNSISExe = nsisExe;
                                mSolidCompression.setVisible(isSolidCompressionSupported());
                                setProcessPriorityVisible(isProcessPrioritySupported());
                                mNSISHomeDirty = false;
                            }
                        }
                        else {
                            mNSISHomeDirty = false;
                        }
                        setValid(state);
                        enableControls(state);
                        return state;
                    }
                    finally {
                        mHandlingNSISHomeChange = false;
                        fireChanged();
                    }
                }
                return true;
            }

            @Override
            public void setDefaults()
            {
                super.setDefaults();
                mUseEclipseHelp.setSelection(true);
                mAutoShowConsole.select(getAutoShowConsoleIndex(AUTO_SHOW_CONSOLE_DEFAULT));
                mBeforeCompileSave.select(getBeforeCompileSaveIndex(BEFORE_COMPILE_SAVE_DEFAULT));
                mNotifyMakeNSISChanged.setSelection(false);
                mWarnProcessPriority.setSelection(true);
            }

            private int getAutoShowConsoleIndex(int autoShowConsole)
            {
                for (int i = 0; i < AUTO_SHOW_CONSOLE_ARRAY.length; i++) {
                    if(AUTO_SHOW_CONSOLE_ARRAY[i]==autoShowConsole) {
                        return i;
                    }
                }
                return 0;
            }

            private int getBeforeCompileSaveIndex(int beforeCompileSave)
            {
                for (int i = 0; i < BEFORE_COMPILE_SAVE_ARRAY.length; i++) {
                    if(BEFORE_COMPILE_SAVE_ARRAY[i]==beforeCompileSave) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void reset()
            {
                NSISPreferences prefs = (NSISPreferences)getSettings();
                NSISHome nsisHome = prefs.getNSISHome();
                mNSISHome.getCombo().setText(nsisHome==null?"":nsisHome.getLocation().getAbsolutePath());
                mUseEclipseHelp.setSelection(prefs.isUseEclipseHelp());
                mAutoShowConsole.select(getAutoShowConsoleIndex(prefs.getAutoShowConsole()));
                mBeforeCompileSave.select(getBeforeCompileSaveIndex(prefs.getBeforeCompileSave()));
                mNotifyMakeNSISChanged.setSelection(prefs.getBoolean(NOTIFY_MAKENSIS_CHANGED));
                mWarnProcessPriority.setSelection(prefs.getPreferenceStore().getBoolean(WARN_PROCESS_PRIORITY));
                super.reset();
            }

            @SuppressWarnings("unchecked")
            @Override
            protected boolean performApply(NSISSettings settings)
            {
                if (getControl() != null) {
                    if(!handleNSISHomeChange(true)) {
                        return false;
                    }
                    if(super.performApply(settings)) {
                        Combo combo = mNSISHome.getCombo();
                        String home = combo.getText();

                        List<String> nsisHomes = (List<String>)mNSISHome.getInput();
                        if (addNSISHome(nsisHomes, home)) {
                            mNSISHome.refresh();
                            combo.setText(home);
                        }

                        boolean dirty = false;
                        if (cInternalNSISHomes.size() == nsisHomes.size()) {
                            ListIterator<String> e1 = cInternalNSISHomes.listIterator();
                            ListIterator<String> e2 = nsisHomes.listIterator();
                            while (e1.hasNext() && e2.hasNext()) {
                                String s1 = e1.next();
                                String s2 = e2.next();
                                if (!Common.stringsAreEqual(s1, s2, true)) {
                                    dirty = true;
                                    break;
                                }
                            }
                        }
                        else {
                            dirty = true;
                        }
                        if (dirty) {
                            cInternalNSISHomes.clear();
                            cInternalNSISHomes.addAll(nsisHomes);
                            saveNSISHomes();
                        }

                        NSISPreferences preferences = (NSISPreferences)settings;
                        preferences.setNSISHome(home);
                        preferences.setAutoShowConsole(AUTO_SHOW_CONSOLE_ARRAY[mAutoShowConsole.getSelectionIndex()]);
                        preferences.setBeforeCompileSave(BEFORE_COMPILE_SAVE_ARRAY[mBeforeCompileSave.getSelectionIndex()]);
                        preferences.setUseEclipseHelp(mUseEclipseHelp.getSelection());
                        preferences.setValue(NOTIFY_MAKENSIS_CHANGED, mNotifyMakeNSISChanged.getSelection());
                        preferences.getPreferenceStore().setValue(WARN_PROCESS_PRIORITY, mWarnProcessPriority.getSelection());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void enableControls(boolean state)
            {
                enableControl(mAutoShowConsole, state);
                enableControl(mBeforeCompileSave, state);
                enableControl(mUseEclipseHelp, state);
                enableControl(mNotifyMakeNSISChanged, state);
                enableControl(mWarnProcessPriority, state);
                super.enableControls(state);
            }

            /**
             * @param state
             */
            private void enableControl(Control control, boolean state)
            {
                control.setEnabled(state);
                Object o = control.getData(LABEL);
                if(o instanceof Control && !((Control)o).isDisposed()) {
                    ((Control)o).setEnabled(state);
                }
            }

            @Override
            public boolean canEnableControls()
            {
                return !Common.isEmpty(mNSISHome.getCombo().getText());
            }

            @Override
            protected void createProcessPriorityCombo(Composite parent)
            {
                super.createProcessPriorityCombo(parent);
                mWarnProcessPriority = new Button(parent,SWT.CHECK);
                mWarnProcessPriority.setText(EclipseNSISPlugin.getResourceString("warn.process.priority.label")); //$NON-NLS-1$
                mWarnProcessPriority.setSelection(NSISPreferences.getInstance().getPreferenceStore().getBoolean(WARN_PROCESS_PRIORITY));
                GridData data = new GridData(SWT.FILL,SWT.FILL,true,false);
                data.horizontalIndent=20;
                data.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
                mWarnProcessPriority.setLayoutData(data);
            }

            @Override
            protected void internalSetProcessPriorityVisible(boolean visible)
            {
                super.internalSetProcessPriorityVisible(visible);
                if(mWarnProcessPriority != null && !mWarnProcessPriority.isDisposed()) {
                    mWarnProcessPriority.setVisible(visible);
                    ((GridData)mWarnProcessPriority.getLayoutData()).exclude = !visible;
                }
            }

            @Override
            protected boolean isWarnProcessPriority()
            {
                if(mWarnProcessPriority != null && !mWarnProcessPriority.isDisposed()) {
                    return mWarnProcessPriority.getSelection();
                }
                return super.isWarnProcessPriority();
            }

            @Override
            protected void setWarnProcessPriority(boolean warnProcessPriority)
            {
                if(mWarnProcessPriority != null && !mWarnProcessPriority.isDisposed()) {
                    mWarnProcessPriority.setSelection(warnProcessPriority);
                }
                else {
                    super.setWarnProcessPriority(warnProcessPriority);
                }
            }

            @Override
            protected Composite createMasterControl(Composite parent)
            {
                Composite composite = new Composite(parent,SWT.NONE);
                GridLayout layout = new GridLayout(3,false);
                layout.marginWidth = 0;
                composite.setLayout(layout);

                Label label = new Label(composite, SWT.LEFT);
                label.setText(EclipseNSISPlugin.getResourceString("nsis.home.text")); //$NON-NLS-1$
                GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
                label.setLayoutData(data);

                Combo c = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
                c.setToolTipText(EclipseNSISPlugin.getResourceString("nsis.home.tooltip")); //$NON-NLS-1$
                data = new GridData(SWT.FILL, SWT.CENTER, true, false);
                c.setLayoutData(data);

                List<String> nsisHomes = new ArrayList<String>(cInternalNSISHomes);
                String home = "";
                NSISHome nsisHome = NSISPreferences.getInstance().getNSISHome();
                if (nsisHome != null)
                {
                    home = nsisHome.getLocation().getAbsolutePath();
                }
                addNSISHome(nsisHomes, home);

                mNSISHome = new ComboViewer(c);
                mNSISHome.setContentProvider(new CollectionContentProvider());
                mNSISHome.setLabelProvider(new CollectionLabelProvider());
                mNSISHome.setInput(nsisHomes);

                c.setText(home);
                c.addModifyListener(new ModifyListener(){
                    public void modifyText(ModifyEvent e)
                    {
                        mNSISHomeDirty = true;
                    }
                });
                c.addSelectionListener(new SelectionAdapter(){
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        mNSISHomeDirty = true;
                        handleNSISHomeChange(true);
                    }
                });
                c.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e)
                    {
                        handleNSISHomeChange(false);
                    }
                });

                Button button = createButton(composite, EclipseNSISPlugin.getResourceString("browse.text"), //$NON-NLS-1$
                                EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
                button.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        Shell shell = getShell();
                        DirectoryDialog dialog = new DirectoryDialog(shell);
                        dialog.setMessage(EclipseNSISPlugin.getResourceString("nsis.home.message")); //$NON-NLS-1$
                        String text = mNSISHome.getCombo().getText();
                        dialog.setFilterPath(text);
                        String nsisHome = dialog.open();
                        if (!Common.isEmpty(nsisHome) && !Common.stringsAreEqual(nsisHome, text)) {
                            if(NSISValidator.findNSISExe(new File(nsisHome)) != null) {
                                mNSISHome.getCombo().setText(nsisHome);
                                mNSISHomeDirty = true;
                                handleNSISHomeChange(false);
                            }
                            else {
                                Common.openError(getShell(), EclipseNSISPlugin.getResourceString("invalid.nsis.home.message"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                                if(!Common.isEmpty(text)) {
                                    mNSISHome.getCombo().setText(""); //$NON-NLS-1$
                                    mNSISHomeDirty = true;
                                    handleNSISHomeChange(false);
                                }
                                else {
                                    enableControls(false);
                                }
                                mNSISHome.getCombo().setFocus();
                            }
                        }
                    }
                });

                Composite composite2 = new Composite(composite,SWT.None);
                data = new GridData(SWT.FILL,SWT.FILL,false,false);
                data.horizontalSpan = 3;
                composite2.setLayoutData(data);
                layout = new GridLayout(2,false);
                layout.marginWidth = 0;
                layout.marginHeight = 0;
                composite2.setLayout(layout);
                mAutoShowConsole = createCombo(composite2, EclipseNSISPlugin.getResourceString("auto.show.console.text"), //$NON-NLS-1$
                                EclipseNSISPlugin.getResourceString("auto.show.console.tooltip"), //$NON-NLS-1$
                                cAutoShowConsoleText,getAutoShowConsoleIndex(((NSISPreferences)getSettings()).getAutoShowConsole()));

                mUseEclipseHelp = createCheckBox(composite, EclipseNSISPlugin.getResourceString("use.eclipse.help.text"), //$NON-NLS-1$
                                EclipseNSISPlugin.getResourceString("use.eclipse.help.tooltip"), //$NON-NLS-1$
                                ((NSISPreferences)getSettings()).isUseEclipseHelp());
                ((GridData)mUseEclipseHelp.getLayoutData()).horizontalSpan = 3;

                mNotifyMakeNSISChanged = createCheckBox(composite, EclipseNSISPlugin.getResourceString("notify.makensis.changed.text"), //$NON-NLS-1$
                                EclipseNSISPlugin.getResourceString("notify.makensis.changed.tooltip"), //$NON-NLS-1$
                                NSISPreferences.getInstance().getPreferenceStore().getBoolean(NOTIFY_MAKENSIS_CHANGED));
                ((GridData)mNotifyMakeNSISChanged.getLayoutData()).horizontalSpan = 3;

                mBeforeCompileSave = createCombo(composite, EclipseNSISPlugin.getResourceString("before.compile.save.text"), //$NON-NLS-1$
                                EclipseNSISPlugin.getResourceString("before.compile.save.tooltip"), //$NON-NLS-1$
                                cBeforeCompileSaveText,getBeforeCompileSaveIndex(((NSISPreferences)getSettings()).getBeforeCompileSave()));
                return composite;
            }
        }
    }
}