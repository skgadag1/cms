<template>
  <b-form-group data-test-id="text-area">
    <label v-if="label"
           :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]"
    >
      {{ label }}<span v-if="required"> *</span>
    </label>
    <b-input-group class="position-relative">
      <b-form-textarea
        :id="id"
        v-model="text"
        :class="[isValidated ? (isTouched ? 'is-valid' : '') : 'is-invalid', {'px-6' : icon}]"
        :rows="rows"
        no-resize
        :name="name"
        :maxlength="maxlength"
        :minlength="minlength"
        :required="required"
        trim
        class="form-control px-3 py-3 bg-white border"
        :placeholder="placeholder"
        @blur.prevent="isTouched = true"
      />
      <span v-if="icon" class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3">
        <b-icon :icon="icon" class="text-black" />
      </span>
    </b-input-group>

    <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'

const {label, name, onChange, required, validationMessage, id, icon, placeholder, minlength, maxlength, validation, rows, bold} = config

export default {
  props: {
    label, name, onChange, required, validationMessage, id, icon, placeholder, minlength, maxlength, validation, rows, bold
  },
  setup(props) {
    const text = ref('')
    const {validation, maxlength, minlength, onChange, name, required} = props
    const regexExpression = (validation === 'none' || !validation) ? null : new RegExp(validation)
    const isTouched = ref(false)
    const isValidated = ref(true)

    const checkValidation = (newVal) => {
      const passMaxLengthCondition = maxlength || !maxlength <= 0 ? newVal?.length <= maxlength : true
      const passMinLengthCondition = minlength ? newVal?.length >= minlength : true
      const passRequiredCondition = !!((required && newVal) || !required)
      const passRegexCondition = regexExpression ? regexExpression.test(newVal) : true
      return required ? (passMaxLengthCondition && passMinLengthCondition && passRegexCondition && passRequiredCondition) : passRequiredCondition
    }

    watch(isTouched, (newVal) => {
      if (newVal) {
        isValidated.value = checkValidation(text.value)
      }
    })

    watch(text, (newVal) => {
      if (onChange) {
        const isValid = checkValidation(newVal)
        if (isTouched.value) { isValidated.value = isValid }
        onChange(newVal, name, isValid)
      }
    })

    return {
      text,
      isValidated,
      isTouched
    }
  }
}
</script>

<style lang="scss" scoped>
.input-icon {
  bottom: unset !important;
  transform: translateY(0) !important;
  top: 1.25rem;
}
</style>
