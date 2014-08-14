/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help;

import java.io.*;
import java.util.Map;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;

public class NSISUsageProvider implements IEclipseNSISService
{
    private static NSISUsageProvider cInstance = null;

    private Map<String, String> mUsages = null;
    private String mLineSeparator;
    private INSISHomeListener mNSISHomeListener = null;

    public static NSISUsageProvider getInstance()
    {
        return cInstance;
    }

    public void start(IProgressMonitor monitor)
    {
        if (cInstance == null) {
            mUsages = new CaseInsensitiveMap<String>();
            mNSISHomeListener = new INSISHomeListener() {
                public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
                {
                    loadUsages(monitor);
                }
            };
            mLineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
            loadUsages(monitor);
            NSISPreferences.getInstance().addListener(mNSISHomeListener);
            cInstance = this;
        }
    }

    public boolean isStarted()
    {
        return cInstance != null;
    }

    public void stop(IProgressMonitor monitor)
    {
        if (cInstance == this) {
            cInstance = null;
            NSISPreferences.getInstance().removeListener(mNSISHomeListener);
            mUsages = null;
            mNSISHomeListener = null;
            mLineSeparator = null;
        }
    }

    public String getUsage(String keyWord)
    {
        if(!Common.isEmpty(keyWord)) {
            return mUsages.get(keyWord);
        }
        else {
            return null;
        }
    }

    private synchronized void loadUsages(IProgressMonitor monitor)
    {
        try {
            if(monitor != null) {
                monitor.beginTask("", 1); //$NON-NLS-1$
                monitor.subTask(EclipseNSISPlugin.getResourceString("loading.cmdhelp.message")); //$NON-NLS-1$
            }
            mUsages.clear();
            NSISHome home = NSISPreferences.getInstance().getNSISHome();
            NSISExe exe = home==null?null:home.getNSISExe();
            if(exe != null && exe.getFile() != null && exe.getFile().exists()) {
                long exeTimeStamp = exe.getFile().lastModified();

                File stateLocation = EclipseNSISPlugin.getPluginStateLocation();
                File cacheFile = new File(stateLocation,NSISUsageProvider.class.getName()+".Usages.ser"); //$NON-NLS-1$
                long cacheTimeStamp = 0;
                if(cacheFile.exists()) {
                    cacheTimeStamp = cacheFile.lastModified();
                }

                if(exeTimeStamp != cacheTimeStamp) {
                    String[] output = MakeNSISRunner.runProcessWithOutput(exe.getFile().getAbsolutePath(),new String[]{
                        MakeNSISRunner.MAKENSIS_VERBOSITY_OPTION+"1", //$NON-NLS-1$
                        MakeNSISRunner.MAKENSIS_CMDHELP_OPTION},
                        null,1);

                    if(!Common.isEmptyArray(output)) {
                        StringBuffer buf = null;
                        for (int i = 0; i < output.length; i++) {
                            String line = output[i];
                            if(buf == null) {
                                buf = new StringBuffer(line);
                            }
                            else {
                                if(Character.isWhitespace(line.charAt(0))) {
                                    buf.append(mLineSeparator).append(line);
                                }
                                else {
                                    setUsage(buf.toString());
                                    buf = new StringBuffer(line);
                                }
                            }
                        }
                        if(buf != null && buf.length() > 0) {
                            setUsage(buf.toString());
                        }
                    }
                    try {
                        IOUtility.writeObject(cacheFile,mUsages);
                        cacheFile.setLastModified(exeTimeStamp);
                    }
                    catch (IOException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
                else {
                    try {
                        mUsages = IOUtility.readObject(cacheFile);
                    }
                    catch (Exception e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
        }
        finally {
            if(monitor != null) {
                monitor.done();
            }
        }
    }

    private void setUsage(String usage)
    {
        int n = usage.indexOf(" "); //$NON-NLS-1$
        String keyword = n > 0?usage.substring(0,n):usage;
        mUsages.put(keyword,usage);
    }
}
