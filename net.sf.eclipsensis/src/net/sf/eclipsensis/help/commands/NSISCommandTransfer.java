/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.util.ObjectTransfer;

public class NSISCommandTransfer extends ObjectTransfer
{
    private static final String[] TYPE_NAMES = {"nsis-command" + System.currentTimeMillis()}; //$NON-NLS-1$
    private static final int[] TYPEIDS = {registerType(TYPE_NAMES[0])};

    public static final NSISCommandTransfer INSTANCE = new NSISCommandTransfer();

    private NSISCommandTransfer()
    {
    }

    @Override
    protected int[] getTypeIds()
    {
        return TYPEIDS;
    }

    @Override
    protected String[] getTypeNames()
    {
        return TYPE_NAMES;
    }
}
