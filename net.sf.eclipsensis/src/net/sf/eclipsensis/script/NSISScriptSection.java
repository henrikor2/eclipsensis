/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

public class NSISScriptSection extends AbstractNSISScriptElementContainer
{
    private String mSectionName = null;
    private boolean mBold = false;
    private boolean mHidden = false;
    private boolean mDefaultUnselected = false;
    private String mIndex = null;

    /**
     * @param name
     */
    public NSISScriptSection(String name, boolean bold, boolean hidden, boolean defaultUnselected)
    {
        this(name, bold, hidden, defaultUnselected, null);
    }

    /**
     * @param name
     */
    public NSISScriptSection(String name, boolean bold, boolean hidden, boolean defaultUnselected, String index)
    {
        super("Section", makeArray(name, bold, hidden, defaultUnselected, index)); //$NON-NLS-1$
        mSectionName = name;
        mBold = bold;
        mHidden = Common.isEmpty(name)||hidden;
        mDefaultUnselected = defaultUnselected;
        mIndex = index;
    }

    private void updateArgs()
    {
        updateArgs(makeArray(mSectionName, mBold, mHidden, mDefaultUnselected, mIndex));
    }

    /**
     *
     */
    private static String[] makeArray(String name, boolean bold, boolean hidden, boolean defaultUnselected, String index)
    {
        String[] args = null;

        if(name.equalsIgnoreCase(INSISConstants.UNINSTALL_SECTION_NAME)) {
            args = new String[] {INSISConstants.UNINSTALL_SECTION_NAME};
        }
        else {
            int size = 1 + (defaultUnselected?1:0) + (index!=null?1:0);
            args = new String[size];
            int n=0;
            if(defaultUnselected) {
                args[n++] = getKeyword("/o"); //$NON-NLS-1$
            }
            args[n++] = (Common.isEmpty(name)?"":(hidden?"-":(bold?"!":""))+name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if(index != null) {
                args[n] = index;
            }
        }
        return args;
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
        mBold = bold;
        updateArgs();
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
        mDefaultUnselected = defaultUnselected;
        updateArgs();
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
        mHidden = hidden;
        updateArgs();
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return mSectionName;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        mSectionName = name;
        updateArgs();
    }

    /**
     * @return Returns the index.
     */
    public String getIndex()
    {
        return mIndex;
    }

    /**
     * @param index The index to set.
     */
    public void setIndex(String index)
    {
        mIndex = index;
        updateArgs();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.INSISScriptElement#write(net.sf.eclipsensis.script.NSISScriptWriter)
     */
    @Override
    public void write(NSISScriptWriter writer)
    {
        super.write(writer);
        writer.indent();
        writeElements(writer);
        writer.unindent();
        writer.println(getKeyword("SectionEnd")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.AbstractNSISScriptElementContainer#validateElement(net.sf.eclipsensis.script.INSISScriptElement)
     */
    @Override
    protected void validateElement(INSISScriptElement element)
            throws InvalidNSISScriptElementException
    {
        if(element != null) {
            if(element instanceof NSISScriptInstruction || element instanceof NSISScriptLabel) {
                return;
            }
        }
        super.validateElement(element);
    }
}
