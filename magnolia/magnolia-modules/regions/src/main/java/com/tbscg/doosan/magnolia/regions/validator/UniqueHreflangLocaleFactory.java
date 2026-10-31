package com.tbscg.doosan.magnolia.regions.validator;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * Validator factory for ensuring hreflangLocale values are unique within a dxp:website node.
 * Checks all dxp:language nodes under the same website to prevent duplicates.
 */
@Slf4j
public class UniqueHreflangLocaleFactory extends AbstractFieldValidatorFactory<UniqueHreflangLocaleDefinition, String> {

    private static final String HREFLANG_LOCALE_NODE = "hreflangLocale";
    private static final String FIELD_PROPERTY = "field";
    private static final String LANGUAGE_NODE_TYPE = "dxp:language";
    private static final String REGION_NODE_TYPE = "dxp:region";
    private static final String WEBSITE_NODE_TYPE = "dxp:website";

    private final String errorMessage;
    private final ValueContext<Item> valueContext;

    @Inject
    public UniqueHreflangLocaleFactory(UniqueHreflangLocaleDefinition definition, 
                                       ValueContext<Item> valueContext) {
        super(definition);
        this.valueContext = valueContext;
        this.errorMessage = StringUtils.defaultIfBlank(definition.getErrorMessage(), 
            "This hreflang locale is already used in another language within this website");
        log.info("UniqueHreflangLocaleFactory instantiated");
    }

    @Override
    public Validator<String> createValidator() {
        return (value, ctx) -> {
            log.info("========================================");
            log.info("UniqueHreflangLocaleFactory - VALIDATOR TRIGGERED for value: '{}'", value);
            log.info("========================================");
            
            if (StringUtils.isBlank(value)) {
                log.info("Value is blank, skipping validation");
                return ValidationResult.ok();
            }

            try {
                Node currentNode = getCurrentNode();
                if (currentNode == null) {
                    log.warn("Current node is null, cannot validate");
                    return ValidationResult.ok();
                }
                log.info("Current node retrieved: {}", currentNode.getPath());

                String duplicatePath = findDuplicateHreflangLocale(value, currentNode);
                if (duplicatePath != null) {
                    String error = errorMessage + " (found at: " + duplicatePath + ")";
                    log.error("========================================");
                    log.error("VALIDATION FAILED: {}", error);
                    log.error("========================================");
                    return ValidationResult.error(error);
                }

                log.info("========================================");
                log.info("VALIDATION PASSED for value: '{}'", value);
                log.info("========================================");
                return ValidationResult.ok();
            } catch (Exception e) {
                log.error("========================================");
                log.error("ERROR validating hreflangLocale uniqueness for value: " + value, e);
                log.error("========================================");
                return ValidationResult.error("Error validating uniqueness. Please contact administrator: " + e.getMessage());
            }
        };
    }

    private Node getCurrentNode() {
        try {
            Item item = valueContext.getSingleOrThrow();
            log.info("ValueContext item type: {}", item != null ? item.getClass().getName() : "null");
            
            if (item instanceof Node) {
                Node node = (Node) item;
                log.info("Retrieved node: {}", node.getPath());
                log.info("Node name: {}", node.getName());
                log.info("Node type: {}", node.getPrimaryNodeType().getName());
                return node;
            } else {
                log.warn("ValueContext item is not a Node, it's: {}", item != null ? item.getClass().getName() : "null");
                return null;
            }
        } catch (Exception e) {
            log.error("Could not get current node from value context", e);
            return null;
        }
    }

    private String findDuplicateHreflangLocale(String value, Node currentNode) throws RepositoryException {
        log.info("=== START Validating hreflangLocale: '{}' for node: {}", value, currentNode.getPath());
        log.info("Current node type: {}", currentNode.getPrimaryNodeType().getName());
        
        // Current node could be either:
        // 1. The dxp:language node (if this is a new multifield item being added)
        // 2. A multifield item node like hreflangLocale0, hreflangLocale1 (if editing existing)
        
        // Determine if we're in a multifield item or the language node
        Node languageNode;
        Node currentItemNode = null;
        
        if (currentNode.getParent() != null && 
            currentNode.getParent().getName().equals(HREFLANG_LOCALE_NODE)) {
            // We're in a multifield item (e.g., hreflangLocale0)
            currentItemNode = currentNode;
            languageNode = currentNode.getParent().getParent();
            log.info("Current node is a multifield item: {}", currentItemNode.getPath());
            log.info("Parent language node: {}", languageNode.getPath());
        } else {
            // We're at the language node level
            languageNode = currentNode;
            log.info("Current node is the language node: {}", languageNode.getPath());
        }
        
        // Get the multifield container
        Node currentMultiFieldContainer = getCurrentMultiFieldContainer(languageNode);
        if (currentMultiFieldContainer == null) {
            log.error("FAILED: Could not find hreflangLocale child node in language node");
            return null;
        }
        log.info("Multifield container: {}", currentMultiFieldContainer.getPath());
        
        // Step 1: Check current list - count occurrences excluding the current item being edited
        int countInCurrentList = countValueOccurrencesExcluding(currentMultiFieldContainer, value, currentItemNode);
        log.info("RESULT: Value '{}' appears {} times in current language list (excluding current item)", value, countInCurrentList);
        
        // If value already exists in current list (not counting the item being edited), it's a duplicate
        if (countInCurrentList > 0) {
            log.error("VALIDATION FAILED: Duplicate found in current language: '{}' appears {} times", value, countInCurrentList);
            return currentMultiFieldContainer.getPath();
        }
        
        // Step 2: Check other languages in the same website (all regions) - ensure value appears zero times
        Node websiteNode = findWebsiteNode(languageNode);
        if (websiteNode == null) {
            log.warn("Could not find parent website node");
            return null;
        }
        
        String duplicatePath = checkOtherLanguagesInWebsite(websiteNode, currentMultiFieldContainer, value);
        if (duplicatePath != null) {
            log.error("VALIDATION FAILED: Duplicate found in another language: '{}' exists at {}", value, duplicatePath);
            return duplicatePath;
        }
        
        log.info("=== END: No duplicates found for value '{}' - VALIDATION PASSED", value);
        return null;
    }

    /**
     * Gets the multifield container (hreflangLocale node) for the current language node.
     * Structure: dxp:language/hreflangLocale/hreflangLocale0/field, hreflangLocale1/field, etc.
     * Current node is dxp:language
     * We need to get the hreflangLocale child node
     */
    private Node getCurrentMultiFieldContainer(Node languageNode) throws RepositoryException {
        log.info("  Looking for hreflangLocale child node in language: {}", languageNode.getPath());
        
        // Current node is the dxp:language node
        // Check if it has a hreflangLocale child node
        if (languageNode.hasNode(HREFLANG_LOCALE_NODE)) {
            Node container = languageNode.getNode(HREFLANG_LOCALE_NODE);
            log.info("    -> Found hreflangLocale container: {}", container.getPath());
            return container;
        }
        
        log.warn("  Language node does not have a hreflangLocale child node");
        return null;
    }

    /**
     * Counts how many times a value appears in a multifield container.
     */
    private int countValueOccurrences(Node multiFieldContainer, String value) throws RepositoryException {
        return countValueOccurrencesExcluding(multiFieldContainer, value, null);
    }
    
    /**
     * Counts how many times a value appears in a multifield container, excluding a specific node.
     * @param multiFieldContainer The container to search in
     * @param value The value to search for
     * @param excludeNode The node to exclude from counting (can be null)
     * @return The count of occurrences
     */
    private int countValueOccurrencesExcluding(Node multiFieldContainer, String value, Node excludeNode) throws RepositoryException {
        int count = 0;
        NodeIterator children = multiFieldContainer.getNodes();
        
        String excludePath = excludeNode != null ? excludeNode.getPath() : null;
        log.info("  Counting occurrences of '{}' in container: {} (excluding: {})", 
            value, multiFieldContainer.getPath(), excludePath != null ? excludePath : "none");
        
        while (children.hasNext()) {
            Node child = children.nextNode();
            log.info("    Checking child node: {}", child.getPath());
            
            // Skip the node we're currently editing
            if (excludeNode != null && child.getPath().equals(excludePath)) {
                log.info("      -> Skipped (current item being edited)");
                continue;
            }
            
            // Skip deleted nodes
            if (child.hasProperty(NodeTypes.Deleted.DELETED)) {
                log.info("      -> Skipped (deleted)");
                continue;
            }
            
            // Check if this child has the value
            if (child.hasProperty(FIELD_PROPERTY)) {
                String existingValue = child.getProperty(FIELD_PROPERTY).getString();
                log.info("      -> Has field property with value: '{}'", existingValue);
                if (value.equals(existingValue)) {
                    count++;
                    log.info("      -> MATCH! Count is now: {}", count);
                }
            } else {
                log.info("      -> No field property");
            }
        }
        
        log.info("  Total count: {}", count);
        return count;
    }

    /**
     * Checks all other languages across all regions in the website (excluding current language) for the value.
     * Returns the path of the duplicate if found, null otherwise.
     */
    private String checkOtherLanguagesInWebsite(Node websiteNode, Node currentMultiFieldContainer, String value) throws RepositoryException {
        log.info("Checking other languages in website: {}", websiteNode.getPath());
        
        String currentMultiFieldPath = currentMultiFieldContainer.getPath();
        
        // Iterate through all child nodes of the website (regions)
        NodeIterator regions = websiteNode.getNodes();
        
        while (regions.hasNext()) {
            Node regionNode = regions.nextNode();
            
            // Check if this child is a region node
            if (!regionNode.isNodeType(REGION_NODE_TYPE)) {
                continue;
            }
            
            log.info("  Checking region: {}", regionNode.getPath());
            
            // Skip deleted region nodes
            if (regionNode.hasProperty(NodeTypes.Deleted.DELETED)) {
                log.info("    -> Skipped (deleted)");
                continue;
            }
            
            // Check all languages in this region
            String duplicateInRegion = checkLanguagesInRegion(regionNode, currentMultiFieldPath, value);
            if (duplicateInRegion != null) {
                return duplicateInRegion;
            }
        }
        
        log.info("No duplicates found in other languages across all regions");
        return null;
    }
    
    /**
     * Checks all languages in a specific region for the value.
     */
    private String checkLanguagesInRegion(Node regionNode, String currentMultiFieldPath, String value) throws RepositoryException {
        NodeIterator languages = regionNode.getNodes();
        
        while (languages.hasNext()) {
            Node languageNode = languages.nextNode();
            
            // Check if this child is a language node
            if (!languageNode.isNodeType(LANGUAGE_NODE_TYPE)) {
                continue;
            }
            
            log.info("    Checking language: {}", languageNode.getPath());
            
            // Skip deleted language nodes
            if (languageNode.hasProperty(NodeTypes.Deleted.DELETED)) {
                log.info("      -> Skipped (deleted)");
                continue;
            }
            
            // Check if this language has a hreflangLocale multifield
            if (!languageNode.hasNode(HREFLANG_LOCALE_NODE)) {
                log.info("      -> No hreflangLocale child");
                continue;
            }
            
            Node otherMultiFieldContainer = languageNode.getNode(HREFLANG_LOCALE_NODE);
            
            // Skip the current language's multifield (we already checked it)
            if (otherMultiFieldContainer.getPath().equals(currentMultiFieldPath)) {
                log.info("      -> Skipping current language multifield");
                continue;
            }
            
            // Count occurrences in this other language
            int countInOtherLanguage = countValueOccurrences(otherMultiFieldContainer, value);
            
            if (countInOtherLanguage > 0) {
                log.warn("      -> Found {} occurrences of '{}' in this language!", 
                    countInOtherLanguage, value);
                return otherMultiFieldContainer.getPath();
            }
            
            log.info("      -> No occurrences found");
        }
        
        return null;
    }
    
    /**
     * Finds the parent dxp:website node
     */
    private Node findWebsiteNode(Node languageNode) throws RepositoryException {
        log.debug("Finding parent website node for: {}", languageNode.getPath());
        
        Node current = languageNode;
        while (current != null && !current.getPath().equals("/")) {
            if (current.isNodeType(WEBSITE_NODE_TYPE)) {
                log.info("Found website node: {}", current.getPath());
                return current;
            }
            current = current.getParent();
        }
        
        log.warn("No website node found in hierarchy");
        return null;
    }

}
