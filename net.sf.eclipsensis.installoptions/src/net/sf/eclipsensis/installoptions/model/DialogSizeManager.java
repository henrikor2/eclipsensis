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

import java.beans.*;
import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.preference.IPreferenceStore;

public class DialogSizeManager
{
    public static final String PROPERTY_DIALOGSIZES = "dialogsizes"; //$NON-NLS-1$
    public static final String PROPERTY_DIALOGSIZES_PREFIX = PROPERTY_DIALOGSIZES+"."; //$NON-NLS-1$
    public static final String PROPERTY_DIALOGSIZES_COUNT = PROPERTY_DIALOGSIZES_PREFIX + "count"; //$NON-NLS-1$
    private static final String SEPARATOR = new String(new char[]{'\u00FF'});
    private static List<DialogSize> cPresetDialogSizes = null;
    private static Map<String, DialogSize> cDialogSizes = null;
    private static PropertyChangeSupport mListeners = new PropertyChangeSupport(DialogSizeManager.class);

    private DialogSizeManager()
    {
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener)
    {
        mListeners.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener)
    {
        mListeners.removePropertyChangeListener(listener);
    }

    public static DialogSize getDefaultDialogSize()
    {
        synchronized(DialogSizeManager.class) {
            loadDialogSizes();
            for (Iterator<DialogSize> iter = cDialogSizes.values().iterator(); iter.hasNext();) {
                DialogSize element = iter.next();
                if(element.isDefault()) {
                    return element;
                }
            }
            return IInstallOptionsConstants.DEFAULT_DIALOG_SIZE;
        }
    }

    public static DialogSize getDialogSize(String name)
    {
        synchronized(DialogSizeManager.class) {
            loadDialogSizes();
            return cDialogSizes.get(name);
        }
    }

    public static DialogSize getDialogSize(Dimension dim)
    {
        DialogSize dialogSize = getDefaultDialogSize();
        if(dialogSize.getSize().equals(dim)) {
            return dialogSize;
        }
        else {
            for (Iterator<DialogSize> iter = cDialogSizes.values().iterator(); iter.hasNext();) {
                DialogSize element = iter.next();
                if(element.getSize().equals(dim)) {
                    return element;
                }
            }
        }
        return null;
    }

    public static List<DialogSize> getDialogSizes()
    {
        synchronized(DialogSizeManager.class) {
            loadDialogSizes();

            return new ArrayList<DialogSize>(cDialogSizes.values());
        }
    }

    private static void loadDialogSizes()
    {
        synchronized(DialogSizeManager.class) {
            if(cDialogSizes == null) {
                IPreferenceStore store = InstallOptionsPlugin.getDefault().getPreferenceStore();
                setDialogSizes(loadDialogSizes(store));
            }
        }
    }

    public static synchronized void setDialogSizes(List<DialogSize> dialogSizes)
    {
        synchronized (DialogSizeManager.class) {
            if(cDialogSizes == null) {
                cDialogSizes = new LinkedHashMap<String, DialogSize>();
            }
            else {
                cDialogSizes.clear();
            }
            List<DialogSize> dialogSizes2 = dialogSizes;
            List<DialogSize> oldList = new ArrayList<DialogSize>(cDialogSizes.values());
            boolean makeCopy = false;
            if(Common.isEmptyCollection(dialogSizes2)) {
                dialogSizes2=getPresetDialogSizes();
                makeCopy = true;
            }
            boolean foundDefault = false;
            for(Iterator<DialogSize> iter=dialogSizes2.iterator(); iter.hasNext(); ) {
                DialogSize dialogSize = iter.next();
                if(makeCopy) {
                    dialogSize = dialogSize.getCopy();
                }
                if(cDialogSizes.containsKey(dialogSize.getName())) {
                    iter.remove();
                    continue;
                }
                cDialogSizes.put(dialogSize.getName(), dialogSize);
                if(dialogSize.isDefault()) {
                    if(foundDefault) {
                        dialogSize.setDefault(false);
                    }
                    else {
                        foundDefault = true;
                    }
                }
            }
            if(!foundDefault) {
                DialogSize dialogSize = cDialogSizes.get(dialogSizes2.get(0).getName());
                dialogSize.setDefault(true);
            }

            if(oldList.size() != dialogSizes2.size() || !oldList.containsAll(dialogSizes2)) {
                mListeners.firePropertyChange(PROPERTY_DIALOGSIZES, oldList, dialogSizes2);
            }
        }
    }

    public static synchronized List<DialogSize> getPresetDialogSizes()
    {
        if(cPresetDialogSizes == null) {
            Object source;
            try {
                source = ResourceBundle.getBundle(DialogSize.class.getPackage().getName()+".DialogSizes"); //$NON-NLS-1$
            }
            catch(MissingResourceException mre) {
                source = null;
            }
            cPresetDialogSizes = loadDialogSizes(source);
            if(cPresetDialogSizes.size() == 0) {
                cPresetDialogSizes.add(IInstallOptionsConstants.DEFAULT_DIALOG_SIZE);
            }
        }
        return cPresetDialogSizes;
    }

    private static String getString(Object source, String name)
    {
        if(source instanceof ResourceBundle) {
            return ((ResourceBundle)source).getString(name);
        }
        else {
            return ((IPreferenceStore)source).getString(name);
        }
    }

    private static List<DialogSize> loadDialogSizes(Object source)
    {
        List<DialogSize> result = new ArrayList<DialogSize>();
        if(source != null) {
            int count = 0;
            try {
                count = Integer.parseInt(getString(source, PROPERTY_DIALOGSIZES_COUNT));
            }
            catch(RuntimeException re) {
                count = 0;
            }

            for(int i=0; i<count; i++) {
                try {
                    String text = getString(source,PROPERTY_DIALOGSIZES_PREFIX+i);
                    if(!Common.isEmpty(text)) {
                        StringTokenizer st = new StringTokenizer(text,SEPARATOR);
                        String name = st.nextToken();
                        boolean isDefault = Boolean.valueOf(st.nextToken()).booleanValue();
                        Dimension dim = new Dimension(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));
                        result.add(new DialogSize(name, isDefault, dim));
                    }
                }
                catch(RuntimeException re) {
                }
            }
        }

        return result;
    }

    public static void storeDialogSizes()
    {
        IPreferenceStore store = InstallOptionsPlugin.getDefault().getPreferenceStore();
        int oldCount = 0;
        try {
            oldCount = Integer.parseInt(store.getString(PROPERTY_DIALOGSIZES_COUNT));
        }
        catch(NumberFormatException nfe) {
            oldCount = 0;
        }
        List<DialogSize> list = getDialogSizes();
        int newCount = list.size();
        for(int i=0; i<newCount; i++) {
            DialogSize ds = list.get(i);
            store.setValue(PROPERTY_DIALOGSIZES_PREFIX+i,new StringBuffer(ds.getName()).append(SEPARATOR).append(
                    ds.isDefault()).append(SEPARATOR).append(ds.getSize().width).append(SEPARATOR).append(
                    ds.getSize().height).toString());
        }
        store.setValue(PROPERTY_DIALOGSIZES_COUNT,newCount);
        if(oldCount > newCount) {
            for(int i=newCount; i<oldCount; i++) {
                store.setValue(PROPERTY_DIALOGSIZES_PREFIX+i,""); //$NON-NLS-1$
            }
        }
    }
}
