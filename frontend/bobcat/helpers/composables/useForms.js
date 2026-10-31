import {useContext, computed} from '@nuxtjs/composition-api'
import FORM_FIELD_TYPES from '~/constants/formFieldTypes'
import {COUNTRY_CODES} from '~/constants/countries'
import {BUSINESS_REGIONS} from '~/constants'

const fieldsWithOptions = [
  FORM_FIELD_TYPES.SELECT,
  FORM_FIELD_TYPES.CHECKBOX_GROUP,
  FORM_FIELD_TYPES.PRODUCT_GROUPS,
  FORM_FIELD_TYPES.RADIO
]
const fieldsWithMultipleValues = [
  FORM_FIELD_TYPES.CHECKBOX_GROUP,
  FORM_FIELD_TYPES.PRODUCT_GROUPS
]

export default function useForms() {
  const {$cookies, $regions, $utils, $ga4} = useContext()

  // CONDITIONS
  const isCaptchaFieldType = fieldName =>
    fieldName === FORM_FIELD_TYPES.CAPTCHA
  // END OF CONDITIONS

  // HELPERS
  const walkFields = (node, callback) => {
    if (!node || typeof node !== 'object') { return }

    // Call callback if the node looks like a field
    callback(node)

    // Traverse arrays of fields
    if (Array.isArray(node.fields)) {
      node.fields.forEach(f => walkFields(f, callback))
    }

    // Traverse elements with @nodes
    const elements = node.elements?.['@nodes']?.map(n => node.elements[n]) || []
    elements.forEach((el) => {
      const nestedField = el?.component?.[el.component.field]
      walkFields(nestedField || el, callback)
    })

    // Traverse dxpFormConfigurationElement
    const dxpElements = node.dxpFormConfigurationElement?.elements
    const dxpNodes = dxpElements?.['@nodes']?.map(n => dxpElements[n]) || []
    dxpNodes.forEach((el) => {
      const nestedField = el?.component?.[el.component.field]
      walkFields(nestedField || el, callback)
    })

    // Traverse generic nested objects (skip meta keys)
    for (const [key, value] of Object.entries(node)) {
      if (!key.startsWith('@') && typeof value === 'object') {
        walkFields(value, callback)
      }
    }
  }

  const extractFields = (node) => {
    const stepFields = []

    for (const key of Object.keys(node)) {
      if (key.startsWith('@') || key.startsWith('mgnl:') || !node[key]?.elements) {
        continue
      }

      const elements = node[key].elements

      for (const elementKey of Object.keys(elements)) {
        if (elementKey.startsWith('@')) { continue }
        const component = elements[elementKey]?.component
        if (!component) { continue }
        const selectedFieldType = component.field
        for (const componentKey of Object.keys(component)) {
          const candidate = component[componentKey]
          if (candidate?.['@nodeType'] !== 'mgnl:contentNode') { continue }

          if (selectedFieldType) {
            if (componentKey !== selectedFieldType) { continue }
            if (selectedFieldType === 'relatedCheckboxField' && componentKey === 'checkboxGroupField') { continue }
          }
          stepFields.push(candidate)
        }
      }
    }

    return stepFields
  }

  const extractSteps = (config) => {
    const fieldsContainer = config?.fields
    if (!fieldsContainer?.['@nodes']) { return [] }

    return fieldsContainer['@nodes']
      .map((fieldKey) => {
        const group = fieldsContainer[fieldKey]?.dxpFormConfigurationElement
        if (!group) { return null }

        const fields = extractFields(group)
        if (fields.length === 0) { return null }

        return {
          id: group['@name'],
          label: group.title || '',
          fields
        }
      })
      .filter(Boolean)
  }

  const extractHiddenFields = (fieldsArray) => {
    if (!Array.isArray(fieldsArray)) { return [] }

    const hiddenFields = []
    fieldsArray.forEach((field) => {
      walkFields(field, (f) => {
        if ([FORM_FIELD_TYPES.HIDDEN, FORM_FIELD_TYPES.DYNAMIC_HIDDEN_FIELD].includes(f['@name'])) {
          hiddenFields.push(f)
        }
      })
    })

    return hiddenFields
  }

  // INIT FUNCTIONS
  const spreadConfig = (configuration, fieldsName = 'fields') => {
    if (typeof configuration !== 'object') {
      return []
    }

    const fields = []
    const pushNestedFields = (arr) => {
      arr?.['@nodes']?.forEach((el) => {
        const component = arr?.[el]?.component
        const fieldType = component?.field
        const field = component[fieldType]
        fields.push(field)
      })
    }

    configuration?.[fieldsName]?.['@nodes'].forEach((f) => {
      const zeroLvl = configuration?.[fieldsName]?.[f]
      const firstLvl = zeroLvl?.dxpFormConfigurationElement?.elements

      if (!firstLvl) {
        zeroLvl?.dxpFormConfigurationElement?.['@nodes']?.forEach((el) => {
          const secondLvl = zeroLvl?.dxpFormConfigurationElement?.[el]?.elements
          pushNestedFields(secondLvl)
        })
      } else {
        pushNestedFields(firstLvl)
      }
    })

    return fields
  }

  const mapOptions = options =>
    options?.['@nodes']?.map(optionName => ({
      ...options[optionName],
      label: options[optionName]?.text
    }))

  const initFormField = (field, value, isValid, isFilled, name) => ({
    label: field?.label,
    value,
    name: name || field.name,
    fieldType: field['@name'],
    isMultipleValues:
      fieldsWithMultipleValues.includes(field['@name']) ||
      (field['@name'] === FORM_FIELD_TYPES.SELECT && field.multiple === 'true'),
    id: field?.id || null,
    isValid,
    isFilled,
    isHidden:
      field['@name'] === FORM_FIELD_TYPES.HIDDEN ||
      field['@name'] === FORM_FIELD_TYPES.DYNAMIC_HIDDEN_FIELD ||
      field['@name'] === FORM_FIELD_TYPES.QUERY,
    isAttachment: field['@name'] === FORM_FIELD_TYPES.UPLOAD_FIELD,
    outputType: field?.outputType || 'default'
  })

  const initFormFields = (fields) => {
    const formFields = {}

    fields
      ?.filter(f => !isCaptchaFieldType(f['@name']))
      ?.forEach((field) => {
        if (field.name) {
          const required = field.required === 'true'
          let value = field?.value || ''
          let isValid = true
          let isFilled = false
          const options = mapOptions(field.options)

          if (field['@name'] === FORM_FIELD_TYPES.RADIO) {
            const option =
              options?.find(
                o => o.selected === 'true' && o.disabled === 'false'
              ) ||
              options?.find(o => o.disabled === 'false') ||
              null
            value = option?.value || null
          } else if (
            field['@name'] === FORM_FIELD_TYPES.SELECT &&
            field.multiple === 'false'
          ) {
            value =
              options?.find(
                o => o.selected === 'true' && o.disabled === 'false'
              ) || null
          } else if (fieldsWithOptions.includes(field['@name'])) {
            value =
              options?.filter(
                o => o.selected === 'true' && o.disabled === 'false'
              ) || []
          } else if (field['@name'] === FORM_FIELD_TYPES.QUERY) {
            value = $cookies.get('Bobcat_ref') || ''
          } else if (field['@name'] === FORM_FIELD_TYPES.ADDRESS) {
            formFields[field.cityName] = initFormField(
              field,
              value,
              true,
              isFilled,
              field.cityName
            )
            formFields[field.zipName] = initFormField(
              field,
              value,
              true,
              isFilled,
              field.zipName
            )
            formFields[field.countryName] = initFormField(
              field,
              value,
              true,
              isFilled,
              field.countryName
            )
            formFields[field.stateName] = initFormField(
              field,
              value,
              true,
              isFilled,
              field.stateName
            )
          } else if (field['@name'] === FORM_FIELD_TYPES.DYNAMIC_DROPDOWN) {
            formFields[field.childName] = initFormField(
              {...field, label: field.childLabel, id: field.childId},
              '',
              false,
              false,
              field.childName
            )
          }

          if (required && (!value || value?.length <= 0)) {
            isValid = false
          }

          if (
            isValid &&
            ((Array.isArray(value) && value?.length > 0) ||
              (typeof value === 'string' && value))
          ) {
            isFilled = true
          }

          if (field?.stateName && field['@name'] === FORM_FIELD_TYPES.STATES) {
            formFields[field.stateName] = {
              value,
              name: field.stateName,
              id: field?.stateId || null,
              isValid,
              isFilled
            }
          }

          formFields[field.name] = initFormField(
            field,
            value,
            isValid,
            isFilled
          )
        }
      })

    return {
      formFields,
      isFormValid: !Object.keys(formFields).find(
        el => !formFields[el].isValid
      )
    }
  }
  // END OF INIT FUNCTIONS

  // HANDLERS
  const handleFormChanges = (
    formFields,
    setPickedCountry,
    setFilledFieldsAmount,
    setFormFields,
    setIsFormValid,
    value,
    fieldName,
    isValid,
    id
  ) => {
    if (fieldName === 'country_code') { // country code from AddressAutocomplete
      setPickedCountry(value)
    }
    if (!formFields.value[fieldName]) {
      return
    }
    formFields.value[fieldName].value = value
    formFields.value[fieldName].isValid = isValid
    setIsFormValid(
      !Object.keys(formFields.value).find(el => !formFields.value[el].isValid)
    )

    if (formFields.value[fieldName].fieldType === 'statesListField') {
      setPickedCountry(value)
    }

    if (
      isValid &&
      ((Array.isArray(value) && value?.length > 0) ||
        (typeof value === 'string' && value) ||
        (formFields.value[fieldName].fieldType ===
          FORM_FIELD_TYPES.UPLOAD_FIELD &&
          value))
    ) {
      formFields.value[fieldName].isFilled = true
    } else {
      formFields.value[fieldName].isFilled = false
    }

    if (id) {
      formFields.value[fieldName].id = id
    }
    setFilledFieldsAmount(
      Object.keys(formFields.value).filter(
        el => formFields.value[el].isFilled && !formFields.value[el].isHidden
      )?.length
    )

    setFormFields(formFields.value)
  }

  const handleMapFormValues = (formFields, formSnapshot = null) => {
    for (const fieldName in formFields.value) {
      const fieldType = formFields.value[fieldName].fieldType
      if (formFields.value[fieldName].isMultipleValues) {
        formFields.value[fieldName].value =
          formFields.value[fieldName].value.join()
      }
      if (
        fieldType === FORM_FIELD_TYPES.NUMERIC &&
        !formFields.value[fieldName].value
      ) {
        formFields.value[fieldName].value = '0'
      }
      if (fieldType === FORM_FIELD_TYPES.PRODUCTS) {
        const productsFromProductGroup = []
        const productSelectorItems = []
        const fieldValue = formFields.value[fieldName]?.value

        if (Array.isArray(fieldValue)) {
          fieldValue.forEach((markedProduct) => {
            if (markedProduct?.products) {
              markedProduct.products.forEach(product =>
                productsFromProductGroup.push(product)
              )
            } else {
              productSelectorItems.push(markedProduct)
            }
          })
        }
        [...productSelectorItems, ...productsFromProductGroup].forEach(
          (selectedProduct) => {
            if (formFields.value[fieldName]?.useMapping === 'true') {
              formFields.value[selectedProduct.identifier] = {
                name: selectedProduct.name,
                mappingId: selectedProduct.identifier,
                label: selectedProduct?.label || selectedProduct.name,
                value:
                  formFields.value[fieldName]?.outputType === 'number'
                    ? 1
                    : '1'
              }
            } else {
              formFields.value[selectedProduct.identifier] = {
                name: selectedProduct.name,
                value: selectedProduct.identifier
              }
            }
          }
        )
        delete formFields.value[fieldName]
      } else if (
        fieldType === FORM_FIELD_TYPES.CHECKBOX_GROUP ||
        fieldType === FORM_FIELD_TYPES.PRODUCT_GROUPS ||
        (fieldType === FORM_FIELD_TYPES.SELECT &&
          formFields.value[fieldName].isMultipleValues)
      ) {
        if (formFields.value[fieldName].value) {
          formFields.value[fieldName].value.split(',').forEach((field) => {
            formFields.value[field] = {
              name: field,
              mappingId: field,
              label: field?.label || formFields.value[fieldName]?.label,
              value:
                formFields.value[fieldName]?.outputType === 'number' ? 1 : '1'
            }
          })
        }
        delete formFields.value[fieldName]
      } else if (fieldType === FORM_FIELD_TYPES.HIDDEN && formSnapshot) {
        // handle field value updates from outside vue (ex: programmatic updates by bots)
        if (!formFields.value[fieldName].value && formSnapshot.get(fieldName)) {
          formFields.value[fieldName].value = formSnapshot.get(fieldName)
        }
      }
      delete formFields.value[fieldName]?.fieldType
      delete formFields.value[fieldName]?.isValid
      delete formFields.value[fieldName]?.isFilled
      delete formFields.value[fieldName]?.isHidden
      delete formFields.value[fieldName]?.isMultipleValues
      delete formFields.value[fieldName]?.outputType
      const currentField = formFields.value[fieldName]
      if (currentField?.id) {
        formFields.value[fieldName] = {
          ...currentField,
          mappingId: currentField.id
        }
      }
      delete formFields.value[fieldName]?.id
    }
    return formFields.value
  }

  const handleEmailSentWithSuccess = (thankYouPage, formSent, form) => {
    if (process.client) {
      if (thankYouPage?.field === 'page') {
        const link = $utils.getLinkField(
          thankYouPage.link,
          $regions.current.language.name
        )
        window.location = link.url
      } else {
        const y =
          form.value.getBoundingClientRect().top +
          window.pageYOffset -
          document.querySelector('header[data-test-id="main-header"]')
            ?.clientHeight -
          1
        window.scrollTo({top: y, behavior: 'smooth'})
      }
    }
    formSent.value = true
    return formSent.value
  }

  const handlerResponse = (
    response,
    config,
    onError,
    thankYouPage,
    formSent,
    form,
    errorMessage
  ) => {
    if (
      parseInt(response?.status) === 201 ||
      parseInt(response?.status) === 200
    ) {
      const requestFields = JSON.parse(response?.config?.data)?.formFields || []
      const formSelections = config?.formSelectionsForGA4?.replace(/\s+/g, '')?.split(',')
      const selectedFields = []
      requestFields?.forEach((item) => {
        if (formSelections?.includes(item?.name?.replace(/\s+/g, ''))) {
          selectedFields.push(`${(item?.label || '')}[${item.name}]: ${item?.value}`)
        }
      })

      if (config?.formType?.toLowerCase()?.includes('newsletter')) {
        // for the specific forms
        $ga4?.events?.newsletter_signup_complete(event, selectedFields?.join(', '), config?.['@name'])
      } else {
        // for the generic forms
        $ga4?.events?.form_submission_complete(event, selectedFields?.join(', '), config?.['@name'])
      }

      formSent.value = handleEmailSentWithSuccess(thankYouPage, formSent, form)
    } else {
      const error = response?.data?.error
      const apiMsg = response.data?.errors?.[0] || error?.message
      onError && onError(response)
      errorMessage.value = `[Error ${error?.code}] ${error?.message}`
      // eslint-disable-next-line no-console
      console.debug('[form] Unable to send form')
      if (config?.formType?.toLowerCase()?.includes('newsletter')) {
        // for the specific forms
        $ga4?.events?.newsletter_signup_failure(event, apiMsg, config?.['@name'])
      } else {
        // for the generic forms
        $ga4?.events?.form_submission_failure(event, apiMsg, config?.['@name'])
      }
    }
    return {formSent: formSent.value, errorMessage: errorMessage.value}
  }

  const isNumber = v => v?.replaceAll('\\', '') === '^[0-9]*$'
  const isPhoneNumber = v => v?.replaceAll('\\', '') === '^[+]?[(]?[0-9]{3}[)]?[-s.]?[0-9]{3}[-s.]?[0-9]{4,6}$'
  const isUnitedStatesZip = v => v?.replaceAll('\\', '') === '^d{5}(?:[-s]d{4})?$'
  const isNARegion = computed(() => BUSINESS_REGIONS.na.includes($regions.current.name))
  // END  OF HANDLERS

  const countryRestriction = computed(() => {
    if (isNARegion.value) { return [COUNTRY_CODES.Canada, COUNTRY_CODES.UnitedStates] } else { return null }
  })

  return {
    mapOptions,
    initFormFields,
    spreadConfig,
    handleFormChanges,
    handleMapFormValues,
    handlerResponse,
    isCaptchaFieldType,
    isNumber,
    isPhoneNumber,
    isUnitedStatesZip,
    countryRestriction,
    extractSteps,
    extractHiddenFields
  }
}
