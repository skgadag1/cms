#!/usr/bin/env groovy
/**
 * Populate Dealer Model Pricing Concepts Script
 * 
 * This script creates concept nodes for the DealerModelPricing component
 * under /bobcat/common/labels/dealerinventory and /bobcat/common/labels/functional
 * with English and Spanish translations.
 * 
 * Concepts created:
 * dealerinventory: share, urlCopiedToClipboard
 * functional: month, months, for (generic labels for payment and financing displays)
 * 
 * Usage:
 *   - Set DRY_RUN to true to see what would be created without applying
 *   - Set DRY_RUN to false to apply changes
 */

import info.magnolia.context.MgnlContext
import javax.jcr.Node
import javax.jcr.Session

// ============= CONFIGURATION =============
WORKSPACE_NAME = "concepts"
DRY_RUN = false // Set to false to apply changes

// ============= CONCEPT DEFINITIONS =============
// Define the concepts to create with English and Spanish translations
def conceptGroups = [
    [
        basePath: "/bobcat/common/labels/dealerinventory",
        concepts: [
            [
                name: "share",
                english: "Share",
                spanish: "Compartir"
            ],
            [
                name: "urlCopiedToClipboard",
                english: "URL copied to clipboard",
                spanish: "URL copiada al portapapeles"
            ]
        ]
    ],
    [
        basePath: "/bobcat/common/labels/functional",
        concepts: [
            [
                name: "month",
                english: "month",
                spanish: "mes"
            ],
            [
                name: "months",
                english: "Months",
                spanish: "Meses"
            ],
            [
                name: "for",
                english: "for",
                spanish: "por"
            ]
        ]
    ]
]

// ============= STATISTICS =============
stats = [
    conceptsCreated: 0,
    conceptsSkipped: 0,
    conceptsUpdated: 0,
    errors: 0
]

// ============= HELPER FUNCTIONS =============

/**
 * Ensures that a path exists in the repository, creating folders as needed
 */
def ensurePathExists(Session session, String path) {
    if (session.nodeExists(path)) {
        return session.getNode(path)
    }
    
    def parts = path.split("/").findAll { it }
    def currentPath = ""
    def lastNode = session.getRootNode()
    
    parts.each { part ->
        currentPath += "/" + part
        if (!session.nodeExists(currentPath)) {
            println "  Creating folder: ${currentPath}"
            lastNode = lastNode.addNode(part, "mgnl:folder")
        } else {
            lastNode = session.getNode(currentPath)
        }
    }
    
    return lastNode
}

/**
 * Creates or updates a concept node with English and Spanish translations
 */
def createOrUpdateConcept(Session session, Node parentNode, String conceptName, String englishText, String spanishText) {
    try {
        if (parentNode.hasNode(conceptName)) {
            println "  Concept '${conceptName}' already exists at: ${parentNode.getPath()}/${conceptName}"
            
            if (!DRY_RUN) {
                // Update existing concept
                def conceptNode = parentNode.getNode(conceptName)
                conceptNode.setProperty("valuePlainText", englishText)
                conceptNode.setProperty("valuePlainText_es", spanishText)
                conceptNode.setProperty("value", "PlainText")
                stats.conceptsUpdated++
                println "    Updated with new values"
            } else {
                stats.conceptsSkipped++
                println "    [DRY RUN] Would update if needed"
            }
        } else {
            println "  Creating concept: ${conceptName}"
            println "    English: ${englishText}"
            println "    Spanish: ${spanishText}"
            
            if (!DRY_RUN) {
                // Create new concept node
                def conceptNode = parentNode.addNode(conceptName, "mgnl:content")
                
                // Set English value
                conceptNode.setProperty("valuePlainText", englishText)
                
                // Set Spanish value
                conceptNode.setProperty("valuePlainText_es", spanishText)
                
                // Set content type
                conceptNode.setProperty("value", "PlainText")
                
                stats.conceptsCreated++
            } else {
                println "    [DRY RUN] Would create node"
                stats.conceptsCreated++
            }
        }
    } catch (Exception e) {
        println "  ERROR creating/updating concept '${conceptName}': ${e.message}"
        stats.errors++
    }
}

/**
 * Prints a summary report
 */
def printReport() {
    println "\n" + "=" * 80
    println "${DRY_RUN ? 'DRY RUN REPORT' : 'EXECUTION REPORT'}"
    println "=" * 80
    println "Concepts created: ${stats.conceptsCreated}"
    println "Concepts updated: ${stats.conceptsUpdated}"
    println "Concepts skipped: ${stats.conceptsSkipped}"
    println "Errors: ${stats.errors}"
    println "=" * 80
}

// ============= MAIN EXECUTION =============

def totalConcepts = conceptGroups.sum { it.concepts.size() }

println """
Dealer Model Pricing Concepts Populator
${DRY_RUN ? '*** DRY RUN MODE - NO CHANGES WILL BE APPLIED ***' : '*** LIVE MODE - CHANGES WILL BE APPLIED ***'}

Configuration:
  Workspace: ${WORKSPACE_NAME}
  Concept groups: ${conceptGroups.size()}
  Total concepts to process: ${totalConcepts}

"""

try {
    // Get the JCR session
    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME)
    
    // Process each concept group
    conceptGroups.each { group ->
        println "\n" + "=" * 80
        println "Processing group: ${group.basePath}"
        println "=" * 80
        
        // Ensure base path exists
        println "Ensuring base path exists: ${group.basePath}"
        def parentNode = ensurePathExists(session, group.basePath)
        
        println "\nProcessing ${group.concepts.size()} concepts...\n"
        
        // Create each concept in this group
        group.concepts.each { concept ->
            createOrUpdateConcept(session, parentNode, concept.name, concept.english, concept.spanish)
        }
    }
    
    // Save changes if not in dry run mode
    if (!DRY_RUN) {
        println "\nSaving changes to repository..."
        try {
            session.save()
            println "Changes saved successfully!"
        } catch (Exception e) {
            println "ERROR saving changes: ${e.message}"
            e.printStackTrace()
            stats.errors++
        }
    }
    
    // Print report
    printReport()
    
    if (DRY_RUN) {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply these changes."
    }
    
} catch (Exception e) {
    println "\nCRITICAL ERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."
