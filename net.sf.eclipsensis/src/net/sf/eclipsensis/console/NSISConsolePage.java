/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *
 * Based upon org.eclipse.ui.internal.console.IOConsolePage
 *
 *******************************************************************************/
package net.sf.eclipsensis.console;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.actions.NSISCancelAction;
import net.sf.eclipsensis.makensis.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.text.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.*;

public class NSISConsolePage extends TextConsolePage implements IMakeNSISRunListener, IDocumentListener
{
    private ScrollLockAction mScrollLockAction;
    private Action mCancelAction;
    private ConsoleRemoveAction mRemoveAction;
    private MenuManager mMenuManager;
    private boolean mIsCompiling = false;
    private boolean mDisposed = false;

    public NSISConsolePage(NSISConsole console, IConsoleView view)
    {
        super(console, view);
        MakeNSISRunner.addListener(this);
        console.getDocument().addDocumentListener(this);
    }

    public void documentAboutToBeChanged(DocumentEvent event)
    {
    }

    public void documentChanged(DocumentEvent event)
    {
        updateActions();
    }

    public void eventOccurred(MakeNSISRunEvent event)
    {
        switch(event.getType()) {
            case MakeNSISRunEvent.STARTED:
                setIsCompiling(true);
                break;
            case MakeNSISRunEvent.STOPPED:
                setIsCompiling(false);
                break;
        }
    }

    private synchronized void setIsCompiling(boolean isCompiling)
    {
        if(mIsCompiling != isCompiling) {
            mIsCompiling = isCompiling;
            if(!mDisposed) {
                mCancelAction.setEnabled(mIsCompiling);
                updateActions();
            }
        }
    }

    private void updateActions()
    {
        boolean b = ((NSISConsole)getConsole()).getDocument().getLength() > 0;
        if(fClearOutputAction != null) {
            fClearOutputAction.setEnabled(!mIsCompiling && b);
        }
        IAction action = (IAction)fGlobalActions.get(ActionFactory.SELECT_ALL.getId());
        if(action != null) {
            action.setEnabled(b);
        }
    }

    @Override
    protected TextConsoleViewer createViewer(Composite parent)
    {
        return new NSISConsoleViewer(parent, (NSISConsole)getConsole());
    }

    public void setAutoScroll(boolean scroll)
    {
        NSISConsoleViewer viewer = (NSISConsoleViewer) getViewer();
        if (viewer != null) {
            viewer.setAutoScroll(scroll);
            mScrollLockAction.setChecked(!scroll);
        }
    }

    public boolean isAutoScroll()
    {
        NSISConsoleViewer viewer = (NSISConsoleViewer) getViewer();
        if (viewer != null) {
            return viewer.isAutoScroll();
        }
        else {
            return false;
        }
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        String id = "#ContextMenu"; //$NON-NLS-1$
        if (getConsole().getType() != null) {
            id = getConsole().getType() + "." + id; //$NON-NLS-1$
        }
        mMenuManager= new MenuManager("#ContextMenu", id);  //$NON-NLS-1$
        mMenuManager.setRemoveAllWhenShown(true);
        mMenuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager m) {
                contextMenuAboutToShow(m);
            }
        });
        Menu menu = mMenuManager.createContextMenu(getViewer().getTextWidget());
        getViewer().getTextWidget().setMenu(menu);
        getSite().registerContextMenu(id, mMenuManager, getViewer());
    }

    private Action makeAction(Action action, String text, String tooltipText, String image, String disabledImage, ActionFactory globalActionFactory, boolean enabled)
    {
        if (!Common.isEmpty(text)) {
            action.setText(text);
        }
        if (!Common.isEmpty(tooltipText)) {
            action.setToolTipText(tooltipText);
        }
        if (!Common.isEmpty(image)) {
            action.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(image));
        }
        if (!Common.isEmpty(disabledImage)) {
            action.setDisabledImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(disabledImage));
        }
        action.setEnabled(enabled);
        if (globalActionFactory != null) {
            getSite().getActionBars().setGlobalActionHandler(globalActionFactory.getId(), action);
        }
        return action;
    }

    @Override
    protected void createActions()
    {
        super.createActions();
        ImageManager imageManager = EclipseNSISPlugin.getImageManager();

        boolean enabled = ((NSISConsole)getConsole()).getDocument().getLength() > 0;
        if(fClearOutputAction != null) {
            fClearOutputAction.setEnabled(enabled);
            fClearOutputAction.setImageDescriptor(imageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("clear.action.icon"))); //$NON-NLS-1$
            fClearOutputAction.setHoverImageDescriptor(imageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("clear.action.icon"))); //$NON-NLS-1$
            fClearOutputAction.setDisabledImageDescriptor(imageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("clear.action.disabled.icon"))); //$NON-NLS-1$
        }

        mRemoveAction = new ConsoleRemoveAction();
        mScrollLockAction = new ScrollLockAction();
        IAction action = (IAction)fGlobalActions.get(ActionFactory.SELECT_ALL.getId());
        if(action != null) {
            action.setEnabled(enabled);
            action.setImageDescriptor(imageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("selectall.action.icon"))); //$NON-NLS-1$
            action.setHoverImageDescriptor(action.getImageDescriptor());
        }
        action = (IAction)fGlobalActions.get(ActionFactory.COPY.getId());
        if(action != null) {
            action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
            action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        }

        final NSISCancelAction cancelActionDelegate = new NSISCancelAction();
        mCancelAction = makeAction(
                new Action() {
                    @Override
                    public void run()
                    {
                        cancelActionDelegate.init(this);
                        cancelActionDelegate.run(this);
                    }
                },
                EclipseNSISPlugin.getResourceString("cancel.action.name"), EclipseNSISPlugin.getResourceString("cancel.action.tooltip"), EclipseNSISPlugin.getResourceString("cancel.action.icon"), EclipseNSISPlugin.getResourceString("cancel.action.disabled.icon"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                null, mIsCompiling);
        cancelActionDelegate.init(mCancelAction);
        getViewer().getControl().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                cancelActionDelegate.dispose();
            }
        });        setIsCompiling(MakeNSISRunner.isCompiling());

        setAutoScroll(!mScrollLockAction.isChecked());
    }

    @Override
    protected void contextMenuAboutToShow(IMenuManager menuManager)
    {
        super.contextMenuAboutToShow(menuManager);
        menuManager.add(mRemoveAction);
        menuManager.add(mCancelAction);
        menuManager.add(mScrollLockAction);
        menuManager.remove(ActionFactory.CUT.getId());
        menuManager.remove(ActionFactory.PASTE.getId());
    }

    @Override
    protected void configureToolBar(IToolBarManager mgr)
    {
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, mRemoveAction);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, mCancelAction);
        IAction copyAction = (IAction)fGlobalActions.get(ActionFactory.COPY.getId());
        if(copyAction != null) {
            mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, copyAction);
        }
        IAction selectAllAction = (IAction)fGlobalActions.get(ActionFactory.SELECT_ALL.getId());
        if(selectAllAction != null) {
            mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, selectAllAction);
        }
        super.configureToolBar(mgr);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, mScrollLockAction);
    }

    @Override
    public void dispose()
    {
        mDisposed = true;
        ((NSISConsole)getConsole()).getDocument().removeDocumentListener(this);
        MakeNSISRunner.removeListener(this);
        mCancelAction = null;
        mRemoveAction = null;
        if (mScrollLockAction != null) {
            mScrollLockAction.dispose();
            mScrollLockAction = null;
        }
        if(mMenuManager != null) {
            mMenuManager.dispose();
        }
        super.dispose();
    }

    private class ScrollLockAction extends Action
    {
        public ScrollLockAction()
        {
            super(EclipseNSISPlugin.getResourceString("console.scroll.lock.action.name")); //$NON-NLS-1$

            setToolTipText(EclipseNSISPlugin.getResourceString("console.scroll.lock.action.tooltip")); //$NON-NLS-1$
            ImageManager imageManager = EclipseNSISPlugin.getImageManager();
            setImageDescriptor(imageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("console.scroll.lock.action.image"))); //$NON-NLS-1$
            setHoverImageDescriptor(getImageDescriptor());
            setDisabledImageDescriptor(imageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("console.scroll.lock.action.disabled.image"))); //$NON-NLS-1$
            boolean checked = !isAutoScroll();
            setChecked(checked);
        }

        @Override
        public void run()
        {
            setAutoScroll(!isChecked());
        }

        public void dispose()
        {
        }
    }

    private class ConsoleRemoveAction extends Action
    {
        public ConsoleRemoveAction()
        {
            super();
            setText(EclipseNSISPlugin.getResourceString("console.remove.action.name")); //$NON-NLS-1$
            setToolTipText(getText());
            setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("console.remove.action.image"))); //$NON-NLS-1$
            setHoverImageDescriptor(getImageDescriptor());
            setDisabledImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("console.remove.action.disabled.image"))); //$NON-NLS-1$
        }

        @Override
        public void run()
        {
            NSISConsoleFactory.closeConsole();
        }
    }}
