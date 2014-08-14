/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.Arrays;
import java.util.regex.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

import org.w3c.dom.Node;

public class VarParam extends ComboParam
{
    private Pattern mVarPattern;

    public VarParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        mVarPattern = Pattern.compile("\\$[0-9a-z_]+",Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
        super.init(node);
    }

    @Override
    protected ComboEntry[] getComboEntries()
    {
        ComboEntry[] entries = EMPTY_COMBO_ENTRIES;
        String[] vars = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.REGISTERS);
        if(!Common.isEmptyArray(vars)) {
            entries = new ComboEntry[vars.length];
            Arrays.sort(vars);
            for (int i = 0; i < vars.length; i++) {
                entries[i] = new ComboEntry(vars[i], vars[i]);
            }
        }

        return entries;
    }

    @Override
    protected boolean isUserEditable()
    {
        return true;
    }

    @Override
    protected String validateUserValue(String value)
    {
        if(value != null) {
            Matcher m = mVarPattern.matcher(value);
            if(m.matches()) {
                return null;
            }
        }
        return EclipseNSISPlugin.getResourceString("var.param.error");  //$NON-NLS-1$
    }
}
