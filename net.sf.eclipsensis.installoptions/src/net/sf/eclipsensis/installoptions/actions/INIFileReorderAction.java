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
import java.util.regex.Matcher;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

public class INIFileReorderAction extends INIFileAction
{
    public INIFileReorderAction(InstallOptionsSourceEditor editor)
    {
        super(editor);
        setText(InstallOptionsPlugin.getResourceString("reorder.action.name")); //$NON-NLS-1$
        setToolTipText(InstallOptionsPlugin.getResourceString("reorder.action.tooltip")); //$NON-NLS-1$
        setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("reorder.action.icon"))); //$NON-NLS-1$
        setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("reorder.action.disabled.icon"))); //$NON-NLS-1$
    }

    @Override
    protected boolean doRun(INIFile iniFile)
    {
        final List<INILine> original = iniFile.getChildren();
        List<INILine> sorted = new ArrayList<INILine>(original);
        Collections.sort(sorted,new Comparator<INILine>() {
            public int compare(INILine line1, INILine line2)
            {
                if(line1 instanceof INISection && line2 instanceof INISection) {
                    INISection sec1 = (INISection)line1;
                    INISection sec2 = (INISection)line2;
                    if(sec1.getName().equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
                        return -1;
                    }
                    else if(sec2.getName().equalsIgnoreCase(InstallOptionsModel.SECTION_SETTINGS)) {
                        return 1;
                    }
                    else if(sec1.isInstallOptionsField() && sec2.isInstallOptionsField()) {
                        return getInstallOptionsFieldNumber(sec1) - getInstallOptionsFieldNumber(sec2);
                    }
                    else if(sec1.isInstallOptionsField()) {
                        return -1;
                    }
                    else if(sec2.isInstallOptionsField()) {
                        return 1;
                    }
                }
                else if(line1 instanceof INISection) {
                    return 1;
                }
                else if(line2 instanceof INISection) {
                    return -1;
                }
                return original.indexOf(line1)-original.indexOf(line2);
            }

            /**
             * @param sec1
             * @return
             */
            private int getInstallOptionsFieldNumber(INISection sec1)
            {
                try {
                    Matcher matcher = InstallOptionsModel.SECTION_FIELD_PATTERN.matcher(sec1.getName());
                    matcher.matches();
                    String group = matcher.group(1);
                    return Integer.parseInt(group);
                }
                catch (Exception e) {
                    return 0;
                }
            }
        });

        if(!original.equals(sorted)) {
            for (Iterator<INILine> iter = sorted.iterator(); iter.hasNext();) {
                INILine line = iter.next();
                boolean lastLine = !iter.hasNext();
                if(line instanceof INISection) {
                    INISection sec = (INISection)line;
                    List<INILine> children = sec.getChildren();
                    INILine lastChild = null;
                    ListIterator<INILine> iter2=children.listIterator(children.size());
                    if(iter2.hasPrevious()) {
                        lastChild = iter2.previous();
                        if(lastChild.isBlank()) {
                            for(; iter2.hasPrevious(); ) {
                                INILine child = iter2.previous();
                                if(child.isBlank()) {
                                    iter2.remove();
                                }
                                else {
                                    break;
                                }
                            }
                        }
                    }
                    if(lastChild == null) {
                        if(!lastLine) {
                            sec.addChild(new INILine("",INSISConstants.LINE_SEPARATOR)); //$NON-NLS-1$
                        }
                    }
                    else if(lastChild.isBlank()) {
                        if(lastLine) {
                            sec.removeChild(lastChild);
                        }
                        else if(lastChild.getDelimiter() == null) {
                            lastChild.setDelimiter(INSISConstants.LINE_SEPARATOR);
                        }
                    }
                    else if(!lastLine) {
                        if(lastChild.getDelimiter() == null) {
                            lastChild.setDelimiter(INSISConstants.LINE_SEPARATOR);
                        }
                        sec.addChild(new INILine("",INSISConstants.LINE_SEPARATOR)); //$NON-NLS-1$
                    }
                }
            }
            original.clear();
            original.addAll(sorted);
            mEditor.getDocumentProvider().getDocument(mEditor.getEditorInput()).set(iniFile.toString());
            return true;
        }
        return false;
    }
}
