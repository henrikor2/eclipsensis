/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import net.sf.eclipsensis.util.INodeConvertible;

public interface ITemplate extends INodeConvertible, Cloneable
{
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_CUSTOM = 1;
    public static final int TYPE_USER = 2;

    public String getDescription();
    public String getId();
    public String getName();
    public int getType();
    public boolean isAvailable();
    public boolean isEnabled();
    public boolean isDeleted();
    public void setDescription(String description);
    public void setId(String id);
    public void setName(String name);
    public void setType(int type);
    public void setEnabled(boolean enabled);
    public void setDeleted(boolean deleted);
    public Object clone();
    public boolean isEqualTo(ITemplate template);
}
