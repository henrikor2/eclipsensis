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

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.editor.InstallOptionsSourceEditor;
import net.sf.eclipsensis.util.CommonImages;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

public class InstallOptionsSourceActionContributor extends TextEditorActionContributor implements IInstallOptionsConstants
{
    private MenuManager mInstallOptionsMenu;
    private List<RetargetAction> mRetargetActions = new ArrayList<RetargetAction>();
    private RetargetAction mExportHTMLAction;
    private RetargetAction mReorderAction;
    private RetargetAction mCreateControlAction;
    private RetargetAction mEditControlAction;
    private RetargetAction mDeleteControlAction;
    private RetargetAction mDeleteControlAction2;
    private RetargetAction mSwitchEditorAction;
    private DropDownAction mPreviewGroupAction;
    private PreviewRetargetAction mPreviewClassicAction;
    private PreviewRetargetAction mPreviewMUIAction;
    private DropDownAction mFixProblemsAction;
    private RetargetAction mFixAllAction;
    private RetargetAction mFixWarningsAction;
    private RetargetAction mFixErrorsAction;
    private LanguageComboContributionItem mLanguageContributionItem;
    private InstallOptionsWizardAction mWizardAction;
    private InstallOptionsHelpAction mHelpAction;

    public void buildActions()
    {
        mWizardAction = new InstallOptionsWizardAction();
        mHelpAction = new InstallOptionsHelpAction();

        mInstallOptionsMenu = new MenuManager(InstallOptionsPlugin.getResourceString("installoptions.menu.name")); //$NON-NLS-1$

        mSwitchEditorAction = new SwitchEditorRetargetAction(InstallOptionsPlugin.getResourceString("switch.design.editor.action.name")); //$NON-NLS-1$);
        registerRetargetAction(mSwitchEditorAction);

        mExportHTMLAction = new RetargetAction(InstallOptionsSourceEditor.EXPORT_HTML_ACTION,InstallOptionsPlugin.getResourceString("export.html.action.name")); //$NON-NLS-1$
        mExportHTMLAction.setToolTipText(InstallOptionsPlugin.getResourceString("export.html.action.tooltip")); //$NON-NLS-1$
        mExportHTMLAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPORT_HTML_ICON));
        mExportHTMLAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPORT_HTML_DISABLED_ICON));
        registerRetargetAction(mExportHTMLAction);

        mReorderAction = new RetargetAction(InstallOptionsSourceEditor.REORDER_ACTION,InstallOptionsPlugin.getResourceString("reorder.action.name")); //$NON-NLS-1$
        mReorderAction.setToolTipText(InstallOptionsPlugin.getResourceString("reorder.action.tooltip")); //$NON-NLS-1$
        mReorderAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("reorder.action.icon"))); //$NON-NLS-1$
        mReorderAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("reorder.action.disabled.icon"))); //$NON-NLS-1$
        registerRetargetAction(mReorderAction);

        mCreateControlAction = new RetargetAction(InstallOptionsSourceEditor.CREATE_CONTROL_ACTION,InstallOptionsPlugin.getResourceString("create.control.action.name")); //$NON-NLS-1$
        mCreateControlAction.setToolTipText(InstallOptionsPlugin.getResourceString("create.control.action.tooltip")); //$NON-NLS-1$
        mCreateControlAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.control.icon"))); //$NON-NLS-1$
        mCreateControlAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.control.disabled.icon"))); //$NON-NLS-1$
        mCreateControlAction.setActionDefinitionId(CREATE_CONTROL_COMMAND_ID);
        registerRetargetAction(mCreateControlAction);

        mEditControlAction = new RetargetAction(InstallOptionsSourceEditor.EDIT_CONTROL_ACTION,InstallOptionsPlugin.getResourceString("edit.control.action.name")); //$NON-NLS-1$
        mEditControlAction.setToolTipText(InstallOptionsPlugin.getResourceString("edit.control.action.tooltip")); //$NON-NLS-1$
        mEditControlAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("edit.control.icon"))); //$NON-NLS-1$
        mEditControlAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("edit.control.disabled.icon"))); //$NON-NLS-1$
        mEditControlAction.setActionDefinitionId(EDIT_CONTROL_COMMAND_ID);
        registerRetargetAction(mEditControlAction);

        mDeleteControlAction = new RetargetAction(InstallOptionsSourceEditor.DELETE_CONTROL_ACTION,InstallOptionsPlugin.getResourceString("delete.control.action.name")); //$NON-NLS-1$
        mDeleteControlAction.setToolTipText(InstallOptionsPlugin.getResourceString("delete.control.action.tooltip")); //$NON-NLS-1$
        mDeleteControlAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.icon"))); //$NON-NLS-1$
        mDeleteControlAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.disabled.icon"))); //$NON-NLS-1$
        mDeleteControlAction.setActionDefinitionId(DELETE_CONTROL_COMMAND_ID);
        registerRetargetAction(mDeleteControlAction);

        mDeleteControlAction2 = new RetargetAction(InstallOptionsSourceEditor.DELETE_CONTROL_ACTION2,InstallOptionsPlugin.getResourceString("delete.control.action.name")); //$NON-NLS-1$
        mDeleteControlAction2.setToolTipText(InstallOptionsPlugin.getResourceString("delete.control.action.tooltip")); //$NON-NLS-1$
        mDeleteControlAction2.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.icon"))); //$NON-NLS-1$
        mDeleteControlAction2.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("delete.control.disabled.icon"))); //$NON-NLS-1$
        mDeleteControlAction2.setActionDefinitionId(DELETE_CONTROL_COMMAND_ID2);
        registerRetargetAction(mDeleteControlAction2);

        mPreviewClassicAction = new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_CLASSIC);
        registerRetargetAction(mPreviewClassicAction);
        mPreviewMUIAction = new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_MUI);
        registerRetargetAction(mPreviewMUIAction);
        PreviewRetargetAction[] previewRetargetActions = new PreviewRetargetAction[] {
                                                     mPreviewClassicAction,
                                                     mPreviewMUIAction
                                                 };
        mPreviewGroupAction = new DropDownAction(IInstallOptionsConstants.PREVIEW_GROUP,
                                                 InstallOptionsPlugin.getDefault().getPreferenceStore(),
                                                 previewRetargetActions);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("preview.action.icon")); //$NON-NLS-1$
        mPreviewGroupAction.setImageDescriptor(imageDescriptor);
        mPreviewGroupAction.setHoverImageDescriptor(imageDescriptor);
        mPreviewGroupAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("preview.action.disabled.icon"))); //$NON-NLS-1$
        mPreviewGroupAction.setDetectCurrent(false);
        getPage().addPartListener(mPreviewGroupAction);

        mFixAllAction = new INIFileFixProblemsRetargetAction(INIFileFixProblemsAction.FIX_ALL_ID);
        registerRetargetAction(mFixAllAction);
        mFixWarningsAction = new INIFileFixProblemsRetargetAction(INIFileFixProblemsAction.FIX_WARNINGS_ID);
        registerRetargetAction(mFixWarningsAction);
        mFixErrorsAction = new INIFileFixProblemsRetargetAction(INIFileFixProblemsAction.FIX_ERRORS_ID);
        registerRetargetAction(mFixErrorsAction);
        RetargetAction[] fixProblemsRetargetActions = new RetargetAction[] {
                                                                            mFixAllAction,
                                                                            mFixErrorsAction,
                                                                            mFixWarningsAction
                                                                           };
        mFixProblemsAction = new DropDownAction(IInstallOptionsConstants.FIX_PROBLEMS_GROUP,
                                                 InstallOptionsPlugin.getDefault().getPreferenceStore(),
                                                 fixProblemsRetargetActions);
        getPage().addPartListener(mFixProblemsAction);

        mInstallOptionsMenu.add(mWizardAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mCreateControlAction);
        mInstallOptionsMenu.add(mEditControlAction);
        mInstallOptionsMenu.add(mDeleteControlAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mReorderAction);
        MenuManager submenu = new MenuManager(InstallOptionsPlugin.getResourceString("fix.problems.submenu.name")); //$NON-NLS-1$
        submenu.add(mFixAllAction);
        submenu.add(mFixErrorsAction);
        submenu.add(mFixWarningsAction);
        mInstallOptionsMenu.add(submenu);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(new PreviewSubMenuManager(previewRetargetActions));
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mExportHTMLAction);
        mInstallOptionsMenu.add(mSwitchEditorAction);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mHelpAction);

        mLanguageContributionItem = new LanguageComboContributionItem(getPage());
    }

    /**
     *
     */
    private void registerRetargetAction(RetargetAction action)
    {
        getPage().addPartListener(action);
        mRetargetActions.add(action);
    }

    @Override
    public void contributeToMenu(IMenuManager menu)
    {
        super.contributeToMenu(menu);
        menu.insertBefore(IWorkbenchActionConstants.M_WINDOW, mInstallOptionsMenu);
    }

    @Override
    public void contributeToToolBar(IToolBarManager tbm)
    {
        tbm.add(mWizardAction);
        tbm.add(new Separator());
        tbm.add(mCreateControlAction);
        tbm.add(mEditControlAction);
        tbm.add(mDeleteControlAction);
        tbm.add(new Separator());
        tbm.add(mReorderAction);
        tbm.add(mFixProblemsAction);
        tbm.add(new Separator());
        tbm.add(mPreviewGroupAction);
        tbm.add(mLanguageContributionItem);
        tbm.add(new Separator());
        tbm.add(mExportHTMLAction);
        tbm.add(mSwitchEditorAction);
        tbm.add(new Separator());
        tbm.add(mHelpAction);
    }

    @Override
    public void init(IActionBars bars)
    {
        buildActions();
        super.init(bars);
    }

    @Override
    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        IActionBars bars = getActionBars();
        ITextEditor editor = (part instanceof ITextEditor?(ITextEditor)part:null);
        for (Iterator<RetargetAction> iter = mRetargetActions.iterator(); iter.hasNext();) {
            String id = iter.next().getId();
            bars.setGlobalActionHandler(id,(editor == null?null:editor.getAction(id)));
        }
        bars.updateActionBars();
    }

    @Override
    public void dispose()
    {
        for (Iterator<RetargetAction> iter = mRetargetActions.iterator(); iter.hasNext();) {
            RetargetAction action = iter.next();
            getPage().removePartListener(action);
            action.dispose();
        }
        mFixProblemsAction.dispose();
        mPreviewGroupAction.dispose();
        mLanguageContributionItem.dispose();
        super.dispose();
    }
}
