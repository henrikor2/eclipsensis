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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.editable.*;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.util.TypeConverter;

import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class InstallOptionsTextEditPart extends InstallOptionsEditableElementEditPart<TextCellEditor>
{
    @Override
    protected String getDirectEditLabelProperty()
    {
        return "text.direct.edit.label"; //$NON-NLS-1$
    }

    @Override
    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        return new TextFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
    }

    @Override
    protected EditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new EditableElementDirectEditPolicy() {
            @Override
            protected String getDirectEditValue(DirectEditRequest edit)
            {
                String text = super.getDirectEditValue(edit);
                if(getInstallOptionsEditableElement().getTypeDef().getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE) &&
                   getInstallOptionsEditableElement().getFlags().contains(InstallOptionsModel.FLAGS_MULTILINE)) {
                    text = TypeConverter.ESCAPED_STRING_CONVERTER.asType(text);
                }
                return text;
            }
        };
    }

    @Override
    protected void handleFlagAdded(String flag)
    {
        TextFigure figure = (TextFigure)getFigure();
        if(flag.equals(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
            figure.setOnlyNumbers(true);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_MULTILINE)) {
            figure.setMultiLine(true);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_NOWORDWRAP)) {
            figure.setNoWordWrap(true);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_READONLY)) {
            figure.setReadOnly(true);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagAdded(flag);
        }
    }
    @Override
    protected void handleFlagRemoved(String flag)
    {
        TextFigure figure = (TextFigure)getFigure();
        if(flag.equals(InstallOptionsModel.FLAGS_ONLY_NUMBERS)) {
            figure.setOnlyNumbers(false);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_MULTILINE)) {
            figure.setMultiLine(false);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_NOWORDWRAP)) {
            figure.setNoWordWrap(false);
            setNeedsRefresh(true);
        }
        else if(flag.equals(InstallOptionsModel.FLAGS_READONLY)) {
            figure.setReadOnly(false);
            setNeedsRefresh(true);
        }
        else {
            super.handleFlagRemoved(flag);
        }
    }
    /**
     * @return
     */
    @Override
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("text.type.name"); //$NON-NLS-1$
    }

    @Override
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class<TextCellEditor> clasz, CellEditorLocator locator)
    {
        return new InstallOptionsTextEditManager(part, clasz, locator);
    }

    @Override
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new TextCellEditorLocator((TextFigure)getFigure());
    }

    @Override
    protected Class<TextCellEditor> getCellEditorClass()
    {
        return TextCellEditor.class;
    }
}
