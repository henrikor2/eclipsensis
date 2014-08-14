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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.settings.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISPropertyPage extends NSISSettingsPage
{
    /**
     * @return
     */
    @Override
    protected String getContextId()
    {
        return PLUGIN_CONTEXT_PREFIX + "nsis_properties_context"; //$NON-NLS-1$
    }

    private IResource getResource()
    {
        IAdaptable adaptable = getElement();
        if(adaptable instanceof IResource) {
            return (IResource)adaptable;
        }
        else {
            return (IResource)adaptable.getAdapter(IResource.class);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.dialogs.NSISSettingsPage#getPageDescription()
     */
    @Override
    protected String getPageDescription()
    {
        String label = null;
        IResource resource = getResource();
        if(resource instanceof IFile) {
            label = "file.properties.header.text"; //$NON-NLS-1$
        }
        else if(resource instanceof IFolder) {
            label = "folder.properties.header.text"; //$NON-NLS-1$
        }
        else {
            label = "project.properties.header.text"; //$NON-NLS-1$
        }
        return EclipseNSISPlugin.getResourceString(label);
    }

    @Override
    protected NSISSettingsEditor createSettingsEditor()
    {
        return new PropertiesEditor();
    }

    private class PropertiesEditor extends NSISSettingsEditor
    {
        @Override
        protected void addPages(TabFolder folder)
        {
            super.addPages(folder);
            if(getResource() instanceof IFile) {
                addPage(folder, "associated.headers.tab.text","associated.headers.tab.tooltip",new NSISAssociatedHeadersPropertyPage(getSettings())); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        protected NSISSettingsEditorGeneralPage createGeneralPage()
        {
            return new PropertiesEditorGeneralPage(getSettings());
        }

        @Override
        protected NSISSettingsEditorSymbolsPage createSymbolsPage()
        {
            return new PropertiesEditorSymbolsPage(getSettings());
        }

        @Override
        protected NSISSettings loadSettings()
        {
            return NSISProperties.getProperties(getResource());
        }

        private class PropertiesEditorSymbolsPage extends NSISSettingsEditorSymbolsPage
        {
            public PropertiesEditorSymbolsPage(NSISSettings settings)
            {
                super(settings);
            }

            @Override
            protected boolean performApply(NSISSettings settings)
            {
                if (getControl() != null) {
                    if(!((NSISProperties)settings).getUseParent()) {
                        return super.performApply(settings);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void setDefaults()
            {
                if (getControl() != null) {
                    if(!((NSISProperties)getSettings()).getUseParent()) {
                        super.setDefaults();
                    }
                }
            }
        }

        private class PropertiesEditorGeneralPage extends NSISSettingsEditorGeneralPage
        {
            private Button mUseParent = null;

            public PropertiesEditorGeneralPage(NSISSettings settings)
            {
                super(settings);
            }

            @Override
            public void reset()
            {
                mUseParent.setSelection(((NSISProperties)getSettings()).getUseParent());
                super.reset();
            }

            @Override
            protected boolean performApply(NSISSettings settings)
            {
                if (getControl() != null) {
                    boolean useParent = mUseParent.getSelection();
                    if(useParent || super.performApply(settings)) {
                        ((NSISProperties)getSettings()).setUseParent(useParent);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void setDefaults()
            {
                if (getControl() != null) {
                    if(mUseParent.getSelection()) {
                        super.setDefaults();
                    }
                }
            }

            @Override
            public boolean canEnableControls()
            {
                return !mUseParent.getSelection();
            }

            @Override
            protected Composite createMasterControl(Composite parent)
            {
                Composite composite = new Composite(parent,SWT.NONE);
                GridLayout layout = new GridLayout(1,false);
                layout.marginWidth = 0;
                composite.setLayout(layout);
                String label = null;
                String tooltip = null;
                IResource resource = getResource();
                if(resource instanceof IFile) {
                    label = "folder.options.label"; //$NON-NLS-1$
                    tooltip = "folder.options.tooltip"; //$NON-NLS-1$
                }
                else if(resource instanceof IFolder) {
                    if(((IFolder)resource).getParent() instanceof IProject) {
                        label = "project.options.label"; //$NON-NLS-1$
                        tooltip = "project.options.tooltip"; //$NON-NLS-1$
                    }
                    else {
                        label = "folder.options.label"; //$NON-NLS-1$
                        tooltip = "folder.options.tooltip"; //$NON-NLS-1$
                    }
                }
                else {
                    label = "global.options.label"; //$NON-NLS-1$
                    tooltip = "global.options.tooltip"; //$NON-NLS-1$
                }
                mUseParent = createCheckBox(composite,
                                            EclipseNSISPlugin.getResourceString(label),
                                            EclipseNSISPlugin.getResourceString(tooltip),
                                            ((NSISProperties)getSettings()).getUseParent());
                mUseParent.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        fireChanged();
                    }
                });

                return composite;
            }

            @Override
            public void enableControls(boolean state)
            {
                Button button = getDefaultsButton();
                if(button != null && !button.isDisposed()) {
                    button.setEnabled(state);
                }
                super.enableControls(state);
            }
        }
    }
}