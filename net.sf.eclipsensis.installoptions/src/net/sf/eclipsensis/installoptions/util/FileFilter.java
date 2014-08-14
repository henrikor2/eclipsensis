/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.util;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.model.InstallOptionsFileRequest;
import net.sf.eclipsensis.util.Common;

public class FileFilter implements Cloneable
{
    private static final FilePattern[] EMPTY_PATTERN_ARRAY = new FilePattern[0];
    private String mDescription;
    private FilePattern[] mPatterns = EMPTY_PATTERN_ARRAY;

    public FileFilter()
    {
    }

    public FileFilter(String description, FilePattern[] patterns)
    {
        this();
        setDescription(description);
        setPatterns(patterns);
    }

    public FileFilter(FileFilter filter)
    {
        this();
        setDescription(filter.getDescription());
        FilePattern[] patterns = filter.getPatterns();
        FilePattern[] patterns2 = null;
        if(!Common.isEmptyArray(patterns)) {
            patterns2 = new FilePattern[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                patterns2[i] = (FilePattern)patterns[i].clone();
            }
        }
        setPatterns(patterns2);
    }

    @Override
    public Object clone()
    {
        return new FileFilter(this);
    }

    public String getDescription()
    {
        return mDescription;
    }

    public void setDescription(String description)
    {
        mDescription = description;
    }

    public FilePattern[] getPatterns()
    {
        return mPatterns;
    }

    public void setPatterns(FilePattern[] patterns)
    {
        mPatterns = (patterns==null?EMPTY_PATTERN_ARRAY:patterns);
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer(mDescription);
        buf.append(IInstallOptionsConstants.LIST_SEPARATOR);
        buf.append(getPatternString());
        return buf.toString();
    }

    public String getPatternString()
    {
        return Common.flatten(mPatterns,InstallOptionsFileRequest.FILTER_SEPARATOR);
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this != obj) {
            if (obj instanceof FileFilter) {
                return toString().equals(obj.toString());
            }
            return false;
        }
        return true;
    }
}