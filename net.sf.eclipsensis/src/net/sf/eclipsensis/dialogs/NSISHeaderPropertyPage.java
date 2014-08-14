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

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class NSISHeaderPropertyPage extends PropertyPage
{
    private static final IFilter IFILE_FILTER = new IFilter() {
        public boolean select(Object toTest)
        {
            if(toTest instanceof IFile) {
                String ext = ((IFile)toTest).getFileExtension();
                return (ext != null && ext.equalsIgnoreCase(INSISConstants.NSI_EXTENSION));
            }
            return false;
        }
    };

    private NSISHeaderAssociationManager mHeaderAssociationManager = NSISHeaderAssociationManager.getInstance();
    private Label mAssociatedScriptLabel = null;
    private Text mNSISScriptName = null;
    private Button mBrowseButton = null;

    public NSISHeaderPropertyPage()
    {
    }

    @Override
    protected Control createContents(Composite parent)
    {
        setDescription(EclipseNSISPlugin.getResourceString("header.properties.description")); //$NON-NLS-1$

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        composite.setLayout(layout);

        mAssociatedScriptLabel = new Label(composite,SWT.NONE);
        mAssociatedScriptLabel.setText(EclipseNSISPlugin.getResourceString("associated.script.label")); //$NON-NLS-1$
        mAssociatedScriptLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));

        mNSISScriptName = new Text(composite, SWT.BORDER);
        IFile file = mHeaderAssociationManager.getAssociatedScript((IFile)getElement());
        mNSISScriptName.setText(file==null?"":file.getFullPath().toString()); //$NON-NLS-1$
        mNSISScriptName.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        mBrowseButton = new Button(composite,SWT.PUSH);
        mBrowseButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        mBrowseButton.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        mBrowseButton.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        mBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                String scriptName = mNSISScriptName.getText();
                IFile file = null;
                try {
                    file = (!Common.isEmpty(scriptName)?ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(scriptName)):null);
                }
                catch (Exception e1) {
                    EclipseNSISPlugin.getDefault().log(e1);
                    file = null;
                }
                while (true) {
                    FileSelectionDialog dialog = new FileSelectionDialog(getShell(), (file==null?((IResource)getElement()).getParent():null),
                                                                         IFILE_FILTER);
                    dialog.setDialogMessage(EclipseNSISPlugin.getResourceString("nsis.script.prompt")); //$NON-NLS-1$
                    dialog.setHelpAvailable(false);
                    if (dialog.open() == Window.OK) {
                        if(!validateScript(dialog.getFile())) {
                            continue;
                        }
                        mNSISScriptName.setText(dialog.getFile().getFullPath().toString());
                    }
                    break;
                }
            }
        });

        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_hdrproperties_context"); //$NON-NLS-1$
        return composite;
    }

    @Override
    protected void performDefaults()
    {
        super.performDefaults();
        mNSISScriptName.setText(""); //$NON-NLS-1$
    }

    private boolean validateScript(IFile file)
    {
        if (file != null) {
            if (Common.stringsAreEqual(INSISConstants.NSI_EXTENSION, file.getFileExtension(), true)) {
                return true;
            }
        }
        Common.openError(getShell(), EclipseNSISPlugin.getResourceString("not.valid.script.error"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
        return false;
    }

    @Override
    public boolean performOk()
    {
        if(super.performOk()) {
            String nsisScriptName = mNSISScriptName.getText();
            IFile header = (IFile)getElement();
            if(Common.isEmpty(nsisScriptName)) {
                mHeaderAssociationManager.disassociateFromScript(header);
            }
            else {
                IPath path = new Path(nsisScriptName);
                if(!path.isAbsolute()) {
                    Common.openError(getShell(), EclipseNSISPlugin.getResourceString("not.valid.script.error"), EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                    return false;
                }
                IFile associatedScript = null;
                if(validateScript(associatedScript = ResourcesPlugin.getWorkspace().getRoot().getFile(path))) {
                    mHeaderAssociationManager.associateWithScript(header, associatedScript);
                    return true;
                }
            }
        }
        return false;
    }
}
