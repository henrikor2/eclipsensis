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

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

public interface IInstallOptionsFigure extends IFigure
{
    public static final List<String> SCROLL_FLAGS = Collections.unmodifiableList(Arrays.asList(
                                                    new String[]{InstallOptionsModel.FLAGS_HSCROLL,
                                                                 InstallOptionsModel.FLAGS_VSCROLL}));

    public void setDisabled(boolean disabled);
    public void setHScroll(boolean hScroll);
    public void setVScroll(boolean vScroll);
    public boolean isDisabled();
    public boolean isHScroll();
    public boolean isVScroll();
    public void refresh();
    public Rectangle getDirectEditArea();
    public boolean isClickThrough();
    public boolean isDefaultClickThroughFigure();
    public boolean hitTest(int x, int y);
}
