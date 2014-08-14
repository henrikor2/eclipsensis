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

public class FilePattern implements Cloneable
{
    private String mPattern;

    public FilePattern(String pattern)
    {
        mPattern = pattern;
    }

    @Override
    public String toString()
    {
        return getPattern();
    }

    public String getPattern()
    {
        return mPattern;
    }

    public void setPattern(String pattern)
    {
        mPattern = pattern;
    }

    @Override
    public Object clone()
    {
        return new FilePattern(mPattern);
    }
}