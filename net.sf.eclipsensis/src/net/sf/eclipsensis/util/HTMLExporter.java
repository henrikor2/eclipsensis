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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.NSISEditorUtilities;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.projection.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.texteditor.*;

public class HTMLExporter
{
    private Shell mShell;
    private File mPreviousFile;
    private int[][] mProjections;
    private PrintWriter mWriter = null;
    private StyledText mStyledText;
    private boolean mLineNumbersVisible;
    private StyleRange[] mRanges;
    private int mCurrentProjection;
    private int mCurrentRange;
    private int mCurrentOffset;
    private int mCurrentLine;
    private boolean mProjectionEnabled;
    private ITextEditor mEditor;
    private ISourceViewer mViewer;
    private String mTaskName;

    public HTMLExporter(ITextEditor editor, ISourceViewer viewer)
    {
        mEditor = editor;
        mViewer = viewer;
        mShell = mEditor.getSite().getShell();
    }

    public synchronized void exportHTML()
    {
        EclipseNSISPlugin.getDefault().run(false, true, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
            {
                try {
                    IPath path = NSISEditorUtilities.getPathEditorInput(mEditor).getPath();
                    mTaskName = EclipseNSISPlugin.getFormattedString("export.html.task.name", //$NON-NLS-1$
                                                new Object[] {path.toOSString()});
                    monitor.beginTask(mTaskName,100);
                    monitor.subTask(EclipseNSISPlugin.getResourceString("preparing.export.message")); //$NON-NLS-1$
                    while(mShell.getDisplay().readAndDispatch()) { }

                    FileDialog fd = new FileDialog(mShell,SWT.SAVE);
                    fd.setText(EclipseNSISPlugin.getResourceString("export.html.dialog.title")); //$NON-NLS-1$
                    fd.setFilterExtensions(new String[] {EclipseNSISPlugin.getResourceString("export.html.html.file.filter"),EclipseNSISPlugin.getResourceString("export.html.all.file.filter")}); //$NON-NLS-1$ //$NON-NLS-2$
                    fd.setFilterNames(new String[] {EclipseNSISPlugin.getResourceString("export.html.html.file.description"),EclipseNSISPlugin.getResourceString("export.html.all.file.description")}); //$NON-NLS-1$ //$NON-NLS-2$
                    if(mPreviousFile != null) {
                        fd.setFileName(mPreviousFile.getAbsolutePath());
                    }
                    else {
                        fd.setFileName(path.removeFileExtension().addFileExtension(EclipseNSISPlugin.getResourceString("html.extension")).lastSegment()); //$NON-NLS-1$
                    }
                    String filename = fd.open();
                    if(filename != null) {
                        File file = new File(filename);
                        if(file.exists()) {
                            if(!Common.openConfirm(mShell,EclipseNSISPlugin.getFormattedString("save.confirm",new Object[]{file.getAbsolutePath()}), EclipseNSISPlugin.getShellImage())) { //$NON-NLS-1$
                                monitor.setCanceled(true);
                                return;
                            }
                        }
                        monitor.worked(10);
                        mPreviousFile = file;
                        monitor.subTask(EclipseNSISPlugin.getResourceString("exporting.html.message")); //$NON-NLS-1$
                        while(mShell.getDisplay().readAndDispatch()) { }
                        writeHTML(file, monitor);
                        if(file.exists()) {
                            monitor.subTask(EclipseNSISPlugin.getResourceString("opening.file.message")); //$NON-NLS-1$
                            while(mShell.getDisplay().readAndDispatch()) { }
                            Common.openExternalBrowser(IOUtility.getFileURLString(file));
                        }
                    }
                    else {
                        monitor.setCanceled(true);
                    }
                }
                finally {
                    monitor.done();
                }
            }
        });
    }

    @SuppressWarnings("restriction")
    private void writeHTML(File file, IProgressMonitor monitor)
    {
        try {
            reset();
            monitor.subTask(EclipseNSISPlugin.getResourceString("html.header.message")); //$NON-NLS-1$
            while(mShell.getDisplay().readAndDispatch()) { }
            writeHead(file);
            monitor.worked(10);
            if(monitor.isCanceled()) {
                return;
            }
            monitor.subTask(EclipseNSISPlugin.getResourceString("html.body.message")); //$NON-NLS-1$
            while(mShell.getDisplay().readAndDispatch()) { }
            mWriter.print("<body>"); //$NON-NLS-1$
            mWriter.print("<div style=\""); //$NON-NLS-1$
            FontData fontData = mStyledText.getFont().getFontData()[0];
            mWriter.print("font-family: '"); //$NON-NLS-1$
            mWriter.print(fontData.getName());
            mWriter.print("';"); //$NON-NLS-1$
            mWriter.print("font-size: "); //$NON-NLS-1$
            mWriter.print(fontData.getHeight());
            mWriter.print("pt;"); //$NON-NLS-1$
            int style = fontData.getStyle();
            String styleText = makeStyle((style&SWT.BOLD) > 0,(style&SWT.ITALIC) > 0,fontData.data.lfUnderline == 1,
                    fontData.data.lfStrikeOut == 1, mStyledText.getForeground(), mStyledText.getBackground());
            if(styleText != null) {
                mWriter.print(styleText);
            }
            mWriter.println("\">"); //$NON-NLS-1$

            int lineNumberWidth = -1;
            IVerticalRuler ruler = (IVerticalRuler)mEditor.getAdapter(IVerticalRuler.class);
            if(ruler == null) {
                ruler = (IVerticalRuler)mEditor.getAdapter(IVerticalRulerInfo.class);
            }
            int width1 = -1;
            if(ruler instanceof CompositeRuler) {
                CompositeRuler c = (CompositeRuler)ruler;
                for(Iterator<?> iter = c.getDecoratorIterator();iter.hasNext();) {
                    IVerticalRulerColumn col = (IVerticalRulerColumn)iter.next();
                    if(mLineNumbersVisible && col instanceof LineNumberRulerColumn) {
                        width1 = col.getWidth();
                    }
                }
            }
            GC gc = new GC(mStyledText);
            Point p = gc.stringExtent(" "); //$NON-NLS-1$
            gc.dispose();

            if(mLineNumbersVisible) {
                if(width1 < 0) {
                    lineNumberWidth = p.x*Integer.toString(mViewer.getTextWidget().getLineCount()).length();
                }
                else {
                    lineNumberWidth = (int)Math.ceil((double)width1/(double)p.x);
                }
            }
            monitor.worked(10);
            if(monitor.isCanceled()) {
                return;
            }

            IProgressMonitor subMonitor = new NestedProgressMonitor(monitor,mTaskName,60);
            try {
                subMonitor.beginTask(EclipseNSISPlugin.getResourceString("exporting.contents.message"), mRanges.length+2); //$NON-NLS-1$
                while(mShell.getDisplay().readAndDispatch()) { }

                mCurrentLine = 1;
                int total = mStyledText.getCharCount();
                mCurrentOffset = 0;
                mCurrentRange = 0;
                mCurrentProjection = 0;
                mWriter.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"); //$NON-NLS-1$
                startLine();
                subMonitor.worked(1);
                if(subMonitor.isCanceled()) {
                    return;
                }
                while(mCurrentRange < mRanges.length) {
                    StyleRange range = mRanges[mCurrentRange];
                    if(mCurrentOffset < range.start) {
                        writeText(mStyledText.getText(mCurrentOffset,range.start-1),null);
                    }
                    styleText = makeStyle((range.fontStyle&SWT.BOLD) > 0,(range.fontStyle&SWT.ITALIC) > 0,
                                          range.underline,range.strikeout,range.foreground,range.background);
                    writeText(mStyledText.getText(range.start,range.start+range.length-1), styleText);
                    mCurrentOffset = range.start+range.length;
                    mCurrentRange++;
                    subMonitor.worked(1);
                    if(subMonitor.isCanceled()) {
                        return;
                    }
                }
                if(mCurrentOffset < total) {
                    writeText(mStyledText.getText(mCurrentOffset,total-1),null);
                }
                endLine();
                if(subMonitor.isCanceled()) {
                    return;
                }
            }
            finally {
                subMonitor.done();
            }
            monitor.subTask(EclipseNSISPlugin.getResourceString("completing.export.message")); //$NON-NLS-1$
            while(mShell.getDisplay().readAndDispatch()) { }
            mWriter.print("<tr>"); //$NON-NLS-1$
            if(mLineNumbersVisible) {
                mWriter.print("<td><pre>"); //$NON-NLS-1$
                for(int i=0;i<lineNumberWidth; i++) {
                    mWriter.print("&nbsp;"); //$NON-NLS-1$
                }
                mWriter.print("</pre></td>"); //$NON-NLS-1$
            }

            if(mProjectionEnabled) {
                mWriter.print("<td><pre>&nbsp;&nbsp;</pre></td>"); //$NON-NLS-1$
            }
            else {
                mWriter.print("<td><pre>&nbsp;</pre></td>"); //$NON-NLS-1$
            }

            mWriter.println("<td></td></tr>"); //$NON-NLS-1$
            mWriter.println("</table>"); //$NON-NLS-1$
            mWriter.println("</div>"); //$NON-NLS-1$
            mWriter.println("<div id=\"lineDiv\"></div>"); //$NON-NLS-1$
            mWriter.println("<div style=\"text-align: center; font-family: Arial, Helvetica, sans-serif; font-size: 8pt; font-weight: bold;\"><hr>"); //$NON-NLS-1$
            mWriter.println(EclipseNSISPlugin.getResourceString("export.html.branding.text")); //$NON-NLS-1$
            mWriter.println(EclipseNSISPlugin.getResourceString("export.html.copyright.text")); //$NON-NLS-1$
            mWriter.println("</div>"); //$NON-NLS-1$
            mWriter.println("</body>"); //$NON-NLS-1$
            mWriter.println("</html>"); //$NON-NLS-1$
            monitor.worked(10);
        }
        catch (IOException e) {
            EclipseNSISPlugin.getDefault().log(e);
            Common.openError(mShell, e.getMessage(), EclipseNSISPlugin.getShellImage());
            if(file.exists()) {
                file.delete();
            }
        }
        finally {
            IOUtility.closeIO(mWriter);
            if(monitor.isCanceled()) {
                if(file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void startLine()
    {
        mWriter.print("<tr"); //$NON-NLS-1$
        if(mProjectionEnabled) {
            mWriter.print(" id=\"line"); //$NON-NLS-1$
            mWriter.print(mCurrentLine);
        }
        mWriter.println("\">"); //$NON-NLS-1$
        if(mLineNumbersVisible) {
            mWriter.print("<td class=\"lineNum\"><pre>"); //$NON-NLS-1$
            mWriter.print(mCurrentLine);
            mWriter.print("</pre></td>"); //$NON-NLS-1$
        }

        mWriter.print("<td class=\"ruler\">"); //$NON-NLS-1$
        if(mProjectionEnabled && mCurrentProjection < mProjections.length &&
           mProjections[mCurrentProjection][0] == mCurrentLine) {
            mWriter.print("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"collapse\" onClick=\"toggle(this,"); //$NON-NLS-1$
            mWriter.print(mProjections[mCurrentProjection][0]+1);
            mWriter.print(","); //$NON-NLS-1$
            mWriter.print(mProjections[mCurrentProjection][1]);
            mWriter.print(");\" onMouseOver=\"showLine(this,"); //$NON-NLS-1$
            mWriter.print(mProjections[mCurrentProjection][1]);
            mWriter.print(");\" onMouseOut=\"hideLine();\"><tr><td class=\"top\">&nbsp;</td><td class=\"top right\">&nbsp;</td></tr><tr><td>&nbsp;</td><td class=\"right\">&nbsp;</td></tr></table>"); //$NON-NLS-1$
            mCurrentProjection++;
        }
        else {
            mWriter.print("<pre>&nbsp;</pre>"); //$NON-NLS-1$
        }
        mWriter.print("</td>"); //$NON-NLS-1$

        mWriter.print("<td><pre>"); //$NON-NLS-1$
        mCurrentLine++;
    }

    private void writeText(String text, String style) throws IOException
    {
        LineNumberReader lnr = new LineNumberReader(new StringReader(text));
        String line = lnr.readLine();
        while(line != null) {
            writeSpan(line, style);
            line = lnr.readLine();
            if(line == null) {
                //This is the last line. Check if text ends with CR or LF
                char c = text.charAt(text.length()-1);
                if(c != SWT.CR && c != SWT.LF) {
                    break;
                }
            }
            endLine();
            startLine();
        }
    }

    /**
     * @param text
     * @param style
     */
    private void writeSpan(String text, String style)
    {
        if(style != null) {
            mWriter.print("<span style=\""); //$NON-NLS-1$
            mWriter.print(style);
            mWriter.print("\">"); //$NON-NLS-1$
        }
        mWriter.print(text);
        if(style != null) {
            mWriter.print("</span>"); //$NON-NLS-1$
        }
    }

    private void endLine()
    {
        mWriter.println("</pre></td>"); //$NON-NLS-1$
        mWriter.println("</tr>"); //$NON-NLS-1$
    }
    /**
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeHead(File file) throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        String charset = null;
        IDocumentProvider provider = mEditor.getDocumentProvider();
        if(provider instanceof IStorageDocumentProvider) {
            charset = ((IStorageDocumentProvider)provider).getEncoding(mEditor.getEditorInput());
            if(charset == null) {
                charset = ((IStorageDocumentProvider)provider).getDefaultEncoding();
            }
        }
        if(charset == null) {
            charset = System.getProperty("file.encoding"); //$NON-NLS-1$
        }
        if(charset != null) {
            mWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset)));
        }
        else {
            mWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        }

        mWriter.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"); //$NON-NLS-1$
        mWriter.println("<html>"); //$NON-NLS-1$
        mWriter.println("<head>"); //$NON-NLS-1$
        mWriter.print("<title>"); //$NON-NLS-1$
        mWriter.print(NSISEditorUtilities.getPathEditorInput(mEditor).getPath().toOSString());
        mWriter.println("</title>"); //$NON-NLS-1$
        mWriter.print("<meta http-equiv=\"Content-Type\" content=\"text/html");  //$NON-NLS-1$
        if(charset != null) {
            mWriter.print("; charset="); //$NON-NLS-1$
            mWriter.print(charset);
        }
        mWriter.println("\">");  //$NON-NLS-1$
        mWriter.println("<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">"); //$NON-NLS-1$
        mWriter.println("<style type=\"text/css\">"); //$NON-NLS-1$
        mWriter.print("body { background-color: #"); //$NON-NLS-1$
        mWriter.print(ColorManager.rgbToHex(mStyledText.getBackground().getRGB()));
        mWriter.println("}"); //$NON-NLS-1$
        mWriter.println("pre { display: inline }"); //$NON-NLS-1$
        mWriter.println("a { color: #567599; text-decoration: none; }"); //$NON-NLS-1$
        mWriter.println("a:hover { background-color: #F4F4F4; color: #303030; text-decoration: underline}"); //$NON-NLS-1$
        if(mProjectionEnabled) {
            mWriter.println("table.expand { float:right; border: 1px solid #567599; font-size: 1px; cursor: pointer; cursor: hand; }"); //$NON-NLS-1$
            mWriter.println("table.expand td { height: 4px; width: 4px; }"); //$NON-NLS-1$
            mWriter.println("table.expand td.top { border-bottom: 1px solid #567599; }"); //$NON-NLS-1$
            mWriter.println("table.expand  td.right { width: 4px; border-left: 1px solid #567599; }"); //$NON-NLS-1$
            mWriter.println("table.collapse { float:right; border: 1px solid #567599; font-size: 1px; cursor: pointer; cursor: hand; }"); //$NON-NLS-1$
            mWriter.println("table.collapse td { height: 4px; width: 4px; }"); //$NON-NLS-1$
            mWriter.println("table.collapse td.top { border-bottom: 1px solid #567599; }"); //$NON-NLS-1$
            mWriter.println(".hiddenRow { display:none; }"); //$NON-NLS-1$
            mWriter.println("#lineDiv { font-size: 1px; display: none; position: absolute; color: #567599; border-left: solid 1px; border-bottom: solid 1px; width: 1px; height: 1px; }"); //$NON-NLS-1$
        }
        if(mLineNumbersVisible) {
            StringBuffer buf = new StringBuffer("text-align: right; color: #"); //$NON-NLS-1$
            RGB rgb=  null;
            // foreground color
            IPreferenceStore store = EditorsUI.getPreferenceStore();
            String pref = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
            if (store.contains(pref)) {
                if (store.isDefault(pref)) {
                    rgb= PreferenceConverter.getDefaultColor(store, pref);
                }
                else {
                    rgb= PreferenceConverter.getColor(store, pref);
                }
            }
            if (rgb == null) {
                rgb= new RGB(0, 0, 0);
            }
            buf.append(ColorManager.rgbToHex(rgb));
            mWriter.print(".lineNum { "); //$NON-NLS-1$
            mWriter.print(buf.toString());
            mWriter.println(" }"); //$NON-NLS-1$
        }
        mWriter.print(".ruler { background-color: #FFFFFF; text-align: right; border-right: 2px solid #"); //$NON-NLS-1$
        mWriter.print(ColorManager.rgbToHex(mShell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB()));
        mWriter.println(" }"); //$NON-NLS-1$
        mWriter.println("</style>"); //$NON-NLS-1$
        if(mProjectionEnabled) {
            mWriter.println("<script type=\"text/javascript\" language=\"javascript\">"); //$NON-NLS-1$
            mWriter.println("<!--"); //$NON-NLS-1$
            mWriter.println("document.write(\"<style type='text/css'>\");"); //$NON-NLS-1$
            mWriter.println("if(navigator.userAgent.toLowerCase().indexOf(\"netscape6\") >= 0) {"); //$NON-NLS-1$
            mWriter.println("  document.write(\"table.collapse td.right { width: 4px; }\")"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("else {"); //$NON-NLS-1$
            mWriter.println("  document.write(\"table.collapse td.right { width: 5px; }\")"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("document.write(\"</style>\");"); //$NON-NLS-1$
            mWriter.println("if (!String.prototype.endsWith) {"); //$NON-NLS-1$
            mWriter.println("  String.prototype.endsWith = function(suffix) {"); //$NON-NLS-1$
            mWriter.println("    var startPos = this.length - suffix.length;"); //$NON-NLS-1$
            mWriter.println("    if (startPos < 0) {"); //$NON-NLS-1$
            mWriter.println("      return false;"); //$NON-NLS-1$
            mWriter.println("    }"); //$NON-NLS-1$
            mWriter.println("    return (this.lastIndexOf(suffix, startPos) == startPos);"); //$NON-NLS-1$
            mWriter.println("  };"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("function getObject(name)"); //$NON-NLS-1$
            mWriter.println("{"); //$NON-NLS-1$
            mWriter.println("  if(document.all) {"); //$NON-NLS-1$
            mWriter.println("    return document.all[name];"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("  else {"); //$NON-NLS-1$
            mWriter.println("    return document.getElementById(name);"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("function showLine(trigger,end)"); //$NON-NLS-1$
            mWriter.println("{"); //$NON-NLS-1$
            mWriter.println("  if(trigger.className != \"expand\") {"); //$NON-NLS-1$
            mWriter.println("    var lineDiv = getObject(\"lineDiv\");"); //$NON-NLS-1$
            mWriter.println("    var sec = getObject(\"line\"+end);"); //$NON-NLS-1$
            mWriter.println("    if(sec && lineDiv) {"); //$NON-NLS-1$
            mWriter.println("      var triggerPos = getElementPosition(trigger);"); //$NON-NLS-1$
            mWriter.println("      var secPos = getElementPosition(sec);"); //$NON-NLS-1$
            mWriter.println("      if(secPos && triggerPos) {"); //$NON-NLS-1$
            mWriter.println("        lineDiv.style.left = triggerPos.left+triggerPos.width/2;"); //$NON-NLS-1$
            mWriter.println("        lineDiv.style.top = triggerPos.top+triggerPos.height;"); //$NON-NLS-1$
            mWriter.println("        lineDiv.style.width = triggerPos.width/2;"); //$NON-NLS-1$
            mWriter.println("        lineDiv.style.height = secPos.top+secPos.height/2-(triggerPos.top+triggerPos.height);"); //$NON-NLS-1$
            mWriter.println("        lineDiv.style.display = \"block\";"); //$NON-NLS-1$
            mWriter.println("      }"); //$NON-NLS-1$
            mWriter.println("    }"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("function hideLine()"); //$NON-NLS-1$
            mWriter.println("{"); //$NON-NLS-1$
            mWriter.println("  var lineDiv = getObject(\"lineDiv\");"); //$NON-NLS-1$
            mWriter.println("  if(lineDiv) {"); //$NON-NLS-1$
            mWriter.println("    lineDiv.style.display = \"none\";"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("function toggle(trigger,start,end) "); //$NON-NLS-1$
            mWriter.println("{"); //$NON-NLS-1$
            mWriter.println("  if(trigger) {"); //$NON-NLS-1$
            mWriter.println("    var i;"); //$NON-NLS-1$
            mWriter.println("    var sec;"); //$NON-NLS-1$
            mWriter.println("    var expand;"); //$NON-NLS-1$
            mWriter.println("    hideLine();"); //$NON-NLS-1$
            mWriter.println("    if(trigger.className == \"expand\") {"); //$NON-NLS-1$
            mWriter.println("      expand = true;"); //$NON-NLS-1$
            mWriter.println("      trigger.className = \"collapse\";"); //$NON-NLS-1$
            mWriter.println("    }"); //$NON-NLS-1$
            mWriter.println("    else {"); //$NON-NLS-1$
            mWriter.println("      expand = false;"); //$NON-NLS-1$
            mWriter.println("      trigger.className = \"expand\";"); //$NON-NLS-1$
            mWriter.println("    }"); //$NON-NLS-1$
            mWriter.println("    for(i=start; i<= end; i++) {"); //$NON-NLS-1$
            mWriter.println("      sec = getObject(\"line\"+i);"); //$NON-NLS-1$
            mWriter.println("      if(sec) {"); //$NON-NLS-1$
            mWriter.println("        if(expand) {"); //$NON-NLS-1$
            mWriter.println("          if(sec.className == \"hiddenRow\") {"); //$NON-NLS-1$
            mWriter.println("            sec.className = \"\";"); //$NON-NLS-1$
            mWriter.println("          }"); //$NON-NLS-1$
            mWriter.println("          else if(sec.className.endsWith(\" hiddenRow\")) {"); //$NON-NLS-1$
            mWriter.println("            sec.className = sec.className.substr(0,sec.className.length-\" hiddenRow\".length);"); //$NON-NLS-1$
            mWriter.println("          }"); //$NON-NLS-1$
            mWriter.println("        }"); //$NON-NLS-1$
            mWriter.println("        else {"); //$NON-NLS-1$
            mWriter.println("          if(sec.className == \"\") {"); //$NON-NLS-1$
            mWriter.println("            sec.className = \"hiddenRow\";"); //$NON-NLS-1$
            mWriter.println("          }"); //$NON-NLS-1$
            mWriter.println("          else {"); //$NON-NLS-1$
            mWriter.println("            sec.className = sec.className + \" hiddenRow\";"); //$NON-NLS-1$
            mWriter.println("          }"); //$NON-NLS-1$
            mWriter.println("        }"); //$NON-NLS-1$
            mWriter.println("      }"); //$NON-NLS-1$
            mWriter.println("    }"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("function getElementPosition(elem){"); //$NON-NLS-1$
            mWriter.println("  var offsetLeft = 0;"); //$NON-NLS-1$
            mWriter.println("  var offsetTop =0;"); //$NON-NLS-1$
            mWriter.println("  var width = elem.offsetWidth;"); //$NON-NLS-1$
            mWriter.println("  var height = elem.offsetHeight;"); //$NON-NLS-1$
            mWriter.println("  while (elem){"); //$NON-NLS-1$
            mWriter.println("    offsetLeft += elem.offsetLeft;"); //$NON-NLS-1$
            mWriter.println("    offsetTop += elem.offsetTop;"); //$NON-NLS-1$
            mWriter.println("    elem = elem.offsetParent;"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("  if (navigator.userAgent.indexOf('Mac') != -1 && typeof(document.body.leftMargin) !='undefined'){"); //$NON-NLS-1$
            mWriter.println("    offsetLeft += document.body.leftMargin;"); //$NON-NLS-1$
            mWriter.println("    offsetTop += document.body.topMargin;"); //$NON-NLS-1$
            mWriter.println("  }"); //$NON-NLS-1$
            mWriter.println("  return {left:offsetLeft,top:offsetTop, width:width, height: height};"); //$NON-NLS-1$
            mWriter.println("}"); //$NON-NLS-1$
            mWriter.println("//-->"); //$NON-NLS-1$
            mWriter.println("</script>"); //$NON-NLS-1$
        }
        mWriter.println("</head>"); //$NON-NLS-1$
    }

    /**
     *
     */
    private void reset()
    {
        IPreferenceStore store = EditorsUI.getPreferenceStore();
        mLineNumbersVisible =  store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER);
        mStyledText = mViewer.getTextWidget();
        mRanges = mStyledText.getStyleRanges();
        mProjectionEnabled = (mViewer instanceof ProjectionViewer && ((ProjectionViewer)mViewer).isProjectionMode());
        List<int[]> list = new ArrayList<int[]>();
        if(mProjectionEnabled) {
            ProjectionAnnotationModel model = ((ProjectionViewer)mViewer).getProjectionAnnotationModel();
            Iterator<?> iter = model.getAnnotationIterator();
            List<Position> projections = new ArrayList<Position>();
            while(iter.hasNext()) {
                Position pos = model.getPosition((Annotation)iter.next());
                if(pos != null) {
                    projections.add(pos);
                }
            }
            if(projections.size() > 0) {
                Collections.sort(projections, new Comparator<Position>() {
                    public int compare(Position p1, Position p2)
                    {
                        int n = p1.getOffset()-p2.getOffset();
                        if(n == 0) {
                            n = p1.getLength()-p2.getLength();
                        }
                        return n;
                    }
                });
                for (ListIterator<Position> iterator = projections.listIterator(); iterator.hasNext();) {
                    Position pos = iterator.next();
                    list.add(new int[]{mStyledText.getLineAtOffset(pos.getOffset())+1,mStyledText.getLineAtOffset(pos.getOffset()+pos.getLength()-1)+1});
                }
            }
            mProjections = list.toArray(new int[projections.size()][]);
        }
    }

    private String makeStyle(boolean bold, boolean italic, boolean underline,
                             boolean strikeOut, Color fgColor, Color bgColor)
    {
        if(bold || italic || underline || strikeOut || fgColor != null || bgColor != null) {
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            if(bold) {
                buf.append("font-weight: bold;"); //$NON-NLS-1$
            }
            if(italic) {
                buf.append("font-style: italic;"); //$NON-NLS-1$
            }
            if(underline || strikeOut) {
                buf.append("text-decoration:"); //$NON-NLS-1$
                if(underline) {
                    buf.append(" underline"); //$NON-NLS-1$
                }
                if(strikeOut) {
                    buf.append(" line-through"); //$NON-NLS-1$
                }
                buf.append(";"); //$NON-NLS-1$
            }
            if(fgColor != null) {
                buf.append("color: #").append(ColorManager.rgbToHex(fgColor.getRGB())).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(bgColor != null) {
                buf.append("background-color: #").append(ColorManager.rgbToHex(bgColor.getRGB())).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return buf.toString();
        }
        else {
            return null;
        }
    }
}