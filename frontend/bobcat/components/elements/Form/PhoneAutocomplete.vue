<template>
  <b-form-group data-test-id="phone-autocomplete">
    <label v-if="label" :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]">
      {{ label }}<span v-if="required"> *</span>
    </label>
    <client-only>
      <VuePhoneNumberInput v-model="text"
                           :placeholder="placeholder"
                           size="lg"
                           error-color="#992100"
                           :required="required"
                           clearable
                           valid-color="#6c757d"
                           :translations="translations"
                           color="#6c757d"
                           input-mode="tel"
                           type="tel"
                           :error="!isValidated"
                           :border-radius="2"
                           @update="handleInputChange"
                           @phone-number-blur="handleBlur"
      >
        <template #arrow>
          <b-icon icon="sort-down" class="form-arr mr-2 text-black" />
        </template>
      </VuePhoneNumberInput>
    </client-only>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref, watch, onMounted} from '@nuxtjs/composition-api'

export default {
  components: {
    VuePhoneNumberInput: () => import('vue-phone-number-input')
  },
  props: {
    label: {
      type: String
    },
    countrySelectorLabel: {
      type: String
    },
    exampleLabel: {
      type: String
    },
    countrySelectorError: {
      type: String
    },
    placeholder: {
      type: String
    },
    validationMessage: {
      type: String
    },
    name: {
      type: String,
      required: true
    },
    required: {
      type: Boolean,
      default: false
    },
    bold: {
      type: Boolean,
      default: false
    },
    onChange: {
      type: Function,
      required: true
    }
  },
  setup({onChange, name, required, placeholder, countrySelectorLabel, countrySelectorError, exampleLabel}) {
    const isTouched = ref(false)
    const text = ref('')
    const isValidated = ref(true)
    const currentValidationStatus = ref(true)

    const translations = ref({
      countrySelectorLabel: countrySelectorLabel || '',
      countrySelectorError: countrySelectorError || '',
      phoneNumberLabel: placeholder || '',
      example: exampleLabel || ''
    })

    onMounted(() => import('vue-phone-number-input/dist/vue-phone-number-input.css'))

    watch(isTouched, (newVal) => {
      if (newVal && required && !text.value) { isValidated.value = false }
    })

    const handleInputChange = (v) => {
      currentValidationStatus.value = v.isValid
      if (isTouched.value) {
        isValidated.value = v.isValid
      }
      onChange(v?.formattedNumber || v.phoneNumber, name, isValidated.value)
    }

    const handleBlur = () => {
      isTouched.value = true
      isValidated.value = currentValidationStatus.value
      if (!text.value) { isValidated.value = false }
    }

    return {
      isTouched,
      isValidated,
      handleInputChange,
      handleBlur,
      text,
      translations
    }
  }
}
</script>
