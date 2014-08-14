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

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.jface.text.templates.*;

public class NSISTemplateTranslator extends TemplateTranslator implements INSISTemplateConstants
{
    private String mErrorMessage = null;

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateTranslator#getErrorMessage()
     */
    @Override
    public String getErrorMessage()
    {
        return mErrorMessage;
    }

    @Override
    public TemplateBuffer translate(Template template) throws TemplateException
    {
        return translate(template.getPattern());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateTranslator#translate(java.lang.String)
     */
    @Override
    public TemplateBuffer translate(String string) throws TemplateException
    {
        StringBuffer buffer = new StringBuffer(""); //$NON-NLS-1$

        int state= TEXT;
        mErrorMessage= null;
        Map<String, List<Integer>> map = new CaseInsensitiveMap<List<Integer>>();

        int n=0;
        int offset = -1;
        for (int i= 0; i != string.length(); i++) {
            char ch= string.charAt(i);

            switch (state) {
            case TEXT:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        state= ESCAPE;
                        break;
                    default:
                        buffer.append(ch);
                        n++;
                        break;
                }
                break;
            case ESCAPE:
                switch (ch) {
                    case IDENTIFIER_BOUNDARY:
                        buffer.append(ch);
                        n++;
                        state= TEXT;
                        break;
                    default:
                        if(!Character.isLetter(ch)) {
                            mErrorMessage= EclipseNSISPlugin.getResourceString("template.invalid.variable.character.error"); //$NON-NLS-1$
                            throw new TemplateException(mErrorMessage);
                        }
                        offset = n;
                        state= IDENTIFIER;
                        buffer.append(ch);
                        n++;
                }
                break;
            case IDENTIFIER:
                switch (ch) {
                case IDENTIFIER_BOUNDARY:
                    String name = buffer.substring(offset,n);
                    List<Integer> list = map.get(name);
                    if(list == null) {
                        list = new ArrayList<Integer>();
                        map.put(name,list);
                    }
                    list.add(new Integer(offset));
                    state= TEXT;
                    break;
                default:
                    if (!Character.isLetterOrDigit(ch) && ch != '_') {
                        // illegal identifier character
                        mErrorMessage= EclipseNSISPlugin.getResourceString("template.invalid.variable.character.error"); //$NON-NLS-1$
                        throw new TemplateException(mErrorMessage);
                    }
                    buffer.append(ch);
                    n++;
                    break;
                }
                break;
            }
        }

        switch (state) {
            case TEXT:
                break;
            default:
                throw new TemplateException(EclipseNSISPlugin.getResourceString("template.incomplete.variable.error")); //$NON-NLS-1$
        }

        String translatedString= buffer.toString();
        List<TemplateVariable> variables = new ArrayList<TemplateVariable>();
        for(Iterator<String> iter=map.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            List<Integer> list = map.get(name);
            int[] offsets = new int[list.size()];
            for (int j = 0; j < offsets.length; j++) {
                offsets[j] = (list.get(j)).intValue();
            }
            variables.add(new TemplateVariable(name, name, offsets));
        }

        return new TemplateBuffer(translatedString, variables.toArray(new TemplateVariable[variables.size()]));
    }
}
