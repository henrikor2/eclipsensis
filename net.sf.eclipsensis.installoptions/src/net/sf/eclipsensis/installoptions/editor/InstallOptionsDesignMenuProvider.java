/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.actions.*;
import net.sf.eclipsensis.installoptions.actions.MatchSizeAction;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.gef.*;
import org.eclipse.gef.ui.actions.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.actions.ActionFactory;

public class InstallOptionsDesignMenuProvider extends org.eclipse.gef.ContextMenuProvider
{
    private ActionRegistry mActionRegistry;
    private InstallOptionsDesignEditor mEditor;
    private SetDialogSizeMenuManager mSetDialogSizeMenu;

    public InstallOptionsDesignMenuProvider(InstallOptionsDesignEditor editor, ActionRegistry registry)
    {
        this(editor.getGraphicalViewer(), registry);
        mEditor = editor;
    }

    public InstallOptionsDesignMenuProvider(EditPartViewer viewer, ActionRegistry registry)
    {
        super(viewer);
        setActionRegistry(registry);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.ContextMenuProvider#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void buildContextMenu(IMenuManager manager)
    {
        GEFActionConstants.addStandardActionGroups(manager);

        IAction action;

        addContextMenu(manager, ActionFactory.UNDO.getId(), GEFActionConstants.GROUP_UNDO);
        addContextMenu(manager, ActionFactory.REDO.getId(), GEFActionConstants.GROUP_UNDO);
        addContextMenu(manager, ActionFactory.CUT.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ActionFactory.COPY.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ActionFactory.PASTE.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ActionFactory.DELETE.getId(), GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ToggleEnablementAction.ID, GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, ToggleLockAction.ID, GEFActionConstants.GROUP_EDIT);

        List<?> selected = getViewer().getSelectedEditParts();
        if(selected.size() == 1) {
            EditPart editPart = (EditPart)selected.get(0);
            if(editPart instanceof InstallOptionsWidgetEditPart) {
                action = getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT);
                if (action != null && action.isEnabled()) {
                    IDirectEditLabelProvider labelProvider = (IDirectEditLabelProvider)((InstallOptionsWidgetEditPart)editPart).getAdapter(IDirectEditLabelProvider.class);
                    String label;
                    if(labelProvider != null) {
                        label = labelProvider.getDirectEditLabel();
                    }
                    else {
                        label = InstallOptionsPlugin.getResourceString("direct.edit.label"); //$NON-NLS-1$
                    }
                    if(!Common.isEmpty(label)) {
                        action.setText(label);
                        action.setToolTipText(label);
                        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
                    }
                }

                action = getActionRegistry().getAction(ExtendedEditAction.ID);
                if (action != null && action.isEnabled()) {
                    IExtendedEditLabelProvider labelProvider = (IExtendedEditLabelProvider)((InstallOptionsWidgetEditPart)editPart).getAdapter(IExtendedEditLabelProvider.class);
                    String label;
                    if(labelProvider != null) {
                        label = labelProvider.getExtendedEditLabel();
                    }
                    else {
                        label = InstallOptionsPlugin.getResourceString("extended.edit.label"); //$NON-NLS-1$
                    }
                    if (!Common.isEmpty(label)) {
                        action.setText(label);
                        action.setToolTipText(label);
                        manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
                    }
                }
            }
        }

        // Alignment Actions
        MenuManager submenu = new MenuManager(InstallOptionsPlugin.getResourceString("align.submenu.name")); //$NON-NLS-1$

        addContextMenu(submenu, GEFActionConstants.ALIGN_LEFT, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_CENTER, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_RIGHT, null);
        if (!submenu.isEmpty()) {
            submenu.add(new Separator());
        }

        addContextMenu(submenu, GEFActionConstants.ALIGN_TOP, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_MIDDLE, null);
        addContextMenu(submenu, GEFActionConstants.ALIGN_BOTTOM, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("match.size.submenu.name")); //$NON-NLS-1$
        addContextMenu(submenu, GEFActionConstants.MATCH_WIDTH, null);
        addContextMenu(submenu, GEFActionConstants.MATCH_HEIGHT, null);
        addContextMenu(submenu, MatchSizeAction.ID, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("distribute.submenu.name")); //$NON-NLS-1$

        addContextMenu(submenu, DistributeAction.HORIZONTAL_BETWEEN_ID, null);
        addContextMenu(submenu, DistributeAction.HORIZONTAL_LEFT_EDGE_ID, null);
        addContextMenu(submenu, DistributeAction.HORIZONTAL_CENTER_ID, null);
        addContextMenu(submenu, DistributeAction.HORIZONTAL_RIGHT_EDGE_ID, null);
        if (!submenu.isEmpty()) {
            submenu.add(new Separator());
        }

        addContextMenu(submenu, DistributeAction.VERTICAL_BETWEEN_ID, null);
        addContextMenu(submenu, DistributeAction.VERTICAL_TOP_EDGE_ID, null);
        addContextMenu(submenu, DistributeAction.VERTICAL_CENTER_ID, null);
        addContextMenu(submenu, DistributeAction.VERTICAL_BOTTOM_EDGE_ID, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("arrange.submenu.name")); //$NON-NLS-1$

        addContextMenu(submenu, ArrangeAction.SEND_BACKWARD_ID, null);
        addContextMenu(submenu, ArrangeAction.SEND_TO_BACK_ID, null);
        addContextMenu(submenu, ArrangeAction.BRING_FORWARD_ID, null);
        addContextMenu(submenu, ArrangeAction.BRING_TO_FRONT_ID, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        if(mEditor != null && selected.size() == 0) {
            if(mSetDialogSizeMenu == null) {
                mSetDialogSizeMenu = new SetDialogSizeMenuManager(manager);
                mSetDialogSizeMenu.setEditor(mEditor);
            }
            mSetDialogSizeMenu.rebuild();
            if(!mSetDialogSizeMenu.isEmpty()) {
                manager.appendToGroup(GEFActionConstants.GROUP_EDIT, mSetDialogSizeMenu);
            }
        }
        addContextMenu(manager, CreateTemplateAction.ID, GEFActionConstants.GROUP_EDIT);
        addContextMenu(manager, RefreshDiagramAction.ID, GEFActionConstants.GROUP_EDIT);

        submenu = new MenuManager(InstallOptionsPlugin.getResourceString("preview.submenu.name")); //$NON-NLS-1$
        addContextMenu(submenu, PreviewAction.PREVIEW_CLASSIC_ID, null);
        addContextMenu(submenu, PreviewAction.PREVIEW_MUI_ID, null);

        if (!submenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_EDIT, submenu);
        }

        addContextMenu(manager, "net.sf.eclipsensis.installoptions.design_editor_prefs", GEFActionConstants.GROUP_REST); //$NON-NLS-1$
        addContextMenu(manager, ActionFactory.SAVE.getId(), GEFActionConstants.GROUP_SAVE);
    }

    /**
     * @param manager
     */
    private void addContextMenu(IMenuManager manager, String id, String group)
    {
        IAction action;
        action = getActionRegistry().getAction(id);
        if (action != null && action.isEnabled()) {
            if(group != null) {
                manager.appendToGroup(group, action);
            }
            else {
                manager.add(action);
            }
        }
    }

    private ActionRegistry getActionRegistry()
    {
        return mActionRegistry;
    }

    private void setActionRegistry(ActionRegistry registry)
    {
        mActionRegistry = registry;
    }

}