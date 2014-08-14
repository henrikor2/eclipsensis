/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.util.regex.Matcher;

import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class NSISConsoleLineProcessor implements INSISConsoleLineProcessor
{
    private int mWarningCount = 0;
    private boolean mErrorMode = false;
    private IPath mScript = null;

    public NSISConsoleLineProcessor(IPath script)
    {
        mScript = script;
    }

    public NSISConsoleLine processText(String text)
    {
        NSISConsoleLine line;
        String text2 = text.trim();

        String lText = text2.toLowerCase();
        if(lText.startsWith("error")) { //$NON-NLS-1$
            Matcher matcher = MakeNSISRunner.MAKENSIS_ERROR_PATTERN.matcher(text2);
            if(matcher.matches()) {
                line = NSISConsoleLine.error(text2);
                setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                return line;
            }
        }
        if(lText.startsWith("!include: error")) { //$NON-NLS-1$
            Matcher matcher = MakeNSISRunner.MAKENSIS_INCLUDE_ERROR_PATTERN.matcher(text2);
            if(matcher.matches()) {
                line = NSISConsoleLine.error(text2);
                setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                return line;
            }
        }
        if(lText.startsWith("error ") || lText.startsWith("error:") || //$NON-NLS-1$ //$NON-NLS-2$
           lText.startsWith("!include: error ") || lText.startsWith("!include: error:") || lText.startsWith("invalid command")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            line = NSISConsoleLine.error(text2);
        }
        else if(lText.startsWith("warning ") || lText.startsWith("warning:") || lText.startsWith("invalid ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            line = NSISConsoleLine.warning(text2);
        }
        else if(lText.endsWith(" warning:") || lText.endsWith(" warnings:")) { //$NON-NLS-1$ //$NON-NLS-2$
            Matcher matcher = MakeNSISRunner.MAKENSIS_WARNINGS_PATTERN.matcher(text2);
            if(matcher.matches()) {
                mWarningCount = Integer.parseInt(matcher.group(1));
            }
            line = NSISConsoleLine.warning(text2);
        }
        else if(MakeNSISRunner.MAKENSIS_SYNTAX_ERROR_PATTERN.matcher(lText).matches()) {
            mErrorMode = true;
            line = NSISConsoleLine.error(text2);
        }
        else if(mErrorMode) {
            line = NSISConsoleLine.error(text2);
        }
        else if(mWarningCount > 0 && !Common.isEmpty(text2)) {
            mWarningCount--;
            line = NSISConsoleLine.warning(text2);
        }
        else {
            line = NSISConsoleLine.info(text2);
        }
        if(line.getType() == NSISConsoleLine.TYPE_WARNING) {
            Matcher matcher = MakeNSISRunner.MAKENSIS_WARNING_PATTERN.matcher(text2);
            if(matcher.matches()) {
                setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            }
            else if(!text2.endsWith("warnings:") && !text2.endsWith("warning:")) { //$NON-NLS-1$ //$NON-NLS-2$
                setLineInfo(line, (mScript.getDevice() != null?mScript:null), 1);
            }
        }

        return line;
    }

    private void setLineInfo(NSISConsoleLine line, IPath path, int lineNum)
    {
        IPath path2 = path;
        int lineNum2 = lineNum;
        if(path2 != null) {
            if(path2.toString().startsWith("macro:")) { //$NON-NLS-1$
                //TODO Add macro discovery here.
                path2 = null;
                lineNum2 = 1;
            }
        }
        if(path2 == null) {
            path2 = mScript;
        }
        else {
            if(mScript.getDevice() == null) {
                if(!path2.isAbsolute()) {
                    path2 = ResourcesPlugin.getWorkspace().getRoot().getFile(mScript).getParent().getFullPath().append(path2);
                }
                else {
                    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path2);
                    if(file != null) {
                        path2 = file.getFullPath();
                    }
                }
            }
            else {
                if(!path2.isAbsolute()) {
                    path2 = mScript.removeLastSegments(1).append(path2);
                }
            }
        }
        line.setSource(path2);
        line.setLineNum(lineNum2);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.console.INSISConsoleLineProcessor#reset()
     */
    public void reset()
    {
        mWarningCount = 0;
        mErrorMode = false;
    }
}
