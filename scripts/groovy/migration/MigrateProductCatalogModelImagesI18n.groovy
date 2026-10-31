#!/usr/bin/env groovy
/**
 * Clean up duplicated "images" asset lists for Product Catalog (Products + Attachments apps)
 *
 * Covers product-catalog/includes/model-images.yaml, which is shared by both:
 *   - the Products app (product-catalog/includes/model-details.yaml, dxp:product nodes,
 *     "products" workspace)
 *   - the Attachments app (product-catalog/includes/models/attachmentDetails.yaml,
 *     dxp:attachment nodes, "attachments" workspace)
 *
 * Unlike the other AV-2269 migration scripts, this is NOT a property-mapping
 * migration: the `images` list's only content is media (video/image/spinset
 * DAM references and a Cloudinary tag) — nothing under it was ever meant to
 * differ per locale (confirmed: after the YAML fix, model-images.yaml's
 * `asset` field is i18n:false with no translatable leaves anywhere below it).
 * So `images_$LANG` sibling lists created by the old group-level i18n:true
 * bug are pure duplicates, not divergent translations, and the fix is
 * cleanup rather than field-by-field migration.
 *
 * SAFETY: this script never deletes an `images_$LANG` node whose asset
 * references (DAM src paths / Cloudinary tags) differ from the base
 * `images` list — those are flagged for MANUAL REVIEW instead, in case a
 * content editor manually added different media per locale despite the
 * dialog not really supporting it. Only lists whose content is identical
 * to (or a subset of) the base `images` list are removed automatically.
 *
 * USAGE:
 * 1. Review configuration below (workspace/nodeType pairs, start paths)
 * 2. Run with DRY_RUN = true first, review the report
 * 3. Manually review anything reported as "DIFFERENT - NEEDS REVIEW"
 * 4. Set DRY_RUN = false to remove the identical/redundant images_$LANG nodes
 * 5. Verify in Magnolia admin interface, then publish
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.Property
import javax.jcr.PropertyIterator
import javax.jcr.Session

// ============= CONFIGURATION =============

DRY_RUN = true
SUPPORTED_LANGUAGES = ["es"]

// One entry per app/workspace sharing the model-images.yaml include
TARGETS = [
    [workspace: "products", nodeType: "dxp:product", startPath: "/"],
    [workspace: "attachments", nodeType: "dxp:attachment", startPath: "/"],
]

IMAGES_FIELD_NAME = "images"

// Properties compared when deciding whether an images_$LANG list is a pure
// duplicate of the base images list (DAM/media identity, not translatable text)
COMPARISON_PROPERTIES = ["src", "tag", "resource_type", "publicId"]

// ============= STATISTICS =============

stats = [
    nodesScanned: 0,
    productsWithLegacyImages: 0,
    listsRemovedAsIdentical: 0,
    listsFlaggedForReview: 0,
    listsEmpty: 0,
    errors: [],
    flaggedPaths: [],
]

// ============= HELPER FUNCTIONS =============

def collectAssetSignature(Node assetsListNode) {
    // Builds a comparable signature (sorted list of asset property maps) for
    // an "images" or "images_$LANG" jcrMultiField node, ignoring JCR/Magnolia
    // metadata and ordering, so two lists with the same media in a different
    // order still compare as identical.
    def signature = []
    NodeUtil.getNodes(assetsListNode).each { item ->
        def itemProps = [:]
        collectPropertiesRecursive(item, itemProps, "")
        signature.add(itemProps)
    }
    return signature.sort { it.toString() }
}

def collectPropertiesRecursive(Node node, Map out, String prefix) {
    PropertyIterator propIter = node.getProperties()
    while (propIter.hasNext()) {
        Property prop = propIter.nextProperty()
        String name = prop.getName()
        if (COMPARISON_PROPERTIES.contains(name)) {
            try {
                out["${prefix}${name}"] = prop.isMultiple() ?
                    prop.getValues().collect { it.getString() }.toString() : prop.getValue().getString()
            } catch (Exception e) {
                out["${prefix}${name}"] = "[unreadable]"
            }
        }
    }
    NodeUtil.getNodes(node).each { child ->
        collectPropertiesRecursive(child, out, "${prefix}${child.getName()}/")
    }
}

def processProductNode(Node node, String workspace) {
    def path = node.getPath()

    SUPPORTED_LANGUAGES.each { lang ->
        def legacyFieldName = "${IMAGES_FIELD_NAME}_${lang}"
        if (!node.hasNode(legacyFieldName)) return

        stats.productsWithLegacyImages++
        def legacyNode = node.getNode(legacyFieldName)
        def legacyChildCount = NodeUtil.getNodes(legacyNode).size()

        if (legacyChildCount == 0) {
            stats.listsEmpty++
            println "  ${'✓'.padRight(2)} EMPTY, removing: [${workspace}] ${path}/${legacyFieldName}"
            if (!DRY_RUN) legacyNode.remove()
            return
        }

        if (!node.hasNode(IMAGES_FIELD_NAME)) {
            // No base list to compare against — flag rather than guess
            stats.listsFlaggedForReview++
            stats.flaggedPaths.add("[${workspace}] ${path}/${legacyFieldName} (no base '${IMAGES_FIELD_NAME}' list to compare against)")
            println "  ⚠ NEEDS REVIEW: [${workspace}] ${path}/${legacyFieldName} — base '${IMAGES_FIELD_NAME}' list missing"
            return
        }

        def baseSignature = collectAssetSignature(node.getNode(IMAGES_FIELD_NAME))
        def legacySignature = collectAssetSignature(legacyNode)

        // Identical, or a subset of the base list (pure duplication) -> safe to remove
        def isRedundant = legacySignature.every { baseSignature.contains(it) }

        if (isRedundant) {
            stats.listsRemovedAsIdentical++
            println "  ✓ IDENTICAL/REDUNDANT, removing: [${workspace}] ${path}/${legacyFieldName} (${legacyChildCount} item(s))"
            if (!DRY_RUN) legacyNode.remove()
        } else {
            stats.listsFlaggedForReview++
            stats.flaggedPaths.add("[${workspace}] ${path}/${legacyFieldName} (content differs from base '${IMAGES_FIELD_NAME}')")
            println "  ⚠ DIFFERENT - NEEDS REVIEW: [${workspace}] ${path}/${legacyFieldName}"
            println "      base items: ${baseSignature.size()}, legacy items: ${legacySignature.size()}"
        }
    }
}

def scanNode(Node node, String workspace, String nodeType) {
    stats.nodesScanned++
    if (stats.nodesScanned % 500 == 0) println "Scanned ${stats.nodesScanned} nodes in ${workspace}..."

    if (node.getPrimaryNodeType().getName() == nodeType) {
        try {
            processProductNode(node, workspace)
        } catch (Exception e) {
            stats.errors.add("[${workspace}] ${node.getPath()}: ${e.message}")
        }
    }

    NodeUtil.getNodes(node).each { child -> scanNode(child, workspace, nodeType) }
}

def printReport() {
    println "\n\n${'=' * 80}"
    println "${DRY_RUN ? 'DRY RUN ' : ''}PRODUCT CATALOG MODEL IMAGES CLEANUP REPORT"
    println "${'=' * 80}"
    println "\nCONFIGURATION:"
    println "  Targets: ${TARGETS.collect { "${it.workspace} (${it.nodeType})" }.join(', ')}"
    println "  Languages: ${SUPPORTED_LANGUAGES.join(', ')}"
    println "  Dry run: ${DRY_RUN}"
    println "\nSTATISTICS:"
    println "  Nodes scanned: ${stats.nodesScanned}"
    println "  Nodes with legacy images_\$LANG: ${stats.productsWithLegacyImages}"
    println "  Empty lists removed: ${stats.listsEmpty}"
    println "  Identical/redundant lists removed: ${stats.listsRemovedAsIdentical}"
    println "  Lists flagged for manual review: ${stats.listsFlaggedForReview}"
    println "  Errors: ${stats.errors.size()}"

    if (!stats.flaggedPaths.isEmpty()) {
        println "\n${'=' * 80}\nFLAGGED FOR MANUAL REVIEW (${stats.flaggedPaths.size()})\n${'=' * 80}"
        stats.flaggedPaths.each { println "  ${it}" }
        println "\nThese were NOT removed automatically — review each one in AdminCentral"
        println "before deciding whether to keep, merge, or discard the divergent content."
    }

    if (!stats.errors.isEmpty()) {
        println "\n${'=' * 80}\nERRORS (${stats.errors.size()})\n${'=' * 80}"
        stats.errors.each { println "  ${it}" }
    }

    println "\n${'=' * 80}"
    if (!stats.errors.isEmpty()) println "STATUS: COMPLETED WITH ERRORS"
    else if (stats.productsWithLegacyImages == 0) println "STATUS: NO LEGACY IMAGES_\$LANG NODES FOUND"
    else println "STATUS: SUCCESS"
    println "${'=' * 80}"
}

// ============= MAIN EXECUTION =============

println """
Product Catalog Model Images Cleanup (Products + Attachments apps)
Removing duplicated images_\$LANG lists (non-i18n media, no field-level migration needed)

Mode: ${DRY_RUN ? 'DRY RUN (no changes)' : 'LIVE (changes will be applied)'}
"""

try {
    TARGETS.each { target ->
        println "\n--- Scanning workspace '${target.workspace}' (${target.nodeType}) from '${target.startPath}' ---"
        Session session = MgnlContext.getJCRSession(target.workspace)
        if (!session.nodeExists(target.startPath)) {
            println "ERROR: Start path '${target.startPath}' does not exist in workspace '${target.workspace}'"
            return
        }
        scanNode(session.getNode(target.startPath), target.workspace, target.nodeType)
    }

    printReport()

    if (!DRY_RUN) {
        TARGETS.each { target ->
            try {
                MgnlContext.getJCRSession(target.workspace).save()
                println "✓ Changes saved for workspace '${target.workspace}'"
            } catch (Exception e) {
                println "✗ Error saving changes for workspace '${target.workspace}': ${e.message}"
                e.printStackTrace()
            }
        }
    } else {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply changes."
    }
} catch (Exception e) {
    println "\nFATAL ERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."
