/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.util.EventListener;

import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

public interface INSISWizardSettingsListener extends EventListener
{
    public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings);
}
