/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template.resolvers;

import net.sf.eclipsensis.help.NSISKeywords;

import org.eclipse.jface.text.templates.*;

public class NSISPagetypeVariableResolver extends TemplateVariableResolver
{
    /**
     * @param type
     * @param description
     */
    public NSISPagetypeVariableResolver(String type, String description)
    {
        super(type, description);
    }

    /**
     *
     */
    public NSISPagetypeVariableResolver()
    {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolveAll(org.eclipse.jface.text.templates.TemplateContext)
     */
    @Override
    protected String[] resolveAll(TemplateContext context)
    {
        return NSISKeywords.getInstance().getKeywordsGroup(NSISKeywords.INSTALLER_PAGES);
    }
}
