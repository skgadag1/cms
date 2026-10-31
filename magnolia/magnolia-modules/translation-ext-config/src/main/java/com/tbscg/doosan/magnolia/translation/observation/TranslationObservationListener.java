package com.tbscg.doosan.magnolia.translation.observation;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SimpleContext;
import info.magnolia.context.SystemContext;
import info.magnolia.module.observation.BaseConfiguration;
import info.magnolia.module.observation.ObservationConfiguration;
import info.magnolia.module.observation.commands.CommandEventListener;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JCR observation listener that monitors translation-related changes in workspaces.
 */
@Slf4j
public class TranslationObservationListener extends CommandEventListener implements BaseConfiguration {

    // Pattern to match language suffix in a node name (e.g., _es, _de, _en_US)
    private static final Pattern LANGUAGE_SUFFIX_PATTERN = Pattern.compile("_[a-z]{2}(_[A-Z]{2})?");

    private ObservationConfiguration configuration;

    @Override
    public void setConfiguration(ObservationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ObservationConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void onEvent(EventIterator events) {
        log.trace("Translation observation listener triggered - starting event processing");
        
        // Save current context (if any)
        Context ctx = MgnlContext.getWebContextOrNull();
        
        int eventCount = 0;
        while (events.hasNext()) {
            eventCount++;
            Event event = (Event) events.next();
            log.trace("Processing event #{}", eventCount);
            processEvent(event);
        }
        
        // Restore original context
        MgnlContext.setInstance(ctx);
        log.trace("Translation observation listener completed - processed {} events", eventCount);
    }
    
    /**
     * Processes a single event with proper context setup.
     * 
     * @param event the JCR event to process
     */
    private void processEvent(Event event) {
        try {
            String path = event.getPath();
            int eventType = event.getType();
            String eventTypeName = getEventTypeName(eventType);
            
            log.trace("Event received - Type: {}, Path: {}", eventTypeName, path);
            
            // Create and set execution context for this thread
            Context executionCtx = new SimpleContext(Components.getComponent(SystemContext.class));
            MgnlContext.setInstance(executionCtx);
            
            try {
                handleTranslationUpdate(path, eventType);
                log.trace("Event processing completed for path: {}", path);
                
            } catch (Exception e) {
                log.error("Error processing translation observation event for path: {}", path, e);
            } finally {
                // Release context for this event
                MgnlContext.release();
            }
            
        } catch (Exception e) {
            log.error("Can't process event", e);
        }
    }

    private void handleTranslationUpdate(String path, int eventType) {
        log.trace("handleTranslationUpdate called for path: {}, eventType: {}", path, getEventTypeName(eventType));
        
        if (eventType != Event.NODE_ADDED) {
            log.trace("Ignoring event - not a NODE_ADDED event (type: {})", getEventTypeName(eventType));
            return;
        }
        
        if (!isTranslatedNode(path)) {
            log.trace("Ignoring path - does not contain language suffix: {}", path);
            return;
        }
        
        log.trace("Path matches translation pattern: {}", path);
        
        try {
            copyMissingPropertiesFromDefaultLanguage(path);
        } catch (Exception e) {
            log.error("Error copying properties from default language for path: {}", path, e);
        }
    }
    
    /**
     * Checks if a node path contains any translated nodes (contains _{lang} anywhere in the path).
     * 
     * @param path the node path
     * @return true if the path contains _{lang} pattern in any node
     */
    private boolean isTranslatedNode(String path) {
        // Check if any part of the path contains language suffix (e.g., _es, _de, _fr, etc.)
        return LANGUAGE_SUFFIX_PATTERN.matcher(path).find();
    }
    
    /**
     * Copies properties from the English (default) counterpart node to the translated node
     * if they don't exist on the translated node.
     * 
     * @param i18nNodePath the path to the translated node
     * @throws RepositoryException if there's an error accessing the JCR
     */
    private void copyMissingPropertiesFromDefaultLanguage(String i18nNodePath) throws RepositoryException {
        log.trace("Starting property copy process for translated node: {}", i18nNodePath);
        
        Session session = MgnlContext.getJCRSession(getConfiguration().getRepository());
        
        // Get the translated node
        if (!session.nodeExists(i18nNodePath)) {
            log.warn("Translated node does not exist: {}", i18nNodePath);
            return;
        }
        
        Node i18nNode = session.getNode(i18nNodePath);
        log.trace("Retrieved translated node: {}", i18nNodePath);
        
        // Get the default (English) node path by removing the language suffix
        String defaultNodePath = getDefaultNodePath(i18nNodePath);
        log.trace("Computed default language path: {} -> {}", i18nNodePath, defaultNodePath);
        
        if (!session.nodeExists(defaultNodePath)) {
            log.warn("Default language node does not exist: {}", defaultNodePath);
            return;
        }
        
        Node defaultNode = session.getNode(defaultNodePath);
        log.trace("Retrieved default language node: {}", defaultNodePath);
        
        // Extract language code from the path for property suffix checking
        String languageCode = extractLanguageCode(i18nNodePath);
        log.trace("Extracted language code: {}", languageCode);
        
        log.info("Copying missing properties from default node '{}' to translated node '{}' (language: {})", 
                defaultNodePath, i18nNodePath, languageCode);
        
        // Track copied properties
        List<String> copiedProperties = new ArrayList<>();
        
        // Iterate through properties of the default node
        PropertyIterator propertyIterator = defaultNode.getProperties();
        while (propertyIterator.hasNext()) {
            Property defaultProperty = propertyIterator.nextProperty();
            String propertyName = defaultProperty.getName();
            
            if (propertyName.startsWith("jcr:") || propertyName.startsWith("mgnl:")) {
                log.trace("Skipping system property: {}", propertyName);
                continue;
            }
            
            if (!hasPropertyOrLanguageVariant(i18nNode, propertyName, languageCode)) {
                log.trace("Property '{}' does not exist on translated node - copying", propertyName);
                copyProperty(defaultProperty, i18nNode, propertyName);
                
                String propertyValue = getPropertyValueAsString(defaultProperty);
                copiedProperties.add(String.format("%s = %s", propertyName, propertyValue));
                
                log.info("Copied property '{}' with value '{}' from default language node to translated node", 
                        propertyName, propertyValue);
            } else {
                log.trace("Property '{}' already exists on translated node - skipping", propertyName);
            }
        }
        
        // Save the session
        if (!copiedProperties.isEmpty()) {
            log.trace("Saving JCR session with {} new properties", copiedProperties.size());
            session.save();
            log.trace("Session saved successfully");
        } else {
            log.trace("No properties copied - session save not needed");
        }
    }
    
    /**
     * Checks if a property exists on the node either with its original name or with a language suffix.
     * For example, if propertyName is "title" and languageCode is "es", this checks for both
     * "title" and "title_es".
     * 
     * @param node the node to check
     * @param propertyName the base property name
     * @param languageCode the language code (e.g., "es", "de", "fr")
     * @return true if either the property or its language variant exists
     * @throws RepositoryException if there's an error accessing the node
     */
    private boolean hasPropertyOrLanguageVariant(Node node, String propertyName, String languageCode) 
            throws RepositoryException {
        // Check if property exists with original name
        if (node.hasProperty(propertyName)) {
            log.trace("Property '{}' exists on node", propertyName);
            return true;
        }
        
        // Check if property exists with language suffix (e.g., title_es)
        if (languageCode != null && !languageCode.isEmpty()) {
            String languageVariantName = propertyName + "_" + languageCode;
            if (node.hasProperty(languageVariantName)) {
                log.trace("Property language variant '{}' exists on node", languageVariantName);
                return true;
            }
        }
        
        log.trace("Neither '{}' nor its language variant exists on node", propertyName);
        return false;
    }
    
    /**
     * Extracts the language code from a translated node path.
     * For example, "/bobcat/na/cards_es/item_es" returns "es".
     * 
     * @param i18nNodePath the translated node path
     * @return the language code (e.g., "es", "de", "fr") or null if not found
     */
    private String extractLanguageCode(String i18nNodePath) {
        java.util.regex.Matcher matcher = LANGUAGE_SUFFIX_PATTERN.matcher(i18nNodePath);
        if (matcher.find()) {
            // Extract the language code from the first match (e.g., "_es" -> "es")
            String suffix = matcher.group();
            String languageCode = suffix.substring(1); // Remove the leading underscore
            log.trace("Extracted language code '{}' from path: {}", languageCode, i18nNodePath);
            return languageCode;
        }
        log.trace("No language code found in path: {}", i18nNodePath);
        return null;
    }
    
    private String getEventTypeName(int eventType) {
        switch (eventType) {
            case Event.NODE_ADDED:
                return "NODE_ADDED";
            case Event.NODE_REMOVED:
                return "NODE_REMOVED";
            case Event.NODE_MOVED:
                return "NODE_MOVED";
            case Event.PROPERTY_ADDED:
                return "PROPERTY_ADDED";
            case Event.PROPERTY_REMOVED:
                return "PROPERTY_REMOVED";
            case Event.PROPERTY_CHANGED:
                return "PROPERTY_CHANGED";
            case Event.PERSIST:
                return "PERSIST";
            default:
                return "UNKNOWN(" + eventType + ")";
        }
    }
    
    /**
     * Gets the default node path by removing all language suffixes from the translated node path.
     * This handles cases where multiple nodes in the path have language suffixes.
     * 
     * @param i18nNodePath the translated node path 
     *                     (e.g., /bobcat/na/equipment/cards_es/cards1_es/asset/img)
     * @return the default node path with all language suffixes removed
     *         (e.g., /bobcat/na/equipment/cards/cards1/asset/img)
     */
    private String getDefaultNodePath(String i18nNodePath) {
        // Remove all language suffixes (e.g., _es, _de, _fr) from all nodes in the path
        return LANGUAGE_SUFFIX_PATTERN.matcher(i18nNodePath).replaceAll("");
    }
    
    /**
     * Copies a property from the source node to the target node.
     * 
     * @param sourceProperty the property to copy
     * @param targetNode the target node
     * @param propertyName the property name
     * @throws RepositoryException if there's an error copying the property
     */
    private void copyProperty(Property sourceProperty, Node targetNode, String propertyName) 
            throws RepositoryException {
        try {
            if (sourceProperty.isMultiple()) {
                // Handle multi-valued properties
                Value[] values = sourceProperty.getValues();
                targetNode.setProperty(propertyName, values);
            } else {
                // Handle single-valued properties
                Value value = sourceProperty.getValue();
                targetNode.setProperty(propertyName, value);
            }
        } catch (Exception e) {
            log.warn("Could not copy property '{}': {}", propertyName, e.getMessage());
        }
    }
    
    /**
     * Gets the property value as a string for logging purposes.
     * 
     * @param property the property
     * @return the property value as a string
     */
    private String getPropertyValueAsString(Property property) {
        try {
            if (property.isMultiple()) {
                Value[] values = property.getValues();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(values[i].getString());
                }
                sb.append("]");
                return sb.toString();
            } else {
                return property.getValue().getString();
            }
        } catch (Exception e) {
            return "<unable to read value: " + e.getMessage() + ">";
        }
    }
}

