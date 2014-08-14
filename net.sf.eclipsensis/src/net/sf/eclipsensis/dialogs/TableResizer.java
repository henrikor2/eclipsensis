/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.Arrays;

import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.*;

public class TableResizer extends ControlAdapter
{
    private double[] mWeights;
    private double[] mCachedWeights;
    private double mTotalWeight;

    public TableResizer()
    {
        this(null);
    }

    public TableResizer(double[] weights)
    {
        super();
        mWeights = weights;
        mCachedWeights = null;
        mTotalWeight = 0;
    }

    @Override
    public void controlResized(ControlEvent e)
    {
        Table table = (Table)e.widget;
        int width = table.getClientArea().width;
        if(((table.getStyle() & SWT.V_SCROLL) > 0)) {
            width -= table.getVerticalBar().getSize().x;
        }
        int lineWidth = table.getGridLineWidth();
        TableColumn[] columns = table.getColumns();
        if(!Common.isEmptyArray(columns)) {
            int n = columns.length-1;
            width -= n*lineWidth;

            int[] minWidths = null;
            final boolean headerVisible = table.getHeaderVisible();
            if(headerVisible) {
                GC gc = new GC(table);
                minWidths = new int[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    minWidths[i] = gc.stringExtent(columns[i].getText()).x+16;
                    if(table.getSortColumn() == columns[i] && table.getSortDirection() != SWT.NONE) {
                        minWidths[i] += 26;
                    }
                }
                gc.dispose();
            }

            if(mCachedWeights == null || columns.length != mCachedWeights.length) {
                if(Common.isEmptyArray(mWeights)) {
                    mCachedWeights = new double[columns.length];
                    Arrays.fill(mCachedWeights, 1.0);
                }
                else {
                    if(mWeights.length != columns.length) {
                        mCachedWeights = (double[])Common.resizeArray(mWeights,columns.length);
                        if(columns.length > mWeights.length) {
                            Arrays.fill(mCachedWeights,mWeights.length,columns.length,1.0);
                        }
                    }
                    else {
                        mCachedWeights = mWeights;
                    }
                }
                mTotalWeight = 0;
                for (int i = 0; i < mCachedWeights.length; i++) {
                    mTotalWeight += mCachedWeights[i];
                }
            }
            int sumWidth = 0;
            for(int i=0; i<n; i++) {
                int width2 =  (int)((mCachedWeights[i]/mTotalWeight)*width);
                if(headerVisible && minWidths != null) {
                    width2 = Math.max(minWidths[i], width2);
                }
                sumWidth += width2;
                columns[i].setWidth(width2);
            }
            width = width-sumWidth;
            if(headerVisible && minWidths != null) {
                width = Math.max(width, minWidths[n]);
            }
            columns[n].setWidth(width);
            table.redraw();
        }
    }
}
