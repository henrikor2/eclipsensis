/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.script.NSISScriptProblem;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;

public class MakeNSISRunner implements INSISConstants
{
    public static final Pattern MAKENSIS_SYNTAX_ERROR_PATTERN = Pattern.compile("[\\w]+ expects [0-9\\-\\+]+ parameters, got [0-9]\\."); //$NON-NLS-1$
    public static final Pattern MAKENSIS_INCLUDE_ERROR_PATTERN = Pattern.compile("!include: error in script:? \"(.+)\" on line (\\d+).*",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    public static final Pattern MAKENSIS_ERROR_PATTERN = Pattern.compile("error in script \"(.+)\" on line (\\d+).*",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    public static final Pattern MAKENSIS_WARNING_PATTERN = Pattern.compile(".+\\((.+):(\\d+)\\).*"); //$NON-NLS-1$
    public static final Pattern MAKENSIS_WARNINGS_PATTERN = Pattern.compile("([1-9][0-9]*) warnings?:"); //$NON-NLS-1$
    public static final String MAKENSIS_OUTFILE_PREFIX = "Output:"; //$NON-NLS-1$
    public static final Pattern MAKENSIS_OUTFILE_PATTERN = Pattern.compile(MAKENSIS_OUTFILE_PREFIX+" \"?([^\"]+)\"?"); //$NON-NLS-1$

    public static final String MAKENSIS_NOTIFYHWND_OPTION = EclipseNSISPlugin.getResourceString("makensis.notifyhwnd.option"); //$NON-NLS-1$
    public static final String MAKENSIS_EXECUTE_OPTION = EclipseNSISPlugin.getResourceString("makensis.execute.option"); //$NON-NLS-1$
    public static final String MAKENSIS_DEFINE_OPTION = EclipseNSISPlugin.getResourceString("makensis.define.option"); //$NON-NLS-1$
    public static final String MAKENSIS_NOCD_OPTION = EclipseNSISPlugin.getResourceString("makensis.nocd.option"); //$NON-NLS-1$
    public static final String MAKENSIS_NOCONFIG_OPTION = EclipseNSISPlugin.getResourceString("makensis.noconfig.option"); //$NON-NLS-1$
    public static final String MAKENSIS_LICENSE_OPTION = EclipseNSISPlugin.getResourceString("makensis.license.option"); //$NON-NLS-1$
    public static final String MAKENSIS_VERSION_OPTION = EclipseNSISPlugin.getResourceString("makensis.version.option"); //$NON-NLS-1$
    public static final String MAKENSIS_HDRINFO_OPTION = EclipseNSISPlugin.getResourceString("makensis.hdrinfo.option"); //$NON-NLS-1$
    public static final String MAKENSIS_VERBOSITY_OPTION = EclipseNSISPlugin.getResourceString("makensis.verbosity.option"); //$NON-NLS-1$
    public static final String MAKENSIS_PROCESS_PRIORITY_OPTION = EclipseNSISPlugin.getResourceString("makensis.process.priority.option"); //$NON-NLS-1$
    public static final String MAKENSIS_CMDHELP_OPTION = EclipseNSISPlugin.getResourceString("makensis.cmdhelp.option"); //$NON-NLS-1$

    private static MakeNSISProcess cCompileProcess = null;
    private static String cBestCompressorFormat = null;
    private static String cCompileLock = "lock"; //$NON-NLS-1$
    private static IMakeNSISDelegate cMakeNSISDelegate;
    private static Set<IMakeNSISRunListener> cListeners = new HashSet<IMakeNSISRunListener>();
    private static IPath cScript = null;

    public static final int COMPRESSOR_DEFAULT = 0;
    public static final int COMPRESSOR_BEST;
    public static final String[] COMPRESSOR_DISPLAY_ARRAY;
    public static final String[] COMPRESSOR_NAME_ARRAY;

    private static final Object cJobFamily = new Object();

    private static final MessageFormat cCompilationTimeFormat;
    private static final MessageFormat cTotalCompilationTimeFormat;

    static {
        cCompilationTimeFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("compilation.time.format")); //$NON-NLS-1$
        cTotalCompilationTimeFormat = new MessageFormat(EclipseNSISPlugin.getResourceString("total.compilation.time.format")); //$NON-NLS-1$
        cBestCompressorFormat = EclipseNSISPlugin.getResourceString("best.compressor.format"); //$NON-NLS-1$

        int index = COMPRESSOR_DEFAULT + 1;
        String[] compressorArray = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),
        "compressors"); //$NON-NLS-1$
        COMPRESSOR_NAME_ARRAY = new String[compressorArray.length+2];
        COMPRESSOR_DISPLAY_ARRAY = new String[compressorArray.length+2];

        COMPRESSOR_NAME_ARRAY[COMPRESSOR_DEFAULT] = ""; //$NON-NLS-1$
        COMPRESSOR_DISPLAY_ARRAY[COMPRESSOR_DEFAULT] = EclipseNSISPlugin.getResourceString("compressor.default.text"); //$NON-NLS-1$
        for(int i=0; i<compressorArray.length; i++) {
            COMPRESSOR_NAME_ARRAY[index] = compressorArray[i];
            COMPRESSOR_DISPLAY_ARRAY[index] = EclipseNSISPlugin.getResourceString(new StringBuffer("compressor.").append( //$NON-NLS-1$
                            compressorArray[i]).append(
                            ".text").toString(), //$NON-NLS-1$
                            compressorArray[i].toUpperCase());
            index++;
        }
        COMPRESSOR_BEST = index;
        COMPRESSOR_NAME_ARRAY[COMPRESSOR_BEST] = "best"; //$NON-NLS-1$
        COMPRESSOR_DISPLAY_ARRAY[COMPRESSOR_BEST] = EclipseNSISPlugin.getResourceString("compressor.best.text"); //$NON-NLS-1$
    }

    private MakeNSISRunner()
    {
    }

    public static IPath getScript()
    {
        return cScript;
    }

    public static void addListener(IMakeNSISRunListener listener)
    {
        cListeners.add(listener);
    }

    public static void removeListener(IMakeNSISRunListener listener)
    {
        cListeners.remove(listener);
    }

    private static void notifyListeners(int type, IPath path, Object data)
    {
        MakeNSISRunEvent event = new MakeNSISRunEvent(type, path, data);
        IMakeNSISRunListener[] listeners = cListeners.toArray(new IMakeNSISRunListener[cListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].eventOccurred(event);
        }
    }

    public static boolean cancel()
    {
        synchronized(cCompileLock) {
            if(cCompileProcess != null) {
                cCompileProcess.cancel();
                return cCompileProcess.isCanceled();
            }
        }
        return false;
    }

    public static boolean isCompiling()
    {
        synchronized(cCompileLock) {
            return cCompileProcess != null;
        }
    }

    /**
     * @param process The process to set.
     */
    private static void setCompileProcess(IPath script, MakeNSISProcess process)
    {
        synchronized(cCompileLock) {
            cScript = script;
            cCompileProcess = process;
        }
    }

    private static List<NSISScriptProblem> processProblems(INSISConsole console, IPath path, List<NSISConsoleLine> errors, List<NSISConsoleLine> warnings)
    {
        List<NSISScriptProblem> problems = new ArrayList<NSISScriptProblem>();
        List<NSISConsoleLine> errors2 = errors;
        if (Common.isEmptyCollection(errors2)) {
            List<String> compileErrors = cMakeNSISDelegate.getErrors();
            if(!Common.isEmptyCollection(compileErrors)) {
                errors2 = new ArrayList<NSISConsoleLine>();
                StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                for(Iterator<String> iter = compileErrors.iterator(); iter.hasNext() ;) {
                    String text = iter.next();
                    if(text.endsWith("\n")) { //$NON-NLS-1$
                        buf.append(text.substring(0,text.length()-1));
                        if(buf.length() > 0) {
                            problems.add(new NSISScriptProblem(path,NSISScriptProblem.TYPE_ERROR,buf.toString()));
                        }
                        buf.setLength(0);
                    }
                    else {
                        buf.append(text);
                    }
                }
                if(buf.length() > 0) {
                    problems.add(new NSISScriptProblem(path,NSISScriptProblem.TYPE_ERROR,buf.toString()));
                }
            }
        }
        else {
            int nextIndex = 0;
            ListIterator<NSISConsoleLine> iter = errors2.listIterator();

            while(iter.hasNext()) {
                NSISConsoleLine error = iter.next();
                while (error.getLineNum() <= 0 && iter.hasNext()) {
                    error = iter.next();
                }
                nextIndex = iter.nextIndex();
                int lineNum = error.getLineNum() > 0?error.getLineNum():0;
                String text = error.toString();
                IPath p = error.getSource() == null?path:error.getSource();
                iter.previous();
                //reset the linenum for errors which don't have a linenum
                while(iter.hasPrevious()) {
                    NSISConsoleLine err = iter.previous();
                    if(err.getLineNum() <= 0) {
                        err.setLineNum(lineNum);
                        continue;
                    }
                    break;
                }

                NSISScriptProblem problem = new NSISScriptProblem(p,NSISScriptProblem.TYPE_ERROR,text,lineNum);
                error.setProblem(problem);
                problems.add(problem);
                iter = errors2.listIterator(nextIndex);
            }
        }

        if(Common.isEmptyCollection(warnings)) {
            List<String> compileWarnings = cMakeNSISDelegate.getWarnings();
            if (compileWarnings != null) {
                CaseInsensitiveMap<String> map = new CaseInsensitiveMap<String>();
                for (Iterator<String> iter = compileWarnings.iterator(); iter.hasNext();) {
                    String text = iter.next();
                    if(text.toLowerCase().startsWith("warning:")) { //$NON-NLS-1$
                        text = text.substring(8).trim();
                    }
                    if(!map.containsKey(text)) {
                        Matcher matcher = MakeNSISRunner.MAKENSIS_WARNING_PATTERN.matcher(text);
                        int lineNum = 1;
                        if(matcher.matches()) {
                            IPath path2 = new Path(matcher.group(1));
                            if(path.getDevice() == null) {
                                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path2);
                                if(file != null) {
                                    path2 = file.getFullPath();
                                }
                            }
                            if(path.equals(path2)) {
                                lineNum = Integer.parseInt(matcher.group(2));
                            }
                        }
                        problems.add(new NSISScriptProblem(path, NSISScriptProblem.TYPE_WARNING, text, lineNum));
                        map.put(text,text);
                    }
                }
            }
        }
        else {
            CaseInsensitiveMap<String> map = new CaseInsensitiveMap<String>();
            for(Iterator<NSISConsoleLine> iter=warnings.iterator(); iter.hasNext(); ) {
                NSISConsoleLine warning = iter.next();
                if(warning.getLineNum() > 0) {
                    String text = warning.toString().trim();
                    if(text.toLowerCase().startsWith("warning:")) { //$NON-NLS-1$
                        text = text.substring(8).trim();
                    }
                    if(!map.containsKey(text)) {
                        NSISScriptProblem problem = new NSISScriptProblem(path, NSISScriptProblem.TYPE_WARNING, text,
                                        (warning.getLineNum() > 0?warning.getLineNum():1));
                        warning.setProblem(problem);
                        problems.add(problem);
                        map.put(text,text);
                    }
                }
            }
        }
        return problems;
    }

    public static synchronized MakeNSISResults compile(IFile script, INSISConsole console, INSISConsoleLineProcessor outputProcessor)
    {
        return compile(script.getFullPath(), NSISProperties.getProperties(script), console, outputProcessor);
    }

    public static synchronized MakeNSISResults compile(IFile script, INSISConsole console, INSISConsoleLineProcessor outputProcessor, boolean notifyHwnd)
    {
        return compile(script.getFullPath(), NSISProperties.getProperties(script), console, outputProcessor, notifyHwnd);
    }

    public static synchronized MakeNSISResults compile(File script, NSISSettings settings, INSISConsole console, INSISConsoleLineProcessor outputProcessor)
    {
        return compile(new Path(script.getAbsolutePath()), settings, console, outputProcessor);
    }

    public static synchronized MakeNSISResults compile(File script, NSISSettings settings, INSISConsole console, INSISConsoleLineProcessor outputProcessor, boolean notifyHwnd)
    {
        return compile(new Path(script.getAbsolutePath()), settings, console, outputProcessor, notifyHwnd);
    }

    public static synchronized MakeNSISResults compile(final IPath script, NSISSettings settings, INSISConsole console, INSISConsoleLineProcessor outputProcessor)
    {
        return compile(script, settings, console, outputProcessor, true);
    }

    public static synchronized MakeNSISResults compile(final IPath script, NSISSettings settings, INSISConsole console, INSISConsoleLineProcessor outputProcessor, boolean notifyHwnd)
    {
        MakeNSISResults results = null;
        if (NSISPreferences.getInstance().getNSISHome() != null && script != null) {
            IFile ifile = null;
            try {
                try {
                    notifyListeners(MakeNSISRunEvent.STARTED, script, null);
                    console.clearConsole();
                    cMakeNSISDelegate.reset();
                    File file;
                    if (script.getDevice() == null) {
                        //Workspace file
                        ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(script);
                        IPath location = ifile.getLocation();
                        if(location != null) {
                            file = location.toFile();
                        }
                        else {
                            throw new IllegalArgumentException(EclipseNSISPlugin.getResourceString("local.filesystem.error")); //$NON-NLS-1$
                        }
                    }
                    else {
                        file = new File(script.toOSString());
                    }
                    String fileName = file.getAbsolutePath();
                    File workDir = file.getParentFile();
                    List<String> options = new ArrayList<String>();
                    if (settings.getVerbosity() != settings.getDefaultVerbosity()) {
                        options.add(MAKENSIS_VERBOSITY_OPTION + settings.getVerbosity());
                    }
                    if(NSISPreferences.getInstance().isProcessPrioritySupported() && settings.getProcessPriority() != settings.getDefaultProcessPriority()) {
                        options.add(MAKENSIS_PROCESS_PRIORITY_OPTION + settings.getProcessPriority());
                    }

                    NSISKeywords keywords = NSISKeywords.getInstance();
                    String setCompressorFinalOption = new StringBuffer(MAKENSIS_EXECUTE_OPTION).append(keywords.getKeyword("SetCompressor")).append( //$NON-NLS-1$
                    " ").append(keywords.getKeyword("/FINAL")).append( //$NON-NLS-1$ //$NON-NLS-2$
                    " ").toString(); //$NON-NLS-1$
                    String solidOption = keywords.getKeyword("/SOLID"); //$NON-NLS-1$
                    boolean solidSupported = keywords.isValidKeyword(solidOption);
                    int compressor = settings.getCompressor();
                    boolean solidCompression = settings.getSolidCompression();
                    if (compressor != MakeNSISRunner.COMPRESSOR_DEFAULT && compressor != MakeNSISRunner.COMPRESSOR_BEST) {
                        StringBuffer buf = new StringBuffer(setCompressorFinalOption);
                        if (solidCompression && solidSupported) {
                            buf.append(solidOption).append(" "); //$NON-NLS-1$
                        }
                        buf.append(MakeNSISRunner.COMPRESSOR_NAME_ARRAY[compressor]);
                        options.add(buf.toString());
                    }
                    if (settings.getHdrInfo()) {
                        options.add(MAKENSIS_HDRINFO_OPTION);
                    }
                    if (settings.getLicense()) {
                        options.add(MAKENSIS_LICENSE_OPTION);
                    }
                    if (settings.getNoConfig()) {
                        options.add(MAKENSIS_NOCONFIG_OPTION);
                    }
                    if (settings.getNoCD()) {
                        options.add(MAKENSIS_NOCD_OPTION);
                    }
                    Map<String,String> symbols = settings.getSymbols();
                    if (symbols != null) {
                        for (Iterator<Map.Entry<String, String>> iter = symbols.entrySet().iterator(); iter.hasNext();) {
                            Map.Entry<String, String> entry = iter.next();
                            String key = entry.getKey();
                            String value = entry.getValue();
                            if (!Common.isEmpty(key)) {
                                StringBuffer buf = new StringBuffer(MAKENSIS_DEFINE_OPTION).append(key);
                                if (!Common.isEmpty(value)) {
                                    buf.append("=").append(value); //$NON-NLS-1$
                                }
                                options.add(buf.toString());
                            }
                        }
                    }
                    List<String> instructions = settings.getInstructions();
                    if (instructions != null) {
                        for (Iterator<String> iter = instructions.iterator(); iter.hasNext();) {
                            String instruction = iter.next();
                            if (!Common.isEmpty(instruction)) {
                                if (compressor == MakeNSISRunner.COMPRESSOR_DEFAULT || !instruction.toLowerCase().startsWith(keywords.getKeyword("SetCompressor").toLowerCase() + " ")) { //$NON-NLS-1$ //$NON-NLS-2$
                                    options.add(MAKENSIS_EXECUTE_OPTION + instruction);
                                }
                            }
                        }
                    }

                    int rv = 0;
                    String[] optionsArray = options.toArray(Common.EMPTY_STRING_ARRAY);
                    int cmdArrayLen = optionsArray.length + (compressor != MakeNSISRunner.COMPRESSOR_BEST?3:4)-(notifyHwnd?0:2);
                    String[] cmdArray = new String[cmdArrayLen];
                    System.arraycopy(optionsArray, 0, cmdArray, cmdArrayLen - optionsArray.length - (notifyHwnd?3:1), optionsArray.length);
                    if(notifyHwnd) {
                        cmdArray[cmdArrayLen - 3] = MAKENSIS_NOTIFYHWND_OPTION;
                        cmdArray[cmdArrayLen - 2] = Long.toString(cMakeNSISDelegate.getHwnd());
                    }
                    cmdArray[cmdArrayLen - 1] = fileName;
                    List<NSISConsoleLine> consoleErrors = new ArrayList<NSISConsoleLine>();
                    List<NSISConsoleLine> consoleWarnings = new ArrayList<NSISConsoleLine>();
                    try {
                        if (compressor != MakeNSISRunner.COMPRESSOR_BEST) {
                            results = runCompileProcess(script, cmdArray, null, workDir, console, outputProcessor, consoleErrors, consoleWarnings, settings.showStatistics(), notifyHwnd);
                        }
                        else {
                            long n = System.currentTimeMillis();
                            int count = solidSupported?2:1;
                            File tempFile = null;
                            String bestCompressor = null;
                            long bestFileSize = Long.MAX_VALUE;
                            int padding;
                            try {
                                padding = Integer.parseInt(EclipseNSISPlugin.getResourceString("summary.compressor.name.padding")); //$NON-NLS-1$
                            }
                            catch (Exception e) {
                                padding = 31;
                            }
                            List<String> compressorSummaryList = new ArrayList<String>();
                            int maxLineLength = 0;
                            outer: for (int i = 0; i < MakeNSISRunner.COMPRESSOR_NAME_ARRAY.length; i++) {
                                if (i != MakeNSISRunner.COMPRESSOR_DEFAULT && i != MakeNSISRunner.COMPRESSOR_BEST) {
                                    for (int j = 0; j < count; j++) {
                                        consoleErrors.clear();
                                        consoleWarnings.clear();
                                        outputProcessor.reset();
                                        StringBuffer buf = new StringBuffer(setCompressorFinalOption);
                                        if (j == 1 && solidSupported) {
                                            buf.append(solidOption).append(" "); //$NON-NLS-1$
                                        }
                                        buf.append(MakeNSISRunner.COMPRESSOR_NAME_ARRAY[i]);
                                        cmdArray[0] = buf.toString();

                                        buf = new StringBuffer(MakeNSISRunner.COMPRESSOR_NAME_ARRAY[i]);
                                        if (j == 1) {
                                            buf.append(" ").append(solidOption); //$NON-NLS-1$
                                        }
                                        String compressorName = buf.toString();
                                        String summaryCompressorName = Common.padString(EclipseNSISPlugin.getFormattedString("summary.compressor.name.format",  //$NON-NLS-1$
                                                        new String[]{compressorName}), padding);
                                        MakeNSISResults tempresults = runCompileProcess(script, cmdArray, null, workDir, console,
                                                        outputProcessor, consoleErrors, consoleWarnings,
                                                        settings.showStatistics(), notifyHwnd);
                                        if (tempresults.getReturnCode() != MakeNSISResults.RETURN_SUCCESS) {
                                            results = tempresults;
                                            if (tempFile != null && tempFile.exists()) {
                                                tempFile.delete();
                                            }
                                            tempFile = null;
                                            break outer;
                                        }
                                        else {
                                            File outputFile = new File(tempresults.getOutputFileName());
                                            if (IOUtility.isValidFile(outputFile)) {
                                                if (settings.showStatistics()) {
                                                    summaryCompressorName = EclipseNSISPlugin.getFormattedString(
                                                                    "summary.line.format", new Object[]{summaryCompressorName, new Long(outputFile.length())}); //$NON-NLS-1$
                                                    maxLineLength = Math.max(summaryCompressorName.length(), maxLineLength);
                                                    compressorSummaryList.add(summaryCompressorName);
                                                }
                                                if (bestCompressor == null || outputFile.length() < bestFileSize) {
                                                    bestCompressor = compressorName;
                                                    if (tempFile == null) {
                                                        tempFile = new File(outputFile.getAbsolutePath() + EclipseNSISPlugin.getResourceString("makensis.tempfile.suffix")); //$NON-NLS-1$
                                                    }
                                                    if (tempFile.exists()) {
                                                        tempFile.delete();
                                                    }
                                                    bestFileSize = outputFile.length();
                                                    outputFile.renameTo(tempFile);
                                                    results = tempresults;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (results != null && tempFile != null && tempFile.exists()) {
                                File outputFile = new File(results.getOutputFileName());
                                if (!IOUtility.isValidFile(outputFile)) {
                                    tempFile.renameTo(outputFile);
                                }
                                if (settings.showStatistics()) {
                                    console.appendLine(NSISConsoleLine.info("")); //$NON-NLS-1$
                                    String header = EclipseNSISPlugin.getResourceString("summary.header"); //$NON-NLS-1$
                                    maxLineLength = Math.max(maxLineLength, header.length());
                                    char[] c = new char[maxLineLength];
                                    Arrays.fill(c, '-');
                                    String dashes = new String(c);
                                    console.appendLine(NSISConsoleLine.info(EclipseNSISPlugin.getResourceString("summary.title"))); //$NON-NLS-1$
                                    console.appendLine(NSISConsoleLine.info(dashes));
                                    console.appendLine(NSISConsoleLine.info(header));
                                    console.appendLine(NSISConsoleLine.info(dashes));
                                    for (Iterator<String> iter = compressorSummaryList.iterator(); iter.hasNext();) {
                                        console.appendLine(NSISConsoleLine.info(iter.next()));
                                    }
                                    console.appendLine(NSISConsoleLine.info(dashes));
                                    console.appendLine(NSISConsoleLine.info("")); //$NON-NLS-1$
                                    console.appendLine(NSISConsoleLine.info(MessageFormat.format(cBestCompressorFormat, new Object[]{bestCompressor})));
                                }
                            }
                            if (settings.showStatistics() && results != null && results.getReturnCode() != MakeNSISResults.RETURN_CANCEL) {
                                console.appendLine(NSISConsoleLine.info(cTotalCompilationTimeFormat.format(splitCompilationTime((int)(System.currentTimeMillis() - n)))));
                            }
                        }
                    }
                    catch (Throwable t) {
                        EclipseNSISPlugin.getDefault().log(t);
                    }
                    finally {
                        if (results != null) {
                            results.setProblems(processProblems(console,
                                            script, consoleErrors, consoleWarnings));
                            rv = results.getReturnCode();
                        }
                        if (rv == MakeNSISResults.RETURN_CANCEL) {
                            console.appendLine(NSISConsoleLine.error(EclipseNSISPlugin.getResourceString("cancel.message"))); //$NON-NLS-1$
                        }
                    }
                }
                catch(Exception ex) {
                    if(Display.getCurrent() != null) {
                        Common.openError(Display.getCurrent().getActiveShell(),ex.getMessage(),EclipseNSISPlugin.getShellImage());
                    }
                    else {
                        EclipseNSISPlugin.getDefault().log(ex);
                    }
                }
                finally {
                    try {
                        if(results != null) {
                            if (ifile != null) {
                                String outputFileName = results.getOutputFileName();
                                File exeFile = outputFileName == null?null:new File(outputFileName);
                                try {
                                    int rv = results.getReturnCode();
                                    if (rv == MakeNSISResults.RETURN_SUCCESS && exeFile != null && exeFile.exists()) {
                                        ifile.setPersistentProperty(NSIS_COMPILE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
                                        ifile.setPersistentProperty(NSIS_EXE_NAME, outputFileName);
                                        ifile.setPersistentProperty(NSIS_EXE_TIMESTAMP, Long.toString(exeFile.lastModified()));
                                    }
                                    else {
                                        ifile.setPersistentProperty(NSIS_COMPILE_TIMESTAMP, null);
                                        ifile.setPersistentProperty(NSIS_EXE_NAME, null);
                                        ifile.setPersistentProperty(NSIS_EXE_TIMESTAMP, null);

                                    }
                                    NSISEditorUtilities.getMarkerAssistant(ifile).updateMarkers(results);
                                }
                                catch (CoreException cex) {
                                    EclipseNSISPlugin.getDefault().log(cex);
                                }
                            }
                            else {
                                NSISCompileTestUtility.INSTANCE.cacheResults(results.getScriptFile(), results);
                                NSISEditorUtilities.getMarkerAssistant(results.getScriptFile()).updateMarkers(results);
                            }
                        }
                    }
                    finally {
                        notifyListeners(MakeNSISRunEvent.STOPPED, script, results);
                    }
                }
            }
            finally {
                if(results != null && results.getReturnCode() == MakeNSISResults.RETURN_SUCCESS && results.getOutputFileName() != null) {
                    try {
                        final IFile[] outputFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(new File(results.getOutputFileName()).toURI());
                        if(!Common.isEmptyArray(outputFiles)) {
                            EclipseNSISPlugin.getDefault().getJobScheduler().scheduleJob(cJobFamily,EclipseNSISPlugin.getResourceString("refreshing.makensis.results.job.name"), //$NON-NLS-1$
                                            new IJobStatusRunnable() {
                                public IStatus run(IProgressMonitor monitor)
                                {
                                    for (int i = 0; i < outputFiles.length; i++) {
                                        if(monitor.isCanceled()) {
                                            return Status.CANCEL_STATUS;
                                        }
                                        try {
                                            outputFiles[i].refreshLocal(IResource.DEPTH_ZERO,null);
                                        }
                                        catch (CoreException e) {
                                            return new Status(IStatus.ERROR,PLUGIN_ID,IStatus.OK,e.getMessage(),e);
                                        }
                                    }
                                    return Status.OK_STATUS;
                                }
                            });
                        }
                    }
                    catch(Exception ex) {
                        EclipseNSISPlugin.getDefault().log(ex);
                    }
                }
            }
        }
        return results;
    }

    private static synchronized MakeNSISResults runCompileProcess(IPath script, String[] cmdArray, String[] env, File workDir,
                    INSISConsole console, INSISConsoleLineProcessor outputProcessor,
                    List<NSISConsoleLine> consoleErrors, List<NSISConsoleLine> consoleWarnings, boolean showStatistics,
                    boolean notifyHwnd)
    {
        long n = System.currentTimeMillis();
        MakeNSISResults results = new MakeNSISResults(new File(cmdArray[cmdArray.length-1]));
        String nsisExePath = NSISPreferences.getInstance().getNSISHome().getNSISExe().getFile().getAbsolutePath();
        String commandLine = new StringBuffer(nsisExePath).append(" ").append( //$NON-NLS-1$
                        Common.flatten(cmdArray,' ')).toString();
        if(EclipseNSISPlugin.getDefault().isDebugging()) {
            EclipseNSISPlugin.getDefault().log(new StringBuffer("Command Line:").append(LINE_SEPARATOR).append(commandLine).toString()); //$NON-NLS-1$
        }
        try {
            notifyListeners(MakeNSISRunEvent.CREATED_PROCESS, script, commandLine);
            MakeNSISProcess proc = createProcess(nsisExePath, cmdArray, env, workDir);
            setCompileProcess(script, proc);
            Process process = proc.getProcess();
            InputStream inputStream = process.getInputStream();
            OutputFileConsoleLineProcessor outFileProcessor = new OutputFileConsoleLineProcessor(outputProcessor);
            NSISConsoleWriter mainWriter = new NSISConsoleWriter(proc, console, inputStream, outFileProcessor);
            new Thread(mainWriter,EclipseNSISPlugin.getResourceString("makensis.stdout.thread.name")).start(); //$NON-NLS-1$
            InputStream errorStream = process.getErrorStream();
            NSISConsoleWriter errorWriter = new NSISConsoleWriter(proc, console, errorStream, new INSISConsoleLineProcessor() {
                public NSISConsoleLine processText(String text)
                {
                    return NSISConsoleLine.error(text);
                }

                public void reset()
                {
                }
            });
            new Thread(errorWriter,EclipseNSISPlugin.getResourceString("makensis.stderr.thread.name")).start(); //$NON-NLS-1$

            int rv = process.waitFor();
            while(mainWriter.isRunning()) {
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {
                    console.appendLine(NSISConsoleLine.error(e.getLocalizedMessage()));
                }
            }
            consoleErrors.addAll(mainWriter.getErrors());
            consoleErrors.addAll(errorWriter.getErrors());
            consoleWarnings.addAll(mainWriter.getWarnings());
            if(EclipseNSISPlugin.getDefault().isDebugging()) {
                EclipseNSISPlugin.getDefault().log("Return Code: "+rv); //$NON-NLS-1$
            }
            if(proc.isCanceled()) {
                rv = MakeNSISResults.RETURN_CANCEL;
            }
            results.setReturnCode(rv);
            if(results.getReturnCode() == MakeNSISResults.RETURN_SUCCESS) {
                if (notifyHwnd) {
                    String outputFileName = cMakeNSISDelegate.getOutputFileName();
                    if (outputFileName == null) {
                        int i = 0;
                        while (outputFileName == null && i < 10) {
                            try {
                                Thread.sleep(100);
                            }
                            catch (Exception e) {
                            }
                            outputFileName = cMakeNSISDelegate.getOutputFileName();
                            i++;
                        }
                        if (outputFileName == null) {
                            outputFileName = outFileProcessor.getOutputFileName();
                        }
                    }
                    results.setOutputFileName(outputFileName);
                    if (EclipseNSISPlugin.getDefault().isDebugging()) {
                        EclipseNSISPlugin.getDefault().log("Output File Name: " + outputFileName); //$NON-NLS-1$
                    }
                }
            }
            else {
                results.setCanceled(proc.isCanceled());
            }
        }
        catch(IOException ioe){
            console.appendLine(NSISConsoleLine.error(ioe.getLocalizedMessage()));
        }
        catch(InterruptedException ie){
            console.appendLine(NSISConsoleLine.error(ie.getLocalizedMessage()));
        }
        finally {
            if(showStatistics && results.getReturnCode() != MakeNSISResults.RETURN_CANCEL) {
                console.appendLine(NSISConsoleLine.info(cCompilationTimeFormat.format(splitCompilationTime((int)(System.currentTimeMillis()-n)))));
            }
            setCompileProcess(null, null);
            notifyListeners(MakeNSISRunEvent.COMPLETED_PROCESS, script, commandLine);
        }
        return results;
    }

    private static Integer[] splitCompilationTime(int time)
    {
        int time2 = time;
        Integer[] result = new Integer[3];
        Arrays.fill(result,Common.ZERO);
        result[2] = new Integer(time2 % 1000);
        time2 /= 1000;
        for(int i=1; i>=0; i--) {
            if(time2 > 0) {
                result[i] = new Integer(time2 % 60);
                time2 /= 60;
            }
            else {
                break;
            }
        }

        return result;
    }

    public static void testInstaller(String exeName, INSISConsole console)
    {
        testInstaller(exeName, console, false);
    }

    public static void testInstaller(String exeName, INSISConsole console, boolean wait)
    {
        if(exeName != null) {
            File exeFile = new File(exeName);
            if (IOUtility.isValidFile(exeFile)) {
                File workDir = exeFile.getParentFile();
                try {
                    List<String> args = new ArrayList<String>();
                    if(EclipseNSISPlugin.getDefault().isWinVista())
                    {
                        args.add("cmd.exe"); //$NON-NLS-1$
                        args.add("/c"); //$NON-NLS-1$
                    }
                    args.add(exeName);
                    Process proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]),null,workDir);
                    if(wait) {
                        proc.waitFor();
                    }
                }
                catch(Exception ex) {
                    if(console != null) {
                        console.clearConsole();
                        console.appendLine(NSISConsoleLine.error(ex.getLocalizedMessage()));
                    }
                    else {
                        Common.openError(EclipseNSISPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
                                        ex.getLocalizedMessage(), EclipseNSISPlugin.getShellImage());
                    }
                }
            }
        }
    }

    public synchronized static void setUnicode(boolean unicode)
    {
        if(cMakeNSISDelegate != null) {
            if(cMakeNSISDelegate.isUnicode() == unicode) {
                return;
            }
        }
        loadMakeNSISDelegate(unicode);
    }

    private synchronized static void loadMakeNSISDelegate(boolean unicode)
    {
        unloadMakeNSISDelegate();
        try {
            cMakeNSISDelegate = unicode?(IMakeNSISDelegate)new MakeNSISDelegateU():
                (IMakeNSISDelegate)new MakeNSISDelegate();
            cMakeNSISDelegate.startup();
        }
        catch (Exception e) {
            EclipseNSISPlugin.getDefault().log(e);
            unloadMakeNSISDelegate();
        }
    }

    private synchronized static void unloadMakeNSISDelegate()
    {
        if(cMakeNSISDelegate != null) {
            cMakeNSISDelegate.shutdown();
            cMakeNSISDelegate = null;
        }
    }

    public synchronized static void startup()
    {
        if(cMakeNSISDelegate == null) {
            NSISHome home = NSISPreferences.getInstance().getNSISHome();
            loadMakeNSISDelegate(home != null && home.getNSISExe().isUnicode());
        }
    }

    public synchronized static void shutdown()
    {
        unloadMakeNSISDelegate();
    }

    public static String[] runProcessWithOutput(String makensisExe, String[] cmdArray, File workDir)
    {
        return runProcessWithOutput(makensisExe, cmdArray, workDir, 0);
    }

    public static String[] runProcessWithOutput(String makensisExe, String[] cmdArray, File workDir, int validReturnCode)
    {
        String[] output = null;
        try {
            MakeNSISProcess proc = createProcess(makensisExe, cmdArray, null, workDir);
            Process process = proc.getProcess();
            new Thread(new RunnableInputStreamReader(process.getErrorStream(),false),EclipseNSISPlugin.getResourceString("makensis.stderr.thread.name")).start(); //$NON-NLS-1$
            output = new RunnableInputStreamReader(process.getInputStream()).getOutput();
            int rv = process.waitFor();
            IOUtility.closeIO(process.getOutputStream());
            if(rv != validReturnCode) {
                output = null;
            }
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
        }
        catch (InterruptedException e) {
            EclipseNSISPlugin.getDefault().log(e);
            output = null;
        }

        return output;
    }

    private static MakeNSISProcess createProcess(String makeNSISExe, String[] cmdArray, String[] env, File workDir) throws IOException
    {
        if(!Common.isEmptyArray(cmdArray))
        {
            String[] newCmdArray = new String[cmdArray.length];
            for (int i = 0; i < cmdArray.length; i++)
            {
                newCmdArray[i] = Common.escapeQuotes(cmdArray[i],"\\","\\"); //$NON-NLS-1$ //$NON-NLS-2$
                newCmdArray[i] = Common.escapeQuotes(newCmdArray[i],"\"","\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            cmdArray = newCmdArray;
        }
        String[] newCmdArray = new String[1+ (Common.isEmptyArray(cmdArray)?0:cmdArray.length)];
        newCmdArray[0] = makeNSISExe;
        if(!Common.isEmptyArray(cmdArray)) {
            System.arraycopy(cmdArray,0,newCmdArray,1,cmdArray.length);
        }
        return new MakeNSISProcess(Runtime.getRuntime().exec(newCmdArray,env,workDir));
    }

    private static class OutputFileConsoleLineProcessor implements INSISConsoleLineProcessor
    {
        private INSISConsoleLineProcessor mDelegate;
        private String mOutputFileName = null;

        public OutputFileConsoleLineProcessor(INSISConsoleLineProcessor delegate)
        {
            mDelegate = delegate;
        }

        public NSISConsoleLine processText(String text)
        {
            if(text.startsWith(MAKENSIS_OUTFILE_PREFIX)) {
                Matcher m = MAKENSIS_OUTFILE_PATTERN.matcher(text);
                if(m.matches()) {
                    mOutputFileName = m.group(1);
                }
            }
            return mDelegate.processText(text);
        }

        public void reset()
        {
            mOutputFileName = null;
            mDelegate.reset();
        }

        protected String getOutputFileName()
        {
            return mOutputFileName;
        }
    }
}
