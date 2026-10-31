#!/usr/bin/env groovy
/**
 * Migrate Evolution Content Carousel from List-Level to Field-Level i18n
 *
 * Covers bobcat:components/evolution-content-carousel's manually-authored
 * "cards" list (slidesSource.forms.manual.cards), which previously had
 * i18n:true set on the whole jcrMultiField with no leaf-level overrides.
 *
 * Modeled directly on MigrateFlexibleListingI18n.groovy (AV-2615 pilot).
 *
 * WHAT THIS SCRIPT MIGRATES:
 *
 * Cards array (cards_es, cards_de, etc.)
 *   - Moves properties from cards_$LANG/cards$N_$LANG to cards/cards$N
 *   - Migrates: title, description, caption, info, link/link/{customUrl,customLabel,pageLabel},
 *     chips/chips$N/title
 *   - Skips: image (media, not i18n), newIndicator, videoIndicator, htag,
 *     link/field, link/link/{pageUrl,target}, chips/chips$N (structural)
 *
 * OLD STRUCTURE:
 *   cards/cards0/title: "English"
 *   cards_es/cards0_es/title: "Spanish"          (whole item duplicated)
 *
 * NEW STRUCTURE:
 *   cards/cards0/title: "English", title_es: "Spanish"
 *
 * USAGE: same as MigrateFlexibleListingI18n.groovy — review config, DRY_RUN
 * first, review report, set DRY_RUN=false, verify, publish.
 *
 * NOTE: this script only covers the "manual" cards source
 * (slidesSource.forms.manual.cards). The "api" and "mgnlPages" sources pull
 * content dynamically or reference existing Magnolia pages and were not
 * affected by this bug.
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.Property
import javax.jcr.PropertyIterator
import javax.jcr.Session
import java.util.regex.Pattern

// ============= CONFIGURATION =============

WORKSPACE_NAME = "website"
DRY_RUN = true
START_PATH = "/bobcat/na"
TARGET_TEMPLATE = "bobcat:components/evolution-content-carousel"
SUPPORTED_LANGUAGES = ["es"]
ALLOW_OVERWRITE = false

// ============= PATH MAPPINGS =============

// NOTE: "cards" lives under the "slidesSource" switchableField's "manual"
// form (slidesSource.forms.manual.properties.cards) — like other
// switchableFields in this codebase (e.g. link/link/customUrl),
// switchableField materializes as its own JCR node, so cards is a CHILD of
// slidesSource, not a sibling of it at the component root. All paths below
// are relative to the component node, hence the "slidesSource/" prefix.
PATH_MAPPINGS = [
    'slidesSource/cards_$LANG/cards$N_$LANG/title': 'slidesSource/cards/cards$N/title_$LANG',
    'slidesSource/cards_$LANG/cards$N_$LANG/description': 'slidesSource/cards/cards$N/description_$LANG',
    'slidesSource/cards_$LANG/cards$N_$LANG/caption': 'slidesSource/cards/cards$N/caption_$LANG',
    'slidesSource/cards_$LANG/cards$N_$LANG/info': 'slidesSource/cards/cards$N/info_$LANG',

    'slidesSource/cards_$LANG/cards$N_$LANG/image': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/image/src': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/newIndicator': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/videoIndicator': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/htag': 'SKIP_ELEMENT',

    'slidesSource/cards_$LANG/cards$N_$LANG/link/field': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/link/link/customUrl': 'slidesSource/cards/cards$N/link/link/customUrl_$LANG',
    'slidesSource/cards_$LANG/cards$N_$LANG/link/link/customLabel': 'slidesSource/cards/cards$N/link/link/customLabel_$LANG',
    'slidesSource/cards_$LANG/cards$N_$LANG/link/link/pageUrl': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/link/link/pageLabel': 'slidesSource/cards/cards$N/link/link/pageLabel_$LANG',
    'slidesSource/cards_$LANG/cards$N_$LANG/link/link/target': 'SKIP_ELEMENT',

    'slidesSource/cards_$LANG/cards$N_$LANG/chips': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/chips/chips$N': 'SKIP_ELEMENT',
    'slidesSource/cards_$LANG/cards$N_$LANG/chips/chips$N/title': 'slidesSource/cards/cards$N/chips/chips$N/title_$LANG',
]

// ============= REMOVAL PATHS =============

REMOVAL_PATHS = [
    'slidesSource/cards_$LANG'
]

// ============= EXCLUDED PROPERTIES =============

EXCLUDED_PROPERTIES = [
    "jcr:primaryType", "jcr:uuid", "jcr:created", "jcr:createdBy", "jcr:mixinTypes",
    "mgnl:created", "mgnl:createdBy", "mgnl:lastModified", "mgnl:lastModifiedBy",
    "mgnl:lastActivated", "mgnl:lastActivatedBy", "mgnl:activationStatus",
    "mgnl:lastActivatedVersion", "mgnl:lastActivatedVersionCreated",
    "mgnl:removeIfEmpty", "versionTime"
]

// ============= STATISTICS =============

stats = [
    componentsScanned: 0, componentsMatched: 0, propertiesMigrated: 0,
    propertiesSkipped: 0, propertiesOverwritten: 0, propertiesUnchanged: 0,
    nodesRemoved: 0, unmappedPropertiesFound: 0, errors: [],
    unmappedProperties: [:], migratedProperties: [], overwrittenProperties: [],
    componentsWithErrors: [:],
]

// ============= HELPER FUNCTIONS =============
// (identical engine to MigrateFlexibleListingI18n.groovy)

def isExcludedProperty(String propertyName) { EXCLUDED_PROPERTIES.contains(propertyName) }

def getPropertyValueAsString(Property prop) {
    try {
        prop.isMultiple() ? prop.getValues().collect { it.getString() }.toString() : prop.getValue().getString()
    } catch (Exception e) {
        return "[binary or complex value]"
    }
}

def findComponentNode(Node node) {
    def current = node
    while (current != null && current.getDepth() > 0) {
        if (current.hasProperty("mgnl:template") && current.getProperty("mgnl:template").getString() == TARGET_TEMPLATE) {
            return current
        }
        current = current.getParent()
    }
    return null
}

def matchesPattern(String path, String pattern) {
    // Record each $LANG/$N placeholder in left-to-right order, so patterns
    // with more than one $N (e.g. a card index AND a chip index in the
    // same path) capture each occurrence separately instead of collapsing
    // them into a single value.
    def placeholders = []
    def placeholderMatcher = Pattern.compile('\\$LANG|\\$N').matcher(pattern)
    while (placeholderMatcher.find()) {
        placeholders.add(placeholderMatcher.group())
    }

    def regex = pattern
        .replace('/', '\\/')
        .replace('$LANG', "(" + SUPPORTED_LANGUAGES.join('|') + ")")
        .replace('$N', "(\\d+)")

    def matcher = Pattern.compile("^${regex}\$").matcher(path)
    if (!matcher.matches()) {
        return [matched: false, wildcards: [:]]
    }

    def result = [matched: true, wildcards: [lang: null, indices: []]]
    placeholders.eachWithIndex { placeholder, i ->
        def value = matcher.group(i + 1)
        if (placeholder == '$LANG') {
            result.wildcards.lang = value
        } else {
            result.wildcards.indices.add(value)
        }
    }
    return result
}

def expandTarget(String targetPattern, String lang, List indices) {
    // Replace each $N occurrence with the NEXT captured index, in order —
    // not the same value for every occurrence (Groovy's String.replace
    // would otherwise substitute ALL $N with a single value).
    def result = lang ? targetPattern.replace('$LANG', lang) : targetPattern
    def sb = new StringBuilder()
    def pos = 0
    def indexPos = 0
    while (true) {
        def nPos = result.indexOf('$N', pos)
        if (nPos < 0) {
            sb.append(result.substring(pos))
            break
        }
        sb.append(result.substring(pos, nPos))
        sb.append(indices[indexPos])
        indexPos++
        pos = nPos + 2
    }
    return sb.toString()
}

def getAllNodePaths(Node rootNode, String basePath = "") {
    def paths = []
    NodeUtil.getNodes(rootNode).each { child ->
        def childPath = basePath.isEmpty() ? child.getName() : "${basePath}/${child.getName()}"
        paths.add(childPath)
        paths.addAll(getAllNodePaths(child, childPath))
    }
    return paths
}

def getAllPropertiesUnderPath(Node componentNode, String relativePath) {
    def properties = []
    try {
        if (!componentNode.hasNode(relativePath)) return properties
        def node = componentNode.getNode(relativePath)
        PropertyIterator propIter = node.getProperties()
        while (propIter.hasNext()) {
            Property prop = propIter.nextProperty()
            String propName = prop.getName()
            if (!isExcludedProperty(propName)) {
                properties.add([nodePath: relativePath, propertyName: propName,
                                 fullPath: "${relativePath}/${propName}", value: getPropertyValueAsString(prop)])
            }
        }
        NodeUtil.getNodes(node).each { child ->
            properties.addAll(getAllPropertiesUnderPath(componentNode, "${relativePath}/${child.getName()}"))
        }
    } catch (Exception e) { /* node doesn't exist */ }
    return properties
}

def findSourcePaths(Node componentNode) {
    // Only match on the node's OWN name ending in "_$LANG" (not any ancestor
    // segment containing it) — otherwise every descendant of a matched node
    // also gets treated as its own separate starting point, and properties
    // under it get walked (and migrated/logged) once per matching ancestor.
    def candidates = [] as Set
    getAllNodePaths(componentNode).each { path ->
        def lastSegment = path.substring(path.lastIndexOf('/') + 1)
        SUPPORTED_LANGUAGES.each { lang -> if (lastSegment.endsWith("_${lang}")) candidates.add(path) }
    }
    // Keep only the outermost match per branch — walking it already reaches
    // every nested match beneath it, so including those separately is redundant.
    return candidates.findAll { candidate ->
        !candidates.any { other -> other != candidate && candidate.startsWith(other + '/') }
    }
}

def validateAllPropertiesMapped(Node componentNode, Map pathMappings) {
    def unmapped = []
    findSourcePaths(componentNode).each { sourcePath ->
        getAllPropertiesUnderPath(componentNode, sourcePath).each { propInfo ->
            def isMapped = pathMappings.any { pattern, target -> matchesPattern(propInfo.fullPath, pattern).matched }
            if (!isMapped) {
                unmapped.add([path: propInfo.fullPath, propertyName: propInfo.propertyName,
                              nodePath: propInfo.nodePath, value: propInfo.value])
            }
        }
    }
    return [valid: unmapped.isEmpty(), unmappedProperties: unmapped]
}

def createParentNodes(Node componentNode, String targetPath) {
    def pathParts = targetPath.split('/')
    def currentNode = componentNode
    for (int i = 0; i < pathParts.length - 1; i++) {
        def nodeName = pathParts[i]
        if (!currentNode.hasNode(nodeName)) {
            if (!DRY_RUN) currentNode = currentNode.addNode(nodeName, "mgnl:contentNode")
        } else {
            currentNode = currentNode.getNode(nodeName)
        }
    }
    return currentNode
}

def migrateProperty(Node componentNode, String sourcePath, String targetPath, Map wildcards, String componentPath = null) {
    def result = [success: false, action: null, sourceValue: null, targetValue: null, error: null]
    try {
        def sourcePathParts = sourcePath.split('/')
        def sourcePropName = sourcePathParts[-1]
        def sourceNodePath = sourcePathParts[0..-2].join('/')
        def targetPathParts = targetPath.split('/')
        def targetPropName = targetPathParts[-1]
        def targetNodePath = targetPathParts[0..-2].join('/')

        if (!componentNode.hasNode(sourceNodePath)) { result.error = "Source node not found: ${sourceNodePath}"; return result }
        def sourceNode = componentNode.getNode(sourceNodePath)
        if (!sourceNode.hasProperty(sourcePropName)) { result.error = "Source property not found: ${sourcePropName}"; return result }

        def sourceProp = sourceNode.getProperty(sourcePropName)
        result.sourceValue = getPropertyValueAsString(sourceProp)

        createParentNodes(componentNode, targetPath)
        def targetNode = targetNodePath.isEmpty() ? componentNode : componentNode.getNode(targetNodePath)

        if (targetNode.hasProperty(targetPropName)) {
            def existingProp = targetNode.getProperty(targetPropName)
            result.targetValue = getPropertyValueAsString(existingProp)
            if (result.sourceValue == result.targetValue) {
                result.action = "UNCHANGED"; result.success = true; stats.propertiesUnchanged++; return result
            }
            def fullSource = componentPath ? "${componentPath}/${sourcePath}" : sourcePath
            def fullTarget = componentPath ? "${componentPath}/${targetPath}" : targetPath
            if (!ALLOW_OVERWRITE) {
                result.error = "Target property already exists with different value (set ALLOW_OVERWRITE=true to overwrite)"
                result.action = "CONFLICT"
                stats.overwrittenProperties.add([source: fullSource, target: fullTarget,
                                                  oldValue: result.targetValue, newValue: result.sourceValue, wildcards: wildcards])
                return result
            }
            stats.overwrittenProperties.add([source: fullSource, target: fullTarget,
                                              oldValue: result.targetValue, newValue: result.sourceValue, wildcards: wildcards])
            result.action = "OVERWRITE"
        } else {
            result.action = "CREATE"
        }

        if (!DRY_RUN) {
            if (sourceProp.isMultiple()) targetNode.setProperty(targetPropName, sourceProp.getValues())
            else targetNode.setProperty(targetPropName, sourceProp.getValue())
        }

        result.success = true
        if (result.action == "OVERWRITE") stats.propertiesOverwritten++ else stats.propertiesMigrated++

        def fullSource = componentPath ? "${componentPath}/${sourcePath}" : sourcePath
        def fullTarget = componentPath ? "${componentPath}/${targetPath}" : targetPath
        stats.migratedProperties.add([source: fullSource, target: fullTarget, value: result.sourceValue,
                                       action: result.action, wildcards: wildcards])
    } catch (Exception e) {
        result.error = "Exception: ${e.message}"; result.success = false
    }
    return result
}

def processComponent(Node componentNode) {
    def componentPath = componentNode.getPath()
    stats.componentsMatched++

    println "\n${'=' * 80}"
    println "${DRY_RUN ? '[DRY RUN] ' : ''}Processing Evolution Content Carousel: ${componentPath}"
    println "${'=' * 80}"

    println "\nPhase 1: Validating property mappings..."
    def validation = validateAllPropertiesMapped(componentNode, PATH_MAPPINGS)
    if (!validation.valid) {
        println "\n✗ ERROR: Unmapped properties detected!"
        validation.unmappedProperties.each { prop ->
            println "  ${componentPath}/${prop.path}"
            println "    Value: ${prop.value.length() > 50 ? prop.value.substring(0, 47) + '...' : prop.value}"
        }
        stats.unmappedPropertiesFound += validation.unmappedProperties.size()
        stats.unmappedProperties[componentPath] = validation.unmappedProperties
        stats.componentsWithErrors[componentPath] = "Unmapped properties found"
        return false
    }
    println "✓ All properties are mapped"

    println "\nPhase 2: Building migration plan..."
    def migrationPlan = []
    PATH_MAPPINGS.each { sourcePattern, targetPattern ->
        if (targetPattern == "SKIP_ELEMENT") return
        findSourcePaths(componentNode).each { sourcePath ->
            getAllPropertiesUnderPath(componentNode, sourcePath).each { propInfo ->
                def match = matchesPattern(propInfo.fullPath, sourcePattern)
                if (match.matched) {
                    def expandedTarget = expandTarget(targetPattern, match.wildcards.lang, match.wildcards.indices)
                    migrationPlan.add([source: propInfo.fullPath, target: expandedTarget, wildcards: match.wildcards])
                }
            }
        }
    }

    def skippedCount = 0
    PATH_MAPPINGS.each { sourcePattern, targetPattern ->
        if (targetPattern == "SKIP_ELEMENT") {
            findSourcePaths(componentNode).each { sourcePath ->
                getAllPropertiesUnderPath(componentNode, sourcePath).each { propInfo ->
                    if (matchesPattern(propInfo.fullPath, sourcePattern).matched) { skippedCount++; stats.propertiesSkipped++ }
                }
            }
        }
    }

    println "Migration plan: ${migrationPlan.size()} properties to migrate"
    println "Skipped properties: ${skippedCount}"

    if (migrationPlan.isEmpty() && skippedCount == 0) {
        println "⚠ Warning: No properties to migrate or skip"
        return true
    }

    println "\nPhase 3: Executing migration..."
    def successCount = 0, errorCount = 0
    migrationPlan.each { plan ->
        def result = migrateProperty(componentNode, plan.source, plan.target, plan.wildcards, componentPath)
        if (result.success) {
            successCount++
            println "  ✓ ${result.action}: ${componentPath}/${plan.source} → ${componentPath}/${plan.target}"
        } else {
            errorCount++
            println "  ✗ ERROR: ${componentPath}/${plan.source} → ${componentPath}/${plan.target} :: ${result.error}"
            stats.errors.add("${componentPath}/${plan.source} - ${result.error}")
        }
    }
    println "\nMigration results: ${successCount} successful, ${errorCount} errors"

    if (errorCount == 0 && !REMOVAL_PATHS.isEmpty()) {
        println "\nPhase 4: Cleanup..."
        REMOVAL_PATHS.each { removalPattern ->
            SUPPORTED_LANGUAGES.each { lang ->
                def expandedPath = removalPattern.replace('$LANG', lang)
                try {
                    if (componentNode.hasNode(expandedPath)) {
                        if (!DRY_RUN) componentNode.getNode(expandedPath).remove()
                        stats.nodesRemoved++
                        println "  ✓ ${DRY_RUN ? '[DRY RUN] Would remove' : 'Removed'}: ${componentPath}/${expandedPath}"
                    }
                } catch (Exception e) {
                    println "  ⚠ Warning: Could not remove ${componentPath}/${expandedPath}: ${e.message}"
                }
            }
        }
    }
    return errorCount == 0
}

def scanForComponents(Node node) {
    stats.componentsScanned++
    if (stats.componentsScanned % 100 == 0) println "Scanned ${stats.componentsScanned} nodes..."
    if (node.hasProperty("mgnl:template") && node.getProperty("mgnl:template").getString() == TARGET_TEMPLATE) {
        processComponent(node)
    }
    NodeUtil.getNodes(node).each { child -> scanForComponents(child) }
}

def printReport() {
    println "\n\n${'=' * 80}"
    println "${DRY_RUN ? 'DRY RUN ' : ''}EVOLUTION CONTENT CAROUSEL MIGRATION REPORT"
    println "${'=' * 80}"
    println "\nCONFIGURATION:"
    println "  Workspace: ${WORKSPACE_NAME}"
    println "  Start path: ${START_PATH}"
    println "  Template: ${TARGET_TEMPLATE}"
    println "  Languages: ${SUPPORTED_LANGUAGES.join(', ')}"
    println "  Dry run: ${DRY_RUN}"
    println "  Allow overwrite: ${ALLOW_OVERWRITE}"
    println "\nSTATISTICS:"
    println "  Nodes scanned: ${stats.componentsScanned}"
    println "  Components matched: ${stats.componentsMatched}"
    println "  Properties migrated: ${stats.propertiesMigrated}"
    println "  Properties overwritten: ${stats.propertiesOverwritten}"
    println "  Properties unchanged: ${stats.propertiesUnchanged}"
    println "  Properties skipped: ${stats.propertiesSkipped}"
    println "  Nodes removed: ${stats.nodesRemoved}"
    println "  Unmapped properties: ${stats.unmappedPropertiesFound}"
    println "  Errors: ${stats.errors.size()}"

    if (!stats.unmappedProperties.isEmpty()) {
        println "\n${'=' * 80}\nUNMAPPED PROPERTIES (${stats.unmappedProperties.size()} components)\n${'=' * 80}"
        stats.unmappedProperties.each { componentPath, props ->
            println "\nComponent: ${componentPath}"
            props.each { prop -> println "  ${componentPath}/${prop.path}" }
        }
        println "\n⚠ Migration aborted due to unmapped properties. Update PATH_MAPPINGS if the structure differs."
    }
    if (!stats.errors.isEmpty()) {
        println "\n${'=' * 80}\nERRORS (${stats.errors.size()})\n${'=' * 80}"
        stats.errors.each { println "  ${it}" }
    }

    println "\n${'=' * 80}"
    if (stats.unmappedPropertiesFound > 0) println "STATUS: FAILED - Unmapped properties detected"
    else if (!stats.errors.isEmpty()) println "STATUS: COMPLETED WITH ERRORS"
    else if (stats.componentsMatched == 0) println "STATUS: NO COMPONENTS FOUND"
    else println "STATUS: SUCCESS"
    println "${'=' * 80}"
}

// ============= MAIN EXECUTION =============

println """
Evolution Content Carousel i18n Migration
List-Level -> Field-Level i18n

Configuration:
  Workspace: ${WORKSPACE_NAME}
  Start Path: ${START_PATH}
  Languages: ${SUPPORTED_LANGUAGES.join(', ')}
  Mode: ${DRY_RUN ? 'DRY RUN (no changes)' : 'LIVE (changes will be applied)'}
"""

try {
    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME)
    if (!session.nodeExists(START_PATH)) { println "ERROR: Start path '${START_PATH}' does not exist"; return }

    Node rootNode = session.getNode(START_PATH)
    def startTime = System.currentTimeMillis()
    scanForComponents(rootNode)
    def duration = (System.currentTimeMillis() - startTime) / 1000.0

    printReport()
    println "\nExecution time: ${String.format('%.2f', duration)} seconds"

    if (!DRY_RUN && stats.errors.isEmpty() && stats.unmappedPropertiesFound == 0) {
        println "\nSaving changes to repository..."
        try {
            session.save()
            println "✓ Changes saved successfully!"
        } catch (Exception e) {
            println "✗ Error saving changes: ${e.message}"
            e.printStackTrace()
        }
    } else if (DRY_RUN) {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply changes."
    }
} catch (Exception e) {
    println "\nFATAL ERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."
