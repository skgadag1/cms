package com.tbscg.doosan.magnolia.regions.validator;

import com.tbscg.doosan.magnolia.regions.i18n.RegionsI18nContentSupport;
import com.tbscg.doosan.magnolia.regions.model.Region;
import com.tbscg.doosan.magnolia.regions.model.RegionSite;
import com.tbscg.doosan.magnolia.regions.service.NoRegionSitesException;
import com.tbscg.doosan.magnolia.regions.service.RegionNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionSiteNotFoundException;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Validator factory for ensuring hreflangLocale values are unique across all languages in a website.
 * This validator checks that the value doesn't exist in any OTHER language within the same website.
 */
@Slf4j
public class UniqueAcrossLanguagesFactory extends AbstractFieldValidatorFactory<UniqueAcrossLanguagesDefinition, String> {

    private static final String HREFLANG_LOCALE_NODE = "hreflangLocale";

    private final String errorMessage;
    private final ValueContext<Item> valueContext;
    private final RegionsService regionsService;

    @Inject
    public UniqueAcrossLanguagesFactory(UniqueAcrossLanguagesDefinition definition, 
                                       ValueContext<Item> valueContext,
                                       RegionsService regionsService) {
        super(definition);
        this.valueContext = valueContext;
        this.regionsService = regionsService;
        this.errorMessage = StringUtils.defaultIfBlank(definition.getErrorMessage(), 
            "This hreflang locale is already used in another language within this website");
        log.info("UniqueAcrossLanguagesFactory instantiated");
    }

    @Override
    public Validator<String> createValidator() {
        return (value, ctx) -> {
            log.info("=== UniqueAcrossLanguagesValidator - validating: '{}'", value);
            
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
                
                // Get the language node
                Node languageNode = getLanguageNode(currentNode);
                if (languageNode == null) {
                    log.warn("Could not find language node");
                    return ValidationResult.ok();
                }
                log.info("Language node: {}", languageNode.getPath());
                
                // Extract path components
                String languagePath = languageNode.getPath();
                String[] pathParts = languagePath.split("/");
                if (pathParts.length < 3) {
                    log.warn("Invalid language path: {}", languagePath);
                    return ValidationResult.ok();
                }
                
                String websiteName = pathParts[1]; // /website/region/language
                
                // Check other languages in the website using RegionsService
                String duplicatePath = checkOtherLanguagesInWebsite(websiteName, languagePath, value);
                if (duplicatePath != null) {
                    String error = errorMessage + " (found at: " + duplicatePath + ")";
                    log.error("VALIDATION FAILED: {}", error);
                    return ValidationResult.error(error);
                }

                log.info("VALIDATION PASSED - no duplicates in other languages");
                return ValidationResult.ok();
                
            } catch (Exception e) {
                log.error("ERROR validating uniqueness across languages for value: " + value, e);
                return ValidationResult.error("Error validating uniqueness. Please contact administrator: " + e.getMessage());
            }
        };
    }

    private Node getCurrentNode() {
        try {
            Item item = valueContext.getSingleOrThrow();
            if (item instanceof Node) {
                Node node = (Node) item;
                log.info("Current node: {}", node.getPath());
                return node;
            }
            return null;
        } catch (Exception e) {
            log.error("Could not get current node from value context", e);
            return null;
        }
    }
    
    private Node getLanguageNode(Node currentNode) throws RepositoryException {
        // Current node could be the language node or a multifield item
        if (currentNode.getParent() != null && 
            currentNode.getParent().getName().equals(HREFLANG_LOCALE_NODE)) {
            // We're in a multifield item, go up to language node
            return currentNode.getParent().getParent();
        }
        // We're at the language node
        return currentNode;
    }
    
    /**
     * Checks if the hreflang value exists in any other language within the website.
     * Uses RegionsService and i18n support instead of JCR traversal.
     * 
     * @param websiteName the website name
     * @param currentLanguagePath the path of the current language being edited
     * @param value the hreflang code to check
     * @return path where duplicate was found, or null if no duplicate
     */
    private String checkOtherLanguagesInWebsite(String websiteName, String currentLanguagePath, String value) {
        log.info("Checking other languages in website: {}", websiteName);
        
        try {
            // Get the region site
            RegionSite regionSite = regionsService.getRegionSiteFromPath("/" + websiteName);
            
            // Get all regions in the website
            List<String> regionNames = regionsService.getRegionsForSite(websiteName);
            
            for (String regionName : regionNames) {
                log.debug("Checking region: {}", regionName);
                
                // Get the region and its i18n support which contains hreflang data
                Region region = regionsService.getRegion(regionSite, regionName);
                RegionsI18nContentSupport i18nSupport = region.getI18n();
                
                // Check if this value exists in any locale's hreflang codes
                Optional<Locale> foundInLocale = i18nSupport.findLocaleByHreflangCode(value);
                
                if (foundInLocale.isPresent()) {
                    String foundLanguagePath = "/" + websiteName + "/" + regionName + "/" 
                        + foundInLocale.get().toLanguageTag();
                    
                    // Skip if it's the current language being edited
                    if (!foundLanguagePath.equals(currentLanguagePath)) {
                        log.warn("Found duplicate hreflang '{}' in language: {}", 
                            value, foundLanguagePath);
                        return foundLanguagePath;
                    } else {
                        log.debug("Skipping current language: {}", foundLanguagePath);
                    }
                }
            }
            
            log.info("No duplicates found for hreflang code: {}", value);
            return null;
            
        } catch (RepositoryException | NoRegionSitesException | RegionSiteNotFoundException | RegionNotFoundException e) {
            log.error("Error checking other languages in website", e);
            return null;
        }
    }
}
