/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class NSISHeaderAssociationManager implements IEclipseNSISService/*, IResourceChangeListener*/
{
    private static NSISHeaderAssociationManager cInstance = null;
    private static File cCacheFile = new File(EclipseNSISPlugin.getPluginStateLocation(),"net.sf.eclipsensis.NSISHeaderAssociations.ser"); //$NON-NLS-1$
    private static IWorkspaceRoot cRoot = ResourcesPlugin.getWorkspace().getRoot();

    private HashMap<IFile, List<IFile>> mScriptMap;
    private HashMap<IFile, IFile> mHeaderMap;

    public static NSISHeaderAssociationManager getInstance()
    {
        return cInstance;
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            load();
//            cRoot.getWorkspace().addResourceChangeListener(this,IResourceChangeEvent.POST_CHANGE);
            cInstance = this;
        }
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
//            cRoot.getWorkspace().removeResourceChangeListener(this);
            store();
        }
    }

    private void store()
    {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        for (Iterator<Map.Entry<IFile,List<IFile>>> iter = mScriptMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<IFile,List<IFile>> entry = iter.next();
            IFile script = entry.getKey();
            if(IOUtility.isValidFile(script)) {
                List<IFile> headers = entry.getValue();
                if(Common.isEmptyCollection(headers)) {
                    iter.remove();
                }
                else {
                    List<String> list = new ArrayList<String>();
                    for (Iterator<IFile> iterator = headers.iterator(); iterator.hasNext();) {
                        IFile header = iterator.next();
                        if(IOUtility.isValidFile(header)) {
                            list.add(header.getFullPath().toString());
                        }
                    }
                    if(list.isEmpty()) {
                        iter.remove();
                    }
                    else {
                        map.put(script.getFullPath().toString(),list);
                    }
                }
            }
            else {
                iter.remove();
            }
        }
        try {
            IOUtility.writeObject(cCacheFile,map);
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
    }

    private void load()
    {
        Map<String,List<String>> cache = null;
        if(IOUtility.isValidFile(cCacheFile)) {
            try {
                cache = IOUtility.readObject(cCacheFile);
            }
            catch (Exception e) {
                cache = null;
            }
        }
        mScriptMap = new HashMap<IFile, List<IFile>>();
        mHeaderMap = new HashMap<IFile, IFile>();

        if (cache != null) {
            for (Iterator<Map.Entry<String,List<String>>> iter = cache.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<String,List<String>> entry = iter.next();
                try {
                    IFile script = cRoot.getFile(new Path(entry.getKey()));
                    if(IOUtility.isValidFile(script)) {
                        List<String> list = entry.getValue();
                        List<IFile> headers = new ArrayList<IFile>();
                        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
                            try {
                                IFile header = cRoot.getFile(new Path(iterator.next()));
                                if(IOUtility.isValidFile(header)) {
                                    headers.add(header);
                                    mHeaderMap.put(header,script);
                                }
                                else {
                                    iterator.remove();
                                }
                            }
                            catch (Exception e) {
                            }
                        }
                        if(!list.isEmpty()) {
                            mScriptMap.put(script,headers);
                        }
                    }
                }
                catch(Exception ex) {
                }
            }
        }
    }

    public List<IFile> getAssociatedHeaders(IFile script)
    {
        List<IFile> list = mScriptMap.get(script);
        return list != null?Collections.unmodifiableList(list):Collections.<IFile>emptyList();
    }

    public void addAssociatedHeader(IFile script, IFile header)
    {
        associateWithScript(header, script);
    }

    public void removeAssociatedHeader(IFile script, IFile header)
    {
        disassociateFromScript(header);
    }

    public void associateWithScript(IFile header, IFile script)
    {
        if (header != null) {
            IFile oldScript = mHeaderMap.get(header);
            if (!Common.objectsAreEqual(oldScript, script)) {
                if (oldScript != null) {
                    List<IFile> list = mScriptMap.get(oldScript);
                    if (list != null) {
                        list.remove(header);
                        if(list.isEmpty()) {
                            mScriptMap.remove(oldScript);
                        }
                    }
                }
                if (script != null) {
                    List<IFile> list = mScriptMap.get(script);
                    if (list == null) {
                        list = new ArrayList<IFile>();
                        mScriptMap.put(script,list);
                    }
                    list.add(header);
                }
            }
            if(script != null) {
                mHeaderMap.put(header,script);
            }
            else {
                mHeaderMap.remove(header);
            }
        }
    }

    public void disassociateFromScript(IFile header)
    {
        associateWithScript(header, null);
    }

    public IFile getAssociatedScript(IFile header)
    {
        if(header != null) {
            return mHeaderMap.get(header);
        }
        return null;
    }
//
//    public void resourceChanged(IResourceChangeEvent event)
//    {
//        try {
//            switch (event.getType())
//            {
//                case IResourceChangeEvent.POST_CHANGE:
//                    System.out.println("Resources have changed.");
//                    event.getDelta().accept(new DeltaPrinter());
//                    break;
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    class DeltaPrinter implements IResourceDeltaVisitor {
//        public boolean visit(IResourceDelta delta) {
//           IResource res = delta.getResource();
//           switch (delta.getKind()) {
//              case IResourceDelta.REMOVED:
//                 System.out.print("Resource ");
//                 System.out.print(res.getFullPath());
//                 System.out.println(" was removed.");
//                 break;
//              case IResourceDelta.CHANGED:
//                 System.out.print("Resource ");
//                 System.out.print(res.getFullPath());
//                 System.out.println(" has changed.");
//                 break;
//           }
//           if((delta.getFlags() & IResourceDelta.OPEN) > 0) {
//               boolean open = res.getProject().isOpen();
//              System.out.print("Resource ");
//              System.out.print(res.getFullPath());
//              System.out.println(" was "+(open?"opened":"closed"));
//           }
//           return true; // visit the children
//        }
//     }
}
