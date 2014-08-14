/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.lang.*;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.template.*;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.wizard.settings.*;

import org.w3c.dom.*;

public class NSISWizardTemplate extends AbstractTemplate
{
    private static final long serialVersionUID = 5904505162934330711L;

    private NSISWizardSettings mSettings = null;

    NSISWizardTemplate()
    {
        this(null);
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String name)
    {
        this(null, name);
    }

    /**
     * @param name
     */
    public NSISWizardTemplate(String id, String name)
    {
        super(id, name);
    }

    /**
     * @param name
     * @param description
     */
    public NSISWizardTemplate(String id, String name, String description)
    {
        super(id, name, description);
    }

    @Override
    public Object clone()
    {
        NSISWizardTemplate template = (NSISWizardTemplate)super.clone();
        try {
            template.mSettings = (mSettings==null?null:(NSISWizardSettings)mSettings.clone());
        }
        catch (CloneNotSupportedException e) {
            EclipseNSISPlugin.getDefault().log(e);
            template.mSettings = new NSISWizardSettings();
        }
        return template;
    }

    @Override
    public boolean isAvailable()
    {
        return (mSettings != null && (mSettings.getMinimumNSISVersion() == null ||
                NSISPreferences.getInstance().getNSISVersion().compareTo(mSettings.getMinimumNSISVersion()) >= 0));
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        validate();
    }

    @Override
    protected void addSkippedProperties(Collection<String> skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("settings"); //$NON-NLS-1$
    }

    @Override
    public Node toNode(Document document)
    {
        Node node = super.toNode(document);
        if(mSettings != null) {
            node.appendChild(mSettings.toNode(document));
        }
        return node;
    }

    @Override
    public void fromNode(Node node)
    {
        super.fromNode(node);
        Node[] settingsNode = XMLUtil.findChildren(node,NSISWizardSettings.NODE);
        if(!Common.isEmptyArray(settingsNode)) {
            NSISWizardSettings settings = new NSISWizardSettings(true);
            settings.fromNode(settingsNode[0]);
            mSettings = settings;
        }
        validate();
    }

    private void validate()
    {
        if(mSettings != null) {
            List<NSISLanguage> languages = mSettings.getLanguages();
            if (!Common.isEmptyCollection(languages)) {
                for (ListIterator<NSISLanguage> iter = languages.listIterator(); iter.hasNext();) {
                    NSISLanguage lang = iter.next();
                    NSISLanguage lang2 = NSISLanguageManager.getInstance().getLanguage(lang.getName());
                    if (lang2 != null) {
                        iter.set(lang2);
                    }
                    else {
                        iter.remove();
                    }
                }
            }
            AbstractNSISInstallGroup installer = (AbstractNSISInstallGroup)mSettings.getInstaller();
            if (installer != null) {
                installer.setExpanded(true, true);
                installer.resetChildTypes(true);
                installer.resetChildren(true);
            }
        }
        else {
            throw new InvalidTemplateException();
        }
    }

    /**
     * @return Returns the settings.
     */
    public synchronized NSISWizardSettings getSettings()
    {
        if(mSettings == null) {
            mSettings = new NSISWizardSettings();
        }
        return mSettings;
    }

    /**
     * @param settings The settings to set.
     */
    public void setSettings(NSISWizardSettings settings)
    {
        mSettings = settings;
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
        return Common.objectsAreEqual(getSettings(),((NSISWizardTemplate)template).getSettings());
    }
}
