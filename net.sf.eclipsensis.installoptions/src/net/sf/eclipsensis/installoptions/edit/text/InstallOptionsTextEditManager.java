/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.text;


import net.sf.eclipsensis.installoptions.figures.TextFigure;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.NumberVerifyListener;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;


public class InstallOptionsTextEditManager extends InstallOptionsGenericTextEditManager
{
    public InstallOptionsTextEditManager(GraphicalEditPart source, Class<TextCellEditor> editorType, CellEditorLocator locator)
    {
        super(source, editorType, locator);
    }

    @Override
    protected String getInitialText(InstallOptionsWidget control)
    {
        String text = super.getInitialText(control);
        if(control.getTypeDef().getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE) &&
           control.getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE)) {
            text = TypeConverter.ESCAPED_STRING_CONVERTER.asString(text);
        }
        return text;
    }

    @Override
    protected TextCellEditor createCellEditor(Composite composite)
    {
        TextCellEditor cellEditor = super.createCellEditor(composite);
        TextFigure figure = (TextFigure)getEditPart().getFigure();
        if(figure.isOnlyNumbers()) {
            ((Text)cellEditor.getControl()).addVerifyListener(new NumberVerifyListener());
        }
        return cellEditor;
    }

    @Override
    protected int getCellEditorStyle()
    {
        TextFigure figure = (TextFigure)getEditPart().getFigure();
        int style = SWT.LEFT;
        if(figure.isMultiLine()) {
            style |= SWT.MULTI;
            if(figure.isNoWordWrap() && !figure.isHScroll()) {
                style |= SWT.WRAP;
            }
        }
        if(figure.isHScroll()) {
            style |= SWT.H_SCROLL;
        }
        if(figure.isVScroll()) {
            style |= SWT.V_SCROLL;
        }
        return style;
    }
}