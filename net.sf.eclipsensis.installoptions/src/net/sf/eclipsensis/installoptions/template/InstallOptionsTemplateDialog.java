/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.template.AbstractTemplateDialog;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class InstallOptionsTemplateDialog extends AbstractTemplateDialog<IInstallOptionsTemplate>
{
    private InstallOptionsWidget[] mWidgets;

    public InstallOptionsTemplateDialog(Shell parentShell, IInstallOptionsTemplate template)
    {
        this(parentShell, template, null);
    }

    public InstallOptionsTemplateDialog(Shell parentShell, InstallOptionsWidget[] widgets)
    {
        this(parentShell, null, widgets);
    }

    private InstallOptionsTemplateDialog(Shell parentShell, IInstallOptionsTemplate template, InstallOptionsWidget[] widgets)
    {
        super(parentShell, InstallOptionsTemplateManager.INSTANCE, template, template == null);
        mWidgets = widgets;
    }

    @Override
    protected IInstallOptionsTemplate createTemplate(String name)
    {
        return new InstallOptionsTemplate2(name);
    }

    @Override
    protected void createUpdateTemplate()
    {
        super.createUpdateTemplate();
        if(isCreate()) {
            (getTemplate()).setWidgets(mWidgets);
        }
    }

    @Override
    protected Image getShellImage()
    {
        return InstallOptionsPlugin.getShellImage();
    }

    @Override
    protected String getShellTitle()
    {
        return InstallOptionsPlugin.getResourceString((isCreate()?"create.template.dialog.title":"edit.template.dialog.title")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
