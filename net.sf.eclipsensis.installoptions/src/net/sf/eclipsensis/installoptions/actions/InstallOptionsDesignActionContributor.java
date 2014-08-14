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

import org.eclipse.core.runtime.*;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

public class InstallOptionsDesignActionContributor extends ActionBarContributor
{
    private MenuManager mInstallOptionsMenu;
    private SetDialogSizeMenuManager mSetDialogSizeMenu;
    private InstallOptionsXYStatusContribution mXYStatusContribution;
    private ToggleLockRetargetAction mToggleLockAction;
    private InstallOptionsWizardAction mWizardAction;
    private InstallOptionsHelpAction mHelpAction;
    private List<DropDownAction> mDropDownActions = new ArrayList<DropDownAction>();
    private LanguageComboContributionItem mLanguageContributionItem;

    private void addDropDownAction(DropDownAction action)
    {
        addAction(action);
        mDropDownActions.add(action);
        getPage().addPartListener(action);
    }

    @Override
    protected void buildActions()
    {
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        RetargetAction retargetAction;

        addRetargetAction(new UndoRetargetAction());
        addRetargetAction(new RedoRetargetAction());

        retargetAction = new RetargetAction(ActionFactory.REVERT.getId(),InstallOptionsPlugin.getResourceString("revert.action.label")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("revert.action.tooltip")); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(ActionFactory.CUT.getId(),InstallOptionsPlugin.getResourceString("cut.action.name")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("cut.action.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
        retargetAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(ActionFactory.COPY.getId(),InstallOptionsPlugin.getResourceString("copy.action.label")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("copy.action.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        retargetAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(ActionFactory.PASTE.getId(),InstallOptionsPlugin.getResourceString("paste.action.label")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("paste.action.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        retargetAction.setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        addRetargetAction(retargetAction);

        addRetargetAction(new DeleteRetargetAction());

        RetargetAction[] arrangeActions = {
                new ArrangeRetargetAction(IInstallOptionsConstants.ARRANGE_SEND_BACKWARD),
                new ArrangeRetargetAction(IInstallOptionsConstants.ARRANGE_SEND_TO_BACK),
                new ArrangeRetargetAction(IInstallOptionsConstants.ARRANGE_BRING_FORWARD),
                new ArrangeRetargetAction(IInstallOptionsConstants.ARRANGE_BRING_TO_FRONT)
        };
        for (int i = 0; i < arrangeActions.length; i++) {
            addRetargetAction(arrangeActions[i]);
        }
        addDropDownAction(new DropDownAction(IInstallOptionsConstants.ARRANGE_GROUP,InstallOptionsPlugin.getDefault().getPreferenceStore(),arrangeActions));

        RetargetAction[] alignmentActions = {
                new AlignmentRetargetAction(PositionConstants.LEFT),
                new AlignmentRetargetAction(PositionConstants.CENTER),
                new AlignmentRetargetAction(PositionConstants.RIGHT),
                new AlignmentRetargetAction(PositionConstants.TOP),
                new AlignmentRetargetAction(PositionConstants.MIDDLE),
                new AlignmentRetargetAction(PositionConstants.BOTTOM)
        };
        for (int i = 0; i < alignmentActions.length; i++) {
            addRetargetAction(alignmentActions[i]);
        }
        addDropDownAction(new DropDownAction(IInstallOptionsConstants.ALIGN_GROUP,InstallOptionsPlugin.getDefault().getPreferenceStore(),alignmentActions));

        RetargetAction[] distributeActions = {
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_HORIZONTAL_BETWEEN),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_HORIZONTAL_LEFT_EDGE),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_HORIZONTAL_CENTER),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_HORIZONTAL_RIGHT_EDGE),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_VERTICAL_BETWEEN),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_VERTICAL_TOP_EDGE),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_VERTICAL_CENTER),
                new DistributeRetargetAction(IInstallOptionsConstants.DISTRIBUTE_VERTICAL_BOTTOM_EDGE)
        };
        for (int i = 0; i < distributeActions.length; i++) {
            addRetargetAction(distributeActions[i]);
        }
        addDropDownAction(new DropDownAction(IInstallOptionsConstants.DISTRIBUTE_GROUP,InstallOptionsPlugin.getDefault().getPreferenceStore(),distributeActions));

        mToggleLockAction = new ToggleLockRetargetAction();
        addRetargetAction(mToggleLockAction);
        getPage().addPartListener(mToggleLockAction);

        retargetAction = new RetargetAction(RefreshDiagramAction.ID,
                InstallOptionsPlugin.getResourceString("refresh.diagram.action.label")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("refresh.diagram.action.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("refresh.icon"))); //$NON-NLS-1$
        retargetAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("refresh.disabled.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(CreateTemplateAction.ID,
                InstallOptionsPlugin.getResourceString("create.template.action.label")); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("create.template.action.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.template.icon"))); //$NON-NLS-1$
        retargetAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("create.template.disabled.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new SwitchEditorRetargetAction(InstallOptionsPlugin.getResourceString("switch.source.editor.action.name")); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        addRetargetAction(new RetargetAction(IInstallOptionsConstants.GRID_SNAP_GLUE_SETTINGS_ACTION_ID,InstallOptionsPlugin.getResourceString("grid.snap.glue.action.name"))); //$NON-NLS-1$

        retargetAction = new LabelRetargetAction(MatchSizeAction.ID,InstallOptionsPlugin.getResourceString("match.size.action.name")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("match.size.icon"))); //$NON-NLS-1$
        retargetAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("match.size.disabled.icon"))); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("match.size.action.tooltip")); //$NON-NLS-1$

        RetargetAction[] matchSizeActions = {
                new MatchWidthRetargetAction(),
                new MatchHeightRetargetAction(),
                retargetAction
        };
        for (int i = 0; i < matchSizeActions.length; i++) {
            addRetargetAction(matchSizeActions[i]);
        }
        addDropDownAction(new DropDownAction(IInstallOptionsConstants.MATCHSIZE_GROUP,InstallOptionsPlugin.getDefault().getPreferenceStore(),matchSizeActions));

        retargetAction = new RetargetAction(
                ToggleDialogSizeVisibilityAction.ID,
                InstallOptionsPlugin.getResourceString("show.dialog.size.action.name"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.dialog.size.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(
                GEFActionConstants.TOGGLE_RULER_VISIBILITY,
                InstallOptionsPlugin.getResourceString("toggle.ruler.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("toggle.ruler.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.rulers.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(
                GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
                InstallOptionsPlugin.getResourceString("toggle.snap.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("toggle.snap.tooltip")); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY,
                InstallOptionsPlugin.getResourceString("toggle.grid.label"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setToolTipText(InstallOptionsPlugin.getResourceString("toggle.grid.tooltip")); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.grid.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        retargetAction = new RetargetAction(ToggleGuideVisibilityAction.ID,
                InstallOptionsPlugin.getResourceString("show.guides.action.name"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        retargetAction.setImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("show.guides.icon"))); //$NON-NLS-1$
        addRetargetAction(retargetAction);

        PreviewRetargetAction[] previewActions = {
                new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_CLASSIC),
                new PreviewRetargetAction(IInstallOptionsConstants.PREVIEW_MUI)
        };
        for (int i = 0; i < previewActions.length; i++) {
            addRetargetAction(previewActions[i]);
        }
        DropDownAction dropDownAction = new DropDownAction(IInstallOptionsConstants.PREVIEW_GROUP,InstallOptionsPlugin.getDefault().getPreferenceStore(),previewActions);
        ImageDescriptor imageDescriptor = InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("preview.action.icon")); //$NON-NLS-1$
        dropDownAction.setImageDescriptor(imageDescriptor);
        dropDownAction.setHoverImageDescriptor(imageDescriptor);
        dropDownAction.setDisabledImageDescriptor(InstallOptionsPlugin.getImageManager().getImageDescriptor(InstallOptionsPlugin.getResourceString("preview.action.disabled.icon"))); //$NON-NLS-1$
        dropDownAction.setDetectCurrent(false);
        addDropDownAction(dropDownAction);

        mWizardAction = new InstallOptionsWizardAction();
        mHelpAction = new InstallOptionsHelpAction();

        mInstallOptionsMenu = new MenuManager(InstallOptionsPlugin.getResourceString("installoptions.menu.name")); //$NON-NLS-1$
        mInstallOptionsMenu.add(getAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY));
        mInstallOptionsMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
        mInstallOptionsMenu.add(getAction(ToggleDialogSizeVisibilityAction.ID));
        mInstallOptionsMenu.add(getAction(ToggleGuideVisibilityAction.ID));
        mInstallOptionsMenu.add(getAction(IInstallOptionsConstants.GRID_SNAP_GLUE_SETTINGS_ACTION_ID));
        mInstallOptionsMenu.add(new Separator());

        MenuManager submenu = new MenuManager(InstallOptionsPlugin.getResourceString("align.submenu.name")); //$NON-NLS-1$
        submenu.add(getAction(GEFActionConstants.ALIGN_LEFT));
        submenu.add(getAction(GEFActionConstants.ALIGN_CENTER));
        submenu.add(getAction(GEFActionConstants.ALIGN_RIGHT));
        submenu.add(new Separator());
        submenu.add(getAction(GEFActionConstants.ALIGN_TOP));
        submenu.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
        submenu.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
        mInstallOptionsMenu.add(submenu);

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("match.size.submenu.name")); //$NON-NLS-1$
        submenu.add(getAction(GEFActionConstants.MATCH_WIDTH));
        submenu.add(getAction(GEFActionConstants.MATCH_HEIGHT));
        submenu.add(getAction(MatchSizeAction.ID));
        mInstallOptionsMenu.add(submenu);

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("distribute.submenu.name")); //$NON-NLS-1$
        submenu.add(getAction(DistributeAction.HORIZONTAL_BETWEEN_ID));
        submenu.add(getAction(DistributeAction.HORIZONTAL_LEFT_EDGE_ID));
        submenu.add(getAction(DistributeAction.HORIZONTAL_CENTER_ID));
        submenu.add(getAction(DistributeAction.HORIZONTAL_RIGHT_EDGE_ID));
        submenu.add(new Separator());
        submenu.add(getAction(DistributeAction.VERTICAL_BETWEEN_ID));
        submenu.add(getAction(DistributeAction.VERTICAL_TOP_EDGE_ID));
        submenu.add(getAction(DistributeAction.VERTICAL_CENTER_ID));
        submenu.add(getAction(DistributeAction.VERTICAL_BOTTOM_EDGE_ID));
        mInstallOptionsMenu.add(submenu);

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("arrange.submenu.name")); //$NON-NLS-1$
        submenu.add(getAction(ArrangeAction.SEND_BACKWARD_ID));
        submenu.add(getAction(ArrangeAction.SEND_TO_BACK_ID));
        submenu.add(getAction(ArrangeAction.BRING_FORWARD_ID));
        submenu.add(getAction(ArrangeAction.BRING_TO_FRONT_ID));
        mInstallOptionsMenu.add(submenu);

        mInstallOptionsMenu.add(getAction(ToggleLockAction.ID));
        mInstallOptionsMenu.add(new Separator());

        mSetDialogSizeMenu = new SetDialogSizeMenuManager(mInstallOptionsMenu);
        mInstallOptionsMenu.add(mSetDialogSizeMenu);
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mWizardAction);
        mInstallOptionsMenu.add(new PreviewSubMenuManager(previewActions));
        mInstallOptionsMenu.add(getAction(RefreshDiagramAction.ID));
        mInstallOptionsMenu.add(getAction(SwitchEditorAction.ID));
        mInstallOptionsMenu.add(new Separator());
        mInstallOptionsMenu.add(mHelpAction);

        mXYStatusContribution = new InstallOptionsXYStatusContribution();
        mLanguageContributionItem = new LanguageComboContributionItem(getPage());
    }

    @Override
    public void dispose()
    {
        for (int i = 0; i < mDropDownActions.size(); i++) {
            DropDownAction action = mDropDownActions.get(i);
            getPage().removePartListener(action);
            action.dispose();
        }
        mToggleLockAction.dispose();
        mLanguageContributionItem.dispose();
        mXYStatusContribution.dispose();
        super.dispose();
    }

    @Override
    public void setActiveEditor(IEditorPart editor)
    {
        mSetDialogSizeMenu.setEditor(editor);
        mXYStatusContribution.editorChanged(editor);
        if(editor.getAdapter(ActionRegistry.class) == null) {
            Platform.getAdapterManager().registerAdapters(new IAdapterFactory(){
                    @SuppressWarnings("unchecked")
                    public Object getAdapter(Object adaptableObject, Class adapterType)
                    {
                        if(adapterType.equals(ActionRegistry.class)) {
                            return new ActionRegistry();
                        }
                        return null;
                    }

                    @SuppressWarnings("unchecked")
                    public Class[] getAdapterList()
                    {
                        return new Class[]{ActionRegistry.class};
                    }
                },editor.getClass());
        }
        super.setActiveEditor(editor);
    }

    @Override
    protected void declareGlobalActionKeys()
    {
        addGlobalActionKey(ActionFactory.PRINT.getId());
        addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
    }

    @Override
    public void contributeToToolBar(IToolBarManager tbm)
    {
        tbm.add(getAction(ActionFactory.UNDO.getId()));
        tbm.add(getAction(ActionFactory.REDO.getId()));
        tbm.add(new Separator());
        tbm.add(getAction(ActionFactory.CUT.getId()));
        tbm.add(getAction(ActionFactory.COPY.getId()));
        tbm.add(getAction(ActionFactory.PASTE.getId()));
        tbm.add(getAction(ActionFactory.DELETE.getId()));

        tbm.add(new Separator());
        tbm.add(getAction(IInstallOptionsConstants.ALIGN_GROUP));
        tbm.add(getAction(IInstallOptionsConstants.MATCHSIZE_GROUP));
        tbm.add(getAction(IInstallOptionsConstants.DISTRIBUTE_GROUP));
        tbm.add(getAction(IInstallOptionsConstants.ARRANGE_GROUP));
        tbm.add(mToggleLockAction);

        tbm.add(new Separator());
        tbm.add(getAction(CreateTemplateAction.ID));

        tbm.add(new Separator());
        tbm.add(getAction(RefreshDiagramAction.ID));
        tbm.add(new Separator());
        tbm.add(mWizardAction);
        tbm.add(new Separator());
        tbm.add(getAction(IInstallOptionsConstants.PREVIEW_GROUP));
        tbm.add(mLanguageContributionItem);
        tbm.add(new Separator());
        tbm.add(getAction(SwitchEditorAction.ID));
        tbm.add(new Separator());
        tbm.add(mHelpAction);
    }

    @Override
    public void contributeToMenu(IMenuManager menubar)
    {
        menubar.insertBefore(IWorkbenchActionConstants.M_WINDOW, mInstallOptionsMenu);
    }

    @Override
    public void contributeToStatusLine(IStatusLineManager statusLineManager)
    {
        statusLineManager.add(mXYStatusContribution);
    }
}
