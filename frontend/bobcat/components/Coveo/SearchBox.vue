<template>
  <section data-test-id="coveo-searchbox">
    <div class="row align-items-end gap-1 mx-0">
      <div class="col-12 col-md-auto form-group px-0 mb-md-3 mb-sm-0">
        <div class="d-none d-flex justify-content-between">
          <label v-if="searchBoxLabel" class="search-label">{{ searchBoxLabel }}</label>
          <label
            v-if="autoDetectLabel"
            class="auto-detect c-primary d-flex align-items-center gap-0125 search-label"
            @click="autoDetectAction('autoDetect')"
          >
            <b-icon v-if="!isLoading" icon="current-location" class="mr-1" />
            <div v-else class="text-center">
              <b-spinner small variant="primary" class="mr-1" />
            </div>
            {{ autoDetectLabel }}
          </label>
        </div>
        <b-input-group class="d-none d-flex z-index-1 form-control-wrapper align-items-center justify-content-between border border-color-gray-100 rounded-2 transition-base bg-white w-100 height-5 flex-nowrap">
          <b-icon icon="pin" class="pl-2" />
          <b-form-input
            ref="autocompleteInput"
            v-model="searchText"
            autocomplete="false"
            name="q"
            :placeholder="searchBoxPlaceholder"
            :disabled="inEditorEdit"
            class="border-0 text-dark form-control search-box-input pl-2"
            @keyup="textChange"
            @input="onInput('addressType')"
            @submit="onAddressSubmit('addressPick')"
          />
        </b-input-group>
      </div>
      <div v-if="industryModelEnabled == 'true' && industryModelField" class="col-12 col-md-auto px-0">
        <CoveoCustomFacet
          ref="industryFacetRef"
          :engine="engine"
          :field="industryModelField"
          :label="industryModelLabel"
          :placeholder="modelPlaceholder"
          :is-categorized="false"
          :default-values="industryDefaultValues"
          :is-disabled="isFacetDisabled || dependentOn(industryModelField)"
          :is-required="isIndustryModelRequired"
          :facet-changed="facetChanged"
          :others-category-label="othersCategoryLabel"
          :facet-name="'industry'"
          @change-engine-context="changeEngineContext"
        />
      </div>
      <div v-if="productModelEnabled == 'true' && productModelField" class="col-12 col-md-auto px-0">
        <CoveoCustomFacet
          ref="productFacetRef"
          :engine="engine"
          :field="productModelField"
          :label="productModelLabel"
          :placeholder="modelPlaceholder"
          :search-enabled="productModelSearchEnabled"
          :search-placeholder="productModelSearchPlaceholder"
          :no-results-label="productModelNoResultsLabel"
          :is-categorized="true"
          :default-values="productDefaultValues"
          :is-disabled="isFacetDisabled || dependentOn(productModelField)"
          :is-required="isProductRequired"
          :allowed-values="productAllowedValues"
          :facet-changed="facetChanged"
          :others-category-label="othersCategoryLabel"
          :facet-name="'product'"
          @change-engine-context="changeEngineContext"
        />
      </div>
      <div v-if="servicesModelEnabled == 'true' && servicesModelField" class="col-12 col-md-auto px-0">
        <CoveoCustomFacet
          ref="servicesFacetRef"
          :engine="engine"
          :field="servicesModelField"
          :label="servicesModelLabel"
          :placeholder="modelPlaceholder"
          :is-categorized="false"
          :default-values="servicesDefaultValues"
          :is-disabled="isFacetDisabled || dependentOn(servicesModelField)"
          :is-required="isServicesModelRequired"
          :facet-changed="facetChanged"
          :selected-label="servicesSelectedLabel"
          :others-category-label="othersCategoryLabel"
          is-multi-select="true"
          :facet-name="'services'"
          @change-engine-context="changeEngineContext"
        />
      </div>
      <div class="col-12 col-md-auto form-group px-0 pt-md-0 pt-sm-3">
        <b-button v-if="searchButtonLabel"
                  variant="primary"
                  type="submit"
                  class="btn-mobile d-inline-flex align-items-center justify-content-center height-5 btn-narrow"
                  data-test-id="parts-finder-form__button"
                  :disabled="inEditorEdit || noQuery || !facetsHaveValues"
                  @click="searchSubmit"
        >
          {{ searchButtonLabel }}
        </b-button>
      </div>
    </div>
    <div class="row align-items-end">
      <div class="col-12">
        <template v-for="(f, i) in facetValues">
          <b-badge v-for="(c, j) in f.values"
                   :key="`facet__${f.field}_${c.value}_${i}_${j}`"
                   data-test-id="dloc-facet-selected-type"
                   pill
                   variant="dark"
                   :class="{'cursor-pointer': true}"
                   class="mr-2 mb-3 mt-3 bg-black"
                   @click.prevent="removeFromList(f.field, c.value)"
          >
            <span v-if="f.field && c.label" class="d-flex align-items-center h6 mb-0">
              {{ breadcrumbLabel(f.field) }}: {{ c.label }}
              <b-icon icon="remove" class="close-icon ml-2" />
            </span>
          </b-badge>
        </template>
      </div>
    </div>
  </section>
</template>

<script>
/* global google */
import {
  buildContext,
  buildSearchBox,
  loadGenericAnalyticsActions,
  loadQueryActions,
  loadSearchActions,
  loadSearchAnalyticsActions
} from '@coveo/headless'
import {computed, onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'
import {LOCATION_KEYS} from '../../constants'
import useQueryParams from '@/helpers/composables/useQueryParams'
import {BUSINESS_REGIONS, GROUND_MAINTENANCE_INDUSTRY, GROUND_MAINTENANCE_PRODUCTS, USER_COVEO_METADATA} from '~/constants'
import {HIDDEN_FIELDS_VALUES} from '~/constants/hiddenFields'
import useLocation from '~/helpers/composables/useLocation'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  props: {
    engine: {
      type: Object
    },
    searchBoxPlaceholder: {
      type: String
    },
    autoDetectLabel: {
      type: String
    },
    searchButtonLabel: {
      type: String
    },
    searchBoxLabel: {
      type: String
    },
    productDefaultValues: {
      type: String
    },
    industryDefaultValues: {
      type: String
    },
    servicesDefaultValues: {
      type: String
    },
    industryModelEnabled: {
      type: String
    },
    industryModelField: {
      type: String
    },
    industryModelLabel: {
      type: String
    },
    isIndustryModelRequired: {
      type: String
    },
    servicesModelEnabled: {
      type: String
    },
    servicesModelField: {
      type: String
    },
    servicesModelLabel: {
      type: String
    },
    servicesPillLabel: {
      type: String
    },
    isServicesModelRequired: {
      type: String
    },
    othersCategoryLabel: {
      type: String
    },
    productTypeLabel: {
      type: String
    },
    productModelEnabled: {
      type: String
    },
    productModelField: {
      type: String
    },
    servicesSelectedLabel: {
      type: String,
      default: 'Selected services'
    },
    productModelLabel: {
      type: String
    },
    isProductModelRequired: {
      type: String
    },
    modelPlaceholder: {
      type: String
    },
    productModelSearchEnabled: {
      type: String
    },
    productModelSearchPlaceholder: {
      type: String
    },
    productModelNoResultsLabel: {
      type: String
    },
    locationsByCountryOnly: {
      type: String,
      default: 'false'
    },
    productDependentOn: {
      type: String
    },
    servicesDependentOn: {
      type: String
    }
  },
  setup(props, {emit, expose}) {
    const {$ga4, $regions} = useContext()
    const autocompleteInput = ref(null)
    const state = ref(null)
    const productFacetRef = ref(null)
    const industryFacetRef = ref(null)
    const servicesFacetRef = ref(null)
    const currentLocation = ref()
    const {saveLocation, address} = useLocation()
    const searchText = ref('')
    const previousSearchValue = ref('')
    const noQuery = ref(true)
    const isFacetDisabled = ref(true)
    const isLoading = ref(false)
    const allowedSearch = ref(false)
    const isFirstInput = ref(true)
    const facetValues = ref([])
    const dependencyMapper = ref({})
    const {engine, locationsByCountryOnly, productTypeLabel, industryModelField, servicesModelField, productModelField, industryModelLabel, servicesPillLabel} = props
    const engineContext = buildContext(engine)
    const {executeSearch} = loadSearchActions(engine)
    const {updateQuery} = loadQueryActions(engine)
    const {logInterfaceChange, logSearchboxSubmit} = loadSearchAnalyticsActions(engine)
    const {logCustomEvent} = loadGenericAnalyticsActions(engine)
    const options = {numberOfSuggestions: 8}
    const controller = buildSearchBox(engine, {options})
    const geocoder = new google.maps.Geocoder()
    const eventType = 'dealer_locator_event'
    const regionInfo = {name: $regions?.current?.name}
    const coveoEventPayload = ref(null)
    const gtmEventMapping = {
      addressType: 'address_typing_start',
      addressPick: 'address_typing_success',
      autoDetect: 'auto_detect',
      searchboxSubmit: 'dealer_list_displayed'
    }
    const {
      queryParams,
      updateQueryParams
    } = useQueryParams()

    const isEMEA = BUSINESS_REGIONS.emea.includes($regions?.current?.name)
    const currentLanguage = $regions?.current?.language?.name
    const groundMaintenanceIndustry = GROUND_MAINTENANCE_INDUSTRY[currentLanguage]
    const groundMaintenanceProducts = GROUND_MAINTENANCE_PRODUCTS[currentLanguage] || []

    // Ground Maintenance makes the product facet mandatory and narrows its values - AV-3012
    const isGroundMaintenanceSelected = computed(() => {
      if (!isEMEA || !groundMaintenanceIndustry) {
        return false
      }
      const industryValues = facetValues.value.find(o => o.field === industryModelField)?.values || []
      return industryValues.some(o => o.value === groundMaintenanceIndustry)
    })

    const productAllowedValues = computed(() => isGroundMaintenanceSelected.value ? groundMaintenanceProducts : [])

    const isProductRequired = computed(() => (props.isProductModelRequired === 'true' || isGroundMaintenanceSelected.value) ? 'true' : 'false')

    const facetsToVerify = computed(() => {
      const facets = []
      if (props.industryModelEnabled === 'true' && props.isIndustryModelRequired === 'true') {
        facets.push(industryModelField)
      }
      if (props.productModelEnabled === 'true' && isProductRequired.value === 'true') {
        facets.push(productModelField)
      }
      if (props.servicesModelEnabled === 'true' && props.isServicesModelRequired === 'true') {
        facets.push(servicesModelField)
      }
      return facets
    })

    const facetsHaveValues = computed(() => facetsToVerify.value.every((facet) => {
      const fValuesObject = facetValues.value.find(o => o.field === facet)
      return !!fValuesObject && fValuesObject.values.length > 0
    }))

    const updateAllQueryParams = () => {
      const params = {
        location: JSON.stringify(currentLocation.value),
        text: searchText.value
      }
      if (props.productModelEnabled) {
        params.products = JSON.stringify(filterSelectedRecords(productModelField))
      }
      if (props.industryModelEnabled) {
        params.industry = JSON.stringify(filterSelectedRecords(industryModelField))
      }
      if (props.servicesModelEnabled) {
        params.services = JSON.stringify(filterSelectedRecords(servicesModelField))
      }

      updateQueryParams(params)
    }

    const setLocation = (place) => {
      saveLocation(LOCATION_KEYS.ADDRESS, place.formatted_address)
      const country = place?.address_components?.find(item => item.types.includes('country'))?.short_name
      const zip = place?.address_components?.find(component => component.types.includes('postal_code'))?.short_name
      saveLocation(LOCATION_KEYS.COUNTRY_CODE, country)
      saveLocation(LOCATION_KEYS.ZIP_CODE, zip)
    }

    const restoreFromLocalStorage = async () => {
      // restore from local storage only when address is not restored in previous steps
      if (address.value && !searchText.value) {
        const place = await getGeoLocationInfo({address: address.value})

        if (place) {
          currentLocation.value = {
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng(),
            ...(getSearchCountryInfo(place))
          }
          setEngineContext(currentLocation.value)
          noQuery.value = !place.formatted_address
          searchText.value = address.value
          isFacetDisabled.value = false
          allowedSearch.value = true
        }
      }
    }

    const isFacetEnabled = (facet) => {
      return (facet === industryModelField && props.industryModelEnabled === 'true') ||
        (facet === productModelField && props.productModelEnabled === 'true') ||
        (facet === servicesModelField && props.servicesModelEnabled === 'true')
    }

    const setDependencyMapper = () => {
      if (productModelField && isFacetEnabled(productModelField) && props.productDependentOn && isFacetEnabled(props.productDependentOn)) {
        dependencyMapper.value[productModelField] = props.productDependentOn
      }
      if (servicesModelField && isFacetEnabled(servicesModelField) && props.servicesDependentOn && isFacetEnabled(props.servicesDependentOn)) {
        dependencyMapper.value[servicesModelField] = props.servicesDependentOn
      }
    }

    setDependencyMapper()

    const getSearchCountryInfo = (place) => {
      if (locationsByCountryOnly === 'true') {
        const country = place?.address_components?.find(item => item.types.includes('country'))
        return {
          search_country: country?.long_name || country?.short_name ? [country.long_name, country.short_name].filter(Boolean) : []
        }
      }
      return {}
    }

    const dependentOn = (field) => {
      const dependencyField = dependencyMapper.value[field]

      if (dependencyField) {
        const fieldDependency = facetValues.value.find(o => o.field === dependencyField)
        return !fieldDependency || fieldDependency.values.length === 0
      }
      return false
    }

    const addAutoCompleteBox = async () => {
      await google.maps.importLibrary('places')

      const autocomplete = new google.maps.places.Autocomplete(autocompleteInput.value.$el)
      autocomplete.addListener('place_changed', () => {
        const place = autocomplete.getPlace()
        if (place.geometry) {
          setLocation(place)
          allowedSearch.value = true
          searchText.value = place.formatted_address

          currentLocation.value = {
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng(),
            ...(getSearchCountryInfo(place))
          }
          setEngineContext(currentLocation.value)

          // Call onAddressSubmit method to log analytics when suggestion is picked
          const cause = 'addressPick'
          onAddressSubmit(cause)
        }
      })
    }

    const isEqual = (obj1, obj2) => {
      if (obj1.length === 0 && obj1.length === obj2.length) {
        return true
      } else {
        const values1 = obj1.map(o => o.label).sort()
        const values2 = obj2.map(o => o.label).sort()
        return values1.join() === values2.join()
      }
    }

    const breadcrumbLabel = (field) => {
      if (field === productModelField) {
        return productTypeLabel
      } else if (field === industryModelField) {
        return industryModelLabel
      } else if (field === servicesModelField) {
        return servicesPillLabel
      }
    }

    const facetChanged = (field, values, origin) => {
      const fValuesObject = facetValues.value.find(o => o.field === field)
      if (!fValuesObject) {
        facetValues.value.push({
          field,
          values
        })
        emit('stateChanged', !!searchText.value && facetsHaveValues.value, origin)
      } else if (!isEqual(values, fValuesObject.values)) {
        fValuesObject.values = values
        // verify that all required facets are selected
        emit('stateChanged', !!searchText.value && facetsHaveValues.value, origin)
      }
    }

    const logGtmEvents = (eventPayload) => {
      $ga4.events.coveo_analytics_events(eventPayload)
    }

    const filterSelectedRecords = (fieldName) => {
      const resultsList = engine.state?.facetSet?.[fieldName]?.request?.currentValues
      return resultsList?.filter(item => item?.state === 'selected')?.map(item => item?.value)
    }

    const setLocalStorageData = () => {
      const currentServices = filterSelectedRecords(servicesModelField)
      const currentIndustries = filterSelectedRecords(industryModelField)

      if (process.client && window) {
        localStorage.setItem(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_SERVICE, currentServices?.length ? currentServices.join('; ') : '')
        localStorage.setItem(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_INDUSTRY, currentIndustries?.length ? currentIndustries.join('; ') : '')
      }
    }

    const searchSubmit = () => {
      if (allowedSearch.value) {
        const query = searchText.value || ''
        const searchLocation = query
        const currentValuesArray = filterSelectedRecords(productModelField)
        const storeLocation = null
        setLocalStorageData()
        updateAllQueryParams()

        if (
          currentLocation.value?.lat &&
          currentLocation.value?.lng &&
          (engine.state?.contextValues?.lat !== currentLocation.value?.lat || engine.state?.contextValues?.lng !== currentLocation.value?.lng)
        ) {
          engineContext.set(currentLocation.value)
        }
        const gtmEventPayload = {
          cta_type: 'Manual Search',
          search_location: searchLocation,
          item_category: currentValuesArray,
          store_location: storeLocation
        }
        // Log custom GTM Analytics for non-NA regions
        if (regionInfo.name !== 'na') {
          logAnalytics('searchboxSubmit')
        } else {
          logGtmEvents(gtmEventPayload)
        }

        setEngineContext(engine.state?.context.contextValues, false, {
          search_location: query,
          item_category: currentValuesArray,
          store_location: null
        })

        engine.dispatch(updateQuery({
          q: query
        }))
        engine.dispatch(executeSearch(logSearchboxSubmit()))
      } else {
        autocompleteInput.value.focus()
      }
    }

    // Logs Analytic Events to Coveo and GTM
    const logAnalytics = (cause) => {
      if (!coveoEventPayload.value) {
        setCoveoEventPayload(cause)
      }
      engine.dispatch(logCustomEvent(coveoEventPayload.value))
      let gtmEventPayload = {
        cause: gtmEventMapping[cause],
        language_preference: $regions.current?.locale?.toLowerCase()
      }
      // Add searched address parameter only for addressType and autoDetect events
      if (cause === 'addressPick' || cause === 'autoDetect') {
        gtmEventPayload = {
          ...gtmEventPayload,
          searched_address: searchText.value
        }
      }
      logGtmEvents(gtmEventPayload)
    }

    const setCoveoEventPayload = (cause) => {
      coveoEventPayload.value = {
        evt: cause,
        type: eventType
      }
    }

    const onInput = (cause) => {
      previousSearchValue.value = searchText.value
      // Log Analytics only when use first enters address and only for non-NA regions
      if (isFirstInput.value === true && regionInfo.name !== 'na') {
        isFirstInput.value = false
        setCoveoEventPayload(cause)
        logAnalytics(cause)
      }
    }

    // Method to handle analytics for address pick (suggestion selection and manual entry of address)
    const onAddressSubmit = (cause) => {
      // Log Analytics for non-NA regions
      if (regionInfo.name !== 'na') {
        setCoveoEventPayload(cause)
        logAnalytics(cause)
      }
    }

    const textChange = () => {
      setEngineContext({
        lat: null,
        lng: null
      })
      noQuery.value = !searchText.value
      allowedSearch.value = false
      isFacetDisabled.value = true
    }

    const updateSearchBoxText = (val) => {
      engine.dispatch(updateQuery({
        q: val
      }))
      searchText.value = val
      noQuery.value = !val
      allowedSearch.value = !!val
      isFacetDisabled.value = !val
      setLocalStorageData()
      updateAllQueryParams()
    }

    const selectedFacetValue = (v) => {
      return {label: v, selected: true, value: v, disabled: 'false'}
    }

    const restoreFromQuery = () => {
      if (Object.keys(queryParams.value)?.length) {
        // restoring previous values from query params

        searchText.value = queryParams.value?.text ? decodeURI(queryParams.value?.text) : null
        // eslint-disable-next-line camelcase
        noQuery.value = !searchText.value
        allowedSearch.value = true

        const products = queryParams.value?.products ? JSON.parse(decodeURI(queryParams.value?.products)) : []
        const industry = queryParams.value?.industry ? JSON.parse(decodeURI(queryParams.value?.industry)) : []
        const services = queryParams.value?.services ? JSON.parse(decodeURI(queryParams.value?.services)) : []

        if (products.length > 0) {
          productFacetRef.value.handleModelChange(products.map(v => selectedFacetValue(v)))
        }
        if (industry.length > 0) {
          industryFacetRef.value.handleModelChange(industry.map(v => selectedFacetValue(v)))
        }
        if (services.length > 0) {
          servicesFacetRef.value.handleModelChange(services.map(v => selectedFacetValue(v)))
        }

        currentLocation.value = queryParams.value?.location ? JSON.parse(queryParams.value?.location) : null

        setEngineContext(currentLocation.value)

        emit('stateChanged', true)
        searchSubmit()
      }
    }

    const addBackAction = async () => {
      const userMetadata = localStorage.getItem(USER_COVEO_METADATA)
      // Check if the key exists in localStorage
      if (userMetadata) {
        try {
          const {isDealerLandingPage = false, products = [], industry = [], services = [], dealerAddress} = JSON.parse(userMetadata)
          // apply back action if user is coming from dealer landing page
          if (isDealerLandingPage) {
            isLoading.value = true
            isFacetDisabled.value = false
            const place = await getGeoLocationInfo({address: dealerAddress})
            isLoading.value = false
            if (place) {
              // eslint-disable-next-line camelcase
              const {formatted_address, geometry} = place
              // eslint-disable-next-line camelcase
              searchText.value = formatted_address
              // eslint-disable-next-line camelcase
              noQuery.value = !formatted_address
              allowedSearch.value = true

              if (products.length > 0) {
                productFacetRef.value.handleModelChange(products.map(v => selectedFacetValue(v)))
              }
              if (industry.length > 0) {
                industryFacetRef.value.handleModelChange(industry.map(v => selectedFacetValue(v)))
              }
              if (services.length > 0) {
                servicesFacetRef.value.handleModelChange(services.map(v => selectedFacetValue(v)))
              }

              currentLocation.value = {
                lat: geometry.location.lat(),
                lng: geometry.location.lng(),
                ...(getSearchCountryInfo(place))
              }

              setEngineContext(currentLocation.value)

              emit('stateChanged', true)
              searchSubmit()
              localStorage.removeItem(USER_COVEO_METADATA)
            } else {
              isFacetDisabled.value = true
            }
          }
        } catch (e) {
          isLoading.value = false
          // eslint-disable-next-line no-console
          console.error('Error parsing coveo_user_metadata:', e)
        }
      }
    }

    const getGeoLocationInfo = async (info) => {
      try {
        const response = await geocoder.geocode(info)
        if (response) {
          const validAddress = response.results.find(result => result.address_components.find(address => !!address.types.includes('country')))
          return validAddress
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('Geocoder failed due to: ' + e)
      }
    }

    const autoDetectAction = (cause) => {
      isLoading.value = true
      navigator.geolocation.getCurrentPosition(async (res) => {
        const latlng = new window.google.maps.LatLng(res.coords.latitude, res.coords.longitude)
        const geocoder = new window.google.maps.Geocoder()

        geocoder.geocode({latLng: latlng}, (results, status) => {
          isLoading.value = false
          if (status === window.google.maps.GeocoderStatus.OK && results[0]) {
            const place = results[0]
            const address = place.formatted_address
            const countryInfo = getSearchCountryInfo(place) || {}

            currentLocation.value = {
              lat: res.coords.latitude,
              lng: res.coords.longitude,
              ...countryInfo
            }

            setEngineContext(currentLocation.value)
            setLocation(place)
            allowedSearch.value = true
            searchText.value = address
            noQuery.value = !address

            if (regionInfo.name !== 'na') {
              setCoveoEventPayload(cause)
              logAnalytics(cause)
            }
          } else {
            // eslint-disable-next-line no-console
            console.error('Geocoder failed: ' + status)
          }
        })
      }, () => {
        isLoading.value = false
        // eslint-disable-next-line no-console
        console.error('location is not enabled')
      })
    }

    onMounted(() => {
      restoreFromQuery()
      addBackAction()
      addAutoCompleteBox()
      restoreFromLocalStorage()
    })

    controller.subscribe(() => {
      state.value = controller.state
    })

    const getFacetRef = (field) => {
      if (field === productModelField) {
        return productFacetRef
      }
      if (field === industryModelField) {
        return industryFacetRef
      }
      if (field === servicesModelField) {
        return servicesFacetRef
      }
    }

    const removeFromList = (field, v) => {
      const facetRef = getFacetRef(field)
      facetRef.value.updateFacet(v, 'idle', true)
      const fValuesObject = facetValues.value.find(o => o.field === field)
      fValuesObject.values = fValuesObject.values.filter(o => o.value !== v)
      emit('stateChanged', !!searchText.value && facetsHaveValues.value)
    }

    watch(isGroundMaintenanceSelected, (isSelected) => {
      if (!isSelected) {
        return
      }
      const selectedProducts = facetValues.value.find(o => o.field === productModelField)?.values || []
      selectedProducts
        .filter(o => !groundMaintenanceProducts.includes(o.value))
        .forEach(o => removeFromList(productModelField, o.value))
    })

    const setEngineContext = async (ctx, shouldExecuteSearch, additionalCtx) => {
      engineContext.set({...ctx, ...additionalCtx})
      if (ctx.lat && ctx.lng) {
        isFacetDisabled.value = false
      }
      if (shouldExecuteSearch) {
        engine.dispatch(executeSearch(logInterfaceChange()))
      }
    }

    const changeEngineContext = (additionalCtx) => {
      engineContext.set({...engineContext?.state?.values, ...additionalCtx})
    }

    expose({
      setEngineContext,
      getGeoLocationInfo,
      getSearchCountryInfo,
      updateSearchBoxText
    })

    return {
      productFacetRef,
      industryFacetRef,
      servicesFacetRef,
      isLoading,
      isFacetDisabled,
      facetValues,
      facetChanged,
      facetsToVerify,
      breadcrumbLabel,
      facetsHaveValues,
      productAllowedValues,
      isProductRequired,
      dependentOn,
      autoDetectAction,
      removeFromList,
      autocompleteInput,
      inEditorEdit,
      noQuery,
      searchSubmit,
      searchText,
      state,
      textChange,
      onInput,
      onAddressSubmit,
      changeEngineContext
    }
  }
}
</script>

<style lang="scss" scoped>
  @import '~bootstrap/scss/functions';
  @import '~/assets/sass/variables';
  @import '~bootstrap/scss/mixins';

  .c-primary {
    color: $primary
  }

  .search-box-input {
    width: 19rem;
  }

  .auto-detect {
    &:hover {
      cursor: pointer;
    }
  }

  .gap-0125 {
    gap: 0.125rem;
  }

  .gap-1 {
    gap: 1rem
  }

  .search-label {
    font-size: 1rem;
  }

  @include media-breakpoint-down('sm') {
    .mb-sm-0 {
      margin-bottom: 0 !important;
    }
  }
</style>
