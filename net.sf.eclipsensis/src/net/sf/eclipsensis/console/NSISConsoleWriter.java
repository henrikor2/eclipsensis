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

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.MakeNSISProcess;
import net.sf.eclipsensis.util.IOUtility;

public class NSISConsoleWriter implements Runnable
{
    private MakeNSISProcess mProcess = null;
    private INSISConsole mConsole = null;
    private InputStream mInputStream = null;
    private INSISConsoleLineProcessor mLineProcessor = null;
    private List<NSISConsoleLine> mErrors = new ArrayList<NSISConsoleLine>();
    private List<NSISConsoleLine> mWarnings = new ArrayList<NSISConsoleLine>();
    private boolean mRunning = false;

    public NSISConsoleWriter(MakeNSISProcess process, INSISConsole console, InputStream inputStream, INSISConsoleLineProcessor lineProcessor)
    {
        mProcess = process;
        mConsole = console;
        mInputStream = inputStream;
        mLineProcessor = lineProcessor;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        BufferedReader br = null;
        mRunning = true;
        try {
            br = new BufferedReader(new InputStreamReader(mInputStream));
            NSISConsoleLine line;
            String text = br.readLine();
            while(text != null) {
                if(!mProcess.isCanceled()) {
                    if(mLineProcessor != null) {
                        line = mLineProcessor.processText(text);
                    }
                    else {
                        line = NSISConsoleLine.info(text);
                    }
                    appendLine(line);
                    try {
                        text = br.readLine();
                    }
                    catch (IOException ioe) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
        }
        catch(Exception ex) {
            EclipseNSISPlugin.getDefault().log(ex);
            appendLine(NSISConsoleLine.error(ex.getLocalizedMessage()));
        }
        finally {
            IOUtility.closeIO(br);
            mRunning = false;
        }
    }

    public boolean isRunning()
    {
        return mRunning;
    }

    private void appendLine(NSISConsoleLine line)
    {
        mConsole.appendLine(line);
        switch(line.getType()) {
            case NSISConsoleLine.TYPE_ERROR:
                mErrors.add(line);
                break;
            case NSISConsoleLine.TYPE_WARNING:
                mWarnings.add(line);
                break;
            default:
                break;
        }
    }

    public List<NSISConsoleLine> getErrors()
    {
        return mErrors;
    }

    public List<NSISConsoleLine> getWarnings()
    {
        return mWarnings;
    }
}
