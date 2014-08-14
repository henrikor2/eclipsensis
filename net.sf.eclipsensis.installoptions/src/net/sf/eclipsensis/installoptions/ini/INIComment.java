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

public class INIComment extends INILine
{
    private static final long serialVersionUID = 1300997609730805814L;

    public INIComment(String text, String delimiter)
    {
        super(text, delimiter);
    }

    public INIComment(String text)
    {
        super(text);
    }

    @Override
    protected void checkProblems(int fixFlag)
    {
    }
}
