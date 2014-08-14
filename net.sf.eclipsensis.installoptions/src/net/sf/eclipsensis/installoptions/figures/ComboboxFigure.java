/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;
import net.sf.eclipsensis.util.ColorManager;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class ComboboxFigure extends AbstractInstallOptionsFigure implements IListItemsFigure
{
    private ListFigure mListFigure;
    private ComboFigure mComboFigure;
    private int mComboHeight;
    private boolean mShowDropdown;

    /**
     *
     */
    public ComboboxFigure(Composite parent, final IPropertySource propertySource)
    {
        super();
        Combo cb = new Combo(parent,SWT.DROP_DOWN);
        cb.setVisible(false);
        cb.setBounds(-100,-100,10,10);
        Point p = cb.computeSize(SWT.DEFAULT,SWT.DEFAULT);
        cb.dispose();
        mComboHeight = p.y;

        setLayoutManager(new XYLayout());
        Rectangle[] bounds = calculateBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
        mComboFigure = new ComboFigure(parent, new CustomPropertySourceWrapper(propertySource, bounds[0]));
        mListFigure = new ListFigure(parent,  new CustomPropertySourceWrapper(propertySource, bounds[1]), SWT.SINGLE);
        mListFigure.setBorder(new LineBorder(ColorManager.getColor(ColorManager.BLACK)));
        mListFigure.setVisible(mShowDropdown);
        add(mComboFigure);
        add(mListFigure);
    }

    public void setShowDropdown(boolean flag)
    {
        if(mShowDropdown != flag) {
            mShowDropdown = flag;
            mListFigure.setVisible(mShowDropdown);
        }
    }

    @Override
    public boolean isClickThrough()
    {
        return true;
    }

    @Override
    public boolean isDefaultClickThroughFigure()
    {
        return false;
    }

    @Override
    protected boolean isTransparentAt(int x, int y)
    {
        if(!mShowDropdown) {
            Rectangle r = mComboFigure.getBounds();
            return !r.contains(x, y);
        }
        return false;
    }

    public ListFigure getListFigure()
    {
        return mListFigure;
    }

    public ComboFigure getComboFigure()
    {
        return mComboFigure;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#setDisabled(boolean)
     */
    public void setDisabled(boolean disabled)
    {
        mListFigure.setDisabled(disabled);
        mComboFigure.setDisabled(disabled);
    }

    public boolean isDisabled()
    {
        return mComboFigure.isDisabled();
    }

    public void setState(String state)
    {
        mComboFigure.setState(state);
        mListFigure.setState(state);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure#refresh()
     */
    public void refresh()
    {
        mComboFigure.refresh();
        mListFigure.refresh();
    }

    private Rectangle[] calculateBounds(Rectangle rect)
    {
        Rectangle rect1 = new Rectangle(0,0,rect.width,Math.min(mComboHeight,rect.height));
        return new Rectangle[] {rect1,
                                new Rectangle(0,Math.max(0,rect1.height),rect.width,Math.max(0,rect.height-rect1.height))};
    }

    @Override
    public void setBounds(Rectangle rect)
    {
        Rectangle[] bounds = calculateBounds(rect);
        getLayoutManager().setConstraint(mComboFigure,bounds[0]);
        getLayoutManager().setConstraint(mListFigure,bounds[1]);
        super.setBounds(rect);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.figures.IEditableElementFigure#getState()
     */
    public String getState()
    {
        return mComboFigure.getState();
    }

    public List<String> getListItems()
    {
        return mListFigure.getListItems();
    }

    public void setListItems(List<String> listItems)
    {
        mListFigure.setListItems(listItems);
    }

    public void setHScroll(boolean hScroll)
    {
        //Scrolling not supported
    }

    public void setVScroll(boolean vScroll)
    {
        //Scrolling not supported
    }

    public boolean isHScroll()
    {
        return false;
    }

    public boolean isVScroll()
    {
        return false;
    }

    public Rectangle getDirectEditArea()
    {
        return getClientArea().getCopy();
    }

    private class CustomPropertySourceWrapper extends PropertySourceWrapper
    {
        private Rectangle mBounds;
        public CustomPropertySourceWrapper(IPropertySource delegate, Rectangle bounds)
        {
            super(delegate);
            mBounds = bounds;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object getPropertyValue(Object id)
        {
            if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                return mBounds;
            }
            else if( InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
                List<String> flags = new ArrayList<String>((List<String>)getDelegate().getPropertyValue(id));
                flags.removeAll(SCROLL_FLAGS);
                return flags;
            }
            else {
                return super.getPropertyValue(id);
            }
        }
    }
}
