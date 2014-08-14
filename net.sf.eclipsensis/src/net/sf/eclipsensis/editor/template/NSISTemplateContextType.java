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

import net.sf.eclipsensis.editor.template.resolvers.NSISPercentVariableResolver;

import org.eclipse.jface.text.templates.*;


public class NSISTemplateContextType extends TemplateContextType
{
    public static final String NSIS_TEMPLATE_CONTEXT_TYPE= NSISTemplateContextType.class.getName();

    public NSISTemplateContextType() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
        addResolver(new NSISPercentVariableResolver());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateContextType#validate(java.lang.String)
     */
    @Override
    public void validate(String pattern) throws TemplateException
    {
        TemplateTranslator translator= new NSISTemplateTranslator();
        TemplateBuffer buffer= translator.translate(pattern);
        validateVariables(buffer.getVariables());
    }
}
