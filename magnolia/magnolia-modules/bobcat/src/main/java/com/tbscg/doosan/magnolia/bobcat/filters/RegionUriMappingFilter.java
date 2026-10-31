package com.tbscg.doosan.magnolia.bobcat.filters;

import com.tbscg.doosan.magnolia.bobcat.BobcatModule;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.util.RequestDispatchUtil;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.module.site.Site;
import info.magnolia.multisite.sites.MultiSiteManager;
import info.magnolia.virtualuri.VirtualUriMapping;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.tbscg.doosan.magnolia.dxp.core.util.LoggingUtil.sanitizeMessage;

/**
 * This filter exists to redirect users who have visited the site through root domain (so without any region specified
 * in URL) to corresponding region and language base on their geographic location or to default ones in case when
 * matching couldn't be done.
 */
public class RegionUriMappingFilter extends AbstractMgnlFilter implements VirtualUriMapping {

    private static final Logger log = LoggerFactory.getLogger(RegionUriMappingFilter.class);
    private static final String REGION_PLACEHOLDER = "${region}";

    private final Provider<AggregationState> aggregationStateProvider;
    private final BobcatModule bobcatModule;
    private final MultiSiteManager multiSiteManager;

    private String siteName;
    private String fromUri;
    private String toUri;
    private UrlPattern pattern;
    private String defaultRegion;

    @Inject
    public RegionUriMappingFilter(Provider<AggregationState> aggregationStateProvider,
                                  BobcatModule bobcatModule,
                                  MultiSiteManager multiSiteManager) {

        this.aggregationStateProvider = aggregationStateProvider;
        this.bobcatModule = bobcatModule;
        this.multiSiteManager = multiSiteManager;
    }

    /**
     * Filter processing method.
     *
     * @param request request object
     * @param response response object
     * @param chain filter chain object
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        log.debug("Executing RegionUriMappingFilter");
        final AggregationState aggregationState = aggregationStateProvider.get();
        String currentUri = aggregationState.getCurrentURI();

        String queryString = aggregationState.getQueryString();
        final URI uri;
        try {
            uri = new URI(null, null, currentUri, queryString, null);
        } catch (URISyntaxException e) {
            log.warn("Cannot reconstruct a valid URI from the aggregation state data", e);
            chain.doFilter(request, response);
            return;
        }

        Optional<Result> mappingResult = this.mapUri(uri);
        if (!mappingResult.isPresent()) {
            log.debug("Finishing execution due to missing mapping result");
            chain.doFilter(request, response);
            return;
        }

        String regionName = bobcatModule.getDetectedRegionName(request);
        if (StringUtils.isEmpty(regionName)) {
            regionName = defaultRegion;
        }
        Result result = mappingResult.get();
        String targetUri = result.getToUri().replace(REGION_PLACEHOLDER, regionName);

        if (response.isCommitted()) {
            log.warn(
                    "Response is already committed, cannot apply virtual URI {} (original URI was {})",
                    targetUri,
                    sanitizeMessage(request.getRequestURI()));
            chain.doFilter(request, response);
            return;
        }

        if (RequestDispatchUtil.dispatch(targetUri, request, response)) {
            return;
        }
        aggregationState.setCurrentURI(targetUri);
        log.debug("Execution has finished");
        chain.doFilter(request, response);
    }

    /**
     * Checks if required conditions for redirect are fulfilled.
     *
     * @param uri current URI
     * @return optional with object containing mapping results.
     */
    @Override
    public Optional<Result> mapUri(URI uri) {
        if (mappingConditionsFulfilled(uri)) {
            return Optional.of(new Result(toUri, pattern.getLength(), this));
        }
        return Optional.empty();
    }

    /**
     * Checks if filter configuration is valid
     *
     * @return true if yes and false otherwise
     */
    @Override
    public boolean isValid() {
        return StringUtils.isNotEmpty(fromUri)
                && StringUtils.isNotEmpty(toUri)
                && StringUtils.isNotEmpty(defaultRegion);
    }

    private boolean multisiteMatches() {
        final Site site = multiSiteManager.getCurrentSite();
        if (StringUtils.isEmpty(siteName)) {
            return site == null || MultiSiteManager.FALLBACK_SITE.equals(site.getName());
        } else {
            return siteName.equals(site.getName());
        }
    }

    private boolean mappingConditionsFulfilled(URI uri) {
        if(!isValid()) {
            log.debug("Filter configuration is not valid");
            return false;
        }
        if(!multisiteMatches()) {
            log.debug("Multisite doesn't match (detected: {}, setup: {})", multiSiteManager.getCurrentSite().getName(), siteName);
            return false;
        }
        if(pattern == null) {
            log.debug("Pattern is null");
            return false;
        }
        if(!pattern.match(uri.getPath())) {
            log.debug("Pattern doesn't match uri path");
            return false;
        }
        return true;
    }

    //--- MUTATORS ---//

    public String getFromUri() {
        return this.fromUri;
    }

    public void setFromUri(String fromUri) {
        setInternalFromUri(fromUri);

        if (StringUtils.isNotEmpty(fromUri)) {
            this.pattern = new SimpleUrlPattern(fromUri);
        }
    }

    protected void setInternalFromUri(String fromUri) {
        this.fromUri = fromUri;
    }

    public String getToUri() {
        return this.toUri;
    }

    public void setToUri(String toUri) {
        this.toUri = toUri;
    }

    public UrlPattern getPattern() {
        return pattern;
    }

    public String getDefaultRegion() {
        return defaultRegion;
    }

    public void setDefaultRegion(String defaultRegion) {
        this.defaultRegion = defaultRegion;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}
