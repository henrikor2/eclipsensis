/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.script;

public class InvalidNSISScriptElementException extends RuntimeException
{
    private static final long serialVersionUID = -7718052999840999457L;

    /**
     * @param message
     */
    public InvalidNSISScriptElementException(INSISScriptElement element)
    {
        super((element != null?element.getClass().getName():null));
    }
}
