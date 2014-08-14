/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.util.*;

public class InstallOptionsModel implements IPropertyChangeListener
{
    public static final String TYPE_DIALOG = "Dialog"; //$NON-NLS-1$
    public static final String TYPE_LABEL = "Label"; //$NON-NLS-1$
    public static final String TYPE_LINK = "Link"; //$NON-NLS-1$
    public static final String TYPE_BUTTON = "Button"; //$NON-NLS-1$
    public static final String TYPE_CHECKBOX = "CheckBox"; //$NON-NLS-1$
    public static final String TYPE_RADIOBUTTON = "RadioButton"; //$NON-NLS-1$
    public static final String TYPE_FILEREQUEST = "FileRequest"; //$NON-NLS-1$
    public static final String TYPE_DIRREQUEST = "DirRequest"; //$NON-NLS-1$
    public static final String TYPE_BITMAP = "Bitmap"; //$NON-NLS-1$
    public static final String TYPE_ICON = "Icon"; //$NON-NLS-1$
    public static final String TYPE_GROUPBOX = "GroupBox"; //$NON-NLS-1$
    public static final String TYPE_TEXT = "Text"; //$NON-NLS-1$
    public static final String TYPE_PASSWORD = "Password"; //$NON-NLS-1$
    public static final String TYPE_COMBOBOX = "Combobox"; //$NON-NLS-1$
    public static final String TYPE_DROPLIST = "DropList"; //$NON-NLS-1$
    public static final String TYPE_LISTBOX = "Listbox"; //$NON-NLS-1$
    public static final String TYPE_UNKNOWN = "Unknown"; //$NON-NLS-1$
    public static final String TYPE_HLINE = "HLine"; //$NON-NLS-1$
    public static final String TYPE_VLINE = "VLine"; //$NON-NLS-1$

    public static final String SECTION_SETTINGS = "Settings"; //$NON-NLS-1$
    public static final String SECTION_FIELD_PREFIX = "Field"; //$NON-NLS-1$
    public static final Pattern SECTION_FIELD_PATTERN = Pattern.compile(SECTION_FIELD_PREFIX+" ([1-9][0-9]*)",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    public static final MessageFormat SECTION_FIELD_FORMAT = new MessageFormat(SECTION_FIELD_PREFIX+" {0}"); //$NON-NLS-1$

    public static final String PROPERTY_TYPE = "Type"; //$NON-NLS-1$
    public static final String PROPERTY_LEFT = "Left"; //$NON-NLS-1$
    public static final String PROPERTY_TOP = "Top"; //$NON-NLS-1$
    public static final String PROPERTY_RIGHT = "Right"; //$NON-NLS-1$
    public static final String PROPERTY_BOTTOM = "Bottom"; //$NON-NLS-1$
    public static final String PROPERTY_NUMFIELDS = "NumFields"; //$NON-NLS-1$
    public static final String PROPERTY_POSITION = "Position"; //$NON-NLS-1$
    public static final String PROPERTY_INDEX = "Index"; //$NON-NLS-1$
    public static final String PROPERTY_FLAGS = "Flags"; //$NON-NLS-1$
    public static final String PROPERTY_TEXT = "Text"; //$NON-NLS-1$
    public static final String PROPERTY_STATE = "State"; //$NON-NLS-1$
    public static final String PROPERTY_MAXLEN = "MaxLen"; //$NON-NLS-1$
    public static final String PROPERTY_MINLEN = "MinLen"; //$NON-NLS-1$
    public static final String PROPERTY_VALIDATETEXT = "ValidateText"; //$NON-NLS-1$
    public static final String PROPERTY_CHILDREN = "Children"; //$NON-NLS-1$
    public static final String PROPERTY_TITLE =  "Title"; //$NON-NLS-1$
    public static final String PROPERTY_CANCEL_ENABLED = "CancelEnabled"; //$NON-NLS-1$
    public static final String PROPERTY_CANCEL_SHOW = "CancelShow"; //$NON-NLS-1$;
    public static final String PROPERTY_BACK_ENABLED = "BackEnabled"; //$NON-NLS-1$;
    public static final String PROPERTY_CANCEL_BUTTON_TEXT = "CancelButtonText"; //$NON-NLS-1$;
    public static final String PROPERTY_NEXT_BUTTON_TEXT = "NextButtonText"; //$NON-NLS-1$;
    public static final String PROPERTY_BACK_BUTTON_TEXT = "BackButtonText"; //$NON-NLS-1$;
    public static final String PROPERTY_RECT = "Rect"; //$NON-NLS-1$;
    public static final String PROPERTY_RTL = "RTL"; //$NON-NLS-1$;
    public static final String PROPERTY_FILTER = "Filter"; //$NON-NLS-1$;
    public static final String PROPERTY_ROOT = "Root"; //$NON-NLS-1$;
    public static final String PROPERTY_TXTCOLOR = "TxtColor"; //$NON-NLS-1$;
    public static final String PROPERTY_LISTITEMS = "ListItems"; //$NON-NLS-1$;
    public static final String PROPERTY_MULTILINE = "MultiLine"; //$NON-NLS-1$;

    public static final String FLAGS_DISABLED = "DISABLED"; //$NON-NLS-1$
    public static final String FLAGS_RIGHT = "RIGHT"; //$NON-NLS-1$
    public static final String FLAGS_ONLY_NUMBERS = "ONLY_NUMBERS"; //$NON-NLS-1$
    public static final String FLAGS_MULTILINE = "MULTILINE"; //$NON-NLS-1$
    public static final String FLAGS_NOWORDWRAP = "NOWORDWRAP"; //$NON-NLS-1$
    public static final String FLAGS_HSCROLL = "HSCROLL"; //$NON-NLS-1$
    public static final String FLAGS_VSCROLL = "VSCROLL"; //$NON-NLS-1$
    public static final String FLAGS_READONLY = "READONLY"; //$NON-NLS-1$
    public static final String FLAGS_MULTISELECT = "MULTISELECT"; //$NON-NLS-1$
    public static final String FLAGS_EXTENDEDSELECT = "EXTENDEDSELCT"; //$NON-NLS-1$

    public static final Integer STATE_DEFAULT = null;
    public static final Integer STATE_UNCHECKED = Common.ZERO;
    public static final Integer STATE_CHECKED = Common.ONE;

    public static final Integer OPTION_DEFAULT = null;
    public static final Integer OPTION_NO = Common.ZERO;
    public static final Integer OPTION_YES = Common.ONE;

    public static final InstallOptionsModel INSTANCE = new InstallOptionsModel();

    private List<IModelListener> mListeners = new ArrayList<IModelListener>();
    private Set<String> mDialogSettings = new CaseInsensitiveSet();
    private Map<String, InstallOptionsModelTypeDef> mCachedControlTypes = new CaseInsensitiveMap<InstallOptionsModelTypeDef>();
    private Map<String, InstallOptionsModelTypeDef> mControlTypes = new CaseInsensitiveMap<InstallOptionsModelTypeDef>();
    private Map<String, String> mControlRequiredSettings = new CaseInsensitiveMap<String>();
    private int mMaxLength;

    /**
     *
     */
    private InstallOptionsModel()
    {
        super();
        loadModel();
        NSISPreferences.getInstance().getPreferenceStore().addPropertyChangeListener(this);
    }

    public int getMaxLength()
    {
        return mMaxLength;
    }

    public void addModelListener(IModelListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeModelListener(IModelListener listener)
    {
        mListeners.remove(listener);
    }

    private void notifyListeners()
    {
        IModelListener[] listeners = mListeners.toArray(new IModelListener[mListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].modelChanged();
        }
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        if(INSISPreferenceConstants.NSIS_HOME.equals(event.getProperty())) {
            loadModel();
        }
    }

    private void loadModel()
    {
        List<String> controlRequiredSettings = new ArrayList<String>();
        List<String> controlTypes = new ArrayList<String>();
        List<String> dialogSettings = new ArrayList<String>();
        Map<String, List<String>> controlSettings = new CaseInsensitiveMap<List<String>>();
        Map<String, List<String>> controlFlags = new CaseInsensitiveMap<List<String>>();
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(getClass().getName());
        } catch (MissingResourceException x) {
            bundle = null;
        }

        if(bundle != null) {
            Version nsisVersion = getNSISVersion();
            HashMap<Version, ArrayList<String[]>> versionMap = new HashMap<Version, ArrayList<String[]>>();
            for(Enumeration<String> e=bundle.getKeys(); e.hasMoreElements();) {
                String key = e.nextElement();
                int n = key.indexOf('#');
                if(n > 1) {
                    String name = key.substring(0,n);
                    Version version = new Version(key.substring(n+1));
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
            List<Version> versionList = new ArrayList<Version>(versionMap.keySet());
            Collections.sort(versionList);

            for (Iterator<Version> iter = versionList.iterator(); iter.hasNext();) {
                Version version = iter.next();
                List<String[]> nameList = versionMap.get(version);
                for (Iterator<String[]> iter2 = nameList.iterator(); iter2.hasNext();) {
                    String[] element = iter2.next();
                    String name = element[0];
                    String key = element[1];

                    String[] values = Common.loadArrayProperty(bundle,key);
                    List<String> list = null;
                    if(name.equals("Dialog.Settings")) { //$NON-NLS-1$
                        list = dialogSettings;
                    }
                    else if(name.equals("Dialog.Control.Types")) { //$NON-NLS-1$
                        list = controlTypes;
                    }
                    else if(name.equals("Control.Required.Settings")) { //$NON-NLS-1$
                        list = controlRequiredSettings;
                    }
                    else {
                        int n = name.indexOf("."); //$NON-NLS-1$
                        if(n > 0) {
                            String type = name.substring(0,n);
                            Map<String, List<String>> map;
                            if(name.endsWith(".Settings")) { //$NON-NLS-1$
                                map = controlSettings;
                            }
                            else if(name.endsWith(".Flags")) { //$NON-NLS-1$
                                map = controlFlags;
                            }
                            else {
                                map = null;
                            }
                            if(map != null) {
                                list = map.get(type);
                                if(list == null) {
                                    list = new ArrayList<String>();
                                    map.put(type,list);
                                }
                            }
                        }
                    }
                    if(list != null) {
                        processValues(list,values);
                    }
                }
            }
        }

        mDialogSettings.clear();
        mDialogSettings.addAll(dialogSettings);

        mControlRequiredSettings.clear();
        for (Iterator<String> iter = controlRequiredSettings.iterator(); iter.hasNext();) {
            String element = iter.next();
            int n = element.indexOf("="); //$NON-NLS-1$
            if(n > 0) {
                mControlRequiredSettings.put(element.substring(0,n),element.substring(n+1));
            }
            else {
                mControlRequiredSettings.put(element,""); //$NON-NLS-1$
            }
        }

        mCachedControlTypes.putAll(mControlTypes);
        mControlTypes.clear();
        if (bundle != null) {
            for (Iterator<String> iter = controlTypes.iterator(); iter
            .hasNext();) {
                String type = iter.next();
                InstallOptionsModelTypeDef typeDef = mCachedControlTypes
                .remove(type);
                if (typeDef == null) {
                    String name = bundle.getString(type + ".Name"); //$NON-NLS-1$
                    String description = bundle
                    .getString(type + ".Description"); //$NON-NLS-1$
                    String largeIcon = bundle.getString(type + ".LargeIcon"); //$NON-NLS-1$
                    String smallIcon = bundle.getString(type + ".SmallIcon"); //$NON-NLS-1$
                    String displayProperty = bundle.getString(type
                                    + ".DisplayProperty"); //$NON-NLS-1$
                    String model = bundle.getString(type + ".Model"); //$NON-NLS-1$
                    String part = bundle.getString(type + ".Part"); //$NON-NLS-1$
                    typeDef = new InstallOptionsModelTypeDef(type, name,
                                    description, smallIcon, largeIcon, displayProperty,
                                    model, part);
                }
                mControlTypes.put(type, typeDef);
                List<String> list = controlSettings.get(type);
                if (list == null) {
                    list = new ArrayList<String>();
                }
                list.addAll(0, mControlRequiredSettings.keySet());
                typeDef.setSettings(list);
                typeDef.setFlags(controlFlags.get(type));
            }
        }
        InstallOptionsModelTypeDef typeDef = mControlTypes.remove(TYPE_UNKNOWN);
        if(typeDef != null) {
            mControlTypes.put(typeDef.getType(),typeDef);
        }

        try {
            mMaxLength = Integer.parseInt(NSISPreferences.getInstance().getNSISHome().getNSISExe().getDefinedSymbol("NSIS_MAX_STRLEN")); //$NON-NLS-1$
        }
        catch(Exception ex){
            mMaxLength = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
        }

        notifyListeners();
    }

    public Version getNSISVersion()
    {
        Version nsisVersion;
        if(EclipseNSISPlugin.getDefault().isConfigured()) {
            nsisVersion = NSISPreferences.getInstance().getNSISVersion();
        }
        else {
            nsisVersion = INSISVersions.MINIMUM_VERSION;
        }
        return nsisVersion;
    }

    private void processValues(List<String> list, String[] values)
    {
        for (int i = 0; i < values.length; i++) {
            int n = values[i].indexOf('~');
            if(values[i].charAt(0) == '-') {
                list.remove(values[i].substring(1));
            }
            else if(n > 0) {
                String oldValue = values[i].substring(0,n);
                String newValue = values[i].substring(n+1);
                int index = list.indexOf(oldValue);
                if(index >= 0) {
                    list.remove(oldValue);
                    list.add(index, newValue);
                }
                else {
                    list.add(newValue);
                }
            }
            else {
                if(values[i].charAt(0) == '+') {
                    values[i] = values[i].substring(1);
                }
                list.add(values[i]);
            }
        }
    }

    public InstallOptionsModelTypeDef getControlTypeDef(String type)
    {
        InstallOptionsModelTypeDef typeDef = mControlTypes.get(type);
        if(typeDef == null) {
            typeDef = getControlTypeDef(TYPE_UNKNOWN);
        }
        return typeDef;
    }

    public Map<String, String> getControlRequiredSettings()
    {
        return Collections.unmodifiableMap(mControlRequiredSettings);
    }

    public Collection<InstallOptionsModelTypeDef> getControlTypeDefs()
    {
        return Collections.unmodifiableCollection(mControlTypes.values());
    }

    public Collection<String> getDialogSettings()
    {
        return Collections.unmodifiableSet(mDialogSettings);
    }
}
