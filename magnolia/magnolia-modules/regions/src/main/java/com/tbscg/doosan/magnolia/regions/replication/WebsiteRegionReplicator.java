package com.tbscg.doosan.magnolia.regions.replication;

import com.tbscg.doosan.magnolia.regions.converter.NodeToRegionSiteConverter;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.site.templates.ConfiguredSiteTemplateAvailability;
import info.magnolia.module.site.templates.TemplateConfig;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Map;

/**
 * {@link WebsiteRegionReplicator} replicates {@link RegionSite} and {@link Region} structures
 * on website workspace.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
@Singleton
public class WebsiteRegionReplicator extends AbstractRegionReplicator implements RegionReplicator {

    private final TemplateDefinitionAssignment templateDefinitionAssignment;
    private final TemplateDefinitionRegistry templateDefinitionRegistry;
    private final NodeToRegionSiteConverter nodeToRegionSiteConverter;
    private final RegionsService regionsService;

    @Inject
    public WebsiteRegionReplicator(TemplateDefinitionRegistry templateDefinitionRegistry,
                                   TemplateDefinitionAssignment templateDefinitionAssignment,
                                   NodeToRegionSiteConverter nodeToRegionSiteConverter,
                                   RegionsService siteService) {
        super(RepositoryConstants.WEBSITE);
        this.templateDefinitionRegistry = templateDefinitionRegistry;
        this.templateDefinitionAssignment = templateDefinitionAssignment;
        this.nodeToRegionSiteConverter = nodeToRegionSiteConverter;
        this.regionsService = siteService;
    }

    @Override
    public void addSite(String path) {
        try {
            this.handleAddSite(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleAddSite(String path) throws RepositoryException {
        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);
        Session websiteSession = MgnlContext.getJCRSession(workspace);

        Node regionsSiteNode = regionsSession.getNode(path);

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionsSiteNode);

        this.createSiteStructure(regionSite);

        websiteSession.save();
    }

    @Override
    public void addRegion(String path) {
        try {
            this.handleAddRegion(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleAddRegion(String path) throws RepositoryException, RegionNotFoundException {
        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);
        Session websiteSession = MgnlContext.getJCRSession(workspace);

        Node regionsSiteNode = regionsSession.getNode(path);

        String regionName = regionsSiteNode.getName();

        RegionSite regionSite = this.nodeToRegionSiteConverter.toBean(regionsSiteNode);

        Region regionToAdd = this.regionsService.getRegion(regionSite, regionName);

        if (regionToAdd != null) {
            this.createRegionStructure(regionSite, regionToAdd);
        }

        websiteSession.save();
    }

    @Override
    void createSiteStructure(RegionSite regionSite) throws RepositoryException {

        Node rootSiteNode = MgnlContext.getJCRSession(workspace).getRootNode();

        TemplateDefinition primaryTemplate = this.getPrimaryTemplate(regionSite);

        String regionSiteName = regionSite.getName();

        Node siteNode;

        if (!rootSiteNode.hasNode(regionSiteName)) {
            siteNode = rootSiteNode.addNode(regionSite.getName(), NodeTypes.Page.NAME);
            siteNode.setProperty(RegionsConstants.Properties.TITLE, regionSite.getTitle());
            this.setPrimaryTemplate(primaryTemplate, siteNode);

        } else {
            siteNode = rootSiteNode.getNode(regionSiteName);
        }

        for (Region region : regionSite.getRegions()) {

            if (!siteNode.hasNode(region.getName())) {
                Node regionNode = siteNode.addNode(region.getName(), NodeTypes.Page.NAME);
                regionNode.setProperty(RegionsConstants.Properties.TITLE, region.getTitle());
                this.setPrimaryTemplate(primaryTemplate, regionNode);
            }
        }
    }

    @Override
    void createRegionStructure(RegionSite regionSite, Region region) throws RepositoryException {
        TemplateDefinition primaryTemplate = this.getPrimaryTemplate(regionSite);

        Session websiteSession = MgnlContext.getJCRSession(workspace);

        Node rootNode = websiteSession.getRootNode();
        String regionSitePath = regionSite.getName();

        if (rootNode.hasNode(regionSitePath)) {
            Node siteNode = rootNode.getNode(regionSitePath);

            if (!siteNode.hasNode(region.getName())) {
                Node regionNode = siteNode.addNode(region.getName(), NodeTypes.Page.NAME);
                regionNode.setProperty(RegionsConstants.Properties.TITLE, region.getTitle());
                this.setPrimaryTemplate(primaryTemplate, regionNode);
            }
        } else {
            throw new ReplicationException("No region site node found");
        }

    }

    @Override
    public void renameRegion(String srcPath, String dstPath) {
        try {
            this.moveStructure(srcPath, dstPath);

        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
        }
    }

    @Override
    public void removeRegion(String path) {
        try {
            this.handleRemoveRegion(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleRemoveRegion(String path) throws RepositoryException {
        Session websiteSession = MgnlContext.getJCRSession(workspace);

        Node regionNode = websiteSession.getNode(path);

        if (!regionNode.hasNodes()) {
            regionNode.remove();
        }

        websiteSession.save();

    }

    @Override
    public void renameSite(String srcPath, String dstPath) {
        try {
            this.moveStructure(srcPath, dstPath);

        } catch (Exception e) {
            throw new ReplicationException(srcPath + " to " + dstPath, e);
        }
    }

    @Override
    public void removeSite(String path) {
        try {
            this.handleRemoveSite(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleRemoveSite(String path) throws RepositoryException {
        Session websiteSession = MgnlContext.getJCRSession(workspace);

        Node siteNode = websiteSession.getNode(path);

        this.removeStructure(siteNode);

        websiteSession.save();
    }

    @Override
    public void updateRegion(String path) {
        try {
            this.handleUpdateRegion(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleUpdateRegion(String path) throws RepositoryException {
        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);
        Session websiteSession = MgnlContext.getJCRSession(workspace);

        Node regionNode = regionsSession.getNode(path);
        Node websiteNode = websiteSession.getNode(path);

        websiteNode.setProperty(RegionsConstants.Properties.TITLE,
                PropertyUtil.getString(regionNode, RegionsConstants.Properties.TITLE));

        websiteSession.save();
    }

    private void setPrimaryTemplate(TemplateDefinition primaryTemplate, Node siteNode) {
        if (primaryTemplate != null) {
            try {
                siteNode.setProperty(NodeTypes.Renderable.TEMPLATE, primaryTemplate.getId());

            } catch (Exception e) {
                log.error("Unable to set default template {} on site node", primaryTemplate.getId());
            }
        }
    }

    private TemplateDefinition getPrimaryTemplate(RegionSite regionSite) throws RepositoryException {
        ConfiguredSiteTemplateAvailability templateAvailability =
                (ConfiguredSiteTemplateAvailability) regionSite.getTemplates().getAvailability();

        Map<String, TemplateConfig> templates = templateAvailability.getTemplates();

        if (templates.isEmpty()) {
            return getDefaultTemplate();
        }

        TemplateConfig primaryTemplateConfig = templates.values().stream().findFirst().get();
        String primaryTemplateId = primaryTemplateConfig.getId();

        return this.templateDefinitionRegistry.getAllDefinitions().stream()
                .filter(templateDefinition -> templateDefinition.getId().equals(primaryTemplateId))
                .findFirst()
                .orElse(getDefaultTemplate());
    }

    private TemplateDefinition getDefaultTemplate() throws RepositoryException {
        Node rootWebsiteNode = MgnlContext.getJCRSession(workspace).getRootNode();

        return templateDefinitionAssignment.getAvailableTemplates(rootWebsiteNode).stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateSite(String path) {
        try {
            this.handleUpdateSite(path);

        } catch (Exception e) {
            throw new ReplicationException(path, e);
        }
    }

    private void handleUpdateSite(String path) throws RepositoryException {
        Session regionsSession = MgnlContext.getJCRSession(RegionsConstants.REGIONS_WORKSPACE);
        Session websiteSession = MgnlContext.getJCRSession(workspace);

        String sitePath = NodeUtil.PATH_SEPARATOR + this.regionsService.getRootSiteNameFromPath(path);

        Node regionSiteNode = regionsSession.getNode(sitePath);
        Node websiteRootNode = websiteSession.getNode(sitePath);

        websiteRootNode.setProperty(RegionsConstants.Properties.TITLE,
                PropertyUtil.getString(regionSiteNode, RegionsConstants.Properties.TITLE));

        websiteSession.save();

    }
}
