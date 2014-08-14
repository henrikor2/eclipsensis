/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.*;

public abstract class UpDownMover<T>
{
    public boolean canMoveUp()
    {
        int[] selectedIndices = getSelectedIndices();
        if(!Common.isEmptyArray(selectedIndices)) {
            for (int i= 0; i < selectedIndices.length; i++) {
                if (selectedIndices[i] != i) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canMoveDown()
    {
        int[] selectedIndices = getSelectedIndices();
        int size = getSize();
        if(!Common.isEmptyArray(selectedIndices) && size > 1) {
            int k= size - 1;
            for (int i= selectedIndices.length - 1; i >= 0 ; i--, k--) {
                if (selectedIndices[i] != k) {
                    return true;
                }
            }
        }
        return false;
    }

    public void moveToTop()
    {
        List<T> move = getMoveElements();
        List<T> elements = new ArrayList<T>(getAllElements());
        elements.removeAll(move);
        elements.addAll(0,move);
        updateElements(elements, move, false);
    }

    public void moveToBottom()
    {
        List<T> move = getMoveElements();
        List<T> elements = new ArrayList<T>(getAllElements());
        elements.removeAll(move);
        elements.addAll(move);
        updateElements(elements, move, true);
    }

    public void moveDown()
    {
        List<T> elements = getAllElements();
        List<T> move = getMoveElements();
        Collections.reverse(elements);
        elements = move(elements,move);
        Collections.reverse(elements);
        updateElements(elements, move, true);
    }

    public void moveUp()
    {
        List<T> move = getMoveElements();
        updateElements(move(getAllElements(), move), move, false);
    }

    private List<T> move(List<T> elements, List<T> move)
    {
        int nElements= elements.size();
        List<T> res= new ArrayList<T>(nElements);
        T floating= null;
        for (int i= 0; i < nElements; i++) {
            T curr= elements.get(i);
            if (move.contains(curr)) {
                res.add(curr);
            } else {
                if (floating != null) {
                    res.add(floating);
                }
                floating= curr;
            }
        }
        if (floating != null) {
            res.add(floating);
        }
        return res;
    }

    private List<T> getMoveElements()
    {
        List<T> moveElements = new ArrayList<T>();
        List<T> allElements = getAllElements();
        if(!Common.isEmptyCollection(allElements)) {
            int[] selectedIndices = getSelectedIndices();

            if(!Common.isEmptyArray(selectedIndices)) {
                for (int i = 0; i < selectedIndices.length; i++) {
                    moveElements.add(allElements.get(selectedIndices[i]));
                }
            }
        }

        return moveElements;
    }

    private int getSize()
    {
        List<T> allElements = getAllElements();
        return Common.isEmptyCollection(allElements)?0:allElements.size();
    }

    protected abstract int[] getSelectedIndices();
    protected abstract List<T> getAllElements();
    protected abstract void updateElements(List<T> elements, List<T> move, boolean isDown);
}
