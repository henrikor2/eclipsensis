/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.editors;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class MultiLineTextCellEditor extends DialogCellEditor
{
    private ILabelProvider mLabelProvider = null;
    private boolean mOnlyNumbers = false;

    /**
     * @param parent
     */
    public MultiLineTextCellEditor(Composite parent)
    {
        super(parent);
    }

    public boolean isOnlyNumbers()
    {
        return mOnlyNumbers;
    }

    public void setOnlyNumbers(boolean onlyNumbers)
    {
        mOnlyNumbers = onlyNumbers;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
     */
    @Override
    protected Object openDialogBox(Control cellEditorWindow)
    {
        Object oldValue = getValue();
        MultiLineTextDialog dialog = new MultiLineTextDialog(cellEditorWindow.getShell(),(String)oldValue);
        dialog.setValidator(getValidator());
        int result = dialog.open();
        return (result == Window.OK?dialog.getValue():oldValue);
    }

    @Override
    protected void updateContents(Object value)
    {
        Label label = getDefaultLabel();
        if (label != null && !label.isDisposed()) {
            String text = "";//$NON-NLS-1$
            if (value != null) {
                if(mLabelProvider != null) {
                    text = mLabelProvider.getText(value);
                }
                else {
                    text = value.toString();
                }
            }
            label.setText(text);
        }
    }

    public ILabelProvider getLabelProvider()
    {
        return mLabelProvider;
    }

    public void setLabelProvider(ILabelProvider labelProvider)
    {
        mLabelProvider = labelProvider;
    }

    private class MultiLineTextDialog extends Dialog
    {
        private String mValue;
        private ICellEditorValidator mValidator;

        public MultiLineTextDialog(Shell parent, String value)
        {
            super(parent);
            setShellStyle(getShellStyle()|SWT.RESIZE);
            mValue = TypeConverter.ESCAPED_STRING_CONVERTER.asString(value);
        }

        public ICellEditorValidator getValidator()
        {
            return mValidator;
        }

        public void setValidator(ICellEditorValidator validator)
        {
            mValidator = validator;
        }

        @Override
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getResourceString("multiline.text.dialog.title")); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getShellImage());
        }

        public String getValue()
        {
            return TypeConverter.ESCAPED_STRING_CONVERTER.asType(mValue);
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);
            final Text text = new Text(composite,SWT.LEFT|SWT.BORDER|SWT.MULTI|SWT.WRAP|SWT.V_SCROLL);
            if(isOnlyNumbers()) {
                text.addVerifyListener(new NumberVerifyListener());
            }
            text.setText(mValue);
            initializeDialogUnits(text);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = convertWidthInCharsToPixels(50);
            data.heightHint = convertHeightInCharsToPixels(4);
            text.setLayoutData(data);
            text.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    mValue = text.getText();
                }
            });
            return composite;
        }

        @Override
        protected void okPressed()
        {
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                String error = validator.isValid(getValue());
                if(!Common.isEmpty(error)) {
                    Common.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error, //$NON-NLS-1$
                                     InstallOptionsPlugin.getShellImage());
                    return;
                }
            }
            super.okPressed();
        }
    }
}
