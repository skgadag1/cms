package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationWorkset;

import javax.jcr.Node;
import java.util.List;
import java.util.function.Supplier;

public interface PublicationRequest {

    PublicationWorkset getOrCreateWorkset(String workspace,
                                          String path,
                                          boolean recursive,
                                          Supplier<PublicationWorkset> supplier);

    List<PageNode> getOrCreatePagesToProcess(String pagePath,
                                             String pageUuid,
                                             boolean recursive,
                                             Supplier<List<PageNode>> supplier);

    List<String> getLanguages(String site, String region);

    void cacheLanguages(String site, String region, List<String> cachedLanguages);

    /**
     * Returns the friendly URL for the given page/language pair, computing and caching it via
     * {@code supplier} on the first call. {@code null} return values from the supplier are cached
     * just like any other value, so they are not recomputed on subsequent invocations within the
     * same request.
     */
    String resolveFriendlyUrl(String pagePath, String language, Supplier<String> supplier);

    List<Node> filterNodes(String workspace, List<Node> nodes);

    boolean includes(String workspace, PublicationItem item);

    void addVersionSessionSaves(int sessionSaveCount);
}
