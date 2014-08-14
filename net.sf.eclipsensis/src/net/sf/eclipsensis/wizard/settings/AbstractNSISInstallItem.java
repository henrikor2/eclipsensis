/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;


public abstract class AbstractNSISInstallItem extends AbstractNSISInstallElement
{
    private static final long serialVersionUID = 7704420240201206452L;

    public final boolean acceptsChildType(String type)
    {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#addChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public final boolean addChild(INSISInstallElement child)
    {
        return false;
    }

    public final boolean addChild(int index, INSISInstallElement child)
    {
        return false;
    }

    public final boolean canAddChild(INSISInstallElement child)
    {
        return false;
    }

    public final int getChildCount()
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildren()
     */
    public final INSISInstallElement[] getChildren()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildTypes()
     */
    public final String[] getChildTypes()
    {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#hasChildren()
     */
    public final boolean hasChildren()
    {
        return false;
    }

    public final int indexOf(INSISInstallElement child)
    {
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeAllChildren()
     */
    public final boolean removeAllChildren()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public final boolean removeChild(INSISInstallElement child)
    {
        return false;
    }

    public final boolean removeChild(int index)
    {
        return false;
    }

    public final void setChildren(INSISInstallElement[] children)
    {
    }
}
