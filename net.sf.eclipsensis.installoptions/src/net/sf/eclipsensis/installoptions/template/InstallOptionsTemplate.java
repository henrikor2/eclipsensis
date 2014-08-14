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

import java.io.*;
import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.template.*;
import net.sf.eclipsensis.util.*;

import org.w3c.dom.*;

public class InstallOptionsTemplate extends AbstractTemplate implements IInstallOptionsTemplate
{
    private static final String SECTIONS_NODE= "sections"; //$NON-NLS-1$

    private static final long serialVersionUID = -2080053812185962758L;
    private static final Comparator<InstallOptionsWidget> cWidgetsComparator = new Comparator<InstallOptionsWidget>(){
        public int compare(InstallOptionsWidget w1, InstallOptionsWidget w2)
        {
            return w1.getIndex()-w2.getIndex();
        }
    };

    private INISection[] mSections;

    InstallOptionsTemplate()
    {
        this(null);
    }

    public InstallOptionsTemplate(String name)
    {
        this(null, name);
    }

    public InstallOptionsTemplate(String id, String name)
    {
        super(id, name);
    }

    public InstallOptionsTemplate(String id, String name, File file)
    {
        this(id, name);
        INIFile iniFile = INIFile.load(file);
        INISection[] sections = iniFile.getSections();
        for (int i = 0; i < sections.length; i++) {
            sections[i] = (INISection)sections[i].trim().clone();
        }
        setSections(sections);
    }

    public InstallOptionsTemplate(String id, String name, InstallOptionsWidget[] widgets)
    {
        this(id, name);
        setWidgets(widgets);
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("sections"); //$NON-NLS-1$
        skippedProperties.add("widgets"); //$NON-NLS-1$
    }

    @Override
    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        INISection[] iniSections = getSections();
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(!Common.isEmptyArray(iniSections)) {
            iniSections[0].update();
            buf.append(iniSections[0]);
            for (int i = 1; i < iniSections.length; i++) {
                iniSections[i].trim().update();
                buf.append(INSISConstants.LINE_SEPARATOR).append(iniSections[i].toString());
            }
        }
        Node sections = document.createElement(SECTIONS_NODE);
        Text data = document.createTextNode(buf.toString());
        sections.appendChild(data);
        node.appendChild(sections);
        return node;
    }

    @Override
    public void fromNode(Node node)
    {
        super.fromNode(node);
        Node[] nodes = XMLUtil.findChildren(node, SECTIONS_NODE);
        if(!Common.isEmptyArray(nodes)) {
            String sections = XMLUtil.readTextNode(nodes[0]);
            INIFile iniFile = INIFile.load(new StringReader(sections));
            INISection[] iniSections = iniFile.getSections();
            for (int i = 0; i < iniSections.length; i++) {
                iniSections[i] = (INISection)iniSections[i].trim().clone();
                iniSections[i].setParent(null);
            }
            setSections(iniSections);
        }
        if(mSections == null) {
            throw new InvalidTemplateException();
        }
    }

    public void setWidgets(InstallOptionsWidget[] widgets)
    {
        Arrays.sort(widgets, cWidgetsComparator);
        INISection[] sections = new INISection[widgets.length];
        for (int i = 0; i < widgets.length; i++) {
            sections[i] = (INISection)widgets[i].updateSection().trim().clone();
            sections[i].setName(InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[]{new Integer(i+1)}));
            sections[i].setParent(null);
            sections[i].update();
        }
        setSections(sections);
    }

    INISection[] getSections()
    {
        return mSections;
    }

    void setSections(INISection[] sections)
    {
        mSections = sections;
    }

    public InstallOptionsWidget[] getWidgets()
    {
        List<InstallOptionsElement> list = new ArrayList<InstallOptionsElement>();
        for (int i = 0; i < mSections.length; i++) {
            INIKeyValue[] keyValues = mSections[i].findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
            if(!Common.isEmptyArray(keyValues)) {
                String type = keyValues[0].getValue();
                InstallOptionsElementFactory factory = InstallOptionsElementFactory.getFactory(type);
                if(factory != null) {
                    list.add(factory.getNewObject((INISection)mSections[i].clone()));
                }
            }
        }
        return list.toArray(new InstallOptionsWidget[list.size()]);
    }

    @Override
    public Object clone()
    {
        InstallOptionsTemplate template = (InstallOptionsTemplate)super.clone();
        INISection[] sections = new INISection[mSections.length];
        for (int i = 0; i < sections.length; i++) {
            sections[i] = (INISection)mSections[i].clone();
        }
        template.setSections(sections);
        return template;
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

        InstallOptionsTemplate other = (InstallOptionsTemplate)template;
        INISection[] sections1 = getSections();
        INISection[] sections2 = other.getSections();
        if (sections1==sections2) {
            return true;
        }
        if (sections1==null || sections2==null) {
            return false;
        }

        int length = sections1.length;
        if (sections2.length != length) {
            return false;
        }

        for (int i=0; i<length; i++) {
            if (!(sections1[i]==null ? sections2[i]==null : sections1[i].isEqualTo(sections2[i]))) {
                return false;
            }
        }
        return true;
    }
}
