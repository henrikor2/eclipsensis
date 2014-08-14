/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.*;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.*;

public class InstallOptionsDropList extends InstallOptionsCombobox
{
    private static final long serialVersionUID = 1180170632304276812L;

    protected InstallOptionsDropList(INISection section)
    {
        super(section);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("maxLen"); //$NON-NLS-1$
    }

    @Override
    public String getType()
    {
        return InstallOptionsModel.TYPE_DROPLIST;
    }

    @Override
    protected boolean isStateReadOnly()
    {
        return true;
    }

    @Override
    public void setListItems(List<String> listItems)
    {
        super.setListItems(listItems);
        String oldState = getState();
        String newState = (!new CaseInsensitiveSet(getListItems()).contains(oldState)?"":oldState); //$NON-NLS-1$
        if(!Common.stringsAreEqual(oldState,newState)) {
            fireModelCommand(createSetPropertyCommand(InstallOptionsModel.PROPERTY_STATE,newState));
        }
    }
}

