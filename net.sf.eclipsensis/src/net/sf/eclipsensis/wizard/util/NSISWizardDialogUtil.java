/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import java.io.File;
import java.net.URL;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISContentBrowserDialog;

import org.eclipse.jface.fieldassist.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISWizardDialogUtil
{
    public static final String CONTROL_DECORATION = "ControlDecoration"; //$NON-NLS-1$

    public static final FieldDecoration REQ_FIELD_DECORATION = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED);

    public static final FieldDecoration DISABLED_REQ_FIELD_DECORATION;

    public static final String LABEL = "LABEL"; //$NON-NLS-1$

    public static final String IMAGE = "IMAGE"; //$NON-NLS-1$

    static {
        final Image[] image = {REQ_FIELD_DECORATION.getImage()};
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                image[0] = new Image(image[0].getDevice(), image[0], SWT.IMAGE_DISABLE);
            }
        });
        DISABLED_REQ_FIELD_DECORATION = new FieldDecoration(image[0], null);
    }

    private NSISWizardDialogUtil()
    {
    }

    private static void addSlave(MasterSlaveController masterSlaveController, Control slave)
    {
        if (masterSlaveController != null) {
            masterSlaveController.addSlave(slave);
        }
    }

    public static Label createRequiredFieldsLabel(Composite parent)
    {
        Composite c = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = 0;
        c.setLayout(layout);
        c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        Label l = new Label(c, SWT.NONE);
        l.setImage(REQ_FIELD_DECORATION.getImage());
        l.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        l = NSISWizardDialogUtil.createLabel(c, "wizard.required.text", true, null, false); //$NON-NLS-1$
        FontData[] fd = l.getFont().getFontData();
        for (int i = 0; i < fd.length; i++) {
            fd[i].height *= 0.9;
        }
        final Font f = new Font(l.getDisplay(), fd);
        l.setFont(f);
        l.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                f.dispose();
            }
        });
        return l;
    }

    public static Label createLabel(Composite parent, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        return createLabel(parent, SWT.LEFT | SWT.WRAP, labelResource, enabled, masterSlaveController, isRequired);
    }

    public static void setEnabled(Control c, boolean enabled)
    {
        if (c != null && !c.isDisposed()) {
            c.setEnabled(enabled);
            updateDecoration(c, enabled);
        }
    }

    public static boolean isDecorated(Control c)
    {
        try {
            ControlDecoration decoration = (ControlDecoration)c.getData(CONTROL_DECORATION);
            return decoration != null;
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * @param c
     * @param enabled
     */
    private static void updateDecoration(Control c, boolean enabled)
    {
        ControlDecoration decoration = (ControlDecoration)c.getData(CONTROL_DECORATION);
        if (decoration != null) {
            FieldDecoration d = (enabled?REQ_FIELD_DECORATION:DISABLED_REQ_FIELD_DECORATION);
            decoration.setImage(d.getImage());
            decoration.setDescriptionText(d.getDescription());
        }
    }

    /**
     * @param parent
     * @param style
     * @param labelResource
     * @param enabled
     * @param masterSlaveController
     * @param isRequired
     * @return
     */
    public static Label createLabel(Composite parent, int style, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Label l = new Label(parent, style);
        if (labelResource != null) {
            l.setText(EclipseNSISPlugin.getResourceString(labelResource));
        }
        l.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        l.setEnabled(enabled);
        if (isRequired) {
            decorate(l);
        }

        addSlave(masterSlaveController, l);
        return l;
    }

    /**
     * @param control
     */
    public static void decorate(Control control)
    {
        ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP, null);
        control.setData(CONTROL_DECORATION, controlDecoration);
        updateDecoration(control, control.isEnabled());
        if (controlDecoration.getImage() != null) {
            ((GridData)control.getLayoutData()).horizontalIndent += controlDecoration.getImage().getBounds().width;
        }
    }

    public static Composite checkParentLayoutColumns(Composite parent, int numColumns)
    {
        Composite parent2 = parent;
        GridLayout layout = (GridLayout)parent2.getLayout();
        if (layout.numColumns < numColumns) {
            parent2 = new Composite(parent2, SWT.NONE);
            GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
            data.horizontalSpan = layout.numColumns;
            parent2.setLayoutData(data);

            layout = new GridLayout(numColumns, layout.makeColumnsEqualWidth);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            parent2.setLayout(layout);
        }
        return parent2;
    }

    public static Text createText(Composite parent, String value, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        GridLayout layout = (GridLayout)parent.getLayout();
        Composite parent2 = checkParentLayoutColumns(parent, 2);
        Label l = createLabel(parent2, labelResource, enabled, masterSlaveController, isRequired);
        Text t = createText(parent2, value, layout.numColumns - 1, enabled, masterSlaveController);
        t.setData(LABEL, l);
        return t;
    }

    public static Text createText(Composite parent, String value, int horizontalSpan, boolean enabled, MasterSlaveController masterSlaveController)
    {
        return createText(parent, value, SWT.SINGLE | SWT.BORDER, horizontalSpan, enabled, masterSlaveController);
    }

    public static Text createText(Composite parent, String value, int style, int horizontalSpan, boolean enabled, MasterSlaveController masterSlaveController)
    {
        Text t = new Text(parent, style);
        t.setText(value);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = horizontalSpan;
        t.setLayoutData(data);
        t.setEnabled(enabled);
        addSlave(masterSlaveController, t);

        return t;
    }

    public static Text createDirectoryBrowser(Composite parent, String value, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, 3);
        GridLayout layout = (GridLayout)parent2.getLayout();
        final Text t = createText(parent2, value, labelResource, enabled, masterSlaveController, isRequired);
        ((GridData)t.getLayoutData()).horizontalSpan = layout.numColumns - 2;

        final Button button = new Button(parent2, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Shell shell = button.getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.NONE);
                String directory = dialog.open();
                if (!Common.isEmpty(directory)) {
                    t.setText(IOUtility.encodePath(directory));
                }
            }
        });
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        button.setLayoutData(data);
        button.setEnabled(enabled);
        addSlave(masterSlaveController, button);

        return t;
    }

    public static Text createFileBrowser(Composite parent, String value, final boolean isSave, final String[] filterNames, final String[] filterExtensions, String labelResource, boolean enabled,
            MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, 3);
        GridLayout layout = (GridLayout)parent2.getLayout();
        final Text t = createText(parent2, value, labelResource, enabled, masterSlaveController, isRequired);
        ((GridData)t.getLayoutData()).horizontalSpan = layout.numColumns - 2;

        createFileBrowserButton(parent2, isSave, filterNames, filterExtensions, t, enabled, masterSlaveController);

        return t;
    }

    /**
     * @param parent
     * @param isSave
     * @param filterNames
     * @param filterExtensions
     * @param masterSlaveController
     * @param t
     */
    public static Button createFileBrowserButton(Composite parent, final boolean isSave, final String[] filterNames, final String[] filterExtensions, final Text t, boolean enabled,
            MasterSlaveController masterSlaveController)
    {
        final Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Shell shell = button.getShell();
                FileDialog dialog = new FileDialog(shell, (isSave?SWT.SAVE:SWT.OPEN));
                dialog.setFileName(IOUtility.decodePath(t.getText()));
                dialog.setFilterNames(filterNames);
                dialog.setFilterExtensions(filterExtensions);
                String file = dialog.open();
                if (!Common.isEmpty(file)) {
                    t.setText(IOUtility.encodePath(file));
                }
            }
        });
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        button.setLayoutData(data);
        button.setEnabled(enabled);
        addSlave(masterSlaveController, button);

        return button;
    }

    public static Text createImageBrowser(Composite parent, String value, Point size, final String[] filterNames, final String[] filterExtensions, String labelResource, boolean enabled,
            MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, 2);
        GridLayout layout = (GridLayout)parent2.getLayout();
        Label l = createLabel(parent2, labelResource, enabled, masterSlaveController, isRequired);
        parent2 = new Composite(parent2, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = layout.numColumns - 1;
        parent2.setLayoutData(data);
        layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent2.setLayout(layout);
        final Text t = createText(parent2, value, 1, enabled, masterSlaveController);
        t.setData(LABEL, l);

        createFileBrowserButton(parent2, false, filterNames, filterExtensions, t, enabled, masterSlaveController);

        final Label l2 = new Label(parent2, SWT.BORDER | SWT.SHADOW_IN | SWT.CENTER);
        data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        if (size != null) {
            if (size.x != SWT.DEFAULT) {
                data.widthHint = size.x;
            }
            if (size.y != SWT.DEFAULT) {
                data.heightHint = size.y;
            }
        }
        l2.setLayoutData(data);

        l2.setEnabled(enabled);
        t.setData(IMAGE, l2);
        addSlave(masterSlaveController, l2);

        t.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                loadImage((Text)e.widget);
            }
        });

        return t;
    }

    public static void loadImage(Text t)
    {
        String fileName = IOUtility.decodePath(t.getText());
        Label l = (Label)t.getData(IMAGE);
        if (l != null) {
            Image image = null;
            if (!Common.isEmpty(fileName) && IOUtility.isValidFile(fileName)) {
                try {
                    URL url = new File(fileName).toURI().toURL();
                    if (EclipseNSISPlugin.getImageManager().containsImage(url)) {
                        image = EclipseNSISPlugin.getImageManager().getImage(url);
                    }
                    else {
                        GridData data = (GridData)l.getLayoutData();
                        Point size = l.computeSize(data.widthHint, data.heightHint);
                        ImageData[] imageData = new ImageLoader().load(fileName);
                        if (!Common.isEmptyArray(imageData)) {
                            ImageData bestData = null;
                            int bestArea = 0;
                            Display display = l.getDisplay();
                            int displayDepth = display.getDepth();
                            for (int i = 0; i < imageData.length; i++) {
                                if (imageData[i].width <= size.x && imageData[i].height <= size.y && imageData[i].depth <= displayDepth) {
                                    if (bestData != null) {
                                        int imageArea = imageData[i].width * imageData[i].height;
                                        if ((imageArea < bestArea) || (imageArea == bestArea && imageData[i].depth < bestData.depth)) {
                                            continue;
                                        }
                                    }
                                    bestData = imageData[i];
                                    bestArea = bestData.width * bestData.height;
                                }
                            }
                            if (bestData == null) {
                                bestData = imageData[0];
                            }
                            image = new Image(display, bestData);
                            EclipseNSISPlugin.getImageManager().putImage(url, image);
                        }
                    }
                }
                catch (Exception ex) {
                    image = null;
                }
            }
            l.setImage(image);
            l.setData((image == null?null:image.getImageData()));
        }
    }

    public static Combo createCombo(Composite parent, String[] items, int selectedItem, boolean isReadOnly, String labelResource, boolean enabled, MasterSlaveController masterSlaveController,
            boolean isRequired)
    {
        return createCombo(parent, items, (selectedItem >= 0 && selectedItem < items.length?items[selectedItem]:""), isReadOnly, labelResource, enabled, masterSlaveController, isRequired); //$NON-NLS-1$
    }

    public static Combo createCombo(Composite parent, String[] items, String selectedItem, boolean isReadOnly, String labelResource, boolean enabled, MasterSlaveController masterSlaveController,
            boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, 2);
        Label l = null;
        if (labelResource != null) {
            l = createLabel(parent2, labelResource, enabled, masterSlaveController, isRequired);
        }

        Combo c = createCombo(parent2, ((GridLayout)parent2.getLayout()).numColumns - (l == null?0:1), items, selectedItem, isReadOnly, enabled, masterSlaveController);
        if (l != null) {
            c.setData(LABEL, l);
        }

        return c;
    }

    /**
     * @param parent
     * @param items
     * @param selectedItem
     * @param isReadOnly
     * @param enabled
     * @param masterSlaveController
     * @return
     */
    public static Combo createCombo(Composite parent, int horizontalSpan, String[] items, String selectedItem, boolean isReadOnly, boolean enabled, MasterSlaveController masterSlaveController)
    {
        Combo c = new Combo(parent, SWT.DROP_DOWN | (isReadOnly?SWT.READ_ONLY:SWT.NONE));
        populateCombo(c, items, selectedItem);

        GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        data.horizontalSpan = horizontalSpan;
        c.setLayoutData(data);
        c.setEnabled(enabled);
        addSlave(masterSlaveController, c);
        return c;
    }

    public static void populateCombo(Combo combo, String[] items, String selectedItem)
    {
        combo.removeAll();
        if (!Common.isEmptyArray(items)) {
            for (int i = 0; i < items.length; i++) {
                combo.add(items[i]);
            }
        }
        combo.setText(selectedItem);
    }

    public static Combo createCombo(Composite parent, int horizontalSpan, String[] items, int selectedIndex, boolean isReadOnly, boolean enabled, MasterSlaveController masterSlaveController)
    {
        Combo c = new Combo(parent, SWT.DROP_DOWN | (isReadOnly?SWT.READ_ONLY:SWT.NONE));
        populateCombo(c, items, (selectedIndex >= 0 && selectedIndex < items.length?items[selectedIndex]:"")); //$NON-NLS-1$

        GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        data.horizontalSpan = horizontalSpan;
        c.setLayoutData(data);
        c.setEnabled(enabled);
        addSlave(masterSlaveController, c);
        return c;
    }

    public static ColorEditor createColorEditor(Composite parent, RGB value, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, 2);
        Label l = createLabel(parent2, labelResource, enabled, masterSlaveController, isRequired);

        GridLayout layout = (GridLayout)parent2.getLayout();
        ColorEditor ce = new ColorEditor(parent2);
        ce.setRGB(value);
        Button b = ce.getButton();
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.horizontalSpan = layout.numColumns - 1;
        b.setLayoutData(data);
        b.setEnabled(enabled);
        addSlave(masterSlaveController, b);
        b.setData(LABEL, l);

        return ce;
    }

    public static Button[] createRadioGroup(Composite parent, String[] items, int selectedItem, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, labelResource != null?2:1);
        Label l = null;
        if(labelResource != null) {
            l = createLabel(parent2, labelResource, enabled, masterSlaveController, isRequired);
        }

        GridLayout layout = (GridLayout)parent2.getLayout();
        parent2 = new Composite(parent2, SWT.NONE);
        GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gd.horizontalSpan = layout.numColumns - (l==null?0:1);
        parent2.setLayoutData(gd);
        layout = new GridLayout(items.length, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent2.setLayout(layout);

        Button[] buttons = new Button[items.length];
        int selectedIndex = -1;
        for (int i = 0; i < items.length; i++) {
            buttons[i] = new Button(parent2, SWT.RADIO | SWT.LEFT);
            buttons[i].setText(items[i]);
            buttons[i].setData(new Integer(i));
            if (i == selectedItem) {
                selectedIndex = i;
                buttons[i].setSelection(true);
            }
            buttons[i].setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            buttons[i].setEnabled(enabled);
            addSlave(masterSlaveController, buttons[i]);
            if(l != null) {
                buttons[i].setData(LABEL, l);
            }
        }
        if (selectedIndex < 0 && items.length > 0) {
            buttons[0].setSelection(true);
        }
        return buttons;
    }

    public static Group createGroup(Composite parent, int numColumns, String labelResource, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        if (!Common.isEmpty(labelResource)) {
            group.setText(EclipseNSISPlugin.getResourceString(labelResource));
        }
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout(numColumns, false);
        group.setLayout(layout);
        addSlave(masterSlaveController, group);

        return group;
    }

    public static Button createCheckBox(Composite parent, String labelResource, boolean state, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        return createToggleButton(parent, labelResource, SWT.CHECK, state, enabled, masterSlaveController, isRequired);
    }

    public static Button createRadioButton(Composite parent, String labelResource, boolean state, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        return createToggleButton(parent, labelResource, SWT.RADIO, state, enabled, masterSlaveController, isRequired);
    }

    private static Button createToggleButton(Composite parent, String labelResource, int style, boolean state, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Button button = new Button(parent, style | SWT.LEFT);
        if (labelResource != null) {
            button.setText(EclipseNSISPlugin.getResourceString(labelResource));
        }
        button.setSelection(state);
        button.setEnabled(enabled);
        addSlave(masterSlaveController, button);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        button.setLayoutData(data);
        return button;
    }

    /**
     * @param composite
     * @param labelResource
     * @param value
     * @param settings
     * @param enabled
     * @param m2
     * @param isRequired
     * @return
     */
    public static Combo createContentBrowser(Composite parent, String labelResource, String value, String[] items, final NSISWizard wizard, boolean enabled,
            MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Composite parent2 = checkParentLayoutColumns(parent, 3);
        int numColumns = ((GridLayout)parent2.getLayout()).numColumns;
        GridData gd;
        Label l = createLabel(parent2, labelResource, enabled, masterSlaveController, isRequired);
        Composite composite = new Composite(parent2, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = numColumns - 1;
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        final Combo c2 = createCombo(composite, 1, items, value, false, enabled, masterSlaveController);
        gd = (GridData)c2.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        c2.setData(LABEL, l);

        final Button b = new Button(composite, SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        b.setLayoutData(gd);
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                NSISContentBrowserDialog dialog = new NSISContentBrowserDialog(b.getShell(), wizard.getSettings());
                if (dialog.open() == Window.OK) {
                    INSISInstallElement element = dialog.getSelectedElement();
                    StringBuffer text = new StringBuffer(""); //$NON-NLS-1$
                    if (element instanceof NSISInstallFiles.FileItem) {
                        String destination = ((NSISInstallFiles)element.getParent()).getDestination();
                        text.append(destination);
                        if (!destination.endsWith("\\")) { //$NON-NLS-1$
                            text.append("\\"); //$NON-NLS-1$
                        }
                        text.append(new File(((NSISInstallFiles.FileItem)element).getName()).getName());
                    }
                    else if (element instanceof NSISInstallFile) {
                        String destination = ((NSISInstallFile)element).getDestination();
                        text.append(destination);
                        if (!destination.endsWith("\\")) { //$NON-NLS-1$
                            text.append("\\"); //$NON-NLS-1$
                        }
                        text.append(new File(((NSISInstallFile)element).getName()).getName());
                    }
                    else if (element instanceof NSISInstallDirectory) {
                        String destination = ((NSISInstallDirectory)element).getDestination();
                        text.append(destination);
                        if (!destination.endsWith("\\")) { //$NON-NLS-1$
                            text.append("\\"); //$NON-NLS-1$
                        }
                        text.append(new File(((NSISInstallDirectory)element).getName()).getName());
                    }
                    c2.setText(text.toString());
                }
            }
        });
        addSlave(masterSlaveController, b);
        return c2;
    }
}
