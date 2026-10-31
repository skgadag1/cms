#!/usr/bin/env groovy
/**
 * Migrate Herospot / Captioned Image / Categorized Captioned Image
 * from List-Level to Field-Level i18n (INC0210867)
 *
 * AV-3053 removed list-level i18n from these lists but left the inner text
 * fields non-translatable; AV-3357 restored list-level i18n, which brought
 * back pre-AV-3053 per-locale list copies (e.g. mower offer cards served on
 * the Spanish compact-loader page). The dialogs are now field-level
 * (list i18n: false, text fields i18n: true); this script moves the content.
 *
 * OLD STRUCTURE:
 *   cards/cards0/title: "English"
 *   cards_es/cards0_es/title: "Spanish"          (whole item duplicated)
 *
 * NEW STRUCTURE:
 *   cards/cards0/title: "English", title_es: "Spanish"
 *
 * STALENESS GUARD:
 *   A locale item is only migrated when it still describes the same base item:
 *   the base item must exist and its asset (image / video / YouTube) must match.
 *   Stale or orphan locale items are reported and NOT migrated - the locale
 *   falls back to English until the page is translated again.
 *   With TREAT_NEWER_BASE_AS_STALE = true, items whose base list was modified
 *   after the locale list are treated as stale too (otherwise only reported).
 *
 * CLEANUP:
 *   Locale lists (slides_$LANG, cards_$LANG) are removed once their component
 *   has no unmapped properties or errors, stale items included.
 *
 * ALSO REPORTED:
 *   HTML markup stored in plain-text fields (e.g. "<h1><span ...>" in a
 *   herospot header), which the frontend renders as literal text.
 *
 * USAGE: review config, DRY_RUN first, review report, set DRY_RUN=false,
 * verify, publish affected pages, then re-run batch translation on pages
 * listed under STALE ITEMS.
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.Property
import javax.jcr.PropertyIterator
import javax.jcr.Session
import java.text.SimpleDateFormat
import java.util.regex.Pattern

// ============= CONFIGURATION =============

WORKSPACE_NAME = "website"
DRY_RUN = true
START_PATH = "/bobcat/na"
SUPPORTED_LANGUAGES = ["es"]
ALLOW_OVERWRITE = false
TREAT_NEWER_BASE_AS_STALE = false

TARGETS = [
    "bobcat:components/herospot"                   : [container: "", list: "slides",
                                                      textFields: ["header", "text"], plainTextFields: ["header", "text"]],
    "bobcat:components/captioned-image"            : [container: "", list: "cards",
                                                      textFields: ["title", "paragraph"], plainTextFields: ["title"]],
    "bobcat:components/categorized-captioned-image": [container: "tabs/*", list: "cards",
                                                      textFields: ["title", "paragraph"], plainTextFields: ["title"]],
]

// ============= ITEM RULES =============

LINK_LABELS = "customLabel|pageLabel|localAnchorLabel|customUrl"

def migrateRules(List textFields) {
    [
        ["^(${textFields.join('|')})(?:_\$L)?\$", '$1_$L'],
        ['^asset/img/(imageAlt|imageTitle)(?:_$L)?$', 'asset/img/$1_$L'],
        ["^link(?:_\$L)?/link/(${LINK_LABELS})(?:_\$L)?\$", 'link/link/$1_$L'],
    ]
}

SKIP_RULES = [
    '^htag$',
    '^asset(?:_$L)?(/.*)?$',
    '^link(?:_$L)?/field$',
    '^link(?:_$L)?/link/(pageUrl|anchorID|target)(?:_$L)?$',
]

ASSET_SIGNATURE_EXCLUDED = ~/^(imageAlt|imageTitle)(_.+)?$/
HTML_PATTERN = ~/<\/?[a-zA-Z][^>]*>/

EXCLUDED_PROPERTIES = [
    "jcr:primaryType", "jcr:uuid", "jcr:created", "jcr:createdBy", "jcr:mixinTypes",
    "mgnl:created", "mgnl:createdBy", "mgnl:lastModified", "mgnl:lastModifiedBy",
    "mgnl:lastActivated", "mgnl:lastActivatedBy", "mgnl:activationStatus",
    "mgnl:lastActivatedVersion", "mgnl:lastActivatedVersionCreated",
    "mgnl:removeIfEmpty", "versionTime"
] as Set

// ============= STATISTICS =============

stats = [
    nodesScanned: 0, componentsMatched: 0, localeListsFound: 0,
    propertiesMigrated: 0, propertiesUnchanged: 0, propertiesSkipped: 0,
    listsRemoved: 0, staleItems: [], outdatedLists: [], conflicts: [], missingBaseNodes: [],
    unmapped: [:], htmlInText: [], errors: [],
]

// ============= HELPER FUNCTIONS =============

def compileRule(String regex, String lang) { Pattern.compile(regex.replace('$L', Pattern.quote(lang))) }

def valueAsString(Property prop) {
    try {
        prop.isMultiple() ? prop.getValues().collect { it.getString() }.toString() : prop.getString()
    } catch (Exception e) {
        "[binary or complex value]"
    }
}

def lastModified(Node node) {
    node.hasProperty("mgnl:lastModified") ? node.getProperty("mgnl:lastModified").getDate().getTime() : null
}

def collectProperties(Node node, String basePath = "") {
    def result = [:]
    PropertyIterator props = node.getProperties()
    while (props.hasNext()) {
        Property prop = props.nextProperty()
        if (!EXCLUDED_PROPERTIES.contains(prop.getName())) result[basePath + prop.getName()] = prop
    }
    NodeUtil.getNodes(node).each { child -> result.putAll(collectProperties(child, "${basePath}${child.getName()}/")) }
    return result
}

def assetSignature(Node item) {
    def asset = NodeUtil.getNodes(item).find { it.getName() ==~ /^asset(_.+)?$/ }
    if (!asset) return [:]
    collectProperties(asset)
        .findAll { path, prop -> !(path.tokenize('/')[-1] ==~ ASSET_SIGNATURE_EXCLUDED) }
        .collectEntries { path, prop -> [path, valueAsString(prop)] }
}

def resolveContainers(Node componentNode, String containerPath) {
    if (containerPath.isEmpty()) return [componentNode]
    if (containerPath.endsWith("/*")) {
        def parentPath = containerPath[0..-3]
        return componentNode.hasNode(parentPath) ? NodeUtil.getNodes(componentNode.getNode(parentPath)).toList() : []
    }
    return componentNode.hasNode(containerPath) ? [componentNode.getNode(containerPath)] : []
}

def baseItemName(String localeItemName, String lang) {
    localeItemName.endsWith("_${lang}") ? localeItemName[0..-(lang.length() + 2)] : localeItemName
}

def checkHtml(Node item, List plainTextFields) {
    PropertyIterator props = item.getProperties()
    while (props.hasNext()) {
        Property prop = props.nextProperty()
        def field = prop.getName().replaceFirst(/_[a-z]{2}(_[A-Z]{2})?$/, '')
        if (plainTextFields.contains(field) && !prop.isMultiple() && prop.getString() =~ HTML_PATTERN) {
            stats.htmlInText.add([path: prop.getPath(), value: prop.getString()])
        }
    }
}

def planLocaleList(Node baseList, Node localeList, String lang, Map target) {
    def rules = migrateRules(target.textFields).collect { regex, replacement -> [compileRule(regex, lang), replacement.replace('$L', lang)] }
    def skips = SKIP_RULES.collect { compileRule(it, lang) }
    def writes = [], unmapped = []

    def outdated = baseList && lastModified(baseList) && lastModified(localeList) &&
        lastModified(baseList).after(lastModified(localeList))
    if (outdated) stats.outdatedLists.add([path: localeList.getPath(), base: lastModified(baseList), locale: lastModified(localeList)])

    NodeUtil.getNodes(localeList).each { localeItem ->
        def baseItemPath = baseItemName(localeItem.getName(), lang)
        def baseItem = baseList?.hasNode(baseItemPath) ? baseList.getNode(baseItemPath) : null

        def staleReason = !baseItem ? "no matching base item" :
            assetSignature(baseItem) != assetSignature(localeItem) ? "asset differs from base item" :
            (outdated && TREAT_NEWER_BASE_AS_STALE) ? "base list modified after locale list" : null
        if (staleReason) {
            stats.staleItems.add([path: localeItem.getPath(), reason: staleReason])
            return
        }

        def targets = [:]
        collectProperties(localeItem).sort { a, b -> (a.key.endsWith("_${lang}") <=> b.key.endsWith("_${lang}")) }.each { path, prop ->
            def rule = rules.find { it[0].matcher(path).matches() }
            if (rule) {
                targets[rule[0].matcher(path).replaceFirst(rule[1])] = prop
            } else if (skips.any { it.matcher(path).matches() }) {
                stats.propertiesSkipped++
            } else {
                unmapped.add("${localeItem.getPath()}/${path}")
            }
        }
        targets.each { targetPath, prop ->
            def nodePath = targetPath.contains('/') ? targetPath[0..<targetPath.lastIndexOf('/')] : ""
            if (nodePath.isEmpty() || baseItem.hasNode(nodePath)) {
                writes.add([baseItem: baseItem, targetPath: targetPath, source: prop])
            } else {
                stats.missingBaseNodes.add("${prop.getPath()} (no ${baseItem.getPath()}/${nodePath})")
            }
        }
    }
    return [writes: writes, unmapped: unmapped]
}

def targetValue(Map write) {
    def nodePath = write.targetPath.contains('/') ? write.targetPath[0..<write.targetPath.lastIndexOf('/')] : ""
    def node = nodePath.isEmpty() ? write.baseItem : (write.baseItem.hasNode(nodePath) ? write.baseItem.getNode(nodePath) : null)
    def propName = write.targetPath.tokenize('/')[-1]
    node?.hasProperty(propName) ? valueAsString(node.getProperty(propName)) : null
}

def applyWrite(Map write) {
    def fullTarget = "${write.baseItem.getPath()}/${write.targetPath}"
    if (targetValue(write) == valueAsString(write.source)) { stats.propertiesUnchanged++; return }
    if (!DRY_RUN) {
        def nodePath = write.targetPath.contains('/') ? write.targetPath[0..<write.targetPath.lastIndexOf('/')] : ""
        def targetNode = nodePath.isEmpty() ? write.baseItem : write.baseItem.getNode(nodePath)
        def propName = write.targetPath.tokenize('/')[-1]
        if (write.source.isMultiple()) targetNode.setProperty(propName, write.source.getValues())
        else targetNode.setProperty(propName, write.source.getValue())
    }
    stats.propertiesMigrated++
    println "  ✓ ${write.source.getPath()} → ${fullTarget}"
}

def processComponent(Node componentNode, Map target) {
    stats.componentsMatched++
    def componentPath = componentNode.getPath()
    def lists = []

    resolveContainers(componentNode, target.container).each { container ->
        def baseList = container.hasNode(target.list) ? container.getNode(target.list) : null
        if (baseList) NodeUtil.getNodes(baseList).each { checkHtml(it, target.plainTextFields) }
        SUPPORTED_LANGUAGES.each { lang ->
            def localeName = "${target.list}_${lang}"
            if (!container.hasNode(localeName)) return
            def localeList = container.getNode(localeName)
            NodeUtil.getNodes(localeList).each { checkHtml(it, target.plainTextFields) }
            stats.localeListsFound++
            lists.add([localeList: localeList, plan: planLocaleList(baseList, localeList, lang, target)])
        }
    }
    if (lists.isEmpty()) return

    println "\n${DRY_RUN ? '[DRY RUN] ' : ''}Processing: ${componentPath}"
    def unmapped = lists.collectMany { it.plan.unmapped }
    if (!unmapped.isEmpty()) {
        stats.unmapped[componentPath] = unmapped
        println "  ✗ Unmapped properties, component skipped:"
        unmapped.each { println "    ${it}" }
        return
    }

    def writes = lists.collectMany { it.plan.writes }
    if (!ALLOW_OVERWRITE) {
        def conflicts = writes.findAll { write ->
            def existing = targetValue(write)
            existing != null && existing != valueAsString(write.source)
        }
        if (!conflicts.isEmpty()) {
            conflicts.each { write ->
                stats.conflicts.add([target: "${write.baseItem.getPath()}/${write.targetPath}",
                                     existing: targetValue(write), incoming: valueAsString(write.source)])
            }
            println "  ✗ Conflicts with existing field-level values, component skipped"
            return
        }
    }
    writes.each { applyWrite(it) }

    lists.each { list ->
        if (!DRY_RUN) list.localeList.remove()
        stats.listsRemoved++
        println "  ✓ ${DRY_RUN ? '[DRY RUN] Would remove' : 'Removed'}: ${list.localeList.getPath()}"
    }
}

def scanForComponents(Node node) {
    stats.nodesScanned++
    if (stats.nodesScanned % 1000 == 0) println "Scanned ${stats.nodesScanned} nodes..."
    if (node.hasProperty("mgnl:template")) {
        def target = TARGETS[node.getProperty("mgnl:template").getString()]
        if (target) {
            try {
                processComponent(node, target)
            } catch (Exception e) {
                stats.errors.add("${node.getPath()} - ${e.message}")
                println "  ✗ ERROR: ${node.getPath()} - ${e.message}"
            }
        }
    }
    NodeUtil.getNodes(node).each { child -> scanForComponents(child) }
}

def formatDate(Date date) {
    date ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date) : "?"
}

def truncate(String value) { value.length() > 80 ? value.substring(0, 77) + '...' : value }

def printSection(String title, List entries, Closure printer) {
    if (entries.isEmpty()) return
    println "\n${'=' * 80}\n${title} (${entries.size()})\n${'=' * 80}"
    entries.each(printer)
}

def printReport() {
    println "\n\n${'=' * 80}"
    println "${DRY_RUN ? 'DRY RUN ' : ''}HEROSPOT / CAPTIONED IMAGE I18N MIGRATION REPORT"
    println "${'=' * 80}"
    println "\nCONFIGURATION:"
    println "  Workspace: ${WORKSPACE_NAME}"
    println "  Start path: ${START_PATH}"
    println "  Templates: ${TARGETS.keySet().join(', ')}"
    println "  Languages: ${SUPPORTED_LANGUAGES.join(', ')}"
    println "  Allow overwrite: ${ALLOW_OVERWRITE}"
    println "  Treat newer base as stale: ${TREAT_NEWER_BASE_AS_STALE}"
    println "  Dry run: ${DRY_RUN}"
    println "\nSTATISTICS:"
    println "  Nodes scanned: ${stats.nodesScanned}"
    println "  Components matched: ${stats.componentsMatched}"
    println "  Locale lists found: ${stats.localeListsFound}"
    println "  Properties migrated: ${stats.propertiesMigrated}"
    println "  Properties unchanged: ${stats.propertiesUnchanged}"
    println "  Properties skipped (non-i18n): ${stats.propertiesSkipped}"
    println "  Locale lists removed: ${stats.listsRemoved}"
    println "  Stale items (not migrated): ${stats.staleItems.size()}"
    println "  Locale lists older than base: ${stats.outdatedLists.size()}"
    println "  Not migrated (base node missing): ${stats.missingBaseNodes.size()}"
    println "  Conflicts: ${stats.conflicts.size()}"
    println "  Components with unmapped properties: ${stats.unmapped.size()}"
    println "  HTML in plain-text fields: ${stats.htmlInText.size()}"
    println "  Errors: ${stats.errors.size()}"

    printSection("STALE ITEMS - NOT MIGRATED, RETRANSLATE THESE PAGES", stats.staleItems.sort { it.path }) { e ->
        println "  ${e.path}\n    ${e.reason}"
    }
    printSection("LOCALE LISTS OLDER THAN BASE LIST - REVIEW TRANSLATION", stats.outdatedLists.sort { it.path }) { e ->
        println "  ${e.path}\n    base modified ${formatDate(e.base)}, locale modified ${formatDate(e.locale)}"
    }
    printSection("NOT MIGRATED - BASE ITEM HAS NO MATCHING NODE", stats.missingBaseNodes.sort()) { println "  ${it}" }
    printSection("CONFLICTS - FIELD-LEVEL VALUE ALREADY EXISTS (set ALLOW_OVERWRITE=true to replace)", stats.conflicts) { e ->
        println "  ${e.target}\n    existing: ${truncate(e.existing)}\n    incoming: ${truncate(e.incoming)}"
    }
    printSection("HTML IN PLAIN-TEXT FIELDS - FIX MANUALLY", stats.htmlInText.sort { it.path }) { e ->
        println "  ${e.path}\n    ${truncate(e.value)}"
    }
    printSection("UNMAPPED PROPERTIES - COMPONENTS SKIPPED", stats.unmapped.collect { k, v -> [component: k, props: v] }) { e ->
        println "  ${e.component}"
        e.props.each { println "    ${it}" }
    }
    printSection("ERRORS", stats.errors) { println "  ${it}" }

    println "\n${'=' * 80}"
    if (!stats.errors.isEmpty() || !stats.unmapped.isEmpty() || !stats.conflicts.isEmpty()) println "STATUS: COMPLETED WITH ISSUES"
    else if (stats.componentsMatched == 0) println "STATUS: NO COMPONENTS FOUND"
    else println "STATUS: SUCCESS"
    println "${'=' * 80}"
}

// ============= MAIN EXECUTION =============

println """
Herospot / Captioned Image / Categorized Captioned Image i18n Migration (INC0210867)
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

    def startTime = System.currentTimeMillis()
    scanForComponents(session.getNode(START_PATH))
    def duration = (System.currentTimeMillis() - startTime) / 1000.0

    printReport()
    println "\nExecution time: ${String.format('%.2f', duration)} seconds"

    if (!DRY_RUN && stats.errors.isEmpty()) {
        println "\nSaving changes to repository..."
        try {
            session.save()
            println "✓ Changes saved successfully!"
        } catch (Exception e) {
            println "✗ Error saving changes: ${e.message}"
            e.printStackTrace()
        }
    } else if (!DRY_RUN) {
        println "\nErrors occurred; no changes were saved."
    } else {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply changes."
    }
} catch (Exception e) {
    println "\nFATAL ERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."
