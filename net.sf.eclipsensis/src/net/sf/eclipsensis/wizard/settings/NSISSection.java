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

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISSectionDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.*;

public class NSISSection extends AbstractNSISInstallGroup
{
    private static final long serialVersionUID = -971949137266423189L;

    public static final String TYPE = "Section"; //$NON-NLS-1$

    private static final Image IMAGE = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("wizard.section.icon")); //$NON-NLS-1$
    private static final String FORMAT = EclipseNSISPlugin.getResourceString("wizard.section.format"); //$NON-NLS-1$

    private String mDescription = null;
    private String mName = null;
    private boolean mBold = false;
    private boolean mHidden = false;
    private boolean mDefaultUnselected = false;

    static {
        NSISInstallElementFactory.register(TYPE, NSISKeywords.getInstance().getKeyword(TYPE), IMAGE, NSISSection.class);
        NSISKeywords.getInstance().addKeywordsListener(new INSISKeywordsListener() {
            public void keywordsChanged()
            {
                NSISInstallElementFactory.setTypeName(TYPE, NSISKeywords.getInstance().getKeyword(TYPE));
            }
        });
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.AbstractNSISInstallGroup#resetChildTypes()
     */
    @Override
    public void setChildTypes()
    {
        clearChildTypes();
        addChildType(NSISInstallFile.TYPE);
        addChildType(NSISInstallFiles.TYPE);
        addChildType(NSISInstallDirectory.TYPE);
        addChildType(NSISInstallShortcut.TYPE);
        addChildType(NSISInstallRegistryKey.TYPE);
        addChildType(NSISInstallRegistryValue.TYPE);
        addChildType(NSISInstallLibrary.TYPE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getType()
     */
    public String getType()
    {
        return TYPE;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getDisplayName()
     */
    public String getDisplayName()
    {
        return MessageFormat.format(FORMAT,new Object[]{mName,NSISKeywords.getInstance().getKeyword(TYPE)}).trim();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#isEditable()
     */
    public boolean isEditable()
    {
        return true;
    }

    public boolean edit(NSISWizard wizard)
    {
        return new NSISSectionDialog(wizard,this).open() == Window.OK;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getImage()
     */
    public Image getImage()
    {
        return IMAGE;
    }

    /**
     * @return Returns the bold.
     */
    public boolean isBold()
    {
        return mBold;
    }
    /**
     * @param bold The bold to set.
     */
    public void setBold(boolean bold)
    {
        if(mBold != bold) {
            setDirty();
            mBold = bold;
        }
    }
    /**
     * @return Returns the defaultUnselected.
     */
    public boolean isDefaultUnselected()
    {
        return mDefaultUnselected;
    }
    /**
     * @param defaultUnselected The defaultUnselected to set.
     */
    public void setDefaultUnselected(boolean defaultUnselected)
    {
        if(mDefaultUnselected != defaultUnselected) {
            setDirty();
            mDefaultUnselected = defaultUnselected;
        }
    }
    /**
     * @return Returns the hidden.
     */
    public boolean isHidden()
    {
        return mHidden;
    }
    /**
     * @param hidden The hidden to set.
     */
    public void setHidden(boolean hidden)
    {
        if(mHidden != hidden) {
            setDirty();
            mHidden = hidden;
        }
    }
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mName;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        if(!Common.stringsAreEqual(mName,name)) {
            setDirty();
            mName = name;
        }
    }

    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return mDescription;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description)
    {
        if(!Common.stringsAreEqual(mDescription,description)) {
            setDirty();
            mDescription = description;
        }
    }

    @Override
    protected Object getNodeValue(Node node, String name, Class<?> clasz)
    {
        if(name.equals("description")) { //$NON-NLS-1$
            return XMLUtil.readTextNode(node);
        }
        else {
            return super.getNodeValue(node, name, clasz);
        }
    }

    @Override
    protected Node createChildNode(Document document, String name, Object value)
    {
        Object value2 = value;
        if(name.equals("description")) { //$NON-NLS-1$
            value2 = document.createTextNode((String)value2);
        }
        return super.createChildNode(document, name, value2);
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + (mBold?1231:1237);
        result = PRIME * result + (mDefaultUnselected?1231:1237);
        result = PRIME * result + ((mDescription == null)?0:mDescription.hashCode());
        result = PRIME * result + (mHidden?1231:1237);
        result = PRIME * result + ((mName == null)?0:mName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NSISSection other = (NSISSection)obj;
        if (mBold != other.mBold) {
            return false;
        }
        if (mDefaultUnselected != other.mDefaultUnselected) {
            return false;
        }
        if (mDescription == null) {
            if (other.mDescription != null) {
                return false;
            }
        }
        else if (!mDescription.equals(other.mDescription)) {
            return false;
        }
        if (mHidden != other.mHidden) {
            return false;
        }
        if (mName == null) {
            if (other.mName != null) {
                return false;
            }
        }
        else if (!mName.equals(other.mName)) {
            return false;
        }
        return true;
    }
}
