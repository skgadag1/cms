<template>
  <b-row v-if="field">
    <b-col :md="isHalfWidth ? 12 : 6" cols="12">
      <Dropdown
        :label="field.label"
        :name="field.name"
        :options="primaryDropdownOptions"
        :placeholder="field.placeholder"
        :required="required"
        :on-change="handlePrimaryDropdownChange"
        :bold="bold"
      />
    </b-col>
    <b-col :md="isHalfWidth ? 12 : 6" cols="12">
      <Dropdown
        :disabled-dropdown="!selectedPrimaryOption"
        :label="field.childLabel"
        :name="field.childName"
        :options="dependentDropdownOptions"
        :placeholder="field.childPlaceholder"
        :required="required"
        :on-change="handleDependentDropdownChange"
        related-dropdowns
        :bold="bold"
      />
    </b-col>
  </b-row>
</template>

<script>
import {computed, ref, watch} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {
  onChange,
  field,
  bold,
  required
} = config

export default {
  props: {
    field,
    onChange,
    bold,
    required
  },
  setup(props) {
    const {field, onChange} = props
    const selectedPrimaryOption = ref(null)
    const selectedDependentOption = ref(null)
    const primaryDropdownOptions = ref([])
    const dependentDropdownOptions = ref([])
    const isHalfWidth = computed(() => field.width?.field === 'halfWidth')

    const initializePrimaryDropdownOptions = () => {
      if (!field?.dropdownValues) {
        return []
      }

      const dropdownValues = field.dropdownValues['@nodes'].map(name => ({
        label: field.dropdownValues[name]?.text || '',
        disabled: 'false',
        ...field.dropdownValues[name]
      }))

      selectedPrimaryOption.value = dropdownValues.find(option => option.selected === 'true') || null
      return dropdownValues
    }

    const initializeDependentDropdownOptions = (selectedOption) => {
      if (!selectedOption?.dxpFormConfigurationElement) {
        return []
      }

      const childField = selectedOption.dxpFormConfigurationElement.elements?.elements0?.component?.dynamicDropdownChildField
      if (!childField?.dropdownValues) {
        return []
      }

      return childField.dropdownValues['@nodes'].map(name => ({
        label: childField.dropdownValues[name]?.text || '',
        disabled: 'false',
        ...childField.dropdownValues[name]
      }))
    }

    primaryDropdownOptions.value = initializePrimaryDropdownOptions()
    dependentDropdownOptions.value = initializeDependentDropdownOptions(selectedPrimaryOption.value)

    const handlePrimaryDropdownChange = (value, name, isValid) => {
      selectedPrimaryOption.value = primaryDropdownOptions.value.find(option => option.value === value) || null
      dependentDropdownOptions.value = initializeDependentDropdownOptions(selectedPrimaryOption.value)

      onChange?.(selectedPrimaryOption.value?.value, name, isValid, field.id || selectedPrimaryOption.value?.value)
    }

    const handleDependentDropdownChange = (value, name, isValid) => {
      selectedDependentOption.value = dependentDropdownOptions.value.find(option => option.value === value) || null
      onChange?.(selectedDependentOption.value?.value, name, isValid, field.childId || selectedDependentOption.value?.value)
    }

    watch(selectedPrimaryOption, (newVal) => {
      dependentDropdownOptions.value = initializeDependentDropdownOptions(newVal)
    })

    return {
      isHalfWidth,
      selectedPrimaryOption,
      primaryDropdownOptions,
      dependentDropdownOptions,
      handlePrimaryDropdownChange,
      handleDependentDropdownChange
    }
  }
}
</script>
