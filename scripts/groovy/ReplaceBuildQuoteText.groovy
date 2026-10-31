import info.magnolia.context.MgnlContext

import javax.jcr.Session
import javax.jcr.Node
import javax.jcr.NodeIterator
import javax.jcr.Property
import javax.jcr.PropertyIterator
import javax.jcr.PropertyType
import javax.jcr.Value
import javax.jcr.ValueFactory
import java.util.ArrayDeque
import java.util.Deque
import java.util.regex.Matcher
import java.util.regex.Pattern

// ============================================================================
// ReplaceBuildQuoteText.groovy
// ----------------------------------------------------------------------------

BUILD_QUOTE_PATTERN = Pattern.compile(
        "(Build)(\\s*(?:&amp;|&|and)\\s*)(Quote)",
        Pattern.CASE_INSENSITIVE
)

// Lightweight pre-check so we only run the regex on values that plausibly match.
QUICK_HINTS = ["quote"]

// the ENTIRE value is left untouched. These are technical identifiers / URLs
CATEGORY_B_SKIP_TOKENS = [
        "buildandquote.bobcat.com",
        "buildquote.api",
        "buildquote.bobcat.com",
        "build_and_quote",
        "build_quote__c",
        "buildquoteid",
        "buildandquoteapi"
].collect { it.toLowerCase() }

// Add more workspaces/paths here only if needed.
WORKSPACES = [
        'website': '/bobcat',
        'concepts': '/bobcat',
]

SKIPPED_PROPERTIES = [
        "jcr:created",
        "jcr:createdBy",
        "jcr:lastModified",
        "jcr:lastModifiedBy",
        "jcr:uuid",
        "mgnl:created",
        "mgnl:createdBy",
        "mgnl:lastModified",
        "mgnl:lastModifiedBy",
        "mgnl:activationStatus"
] as Set

SKIPPED_NODE_NAMES = [
        "jcr:system"
] as Set

// Keep DRY_RUN = true for the inventory / dry-run pass requested.
// Review the report, then flip to false to apply changes.
DRY_RUN = true

// Set high enough to inventory everything during the dry run.
MAX_UPDATES_PER_WORKSPACE = 100000

SAVE_BATCH_SIZE = 100

VERBOSE = false

// ============================================================================
// Replacement logic
// ============================================================================

// Replaces "Quote" with "Price" preserving the casing of the original word.
String matchQuoteCasing(String quoteWord) {
    if (quoteWord == quoteWord.toUpperCase()) {
        return "PRICE"
    }
    if (quoteWord == quoteWord.toLowerCase()) {
        return "price"
    }
    // Title case or anything else -> Price
    return "Price"
}

String replaceValue(String originalValue) {
    Matcher matcher = BUILD_QUOTE_PATTERN.matcher(originalValue)
    StringBuffer sb = new StringBuffer()

    while (matcher.find()) {
        String buildWord = matcher.group(1)   // Build / BUILD / build
        String connector = matcher.group(2)   // e.g. " & ", " &amp; ", " and "
        String quoteWord = matcher.group(3)   // Quote / QUOTE / quote

        String priceWord = matchQuoteCasing(quoteWord)
        String replacement = buildWord + connector + priceWord

        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
    }
    matcher.appendTail(sb)

    return sb.toString()
}

boolean isCategoryBValue(String value) {
    String lower = value.toLowerCase()
    return CATEGORY_B_SKIP_TOKENS.any { lower.contains(it) }
}

boolean mightContainTarget(String value) {
    String lower = value.toLowerCase()
    return QUICK_HINTS.any { lower.contains(it) }
}

boolean processProperty(Property prop) {
    boolean modified = false
    String propName = prop.getName()

    if (prop.isMultiple()) {
        Value[] values = prop.getValues()
        ValueFactory valueFactory = prop.getSession().getValueFactory()
        Value[] newValues = new Value[values.length]

        for (int i = 0; i < values.length; i++) {
            String originalValue = values[i].getString()
            String finalValue = originalValue

            if (mightContainTarget(originalValue) && !isCategoryBValue(originalValue)) {
                String updatedValue = replaceValue(originalValue)

                if (updatedValue != originalValue) {
                    modified = true

                    if (VERBOSE) {
                        println("  -> Multi Property [$propName][$i]")
                        println("     Old: $originalValue")
                        println("     New: $updatedValue")
                    }

                    finalValue = updatedValue
                }
            }

            newValues[i] = valueFactory.createValue(finalValue)
        }

        if (modified && !DRY_RUN) {
            prop.getParent().setProperty(propName, newValues)
        }
    } else {
        String originalValue = prop.getString()

        if (!mightContainTarget(originalValue) || isCategoryBValue(originalValue)) {
            return false
        }

        String updatedValue = replaceValue(originalValue)

        if (updatedValue != originalValue) {
            modified = true

            if (VERBOSE) {
                println("  -> Property [$propName]")
                println("     Old: $originalValue")
                println("     New: $updatedValue")
            }

            if (!DRY_RUN) {
                prop.getParent().setProperty(prop.getName(), updatedValue)
            }
        }
    }

    return modified
}

boolean processNodeProperties(Node node) {
    boolean modified = false
    PropertyIterator properties = node.getProperties()

    while (properties.hasNext()) {
        Property property = properties.nextProperty()

        // Skip Magnolia/JCR system properties
        if (SKIPPED_PROPERTIES.contains(property.getName())) {
            continue
        }

        // Only inspect STRING properties
        if (property.getType() != PropertyType.STRING) {
            continue
        }

        modified = processProperty(property) || modified
    }

    return modified
}

// ============================================================================
// Workspace Processing
// ============================================================================

// Returns the number of nodes matched/updated in this workspace so the caller
// can accumulate the grand total. (Using a return value avoids Groovy binding
// scope issues that can leave a script-level counter stuck at 0.)
int processWorkspace(String workspace, String startPath) {
    int updatedNodes = 0

    try {
        println("\nSearching workspace: $workspace")
        println("Root path: $startPath")

        Session session = MgnlContext.getJCRSession(workspace)
        Node rootNode = session.getNode(startPath)

        Deque<Node> nodesToProcess = new ArrayDeque<>()
        nodesToProcess.add(rootNode)

        while (!nodesToProcess.isEmpty()) {
            if (updatedNodes >= MAX_UPDATES_PER_WORKSPACE) {
                println("Reached limit of $MAX_UPDATES_PER_WORKSPACE updates.")
                break
            }

            Node currentNode = nodesToProcess.removeFirst()

            if (SKIPPED_NODE_NAMES.contains(currentNode.getName())) {
                continue
            }

            if (currentNode.getName().equals("jcr:frozenNode") || currentNode.isNodeType("nt:frozenNode")) {
                println("Skipping frozen node: ${currentNode.getPath()}")
                continue
            }

            if (currentNode.isLocked()) {
                println("Skipping locked node: ${currentNode.getPath()}")
                continue
            }

            boolean nodeModified = processNodeProperties(currentNode)

            if (nodeModified) {
                updatedNodes++
                println("Updated node [$updatedNodes]: ${currentNode.getPath()}")

                if (!DRY_RUN && updatedNodes % SAVE_BATCH_SIZE == 0) {
                    println("Saving batch...")
                    session.save()
                }
            }

            NodeIterator children = currentNode.getNodes()

            while (children.hasNext()) {
                nodesToProcess.add(children.nextNode())
            }
        }

        if (!DRY_RUN) {
            println("Performing final save...")
            session.save()
        }

        println("Workspace [$workspace] complete.")
        println("Matched/updated $updatedNodes nodes.")
        return updatedNodes
    } catch (Exception e) {
        println("Failed processing workspace: $workspace")
        println(e)
        e.printStackTrace()
    }

    return updatedNodes
}

// ============================================================================
// Entry Point
// ============================================================================

println("============================================================")
println("ReplaceBuildQuoteText  (DRY_RUN = $DRY_RUN)")
println("Build & Quote  ->  Build & Price")
println("============================================================")

int totalUpdatedNodes = 0
WORKSPACES.each { workspace, path ->
    totalUpdatedNodes += processWorkspace(workspace, path)
}

println("\n============================================================")
println("DONE. Total nodes matched/updated across all workspaces: $totalUpdatedNodes")
if (DRY_RUN) {
    println("This was a DRY RUN - no content was changed.")
    println("Review the output above, then set DRY_RUN = false to apply.")
}
println("============================================================")