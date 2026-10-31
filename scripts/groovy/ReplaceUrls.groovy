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

// ============================================================================
// Configuration
// ============================================================================

FIND_PATTERNS = [
        "/www.bobcat.com",
        "/new.bobcat.com",
        "/bobcat.com"
]

REPLACE_WITH = "/new.dev.bobcat.com"

FIND_PATTERNS_LOWER =
        FIND_PATTERNS.collect { it.toLowerCase() }

WORKSPACES = [
        'concepts': '/',
        'website' : '/',
        'faq' : '/',
        'products': '/',
        'attachments': '/',
        'features': '/',
        'category': '/',
        'dxpTags': '/',
        'facets': '/',
        'formsConfiguration': '/',
        'formsEmailConfig': '/',
        'formsMapping': '/',
        'formsType': '/',
//      'regions': '/'
        'toc': '/',
        'toc-packages': '/'
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

DRY_RUN = true

MAX_UPDATES_PER_WORKSPACE = 50

SAVE_BATCH_SIZE = 100

VERBOSE = false

// ============================================================================
// Helpers
// ============================================================================

String replaceValue(String originalValue) {
    String updatedValue = originalValue

    FIND_PATTERNS.each { pattern ->
        updatedValue = updatedValue.replace(pattern, REPLACE_WITH)
    }

    return updatedValue
}

boolean mightContainTarget(String value) {
    String lower = value.toLowerCase()

    return FIND_PATTERNS_LOWER.any {
        lower.contains(it)
    }
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

            if (mightContainTarget(originalValue)) {
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

        if (!mightContainTarget(originalValue)) {
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

void processWorkspace(String workspace, String startPath) {
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
        println("Updated $updatedNodes nodes.")
    } catch (Exception e) {
        println("Failed processing workspace: $workspace")
        println(e)
        e.printStackTrace()
    }
}

// ============================================================================
// Entry Point
// ============================================================================

WORKSPACES.each { workspace, path ->
    processWorkspace(workspace, path)
}