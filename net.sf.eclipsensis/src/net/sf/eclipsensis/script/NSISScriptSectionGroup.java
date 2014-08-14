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

public class NSISScriptSectionGroup extends AbstractNSISScriptElementContainer
{
    private String mCaption = null;
    private boolean mExpanded = false;
    private boolean mBold = false;
    private String mIndex = null;

    public NSISScriptSectionGroup(String caption, boolean expanded, boolean bold)
    {
        this(caption, expanded, bold, null);
    }

    public NSISScriptSectionGroup(String caption, boolean expanded, boolean bold, String index)
    {
        super("SubSection",makeArray(caption, expanded, bold, index)); //$NON-NLS-1$
        mCaption = caption;
        mExpanded = expanded;
        mBold = bold;
        mIndex = index;
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
     * @return Returns the caption.
     */
    public String getCaption()
    {
        return mCaption;
    }

    /**
     * @param caption The caption to set.
     */
    public void setCaption(String caption)
    {
        mCaption = caption;
        updateArgs();
    }

    /**
     * @return Returns the expanded.
     */
    public boolean isExpanded()
    {
        return mExpanded;
    }

    /**
     * @param expanded The expanded to set.
     */
    public void setExpanded(boolean expanded)
    {
        mExpanded = expanded;
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

    private void updateArgs()
    {
        updateArgs(makeArray(mCaption, mExpanded, mBold, mIndex));
    }

    private static String[] makeArray(String caption, boolean expanded, boolean bold, String index)
    {
        String[] args = new String[(expanded?2:1) + (Common.isEmpty(index)?0:1)];
        int n = 0;
        if(expanded) {
            args[n++] = getKeyword("/e"); //$NON-NLS-1$
        }
        args[n++] = (bold?"!":"")+caption; //$NON-NLS-1$ //$NON-NLS-2$
        if(!Common.isEmpty(index)) {
            args[n] = index;
        }
        return args;
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
        writer.println(getKeyword("SubSectionEnd")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.script.AbstractNSISScriptElementContainer#validateElement(net.sf.eclipsensis.script.INSISScriptElement)
     */
    @Override
    protected void validateElement(INSISScriptElement element)
            throws InvalidNSISScriptElementException
    {
        if(element != null) {
            if(element instanceof NSISScriptSectionGroup || (element instanceof NSISScriptSection && !((NSISScriptSection)element).getName().equalsIgnoreCase(INSISConstants.UNINSTALL_SECTION_NAME))) {
                return;
            }
        }
        super.validateElement(element);
    }
}
