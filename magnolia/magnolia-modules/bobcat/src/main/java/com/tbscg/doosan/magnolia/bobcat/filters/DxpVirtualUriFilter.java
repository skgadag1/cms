package com.tbscg.doosan.magnolia.bobcat.filters;
import com.tbscg.doosan.magnolia.bobcat.BobcatModule;
import com.tbscg.doosan.magnolia.bobcat.mapping.RegexpVirtualUriMappingWithKey;
import com.tbscg.doosan.magnolia.bobcat.util.DxpRequestDispatchUtil;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.virtualuri.VirtualUriMapping.Result;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.virtualuri.VirtualUriRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Copy of the info.magnolia.virtualuri.VirtualUriFilter class, but using DxpRequestDispatchUtil instead of RequestDispatchUtil to accomodate for api keys in the proxy funcionality

public class DxpVirtualUriFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(DxpVirtualUriFilter.class);

    private VirtualUriRegistry registry;
    private Provider<AggregationState> aggregationStateProvider;

    private BobcatModule module;

    @Inject
    public DxpVirtualUriFilter(VirtualUriRegistry registry, Provider<AggregationState> aggregationStateProvider, BobcatModule module) {
        this.module = module;
        this.registry = registry;
        this.aggregationStateProvider = aggregationStateProvider;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final AggregationState aggregationState = aggregationStateProvider.get();
        String uriPrefix = aggregationState.getUriPrefix();
        String currentUri = aggregationState.getCurrentURI();
        String queryString = aggregationState.getQueryString();

        currentUri = StringUtils.join(StringUtils.removeEnd(uriPrefix, "/"), currentUri);

        // Encapsulate pre-decoded handle/path-info (that is without context path) + query string into a relative URI
        final URI uri;
        try {
            uri = new URI(null, null, currentUri, queryString, null);
        } catch (URISyntaxException e) {
            log.warn("Cannot reconstruct a valid URI from the aggregation state data", e);
            chain.doFilter(request, response);
            return;
        }

        Stream<Result> matches = registry.getAllDefinitions().stream()
                .map(uriMapping -> uriMapping.mapUri(uri))
                .filter(Optional::isPresent)
                .map(Optional::get);

        if (log.isDebugEnabled()) {
            matches = matches.peek(result -> log.debug("┌── {}: weight {}", result.getMapping(), result.getWeight()));
        }
        Optional<Result> mappingBestMatch = matches.max(Result::compareTo);

        if (!mappingBestMatch.isPresent()) {
            chain.doFilter(request, response);
            return;
        }

        Result result = mappingBestMatch.get();
        String targetUri = result.getToUri();
        log.debug("Evaluated Virtual URI mappings for {}. Best match: {}. Target URI: {}", uri, result.getMapping(), targetUri);

        // set to null because of forward
        aggregationState.setUriPrefix(null);

        if (response.isCommitted()) {
            log.warn(
                    "Response is already committed, cannot apply virtual URI {} (original URI was {})",
                    targetUri,
                    request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        String apiKey = this.getApiKey(result);
        if (DxpRequestDispatchUtil.dispatch(targetUri, request, response, apiKey)) {
            return;
        }

        aggregationState.setCurrentURI(targetUri);
        chain.doFilter(request, response);
    }

    private String getApiKey(Result result) {
        if(!(result.getMapping() instanceof RegexpVirtualUriMappingWithKey)) {
            return "";
        }
        RegexpVirtualUriMappingWithKey mapping = (RegexpVirtualUriMappingWithKey) result.getMapping();
        String apiKeyType = mapping.getApiKeyType();
        switch (apiKeyType) {
            case "bobcat":
                return this.module.getBobcatApiKey();
            case "ssr":
                return this.module.getSsrApiKey();
            default:
                return "";
        }
    }

}
