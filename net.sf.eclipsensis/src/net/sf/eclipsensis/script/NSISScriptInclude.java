/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

public class NSISScriptInclude extends AbstractNSISScriptElement
{
    private String mFileName;

    /**
     * @param fileName
     */
    public NSISScriptInclude(String fileName)
    {
        super("!include",fileName); //$NON-NLS-1$
        mFileName = fileName;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName()
    {
        return mFileName;
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName)
    {
        mFileName = fileName;
        updateArgs(mFileName);
    }
}
