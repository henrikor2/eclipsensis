/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class NSISOutlineContentResources implements IEclipseNSISService,  INSISKeywordsListener
{
    private static NSISOutlineContentResources cInstance = null;

    private static final String[] cTypes = {"!define", "!if","!ifdef", "!ifndef", "!ifmacrodef",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        "!ifmacrondef", "!else", "!else if","!else ifdef", "!else ifndef", "!else ifmacrodef",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        "!else ifmacrondef", "!endif", "!macro", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "Function", "FunctionEnd", "Section", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "SubSection", "SubSectionEnd", "SectionGroup", "SectionGroupEnd",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "Page", "PageEx", "PageExEnd","!include","Var", "Name","#label","#global label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    private static final Set<String> cClosingTypes = new HashSet<String>(Arrays.asList("!endif", "!macroend",  //$NON-NLS-1$ //$NON-NLS-2$
                    "FunctionEnd", "SectionEnd",  //$NON-NLS-1$ //$NON-NLS-2$
                    "SubSectionEnd", "SectionGroupEnd",  //$NON-NLS-1$ //$NON-NLS-2$
                    "PageExEnd","Name")); //$NON-NLS-1$ //$NON-NLS-2$
    private static final File cFilterCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),NSISOutlineContentResources.class.getName()+".Filter.ser"); //$NON-NLS-1$

    private Map<Type, Integer> mTypeIndexes = new HashMap<Type, Integer>();
    private Map<String, Type> mTypes = null;
    private List<NSISContentOutlinePage> mPages = null;
    private Collection<String> mFilteredTypes = null;

    public static NSISOutlineContentResources getInstance()
    {
        return cInstance;
    }

    private void load()
    {
        mTypeIndexes.clear();
        mTypes.clear();
        for(int i=0; i<cTypes.length; i++) {
            boolean pseudo = false;
            String type = cTypes[i];
            String typeName;
            if(type.charAt(0) == '#') {
                pseudo = true;
                type = typeName = type.substring(1);
            }
            else {
                typeName = NSISKeywords.getInstance().getKeyword(type, false);
                if(!NSISKeywords.getInstance().isValidKeyword(typeName)) {
                    continue;
                }
            }
            Image image = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString(new StringBuffer("outline.").append( //$NON-NLS-1$
                            type.toLowerCase().replaceAll("!","").replaceAll(" ",".")).append(".icon").toString(),null));
            Type t = new Type(typeName, type, image, pseudo, cClosingTypes.contains(type));
            mTypeIndexes.put(t, i);
            mTypes.put(typeName,t);
        }
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            try {
                monitor.beginTask("", 1); //$NON-NLS-1$
                monitor.subTask(EclipseNSISPlugin.getResourceString("loading.outline.message")); //$NON-NLS-1$
                mTypeIndexes = new HashMap<Type, Integer>();
                mTypes = new CaseInsensitiveMap<Type>();
                mPages = new ArrayList<NSISContentOutlinePage>();
                load();
                if(IOUtility.isValidFile(cFilterCacheFile)) {
                    try {
                        mFilteredTypes = IOUtility.readObject(cFilterCacheFile);
                    }
                    catch (Exception e) {
                        EclipseNSISPlugin.getDefault().log(e);
                        mFilteredTypes = new CaseInsensitiveSet();
                    }
                }
                else {
                    mFilteredTypes = new CaseInsensitiveSet();
                }
                NSISKeywords.getInstance().addKeywordsListener(this);
                cInstance = this;
            }
            finally {
                monitor.done();
            }
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISKeywords.getInstance().removeKeywordsListener(this);
            if(!Common.isEmptyCollection(mFilteredTypes)) {
                try {
                    IOUtility.writeObject(cFilterCacheFile,mFilteredTypes);
                }
                catch (IOException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
            else if(IOUtility.isValidFile(cFilterCacheFile)) {
                cFilterCacheFile.delete();
            }
            mTypeIndexes = null;
            mTypes = null;
            mPages = null;
        }
    }

    void connect(NSISContentOutlinePage page)
    {
        if(!mPages.contains(page)) {
            mPages.add(page);
        }
    }

    void disconnect(NSISContentOutlinePage page)
    {
        mPages.remove(page);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.help.INSISKeywordsListener#keywordsChanged()
     */
    public void keywordsChanged()
    {
        load();
        if(Display.getCurrent() == null) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    refreshPages();
                }
            });
        }
        else {
            refreshPages();
        }
    }

    private void refreshPages()
    {
        for(Iterator<NSISContentOutlinePage> iter=mPages.iterator(); iter.hasNext(); ) {
            iter.next().refresh();
        }
    }

    public Collection<Type> getTypes()
    {
        return Collections.unmodifiableCollection(mTypes.values());
    }

    public Type getType(String typeName)
    {
        if(typeName.endsWith(":")) //$NON-NLS-1$
        {
            //label
            return mTypes.get("label"); //$NON-NLS-1$
        }
        return mTypes.get(typeName);
    }

    public int getTypeIndex(Type type)
    {
        return type != null && mTypeIndexes.containsKey(type)?mTypeIndexes.get(type):-1;
    }

    public Collection<String> getFilteredTypes()
    {
        return Collections.unmodifiableCollection(mFilteredTypes);
    }

    public void setFilteredTypes(Collection<String> collection)
    {
        mFilteredTypes.clear();
        if(collection != null) {
            mFilteredTypes.addAll(collection);
        }
    }

    public static class Type
    {
        private String mName;
        private String mType;
        private Image mImage;
        private boolean mPseudo;
        private boolean mClosing;

        public Type(String name, String type, Image image, boolean pseudo, boolean closing)
        {
            super();
            mName = name;
            mType = type;
            mImage = image;
            mPseudo = pseudo;
            mClosing = closing;
        }

        public String getName()
        {
            return mName;
        }

        public String getType()
        {
            return mType;
        }

        public Image getImage()
        {
            return mImage;
        }

        public boolean isPseudo()
        {
            return mPseudo;
        }

        public boolean isClosing()
        {
            return mClosing;
        }

        @Override
        public String toString()
        {
            return getName();
        }
    }
}
