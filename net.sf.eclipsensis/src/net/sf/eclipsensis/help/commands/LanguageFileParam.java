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

import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.util.Common;

import org.w3c.dom.Node;

public class LanguageFileParam extends ComboParam
{
    public LanguageFileParam(Node node)
    {
        super(node);
    }

    @Override
    protected ComboEntry[] getComboEntries()
    {
        ComboEntry[] entries = EMPTY_COMBO_ENTRIES;
        List<NSISLanguage> languages = NSISLanguageManager.getInstance().getLanguages();
        if(!Common.isEmptyCollection(languages)) {
            entries = new ComboEntry[languages.size()];
            Collections.sort(languages, new Comparator<NSISLanguage>() {
                public int compare(NSISLanguage lang1, NSISLanguage lang2)
                {
                    return lang1.getName().compareTo(lang2.getName());
                }
            });
            int i=0;
            for (Iterator<NSISLanguage> iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage lang = iter.next();
                String file = lang.getName()+INSISConstants.LANGUAGE_FILES_EXTENSION;
                entries[i++] = new ComboEntry(file, file);
            }
        }
        return entries;
    }
}
