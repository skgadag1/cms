<template>
  <section data-test-id="location-modal">
    <client-only>
      <b-modal
        ref="modal"
        :title="title"
        :cancel-title="cancelLabel"
        :ok-title="searchLabel"
        cancel-variant="outline-primary"
        size="md"
        :ok-disabled="!address"
        @ok="saveLocationModalAction"
      >
        <p v-html="description" />
        <AddressAutocompleteSimple
          id="location-modal-autocomplete"
          :key="address?.fullAddress"
          :api-key="apiKey"
          :init-value="address?.fullAddress"
          icon="pin"
          :placeholder="locationPlaceholder"
          :label="locationLabel"
          :autodetect-label="autoDetectLabel"
          :validation-message="validationMessage"
          @place-changed="handleAddressSelected"
          @text-changed="handleAddressTextChanged"
        />
        <div>
          <label>{{ distanceLabel }}</label>
          <VueSlider v-model="locationRange" :min="minimum" :max="maximum" :interval="skipInterval" drag-on-click />
          <div v-if="steps && steps.length" class="d-flex justify-content-between">
            <span v-for="(step, i) in steps" :key="`${step}_${i}`" :class="{ 'mr-2': i === steps.length }">{{
              step
            }} {{ metric }}</span>
          </div>
        </div>
      </b-modal>
    </client-only>
  </section>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import config from '~/constants/props/form.config'
import DISTANCE_METRICS from '~/constants/distance'
import {COUNTRY_CODES} from '~/constants/countries'
import {LOCATION_KEYS} from '~/constants'
import useLocation from '~/helpers/composables/useLocation'
import useMounted from '~/helpers/composables/useMounted'
import {inMgnlIframe, inEditorEdit, inMgnlPreview} from '~/helpers/vue-editor'
import useQueryParams from '@/helpers/composables/useQueryParams'

const {
  title,
  description,
  min,
  max,
  interval,
  apiKey,
  locationPlaceholder,
  locationLabel,
  autoDetectLabel,
  validationMessage,
  distanceLabel
} = config

export default {
  components: {
    VueSlider: () => import('vue-slider-component')
  },
  props: {
    title,
    description,
    min,
    max,
    interval,
    apiKey,
    locationPlaceholder,
    locationLabel,
    autoDetectLabel,
    validationMessage,
    distanceLabel,
    cancelLabel: String,
    searchLabel: String,
    nodeId: String,
    hideModalInMagnoliaPreview: String
  },
  setup(props) {
    const modal = ref(null)
    const address = ref(null)
    const {min, max, interval} = props
    const {queryParams} = useQueryParams()

    const minimum = ref(Number(min))
    const maximum = ref(Number(max))
    const skipInterval = ref(Number(interval))

    const isValidSliderInterval =
      Number.isFinite(minimum.value) &&
      Number.isFinite(maximum.value) &&
      maximum.value > minimum.value &&
      Number.isFinite(skipInterval.value) &&
      skipInterval.value > 0 &&
      Number.isInteger((maximum.value - minimum.value) / skipInterval.value)

    if (!isValidSliderInterval) {
      skipInterval.value = 1
    }

    const locationRange = ref(minimum.value)

    const steps = ref([])
    const metric = ref(DISTANCE_METRICS.MI)
    const NUMBER_OF_STEPS = 4

    const skipStep = maximum.value / skipInterval.value <= NUMBER_OF_STEPS
      ? skipInterval.value
      : Math.round(maximum.value / NUMBER_OF_STEPS)

    for (let x = minimum.value; x <= maximum.value; x += skipStep) {
      if (x + skipStep > maximum.value) {
        steps.value.push(maximum.value)
      } else {
        steps.value.push(x)
      }
    }

    watch(address, () => {
      if (address.value?.country?.toUpperCase() === COUNTRY_CODES.UnitedStates) {
        metric.value = DISTANCE_METRICS.MI
      } else {
        metric.value = DISTANCE_METRICS.KM
      }
    })

    const handleAddressSelected = (fullAddress, country, zip) => {
      address.value = {
        fullAddress,
        country,
        zip
      }
    }

    const handleAddressTextChanged = (fullAddress) => {
      if (!fullAddress) {
        address.value = null
      }
    }

    const {saveLocation, getLocationFromStore, notify} = useLocation()
    const saveLocationModalAction = () => {
      saveLocation(LOCATION_KEYS.COUNTRY_CODE, address.value.country)
      saveLocation(LOCATION_KEYS.ADDRESS, address.value.fullAddress)
      saveLocation(LOCATION_KEYS.ZIP_CODE, address.value.zip)
      saveLocation(LOCATION_KEYS.RANGE, locationRange.value)

      notify()
    }

    const shouldModalAppear = () => {
      if (props.hideModalInMagnoliaPreview === 'true' &&
        (
          inMgnlIframe || inEditorEdit || inMgnlPreview
        )
      ) { return false }

      const storedLocation = getLocationFromStore()
      const locationFromQuery = queryParams.value?.location
      return !storedLocation.address && !locationFromQuery
    }

    watch(modal, () => {
      if (modal.value && shouldModalAppear()) {
        modal.value.show()
      }
    })

    useMounted(props.nodeId)

    return {
      handleAddressSelected,
      handleAddressTextChanged,
      modal,
      skipInterval,
      locationRange,
      minimum,
      maximum,
      steps,
      metric,
      saveLocationModalAction,
      address
    }
  }
}
</script>

<style lang="scss">
section[data-test-id='location-modal'] {
  iframe {
    width: 100%;
  }
}
</style>
