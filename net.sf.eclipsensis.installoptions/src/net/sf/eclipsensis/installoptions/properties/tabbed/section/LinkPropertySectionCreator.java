/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.properties.tabbed.section;

import java.beans.*;

import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsCommandHelper;
import net.sf.eclipsensis.installoptions.properties.descriptors.PropertyDescriptorHelper;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class LinkPropertySectionCreator extends UneditableElementPropertySectionCreator
{
    public LinkPropertySectionCreator(InstallOptionsLink link)
    {
        super(link);
    }

    @Override
    protected Control createAppearancePropertySection(final Composite parent, final TabbedPropertySheetWidgetFactory widgetFactory, final InstallOptionsCommandHelper commandHelper)
    {
        final Composite composite = widgetFactory.createComposite(parent);
        GridLayout layout = new GridLayout(2,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData data = new GridData(SWT.FILL, SWT.FILL,true,true);
        data.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        composite.setLayoutData(data);
        Control c = super.createAppearancePropertySection(composite, widgetFactory, commandHelper);
        data = (GridData)c.getLayoutData();
        if(data == null) {
            data = new GridData();
            c.setLayoutData(data);
        }
        data.horizontalSpan = 2;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = data.verticalAlignment = SWT.FILL;

        Composite composite2 = parent;
        layout = (GridLayout)parent.getLayout();
        if(layout.numColumns != 2) {
            composite2 = widgetFactory.createComposite(composite2);
            data = new GridData(SWT.FILL,SWT.FILL,true,false);
            data.horizontalSpan = layout.numColumns;
            composite2.setLayoutData(data);

            layout = new GridLayout(2,false);
            layout.marginHeight = layout.marginWidth = 0;
            composite2.setLayout(layout);
        }
        final IPropertyDescriptor descriptor = getWidget().getPropertyDescriptor(InstallOptionsModel.PROPERTY_TXTCOLOR);
        final ICellEditorValidator validator = PropertyDescriptorHelper.getCellEditorValidator((PropertyDescriptor) descriptor);

        CLabel label = widgetFactory.createCLabel(composite2, descriptor.getDisplayName());
        label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));

        composite2 = widgetFactory.createComposite(composite2);
        composite2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        layout = new GridLayout(3,false);
        layout.marginHeight = layout.marginWidth = 0;
        composite2.setLayout(layout);

        final ILabelProvider labelProvider = descriptor.getLabelProvider();
        RGB rgb = (RGB)getWidget().getPropertyValue(InstallOptionsModel.PROPERTY_TXTCOLOR);
        final Text colorText = widgetFactory.createText(composite2, labelProvider.getText(rgb), SWT.FLAT|SWT.BORDER);
        colorText.setEditable(false);
        colorText.setBackground(colorText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        colorText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));

        final ColorEditor colorEditor = new ColorEditor(composite2, SWT.FLAT|widgetFactory.getOrientation());
        colorEditor.setRGB(rgb==null?InstallOptionsLink.DEFAULT_TXTCOLOR:rgb);
        colorEditor.getButton().setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        colorEditor.getButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                RGB newRGB = colorEditor.getRGB();
                if(validator != null) {
                    String error = validator.isValid(newRGB);
                    if(!Common.isEmpty(error)) {
                        Common.openError(((Control)e.widget).getShell(), error, InstallOptionsPlugin.getShellImage());
                        newRGB = InstallOptionsLink.DEFAULT_TXTCOLOR; //Default
                    }
                }
                updateRGB(commandHelper, descriptor, labelProvider, colorText, newRGB);
            }
        });

        //Reject focus
        colorText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                colorEditor.getButton().setFocus();
            }
        });

        Button resetButton = widgetFactory.createButton(composite2,
                        InstallOptionsPlugin.getResourceString("restore.default.label"),SWT.PUSH); //$NON-NLS-1$
        resetButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        resetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                updateRGB(commandHelper, descriptor, labelProvider, colorText, null);
            }
        });

        final PropertyChangeListener propertyListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_TXTCOLOR)) {
                    RGB newRGB = checkDefault((RGB)evt.getNewValue());
                    RGB oldRGB = checkDefault(colorEditor.getRGB());
                    if(!Common.objectsAreEqual(oldRGB, newRGB)) {
                        colorEditor.setRGB(newRGB==null?InstallOptionsLink.DEFAULT_TXTCOLOR:newRGB);
                        colorText.setText(labelProvider.getText(newRGB));
                    }
                }
                else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_MULTILINE)) {
                    Control[] controls = composite.getChildren();
                    for (int i = 0; i < controls.length; i++) {
                        controls[i].dispose();
                    }
                    Control c = LinkPropertySectionCreator.super.createAppearancePropertySection(composite, widgetFactory, commandHelper);
                    GridData data = (GridData)c.getLayoutData();
                    if(data == null) {
                        data = new GridData();
                        c.setLayoutData(data);
                    }
                    data.horizontalSpan = 2;
                    data.grabExcessHorizontalSpace = true;
                    data.grabExcessVerticalSpace = true;
                    data.horizontalAlignment = data.verticalAlignment = SWT.FILL;
                    forceLayout(composite);
                }
            }
        };
        getWidget().addPropertyChangeListener(propertyListener);
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                getWidget().removePropertyChangeListener(propertyListener);
            }
        });

        return parent;
    }

    private RGB checkDefault(RGB rgb)
    {
        if(InstallOptionsLink.DEFAULT_TXTCOLOR.equals(rgb)) {
            return null;
        }
        return rgb;
    }

    /**
     * @param commandHelper
     * @param descriptor
     * @param labelProvider
     * @param colorText
     * @param newRGB
     */
    private void updateRGB(final InstallOptionsCommandHelper commandHelper, final IPropertyDescriptor descriptor, final ILabelProvider labelProvider, final Text colorText, RGB rgb)
    {
        RGB newRGB = rgb;
        RGB oldRGB = (RGB)getWidget().getPropertyValue(InstallOptionsModel.PROPERTY_TXTCOLOR);
        if(InstallOptionsLink.DEFAULT_TXTCOLOR.equals(newRGB)) {
            newRGB = null;
        }
        if(!Common.objectsAreEqual(oldRGB, newRGB)) {
            colorText.setText(labelProvider.getText(newRGB));
            commandHelper.propertyChanged(InstallOptionsModel.PROPERTY_TXTCOLOR,
                            descriptor.getDisplayName(), getWidget(), newRGB);
        }
    }

    @Override
    protected boolean isTextPropertyMultiline()
    {
        return ((InstallOptionsLink)getWidget()).isMultiLine();
    }
}
