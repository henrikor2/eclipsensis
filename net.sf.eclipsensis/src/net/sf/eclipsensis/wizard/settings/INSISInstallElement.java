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

import java.io.Serializable;
import java.util.Collection;

import net.sf.eclipsensis.util.INodeConvertible;
import net.sf.eclipsensis.wizard.*;

import org.eclipse.swt.graphics.Image;

public interface INSISInstallElement extends INSISWizardConstants, Serializable, INodeConvertible, Cloneable
{
    public static final String NODE = "element"; //$NON-NLS-1$
    public static final String CHILD_NODE = "attribute"; //$NON-NLS-1$
    public static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

    public String getType();
    public String getDisplayName();
    public boolean isEditable();
    public boolean isRemovable();
    public boolean edit(NSISWizard wizard);
    public boolean hasChildren();
    public INSISInstallElement[] getChildren();
    public int getChildCount();
    public void setChildren(INSISInstallElement[] children);
    public INSISInstallElement getParent();
    void setParent(INSISInstallElement parent);
    public String[] getChildTypes();
    public boolean acceptsChildType(String type);
    public boolean canAddChild(INSISInstallElement child);
    public boolean addChild(INSISInstallElement child);
    public boolean addChild(int index, INSISInstallElement child);
    public boolean removeChild(INSISInstallElement child);
    public boolean removeChild(int index);
    public int indexOf(INSISInstallElement child);
    public boolean removeAllChildren();
    public Image getImage();
    public void setSettings(NSISWizardSettings settings);
    public NSISWizardSettings getSettings();
    public Object clone() throws CloneNotSupportedException;
    public String validate(Collection<INSISInstallElement> changedElements);
    public String getError();
    public void setTargetPlatform(int targetPlatform);
}
