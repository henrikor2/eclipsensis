/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import java.util.List;

import org.eclipse.jface.text.Position;

public interface IINIContainer
{
    public void addChild(INILine element);
    public void addChild(int index, INILine element);
    public void removeChild(INILine element);
    public List<INILine> getChildren();
    public void setDirty(boolean dirty);
    public boolean isDirty();
    public Position getChildPosition(INILine child);
    public INILine getLineAtOffset(int offset);
}
