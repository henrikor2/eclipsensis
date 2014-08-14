/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.settings.NSISSettingsEditor;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PropertyPage;

public abstract class NSISSettingsPage extends PropertyPage implements IWorkbenchPreferencePage, INSISConstants
{
    protected NSISSettingsEditor mSettingsEditor;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected final Control createContents(Composite parent)
    {
        mSettingsEditor = createSettingsEditor();
        return mSettingsEditor.createControl(parent);
    }

    @Override
    public void createControl(Composite parent)
    {
        setDescription(getPageDescription());
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),getContextId());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    @Override
    public final boolean performOk()
    {
        if(super.performOk()) {
            return mSettingsEditor.performApply();
        }
        return false;
    }

    @Override
    protected final void performDefaults()
    {
        super.performDefaults();
        mSettingsEditor.performDefaults();
    }

    protected abstract String getPageDescription();
    protected abstract NSISSettingsEditor createSettingsEditor();
    protected abstract String getContextId();
}