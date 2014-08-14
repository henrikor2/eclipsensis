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

import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public interface IInstallOptionsEditorInput extends IPathEditorInput, IStorageEditorInput
{
    public void prepareForSwitch();
    public void completedSwitch();
    public TextFileDocumentProvider getDocumentProvider();
    public Object getSource();
}
