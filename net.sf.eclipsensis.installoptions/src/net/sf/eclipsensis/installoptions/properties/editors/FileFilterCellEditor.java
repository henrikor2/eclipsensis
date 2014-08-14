/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.editors;

import net.sf.eclipsensis.installoptions.model.InstallOptionsFileRequest;
import net.sf.eclipsensis.installoptions.properties.dialogs.FileFilterDialog;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;

public class FileFilterCellEditor extends DialogCellEditor
{
    private InstallOptionsFileRequest mRequest;

    public FileFilterCellEditor(InstallOptionsFileRequest request, Composite parent)
    {
        super(parent);
        mRequest = request;
    }

    @Override
    protected void updateContents(Object value)
    {
        Label label = getDefaultLabel();
        if (label != null) {
            label.setText(InstallOptionsFileRequest.FILTER_LABEL_PROVIDER.getText(value));
        }
    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow)
    {
        FileFilterDialog dialog = new FileFilterDialog(cellEditorWindow.getShell(),mRequest.getFilter());
        dialog.setValidator(getValidator());
        int result = dialog.open();
        return (result == Window.OK?dialog.getFilter():mRequest.getFilter());
    }
}