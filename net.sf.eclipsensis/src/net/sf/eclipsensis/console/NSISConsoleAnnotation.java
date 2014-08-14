/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;

class NSISConsoleAnnotation extends Annotation implements IAnnotationPresentation
{
    public static final String TYPE = "net.sf.eclipsensis.console.nsisConsoleAnnotation"; //$NON-NLS-1$

    private Image mImage;
    private Position mPosition;
    private NSISConsoleLine mLine;

    public NSISConsoleAnnotation(Image image, Position pos, NSISConsoleLine line)
    {
        super();
        mImage = image;
        mPosition = pos;
        mLine = line;
        setType(TYPE);
    }

    public int getLayer()
    {
        return DEFAULT_LAYER;
    }

    protected NSISConsoleLine getLine()
    {
        return mLine;
    }

    protected Position getPosition()
    {
        return mPosition;
    }

    public void paint(GC gc, Canvas canvas, Rectangle bounds)
    {
        if (mImage != null) {
            ImageUtilities.drawImage(mImage, gc, canvas, bounds, SWT.CENTER, SWT.CENTER);
        }
    }
}