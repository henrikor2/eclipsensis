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

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.*;

public class NSISLaunchConfigMigrationDelegate implements ILaunchConfigurationMigrationDelegate
{
    public boolean isCandidate(ILaunchConfiguration candidate) throws CoreException
    {
        return (!Common.isEmptyArray(getFiles(candidate)) && Common.isEmptyArray(candidate.getMappedResources()));
    }

    public void migrate(ILaunchConfiguration candidate) throws CoreException
    {
        IFile[] files = getFiles(candidate);
        ILaunchConfigurationWorkingCopy wc = candidate.getWorkingCopy();
        wc.setMappedResources(files);
        wc.doSave();
    }

    private IFile[] getFiles(ILaunchConfiguration config) throws CoreException
    {
        String script = config.getAttribute(NSISLaunchSettings.SCRIPT,""); //$NON-NLS-1$
        if(!script.equals("")) { //$NON-NLS-1$
            String file = null;
            try {
                file = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(script);
            }
            catch (CoreException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
            if(file != null) {
                File f = new File(file);
                if(f.exists()) {
                    return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(f.toURI());
                }
            }
        }
        return null;
    }
}
