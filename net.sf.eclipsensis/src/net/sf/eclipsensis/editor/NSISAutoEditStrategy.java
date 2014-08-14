/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public abstract class NSISAutoEditStrategy implements IAutoEditStrategy
{
    protected boolean mUseSpacesForTabs;
    protected int mTabWidth;
    protected IPreferenceStore mPreferenceStore;

    public NSISAutoEditStrategy(IPreferenceStore preferenceStore)
    {
        super();
        mPreferenceStore = preferenceStore;
        updateFromPreferences();
    }

    public void updateFromPreferences()
    {
        mUseSpacesForTabs = mPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
        mTabWidth = mPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    }

}
