/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dnd;

import net.sf.eclipsensis.util.ObjectTransfer;


public class InstallOptionsTemplateTransfer extends ObjectTransfer
{
    public static final InstallOptionsTemplateTransfer INSTANCE = new InstallOptionsTemplateTransfer();
    private static final String[] TYPE_NAMES = {new StringBuffer("InstallOptions Template transfer").append( //$NON-NLS-1$
                                                             System.currentTimeMillis()).append(
                                                             ":").append(INSTANCE.hashCode()).toString()};//$NON-NLS-1$
    private static final int[] TYPEIDS = {registerType(TYPE_NAMES[0])};

    private InstallOptionsTemplateTransfer()
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

    public void setTemplate(Object template)
    {
        setObject(template);
    }

    public Object getTemplate()
    {
        return getObject();
    }
}
