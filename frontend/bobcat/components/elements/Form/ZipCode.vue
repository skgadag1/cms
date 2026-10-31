<template>
  <b-form-group data-test-id="zip-code-input">
    <div class="d-flex align-items-center justify-content-between">
      <label v-if="label"
             :class="[!isValidated && 'is-invalid', {'font-weight-bold': bold}]"
      >
        <LazyHydrate never><span>{{ label }}</span></LazyHydrate>
        <LazyHydrate never><span v-if="required" v-html="requiredSign" /></LazyHydrate>
      </label>
      <button :disabled="disabled || isLoading" class="border-0 bg-transparent d-flex align-items-center p-0 mb-2 ml-2" :class="disabled ? 'text-gray-400' : 'text-primary'" @click.prevent="getCurrentLocation">
        <b-icon v-if="!isLoading" icon="current-location" class="mr-1" />
        <div v-else class="text-center">
          <b-spinner small variant="primary" class="mr-1" />
        </div>
        <LazyHydrate never><span>{{ autoDetect }}</span></LazyHydrate>
      </button>
    </div>
    <b-input-group class="position-relative">
      <b-form-input
        :id="id"
        ref="bInput"
        v-model="text"
        :disabled="disabled || isLoading"
        :name="name"
        :required="required"
        trim
        class="form-control px-3 py-4 border-color-gray-600"
        :placeholder="placeholder"
        :class="[isValidated ? (isTouched ? 'is-valid' : '') : 'is-invalid', {'px-6' : icon}, disabled ? 'text-gray-400 border-gray-light' : 'bg-white']"
        @blur.prevent="isTouched = true"
        @keydown.enter.prevent="handleEnterKey"
      />
      <span v-if="icon" class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3">
        <b-icon :icon="icon" class="text-black" />
      </span>
    </b-input-group>
    <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
      <LazyHydrate never><span>{{ validationMessage }}</span></LazyHydrate>
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {ref, watch, onMounted} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'
import {caValidator, usValidator} from '~/constants/validators'
const {label, name, onChange, onEnter, required, validationMessage, id, icon, placeholder, minlength, maxlength, bold, apiKey, reset, autoDetect, disabled, initValue, requiredSign} = config

export default {
  props: {
    label, name, onChange, onEnter, required, validationMessage, id, icon, placeholder, minlength, maxlength, bold, apiKey, reset, autoDetect, disabled, initValue, requiredSign
  },
  setup(props) {
    const text = ref('')
    const {maxlength, minlength, onChange, onEnter, name, required, apiKey} = props
    const usValidaton = new RegExp(usValidator)
    const caValidaton = new RegExp(caValidator)
    const isTouched = ref(false)
    const isValidated = ref(true)
    const isLoading = ref(false)
    const bInput = ref(null)

    const checkValidation = (newVal) => {
      const passMaxLengthCondition = maxlength || !maxlength <= 0 ? newVal?.length <= maxlength : true
      const passMinLengthCondition = minlength ? newVal?.length >= minlength : true
      const passRequiredCondition = !!((required && newVal) || !required)
      const passRegexCondition = usValidaton.test(newVal) || caValidaton.test(newVal)
      return required ? (passMaxLengthCondition && passMinLengthCondition && passRegexCondition && passRequiredCondition) : passRequiredCondition
    }

    watch(isTouched, (newVal) => {
      if (newVal) {
        isValidated.value = checkValidation(text.value)
      }
    })

    watch(() => props.reset, (newVal) => {
      if (newVal) {
        text.value = ''
        isTouched.value = false
      }
    })

    watch(text, (newVal) => {
      if (onChange) {
        const isValid = checkValidation(newVal)
        if (isTouched.value) { isValidated.value = isValid }
        onChange(newVal, name, isValid)
      }
    })

    watch(() => props.initValue, (v) => {
      if (v) {
        text.value = v
        isTouched.value = true
      }
    })

    const getCurrentLocation = () => {
      if (navigator?.geolocation && window?.google) {
        isLoading.value = true
        navigator.geolocation.getCurrentPosition(async (position, error) => {
          if (error) {
            // eslint-disable-next-line no-console
            console.error(error)
            isLoading.value = false
            return
          }
          const latlng = new window.google.maps.LatLng(position.coords.latitude, position.coords.longitude)
          const geocoder = new window.google.maps.Geocoder()

          await geocoder.geocode({latLng: latlng}, (results, status) => {
            // This is checking to see if the Geoeode Status is OK before proceeding
            if (status === window.google.maps.GeocoderStatus.OK) {
              const zipcode = results[0].address_components.find(ac => ac.types.find(type => type === 'postal_code'))?.long_name
              text.value = zipcode
              bInput.value?.focus()
            }
            isLoading.value = false
          })
        }, () => {
          isLoading.value = false
        })
      }
    }

    const handleEnterKey = () => {
      onEnter()
    }

    onMounted(() => {
      if (process.client && apiKey) {
        const scriptExists = document.querySelector(`script[src="https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places"]`)
        if (!scriptExists) {
          const scriptElement = document.createElement('script')
          scriptElement.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places`
          scriptElement.async = true
          document.head.appendChild(scriptElement)
        }
      }
    })

    return {text, isTouched, isValidated, getCurrentLocation, isLoading, bInput, handleEnterKey}
  }
}
</script>
