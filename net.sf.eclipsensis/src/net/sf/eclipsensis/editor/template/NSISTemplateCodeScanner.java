/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import java.util.List;

import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

public class NSISTemplateCodeScanner extends NSISCodeScanner
{
    private static final String START_STOP_SEQUENCE = Character.toString(INSISTemplateConstants.IDENTIFIER_BOUNDARY);
    protected NSISWordPatternRule mTemplateVariableRule;

    /**
     * @param preferenceStore
     */
    public NSISTemplateCodeScanner(IPreferenceStore preferenceStore)
    {
        super(preferenceStore);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#addRules(java.util.List, org.eclipse.jface.text.rules.IToken)
     */
    @Override
    protected void addRules(List<IRule> rules)
    {
        rules.add(getTemplateVariableRule());
        super.addRules(rules);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.editor.text.NSISRuleBasedScanner#reset(boolean)
     */
    @Override
    public void reset(boolean full)
    {
        if(full) {
            mTemplateVariableRule = null;
        }
        super.reset(full);
    }

    protected synchronized IRule getTemplateVariableRule()
    {
        if(mTemplateVariableRule == null) {
            mTemplateVariableRule = new NSISWordPatternRule(new NSISWordDetector(){
                private boolean mFoundEndSequence = false;
                /*
                 * (non-Javadoc) Method declared on IWordDetector.
                 */
                public boolean isWordStart(char character)
                {
                    mFoundEndSequence = false;
                    if(character == INSISTemplateConstants.IDENTIFIER_BOUNDARY) {
                        mFoundEndSequence = true;
                        return true;
                    }
                    return Character.isLetter(character);
                }

                @Override
                public boolean isWordPart(char character)
                {
                    if(!mFoundEndSequence) {
                        if(character == INSISTemplateConstants.IDENTIFIER_BOUNDARY) {
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
            }, START_STOP_SEQUENCE,START_STOP_SEQUENCE,new Token(new TextAttribute(JFaceResources.getColorRegistry().get(INSISPreferenceConstants.TEMPLATE_VARIABLE_COLOR))));
        }
        return mTemplateVariableRule;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    @Override
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if(property.equals(INSISPreferenceConstants.TEMPLATE_VARIABLE_COLOR)) {
            mTemplateVariableRule = null;
            reset();
        }
        else {
            super.adaptToProperty(store, property);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    @Override
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        if(property.equals(INSISPreferenceConstants.TEMPLATE_VARIABLE_COLOR)) {
            return true;
        }
        else {
            return super.canAdaptToProperty(store, property);
        }
    }
}
