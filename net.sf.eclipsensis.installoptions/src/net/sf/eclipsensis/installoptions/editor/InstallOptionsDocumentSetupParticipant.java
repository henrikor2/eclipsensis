/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import net.sf.eclipsensis.installoptions.editor.text.InstallOptionsPartitionScanner;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.FastPartitioner;

public class InstallOptionsDocumentSetupParticipant implements IDocumentSetupParticipant
{
    public void setup(IDocument document) {
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3= (IDocumentExtension3) document;
            IDocumentPartitioner partitioner= new FastPartitioner(new InstallOptionsPartitionScanner(), InstallOptionsPartitionScanner.INSTALLOPTIONS_PARTITION_TYPES);
            extension3.setDocumentPartitioner(InstallOptionsPartitionScanner.INSTALLOPTIONS_PARTITIONING, partitioner);
            partitioner.connect(document);
        }
    }
}
