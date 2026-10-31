import info.magnolia.jcr.predicate.NodePropertyNamePredicate
import java.util.stream.Collectors
import java.util.stream.Stream

import static com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.*

FORM_CSV_HEADERS = ["form_path", "form_name", "region", "is_published", "pages_used", "fields"]
FORM_PATH_INDEX = 0
FORM_FIELDS_INDEX = 5

FIELD_CSV_HEADERS = ["field_path", "field_name", "region", "is_published", "forms_used", "included_in_dynamic_fields"]
FIELD_PATH_INDEX = 0
FIELD_FORMS_USED_INDEX = 4

ENABLE_FORMS = true
ENABLE_FIELDS = true

def printRow(row) {
    println row.join(',')
}

def main() {
    usedForms = getUsedForms()
    csvRows = listForms(usedForms)

    if (ENABLE_FORMS) {
        println "-- Generating Forms CSV"
        printRow(FORM_CSV_HEADERS)
        csvRows.each { printRow(it) }
        println "-- Done: Generating Forms CSV"
    }

    if (ENABLE_FIELDS) {
        mapping = getFieldFormMapping(csvRows)
        fields = listFormFields(mapping)

        println "-- Generating Form Fields CSV"
        printRow(FIELD_CSV_HEADERS)
        fields.each { printRow(it) }
        println "-- Done: Generating Form Fielsd CSV"
    }
}

def isFormTemplate(node) {
    com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getNodeProperty(node, "mgnl:template") == "bobcat:components/form"
}

def ArrayList<Map<String, String>> getUsedForms() {
    println "-- Getting all form components in pages"
    session = ctx.getJCRSession("website")
    bobcat = session.getRootNode().getNode("bobcat")
    forms = []
    com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getNodes(bobcat, "mgnl:component")
            .each {
                if (isFormTemplate(it)) {
                    parent = tryGetParentNodeWithType(it, "mgnl:page").get()
                    forms.add([formId         : com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getNodeProperty(it, "configuration"),
                               pagePath       : parent.getPath(),
                               isPagePublished: com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isActivated(it),])
                }
            }
    println "-- Done: Getting all form components in pages"
    return forms
}

def listForms(usedForms) {
    println "-- Listing all forms"
    formsSession = ctx.getJCRSession("formsConfiguration")
    fieldsSession = ctx.getJCRSession("formsType")
    bobcat = formsSession.getRootNode().getNode("bobcat")
    forms = []
    bobcat.getNodes().each {
        it.getNodes()
                .each(formNode -> {
                    filteredPages = usedForms.stream()
                            .filter { it["formId"] == formNode.getUUID() }
                            .map { it["pagePath"] }
                            .collect(Collectors.toList())
                            .join(';')
                    fields = ''
                    if (formNode.hasNode("fields")) {
                        fields = formNode.getNode("fields").getNodes()
                                .stream()
                                .map {
                                    fieldId = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getNodeProperty(it, "dxpFormConfigurationElement")
                                    if (fieldId != null) {
                                        try {
                                            return fieldsSession.getNodeByIdentifier(fieldId).getPath()
                                        } catch (e) {
                                            println e
                                            println "  Field not found for id ${fieldId}"
                                        }
                                    }
                                }
                                .filter { it != null }
                                .collect(Collectors.toList())
                                .join(';')
                    }
                    forms.add([formNode.getPath(),
                               formNode.getName(),
                               it.getName(),
                               com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isActivated(formNode),
                               filteredPages,
                               fields,])
                })
    }
    println "-- Done: Listing all forms"
    return forms
}

def getFieldFormMapping(formsCsv) {
    mapping = new HashMap<String, ArrayList<String>>()

    formsCsv.each {
        it[FORM_FIELDS_INDEX].split(';').each(field -> {
            if (!mapping.containsKey(field)) {
                mapping.put(field, new ArrayList())
            }
            mapping.get(field).add(it[FORM_PATH_INDEX])
        })
    }

    return mapping
}

def extendMappingWithDynamicFields(String fieldPath, component, Map<String, List<String>> dynamicFieldsMapping) {
    try {
        var type = component.getProperty('field').getValue().getString()
        switch (type) {
            case 'yesNoDynamicFormField':
                var yesNoField = component.getNode('yesNoDynamicFormField')
                Stream.concat(
                        yesNoField.getNode('yesFields').getNodes().stream(),
                        yesNoField.getNode('noFields').getNodes().stream(),
                ).forEach {
                    final String nodeId = it.getProperty('dxpFormConfigurationElement').getValue().getString()
                    if (!dynamicFieldsMapping.containsKey(nodeId)) {
                        dynamicFieldsMapping.put(nodeId, new ArrayList<String>())
                    }
                    dynamicFieldsMapping.get(nodeId).add(fieldPath)
                }
                break
        }
    } catch (e) {
        println e
        println "  Error processing dynamic fields for field ${fieldPath}"
    }
}

def processFieldNode(fieldNode, Map<String, List<String>> fields, Map<String, List<String>> dynamicFieldsMapping, String region) {
    var fieldPath = fieldNode.getPath()
    fields.put(fieldPath, [fieldNode.getPath(),
                           fieldNode.getName(),
                           region,
                           com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.isActivated(fieldNode),
            fieldNode.getUUID(), // temporarily store UUID here, it will be replaced by form paths later
    ])

    if (fieldNode.getPrimaryNodeType() == 'dxp:fieldGroup') {
        // field group
        fieldNode.getNodes().each(nestedNode -> processFieldNode(nestedNode, fields, dynamicFieldsMapping, region))
    } else if (fieldNode.hasNode('elements')) {
        fieldNode.getNode('elements').getNodes().each {
            var component = it.getNode('component')
            if (component != null) {
                extendMappingWithDynamicFields(fieldPath, component, dynamicFieldsMapping)
            }
        }
    }
}

def listFormFields(Map<String, List<String>> mapping) {
    println "-- Listing all form fields"
    session = ctx.getJCRSession("formsType")
    bobcat = session.getRootNode().getNode("bobcat")
    fields = new HashMap<String, List<String>>()
    dynamicFieldsMapping = new HashMap<String, List<String>>()
    bobcat.getNodes().each {
        it.getNodes()
                .each(fieldNode -> processFieldNode(fieldNode, fields, dynamicFieldsMapping, it.getName()))
    }
    var allFields = fields.entrySet().stream().map {
        var row = it.getValue()
        var fieldPath = row.get(FIELD_PATH_INDEX)
        var fieldId = row[FIELD_FORMS_USED_INDEX]
        var formsUsed = mapping.get(fieldPath) ?: new ArrayList<String>()

        if (dynamicFieldsMapping.containsKey(fieldId)) {
            usedInFields = dynamicFieldsMapping.get(fieldId)
            usedInFields.forEach {
                mapping.get(it)?.forEach(form -> {
                    if (!formsUsed.contains(form)) {
                        formsUsed.add(form)
                    }
                })
            }
            row.add(usedInFields.join(';'))
        }
        row[FIELD_FORMS_USED_INDEX] = formsUsed.join(';') ?: ''

        return row
    }.collect(Collectors.toList())
    println "-- Done: Listing all form fields"
    return allFields
}

main()
return "done"