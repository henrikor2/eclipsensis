/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

import java.util.*;

public abstract class AbstractNSISScriptElementContainer extends AbstractNSISScriptElement
{
    protected final List<INSISScriptElement> mElements = new ArrayList<INSISScriptElement>();

    /**
     * @param name
     */
    public AbstractNSISScriptElementContainer(String name)
    {
        super(name);
    }
    /**
     * @param name
     * @param arg
     */
    public AbstractNSISScriptElementContainer(String name, Object arg)
    {
        super(name, arg);
    }

    public INSISScriptElement addElement(INSISScriptElement element)
    {
        validateElement(element);
        mElements.add(element);
        return element;
    }

    public INSISScriptElement addElement(int position, INSISScriptElement element)
    {
        validateElement(element);
        mElements.add(position, element);
        return element;
    }

    public INSISScriptElement insertElement(INSISScriptElement beforeElement, INSISScriptElement element)
    {
        validateElement(element);
        mElements.add(mElements.indexOf(beforeElement), element);
        return element;
    }

    public INSISScriptElement insertAfterElement(INSISScriptElement afterElement, INSISScriptElement element)
    {
        validateElement(element);
        int index = mElements.indexOf(afterElement);
        if(index >= 0) {
            index++;
            mElements.add(index, element);
            return element;
        }
        else {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
    }

    /**
     * @param writer
     */
    protected void writeElements(NSISScriptWriter writer)
    {
        for (Iterator<INSISScriptElement> iter = mElements.iterator(); iter.hasNext();) {
            INSISScriptElement element = iter.next();
            element.write(writer);
        }
    }

    public int size()
    {
        return mElements.size();
    }

    public INSISScriptElement get(int n)
    {
        return mElements.get(n);
    }

    public INSISScriptElement remove(int n)
    {
        return mElements.remove(n);
    }

    public boolean remove(INSISScriptElement element)
    {
        return mElements.remove(element);
    }

    protected void validateElement(INSISScriptElement element) throws InvalidNSISScriptElementException
    {
        if(element instanceof NSISScriptMultiLineComment || element instanceof NSISScriptBlankLine ||
           element instanceof NSISScriptDefine || element instanceof NSISScriptUndef || element instanceof NSISScriptInsertMacro) {
            return;
        }
        throw new InvalidNSISScriptElementException(element);
    }
}
