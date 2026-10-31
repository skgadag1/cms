#!/usr/bin/env groovy

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.Property
import javax.jcr.PropertyIterator
import javax.jcr.Session
import java.text.SimpleDateFormat
import java.util.regex.Pattern

WORKSPACE_NAME = "website"
DRY_RUN = true
START_PATH = "/bobcat"
CUTOFF_DATE = "2026-09-07"
SUPPORTED_LANGUAGES = ["es"]
SEED_MISSING_LOCALES = false

TARGETS = [
    "bobcat:components/captioned-image"            : [container: "", list: "cards"],
    "bobcat:components/herospot"                   : [container: "", list: "slides"],
    "bobcat:components/categorized-captioned-image": [container: "tabs/*", list: "cards"],
]

LOCALE_SUFFIX = "([a-z]{2,3}(?:_[A-Z]{2})?)"

cutoff = new SimpleDateFormat("yyyy-MM-dd").parse(CUTOFF_DATE)

stats = [
    nodesScanned: 0, componentsMatched: 0, listsInspected: 0,
    unaffected: 0, localeOnly: 0, listsSeeded: 0,
    reviewNeeded: [], baseOnlyAfterCutoff: [], errors: [],
]

def lastModified(Node node) {
    node.hasProperty("mgnl:lastModified") ? node.getProperty("mgnl:lastModified").getDate().getTime() : null
}

def lastModifiedBy(Node node) {
    node.hasProperty("mgnl:lastModifiedBy") ? node.getProperty("mgnl:lastModifiedBy").getString() : "?"
}

def latest(List dates) {
    dates.findAll { it != null }.max()
}

def resolveContainers(Node componentNode, String containerPath) {
    if (containerPath.isEmpty()) return [componentNode]
    if (containerPath.endsWith("/*")) {
        def parentPath = containerPath[0..-3]
        if (!componentNode.hasNode(parentPath)) return []
        return NodeUtil.getNodes(componentNode.getNode(parentPath)).toList()
    }
    return componentNode.hasNode(containerPath) ? [componentNode.getNode(containerPath)] : []
}

def findLocaleLists(Node container, String listName) {
    def pattern = Pattern.compile("^${Pattern.quote(listName)}_${LOCALE_SUFFIX}\$")
    def locales = [:]
    NodeUtil.getNodes(container).each { child ->
        def matcher = pattern.matcher(child.getName())
        if (matcher.matches()) locales[matcher.group(1)] = child
    }
    return locales
}

def copyNodeShallow(Node source, Node targetParent, String targetName) {
    Node target = targetParent.addNode(targetName, source.getPrimaryNodeType().getName())
    source.getMixinNodeTypes().each { mixin ->
        if (target.canAddMixin(mixin.getName())) target.addMixin(mixin.getName())
    }
    PropertyIterator props = source.getProperties()
    while (props.hasNext()) {
        Property prop = props.nextProperty()
        if (prop.getDefinition().isProtected()) continue
        if (prop.isMultiple()) target.setProperty(prop.getName(), prop.getValues())
        else target.setProperty(prop.getName(), prop.getValue())
    }
    return target
}

def copyNodeDeep(Node source, Node targetParent, String targetName) {
    Node target = copyNodeShallow(source, targetParent, targetName)
    NodeUtil.getNodes(source).each { child -> copyNodeDeep(child, target, child.getName()) }
    return target
}

def seedLocaleList(Node container, Node baseList, String lang) {
    if (DRY_RUN) return
    Node localeList = copyNodeShallow(baseList, container, "${baseList.getName()}_${lang}")
    NodeUtil.getNodes(baseList).each { item -> copyNodeDeep(item, localeList, "${item.getName()}_${lang}") }
}

def inspectList(Node componentNode, Node container, String listName) {
    stats.listsInspected++
    def listPath = "${container.getPath()}/${listName}"
    def baseList = container.hasNode(listName) ? container.getNode(listName) : null
    def localeLists = findLocaleLists(container, listName)

    if (baseList == null) {
        if (!localeLists.isEmpty()) stats.localeOnly++
        return
    }

    def modified = latest([lastModified(componentNode), lastModified(baseList)])
    def afterCutoff = modified != null && modified.after(cutoff)
    def entry = [path: listPath, modified: modified, modifiedBy: lastModifiedBy(componentNode),
                 locales: localeLists.keySet().sort(), items: NodeUtil.getNodes(baseList).toList().size()]

    if (!afterCutoff) {
        stats.unaffected++
        return
    }

    if (!localeLists.isEmpty()) {
        stats.reviewNeeded.add(entry)
        return
    }

    stats.baseOnlyAfterCutoff.add(entry)
    if (!SEED_MISSING_LOCALES || entry.items == 0) return

    SUPPORTED_LANGUAGES.each { lang ->
        try {
            seedLocaleList(container, baseList, lang)
            stats.listsSeeded++
            println "  ✓ ${DRY_RUN ? '[DRY RUN] Would seed' : 'Seeded'}: ${listPath}_${lang} (${entry.items} items)"
        } catch (Exception e) {
            stats.errors.add("${listPath}_${lang} - ${e.message}")
            println "  ✗ ERROR seeding ${listPath}_${lang}: ${e.message}"
        }
    }
}

def processComponent(Node componentNode, Map target) {
    stats.componentsMatched++
    try {
        resolveContainers(componentNode, target.container).each { container ->
            inspectList(componentNode, container, target.list)
        }
    } catch (Exception e) {
        stats.errors.add("${componentNode.getPath()} - ${e.message}")
    }
}

def scanForComponents(Node node) {
    stats.nodesScanned++
    if (stats.nodesScanned % 1000 == 0) println "Scanned ${stats.nodesScanned} nodes..."
    if (node.hasProperty("mgnl:template")) {
        def target = TARGETS[node.getProperty("mgnl:template").getString()]
        if (target) processComponent(node, target)
    }
    NodeUtil.getNodes(node).each { child -> scanForComponents(child) }
}

def formatDate(Date date) {
    date ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date) : "?"
}

def printReport() {
    println "\n\n${'=' * 80}"
    println "${DRY_RUN ? 'DRY RUN ' : ''}CAPTIONED IMAGE / HEROSPOT I18N LIST AUDIT REPORT"
    println "${'=' * 80}"
    println "\nCONFIGURATION:"
    println "  Workspace: ${WORKSPACE_NAME}"
    println "  Start path: ${START_PATH}"
    println "  Templates: ${TARGETS.keySet().join(', ')}"
    println "  Cutoff date: ${CUTOFF_DATE}"
    println "  Seed missing locales: ${SEED_MISSING_LOCALES} (${SUPPORTED_LANGUAGES.join(', ') ?: 'no languages'})"
    println "  Dry run: ${DRY_RUN}"
    println "\nSTATISTICS:"
    println "  Nodes scanned: ${stats.nodesScanned}"
    println "  Components matched: ${stats.componentsMatched}"
    println "  Lists inspected: ${stats.listsInspected}"
    println "  Unaffected (not modified after cutoff): ${stats.unaffected}"
    println "  Locale lists without base list: ${stats.localeOnly}"
    println "  A - review needed: ${stats.reviewNeeded.size()}"
    println "  B - base list only, modified after cutoff: ${stats.baseOnlyAfterCutoff.size()}"
    println "  Locale lists seeded: ${stats.listsSeeded}"
    println "  Errors: ${stats.errors.size()}"

    if (!stats.reviewNeeded.isEmpty()) {
        println "\n${'=' * 80}\nA - REVIEW NEEDED (${stats.reviewNeeded.size()})\n${'=' * 80}"
        println "Base list modified after cutoff while per-locale lists exist. Edits made in a non-default"
        println "locale may have overwritten the base list; restore from page versions if so."
        stats.reviewNeeded.sort { it.path }.each { e ->
            println "\n  ${e.path}"
            println "    Modified: ${formatDate(e.modified)} by ${e.modifiedBy}"
            println "    Locale lists: ${e.locales.join(', ')}"
        }
    }
    if (!stats.baseOnlyAfterCutoff.isEmpty()) {
        println "\n${'=' * 80}\nB - BASE LIST ONLY, MODIFIED AFTER CUTOFF (${stats.baseOnlyAfterCutoff.size()})\n${'=' * 80}"
        println "Non-default locales show an empty list in the dialog but fall back to the base list in delivery."
        println "Set SEED_MISSING_LOCALES = true and SUPPORTED_LANGUAGES to copy the base list per locale."
        stats.baseOnlyAfterCutoff.sort { it.path }.each { e ->
            println "\n  ${e.path}"
            println "    Modified: ${formatDate(e.modified)} by ${e.modifiedBy}, items: ${e.items}"
        }
    }
    if (!stats.errors.isEmpty()) {
        println "\n${'=' * 80}\nERRORS (${stats.errors.size()})\n${'=' * 80}"
        stats.errors.each { println "  ${it}" }
    }

    println "\n${'=' * 80}"
    if (!stats.errors.isEmpty()) println "STATUS: COMPLETED WITH ERRORS"
    else if (stats.componentsMatched == 0) println "STATUS: NO COMPONENTS FOUND"
    else println "STATUS: SUCCESS"
    println "${'=' * 80}"
}

println """
Captioned Image / Categorized Captioned Image / Herospot i18n list audit (INC0206319)

Configuration:
  Workspace: ${WORKSPACE_NAME}
  Start Path: ${START_PATH}
  Cutoff: ${CUTOFF_DATE}
  Mode: ${DRY_RUN ? 'DRY RUN (no changes)' : 'LIVE (changes will be applied)'}
"""

try {
    if (SEED_MISSING_LOCALES && SUPPORTED_LANGUAGES.isEmpty()) {
        println "ERROR: SEED_MISSING_LOCALES requires SUPPORTED_LANGUAGES"
        return
    }

    Session session = MgnlContext.getJCRSession(WORKSPACE_NAME)
    if (!session.nodeExists(START_PATH)) { println "ERROR: Start path '${START_PATH}' does not exist"; return }

    def startTime = System.currentTimeMillis()
    scanForComponents(session.getNode(START_PATH))
    def duration = (System.currentTimeMillis() - startTime) / 1000.0

    printReport()
    println "\nExecution time: ${String.format('%.2f', duration)} seconds"

    if (!DRY_RUN && stats.listsSeeded > 0 && stats.errors.isEmpty()) {
        println "\nSaving changes to repository..."
        try {
            session.save()
            println "✓ Changes saved successfully!"
        } catch (Exception e) {
            println "✗ Error saving changes: ${e.message}"
            e.printStackTrace()
        }
    } else if (!DRY_RUN && !stats.errors.isEmpty()) {
        println "\nErrors occurred; no changes were saved."
    } else if (DRY_RUN) {
        println "\nThis was a DRY RUN. Set DRY_RUN = false to apply changes."
    }
} catch (Exception e) {
    println "\nFATAL ERROR: ${e.getMessage()}"
    e.printStackTrace()
}

println "\nScript completed."
