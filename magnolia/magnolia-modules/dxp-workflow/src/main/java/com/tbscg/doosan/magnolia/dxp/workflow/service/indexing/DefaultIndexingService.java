package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import org.apache.logging.log4j.util.Strings;

public class DefaultIndexingService implements Indexable {

    @Override
    public boolean index(final String indexingAction,
                         final String pagePath,
                         final String pageUuid,
                         final String publishTime,
                         final boolean isRecursive) throws IndexingException {
        return true;
    }

    @Override
    public String getWorkspace() {
        return Strings.EMPTY;
    }
}
