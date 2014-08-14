/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.installoptions.builder.InstallOptionsBuilder;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.job.JobScheduler;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.osgi.framework.*;
import org.osgi.service.prefs.BackingStoreException;

public class InstallOptionsPlugin extends AbstractUIPlugin implements IInstallOptionsConstants
{
    public static final String GEF_BUNDLE_ID = "org.eclipse.gef"; //$NON-NLS-1$
    public static final RGB SYNTAX_COMMENTS = new RGB(0x7f,0x9f,0xbf);
    public static final RGB SYNTAX_NUMBERS = new RGB(0x61,0x31,0x1e);
    public static final RGB SYNTAX_SECTIONS = new RGB(0x0,0x50,0x50);
    private static Image cShellImage;
    private static File cStateLocation = null;

    private static InstallOptionsPlugin cPlugin;
    private Map<Locale, CompoundResourceBundle> mResourceBundles = new HashMap<Locale, CompoundResourceBundle>();
    public static final String[] BUNDLE_NAMES = new String[]{RESOURCE_BUNDLE,MESSAGE_BUNDLE};
    private ImageManager mImageManager;
    private String mName = null;
    private JobScheduler mJobScheduler = new JobScheduler();
    private ChainedPreferenceStore mCombinedPreferenceStore;
    private IEclipsePreferences mPreferences;

    /**
     *
     */
    public InstallOptionsPlugin()
    {
        super();
        cPlugin = this;
    }

    /**
     * Returns the shared instance.
     */
    public static InstallOptionsPlugin getDefault() {
        return cPlugin;
    }

    public IPreferenceStore getCombinedPreferenceStore()
    {
        if(mCombinedPreferenceStore == null) {
            mCombinedPreferenceStore = new ChainedPreferenceStore(new IPreferenceStore[]{
                                    getPreferenceStore(),
                                    EditorsUI.getPreferenceStore()
                            });
        }
        return mCombinedPreferenceStore;
    }

    public static ImageManager getImageManager()
    {
        return getDefault().mImageManager;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        return getResourceString(key, key);
    }

    public static String getResourceString(Locale locale, String key)
    {
        return getResourceString(locale, key, key);
    }

    public static String getResourceString(String key, String defaultValue)
    {
        return getResourceString(Locale.getDefault(),key,defaultValue);
    }

    public static String getResourceString(Locale locale, String key, String defaultValue)
    {
        InstallOptionsPlugin plugin = getDefault();
        if(plugin != null) {
            ResourceBundle bundle = plugin.getResourceBundle(locale);
            try {
                return (bundle != null) ? bundle.getString(key) : defaultValue;
            }
            catch (MissingResourceException e) {
            }
        }
        return defaultValue;
    }


    public static String getFormattedString(String key, Object[] args)
    {
        return MessageFormat.format(getResourceString(key),args);
    }

    /**
     * Returns the string from the plugin bundle's resource bundle,
     * or 'key' if not found.
     */
    public static String getBundleResourceString(String key)
    {
        return Platform.getResourceString(getDefault().getBundle(), key);
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return getResourceBundle(Locale.getDefault());
    }

    public synchronized ResourceBundle getResourceBundle(Locale locale)
    {
        if(!mResourceBundles.containsKey(locale)) {
            mResourceBundles.put(locale,new CompoundResourceBundle(getClass().getClassLoader(),locale, BUNDLE_NAMES));
        }
        return mResourceBundles.get(locale);
    }

    private void initializePreference(IPreferenceStore store, String name, String defaultValue)
    {
        store.setDefault(name,defaultValue);
        if(!store.contains(name)) {
            store.setToDefault(name);
        }
    }

    private void initializePreferences()
    {
        IPreferenceStore store = getPreferenceStore();
        initializePreference(store,PREFERENCE_SHOW_GRID,SHOW_GRID_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_RULERS,SHOW_RULERS_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_GUIDES,SHOW_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SHOW_DIALOG_SIZE,SHOW_DIALOG_SIZE_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GRID,SNAP_TO_GRID_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GEOMETRY,SNAP_TO_GEOMETRY_DEFAULT.toString());
        initializePreference(store,PREFERENCE_SNAP_TO_GUIDES,SNAP_TO_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_GLUE_TO_GUIDES,GLUE_TO_GUIDES_DEFAULT.toString());
        initializePreference(store,PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER.asString(GRID_SPACING_DEFAULT));
        initializePreference(store,PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER.asString(GRID_ORIGIN_DEFAULT));
        initializePreference(store,PREFERENCE_GRID_STYLE,GRID_STYLE_DEFAULT);

        String preference = store.getString(IInstallOptionsConstants.PREFERENCE_SYNTAX_STYLES);
        Map<String, NSISSyntaxStyle> map;
        if(!Common.isEmpty(preference)) {
            map = NSISTextUtility.parseSyntaxStylesMap(preference);
        }
        else {
            map = new LinkedHashMap<String, NSISSyntaxStyle>();
        }
        boolean changed = setSyntaxStyles(map);
        if(changed) {
            store.putValue(IInstallOptionsConstants.PREFERENCE_SYNTAX_STYLES, NSISTextUtility.flattenSyntaxStylesMap(map));
        }
        initializePaletteViewerPreferences(store);
    }

    private void initializePaletteViewerPreferences(IPreferenceStore store)
    {
        //This should be done just once.
        if(!store.getBoolean(PREFERENCE_PALETTE_VIEWER_PREFS_INIT)) {
            String[] properties = {
                    PaletteViewerPreferences.PREFERENCE_LAYOUT,
                    PaletteViewerPreferences.PREFERENCE_AUTO_COLLAPSE,
                    PaletteViewerPreferences.PREFERENCE_COLUMNS_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_LIST_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_ICONS_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_DETAILS_ICON_SIZE,
                    PaletteViewerPreferences.PREFERENCE_FONT
                };
            Bundle bundle = Platform.getBundle(GEF_BUNDLE_ID);
            if(bundle != null) {
                IPreferencesService preferencesService = Platform.getPreferencesService();
                IScopeContext[] contexts = {new InstanceScope()};
                for (int i = 0; i < properties.length; i++) {
                    if(!store.contains(properties[i])) {
                        String val = preferencesService.getString(GEF_BUNDLE_ID, properties[i], "", contexts); //$NON-NLS-1$
                        if(!Common.isEmpty(val)) {
                            store.setValue(properties[i], val);
                        }
                    }
                }
            }
            store.setValue(PREFERENCE_PALETTE_VIEWER_PREFS_INIT,true);
        }
    }

    public boolean setSyntaxStyles(Map<String, NSISSyntaxStyle> map)
    {
        boolean changed = setSyntaxStyle(map,IInstallOptionsConstants.COMMENT_STYLE,new NSISSyntaxStyle(SYNTAX_COMMENTS,null,false,true,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.SECTION_STYLE,new NSISSyntaxStyle(SYNTAX_SECTIONS,null,true,false,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.KEY_STYLE,new NSISSyntaxStyle(ColorManager.BLUE,null,false,false,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.KEY_VALUE_DELIM_STYLE,new NSISSyntaxStyle(ColorManager.RED,null,false,false,false,false));
        changed |= setSyntaxStyle(map,IInstallOptionsConstants.NUMBER_STYLE,new NSISSyntaxStyle(SYNTAX_NUMBERS,null,true,false,false,false));
        return changed;
    }

    private boolean setSyntaxStyle(Map<String, NSISSyntaxStyle> map, String name, NSISSyntaxStyle style)
    {
        if(!map.containsKey(name) || map.get(name) == null) {
            map.put(name,style);
            return true;
        }
        return false;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        mName = (String)getBundle().getHeaders().get("Bundle-Name"); //$NON-NLS-1$
        mPreferences = new InstanceScope().getNode(mName);
        mImageManager = new ImageManager(this);
        cShellImage = mImageManager.getImage(getResourceString("installoptions.icon")); //$NON-NLS-1$
        initializePreferences();
        mJobScheduler.start();
        new Thread(new Runnable(){
            public void run()
            {
                InstallOptionsBuilder.buildWorkspace(null);
            }
        }, InstallOptionsPlugin.getResourceString("workspace.build.thread.name")).start(); //$NON-NLS-1$
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        mJobScheduler.stop();
        mImageManager = null;
        super.stop(context);
    }

    public JobScheduler getJobScheduler()
    {
        return mJobScheduler;
    }

    public void log(Throwable t)
    {
        ILog log = getLog();
        if(log != null) {
            IStatus status;
            if(t instanceof CoreException) {
                status = ((CoreException)t).getStatus();
            }
            else {
                String message = t.getMessage();
                status = new Status(IStatus.ERROR,PLUGIN_ID,IStatus.ERROR, message==null?t.getClass().getName():message,t);
            }
            log.log(status);
        }
        else {
            t.printStackTrace();
        }
    }

    public static Image getShellImage()
    {
        return cShellImage;
    }

    public static synchronized File getPluginStateLocation()
    {
        if(cStateLocation == null) {
            InstallOptionsPlugin plugin = getDefault();
            if(plugin != null) {
                cStateLocation = plugin.getStateLocation().toFile();
            }
        }
        return cStateLocation;
    }

    public void savePreferences()
    {
        try {
            mPreferences.flush();
        }
        catch (BackingStoreException e) {
            log(e);
        }
    }
}
