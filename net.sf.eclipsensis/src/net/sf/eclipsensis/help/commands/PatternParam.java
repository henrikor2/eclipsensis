package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;

public class PatternParam extends RegexpParam
{
    public static final String ATTR_REGEXP = "regexp"; //$NON-NLS-1$
    public static final String ATTR_ERROR_MESSAGE = "errorMessage"; //$NON-NLS-1$
    private String mRegexp;
    private String mValidateErrorMessage;

    public PatternParam(Node node)
    {
        super(node);
    }

    @Override
    protected void init(Node node)
    {
        NamedNodeMap attributes = node.getAttributes();
        mRegexp = XMLUtil.getStringValue(attributes, ATTR_REGEXP);
        mValidateErrorMessage = EclipseNSISPlugin.getResourceString(XMLUtil.getStringValue(attributes, ATTR_ERROR_MESSAGE));
        super.init(node);
    }

    @Override
    protected String getRegexp()
    {
        return mRegexp;
    }

    @Override
    protected String getValidateErrorMessage()
    {
        return mValidateErrorMessage;
    }

}
