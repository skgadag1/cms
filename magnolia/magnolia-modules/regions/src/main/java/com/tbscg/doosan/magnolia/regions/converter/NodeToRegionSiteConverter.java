package com.tbscg.doosan.magnolia.regions.converter;

import com.google.common.base.Preconditions;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConversionException;
import com.tbscg.doosan.magnolia.dxp.core.converter.BeanConverter;
import com.tbscg.doosan.magnolia.regions.i18n.RegionsI18nContentSupport;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.site.Domain;
import info.magnolia.module.site.templates.ConfiguredSiteTemplateAvailability;
import info.magnolia.module.site.templates.ConfiguredTemplateSettings;
import info.magnolia.module.site.templates.TemplateConfig;
import info.magnolia.module.site.templates.TemplateSettings;
import info.magnolia.jcr.util.NodeTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.*;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath;

/**
 * {@link BeanConverter} for converting {@link Node} into {@link RegionSite}.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
@Singleton
public class NodeToRegionSiteConverter implements BeanConverter<Node, RegionSite> {

    private static final String SITE_TITLE = "title";
    private static final String SITE_COUNTRY_CODE = "countryCode";
    private static final String SITE_TEMPLATES = "templates";
    private static final String SITE_TEMPLATES_PROPERTY = "templateId";
    private static final String SITE_DOMAINS = "domains";
    private static final String PUBLISHERS = "publishers";
    private static final String PUBLISHERS_PROPERTY = "publisherId";
    private static final String X_DEFAULT = "xDefault";
    private static final String HREFLANG_LOCALE = "hreflangLocale";
    private static final String HREFLANG_FIELD = "field";

    private static final String ERROR_MSG_DELETED_STATE = "Node can't be deleted";

    private final Node2BeanProcessor node2BeanProcessor;

    @Inject
    public NodeToRegionSiteConverter(Node2BeanProcessor node2BeanProcessor) {
        this.node2BeanProcessor = node2BeanProcessor;
    }

    /**
     * Converts JCR node to <code>RegionSite</code> model instance.
     *
     * @param fromNode node to convert into <code>RegionSite</code>.
     * @return <code>RegionSite</code> model instance constructed from node parameter, <code>null</code> if there is
     * any error during the conversion.
     * @throws BeanConversionException in case that something goes wrong when converting node to <code>RegionSite</code>.
     */
    @Override
    public RegionSite toBean(Node fromNode) throws BeanConversionException {

        try {
            return this.handleConversion(fromNode);

        } catch (Exception e) {
            throw new BeanConversionException("Unable to convert node to RegionSite", e);
        }
    }

    private RegionSite handleConversion(Node fromNode) throws RepositoryException {
        Node rootSiteNode = this.getRootSiteNode(fromNode);

        Preconditions.checkState(!com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isDeleted(rootSiteNode), ERROR_MSG_DELETED_STATE);

        RegionSite regionSite = new RegionSite();

        String rootSiteName = rootSiteNode.getName();

        //this property will always exist
        regionSite.setName(rootSiteName);

        String rootSiteTitle = PropertyUtil.getString(rootSiteNode, SITE_TITLE);

        if (rootSiteTitle != null) {

            regionSite.setTitle(rootSiteTitle);

        }

        List<Domain> siteDomains = this.getDomains(rootSiteNode);

        if (!siteDomains.isEmpty()) {

            regionSite.setDomains(siteDomains);

        }

        List<Region> siteRegions = this.getRegions(rootSiteNode);

        if (!siteRegions.isEmpty()) {

            regionSite.setRegions(siteRegions);

        }
        TemplateSettings templateSettings = this.getTemplates(rootSiteNode);
        regionSite.setTemplates(templateSettings);

        return regionSite;
    }

    /**
     * Gets <code>dxp:website</code> node as an ancestor of a given node. Returns <code>null</code>
     *
     * @param fromNode node in regions workspace.
     * @return <code>dxp:website</code> node.
     * @throws RepositoryException in case something goes wrong when trying to retrieve root site node.
     */
    private Node getRootSiteNode(Node fromNode) throws RepositoryException {

        if (fromNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Website.NAME)) {

            return fromNode;

        } else if (fromNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Region.NAME) ||
                fromNode.getPrimaryNodeType().isNodeType(RegionsConstants.NodeTypes.Language.NAME)) {

            return NodeUtil.getNearestAncestorOfType(fromNode, RegionsConstants.NodeTypes.Website.NAME);
        }

        throw new RepositoryException("Unable to retrieve root site node");
    }

    /**
     * Gets domains from <code>dxp:website</code> based on "domains" <code>mgnl:contentNode</code> node.
     * Returns an empty list in case that there are no domains defined.
     *
     * @param rootSiteNode <code>dxp:website</code> node.
     * @return <code>List<Domain></></code> based on its "domains" node.
     */
    private List<Domain> getDomains(Node rootSiteNode) {
        try {
            return this.handleGetDomains(rootSiteNode);

        } catch (Exception e) {
            log.debug("Unable to get domains for region site {}", getPath(rootSiteNode));
        }

        return Collections.emptyList();
    }

    private List<Domain> handleGetDomains(Node rootSiteNode) throws RepositoryException, Node2BeanException {
        List<Domain> domainsList = new ArrayList<>();

        Node domainsNode = rootSiteNode.getNode(SITE_DOMAINS);

        NodeIterator nodeDomainsIt = domainsNode.getNodes();

        while (nodeDomainsIt.hasNext()) {

            Node domainNode = nodeDomainsIt.nextNode();

            domainsList.add((Domain) node2BeanProcessor.toBean(domainNode, Domain.class));

        }

        return domainsList;
    }

    /**
     * Gets <code>dxp:region</code> nodes from <code>dxp:website</code> node and converts them into
     * <code>List<Region></code>. Returns an empty list in case there is no region on the root site node.
     *
     * @param rootSiteNode <code>dxp:website</code> node
     * @return <code>List<Region></code> based on <code>dxp:region</code> nodes.
     */
    private List<Region> getRegions(Node rootSiteNode) {
        try {
            return handleGetRegions(rootSiteNode);

        } catch (Exception e) {
            log.debug("Unable to get regions from site node {}", getPath(rootSiteNode));
        }

        return Collections.emptyList();
    }

    private List<Region> handleGetRegions(Node rootSiteNode) throws RepositoryException {

        List<Region> regionsList = new ArrayList<>();

        Iterator<Node> regionNodesIt = NodeUtil.getNodes(rootSiteNode, RegionsConstants.NodeTypes.Region.NAME).iterator();

        regionNodesIt.forEachRemaining(regionNode -> {

            Region region = this.getRegion(regionNode);

            if (region != null) {
                regionsList.add(region);
            }
        });

        return regionsList;
    }

    /**
     * Converts <code>dxp:region</code> node into <code>Region</code> model.
     *
     * @param regionNode <code>dxp:region</code> node.
     * @return <code>Region</code> model.
     */
    private Region getRegion(Node regionNode) {
        try {
            return this.handleGetRegion(regionNode);

        } catch (Exception e) {
            log.debug("Unable to fetch region from node {}", getPath(regionNode));
        }

        return null;
    }

    private Region handleGetRegion(Node regionNode) throws RepositoryException {
        Preconditions.checkState(!com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isDeleted(regionNode), ERROR_MSG_DELETED_STATE);

        Region region = new Region();

        String regionName = regionNode.getName();
        region.setName(regionName);

        String regionTitle = PropertyUtil.getString(regionNode, SITE_TITLE);

        if (regionTitle != null) {

            region.setTitle(regionTitle);

        }
        String countryCode = PropertyUtil.getString(regionNode, SITE_COUNTRY_CODE);

        if (countryCode != null) {

            region.setCountryCode(countryCode);

        }

        List<String> publisher = getPublishers(regionNode);
        region.setPublishers(publisher);

        // Extract xDefault field
        String xDefault = PropertyUtil.getString(regionNode, X_DEFAULT);
        if (xDefault != null) {
            region.setXDefault(xDefault);
        }

        RegionsI18nContentSupport regionsI18n = new RegionsI18nContentSupport();

        regionsI18n.setEnabled(true);
        regionsI18n.setLocales(this.getLanguages(regionNode));

        // Populate hreflang data for each locale
        try {
            populateHreflangData(regionNode, regionsI18n);
        } catch (RepositoryException e) {
            log.warn("Could not populate hreflang data for region: {}", getPath(regionNode), e);
        }

        region.setI18n(regionsI18n);

        return region;
    }

    /**
     * Gets <code>dxp:language</code> into <code>List<LocaleDefinition></code>. Returns an empty list in case there is
     * no language on the given <code>dxp:region</code> node.
     *
     * @param regionNode <code>dxp:region</code> node.
     * @return <code>List<LocaleDefinition></code>.
     */
    private List<LocaleDefinition> getLanguages(Node regionNode) {
        try {
            return this.handleGetLanguages(regionNode);

        } catch (Exception e) {
            log.debug("Unable to get languages for region node {}", getPath(regionNode));
        }

        return Collections.emptyList();
    }

    private List<LocaleDefinition> handleGetLanguages(Node regionNode) throws RepositoryException {
        List<LocaleDefinition> regionLocales = new ArrayList<>();

        Iterator<Node> languageNodes = NodeUtil.getNodes(regionNode, RegionsConstants.NodeTypes.Language.NAME).iterator();

        languageNodes.forEachRemaining(languageNode -> {

            LocaleDefinition localeDefinition = this.getLanguage(languageNode);

            if (localeDefinition != null) {
                regionLocales.add(localeDefinition);
            }

        });

        return regionLocales;
    }

    /**
     * Converts <code>dxp:language</code> node into <code>LocaleDefinition</code>.
     *
     * @param languageNode <code>dxp:language</code> node.
     * @return <code>LocaleDefinition</code>. <code>null</code> in case that any problem occurs during the conversion.
     */
    private LocaleDefinition getLanguage(Node languageNode) {
        try {
            return this.handleGetLanguage(languageNode);

        } catch (Exception e) {
            log.debug("Unable to fetch language from language node {}", getPath(languageNode));
        }

        return null;
    }

    private LocaleDefinition handleGetLanguage(Node languageNode) throws RepositoryException {
        Preconditions.checkState(!com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isDeleted(languageNode), ERROR_MSG_DELETED_STATE);

        LocaleDefinition localeDefinition = new LocaleDefinition();

        localeDefinition.setEnabled(true);
        String languageTag = languageNode.getName();

        Locale locale = Locale.forLanguageTag(languageTag);
        localeDefinition.setLocale(locale);

        String language = locale.getLanguage();
        localeDefinition.setLanguage(language);

        String country = locale.getCountry();
        localeDefinition.setCountry(country);

        return localeDefinition;
    }

    /**
     * Retrieves available page templates from site node into <code>TemplateSettings</code>.
     *
     * @param siteNode <code>dxp:website</code> node.
     * @return <code>TemplateSettings</code>. <code>null</code> in case that any problem occurs during the conversion.
     */
    private TemplateSettings getTemplates(Node siteNode) {
        try {
            return this.handleGetTemplates(siteNode);

        } catch (Exception e) {
            //templates are now not mandatory to fill out
            log.debug("Unable to retrieve templates from site node {}", getPath(siteNode));
        }

        return this.createTemplateSettings();
    }

    private TemplateSettings handleGetTemplates(Node siteNode) throws RepositoryException {
        Node templatesNode = siteNode.getNode(SITE_TEMPLATES);
        NodeIterator templatesNodeIt = templatesNode.getNodes();

        ConfiguredTemplateSettings templateSettings = this.createTemplateSettings();

        ConfiguredSiteTemplateAvailability siteTemplateAvailability =
                (ConfiguredSiteTemplateAvailability) templateSettings.getAvailability();

        while (templatesNodeIt.hasNext()) {
            Node templateNode = templatesNodeIt.nextNode();
            String templateId = templateNode.getProperty(SITE_TEMPLATES_PROPERTY).getString();
            String templateName = templateNode.getName();

            TemplateConfig templateConfig = new TemplateConfig();
            templateConfig.setId(templateId);
            templateConfig.setName(templateName);

            siteTemplateAvailability.addTemplate(templateConfig);
        }

        return templateSettings;
    }

    private ConfiguredTemplateSettings createTemplateSettings() {
        ConfiguredSiteTemplateAvailability siteTemplateAvailability = new ConfiguredSiteTemplateAvailability();

        ConfiguredTemplateSettings templateSettings = new ConfiguredTemplateSettings();
        templateSettings.setAvailability(siteTemplateAvailability);

        return templateSettings;
    }

    private List<String> getPublishers(Node regionNode) {
        try {
            return this.handleGetPublishers(regionNode);

        } catch (Exception e) {
            log.debug("No publishers assigned to region node {}", getPath(regionNode));
        }

        return Collections.emptyList();
    }

    private List<String> handleGetPublishers(Node regionNode) throws RepositoryException {
        List<String> publisherIds = new ArrayList<>();

        Node publishersNode = regionNode.getNode(PUBLISHERS);
        NodeIterator publishersNodeIt = publishersNode.getNodes();

        while (publishersNodeIt.hasNext()) {
            Node publisherNode = publishersNodeIt.nextNode();
            String publisherId = publisherNode.getProperty(PUBLISHERS_PROPERTY).getString();
            publisherIds.add(publisherId);
        }

        return publisherIds;
    }

    /**
     * Populates hreflang locale codes for each language in the region.
     * @param regionNode the dxp:region node
     * @param i18nSupport the RegionsI18nContentSupport to populate
     * @throws RepositoryException if there's an error accessing JCR nodes
     */
    private void populateHreflangData(Node regionNode, RegionsI18nContentSupport i18nSupport) 
            throws RepositoryException {
        
        Iterator<Node> languageNodes = NodeUtil.getNodes(regionNode, 
            RegionsConstants.NodeTypes.Language.NAME).iterator();
        
        while (languageNodes.hasNext()) {
            Node languageNode = languageNodes.next();
            
            // Skip deleted nodes
            if (com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isDeleted(languageNode)) {
                continue;
            }
            
            // Get the locale for this language
            String languageTag = languageNode.getName();
            Locale locale = Locale.forLanguageTag(languageTag);
            
            // Extract hreflang codes for this language
            List<String> hreflangCodes = extractHreflangLocales(languageNode);
            
            // Store in i18n support
            if (!hreflangCodes.isEmpty()) {
                i18nSupport.setHreflangLocales(locale, hreflangCodes);
                log.debug("Added {} hreflang codes for locale {}", hreflangCodes.size(), locale);
            }
        }
    }

    /**
     * Extracts hreflangLocale multifield values from a language node.
     * @param languageNode the dxp:language node
     * @return list of hreflang codes, empty if none
     */
    private List<String> extractHreflangLocales(Node languageNode) {
        List<String> locales = new ArrayList<>();
        
        try {
            if (!languageNode.hasNode(HREFLANG_LOCALE)) {
                return locales;
            }
            
            Node hreflangContainer = languageNode.getNode(HREFLANG_LOCALE);
            NodeIterator items = hreflangContainer.getNodes();
            
            while (items.hasNext()) {
                Node item = items.nextNode();
                
                // Skip deleted items
                if (item.hasProperty(NodeTypes.Deleted.DELETED)) {
                    continue;
                }
                
                // Get the field value
                if (item.hasProperty(HREFLANG_FIELD)) {
                    String hreflangCode = item.getProperty(HREFLANG_FIELD).getString();
                    if (StringUtils.isNotBlank(hreflangCode)) {
                        locales.add(hreflangCode);
                    }
                }
            }
        } catch (RepositoryException e) {
            log.warn("Could not extract hreflang locales from language node: {}", 
                getPath(languageNode), e);
        }
        
        return locales;
    }

}
