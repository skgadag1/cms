package com.tbscg.doosan.magnolia.localizations.compatibility.app.availability;

import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.*;

/**
 * {@link AbstractAvailabilityRule} that checks if a given node is a localized package.
 *
 * @author apuigescobar
 */
@Deprecated
@Slf4j
public class IsLocalizedPackage extends AbstractAvailabilityRule {

    private final LocaleService localeService;
    private final SiteManager siteManager;

    @Inject
    public IsLocalizedPackage(LocaleService localeService, SiteManager siteManager) {
        this.localeService = localeService;
        this.siteManager = siteManager;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        if (itemId instanceof JcrItemId) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            try {
                Node node = NodeUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
                Collection<Node> nodeChildren = getDirectNodes(node);
                return isLocalizedPackage(node) || (!nodeChildren.isEmpty() && nodeChildren.stream().allMatch(this::isLocalizedPackage));
            } catch (RepositoryException e) {
                log.debug("Unable to verify if UI item [{}] is localizable ", itemId.toString(), e);
                return false;
            }
        }
        return false;
    }

    private boolean isLocalizedPackage(Node node) {
        try {
            return localeService.isLocalized(node) && isValidPackageStructure(node) && isSiteDefinitionLevel(node);
        } catch (RepositoryException e) {
            log.debug("Unable to verify if package is localizable for node [{}]", getPath(node), e);
            return false;
        }
    }

    private static boolean isValidPackageStructure(Node node) throws RepositoryException {
        Collection<Node> childrenNodes = getDirectNodes(node);
        return !childrenNodes.isEmpty() && childrenNodes.stream().allMatch(IsLocalizedPackage::isValidFolderPackageStructure);
    }

    private static boolean isValidFolderPackageStructure(Node node) {
        try {
            boolean isFolder = isNodeType(node, NodeTypes.Folder.NAME);
            Collection<Node> childrenNodes = getDirectNodes(node);
            boolean areChildrenConcepts = !childrenNodes.isEmpty() && childrenNodes.stream().allMatch(childNode -> isNodeType(childNode, NodeTypes.Content.NAME));
            return isFolder && areChildrenConcepts;
        } catch (RepositoryException e) {
            log.debug("Unable to verify folder package structure for node [{}]", getPath(node), e);
            return false;
        }
    }

    private boolean isSiteDefinitionLevel(Node node) {
        Site site = siteManager.getAssignedSite(node);
        if (site != null && site.getMappings() != null) {
            return site.getMappings().values().stream().anyMatch(mapping -> getPath(node).equals(mapping.getHandlePrefix()));
        }
        return false;
    }
}
