/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor.annotation;

import net.sf.eclipsensis.installoptions.ini.INIProblem;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;

public class INIProblemAnnotation extends Annotation implements IQuickFixableAnnotation
{
    private INIProblem mProblem;

    public INIProblemAnnotation(INIProblem problem)
    {
        super(problem.getType(), false, problem.getMessage());
        mProblem = problem;
    }

    public boolean isQuickFixable()
    {
        return mProblem.canFix();
    }

    public boolean isQuickFixableStateSet()
    {
        return true;
    }

    public void setQuickFixable(boolean state)
    {
    }

    public INIProblem getProblem()
    {
        return mProblem;
    }
}
