package com.tbscg.doosan.magnolia.dxp.workflow.service;

import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.service.indexing.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.DEINDEX_ACTION;
import static com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration.INDEX_ACTION;

/**
 * Indexing service responsible for the indexing process with Elasticsearch indexing and DynamoDB streams.
 */
public class IndexingService {

    private final Map<String, Indexable> indexableMap = new HashMap<>();
    private final DefaultIndexingService defaultIndexingService;

    @Inject
    public IndexingService(PageIndexingService pageIndexingService,
                           ProductIndexingService productIndexingService,
                           AttachmentIndexingService attachmentIndexingService) {
        this.defaultIndexingService = new DefaultIndexingService();
        this.indexableMap.put(pageIndexingService.getWorkspace(), pageIndexingService);
        this.indexableMap.put(productIndexingService.getWorkspace(), productIndexingService);
        this.indexableMap.put(attachmentIndexingService.getWorkspace(), attachmentIndexingService);
    }

    /**
     * Starts indexing process for given workspace and its path and, if recursive, all of its children.
     * Currently, supports website and products JCR workspaces.
     *
     * @param workspace workspace that is allowed to be indexed
     * @param path path to the JCR node to be indexed
     * @param uuid UUID of the page to be indexed
     * @param publishTime publication timestamp to send to the indexer
     * @param isRecursive flag indicating if publication process is recursive and if subpages should be indexed
     * @return true if processed successfully, false otherwise
     * @throws IndexingException if indexing cannot be completed
     */
    public boolean index(final String workspace, final String path, final String uuid, final String publishTime, final boolean isRecursive) throws IndexingException {
        return indexableMap.getOrDefault(workspace, defaultIndexingService).index(INDEX_ACTION, path, uuid, publishTime, isRecursive);
    }

    /**
     * Starts page de-indexing process for given workspace and its path and all of its subnodes.
     *
     * @param workspace workspace that is allowed to be deindexed
     * @param path path to the page to be deindexed
     * @param uuid UUID of the page to be deindexed
     * @param publishTime publication timestamp to send to the indexer
     * @return true if processed successfully, false otherwise
     * @throws IndexingException if deindexing cannot be completed
     */
    public boolean deindex(final String workspace, final String path, final String uuid, final String publishTime) throws IndexingException {
        return indexableMap.getOrDefault(workspace, defaultIndexingService).index(DEINDEX_ACTION, path, uuid, publishTime, true);
    }

}
