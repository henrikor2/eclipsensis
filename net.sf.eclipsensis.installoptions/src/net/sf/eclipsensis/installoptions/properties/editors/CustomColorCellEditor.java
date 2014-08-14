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

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class CustomColorCellEditor extends DialogCellEditor
{
    private static final int DEFAULT_EXTENT= 16;
    private static final int GAP = 6;

    private Composite mComposite;
    private Label mColorLabel;
    private Label mRGBLabel;
    private Image mImage;
    private ILabelProvider mLabelProvider;
    private RGB mDefaultColor;
    private Button mButton;

    public CustomColorCellEditor(Composite parent)
    {
        this(parent, SWT.NONE);
    }

    public CustomColorCellEditor(Composite parent, int style)
    {
        super(parent, style);
    }

    private ImageData createColorImage(Control control, RGB color)
    {
        GC gc = new GC(control);
        FontMetrics fm = gc.getFontMetrics();
        int size = fm.getAscent();
        gc.dispose();

        int indent = 6;
        int extent = DEFAULT_EXTENT;
        if (control instanceof Table) {
            extent = ((Table) control).getItemHeight() - 1;
        }
        else if (control instanceof Tree) {
            extent = ((Tree) control).getItemHeight() - 1;
        }

        if (size > extent) {
            size = extent;
        }

        int width = indent + size;
        int height = extent;

        int xoffset = indent;
        int yoffset = (height - size) / 2;

        RGB black = new RGB(0, 0, 0);
        PaletteData dataPalette = new PaletteData(new RGB[] {black, black, color});
        ImageData data = new ImageData(width, height, 4, dataPalette);
        data.transparentPixel = 0;

        int end = size - 1;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (x == 0 || y == 0 || x == end || y == end) {
                    data.setPixel(x + xoffset, y + yoffset, 1);
                }
                else {
                    data.setPixel(x + xoffset, y + yoffset, 2);
                }
            }
        }

        return data;
    }

    /* (non-Javadoc)
     * Method declared on DialogCellEditor.
     */
    @Override
    protected Control createContents(Composite cell)
    {
        Color bg = cell.getBackground();
        mComposite = new Composite(cell, getStyle());
        mComposite.setBackground(bg);
        mComposite.setLayout(new ColorCellLayout());
        mColorLabel = new Label(mComposite, SWT.LEFT);
        mColorLabel.setBackground(bg);
        mRGBLabel = new Label(mComposite, SWT.LEFT);
        mRGBLabel.setBackground(bg);
        mRGBLabel.setFont(cell.getFont());
        mButton = new Button(mComposite,SWT.PUSH);
        mButton.setText(InstallOptionsPlugin.getResourceString("restore.default.label")); //$NON-NLS-1$
        mButton.setFont(cell.getFont());
        mButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                markDirty();
                doSetValue(null);
                fireApplyEditorValue();
            }
        });
        return mComposite;
    }
    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    @Override
    public void dispose()
    {
        if (mImage != null) {
            mImage.dispose();
            mImage = null;
        }
        super.dispose();
    }

    /* (non-Javadoc)
     * Method declared on DialogCellEditor.
     */
    @Override
    protected Object openDialogBox(Control cellEditorWindow)
    {
        ColorDialog dialog = new ColorDialog(cellEditorWindow.getShell());
        RGB value = (RGB)getValue();
        if (value != null) {
            dialog.setRGB(value);
        }
        else {
            dialog.setRGB(getDefaultColor());
        }
        value = dialog.open();
        return dialog.getRGB();
    }

    public ILabelProvider getLabelProvider()
    {
        return mLabelProvider;
    }

    public void setLabelProvider(ILabelProvider labelProvider)
    {
        mLabelProvider = labelProvider;
    }

    public RGB getDefaultColor()
    {
        if(mDefaultColor == null) {
            mDefaultColor = new RGB(0,0,0);
        }
        return mDefaultColor;
    }

    public void setDefaultColor(RGB defaultColor)
    {
        mDefaultColor = defaultColor;
    }

    /* (non-Javadoc)
     * Method declared on DialogCellEditor.
     */
    @Override
    protected void updateContents(Object value)
    {
        RGB rgb = (RGB) value;
        if (rgb == null) {
            rgb = getDefaultColor();
        }
        if (mImage != null) {
            mImage.dispose();
        }

        ImageData id = createColorImage(mColorLabel.getParent().getParent(), rgb);
        ImageData mask = id.getTransparencyMask();
        mImage = new Image(mColorLabel.getDisplay(), id, mask);
        mColorLabel.setImage(mImage);

        if(getLabelProvider() != null) {
            mRGBLabel.setText(getLabelProvider().getText(rgb));
        }
        else {
            mRGBLabel.setText("(" + rgb.red + "," + rgb.green + "," + rgb.blue + ")");//$NON-NLS-4$//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
        }
    }

    /**
     * Internal class for laying out this cell editor.
     */
    private class ColorCellLayout extends Layout
    {
        @Override
        public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache)
        {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
                return new Point(wHint, hHint);
            }
            Point colorSize = mColorLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            Point rgbSize = mRGBLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            Point buttonSize = mButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            return new Point(colorSize.x + rgbSize.x + buttonSize.x + 2*GAP,
                            Math.max(Math.max(colorSize.y, rgbSize.y),buttonSize.y));
        }

        @Override
        public void layout(Composite composite, boolean flushCache)
        {
            Rectangle bounds = composite.getClientArea();
            Point colorSize = mColorLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            Point rgbSize = mRGBLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            Point buttonSize = mButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);

            int ty = (bounds.height - rgbSize.y) / 2;
            if (ty < 0) {
                ty = 0;
            }

            mColorLabel.setBounds(-1, 0, colorSize.x, colorSize.y);
            mRGBLabel.setBounds(colorSize.x + GAP - 1, ty,
                                bounds.width - colorSize.x - buttonSize.x - 2*GAP,
                                bounds.height);
            mButton.setBounds(bounds.width - buttonSize.x - 1, 0,
                              buttonSize.x, bounds.height);
        }
    }
}
