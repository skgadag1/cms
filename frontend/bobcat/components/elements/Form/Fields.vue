<template>
  <b-row class="mb-2">
    <b-col v-for="field in fields" :key="field['@id']" :md="field.width && field.width.field === 'halfWidth' ? 6 : 12" cols="12">
      <InputText v-if="field['@name'] === FORM_FIELD_TYPES.INPUT"
                 :name="field.name"
                 :icon="(field.icon && field.icon !== 'none') ? field.icon : null"
                 :required="field.required === 'true'"
                 :minlength="field.minLength"
                 :maxlength="field.maxLength"
                 :label="field.label"
                 :placeholder="field.placeholder"
                 :validation-message="field.validationMessage"
                 :validation="field.validation"
                 :on-change="handleFormChange"
                 :bold="field.bold === 'true'"
                 :value="getCurrentFieldValue(field.name)"
      />
      <Textarea v-else-if="field['@name'] === FORM_FIELD_TYPES.AREA"
                :name="field.name"
                :icon="(field.icon && field.icon !== 'none') ? field.icon : null"
                :required="field.required === 'true'"
                :minlength="field.minLength"
                :maxlength="field.maxLength"
                :rows="field.rows"
                :label="field.label"
                :placeholder="field.placeholder"
                :validation-message="field.validationMessage"
                :validation="field.validation"
                :on-change="handleFormChange"
                :bold="field.bold === 'true'"
                :value="getCurrentFieldValue(field.name)"
      />
      <InputText v-else-if="field['@name'] === FORM_FIELD_TYPES.NUMERIC"
                 :name="field.name"
                 :icon="(field.icon && field.icon !== 'none') ? field.icon : null"
                 :required="field.required === 'true'"
                 :minlength="field.minLength"
                 :maxlength="field.maxLength"
                 :label="field.label"
                 :placeholder="field.placeholder"
                 :validation-message="field.validationMessage"
                 validation="^[0-9]*$"
                 :on-change="handleFormChange"
                 :bold="field.bold === 'true'"
                 :value="getCurrentFieldValue(field.name)"
      />
      <UploadField v-else-if="field['@name'] === FORM_FIELD_TYPES.UPLOAD_FIELD"
                   :name="field.name"
                   :field="field"
                   :required="field.required === 'true'"
                   :label="field.label"
                   :placeholder="field.placeholder"
                   :validation-message="field.validationMessage"
                   :on-change="handleFormChange"
                   :bold="field.bold === 'true'"
      />
      <AddressAutocomplete v-else-if="field['@name'] === FORM_FIELD_TYPES.ADDRESS"
                           :name="field.name"
                           :required="field.required === 'true'"
                           :label="field.label"
                           :field="field"
                           :placeholder="field.placeholder"
                           :api-key="field.apiKey"
                           :width="field?.addressWidth?.field"
                           :validation-message="field.validationMessage"
                           :on-change="handleFormChange"
                           :bold="field.bold === 'true'"
      />
      <PhoneAutocomplete
        v-else-if="field['@name'] === FORM_FIELD_TYPES.PHONE"
        :bold="field.bold === 'true'"
        :name="field.name"
        :required="field.required === 'true'"
        :label="field.label"
        :placeholder="field.placeholder"
        :validation-message="field.validationMessage"
        :on-change="handleFormChange"
        :country-selector-label="field.countrySelectorLabel"
        :country-selector-error="field.countrySelectorError"
        :example-label="field.exampleLabel"
      />
      <Radio v-else-if="field['@name'] === FORM_FIELD_TYPES.RADIO"
             :bold="field.bold === 'true'"
             :on-change="handleFormChange"
             :name="field.name"
             :label="field.label"
             :options="mapOptions(field.options)"
      />
      <CheckboxGroup v-else-if="field['@name'] === FORM_FIELD_TYPES.CHECKBOX_GROUP"
                     :required="field.required === 'true'"
                     :validation-message="field.validationMessage"
                     :on-change="handleFormChange"
                     :name="field.name"
                     :options="mapOptions(field.options)"
                     :label="field.label"
                     :bold="field.bold === 'true'"
      />
      <Dropdown v-else-if="field['@name'] === FORM_FIELD_TYPES.SELECT && field.multiple === 'false'"
                :icon="(field.icon && field.icon !== 'none') ? field.icon : null"
                :validation-message="field.validationMessage"
                :label="field.label"
                :name="field.name"
                :no-results-msg="field.noResultsMsg"
                :placeholder="field.placeholder"
                :options="mapOptions(field.options)"
                :required="field.required === 'true'"
                :on-change="handleFormChange"
                :bold="field.bold === 'true'"
      />
      <MultiSelect v-else-if="field['@name'] === FORM_FIELD_TYPES.SELECT && field.multiple === 'true'"
                   :icon="(field.icon && field.icon !== 'none') ? field.icon : null"
                   :name="field.name"
                   :placeholder="field.placeholder"
                   :validation-message="field.validationMessage"
                   :label="field.label"
                   :options="mapOptions(field.options)"
                   :required="field.required === 'true'"
                   :on-change="handleFormChange"
                   :bold="field.bold === 'true'"
                   :clear-label="field.clearLabel"
                   :selected-label="field.selectedLabel"
      />
      <ProductSelector v-else-if="field['@name'] === FORM_FIELD_TYPES.PRODUCTS"
                       :name="field.name"
                       :use-mapping="field.useMapping === 'true'"
                       :validation-message="field.validationMessage"
                       :label="field.label"
                       :categories-label="field.categoriesLabel"
                       :subcategories-label="field.subcategoriesLabel"
                       :product-groups-label="field.productGroupsLabel"
                       :products-label="field.productsLabel"
                       :clear-all-label="field.clearAllLabel"
                       :placeholder="field.placeholder"
                       :no-results-message="field.noResultsMessage"
                       :required="field.required === 'true'"
                       :on-change="handleFormChange"
                       :bold="field.bold === 'true'"
      />
      <ProductInterestSelector
        v-else-if="field['@name'] === FORM_FIELD_TYPES.PRODUCT_INTEREST_SELECTOR"
        :field="field"
        :on-change="handleFormChange"
      />
      <DealerLocatorProductInterest
        v-else-if="field['@name'] === FORM_FIELD_TYPES.PRODUCT_INTEREST"
        :field="field"
        :on-change="handleFormChange"
      />
      <ProductInterestSf
        v-else-if="field['@name'] === FORM_FIELD_TYPES.PRODUCT_INTEREST_SF"
        :field="field"
        :on-change="handleFormChange"
      />
      <StatesList
        v-else-if="field['@name'] === FORM_FIELD_TYPES.STATES"
        :validation-message="field.validationMessage"
        :no-results-msg="field.noResultsMsg"
        :name="field.name"
        :state-name="field.stateName"
        :required="field.required === 'true'"
        :on-change="handleFormChange"
        :countries-label="field.countriesLabel"
        :states-label="field.statesLabel"
        :type="field.type"
        :countries-placeholder="field.countriesPlaceholder"
        :states-placeholder="field.statesPlaceholder"
        :country-bold="field.countryBold === 'true'"
        :state-bold="field.countryBold === 'true'"
      />
      <RelatedFields
        v-else-if="field['@name'] === FORM_FIELD_TYPES.RELATED_FIELDS"
        :field="field"
        :on-change="handleFormChange"
        :init-related-fields="initRelatedFields"
        :remove-related-fields="removeRelatedFields"
        :bold="field.bold === 'true'"
      />
      <RelatedCheckbox
        v-else-if="field['@name'] === FORM_FIELD_TYPES.RELATED_CHECKBOX"
        :field="field"
        :on-change="handleFormChange"
        :bold="field.bold === 'true'"
        :picked-country="pickedCountry"
      />
      <DynamicForm
        v-else-if="field['@name'] === FORM_FIELD_TYPES.YES_NO_DYNAMIC_FORM"
        :field="field"
        :init-dynamic-forms="initDynamicForms"
        :on-change="handleFormChange"
        :bold="field.bold === 'true'"
        :required="field.required === 'true'"
        :init-related-fields="initRelatedFields"
        :remove-related-fields="removeRelatedFields"
        :picked-country="pickedCountry"
      />
      <DynamicDropdown
        v-else-if="field['@name'] === FORM_FIELD_TYPES.DYNAMIC_DROPDOWN"
        :field="field"
        :on-change="handleFormChange"
        :bold="field.bold === 'true'"
        :required="field.required === 'true'"
      />
      <DynamicHiddenField
        v-else-if="field['@name'] === FORM_FIELD_TYPES.DYNAMIC_HIDDEN_FIELD"
        :id="field.id"
        :name="field.name"
        :field="field"
        :on-change="handleFormChange"
      />
      <HiddenField
        v-else-if="field['@name'] === FORM_FIELD_TYPES.HIDDEN"
        :id="field.id"
        :label="field.label"
        :name="field.name"
        :value="field.value"
        :on-change="handleFormChange"
      />
      <ProductGroups v-else-if="field['@name'] === FORM_FIELD_TYPES.PRODUCT_GROUPS"
                     :field="field"
                     @change="handleFormChange"
      />
      <ProductGroupsDropdown v-else-if="field['@name'] === FORM_FIELD_TYPES.PRODUCT_GROUPS_DROPDOWN"
                             :field="field"
                             :on-change="handleFormChange"
      />
      <b-button
        v-else-if="field['@name'] === FORM_FIELD_TYPES.BROCHURE && field.filePath && field.field !== 'none'"
        data-test-id="brochure-download-button"
        variant="primary"
        size="lg"
        type="button"
        class="btn-outline-primary btn-secondary my-2"
        @click.prevent="download(field.filePath)"
      >
        {{ field.label || 'Download brochure' }}
      </b-button>
      <div
        v-else-if="field['@name'] === FORM_FIELD_TYPES.CUSTOM_TEXT && field.text"
        data-test-id="custom-text"
        v-html="field.text"
      />
      <PrivacyPolicy
        v-else-if="field['@name'] === FORM_FIELD_TYPES.PRIVACY_POLICY"
        :required="field.required === 'true'"
        :validation-message="field.validationMessage"
        :on-change="handleFormChange"
        :name="field.name"
        :text="field.text"
      />
      <TermsAndConditions
        v-else-if="field['@name'] === FORM_FIELD_TYPES.TERMS_AND_CONDITIONS"
        :required="field.required === 'true'"
        :validation-message="field.validationMessage"
        :on-change="handleFormChange"
        :name="field.name"
        :title="field.title"
        :text="field.text"
      />
    </b-col>
  </b-row>
</template>

<script>
import {useContext} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'
import useForms from '~/helpers/composables/useForms'
import FORM_FIELD_TYPES from '~/constants/formFieldTypes'

const {
  handleFormChange,
  fields,
  initRelatedFields,
  removeRelatedFields,
  pickedCountry,
  initDynamicForms,
  formFields
} = config

export default {
  props: {
    fields,
    handleFormChange,
    initRelatedFields,
    removeRelatedFields,
    pickedCountry,
    initDynamicForms,
    formFields
  },
  setup(props) {
    const {$utils} = useContext()
    const {mapOptions} = useForms()

    const download = (url, fileName = 'file') => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    const getCurrentFieldValue = (fieldName) => {
      return props.formFields?.[fieldName]?.value || ''
    }

    return {
      download,
      mapOptions,
      FORM_FIELD_TYPES,
      getCurrentFieldValue
    }
  }
}
</script>
