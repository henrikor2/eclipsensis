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

import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.template.AbstractTemplateManager;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;

public class InstallOptionsTemplateManager extends AbstractTemplateManager<IInstallOptionsTemplate>
{
    private static final Path cPath = new Path("templates"); //$NON-NLS-1$
    public static final InstallOptionsTemplateManager INSTANCE = new InstallOptionsTemplateManager();

    private List<IInstallOptionsTemplateListener> mListeners = new ArrayList<IInstallOptionsTemplateListener>();
    private Map<IInstallOptionsTemplate, InstallOptionsTemplateCreationFactory> mTemplateFactories = new HashMap<IInstallOptionsTemplate, InstallOptionsTemplateCreationFactory>();
    private List<InstallOptionsTemplateEvent> mEventQueue = new ArrayList<InstallOptionsTemplateEvent>();

    private InstallOptionsTemplateManager()
    {
        super();
    }

    @Override
    protected Plugin getPlugin()
    {
        return InstallOptionsPlugin.getDefault();
    }

    @Override
    protected IPath getTemplatesPath()
    {
        return cPath;
    }

    @Override
    protected Class<IInstallOptionsTemplate> getTemplateClass()
    {
        return IInstallOptionsTemplate.class;
    }

    public synchronized InstallOptionsTemplateCreationFactory getTemplateFactory(IInstallOptionsTemplate template)
    {
        InstallOptionsTemplateCreationFactory factory = mTemplateFactories.get(template);
        if(factory == null) {
            if(getTemplates().contains(template)) {
                factory = new InstallOptionsTemplateCreationFactory(template);
                mTemplateFactories.put(template, factory);
            }
        }
        return factory;
    }

    @Override
    public boolean addTemplate(IInstallOptionsTemplate template)
    {
        if(super.addTemplate(template)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, template);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateTemplate(IInstallOptionsTemplate oldTemplate, IInstallOptionsTemplate newTemplate)
    {
        if(super.updateTemplate(oldTemplate, newTemplate)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, oldTemplate, newTemplate);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean removeTemplate(IInstallOptionsTemplate template)
    {
        if(super.removeTemplate(template)) {
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void resetToDefaults()
    {
        for(Iterator<IInstallOptionsTemplate> iter=getTemplates().iterator(); iter.hasNext(); ) {
            IInstallOptionsTemplate template = iter.next();
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
        }
        super.resetToDefaults();
        for(Iterator<IInstallOptionsTemplate> iter=getTemplates().iterator(); iter.hasNext(); ) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, iter.next());
        }
    }

    @Override
    protected boolean restore(IInstallOptionsTemplate template)
    {
        if(super.restore(template)) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, template);
            return true;
        }
        return false;
    }

    @Override
    public IInstallOptionsTemplate revert(IInstallOptionsTemplate template)
    {
        IInstallOptionsTemplate newTemplate = super.revert(template);
        if(newTemplate != null) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_UPDATED, template, newTemplate);
        }
        return newTemplate;
    }

    public void addTemplateListener(IInstallOptionsTemplateListener listener)
    {
        if(!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeTemplateListener(IInstallOptionsTemplateListener listener)
    {
        mListeners.remove(listener);
    }

    private void queueEvent(int type, IInstallOptionsTemplate oldTemplate, IInstallOptionsTemplate newTemplate)
    {
        mEventQueue.add(new InstallOptionsTemplateEvent(type, oldTemplate, newTemplate));
    }

    private void notifyListeners()
    {
        InstallOptionsTemplateEvent[] events = mEventQueue.toArray(new InstallOptionsTemplateEvent[mEventQueue.size()]);
        mEventQueue.clear();
        IInstallOptionsTemplateListener[] listeners = mListeners.toArray(new IInstallOptionsTemplateListener[mListeners.size()]);
        for (int i = 0; i < listeners.length; i++) {
            try {
                listeners[i].templatesChanged(events);
            }
            catch (Exception e) {
                InstallOptionsPlugin.getDefault().log(e);
            }
        }
    }

    @Override
    protected Map<String, IInstallOptionsTemplate> loadDefaultTemplateStore() throws IOException, ClassNotFoundException
    {
        Map<String, IInstallOptionsTemplate> map = super.loadDefaultTemplateStore();
        for (Iterator<Map.Entry<String, IInstallOptionsTemplate>> iter = map.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, IInstallOptionsTemplate> entry = iter.next();
            IInstallOptionsTemplate template = entry.getValue();
            if(template instanceof InstallOptionsTemplate) {
                template = new InstallOptionsTemplate2(template);
                entry.setValue(template);
            }
        }
        return map;
    }

    @Override
    protected List<IInstallOptionsTemplate> loadUserTemplateStore() throws IOException, ClassNotFoundException
    {
        List<IInstallOptionsTemplate> list = super.loadUserTemplateStore();
        if (list != null)
        {
            for (ListIterator<IInstallOptionsTemplate> iter = list.listIterator(); iter.hasNext();)
            {
                IInstallOptionsTemplate template = iter.next();
                if (template instanceof InstallOptionsTemplate)
                {
                    template = new InstallOptionsTemplate2(template);
                    iter.set(template);
                }
            }
        }
        return list;
    }

    @Override
    public void save() throws IOException
    {
        super.save();
        notifyListeners();
    }

    @Override
    public void discard()
    {
        for(Iterator<IInstallOptionsTemplate> iter=getTemplates().iterator(); iter.hasNext(); ) {
            IInstallOptionsTemplate template = iter.next();
            if(!template.isDeleted()) {
                queueEvent(InstallOptionsTemplateEvent.TEMPLATE_REMOVED, template, null);
            }
        }
        super.discard();
        for(Iterator<IInstallOptionsTemplate> iter=getTemplates().iterator(); iter.hasNext(); ) {
            queueEvent(InstallOptionsTemplateEvent.TEMPLATE_ADDED, null, iter.next());
        }

        notifyListeners();
    }

    @Override
    protected InstallOptionsTemplateReaderWriter createReaderWriter()
    {
        return InstallOptionsTemplateReaderWriter.INSTANCE;
    }

    @Override
    protected Image getShellImage()
    {
        return InstallOptionsPlugin.getShellImage();
    }
}
