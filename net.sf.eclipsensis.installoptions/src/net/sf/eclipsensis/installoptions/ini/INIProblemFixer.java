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

import java.util.*;

import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;

public abstract class INIProblemFixer
{
    private static final Comparator<INIProblemFix> cReversePositionComparator = new Comparator<INIProblemFix>() {
        public int compare(INIProblemFix o1, INIProblemFix o2)
        {
            Position p1 = o1.getPosition();
            Position p2 = o2.getPosition();
            int n = p2.offset-p1.offset;
            if(n == 0) {
                n = p2.length-p1.length;
            }
            return n;
        }
    };

    private INIProblemFix[] mFixes = null;
    private String mFixDescription = null;

    public INIProblemFixer(String fixDescription)
    {
        mFixDescription = fixDescription;
    }

    String getFixDescription()
    {
        return mFixDescription;
    }

    INIProblemFix[] getFixes()
    {
        if(mFixes == null) {
            mFixes = createFixes();
        }
        return mFixes;
    }

    void fix(IDocument document)
    {
        getFixes();
        if(!Common.isEmptyArray(mFixes)) {
            try {
                for (int i=0; i<mFixes.length; i++) {
                    INILine line = mFixes[i].getLine();
                    if(line == null) {
                        mFixes[i].setPosition(new Position(0,0));
                    }
                    else {
                        mFixes[i].setPosition(line.getParent().getChildPosition(line));
                    }
                }
                Arrays.sort(mFixes,cReversePositionComparator);
                for (int i=0; i<mFixes.length; i++) {
                    Position position = mFixes[i].getPosition();
                    document.replace(position.offset,position.length,mFixes[i].getText());
                }
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract INIProblemFix[] createFixes();
}
