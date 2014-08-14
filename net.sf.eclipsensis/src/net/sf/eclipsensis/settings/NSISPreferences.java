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

import java.io.*;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.*;
import net.sf.eclipsensis.editor.NSISTaskTag;
import net.sf.eclipsensis.editor.text.NSISSyntaxStyle;
import net.sf.eclipsensis.filemon.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public class NSISPreferences extends NSISSettings implements IFileChangeListener, INSISEditorPreferenceConstants
{
    public static final RGB SYNTAX_COMMENTS = new RGB(0x7f,0x9f,0xbf);
    public static final RGB SYNTAX_ATTRIBUTES = new RGB(0x80,0,0);
    public static final RGB SYNTAX_COMMANDS = new RGB(0x64,0x32,0);
    public static final RGB SYNTAX_SYMBOLS = new RGB(0x48,0x7,0x70);
    public static final RGB SYNTAX_USER_VARIABLES = new RGB(0x48,0x7,0x70);
    public static final RGB SYNTAX_PARAMETERS = new RGB(0x48,0x7,0x70);
    public static final RGB SYNTAX_INSTRUCTIONS = new RGB(0x8c,0x0,0x46);
    public static final RGB SYNTAX_OPTIONS = new RGB(0x53,0x53,0x0);
    public static final RGB SYNTAX_PREDEF_VARIABLES = new RGB(0x42,0x53,0x2f);
    public static final RGB SYNTAX_STRINGS = new RGB(0x0,0x42,0x42);
    public static final RGB SYNTAX_CALLBACKS = new RGB(0,0,0x80);
    public static final RGB SYNTAX_LANGSTRINGS = new RGB(0x61,0x31,0x1e);
    public static final RGB SYNTAX_TASK_TAGS = new RGB(0x0,0x50,0x50);
    public static final RGB SYNTAX_PLUGINS   = new RGB(0x54,0x4a,0x3d);

    private static volatile NSISPreferences cInstance = null;

    public static final String NSIS_CONFIG_COMPRESSION_SUPPORT="NSIS_CONFIG_COMPRESSION_SUPPORT"; //$NON-NLS-1$
    public static final String NSIS_UNICODE_SUPPORT="NSIS_UNICODE"; //$NON-NLS-1$

    private IPreferenceStore mPreferenceStore = null;
    private NSISHome mNSISHome = null;
    private boolean mUseEclipseHelp = false;
    private int mAutoShowConsole = AUTO_SHOW_CONSOLE_DEFAULT;
    private int mBeforeCompileSave = BEFORE_COMPILE_SAVE_DEFAULT;
    private Collection<NSISTaskTag> mTaskTags = null;
    private Collection<NSISTaskTag> mDefaultTaskTags = null;
    private boolean mCaseSensitiveTaskTags = true;
    private List<INSISHomeListener> mListeners = new ArrayList<INSISHomeListener>();

    private static NSISPreferences loadInstance()
    {
        IPreferenceStore preferenceStore = EclipseNSISPlugin.getDefault().getPreferenceStore();
        NSISPreferences instance = new NSISPreferences(preferenceStore);
        instance.load();
        return instance;
    }

    public static NSISPreferences getInstance()
    {
        if(cInstance == null)
        {
            synchronized(NSISPreferences.class)
            {
                if(cInstance == null)
                {
                    cInstance = loadInstance();
                }
            }
        }
        return cInstance;
    }

    protected NSISPreferences(IPreferenceStore preferenceStore)
    {
        mPreferenceStore = preferenceStore;
    }

    public void addListener(INSISHomeListener listener)
    {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(INSISHomeListener listener)
    {
        mListeners.remove(listener);
    }

    /**
     * @return Returns the preferenceStore.
     */
    public IPreferenceStore getPreferenceStore()
    {
        return mPreferenceStore;
    }

    private void initializePreference(String name, Object defaultValue)
    {
        if(defaultValue != null) {
            Class<? extends Object> type = defaultValue.getClass();
            if(type.equals(Integer.class)) {
                mPreferenceStore.setDefault(name,((Integer)defaultValue).intValue());
            }
            else if(type.equals(Long.class)) {
                mPreferenceStore.setDefault(name,((Long)defaultValue).longValue());
            }
            else if(type.equals(Float.class)) {
                mPreferenceStore.setDefault(name,((Float)defaultValue).floatValue());
            }
            else if(type.equals(Double.class)) {
                mPreferenceStore.setDefault(name,((Double)defaultValue).doubleValue());
            }
            else if(type.equals(Boolean.class)) {
                mPreferenceStore.setDefault(name,((Boolean)defaultValue).booleanValue());
            }
            else {
                mPreferenceStore.setDefault(name, defaultValue.toString());
            }
            if(!mPreferenceStore.contains(name)) {
                mPreferenceStore.setToDefault(name);
            }
        }
    }

    private void initializeNSISPreferences()
    {
        initializePreference(NSIS_HOME,""); //$NON-NLS-1$
        initializePreference(NOTIFY_MAKENSIS_CHANGED, Boolean.TRUE);
        initializePreference(USE_ECLIPSE_HELP,Boolean.FALSE);
        initializePreference(HDRINFO,(getDefaultHdrInfo()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(LICENSE,(getDefaultLicense()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(NOCONFIG,(getDefaultNoConfig()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(NOCD,(getDefaultNoCD()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(VERBOSITY,new Integer(getDefaultVerbosity()));
        initializePreference(PROCESS_PRIORITY,new Integer(getDefaultProcessPriority()));
        initializePreference(WARN_PROCESS_PRIORITY,Boolean.TRUE);
        initializePreference(WARN_REASSOCIATE_HEADER,Boolean.TRUE);
        initializePreference(COMPRESSOR,new Integer(getDefaultCompressor()));
        initializePreference(SOLID_COMPRESSION,(getDefaultSolidCompression()?Boolean.TRUE:Boolean.FALSE));
        initializePreference(AUTO_SHOW_CONSOLE,new Integer(AUTO_SHOW_CONSOLE_DEFAULT));
        initializePreference(BEFORE_COMPILE_SAVE,new Integer(BEFORE_COMPILE_SAVE_DEFAULT));
        initializePreference(INSTRUCTIONS,""); //$NON-NLS-1$
        initializePreference(SYMBOLS,""); //$NON-NLS-1$

        String pref = mPreferenceStore.getString(AUTO_SHOW_CONSOLE);
        int autoShowConsole;
        try {
            autoShowConsole = Integer.parseInt(pref);
        }
        catch (NumberFormatException e) {
            autoShowConsole = Boolean.valueOf(pref).booleanValue()?AUTO_SHOW_CONSOLE_ALWAYS:AUTO_SHOW_CONSOLE_NEVER;
            mPreferenceStore.setValue(AUTO_SHOW_CONSOLE, autoShowConsole);
        }
        setAutoShowConsole(autoShowConsole);
        setBeforeCompileSave(mPreferenceStore.getInt(BEFORE_COMPILE_SAVE));
        setNSISHome(mPreferenceStore.getString(NSIS_HOME));
        setUseEclipseHelp(mPreferenceStore.getBoolean(USE_ECLIPSE_HELP));
    }

    private void initializeEditorPreferences()
    {
        initializePreference(MATCHING_DELIMITERS,Boolean.TRUE);
        initializePreference(MATCHING_DELIMITERS_COLOR,StringConverter.asString(new RGB(128,128,128)));

        initializePreference(DROP_EXTERNAL_FILES_ACTION,new Integer(DROP_EXTERNAL_FILES_DEFAULT));
    }

    private void initializeSyntaxPreference(String name, RGB foreground, RGB background, boolean bold,
                    boolean italic, boolean underline, boolean strikethrough)
    {
        NSISSyntaxStyle style = new NSISSyntaxStyle(foreground, background, bold, italic,
                        underline, strikethrough);
        mPreferenceStore.setDefault(name,style.toString());
        if(!mPreferenceStore.contains(name)) {
            mPreferenceStore.setToDefault(name);
        }
    }

    private void initializeSyntaxPreferences()
    {
        initializeSyntaxPreference(CALLBACKS_STYLE,SYNTAX_CALLBACKS, null, false, false, false, false);
        initializeSyntaxPreference(SYMBOLS_STYLE,SYNTAX_SYMBOLS, null, false, false, false, false);
        initializeSyntaxPreference(LANGSTRINGS_STYLE,SYNTAX_LANGSTRINGS, null, false, false, false, false);
        initializeSyntaxPreference(PREDEFINED_VARIABLES_STYLE,SYNTAX_PREDEF_VARIABLES, null, false, false, false, false);
        initializeSyntaxPreference(USERDEFINED_VARIABLES_STYLE,SYNTAX_USER_VARIABLES, null, false, false, false, false);
        initializeSyntaxPreference(INSTRUCTIONS_STYLE,SYNTAX_INSTRUCTIONS, null, false, false, false, false);
        initializeSyntaxPreference(INSTRUCTION_PARAMETERS_STYLE,SYNTAX_PARAMETERS, null, false, false, false, false);
        initializeSyntaxPreference(INSTRUCTION_OPTIONS_STYLE,SYNTAX_OPTIONS, null, false, false, false, false);
        initializeSyntaxPreference(COMMANDS_STYLE,SYNTAX_COMMANDS, null, true, false, false, false);
        initializeSyntaxPreference(INSTALLER_ATTRIBUTES_STYLE,SYNTAX_ATTRIBUTES, null, false, false, false, false);
        initializeSyntaxPreference(COMPILETIME_COMMANDS_STYLE,ColorManager.RED, null, false, false, false, false);
        initializeSyntaxPreference(NUMBERS_STYLE,ColorManager.RED, null, false, false, false, false);
        initializeSyntaxPreference(STRINGS_STYLE,SYNTAX_STRINGS, null, false, false, false, false);
        initializeSyntaxPreference(COMMENTS_STYLE, SYNTAX_COMMENTS, null, false, true, false, false);
        initializeSyntaxPreference(TASK_TAGS_STYLE,SYNTAX_TASK_TAGS, null, true, false, false, false);
        initializeSyntaxPreference(PLUGINS_STYLE,SYNTAX_PLUGINS, null, false, false, false, false);
    }

    @Override
    public void load()
    {
        initializeNSISPreferences();
        initializeEditorPreferences();
        initializeSyntaxPreferences();
        initializeDefaultTaskTags();
        initializePreference(TASK_TAGS,""); //$NON-NLS-1$
        mTaskTags = this.<Collection<NSISTaskTag>>loadObject(TASK_TAGS);
        initializePreference(CASE_SENSITIVE_TASK_TAGS,Boolean.TRUE);
        mCaseSensitiveTaskTags = getBoolean(CASE_SENSITIVE_TASK_TAGS);

        initializePreference(NSIS_COMMAND_VIEW_FLAT_MODE, Boolean.FALSE);
        initializePreference(NSIS_HELP_VIEW_SHOW_NAV, Boolean.TRUE);
        initializePreference(NSIS_HELP_VIEW_SYNCHED, Boolean.TRUE);
        super.load();
    }

    @Override
    protected boolean migrate(Version settingsVersion)
    {
        boolean b = super.migrate(settingsVersion);
        if(EclipseNSISPlugin.getDefault().getVersion().compareTo(settingsVersion) > 0) {
            if(IPluginVersions.VERSION_0_9_6.compareTo(settingsVersion) > 0) {
                mPreferenceStore.setDefault(USE_SPACES_FOR_TABS,true);
                EditorsUI.getPreferenceStore().setValue(
                                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
                                mPreferenceStore.getBoolean(USE_SPACES_FOR_TABS));
                b = true;
            }
        }
        return b;
    }

    /**
     *
     */
    private void initializeDefaultTaskTags()
    {
        String defaultTaskTag = EclipseNSISPlugin.getResourceString("default.task.tag",""); //$NON-NLS-1$ //$NON-NLS-2$
        String[] taskTags = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"task.tags"); //$NON-NLS-1$
        String[] taskTagPriorities = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"task.priorities"); //$NON-NLS-1$
        mDefaultTaskTags = new ArrayList<NSISTaskTag>();
        if(!Common.isEmptyArray(taskTags)) {
            for (int i = 0; i < taskTags.length; i++) {
                NSISTaskTag tag;
                if(!Common.isEmptyArray(taskTagPriorities) && taskTagPriorities.length > i) {
                    try {
                        tag = new NSISTaskTag(taskTags[i],Integer.parseInt(taskTagPriorities[i]));
                    }
                    catch(NumberFormatException nfe) {
                        EclipseNSISPlugin.getDefault().log(nfe);
                        tag = new NSISTaskTag(taskTags[i],IMarker.PRIORITY_NORMAL);
                    }
                }
                else {
                    tag = new NSISTaskTag(taskTags[i],IMarker.PRIORITY_NORMAL);
                }
                tag.setDefault(taskTags[i].equals(defaultTaskTag));
                mDefaultTaskTags.add(tag);
            }
        }
    }

    @Override
    public void store()
    {
        setValue(NSIS_HOME,mNSISHome != null?mNSISHome.getLocation().getAbsolutePath():"");
        setValue(USE_ECLIPSE_HELP,mUseEclipseHelp);
        setValue(AUTO_SHOW_CONSOLE,mAutoShowConsole);
        setValue(BEFORE_COMPILE_SAVE,mBeforeCompileSave);
        setValue(CASE_SENSITIVE_TASK_TAGS,mCaseSensitiveTaskTags);
        storeObject(TASK_TAGS,mTaskTags);
        super.store();
    }

    private Collection<NSISTaskTag> createCopy(Collection<NSISTaskTag> tags)
    {
        Collection<NSISTaskTag> copy = new ArrayList<NSISTaskTag>();
        for (Iterator<NSISTaskTag> iter=tags.iterator(); iter.hasNext(); ) {
            copy.add(new NSISTaskTag(iter.next()));
        }
        return copy;
    }

    /**
     * @return Returns the taskTags.
     */
    public Collection<NSISTaskTag> getTaskTags()
    {
        return mTaskTags == null?getDefaultTaskTags():createCopy(mTaskTags);
    }

    /**
     * @param taskTags The taskTags to set.
     */
    public void setTaskTags(Collection<NSISTaskTag> taskTags)
    {
        Collection<NSISTaskTag> taskTags2 = taskTags;
        if(taskTags2 != null && taskTags2.size() == mDefaultTaskTags.size()) {
            boolean different = false;
            for (Iterator<NSISTaskTag> iter = taskTags2.iterator(); iter.hasNext();) {
                if(!mDefaultTaskTags.contains(iter.next())) {
                    different = true;
                    break;
                }
            }
            if(!different) {
                taskTags2 = null;
            }
        }
        mTaskTags = taskTags2;
    }


    /**
     * @return Returns the defaultTaskTags.
     */
    public Collection<NSISTaskTag> getDefaultTaskTags()
    {
        return createCopy(mDefaultTaskTags);
    }

    /**
     * @return Returns the caseSensitiveTaskTags.
     */
    public boolean isCaseSensitiveTaskTags()
    {
        return mCaseSensitiveTaskTags;
    }

    /**
     * @param caseSensitiveTaskTags The caseSensitiveTaskTags to set.
     */
    public void setCaseSensitiveTaskTags(boolean caseSensitiveTaskTags)
    {
        mCaseSensitiveTaskTags = caseSensitiveTaskTags;
    }

    /**
     * @return Returns the NSISHome.
     */
    public NSISHome getNSISHome()
    {
        return mNSISHome;
    }

    public boolean isSolidCompressionSupported()
    {
        NSISHome nsisHome = getNSISHome();
        if(nsisHome != null)
        {
            NSISExe nsisExe = nsisHome.getNSISExe();
            if(nsisExe != null)
            {
                return nsisExe.isSolidCompressionSupported();
            }
        }
        return false;
    }

    public boolean isUnicode()
    {
        NSISHome nsisHome = getNSISHome();
        if(nsisHome != null)
        {
            NSISExe nsisExe = nsisHome.getNSISExe();
            if(nsisExe != null)
            {
                return nsisExe.isUnicode();
            }
        }
        return false;
    }

    public boolean isProcessPrioritySupported()
    {
        NSISHome nsisHome = getNSISHome();
        if(nsisHome != null)
        {
            NSISExe nsisExe = nsisHome.getNSISExe();
            if(nsisExe != null)
            {
                return nsisExe.isProcessPrioritySupported();
            }
        }
        return false;
    }

    /**
     * @param nsisHome The NSISHome to set.
     */
    public void setNSISHome(String nsisHome)
    {
        final NSISHome oldHome = mNSISHome;
        internalSetNSISHome(nsisHome);
        if(Display.getCurrent() != null) {
            fireNSISHomeChanged(oldHome, mNSISHome);
        }
        else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    fireNSISHomeChanged(oldHome, mNSISHome);
                }
            });
        }
    }

    private void fireNSISHomeChanged(final NSISHome oldHome, final NSISHome newHome)
    {
        if(!Common.objectsAreEqual(oldHome, newHome) && mListeners.size() > 0) {
            EclipseNSISPlugin.getDefault().run(true,false,new NSISHomeChangedRunnable(oldHome, newHome));
        }
    }

    /**
     * @param nsisHome The NSISHome to set.
     */
    private void internalSetNSISHome(String nsisHome)
    {
        if(mNSISHome != null) {
            FileMonitor.INSTANCE.unregister(mNSISHome.getNSISExe().getFile(),this);
        }
        NSISHome home = new NSISHome(new File(nsisHome));
        if(home.getNSISExe() != null) {
            mNSISHome = home;
            MakeNSISRunner.setUnicode(home.getNSISExe().isUnicode());
            FileMonitor.INSTANCE.register(home.getNSISExe().getFile(),this);
        }
        else {
            mNSISHome = null;
        }
    }

    private void setBestNSISHome(String nsisHome)
    {
        internalSetNSISHome(nsisHome);
        boolean dirty = false;
        while(mNSISHome == null) {
            dirty = true;
            NSISPreferencePage.removeNSISHome(nsisHome);
            if(NSISPreferencePage.NSIS_HOMES.size() > 0) {
                nsisHome = NSISPreferencePage.NSIS_HOMES.get(0);
                internalSetNSISHome(nsisHome);
            }
            else {
                break;
            }
        }
        if(dirty) {
            NSISPreferencePage.saveNSISHomes();
        }
    }

    public void fileChanged(int type, File file)
    {
        if(file.equals(mNSISHome == null ? null : mNSISHome.getNSISExe().getFile())) {
            final String message;
            switch(type) {
                case FileMonitor.FILE_MODIFIED:
                case FileMonitor.FILE_CREATED:
                    message = EclipseNSISPlugin.getResourceString("makensis.modified.message"); //$NON-NLS-1$
                    break;
                case FileMonitor.FILE_DELETED:
                    message = EclipseNSISPlugin.getResourceString("makensis.deleted.message"); //$NON-NLS-1$
                    break;
                default:
                    return;
            }
            final String nsisHome = mNSISHome.getLocation().getAbsolutePath();
            mNSISHome = null;
            final boolean silent = !mPreferenceStore.getBoolean(NOTIFY_MAKENSIS_CHANGED);
            if(silent) {
                setBestNSISHome(nsisHome);
                if(mListeners.size() > 0) {
                    new NSISHomeChangedRunnable(null, mNSISHome).run(new NullProgressMonitor());
                }

                Display.getDefault().syncExec(new Runnable() {
                    public void run()
                    {
                        maybeConfigure();
                    }
                });
            }
            else {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        MessageDialogWithToggle dialog = new MessageDialogWithToggle(
                                        shell,
                                        EclipseNSISPlugin.getDefault().getName(),
                                        EclipseNSISPlugin.getShellImage(),
                                        message,
                                        MessageDialog.WARNING,
                                        new String[] { IDialogConstants.OK_LABEL }, 0,
                                        EclipseNSISPlugin.getResourceString("notify.makensis.changed.toggle"), silent); //$NON-NLS-1$
                        dialog.open();
                        mPreferenceStore.setValue(NOTIFY_MAKENSIS_CHANGED,!dialog.getToggleState());
                        BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
                            public void run()
                            {
                                setBestNSISHome(nsisHome);
                                fireNSISHomeChanged(null, mNSISHome);
                            }
                        });
                        maybeConfigure();
                    }
                });
            }
        }

    }

    private void maybeConfigure()
    {
        if(mNSISHome == null) {
            Shell shell = Display.getDefault().getActiveShell();
            if (Common.openConfirm(shell,
                            EclipseNSISPlugin.getDefault().getName(),
                            EclipseNSISPlugin.getResourceString("unconfigured.confirm"), //$NON-NLS-1$
                            EclipseNSISPlugin.getShellImage())) {
                new NSISConfigWizardDialog(shell).open();
            }
            if (mNSISHome == null) {
                Common.openWarning(shell, EclipseNSISPlugin.getDefault().getName(), EclipseNSISPlugin.getResourceString("unconfigured.warning"), //$NON-NLS-1$
                                EclipseNSISPlugin.getShellImage());
            }
        }
    }

    /**
     * @return Returns the NSIS Version.
     */
    public Version getNSISVersion()
    {
        NSISHome nsisHome = getNSISHome();
        if(nsisHome != null)
        {
            NSISExe nsisExe = nsisHome.getNSISExe();
            if(nsisExe != null)
            {
                return nsisExe.getVersion();
            }
        }

        return Version.EMPTY_VERSION;
    }

    /**
     * @return Returns the useIntegratedHelp.
     */
    public boolean isUseEclipseHelp()
    {
        return mUseEclipseHelp;
    }

    /**
     * @param useEclipseHelp The useEclipseHelp to set.
     */
    public void setUseEclipseHelp(boolean useEclipseHelp)
    {
        mUseEclipseHelp = useEclipseHelp;
    }

    /**
     * @return Returns the autoShowConsole.
     */
    public int getAutoShowConsole()
    {
        return mAutoShowConsole;
    }

    /**
     * @param autoShowConsole The autoShowConsole to set.
     */
    public void setAutoShowConsole(int autoShowConsole)
    {
        mAutoShowConsole = autoShowConsole;
    }

    public int getBeforeCompileSave()
    {
        return mBeforeCompileSave;
    }

    public void setBeforeCompileSave(int beforeCompileSave)
    {
        mBeforeCompileSave = beforeCompileSave;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String name)
    {
        return mPreferenceStore.getBoolean(name);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getInt(java.lang.String)
     */
    @Override
    public int getInt(String name)
    {
        return mPreferenceStore.getInt(name);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#getString(java.lang.String)
     */
    @Override
    public String getString(String name)
    {
        return mPreferenceStore.getString(name);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, boolean)
     */
    @Override
    public void setValue(String name, boolean value)
    {
        mPreferenceStore.setValue(name, value);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, int)
     */
    @Override
    public void setValue(String name, int value)
    {
        mPreferenceStore.setValue(name, value);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#setValue(java.lang.String, java.lang.String)
     */
    @Override
    public void setValue(String name, String value)
    {
        mPreferenceStore.setValue(name, value);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeBoolean(java.lang.String)
     */
    @Override
    public void removeBoolean(String name)
    {
        mPreferenceStore.setValue(name, IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeInt(java.lang.String)
     */
    @Override
    public void removeInt(String name)
    {
        mPreferenceStore.setValue(name, IPreferenceStore.INT_DEFAULT_DEFAULT);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.NSISSettings#removeString(java.lang.String)
     */
    @Override
    public void removeString(String name)
    {
        mPreferenceStore.setValue(name, IPreferenceStore.STRING_DEFAULT_DEFAULT);
    }

    @Override
    public <T> void storeObject(String name, T object)
    {
        String fileName = getString(name);
        if(Common.isEmpty(fileName)) {
            fileName = makeSettingFileName(name);
        }
        File objectFile = new File(PLUGIN_STATE_LOCATION,fileName);
        if(object == null) {
            if(objectFile.exists()) {
                objectFile.delete();
            }
            setValue(name,""); //$NON-NLS-1$
        }
        else {
            try {
                IOUtility.writeObject(objectFile, object);
                setValue(name,fileName);
            }
            catch(IOException ioe) {
                setValue(name,""); //$NON-NLS-1$
                EclipseNSISPlugin.getDefault().log(ioe);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T loadObject(String name)
    {
        String fileName = getString(name);
        File objectFile = new File(PLUGIN_STATE_LOCATION,fileName);
        T object = null;
        if(objectFile.exists()) {
            try {
                object = (T) IOUtility.readObject(objectFile);
            }
            catch (Exception e) {
                object = null;
            }
        }

        return object;
    }

    @Override
    public void removeObject(String name)
    {
        storeObject(name, null);
    }

    private String makeSettingFileName(String name)
    {
        return new StringBuffer(getClass().getName()).append(".").append(name).append(".ser").toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private class NSISHomeChangedRunnable implements IRunnableWithProgress
    {
        private NSISHome mOldHome;
        private NSISHome mNewHome;

        public NSISHomeChangedRunnable(NSISHome oldHome, NSISHome newHome)
        {
            mOldHome = oldHome;
            mNewHome = newHome;
        }

        public void run(IProgressMonitor monitor)
        {
            try {
                String taskName = EclipseNSISPlugin.getResourceString("propagating.home.message"); //$NON-NLS-1$
                monitor.beginTask(taskName,10*mListeners.size());
                INSISHomeListener[] listeners = mListeners.toArray(new INSISHomeListener[mListeners.size()]);
                for (int i = 0; i < listeners.length; i++) {
                    NestedProgressMonitor subMonitor = new NestedProgressMonitor(monitor, taskName, 10);
                    try {
                        listeners[i].nsisHomeChanged(subMonitor, mOldHome, mNewHome);
                    }
                    catch (Exception e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    finally {
                        subMonitor.done();
                    }
                }
            }
            finally {
                monitor.done();
            }
        }
    }
}
