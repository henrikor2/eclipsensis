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

import java.util.Collection;

import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.INSISWizardConstants;

import org.w3c.dom.*;

public abstract class AbstractNSISInstallElement extends AbstractNodeConvertible implements INSISInstallElement
{
    private static final long serialVersionUID = 742172003526190746L;

    private NSISWizardSettings mSettings = null;
    private INSISInstallElement mParent = null;
    private transient String mError = null;
    private transient boolean mDirty = true;

    protected int mTargetPlatform = INSISWizardConstants.TARGET_PLATFORM_ANY;

    protected final void setDirty()
    {
        mDirty = true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        AbstractNSISInstallElement element = (AbstractNSISInstallElement)super.clone();
        element.mSettings = null;
        element.mParent = null;
        return element;
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("removable"); //$NON-NLS-1$
        skippedProperties.add("editable"); //$NON-NLS-1$
        skippedProperties.add("settings"); //$NON-NLS-1$
        skippedProperties.add("parent"); //$NON-NLS-1$
        skippedProperties.add("childTypes"); //$NON-NLS-1$
        skippedProperties.add("displayName"); //$NON-NLS-1$
        skippedProperties.add("image"); //$NON-NLS-1$
        skippedProperties.add("type"); //$NON-NLS-1$
    }

    public boolean isRemovable()
    {
        return true;
    }

    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
    }

    public NSISWizardSettings getSettings()
    {
        return mSettings;
    }

    public void setParent(INSISInstallElement parent)
    {
        mParent = parent;
    }

    public INSISInstallElement getParent()
    {
        return mParent;
    }

    @Override
    public void fromNode(Node node)
    {
        String nodeType = node.getAttributes().getNamedItem(TYPE_ATTRIBUTE).getNodeValue();
        if(nodeType.equals(getType()) || nodeType.equals(NSISInstallElementFactory.getAlias(getType()))) {
            super.fromNode(node);
        }
    }

    @Override
    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        XMLUtil.addAttribute(document,node,TYPE_ATTRIBUTE,getType());
        return node;
    }

    @Override
    protected String getChildNodeName()
    {
        return CHILD_NODE;
    }

    public final String getNodeName()
    {
        return NODE;
    }

    public String getError()
    {
        return mError;
    }

    public String validate(Collection<INSISInstallElement> changedElements)
    {
        if(mDirty) {
            String error = doValidate();
            if(!Common.stringsAreEqual(mError,error)) {
                changedElements.add(this);
            }
            mError = error;
            mDirty = false;
        }
        return mError;
    }

    protected String doValidate()
    {
        return null;
    }

    public void setTargetPlatform(int targetPlatform)
    {
        mTargetPlatform = targetPlatform;
    }

    public int getTargetPlatform()
    {
        return mTargetPlatform;
    }
}
