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
import java.util.List;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.settings.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class NSISCompileTestUtility
{
    public static final NSISCompileTestUtility INSTANCE = new NSISCompileTestUtility();

    private static final long cEclipseStartTime = Long.parseLong(System.getProperty("eclipse.startTime",String.valueOf(System.currentTimeMillis()))); //$NON-NLS-1$

    private NSISHeaderAssociationManager mHeaderAssociationManager = NSISHeaderAssociationManager.getInstance();
    private Map<File, MakeNSISResults> mResultsMap;
    private Pattern mNSISExtPattern = Pattern.compile(INSISConstants.NSI_WILDCARD_EXTENSION,Pattern.CASE_INSENSITIVE);

    private NSISCompileTestUtility()
    {
        File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
        final File resultsCacheFile = new File(stateLocation, getClass().getName() + ".ResultsCache.ser"); //$NON-NLS-1$
        EclipseNSISPlugin.getDefault().registerService(new IEclipseNSISService() {
            private boolean mStarted = false;

            public boolean isStarted()
            {
                return mStarted;
            }

            @SuppressWarnings("unchecked")
            private Map<File, MakeNSISResults> loadMap(File file)
            {
                Map<File, MakeNSISResults> map = null;
                if(IOUtility.isValidFile(file)) {
                    Object obj = null;
                    try {
                        obj = IOUtility.readObject(file);
                    }
                    catch (Exception e) {
                        obj = null;
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                    if (obj != null && Map.class.isAssignableFrom(obj.getClass())) {
                        map = (Map<File, MakeNSISResults>)obj;
                    }
                }
                if(map == null) {
                    map = new MRUMap<File, MakeNSISResults>(20);
                }
                else {
                    if(!(map instanceof MRUMap)) {
                        map = new MRUMap<File, MakeNSISResults>(20, map);
                    }
                }
                return map;
            }

            private void storeMap(File file, Map<File, MakeNSISResults> map)
            {
                if(Common.isEmptyMap(map)) {
                    file.delete();
                }
                else {
                    try {
                        IOUtility.writeObject(file, map);
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }

            public void start(IProgressMonitor monitor)
            {
                mResultsMap = loadMap(resultsCacheFile);
                mStarted = true;
            }

            public void stop(IProgressMonitor monitor)
            {
                mStarted = false;
                storeMap(resultsCacheFile, mResultsMap);
            }
        });
    }

    public void removeAssociatedHeader(IFile script, IFile header)
    {
        mHeaderAssociationManager.removeAssociatedHeader(script, header);
    }

    public void addAssociatedHeader(IFile script, IFile header)
    {
        mHeaderAssociationManager.addAssociatedHeader(script, header);
    }

    public void cacheResults(File script, MakeNSISResults results)
    {
        if(results != null) {
            mResultsMap.put(script, results);
        }
        else {
            removeCachedResults(script);
        }
    }

    public MakeNSISResults getCachedResults(File script)
    {
        if(IOUtility.isValidFile(script)) {
            if (!MakeNSISRunner.isCompiling()) {
                MakeNSISResults results = mResultsMap.get(script);
                if(results != null) {
                    if(script.lastModified() > results.getCompileTimestamp()) {
                        if(script.lastModified() < cEclipseStartTime) {
                            results = null;
                            mResultsMap.remove(script);
                        }
                    }
                }
                return results;
            }
        }
        return null;
    }

    public void removeCachedResults(File script)
    {
        if(IOUtility.isValidFile(script)) {
            if (!MakeNSISRunner.isCompiling()) {
                mResultsMap.remove(script);
            }
        }
    }

    public synchronized boolean compile(IPath script)
    {
        return compile(script, false);
    }

    public boolean compile(IPath script, boolean test)
    {
        IPath nsisScript = getCompileScript(script);
        if(nsisScript != null && script != null) {
            IFile scriptFile = (script.getDevice() == null?ResourcesPlugin.getWorkspace().getRoot().getFile(script):null);
            IFile nsisScriptFile = (nsisScript.getDevice() == null?ResourcesPlugin.getWorkspace().getRoot().getFile(nsisScript):null);
            List<IFile> associatedHeaders = (nsisScriptFile == null?null:NSISHeaderAssociationManager.getInstance().getAssociatedHeaders(nsisScriptFile));
            List<IEditorPart> editorList = new ArrayList<IEditorPart>();
            int beforeCompileSave = NSISPreferences.getInstance().getPreferenceStore().getInt(INSISPreferenceConstants.BEFORE_COMPILE_SAVE);
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            outer:
            for (int i = 0; i < windows.length; i++) {
                IWorkbenchPage[] pages = windows[i].getPages();
                for (int j = 0; j < pages.length; j++) {
                    IEditorPart[] editors = pages[j].getDirtyEditors();
                    for (int k = 0; k < editors.length; k++) {
                        IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(editors[k]);
                        switch(beforeCompileSave) {
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ASSOCIATED_CONFIRM:
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ASSOCIATED_AUTO:
                                if(!Common.isEmptyCollection(associatedHeaders)) {
                                    //This will only happen if this is a workspace file.
                                    //If not, this will drop through to the next case.
                                    if(input instanceof IFileEditorInput) {
                                        IFile file = ((IFileEditorInput)input).getFile();
                                        if(associatedHeaders.contains(file) || (nsisScriptFile != null && nsisScriptFile.equals(file))) {
                                            editorList.add(editors[k]);
                                        }
                                    }
                                    continue;
                                }
                                //$FALL-THROUGH$
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_CONFIRM:
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_AUTO:
                                if(scriptFile != null && input instanceof IFileEditorInput) {
                                    if(!scriptFile.equals(((IFileEditorInput)input).getFile())) {
                                        continue;
                                    }
                                }
                                else if (script.getDevice() != null && input != null) {
                                    if(!script.equals(input.getPath())) {
                                        continue;
                                    }
                                }
                                else {
                                    continue;
                                }
                                editorList.add(editors[k]);
                                break outer;
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_CONFIRM:
                            case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_AUTO:
                                String ext;
                                if(input instanceof IFileEditorInput) {
                                    ext = ((IFileEditorInput)input).getFile().getFullPath().getFileExtension();
                                }
                                else if (input != null) {
                                    ext = input.getPath().getFileExtension();
                                }
                                else {
                                    continue;
                                }
                                if(mNSISExtPattern.matcher(ext).matches()) {
                                    editorList.add(editors[k]);
                                }
                        }
                    }
                }
            }
            if(!saveEditors(editorList, beforeCompileSave)) {
                return false;
            }
            new Thread(new NSISCompileRunnable(nsisScript,test),EclipseNSISPlugin.getResourceString("makensis.thread.name")).start(); //$NON-NLS-1$
            return true;
        }
        return false;
    }

    private boolean saveEditors(List<IEditorPart> editors, int beforeCompileSave)
    {
        List<IEditorPart> editors2 = editors;
        if (!Common.isEmptyCollection(editors2)) {
            boolean ok = false;
            String message = null;
            switch(beforeCompileSave) {
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ASSOCIATED_CONFIRM:
                    if(editors2.size() > 1) {
                        StringBuffer buf = new StringBuffer();
                        for (Iterator<IEditorPart> iter = editors2.iterator(); iter.hasNext();) {
                            IEditorPart editor = iter.next();
                            buf.append(INSISConstants.LINE_SEPARATOR).append(((IFileEditorInput)editor.getEditorInput()).getFile().getFullPath().toString());
                        }
                        message = EclipseNSISPlugin.getFormattedString("compile.save.associated.confirmation", //$NON-NLS-1$
                                new String[]{buf.toString()});
                        break;
                    }
                    //$FALL-THROUGH$
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_CONFIRM:
                    IEditorPart editor = editors2.get(0);
                    if(editors2.size() > 1) {
                        editors2 = editors2.subList(0,1);
                    }
                    IPathEditorInput input = NSISEditorUtilities.getPathEditorInput(editor);
                    IPath path = (input instanceof IFileEditorInput?((IFileEditorInput)input).getFile().getFullPath():input.getPath());
                    message = EclipseNSISPlugin.getFormattedString("compile.save.current.confirmation", //$NON-NLS-1$
                                                                   new String[]{path.toString()});
                    break;
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_CONFIRM:
                    message = EclipseNSISPlugin.getResourceString("compile.save.all.confirmation"); //$NON-NLS-1$
                    break;
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_CURRENT_AUTO:
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ASSOCIATED_AUTO:
                case INSISPreferenceConstants.BEFORE_COMPILE_SAVE_ALL_AUTO:
                    ok = true;
            }
            Shell shell = Display.getDefault().getActiveShell();
            if(!ok) {
                MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                        EclipseNSISPlugin.getShellImage(), message,
                        MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0,
                        EclipseNSISPlugin.getResourceString("compile.save.toggle.message"),false); //$NON-NLS-1$
                dialog.open();
                ok = dialog.getReturnCode() == IDialogConstants.OK_ID;
                if(ok && dialog.getToggleState()) {
                    NSISPreferences.getInstance().setBeforeCompileSave(beforeCompileSave|INSISPreferenceConstants.BEFORE_COMPILE_SAVE_AUTO_FLAG);
                    NSISPreferences.getInstance().store();
                }
            }
            if(ok) {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                dialog.open();
                IProgressMonitor progressMonitor = dialog.getProgressMonitor();
                if(editors2.size() > 1) {
                    progressMonitor.beginTask(EclipseNSISPlugin.getResourceString("saving.before.compilation.task.name"),editors2.size()); //$NON-NLS-1$
                    for (Iterator<IEditorPart> iter = editors2.iterator(); iter.hasNext();) {
                        IEditorPart editor = iter.next();
                        SubProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 1);
                        editor.doSave(monitor);
                        if(monitor.isCanceled()) {
                            break;
                        }
                    }
                }
                else {
                    editors2.get(0).doSave(progressMonitor);
                }
                dialog.close();
                if (progressMonitor.isCanceled()) {
                    return false;
                }
            }
            return ok;
        }
        return true;
    }

    public IPath getCompileScript(IPath input)
    {
        if(input != null) {
            String ext = input.getFileExtension();
            if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,ext,true)) {
                return input;
            }
            else if(Common.stringsAreEqual(INSISConstants.NSH_EXTENSION,ext,true) && input.getDevice() == null) {
                IFile header = getFile(input);
                IFile script =  mHeaderAssociationManager.getAssociatedScript(header);
                if(script != null) {
                    IPath path = script.getFullPath();
                    if(path != null) {
                        if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,path.getFileExtension(),true)) {
                            return path;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean canTest(IPath script)
    {
        return getExeName(script) != null;
    }

    private String getExeName(IPath script)
    {
        IPath script2 = getCompileScript(script);
        if(script2 != null) {
            if(script2.getDevice() == null) {
                return getExeName(getFile(script2));
            }
            else {
                return getExeName(new File(script2.toOSString()));
            }
        }
        return null;
    }

    private boolean validateHeadersTimestamps(long exeTimestamp, Collection<IFile> headers)
    {
        if(!Common.isEmptyCollection(headers)) {
            for (Iterator<IFile> iter = headers.iterator(); iter.hasNext();) {
                IFile header = iter.next();
                if(header != null && header.exists()) {
                    if(!header.isSynchronized(IResource.DEPTH_ZERO) || header.getLocalTimeStamp() > exeTimestamp) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String getExeName(File file)
    {
        MakeNSISResults results = getCachedResults(file);
        if(results != null) {
            String outputFileName = results.getOutputFileName();
            if(outputFileName != null) {
                File exeFile = new File(outputFileName);
                if(IOUtility.isValidFile(exeFile)) {
                    long exeTimestamp = exeFile.lastModified();
                    long fileTimestamp = file.lastModified();
                    if(exeTimestamp >= fileTimestamp) {
                        return exeFile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private String getExeName(IFile file)
    {
        if(file != null && file.isSynchronized(IResource.DEPTH_ZERO)) {
            if (!MakeNSISRunner.isCompiling()) {
                try {
                    String temp = file.getPersistentProperty(INSISConstants.NSIS_COMPILE_TIMESTAMP);
                    if(temp != null) {
                        long nsisCompileTimestamp = Long.parseLong(temp);
                        if(nsisCompileTimestamp >= file.getLocalTimeStamp()) {
                            temp = file.getPersistentProperty(INSISConstants.NSIS_EXE_NAME);
                            if(temp != null) {
                                File exeFile = new File(temp);
                                if(exeFile.exists()) {
                                    temp = file.getPersistentProperty(INSISConstants.NSIS_EXE_TIMESTAMP);
                                    if(temp != null) {
                                        long exeTimestamp = exeFile.lastModified();
                                        if(Long.parseLong(temp) == exeTimestamp) {
                                            if(validateHeadersTimestamps(exeTimestamp, mHeaderAssociationManager.getAssociatedHeaders(file))) {
                                                return exeFile.getAbsolutePath();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception ex) {
                    EclipseNSISPlugin.getDefault().log(ex);
                }
            }
        }
        return null;
    }

    public void test(IPath script)
    {
        test(getExeName(script), EclipseNSISPlugin.getDefault().getConsole());
    }

    private void test(final String exeName, final INSISConsole console)
    {
        if(exeName != null) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    BusyIndicator.showWhile(Display.getDefault(),new Runnable() {
                        public void run()
                        {
                            MakeNSISRunner.testInstaller(exeName, console);
                        }
                    });
                }
            });
        }
    }

    private IFile getFile(IPath path)
    {
        return path != null?ResourcesPlugin.getWorkspace().getRoot().getFile(path):null;
    }

    private class NSISCompileRunnable implements Runnable, INSISConsoleLineProcessor
    {
        private String mOutputExeName = null;
        private IPath mScript = null;
        private boolean mTest = false;
        private INSISConsoleLineProcessor mDelegate;

        public NSISCompileRunnable(IPath script, boolean test)
        {
            mScript = script;
            mTest = test;
            mDelegate = new NSISConsoleLineProcessor(mScript);
        }

        public void run()
        {
            if(mScript != null) {
                reset();
                MakeNSISResults results;
                if(mScript.getDevice() == null) {
                    results = MakeNSISRunner.compile(getFile(mScript), EclipseNSISPlugin.getDefault().getConsole(), this);
                }
                else {
                    File file = new File(mScript.toOSString());
                    results = MakeNSISRunner.compile(file, NSISPreferences.getInstance(),
                                                     EclipseNSISPlugin.getDefault().getConsole(),this);
                }
                if(results != null) {
                    mOutputExeName = results.getOutputFileName();
                    if(mTest && mOutputExeName != null) {
                        test(mOutputExeName, null);
                    }
                }
            }
        }

        public NSISConsoleLine processText(String text)
        {
            return mDelegate.processText(text);
        }

        public void reset()
        {
            mOutputExeName = null;
            mDelegate.reset();
        }
    }
}
