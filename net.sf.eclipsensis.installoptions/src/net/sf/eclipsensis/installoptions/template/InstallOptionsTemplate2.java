/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.template;

import java.util.Arrays;

import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.template.*;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;

public class InstallOptionsTemplate2 extends AbstractTemplate implements IInstallOptionsTemplate
{
    private static final long serialVersionUID = -2080053812185962758L;

    private static final String VERSION_ATTRIBUTE = "version"; //$NON-NLS-1$
    private static final int VERSION = 2;

    private InstallOptionsWidget[] mWidgets;

    InstallOptionsTemplate2()
    {
        this((String)null);
    }

    InstallOptionsTemplate2(IInstallOptionsTemplate template)
    {
        this();
        copy(template);
    }

    public InstallOptionsTemplate2(String name)
    {
        this(null, name);
    }

    public InstallOptionsTemplate2(String id, String name)
    {
        super(id, name);
    }

    public InstallOptionsTemplate2(String id, String name, InstallOptionsWidget[] widgets)
    {
        this(id, name);
        setWidgets(widgets);
    }

    private void copy(IInstallOptionsTemplate template)
    {
        setId(template.getId());
        setName(template.getName());
        setWidgets(template.getWidgets());
        setDescription(template.getDescription());
        setDeleted(template.isDeleted());
        setEnabled(template.isEnabled());
        setType(template.getType());
    }

    public void setWidgets(InstallOptionsWidget[] widgets)
    {
        mWidgets = cloneWidgets(widgets);
    }

    @Override
    public Object clone()
    {
        InstallOptionsTemplate2 template = (InstallOptionsTemplate2)super.clone();
        template.setWidgets(mWidgets);
        return template;
    }

    public InstallOptionsWidget[] getWidgets()
    {
        return cloneWidgets(mWidgets);
    }

    private InstallOptionsWidget[] cloneWidgets(InstallOptionsWidget[] widgets)
    {
        InstallOptionsWidget[] newWidgets = null;

        if(widgets != null) {
            Arrays.sort(widgets, InstallOptionsWidgetComparator.INSTANCE);
            InstallOptionsWidgetList list = new InstallOptionsWidgetList();
            for (int i = 0; i < widgets.length; i++) {
                list.add(widgets[i]);
            }
            newWidgets = list.getWidgets();
        }
        return newWidgets;
    }

    @Override
    public void fromNode(Node node)
    {
        int version = XMLUtil.getIntValue(node.getAttributes(),VERSION_ATTRIBUTE);
        if(version == VERSION) {
            super.fromNode(node);
        }
        else {
            //Maybe this is the older version
            InstallOptionsTemplate template = new InstallOptionsTemplate();
            template.fromNode(node);
            copy(template);
        }
    }

    @Override
    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        XMLUtil.addAttribute(document, node, VERSION_ATTRIBUTE, Integer.toString(VERSION));
        return node;
    }

    @Override
    public boolean isEqualTo(ITemplate template)
    {
        if (this == template) {
            return true;
        }
        if (!super.equals(template)) {
            return false;
        }
        if (getClass() != template.getClass()) {
            return false;
        }

        return true;
    }
}
