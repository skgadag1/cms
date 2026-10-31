<template>
  <b-row class="mb-2">
    <b-col
      v-if="!isPlacedSearched"
      :md="width && width === 'halfWidth' ? 6 : 12"
      cols="12"
    >
      <b-form-group
        v-if="scriptsLoaded && apiKey"
        data-test-id="address-autocomplete"
      >
        <label
          v-if="label"
          :class="[!isValidated && 'is-invalid', { 'font-weight-bold': bold }]"
        >
          {{ label }}<span v-if="required"> *</span>
        </label>
        <client-only>
          <VueGoogleAutocomplete
            :id="name"
            :classname="[
              isValidated ? (isTouched ? 'is-valid' : '') : 'is-invalid',
              'form-control px-3 py-4',
            ]"
            :placeholder="placeholder"
            :country="countryRestriction"
            types=""
            :fields="autocompleteFields"
            @placechanged="handleChange"
            @inputChange="handleInputChange"
          />
        </client-only>
        <b-form-invalid-feedback v-if="validationMessage" :state="isValidated">
          {{ validationMessage }}
        </b-form-invalid-feedback>
        <button
          v-if="field && field.manualAddressLabel"
          class="bg-transparent border-0 mt-2 p-0 small w-100 d-flex justify-content-end"
          @click.prevent="isPlacedSearched = !isPlacedSearched"
        >
          {{ field.manualAddressLabel }}
        </button>
      </b-form-group>
    </b-col>
    <template v-else>
      <b-col :md="width && width === 'halfWidth' ? 6 : 12" cols="12">
        <InputText
          :name="name"
          :on-change="updateAddressField"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :value="address"
          :validation-message="validationMessage"
          :bold="bold"
        />
      </b-col>
      <b-col
        v-for="addressField in addressFields"
        :key="addressField.name"
        :md="addressField.width && addressField.width === 'halfWidth' ? 6 : 12"
        cols="12"
      >
        <Dropdown
          v-if="addressField.name === (field?.countryName || 'country') && isEMEARegion"
          :key="`country-dropdown-${addressField.value}`"
          :name="addressField.name"
          :on-change="updateAddressField"
          :required="addressField.required"
          :label="addressField.label"
          :placeholder="addressField.placeholder"
          :options="countryOptions"
          :init-value="getCountryDropdownValue(addressField.value)"
          :reset="false"
          :bold="addressField.bold"
          :validation-message="addressField.validationMessage"
        />
        <InputText
          v-else
          :name="addressField.name"
          :on-change="updateAddressField"
          :required="addressField.required"
          :label="addressField.label"
          :placeholder="addressField.placeholder"
          :value="addressField.value"
          :validation="addressField.validation"
          :validation-message="addressField.validationMessage"
          :bold="addressField.bold"
        />
      </b-col>
    </template>
  </b-row>
</template>

<script>
import {onBeforeUnmount, onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'
import useForms from '~/helpers/composables/useForms'
import {BUSINESS_REGIONS} from '~/constants'
import useLocalizedCountries from '~/helpers/composables/useLocalizedCountries'
import useGoogle from '~/helpers/composables/useGoogle'

export default {
  components: {
    VueGoogleAutocomplete: () => import('vue-google-autocomplete'),
    Dropdown: () => import('~/components/elements/Form/Dropdown.vue'),
    InputText: () => import('~/components/elements/Form/InputText.vue')
  },
  props: {
    label: {
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
    },
    field: {
      type: Object,
      required: true
    },
    apiKey: {
      type: String,
      required: true
    },
    width: {
      type: String,
      required: false
    }
  },
  setup({onChange, name, required, apiKey, field}) {
    const {countryRestriction} = useForms()
    const {getCountryCodeFromGoogle, autocompleteFields, loadGoogleMapApiScript} = useGoogle()
    const {$regions} = useContext()
    const {
      getLocalizedCountryOptions,
      getLocalizedCountryName,
      createLocalizedCountriesData
    } = useLocalizedCountries()
    const isTouched = ref(false)
    const text = ref('')
    const isValidated = ref(true)
    const address = ref('')
    const scriptsLoaded = ref(false)
    const isPlacedSearched = ref(false)
    const isNotNARegion = ref(
      !BUSINESS_REGIONS.na.includes($regions.current.name)
    )
    const isEMEARegion = ref(
      BUSINESS_REGIONS.emea.includes($regions.current.name)
    )
    let debounceTimer = null

    const getCurrentLanguage = () => {
      return $regions?.current?.language?.name || 'en'
    }

    const countryOptions = ref(
      getLocalizedCountryOptions(getCurrentLanguage(), false)
    )

    const getCountryDropdownValue = (countryName) => {
      if (!countryName) {
        return null
      }

      const currentLang = getCurrentLanguage()
      const localizedCountries = createLocalizedCountriesData()
      const country = localizedCountries.find(
        c =>
          c[currentLang]?.toLowerCase() === countryName.toLowerCase() ||
          c.en?.toLowerCase() === countryName.toLowerCase() ||
          c.native?.toLowerCase() === countryName.toLowerCase()
      )

      if (country) {
        const localizedName = getLocalizedCountryName(
          country.code,
          currentLang,
          false
        )
        return {label: localizedName, value: country.code}
      }

      return null
    }

    const initAddressField = baseKey => ({
      name: field[`${baseKey}Name`],
      id: field[`${baseKey}Id`],
      value: '',
      label: field[`${baseKey}Label`],
      placeholder: field[`${baseKey}Placeholder`],
      hidden: field[`${baseKey}Hidden`] === 'true',
      required: field[`${baseKey}Required`] === 'true',
      bold: field[`${baseKey}Bold`] === 'true',
      validation: field[`${baseKey}Validation`],
      validationMessage: field[`${baseKey}ValidationMessage`],
      width: field[`${baseKey}Width`]?.field || 'fullWidth'
    })

    const addressFields = ref({})
    const addressKeys = [
      field?.cityName || 'city',
      field?.stateName || 'state',
      field?.countryName || 'country',
      field?.zipName || 'zip'
    ]
    addressKeys.forEach((key) => {
      if (field[`${key}Hidden`] !== 'true') {
        addressFields.value[key] = initAddressField(key)
      }
    })

    onMounted(() => {
      if (process.client && apiKey) {
        loadGoogleMapApiScript(apiKey).then(() => {
          scriptsLoaded.value = true
        })
      }
    })

    onBeforeUnmount(() => {
      clearTimeout(debounceTimer)
    })

    watch(isTouched, (newVal) => {
      if (newVal && required && !text.value) {
        isValidated.value = false
      }
    })

    const handleInputChange = ({newVal}) => {
      text.value = newVal
      if (!newVal && required) {
        isValidated.value = false
      }
    }

    const handleBlur = () => {
      isTouched.value = true
      if (text.value) {
        isValidated.value = false
      }
    }

    const updateAddressField = async (val, n, isValid) => {
      if (typeof addressFields.value[n] !== 'undefined') {
        if (n === field?.countryName || n === 'country') {
          if (isEMEARegion.value) {
            // EMEA region: use dropdown with localized countries
            const currentLang = getCurrentLanguage()
            const localizedName = getLocalizedCountryName(val, currentLang, false)
            if (localizedName) {
              addressFields.value[n].value = localizedName
              onChange(val, 'country_code', true, 'countryCodeId')
              onChange(val, n, isValid, addressFields.value[n].id)
              return
            }
          } else {
            // NA/ALAO region: use text input with Google geocoding
            addressFields.value[n].value = val
            if (val?.length > 1) {
              clearTimeout(debounceTimer)
              debounceTimer = setTimeout(async () => {
                const countryCode = await getCountryCodeFromGoogle(val)
                if (countryCode) {
                  onChange(countryCode, n, isValid, addressFields.value[n].id)
                  onChange(countryCode, 'country_code', true, 'countryCodeId')
                }
              }, 500)
            } else {
              onChange(val, n, isValid, addressFields.value[n].id)
            }
            return
          }
        } else {
          addressFields.value[n].value = val
        }

        onChange(val, n, isValid, addressFields.value[n].id)
      } else if (n === name) {
        onChange(val, n, isValid, field?.id)
      }
    }

    const handleChange = (addressData, place) => {
      isPlacedSearched.value = true
      handleBlur()
      const getAddressComponent = (types) => {
        const component = place?.address_components?.find(c =>
          types.some(type => c.types?.includes(type))
        )
        return component?.long_name || ''
      }

      const getShortName = (types) => {
        const component = place?.address_components?.find(c =>
          types.some(type => c.types?.includes(type))
        )
        return component?.short_name || ''
      }

      const mappings = {
        [field?.cityName]: ['locality'],
        [field?.stateName]: ['administrative_area_level_1'],
        [field?.countryName]: ['country'],
        [field?.zipName]: ['postal_code']
      }

      const countryCode = getShortName(['country'])
      if (countryCode) {
        onChange(countryCode, 'country_code', true, 'countryCodeId')
      }

      const processAddressComponents = (components, separator = ' ') => {
        components = components
          .map(k => getAddressComponent([k]))
          .filter(Boolean)
        return components.length > 1
          ? components.join(separator)
          : components[0] || ''
      }

      for (const key in mappings) {
        if (!addressFields.value[key]) {
          continue
        }

        let value = getAddressComponent(mappings[key])

        // fallback for EMEA city
        if (isNotNARegion.value && key === field?.cityName && !value) {
          value = getAddressComponent(['administrative_area_level_2'])
        }

        if (key === field?.countryName || key === 'country') {
          const countryCode = getShortName(['country'])
          if (countryCode && value) {
            if (isEMEARegion.value) {
              // EMEA region: use localized country names
              const currentLang = getCurrentLanguage()
              const localizedName = getLocalizedCountryName(
                countryCode,
                currentLang,
                false
              )
              addressFields.value[key].value = localizedName || value
            } else {
              // NA/ALAO region: use the original country name from Google
              addressFields.value[key].value = value
            }
            onChange(countryCode, field[`${key}Name`], true, field[`${key}Id`])
          }
        } else {
          addressFields.value[key].value = value
        }
      }

      if (isNotNARegion.value) {
        address.value = processAddressComponents(['route', 'street_number'])
      } else {
        address.value = processAddressComponents(['street_number', 'route'])
      }

      isValidated.value = address.value?.length > 1
      if (onChange) {
        onChange(address.value, name, isValidated.value, field?.id)

        for (const key in mappings) {
          if (addressFields.value[key]) {
            onChange(
              addressFields.value[key]?.value,
              field[`${key}Name`],
              true,
              field[`${key}Id`]
            )
          }
        }
      }
    }

    return {
      handleChange,
      isTouched,
      isValidated,
      handleInputChange,
      handleBlur,
      scriptsLoaded,
      addressFields,
      autocompleteFields,
      updateAddressField,
      isPlacedSearched,
      address,
      countryRestriction,
      countryOptions,
      getCountryDropdownValue,
      isEMEARegion
    }
  }
}
</script>
