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

import net.sf.eclipsensis.template.AbstractTemplateReaderWriter;

class InstallOptionsTemplateReaderWriter extends AbstractTemplateReaderWriter<IInstallOptionsTemplate>
{
    static final InstallOptionsTemplateReaderWriter INSTANCE = new InstallOptionsTemplateReaderWriter();

    private InstallOptionsTemplateReaderWriter()
    {
        super();
    }

    @Override
    public Collection<IInstallOptionsTemplate> import$(File file) throws IOException
    {
        Collection<IInstallOptionsTemplate> templates = super.import$(file);
        List<IInstallOptionsTemplate> list = new ArrayList<IInstallOptionsTemplate>(templates);
        boolean changed = false;
        for(ListIterator<IInstallOptionsTemplate> iter = list.listIterator(); iter.hasNext(); ) {
            IInstallOptionsTemplate template = iter.next();
            if(template instanceof InstallOptionsTemplate) {
                template = new InstallOptionsTemplate2(template);
                iter.set(template);
                changed = true;
            }
        }
        if(changed) {
            templates.clear();
            templates.addAll(list);
        }
        return templates;
    }

    @Override
    protected IInstallOptionsTemplate createTemplate()
    {
        return new InstallOptionsTemplate2();
    }
}
