<template>
  <b-row v-if="countriesList && countriesList.length">
    <b-col md="6" cols="12">
      <Dropdown
        :validation-message="validationMessage"
        :label="countriesLabel"
        :name="name"
        :options="countriesList"
        :placeholder="countriesPlaceholder"
        :required="required"
        :no-results-msg="noResultsMsg"
        :on-change="handleCountryChange"
        :bold="countryBold"
      />
    </b-col>
    <b-col md="6" cols="12">
      <Dropdown
        :disabled-dropdown="!selectedCountry"
        :validation-message="validationMessage"
        :label="statesLabel"
        :name="stateName"
        :no-results-msg="noResultsMsg"
        :options="selectedCountry.states"
        :placeholder="statesPlaceholder"
        :required="required"
        :on-change="handleStateChange"
        related-dropdowns
        :bold="stateBold"
      />
    </b-col>
  </b-row>
</template>

<script>
import {onMounted, ref} from '@nuxtjs/composition-api'
import useCountries from '~/helpers/composables/useCountries'
import config from '~/constants/props/form.config'

const {statesLabel, name, type, stateName, stateBold, countryBold, onChange, required, countriesPlaceholder, statesPlaceholder, validationMessage, countriesLabel, noResultsMsg} = config

export default {
  props: {
    validationMessage,
    countriesLabel,
    statesLabel,
    countriesPlaceholder,
    statesPlaceholder,
    name,
    type,
    stateName,
    stateBold,
    countryBold,
    required,
    onChange,
    noResultsMsg
  },
  setup(props) {
    const {onChange, type} = props
    const countriesList = ref([])
    const selectedCountry = ref('')
    const {getCountries} = useCountries()

    onMounted(async () => {
      countriesList.value = await getCountries(type)
    })

    const handleCountryChange = (v, n, isValid) => {
      selectedCountry.value = countriesList.value.find(country => country.value === v)
      if (onChange) {
        onChange(selectedCountry.value.label, n, isValid)
      }
    }

    const handleStateChange = (v, n, isValid) => {
      if (onChange) {
        const value = v ? selectedCountry.value.states.find(state => state.value === v).label : null
        onChange(value, n, isValid)
      }
    }

    return {
      countriesList,
      selectedCountry,
      handleCountryChange,
      handleStateChange
    }
  }
}
</script>
