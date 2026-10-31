package com.tbscg.doosan.magnolia.bobcat.rest.delivery;

import com.tbscg.doosan.magnolia.concepts.service.ConceptNotFoundException;
import com.tbscg.doosan.magnolia.concepts.service.ConceptsService;
import com.tbscg.doosan.magnolia.dxp.core.util.PropertyUtil;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.context.Context;
import info.magnolia.rest.reference.ReferenceResolver;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Optional;

@Slf4j
public class PageTitleResolver implements ReferenceResolver<String, String> {

    private final static String AFFIXES_CONCEPT_PATH = "/bobcat/common/pages_names";
    private static final String LANG_PARAM = "lang";
    private static final String FALLBACK_LANG = "en";
    private final static String SLASH = "/";
    private final static String SUFFIX = "suffix";
    private final static String PREFIX = "prefix";
    private final static String TITLE = "title";
    private final static String OVERRIDE_AFFIX_PROPERTY = "overrideTitleAffix";

    private final PageTitleResolverDefinition definition;
    private final Provider<Context> contextProvider;
    private final ConceptsService conceptsService;

    @Inject
    public PageTitleResolver(PageTitleResolverDefinition definition, Provider<Context> contextProvider, ConceptsService conceptsService) {
        this.definition = definition;
        this.contextProvider = contextProvider;
        this.conceptsService = conceptsService;
    }

    @Override
    public Optional<String> resolve(final String item) {
        try {
            Node node = contextProvider.get().getJCRSession(definition.getTargetWorkspace()).getNodeByIdentifier(item);
            if (isAffixesDisabled(node)) {
                return Optional.of(getPageTitle(node));
            }
            String conceptPathToAffixes = getConceptPathToAffixes(node);
            String tabTitle = getTitleWithAffixes(node, conceptPathToAffixes);
            return Optional.of(tabTitle);
        } catch (final RepositoryException e) {
            log.warn("Error on resolving item in PageTitleResolver", e);
        }
        return Optional.empty();
    }

    private boolean isAffixesDisabled(Node node) {
        return PropertyUtil.tryGetBooleanValue(node, OVERRIDE_AFFIX_PROPERTY);
    }

    private String getConceptPathToAffixes(Node node) throws RepositoryException {
        String region = getPageRegion(node);
        String language = getPageLanguage();
        String conceptPath = String.join(SLASH, AFFIXES_CONCEPT_PATH, region, language);
        return conceptsService.isConceptFolderEmpty(conceptPath) ? String.join(SLASH, AFFIXES_CONCEPT_PATH, region, FALLBACK_LANG) : conceptPath;
    }

    private String getPageRegion(Node node) throws RepositoryException {
        String path = node.getPath();
        return path.split("/")[2];
    }

    private String getPageLanguage() {
        Context context = this.contextProvider.get();
        String langFromContext = context.getAttribute(LANG_PARAM);
        return langFromContext != null ? langFromContext : FALLBACK_LANG;
    }

    private String getTitleWithAffixes(Node node, String conceptPath) throws RepositoryException {
        String pageTitle = getPageTitle(node);
        String prefix = getAffixFromConcepts(PREFIX, conceptPath);
        String suffix = getAffixFromConcepts(SUFFIX, conceptPath);
        return prefix + pageTitle + suffix;
    }

    private String getPageTitle(Node node) {
        String nodeTitle = null;
        try {
            nodeTitle = node.getProperty(TITLE).getString();
        } catch (RepositoryException e) {
            log.warn("Error on resolving page title in PageTitleResolver", e);
        }
        if (nodeTitle == null || nodeTitle.isEmpty()) {
            try {
                nodeTitle = node.getName();
            } catch (RepositoryException e) {
                log.warn("Error on resolving page title in PageTitleResolver", e);
            }
        }
        return nodeTitle == null ? "" : nodeTitle;
    }

    private String getAffixFromConcepts(String affixType, String conceptPath) {
        try {
            String affix = conceptsService.getConceptByPath(String.join(SLASH, conceptPath, affixType));
            return affix == null ? "" : affix;
        } catch (ConceptNotFoundException e) {
            log.warn("Error on resolving affix from concepts in PageTitleResolver for {}", conceptPath);
        }
        return "";
    }
}
