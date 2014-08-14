/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;

import org.w3c.dom.*;

public class NSISCommand
{
    public static final String ATTR_NAME = "name"; //$NON-NLS-1$
    public static final String ATTR_CATEGORY = "category"; //$NON-NLS-1$
    public static final String ATTR_TERMINATOR = "terminator"; //$NON-NLS-1$

    private String mName;
    private GroupParam mParam;
    private String mCategory;
    private String mTerminator;

    public NSISCommand(Node node)
    {
        NamedNodeMap attributes = node.getAttributes();
        mName = XMLUtil.getStringValue(attributes, ATTR_NAME);
        XMLUtil.removeValue(attributes, ATTR_NAME);
        mParam = loadParam(node);
        mCategory = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(attributes, ATTR_CATEGORY));
        mTerminator = XMLUtil.getStringValue(attributes, ATTR_TERMINATOR);
    }

    public String getCategory()
    {
        return mCategory;
    }

    /**
     * @param node
     * @return
     */
    private GroupParam loadParam(Node node)
    {
        GroupParam groupParam = new GroupParam(node);
        NSISParam[] mChildParams = groupParam.getChildParams();
        if (mChildParams != null && mChildParams.length == 1 && mChildParams[0] instanceof GroupParam)
        {
            GroupParam param = (GroupParam) mChildParams[0];
            if (groupParam.isOptional() == param.isOptional()
                    && Common.stringsAreEqual(groupParam.getName(), param.getName()))
            {
                groupParam = param;
            }
        }
        return groupParam;
    }

    public String getName()
    {
        return mName;
    }

    public INSISParamEditor createEditor()
    {
        return mParam.createEditor(this, null);
    }

    public NSISCommandResult getResult()
    {
        return getResult(null);
    }

    NSISCommandResult getResult(INSISParamEditor editor)
    {
        return getResult(editor, false);
    }

    NSISCommandResult getResultPreview(INSISParamEditor editor)
    {
        return getResult(editor, true);
    }

    private NSISCommandResult getResult(INSISParamEditor editor, boolean preview)
    {
        StringBuffer buf = new StringBuffer(getName());
        if (editor != null && this.equals(editor.getCommand()))
        {
            editor.appendText(buf, preview);
        }
        buf.append(INSISConstants.LINE_SEPARATOR);
        int length = buf.length();
        if (!Common.isEmpty(mTerminator))
        {
            buf.append(INSISConstants.LINE_SEPARATOR);
            buf.append(mTerminator);
            buf.append(INSISConstants.LINE_SEPARATOR);
        }
        return new NSISCommandResult(buf.toString(), length);
    }

    public boolean hasParameters()
    {
        if (mParam != null)
        {
            return !Common.isEmptyArray(mParam.getChildParams());
        }
        return false;
    }
}
