/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.template.AbstractTemplateDialog;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class NSISWizardTemplateDialog extends AbstractTemplateDialog<NSISWizardTemplate>
{
    private NSISWizardSettings mSettings = null;

    /**
     * @param parentShell
     */
    public NSISWizardTemplateDialog(Shell parentShell, NSISWizardTemplateManager templateManager, NSISWizardTemplate template, NSISWizardSettings settings)
    {
        super(parentShell, templateManager, template, true);
        mSettings = settings;
    }

    @Override
    protected NSISWizardTemplate createTemplate(String name)
    {
        return new NSISWizardTemplate(name);
    }

    @Override
    protected void createUpdateTemplate()
    {
        super.createUpdateTemplate();
        NSISWizardTemplate template = getTemplate();
        template.setSettings(mSettings);
    }

    @Override
    protected Image getShellImage()
    {
        return EclipseNSISPlugin.getShellImage();
    }

    @Override
    protected String getShellTitle()
    {
        return EclipseNSISPlugin.getResourceString("wizard.template.dialog.title"); //$NON-NLS-1$
    }
}
