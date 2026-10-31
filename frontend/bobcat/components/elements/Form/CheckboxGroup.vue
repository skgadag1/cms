<template>
  <b-form-group v-if="options && options.length" data-test-id="checkbox-wrapper">
    <label v-if="label" :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]">{{ label }}<span v-if="required"> *</span></label>
    <b-form-checkbox-group
      v-model="selected"
      stacked
      :name="name"
      @change="handleChange"
    >
      <b-form-checkbox
        v-for="o in options"
        :key="`check-option__${o['@id']}`"
        :value="o.value"
        :disabled="o.disabled === 'true'"
        class="custom-checkbox z-index-0"
        :class="{'ml-4' : label}"
        button-variant="secondary"
      >
        {{ o.label }}
      </b-form-checkbox>
      <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
        {{ validationMessage }}
      </b-form-invalid-feedback>
    </b-form-checkbox-group>
  </b-form-group>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {label, name, options, onChange, required, validationMessage, bold} = config

export default {
  props: {
    label, name, options, onChange, required, validationMessage, bold
  },
  setup(props) {
    const {onChange, name, options, required} = props
    const selected = ref(options?.filter(o => o.selected === 'true' && o.disabled === 'false')?.map(o => o.value) || [])
    const isValidated = ref(true)

    if (onChange && selected?.value) {
      const isValid = required ? selected.value.length > 0 : true
      onChange(selected.value, name, isValid)
    }

    const handleChange = (val) => {
      if (onChange) {
        const isValid = required ? val.length > 0 : true
        isValidated.value = isValid
        onChange(val, name, isValid)
      }
    }

    return {
      selected,
      handleChange,
      isValidated
    }
  }
}
</script>
