<template>
  <b-form-group v-if="scriptsLoaded && apiKey" data-test-id="address-autocomplete">
    <div class="d-flex align-items-center mb-2 justify-content-between">
      <label v-if="label" class="mb-0" :class="{ 'is-invalid': !isValidated }">
        {{ label }}
      </label>
      <button :disabled="isDisabled"
              class="border-0 bg-transparent d-flex align-items-center p-0 mb-2 ml-2"
              :class="{ 'text-gray-400': isDisabled, 'text-primary': !isDisabled }"
              @click.prevent="getGPSLocation"
      >
        <b-icon v-if="!isLoading" icon="current-location" class="mr-1" />
        <span v-else class="text-center">
          <b-spinner small variant="primary" class="mr-1" />
        </span>
        <span>{{ autodetectLabel }}</span>
      </button>
    </div>
    <client-only>
      <b-input-group class="position-relative">
        <VueGoogleAutocomplete :id="id"
                               ref="autocomplete"
                               :disabled="disabled"
                               enable-geolocation
                               :classname="inputClass"
                               types=""
                               :placeholder="placeholder"
                               :country="countryRestriction"
                               :fields="autocompleteFields"
                               @placechanged="handlePlaceChanged"
                               @inputChange="handleInputChange"
        />
        <span v-if="icon"
              class="position-absolute input-icon d-flex align-items-center justify-content-center z-index-3"
        >
          <b-icon :icon="icon" :class="isValidated ? 'text-black' : 'text-danger'" />
        </span>
      </b-input-group>
    </client-only>
    <b-form-invalid-feedback v-if="!isValidated">
      {{ validationMessage }}
    </b-form-invalid-feedback>
  </b-form-group>
</template>

<script>
import {computed, onMounted, ref} from '@nuxtjs/composition-api'
import useGoogle from '~/helpers/composables/useGoogle'
import useForms from '~/helpers/composables/useForms'

export default {
  components: {
    VueGoogleAutocomplete: () => import('vue-google-autocomplete')
  },
  props: {
    id: {
      type: String,
      required: true
    },
    apiKey: {
      type: String,
      required: true
    },
    disabled: {
      type: Boolean
    },
    label: {
      type: String
    },
    placeholder: {
      type: String
    },
    validationMessage: {
      type: String
    },
    autodetectLabel: {
      type: String
    },
    initValue: {
      type: String
    },
    icon: {
      type: String
    }
  },
  emits: ['place-changed', 'text-changed'],
  setup(props, {emit}) {
    const {countryRestriction} = useForms()
    const {loadGoogleMapApiScript, autocompleteFields} = useGoogle()

    const {initValue, apiKey, disabled} = props
    const address = ref(initValue)
    const scriptsLoaded = ref(false)
    const isLoading = ref(false)
    const isValidated = ref(true)
    const isTouched = ref(false)
    const text = ref('')
    const autocomplete = ref(null)

    const isDisabled = computed(() => disabled || isLoading.value)
    const inputClass = computed(() =>
      [
        'form-control pr-6 custom-icon-space py-4 border-color-gray-600',
        isValidated.value ? (isTouched.value ? 'is-valid' : '') : 'is-invalid'
      ].join(' ')
    )

    const validateAddress = (value) => {
      isValidated.value = Boolean(value && value.trim())
    }

    const setInitValue = (value) => {
      if (value && autocomplete.value) {
        address.value = value || ''
        validateAddress(value)
        autocomplete.value.update(value)
      }
    }

    const handleBlur = () => {
      isTouched.value = true
      if (text.value) {
        isValidated.value = false
      }
    }

    const changeAddress = (formattedAddress) => {
      address.value = formattedAddress
      validateAddress(formattedAddress)
      isTouched.value = true
    }

    const parseGeocodeResults = (results, status) => {
      if (status === 'OK' && results.length) {
        const result = results[0]
        const formattedAddress = result.formatted_address
        const countryCode = result.address_components.find(component =>
          component.types.includes('country')
        )?.short_name

        const zip = result.address_components.find(component =>
          component.types.includes('postal_code'))?.short_name

        changeAddress(formattedAddress)

        if (autocomplete.value) {
          autocomplete.value.update(formattedAddress)
        }

        emit('place-changed', formattedAddress, countryCode, zip)
      } else {
        validateAddress('')
      }

      isLoading.value = false
    }

    const parseAddress = (fullAddress) => {
      if (!window?.google?.maps) {
        return
      }
      const geocoder = new window.google.maps.Geocoder()
      geocoder.geocode({address: fullAddress}, parseGeocodeResults)
    }

    const getGPSLocation = () => {
      if (!window?.google?.maps) {
        return
      }

      if (!navigator.geolocation) {
        return
      }

      isLoading.value = true

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const {latitude, longitude} = position.coords
          const myLatlng = new window.google.maps.LatLng(latitude, longitude)
          const geocoder = new window.google.maps.Geocoder()

          geocoder.geocode({location: myLatlng}, parseGeocodeResults)
        },
        () => {
          isLoading.value = false
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0
        }
      )
    }

    const handleInputChange = ({newVal}) => {
      text.value = newVal
      if (!newVal) {
        isValidated.value = false
      }
      emit('text-changed', text.value)
    }

    const handlePlaceChanged = (addressData, placeResultData) => {
      handleBlur()

      if (!placeResultData) {
        validateAddress('')
        return
      }

      const addressComponents = placeResultData.address_components

      const countryComponent = addressComponents.find(
        component => component.types.includes('country')
      )
      const countryCode = countryComponent?.short_name || ''
      const countryName = countryComponent?.long_name || ''

      const formattedAddress = placeResultData.formatted_address.replace(`, ${countryName}`, '')

      const zip = addressComponents.find(
        component => component.types.includes('postal_code')
      )?.short_name

      changeAddress(formattedAddress)

      emit('place-changed', formattedAddress, countryCode, zip)
    }

    onMounted(async () => {
      if (process.client && apiKey) {
        scriptsLoaded.value = await loadGoogleMapApiScript(apiKey)
        setTimeout(() => {
          setInitValue(props.initValue)
        }, 200)
      }
    })

    return {
      getGPSLocation,
      handlePlaceChanged,
      parseAddress,
      scriptsLoaded,
      isLoading,
      isValidated,
      isTouched,
      address,
      isDisabled,
      inputClass,
      handleInputChange,
      autocomplete,
      autocompleteFields,
      countryRestriction
    }
  }
}
</script>

<style lang="scss">
fieldset[data-test-id="address-autocomplete"] {
  .custom-icon-space {
    padding-left: 2.625rem;
  }
}
</style>
