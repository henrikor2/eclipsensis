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
import java.util.*;

import net.sf.eclipsensis.script.NSISScriptProblem;

public class MakeNSISResults implements Serializable
{
    private static final long serialVersionUID = -5087200320522747802L;

    public static final int RETURN_SUCCESS = 0;
    public static final int RETURN_CANCEL = -1;

    private long mCompileTimestamp = System.currentTimeMillis();
    private int mReturnCode = RETURN_SUCCESS;
    private String mOutputFileName = null;
    private File mScriptFile = null;
    private List<NSISScriptProblem> mProblems = null;
    private boolean mCanceled = false;


    public MakeNSISResults(File file)
    {
        mScriptFile = file;
    }

    public long getCompileTimestamp()
    {
        return mCompileTimestamp;
    }
    /**
     * @return Returns the problems.
     */
    public List<NSISScriptProblem> getProblems()
    {
        return (mProblems==null?null:Collections.unmodifiableList(mProblems));
    }
    /**
     * @param errors The error to set.
     */
    void setProblems(List<NSISScriptProblem> errors)
    {
        mProblems = errors;
    }
    /**
     * @return Returns the outputFileName.
     */
    public String getOutputFileName()
    {
        return mOutputFileName;
    }
    /**
     * @param outputFileName The outputFileName to set.
     */
    void setOutputFileName(String outputFileName)
    {
        mOutputFileName = outputFileName;
    }
    /**
     * @return Returns the scriptFile.
     */
    public File getScriptFile()
    {
        return mScriptFile;
    }
    /**
     * @param scriptFile The scriptFile to set.
     */
    void setScriptFile(File scriptFile)
    {
        mScriptFile = scriptFile;
    }

    /**
     * @return Returns the returnCode.
     */
    public int getReturnCode()
    {
        return mReturnCode;
    }

    /**
     * @param returnCode The returnCode to set.
     */
    void setReturnCode(int returnCode)
    {
        mReturnCode = returnCode;
    }

    /**
     * @return Returns the canceled.
     */
    public boolean isCanceled()
    {
        return mCanceled;
    }

    /**
     * @param canceled The canceled to set.
     */
    void setCanceled(boolean canceled)
    {
        mCanceled = canceled;
    }
}
