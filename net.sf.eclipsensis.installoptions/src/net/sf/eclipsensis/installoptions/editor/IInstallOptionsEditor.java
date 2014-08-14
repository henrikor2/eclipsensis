/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.ini.INIFile;

import org.eclipse.ui.IEditorPart;

public interface IInstallOptionsEditor extends IEditorPart, IInstallOptionsConstants
{
    public boolean canSwitch();
    public void prepareForSwitch();
    public INIFile getINIFile();
}
