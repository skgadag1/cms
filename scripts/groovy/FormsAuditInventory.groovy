#!/usr/bin/env groovy
/**
 * Forms / Form Configurations / Form Fields audit inventory
 *
 * Workspaces:
 *   website            → page form components (bobcat:components/form, model-specific-lead)
 *   formsConfiguration → dxp:formConfiguration
 *   formsType          → dxp:fieldType / dxp:fieldGroup
 *   formsEmailConfig   → dxp:formEmailConfiguration (optional report)
 *
 * Output: CSV sections printed to the Groovy console. Copy each section into a .csv file.
 * Orphans / shared fields / broken UUID references are reported as separate CSVs.
 * STATISTICS are printed BEFORE the CSV dumps — the Magnolia console often truncates long output.
 *
 * Usage (Magnolia Groovy console / script app):
 *   - START_PATH narrows the scan (e.g. "/bobcat/na"); forms workspaces fall back to /bobcat
 *     when the narrowed path does not exist there.
 *   - REGION_FILTER additionally filters rows by region folder.
 *
 * NOTE: The Magnolia Groovy console maps property-style access on JCR nodes (node.path) to JCR
 * properties, returning 'unknown property' for missing ones. Always use explicit getters here.
 *
 * IMPORTANT: Read-only. Does not modify JCR.
 */

import info.magnolia.context.MgnlContext
import info.magnolia.jcr.util.NodeTypes

import javax.jcr.Node
import javax.jcr.Property
import javax.jcr.Session

// ============= CONFIGURATION =============
/** Site node name directly under the workspace root; region is the segment right after it. */
SITE_NAME = "bobcat"

/** Scan root. Use "/bobcat" for everything, or narrow to a region e.g. "/bobcat/na". */
START_PATH = "/bobcat/na"

/** Optional row-level filter by region folder (e.g. "na"), or null for all. */
REGION_FILTER = null

FORM_COMPONENT_TEMPLATES = [
    "bobcat:components/form",
    "bobcat:components/model-specific-lead"
] as Set

ENABLE_FORMS_USAGE = true
ENABLE_FORM_CONFIGURATIONS = true
ENABLE_FORM_FIELDS = true
ENABLE_EMAIL_CONFIGS = true
ENABLE_SHARED_FIELDS = true
ENABLE_ORPHANS = true
ENABLE_BROKEN_REFS = true

// ============= CSV HEADERS =============
FORMS_USAGE_HEADERS = [
    "component_path",
    "page_path",
    "region",
    "component_template",
    "form_name",
    "page_is_published",
    "component_is_published",
    "configuration_property",
    "configuration_uuid",
    "configuration_path",
    "configuration_found",
    "ga4_form_type",
    "fields"
]

FORM_CONFIG_HEADERS = [
    "configuration_path",
    "configuration_name",
    "jcr_name",
    "region",
    "is_published",
    "sender_type",
    "email_config_path",
    "salesforce_request_url",
    "salesforce_record_type",
    "salesforce_currency",
    "field_count",
    "field_paths",
    "associated_form_components",
    "pages_used",
    "page_count",
    "is_orphan",
    "last_modified"
]
CFG_PATH = 0
CFG_NAME = 1
CFG_REGION = 3
CFG_PUBLISHED = 4
CFG_PAGES = 13
CFG_ORPHAN = 15

FORM_FIELD_HEADERS = [
    "field_path",
    "field_name",
    "jcr_name",
    "node_type",
    "region",
    "is_published",
    "component_types",
    "input_names",
    "forms_used",
    "form_count",
    "included_in_dynamic_fields",
    "is_shared",
    "is_orphan",
    "modification_impact",
    "last_modified"
]
FLD_PATH = 0
FLD_NAME = 1
FLD_REGION = 4
FLD_PUBLISHED = 5
FLD_FORMS = 8
FLD_FORM_COUNT = 9
FLD_SHARED = 11
FLD_ORPHAN = 12
FLD_IMPACT = 13

EMAIL_CONFIG_HEADERS = [
    "email_config_path",
    "email_config_name",
    "jcr_name",
    "region",
    "is_published",
    "email_template",
    "sender_email",
    "recipient_email",
    "configurations_used",
    "config_count",
    "is_orphan",
    "last_modified"
]
EML_PATH = 0
EML_NAME = 1
EML_REGION = 3
EML_PUBLISHED = 4
EML_ORPHAN = 10

SHARED_FIELD_HEADERS = [
    "field_path",
    "field_name",
    "region",
    "form_count",
    "forms_used",
    "page_count",
    "pages_used",
    "modification_impact"
]

ORPHAN_HEADERS = [
    "item_type",
    "path",
    "name",
    "region",
    "is_published",
    "reason"
]

BROKEN_REF_HEADERS = [
    "ref_type",
    "source_type",
    "source_path",
    "source_region",
    "source_is_published",
    "page_path",
    "property",
    "unresolved_uuid",
    "notes"
]

// ============= STATE =============
stats = [
    formComponents: 0,
    configurations: 0,
    fields: 0,
    emailConfigs: 0,
    brokenRefs: 0,
    errors: []
]

// configuration uuid -> list of usage maps
configUsages = [:] as Map
// field path -> set of configuration paths (direct refs from configs)
fieldToConfigs = [:] as Map
// field uuid -> set of parent field paths that reference it dynamically
dynamicFieldParents = [:] as Map
// email config path -> set of configuration paths
emailToConfigs = [:] as Map
// broken UUID references discovered during the scan
brokenRefs = [] as List

// ============= HELPERS =============

def prop(Node node, String name) {
    if (node == null) {
        return null
    }
    try {
        if (!node.hasProperty(name)) {
            return null
        }
        Property p = node.getProperty(name)
        if (p.isMultiple()) {
            return p.getValues().collect { it.getString() }.join(";")
        }
        return p.getString()
    } catch (Exception e) {
        return null
    }
}

def isActivated(Node node) {
    if (node == null) {
        return false
    }
    try {
        return NodeTypes.Activatable.isActivated(node)
    } catch (Exception e) {
        return false
    }
}

def lastModified(Node node) {
    try {
        if (node.hasProperty("mgnl:lastModified")) {
            def cal = node.getProperty("mgnl:lastModified").getDate()
            if (cal != null) {
                return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime())
            }
        }
    } catch (Exception ignored) {
    }
    return ""
}

/**
 * Region is the folder immediately under /bobcat, but only when the inventoried node lives
 * deeper than that folder. Items parked directly under /bobcat (e.g. /bobcat/uploadField)
 * have no region — returning the node name itself was a false positive.
 */
def regionFromPath(String path) {
    def parts = path == null ? [] : path.split("/").findAll { it }
    def idx = parts.indexOf(SITE_NAME)
    // Need at least /bobcat/{region}/{item...}
    if (idx >= 0 && parts.size() > idx + 2) {
        return parts[idx + 1]
    }
    return ""
}

def recordBrokenRef(Map row) {
    brokenRefs << [
        row.refType ?: "",
        row.sourceType ?: "",
        row.sourcePath ?: "",
        row.sourceRegion ?: "",
        row.sourcePublished == null ? "" : row.sourcePublished,
        row.pagePath ?: "",
        row.property ?: "",
        row.uuid ?: "",
        row.notes ?: ""
    ]
    stats.brokenRefs++
}

def matchesRegion(String path) {
    if (!REGION_FILTER) {
        return true
    }
    return regionFromPath(path) == REGION_FILTER
}

def csvEscape(Object value) {
    def s = value == null ? "" : value.toString()
    if (s.contains('"') || s.contains(',') || s.contains('\n') || s.contains('\r')) {
        return '"' + s.replace('"', '""') + '"'
    }
    return s
}

def printCsv(List headers, List rows) {
    println headers.collect { csvEscape(it) }.join(",")
    rows.each { row ->
        println row.collect { csvEscape(it) }.join(",")
    }
}

def joinSorted(Collection values) {
    if (!values) {
        return ""
    }
    return values.findAll { it }.collect { it.toString() }.unique().sort().join(";")
}

/** Resolve the scan root, falling back to the site root when a narrowed path is absent. */
def resolveStartPath(Session session, String preferred) {
    def fallback = "/" + SITE_NAME
    try {
        if (preferred && session.nodeExists(preferred)) {
            return preferred
        }
        if (session.nodeExists(fallback)) {
            println "  [INFO] '${preferred}' not present in '${session.getWorkspace().getName()}'; using '${fallback}'"
            return fallback
        }
    } catch (Exception e) {
        stats.errors << "Start path resolution failed for ${session.getWorkspace().getName()}: ${e.message}"
    }
    return null
}

def findPageNode(Node node) {
    def current = node
    while (current != null && current.getDepth() > 0) {
        try {
            if (current.isNodeType(NodeTypes.Page.NAME)) {
                return current
            }
        } catch (Exception ignored) {
        }
        try {
            current = current.getParent()
        } catch (Exception ignored) {
            return null
        }
    }
    return null
}

def collectNodesByType(Node root, String primaryType, List<Node> out) {
    try {
        if (root.isNodeType(primaryType)) {
            out.add(root)
        }
    } catch (Exception ignored) {
    }
    try {
        root.getNodes().each { child -> collectNodesByType(child, primaryType, out) }
    } catch (Exception e) {
        stats.errors << "Traversal error at ${root.getPath()}: ${e.message}"
    }
}

def resolveByUuid(Session session, String uuid) {
    if (!uuid || session == null) {
        return null
    }
    try {
        return session.getNodeByIdentifier(uuid)
    } catch (Exception e) {
        return null
    }
}

/** Collect configuration / configuration_<lang> UUID properties from a form component. */
def collectConfigurationRefs(Node component) {
    def refs = []
    try {
        component.getProperties().each { Property p ->
            def name = p.getName()
            if (name == "configuration" || name.startsWith("configuration_")) {
                try {
                    def value = p.getString()
                    if (value) {
                        refs << [property: name, uuid: value]
                    }
                } catch (Exception ignored) {
                }
            }
        }
    } catch (Exception e) {
        stats.errors << "Property read error at ${component.getPath()}: ${e.message}"
    }
    return refs
}

def rememberConfigUsage(String uuid, Map usage) {
    if (!uuid) {
        return
    }
    if (!configUsages.containsKey(uuid)) {
        configUsages[uuid] = []
    }
    configUsages[uuid] << usage
}

def addFieldConfigLink(String fieldPath, String configPath) {
    if (!fieldPath || !configPath) {
        return
    }
    if (!fieldToConfigs.containsKey(fieldPath)) {
        fieldToConfigs[fieldPath] = [] as Set
    }
    fieldToConfigs[fieldPath] << configPath
}

def addDynamicParent(String fieldUuid, String parentFieldPath) {
    if (!fieldUuid || !parentFieldPath) {
        return
    }
    if (!dynamicFieldParents.containsKey(fieldUuid)) {
        dynamicFieldParents[fieldUuid] = [] as Set
    }
    dynamicFieldParents[fieldUuid] << parentFieldPath
}

def addEmailConfigLink(String emailPath, String configPath) {
    if (!emailPath || !configPath) {
        return
    }
    if (!emailToConfigs.containsKey(emailPath)) {
        emailToConfigs[emailPath] = [] as Set
    }
    emailToConfigs[emailPath] << configPath
}

/**
 * Recursively collect every dxpFormConfigurationElement UUID under a node
 * (form config fields + nested dynamic field branches).
 */
def collectElementUuids(Node node, Set<String> uuids) {
    if (node == null) {
        return
    }
    def id = prop(node, "dxpFormConfigurationElement")
    if (id) {
        uuids << id
    }
    try {
        node.getNodes().each { child -> collectElementUuids(child, uuids) }
    } catch (Exception ignored) {
    }
}

def isFieldNode(Node node) {
    try {
        return node.isNodeType("dxp:fieldType") || node.isNodeType("dxp:fieldGroup")
    } catch (Exception e) {
        return false
    }
}

def isFieldGroup(Node node) {
    try {
        return node.isNodeType("dxp:fieldGroup")
    } catch (Exception e) {
        return false
    }
}

/** Walk a formsType node for dynamic nested field refs (yes/no + dropdown parent). */
def indexDynamicRefs(Node fieldNode) {
    def fieldPath = fieldNode.getPath()
    try {
        if (fieldNode.hasNode("elements")) {
            fieldNode.getNode("elements").getNodes().each { el ->
                if (!el.hasNode("component")) {
                    return
                }
                def component = el.getNode("component")
                def type = prop(component, "field")
                if (type == "yesNoDynamicFormField" && component.hasNode("yesNoDynamicFormField")) {
                    def yn = component.getNode("yesNoDynamicFormField")
                    ["yesFields", "noFields"].each { branch ->
                        if (yn.hasNode(branch)) {
                            yn.getNode(branch).getNodes().each { item ->
                                def nested = [] as Set
                                collectElementUuids(item, nested)
                                nested.each { id -> addDynamicParent(id, fieldPath) }
                            }
                        }
                    }
                }
                if (type == "dynamicDropdownParentField") {
                    def nested = [] as Set
                    collectElementUuids(component, nested)
                    nested.each { id -> addDynamicParent(id, fieldPath) }
                }
            }
        }
        if (isFieldGroup(fieldNode)) {
            fieldNode.getNodes().each { child ->
                if (isFieldNode(child)) {
                    indexDynamicRefs(child)
                }
            }
        }
    } catch (Exception e) {
        stats.errors << "Dynamic index error at ${fieldPath}: ${e.message}"
    }
}

def collectComponentTypes(Node fieldNode, Set<String> types) {
    try {
        if (fieldNode.hasNode("elements")) {
            fieldNode.getNode("elements").getNodes().each { el ->
                if (el.hasNode("component")) {
                    def t = prop(el.getNode("component"), "field")
                    if (t) {
                        types << t
                    }
                }
            }
        }
        if (isFieldGroup(fieldNode)) {
            fieldNode.getNodes().each { child ->
                if (isFieldNode(child)) {
                    collectComponentTypes(child, types)
                }
            }
        }
    } catch (Exception ignored) {
    }
}

/** Collect CRM/input "name" properties used by field widgets. */
def collectInputNames(Node fieldNode, Set<String> names) {
    try {
        if (fieldNode.hasNode("elements")) {
            fieldNode.getNode("elements").getNodes().each { el ->
                if (!el.hasNode("component")) {
                    return
                }
                def component = el.getNode("component")
                def direct = prop(component, "name")
                if (direct) {
                    names << direct
                }
                // the actual widget config sits on the switchable child (inputField/name, …)
                component.getNodes().each { child ->
                    def n = prop(child, "name")
                    if (n) {
                        names << n
                    }
                }
            }
        }
        if (isFieldGroup(fieldNode)) {
            fieldNode.getNodes().each { child ->
                if (isFieldNode(child)) {
                    collectInputNames(child, names)
                }
            }
        }
    } catch (Exception ignored) {
    }
}

def readSender(Node configNode, Session emailSession) {
    def result = [
        senderType: "",
        emailPath: "",
        requestUrl: "",
        recordType: "",
        currency: ""
    ]
    try {
        // Legacy property used by the workflow related-nodes finder
        def legacyEmail = prop(configNode, "dxpFormEmailConfig")
        if (legacyEmail) {
            def emailNode = resolveByUuid(emailSession, legacyEmail)
            if (emailNode != null) {
                result.emailPath = emailNode.getPath()
            }
        }

        if (!configNode.hasNode("form-sender-selector")) {
            return result
        }
        def sender = configNode.getNode("form-sender-selector")
        result.senderType = prop(sender, "field") ?: ""

        if (sender.hasNode("link")) {
            def link = sender.getNode("link")
            result.requestUrl = prop(link, "requestUrl") ?: ""
            result.recordType = prop(link, "recordType") ?: ""
            result.currency = prop(link, "currency") ?: ""
            def emailUuid = prop(link, "email")
            if (emailUuid) {
                def emailNode = resolveByUuid(emailSession, emailUuid)
                result.emailPath = emailNode != null ? emailNode.getPath() : "UNRESOLVED:${emailUuid}"
            }
        }
    } catch (Exception e) {
        stats.errors << "Sender read error at ${configNode.getPath()}: ${e.message}"
    }
    return result
}

def modificationImpact(int formCount, int pageCount) {
    if (formCount <= 0) {
        return "Orphan - not referenced by any form configuration; review for cleanup"
    }
    if (formCount == 1 && pageCount <= 1) {
        return "Low - used by 1 configuration / ${pageCount} page(s); change is localized"
    }
    if (formCount == 1) {
        return "Medium - used by 1 configuration across ${pageCount} page(s); verify all pages"
    }
    return "High - shared by ${formCount} configurations and ${pageCount} page(s); changing this field affects all of them"
}

// ============= SCANS =============

def linkFieldAndNestedToConfig(Node fieldNode, String configPath) {
    addFieldConfigLink(fieldNode.getPath(), configPath)
    try {
        if (isFieldGroup(fieldNode)) {
            fieldNode.getNodes().each { child ->
                if (isFieldNode(child)) {
                    linkFieldAndNestedToConfig(child, configPath)
                }
            }
        }
    } catch (Exception ignored) {
    }
}

def scanFormComponents(Session websiteSession, Session formsSession, Session fieldsSession) {
    println "-- Scanning website form components..."
    def rows = []
    def startPath = resolveStartPath(websiteSession, START_PATH)
    if (startPath == null) {
        println "ERROR: no usable start path in website workspace"
        return rows
    }

    def stack = [websiteSession.getNode(startPath)] as List
    while (!stack.isEmpty()) {
        def node = stack.remove(stack.size() - 1)
        try {
            node.getNodes().each { child -> stack << child }
        } catch (Exception e) {
            stats.errors << "Traversal error at ${node.getPath()}: ${e.message}"
            continue
        }

        def template = prop(node, "mgnl:template")
        if (!FORM_COMPONENT_TEMPLATES.contains(template)) {
            continue
        }

        def nodePath = node.getPath()
        def page = findPageNode(node)
        def pagePath = page == null ? "" : page.getPath()
        if (!matchesRegion(pagePath ?: nodePath)) {
            continue
        }

        stats.formComponents++
        def region = regionFromPath(pagePath ?: nodePath)
        def formName = prop(node, "title") ?: node.getName()
        def ga4 = prop(node, "formType") ?: ""
        def pagePublished = page != null && isActivated(page)
        def componentPublished = isActivated(node)

        def refs = collectConfigurationRefs(node)
        if (refs.isEmpty()) {
            recordBrokenRef([
                refType: "missing_configuration_property",
                sourceType: "form_component",
                sourcePath: nodePath,
                sourceRegion: region,
                sourcePublished: componentPublished,
                pagePath: pagePath,
                property: "configuration",
                uuid: "",
                notes: "Form component has no configuration / configuration_<lang> property"
            ])
            rows << [
                nodePath, pagePath, region, template, formName,
                pagePublished, componentPublished,
                "", "", "", false, ga4, ""
            ]
            continue
        }

        refs.each { ref ->
            def configNode = resolveByUuid(formsSession, ref.uuid)
            def configPath = configNode == null ? "" : configNode.getPath()
            def fieldPaths = [] as Set
            if (configNode != null) {
                def uuids = [] as Set
                collectElementUuids(configNode, uuids)
                uuids.each { id ->
                    def fieldNode = resolveByUuid(fieldsSession, id)
                    if (fieldNode != null) {
                        fieldPaths << fieldNode.getPath()
                    }
                }
                rememberConfigUsage(ref.uuid, [
                    componentPath: nodePath,
                    pagePath: pagePath,
                    template: template,
                    configProperty: ref.property
                ])
            } else {
                recordBrokenRef([
                    refType: "missing_configuration",
                    sourceType: "form_component",
                    sourcePath: nodePath,
                    sourceRegion: region,
                    sourcePublished: componentPublished,
                    pagePath: pagePath,
                    property: ref.property,
                    uuid: ref.uuid,
                    notes: "UUID not found in formsConfiguration workspace"
                ])
            }

            rows << [
                nodePath, pagePath, region, template, formName,
                pagePublished, componentPublished,
                ref.property, ref.uuid, configPath, configNode != null,
                ga4, joinSorted(fieldPaths)
            ]
        }
    }
    println "-- Done: ${stats.formComponents} form component(s), ${rows.size()} usage row(s)"
    return rows
}

def scanFormConfigurations(Session formsSession, Session fieldsSession, Session emailSession) {
    println "-- Scanning form configurations..."
    def rows = []
    def startPath = resolveStartPath(formsSession, START_PATH)
    if (startPath == null) {
        println "ERROR: no usable start path in formsConfiguration workspace"
        return rows
    }

    def configs = [] as List
    collectNodesByType(formsSession.getNode(startPath), "dxp:formConfiguration", configs)

    configs.each { configNode ->
        def configPath = configNode.getPath()
        if (!matchesRegion(configPath)) {
            return
        }
        stats.configurations++

        def fieldUuids = [] as Set
        collectElementUuids(configNode, fieldUuids)
        def fieldPaths = [] as List
        def configRegion = regionFromPath(configPath)
        def configPublished = isActivated(configNode)
        fieldUuids.each { id ->
            def fieldNode = resolveByUuid(fieldsSession, id)
            if (fieldNode != null) {
                fieldPaths << fieldNode.getPath()
                linkFieldAndNestedToConfig(fieldNode, configPath)
            } else {
                fieldPaths << "UNRESOLVED:${id}".toString()
                recordBrokenRef([
                    refType: "missing_field",
                    sourceType: "form_configuration",
                    sourcePath: configPath,
                    sourceRegion: configRegion,
                    sourcePublished: configPublished,
                    pagePath: "",
                    property: "dxpFormConfigurationElement",
                    uuid: id,
                    notes: "UUID not found in formsType workspace"
                ])
            }
        }
        fieldPaths = fieldPaths.unique().sort()

        def sender = readSender(configNode, emailSession)
        if (sender.emailPath && sender.emailPath.toString().startsWith("UNRESOLVED:")) {
            def emailUuid = sender.emailPath.toString().substring("UNRESOLVED:".length())
            recordBrokenRef([
                refType: "missing_email_config",
                sourceType: "form_configuration",
                sourcePath: configPath,
                sourceRegion: configRegion,
                sourcePublished: configPublished,
                pagePath: "",
                property: "form-sender-selector/link/email",
                uuid: emailUuid,
                notes: "UUID not found in formsEmailConfig workspace"
            ])
        } else if (sender.emailPath) {
            addEmailConfigLink(sender.emailPath.toString(), configPath)
        }

        def usages = configUsages[configNode.getIdentifier()] ?: []
        def componentPaths = usages.collect { it.componentPath }
        def pagePaths = usages.collect { it.pagePath }.findAll { it }

        rows << [
            configPath,
            configNode.getName(),
            prop(configNode, "jcrName") ?: "",
            configRegion,
            configPublished,
            sender.senderType,
            sender.emailPath,
            sender.requestUrl,
            sender.recordType,
            sender.currency,
            fieldPaths.size(),
            joinSorted(fieldPaths),
            joinSorted(componentPaths),
            joinSorted(pagePaths),
            pagePaths.unique().size(),
            componentPaths.isEmpty(),
            lastModified(configNode)
        ]
    }
    println "-- Done: ${stats.configurations} configuration(s)"
    return rows
}

def scanFormFields(Session fieldsSession, List configRows) {
    println "-- Scanning form fields..."
    def rows = []
    def startPath = resolveStartPath(fieldsSession, START_PATH)
    if (startPath == null) {
        println "ERROR: no usable start path in formsType workspace"
        return rows
    }

    def fieldNodes = [] as List
    def root = fieldsSession.getNode(startPath)
    collectNodesByType(root, "dxp:fieldType", fieldNodes)
    collectNodesByType(root, "dxp:fieldGroup", fieldNodes)

    // Index dynamic refs first so usage can be attributed through dynamic parents
    fieldNodes.each { indexDynamicRefs(it) }

    def configPages = [:] as Map
    configRows.each { row ->
        configPages[row[CFG_PATH]] = (row[CFG_PAGES] ? row[CFG_PAGES].toString().split(";").findAll { it } : []) as List
    }

    fieldNodes.each { fieldNode ->
        def fieldPath = fieldNode.getPath()
        if (!matchesRegion(fieldPath)) {
            return
        }
        stats.fields++

        def types = [] as Set
        def inputNames = [] as Set
        collectComponentTypes(fieldNode, types)
        collectInputNames(fieldNode, inputNames)

        def forms = [] as Set
        forms.addAll(fieldToConfigs[fieldPath] ?: [] as Set)

        // Usage inherited through dynamic parents (yes/no, dependent dropdowns)
        def dynamicParents = (dynamicFieldParents[fieldNode.getIdentifier()] ?: [] as Set) as Set
        dynamicParents.each { parentPath ->
            def parentConfigs = fieldToConfigs[parentPath]
            if (parentConfigs) {
                forms.addAll(parentConfigs)
            }
        }

        def pages = [] as Set
        forms.each { configPath ->
            def p = configPages[configPath]
            if (p) {
                pages.addAll(p)
            }
        }

        def formCount = forms.size()
        def pageCount = pages.size()

        def nodeType = ""
        try {
            nodeType = fieldNode.getPrimaryNodeType().getName()
        } catch (Exception ignored) {
        }

        rows << [
            fieldPath,
            fieldNode.getName(),
            prop(fieldNode, "jcrName") ?: "",
            nodeType,
            regionFromPath(fieldPath),
            isActivated(fieldNode),
            joinSorted(types),
            joinSorted(inputNames),
            joinSorted(forms),
            formCount,
            joinSorted(dynamicParents),
            formCount > 1,
            formCount == 0 && dynamicParents.isEmpty(),
            modificationImpact(formCount, pageCount),
            lastModified(fieldNode)
        ]
    }
    println "-- Done: ${stats.fields} field node(s)"
    return rows
}

def scanEmailConfigs(Session emailSession) {
    println "-- Scanning email configs..."
    def rows = []
    if (emailSession == null) {
        println "WARN: formsEmailConfig workspace unavailable"
        return rows
    }
    def startPath = resolveStartPath(emailSession, START_PATH)
    if (startPath == null) {
        println "WARN: no usable start path in formsEmailConfig workspace"
        return rows
    }

    def emails = [] as List
    collectNodesByType(emailSession.getNode(startPath), "dxp:formEmailConfiguration", emails)
    emails.each { node ->
        def path = node.getPath()
        if (!matchesRegion(path)) {
            return
        }
        stats.emailConfigs++
        def configs = emailToConfigs[path] ?: [] as Set
        rows << [
            path,
            node.getName(),
            prop(node, "jcrName") ?: "",
            regionFromPath(path),
            isActivated(node),
            prop(node, "emailTemplate") ?: "",
            prop(node, "senderEmail") ?: "",
            prop(node, "recipientEmail") ?: "",
            joinSorted(configs),
            configs.size(),
            configs.isEmpty(),
            lastModified(node)
        ]
    }
    println "-- Done: ${stats.emailConfigs} email config(s)"
    return rows
}

def buildSharedFieldsReport(List fieldRows, List configRows) {
    def configPages = [:] as Map
    configRows.each { row ->
        configPages[row[CFG_PATH]] = (row[CFG_PAGES] ? row[CFG_PAGES].toString().split(";").findAll { it } : []) as List
    }
    def rows = []
    fieldRows.each { row ->
        if (row[FLD_SHARED] != true) {
            return
        }
        def formsUsed = row[FLD_FORMS] == null ? "" : row[FLD_FORMS].toString()
        def pages = [] as Set
        formsUsed.split(";").findAll { it }.each { cfg ->
            def p = configPages[cfg]
            if (p) {
                pages.addAll(p)
            }
        }
        rows << [
            row[FLD_PATH],
            row[FLD_NAME],
            row[FLD_REGION],
            row[FLD_FORM_COUNT],
            formsUsed,
            pages.size(),
            joinSorted(pages),
            row[FLD_IMPACT]
        ]
    }
    return rows
}

def buildOrphansReport(List configRows, List fieldRows, List emailRows) {
    def rows = []
    configRows.each { row ->
        if (row[CFG_ORPHAN] == true) {
            rows << ["form_configuration", row[CFG_PATH], row[CFG_NAME], row[CFG_REGION], row[CFG_PUBLISHED],
                     "Candidate: no website form component currently references this configuration. Review broken_references.csv before cleanup."]
        }
    }
    fieldRows.each { row ->
        if (row[FLD_ORPHAN] == true) {
            rows << ["form_field", row[FLD_PATH], row[FLD_NAME], row[FLD_REGION], row[FLD_PUBLISHED],
                     "Candidate: not referenced by any resolvable form configuration (direct or dynamic). Review broken_references.csv before cleanup."]
        }
    }
    emailRows.each { row ->
        if (row[EML_ORPHAN] == true) {
            rows << ["email_config", row[EML_PATH], row[EML_NAME], row[EML_REGION], row[EML_PUBLISHED],
                     "Candidate: not referenced by any form configuration sender. Review broken_references.csv before cleanup."]
        }
    }
    return rows
}

// ============= MAIN =============
println """
================================================================================
Forms Audit Inventory
================================================================================
Site name:       ${SITE_NAME}
Start path:      ${START_PATH}
Region filter:   ${REGION_FILTER ?: '(all)'}
Components:      ${FORM_COMPONENT_TEMPLATES.join(', ')}
Mode:            READ-ONLY
================================================================================
"""

try {
    Session websiteSession = MgnlContext.getJCRSession("website")
    Session formsSession = MgnlContext.getJCRSession("formsConfiguration")
    Session fieldsSession = MgnlContext.getJCRSession("formsType")
    Session emailSession = null
    try {
        emailSession = MgnlContext.getJCRSession("formsEmailConfig")
    } catch (Exception e) {
        println "WARN: formsEmailConfig workspace unavailable: ${e.message}"
    }

    def start = System.currentTimeMillis()

    // 1) Page form components (populates configUsages)
    def formsUsageRows = scanFormComponents(websiteSession, formsSession, fieldsSession)

    // 2) Configurations (populates fieldToConfigs + emailToConfigs)
    def configRows = scanFormConfigurations(formsSession, fieldsSession, emailSession)

    // 3) Fields
    def fieldRows = scanFormFields(fieldsSession, configRows)

    // 4) Email configs
    def emailRows = ENABLE_EMAIL_CONFIGS ? scanEmailConfigs(emailSession) : []

    def sharedRows = ENABLE_SHARED_FIELDS ? buildSharedFieldsReport(fieldRows, configRows) : []
    def orphanRows = ENABLE_ORPHANS ? buildOrphansReport(configRows, fieldRows, emailRows) : []
    def brokenRows = ENABLE_BROKEN_REFS ? brokenRefs : []

    def duration = (System.currentTimeMillis() - start) / 1000.0

    // Print summary BEFORE CSV dumps — Magnolia Groovy console often truncates long output.
    def uniqueMissingConfigs = brokenRows.findAll { it[0] == "missing_configuration" }.collect { it[7] }.unique().size()
    def uniqueMissingFields = brokenRows.findAll { it[0] == "missing_field" }.collect { it[7] }.unique().size()
    def uniqueMissingEmails = brokenRows.findAll { it[0] == "missing_email_config" }.collect { it[7] }.unique().size()
    def resolvedUsage = formsUsageRows.count { it[10] == true }
    def unresolvedUsage = formsUsageRows.count { it[8] && it[10] == false }

    println """
================================================================================
STATISTICS (printed before CSVs to survive console truncation)
================================================================================
Form components scanned:       ${stats.formComponents}
Form usage rows:               ${formsUsageRows.size()}
  with resolved configuration: ${resolvedUsage}
  with missing configuration:  ${unresolvedUsage}
Form configurations:           ${stats.configurations}
Form fields / groups:          ${stats.fields}
Email configs:                 ${stats.emailConfigs}
Shared fields (form_count>1):  ${sharedRows.size()}
Orphan candidates:             ${orphanRows.size()}
Broken references:             ${brokenRows.size()}
  unique missing configs:      ${uniqueMissingConfigs}
  unique missing fields:       ${uniqueMissingFields}
  unique missing emails:       ${uniqueMissingEmails}
Execution time:                ${String.format('%.2f', duration)} s
================================================================================
"""
    if (!stats.errors.isEmpty()) {
        println "ERRORS / WARNINGS (${stats.errors.size()}):"
        stats.errors.each { println "  - ${it}" }
        println ""
    }

    if (ENABLE_FORMS_USAGE) {
        println "\n===== CSV: forms_usage.csv ====="
        printCsv(FORMS_USAGE_HEADERS, formsUsageRows)
        println "===== END forms_usage.csv (${formsUsageRows.size()} rows) ====="
    }

    if (ENABLE_FORM_CONFIGURATIONS) {
        println "\n===== CSV: form_configurations.csv ====="
        printCsv(FORM_CONFIG_HEADERS, configRows)
        println "===== END form_configurations.csv (${configRows.size()} rows) ====="
    }

    if (ENABLE_FORM_FIELDS) {
        println "\n===== CSV: form_fields.csv ====="
        printCsv(FORM_FIELD_HEADERS, fieldRows)
        println "===== END form_fields.csv (${fieldRows.size()} rows) ====="
    }

    if (ENABLE_EMAIL_CONFIGS) {
        println "\n===== CSV: form_email_configs.csv ====="
        printCsv(EMAIL_CONFIG_HEADERS, emailRows)
        println "===== END form_email_configs.csv (${emailRows.size()} rows) ====="
    }

    if (ENABLE_SHARED_FIELDS) {
        println "\n===== CSV: shared_fields.csv ====="
        printCsv(SHARED_FIELD_HEADERS, sharedRows)
        println "===== END shared_fields.csv (${sharedRows.size()} rows) ====="
    }

    if (ENABLE_ORPHANS) {
        println "\n===== CSV: orphans.csv ====="
        printCsv(ORPHAN_HEADERS, orphanRows)
        println "===== END orphans.csv (${orphanRows.size()} rows) ====="
    }

    if (ENABLE_BROKEN_REFS) {
        println "\n===== CSV: broken_references.csv ====="
        printCsv(BROKEN_REF_HEADERS, brokenRows)
        println "===== END broken_references.csv (${brokenRows.size()} rows) ====="
    }

    println "\nScript completed (read-only, no changes saved)."
} catch (Exception e) {
    println "\nERROR: ${e.message}"
    e.printStackTrace()
}
