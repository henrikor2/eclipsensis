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

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.installoptions.ini.*;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.*;
import org.eclipse.swt.widgets.Control;

public abstract class INIFileAction extends Action
{
    protected InstallOptionsSourceEditor mEditor;

    public INIFileAction(InstallOptionsSourceEditor editor)
    {
        mEditor = editor;
    }
    @Override
    public void run()
    {
        if(doRun(mEditor.getINIFile().copy())) {
            Control c = (Control)mEditor.getAdapter(Control.class);
            if(c != null) {
                c.setFocus();
            }
        }
    }

    /**
     * @param iniFile
     */
    protected abstract boolean doRun(INIFile iniFile);
    /**
     * @param iniFile
     * @param section
     */
    protected void updateDocument(INIFile iniFile, INISection section)
    {
        List<INISection> dirtyList = new ArrayList<INISection>();
        for (Iterator<INILine> iter = iniFile.getChildren().iterator(); iter.hasNext();) {
            INILine line = iter.next();
            if (line instanceof INISection && ((INISection)line).isDirty()) {
                dirtyList.add((INISection) line);
            }
        }
        boolean isDelete = false;
        if(section != null && !dirtyList.contains(section)) {
            isDelete = true;
            dirtyList.add(section);
        }
        if (dirtyList.size() > 0) {
            Collections.sort(dirtyList, new Comparator<INISection>() {

                private Position getPosition(INISection section)
                {
                    Position p = section.getPosition();
                    return (p == null?IInstallOptionsConstants.MAX_POSITION:p);
                }

                public int compare(INISection s1, INISection s2)
                {
                    Position p1 = getPosition(s1);
                    Position p2 = getPosition(s2);
                    int n = p2.getOffset() - p1.getOffset();
                    if (n == 0) {
                        n = p2.getLength() - p1.getLength();
                    }
                    return n;
                }
            });
            IRewriteTarget target = (IRewriteTarget)mEditor.getAdapter(IRewriteTarget.class);
            IDocument document = target.getDocument();
            try {
                target.beginCompoundChange();
                Iterator<INISection> iter = dirtyList.iterator();
                while (iter.hasNext()) {
                    INISection sec = iter.next();
                    Position p = sec.getPosition();
                    if(p == null) {
                        document.replace(document.getLength(), 0, sec.toString());
                    }
                    else {
                        if(sec == section && isDelete) {
                            document.replace(p.getOffset(), p.getLength(), ""); //$NON-NLS-1$
                        }
                        else {
                            document.replace(p.getOffset(), p.getLength(), sec.toString());
                        }
                    }
                }
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
            finally {
                target.endCompoundChange();
            }
            if (!isDelete && section != null) {
                INISection[] sections = mEditor.getINIFile().findSections(section.getName());
                if (sections != null && sections.length == 1) {
                    Position p = sections[0].getPosition();
                    mEditor.getSelectionProvider().setSelection(new TextSelection(p.getOffset(), p.getLength()));
                }
            }
        }
    }
}
