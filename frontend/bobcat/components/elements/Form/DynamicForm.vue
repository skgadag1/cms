<template>
  <b-form-group data-test-id="yes-no-dynamic-field"
                :class="{'font-weight-bold': bold}"
                :label="required ? `${field.label} *` : field.label"
  >
    <b-form-radio v-model="selected" class="ml-4" :value="field.yesValue">{{ field.yesLabel }}</b-form-radio>
    <b-form-radio v-model="selected"
                  class="ml-4"
                  :class="{'mb-2' : selectedFields && selectedFields.length}"
                  :value="field.noValue"
    >
      {{ field.noLabel }}
    </b-form-radio>
    <Fields
      v-if="selectedFields && selectedFields.length"
      :picked-country="pickedCountry"
      :init-related-fields="initRelatedFields"
      :remove-related-fields="removeRelatedFields"
      :init-dynamic-forms="initDynamicForms"
      :handle-form-change="onChange"
      :fields="selectedFields"
    />
  </b-form-group>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import useForms from '~/helpers/composables/useForms'
import config from '~/constants/props/form.config'

const {
  onChange,
  field,
  initRelatedFields,
  removeRelatedFields,
  pickedCountry,
  initDynamicForms,
  bold,
  required
} = config

export default {
  props: {
    field,
    onChange,
    initDynamicForms,
    bold,
    required,
    initRelatedFields,
    removeRelatedFields,
    pickedCountry
  },
  setup({field, onChange, initDynamicForms}) {
    const selected = ref('')
    const selectedFields = ref([])
    const {spreadConfig, initFormFields} = useForms()

    const noFields = spreadConfig(field, 'noFields')
    const yesFields = spreadConfig(field, 'yesFields')

    watch(selected, (newVal) => {
      const isYesVal = newVal === field.yesValue
      selectedFields.value = isYesVal ? yesFields : noFields
      onChange(newVal, field.name, true, isYesVal ? field?.yesId : field?.noId)
      const data = initFormFields(selectedFields.value)
      initDynamicForms(data.formFields, data.isFormValid, field.name)
    })

    return {
      selected,
      selectedFields
    }
  }
}
</script>
