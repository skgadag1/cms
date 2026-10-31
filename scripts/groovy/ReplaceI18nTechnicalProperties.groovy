#!/usr/bin/env groovy
/**
 * Replace i18n Technical Properties Script
 * 
 * This script replaces technical properties (id, name, icon, field) in localized nodes
 * with their English counterparts. Unlike SyncI18nProperties.groovy which adds missing
 * properties, this script REPLACES existing values to ensure consistency.
 * 
 * Important: If both 'name' and 'name_es' exist in an _es node, BOTH will be overwritten
 * with the English value.
 * 
 * Example:
 *   English: email -> elements -> elements0 -> component -> inputField
 *     - id: 'email'
 *     - name: 'email'
 *     - icon: 'none'
 *   
 *   Spanish: email -> elements_es -> elements0_es -> component -> inputField
 *     Before: id: 'DIRECCIÓN', name: 'address', name_es: 'DIRECCIÓN', icon: 'ninguno'
 *     After:  id: 'email', name: 'email', name_es: 'email', icon: 'none'
 * 
 * Example 2 - Full sync nodes (hiddenInputField, dynamicHiddenField):
 *   ALL non-Magnolia properties are replaced from English to localized versions
 * 
 * Usage:
 *   - Set WORKSPACE_NAME to the target workspace
 *   - Set DRY_RUN to true to see what would change without applying
 *   - Set DRY_RUN to false to apply changes
 *   - Modify TECHNICAL_PROPERTIES list to add more properties to sync
 *   - Modify FULL_SYNC_NODES list to add more node types for full sync
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.Property
import javax.jcr.PropertyIterator
import javax.jcr.Session

// ============= CONFIGURATION =============
WORKSPACE_NAME = "formsType" // Change to your target workspace
DRY_RUN = true // Set to false to apply changes
LANGUAGE_SUFFIXES = ["_es"] // Language suffixes to process
START_PATH = "/bobcat/na" // Root path to start from

// Properties to replace (technical properties that should always match EN)
TECHNICAL_PROPERTIES = [
    "name", "icon", "id", "field", "cityId", "cityName", "cityValidation", "countryId", "countryName", 
    "countryValidation", "stateId", "stateName", "stateValidation", "streetId", "streetName", 
    "streetValidation", "zipId", "zipName", "zipValidation", "validation",
    "captchaV2SiteKey", "captchaV3SiteKey"
]

// Node names where ALL properties (except Magnolia metadata) should be replaced
FULL_SYNC_NODES = ["hiddenInputField", "dynamicHiddenField"]

// Properties to exclude from any processing (metadata properties)
EXCLUDED_PROPERTIES = [
    "jcr:primaryType",
    "jcr:uuid",
    "jcr:created",
    "jcr:createdBy",
    "mgnl:created",
    "mgnl:createdBy",
    "mgnl:lastModified",
    "mgnl:lastModifiedBy",
    "mgnl:lastActivated",
    "mgnl:lastActivatedBy",
    "mgnl:activationStatus",
    "mgnl:lastActivatedVersion",
    "mgnl:lastActivatedVersionCreated",
    "versionTime"
]

// ============= STATISTICS =============
stats = [
    nodesScanned: 0,
    i18nNodePairsFound: 0,
    propertiesReplaced: 0,
    nodesWithChanges: 0,
    changedNodes: []
]

// ============= HELPER FUNCTIONS =============

/**
 * Extracts the language code from a property or node name
 */
def extractLanguage(String name) {
    for (suffix in LANGUAGE_SUFFIXES) {
        if (name.endsWith(suffix)) {
            return suffix
        }
    }
    return null
}

/**
 * Removes language suffix from a name
 */
def removeLanguageSuffix(String name) {
    for (suffix in LANGUAGE_SUFFIXES) {
        if (name.endsWith(suffix)) {
            return name.substring(0, name.length() - suffix.length())
        }
    }
    return name
}

/**
 * Checks if a property should be excluded from processing
 */
def isExcludedProperty(String propertyName) {
    return EXCLUDED_PROPERTIES.contains(propertyName)
}

/**
 * Checks if a property is a technical property that should be replaced
 */
def isTechnicalProperty(String propertyName) {
    return TECHNICAL_PROPERTIES.contains(propertyName)
}

/**
 * Checks if a node should have ALL properties synced (not just technical ones)
 */
def isFullSyncNode(Node node) {
    def nodeName = node.getName()
    return FULL_SYNC_NODES.contains(nodeName)
}

/**
 * Checks if a property should be synced based on node type
 */
def shouldSyncProperty(Node enNode, String propertyName) {
    // Always skip excluded properties
    if (isExcludedProperty(propertyName)) {
        return false
    }
    
    // Skip if property has language suffix in EN
    if (extractLanguage(propertyName) != null) {
        return false
    }
    
    // If this is a full-sync node, sync all non-excluded properties
    if (isFullSyncNode(enNode)) {
        return true
    }
    
    // Otherwise, only sync technical properties
    return isTechnicalProperty(propertyName)
}

/**
 * Gets property value as string for comparison/display
 */
def getPropertyValueAsString(Property prop) {
    try {
        if (prop.isMultiple()) {
            return prop.getValues().collect { it.getString() }.toString()
        } else {
            return prop.getValue().getString()
        }
    } catch (Exception e) {
        return "[complex value: ${e.message}]"
    }
}

/**
 * Recursively replaces technical properties in i18n node from EN node
 */
def syncNodeProperties(Node enNode, Node i18nNode, String language, String nodePath, int depth = 0) {
    def changes = []
    def nodeChanges = [
        path: nodePath,
        language: language,
        properties: []
    ]
    
    // Get properties from EN node
    PropertyIterator propIterator = enNode.getProperties()
    while (propIterator.hasNext()) {
        Property enProp = propIterator.nextProperty()
        String propName = enProp.getName()
        
        // Check if this property should be synced based on node type
        if (!shouldSyncProperty(enNode, propName)) {
            continue
        }
        
        // Get EN value
        def enValue = getPropertyValueAsString(enProp)
        
        // Check if i18n node has this property (with or without language suffix)
        // Handle BOTH cases - property might exist with and without suffix
        def propertiesToReplace = []
        
        // Check for property without language suffix
        if (i18nNode.hasProperty(propName)) {
            propertiesToReplace.add([
                name: propName,
                prop: i18nNode.getProperty(propName)
            ])
        }
        
        // Check for property with language suffix
        def propNameWithSuffix = propName + language
        if (i18nNode.hasProperty(propNameWithSuffix)) {
            propertiesToReplace.add([
                name: propNameWithSuffix,
                prop: i18nNode.getProperty(propNameWithSuffix)
            ])
        }
        
        if (!propertiesToReplace.isEmpty()) {
            // Process each property variant found
            propertiesToReplace.each { propInfo ->
                def i18nPropName = propInfo.name
                def i18nProp = propInfo.prop
                def i18nValue = getPropertyValueAsString(i18nProp)
                
                if (!enValue.equals(i18nValue)) {
                    // Values differ - need to replace
                    def changeInfo = [
                        property: i18nPropName,
                        enProperty: propName,
                        oldValue: i18nValue,
                        newValue: enValue
                    ]
                    nodeChanges.properties.add(changeInfo)
                    
                    if (!DRY_RUN) {
                        // Copy the property value from EN to i18n
                        try {
                            if (enProp.isMultiple()) {
                                i18nNode.setProperty(i18nPropName, enProp.getValues())
                            } else {
                                i18nNode.setProperty(i18nPropName, enProp.getValue())
                            }
                            stats.propertiesReplaced++
                        } catch (Exception e) {
                            println "ERROR: Failed to replace property ${i18nPropName} at ${nodePath}: ${e.message}"
                        }
                    }
                }
            }
        } else {
            // Property doesn't exist in i18n node at all - add it
            def changeInfo = [
                property: propName,
                enProperty: propName,
                oldValue: "[MISSING]",
                newValue: enValue
            ]
            nodeChanges.properties.add(changeInfo)
            
            if (!DRY_RUN) {
                try {
                    if (enProp.isMultiple()) {
                        i18nNode.setProperty(propName, enProp.getValues())
                    } else {
                        i18nNode.setProperty(propName, enProp.getValue())
                    }
                    stats.propertiesReplaced++
                } catch (Exception e) {
                    println "ERROR: Failed to add property ${propName} at ${nodePath}: ${e.message}"
                }
            }
        }
    }
    
    // Only add to changes if there were property changes
    if (!nodeChanges.properties.isEmpty()) {
        changes.add(nodeChanges)
        stats.nodesWithChanges++
        stats.changedNodes.add(nodePath)
    }
    
    // Recursively process child nodes
    def enChildren = NodeUtil.getNodes(enNode)
    def i18nChildren = NodeUtil.getNodes(i18nNode)
    
    // Create a map of i18n children by their base name
    def i18nChildrenMap = [:]
    i18nChildren.each { i18nChild ->
        def baseName = removeLanguageSuffix(i18nChild.getName())
        i18nChildrenMap[baseName] = i18nChild
    }
    
    // Process each EN child
    enChildren.each { enChild ->
        def childName = enChild.getName()
        def langSuffix = extractLanguage(childName)
        
        // Skip if this is already a language-specific node in EN
        if (langSuffix != null) {
            return
        }
        
        // Look for corresponding i18n child
        // Try with language suffix first (e.g., elements0_es), then without (e.g., component)
        def i18nChildName = null
        def i18nChild = null
        
        def i18nChildNameWithSuffix = childName + language
        if (i18nNode.hasNode(i18nChildNameWithSuffix)) {
            i18nChildName = i18nChildNameWithSuffix
            i18nChild = i18nNode.getNode(i18nChildNameWithSuffix)
        } else if (i18nNode.hasNode(childName)) {
            // Nested nodes often don't have language suffix
            i18nChildName = childName
            i18nChild = i18nNode.getNode(childName)
        }
        
        if (i18nChild != null) {
            // i18n child exists, sync properties recursively
            def childChanges = syncNodeProperties(enChild, i18nChild, language, nodePath + "/" + i18nChildName, depth + 1)
            changes.addAll(childChanges)
        } else {
            // i18n child doesn't exist - skip (not creating new nodes, just updating existing ones)
            println "  >> Skipping missing i18n child node: ${nodePath}/${childName}${language}"
        }
    }
    
    return changes
}

/**
 * Finds and processes i18n node pairs (EN base + language variants)
 */
def processNode(Node node, String path = "") {
    stats.nodesScanned++
    
    if (stats.nodesScanned % 100 == 0) {
        println "Scanned ${stats.nodesScanned} nodes..."
    }
    
    def changes = []
    def nodeName = node.getName()
    
    // Check if this node has language-specific variants
    if (node.getDepth() > 0) {
        def parentNode = node.getParent()
        
        // Look for patterns like: elements (EN) + elements_es, elements_de, etc.
        LANGUAGE_SUFFIXES.each { suffix ->
            def i18nNodeName = nodeName + suffix
            if (parentNode.hasNode(i18nNodeName)) {
                def i18nNode = parentNode.getNode(i18nNodeName)
                stats.i18nNodePairsFound++
                
                // Calculate i18n path
                def parentPath = path.substring(0, Math.max(0, path.lastIndexOf('/')))
                if (parentPath.isEmpty() && !path.isEmpty()) {
                    parentPath = ""
                }
                def i18nPath = (parentPath.isEmpty() ? "" : parentPath + "/") + i18nNodeName
                
                println "\n${DRY_RUN ? '[DRY RUN] ' : ''}Processing i18n pair: ${path} <-> ${i18nPath}"
                
                def nodeChanges = syncNodeProperties(node, i18nNode, suffix, i18nPath, 0)
                changes.addAll(nodeChanges)
            }
        }
    }
    
    // Recursively process child nodes
    def children = NodeUtil.getNodes(node)
    children.each { child ->
        def childPath = path + "/" + child.getName()
        def childChanges = processNode(child, childPath)
        changes.addAll(childChanges)
    }
    
    return changes
}

/**
 * Prints a detailed report of changes
 */
def printReport(List changes) {
    println "\n" + "=" * 80
    println "${DRY_RUN ? 'DRY RUN REPORT' : 'CHANGES APPLIED'}"
    println "=" * 80
    
    if (changes.isEmpty()) {
        println "\nNo technical property differences found. All i18n nodes are in sync!"
    } else {
        println "\nTECHNICAL PROPERTIES TO BE REPLACED (${stats.nodesWithChanges} nodes)"
        println "-" * 80
        
        changes.each { change ->
            println "\nPath: ${change.path}"
            println "Language: ${change.language}"
            println "Properties to ${DRY_RUN ? 'replace' : 'replaced'} (${change.properties.size()}):"
            change.properties.each { prop ->
                def oldDisplay = prop.oldValue.length() > 50 ? prop.oldValue.substring(0, 47) + "..." : prop.oldValue
                def newDisplay = prop.newValue.length() > 50 ? prop.newValue.substring(0, 47) + "..." : prop.newValue
                println "  - ${prop.property}"
                println "    Old: ${oldDisplay}"
                println "    New: ${newDisplay}"
            }
        }
        println ""
    }
    
    println "=" * 80
    println "STATISTICS"
    println "=" * 80
    println "Nodes scanned: ${stats.nodesScanned}"
    println "i18n node pairs found: ${stats.i18nNodePairsFound}"
    println "Nodes with changes: ${stats.nodesWithChanges}"
    
    if (DRY_RUN) {
        def totalProps = changes.sum { it.properties?.size() ?: 0 } ?: 0
        println "Properties that would be replaced: ${totalProps}"
    } else {
        println "Properties replaced: ${stats.propertiesReplaced}"
    }
    
    println "=" * 80
}

// ============= MAIN EXECUTION =============

println """
Configuration:
  Workspace: ${WORKSPACE_NAME}
  Start Path: ${START_PATH}
  Mode: ${DRY_RUN ? 'DRY RUN (no changes will be applied)' : 'LIVE (changes will be applied)'}
  Languages: ${LANGUAGE_SUFFIXES.join(', ')}
  Technical Properties: ${TECHNICAL_PROPERTIES.join(', ')}
  Full Sync Nodes (all properties): ${FULL_SYNC_NODES.join(', ')}

"""

try {
    // Get the JCR session
    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME)
    
    if (!session.nodeExists(START_PATH)) {
        println "ERROR: Start path '${START_PATH}' does not exist in workspace '${WORKSPACE_NAME}'"
        return
    }
    
    Node rootNode = session.getNode(START_PATH)
    
    println "Starting scan from: ${START_PATH}"
    println "Please wait...\n"
    
    def startTime = System.currentTimeMillis()
    
    // Process all nodes starting from root
    def allChanges = processNode(rootNode, START_PATH.equals("/") ? "" : START_PATH)
    
    def endTime = System.currentTimeMillis()
    def duration = (endTime - startTime) / 1000.0
    
    // Print report
    printReport(allChanges)
    
    println "\nExecution time: ${String.format("%.2f", duration)} seconds"
    
    // Save changes if not in dry run mode
    if (!DRY_RUN && !allChanges.isEmpty()) {
        println "\nSaving changes to repository..."
        try {
            session.save()
            println "Changes saved successfully!"
            println "  - ${stats.nodesWithChanges} nodes updated"
            println "  - ${stats.propertiesReplaced} properties replaced"
        } catch (Exception e) {
            println "Error saving changes: ${e.message}"
            e.printStackTrace()
        }
    } else if (DRY_RUN && !allChanges.isEmpty()) {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply these changes."
    } else if (!DRY_RUN && allChanges.isEmpty()) {
        println "\nNo changes needed."
    }
    
} catch (Exception e) {
    println "\nERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."
