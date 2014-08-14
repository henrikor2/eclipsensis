/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.util.IOUtility;

import org.w3c.dom.Node;

public abstract class LocalFilesystemObjectParam extends SubstitutableParam
{
    public LocalFilesystemObjectParam(Node node)
    {
        super(node);
    }

    @Override
    protected final PrefixableParamEditor createPrefixableParamEditor(NSISCommand command, INSISParamEditor parentEditor)
    {
        return createLocalFilesystemObjectParamEditor(command, parentEditor);
    }

    protected abstract LocalFilesystemObjectParamEditor createLocalFilesystemObjectParamEditor(NSISCommand command, INSISParamEditor parentEditor);

    protected abstract class LocalFilesystemObjectParamEditor extends PrefixableParamEditor
    {
        public LocalFilesystemObjectParamEditor(NSISCommand command, INSISParamEditor parentEditor)
        {
            super(command, parentEditor);
        }

        protected boolean testSymbol(String text)
        {
            if(text != null) {
                char[] chars = text.toCharArray();
                boolean foundSymbol = false;
                for (int i = 0; i < chars.length; i++) {
                    if(chars[i] == '$') {
                        char t;
                        if(i < chars.length - 1) {
                            t = chars[i+1];
                        }
                        else {
                            t = '\0';
                        }
                        if(t == '{') {
                            int nested = 0;
                            int j = i+2;
                            for(; j<chars.length; j++) {
                                if(chars[j]=='{') {
                                    nested++;
                                }
                                else if(chars[j] == '}') {
                                    nested--;
                                    if(nested < 0) {
                                        break;
                                    }
                                }
                            }
                            if(j <= chars.length && j>i+2 && nested < 0) {
                                foundSymbol = true;
                                i++;
                            }
                            else {
                                return false;
                            }
                        }
                    }
                }
                return foundSymbol;
            }
            return false;
        }

        protected boolean testVar(String text)
        {
            if(text != null) {
                int n = text.indexOf('$');
                if(n >=0 && n < (text.length()-1)) {
                    char c = Character.toLowerCase(text.charAt(n+1));
                    if((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected final String validateParam()
        {
            String error = validateLocalFilesystemObjectParam();
            if(error != null) {
                String name = IOUtility.decodePath(getPrefixableParamText());
                if(isAcceptSymbol() && testSymbol(name)) {
                    return null;
                }
                if(isAcceptVar() && testVar(name)) {
                    return null;
                }
                return error;
            }
            return null;
        }

        protected abstract String validateLocalFilesystemObjectParam();
    }
}
