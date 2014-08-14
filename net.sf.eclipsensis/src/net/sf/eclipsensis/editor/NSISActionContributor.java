/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.ResourceBundle;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.actions.NSISConfigWizardAction;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.*;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.*;

/**
 * Contributes interesting NSIS actions to the desktop's Edit menu and the toolbar.
 */
public class NSISActionContributor extends TextEditorActionContributor implements INSISConstants, IMenuListener
{
    private RetargetTextEditorAction mInsertTemplate;
    private RetargetTextEditorAction mContentAssistProposal;
    private RetargetTextEditorAction mTabsToSpaces;
    private RetargetTextEditorAction mToggleComment;
    private RetargetTextEditorAction mAddBlockComment;
    private RetargetTextEditorAction mRemoveBlockComment;
    private RetargetTextEditorAction mInsertFile;
    private RetargetTextEditorAction mInsertDirectory;
    private RetargetTextEditorAction mInsertColor;
    private RetargetTextEditorAction mInsertRegFile;
    private RetargetTextEditorAction mInsertRegKey;
    private RetargetTextEditorAction mInsertRegVal;
    private IContributionItem mConfigWizardAction;

    /**
     * Default constructor.
     */
    public NSISActionContributor()
    {
        super();
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
        mContentAssistProposal= new RetargetTextEditorAction(bundle, "content.assist.proposal."); //$NON-NLS-1$
        mContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);

        mInsertTemplate= new RetargetTextEditorAction(bundle, "insert.template."); //$NON-NLS-1$
        mInsertTemplate.setActionDefinitionId(INSERT_TEMPLATE_COMMAND_ID);

        mTabsToSpaces= new RetargetTextEditorAction(bundle, "tabs.to.spaces."); //$NON-NLS-1$
        mTabsToSpaces.setActionDefinitionId(TABS_TO_SPACES_COMMAND_ID);

        mToggleComment= new RetargetTextEditorAction(bundle, "toggle.comment."); //$NON-NLS-1$
        mToggleComment.setActionDefinitionId(TOGGLE_COMMENT_COMMAND_ID);

        mAddBlockComment= new RetargetTextEditorAction(bundle, "add.block.comment."); //$NON-NLS-1$
        mAddBlockComment.setActionDefinitionId(ADD_BLOCK_COMMENT_COMMAND_ID);

        mRemoveBlockComment= new RetargetTextEditorAction(bundle, "remove.block.comment."); //$NON-NLS-1$
        mRemoveBlockComment.setActionDefinitionId(REMOVE_BLOCK_COMMENT_COMMAND_ID);

        mInsertFile= new RetargetTextEditorAction(bundle, "insert.file."); //$NON-NLS-1$
        mInsertFile.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(bundle.getString("insert.file.image"))); //$NON-NLS-1$
        mInsertFile.setActionDefinitionId(INSERT_FILE_COMMAND_ID);

        mInsertDirectory= new RetargetTextEditorAction(bundle, "insert.directory."); //$NON-NLS-1$
        mInsertDirectory.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(bundle.getString("insert.directory.image"))); //$NON-NLS-1$
        mInsertDirectory.setActionDefinitionId(INSERT_DIRECTORY_COMMAND_ID);

        mInsertColor= new RetargetTextEditorAction(bundle, "insert.color."); //$NON-NLS-1$
        mInsertColor.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(bundle.getString("insert.color.image"))); //$NON-NLS-1$
        mInsertColor.setActionDefinitionId(INSERT_COLOR_COMMAND_ID);

        mInsertRegFile= new RetargetTextEditorAction(bundle, "insert.regfile."); //$NON-NLS-1$
        mInsertRegFile.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(bundle.getString("insert.regfile.image"))); //$NON-NLS-1$
        mInsertRegFile.setActionDefinitionId(INSERT_REGFILE_COMMAND_ID);

        mInsertRegKey= new RetargetTextEditorAction(bundle, "insert.regkey."); //$NON-NLS-1$
        mInsertRegKey.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(bundle.getString("insert.regkey.image"))); //$NON-NLS-1$
        mInsertRegKey.setActionDefinitionId(INSERT_REGKEY_COMMAND_ID);

        mInsertRegVal= new RetargetTextEditorAction(bundle, "insert.regval."); //$NON-NLS-1$
        mInsertRegVal.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(bundle.getString("insert.regval.image"))); //$NON-NLS-1$
        mInsertRegVal.setActionDefinitionId(INSERT_REGVAL_COMMAND_ID);

        mConfigWizardAction = new ActionContributionItem(new NSISConfigWizardAction());
    }

    /*
     * @see IEditorActionBarContributor#init(IActionBars)
     */
    @Override
    public void init(IActionBars bars)
    {
        super.init(bars);

        IMenuManager menuManager= bars.getMenuManager();
        IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
        if (editMenu != null) {
            editMenu.add(new Separator());
            editMenu.add(mContentAssistProposal);
            editMenu.add(mInsertTemplate);
            editMenu.add(new Separator());
            editMenu.add(mTabsToSpaces);
            editMenu.add(mToggleComment);
            editMenu.add(mAddBlockComment);
            editMenu.add(mRemoveBlockComment);
            editMenu.add(new Separator());
            editMenu.add(mInsertFile);
            editMenu.add(mInsertDirectory);
            editMenu.add(mInsertColor);
            editMenu.add(mInsertRegFile);
            editMenu.add(mInsertRegKey);
            editMenu.add(mInsertRegVal);
            editMenu.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager)
                {
                    IEditorPart editor = getActiveEditorPart();
                    if(editor != null && editor instanceof NSISEditor) {
                        ISelection sel = ((NSISEditor)editor).getSelectionProvider().getSelection();
                        if(sel instanceof ITextSelection) {
                            mAddBlockComment.setEnabled(((ITextSelection)sel).getLength() > 0);
                        }
                    }
                }
            });
        }
    }

    private void doSetActiveEditor(IEditorPart part)
    {
        IEditorPart oldEditor = getActiveEditorPart();
        if(oldEditor != null) {
            IMenuManager manager = oldEditor.getEditorSite().getActionBars().getMenuManager();
            if(manager != null) {
                manager.removeMenuListener(this);
            }
        }
        ITextEditor editor= null;
        if (part instanceof ITextEditor) {
            editor= (ITextEditor) part;
        }

        if(editor != null) {
            try {
                IMenuManager manager = editor.getEditorSite().getActionBars().getMenuManager();
                manager = manager.findMenuUsingPath("net.sf.eclipsensis.Menu"); //$NON-NLS-1$
                if (manager != null) {
                    manager.addMenuListener(this);
                }
            }
            catch (NullPointerException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }

        mContentAssistProposal.setAction(getAction(editor, INSISEditorConstants.CONTENT_ASSIST_PROPOSAL));
        mInsertTemplate.setAction(getAction(editor, INSISEditorConstants.INSERT_TEMPLATE));
        mTabsToSpaces.setAction(getAction(editor, INSISEditorConstants.TABS_TO_SPACES));
        mToggleComment.setAction(getAction(editor, INSISEditorConstants.TOGGLE_COMMENT));
        mAddBlockComment.setAction(getAction(editor, INSISEditorConstants.ADD_BLOCK_COMMENT));
        mRemoveBlockComment.setAction(getAction(editor, INSISEditorConstants.REMOVE_BLOCK_COMMENT));
        mInsertFile.setAction(getAction(editor, INSISEditorConstants.INSERT_FILE));
        mInsertDirectory.setAction(getAction(editor, INSISEditorConstants.INSERT_DIRECTORY));
        mInsertColor.setAction(getAction(editor, INSISEditorConstants.INSERT_COLOR));
        mInsertRegFile.setAction(getAction(editor, INSISEditorConstants.INSERT_REGFILE));
        mInsertRegKey.setAction(getAction(editor, INSISEditorConstants.INSERT_REGKEY));
        mInsertRegVal.setAction(getAction(editor, INSISEditorConstants.INSERT_REGVAL));
    }

    /*
     * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
     */
    @Override
    public void setActiveEditor(IEditorPart part)
    {
        doSetActiveEditor(part);
        super.setActiveEditor(part);
    }

    /*
     * @see IEditorActionBarContributor#dispose()
     */
    @Override
    public void dispose()
    {
        doSetActiveEditor(null);
        super.dispose();
    }

    public void menuAboutToShow(IMenuManager manager)
    {
        if(manager != null) {
            if(showHideActions(getActiveEditorPart(), manager)) {
                manager.updateAll(true);
            }
            if(!EclipseNSISPlugin.getDefault().isConfigured()) {
                if(manager.find(NSISConfigWizardAction.ID)==null) {
                    manager.appendToGroup("net.sf.eclipsensis.Group4", mConfigWizardAction); //$NON-NLS-1$
                }
            }
            else {
                if(manager.find(NSISConfigWizardAction.ID)!=null) {
                    manager.remove(mConfigWizardAction);
                }
            }
        }
    }

    private boolean showHideAction(IContributionManager manager, String id, boolean show)
    {
        IContributionItem item = manager.find(id);
        if(item != null) {
            if(item.isVisible() != show) {
                item.setVisible(show);
                item.update();
                return true;
            }
        }
        return false;
    }

    /**
     * @param editor
     * @param manager
     */
    private boolean showHideActions(IEditorPart editor, IContributionManager manager)
    {
        if(editor instanceof NSISEditor) {
            IEditorInput input = editor.getEditorInput();
            boolean openAssociatedScript = false;
            boolean openAssociatedHeaders = false;
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput)input).getFile();
                String ext = file.getFileExtension();
                if (Common.stringsAreEqual(INSISConstants.NSH_EXTENSION, ext)) {
                    openAssociatedScript = true;
                }
                else if (Common.stringsAreEqual(INSISConstants.NSI_EXTENSION, ext)) {
                    openAssociatedHeaders = true;
                }
            }
            boolean b1 = showHideAction(manager, INSISConstants.OPEN_ASSOCIATED_SCRIPT_ACTION_ID, openAssociatedScript);
            boolean b2 = showHideAction(manager, INSISConstants.OPEN_ASSOCIATED_HEADERS_ACTION_ID, openAssociatedHeaders);
            return b1 || b2;
        }
        return false;
    }
}
