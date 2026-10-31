<template>
  <b-form-group data-test-id="text-input">
    <label v-if="label"
           :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]"
    >
      {{ label }}<span v-if="required"> *</span>
    </label>
    <b-input-group class="position-relative">
      <b-form-input
        :id="id"
        v-model="text"
        :name="name"
        :maxlength="maxlength"
        :input-mode="inputMode"
        :type="inputType"
        :minlength="minlength"
        :required="required"
        trim
        class="form-control px-3 py-4 bg-white border border-color-gray-600"
        :placeholder="placeholder"
        :class="[isValidated ? (isTouched ? 'is-valid' : '') : 'is-invalid', {'px-6' : icon}]"
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
import {onMounted, ref, watch} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'
import useForms from '~/helpers/composables/useForms'

const {
  label,
  name,
  onChange,
  required,
  validationMessage,
  id,
  icon,
  placeholder,
  minlength,
  maxlength,
  validation,
  bold,
  value
} = config

export default {
  props: {
    label,
    name,
    onChange,
    required,
    validationMessage,
    id,
    icon,
    placeholder,
    minlength,
    maxlength,
    validation,
    bold,
    value
  },
  setup(props) {
    const text = ref('')
    const {validation, maxlength, minlength, onChange, name, required} = props
    const {isNumber, isPhoneNumber, isUnitedStatesZip} = useForms()
    const regexExpression = (validation === 'none' || !validation) ? null : new RegExp(validation)
    const isTouched = ref(false)
    const isValidated = ref(true)
    const inputType = ref('text')
    const inputMode = ref('none')

    const defineTypeAndMode = () => {
      if (isUnitedStatesZip(validation)) {
        inputType.value = 'number'
        inputMode.value = 'numeric'
      } else if (isNumber(validation) || isPhoneNumber(validation)) {
        inputType.value = 'tel'
        inputMode.value = 'tel'
      }
    }

    defineTypeAndMode()

    const checkValidation = (newVal) => {
      if (required && !newVal) {
        return false
      }

      if (!required && !newVal) {
        return true
      }

      let passMaxLengthCondition = true
      let passMinLengthCondition = true
      const passRegexCondition = regexExpression ? regexExpression.test(newVal) : true

      if (minlength) {
        passMinLengthCondition = newVal.length >= minlength
      }

      if (maxlength) {
        passMaxLengthCondition = newVal.length <= maxlength
      }

      return passMaxLengthCondition && passMinLengthCondition && passRegexCondition
    }

    watch(isTouched, (newVal) => {
      if (newVal) {
        isValidated.value = checkValidation(text.value)
      }
    })

    watch(text, (newVal) => {
      if (onChange) {
        const isValid = checkValidation(newVal)
        if (isTouched.value) {
          isValidated.value = isValid
        }
        onChange(newVal, name, isValid)
      }
    })

    watch(() => props.value, (newVal) => {
      if (newVal) {
        isTouched.value = true
        text.value = newVal
      }
    })

    onMounted(() => {
      if (props.value) {
        isTouched.value = true
        text.value = props.value
      }
    })

    return {text, isTouched, isValidated, inputType, inputMode}
  }
}
</script>
