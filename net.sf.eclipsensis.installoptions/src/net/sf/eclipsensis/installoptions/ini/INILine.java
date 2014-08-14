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

import java.io.Serializable;
import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

public class INILine implements Cloneable, Serializable
{
    private static final long serialVersionUID = -1038711652231662150L;

    public static final int VALIDATE_FIX_NONE = 0;
    public static final int VALIDATE_FIX_ERRORS = 1;
    public static final int VALIDATE_FIX_WARNINGS = 2;
    public static final int VALIDATE_FIX_ALL = VALIDATE_FIX_ERRORS|VALIDATE_FIX_WARNINGS;

    private String mText = ""; //$NON-NLS-1$
    private String mDelimiter = INSISConstants.LINE_SEPARATOR;
    private IINIContainer mParent;
    private List<INIProblem> mErrors = new ArrayList<INIProblem>();
    private List<INIProblem> mWarnings = new ArrayList<INIProblem>();

    public INILine(String text, String delimiter)
    {
        this(text);
        mDelimiter = delimiter;
    }

    public INILine(String text)
    {
        super();
        mText = text;
    }

    public void setText(String text)
    {
        if(!Common.stringsAreEqual(mText, text)) {
            mText = text;
            setDirty(true);
        }
    }

    protected void setDirty(boolean dirty)
    {
        if(getParent() != null) {
            getParent().setDirty(dirty);
        }
    }

    public String getText()
    {
        return mText;
    }

    public String getDelimiter()
    {
        return mDelimiter;
    }

    public void setDelimiter(String delimiter)
    {
        if(!Common.stringsAreEqual(mDelimiter, delimiter)) {
            mDelimiter = delimiter;
            setDirty(true);
        }
    }

    public int getLength()
    {
        String text = getText();
        return (text != null?text.length():0)+(mDelimiter != null?mDelimiter.length():0);
    }

    public IINIContainer getParent()
    {
        return mParent;
    }

    public boolean isBlank()
    {
        return Common.isEmpty(getText());
    }

    public void setParent(IINIContainer parent)
    {
        mParent = parent;
    }

    public void update()
    {
    }

    @Override
    public String toString()
    {
        return (mText != null?mText:"")+(mDelimiter != null?mDelimiter:""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public boolean hasErrors()
    {
        return !Common.isEmptyCollection(mErrors);
    }

    public boolean hasWarnings()
    {
        return !Common.isEmptyCollection(mWarnings);
    }

    public final void validate()
    {
        validate(VALIDATE_FIX_NONE);
    }

    public void validate(int fixFlag)
    {
        mErrors.clear();
        mWarnings.clear();
        checkProblems(fixFlag);
    }

    protected void checkProblems(int fixFlag)
    {
        if(!Common.isEmpty(getText())) {
            if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                getParent().removeChild(this);
            }
            else {
                INIProblem problem = new INIProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getResourceString("line.ignored.warning")); //$NON-NLS-1$
                problem.setFixer(new INIProblemFixer(InstallOptionsPlugin.getResourceString("quick.fix.remove.line")) { //$NON-NLS-1$
                    @Override
                    protected INIProblemFix[] createFixes()
                    {
                        return new INIProblemFix[] {new INIProblemFix(INILine.this)};
                    }
                });
                addProblem(problem);
            }
        }
    }

    public List<INIProblem> getErrors()
    {
        return Collections.unmodifiableList(mErrors);
    }

    public List<INIProblem> getWarnings()
    {
        return Collections.unmodifiableList(mWarnings);
    }

    public List<INIProblem> getProblems()
    {
        List<INIProblem> list = new ArrayList<INIProblem>();
        list.addAll(mErrors);
        list.addAll(mWarnings);
        return list;
    }

    public void addProblem(INIProblem problem)
    {
        if(INIProblem.TYPE_ERROR.equals(problem.getType())) {
            if(!mErrors.contains(problem)) {
                mErrors.add(problem);
            }
        }
        else if(INIProblem.TYPE_WARNING.equals(problem.getType())) {
            if(!mWarnings.contains(problem)) {
                mWarnings.add(problem);
            }
        }
    }

    public INILine copy()
    {
        return (INILine)clone();
    }

    @Override
    public Object clone()
    {
        try {
            INILine line = (INILine)super.clone();
            line.mWarnings = new ArrayList<INIProblem>();
            line.mErrors = new ArrayList<INIProblem>();
            return line;
        }
        catch (CloneNotSupportedException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return null;
        }
    }

    public boolean isEqualTo(INILine line)
    {
        if (this == line) {
            return true;
        }
        if (line == null) {
            return false;
        }
        if (getClass() != line.getClass()) {
            return false;
        }

        if(!Common.objectsAreEqual(mDelimiter,line.mDelimiter)) {
            return false;
        }
        if(!Common.objectsAreEqual(mText,line.mText)) {
            return false;
        }
        return true;
    }
}
