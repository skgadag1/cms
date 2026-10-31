package com.tbscg.doosan.magnolia.localizations.service;

import com.tbscg.doosan.magnolia.regions.service.LocaleService;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.*;

/**
 * Service for checking if a node is a localized package.
 * Extracted from IsLocalizedPackage availability rule to avoid code duplication.
 * 
 * @author modernization
 */
@Slf4j
public class LocalizationPackageService {
    
    private final LocaleService localeService;
    private final SiteManager siteManager;
    
    @Inject
    public LocalizationPackageService(LocaleService localeService, SiteManager siteManager) {
        this.localeService = localeService;
        this.siteManager = siteManager;
    }
    
    /**
     * Checks if the given node is a localized package.
     * A localized package must:
     * - Be localized
     * - Have valid package structure (folders containing content nodes)
     * - Be at site definition level
     *
     * @param node the node to check
     * @return true if the node is a localized package
     */
    public boolean isLocalizedPackage(Node node) {
        try {
            String nodePath = getPath(node);
            
            boolean isLocalized = localeService.isLocalized(node);
            log.debug("  Node [{}] - isLocalized: {}", nodePath, isLocalized);
            
            if (!isLocalized) {
                return false;
            }
            
            boolean hasValidStructure = isValidPackageStructure(node);
            log.debug("  Node [{}] - hasValidStructure: {}", nodePath, hasValidStructure);
            
            if (!hasValidStructure) {
                    return false;
            }
            
            boolean isSiteLevel = isSiteDefinitionLevel(node);
            log.debug("  Node [{}] - isSiteDefinitionLevel: {}", nodePath, isSiteLevel);
            
            return isSiteLevel;
        } catch (RepositoryException e) {
            log.error("Unable to verify if package is localizable for node [{}]", getPath(node), e);
            return false;
        }
    }
    
    /**
     * Validates that the node has the correct package structure.
     * A valid package structure has folders that contain content nodes.
     *
     * @param node the node to validate
     * @return true if the node has valid package structure
     * @throws RepositoryException if an error occurs accessing the repository
     */
    private boolean isValidPackageStructure(Node node) throws RepositoryException {
        String nodePath = getPath(node);
        
        if (!node.hasNodes()) {
            log.debug("    Node [{}] has no children - INVALID structure", nodePath);
            return false;
        }
        
        NodeIterator children = node.getNodes();
        if (!children.hasNext()) {
            log.debug("    Node [{}] has no children iterator - INVALID structure", nodePath);
            return false;
        }
        
        
        int childCount = 0;
        while (children.hasNext()) {
            Node child = children.nextNode();
            childCount++;
            if (!isValidFolderPackageStructure(child) ) {
                String childNodePath = getPath(child);
                log.debug("    Node [{}] - child #{} [{}] has INVALID folder structure", nodePath, childCount, childNodePath);
                return false;
            }
        }
        
        log.debug("    Node [{}] has VALID package structure with {} children", nodePath, childCount);
        return true;
    }
    
    /**
     * Validates that a folder has the correct structure 
     *  - contains only content nodes
     *  - or contains only folders that themselves have valid package structure
     *
     * @param node the folder node to validate
     * @return true if the folder has valid structure
     */
    private boolean isValidFolderPackageStructure(Node node) {
        try {
            String nodePath = getPath(node);
            boolean isFolder = isNodeType(node, NodeTypes.Folder.NAME);
            
            if (!isFolder) {
                log.debug("      Node [{}] is NOT a folder (type: {})", nodePath, node.getPrimaryNodeType().getName());
                return false;
            }
            
            if (!node.hasNodes()) {
                log.debug("      Folder [{}] has no children", nodePath);
                return false;
            }

            //Validate folder structure recursively if has only folders
            boolean hasOnlyFolders = hasAllChildrenOfType(node, NodeTypes.Folder.NAME);
            if (hasOnlyFolders) {
                NodeIterator children = node.getNodes();
                while (children.hasNext()) {
                    Node child = children.nextNode();
                    String childNodePath = getPath(child);
                    if (!isValidFolderPackageStructure(child)) {
                        log.debug("      Folder [{}] - child [{}] has INVALID folder structure", nodePath, childNodePath);
                        return false;
                    }
                }
                log.debug("      Folder [{}] has VALID structure with all its child folders", nodePath);
                return true;
            }

            //else validate that has only content nodes
            boolean hasOnlyContentNodes = hasAllChildrenOfType(node, NodeTypes.Content.NAME);

            if (!hasOnlyContentNodes) {
                log.debug("      Folder [{}] - is NOT a valid folder structure (has mixed content and folders)", 
                    nodePath);
                return false;
            }
          
            log.debug("      Folder [{}] has VALID structure with", nodePath);
            return true;
        } catch (RepositoryException e) {
            log.error("Unable to verify folder package structure for node [{}]", getPath(node), e);
            return false;
        }
    }
    
    private boolean hasAllChildrenOfType(Node node, String nodeType) throws RepositoryException {
        NodeIterator children = node.getNodes();
        while (children.hasNext()) {
            Node child = children.nextNode();
            if (!isNodeType(child, nodeType)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks if the node is at the site definition level.
     * The node must match one of the site's handlePrefix mappings.
     *
     * @param node the node to check
     * @return true if the node is at site definition level
     */
    private boolean isSiteDefinitionLevel(Node node) {
        String nodePath = getPath(node);
        Site site = siteManager.getAssignedSite(node);
        
        if (site == null) {
            log.debug("    Node [{}] - no site assigned", nodePath);
            return false;
        }
        
        log.debug("    Node [{}] - assigned to site: {}", nodePath, site.getName());
        
        if (site.getMappings() == null) {
            log.debug("    Node [{}] - site [{}] has no mappings", nodePath, site.getName());
            return false;
        }
        
        boolean isSiteLevel = site.getMappings().values().stream()
            .anyMatch(mapping -> {
                String handlePrefix = mapping.getHandlePrefix();
                boolean matches = nodePath.equals(handlePrefix);
                log.debug("      Comparing node path [{}] with handlePrefix [{}]: {}", nodePath, handlePrefix, matches);
                return matches;
            });
        
        log.debug("    Node [{}] - isSiteDefinitionLevel: {}", nodePath, isSiteLevel);
        return isSiteLevel;
    }
}

