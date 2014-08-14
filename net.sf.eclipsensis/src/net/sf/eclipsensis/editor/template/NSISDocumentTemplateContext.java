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

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.templates.*;

public class NSISDocumentTemplateContext extends DocumentTemplateContext
{
    private boolean mInsertTemplatesMode = false;
    /**
     * @param type
     * @param document
     * @param completionOffset
     * @param completionLength
     */
    public NSISDocumentTemplateContext(TemplateContextType type,
            IDocument document, int completionOffset, int completionLength, boolean insertTemplatesMode)
    {
        super(type, document, completionOffset, completionLength);
        mInsertTemplatesMode = insertTemplatesMode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateContext#canEvaluate(org.eclipse.jface.text.templates.Template)
     */
    @Override
    public boolean canEvaluate(Template template)
    {
        String key = getKey();
        return super.canEvaluate(template) &&
            template.matches(key, getContextType().getId()) &&
            (mInsertTemplatesMode || (key.length() != 0 && template.getName().toLowerCase().startsWith(key.toLowerCase())));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
     */
    @Override
    public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException
    {
        if (!canEvaluate(template)) {
            return null;
        }

        TemplateTranslator translator= new NSISTemplateTranslator();
        TemplateBuffer buffer= translator.translate(template);

        getContextType().resolve(buffer, this);

        return buffer;
    }
}
