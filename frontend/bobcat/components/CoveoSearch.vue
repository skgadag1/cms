<template>
  <section ref="coveoSearchSection" data-test-id="coveo-search" class="w-100 d-flex flex-column justify-content-center align-items-center coveo-search">
    <client-only>
      <div :class="{'my-4': error || inEditorEdit}">
        <WarningAlert v-if="error"><div v-html="error" /></WarningAlert>
        <WarningAlert v-else-if="inEditorEdit" type="warning">
          No data to display due to edit mode
        </WarningAlert>
      </div>
      <atomic-search-interface
        v-if="!error"
        reflect-state-in-url="false"
        language-assets-path="/coveo-lang"
        class="w-100 d-flex flex-column justify-content-center align-items-center"
        :fields-to-include="interfaceFields"
      >
        <atomic-search-layout :class="extraLayoutClass">
          <atomic-layout-section section="search" class="col-12 w-100 mb-3 mt-lg-0 px-md-0 px-lg-3">
            <CoveoSearchBox v-if="engine"
                            ref="coveoSearchBox"
                            :engine="engine"
                            :search-box-label="searchBoxLabel"
                            :search-box-placeholder="searchBoxPlaceholder"
                            :search-button-label="searchButtonLabel"
                            :auto-detect-label="autoDetectLabel"
                            :model-placeholder="modelPlaceholder"
                            :product-default-values="productDefaultValues"
                            :product-model-enabled="productModelEnabled"
                            :product-model-field="productModelField"
                            :product-model-label="productModelLabel"
                            :product-model-search-enabled="productModelSearchEnabled"
                            :product-model-search-placeholder="productModelSearchPlaceholder"
                            :product-model-no-results-label="productModelNoResultsLabel"
                            :product-type-label="productTypeLabel"
                            :product-dependent-on="productDependentOn"
                            :is-product-model-required="isProductModelRequired"
                            :industry-model-enabled="industryModelEnabled"
                            :industry-model-field="industryModelField"
                            :industry-model-label="industryModelLabel"
                            :industry-default-values="industryDefaultValues"
                            :is-industry-model-required="isIndustryModelRequired"
                            :services-model-enabled="servicesModelEnabled"
                            :services-model-field="servicesModelField"
                            :services-model-label="servicesModelLabel"
                            :services-pill-label="servicesPillLabel"
                            :services-selected-label="servicesSelectedLabel"
                            :services-default-values="servicesDefaultValues"
                            :is-services-model-required="isServicesModelRequired"
                            :services-dependent-on="servicesDependentOn"
                            :locations-by-country-only="locationsByCountryOnly"
                            :others-category-label="othersCategoryLabel"
                            @stateChanged="stateChanged"
            />
            <atomic-did-you-mean data-atomic-rendered="true" data-atomic-loaded="true" class="hydrated hidden" automatically-correct-query="" query-correction-mode="legacy" />
          </atomic-layout-section>

          <atomic-layout-section section="main" class="mb-0">
            <atomic-layout-section section="status" class="hydrated">
              <CoveoQuerySummary v-if="engine" :engine="engine" :results-label="resultsLabel" />
              <CoveoUnitDropdown v-if="engine"
                                 :units-label="unitsLabel"
                                 :kilometer-label="kilometerLabel"
                                 :miles-label="milesLabel"
                                 :engine="engine"
                                 @unit-selected="handleUnitSelected"
                                 @change-engine-context="changeEngineContext"
              />
            </atomic-layout-section>
            <atomic-layout-section section="results">
              <CoveoResultTemplate
                v-if="engine"
                ref="coveoResultTemplate"
                :engine="engine"
                :google-api-key="googleApiKey"
                :map-center="mapCenter"
                :selected-unit="selectedUnit"
                :visit-website-label="visitWebsiteLabel"
                :get-directions-label="getDirectionsLabel"
                :coveo-search-box="coveoSearchBox"
                :origin-address="originAddress"
                :selected-marker-index="selectedMarkerIndex"
                :product-model-field="productModelField"
                :industry-model-field="industryModelField"
                :services-model-field="servicesModelField"
                :mi-label="miLabel"
                :km-label="kmLabel"
                @change-engine-context="changeEngineContext"
                @result-template-hovered="hoverMapMarker"
              />
              <atomic-query-error />
              <atomic-no-results />
            </atomic-layout-section>
          </atomic-layout-section>
          <atomic-layout-section v-if="scriptsLoaded && googleApiKey" section="map">
            <div ref="mapElement" class="mb-5 w-100 h-100" />
          </atomic-layout-section>
          <atomic-layout-section v-if="scriptsLoaded && engine && (!showResults || !validState)" section="coveo-placeholder" class="px-3">
            <div v-if="infoMessage" class="info"><b-icon icon="info" /><span class="font-weight-bold">{{ infoMessage }}</span></div>
          </atomic-layout-section>
        </atomic-search-layout>
      </atomic-search-interface>
    </client-only>
  </section>
</template>

<script>
/* global google */
import {onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'
import {inEditorEdit} from '~/helpers/vue-editor'

let coveoElementsRegistered = false

async function registerCoveoElements() {
  if (coveoElementsRegistered) {
    return
  }
  try {
    const {defineCustomElements} = await import('@coveo/atomic/loader')
    defineCustomElements(window)
    coveoElementsRegistered = true
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Failed to load Coveo elements:', error)
    throw error
  }
}

export default {
  props: {
    nodeId: {
      type: String
    },
    orgId: {
      type: String
    },
    tokenUri: {
      type: String
    },
    fieldsToInclude: {
      type: String
    },
    googleApiKey: {
      type: String
    },
    mapId: {
      type: String
    },
    zoomLevel: {
      type: String
    },
    searchBoxLabel: {
      type: String
    },
    searchBoxPlaceholder: {
      type: String
    },
    searchButtonLabel: {
      type: String
    },
    autoDetectLabel: {
      type: String
    },
    completeFieldsLabel: {
      type: String
    },
    noResultsLabel: {
      type: String
    },
    mapSearchButtonLabel: {
      type: String
    },
    mapInvalidAddressLabel: {
      type: String,
      default: 'Invalid address. Select a new location.'
    },
    productModelEnabled: {
      type: String
    },
    productModelField: {
      type: String
    },
    productModelLabel: {
      type: String
    },
    productTypeLabel: {
      type: String
    },
    isProductModelRequired: {
      type: String
    },
    productDependentOn: {
      type: String
    },
    modelPlaceholder: {
      type: String
    },
    resultsLabel: {
      type: String
    },
    unitsLabel: {
      type: String
    },
    kilometerLabel: {
      type: String
    },
    milesLabel: {
      type: String
    },
    visitWebsiteLabel: {
      type: String
    },
    getDirectionsLabel: {
      type: String
    },
    productDefaultValues: {
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
    industryDefaultValues: {
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
    servicesSelectedLabel: {
      type: String,
      default: 'Selected services'
    },
    isServicesModelRequired: {
      type: String
    },
    servicesDefaultValues: {
      type: String
    },
    servicesDependentOn: {
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
      type: String
    },
    othersCategoryLabel: {
      type: String
    },
    miLabel: {
      type: String
    },
    kmLabel: {
      type: String
    }
  },
  setup(props) {
    const mapElement = ref(null)
    const map = ref(null)
    const customMapButtons = ref([])
    const coveoSearchSection = ref()
    const scriptsLoaded = ref(false)
    const error = ref('')
    const extraLayoutClass = ref('hide-results hydrated')
    const locationMarkers = []
    const showResults = ref(false)
    const validState = ref(false)
    const wasLoading = ref(false)
    const dictionaryFieldContext = ref({})
    const engine = ref()
    const coveoSearchBox = ref()
    const coveoResultTemplate = ref()
    const mapCenter = ref({lat: -34.397, lng: 150.644}) // Initial center
    const selectedMarker = ref()
    const originAddress = ref()
    const selectedMarkerIndex = ref()
    const {$regions, $utils} = useContext()
    const selectedUnit = $regions?.current?.name === 'na' ? ref('mi') : ref('km')

    const regionInfo = {
      language: $regions?.current?.language?.name,
      name: $regions?.current?.name
    }

    const {fieldsToInclude, orgId, tokenUri, googleApiKey, zoomLevel, mapId, completeFieldsLabel, noResultsLabel, mapSearchButtonLabel, mapInvalidAddressLabel} = props

    const buildDictionaryField = () => {
      if (props.productModelEnabled && props.productModelField) {
        dictionaryFieldContext.value[props.productModelField] = `${regionInfo.name}-${regionInfo.language}`
      }
      if (props.industryModelEnabled && props.industryModelField) {
        dictionaryFieldContext.value[props.industryModelField] = `${regionInfo.name}-${regionInfo.language}`
      }
      if (props.servicesModelEnabled && props.servicesModelField) {
        dictionaryFieldContext.value[props.servicesModelField] = `${regionInfo.name}-${regionInfo.language}`
      }
    }

    buildDictionaryField()

    const infoMessage = ref(completeFieldsLabel || '')

    const interfaceFields = JSON.stringify(fieldsToInclude?.split(',') || [])
    let selectedElement = null

    useMounted(props.nodeId)

    const handleUnitSelected = (unit) => {
      selectedUnit.value = unit
    }

    const changeEngineContext = (newContext = mapCenter.value, forceSearch, additionalCtx = {}) => {
      coveoSearchBox.value.setEngineContext(newContext, forceSearch, additionalCtx)
    }

    const hoverMapMarker = ({index, ishovered}) => {
      const markerElement = document.querySelector(`.map-marker-container[data-index="${index + 1}"]`)
      if (markerElement) {
        if (ishovered) {
          markerElement.classList.add('hovered')
        } else {
          markerElement.classList.remove('hovered')
        }
      }
    }

    const addMarker = async (position, index) => {
      const {AdvancedMarkerElement} = await google.maps.importLibrary('marker')
      const markerElement = document.createElement('div')
      markerElement.classList.add('map-marker-container')
      markerElement.setAttribute('data-index', index)

      const markerClicked = () => {
        // Log Analytics for Pin click but only for non-NA regions
        const cause = 'pinClick'
        if (regionInfo.name !== 'na') {
          logAnalytics(cause)
        }
        if (selectedMarker.value) {
          selectedMarker.value.classList.remove('selected')
        }
        markerElement.classList.add('selected')
        selectedElement = markerElement
        selectedMarker.value = markerElement
        setMapCenter(position.lat, position.lng)
        selectedMarkerIndex.value = index
      }

      markerElement.onmouseenter = () => {
        // Remove selected element properties when hovering over another pin
        if (selectedElement && selectedElement !== markerElement) {
          selectedElement.classList.remove('selected')
          selectedElement.classList.remove('hovered')
          selectedElement = null
        }
        markerElement.classList.add('hovered')
        selectedMarkerIndex.value = index
      }

      markerElement.onmouseleave = () => {
        if (!markerElement.classList.contains('selected')) {
          selectedMarkerIndex.value = -1
          markerElement.classList.remove('hovered')
        }
      }

      markerElement.innerHTML = `<span class="marker-index">${index}</span>`
      const marker = new AdvancedMarkerElement({
        position,
        map: map.value,
        content: markerElement
      })
      google.maps.event.addListener(marker, 'click', () => {
        markerClicked()
      })
      locationMarkers.push(marker)
    }

    const setMapCenter = (lat, lng) => {
      map.value?.setCenter({lat, lng})
    }

    let eventWasFired = false
    const stateChanged = (value, origin) => {
      if (origin !== 'subscribe') {
        showResults.value = false
      }
      validState.value = value
      const noResults = engine.value?.state.search.results.length === 0 && !!engine.value?.state.search.requestId
      infoMessage.value = !value ? completeFieldsLabel : (noResults ? noResultsLabel : completeFieldsLabel)
      extraLayoutClass.value = !value || !showResults.value ? 'hydrated hide-results' : 'hydrated'
      if (showResults.value && !value) {
        showResults.value = value
      }

      if (value === true && eventWasFired === false) {
        eventWasFired = true
        navigator.geolocation.getCurrentPosition((res) => {
          setMapCenter(res.coords.latitude, res.coords.longitude)
        })
      }
    }

    // Logs Analytic Events to Coveo and GTM
    // Migrated this logic to result template code since importing coveo headless is not possible in this component, because of SSR.
    const logAnalytics = (cause) => {
      coveoResultTemplate.value?.logMapAnalytics(cause, selectedMarkerIndex.value)
    }

    const showMapComponent = (map, component, position) => {
      const elements = map.controls[position].getArray()
      const el = elements?.find(item => item.id === component.id)
      if (!el) {
        map.controls[position].push(component)
        customMapButtons.value.push(component)
      } else {
        el.style.display = 'block'
      }
    }

    const hideAllMapComponents = () => {
      customMapButtons.value.forEach((component) => {
        component.style.display = 'none'
      })
    }

    const hideMapComponent = (map, id, position) => {
      const elements = map.controls[position].getArray()
      const el = elements?.find(item => item.id === id)
      if (el) {
        el.style.display = 'none'
      }
    }

    const addWarningMessage = (map) => {
      const warningMessage = document.createElement('h4')
      // Set CSS for the control.
      warningMessage.classList.add('warning-message', 'my-5', 'py-2', 'px-2')
      warningMessage.textContent = mapInvalidAddressLabel
      warningMessage.title = mapInvalidAddressLabel
      warningMessage.id = 'warning-message-container'
      showMapComponent(map, warningMessage, google.maps.ControlPosition.TOP_CENTER)
    }

    const createCenterControl = (map, id) => {
      const controlButton = document.createElement('button')
      // Set CSS for the control.
      controlButton.classList.add('btn', 'btn-outline-primary', 'btn-secondary', 'my-5', 'mr-6')
      controlButton.textContent = mapSearchButtonLabel
      controlButton.title = mapSearchButtonLabel
      controlButton.type = 'button'

      const centerControlDiv = document.createElement('div')
      controlButton.addEventListener('click', async () => {
        // hide Search This Area button
        hideMapComponent(map, id, google.maps.ControlPosition.TOP_RIGHT)
        const cause = 'searchArea'

        selectedMarker.value = null
        let latlng = {
          lat: map.getCenter().lat(),
          lng: map.getCenter().lng()
        }
        const place = await coveoSearchBox.value.getGeoLocationInfo({location: latlng})

        if (place) {
          const address = place.formatted_address

          latlng = {
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng()
          }

          coveoSearchBox.value.updateSearchBoxText(address)
          changeEngineContext({
            ...latlng,
            ...(place ? coveoSearchBox.value.getSearchCountryInfo(place) : {})
          }, true)

          // Log Analytics for Search Area click but only for non-NA regions
          if (regionInfo.name !== 'na') {
            logAnalytics(cause)
          }
        } else {
          addWarningMessage(map)
        }
      })
      centerControlDiv.setAttribute('id', id)
      centerControlDiv.appendChild(controlButton)
      return centerControlDiv
    }

    const sortLocations = async (origin, locations) => {
      const service = new google.maps.DistanceMatrixService()
      const req = {
        origins: [origin],
        destinations: locations,
        travelMode: google.maps.TravelMode.DRIVING,
        unitSystem: google.maps.UnitSystem.IMPERIAL,
        avoidHighways: false,
        avoidTolls: false,
        language: regionInfo.language
      }
      try {
        const response = await service.getDistanceMatrix(req)
        if (response?.rows?.length > 0) {
          const elements = [...response.rows[0]?.elements.filter(el => el.status === 'OK')]
          if (elements.length !== locations.length) {
            return []
          }
          const sorted = elements.map((value, index) => ({value, index}))
            .sort((a, b) => {
              return a.value.distance.value - b.value.distance.value
            })
          return sorted
        } else {
          return []
        }
      } catch (err) {
        // eslint-disable-next-line no-console
        console.error(err)
        return []
      }
    }

    const initializeMap = async () => {
      const {Map} = await google.maps.importLibrary('maps')

      scriptsLoaded.value = true
      const setupMap = async (lat, lng) => {
        const mapOptions = {
          center: {
            lat,
            lng
          },
          zoom: Number(zoomLevel) || 10,
          mapId,
          disableDefaultUI: true,
          streetViewControl: false,
          mapTypeControl: false
        }
        map.value = new Map(mapElement.value, mapOptions)

        const showSearhAreaButton = () => {
          hideMapComponent(map.value, 'warning-message-container', google.maps.ControlPosition.TOP_CENTER)
          const containerId = 'search-area-container'
          const searchAreaComp = createCenterControl(map.value, containerId)
          showMapComponent(map.value, searchAreaComp, google.maps.ControlPosition.TOP_RIGHT)
        }
        map.value.addListener('dragend', () => {
          showSearhAreaButton()
        })
        map.value.addListener('zoom_changed', () => {
          showSearhAreaButton()
        })

        google.maps.event.addListener(map.value, 'idle', () => {
          google.maps.event.trigger(map.value, 'resize')
        })

        const sInterface = await $utils.initializeCoveo(orgId, tokenUri, locationMarkers, regionInfo, addMarker, setMapCenter, dictionaryFieldContext.value, sortLocations)
        sInterface.engine.subscribe(() => {
          originAddress.value = sInterface.engine.state.query?.q
          mapCenter.value = {
            lat: map.value.getCenter().lat(),
            lng: map.value.getCenter().lng()
          }
          const hasQuery = !!sInterface.engine.state.query?.q
          if (sInterface.engine.state.search?.isLoading) {
            wasLoading.value = true
          }
          if (!sInterface.engine.state.search?.isLoading && wasLoading.value) {
            hideAllMapComponents()
            wasLoading.value = false
            const resultsCount = sInterface.engine.state.search.results.length
            if (resultsCount === 0 || !validState.value) {
              showResults.value = false
              extraLayoutClass.value = 'hide-results hydrated'
              infoMessage.value = resultsCount === 0 ? noResultsLabel : completeFieldsLabel
            } else {
              extraLayoutClass.value = hasQuery && validState.value ? 'hydrated' : 'hide-results hydrated'
              showResults.value = hasQuery && validState.value
            }
          }
        })
        engine.value = sInterface.engine
      }

      watch(mapElement, () => {
        setupMap(46.8167, -71.167)
      })
    }

    const loadGoogleMapsScript = () => {
      window.initMap = initializeMap
      const script = document.createElement('script')
      script.src = `https://maps.googleapis.com/maps/api/js?key=${googleApiKey}&libraries=places&callback=initMap`
      script.async = true
      script.loading = 'async'
      script.defer = true
      document.head.appendChild(script)
    }

    onMounted(async () => {
      const missingParams = []
      if (!fieldsToInclude) {
        missingParams.push('Fields To Include')
      }
      if (!orgId) {
        missingParams.push('Organization ID')
      }
      if (!tokenUri) {
        missingParams.push('Token Generation URI')
      }
      if (!googleApiKey) {
        missingParams.push('Google API Key')
      }
      if (!mapId) {
        missingParams.push('Map ID')
      }
      if (!zoomLevel) {
        missingParams.push('Zoom Level')
      }
      if (missingParams.length > 0) {
        error.value = `Missing Parameters ${missingParams.join(' & ')}`
        return
      }

      await registerCoveoElements()

      if (process.client && googleApiKey) {
        if (typeof google === 'undefined') {
          loadGoogleMapsScript()
        } else {
          initializeMap()
        }
      }
      if (coveoSearchSection.value?.title) {
        coveoSearchSection.value.title = ''
      }
    })

    return {
      infoMessage,
      coveoSearchBox,
      scriptsLoaded,
      interfaceFields,
      engine,
      mapElement,
      map,
      extraLayoutClass,
      error,
      inEditorEdit,
      showResults,
      mapCenter,
      selectedUnit,
      handleUnitSelected,
      stateChanged,
      validState,
      changeEngineContext,
      originAddress,
      selectedMarkerIndex,
      coveoSearchSection,
      hoverMapMarker,
      coveoResultTemplate
    }
  }
}
</script>

<style lang="scss">
  @import '~/node_modules/@coveo/atomic/dist/atomic/themes/coveo.css';
  @import '~bootstrap/scss/functions';
  @import '~/assets/sass/variables';
  @import '~bootstrap/scss/mixins';

  :root {
    --atomic-primary: #{$primary};
    --atomic-primary-background: #{$primary};
    --atomic-primary-light: #{$primary};
  }
  atomic-search-layout {
    display: grid
  }

  atomic-layout-section[section="main"] {
    overflow-y: auto;
    padding-bottom: 2rem;
  }

  atomic-layout-section[section="status"] {
    align-items: center;
    background-color: #{$gray-100};
    padding: 1rem 1rem;
  }

  atomic-layout-section[section="facets"] {
    margin: var(--atomic-layout-spacing-y) auto;
  }

  atomic-layout-section[section="map"] {
    grid-area: coveo-map;
    min-height: 37.5rem;
  }

  atomic-layout-section[section="map"],
  atomic-search-layout atomic-layout-section[section='main'] {
    max-height: calc(100vh - 7.5rem);
  }

  atomic-did-you-mean.hidden,
  atomic-layout-section.hidden {
    display: none;
  }

  atomic-layout-section[section="coveo-placeholder"] {
      grid-area: coveo-placeholder;
      height: 350px;
      background-color: $gray-100;
  }

  atomic-layout-section[section="coveo-placeholder"] .info {
      margin: 2.875rem auto;
      padding: 1rem;
      max-width: 29.5625rem;
      font-family: $font-family-sans-serif;
      border: 2px solid $gray-200;
      font-size: 1.125rem;
      display: flex;
      column-gap: 1rem;
      align-items: flex-start;
      line-height: 1.5rem;
  }

  atomic-search-layout.hide-results atomic-layout-section[section="main"],
  atomic-search-layout.hide-results atomic-layout-section[section="map"] {
    display: none;
  }

  .map-marker-container {
    display: flex;
    width: 1.5rem;
    height: 2rem;
    background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAABACAMAAABr/gR0AAABd1BMVEUAAAD/AAD/AAD/VQD/QAD/MwD/KwD/JAD/QAD/OQD/MwD/OwD/MwD/MAD/OQD/MQD/OgD/NQD/MwD/OwD/NQD/MwD/NgD/NgD/MwD/OAD/NAD/NAD/NQD/NQD/NQD/NwD/NgD/NAD/OAD/NwD/NwD/NwD/NgD/NQD/NQD/NwD/NgD/NQD/NQD/NgD/NgD/NQD/NgD/NQD/NQD/NwD/NgD/NwD/NwD/NgD/NgD/NQD/NgD/NQD/NQD/NwD/NQD/NgD/NgD/NgD/NgD/NQD/NgD/NgD/NgD/NgD/NgD/NQD/NQD/NQD/NwD/NgD/NQD/NgD/NgD/NwD/NgD/NgD/NgD/NgD/NgD/NgD/NQD/NgD/NgD/NgD/NgD/NwD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD/NgD///+8hC6oAAAAe3RSTlMAAQIDBAUGBwgJCg0PEBIVFhgZGh0eISYoKTE2P0NERUdJTk9TVFVWV1hZW1xeY2VoaWprbW9wcXd4e3yBg4aJjpKTlJibnKKlp6yxtre6wMHExcbHyMvMzc7T1NXX2Nna29zd3uDh4+Tn6Ovt7u/x8vT19vf4+fr7/f63VTyBAAAAAWJLR0R80bYgXwAAAa1JREFUGBmVwQlDElEYBdA7JCVZYumEZVa2mlmo7agVplYmtNAmZftCAQYaCHP/fIBGDPNm3vfOgcfg5EL6c7FWLX5I3TsfhcbAjTV2cp5PHYQ/+/4fepTuDkAtfL1MpfKtPVA4mqWvjA2PC2UGKJxEl6t1BqpOwGWGOs5ldBivU6s6irZYiQIFG7v2ZSnyJIQdsxS6ghZ7i0L5A2haoFgCDdEtiuUjAG7SwEUAb2hgFThME5UILtHIKJI0kkCaRh7gC41kUKSRdWzTSA7bNJLDBo28xw8aeY1nNLKCJRqZxwyNnIXt0EClD1ijgRSAKRo4A6D3F8U+htCQoFgcTZHvFHrXg5ZJCp3CDitDkcf4Z7hCgd9DaJujwDT+2/uWWi9C6DBSo8ZmDC5z1JiGW89LBkpb6DK8yQAbQ/C4xgDj8LJW6eshVAYL9JHrh9IE1Zwx+HhEpST89OeosL4fvk449KiNIECSHrMI0ptll1dhBDpWpUs5Bo0EXeLQCT1lh5QFrSMltuUPQSDOtnMQWeGuZchEf7Llax+ETrOpfhxii2yYh1z4zrdPty2o/AVcl+VIXpV+jAAAAABJRU5ErkJggg==");
    background-repeat: no-repeat;
    background-size: contain;
    justify-content: center;
    color: $white;
    font-family: $headings-font-family;
    font-size: 1.125rem;
    padding-top: 0.125rem;
    cursor: pointer;
  }

  .map-marker-container.selected,
  .map-marker-container.hovered{
    width: 2.875rem;
    height: 3.875rem;
    font-size: 2.25rem;
  }

  atomic-search-interface atomic-search-layout.hide-results {
    grid-template-areas:
      '.                 atomic-section-search                 .'
      'coveo-placeholder coveo-placeholder     coveo-placeholder';
  }

  atomic-search-interface:not(.atomic-search-interface-no-results, .atomic-search-interface-error) atomic-search-layout:not(.hide-results) {
    grid-template-areas:
      '.                    atomic-section-search                    .'
      'coveo-map            coveo-map                        coveo-map'
      'atomic-section-main  atomic-section-main    atomic-section-main';
  }

  .coveo-search {
    min-height: 29.875rem;
  }

  .warning-message {
    position: relative !important;
    top: unset !important;
    left: unset !important;
    margin: 0 auto;
    text-align: center;
    color: #{$primary};
    background: rgba(255,255,255, 0.8);
    width: fit-content;
  }

  @include media-breakpoint-down(lg) {
    atomic-layout-section[section="main"] {
      min-height: 50rem;
    }
  }

  @include media-breakpoint-down(md) {
    atomic-search-layout atomic-layout-section[section="search"] {
      margin-bottom: 0 !important;
      padding-left: 0 !important;
      padding-right: 0 !important;
    }

    atomic-search-layout {
      grid-template-columns: 1rem minmax(0, 1fr) 1rem;
    }

    .coveo-search {
      min-height: 39.875rem;
    }
  }

  @include media-breakpoint-down(sm) {
    #search-area-container {
      top:0;
      right:0;
      left:0;
      display: flex;
      justify-content: center;
      padding: 0 1rem;
    }

    #search-area-container > button {
      margin-right: 0 !important;
      width: 100%;
    }
  }

  atomic-search-layout atomic-layout-section[section="search"] {
    --atomic-layout-max-search-box-input-width: 100%;
  }

  @include media-breakpoint-up(lg) {
    atomic-search-layout atomic-layout-section[section="search"] {
      justify-self: center !important;
      display: flex !important;
      max-width: 1140px !important;
    }

    atomic-search-layout {
      grid-template-areas:
      '.                   atomic-section-search atomic-section-search            .'
      'atomic-section-main atomic-section-main   coveo-map                coveo-map' !important;
      grid-template-columns: 1fr minmax(360px, 500px) minmax(50%, 100%) 1fr !important;
      grid-template-rows: minmax(100px, auto) min-content !important;
      column-gap: 0px !important;
    }

    atomic-search-interface atomic-search-layout.hide-results {
      grid-template-areas:
        '.                   atomic-section-search atomic-section-search                  .'
        'coveo-placeholder   coveo-placeholder     coveo-placeholder      coveo-placeholder' !important;
    }

    atomic-search-interface:not(.atomic-search-interface-no-results, .atomic-search-interface-error) atomic-search-layout:not(.hide-results) {
      grid-template-areas:
        '.                   atomic-section-search atomic-section-search            .'
        'atomic-section-main atomic-section-main   coveo-map                coveo-map' !important;
    }

    atomic-search-interface:not(.atomic-search-interface-no-results, .atomic-search-interface-error) atomic-search-layout atomic-layout-section[section="facets"] {
      display: block;
    }

    atomic-search-layout atomic-layout-section[section="horizontal-facets"] > atomic-popover:not(.atomic-hidden) {
      display: block;
    }
  }
</style>
