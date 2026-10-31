#!/usr/bin/env groovy
/**
 * JCR i18n Node Synchronization Script
 * 
 * This script synchronizes properties from English (base) nodes to their i18n variants.
 * It finds properties that exist in EN but are missing in other language versions and copies them.
 * 
 * Usage:
 *   - Set WORKSPACE_NAME to the target workspace
 *   - Set DRY_RUN to true to see what would change without applying
 *   - Set DRY_RUN to false to apply changes
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
LANGUAGE_SUFFIXES = ["_es"] // Add more as needed
START_PATH = "/bobcat/na/" // Root path to start from, or specific path like "/bobcat/na"

// Properties to exclude from synchronization (metadata properties)
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
    "mgnl:lastActivatedVersion",
    "mgnl:lastActivatedVersionCreated",
    "versionTime"
]

// ============= STATISTICS =============
stats = [
    nodesScanned: 0,
    i18nNodePairsFound: 0,
    propertiesAdded: 0,
    nodesCreated: 0,
    nodesMissingProperties: [],
    nodesCreatedList: []
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
 * Checks if a property should be excluded from synchronization
 */
def isExcludedProperty(String propertyName) {
    return EXCLUDED_PROPERTIES.contains(propertyName)
}

/**
 * Gets all properties from a node (excluding metadata)
 */
def getRelevantProperties(Node node) {
    def properties = [:]
    PropertyIterator propIterator = node.getProperties()
    while (propIterator.hasNext()) {
        Property prop = propIterator.nextProperty()
        String propName = prop.getName()
        if (!isExcludedProperty(propName)) {
            properties[propName] = prop
        }
    }
    return properties
}

/**
 * Recursively copies an entire node structure from EN to i18n
 */
def copyNodeStructure(Node enNode, Node parentI18nNode, String i18nNodeName, String language, String nodePath, int depth = 0) {
    def changes = []
    
    println "  >> Creating missing node at depth ${depth}: ${nodePath}"
    
    // Create change info for reporting (both dry run and actual run)
    def changeInfo = [
        path: nodePath,
        language: language,
        action: "CREATE_NODE",
        nodeType: enNode.getPrimaryNodeType().getName(),
        wouldCopyProperties: [],
        wouldCopyChildren: []
    ]
    
    if (DRY_RUN) {
        // In dry run, just report what would be created
        
        // Report properties that would be copied
        def enProperties = getRelevantProperties(enNode)
        enProperties.each { propName, enProp ->
            if (extractLanguage(propName) == null) {
                try {
                    def value
                    if (enProp.isMultiple()) {
                        value = enProp.getValues().collect { it.getString() }
                    } else {
                        value = enProp.getValue().getString()
                    }
                    changeInfo.wouldCopyProperties.add([name: propName, value: value])
                } catch (Exception e) {
                    changeInfo.wouldCopyProperties.add([name: propName, value: "[complex value]"])
                }
            }
        }
        
        // Report child nodes that would be copied
        def enChildren = NodeUtil.getNodes(enNode)
        enChildren.each { enChild ->
            if (extractLanguage(enChild.getName()) == null) {
                changeInfo.wouldCopyChildren.add(enChild.getName())
            }
        }
        
        changes.add(changeInfo)
        stats.nodesMissingProperties.add(changeInfo)
        
        return changes
    }
    
    // Not dry run - actually create the node
    changes.add(changeInfo)
    stats.nodesMissingProperties.add(changeInfo)
    
    // Create the i18n node
    def i18nNode = parentI18nNode.addNode(i18nNodeName, enNode.getPrimaryNodeType().getName())
    println " Created node: ${i18nNode.getPath()}"
    stats.nodesCreated++
    stats.nodesCreatedList.add(i18nNode.getPath())
    
    // Copy all properties
    def enProperties = getRelevantProperties(enNode)
    enProperties.each { propName, enProp ->
        // Skip properties with language suffixes in EN (shouldn't happen)
        if (extractLanguage(propName) != null) {
            return
        }
        
        try {
            if (enProp.isMultiple()) {
                i18nNode.setProperty(propName, enProp.getValues())
            } else {
                i18nNode.setProperty(propName, enProp.getValue())
            }
            stats.propertiesAdded++
        } catch (Exception e) {
            println "Error copying property ${propName}: ${e.message}"
        }
    }
    
    // Recursively copy child nodes
    def enChildren = NodeUtil.getNodes(enNode)
    enChildren.each { enChild ->
        def childName = enChild.getName()
        def langSuffix = extractLanguage(childName)
        
        // Skip if this is already a language-specific node
        if (langSuffix != null) {
            return
        }
        
        // Use the same naming logic as when searching for existing nodes
        // First try with language suffix, then try without (for nested nodes)
        def i18nChildNameWithSuffix = childName + language
        def i18nChildName
        
        // Try to determine correct naming by checking if similar nodes exist
        // This ensures consistency with existing structure
        if (depth == 0) {
            // Direct children of i18n root should have suffix
            i18nChildName = i18nChildNameWithSuffix
        } else {
            // For nested children, first try with suffix, then without
            // In practice, nested children usually don't have suffix
            i18nChildName = childName
        }
        
        def childChanges = copyNodeStructure(enChild, i18nNode, i18nChildName, language, nodePath + "/" + i18nChildName, depth + 1)
        changes.addAll(childChanges)
    }
    
    return changes
}

/**
 * Recursively compares EN node with i18n variant and reports/adds missing properties
 */
def syncNodeProperties(Node enNode, Node i18nNode, String language, String nodePath, int depth = 0) {
    def changes = []
    
    // Get properties from both nodes
    def enProperties = getRelevantProperties(enNode)
    def i18nProperties = getRelevantProperties(i18nNode)
    
    // Find properties that exist in EN but missing in i18n variant
    def missingProperties = []
    enProperties.each { propName, enProp ->
        // Skip if this EN property already has a language suffix (shouldn't happen, but just in case)
        if (extractLanguage(propName) != null) {
            return
        }
        
        // Check if the property exists in i18n node either:
        def i18nHasProperty = i18nProperties.containsKey(propName)
        def i18nHasPropertyWithSuffix = i18nProperties.containsKey(propName + language)
        
        if (!i18nHasProperty && !i18nHasPropertyWithSuffix) {
            missingProperties.add(propName)
        }
    }
    
    // Add missing properties
    if (!missingProperties.isEmpty()) {
        def changeInfo = [
            path: nodePath,
            language: language,
            missingProperties: missingProperties,
            values: [:]
        ]
        
        missingProperties.each { propName ->
            def enProp = enProperties[propName]
            // Get value directly from Property object
            def value
            try {
                if (enProp.isMultiple()) {
                    value = enProp.getValues().collect { it.getString() }
                } else {
                    value = enProp.getValue().getString()
                }
            } catch (Exception e) {
                value = enProp.getString()
            }
            changeInfo.values[propName] = value
            
            if (!DRY_RUN) {
                // Copy the property to i18n node
                if (enProp.isMultiple()) {
                    i18nNode.setProperty(propName, enProp.getValues())
                } else {
                    i18nNode.setProperty(propName, enProp.getValue())
                }
                stats.propertiesAdded++
            }
        }
        
        changes.add(changeInfo)
        stats.nodesMissingProperties.add(changeInfo)
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

        // Skip if this is already a language-specific node
        if (langSuffix != null) {
            return
        }
        
        // Look for corresponding i18n child
        // First try with language suffix, then try without (for nested nodes)
        def i18nChildNameWithSuffix = childName + language
        def i18nChildName = null
        
        if (i18nNode.hasNode(i18nChildNameWithSuffix)) {
            i18nChildName = i18nChildNameWithSuffix
        } else if (i18nNode.hasNode(childName)) {
            // Nested nodes often don't have language suffix
            i18nChildName = childName
        }
        
        if (i18nChildName != null) {
            // i18n child exists, sync properties
            def i18nChild = i18nNode.getNode(i18nChildName)
            def childChanges = syncNodeProperties(enChild, i18nChild, language, nodePath + "/" + i18nChildName, depth + 1)
            changes.addAll(childChanges)
        } else {
            // i18n child doesn't exist, create it and copy entire structure
            // Use same naming logic: direct children (depth 0) get suffix, nested ones don't
            def newI18nChildName
            if (depth == 0) {
                // Direct children of i18n root pair should have language suffix
                newI18nChildName = i18nChildNameWithSuffix
            } else {
                // Nested children typically don't have suffix
                newI18nChildName = childName
            }
            println "Missing i18n child node: ${childName} -> will create ${newI18nChildName}"
            def childChanges = copyNodeStructure(enChild, i18nNode, newI18nChildName, language, nodePath + "/" + newI18nChildName, depth + 1)
            changes.addAll(childChanges)
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
    // Skip if this is the root node (which has no parent)
    if (node.getDepth() > 0) {
        def parentNode = node.getParent()
        
        // Look for patterns like: elements (EN) + elements_es, elements_de, etc.
        LANGUAGE_SUFFIXES.each { suffix ->
            def i18nNodeName = nodeName + suffix
            if (parentNode.hasNode(i18nNodeName)) {
                def i18nNode = parentNode.getNode(i18nNodeName)
                stats.i18nNodePairsFound++
                
                // Calculate parent path by removing the last segment from path
                def parentPath = path.substring(0, path.lastIndexOf('/'))
                if (parentPath.isEmpty()) {
                    parentPath = ""
                }
                def i18nPath = parentPath + "/" + i18nNodeName
                
                println "\n${DRY_RUN ? '[DRY RUN] ' : ''}Found i18n pair: ${path} <-> ${i18nPath}"
                
                def nodeChanges = syncNodeProperties(node, i18nNode, suffix, i18nPath)
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
    
    // Separate changes into node creations and property updates
    def nodeCreations = changes.findAll { it.action == "CREATE_NODE" }
    def propertyUpdates = changes.findAll { it.action != "CREATE_NODE" }
    
    if (changes.isEmpty()) {
        println "\nNo missing properties or nodes found. All i18n nodes are in sync!"
    } else {
        // Report node creations
        if (!nodeCreations.isEmpty()) {
            println "\nMISSING NODES TO BE CREATED (${nodeCreations.size()})"
            println "-" * 80
            
            nodeCreations.each { change ->
                println "\nPath: ${change.path}"
                println "Language: ${change.language}"
                println "Node Type: ${change.nodeType}"
                
                if (!change.wouldCopyProperties.isEmpty()) {
                    println "Properties to copy (${change.wouldCopyProperties.size()}):"
                    change.wouldCopyProperties.each { prop ->
                        def displayValue = prop.value.toString()
                        if (displayValue.length() > 60) {
                            displayValue = displayValue.substring(0, 57) + "..."
                        }
                        println "  - ${prop.name} = ${displayValue}"
                    }
                }
                
                if (!change.wouldCopyChildren.isEmpty()) {
                    println "Child nodes to copy (${change.wouldCopyChildren.size()}): ${change.wouldCopyChildren.join(', ')}"
                }
            }
            println ""
        }
        
        // Report property updates
        if (!propertyUpdates.isEmpty()) {
            println "\nMISSING PROPERTIES IN EXISTING NODES (${propertyUpdates.size()})"
            println "-" * 80
            
            propertyUpdates.each { change ->
                println "\nPath: ${change.path}"
                println "Language: ${change.language}"
                println "Missing properties (${change.missingProperties.size()}):"
                change.missingProperties.each { propName ->
                    def value = change.values[propName]
                    def displayValue = value.toString()
                    if (displayValue.length() > 60) {
                        displayValue = displayValue.substring(0, 57) + "..."
                    }
                    println "  - ${propName} = ${displayValue}"
                }
            }
            println ""
        }
    }
    
    println "=" * 80
    println "STATISTICS"
    println "=" * 80
    println "Nodes scanned: ${stats.nodesScanned}"
    println "i18n node pairs found: ${stats.i18nNodePairsFound}"
    
    def nodeCreationsChanges = changes.findAll { it.action == "CREATE_NODE" }
    def propertyUpdatesChanges = changes.findAll { it.action != "CREATE_NODE" }
    
    println "Nodes ${DRY_RUN ? 'that would be created' : 'created'}: ${DRY_RUN ? nodeCreationsChanges.size() : stats.nodesCreated}"
    println "Nodes with missing properties: ${propertyUpdatesChanges.size()}"
    
    if (DRY_RUN) {
        def totalPropsInUpdates = propertyUpdatesChanges.sum { it.missingProperties?.size() ?: 0 } ?: 0
        def totalPropsInCreations = nodeCreationsChanges.sum { it.wouldCopyProperties?.size() ?: 0 } ?: 0
        println "Properties that would be added: ${totalPropsInUpdates + totalPropsInCreations}"
    } else {
        println "Properties added: ${stats.propertiesAdded}"
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
            println "  - ${stats.nodesCreated} nodes created"
            println "  - ${stats.propertiesAdded} properties added"
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

