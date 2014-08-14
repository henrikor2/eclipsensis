/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dialogs;

import java.beans.*;
import java.util.Collection;

import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.installoptions.*;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;
import net.sf.eclipsensis.installoptions.properties.tabbed.CustomTabbedPropertySheetPage;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

public class InstallOptionsWidgetEditorDialog extends StatusMessageDialog implements PropertyChangeListener, IModelCommandListener, IPropertySourceProvider, ITabbedPropertySheetPageContributor
{
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 480;

    private static final String HELP_CONTEXT = IInstallOptionsConstants.PLUGIN_CONTEXT_PREFIX+"installoptions_widgeteditor_context"; //$NON-NLS-1$

    private IPropertySheetPage mPage = new CustomTabbedPropertySheetPage(this);
    private int mOldValidateFixMode;
//    private IPropertySheetPage mPage = new CustomPropertySheetPage();
    private InstallOptionsDialog mDialog;
    private InstallOptionsWidget mCurrentWidget;
    private INISection mSection;
    private boolean mCreateMode = false;

    public InstallOptionsWidgetEditorDialog(Shell parent, INIFile iniFile)
    {
        this(parent, iniFile, null);
    }

    public InstallOptionsWidgetEditorDialog(Shell parent, INIFile iniFile, INISection section)
    {
        super(parent);
        mDialog = InstallOptionsDialog.loadINIFile(iniFile);
        mSection = section;
        mCurrentWidget = (InstallOptionsWidget)mDialog.getElement(mSection);
        mCreateMode = (mCurrentWidget==null);
        mOldValidateFixMode = iniFile.getValidateFixMode();
        iniFile.setValidateFixMode(mCreateMode?INILine.VALIDATE_FIX_ALL:INILine.VALIDATE_FIX_ERRORS);
        setTitle(mCreateMode?InstallOptionsPlugin.getResourceString("create.control.dialog.title"):InstallOptionsPlugin.getResourceString("edit.control.dialog.title")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public boolean close()
    {
        if(mSection != null) {
            INIFile iniFile = (INIFile)mSection.getParent();
            if(iniFile != null) {
                iniFile.setValidateFixMode(mOldValidateFixMode);
            }
        }
        if(mCurrentWidget != null) {
            mCurrentWidget.removeModelCommandListener(InstallOptionsWidgetEditorDialog.this);
            mCurrentWidget.removePropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
            if(mCurrentWidget.getParent() != null) {
                mCurrentWidget.getParent().removePropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
            }
        }
        return super.close();
    }

    public IPropertySource getPropertySource(Object object)
    {
        if(object instanceof IPropertySource) {
            if(object instanceof CustomPropertySourceWrapper) {
                return (IPropertySource)object;
            }
            return new CustomPropertySourceWrapper((IPropertySource)object);
        }
        else if(object instanceof IPropertySourceProvider && !this.getClass().equals(object.getClass())) {
            return getPropertySource(((IPropertySourceProvider)object).getPropertySource(object));
        }
        return null;
    }

    public String getContributorId()
    {
        return IInstallOptionsConstants.TABBED_PROPERTIES_CONTRIBUTOR_ID;
    }

    public void executeModelCommand(ModelCommandEvent event)
    {
        Object obj = event.getModel();
        if(Common.objectsAreEqual(obj, mCurrentWidget)) {
            event.getCommand().execute();
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_INDEX)) {
            mDialog.moveChild(mCurrentWidget, ((Integer)evt.getNewValue()).intValue());
        }
        else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_CHILDREN)) {
            if(Common.objectsAreEqual(mCurrentWidget,evt.getOldValue()) && evt.getNewValue() instanceof InstallOptionsWidget) {
                InstallOptionsWidget widget = (InstallOptionsWidget)evt.getNewValue();
                mCurrentWidget.removeModelCommandListener(InstallOptionsWidgetEditorDialog.this);
                mCurrentWidget.removePropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
                mCurrentWidget = widget;
                mSection = mCurrentWidget.getSection();
                mCurrentWidget.addModelCommandListener(InstallOptionsWidgetEditorDialog.this);
                mCurrentWidget.addPropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        mPage.selectionChanged(null, new StructuredSelection(mCurrentWidget));
                    }
                });
            }
        }
    }

    @Override
    protected Point getInitialSize()
    {
        Point size = super.getInitialSize();
        if(size.x < MIN_WIDTH) {
            size.x = MIN_WIDTH;
        }
        if(size.y < MIN_HEIGHT) {
            size.y = MIN_HEIGHT;
        }
        return size;
    }

    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
    {
        return super.createButton(parent, id, label, false);
    }

    @Override
    protected Control createControl(Composite parent)
    {
        Composite propertyComposite = new Composite(parent,SWT.BORDER);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = layout.marginHeight = 0;
        propertyComposite.setLayout(layout);
        if(mPage instanceof Page) {
            ((Page)mPage).init(new IPageSite() {
                public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider)
                {
                }

                public IActionBars getActionBars()
                {
                    return null;
                }

                public IWorkbenchPage getPage()
                {
                    return getWorkbenchWindow().getActivePage();
                }

                public ISelectionProvider getSelectionProvider()
                {
                    return null;
                }

                public Shell getShell()
                {
                    return getWorkbenchWindow().getShell();
                }

                public IWorkbenchWindow getWorkbenchWindow()
                {
                    return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                }

                public void setSelectionProvider(ISelectionProvider provider)
                {
                }

                @SuppressWarnings("unchecked")
                public Object getAdapter(Class adapter)
                {
                    return null;
                }

                @SuppressWarnings("unchecked")
                public Object getService(Class api)
                {
                    return null;
                }

                @SuppressWarnings("unchecked")
                public boolean hasService(Class api)
                {
                    return false;
                }
            });
        }
        if(mPage instanceof PropertySheetPage) {
            ((PropertySheetPage)mPage).setPropertySourceProvider(this);
        }
        mPage.createControl(propertyComposite);
        mPage.setActionBars(new DummyActionBars());
        final Control control = mPage.getControl();
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        if(control instanceof Tree) {
            final Tree tree = (Tree)control;
            data.heightHint = tree.getItemHeight()*13+(tree.getLinesVisible()?12*tree.getGridLineWidth():0)+
                             (tree.getHeaderVisible()?tree.getHeaderHeight():0)+2*tree.getBorderWidth()+
                             (tree.getHorizontalBar() != null?tree.getHorizontalBar().getSize().x:0);
            tree.addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    Rectangle area = tree.getClientArea();
                    TreeColumn[] columns = tree.getColumns();
                    if (area.width > 0) {
                        columns[0].setWidth(area.width * 40 / 100);
                        columns[1].setWidth(area.width - columns[0].getWidth() - 4);
                    }
                }
            });
        }
        else if(control instanceof Composite){
            control.addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    ((Composite)control).layout(true, true);
                }
            });
        }
        control.setLayoutData(data);
        ISelection selection;
        if(mCurrentWidget == null) {
            Collection<InstallOptionsModelTypeDef> typeDefs = InstallOptionsModel.INSTANCE.getControlTypeDefs();
            if(typeDefs.size() > 0) {
                InstallOptionsModelTypeDef typeDef = typeDefs.iterator().next();
                InstallOptionsElementFactory factory = InstallOptionsElementFactory.getFactory(typeDef.getType());
                mCurrentWidget = (InstallOptionsWidget)factory.getNewObject();
                mDialog.addChild(mCurrentWidget);
            }
        }

        if(mCurrentWidget != null) {
            mCurrentWidget.addModelCommandListener(InstallOptionsWidgetEditorDialog.this);
            mCurrentWidget.addPropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
            if(mCurrentWidget.getParent() != null) {
                mCurrentWidget.getParent().addPropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
            }
            selection = new StructuredSelection(mCurrentWidget);
        }
        else {
            selection = StructuredSelection.EMPTY;
        }

        mPage.selectionChanged(null, selection);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(mPage.getControl(),HELP_CONTEXT);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(propertyComposite,HELP_CONTEXT);

        return propertyComposite;
    }

    @Override
    protected void okPressed()
    {
        if(mDialog.canUpdateINIFile()) {
            mDialog.updateINIFile();
            if(mSection == null) {
                mSection = mCurrentWidget.getSection();
            }
            super.okPressed();
        }
        else {
            super.cancelPressed();
        }
    }

    public INISection getSection()
    {
        return mSection;
    }


    private final class CustomPropertySourceWrapper extends PropertySourceWrapper
    {
        private CustomPropertySourceWrapper(IPropertySource delegate)
        {
            super(delegate);
        }

        @Override
        public Object getEditableValue()
        {
            Object object = super.getEditableValue();
            if(!((object instanceof CustomPropertySourceWrapper))) {
                if (object instanceof IPropertySource) {
                    object = new CustomPropertySourceWrapper((IPropertySource)object);
                }
            }
            return object;
        }

        @Override
        public IPropertyDescriptor[] getPropertyDescriptors()
        {
            IPropertyDescriptor[] descriptors = super.getPropertyDescriptors();
            if(!Common.isEmptyArray(descriptors)) {
                IPropertyDescriptor[] wrappers = new IPropertyDescriptor[descriptors.length];
                for (int i = 0; i < descriptors.length; i++) {
                    if(descriptors[i] != null && !(descriptors[i] instanceof PropertyDescriptorWrapper)) {
                        wrappers[i] = new PropertyDescriptorWrapper(descriptors[i]);
                    }
                    else {
                        wrappers[i] = descriptors[i];
                    }
                }
                return wrappers;
            }
            return descriptors;
        }

        @Override
        public void setPropertyValue(Object id, Object value)
        {
            if(InstallOptionsModel.PROPERTY_TYPE.equals(id)) {
                propertyChange(new PropertyChangeEvent(getDelegate(),(String)id,getPropertyValue(id),value));
            }
            else {
                super.setPropertyValue(id, value);
            }
        }
    }

    private class DummyActionBars implements IActionBars
    {
        private IMenuManager mMenuManager = null;
        private IToolBarManager mToolBarManager = null;
        private IStatusLineManager mStatusLineManager = null;

        public void clearGlobalActionHandlers()
        {
        }

        public IAction getGlobalActionHandler(String actionId)
        {
            return null;
        }

        public IMenuManager getMenuManager()
        {
            if(mMenuManager == null) {
                mMenuManager = new MenuManager();
            }
            return mMenuManager;
        }

        public IStatusLineManager getStatusLineManager()
        {
            if(mStatusLineManager == null) {
                mStatusLineManager = new StatusLineManager() {

                    @Override
                    public void setErrorMessage(Image image, String message)
                    {
                        if(Common.isEmpty(message)) {
                            setMessage(image,message);
                        }
                        else {
                            updateStatus(new DialogStatus(IStatus.ERROR,message,image));
                        }
                    }

                    @Override
                    public void setErrorMessage(String message)
                    {
                        setErrorMessage(null, message);
                    }

                    @Override
                    public void setMessage(Image image, String message)
                    {
                        updateStatus(new DialogStatus(IStatus.OK,message,image));
                    }

                    @Override
                    public void setMessage(String message)
                    {
                        setMessage(null,message);
                    }
                };
            }
            return mStatusLineManager;
        }

        public IToolBarManager getToolBarManager()
        {
            if(mToolBarManager == null) {
                mToolBarManager = new ToolBarManager();
            }
            return mToolBarManager;
        }

        public void setGlobalActionHandler(String actionId, IAction handler)
        {
        }

        public void updateActionBars()
        {
        }

        public IServiceLocator getServiceLocator()
        {
            return null;
        }
    }

    private class PropertyDescriptorWrapper extends PropertyDescriptor
    {
        private IPropertyDescriptor mDelegate;
        public PropertyDescriptorWrapper(IPropertyDescriptor delegate)
        {
            super(delegate.getId(), delegate.getDisplayName());
            mDelegate = delegate;
        }

        @Override
        public CellEditor createPropertyEditor(Composite parent)
        {
            return mDelegate.createPropertyEditor(parent);
        }

        @Override
        public String getCategory()
        {
            return mDelegate.getCategory();
        }

        @Override
        public String getDescription()
        {
            return mDelegate.getDescription();
        }

        @Override
        public String getDisplayName()
        {
            return mDelegate.getDisplayName();
        }

        @Override
        public String[] getFilterFlags()
        {
            return mDelegate.getFilterFlags();
        }

        @Override
        public Object getHelpContextIds()
        {
            return HELP_CONTEXT;
        }

        @Override
        public Object getId()
        {
            return mDelegate.getId();
        }

        @Override
        public ILabelProvider getLabelProvider()
        {
            return mDelegate.getLabelProvider();
        }

        @Override
        public boolean isCompatibleWith(IPropertyDescriptor anotherProperty)
        {
            return mDelegate.isCompatibleWith(anotherProperty);
        }
    }
}
