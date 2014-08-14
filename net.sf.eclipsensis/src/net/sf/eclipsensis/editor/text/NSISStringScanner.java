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
import org.eclipse.jface.text.rules.*;

public class NSISStringScanner extends NSISRuleBasedScanner
{
    private IToken mDefaultToken;
    protected NSISVariablesWordRule mVariablesWordRule;
    protected NSISWordPatternRule mSymbolsRule;
    protected NSISWordPatternRule mLangstringsRule;

    /**
     * @param preferenceStore
     */
    public NSISStringScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /**
     * @return
     */
    @Override
    protected void addRules(List<IRule> rules)
    {
        rules.add(getSymbolsRule());
        rules.add(getVariablesRule());
        rules.add(getLangstringsRule());
    }

    /**
     * @param provider
     * @return
     */
    @Override
    protected synchronized IToken getDefaultToken()
    {
        if(mDefaultToken == null) {
            mDefaultToken= createTokenFromPreference(INSISEditorPreferenceConstants.STRINGS_STYLE);
        }
        return mDefaultToken;
    }

    protected synchronized IRule getSymbolsRule()
    {
        if(mSymbolsRule == null) {
            mSymbolsRule = new NSISWordPatternRule(new NSISWordDetector(){
                private boolean mFoundEndSequence = false;
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    mFoundEndSequence = false;
                    return (Character.isLetterOrDigit(character) || character == '_');
                }

                @Override
                public boolean isWordPart(char character)
                {
                    if(!mFoundEndSequence) {
                        if(character == '}') {
                            mFoundEndSequence = true;
                            return true;
                        }
                        else {
                            mFoundEndSequence = false;
                            return super.isWordPart(character);
                        }
                    }
                    else {
                        mFoundEndSequence = false;
                        return false;
                    }
                }
            }, "${","}",createTokenFromPreference(INSISEditorPreferenceConstants.SYMBOLS_STYLE)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return mSymbolsRule;
    }

    protected synchronized IRule getLangstringsRule()
    {
        if(mLangstringsRule == null) {
            mLangstringsRule = new NSISWordPatternRule(new NSISWordDetector(){
                private boolean mFoundEndSequence = false;
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    mFoundEndSequence = false;
                    return (Character.isLetterOrDigit(character) || character == '_' || character == '^');
                }

                @Override
                public boolean isWordPart(char character)
                {
                    if(!mFoundEndSequence) {
                        if(character == ')') {
                            mFoundEndSequence = true;
                            return true;
                        }
                        else {
                            mFoundEndSequence = false;
                            return super.isWordPart(character);
                        }
                    }
                    else {
                        mFoundEndSequence = false;
                        return false;
                    }
                }
            }, "$(",")",createTokenFromPreference(INSISEditorPreferenceConstants.LANGSTRINGS_STYLE)); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return mLangstringsRule;
    }

    protected synchronized IRule getVariablesRule()
    {
        if(mVariablesWordRule == null) {
            mVariablesWordRule = new NSISVariablesWordRule(createTokenFromPreference(INSISEditorPreferenceConstants.PREDEFINED_VARIABLES_STYLE),
                                                           createTokenFromPreference(INSISEditorPreferenceConstants.USERDEFINED_VARIABLES_STYLE));
        }
        return mVariablesWordRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if (INSISEditorPreferenceConstants.STRINGS_STYLE.equals(property)) {
            mDefaultToken = null;
        }
        else if (INSISEditorPreferenceConstants.SYMBOLS_STYLE.equals(property)) {
            mSymbolsRule = null;
        }
        else if (INSISEditorPreferenceConstants.LANGSTRINGS_STYLE.equals(property)) {
            mLangstringsRule = null;
        }
        else if (INSISEditorPreferenceConstants.USERDEFINED_VARIABLES_STYLE.equals(property) ||
                INSISEditorPreferenceConstants.PREDEFINED_VARIABLES_STYLE.equals(property)) {
            mVariablesWordRule = null;
        }
        else {
            return;
        }
        reset();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#reset(boolean)
     */
    @Override
    public void reset(boolean full)
    {
        if(full) {
            mDefaultToken = null;
            mSymbolsRule = null;
            mLangstringsRule = null;
            mVariablesWordRule = null;
        }
        super.reset(full);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        return (INSISEditorPreferenceConstants.STRINGS_STYLE.equals(property) ||
                INSISEditorPreferenceConstants.SYMBOLS_STYLE.equals(property) ||
                INSISEditorPreferenceConstants.LANGSTRINGS_STYLE.equals(property) ||
                INSISEditorPreferenceConstants.USERDEFINED_VARIABLES_STYLE.equals(property) ||
                INSISEditorPreferenceConstants.PREDEFINED_VARIABLES_STYLE.equals(property));
    }
}