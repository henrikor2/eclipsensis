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

import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;

import org.w3c.dom.Node;

public class HKEYParam extends ComboParam
{
    public HKEYParam(Node node)
    {
        super(node);
    }

    @Override
    protected ComboEntry[] getComboEntries()
    {
        ComboEntry[] entries = EMPTY_COMBO_ENTRIES;
        String[] hkeys = NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.HKEY_LONG_PARAMETERS);
        if(!Common.isEmptyArray(hkeys)) {
            entries = new ComboEntry[hkeys.length];
            for (int i = 0; i < hkeys.length; i++) {
                entries[i] = new ComboEntry(hkeys[i], hkeys[i]);
            }
        }
        return entries;
    }
}
