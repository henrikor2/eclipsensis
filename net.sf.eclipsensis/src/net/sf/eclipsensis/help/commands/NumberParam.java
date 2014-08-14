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

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.Node;

public class NumberParam extends RegexpParam
{
    public static final String ATTR_DOMAIN = "domain"; //$NON-NLS-1$
    public static final String DOMAIN_POSITIVE="positive"; //$NON-NLS-1$
    public static final String DOMAIN_NATURAL="natural"; //$NON-NLS-1$

    private String mDomain;

    public NumberParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        String domain = XMLUtil.getStringValue(node.getAttributes(), ATTR_DOMAIN);
        if(DOMAIN_NATURAL.equalsIgnoreCase(domain)) {
            mDomain = DOMAIN_NATURAL;
        }
        else if(DOMAIN_POSITIVE.equalsIgnoreCase(domain)) {
            mDomain = DOMAIN_POSITIVE;
        }
        else {
            mDomain = null;
        }
        super.init(node);
    }

    @Override
    protected String getRegexp()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(isAcceptVar()) {
            buf.append(getVarRegexp()).append("|"); //$NON-NLS-1$
        }
        if(isAcceptSymbol()) {
            buf.append(getSymbolRegexp()).append("|"); //$NON-NLS-1$
        }
        if(mDomain == DOMAIN_NATURAL) {
            buf.append("0x[0-9a-f]+|0[0-7]+|[0-9]+"); //$NON-NLS-1$
        }
        else if(mDomain == DOMAIN_POSITIVE) {
            buf.append("0x[0-9a-f]*[1-9a-f][0-9a-f]*|0[0-7]*[1-7][0-7]*|[0-9]*[1-9][0-9]*"); //$NON-NLS-1$
        }
        else {
            buf.append("0x[0-9a-f]+|0[0-7]+|\\-?[0-9]+"); //$NON-NLS-1$
        }
        return buf.toString();
    }

    @Override
    protected String getDefaultValue2()
    {
        if(mDomain == DOMAIN_NATURAL) {
            return "0"; //$NON-NLS-1$
        }
        else if(mDomain == DOMAIN_POSITIVE) {
            return "1"; //$NON-NLS-1$
        }
        else {
            return super.getDefaultValue2();
        }
    }

    @Override
    protected String getValidateErrorMessage()
    {
        return EclipseNSISPlugin.getResourceString("number.param.error"); //$NON-NLS-1$
    }

    @Override
    public boolean verifyText(String text)
    {
        if(text != null && text.length() > 0) {
            return ((isAcceptSymbol() || isAcceptVar()) && text.charAt(0)=='$') || mPattern.matcher(text).matches() || (mDomain == null && "-".equals(text)); //$NON-NLS-1$
        }
        return super.verifyText(text);
    }
}
