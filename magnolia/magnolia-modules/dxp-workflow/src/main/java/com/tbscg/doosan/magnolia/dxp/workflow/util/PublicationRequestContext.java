package com.tbscg.doosan.magnolia.dxp.workflow.util;

import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationItem;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PublicationWorkset;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public final class PublicationRequestContext implements AutoCloseable, PublicationRequest {

    private static final ThreadLocal<PublicationRequestContext> CURRENT = new ThreadLocal<>();

    private final PublicationRequestContext previous;
    private final String workspace;
    private final String path;
    private final boolean recursive;
    private final PublicationScope scope;
    private final Map<WorksetKey, PublicationWorkset> worksets = new HashMap<>();
    private final Map<PageScopeKey, List<PageNode>> pagesToProcess = new HashMap<>();
    private final Map<LanguageKey, List<String>> languages = new HashMap<>();
    private final Map<FriendlyUrlKey, String> friendlyUrls = new HashMap<>();
    private int versionSessionSaveCount;

    private PublicationRequestContext(final PublicationRequestContext previous,
                                      final String workspace,
                                      final String path,
                                      final boolean recursive,
                                      final PublicationScope scope) {
        this.previous = previous;
        this.workspace = workspace;
        this.path = path;
        this.recursive = recursive;
        this.scope = scope == null ? PublicationScopes.all() : scope;
    }

    public static PublicationRequestContext open(final String workspace,
                                                 final String path,
                                                 final boolean recursive) {
        return open(workspace, path, recursive, PublicationScopes.all());
    }

    public static PublicationRequestContext open(final String workspace,
                                                 final String path,
                                                 final boolean recursive,
                                                 final PublicationScope scope) {
        final PublicationRequestContext context = new PublicationRequestContext(CURRENT.get(), workspace, path, recursive, scope);
        CURRENT.set(context);
        return context;
    }

    public static Optional<PublicationRequest> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public PublicationWorkset getOrCreateWorkset(final String workspace,
                                                 final String path,
                                                 final boolean recursive,
                                                 final Supplier<PublicationWorkset> supplier) {
        return worksets.computeIfAbsent(new WorksetKey(workspace, path, recursive), ignored -> supplier.get());
    }

    public List<PageNode> getOrCreatePagesToProcess(final String pagePath,
                                                    final String pageUuid,
                                                    final boolean recursive,
                                                    final Supplier<List<PageNode>> supplier) {
        return pagesToProcess.computeIfAbsent(new PageScopeKey(pagePath, pageUuid, recursive), ignored -> List.copyOf(supplier.get()));
    }

    public List<String> getLanguages(final String site, final String region) {
        return languages.get(new LanguageKey(site, region));
    }

    public void cacheLanguages(final String site, final String region, final List<String> cachedLanguages) {
        languages.put(new LanguageKey(site, region), List.copyOf(cachedLanguages));
    }

    @Override
    public String resolveFriendlyUrl(final String pagePath, final String language, final Supplier<String> supplier) {
        final FriendlyUrlKey key = new FriendlyUrlKey(pagePath, language);
        // computeIfAbsent would skip caching when the supplier returns null; use containsKey
        // semantics instead so null values are remembered and not recomputed on subsequent calls.
        if (friendlyUrls.containsKey(key)) {
            return friendlyUrls.get(key);
        }
        final String friendlyUrl = supplier.get();
        friendlyUrls.put(key, friendlyUrl);
        return friendlyUrl;
    }

    public List<Node> filterNodes(final String workspace, final List<Node> nodes) {
        return scope.filterNodes(workspace, nodes);
    }

    public boolean includes(final String workspace, final PublicationItem item) {
        return scope.includes(workspace, item);
    }

    public void addVersionSessionSaves(final int sessionSaveCount) {
        this.versionSessionSaveCount += sessionSaveCount;
    }

    @Override
    public void close() {
        if (previous == null) {
            log.info("Publish request summary: workspace={}, path={}, recursive={}, worksets={}, cachedPageScopes={}, languageCacheEntries={}, friendlyUrlCacheEntries={}, versionSessionSaves={}",
                    workspace,
                    path,
                    recursive,
                    worksets.size(),
                    pagesToProcess.size(),
                    languages.size(),
                    friendlyUrls.size(),
                    versionSessionSaveCount);
            CURRENT.remove();
            return;
        }
        log.debug("Nested publish context closing: workspace={}, path={}, recursive={}, worksets={}, cachedPageScopes={}, languageCacheEntries={}, friendlyUrlCacheEntries={}, versionSessionSaves={}",
                workspace,
                path,
                recursive,
                worksets.size(),
                pagesToProcess.size(),
                languages.size(),
                friendlyUrls.size(),
                versionSessionSaveCount);
        CURRENT.set(previous);
    }

    private record WorksetKey(String workspace, String path, boolean recursive) {
    }

    private record PageScopeKey(String pagePath, String pageUuid, boolean recursive) {
    }

    private record LanguageKey(String site, String region) {
    }

    private record FriendlyUrlKey(String pagePath, String language) {
    }
}
