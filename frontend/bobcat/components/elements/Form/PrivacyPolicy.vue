<template>
  <b-form-group data-test-id="privacy-policy">
    <b-form-checkbox
      class="custom-checkbox z-index-0 ml-4"
      button-variant="secondary"
      @change="handleChange"
    >
      <div v-html="text" />
    </b-form-checkbox>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValid">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {text, name, onChange, required, validationMessage} = config

export default {
  props: {
    name,
    onChange,
    required,
    validationMessage,
    text
  },
  setup(props) {
    const {onChange, name, required} = props
    const isValid = ref(true)

    const handleChange = (value) => {
      const paredValue = value ? 1 : 0
      if (required) { isValid.value = paredValue === 1 }
      onChange(paredValue, name, isValid.value)
    }

    return {
      handleChange,
      isValid
    }
  }
}
</script>
