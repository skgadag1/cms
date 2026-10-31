<template>
  <b-form-group data-test-id="related-checkbox">
    <label v-if="field.label" :class="{'font-weight-bold': bold}">{{ field.label }}</label>
    <b-form-checkbox
      v-model="status"
      class="custom-checkbox z-index-0"
      button-variant="secondary"
      value="1"
      unchecked-value=""
    >
      {{ field.buttonLabel }}
    </b-form-checkbox>
  </b-form-group>
</template>

<script>
import {ref, watch} from '@nuxtjs/composition-api'
import useCountries from '~/helpers/composables/useCountries'

export default {
  props: {
    field: {
      type: Object,
      required: true
    },
    onChange: {
      type: Function,
      required: true
    },
    bold: {
      type: Boolean,
      default: false
    },
    pickedCountry: {
      type: String
    }
  },
  setup(props) {
    const {field, onChange} = props
    const status = ref('0')
    const {countryLabel} = useCountries()

    watch(status, (newVal) => {
      onChange(newVal, field.name, true)
    })

    watch(() => props.pickedCountry, (newVal) => {
      if ([countryLabel.UNITED_STATES, 'US'].includes(newVal)) { status.value = '1' } else { status.value = '0' }
    })

    return {
      status
    }
  }
}
</script>
