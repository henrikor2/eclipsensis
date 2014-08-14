/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.beans.PropertyChangeEvent;
import java.util.*;

import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.requests.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.winapi.WinAPI;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.tools.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.ui.views.properties.IPropertySource;

public abstract class InstallOptionsWidgetEditPart extends InstallOptionsEditPart implements IDirectEditLabelProvider, IExtendedEditLabelProvider
{
    private DirectEditManager mManager;
    private String mDirectEditLabel;
    private String mExtendedEditLabel;
    private boolean mNeedsRefresh = false;
    private Label mToolTip;

    private Label getToolTip()
    {
        if(mToolTip == null) {
            mToolTip = new Label();
            mToolTip.setBorder(new MarginBorder(1,2,1,2));
            mToolTip.setOpaque(true);
            resetToolTipText();
        }
        return mToolTip;
    }

    protected void resetToolTipText()
    {
        if(mToolTip != null) {
            mToolTip.setText(getTypeName());
            mToolTip.setBackgroundColor(ColorConstants.tooltipBackground);
            mToolTip.setForegroundColor(ColorConstants.tooltipForeground);
            Dimension dim = FigureUtilities.getStringExtents(mToolTip.getText(),FontUtility.getInstallOptionsFont());
            dim.expand(8,6);
            mToolTip.setSize(dim);
        }
    }

    public InstallOptionsWidgetEditPart()
    {
        super();
        setDirectEditLabel(InstallOptionsPlugin.getResourceString(getDirectEditLabelProperty()));
        setExtendedEditLabel(InstallOptionsPlugin.getResourceString(getExtendedEditLabelProperty()));
    }

    @Override
    public DragTracker getDragTracker(Request req)
    {
        return new InstallOptionsDragEditPartsTracker(this);
    }

    protected String getDirectEditLabelProperty()
    {
        return "direct.edit.label"; //$NON-NLS-1$
    }

    protected String getExtendedEditLabelProperty()
    {
        return ""; //$NON-NLS-1$
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key)
    {
        if(IDirectEditLabelProvider.class.equals(key)) {
            if(!Common.isEmpty(getDirectEditLabel())) {
                return this;
            }
        }
        else if(IExtendedEditLabelProvider.class.equals(key)) {
            if(!Common.isEmpty(getExtendedEditLabel())) {
                return this;
            }
        }
        return super.getAdapter(key);
    }

    public String getDirectEditLabel()
    {
        return mDirectEditLabel;
    }

    public void setDirectEditLabel(String directEditLabel)
    {
        mDirectEditLabel = directEditLabel;
    }

    public String getExtendedEditLabel()
    {
        return mExtendedEditLabel;
    }

    public void setExtendedEditLabel(String extendedEditLabel)
    {
        mExtendedEditLabel = extendedEditLabel;
    }

    @Override
    protected final AccessibleEditPart createAccessible() {
        return new AccessibleGraphicalEditPart(){
            @Override
            public void getValue(AccessibleControlEvent e) {
                e.result = getAccessibleControlEventResult();
            }

            @Override
            public void getName(AccessibleEvent e) {
                e.result = getTypeName();
            }
        };
    }

    public boolean isNeedsRefresh()
    {
        return mNeedsRefresh;
    }

    public void setNeedsRefresh(boolean needsRefresh)
    {
        mNeedsRefresh = needsRefresh;
    }

    public final void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (InstallOptionsModel.PROPERTY_POSITION.equals(prop)) {
            refreshVisuals();
        }
        else if (InstallOptionsModel.PROPERTY_INDEX.equals(prop)) {
            Command command = getParent().getCommand(new ReorderPartRequest(this,((Integer)evt.getNewValue()).intValue()));
            if(command != null) {
                getViewer().getEditDomain().getCommandStack().execute(command);
            }
        }
        else {
            doPropertyChange(evt);
            synchronized(this) {
                if(isNeedsRefresh()) {
                    IInstallOptionsFigure figure = (IInstallOptionsFigure)getFigure();
                    figure.refresh();
                    setNeedsRefresh(false);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if(InstallOptionsModel.PROPERTY_FLAGS.equals(prop)) {
            List<String> oldFlags = new ArrayList<String>((List<String>)evt.getOldValue());
            oldFlags.removeAll((List<String>)evt.getNewValue());
            for (Iterator<String> iter = oldFlags.iterator(); iter.hasNext();) {
                String flag = iter.next();
                if(getInstallOptionsWidget().getTypeDef().getFlags().contains(flag)) {
                    handleFlagRemoved(flag);
                }
            }
            List<String> newFlags = new ArrayList<String>((List<String>)evt.getNewValue());
            newFlags.removeAll((List<String>)evt.getOldValue());
            for (Iterator<String> iter = newFlags.iterator(); iter.hasNext();) {
                String flag = iter.next();
                if(getInstallOptionsWidget().getTypeDef().getFlags().contains(flag)) {
                    handleFlagAdded(flag);
                }
            }
        }
    }

    protected void handleFlagRemoved(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_DISABLED)) {
            ((IInstallOptionsFigure)getFigure()).setDisabled(false);
            setNeedsRefresh(true);
        }
        else if(supportsScrolling()) {
            if(flag.equals(InstallOptionsModel.FLAGS_HSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setHScroll(false);
                setNeedsRefresh(true);
            }
            else if(flag.equals(InstallOptionsModel.FLAGS_VSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setVScroll(false);
                setNeedsRefresh(true);
            }
        }
    }

    protected void handleFlagAdded(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_DISABLED)) {
            ((IInstallOptionsFigure)getFigure()).setDisabled(true);
            setNeedsRefresh(true);
        }
        else if(supportsScrolling()) {
            if(flag.equals(InstallOptionsModel.FLAGS_HSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setHScroll(true);
                setNeedsRefresh(true);
            }
            else if(flag.equals(InstallOptionsModel.FLAGS_VSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setVScroll(true);
                setNeedsRefresh(true);
            }
        }
    }

    @Override
    protected final IFigure createFigure()
    {
        IInstallOptionsFigure figure2 = createInstallOptionsFigure();
        figure2.setFocusTraversable(true);
        figure2.setToolTip(getToolTip());
        return figure2;
    }

    /**
     * Updates the visual aspect of this.
     */
    @Override
    protected void refreshVisuals()
    {
        InstallOptionsWidget widget = (InstallOptionsWidget)getInstallOptionsElement();
        Position pos = widget.getPosition();
        pos = widget.toGraphical(pos);
        Rectangle r = new Rectangle(pos.left,pos.top,pos.right-pos.left+1,pos.bottom-pos.top+1);

        ((GraphicalEditPart)getParent()).setLayoutConstraint(this, getFigure(), r);
    }

    @Override
    public void performRequest(Request request)
    {
        if(request.getType().equals(IInstallOptionsConstants.REQ_EXTENDED_EDIT)) {
            performExtendedEdit(request);
        }
        else if(request.getType().equals(RequestConstants.REQ_OPEN)) {
            performExtendedEdit(new ExtendedEditRequest(this));
        }
        else if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            performDirectEdit();
        }
        super.performRequest(request);
    }

    protected void performExtendedEdit(Request request)
    {
        InstallOptionsExtendedEditPolicy policy = (InstallOptionsExtendedEditPolicy)getEditPolicy(InstallOptionsExtendedEditPolicy.ROLE);
        if(policy != null) {
            IExtendedEditSupport support = (IExtendedEditSupport)getAdapter(IExtendedEditSupport.class);
            if(support != null) {
                if(support.performExtendedEdit()) {
                    ((ExtendedEditRequest)request).setNewValue(support.getNewValue());
                    Command command = policy.getCommand(request);
                    if(command != null) {
                        getViewer().getEditDomain().getCommandStack().execute(command);
                    }
                }
            }
        }
    }

    protected void performDirectEdit()
    {
        if(mManager == null) {
            mManager = creatDirectEditManager(this, createCellEditorLocator((IInstallOptionsFigure)getFigure()));
        }
        if(mManager != null) {
            mManager.show();
        }
    }

    public InstallOptionsWidget getInstallOptionsWidget()
    {
        return (InstallOptionsWidget)getModel();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();
    }

    protected boolean supportsScrolling()
    {
        return true;
    }

    private static class InstallOptionsDragEditPartsTracker extends DragEditPartsTracker
    {
        public InstallOptionsDragEditPartsTracker(EditPart sourceEditPart)
        {
            super(sourceEditPart);
        }

        @Override
        protected boolean handleDragStarted()
        {
            List<?> list = getSourceEditPart().getViewer().getSelectedEditParts();
            if(!Common.isEmptyCollection(list)) {
                for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
                    EditPart part = (EditPart)iter.next();
                    if(part instanceof InstallOptionsWidgetEditPart) {
                        if(((InstallOptionsWidget)part.getModel()).isLocked()) {
                            return false;
                        }
                    }
                }
            }
            return super.handleDragStarted();
        }
    }

    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    public static abstract class NTFigure extends ScrollBarsFigure
    {
        private boolean mDisabled = false;
        private boolean mHScroll = false;
        private boolean mVScroll = false;
        private ScrollBar mHScrollBar;
        private ScrollBar mVScrollBar;
        private Label mGlassPanel;

        private Rectangle mChildBounds = new Rectangle(0,0,0,0);

        public NTFigure(IPropertySource propertySource)
        {
            super();
            setOpaque(true);
            setLayoutManager(new XYLayout());
            mHScrollBar = new ScrollBar();
            mHScrollBar.setHorizontal(true);
            mHScrollBar.setVisible(false);
            add(mHScrollBar);
            mVScrollBar = new ScrollBar();
            mVScrollBar.setHorizontal(false);
            add(mVScrollBar);
            mGlassPanel = new Label();
            mGlassPanel.setOpaque(false);
            add(mGlassPanel);
            createChildFigures();
            init(propertySource);
        }

        @SuppressWarnings("unchecked")
        protected void init(IPropertySource propertySource)
        {
            List<String> flags = (List<String>)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
            setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
            setHScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_HSCROLL));
            setVScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_VSCROLL));
            setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
        }

        public void setDisabled(boolean disabled)
        {
            if(mDisabled != disabled) {
                mDisabled = disabled;
                refresh();
            }
        }

        public boolean isDisabled()
        {
            return mDisabled;
        }

        public void setHScroll(boolean hScroll)
        {
            if(mHScroll != hScroll) {
                mHScroll = hScroll;
                refresh();
            }
        }

        public void setVScroll(boolean vScroll)
        {
            if(mVScroll != vScroll) {
                mVScroll = vScroll;
                refresh();
            }
        }

        public boolean isHScroll()
        {
            return mHScroll;
        }

        public boolean isVScroll()
        {
            return mVScroll;
        }

        public void refresh()
        {
            updateBounds(bounds);
            layout();
            revalidate();
        }

        private void updateBounds(Rectangle newBounds)
        {
            Rectangle childBounds = new Rectangle(0,0,newBounds.width,newBounds.height);
            setConstraint(mGlassPanel, childBounds.getCopy());
            int hbarHeight = WinAPI.INSTANCE.getSystemMetrics (WinAPI.SM_CYHSCROLL);
            int vbarWidth = WinAPI.INSTANCE.getSystemMetrics (WinAPI.SM_CXVSCROLL);
            mHScrollBar.setVisible(mHScroll);
            if(mHScroll) {
                setConstraint(mHScrollBar, new Rectangle(0,newBounds.height-hbarHeight,
                                newBounds.width-(mVScroll?vbarWidth:0), hbarHeight));
                childBounds.height -= hbarHeight;
            }
            mVScrollBar.setVisible(mVScroll);
            if(mVScroll) {
                setConstraint(mVScrollBar, new Rectangle(newBounds.width-vbarWidth,0,
                                vbarWidth, newBounds.height-(mHScroll?hbarHeight:0)));
                childBounds.width -= vbarWidth;
            }
            if(!mChildBounds.equals(childBounds)) {
                setChildConstraints(childBounds);
                mChildBounds = childBounds;
            }
        }

        @Override
        public void setBounds(Rectangle newBounds)
        {
            if(!bounds.getSize().equals(newBounds.getSize())) {
                updateBounds(newBounds);
            }
            super.setBounds(newBounds);
        }

        @Override
        protected boolean supportsScrollBars()
        {
            return true;
        }

        protected abstract void setChildConstraints(Rectangle bounds);
        protected abstract void createChildFigures();
    }

    protected abstract DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, CellEditorLocator locator);
    protected abstract CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure);
    protected abstract IInstallOptionsFigure createInstallOptionsFigure();
    protected abstract String getAccessibleControlEventResult();
}
