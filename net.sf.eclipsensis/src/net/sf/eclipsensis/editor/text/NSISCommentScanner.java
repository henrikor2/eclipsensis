/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.text;

import java.util.List;

import net.sf.eclipsensis.settings.INSISEditorPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.rules.*;

public class NSISCommentScanner extends NSISRuleBasedScanner implements INSISBackwardScanner
{
    private NSISTaskTagRule mTaskTagsRule;
    private boolean mCaseSensitive;

   /**
     * @param preferenceStore
     */
    public NSISCommentScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    public int getPreviousCharacter(int count)
    {
        if(getOffset() >= (count+1)) {
            try {
                return fDocument.get(getOffset()-(count+1),1).charAt(0);
            }
            catch (BadLocationException e) {
            }
        }
        return -1;
    }

    /**
     * @return
     */
    @Override
    protected IToken getDefaultToken()
    {
        return createTokenFromPreference(INSISEditorPreferenceConstants.COMMENTS_STYLE);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#isCaseSensitive()
     */
    @Override
    protected boolean isCaseSensitive()
    {
        return mCaseSensitive;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#addRules(java.util.List, org.eclipse.jface.text.rules.IToken)
     */
    @Override
    protected void addRules(List<IRule> rules)
    {
        mCaseSensitive = getPreferenceStore().getBoolean(INSISEditorPreferenceConstants.CASE_SENSITIVE_TASK_TAGS);
        IRule taskTagsRule = getTaskTagsRule();
        if(taskTagsRule != null) {
            rules.add(taskTagsRule);
        }
    }

    protected synchronized IRule getTaskTagsRule()
    {
        if(mTaskTagsRule == null) {
            mTaskTagsRule = new NSISTaskTagRule(createTokenFromPreference(INSISEditorPreferenceConstants.TASK_TAGS_STYLE));
        }
        return mTaskTagsRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        return property.equals(INSISEditorPreferenceConstants.COMMENTS_STYLE) ||
               property.equals(INSISEditorPreferenceConstants.TASK_TAGS_STYLE) ||
               property.equals(INSISEditorPreferenceConstants.TASK_TAGS) ||
               property.equals(INSISEditorPreferenceConstants.CASE_SENSITIVE_TASK_TAGS);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if(property.equals(INSISEditorPreferenceConstants.TASK_TAGS) ||
                property.equals(INSISEditorPreferenceConstants.TASK_TAGS_STYLE) ||
                property.equals(INSISEditorPreferenceConstants.CASE_SENSITIVE_TASK_TAGS)) {
            mTaskTagsRule = null;
        }
        else if(!property.equals(INSISEditorPreferenceConstants.COMMENTS_STYLE)) {
            return;
        }
        reset();
    }
}