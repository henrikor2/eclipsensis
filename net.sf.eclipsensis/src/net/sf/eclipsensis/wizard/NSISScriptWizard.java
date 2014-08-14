/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.template.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;

public class NSISScriptWizard extends NSISWizard
{
    private boolean mSaveAsTemplate = false;
    private boolean mCheckOverwrite = false;
    private NSISWizardTemplateManager mTemplateManager = new NSISWizardTemplateManager();
    /**
     *
     */
    public NSISScriptWizard()
    {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle(EclipseNSISPlugin.getResourceString("wizard.window.title")); //$NON-NLS-1$
    }

    @Override
    public String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizard_context"; //$NON-NLS-1$
    }

    /**
     * @return Returns the templateManager.
     */
    public NSISWizardTemplateManager getTemplateManager()
    {
        return mTemplateManager;
    }

    public boolean isCheckOverwrite()
    {
        return mCheckOverwrite;
    }

    public void setCheckOverwrite(boolean checkOverwrite)
    {
        mCheckOverwrite = checkOverwrite;
    }

    private boolean saveTemplate()
    {
        NSISWizardTemplate template;
        if(getTemplate() != null) {
            template = (NSISWizardTemplate)getTemplate().clone();
        }
        else {
            template = new NSISWizardTemplate(""); //$NON-NLS-1$
            setTemplate(template);
        }
        NSISWizardTemplateDialog dialog = new NSISWizardTemplateDialog(getShell(),getTemplateManager(), template, getSettings());
        return(dialog.open() == Window.OK);
    }

    void loadTemplate(NSISWizardTemplate template)
    {
        setTemplate(template);
        setSettings(template.getSettings());
    }

    @Override
    public boolean performFinish()
    {
        IPath path = new Path(getSettings().getSavePath());
        if(Common.isEmpty(path.getFileExtension())) {
            path = path.addFileExtension(INSISConstants.NSI_EXTENSION);
        }
        if(!path.isAbsolute()) {
            Common.openError(getShell(),EclipseNSISPlugin.getResourceString("absolute.save.path.error"),EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
            return false;
        }
        final boolean saveExternal = getSettings().isSaveExternal();
        String pathString = saveExternal?path.toOSString():path.toString();
        final boolean exists;
        final File file;
        final IFile ifile;
        if(saveExternal) {
            ifile = null;
            file = new File(pathString);
            exists = file.exists();
        }
        else {
            file = null;
            ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            exists = ifile != null && ifile.exists();
            if (ifile != null) {
                path = ifile.getLocation();
            }
            if(path == null) {
                Common.openError(getShell(),EclipseNSISPlugin.getResourceString("local.filesystem.error"),EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                return false;
            }
        }
        if(exists && mCheckOverwrite) {
            if(!Common.openQuestion(getShell(), EclipseNSISPlugin.getResourceString("question.title"), //$NON-NLS-1$
                    EclipseNSISPlugin.getFormattedString("save.path.question",new String[] {pathString}),  //$NON-NLS-1$
                    EclipseNSISPlugin.getShellImage())) {
                return false;
            }
            mCheckOverwrite = false;
        }
        getSettings().setSavePath(pathString);
        if(mSaveAsTemplate) {
            if(!saveTemplate()) {
                return false;
            }
        }
        java.util.List<IEditorPart> editors = NSISEditorUtilities.findEditors(path);
        if(!Common.isEmptyCollection(editors)) {
            java.util.List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();
            for (Iterator<IEditorPart> iter = editors.iterator(); iter.hasNext();) {
                IEditorPart editor = iter.next();
                if(editor.isDirty()) {
                    dirtyEditors.add(editor);
                }
            }
            if(dirtyEditors.size() > 0) {
                if(!Common.openConfirm(getShell(), EclipseNSISPlugin.getFormattedString("save.dirty.editor.confirm",new String[] {pathString}),  //$NON-NLS-1$
                    EclipseNSISPlugin.getShellImage())) {
                    return false;
                }
                for (Iterator<IEditorPart> iter = dirtyEditors.iterator(); iter.hasNext();) {
                    IEditorPart editor = iter.next();
                    editor.getSite().getPage().closeEditor(editor,false);
                    editors.remove(editor);
                }

                if(saveExternal) {
                    for (Iterator<IEditorPart> iter = editors.iterator(); iter.hasNext();) {
                        IEditorPart editor = iter.next();
                        editor.getSite().getPage().closeEditor(editor,false);
                    }
                }
            }
        }
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException
            {
                try {
                    if(exists) {
                        if(saveExternal) {
                            if (file != null) {
                                file.delete();
                            }
                        }
                        else {
                            if (ifile != null) {
                                ifile.delete(true, true, null);
                            }
                        }
                    }
                    new NSISWizardScriptGenerator(getSettings()).generate(getShell(),monitor);
                }
                catch (Exception e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
        try {
            getContainer().run(true, false, op);
        }
        catch (InterruptedException e) {
            return false;
        }
        catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            Common.openError(getShell(), realException.getLocalizedMessage(), EclipseNSISPlugin.getShellImage());
            return false;
        }
        return true;
    }

    @Override
    public boolean performCancel()
    {
        if(isForcedCancel() || Common.openQuestion(getShell(),EclipseNSISPlugin.getResourceString("wizard.cancel.question"), //$NON-NLS-1$
                EclipseNSISPlugin.getShellImage())) {
            return super.performCancel();
        }
        else {
            return false;
        }
    }

    /**
     * @return Returns the saveAsTemplate.
     */
    public boolean isSaveAsTemplate()
    {
        return mSaveAsTemplate;
    }

    /**
     * @param saveAsTemplate The saveAsTemplate to set.
     */
    public void setSaveAsTemplate(boolean saveAsTemplate)
    {
        mSaveAsTemplate = saveAsTemplate;
    }

    /**
     *
     */
    @Override
    protected void addStartPage()
    {
        addPage(new NSISWizardWelcomePage());
    }

    /**
     * @return Returns the template.
     */
    @Override
    protected NSISWizardTemplate getTemplate()
    {
        return mTemplate;
    }

    /**
     * @param template The template to set.
     */
    @Override
    protected void setTemplate(NSISWizardTemplate template)
    {
        mTemplate = template;
    }
}
