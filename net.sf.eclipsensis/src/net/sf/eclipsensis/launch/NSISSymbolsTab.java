/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.launch;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.settings.*;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

class NSISSymbolsTab extends NSISTab
{
    static final String[] FILTER_EXTENSIONS = new String[] {"*."+INSISConstants.NSI_EXTENSION}; //$NON-NLS-1$
    static final String[] FILTER_NAMES = new String[] {EclipseNSISPlugin.getResourceString("nsis.script.filtername")}; //$NON-NLS-1$

    private boolean mBuilder = false;

    public NSISSymbolsTab(boolean builder)
    {
        super();
        mBuilder = builder;
    }

    @Override
    protected NSISSettingsEditorPage createPage()
    {
        return new NSISSettingsEditorSymbolsPage(mSettings);
    }

    @Override
    protected IFilter createSettingsFilter()
    {
        return new IFilter() {
            public boolean select(Object toTest)
            {
                return INSISSettingsConstants.SYMBOLS.equals(toTest);
            }
        };
    }

    @Override
    public Image getImage()
    {
        return EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("nsis.symbols.tab.icon")); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        if (mBuilder) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_buildconfig_symbols_context"); //$NON-NLS-1$
        }
        else {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_launchconfig_symbols_context"); //$NON-NLS-1$
        }
    }

    public String getName()
    {
        return EclipseNSISPlugin.getResourceString("launchconfig.symbols.tab.name"); //$NON-NLS-1$
    }
}