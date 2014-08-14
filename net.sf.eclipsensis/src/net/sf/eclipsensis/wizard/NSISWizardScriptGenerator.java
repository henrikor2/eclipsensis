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

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.Pattern;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.editor.*;
import net.sf.eclipsensis.help.*;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.script.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class NSISWizardScriptGenerator implements INSISWizardConstants
{
    private static final String MUI_ITEM_PREFIX = "MUI_"; //$NON-NLS-1$
    private static final int BEGIN_TASK = 1;
    private static final int SET_TASK_NAME = 2;
    private static final String cUninstallRegKey = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\$(^Name)"; //$NON-NLS-1$
    protected static ICommandService cCommandService = (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);
    protected static IHandlerService cHandlerService = (IHandlerService)PlatformUI.getWorkbench().getAdapter(IHandlerService.class);

    private static Map<String, Pattern> cReservedSubKeysMap = new CaseInsensitiveMap<Pattern>();

    private boolean mNewRmDirUsage = false;

    private NSISWizardSettings mSettings = null;
    private PrintWriter mWriter = null;
    private IProgressMonitor mMonitor = null;
    private File mSaveFile;
    private String mNsisDirKeyword;

    private NSISScript mScript;
    private List<NSISScriptSection> mUnSectionList;
    private NSISScriptFunction mOnInitFunction;
    private NSISScriptFunction mUnOnInitFunction;
    private int mSectionCounter = 0;
    private int mSectionGroupCounter = 0;
    private INSISScriptElement mSectionsPlaceHolder;
    private INSISScriptElement mUnsectionsPlaceHolder = null;
    private List<String> mIncludes = new ArrayList<String>();
    private List<String> mVars = new ArrayList<String>();
    private List<String> mReservedFiles = new ArrayList<String>();
    private Map<String,String> mKeywordCache = new HashMap<String,String>();
    private boolean mCreatedSMGroupShortcutFunctions = false;
    private INSISScriptElement mFunctionsPlaceHolder;
    private INSISScriptElement mUnfunctionsPlaceHolder = null;
    private boolean mIsSilent = false;
    private boolean mIsMUI = false;
    private boolean mIsMultiUser = false;
    private String mMUIHeader = null;

    static {
        for (int i = 0; i < NSISWizardDisplayValues.HKEY_NAMES.length; i++) {
            String pattern = EclipseNSISPlugin.getResourceString(NSISWizardDisplayValues.HKEY_NAMES[i].toLowerCase()+".reserved.subkeys",null); //$NON-NLS-1$
            if(pattern != null) {
                cReservedSubKeysMap.put(NSISWizardDisplayValues.HKEY_NAMES[i], Pattern.compile(pattern,Pattern.CASE_INSENSITIVE));
            }
        }
    }

    public NSISWizardScriptGenerator(NSISWizardSettings settings)
    {
        mSettings = settings;
        String usage = NSISUsageProvider.getInstance().getUsage(getKeyword("RmDir")); //$NON-NLS-1$
        if(!Common.isEmpty(usage)) {
            usage = usage.toUpperCase();
            String search = new StringBuffer(getKeyword("/r")).append("|").append( //$NON-NLS-1$ //$NON-NLS-2$
                    getKeyword("/REBOOTOK")).toString().toUpperCase(); //$NON-NLS-1$
            mNewRmDirUsage = usage.indexOf(search) < 0;
        }
    }

    private void updateMonitorTask(String resource, Object arg, int flag)
    {
        if(mMonitor != null) {
            String message = EclipseNSISPlugin.getResourceString(resource);
            if(arg != null) {
                Object[] args;
                if(arg.getClass().isArray()) {
                    args = (Object[])arg;
                }
                else {
                    args = new Object[]{arg};
                }
                message = MessageFormat.format(message,args);
            }
            switch(flag) {
                case BEGIN_TASK:
                    mMonitor.beginTask(message,3);
                    break;
                case SET_TASK_NAME:
                    mMonitor.setTaskName(message);
                    break;
            }
        }
    }

    private void incrementMonitor(int work)
    {
        if(mMonitor != null) {
            mMonitor.worked(work);
        }
    }

    public synchronized void generate(Shell shell, IProgressMonitor monitor) throws CoreException, IOException
    {
        try {
            mMonitor = monitor;
            String savePath = mSettings.getSavePath();
            updateMonitorTask("scriptgen.create.message",savePath,BEGIN_TASK); //$NON-NLS-1$

            Runnable runnable = new Runnable() {
                public void run()
                {
                    writeScript();
                }
            };

            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            final IFile file;

            if(mSettings.isSaveExternal()) {
                file = null;
                mSaveFile = new File(savePath);
                mWriter = new PrintWriter(new BufferedWriter(new FileWriter(mSaveFile)));
                runnable.run();

                IFile[] files = root.findFilesForLocationURI(mSaveFile.toURI());
                if(!Common.isEmptyArray(files)) {
                    for (int i = 0; i < files.length; i++) {
                        files[i].refreshLocal(IResource.DEPTH_ZERO, null);
                    }
                }
            }
            else {
                file = root.getFile(new Path(savePath));
                mSaveFile = new File(file.getLocation().toOSString());
                PipedOutputStream pos = new PipedOutputStream();
                mWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pos)));
                InputStream is = new BufferedInputStream(new PipedInputStream(pos));

                new Thread(runnable,EclipseNSISPlugin.getResourceString("wizard.script.generator.thread.name")).start(); //$NON-NLS-1$
                file.create(is,true,null);
                new NSISTaskTagUpdater().updateTaskTags(file);
            }

            IOUtility.closeIO(mWriter);
            mWriter = null;
            incrementMonitor(1);

            updateMonitorTask("scriptgen.open.message",savePath,SET_TASK_NAME); //$NON-NLS-1$
            shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try {
                        IEditorInput input;
                        if(mSettings.isSaveExternal()) {
                            input = new NSISExternalFileEditorInput(mSaveFile);
                        }
                        else {
                            input = new FileEditorInput(file);
                        }
                        IDE.openEditor(page, input, INSISConstants.EDITOR_ID, true);
                    }
                    catch (PartInitException e) {
                    }
                }
            });
            incrementMonitor(1);
            if(mSettings.isCompileScript()) {
                updateMonitorTask("scriptgen.compile.message",savePath,SET_TASK_NAME); //$NON-NLS-1$
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        NSISCompileTestUtility.INSTANCE.compile(file==null?new Path(mSaveFile.toString()):file.getFullPath(),mSettings.isTestScript());
                    }
                });
                incrementMonitor(1);
            }
        }
        finally {
            mMonitor.done();
            mMonitor = null;
            IOUtility.closeIO(mWriter);
        }
    }

    private String maybeMakeRelative(File reference, String pathname)
    {
        if(pathname.toUpperCase().startsWith(mNsisDirKeyword)) {
            return Common.quote(pathname);
        }
        else {
            if(!Common.isEmpty(pathname) && mSettings.isMakePathsRelative()) {
                return IOUtility.makeRelativeLocation(reference,pathname);
            }
        }
        return pathname;
    }

    private void writeScript()
    {
        String defaultTaskTag = ""; //$NON-NLS-1$
        Collection<NSISTaskTag> taskTags = NSISPreferences.getInstance().getTaskTags();
        for (Iterator<NSISTaskTag> iter = taskTags.iterator(); iter.hasNext();) {
            NSISTaskTag taskTag = iter.next();
            if(taskTag.isDefault()) {
                defaultTaskTag = taskTag.getTag();
                break;
            }
        }
        mNsisDirKeyword = getKeyword("${NSISDIR}").toUpperCase(); //$NON-NLS-1$
        switch(mSettings.getInstallerType()) {
            case INSTALLER_TYPE_SILENT:
                mIsSilent = true;
                break;
            case INSTALLER_TYPE_MUI:
                mIsMUI = true;
                mMUIHeader = "MUI.nsh"; //$NON-NLS-1$
                break;
            case INSTALLER_TYPE_MUI2:
                mIsMUI = true;
                if(mSettings.isMultiUserInstallation()) {
                    mIsMultiUser = true;
                }
                mMUIHeader = "MUI2.nsh"; //$NON-NLS-1$
                break;
        }

        List<NSISLanguage> languages = mSettings.getLanguages();
        mScript = new NSISScript(mSettings.getName());

        if(mSettings.getCompressorType() != MakeNSISRunner.COMPRESSOR_DEFAULT) {
            Object args;
            String solidKeyword = NSISKeywords.getInstance().getKeyword("/SOLID"); //$NON-NLS-1$
            if(NSISKeywords.getInstance().isValidKeyword(solidKeyword) && mSettings.isSolidCompression()) {
                args = new String[]{solidKeyword,MakeNSISRunner.COMPRESSOR_NAME_ARRAY[mSettings.getCompressorType()]};
            }
            else {
                args = MakeNSISRunner.COMPRESSOR_NAME_ARRAY[mSettings.getCompressorType()];
            }
            mScript.addElement(new NSISScriptAttribute("SetCompressor",args)); //$NON-NLS-1$
            mScript.addElement(new NSISScriptBlankLine());
        }
        if(!mIsMultiUser && mSettings.getExecutionLevel() != EXECUTION_LEVEL_NONE) {
            String execLevel;
            switch(mSettings.getExecutionLevel()) {
                case EXECUTION_LEVEL_ADMIN:
                    execLevel = "admin"; //$NON-NLS-1$
                    break;
                case EXECUTION_LEVEL_USER:
                    execLevel = "user"; //$NON-NLS-1$
                    break;
                default:
                    execLevel = "highest"; //$NON-NLS-1$
            }
            mScript.addElement(new NSISScriptAttribute("RequestExecutionLevel", execLevel)); //$NON-NLS-1$
            mScript.addElement(new NSISScriptBlankLine());
        }
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.defines.comment"))); //$NON-NLS-1$
        INSISScriptElement definesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("REGKEY",Common.quote("SOFTWARE\\$(^Name)"))); //$NON-NLS-1$ //$NON-NLS-2$

        if(mIsMultiUser) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString(EclipseNSISPlugin.getResourceString("scriptgen.multi.user.defines.comment")))); //$NON-NLS-1$

            String execLevel;
            switch(mSettings.getMultiUserExecLevel()) {
                case MULTIUSER_EXEC_LEVEL_ADMIN:
                    execLevel = "Admin"; //$NON-NLS-1$
                    break;
                case MULTIUSER_EXEC_LEVEL_POWER:
                    execLevel = "Power"; //$NON-NLS-1$
                    break;
                case MULTIUSER_EXEC_LEVEL_HIGHEST:
                    execLevel = "Highest"; //$NON-NLS-1$
                    break;
                default:
                    execLevel = "Standard"; //$NON-NLS-1$
            }
            mScript.addElement(new NSISScriptDefine("MULTIUSER_EXECUTIONLEVEL",execLevel)); //$NON-NLS-1$
            if(!mSettings.isCreateUninstaller()) {
                mScript.addElement(new NSISScriptDefine("MULTIUSER_NOUNINSTALL")); //$NON-NLS-1$
            }
            if(mSettings.getMultiUserExecLevel() > MULTIUSER_EXEC_LEVEL_STANDARD) {
                if(mSettings.getMultiUserInstallMode() == MULTIUSER_INSTALL_MODE_CURRENTUSER) {
                    mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_DEFAULT_CURRENTUSER")); //$NON-NLS-1$
                }
                if(mSettings.isMultiUserInstallModeAsk()) {
                    mScript.addElement(new NSISScriptDefine("MULTIUSER_MUI")); //$NON-NLS-1$
                }
                if(mSettings.isMultiUserInstallModeRemember()) {
                    mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_KEY",Common.quote("${REGKEY}"))); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_VALUENAME","MultiUserInstallMode")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_COMMANDLINE")); //$NON-NLS-1$
            mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_INSTDIR", mSettings.getInstallDir())); //$NON-NLS-1$
            mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_KEY", Common.quote("${REGKEY}"))); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.addElement(new NSISScriptDefine("MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_VALUE", Common.quote("Path"))); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.addElement(new NSISScriptBlankLine());
            mIncludes.add("MultiUser.nsh"); //$NON-NLS-1$
        }
        INSISScriptElement muiDefsPlaceHolder = null;
        if(mIsMUI) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.muidefs.comment"))); //$NON-NLS-1$
            muiDefsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        }
        INSISScriptElement includePlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        INSISScriptElement reservedFilesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        INSISScriptElement varsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.pages.comment"))); //$NON-NLS-1$
        INSISScriptElement pagesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.attributes.comment"))); //$NON-NLS-1$
        INSISScriptElement attributesPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.sections.comment"))); //$NON-NLS-1$
        mSectionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        if(mSettings.isCreateUninstaller()) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.unsections.comment"))); //$NON-NLS-1$
            mUnsectionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        }
        mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.functions.comment"))); //$NON-NLS-1$
        mFunctionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        if(mSettings.isCreateUninstaller()) {
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.unfunctions.comment"))); //$NON-NLS-1$
            mUnfunctionsPlaceHolder = mScript.addElement(new NSISScriptBlankLine());
        }

        mIncludes.add("Sections.nsh"); //$NON-NLS-1$
        if(mIsMUI) {
            mIncludes.add(mMUIHeader);
        }

        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("OutFile",maybeMakeRelative(mSaveFile,mSettings.getOutFile()))); //$NON-NLS-1$

        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("InstallDir",mSettings.getInstallDir())); //$NON-NLS-1$
        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("CRCCheck",getKeyword("on"))); //$NON-NLS-1$ //$NON-NLS-2$
        mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("XPStyle",getKeyword("on"))); //$NON-NLS-1$ //$NON-NLS-2$

        String icon = maybeMakeRelative(mSaveFile,mSettings.getIcon());
        if(!Common.isEmpty(icon)) {
            if(mIsMUI) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_ICON",icon)); //$NON-NLS-1$
            }
            else {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("Icon",icon)); //$NON-NLS-1$
            }
        }

        if(!mIsSilent) {
            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("ShowInstDetails",getKeyword((mSettings.isShowInstDetails()?"show":"hide")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(!mSettings.isAutoCloseInstaller()) {
                if(mIsMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_NOAUTOCLOSE")); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("AutoCloseWindow",getKeyword("false"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            else if (!mIsMUI) {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("AutoCloseWindow",getKeyword("true"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else {
            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("SilentInstall",getKeyword("silent"))); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if(mIsMUI) {
            mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_WELCOME")); //$NON-NLS-1$
            if(mSettings.isShowLicense()) {
                int licenseButtonType = mSettings.getLicenseButtonType();
                switch(licenseButtonType) {
                    case LICENSE_BUTTON_CHECKED:
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LICENSEPAGE_CHECKBOX")); //$NON-NLS-1$
                        break;
                    case LICENSE_BUTTON_RADIO:
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LICENSEPAGE_RADIOBUTTONS")); //$NON-NLS-1$
                        break;
                }
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_LICENSE",new String[]{maybeMakeRelative(mSaveFile,mSettings.getLicenseData())})); //$NON-NLS-1$
            }
            if(mSettings.isSelectComponents()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_COMPONENTS")); //$NON-NLS-1$
            }
            if(mIsMultiUser && mSettings.getMultiUserExecLevel() > MULTIUSER_EXEC_LEVEL_STANDARD && mSettings.isMultiUserInstallModeAsk()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MULTIUSER_PAGE_INSTALLMODE")); //$NON-NLS-1$
            }
            if(mSettings.isChangeInstallDir()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_DIRECTORY")); //$NON-NLS-1$
            }
            if(mSettings.isCreateStartMenuGroup()) {
                mVars.add("StartMenuGroup"); //$NON-NLS-1$
                if(mSettings.isChangeStartMenuGroup()) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_REGISTRY_ROOT",getKeyword("HKLM"))); //$NON-NLS-1$ //$NON-NLS-2$
                    if(!mSettings.isDisableStartMenuShortcuts()) {
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_NODISABLE")); //$NON-NLS-1$
                    }
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_REGISTRY_KEY","${REGKEY}")); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_REGISTRY_VALUENAME","StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_STARTMENUPAGE_DEFAULTFOLDER",mSettings.getStartMenuGroup())); //$NON-NLS-1$
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_STARTMENU",new String[]{"Application","$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_INSTFILES")); //$NON-NLS-1$
            if(!Common.isEmpty(mSettings.getRunProgramAfterInstall())) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_RUN",mSettings.getRunProgramAfterInstall())); //$NON-NLS-1$
                if(!Common.isEmpty(mSettings.getRunProgramAfterInstallParams())) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_RUN_PARAMETERS",Common.escapeQuotes(mSettings.getRunProgramAfterInstallParams()))); //$NON-NLS-1$
                }
            }
            if(!Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_FINISHPAGE_SHOWREADME",mSettings.getOpenReadmeAfterInstall())); //$NON-NLS-1$
            }
            mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_PAGE_FINISH")); //$NON-NLS-1$

            if(mSettings.isCreateUninstaller()) {
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_UNPAGE_CONFIRM")); //$NON-NLS-1$
                mScript.insertElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_UNPAGE_INSTFILES")); //$NON-NLS-1$
            }
        }
        else {
            if(!mIsSilent) {
                if(mSettings.isShowLicense()) {
                    int licenseButtonType = mSettings.getLicenseButtonType();
                    switch(licenseButtonType) {
                        case LICENSE_BUTTON_CHECKED:
                            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("LicenseForceSelection",getKeyword("checkbox"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                        case LICENSE_BUTTON_RADIO:
                            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("LicenseForceSelection",getKeyword("radiobuttons"))); //$NON-NLS-1$ //$NON-NLS-2$
                            break;
                    }
                    mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("LicenseData",maybeMakeRelative(mSaveFile,mSettings.getLicenseData()))); //$NON-NLS-1$
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("license"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(mSettings.isSelectComponents()) {
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("components"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(mSettings.isChangeInstallDir()) {
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("directory"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            if(mSettings.isCreateStartMenuGroup()) {
                mVars.add("StartMenuGroup"); //$NON-NLS-1$
                if(!mIsSilent && mSettings.isChangeStartMenuGroup()) {
                    mReservedFiles.add("StartMenu.dll"); //$NON-NLS-1$
                    mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",new String[]{getKeyword("custom"),"StartMenuGroupSelect","", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            Common.quote((mSettings.isEnableLanguageSupport()?": $(StartMenuPageTitle)": //$NON-NLS-1$
                                MessageFormat.format(": {0}",new Object[]{ //$NON-NLS-1$
                                        EclipseNSISPlugin.getResourceString("scriptgen.start.menu.page.title")})))})); //$NON-NLS-1$
                    NSISScriptFunction fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction("StartMenuGroupSelect")); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Push",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$

                    String[] args = new String[]{"/autoadd","/text", //$NON-NLS-1$ //$NON-NLS-2$
                            Common.quote(mSettings.isEnableLanguageSupport()?"$(StartMenuPageText)": //$NON-NLS-1$
                                EclipseNSISPlugin.getResourceString("scriptgen.start.menu.page.text")), //$NON-NLS-1$
                                "/lastused","$StartMenuGroup",mSettings.getStartMenuGroup()}; //$NON-NLS-1$ //$NON-NLS-2$
                    if(mSettings.isDisableStartMenuShortcuts()) {
                        args = (String[])Common.joinArrays(new Object[] {new String[] {"/checknoshortcuts", //$NON-NLS-1$
                                Common.quote(mSettings.isEnableLanguageSupport()?"$(DisableStartMenuShortcutsText)": //$NON-NLS-1$
                                    EclipseNSISPlugin.getResourceString("scriptgen.disable.start.menu.shortcuts.text"))}, args}); //$NON-NLS-1$
                    }
                    fn.addElement(new NSISScriptInstruction("StartMenu::Select",args)); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptInstruction("StrCmp",new String[]{getKeyword("$R1"),"success","success"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    fn.addElement(new NSISScriptInstruction("StrCmp",new String[]{getKeyword("$R1"),"cancel","done"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    fn.addElement(new NSISScriptInstruction("MessageBox",new String[]{getKeyword("MB_OK"),getKeyword("$R1")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    fn.addElement(new NSISScriptInstruction("Goto","done")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptLabel("success")); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Pop","$StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn.addElement(new NSISScriptLabel("done")); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());
                }
            }

            mScript.insertElement(pagesPlaceHolder,new NSISScriptAttribute("Page",getKeyword("instfiles"))); //$NON-NLS-1$ //$NON-NLS-2$
            if(!Common.isEmpty(mSettings.getRunProgramAfterInstall()) ||
                    !Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                mScript.addElement(new NSISScriptBlankLine());
                NSISScriptFunction fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction(getKeyword(".onInstSuccess"))); //$NON-NLS-1$
                if(!Common.isEmpty(mSettings.getRunProgramAfterInstall())) {
                    StringBuffer buf = new StringBuffer("$\\\"").append( //$NON-NLS-1$
                            mSettings.getRunProgramAfterInstall()).append(
                            "$\\\""); //$NON-NLS-1$
                    if(!Common.isEmpty(mSettings.getRunProgramAfterInstallParams())) {
                        buf.append(" ").append(Common.escapeQuotes(mSettings.getRunProgramAfterInstallParams())); //$NON-NLS-1$
                    }
                    fn.addElement(new NSISScriptInstruction("Exec",buf.toString())); //$NON-NLS-1$
                    mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());
                }
                if(!Common.isEmpty(mSettings.getOpenReadmeAfterInstall())) {
                    fn.addElement(new NSISScriptInstruction("ExecShell",new String[]{"open",mSettings.getOpenReadmeAfterInstall()})); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        if(!mIsSilent && mSettings.isShowBackground()) {
            String backgroundBMP = mSettings.getBackgroundBMP();
            String backgroundWAV = mSettings.getBackgroundWAV();
            RGB topColor = mSettings.getBGTopColor();
            RGB bottomColor = mSettings.getBGBottomColor();
            RGB textColor = mSettings.getBGTextColor();
            if(Common.isEmpty(backgroundBMP)) {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("BGGradient", //$NON-NLS-1$
                        new String[] {
                        ColorManager.rgbToHex(topColor),
                        ColorManager.rgbToHex(bottomColor),
                        ColorManager.rgbToHex(textColor)}));
                if(!Common.isEmpty(backgroundWAV)) {
                    mReservedFiles.add("BGImage.dll"); //$NON-NLS-1$
                    NSISScriptFunction fn;
                    if(mIsMUI) {
                        mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_CUSTOMFUNCTION_GUIINIT","CustomGUIInit")); //$NON-NLS-1$ //$NON-NLS-2$
                        fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction("CustomGUIInit")); //$NON-NLS-1$
                    }
                    else {
                        fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIInit"))); //$NON-NLS-1$
                    }
                    fn.addElement(new NSISScriptInstruction("File",new String[]{new StringBuffer(getKeyword("/oname")).append( //$NON-NLS-1$ //$NON-NLS-2$
                    "=").append(getKeyword("$PLUGINSDIR")).append("\\bgimage.wav").toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    maybeMakeRelative(mSaveFile,backgroundWAV)}));
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound",new String[]{getKeyword("/NOUNLOAD"), //$NON-NLS-1$ //$NON-NLS-2$
                            "/LOOP", //$NON-NLS-1$
                            getKeyword("$PLUGINSDIR")+"\\bgimage.wav"})); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());

                    fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIEnd"))); //$NON-NLS-1$
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound","/STOP")); //$NON-NLS-1$ //$NON-NLS-2$
                    mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());
                }
            }
            else {
                mReservedFiles.add("BGImage.dll"); //$NON-NLS-1$
                NSISScriptFunction fn;
                if(mIsMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_CUSTOMFUNCTION_GUIINIT","CustomGUIInit")); //$NON-NLS-1$ //$NON-NLS-2$
                    fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction("CustomGUIInit")); //$NON-NLS-1$
                }
                else {
                    fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIInit"))); //$NON-NLS-1$
                }
                fn.addElement(new NSISScriptInstruction("Push",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Push",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("BgImage::SetReturn",new String[]{getKeyword("/NOUNLOAD"),"on"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fn.addElement(new NSISScriptInstruction("BgImage::SetBg",new String[]{getKeyword("/NOUNLOAD"),"/GRADIENT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Integer.toString(topColor.red), Integer.toString(topColor.green),
                        Integer.toString(topColor.blue), Integer.toString(bottomColor.red),
                        Integer.toString(bottomColor.green), Integer.toString(bottomColor.blue)}));
                fn.addElement(new NSISScriptInstruction("Pop","$R1")); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Strcmp",new String[]{getKeyword("$R1"),"success","0","error"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fn.addElement(new NSISScriptInstruction("File",new String[]{new StringBuffer(getKeyword("/oname")).append( //$NON-NLS-1$ //$NON-NLS-2$
                "=").append(getKeyword("$PLUGINSDIR")).append("\\bgimage.bmp").toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                maybeMakeRelative(mSaveFile,backgroundBMP)}));
                fn.addElement(new NSISScriptInstruction("System::call","user32::GetSystemMetrics(i 0)i.R1")); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("System::call","user32::GetSystemMetrics(i 1)i.R2")); //$NON-NLS-1$ //$NON-NLS-2$
                ImageData imageData = new ImageData(IOUtility.decodePath(backgroundBMP));
                fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R1"),getKeyword("$R1"),"-",Integer.toString(imageData.width)})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R1"),getKeyword("$R1"),"/","2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R2"),getKeyword("$R2"),"-",Integer.toString(imageData.height)})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                fn.addElement(new NSISScriptInstruction("IntOp",new String[]{getKeyword("$R2"),getKeyword("$R2"),"/","2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fn.addElement(new NSISScriptInstruction("BGImage::AddImage",new String[]{getKeyword("/NOUNLOAD"), //$NON-NLS-1$ //$NON-NLS-2$
                        getKeyword("$PLUGINSDIR")+"\\bgimage.bmp",getKeyword("$R1"),getKeyword("$R2")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                fn.addElement(new NSISScriptInstruction("CreateFont",new String[]{getKeyword("$R1"),"Times New Roman","26","700",getKeyword("/ITALIC")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                fn.addElement(new NSISScriptInstruction("BGImage::AddText",new String[]{getKeyword("/NOUNLOAD"),Common.quote("$(^SetupCaption)"),getKeyword("$R1"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        Integer.toString(textColor.red), Integer.toString(textColor.green),
                        Integer.toString(textColor.blue),"16","8","500","100"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Strcmp",new String[]{getKeyword("$R1"),"success","0","error"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                fn.addElement(new NSISScriptInstruction("BGImage::Redraw",getKeyword("/NOUNLOAD"))); //$NON-NLS-1$ //$NON-NLS-2$
                if(!Common.isEmpty(backgroundWAV)) {
                    fn.addElement(new NSISScriptInstruction("File",new String[]{new StringBuffer(getKeyword("/oname")).append( //$NON-NLS-1$ //$NON-NLS-2$
                    "=").append(getKeyword("$PLUGINSDIR")).append("\\bgimage.wav").toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    maybeMakeRelative(mSaveFile,backgroundWAV)}));
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound",new String[]{getKeyword("/NOUNLOAD"), //$NON-NLS-1$ //$NON-NLS-2$
                            "/LOOP",getKeyword("$PLUGINSDIR")+"\\bgimage.wav"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                fn.addElement(new NSISScriptInstruction("Goto","done")); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptLabel("error")); //$NON-NLS-1$
                fn.addElement(new NSISScriptInstruction("MessageBox",new String[]{new StringBuffer(getKeyword("MB_OK")).append( //$NON-NLS-1$ //$NON-NLS-2$
                "|").append(getKeyword("MB_ICONSTOP")).toString(),getKeyword("$R1")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                fn.addElement(new NSISScriptLabel("done")); //$NON-NLS-1$
                fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
                fn.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());

                fn = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction(getKeyword(".onGUIEnd"))); //$NON-NLS-1$
                if(!Common.isEmpty(backgroundWAV)) {
                    fn.addElement(new NSISScriptInstruction("BGImage::Sound",new String[]{getKeyword("/NOUNLOAD"),"/STOP"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                fn.addElement(new NSISScriptInstruction("BGImage::Destroy")); //$NON-NLS-1$
                mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());
            }
        }

        NSISLanguage defaultLanguage = null;
        if(mSettings.isEnableLanguageSupport()) {
            if(!mIsSilent && mSettings.isSelectLanguage() && languages.size() > 1) {
                if(mIsMUI) {
                    mReservedFiles.add("MUI_RESERVEFILE_LANGDLL"); //$NON-NLS-1$
                }
                else {
                    mReservedFiles.add("LangDLL.dll"); //$NON-NLS-1$
                }
            }

            INSISScriptElement languagesPlaceHolder = mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptBlankLine());
            mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.languages.comment"))); //$NON-NLS-1$
            defaultLanguage = languages.get(0);
            for (Iterator<NSISLanguage> iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage language = iter.next();
                if(mIsMUI) {
                    mScript.insertElement(languagesPlaceHolder,new NSISScriptInsertMacro("MUI_LANGUAGE",language.getName())); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(languagesPlaceHolder,new NSISScriptAttribute("LoadLanguageFile", //$NON-NLS-1$
                            new StringBuffer(mNsisDirKeyword).append("\\").append(INSISConstants.LANGUAGE_FILES_LOCATION).append( //$NON-NLS-1$
                            "\\").append(language.getName()).append(INSISConstants.LANGUAGE_FILES_EXTENSION).toString())); //$NON-NLS-1$
                }
            }
        }
        else {
            defaultLanguage = NSISLanguageManager.getInstance().getDefaultLanguage();
            if(mIsMUI) {
                mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptBlankLine());
                mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptInsertMacro("MUI_LANGUAGE",defaultLanguage.getName())); //$NON-NLS-1$
                mScript.insertAfterElement(pagesPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.languages.comment"))); //$NON-NLS-1$
            }
        }

        String version = mSettings.getVersion();
        if(!Common.isEmpty(version)) {
            mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("VERSION",version)); //$NON-NLS-1$
            mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("COMPANY",mSettings.getCompany())); //$NON-NLS-1$
            mScript.insertElement(definesPlaceHolder,new NSISScriptDefine("URL",mSettings.getUrl())); //$NON-NLS-1$

            int[] numbers = new Version(version).getNumbers();
            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
            int i = 0;
            for (; i < Math.min(numbers.length,4); i++) {
                buf.append((i>0?".":"")).append(numbers[i]); //$NON-NLS-1$ //$NON-NLS-2$
            }
            for(int j=i; j<4; j++) {
                buf.append((j>0?".0":"0")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String langId=mSettings.isEnableLanguageSupport()?new StringBuffer(getKeyword("/LANG")).append("=").append(defaultLanguage.getLangDef()).toString():null; //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("VIProductVersion",buf.toString())); //$NON-NLS-1$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"ProductName",mSettings.getName()})); //$NON-NLS-1$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId,new String[]{"ProductVersion",Common.quote("${VERSION}")})); //$NON-NLS-1$ //$NON-NLS-2$
            if(!Common.isEmpty(mSettings.getCompany())) {
                mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"CompanyName",Common.quote("${COMPANY}")})); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(!Common.isEmpty(mSettings.getUrl())) {
                mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"CompanyWebsite",Common.quote("${URL}")})); //$NON-NLS-1$ //$NON-NLS-2$
            }
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"FileVersion",Common.quote("${VERSION}")})); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"FileDescription",""})); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(attributesPlaceHolder,createVersionInfoKey(langId, new String[]{"LegalCopyright",""})); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if(!mIsSilent && mSettings.isShowSplash()) {
            if(mSettings.getFadeInDelay() > 0 || mSettings.getFadeOutDelay() > 0) {
                mReservedFiles.add("AdvSplash.dll"); //$NON-NLS-1$
            }
            else {
                mReservedFiles.add("Splash.dll"); //$NON-NLS-1$
            }
        }

        mOnInitFunction = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptFunction(getKeyword(".onInit"))); //$NON-NLS-1$
        mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());
        mOnInitFunction.addElement(new NSISScriptInstruction("InitPluginsDir")); //$NON-NLS-1$
        if(mSettings.isCreateStartMenuGroup() && (mIsSilent || !mSettings.isChangeStartMenuGroup())) {
            mOnInitFunction.addElement(new NSISScriptInstruction("StrCpy",new String[]{"$StartMenuGroup", mSettings.getStartMenuGroup()})); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if(!mIsSilent) {
            if(mSettings.isShowSplash()) {
                mOnInitFunction.addElement(new NSISScriptInstruction("Push",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                mOnInitFunction.addElement(new NSISScriptInstruction("File", //$NON-NLS-1$
                        new String[]{new StringBuffer(getKeyword("/oname")).append("=").append( //$NON-NLS-1$ //$NON-NLS-2$
                                getKeyword("$PLUGINSDIR")).append("\\spltmp.bmp").toString(), //$NON-NLS-1$ //$NON-NLS-2$
                                maybeMakeRelative(mSaveFile,mSettings.getSplashBMP())}));
                if(!Common.isEmpty(mSettings.getSplashWAV())) {
                    mOnInitFunction.addElement(new NSISScriptInstruction("File", //$NON-NLS-1$
                            new String[]{new StringBuffer(getKeyword("/oname")).append("=").append( //$NON-NLS-1$ //$NON-NLS-2$
                                    getKeyword("$PLUGINSDIR")).append("\\spltmp.wav").toString(), //$NON-NLS-1$ //$NON-NLS-2$
                                    maybeMakeRelative(mSaveFile,mSettings.getSplashWAV())}));
                }
                if(mSettings.getFadeInDelay() > 0 || mSettings.getFadeOutDelay() > 0) {
                    mOnInitFunction.addElement(new NSISScriptInstruction("advsplash::show",new String[]{ //$NON-NLS-1$
                            Integer.toString(mSettings.getSplashDelay()),
                            Integer.toString(mSettings.getFadeInDelay()),
                            Integer.toString(mSettings.getFadeOutDelay()),
                            "-1",getKeyword("$PLUGINSDIR")+"\\spltmp" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }));
                }
                else {
                    mOnInitFunction.addElement(new NSISScriptInstruction("splash::show", //$NON-NLS-1$
                            new String[]{
                            Integer.toString(mSettings.getSplashDelay()),
                            getKeyword("$PLUGINSDIR")+"\\spltmp" //$NON-NLS-1$ //$NON-NLS-2$
                    }));
                }
                mOnInitFunction.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                mOnInitFunction.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(mSettings.isEnableLanguageSupport() && mSettings.isSelectLanguage() && languages.size() > 1) {
                if(mIsMUI) {
                    if(NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_26) >= 0 &&
                       !mSettings.isDisplaySupportedLanguages()) {
                        mOnInitFunction.addElement(new NSISScriptDefine("MUI_LANGDLL_ALLLANGUAGES")); //$NON-NLS-1$
                    }
                    mOnInitFunction.addElement(new NSISScriptInsertMacro("MUI_LANGDLL_DISPLAY")); //$NON-NLS-1$
                }
                else {
                    boolean useCodePage = NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_26) >= 0 &&
                                              mSettings.isDisplaySupportedLanguages();
                    mOnInitFunction.addElement(new NSISScriptInstruction("Push","")); //$NON-NLS-1$ //$NON-NLS-2$
                    for (Iterator<NSISLanguage> iter = languages.iterator(); iter.hasNext();) {
                        NSISLanguage language = iter.next();
                        if(useCodePage)
                        {
                            mOnInitFunction.addElement(new NSISScriptInstruction("Push",language.getCodePage())); //$NON-NLS-1$
                        }
                        mOnInitFunction.addElement(new NSISScriptInstruction("Push",language.getLangDef())); //$NON-NLS-1$
                        mOnInitFunction.addElement(new NSISScriptInstruction("Push",language.getDisplayName())); //$NON-NLS-1$
                    }
                    mOnInitFunction.addElement(new NSISScriptInstruction("Push",useCodePage?"CA":"A")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    mOnInitFunction.addElement(new NSISScriptInstruction("LangDLL::LangDialog",new String[]{ //$NON-NLS-1$
                            EclipseNSISPlugin.getResourceString("scriptgen.langdialog.title"),EclipseNSISPlugin.getResourceString("scriptgen.langdialog.message")})); //$NON-NLS-1$ //$NON-NLS-2$
                    mOnInitFunction.addElement(new NSISScriptInstruction("Pop",getKeyword("$LANGUAGE"))); //$NON-NLS-1$ //$NON-NLS-2$
                    mOnInitFunction.addElement(new NSISScriptInstruction("StrCmp",new String[]{ //$NON-NLS-1$
                            getKeyword("$LANGUAGE"),Common.quote("cancel"),"0","+2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    mOnInitFunction.addElement(new NSISScriptInstruction("Abort")); //$NON-NLS-1$
                }
            }
            if(mIsMultiUser) {
                mOnInitFunction.addElement(new NSISScriptInsertMacro("MULTIUSER_INIT")); //$NON-NLS-1$
            }
        }

        NSISScriptSection postSection = null;
        NSISScriptSection unPostSection = null;
        mUnOnInitFunction = null;

        if(mSettings.isCreateUninstaller()) {
            postSection = new NSISScriptSection("post",false,true,false); //$NON-NLS-1$
            postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                    getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path",getKeyword("$INSTDIR")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if(!mIsMUI) {
                if(mSettings.isSelectLanguage() && languages.size() > 1) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                            getKeyword("HKLM"),Common.quote("${REGKEY}"),"InstallerLanguage",getKeyword("$LANGUAGE")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                if(mSettings.isCreateStartMenuGroup() && !mIsSilent && mSettings.isChangeStartMenuGroup()) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                            getKeyword("HKLM"),Common.quote("${REGKEY}"),"StartMenuGroup","$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
            }

            unPostSection = new NSISScriptSection("un.post",false,true,false); //$NON-NLS-1$
            mScript.insertAfterElement(mSectionsPlaceHolder,new NSISScriptBlankLine());
            NSISScriptMacro macro = (NSISScriptMacro)mScript.insertAfterElement(mSectionsPlaceHolder,new NSISScriptMacro("SELECT_UNSECTION",new String[]{"SECTION_NAME","UNSECTION_ID"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            macro.addElement(new NSISScriptInstruction("Push",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{getKeyword("$R0"),getKeyword("HKLM"),Common.quote("${REGKEY}\\Components"),Common.quote("${SECTION_NAME}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            macro.addElement(new NSISScriptInstruction("StrCmp",new String[]{"$R0","1","0","next${UNSECTION_ID}"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            macro.addElement(new NSISScriptInsertMacro("SelectSection",Common.quote("${UNSECTION_ID}"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("GoTo","done${UNSECTION_ID}")); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptLabel("next${UNSECTION_ID}")); //$NON-NLS-1$
            macro.addElement(new NSISScriptInsertMacro("UnselectSection",Common.quote("${UNSECTION_ID}"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptLabel("done${UNSECTION_ID}")); //$NON-NLS-1$
            macro.addElement(new NSISScriptInstruction("Pop",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertAfterElement(mSectionsPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.select.unsection.macro.comment"))); //$NON-NLS-1$

            unPostSection.addElement(new NSISScriptInstruction("RmDir",new String[]{getKeyword("/REBOOTOK"),getKeyword("$INSTDIR")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(mSettings.isCreateStartMenuGroup()) {
                boolean checkSMGroup = !mIsSilent && mSettings.isChangeStartMenuGroup() && mSettings.isDisableStartMenuShortcuts();
                if(checkSMGroup) {
                    unPostSection.addElement(new NSISScriptInstruction("Push",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
                    unPostSection.addElement(new NSISScriptInstruction("StrCpy",new String[] {getKeyword("$R0"),"$StartMenuGroup","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    unPostSection.addElement(new NSISScriptInstruction("StrCmp",new String[] {getKeyword("$R0"),Common.quote(">"),"no_smgroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                unPostSection.addElement(0,new NSISScriptInstruction("RmDir",new String[]{ //$NON-NLS-1$
                        getKeyword("/REBOOTOK"),getKeyword("$SMPROGRAMS")+"\\$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if(checkSMGroup) {
                    unPostSection.addElement(new NSISScriptLabel("no_smgroup")); //$NON-NLS-1$
                    unPostSection.addElement(new NSISScriptInstruction("Pop",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegKey",new String[]{getKeyword("/IfEmpty"), //$NON-NLS-1$ //$NON-NLS-2$
                    getKeyword("HKLM"),Common.quote("${REGKEY}")})); //$NON-NLS-1$ //$NON-NLS-2$
            unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegKey",new String[]{getKeyword("/IfEmpty"), //$NON-NLS-1$ //$NON-NLS-2$
                    getKeyword("HKLM"),Common.quote("${REGKEY}\\Components")})); //$NON-NLS-1$ //$NON-NLS-2$
            unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                    getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if(!mIsMUI) {
                if(mSettings.isSelectLanguage() && languages.size() > 1) {
                    unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                            getKeyword("HKLM"),Common.quote("${REGKEY}"),"InstallerLanguage"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            if(mSettings.isCreateStartMenuGroup() && !mIsSilent && mSettings.isChangeStartMenuGroup()) {
                unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                        getKeyword("HKLM"),Common.quote("${REGKEY}"),"StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("InstallDirRegKey",new String[]{getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            String unIcon = maybeMakeRelative(mSaveFile,mSettings.getUninstallIcon());
            if(!Common.isEmpty(unIcon)) {
                if(mIsMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_UNICON",unIcon)); //$NON-NLS-1$
                }
                else {
                    mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("UninstallIcon",unIcon)); //$NON-NLS-1$
                }
            }
            if(mSettings.isSilentUninstaller()) {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("SilentUnInstall", getKeyword("silent"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else {
                mScript.insertElement(attributesPlaceHolder,new NSISScriptAttribute("ShowUninstDetails",getKeyword((mSettings.isShowUninstDetails()?"show":"hide")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if(!mSettings.isAutoCloseUninstaller() && mIsMUI) {
                    mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_UNFINISHPAGE_NOAUTOCLOSE")); //$NON-NLS-1$
                }
            }
            if(mSettings.isEnableLanguageSupport()&& mIsMUI && mSettings.isSelectLanguage() &&
                    languages.size() > 1) {
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LANGDLL_REGISTRY_ROOT",getKeyword("HKLM"))); //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LANGDLL_REGISTRY_KEY","${REGKEY}")); //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(muiDefsPlaceHolder,new NSISScriptDefine("MUI_LANGDLL_REGISTRY_VALUENAME","InstallerLanguage")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            mUnOnInitFunction = (NSISScriptFunction)mScript.insertElement(mUnfunctionsPlaceHolder,new NSISScriptFunction(getKeyword("un.onInit"))); //$NON-NLS-1$
            mScript.insertElement(mUnfunctionsPlaceHolder,new NSISScriptBlankLine());

            if(!mSettings.isSilentUninstaller() && mSettings.isAutoCloseUninstaller()) {
                mUnOnInitFunction.addElement(new NSISScriptInstruction("SetAutoClose",getKeyword("true"))); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if(!mIsMultiUser) {
                mUnOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{getKeyword("$INSTDIR"),getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            }
            if(mSettings.isCreateStartMenuGroup()) {
                if(mIsSilent || !mSettings.isChangeStartMenuGroup()) {
                    mUnOnInitFunction.addElement(new NSISScriptInstruction("StrCpy",new String[]{"$StartMenuGroup", mSettings.getStartMenuGroup()})); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else {
                    if(mIsMUI) {
                        mUnOnInitFunction.addElement(new NSISScriptInsertMacro("MUI_STARTMENU_GETFOLDER",new String[] {"Application","$StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    else {
                        mUnOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{"$StartMenuGroup",getKeyword("HKLM"),Common.quote("${REGKEY}"),"StartMenuGroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    }
                }
            }
            if(mSettings.isEnableLanguageSupport()) {
                if(!mIsSilent && mSettings.isSelectLanguage() && languages.size() > 1) {
                    if(mIsMUI) {
                        mUnOnInitFunction.addElement(new NSISScriptInsertMacro("MUI_UNGETLANGUAGE")); //$NON-NLS-1$
                    }
                    else {
                        mUnOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{getKeyword("$LANGUAGE"),getKeyword("HKLM"),Common.quote("${REGKEY}"),"InstallerLanguage"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    }
                }
            }
            if(mIsMultiUser) {
                mUnOnInitFunction.addElement(new NSISScriptInsertMacro("MULTIUSER_UNINIT")); //$NON-NLS-1$
            }

            String uninstallFile = new StringBuffer(getKeyword("$INSTDIR")).append("\\").append(mSettings.getUninstallFile()).toString(); //$NON-NLS-1$ //$NON-NLS-2$
            postSection.addElement(new NSISScriptInstruction("SetOutPath",getKeyword("$INSTDIR"))); //$NON-NLS-1$ //$NON-NLS-2$
            postSection.addElement(new NSISScriptInstruction("WriteUninstaller",uninstallFile)); //$NON-NLS-1$
            unPostSection.addElement(0,new NSISScriptInstruction("Delete",new String[]{getKeyword("/REBOOTOK"),uninstallFile})); //$NON-NLS-1$ //$NON-NLS-2$

            if(mSettings.isCreateStartMenuGroup()) {
                if(mIsMUI && mSettings.isChangeStartMenuGroup()) {
                    postSection.addElement(new NSISScriptInsertMacro("MUI_STARTMENU_WRITE_BEGIN", "Application")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if(mSettings.isCreateUninstallerStartMenuShortcut()) {
                    String name = mSettings.isEnableLanguageSupport()?"$(^UninstallLink)": //$NON-NLS-1$
                        EclipseNSISPlugin.getResourceString("scriptgen.uninstall.link"); //$NON-NLS-1$
                    if (!mIsSilent && !mIsMUI && mSettings.isChangeStartMenuGroup() && mSettings.isDisableStartMenuShortcuts()) {
                        addSMGroupShortcutFunctions();
                        postSection.addElement(new NSISScriptInsertMacro("CREATE_SMGROUP_SHORTCUT", new String[]{name, uninstallFile})); //$NON-NLS-1$
                        unPostSection.addElement(0, new NSISScriptInsertMacro("DELETE_SMGROUP_SHORTCUT", name)); //$NON-NLS-1$
                    }
                    else {
                        postSection.addElement(new NSISScriptInstruction("SetOutPath",getKeyword("$SMPROGRAMS")+"\\$StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        String startMenuLink = Common.quote(new StringBuffer(getKeyword("$SMPROGRAMS")).append("\\$StartMenuGroup\\").append( //$NON-NLS-1$ //$NON-NLS-2$
                                name).append(".lnk").toString()); //$NON-NLS-1$
                        postSection.addElement(new NSISScriptInstruction("CreateShortcut",new String[]{startMenuLink,uninstallFile})); //$NON-NLS-1$

                        unPostSection.addElement(0,new NSISScriptInstruction("Delete",new String[]{getKeyword("/REBOOTOK"),startMenuLink})); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                if(mIsMUI && mSettings.isChangeStartMenuGroup()) {
                    postSection.addElement(new NSISScriptInsertMacro("MUI_STARTMENU_WRITE_END")); //$NON-NLS-1$
                }
            }

            if(mSettings.isCreateUninstallerControlPanelEntry()) {
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"DisplayName",Common.quote("$(^Name)")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                if(!Common.isEmpty(mSettings.getVersion())) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"DisplayVersion",Common.quote("${VERSION}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                if(!Common.isEmpty(mSettings.getCompany())) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"Publisher",Common.quote("${COMPANY}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                if(!Common.isEmpty(mSettings.getUrl())) {
                    postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"URLInfoAbout",Common.quote("${URL}")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"DisplayIcon",uninstallFile})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                postSection.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"UninstallString",uninstallFile})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                postSection.addElement(new NSISScriptInstruction("WriteRegDWORD",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"NoModify","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                postSection.addElement(new NSISScriptInstruction("WriteRegDWORD",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey),"NoRepair","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

                unPostSection.addElement(0,new NSISScriptInstruction("DeleteRegKey",new String[]{getKeyword("HKLM"),Common.quote(cUninstallRegKey)})); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        mUnSectionList = unPostSection == null?null:new ArrayList<NSISScriptSection>();
        INSISInstallElement[] contents =  mSettings.getInstaller().getChildren();
        mSectionCounter = 0;
        mSectionGroupCounter = 0;
        Map<String, String> secDescMap = mIsMUI?new LinkedHashMap<String,String>():null;
        for (int i = 0; i < contents.length; i++) {
            if(contents[i] instanceof NSISSection) {
                mScript.insertElement(mSectionsPlaceHolder, buildSection((NSISSection)contents[i], secDescMap));
            }
            else if(contents[i] instanceof NSISSectionGroup) {
                mScript.insertElement(mSectionsPlaceHolder, buildSectionGroup((NSISSectionGroup)contents[i], secDescMap));
            }
            mScript.insertElement(mSectionsPlaceHolder, new NSISScriptBlankLine());
        }
        if(mSettings.isCreateUninstaller()) {
            String sectionId = MessageFormat.format("SEC{0,number,0000}",new Object[]{new Integer(mSectionCounter++)}); //$NON-NLS-1$
            if (postSection != null) {
                postSection.setIndex(sectionId);
                mScript.insertElement(mSectionsPlaceHolder, postSection);
            }
            if(!Common.isEmptyCollection(mUnSectionList)) {
                Collections.reverse(mUnSectionList);
                for(Iterator<NSISScriptSection> iter=mUnSectionList.iterator(); iter.hasNext(); ) {
                    mScript.insertElement(mUnsectionsPlaceHolder,iter.next());
                    mScript.insertElement(mUnsectionsPlaceHolder, new NSISScriptBlankLine());
                }
            }
            if (unPostSection != null) {
                unPostSection.setIndex("UN" + sectionId); //$NON-NLS-1$
                mScript.insertElement(mUnsectionsPlaceHolder, unPostSection);
            }
        }

        if(secDescMap != null && secDescMap.size() > 0) {
            mScript.addElement(new NSISScriptBlankLine());
            mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.section.desc.comment"))); //$NON-NLS-1$
            mScript.addElement(new NSISScriptInsertMacro("MUI_FUNCTION_DESCRIPTION_BEGIN")); //$NON-NLS-1$
            for (Iterator<String> iter = secDescMap.keySet().iterator(); iter.hasNext();) {
                String id = iter.next();
                mScript.addElement(new NSISScriptInsertMacro("MUI_DESCRIPTION_TEXT", //$NON-NLS-1$
                        new String[] {new StringBuffer("${").append(id).append("}").toString(), //$NON-NLS-1$ //$NON-NLS-2$
                        mSettings.isEnableLanguageSupport()?
                                new StringBuffer("$(").append(id).append("_DESC)").toString(): //$NON-NLS-1$ //$NON-NLS-2$
                                    secDescMap.get(id)}));
            }
            mScript.addElement(new NSISScriptInsertMacro("MUI_FUNCTION_DESCRIPTION_END")); //$NON-NLS-1$
        }

        if(mSettings.isEnableLanguageSupport()) {
            Locale defaultLocale = NSISLanguageManager.getInstance().getDefaultLocale();
            ResourceBundle defaultBundle = EclipseNSISPlugin.getDefault().getResourceBundle(defaultLocale);
            NSISScriptlet smTitleScriptlet = new NSISScriptlet();
            NSISScriptlet smTextScriptlet = new NSISScriptlet();
            NSISScriptlet unlinkScriptlet = new NSISScriptlet();
            NSISScriptlet disableSMScriptlet = new NSISScriptlet();
            NSISScriptlet secDescScriptlet = new NSISScriptlet();
            for (Iterator<NSISLanguage> iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage language = iter.next();
                Locale locale = NSISLanguageManager.getInstance().getLocaleForLangId(language.getLangId());
                ResourceBundle bundle;
                if(locale.equals(defaultLocale)) {
                    bundle = defaultBundle;
                }
                else {
                    bundle = EclipseNSISPlugin.getDefault().getResourceBundle(locale);
                    if(!bundle.equals(defaultBundle) && !validateLocale(locale,bundle.getLocale())) {
                        bundle = defaultBundle;
                    }
                }
                if(mSettings.isCreateUninstaller() && mSettings.isCreateStartMenuGroup() && mSettings.isCreateUninstallerStartMenuShortcut()) {
                    unlinkScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                            new String[]{"^UninstallLink",language.getLangDef(), //$NON-NLS-1$
                            bundle.getString("scriptgen.uninstall.link")})); //$NON-NLS-1$
                }
                if(!mIsMUI && mSettings.isCreateStartMenuGroup() && !mIsSilent && mSettings.isChangeStartMenuGroup()) {
                    smTitleScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                            new String[]{"StartMenuPageTitle",language.getLangDef(), //$NON-NLS-1$
                            bundle.getString("scriptgen.start.menu.page.title")})); //$NON-NLS-1$
                    smTextScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                            new String[]{"StartMenuPageText",language.getLangDef(), //$NON-NLS-1$
                            bundle.getString("scriptgen.start.menu.page.text")})); //$NON-NLS-1$
                    if(mSettings.isDisableStartMenuShortcuts()) {
                        disableSMScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                                new String[]{"DisableStartMenuShortcutsText",language.getLangDef(), //$NON-NLS-1$
                                bundle.getString("scriptgen.disable.start.menu.shortcuts.text")})); //$NON-NLS-1$
                    }
                }
                if(secDescMap != null && secDescMap.size() > 0) {
                    if(secDescScriptlet.size() > 0) {
                        secDescScriptlet.addElement(new NSISScriptBlankLine());
                    }
                    for (Iterator<String> iter2 = secDescMap.keySet().iterator(); iter2.hasNext();) {
                        String id = iter2.next();
                        secDescScriptlet.addElement(new NSISScriptAttribute("LangString", //$NON-NLS-1$
                                new String[]{id+"_DESC",language.getLangDef(), //$NON-NLS-1$
                                secDescMap.get(id)}));
                    }
                }
            }
            if(smTitleScriptlet.size() > 0 || unlinkScriptlet.size() > 0 || smTextScriptlet.size() > 0
                    || disableSMScriptlet.size() > 0 || secDescScriptlet.size() > 0) {
                mScript.addElement(new NSISScriptBlankLine());
                mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.langstring.comment1"))); //$NON-NLS-1$
                mScript.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getFormattedString("scriptgen.langstring.comment2",new Object[]{defaultTaskTag}).trim())); //$NON-NLS-1$
                if(smTitleScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(smTitleScriptlet);
                }
                if(smTextScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(smTextScriptlet);
                }
                if(disableSMScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(disableSMScriptlet);
                }
                if(unlinkScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(unlinkScriptlet);
                }
                if(secDescScriptlet.size() > 0) {
                    mScript.addElement(new NSISScriptBlankLine());
                    mScript.append(secDescScriptlet);
                }
            }
        }

        if(mReservedFiles.size() > 0) {
            mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.reservedfiles.comment"))); //$NON-NLS-1$
            for (Iterator<String> iter = mReservedFiles.iterator(); iter.hasNext();) {
                String item = iter.next();
                if(item.startsWith(MUI_ITEM_PREFIX)) {
                    mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptInsertMacro(item));
                }
                else {
                    StringBuffer buf = new StringBuffer(mNsisDirKeyword).append("\\Plugins\\").append(item); //$NON-NLS-1$
                    mScript.insertElement(reservedFilesPlaceHolder,new NSISScriptAttribute("ReserveFile",Common.quote(buf.toString()))); //$NON-NLS-1$
                }
            }
        }
        else {
            mScript.remove(reservedFilesPlaceHolder);
        }

        if(mIncludes.size() > 0) {
            mScript.insertElement(includePlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.includes.comment"))); //$NON-NLS-1$
            for (Iterator<String> iter = mIncludes.iterator(); iter.hasNext();) {
                mScript.insertElement(includePlaceHolder,new NSISScriptInclude(iter.next()));
            }
        }
        else {
            mScript.remove(includePlaceHolder);
        }

        if(mVars.size() > 0) {
            mScript.insertElement(varsPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.variables.comment"))); //$NON-NLS-1$
            for (Iterator<String> iter = mVars.iterator(); iter.hasNext();) {
                mScript.insertElement(varsPlaceHolder,new NSISScriptAttribute("Var",iter.next())); //$NON-NLS-1$
            }
        }
        else {
            mScript.remove(varsPlaceHolder);
        }
        mScript.compact();

        mWriter.print(NSISScriptSingleLineComment.PREFIX_HASH+" "); //$NON-NLS-1$
        mWriter.println(EclipseNSISPlugin.getResourceString("scriptgen.header.comment")); //$NON-NLS-1$
        mWriter.print(NSISScriptSingleLineComment.PREFIX_HASH+" "); //$NON-NLS-1$
        mWriter.println(DateFormat.getDateTimeInstance().format(new Date()));
        mWriter.println();
        mScript.write(new NSISScriptWriter(mWriter));
        mWriter.flush();
        IOUtility.closeIO(mWriter);
        mWriter = null;
    }

    /**
     * @param langId
     * @param args
     */
    private NSISScriptAttribute createVersionInfoKey(String langId, String[] args)
    {
        String[] args2 = args;
        if(langId != null) {
            String[] temp = new String[Common.isEmptyArray(args2)?1:args2.length+1];
            temp[0]=langId;
            if(!Common.isEmptyArray(args2)) {
                System.arraycopy(args2,0,temp,1,args2.length);
            }
            args2 = temp;
        }
        return new NSISScriptAttribute("VIAddVersionKey",args2); //$NON-NLS-1$
    }

    private NSISScriptSectionGroup buildSectionGroup(NSISSectionGroup secGrp, Map<String, String> sectionDescMap)
    {
        String secGrpId = MessageFormat.format("SECGRP{0,number,0000}",new Object[]{new Integer(mSectionGroupCounter++)}); //$NON-NLS-1$
        NSISScriptSectionGroup scriptSecgrp = new NSISScriptSectionGroup(secGrp.getCaption(),secGrp.isDefaultExpanded(),secGrp.isBold(),
                secGrpId);
        if(sectionDescMap != null && mIsMUI && !Common.isEmpty(secGrp.getDescription())) {
            sectionDescMap.put(secGrpId, secGrp.getDescription());
        }
        INSISInstallElement[] children = secGrp.getChildren();
        if(!Common.isEmptyArray(children)) {
            for (int i = 0; i < children.length; i++) {
                if(i > 0) {
                    scriptSecgrp.addElement(new NSISScriptBlankLine());
                }
                scriptSecgrp.addElement((children[i] instanceof NSISSectionGroup?
                        (INSISScriptElement)buildSectionGroup((NSISSectionGroup)children[i], sectionDescMap):
                            (INSISScriptElement)buildSection((NSISSection)children[i], sectionDescMap)));
            }
        }

        return scriptSecgrp;
    }

    private void initLibInstallVar()
    {
        if(!mVars.contains("LibInstall")) { //$NON-NLS-1$
            mVars.add("LibInstall"); //$NON-NLS-1$
            mOnInitFunction.addElement(new NSISScriptInstruction("Push","$0")); //$NON-NLS-1$ //$NON-NLS-2$
            mOnInitFunction.addElement(new NSISScriptInstruction("ReadRegStr", new String[]{"$0",getKeyword("HKLM"),Common.quote("${REGKEY}"),"Path"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            mOnInitFunction.addElement(new NSISScriptInstruction("ClearErrors")); //$NON-NLS-1$
            mOnInitFunction.addElement(new NSISScriptInstruction("StrCmp",new String[] {"$0","","+2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            mOnInitFunction.addElement(new NSISScriptInstruction("StrCpy",new String[] {"$LibInstall","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            mOnInitFunction.addElement(new NSISScriptInstruction("Pop","$0")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private void addCreateRegKeyMacro()
    {
        if(!mReservedFiles.contains("System.dll")) { //$NON-NLS-1$
            mReservedFiles.add("System.dll"); //$NON-NLS-1$
            mScript.insertElement(mSectionsPlaceHolder,new NSISScriptSingleLineComment(EclipseNSISPlugin.getResourceString("scriptgen.create.reg.key.comment"))); //$NON-NLS-1$
            List<NSISScriptDefine> list = new ArrayList<NSISScriptDefine>();
            for (int i = 0; i < NSISWizardDisplayValues.HKEY_NAMES.length; i++) {
                String handle = RegistryImporter.rootKeyNameToHandle(NSISWizardDisplayValues.HKEY_NAMES[i]);
                if(!Common.isEmpty(handle)) {
                    list.add(new NSISScriptDefine(NSISWizardDisplayValues.HKEY_NAMES[i], handle));
                }
            }
            Collections.sort(list, new Comparator<NSISScriptDefine>() {
                public int compare(NSISScriptDefine o1, NSISScriptDefine o2)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            for (Iterator<NSISScriptDefine> iter = list.iterator(); iter.hasNext();) {
                mScript.insertElement(mSectionsPlaceHolder,iter.next());
            }
            mScript.insertElement(mSectionsPlaceHolder,new NSISScriptDefine("KEY_CREATE_SUB_KEY", "0x0004")); //$NON-NLS-1$ //$NON-NLS-2$
            NSISScriptMacro macro = (NSISScriptMacro)mScript.insertElement(mSectionsPlaceHolder,new NSISScriptMacro("CreateRegKey",new String[]{"ROOT_KEY","SUB_KEY"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            macro.addElement(new NSISScriptInstruction("Push",getKeyword("$0"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("Push",getKeyword("$1"))); //$NON-NLS-1$ //$NON-NLS-2$

            macro.addElement(new NSISScriptInstruction("System::Call",new String[] {"/NOUNLOAD",getKeyword("Advapi32::RegCreateKeyExA(i, t, i, t, i, i, i, *i, i) i(${ROOT_KEY}, '${SUB_KEY}', 0, '', 0, ${KEY_CREATE_SUB_KEY}, 0, .r0, 0) .r1")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            macro.addElement(new NSISScriptInstruction("StrCmp",new String[] {"$1", "0", "+2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            macro.addElement(new NSISScriptInstruction("SetErrors")); //$NON-NLS-1$
            macro.addElement(new NSISScriptInstruction("StrCmp",new String[] {"$0", "0", "+2"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            macro.addElement(new NSISScriptInstruction("System::Call",new String[] {"/NOUNLOAD",getKeyword("Advapi32::RegCloseKey(i) i(r0) .r1")})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            macro.addElement(new NSISScriptInstruction("System::Free","0")); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("Pop",getKeyword("$1"))); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("Pop",getKeyword("$0"))); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(mSectionsPlaceHolder,new NSISScriptBlankLine());
        }
    }

    private NSISScriptSection buildSection(NSISSection sec, Map<String, String> sectionDescMap)
    {
        String sectionId = MessageFormat.format("SEC{0,number,0000}",new Object[]{new Integer(mSectionCounter++)}); //$NON-NLS-1$
        String unSectionId = "UN"+sectionId; //$NON-NLS-1$
        if(sectionDescMap != null && mIsMUI && !sec.isHidden() && !Common.isEmpty(sec.getDescription())) {
            sectionDescMap.put(sectionId, sec.getDescription());
        }

        NSISScriptSection section = new NSISScriptSection(sec.getName(),sec.isBold(), sec.isHidden(), sec.isDefaultUnselected(),
                sectionId);
        NSISScriptSection unSection = null;

        if(mUnSectionList!=null) {
            mUnOnInitFunction.addElement(new NSISScriptInsertMacro("SELECT_UNSECTION",new String[]{sec.getName(),new StringBuffer("${").append( //$NON-NLS-1$ //$NON-NLS-2$
                    unSectionId).append("}").toString()})); //$NON-NLS-1$

            unSection = new NSISScriptSection("un."+sec.getName(),false, true, true,  //$NON-NLS-1$
                    unSectionId);
            mUnSectionList.add(unSection);
        }
        INSISInstallElement[] children = sec.getChildren();
        String outdir = ""; //$NON-NLS-1$
        int overwriteMode = -1;
        for (int i = 0; i < children.length; i++) {
            INSISInstallElement installElement = children[i];
            String type = installElement.getType();
            if(NSISInstallElementFactory.isValidType(type)) {
                if(installElement instanceof INSISInstallFileSystemObject) {
                    INSISInstallFileSystemObject fsObject = (INSISInstallFileSystemObject)installElement;
                    String tempOutdir = fsObject.getDestination();
                    if(!outdir.equalsIgnoreCase(tempOutdir)) {
                        outdir = tempOutdir;
                        section.addElement(new NSISScriptInstruction("SetOutPath",outdir)); //$NON-NLS-1$
                    }
                    if(overwriteMode != fsObject.getOverwriteMode()) {
                        overwriteMode = fsObject.getOverwriteMode();
                        switch(overwriteMode) {
                            case OVERWRITE_ON:
                                section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("on"))); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            case OVERWRITE_OFF:
                                section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("off"))); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            case OVERWRITE_TRY:
                                section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("try"))); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            case OVERWRITE_NEWER:
                                section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("ifnewer"))); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            case OVERWRITE_IFDIFF:
                                section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("ifdiff"))); //$NON-NLS-1$ //$NON-NLS-2$
                                break;
                            default:
                                section.addElement(new NSISScriptInstruction("SetOverwrite",getKeyword("lastused"))); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                    if(type.equals(NSISInstallFile.TYPE)) {
                        section.addElement(new NSISScriptInstruction("File", new String[] { //$NON-NLS-1$
                                (((NSISInstallFile) installElement).getNonFatal() ? getKeyword("/nonfatal") : null), //$NON-NLS-1$
                                (((NSISInstallFile) installElement).getPreserveAttributes() ? getKeyword("/a") : null), //$NON-NLS-1$
                                maybeMakeRelative(mSaveFile, ((NSISInstallFile) installElement).getName()) }));
                        if(unSection != null) {
                            unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                    new String[]{getKeyword("/REBOOTOK"), //$NON-NLS-1$
                                    new StringBuffer(outdir).append("\\").append(new Path(((NSISInstallFile)installElement).getName()).lastSegment()).toString()})); //$NON-NLS-1$
                        }
                    }
                    else if (type.equals(NSISInstallFiles.TYPE)) {
                        INSISInstallElement[] children2 = ((NSISInstallFiles)installElement).getChildren();
                        for (int j = 0; j < children2.length; j++) {
                            section.addElement(new NSISScriptInstruction("File", new String[] { //$NON-NLS-1$
                                    (((NSISInstallFiles) installElement).getNonFatal() ? getKeyword("/nonfatal"): null), //$NON-NLS-1$
                                    (((NSISInstallFiles) installElement).getPreserveAttributes() ? getKeyword("/a"): null), //$NON-NLS-1$
                                    maybeMakeRelative(mSaveFile,((NSISInstallFiles.FileItem) children2[j]).getName()) }));
                            if(unSection != null) {
                                unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                        new String[]{getKeyword("/REBOOTOK"), //$NON-NLS-1$
                                        new StringBuffer(outdir).append("\\").append(new Path(((NSISInstallFiles.FileItem)children2[j]).getName()).lastSegment()).toString()})); //$NON-NLS-1$
                            }
                        }
                    }
                    else if (type.equals(NSISInstallDirectory.TYPE)) {
                        NSISInstallDirectory installDirectory = (NSISInstallDirectory)installElement;
                        String name = installDirectory.getDisplayName() + "\\*"; //$NON-NLS-1$
                        if(installDirectory.isRecursive()) {
                            section.addElement(new NSISScriptInstruction("File",new String[]{ //$NON-NLS-1$
                                    (installDirectory.getNonFatal() ? getKeyword("/nonfatal"): null), //$NON-NLS-1$
                                    (installDirectory.getPreserveAttributes() ? getKeyword("/a"): null), //$NON-NLS-1$
                                    getKeyword("/r"), //$NON-NLS-1$
                                    maybeMakeRelative(mSaveFile,name)}));
                            if(unSection != null) {
                                if(mNewRmDirUsage) {
                                    unSection.addElement(0,new NSISScriptInstruction("RmDir", //$NON-NLS-1$
                                            new String[]{getKeyword("/r"), //$NON-NLS-1$
                                            getKeyword("/REBOOTOK"), //$NON-NLS-1$
                                            outdir}));
                                }
                                else {
                                    unSection.addElement(0,new NSISScriptInstruction("RmDir", //$NON-NLS-1$
                                            new String[]{getKeyword("/r"), //$NON-NLS-1$
                                            outdir}));
                                }
                            }
                        }
                        else {
                            section.addElement(new NSISScriptInstruction("File", new String[] { //$NON-NLS-1$
                                    (installDirectory.getNonFatal() ? getKeyword("/nonfatal"): null), //$NON-NLS-1$
                                    (installDirectory.getPreserveAttributes() ? getKeyword("/a"): null), //$NON-NLS-1$
                                    maybeMakeRelative(mSaveFile,name)}));
                            if(unSection != null) {
                                unSection.addElement(0,new NSISScriptInstruction("RmDir", //$NON-NLS-1$
                                        new String[]{getKeyword("/REBOOTOK"),outdir})); //$NON-NLS-1$
                                unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                        new String[]{getKeyword("/REBOOTOK"),outdir+"\\*"})); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
                else  {
                    if (type.equals(NSISInstallShortcut.TYPE)) {
                        NSISInstallShortcut shortcut = (NSISInstallShortcut)installElement;
                        String location = shortcut.getLocation();
                        String path = shortcut.getShortcutType()==SHORTCUT_INSTALLELEMENT?shortcut.getPath():shortcut.getUrl();
                        String name = shortcut.getName();
                        if(shortcut.isCreateInStartMenuGroup()) {
                            if(!mIsSilent && mSettings.isChangeStartMenuGroup() && mSettings.isDisableStartMenuShortcuts()) {
                                addSMGroupShortcutFunctions();
                                section.addElement(new NSISScriptInsertMacro("CREATE_SMGROUP_SHORTCUT",  //$NON-NLS-1$
                                        new String[] {name, path}));
                                if(unSection != null) {
                                    unSection.addElement(0, new NSISScriptInsertMacro("DELETE_SMGROUP_SHORTCUT",  //$NON-NLS-1$
                                            name));
                                }
                                continue;
                            }
                            location = getKeyword("$SMPROGRAMS")+"\\$StartMenuGroup"; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        if(!outdir.equalsIgnoreCase(location)) {
                            outdir = location;
                            section.addElement(new NSISScriptInstruction("SetOutPath",outdir)); //$NON-NLS-1$
                        }
                        location = new StringBuffer(outdir).append("\\").append( //$NON-NLS-1$
                                name).append(".lnk").toString(); //$NON-NLS-1$
                        section.addElement(new NSISScriptInstruction("CreateShortcut",new String[]{location, path})); //$NON-NLS-1$
                        if(unSection != null) {
                            unSection.addElement(0,new NSISScriptInstruction("Delete", //$NON-NLS-1$
                                    new String[]{getKeyword("/REBOOTOK"),location})); //$NON-NLS-1$
                        }
                        continue;
                    }
                    if (type.equals(NSISInstallRegistryValue.TYPE)) {
                        NSISInstallRegistryValue regValue = (NSISInstallRegistryValue)installElement;
                        if(regValue.getValueType() == REG_SZ && regValue.getValue().equals("") && regValue.getData().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                            type = NSISInstallRegistryKey.TYPE;
                            NSISInstallRegistryKey regKey = new NSISInstallRegistryKey();
                            regKey.setRootKey(regValue.getRootKey());
                            regKey.setSubKey(regValue.getSubKey());
                            regKey.setSettings(regValue.getSettings());
                            regKey.setParent(regValue.getParent());
                            installElement = regKey;
                        }
                        else {
                            String rootKey = ""; //$NON-NLS-1$
                            if(regValue.getRootKey() >= 0 && regValue.getRootKey() < NSISWizardDisplayValues.HKEY_NAMES.length) {
                                rootKey = NSISWizardDisplayValues.HKEY_NAMES[regValue.getRootKey()];
                            }
                            String instruction;
                            switch(regValue.getValueType()) {
                                case REG_DWORD:
                                    instruction = "WriteRegDWORD"; //$NON-NLS-1$
                                    break;
                                case REG_EXPAND_SZ:
                                    instruction = "WriteRegExpandStr"; //$NON-NLS-1$
                                    break;
                                case REG_BIN:
                                    instruction = "WriteRegBin"; //$NON-NLS-1$
                                    break;
                                default:
                                    instruction = "WriteRegStr"; //$NON-NLS-1$
                            }
                            section.addElement(new NSISScriptInstruction(instruction,
                                    new String[]{rootKey,
                                    regValue.getSubKey(),
                                    regValue.getValue(),
                                    regValue.getData()
                            }));
                            if(unSection != null) {
                                unSection.addElement(0,new NSISScriptInstruction("DeleteRegValue", //$NON-NLS-1$
                                        new String[]{rootKey,
                                        regValue.getSubKey(),
                                        regValue.getValue()}));
                            }
                            continue;
                        }
                    }
                    if (type.equals(NSISInstallRegistryKey.TYPE)) {
                        NSISInstallRegistryKey regKey = (NSISInstallRegistryKey)installElement;
                        if(regKey.getRootKey() >= 0 && regKey.getRootKey() < NSISWizardDisplayValues.HKEY_NAMES.length) {
                            addCreateRegKeyMacro();
                            String rootKey = NSISWizardDisplayValues.HKEY_NAMES[regKey.getRootKey()];
                            Pattern pattern = cReservedSubKeysMap.get(rootKey);
                            section.addElement(new NSISScriptInsertMacro("CreateRegKey", //$NON-NLS-1$
                                    new String[]{
                                    new StringBuffer("${").append(rootKey).append("}").toString(), //$NON-NLS-1$ //$NON-NLS-2$
                                    regKey.getSubKey()}));
                            if(unSection != null) {
                                String subKey = regKey.getSubKey();
                                int index = 0;
                                while(subKey.length() > 0) {
                                    if(pattern != null && pattern.matcher(subKey).matches()) {
                                        break;
                                    }
                                    unSection.addElement(index++,new NSISScriptInstruction("DeleteRegKey", //$NON-NLS-1$
                                            new String[]{getKeyword("/IfEmpty"), //$NON-NLS-1$
                                            rootKey,
                                            subKey}));
                                    int n = subKey.lastIndexOf('\\');
                                    if(n < 0) {
                                        break;
                                    }
                                    else {
                                        subKey = subKey.substring(0,n);
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    if (type.equals(NSISInstallLibrary.TYPE)) {
                        if(!mIncludes.contains("Library.nsh")) { //$NON-NLS-1$
                            mIncludes.add("Library.nsh"); //$NON-NLS-1$
                        }
                        NSISInstallLibrary library = (NSISInstallLibrary)installElement;
                        if(library.isShared()) {
                            initLibInstallVar();
                        }
                        String destination = library.getDestination();
                        String file = maybeMakeRelative(mSaveFile,library.getName());
                        StringBuffer buf = new StringBuffer(destination);
                        if(destination.charAt(destination.length()-1) != '\\') {
                            buf.append('\\');
                        }
                        buf.append(new File(Common.maybeUnquote(file)).getName());
                        destination = buf.toString();
                        String libType;
                        switch(library.getLibType()) {
                            case LIBTYPE_REGDLL:
                                libType = "REGDLL"; //$NON-NLS-1$
                                break;
                            case LIBTYPE_TLB:
                                libType = "TLB"; //$NON-NLS-1$
                                break;
                            case LIBTYPE_REGDLLTLB:
                                libType = "REGDLLTLB"; //$NON-NLS-1$
                                break;
                            case LIBTYPE_REGEXE:
                                if(NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_42) >= 0) {
                                    libType = "REGEXE"; //$NON-NLS-1$
                                    break;
                                }
                                //$FALL-THROUGH$
                            default:
                                libType = "DLL"; //$NON-NLS-1$
                        }
                        String installType;
                        if(library.isProtected()) {
                            if(library.isReboot()) {
                                installType = "REBOOT_PROTECTED"; //$NON-NLS-1$
                            }
                            else {
                                installType = "NOREBOOT_PROTECTED"; //$NON-NLS-1$
                            }
                        }
                        else {
                            if(library.isReboot()) {
                                installType = "REBOOT_NOTPROTECTED"; //$NON-NLS-1$

                            }
                            else {
                                installType = "NOREBOOT_NOTPROTECTED"; //$NON-NLS-1$
                            }
                        }
                        if(section.size() > 0) {
                            if(!(section.get(section.size()-1) instanceof NSISScriptBlankLine)) {
                                section.addElement(new NSISScriptBlankLine());
                            }
                        }
                        section.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getFormattedString("scriptgen.library.install.comment", new String[] {file}))); //$NON-NLS-1$
                        if(library.isRefreshShell()) {
                            section.addElement(new NSISScriptDefine("LIBRARY_SHELL_EXTENSION")); //$NON-NLS-1$
                        }
                        if(library.isUnloadLibraries()) {
                            section.addElement(new NSISScriptDefine("LIBRARY_COM")); //$NON-NLS-1$
                        }
                        boolean flag = NSISPreferences.getInstance().getNSISVersion().compareTo(INSISVersions.VERSION_2_26) >= 0;
                        if(flag) {
                            if(library.isX64()) {
                                section.addElement(new NSISScriptDefine("LIBRARY_X64")); //$NON-NLS-1$
                            }
                            if(library.isIgnoreVersion()) {
                                section.addElement(new NSISScriptDefine("LIBRARY_IGNORE_VERSION")); //$NON-NLS-1$
                            }
                        }
                        section.addElement(new NSISScriptInsertMacro("InstallLib", //$NON-NLS-1$
                                new String[]{libType,(library.isShared()?"$LibInstall":"NOTSHARED"), //$NON-NLS-1$ //$NON-NLS-2$
                                installType,file,destination,getKeyword("$INSTDIR")})); //$NON-NLS-1$
                        if(library.isRefreshShell()) {
                            section.addElement(new NSISScriptUndef("LIBRARY_SHELL_EXTENSION")); //$NON-NLS-1$
                        }
                        if(library.isUnloadLibraries()) {
                            section.addElement(new NSISScriptUndef("LIBRARY_COM")); //$NON-NLS-1$
                        }
                        if(flag) {
                            if(library.isX64()) {
                                section.addElement(new NSISScriptUndef("LIBRARY_X64")); //$NON-NLS-1$
                            }
                            if(library.isIgnoreVersion()) {
                                section.addElement(new NSISScriptUndef("LIBRARY_IGNORE_VERSION")); //$NON-NLS-1$
                            }
                        }
                        section.addElement(new NSISScriptBlankLine());
                        if(unSection != null) {
                            if(unSection.size() > 0) {
                                if(!(unSection.get(unSection.size()-1) instanceof NSISScriptBlankLine)) {
                                    unSection.addElement(new NSISScriptBlankLine());
                                }
                            }
                            unSection.addElement(new NSISScriptSingleLineComment(EclipseNSISPlugin.getFormattedString("scriptgen.library.uninstall.comment", new String[] {destination}))); //$NON-NLS-1$
                            if(library.isRefreshShell()) {
                                unSection.addElement(new NSISScriptDefine("LIBRARY_SHELL_EXTENSION")); //$NON-NLS-1$
                            }
                            if(library.isUnloadLibraries()) {
                                unSection.addElement(new NSISScriptDefine("LIBRARY_COM")); //$NON-NLS-1$
                            }
                            unSection.addElement(new NSISScriptInsertMacro("UnInstallLib", //$NON-NLS-1$
                                    new String[]{libType,(library.isShared()?"SHARED":"NOTSHARED"), //$NON-NLS-1$ //$NON-NLS-2$
                                    (library.isRemoveOnUninstall()?installType:"NOREMOVE"),destination})); //$NON-NLS-1$
                            if(library.isRefreshShell()) {
                                unSection.addElement(new NSISScriptUndef("LIBRARY_SHELL_EXTENSION")); //$NON-NLS-1$
                            }
                            if(library.isUnloadLibraries()) {
                                unSection.addElement(new NSISScriptUndef("LIBRARY_COM")); //$NON-NLS-1$
                            }
                            unSection.addElement(new NSISScriptBlankLine());
                        }
                        continue;
                    }
                }
            }
        }
        section.addElement(new NSISScriptInstruction("WriteRegStr",new String[]{ //$NON-NLS-1$
                getKeyword("HKLM"),Common.quote("${REGKEY}\\Components"),sec.getName(),"1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if(unSection != null) {
            unSection.addElement(new NSISScriptInstruction("DeleteRegValue",new String[]{ //$NON-NLS-1$
                    getKeyword("HKLM"),Common.quote("${REGKEY}\\Components"),sec.getName()})); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return section;
    }

    private void addSMGroupShortcutFunctions()
    {
        if(!mCreatedSMGroupShortcutFunctions) {
            NSISScriptMacro macro = (NSISScriptMacro)mScript.insertElement(mSectionsPlaceHolder, new NSISScriptMacro("CREATE_SMGROUP_SHORTCUT",new String[] {"NAME","PATH"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            macro.addElement(new NSISScriptInstruction("Push","\"${NAME}\"")); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("Push","\"${PATH}\"")); //$NON-NLS-1$ //$NON-NLS-2$
            macro.addElement(new NSISScriptInstruction("Call","CreateSMGroupShortcut")); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(mSectionsPlaceHolder,new NSISScriptBlankLine());
            NSISScriptFunction function = (NSISScriptFunction)mScript.insertElement(mFunctionsPlaceHolder, new NSISScriptFunction("CreateSMGroupShortcut")); //$NON-NLS-1$
            function.addElement(new NSISScriptInstruction("Exch",new String[] {getKeyword("$R0"),";PATH"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            function.addElement(new NSISScriptInstruction("Exch")); //$NON-NLS-1$
            function.addElement(new NSISScriptInstruction("Exch",new String[] {getKeyword("$R1"),";NAME"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            function.addElement(new NSISScriptInstruction("Push",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
            function.addElement(new NSISScriptInstruction("StrCpy",new String[] {getKeyword("$R2"),"$StartMenuGroup","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            function.addElement(new NSISScriptInstruction("StrCmp",new String[] {getKeyword("$R2"),Common.quote(">"),"no_smgroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            function.addElement(new NSISScriptInstruction("SetOutPath",getKeyword("$SMPROGRAMS")+"\\$StartMenuGroup")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String startMenuLink = Common.quote(new StringBuffer(getKeyword("$SMPROGRAMS")).append("\\$StartMenuGroup\\").append( //$NON-NLS-1$ //$NON-NLS-2$
                    getKeyword("$R1")).append(".lnk").toString()); //$NON-NLS-1$ //$NON-NLS-2$
            function.addElement(new NSISScriptInstruction("CreateShortcut",new String[]{startMenuLink,getKeyword("$R0")})); //$NON-NLS-1$ //$NON-NLS-2$
            function.addElement(new NSISScriptLabel("no_smgroup")); //$NON-NLS-1$
            function.addElement(new NSISScriptInstruction("Pop",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
            function.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
            function.addElement(new NSISScriptInstruction("Pop",getKeyword("$R0"))); //$NON-NLS-1$ //$NON-NLS-2$
            mScript.insertElement(mFunctionsPlaceHolder,new NSISScriptBlankLine());

            if(mUnfunctionsPlaceHolder != null) {
                macro = (NSISScriptMacro)mScript.insertElement(mUnsectionsPlaceHolder, new NSISScriptMacro("DELETE_SMGROUP_SHORTCUT",new String[] {"NAME"})); //$NON-NLS-1$ //$NON-NLS-2$
                macro.addElement(new NSISScriptInstruction("Push","\"${NAME}\"")); //$NON-NLS-1$ //$NON-NLS-2$
                macro.addElement(new NSISScriptInstruction("Call","un.DeleteSMGroupShortcut")); //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(mUnsectionsPlaceHolder,new NSISScriptBlankLine());
                function = (NSISScriptFunction)mScript.insertElement(mUnfunctionsPlaceHolder, new NSISScriptFunction("un.DeleteSMGroupShortcut")); //$NON-NLS-1$
                function.addElement(new NSISScriptInstruction("Exch",new String[] {getKeyword("$R1"),";NAME"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                function.addElement(new NSISScriptInstruction("Push",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
                function.addElement(new NSISScriptInstruction("StrCpy",new String[] {getKeyword("$R2"),"$StartMenuGroup","1"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                function.addElement(new NSISScriptInstruction("StrCmp",new String[] {getKeyword("$R2"),Common.quote(">"),"no_smgroup"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                function.addElement(new NSISScriptInstruction("Delete",new String[]{getKeyword("/REBOOTOK"),startMenuLink})); //$NON-NLS-1$ //$NON-NLS-2$
                function.addElement(new NSISScriptLabel("no_smgroup")); //$NON-NLS-1$
                function.addElement(new NSISScriptInstruction("Pop",getKeyword("$R2"))); //$NON-NLS-1$ //$NON-NLS-2$
                function.addElement(new NSISScriptInstruction("Pop",getKeyword("$R1"))); //$NON-NLS-1$ //$NON-NLS-2$
                mScript.insertElement(mUnfunctionsPlaceHolder,new NSISScriptBlankLine());
            }
            mCreatedSMGroupShortcutFunctions = true;
        }
    }

    private boolean validateLocale(Locale requested, Locale received)
    {
        if(!requested.equals(received)) {
            String country1 = requested.getCountry();
            String language1 = requested.getLanguage();
            String country2 = received.getCountry();
            String language2 = received.getLanguage();
            if(Common.isEmpty(received.getVariant())) {
                if(country1.equals(country2)) {
                    return Common.isEmpty(language2) || language1.equals(language2);
                }
            }
            return false;
        }
        return true;
    }

    private String getKeyword(String keyword)
    {
        String str = mKeywordCache.get(keyword);
        if(str == null) {
            str = NSISKeywords.getInstance().getKeyword(keyword);
            mKeywordCache.put(keyword, str);
        }
        return str;
    }
}
