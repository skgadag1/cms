import info.magnolia.context.MgnlContext
import javax.jcr.*
import javax.jcr.query.*

Set<String> availableFormFields = [
        'radioButtonField', 'selectField', 'queryStringField', 'checkboxGroupField',
        'captchaField', 'statesListField', 'hiddenInputField', 'textAreaField',
        'inputField', 'numericField', 'addressAutocompleteField', 'phoneAutocompleteField',
        'productSelectorField', 'relatedFields', 'relatedCheckboxField', 'brochureField',
        'customText', 'yesNoDynamicFormField', 'dynamicDropdownParentField', 'dynamicHiddenField',
        'productGroupsField', 'uploadField', 'dealerLocatorProductInterestField'
]

DXP_FORM_CONFIGURATION_ELEMENT = 'dxpFormConfigurationElement'
CONFIGURATION = 'configuration'
FORMS_CONFIGURATION = 'formsConfiguration'
FORMS_TYPE = 'formsType'
FIELD = 'field'

def getUsedComponents(Session session) {
    def queryManager = session.workspace.queryManager
    def query = queryManager.createQuery(
            "SELECT * FROM [nt:base] WHERE [mgnl:template] = 'bobcat:components/form'", Query.JCR_SQL2
    )
    def result = query.execute()

    return result.nodes.collect { it.hasProperty(CONFIGURATION) ? it.getProperty(CONFIGURATION).getString() : null }
            .findAll { it != null } as Set
}

def executeUuidQuery(Set<String> uuids, String workspace) {
    if (uuids.isEmpty()) return []

    def session = MgnlContext.getJCRSession(workspace)
    def queryManager = session.workspace.queryManager
    def queryCondition = uuids.collect { "[jcr:uuid] = '$it'" }.join(" OR ")
    def query = queryManager.createQuery("SELECT * FROM [nt:base] WHERE $queryCondition", Query.JCR_SQL2)

    return query.execute().nodes.toList()
}

def extractProperties(Node node, List<String> collectedProperties, String property) {
    if (node.hasProperty(property)) {
        collectedProperties.add(node.getProperty(property).getString())
    }
    node.nodes.each { extractProperties(it, collectedProperties, property) }
}

def getUsedFormsConfiguration(Set<String> usedComponents) {
    def usedFormTypes = []
    executeUuidQuery(usedComponents, FORMS_CONFIGURATION).each { node ->
        extractProperties(node, usedFormTypes, DXP_FORM_CONFIGURATION_ELEMENT)
    }
    return usedFormTypes as Set
}

def getUsedFormFields(Set<String> usedFormTypes) {
    def usedFormFields = []
    executeUuidQuery(usedFormTypes, FORMS_TYPE).each { node ->
        extractProperties(node, usedFormFields, FIELD)
    }
    return usedFormFields as Set
}

def main(availableFormFields){
    def session = MgnlContext.getJCRSession("website")
    def usedComponents = getUsedComponents(session)
    def usedFormTypes = getUsedFormsConfiguration(usedComponents)
    def usedFormFields = getUsedFormFields(usedFormTypes)

    def unusedFormFields = availableFormFields - usedFormFields

    println "Unused Form Fields: ${unusedFormFields}"
}

main(availableFormFields)
