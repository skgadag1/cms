#!/usr/bin/env groovy
/**
 * Copy Properties to Locale Script
 * 
 * This script traverses the website workspace under a given path and for each page
 * copies specified properties to their localized versions (e.g., customNodeName -> customNodeName_es).
 * 
 * Traverses the page tree recursively without loading all pages into memory.
 * 
 * Usage:
 *   - Set START_PATH to the path to traverse
 *   - Set LOCALE_SUFFIX to the target locale (e.g., "_es", "_fr", "_de")
 *   - Set SOURCE_PROPERTIES to the list of properties to copy
 *   - Set DRY_RUN to true to see what would change without applying
 *   - Set DRY_RUN to false to apply changes
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.NodeIterator
import javax.jcr.Session

// ============= CONFIGURATION =============
WORKSPACE_NAME = "website"
START_PATH = "/bobcat/na" // Path to traverse - change as needed
LOCALE_SUFFIX = "_es" // Target locale suffix (e.g., "_es", "_fr", "_de")
SOURCE_PROPERTIES = ["customNodeName"] // List of properties to copy
DRY_RUN = true // Set to false to apply changes

// ============= STATISTICS =============
stats = [
    pagesScanned: 0,
    totalPropertiesCopied: 0,
    totalPropertiesOverwritten: 0,
    totalPropertiesCreated: 0,
    pagesModified: 0,
    pagesSkipped: 0,
    byProperty: [:] // Per-property statistics
]

// Initialize per-property statistics
SOURCE_PROPERTIES.each { prop ->
    stats.byProperty[prop] = [
        pagesWithProperty: 0,
        propertiesCopied: 0,
        propertiesOverwritten: 0,
        propertiesCreated: 0,
        pagesSkipped: 0
    ]
}

// ============= HELPER FUNCTIONS =============

/**
 * Truncates long values for display
 */
def truncateValue(String value) {
    if (value == null) return "[null]"
    if (value.length() > 60) {
        return value.substring(0, 57) + "..."
    }
    return value
}

/**
 * Processes a single property for a page node
 * Returns true if any changes were made, false otherwise
 */
def processProperty(Node pageNode, String sourceProperty) {
    def pagePath = pageNode.getPath()
    def targetProperty = sourceProperty + LOCALE_SUFFIX
    def propStats = stats.byProperty[sourceProperty]
    
    // Check if source property exists
    if (!pageNode.hasProperty(sourceProperty)) {
        propStats.pagesSkipped++
        return false
    }
    
    propStats.pagesWithProperty++
    
    def sourceValue = pageNode.getProperty(sourceProperty).getString()
    def action = null
    def previousValue = null
    
    // Check if target property already exists
    if (pageNode.hasProperty(targetProperty)) {
        def existingValue = pageNode.getProperty(targetProperty).getString()
        
        if (existingValue.equals(sourceValue)) {
            // Values are the same, no change needed
            propStats.pagesSkipped++
            return false
        }
        
        action = "OVERWRITE"
        previousValue = existingValue
        propStats.propertiesOverwritten++
        stats.totalPropertiesOverwritten++
    } else {
        action = "CREATE"
        propStats.propertiesCreated++
        stats.totalPropertiesCreated++
    }
    
    // Log the change
    println "${DRY_RUN ? '[DRY RUN] ' : ''}${action}: ${pagePath}"
    println "  ${sourceProperty} = '${truncateValue(sourceValue)}' -> ${targetProperty}"
    if (action == "OVERWRITE") {
        println "  Previous ${targetProperty} = '${truncateValue(previousValue)}'"
    }
    
    // Apply the change if not in dry run mode
    if (!DRY_RUN) {
        pageNode.setProperty(targetProperty, sourceValue)
    }
    
    propStats.propertiesCopied++
    stats.totalPropertiesCopied++
    
    return true
}

/**
 * Processes a single page node - copies all configured properties to their localized versions
 * Logs changes immediately as they happen
 */
def processPage(Node pageNode) {
    stats.pagesScanned++
    
    def pageModified = false
    
    // Process each configured property
    SOURCE_PROPERTIES.each { sourceProperty ->
        def modified = processProperty(pageNode, sourceProperty)
        pageModified = pageModified || modified
    }
    
    if (pageModified) {
        stats.pagesModified++
    } else {
        stats.pagesSkipped++
    }
}

/**
 * Recursively traverses the node tree, processing pages on the fly
 * Only traverses child pages (mgnl:page nodes)
 */
def traverseAndProcess(Node node) {
    // Check if current node is a page
    if (node.getPrimaryNodeType().getName().equals("mgnl:page")) {
        processPage(node)
        
        // Progress indicator every 100 pages
        if (stats.pagesScanned % 100 == 0) {
            println "--- Progress: ${stats.pagesScanned} pages scanned, ${stats.totalPropertiesCopied} changes ---"
        }
    }
    
    // Recursively process child nodes that are pages
    NodeIterator children = node.getNodes()
    while (children.hasNext()) {
        Node child = children.nextNode()
        // Only traverse into page nodes to find nested pages
        if (child.getPrimaryNodeType().getName().equals("mgnl:page")) {
            traverseAndProcess(child)
        }
    }
}

/**
 * Prints final summary
 */
def printSummary() {
    println "\n" + "=" * 80
    println "${DRY_RUN ? 'DRY RUN SUMMARY' : 'EXECUTION SUMMARY'}"
    println "=" * 80
    println "Total pages scanned: ${stats.pagesScanned}"
    println "Pages modified: ${stats.pagesModified}"
    println "Pages skipped (no changes needed): ${stats.pagesSkipped}"
    println ""
    println "Total properties ${DRY_RUN ? 'to be copied' : 'copied'}: ${stats.totalPropertiesCopied}"
    println "  - New properties ${DRY_RUN ? 'to be created' : 'created'}: ${stats.totalPropertiesCreated}"
    println "  - Existing properties ${DRY_RUN ? 'to be overwritten' : 'overwritten'}: ${stats.totalPropertiesOverwritten}"
    
    // Print per-property breakdown
    println ""
    println "Breakdown by property:"
    SOURCE_PROPERTIES.each { prop ->
        def propStats = stats.byProperty[prop]
        println "  ${prop}:"
        println "    - Pages with property: ${propStats.pagesWithProperty}"
        println "    - Properties copied: ${propStats.propertiesCopied}"
        println "    - Created: ${propStats.propertiesCreated}"
        println "    - Overwritten: ${propStats.propertiesOverwritten}"
    }
    println "=" * 80
}

// ============= MAIN EXECUTION =============

println """
================================================================================
Copy Properties to Locale Script
================================================================================

Configuration:
  Workspace: ${WORKSPACE_NAME}
  Start Path: ${START_PATH}
  Mode: ${DRY_RUN ? 'DRY RUN (no changes will be applied)' : 'LIVE (changes will be applied)'}
  
  Source Properties: ${SOURCE_PROPERTIES.join(', ')}
  Locale Suffix: ${LOCALE_SUFFIX}
  
  Property Mappings:
"""

SOURCE_PROPERTIES.each { prop ->
    println "    ${prop} -> ${prop}${LOCALE_SUFFIX}"
}

println "\nStarting traversal..."

try {
    // Get the JCR session
    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME)
    
    if (!session.nodeExists(START_PATH)) {
        println "ERROR: Start path '${START_PATH}' does not exist in workspace '${WORKSPACE_NAME}'"
        return
    }
    
    Node rootNode = session.getNode(START_PATH)
    
    def startTime = System.currentTimeMillis()
    
    // Traverse and process pages on the fly
    traverseAndProcess(rootNode)
    
    def endTime = System.currentTimeMillis()
    def duration = (endTime - startTime) / 1000.0
    
    // Print summary
    printSummary()
    
    println "\nExecution time: ${String.format("%.2f", duration)} seconds"
    
    // Save changes if not in dry run mode
    if (!DRY_RUN && stats.totalPropertiesCopied > 0) {
        println "\nSaving changes to repository..."
        try {
            session.save()
            println "Changes saved successfully!"
            println "  - ${stats.totalPropertiesCreated} new properties created"
            println "  - ${stats.totalPropertiesOverwritten} existing properties overwritten"
        } catch (Exception e) {
            println "Error saving changes: ${e.message}"
            e.printStackTrace()
        }
    } else if (DRY_RUN && stats.totalPropertiesCopied > 0) {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply these changes."
    } else if (stats.totalPropertiesCopied == 0) {
        println "\nNo changes needed."
    }
    
} catch (Exception e) {
    println "\nERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."

