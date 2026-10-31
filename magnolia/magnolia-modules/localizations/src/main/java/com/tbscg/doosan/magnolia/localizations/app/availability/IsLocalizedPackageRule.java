package com.tbscg.doosan.magnolia.localizations.app.availability;

import com.tbscg.doosan.magnolia.localizations.service.LocalizationPackageService;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.availability.rule.AbstractJcrRule;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getDirectNodes;
import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath;

/**
 * Availability rule that checks if a given node is a localized package.
 * Uses {@link LocalizationPackageService} for the actual validation logic.
 * 
 * This rule checks both the node itself and its direct children. A node is considered
 * available if either:
 * - The node itself is a localized package
 * - All direct children of the node are localized packages
 * 
 * @author modernization
 */
@Slf4j
public class IsLocalizedPackageRule extends AbstractJcrRule<IsLocalizedPackageRuleDefinition> {
    
    private final LocalizationPackageService localizationPackageService;
    
    public IsLocalizedPackageRule(
            AvailabilityDefinition availabilityDefinition,
            IsLocalizedPackageRuleDefinition ruleDefinition,
            LocalizationPackageService localizationPackageService) {
        super(availabilityDefinition, ruleDefinition);
        this.localizationPackageService = localizationPackageService;
    }
    
    @Override
    protected boolean isAvailableForJcrItem(Item item) throws RepositoryException {
        if (!item.isNode()) {
            return false;
        }
        
        try {
            Node node = (Node) item;
            Collection<Node> nodeChildren = getDirectNodes(node);
            
            boolean isLocalizedPackage = localizationPackageService.isLocalizedPackage(node);
            boolean areAllChildrenLocalizedPackages = !nodeChildren.isEmpty() && 
                nodeChildren.stream().allMatch(localizationPackageService::isLocalizedPackage);
            
            log.debug("Node [{}] - isLocalizedPackage: {}, children count: {}, all children are packages: {}", 
                getPath(node), isLocalizedPackage, nodeChildren.size(), areAllChildrenLocalizedPackages);
            
            return isLocalizedPackage || areAllChildrenLocalizedPackages;
        } catch (RepositoryException e) {
            log.debug("Unable to verify if UI item [{}] is localizable", getPath(item), e);
            return false;
        }
    }
}

