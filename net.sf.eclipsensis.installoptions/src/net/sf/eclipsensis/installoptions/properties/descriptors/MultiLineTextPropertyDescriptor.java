/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.descriptors;

import java.beans.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.editors.MultiLineTextCellEditor;
import net.sf.eclipsensis.util.NumberVerifyListener;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class MultiLineTextPropertyDescriptor extends TextPropertyDescriptor implements PropertyChangeListener
{
    private static final String PROPERTY_LABEL_PROVIDER="LabelProvider"; //$NON-NLS-1$

    private InstallOptionsElement mSource;
    private boolean mMultiLine = true;
    private boolean mOnlyNumbers = false;
    private PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * @param id
     * @param displayName
     */
    public MultiLineTextPropertyDescriptor(InstallOptionsElement source, Object id, String displayName)
    {
        super(id, displayName);
        mSource = source;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_FLAGS)) {
            List<?> newFlags = (List<?>)evt.getNewValue();
            setMultiLine(newFlags.contains(InstallOptionsModel.FLAGS_MULTILINE));
            setOnlyNumbers(newFlags.contains(InstallOptionsModel.FLAGS_ONLY_NUMBERS));
        }
        else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_MULTILINE)) {
            setMultiLine(((Boolean)evt.getNewValue()).booleanValue());
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    public boolean isMultiLine()
    {
        return mMultiLine;
    }

    public void setMultiLine(boolean multiLine)
    {
        if(mMultiLine != multiLine) {
            boolean oldMultiLine = mMultiLine;
            mMultiLine = multiLine;
            mPropertyChangeSupport.firePropertyChange(InstallOptionsModel.PROPERTY_MULTILINE,oldMultiLine,mMultiLine);
        }
    }

    public boolean isOnlyNumbers()
    {
        return mOnlyNumbers;
    }

    public void setOnlyNumbers(boolean onlyNumbers)
    {
        if(mOnlyNumbers != onlyNumbers) {
            boolean oldOnlyNumbers = mOnlyNumbers;
            mOnlyNumbers = onlyNumbers;
            mPropertyChangeSupport.firePropertyChange(InstallOptionsModel.FLAGS_ONLY_NUMBERS,oldOnlyNumbers,mOnlyNumbers);
        }
    }

    @Override
    public void setLabelProvider(ILabelProvider provider)
    {
        ILabelProvider oldProvider = getLabelProvider();
        super.setLabelProvider(provider);
        mPropertyChangeSupport.firePropertyChange(PROPERTY_LABEL_PROVIDER,oldProvider,getLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent)
    {
        final ProxyTextCellEditor editor = new ProxyTextCellEditor(parent, isMultiLine(), getLabelProvider());
        final PropertyChangeListener listener = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_MULTILINE)) {
                    editor.setStyle(((Boolean)evt.getNewValue()).booleanValue()?1:0);
                    editor.create();
                }
                else if(evt.getPropertyName().equals(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
                    editor.setOnlyNumbers(((Boolean)evt.getNewValue()).booleanValue());
                }
                else if(evt.getPropertyName().equals(PROPERTY_LABEL_PROVIDER)) {
                    editor.setLabelProvider((ILabelProvider)evt.getNewValue());
                }
            }
        };
        addPropertyChangeListener(listener);

        final PropertyChangeListener listener2 = new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(getId())) {
                    editor.setValue(evt.getNewValue());
                }
            }
        };
        mSource.addPropertyChangeListener(listener2);
        editor.setOnlyNumbers(isOnlyNumbers());
        editor.getParent().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                removePropertyChangeListener(listener);
                mSource.removePropertyChangeListener(listener2);
            }
        });
        editor.setValidator(getValidator());
        return editor;
    }

    private class ProxyTextCellEditor extends CellEditor
    {
        private Composite mParent;
        private CellEditor mDelegate;
        private Object mValue;
        private ListenerList mListeners = new ListenerList(ListenerList.IDENTITY);
        private ListenerList mPropertyChangeListeners = new ListenerList(ListenerList.IDENTITY);
        private ICellEditorValidator mValidator;
        private boolean mProxyOnlyNumbers = false;
        private VerifyListener mNumberVerifyListener;
        private ILabelProvider mLabelProvider;

        public ProxyTextCellEditor(Composite parent, boolean multiLine, ILabelProvider labelProvider)
        {
            super(parent,(multiLine?1:0));
            mParent = parent;
            setLabelProvider(labelProvider);

        }

        public void setLabelProvider(ILabelProvider labelProvider)
        {
            mLabelProvider = labelProvider;
            if(mDelegate instanceof MultiLineTextCellEditor) {
                ((MultiLineTextCellEditor)mDelegate).setLabelProvider(labelProvider);
            }
        }

        private VerifyListener getNumberVerifyListener()
        {
            if(mNumberVerifyListener == null) {
                mNumberVerifyListener = new NumberVerifyListener();
            }
            return mNumberVerifyListener;
        }

        public void create()
        {
            dispose();
            create(mParent);
        }

        public Composite getParent()
        {
            return mParent;
        }

        public void setOnlyNumbers(boolean onlyNumbers)
        {
            if(mProxyOnlyNumbers != onlyNumbers) {
                mProxyOnlyNumbers = onlyNumbers;
                updateNumberVerifier();
            }
        }

        private void updateNumberVerifier()
        {
            if(mDelegate != null) {
                if(mDelegate instanceof TextCellEditor) {
                    Text text = (Text)mDelegate.getControl();
                    if(text != null) {
                        if(mProxyOnlyNumbers) {
                            text.addVerifyListener(getNumberVerifyListener());
                        }
                        else {
                            text.removeVerifyListener(getNumberVerifyListener());
                        }
                    }
                }
                else {
                    ((MultiLineTextCellEditor)mDelegate).setOnlyNumbers(mProxyOnlyNumbers);
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createControl(Composite parent)
        {
            createInnerEditor(parent,getStyle()==1);
            return mDelegate.getControl();
        }

        protected void createInnerEditor(Composite parent, boolean multiLine)
        {
            Object value = null;

            if(mDelegate != null) {
                mDelegate.dispose();
                mDelegate = null;
                value = getValue();
            }
            if(multiLine) {
                mDelegate = new MultiLineTextCellEditor(parent);
                ((MultiLineTextCellEditor)mDelegate).setLabelProvider(mLabelProvider);
            }
            else {
                mDelegate = new TextCellEditor(parent);
            }
            updateNumberVerifier();
            if (getValidator() != null) {
                mDelegate.setValidator(getValidator());
            }
            if(mListeners != null && mListeners.size() > 0) {
                Object[] listeners = mListeners.getListeners();
                for(int i=0; i<listeners.length; i++) {
                    mDelegate.addListener((ICellEditorListener)listeners[i]);
                }
            }
            if(mPropertyChangeListeners != null && mPropertyChangeListeners.size() > 0) {
                Object[] listeners = mPropertyChangeListeners.getListeners();
                for(int i=0; i<listeners.length; i++) {
                    mDelegate.addPropertyChangeListener((IPropertyChangeListener)listeners[i]);
                }
            }
            mDelegate.setValue(value==null?"":value); //$NON-NLS-1$
        }

        @Override
        public void activate()
        {
            if(mDelegate != null) {
                mDelegate.activate();
                mDelegate.getControl().setVisible(true);
            }
            super.activate();
        }

        @Override
        public void deactivate()
        {
            if(mDelegate != null) {
                mDelegate.deactivate();
            }
            super.deactivate();
        }

        @Override
        public void addListener(ICellEditorListener listener)
        {
            mListeners.add(listener);
            if(mDelegate != null) {
                mDelegate.addListener(listener);
            }
        }

        @Override
        public void addPropertyChangeListener(IPropertyChangeListener listener)
        {
            mPropertyChangeListeners.add(listener);
            if(mDelegate != null) {
                mDelegate.addPropertyChangeListener(listener);
            }
        }

        @Override
        public void dispose()
        {
            if(mDelegate != null) {
                mDelegate.dispose();
            }
            super.dispose();
        }

        @Override
        public String getErrorMessage()
        {
            if(mDelegate != null) {
                return mDelegate.getErrorMessage();
            }
            else {
                return null;
            }
        }

        @Override
        public ICellEditorValidator getValidator()
        {
            return mValidator;
        }

        @Override
        public boolean isCopyEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isCopyEnabled();
            }
            else {
                return super.isCopyEnabled();
            }
        }

        @Override
        public boolean isCutEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isCutEnabled();
            }
            else {
                return super.isCutEnabled();
            }
        }

        @Override
        public boolean isDeleteEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isDeleteEnabled();
            }
            else {
                return super.isDeleteEnabled();
            }
        }

        @Override
        public boolean isDirty()
        {
            if(mDelegate != null) {
                return mDelegate.isDirty();
            }
            else {
                return super.isDirty();
            }
        }

        @Override
        public boolean isFindEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isFindEnabled();
            }
            else {
                return super.isFindEnabled();
            }
        }

        @Override
        public boolean isPasteEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isPasteEnabled();
            }
            else {
                return super.isPasteEnabled();
            }
        }

        @Override
        public boolean isRedoEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isRedoEnabled();
            }
            else {
                return super.isRedoEnabled();
            }
        }

        @Override
        public boolean isSelectAllEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isSelectAllEnabled();
            }
            else {
                return super.isSelectAllEnabled();
            }
        }

        @Override
        public boolean isUndoEnabled()
        {
            if(mDelegate != null) {
                return mDelegate.isUndoEnabled();
            }
            else {
                return super.isUndoEnabled();
            }
        }

        @Override
        public boolean isValueValid()
        {
            if(mDelegate != null) {
                return mDelegate.isValueValid();
            }
            else {
                return super.isValueValid();
            }
        }

        @Override
        public void performCopy()
        {
            if(mDelegate != null) {
                mDelegate.performCopy();
            }
        }

        @Override
        public void performCut()
        {
            if(mDelegate != null) {
                mDelegate.performCut();
            }
        }

        @Override
        public void performDelete()
        {
            if(mDelegate != null) {
                mDelegate.performDelete();
            }
        }

        @Override
        public void performFind()
        {
            if(mDelegate != null) {
                mDelegate.performFind();
            }
        }

        @Override
        public void performPaste()
        {
            if(mDelegate != null) {
                mDelegate.performPaste();
            }
        }

        @Override
        public void performRedo()
        {
            if(mDelegate != null) {
                mDelegate.performRedo();
            }
        }

        @Override
        public void performSelectAll()
        {
            if(mDelegate != null) {
                mDelegate.performSelectAll();
            }
        }

        @Override
        public void performUndo()
        {
            if(mDelegate != null) {
                mDelegate.performUndo();
            }
        }

        @Override
        public void removeListener(ICellEditorListener listener)
        {
            mListeners.remove(listener);
            if(mDelegate != null) {
                mDelegate.removeListener(listener);
            }
        }

        @Override
        public void removePropertyChangeListener(
                IPropertyChangeListener listener)
        {
            mPropertyChangeListeners.remove(listener);
            if(mDelegate != null) {
                mDelegate.removePropertyChangeListener(listener);
            }
        }

        @Override
        public void setFocus()
        {
            if(mDelegate != null) {
                mDelegate.setFocus();
            }
        }

        @Override
        public void setValidator(ICellEditorValidator validator)
        {
            mValidator = validator;
            if(mDelegate != null) {
                mDelegate.setValidator(validator);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
         */
        @Override
        protected Object doGetValue()
        {
            if(mDelegate != null) {
                mValue = mDelegate.getValue();
            }

            if(mValue == null) {
                mValue = ""; //$NON-NLS-1$
            }
            return mValue;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
         */
        @Override
        protected void doSetValue(Object value)
        {
            Object value2 = value;
            if(value2 == null) {
                value2 = ""; //$NON-NLS-1$
            }
            mValue = value2;
            if(mDelegate != null) {
                mDelegate.setValue(value2);
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
         */
        @Override
        protected void doSetFocus()
        {
        }
    }
}
