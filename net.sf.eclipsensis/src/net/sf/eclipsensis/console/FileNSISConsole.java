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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.util.IOUtility;

public class FileNSISConsole implements INSISConsole, IMakeNSISRunListener
{
    private File mFile;
    private boolean mAppend;
    private PrintWriter mWriter = null;

    public FileNSISConsole(File file, boolean append)
    {
        super();
        mFile = file;
        mAppend = append;
    }

    public synchronized void appendLine(NSISConsoleLine line)
    {
        if(MakeNSISRunner.isCompiling()) {
            if(mWriter == null) {
                try {
                    mWriter = new PrintWriter(new BufferedWriter(new FileWriter(mFile,mAppend)));
                    MakeNSISRunner.addListener(this);
                }
                catch (IOException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    mWriter = null;
                }
            }
            if(mWriter != null) {
                mWriter.println(line.toString());
            }
        }
    }

    public void clearConsole()
    {
        // Nothing to do here
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        switch(event.getType()) {
            case MakeNSISRunEvent.STOPPED:
                IOUtility.closeIO(mWriter);
                mWriter = null;
                MakeNSISRunner.removeListener(this);
                break;
        }
    }
}
