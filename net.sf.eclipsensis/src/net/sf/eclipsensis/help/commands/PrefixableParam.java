/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.util.*;

import org.w3c.dom.*;

public abstract class PrefixableParam extends NSISParam
{
    public static final String ATTR_ALLOW_BLANK = "allowBlank"; //$NON-NLS-1$
    public static final String ATTR_PREFIX = "prefix"; //$NON-NLS-1$
    private String mPrefix;
    protected boolean mAllowBlank;

    public PrefixableParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        super.init(node);
        NamedNodeMap attributes = node.getAttributes();
        String prefix = XMLUtil.getStringValue(attributes, ATTR_PREFIX);
        if (!Common.isEmpty(prefix))
        {
            setPrefix(prefix);
        }
        mAllowBlank = XMLUtil.getBooleanValue(attributes, ATTR_ALLOW_BLANK, false);
    }

    public boolean isAllowBlank()
    {
        return mAllowBlank;
    }

    public String getPrefix()
    {
        return mPrefix;
    }

    public void setPrefix(String prefix)
    {
        mPrefix = prefix;
    }

    @Override
    protected final NSISParamEditor createParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return createPrefixableParamEditor(command, parentEditor);
    }

    @Override
    protected final String getDefaultValue()
    {
        if (mAllowBlank)
        {
            return ""; //$NON-NLS-1$
        }
        return getDefaultValue2();
    }

    protected String getDefaultValue2()
    {
        return ""; //$NON-NLS-1$
    }

    protected abstract PrefixableParamEditor createPrefixableParamEditor(NSISCommand command,
            INSISParamEditor parentEditor);

    protected abstract class PrefixableParamEditor extends NSISParamEditor
    {
        public static final String DATA_BUTTON = "BUTTON"; //$NON-NLS-1$

        public PrefixableParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        @Override
        protected final void appendParamText(StringBuffer buf, boolean preview)
        {
            String text = getPrefixableParamText();
            if (text != null)
            {
                boolean shouldQuote = shouldQuote(text);
                String prefix = getPrefix();
                if (!Common.isEmpty(prefix))
                {
                    if (Character.isWhitespace(prefix.charAt(prefix.length() - 1)))
                    {
                        if (shouldQuote)
                        {
                            text = Common.quote(text);
                            shouldQuote = false;
                        }
                    }
                    text = prefix + text;
                }
                buf.append(" ").append(shouldQuote ? Common.quote(text) : text); //$NON-NLS-1$
            }
        }

        @Override
        protected boolean shouldDecorate()
        {
            return true;
        }

        @Override
        protected String validateParam()
        {
            return null;
        }

        protected abstract String getPrefixableParamText();
    }
}
