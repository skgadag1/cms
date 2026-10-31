#!/usr/bin/env groovy
/**
 * Flexible Listing card i18n audit (INC0210867)
 *
 * AV-3053 left every flexiblelisting card text field non-translatable, so
 * from CUTOFF_DATE until the dialog fix English edits to cards had no Spanish
 * counterpart, while older field-level values (title_es, description_es, ...
 * from the AV-2615 migration) stayed in JCR and are served again now that the
 * fields are i18n.
 *
 * REPORTS:
 *   A - cards modified after CUTOFF_DATE that still carry "_$LANG" values:
 *       the translation likely predates the English content. Batch translation
 *       skips filled fields, so translate these pages with overwrite, or set
 *       CLEAR_STALE_TRANSLATIONS = true to remove the values (falls back to English).
 *   B - cards modified after CUTOFF_DATE without any translation.
 *   C - leftover list-level copies (cards_$LANG) that were never migrated.
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeUtil

import javax.jcr.Node
import javax.jcr.Session
import java.text.SimpleDateFormat

// ============= CONFIGURATION =============

WORKSPACE_NAME = "website"
DRY_RUN = true
START_PATH = "/bobcat/na"
TARGET_TEMPLATE = "bobcat:components/flexiblelisting"
SUPPORTED_LANGUAGES = ["es"]
CUTOFF_DATE = "2026-08-19"
CLEAR_STALE_TRANSLATIONS = false

TEXT_FIELDS = ["title", "evolutionTitle", "description", "caption", "info"]

cutoff = new SimpleDateFormat("yyyy-MM-dd").parse(CUTOFF_DATE)

stats = [
    nodesScanned: 0, componentsMatched: 0, cardsInspected: 0, propertiesCleared: 0,
    staleTranslations: [], untranslated: [], localeLists: [], errors: [],
]

// ============= HELPER FUNCTIONS =============

def lastModified(Node node) {
    node.hasProperty("mgnl:lastModified") ? node.getProperty("mgnl:lastModified").getDate().getTime() : null
}

def formatDate(Date date) {
    date ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date) : "?"
}

def inspectCard(Node card, Date componentModified, String lang) {
    def modified = lastModified(card) ?: componentModified
    if (!modified?.after(cutoff)) return

    def translated = TEXT_FIELDS.findAll { card.hasProperty("${it}_${lang}") }
    def entry = [path: card.getPath(), modified: modified, lang: lang, fields: translated]
    if (translated.isEmpty()) {
        stats.untranslated.add(entry)
        return
    }
    stats.staleTranslations.add(entry)
    if (!CLEAR_STALE_TRANSLATIONS) return
    translated.each { field ->
        if (!DRY_RUN) card.getProperty("${field}_${lang}").remove()
        stats.propertiesCleared++
    }
}

def processComponent(Node componentNode) {
    stats.componentsMatched++
    def componentModified = lastModified(componentNode)
    SUPPORTED_LANGUAGES.each { lang ->
        if (componentNode.hasNode("cards_${lang}")) stats.localeLists.add(componentNode.getNode("cards_${lang}").getPath())
        if (!componentNode.hasNode("cards")) return
        NodeUtil.getNodes(componentNode.getNode("cards")).each { card ->
            stats.cardsInspected++
            inspectCard(card, componentModified, lang)
        }
    }
}

def scanForComponents(Node node) {
    stats.nodesScanned++
    if (stats.nodesScanned % 1000 == 0) println "Scanned ${stats.nodesScanned} nodes..."
    if (node.hasProperty("mgnl:template") && node.getProperty("mgnl:template").getString() == TARGET_TEMPLATE) {
        try {
            processComponent(node)
        } catch (Exception e) {
            stats.errors.add("${node.getPath()} - ${e.message}")
        }
    }
    NodeUtil.getNodes(node).each { child -> scanForComponents(child) }
}

def printReport() {
    println "\n\n${'=' * 80}"
    println "${DRY_RUN ? 'DRY RUN ' : ''}FLEXIBLE LISTING I18N AUDIT REPORT"
    println "${'=' * 80}"
    println "\nCONFIGURATION:"
    println "  Workspace: ${WORKSPACE_NAME}"
    println "  Start path: ${START_PATH}"
    println "  Cutoff date: ${CUTOFF_DATE}"
    println "  Languages: ${SUPPORTED_LANGUAGES.join(', ')}"
    println "  Clear stale translations: ${CLEAR_STALE_TRANSLATIONS}"
    println "  Dry run: ${DRY_RUN}"
    println "\nSTATISTICS:"
    println "  Nodes scanned: ${stats.nodesScanned}"
    println "  Components matched: ${stats.componentsMatched}"
    println "  Cards inspected: ${stats.cardsInspected}"
    println "  A - translation older than English edit: ${stats.staleTranslations.size()}"
    println "  B - edited after cutoff, untranslated: ${stats.untranslated.size()}"
    println "  C - unmigrated list-level copies: ${stats.localeLists.size()}"
    println "  Properties cleared: ${stats.propertiesCleared}"
    println "  Errors: ${stats.errors.size()}"

    if (!stats.staleTranslations.isEmpty()) {
        println "\n${'=' * 80}\nA - TRANSLATION OLDER THAN ENGLISH EDIT (${stats.staleTranslations.size()})\n${'=' * 80}"
        println "Translate these pages with overwrite, or set CLEAR_STALE_TRANSLATIONS = true."
        stats.staleTranslations.sort { it.path }.each { e ->
            println "\n  ${e.path}"
            println "    Modified: ${formatDate(e.modified)}, ${e.lang} fields: ${e.fields.join(', ')}"
        }
    }
    if (!stats.untranslated.isEmpty()) {
        println "\n${'=' * 80}\nB - EDITED AFTER CUTOFF, UNTRANSLATED (${stats.untranslated.size()})\n${'=' * 80}"
        println "Add these pages to a translation batch."
        stats.untranslated.sort { it.path }.each { e -> println "  ${e.path} (${formatDate(e.modified)}, ${e.lang})" }
    }
    if (!stats.localeLists.isEmpty()) {
        println "\n${'=' * 80}\nC - UNMIGRATED LIST-LEVEL COPIES (${stats.localeLists.size()})\n${'=' * 80}"
        println "Delivery serves these instead of the field-level values; migrate with the AV-2615 script (git show 137b72a8ab)."
        stats.localeLists.sort().each { println "  ${it}" }
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

// ============= MAIN EXECUTION =============

println """
Flexible Listing card i18n audit (INC0210867)

Configuration:
  Workspace: ${WORKSPACE_NAME}
  Start Path: ${START_PATH}
  Cutoff: ${CUTOFF_DATE}
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

    if (!DRY_RUN && stats.propertiesCleared > 0 && stats.errors.isEmpty()) {
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
