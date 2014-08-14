/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.search;

import java.util.Collection;

import org.apache.lucene.search.*;

public interface INSISHelpSearchRequester
{
    public String getSearchText();
    public Filter getFilter();
    public void queryParsed(Query query);
    public void searchCompleted(NSISHelpSearchResult[] results, Collection<String> highlightTerms);
    public boolean isCanceled();
    public boolean useStemming();
}
