package com.tbscg.doosan.magnolia.regions.validator;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validator factory for ensuring no duplicate values within the same multifield list.
 * This validator checks that the value doesn't appear multiple times in the current language's hreflang list.
 * It validates against the current form state (including unsaved values) before the form is saved.
 */
@Slf4j
public class UniqueWithinListFactory extends AbstractFieldValidatorFactory<UniqueWithinListDefinition, String> {

    private final String errorMessage;

    @Inject
    public UniqueWithinListFactory(UniqueWithinListDefinition definition) {
        super(definition);
        this.errorMessage = StringUtils.defaultIfBlank(definition.getErrorMessage(), 
            "This hreflang locale is already used in this language");
        log.info("UniqueWithinListFactory instantiated");
    }

    @Override
    public Validator<String> createValidator() {
        return (value, ctx) -> {
            log.info("=== UniqueWithinListValidator - validating: '{}'", value);
            
            if (StringUtils.isBlank(value)) {
                log.info("Value is blank, skipping validation");
                return ValidationResult.ok();
            }

            try {
                // Get the current field component from the Vaadin context
                Component currentField = extractComponentFromContext(ctx);
                
                if (currentField != null) {
                    log.info("Found Vaadin component, checking form state");
                    int duplicates = countDuplicatesInFormState(currentField, value);
                    if (duplicates > 0) {
                        log.error("VALIDATION FAILED - {} duplicate(s) found in form state", duplicates);
                        return ValidationResult.error(errorMessage);
                    }
                    log.info("VALIDATION PASSED - no duplicates found in form state");
                    return ValidationResult.ok();
                } else {
                    log.warn("Could not extract Vaadin component from context, skipping validation");
                    return ValidationResult.ok();
                }
                
            } catch (Exception e) {
                log.error("ERROR validating uniqueness within list for value: " + value, e);
                return ValidationResult.error("Error validating uniqueness. Please contact administrator: " + e.getMessage());
            }
        };
    }
    
    /**
     * Extract the Vaadin Component from the ValueContext.
     * This allows us to access the UI component hierarchy.
     */
    private Component extractComponentFromContext(ValueContext ctx) {
        try {
            // The Vaadin ValueContext wraps the HasValue (field) component
            Optional<?> component = ctx.getComponent();
            if (component.isPresent() && component.get() instanceof Component) {
                return (Component) component.get();
            }
        } catch (Exception e) {
            log.debug("Could not extract component from context: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Count how many times the value appears in the current form state (UI components).
     * This checks all fields in the multifield for matching values.
     * If the value appears more than once (count > 1), it's a duplicate.
     */
    private int countDuplicatesInFormState(Component currentField, String value) {
        try {
            // Find the parent multifield container by walking up the component tree
            Component parent = currentField.getParent();
            int maxDepth = 15; // Increase depth to search higher up the tree
            int depth = 0;
            
            log.info("Starting component tree traversal from field: {}", currentField.getClass().getSimpleName());
            
            while (parent != null && depth < maxDepth) {
                String parentClassName = parent.getClass().getSimpleName();
                log.info("  Depth {}: {} - checking for fields...", depth, parentClassName);
                
                // Look for a layout or container that might hold all multifield items
                List<AbstractField<?>> allFields = findAllFieldsInContainer(parent);
                
                log.info("    Found {} fields at this level", allFields.size());
                
                // We want to find a container that has multiple fields (the multifield items)
                // Keep going up until we find a container with more than just the current field
                if (allFields.size() > 1) {
                    log.info("Found container with multiple fields ({}) at depth {}: {}", 
                            allFields.size(), depth, parentClassName);
                    int occurrenceCount = 0;
                    
                    // Count how many times this value appears in ALL fields
                    for (AbstractField<?> field : allFields) {
                        try {
                            Object fieldValue = field.getValue();
                            log.debug("Checking field with value: {}", fieldValue);
                            if (fieldValue != null && value.equals(fieldValue.toString())) {
                                occurrenceCount++;
                                log.info("Found occurrence #{} of value '{}'", occurrenceCount, value);
                            }
                        } catch (Exception e) {
                            log.debug("Could not get value from field: {}", e.getMessage());
                        }
                    }
                    
                    log.info("Total occurrences of '{}': {}", value, occurrenceCount);
                    
                    // If it appears more than once, we have duplicates
                    // Return the number of duplicates (occurrences - 1)
                    return occurrenceCount > 1 ? occurrenceCount - 1 : 0;
                }
                
                parent = parent.getParent();
                depth++;
            }
            
            log.warn("Could not find multifield container in component hierarchy after {} levels", depth);
        } catch (Exception e) {
            log.error("Error checking form state: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Find all AbstractField components in a container.
     */
    private List<AbstractField<?>> findAllFieldsInContainer(Component container) {
        List<AbstractField<?>> fields = new ArrayList<>();
        
        try {
            if (container instanceof com.vaadin.ui.HasComponents) {
                com.vaadin.ui.HasComponents hasComponents = (com.vaadin.ui.HasComponents) container;
                hasComponents.iterator().forEachRemaining(child -> {
                    fields.addAll(findFieldsRecursively(child));
                });
            }
        } catch (Exception e) {
            log.debug("Error finding fields in container: {}", e.getMessage());
        }
        
        return fields;
    }
    
    /**
     * Recursively search for AbstractField components in a component tree.
     */
    private List<AbstractField<?>> findFieldsRecursively(Component component) {
        List<AbstractField<?>> fields = new ArrayList<>();
        
        if (component instanceof AbstractField) {
            fields.add((AbstractField<?>) component);
        } else if (component instanceof com.vaadin.ui.HasComponents) {
            com.vaadin.ui.HasComponents hasComponents = (com.vaadin.ui.HasComponents) component;
            hasComponents.iterator().forEachRemaining(child -> {
                fields.addAll(findFieldsRecursively(child));
            });
        }
        
        return fields;
    }
}
