/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.core.runtime.IProgressMonitor;

public class NSISKeywords implements INSISConstants, IEclipseNSISService
{
    private static NSISKeywords cInstance = null;

    public static final String ALL_KEYWORDS="ALL_KEYWORDS"; //$NON-NLS-1$
    public static final String SINGLELINE_COMPILETIME_COMMANDS="SINGLELINE_COMPILETIME_COMMANDS"; //$NON-NLS-1$
    public static final String MULTILINE_COMPILETIME_COMMANDS="MULTILINE_COMPILETIME_COMMANDS"; //$NON-NLS-1$
    public static final String INSTALLER_ATTRIBUTES="INSTALLER_ATTRIBUTES"; //$NON-NLS-1$
    public static final String COMMANDS="COMMANDS"; //$NON-NLS-1$
    public static final String INSTRUCTIONS="INSTRUCTIONS"; //$NON-NLS-1$
    public static final String INSTALLER_PAGES="INSTALLER_PAGES"; //$NON-NLS-1$
    public static final String HKEY_LONG_PARAMETERS="HKEY_LONG_PARAMETERS"; //$NON-NLS-1$
    public static final String HKEY_SHORT_PARAMETERS="HKEY_SHORT_PARAMETERS"; //$NON-NLS-1$
    public static final String HKEY_PARAMETERS="HKEY_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_OPTION_BUTTON_PARAMETERS="MESSAGEBOX_OPTION_BUTTON_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_OPTION_ICON_PARAMETERS="MESSAGEBOX_OPTION_ICON_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_OPTION_DEFAULT_PARAMETERS="MESSAGEBOX_OPTION_DEFAULT_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_OPTION_OTHER_PARAMETERS="MESSAGEBOX_OPTION_OTHER_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_OPTION_PARAMETERS="MESSAGEBOX_OPTION_PARAMETERS"; //$NON-NLS-1$
    public static final String MESSAGEBOX_RETURN_PARAMETERS="MESSAGEBOX_RETURN_PARAMETERS"; //$NON-NLS-1$
    public static final String INSTRUCTION_PARAMETERS="INSTRUCTION_PARAMETERS"; //$NON-NLS-1$
    public static final String INSTRUCTION_OPTIONS="INSTRUCTION_OPTIONS"; //$NON-NLS-1$
    public static final String CALLBACKS="CALLBACKS"; //$NON-NLS-1$
    public static final String PATH_CONSTANTS_AND_VARIABLES="PATH_CONSTANTS_AND_VARIABLES"; //$NON-NLS-1$
    public static final String ALL_CONSTANTS_AND_VARIABLES="ALL_CONSTANTS_AND_VARIABLES"; //$NON-NLS-1$
    public static final String ALL_CONSTANTS_VARIABLES_AND_SYMBOLS="ALL_CONSTANTS_VARIABLES_AND_SYMBOLS"; //$NON-NLS-1$
    public static final String REGISTERS="REGISTERS"; //$NON-NLS-1$
    public static final String PATH_VARIABLES="PATH_VARIABLES"; //$NON-NLS-1$
    public static final String VARIABLES="VARIABLES"; //$NON-NLS-1$
    public static final String ALL_VARIABLES="ALL_VARIABLES"; //$NON-NLS-1$
    public static final String SHELL_CONSTANTS="SHELL_CONSTANTS"; //$NON-NLS-1$
    public static final String PATH_CONSTANTS="PATH_CONSTANTS"; //$NON-NLS-1$
    public static final String STRING_CONSTANTS="STRING_CONSTANTS"; //$NON-NLS-1$
    public static final String ALL_CONSTANTS="ALL_CONSTANTS"; //$NON-NLS-1$
    public static final String SYMBOLS="SYMBOLS"; //$NON-NLS-1$
    public static final String PREDEFINES="PREDEFINES"; //$NON-NLS-1$
    public static final String PLUGINS="PLUGINS"; //$NON-NLS-1$

    private List<ShellConstant> mShellConstants = null;
    private Map<String, String[]> mKeywordGroupsMap = null;
    private Map<String, List<String>> mNewerKeywordsMap = null;
    private Set<String> mAllKeywordsSet = null;
    private List<INSISKeywordsListener> mListeners = null;
    private INSISHomeListener mNSISHomeListener = null;

    public static NSISKeywords getInstance()
    {
        return cInstance;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            mKeywordGroupsMap = new HashMap<String, String[]>();
            mNewerKeywordsMap = new CaseInsensitiveMap<List<String>>();
            mAllKeywordsSet = new CaseInsensitiveSet();
            mListeners = new ArrayList<INSISKeywordsListener>();
            mNSISHomeListener  = new INSISHomeListener() {
                public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
                {
                    loadKeywords(monitor);
                }
            };
            loadKeywords(monitor);
            NSISPreferences.getInstance().addListener(mNSISHomeListener);
            cInstance = this;
        }
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISPreferences.getInstance().removeListener(mNSISHomeListener);
            mKeywordGroupsMap = null;
            mNewerKeywordsMap = null;
            mAllKeywordsSet = null;
            mListeners = null;
            mNSISHomeListener = null;
        }
    }

    private void loadKeywords(IProgressMonitor monitor)
    {
        try {
            if(monitor != null) {
                monitor.beginTask("", 100+(mListeners==null?0:mListeners.size())); //$NON-NLS-1$
                monitor.subTask(EclipseNSISPlugin.getResourceString("loading.keywords.message")); //$NON-NLS-1$
            }
            mNewerKeywordsMap.clear();
            mAllKeywordsSet.clear();

            ResourceBundle bundle;
            Version nsisVersion = NSISPreferences.getInstance().getNSISVersion();
            try {
                bundle = ResourceBundle.getBundle(NSISKeywords.class.getName());
            } catch (MissingResourceException x) {
                bundle = null;
            }
            Set<String> registers = new CaseInsensitiveSet();
            Set<String> pathVariables = new CaseInsensitiveSet();
            Set<String> variables = new CaseInsensitiveSet();
            Set<String> shellConstants = new CaseInsensitiveSet();
            Set<String> pathConstants = new CaseInsensitiveSet();
            Set<String> stringConstants = new CaseInsensitiveSet();
            Set<String> predefines = new CaseInsensitiveSet();
            Set<String> singlelineCompiletimeCommands = new CaseInsensitiveSet();
            Set<String> multilineCompiletimeCommands = new CaseInsensitiveSet();
            Set<String> installerAttributes = new CaseInsensitiveSet();
            Set<String> commands = new CaseInsensitiveSet();
            Set<String> instructions = new CaseInsensitiveSet();
            Set<String> installerPages = new CaseInsensitiveSet();
            Set<String> hkeyLongParameters = new CaseInsensitiveSet();
            Set<String> hkeyShortParameters = new CaseInsensitiveSet();
            Set<String> messageboxOptionButtonParameters = new CaseInsensitiveSet();
            Set<String> messageboxOptionIconParameters = new CaseInsensitiveSet();
            Set<String> messageboxOptionDefaultParameters = new CaseInsensitiveSet();
            Set<String> messageboxOptionOtherParameters = new CaseInsensitiveSet();
            Set<String> messageboxReturnParameters = new CaseInsensitiveSet();
            Set<String> instructionParameters = new CaseInsensitiveSet();
            Set<String> instructionOptions = new CaseInsensitiveSet();
            Set<String> callbacks = new CaseInsensitiveSet();

            Map<String,String> generalShellConstants = null;
            Map<String,String> userShellConstants = null;
            Map<String,String> commonShellConstants = null;

            if(bundle != null && nsisVersion != null) {
                HashMap<Version, ArrayList<String[]>> versionMap = new HashMap<Version, ArrayList<String[]>>();
                for(Enumeration<String> e=bundle.getKeys(); e.hasMoreElements();) {
                    String key = e.nextElement();
                    if(key.equals("general.shell.constants")) { //$NON-NLS-1$
                        generalShellConstants = Common.loadMapProperty(bundle, key);
                    }
                    else if(key.equals("user.shell.constants")) { //$NON-NLS-1$
                        userShellConstants = Common.loadMapProperty(bundle, key);
                    }
                    else if(key.equals("common.shell.constants")) { //$NON-NLS-1$
                        commonShellConstants = Common.loadMapProperty(bundle, key);
                    }
                    else {
                        int n = key.indexOf('#');
                        String name = n >= 0?key.substring(0,n):key;
                        Version version = n >= 0?new Version(key.substring(n+1)):INSISVersions.MINIMUM_VERSION;
                        if(nsisVersion.compareTo(version) >= 0) {
                            ArrayList<String[]> list = versionMap.get(version);
                            if(list == null) {
                                list = new ArrayList<String[]>();
                                versionMap.put(version, list);
                            }
                            list.add(new String[]{name,key});
                        }
                    }
                }

                if(monitor != null) {
                    monitor.worked(20);
                }

                List<Version> versionList = new ArrayList<Version>(versionMap.keySet());
                Collections.sort(versionList);

                for (Iterator<Version> iter = versionList.iterator(); iter.hasNext();) {
                    Version version = iter.next();
                    List<String[]> list = versionMap.get(version);
                    for (Iterator<String[]> iter2 = list.iterator(); iter2.hasNext();) {
                        String[] element = iter2.next();
                        String name = element[0];
                        String key = element[1];
                        String[] values = Common.loadArrayProperty(bundle,key);
                        if(!Common.isEmptyArray(values)) {
                            Set<String> set = null;
                            if(name.equals("registers")) { //$NON-NLS-1$
                                set = registers;
                            }
                            else if(name.equals("path.variables")) { //$NON-NLS-1$
                                set = pathVariables;
                            }
                            else if(name.equals("variables")) { //$NON-NLS-1$
                                set = variables;
                            }
                            else if(name.equals("shell.constants")) { //$NON-NLS-1$
                                set = shellConstants;
                            }
                            else if(name.equals("path.constants")) { //$NON-NLS-1$
                                set = pathConstants;
                            }
                            else if(name.equals("string.constants")) { //$NON-NLS-1$
                                set = stringConstants;
                            }
                            else if(name.equals("predefines")) { //$NON-NLS-1$
                                set = predefines;
                            }
                            else if(name.equals("singleline.compiletime.commands")) { //$NON-NLS-1$
                                set = singlelineCompiletimeCommands;
                            }
                            else if(name.equals("multiline.compiletime.commands")) { //$NON-NLS-1$
                                set = multilineCompiletimeCommands;
                            }
                            else if(name.equals("installer.attributes")) { //$NON-NLS-1$
                                set = installerAttributes;
                            }
                            else if(name.equals("commands")) { //$NON-NLS-1$
                                set = commands;
                            }
                            else if(name.equals("instructions")) { //$NON-NLS-1$
                                set = instructions;
                            }
                            else if(name.equals("installer.pages")) { //$NON-NLS-1$
                                set = installerPages;
                            }
                            else if(name.equals("hkey.long.parameters")) { //$NON-NLS-1$
                                set = hkeyLongParameters;
                            }
                            else if(name.equals("hkey.short.parameters")) { //$NON-NLS-1$
                                set = hkeyShortParameters;
                            }
                            else if(name.equals("messagebox.option.button.parameters")) { //$NON-NLS-1$
                                set = messageboxOptionButtonParameters;
                            }
                            else if(name.equals("messagebox.option.icon.parameters")) { //$NON-NLS-1$
                                set = messageboxOptionIconParameters;
                            }
                            else if(name.equals("messagebox.option.default.parameters")) { //$NON-NLS-1$
                                set = messageboxOptionDefaultParameters;
                            }
                            else if(name.equals("messagebox.option.other.parameters")) { //$NON-NLS-1$
                                set = messageboxOptionOtherParameters;
                            }
                            else if(name.equals("messagebox.return.parameters")) { //$NON-NLS-1$
                                set = messageboxReturnParameters;
                            }
                            else if(name.equals("instruction.parameters")) { //$NON-NLS-1$
                                set = instructionParameters;
                            }
                            else if(name.equals("instruction.options")) { //$NON-NLS-1$
                                set = instructionOptions;
                            }
                            else if(name.equals("callbacks")) { //$NON-NLS-1$
                                set = callbacks;
                            }

                            if (set != null) {
                                for (int i = 0; i < values.length; i++) {
                                    int m = values[i].indexOf('^');
                                    int n = values[i].indexOf('~');
                                    if (values[i].charAt(0) == '-') {
                                        set.remove(values[i].substring(1));
                                    } else if (m > 0) {
                                        String oldValue = values[i].substring(
                                                        0, m);
                                        String newValue = values[i]
                                                                 .substring(m + 1);
                                        List<String> list2 = mNewerKeywordsMap
                                        .get(oldValue);
                                        if (list2 == null) {
                                            list2 = new ArrayList<String>();
                                            list2.add(oldValue);
                                            mNewerKeywordsMap.put(oldValue,
                                                            list2);
                                        }
                                        if (!list2.contains(newValue)) {
                                            list2.add(newValue);
                                        }
                                        set.add(oldValue);
                                        set.add(newValue);
                                    } else if (n > 0) {
                                        String oldValue = values[i].substring(
                                                        0, n);
                                        String newValue = values[i]
                                                                 .substring(n + 1);
                                        List<String> list2 = mNewerKeywordsMap
                                        .get(oldValue);
                                        if (list2 == null) {
                                            list2 = new ArrayList<String>();
                                            mNewerKeywordsMap.put(oldValue,
                                                            list2);
                                        } else {
                                            list2.remove(oldValue);
                                        }
                                        if (!list2.contains(newValue)) {
                                            list2.add(newValue);
                                        }
                                        set.remove(oldValue);
                                        set.add(newValue);
                                    } else {
                                        if (values[i].charAt(0) == '+') {
                                            values[i] = values[i].substring(1);
                                        }
                                        set.add(values[i]);
                                    }
                                }
                            }
                        }
                    }
                }
                if(monitor != null) {
                    monitor.worked(50);
                }
            }

            String[] temp;

            Set<String> set = getValidKeywords(registers);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(REGISTERS,temp);

            set = getValidKeywords(pathVariables);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(PATH_VARIABLES,temp);

            set = getValidKeywords(variables);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(VARIABLES,temp);

            set = getValidKeywords(shellConstants);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(SHELL_CONSTANTS,temp);

            set = getValidKeywords(pathConstants);
            mAllKeywordsSet.addAll(set);
            temp = (String[])Common.joinArrays(new Object[] {mKeywordGroupsMap.get(SHELL_CONSTANTS),
                            set.toArray(Common.EMPTY_STRING_ARRAY)});
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(PATH_CONSTANTS,temp);

            set = getValidKeywords(stringConstants);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(STRING_CONSTANTS,temp);

            set = new CaseInsensitiveSet();
            if(NSISPreferences.getInstance().getNSISHome() != null) {
                Set<Object> keySet = NSISPreferences.getInstance().getNSISHome().getNSISExe().getDefinedSymbols().keySet();
                StringBuffer buf = new StringBuffer("${"); //$NON-NLS-1$
                for (Iterator<Object> iter = keySet.iterator(); iter.hasNext();) {
                    set.add(buf.append(String.valueOf(iter.next())).append("}").toString()); //$NON-NLS-1$
                    buf.setLength(2);
                }
            }
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(SYMBOLS,temp);

            set = getValidKeywords(predefines);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(PREDEFINES,temp);

            temp = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(PATH_CONSTANTS), getKeywordsGroup(PATH_VARIABLES)});
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(PATH_CONSTANTS_AND_VARIABLES,temp);

            temp = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(PATH_CONSTANTS), getKeywordsGroup(STRING_CONSTANTS)});
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(ALL_CONSTANTS,temp);

            temp = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(REGISTERS), getKeywordsGroup(PATH_CONSTANTS_AND_VARIABLES),
                            getKeywordsGroup(VARIABLES)});
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(ALL_VARIABLES,temp);

            temp = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(ALL_CONSTANTS), getKeywordsGroup(ALL_VARIABLES)});
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(ALL_CONSTANTS_AND_VARIABLES,temp);

            temp = (String[])Common.joinArrays(new Object[]{getKeywordsGroup(ALL_CONSTANTS), getKeywordsGroup(ALL_VARIABLES),
                            getKeywordsGroup(SYMBOLS), getKeywordsGroup(PREDEFINES)});
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(ALL_CONSTANTS_VARIABLES_AND_SYMBOLS,temp);

            set = getValidKeywords(singlelineCompiletimeCommands);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(SINGLELINE_COMPILETIME_COMMANDS,temp);

            set = getValidKeywords(multilineCompiletimeCommands);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MULTILINE_COMPILETIME_COMMANDS, temp);

            set = getValidKeywords(installerAttributes);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(INSTALLER_ATTRIBUTES,temp);

            set = getValidKeywords(commands);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(COMMANDS,temp);

            set = getValidKeywords(instructions);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(INSTRUCTIONS,temp);

            set = getValidKeywords(installerPages);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(INSTALLER_PAGES,temp);

            set = getValidKeywords(hkeyLongParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(HKEY_LONG_PARAMETERS,temp);

            set = getValidKeywords(hkeyShortParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(HKEY_SHORT_PARAMETERS,temp);

            temp = (String[])Common.appendArray(temp, getKeywordsGroup(HKEY_LONG_PARAMETERS));
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(HKEY_PARAMETERS,temp);

            set = getValidKeywords(messageboxOptionButtonParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MESSAGEBOX_OPTION_BUTTON_PARAMETERS,temp);

            set = getValidKeywords(messageboxOptionIconParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MESSAGEBOX_OPTION_ICON_PARAMETERS,temp);

            set = getValidKeywords(messageboxOptionDefaultParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MESSAGEBOX_OPTION_DEFAULT_PARAMETERS,temp);

            set = getValidKeywords(messageboxOptionOtherParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MESSAGEBOX_OPTION_OTHER_PARAMETERS,temp);

            temp = (String[])Common.appendArray(temp, getKeywordsGroup(MESSAGEBOX_OPTION_BUTTON_PARAMETERS));
            temp = (String[])Common.appendArray(temp, getKeywordsGroup(MESSAGEBOX_OPTION_ICON_PARAMETERS));
            temp = (String[])Common.appendArray(temp, getKeywordsGroup(MESSAGEBOX_OPTION_DEFAULT_PARAMETERS));
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MESSAGEBOX_OPTION_PARAMETERS,temp);

            set = getValidKeywords(messageboxReturnParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(MESSAGEBOX_RETURN_PARAMETERS,temp);

            set = getValidKeywords(instructionParameters);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            temp = (String[])Common.appendArray(temp, getKeywordsGroup(HKEY_PARAMETERS));
            temp = (String[])Common.appendArray(temp, getKeywordsGroup(MESSAGEBOX_OPTION_PARAMETERS));
            temp = (String[])Common.appendArray(temp, getKeywordsGroup(MESSAGEBOX_RETURN_PARAMETERS));
            temp = (String[])Common.appendArray(temp, getKeywordsGroup(INSTALLER_PAGES));
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(INSTRUCTION_PARAMETERS,temp);

            set = getValidKeywords(instructionOptions);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(INSTRUCTION_OPTIONS,temp);

            set = getValidKeywords(callbacks);
            mAllKeywordsSet.addAll(set);
            temp = set.toArray(Common.EMPTY_STRING_ARRAY);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(CALLBACKS,temp);

            temp = mAllKeywordsSet.toArray(new String[mAllKeywordsSet.size()]);
            Arrays.sort(temp, String.CASE_INSENSITIVE_ORDER);
            mKeywordGroupsMap.put(ALL_KEYWORDS,temp);

            File cacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISKeywords.class.getName()+".Plugins.ser"); //$NON-NLS-1$
            if(IOUtility.isValidFile(cacheFile)) {
                cacheFile.delete();
            }

            String[] plugins = null;
            try {
                NSISPluginManager.INSTANCE.loadDefaultPlugins();
                plugins = NSISPluginManager.INSTANCE.getDefaultPluginNames();
            }
            catch (Exception e) {
                EclipseNSISPlugin.getDefault().log(e);
                plugins = null;
            }
            if(plugins == null) {
                plugins = Common.EMPTY_STRING_ARRAY;
            }
            else {
                Arrays.sort(plugins, String.CASE_INSENSITIVE_ORDER);
            }
            mKeywordGroupsMap.put(PLUGINS,plugins);

            mShellConstants = loadShellConstants(generalShellConstants, userShellConstants, commonShellConstants);

            if(monitor != null) {
                monitor.worked(20);
            }

            notifyListeners(monitor);
        }
        finally {
            if(monitor != null) {
                monitor.done();
            }
        }
    }

    private List<ShellConstant> loadShellConstants(Map<String,String> generalShellConstants, Map<String,String> userShellConstants, Map<String,String> commonShellConstants)
    {
        List<ShellConstant> list = new ArrayList<ShellConstant>();
        loadShellConstants(generalShellConstants, list, ShellConstant.CONTEXT_GENERAL);
        loadShellConstants(userShellConstants, list, ShellConstant.CONTEXT_USER);
        loadShellConstants(commonShellConstants, list, ShellConstant.CONTEXT_COMMON);
        if(list.size() > 0) {
            Collections.sort(list, new Comparator<ShellConstant>() {
                public int compare(ShellConstant o1, ShellConstant o2)
                {
                    return o2.value.length()-o1.value.length();
                }
            });
        }

        return list;
    }

    private void loadShellConstants(Map<String,String> shellConstants, List<ShellConstant> list, String context)
    {
        if (!Common.isEmptyMap(shellConstants)) {
            for (Iterator<Map.Entry<String,String>> iter = shellConstants.entrySet().iterator(); iter.hasNext();) {
                try {
                    Map.Entry<String,String> entry = iter.next();
                    String name = entry.getKey();
                    if (isValidKeyword(name)) {
                        String shellFolder = WinAPI.INSTANCE.getShellFolder(Integer.parseInt(entry.getValue()));
                        if (!Common.isEmpty(shellFolder)) {
                            if (entry.getKey().equals(getKeyword("$QUICKLAUNCH"))) { //$NON-NLS-1$
                                shellFolder = shellFolder + "\\Microsoft\\Internet Explorer\\Quick Launch"; //$NON-NLS-1$
                            }
                            entry.setValue(shellFolder);
                            list.add(new ShellConstant(name, shellFolder, context));

                            String shortPath = WinAPI.INSTANCE.getShortPathName(shellFolder);
                            if (shortPath != null && !shortPath.equalsIgnoreCase(shellFolder)) {
                                list.add(new ShellConstant(name, shortPath, context));
                            }
                        }
                    }
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    public List<ShellConstant> getShellConstants()
    {
        List<ShellConstant> list = new ArrayList<ShellConstant>();
        ShellConstant temp = null;
        if(isValidKeyword("$TEMP")) { //$NON-NLS-1$
            temp = new ShellConstant(getKeyword("$TEMP"),WinAPI.INSTANCE.getEnvironmentVariable("TEMP"),ShellConstant.CONTEXT_GENERAL); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!Common.isEmptyCollection(mShellConstants)) {
            for (Iterator<ShellConstant> iter = mShellConstants.iterator(); iter.hasNext();) {
                ShellConstant constant = iter.next();
                if (temp != null && temp.value.length() >= constant.value.length()) {
                    list.add(temp);
                    temp = null;
                }
                list.add(constant);
            }
        }
        else if (temp != null) {
            list.add(temp);
        }
        return list;
    }

    private Set<String> getValidKeywords(Set<String> keywordSet)
    {
        HashSet<String> set = new HashSet<String>();
        for (Iterator<String> iter = keywordSet.iterator(); iter.hasNext();) {
            String keyword = iter.next();
            String mappedKeyword = getKeyword(keyword, false);
            if(mappedKeyword.equalsIgnoreCase(keyword)) {
                set.add(keyword);
            }
        }
        return set;
    }

    public String[] getKeywordsGroup(String group)
    {
        return mKeywordGroupsMap.get(group);
    }

    public boolean isValidKeyword(String keyword)
    {
        return mAllKeywordsSet.contains(keyword);
    }

    private void notifyListeners(IProgressMonitor monitor)
    {
        if(mListeners.size() > 0) {
            if(monitor != null) {
                monitor.subTask(EclipseNSISPlugin.getResourceString("updating.keywords.message")); //$NON-NLS-1$
            }
            INSISKeywordsListener[] listeners = mListeners.toArray(new INSISKeywordsListener[mListeners.size()]);
            for (int i = 0; i < listeners.length; i++) {
                listeners[i].keywordsChanged();
                if(monitor != null) {
                    monitor.worked(1);
                }
            }
        }
    }

    public void addKeywordsListener(INSISKeywordsListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeKeywordsListener(INSISKeywordsListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public String getKeyword(String name)
    {
        return getKeyword(name, true);
    }

    public String getKeyword(String name, boolean getNewest)
    {
        if((!mAllKeywordsSet.contains(name) || getNewest) && mNewerKeywordsMap.containsKey(name)) {
            List<String> list = mNewerKeywordsMap.get(name);
            if(list.size() > 0) {
                if(getNewest) {
                    return getKeyword(list.get(list.size()-1), getNewest);
                }
                else {
                    return list.get(0);
                }
            }
        }
        return name;
    }

    public VariableMatcher createVariableMatcher()
    {
        return new VariableMatcher();
    }

    public static class ShellConstant
    {
        public static final String CONTEXT_GENERAL = ""; //$NON-NLS-1$
        public static final String CONTEXT_USER = "current"; //$NON-NLS-1$
        public static final String CONTEXT_COMMON = "all"; //$NON-NLS-1$

        public final String name;
        public final String value;
        public final String context;

        public ShellConstant(String name, String value, String context)
        {
            this.name = name;
            this.value = value;
            this.context = context;
        }
    }

    public class VariableMatcher
    {
        private int mPotentialMatchIndex = -1;
        private String mText = null;
        private String[] mKeywords = getKeywordsGroup(ALL_CONSTANTS_VARIABLES_AND_SYMBOLS);

        public void reset()
        {
            mPotentialMatchIndex = -1;
            mText = null;
        }

        public void setText(String text)
        {
            String text2 = text.toLowerCase();
            if(mText != null) {
                if(!text2.startsWith(mText)) {
                    reset();
                }
            }
            mText = text2;
        }

        public boolean hasPotentialMatch()
        {
            if(mText != null) {
                for(int i=Math.max(mPotentialMatchIndex,0); i<mKeywords.length; i++) {
                    int n = mKeywords[i].compareToIgnoreCase(mText);
                    if(n < 0) {
                        continue;
                    }
                    else if(n >= 0) {
                        if(mKeywords[i].regionMatches(true,0,mText,0,mText.length())) {
                            mPotentialMatchIndex = i;
                            return true;
                        }
                        break;
                    }
                }
            }
            return false;
        }

        public boolean isMatch()
        {
            return mPotentialMatchIndex >= 0 && mKeywords[mPotentialMatchIndex].equalsIgnoreCase(mText);
        }
    }
}
