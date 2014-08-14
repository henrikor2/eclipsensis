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

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.*;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.IInstallOptionsEditor;
import net.sf.eclipsensis.installoptions.figures.DashedLineBorder;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.*;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.script.NSISScriptProblem;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.Disposable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class PreviewAction extends Action implements Disposable, IMakeNSISRunListener, INSISHomeListener
{
    public static final String PREVIEW_CLASSIC_ID = "net.sf.eclipsensis.installoptions.preview_classic"; //$NON-NLS-1$
    public static final String PREVIEW_MUI_ID = "net.sf.eclipsensis.installoptions.preview_mui"; //$NON-NLS-1$

    private static final String cMUIDialogSizeName = InstallOptionsPlugin.getResourceString("mui.dialog.size.name"); //$NON-NLS-1$
    private static final String cClassicDialogSizeName = InstallOptionsPlugin.getResourceString("classic.dialog.size.name"); //$NON-NLS-1$
    private static INSISConsole cDummyConsole = new NullNSISConsole();
    private static Map<PreviewCacheKey,File> cPreviewCache = new HashMap<PreviewCacheKey,File>();
    private static Map<Dimension, File> cBitmapCache = new HashMap<Dimension, File>();
    private static Map<Dimension, File> cIconCache = new HashMap<Dimension, File>();

    private IInstallOptionsEditor mEditor;
    private NSISSettings mSettings = new DummyNSISSettings();
    private IPreferenceStore mPreferenceStore = InstallOptionsPlugin.getDefault().getPreferenceStore();

    public PreviewAction(int type, IInstallOptionsEditor editor)
    {
        super();
        mEditor = editor;
        String resource;
        Map<String,String> symbols = new LinkedHashMap<String,String>();
        switch(type) {
            case IInstallOptionsConstants.PREVIEW_CLASSIC:
                setId(PREVIEW_CLASSIC_ID);
                resource = "preview.action.classic.label"; //$NON-NLS-1$
                break;
            default:
                setId(PREVIEW_MUI_ID);
                resource = "preview.action.mui.label"; //$NON-NLS-1$
                symbols.put("PREVIEW_MUI",null); //$NON-NLS-1$
        }
        mSettings.setVerbosity(INSISSettingsConstants.VERBOSITY_DEFAULT);
        mSettings.setSymbols(symbols);
        String label = InstallOptionsPlugin.getResourceString(resource);
        setText(label);
        setToolTipText(label);
        NSISPreferences.getInstance().addListener(this);
        MakeNSISRunner.addListener(this);
        updateEnabled();
    }

    public void nsisHomeChanged(IProgressMonitor monitor, NSISHome oldHome, NSISHome newHome)
    {
        updateEnabled();
    }

    private void updateEnabled()
    {
        setEnabled((mEditor != null && EclipseNSISPlugin.getDefault().isConfigured() && !MakeNSISRunner.isCompiling()));
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        switch(event.getType()) {
            case MakeNSISRunEvent.STARTED:
                setEnabled(false);
                break;
            case MakeNSISRunEvent.STOPPED:
                updateEnabled();
                break;
        }
    }

    public void scriptUpdated()
    {
        updateEnabled();
    }

    public void dispose()
    {
        NSISPreferences.getInstance().removeListener(this);
        MakeNSISRunner.removeListener(this);
    }

    @Override
    public void run()
    {
        if(mEditor != null) {
            Shell shell = mEditor.getSite().getShell();
            if(mEditor.isDirty()) {
                boolean autosaveBeforePreview = mPreferenceStore.getBoolean(IInstallOptionsConstants.PREFERENCE_AUTOSAVE_BEFORE_PREVIEW);
                boolean shouldSave = autosaveBeforePreview;
                if(!shouldSave) {
                    MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, EclipseNSISPlugin.getResourceString("confirm.title"), //$NON-NLS-1$
                                    InstallOptionsPlugin.getShellImage(), InstallOptionsPlugin.getResourceString("save.before.preview.confirm"), //$NON-NLS-1$
                                    MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0,
                                    InstallOptionsPlugin.getResourceString("confirm.toggle.message"),false); //$NON-NLS-1$
                    dialog.open();
                    shouldSave = dialog.getReturnCode()==IDialogConstants.OK_ID;
                    if(shouldSave && dialog.getToggleState()) {
                        mPreferenceStore.setValue(IInstallOptionsConstants.PREFERENCE_AUTOSAVE_BEFORE_PREVIEW, true);
                    }
                }
                if(shouldSave) {
                    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                    dialog.open();
                    IProgressMonitor progressMonitor = dialog.getProgressMonitor();
                    mEditor.doSave(progressMonitor);
                    dialog.close();
                    if(progressMonitor.isCanceled()) {
                        return;
                    }
                }
                else {
                    return;
                }
            }
            INIFile iniFile = mEditor.getINIFile();
            if(iniFile.hasErrors()) {
                Common.openError(shell,InstallOptionsPlugin.getResourceString("ini.errors.preview.error"),  //$NON-NLS-1$
                                InstallOptionsPlugin.getShellImage());
                return;
            }
            INISection settings = iniFile.findSections(InstallOptionsModel.SECTION_SETTINGS)[0];
            INIKeyValue numFields = settings.findKeyValues(InstallOptionsModel.PROPERTY_NUMFIELDS)[0];
            if(Integer.parseInt(numFields.getValue()) <= 0) {
                Common.openError(shell,InstallOptionsPlugin.getResourceString("ini.numfields.preview.error"),  //$NON-NLS-1$
                                InstallOptionsPlugin.getShellImage());
            }
            else {
                IPathEditorInput editorInput = NSISEditorUtilities.getPathEditorInput(mEditor);
                if(editorInput instanceof IFileEditorInput) {
                    IFile file = ((IFileEditorInput)editorInput).getFile();
                    if(file.exists()) {
                        IPath location = file.getLocation();
                        if(location != null) {
                            doPreview(iniFile, location.toFile());
                        }
                        else {
                            Common.openError(shell,EclipseNSISPlugin.getResourceString("local.filesystem.error"),  //$NON-NLS-1$
                                            InstallOptionsPlugin.getShellImage());
                        }
                    }
                }
                else if(editorInput != null) {
                    doPreview(iniFile, new File(editorInput.getPath().toOSString()));
                }
            }
        }
    }

    private void doPreview(final INIFile iniFile, final File file)
    {
        final Shell shell = mEditor.getSite().getShell();
        BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
            public void run() {
                final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell) {
                    @Override
                    protected void configureShell(Shell shell)
                    {
                        super.configureShell(shell);
                        Rectangle rect = shell.getDisplay().getBounds();
                        shell.setLocation(rect.x+rect.width+1,rect.y+rect.height+1);
                    }

                    @Override
                    protected Rectangle getConstrainedShellBounds(Rectangle preferredSize)
                    {
                        Rectangle rect = shell.getDisplay().getBounds();
                        return new Rectangle(rect.x+rect.width+1,rect.y+rect.height+1,preferredSize.width,preferredSize.height);
                    }
                };
                pmd.open();
                try {
                    ModalContext.run(new IRunnableWithProgress() {
                        private File createPreviewFile(File previewFile, INIFile inifile, final NSISLanguage lang) throws IOException
                        {
                            File previewFile2 = previewFile;
                            INIFile inifile2 = inifile;
                            if(previewFile2 == null) {
                                previewFile2 = File.createTempFile("preview",".ini"); //$NON-NLS-1$ //$NON-NLS-2$
                                previewFile2.deleteOnExit();
                            }
                            inifile2 = inifile2.copy();
                            InstallOptionsDialog dialog = InstallOptionsDialog.loadINIFile(inifile2);
                            DialogSize dialogSize;
                            if(getId().equals(PREVIEW_MUI_ID)) {
                                dialogSize = DialogSizeManager.getDialogSize(cMUIDialogSizeName);
                            }
                            else {
                                dialogSize = DialogSizeManager.getDialogSize(cClassicDialogSizeName);
                            }
                            if(dialogSize == null) {
                                dialogSize = DialogSizeManager.getDefaultDialogSize();
                            }
                            dialog.setDialogSize(dialogSize);
                            Font font = FontUtility.getFont(lang);
                            for(Iterator<InstallOptionsWidget> iter=dialog.getChildren().iterator(); iter.hasNext(); ) {
                                InstallOptionsWidget widget = iter.next();
                                if(widget instanceof InstallOptionsPicture) {
                                    final InstallOptionsPicture picture = (InstallOptionsPicture)widget;
                                    final Dimension dim = widget.toGraphical(widget.getPosition(), font).getSize();
                                    final Map<Dimension, File> cache;
                                    switch(picture.getSWTImageType()) {
                                        case SWT.IMAGE_BMP:
                                            cache = cBitmapCache;
                                            break;
                                        case SWT.IMAGE_ICO:
                                            cache = cIconCache;
                                            break;
                                        default:
                                            continue;
                                    }
                                    final File[] imageFile = new File[] {cache.get(dim)};
                                    if(!IOUtility.isValidFile(imageFile[0])) {
                                        shell.getDisplay().syncExec(new Runnable() {
                                            public void run()
                                            {
                                                Image widgetImage = picture.getImage();
                                                ImageData data = widgetImage.getImageData();
                                                data.width = dim.width;
                                                data.height = dim.height;
                                                data.type = picture.getSWTImageType();

                                                Image bitmap = new Image(shell.getDisplay(), data);
                                                GC gc = new GC(bitmap);
                                                gc.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                                                gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                                                gc.fillRectangle(0, 0, dim.width, dim.height);
                                                Rectangle rect = widgetImage.getBounds();
                                                int x, y, width, height;
                                                if (rect.width > dim.width) {
                                                    x = 0;
                                                    width = dim.width;
                                                }
                                                else {
                                                    x = (dim.width - rect.width) / 2;
                                                    width = rect.width;
                                                }
                                                if (rect.height > dim.height) {
                                                    y = 0;
                                                    height = dim.height;
                                                }
                                                else {
                                                    y = (dim.height - rect.height) / 2;
                                                    height = rect.height;
                                                }
                                                gc.drawImage(widgetImage, 0, 0, rect.width, rect.height, x, y, width, height);
                                                gc.setLineStyle(SWT.LINE_CUSTOM);
                                                gc.setLineDash(DashedLineBorder.DASHES);
                                                gc.drawRectangle(0, 0, dim.width-1, dim.height-1);
                                                gc.dispose();
                                                ImageLoader loader = new ImageLoader();
                                                loader.data = new ImageData[]{bitmap.getImageData()};
                                                try {
                                                    imageFile[0] = File.createTempFile("preview", picture.getFileExtension()); //$NON-NLS-1$
                                                    imageFile[0].deleteOnExit();
                                                    loader.save(imageFile[0].getAbsolutePath(), picture.getSWTImageType());
                                                    cache.put(dim, imageFile[0]);
                                                }
                                                catch (IOException e) {
                                                    imageFile[0] = null;
                                                    InstallOptionsPlugin.getDefault().log(e);
                                                    cache.remove(dim);
                                                }
                                            }
                                        });
                                    }
                                    if(imageFile[0] != null) {
                                        picture.setPropertyValue(InstallOptionsModel.PROPERTY_TEXT, imageFile[0].getAbsolutePath());
                                    }
                                }
                            }
                            inifile2 = dialog.updateINIFile();
                            IOUtility.writeContentToFile(previewFile2,inifile2.toString().getBytes());
                            return previewFile2;
                        }

                        public void run(IProgressMonitor monitor)
                        {
                            try {
                                monitor.beginTask(InstallOptionsPlugin.getResourceString("previewing.script.task.name"),IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                                String pref = InstallOptionsPlugin.getDefault().getPreferenceStore().getString(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG);
                                NSISLanguage lang;
                                if(pref.equals("")) { //$NON-NLS-1$
                                    lang = NSISLanguageManager.getInstance().getDefaultLanguage();
                                }
                                else {
                                    lang = NSISLanguageManager.getInstance().getLanguage(pref);
                                    if(lang == null) {
                                        lang = NSISLanguageManager.getInstance().getDefaultLanguage();
                                        InstallOptionsPlugin.getDefault().getPreferenceStore().setValue(IInstallOptionsConstants.PREFERENCE_PREVIEW_LANG, ""); //$NON-NLS-1$
                                    }
                                }
                                PreviewCacheKey key = new PreviewCacheKey(file,lang);
                                File previewFile = cPreviewCache.get(key);
                                if(previewFile == null || file.lastModified() > previewFile.lastModified()) {
                                    previewFile = createPreviewFile(previewFile, iniFile, lang);
                                    cPreviewCache.put(key,previewFile);
                                }

                                Map<String,String> symbols = mSettings.getSymbols();

                                symbols.put("PREVIEW_INI",previewFile.getAbsolutePath()); //$NON-NLS-1$
                                symbols.put("PREVIEW_LANG",lang.getName()); //$NON-NLS-1$
                                Locale locale = NSISLanguageManager.getInstance().getLocaleForLangId(lang.getLangId());
                                if(getId().equals(PREVIEW_MUI_ID)) {
                                    symbols.put("PREVIEW_TITLE",InstallOptionsPlugin.getResourceString(locale,"preview.setup.title")); //$NON-NLS-1$ //$NON-NLS-2$
                                    symbols.put("PREVIEW_SUBTITLE",InstallOptionsPlugin.getResourceString(locale,"preview.setup.subtitle")); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                else {
                                    symbols.put("PREVIEW_BRANDING",InstallOptionsPlugin.getResourceString(locale,"preview.setup.branding")); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                symbols.put("PREVIEW_NAME",InstallOptionsPlugin.getResourceString(locale,"preview.setup.name")); //$NON-NLS-1$ //$NON-NLS-2$
                                if(EclipseNSISPlugin.getDefault().isWinVista() && NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_21) >= 0) {
                                    symbols.put("WINDOWS_VISTA",""); //$NON-NLS-1$ //$NON-NLS-2$
                                }

                                mSettings.setSymbols(symbols);
                                final File previewScript = getPreviewScript();
                                long timestamp = System.currentTimeMillis();
                                MakeNSISResults results = null;
                                results = MakeNSISRunner.compile(previewScript, mSettings, cDummyConsole, new INSISConsoleLineProcessor() {
                                    public NSISConsoleLine processText(String text)
                                    {
                                        return NSISConsoleLine.info(text);
                                    }

                                    public void reset()
                                    {
                                    }
                                });
                                if(results != null) {
                                    if (results.getReturnCode() != 0) {
                                        List<NSISScriptProblem> errors = results.getProblems();
                                        final String error;
                                        if (!Common.isEmptyCollection(errors)) {
                                            Iterator<NSISScriptProblem> iter = errors.iterator();
                                            StringBuffer buf = new StringBuffer(iter.next().getText());
                                            while (iter.hasNext()) {
                                                buf.append(INSISConstants.LINE_SEPARATOR).append(iter.next().getText());
                                            }
                                            error = buf.toString();
                                        }
                                        else {
                                            error = InstallOptionsPlugin.getResourceString("preview.compile.error"); //$NON-NLS-1$
                                        }
                                        shell.getDisplay().asyncExec(new Runnable() {
                                            public void run()
                                            {
                                                Common.openError(shell, error, InstallOptionsPlugin.getShellImage());
                                            }
                                        });
                                    }
                                    else {
                                        final File outfile = new File(results.getOutputFileName());
                                        if (IOUtility.isValidFile(outfile) && outfile.lastModified() > timestamp) {
                                            MakeNSISRunner.testInstaller(outfile.getAbsolutePath(), null, true);
                                        }
                                    }
                                }
                            }
                            catch (final Exception e) {
                                InstallOptionsPlugin.getDefault().log(e);
                                shell.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        Common.openError(shell, e.getMessage(), InstallOptionsPlugin.getShellImage());
                                    }
                                });
                            }
                            finally {
                                monitor.done();
                            }
                        }
                    },true,pmd.getProgressMonitor(),shell.getDisplay());
                }
                catch (Exception e) {
                    InstallOptionsPlugin.getDefault().log(e);
                    Common.openError(shell, e.getMessage(), InstallOptionsPlugin.getShellImage());
                }
                finally {
                    pmd.close();
                }
            }
        });
    }

    private File getPreviewScript() throws IOException
    {
        return IOUtility.ensureLatest(InstallOptionsPlugin.getDefault().getBundle(),
                        new Path("/preview/preview.nsi"),  //$NON-NLS-1$
                        new File(InstallOptionsPlugin.getPluginStateLocation(),"preview")); //$NON-NLS-1$
    }

    private class PreviewCacheKey
    {
        private File mFile;
        private NSISLanguage mLanguage;

        public PreviewCacheKey(File file, NSISLanguage language)
        {
            mFile = file;
            mLanguage = language;
        }

        @Override
        public int hashCode()
        {
            int result = 31 + (mFile == null?0:mFile.hashCode());
            result = 31 * result + (mLanguage == null?0:mLanguage.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj != this) {
                if(obj instanceof PreviewCacheKey) {
                    return Common.objectsAreEqual(mFile,((PreviewCacheKey)obj).mFile) &&
                    Common.objectsAreEqual(mLanguage,((PreviewCacheKey)obj).mLanguage);
                }
                return false;
            }
            return true;
        }
    }
}