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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.installoptions.ini.*;

import org.eclipse.jface.text.*;

public class INIFileFixProblemsAction extends INIFileAction
{
    public static final String FIX_ALL_ID = "net.sf.eclipsensis.installoptions.fix_all"; //$NON-NLS-1$
    public static final String FIX_ERRORS_ID = "net.sf.eclipsensis.installoptions.fix_errors"; //$NON-NLS-1$
    public static final String FIX_WARNINGS_ID = "net.sf.eclipsensis.installoptions.fix_warnings"; //$NON-NLS-1$

    private int mFixFlag;

    public INIFileFixProblemsAction(InstallOptionsSourceEditor editor, String id)
    {
        super(editor);
        String prefix;
        String id2 = id;
        if(FIX_ALL_ID.equals(id2)) {
            mFixFlag = INILine.VALIDATE_FIX_ALL;
            prefix="fix.all"; //$NON-NLS-1$
        }
        else if(FIX_WARNINGS_ID.equals(id2)) {
            mFixFlag = INILine.VALIDATE_FIX_WARNINGS;
            prefix="fix.warnings"; //$NON-NLS-1$
        }
        else {
            mFixFlag = INILine.VALIDATE_FIX_ERRORS;
            id2 = FIX_ERRORS_ID;
            prefix="fix.errors"; //$NON-NLS-1$
        }
        setId(id2);

        setText(InstallOptionsPlugin.getResourceString(prefix+".action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString(prefix+".action.tooltip")); //$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".action.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString(prefix+".action.disabled.icon"))); //$NON-NLS-1$
    }

    @Override
    protected boolean doRun(INIFile iniFile)
    {
        iniFile.validate(mFixFlag, true);
        iniFile.update();
        IRewriteTarget target = (IRewriteTarget)mEditor.getAdapter(IRewriteTarget.class);
        IDocument document = target.getDocument();
        document.set(iniFile.toString());
        return true;
    }
}
