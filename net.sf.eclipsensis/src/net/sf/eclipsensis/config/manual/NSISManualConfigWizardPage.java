/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.config.manual;

import java.io.File;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class NSISManualConfigWizardPage extends WizardPage
{
    public static final String ID = "nsisManualConfigWizardPage"; //$NON-NLS-1$
    private static ImageDescriptor cImage = EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("manual.config.wizard.title.image")); //$NON-NLS-1$

    private Text mNSISHomeText;

    public NSISManualConfigWizardPage()
    {
        super(ID, EclipseNSISPlugin.getResourceString("manual.config.wizard.name"), cImage); //$NON-NLS-1$
        setDescription(EclipseNSISPlugin.getResourceString("manual.config.wizard.description")); //$NON-NLS-1$
    }

    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(3,false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.LEFT);
        label.setText(EclipseNSISPlugin.getResourceString("nsis.home.text")); //$NON-NLS-1$
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        mNSISHomeText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        mNSISHomeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mNSISHomeText.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                validate();
            }
        });

        Button button = new Button(composite, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        button.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Shell shell = getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setMessage(EclipseNSISPlugin.getResourceString("nsis.home.message")); //$NON-NLS-1$
                String text = mNSISHomeText.getText();
                dialog.setFilterPath(text);
                String nsisHome = dialog.open();
                if (!Common.isEmpty(nsisHome)) {
                    mNSISHomeText.setText(nsisHome);
                }
            }
        });
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_manualconfig_wizard_context"); //$NON-NLS-1$
        setControl(composite);
        validate();
    }

    @Override
    public void setErrorMessage(String message)
    {
        super.setMessage(message,ERROR);
    }

    private void validate()
    {
        String nsisHome = mNSISHomeText.getText();
        if(Common.isEmpty(nsisHome)) {
            setPageComplete(false);
            setErrorMessage(EclipseNSISPlugin.getResourceString("manual.config.wizard.empty.folder.error")); //$NON-NLS-1$
        }
        else {
            File folder = new File(nsisHome);
            if(IOUtility.isValidDirectory(folder)) {
                if(IOUtility.isValidFile(new File(folder,INSISConstants.MAKENSIS_EXE))) {
                    setPageComplete(true);
                    setErrorMessage(null);
                }
                else {
                    setPageComplete(false);
                    setErrorMessage(EclipseNSISPlugin.getResourceString("manual.config.wizard.no.makensis.error")); //$NON-NLS-1$
                }
            }
            else {
                setPageComplete(false);
                setErrorMessage(EclipseNSISPlugin.getResourceString("manual.config.wizard.invalid.folder.error")); //$NON-NLS-1$
            }
        }
    }

    public boolean performFinish()
    {
        final String nsisHome = mNSISHomeText.getText();
        if(NSISValidator.findNSISExe(new File(nsisHome)) != null) {
            NSISPreferences.getInstance().setNSISHome(nsisHome);
            NSISPreferences.getInstance().store();
            return true;
        }
        setPageComplete(false);
        setErrorMessage(EclipseNSISPlugin.getResourceString("invalid.nsis.home.message")); //$NON-NLS-1$
        return false;
    }
}
