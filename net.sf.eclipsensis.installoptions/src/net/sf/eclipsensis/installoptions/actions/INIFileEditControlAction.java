/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.util.Iterator;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.util.Common;

public class INIFileEditControlAction extends INIFileCreateControlAction
{
    /**
     * @param editor
     */
    public INIFileEditControlAction(InstallOptionsSourceEditor editor)
    {
        super(editor);
        setText(InstallOptionsPlugin.getResourceString("edit.control.action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("edit.control.action.tooltip")); //$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("edit.control.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("edit.control.disabled.icon"))); //$NON-NLS-1$)
    }

    @Override
    protected INISection getSection(INIFile iniFile)
    {
        INISection section = null;
        INISection currSection = mEditor.getCurrentSection();
        if (currSection != null && !mEditor.getINIFile().hasErrors()) {
            for(Iterator<INILine> iter = iniFile.getChildren().iterator(); iter.hasNext(); ) {
                INILine line = iter.next();
                if (line instanceof INISection) {
                    INISection sec2 = (INISection)line;
                    if(Common.stringsAreEqual(sec2.getName(), currSection.getName())) {
                        section = sec2;
                        break;
                    }
                }
            }
        }
        return section;
    }

    @Override
    protected boolean doRun(INIFile iniFile, INISection section)
    {
        if (section != null) {
            return doRun2(iniFile, section);
        }
        return false;
    }

    /**
     * @param iniFile
     * @param section
     */
    protected boolean doRun2(INIFile iniFile, INISection section)
    {
        return super.doRun(iniFile, section);
    }
}