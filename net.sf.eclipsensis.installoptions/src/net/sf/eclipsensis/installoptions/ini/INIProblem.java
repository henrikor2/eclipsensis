/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.IDocument;

public class INIProblem
{
    public static final String TYPE_WARNING=IInstallOptionsConstants.INSTALLOPTIONS_WARNING_ANNOTATION_NAME;
    public static final String TYPE_ERROR=IInstallOptionsConstants.INSTALLOPTIONS_ERROR_ANNOTATION_NAME;

    private int mLine = 0;
    private String mMessage;
    private String mType;
    private INIProblemFixer mFixer = null;

    public INIProblem(String type, String message)
    {
        super();
        mMessage = message;
        mType = type;
    }

    public String getFixDescription()
    {
        return (mFixer != null?mFixer.getFixDescription():null);
    }

    public void setFixer(INIProblemFixer fixer)
    {
        mFixer = fixer;
    }

    public void fix(IDocument document)
    {
        if(mFixer != null) {
            mFixer.fix(document);
        }
    }

    public boolean canFix()
    {
        return mFixer != null;
    }

    void setLine(int line)
    {
        mLine = line;
    }

    public int getLine()
    {
        return mLine;
    }

    public String getType()
    {
        return mType;
    }

    public String getMessage()
    {
        return mMessage;
    }

    @Override
    public int hashCode()
    {
        return (mMessage==null?0:mMessage.hashCode())+(mType==null?0:mType.hashCode());
    }

    @Override
    public boolean equals(Object o)
    {
        if(o != this) {
            if(o instanceof INIProblem) {
                INIProblem p = (INIProblem)o;
                return Common.stringsAreEqual(getType(),p.getType()) &&
                       Common.stringsAreEqual(getMessage(),p.getMessage());
            }
            return false;
        }
        return true;
    }
}
