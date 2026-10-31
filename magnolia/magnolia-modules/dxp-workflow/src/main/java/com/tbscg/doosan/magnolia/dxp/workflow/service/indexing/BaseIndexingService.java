package com.tbscg.doosan.magnolia.dxp.workflow.service.indexing;

import com.tbscg.doosan.magnolia.bobcat.custompath.CustomPathUtil;
import com.tbscg.doosan.magnolia.bobcat.custompath.ParsedPath;
import com.tbscg.doosan.magnolia.dxp.core.SitePagePath;
import com.tbscg.doosan.magnolia.dxp.workflow.model.PageNode;
import com.tbscg.doosan.magnolia.dxp.workflow.service.DynamoDBService;
import com.tbscg.doosan.magnolia.dxp.workflow.service.configuration.IndexingServiceConfiguration;
import com.tbscg.doosan.magnolia.dxp.workflow.service.exception.IndexingException;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationRequest;
import com.tbscg.doosan.magnolia.dxp.workflow.util.PublicationRequestContext;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Base Indexing service containing common methods for storing indexing events in DynamoDB table.
 */
@Slf4j
public abstract class BaseIndexingService implements Indexable {

    protected static final String PAGE_NOT_INDEXED_MSG = "PAGE NOT INDEXED";

    protected final RegionsService regionsService;
    protected final IndexingServiceConfiguration indexingServiceConfig;
    protected final DynamoDBService dynamoDBService;
    protected final String indexerTableName;

    public BaseIndexingService(final IndexingServiceConfiguration indexingServiceConfig,
                               final RegionsService regionsService,
                               final DynamoDBService dynamoDBService) throws IndexingException {
        this.indexingServiceConfig = indexingServiceConfig;
        this.regionsService = regionsService;
        this.dynamoDBService = dynamoDBService;
        this.indexerTableName = getIndexerTableName();
    }

    /**
     * Prepares and stores indexing events in DynamoDB table, that are later processed in Lambda functions.
     *
     * @param indexingAction type. Can be INDEX_ACTION or REINDEX
     * @param pagePath       JCR node path.
     * @param pageUuid       JCR UUID
     * @param publishTime    time of publication action.
     * @param isRecursive    flag saying if recursive publication was used.
     * @return true value if processing was successful, false otherwise.
     * @throws IndexingException in case of any exception during indexing process
     */
    @Override
    public boolean index(final String indexingAction,
                         final String pagePath,
                         final String pageUuid,
                         final String publishTime,
                         final boolean isRecursive) throws IndexingException {
        return true;
    }

    /**
     * Method to distinguish workspace of currently indexed item.
     *
     * @return current workspace
     */
    @Override
    public abstract String getWorkspace();

    protected List<String> getLanguages(final String siteName, final String region) throws IndexingException {
        return PublicationRequestContext.current()
                .map(context -> getLanguagesFromContext(context, siteName, region))
                .orElseGet(() -> loadLanguages(siteName, region));
    }

    private List<String> getLanguagesFromContext(final PublicationRequest context,
                                                 final String siteName,
                                                 final String region) {
        final List<String> cachedLanguages = context.getLanguages(siteName, region);
        if (cachedLanguages != null) {
            return cachedLanguages;
        }
        final List<String> languages = loadLanguages(siteName, region);
        context.cacheLanguages(siteName, region, languages);
        return languages;
    }

    protected String getFriendlyUrlForLang(final PageNode page, final String lang) {
        return getFriendlyUrlForLang(page.path().toString(), lang, () -> {
            final String customLink = page.getCustomLinkForLanguage(lang);
            if (StringUtils.isNotBlank(customLink)) {
                return customLink;
            }
            return ParsedPath.toParsedPath(page.path().toString(), lang).toPathWithLang();
        });
    }

    protected String getFriendlyUrlForLang(final Node node, final SitePagePath pagePath, final String lang) {
        return getFriendlyUrlForLang(pagePath.toString(), lang, () -> CustomPathUtil.getCustomLinkFromNode(node, pagePath.toString(), lang));
    }

    private List<String> loadLanguages(final String siteName, final String region) throws IndexingException {
        try {
            final List<String> languages = new ArrayList<>(regionsService.getLanguagesForSiteAndRegion(siteName, region));
            if (languages.isEmpty()) {
                log.warn("No languages were found for region '{}'.", region);
                languages.add(EMPTY);
            }
            return languages;
        } catch (RepositoryException e) {
            final String message = this.handleExceptionMessage(
                    String.format("Error while trying to get languages from region '%s': %s", region, e.getMessage()));
            throw new IndexingException(message, e);
        }
    }

    private String getFriendlyUrlForLang(final String pagePath, final String lang, final Supplier<String> supplier) {
        return PublicationRequestContext.current()
                .map(context -> context.resolveFriendlyUrl(pagePath, lang, supplier))
                .orElseGet(supplier);
    }

    private String getIndexerTableName() throws IndexingException {
        return indexingServiceConfig.getIndexerTableName();
    }

    private String handleExceptionMessage(final String message) {
        final String errorMessage = String.format("%s: %s", PAGE_NOT_INDEXED_MSG, message);
        log.error(errorMessage);
        return errorMessage;
    }
}
