package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;

public interface Indexable {

    boolean index(final String indexingAction,
                  final String pagePath,
                  final String pageUuid,
                  final String publishTime,
                  final boolean isRecursive) throws IndexingException;

    String getWorkspace();
}
