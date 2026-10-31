<template>
  <div v-if="state.results && state.results.length">
    <div class="list-wrapper">
      <div class="list-root display-list">
        <div
          v-for="(result, index) in state.results"
          :key="result.uniqueId"
          :class="['result-component', 'result-' + index, { 'hovered': hoveredIndex === index }]"
          @mouseenter="onMouseEnter(index)"
          @mouseleave="onMouseLeave(index)"
          @click="handleCardClick(result)"
        >
          <div
            class="result-root display-list density-normal image-icon with-sections"
          >
            <div class="result-section-visual">
              <div class="counter">
                {{ index + 1 }}
              </div>
            </div>
            <div class="result-section-title">
              <a class="result-link" :href="dynamicHref(result)" @click="event => {event.stopPropagation();logAnalytics(result, 'View Dealer')}">
                <div class="title-wrapper">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M13.125 10.875H4.125C3.45 10.875 3 11.325 3 12C3 12.675 3.45 13.125 4.125 13.125H13.125V18.75L21 12L13.125 5.25V10.875Z" fill="#333333" />
                  </svg>
                  <div class="result-title pl-2 pr-3">
                    {{ result.raw.sfmailing_label__c }}
                  </div>
                </div>
              </a>
            </div>
            <div class="result-section-metadata">
              <div class="bobc-addr">
                <div class="field">
                  {{ result.raw.bobc_address }}
                </div>
              </div>
              <div class="distances-container">
                <div v-if="selectedUnit && result.raw.googleData?.distance" class="distances">
                  <CoveoDistanceToDestination :distance="result.raw.googleData?.distance"
                                              :map-center="mapCenter"
                                              :unit="selectedUnit"
                                              :mi-label="miLabel"
                                              :km-label="kmLabel"
                  />
                  <div class="units-divider">/</div>
                  <CoveoTimeToDestination :duration="result.raw.googleData?.duration" />
                </div>
              </div>
              <div class="bottom-hyperlinks">
                <a v-if="result.raw.sfphone" :href="'tel:' + result.raw.sfphone" @click="event => {event.stopPropagation();logAnalytics(result, 'Phone Number'); }">
                  <div class="cta-label-container">
                    <b-icon icon="phone" class="cta-icon mr-2" />
                    <div class="cta-label">
                      <div class="cta-call-label">
                        Call
                      </div>
                      <div class="cta-phone-label">
                        {{ result.raw.sfphone }}
                      </div>
                    </div>
                  </div>
                </a>
                <span v-if="result.raw?.sfphone" class="hyperlinks-divider" />
                <a v-if="result.raw.sfwebsite && visitWebsiteLabel" :href="getValidWebsiteUrl(result.raw?.sfwebsite)" target="_blank" @click="event => {event.stopPropagation();logAnalytics(result, 'Visit Website')}">
                  <div class="cta-label-container">
                    <b-icon icon="launch" class="cta-icon mr-2" />
                    <div class="cta-label">
                      {{ visitWebsiteLabel }}
                    </div>
                  </div>
                </a>
                <span v-if="result.raw?.sfwebsite && getDirectionsLabel" class="hyperlinks-divider" />
                <a v-if="result.raw?.bobc_address && getDirectionsLabel" :href="getMapLink(result.raw.bobc_address)" target="_blank" @click="event => {event.stopPropagation();logAnalytics(result, 'Get Directions')}">
                  <div class="cta-label-container">
                    <b-icon icon="directions" class="cta-icon mr-2" />
                    <div class="cta-label">
                      {{ getDirectionsLabel }}
                    </div>
                  </div>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {buildResultList, loadClickAnalyticsActions, loadGenericAnalyticsActions} from '@coveo/headless'
import {nextTick, onMounted, ref, useContext, watch} from '@nuxtjs/composition-api'
import {getValidWebsiteUrl} from '../../plugins/utils/coveoUtils'
import useLocation from '../../helpers/composables/useLocation'

export default {
  props: {
    engine: {
      type: Object
    },
    googleApiKey: {
      type: String
    },
    mapCenter: {
      type: Object,
      required: true
    },
    selectedUnit: {
      type: String
    },
    visitWebsiteLabel: {
      type: String
    },
    getDirectionsLabel: {
      type: String
    },
    originAddress: {
      type: String
    },
    selectedMarkerIndex: {
      type: Number
    },
    productModelField: {
      type: String
    },
    industryModelField: {
      type: String
    },
    servicesModelField: {
      type: String
    },
    miLabel: {
      type: String
    },
    kmLabel: {
      type: String
    }
  },
  emits: ['change-engine-context', 'result-template-hovered'],
  setup(props, {emit, expose}) {
    const {$ga4, $utils, $regions} = useContext()

    const {engine, productModelField, industryModelField, servicesModelField} = props
    const {logDocumentOpen} = loadClickAnalyticsActions(engine)
    const {logCustomEvent} = loadGenericAnalyticsActions(engine)
    const state = ref({
      results: []
    })
    const eventType = 'dealer_locator_event'
    const isFirstMapEvent = ref(false)
    const hoveredIndex = ref(null)
    const regionInfo = {
      name: $regions?.current?.name
    }
    const gtmEventMapping = {
      searchArea: 'search_this_area',
      pinClick: 'locator_map_clicked',
      phoneNumber: 'locator_map_click_to_call',
      visitWebsite: 'locator_map_click_to_web',
      getDirections: 'locator_map_click_for_directions',
      viewDealer: 'dealer_view_dealer_details'
    }
    const {zipCode} = useLocation()

    const onMouseEnter = (index) => {
      hoveredIndex.value = index
      emit('result-template-hovered', {index, ishovered: true})
    }

    const onMouseLeave = (index) => {
      hoveredIndex.value = null
      emit('result-template-hovered', {index, ishovered: false})
    }

    const highlightAndScroll = (index) => {
      const resultListContainer = document.querySelector('atomic-layout-section[section="main"]')
      const element = document.querySelector(`.result-component.result-${index}`)
      hoveredIndex.value = index
      if (element) {
        const offset = element.offsetTop - resultListContainer.offsetTop
        resultListContainer.scrollTo({
          top: offset - 10,
          behavior: 'smooth'
        })
        element.classList.add('selected')
      }
    }

    watch(() => props.selectedMarkerIndex, (index) => {
      if (index) {
        nextTick(() => {
          highlightAndScroll(index - 1)
        })
      }
    })

    const setEngineAdditionalContext = (additionalContext) => {
      emit('change-engine-context', undefined, false, additionalContext)
    }
    const logGtmEvents = (eventPayload) => {
      $ga4.events.coveo_analytics_events(eventPayload)
    }

    const createContextAndPayloads = (result, cause) => {
      const engineState = engine.state
      const searchLocation = engine.state?.search?.queryExecuted
      const currentValues = engine.state?.facetSet?.[productModelField]?.request?.currentValues
      const currentValuesArray = currentValues?.filter(item => item.state === 'selected').map(item => item.value)
      const storeLocation = result.raw.sfmailing_label__c

      if (regionInfo.name !== 'na') {
        // For non-na regions, removes space from cause and change to camelCase (For example: "Phone Number" to "phoneNumber").
        // Reason: NA regions use 'Phone Number' and non-NA regions use 'phoneNumber' as the cause
        cause = cause?.replace(/\s+/g, '')
        cause = cause?.charAt(0).toLowerCase() + cause?.slice(1)
      }

      const additionalContext = {
        search_location: searchLocation,
        item_category: currentValuesArray,
        store_location: storeLocation
      }

      const coveoEventPayload = {
        evt: cause,
        type: eventType
      }

      let gtmEventPayload = {
        cta_type: cause,
        search_location: searchLocation,
        item_category: currentValuesArray,
        store_location: storeLocation
      }

      // Change gtm payload for non-NA regions
      if (regionInfo.name !== 'na') {
        gtmEventPayload = {
          cta_type: gtmEventMapping[cause],
          dealer_address: result?.raw?.bobc_address
        }
        // Edge case of gtm event for phone number event
        if (cause === 'phoneNumber') {
          gtmEventPayload = {
            cta_type: gtmEventMapping[cause],
            tel_number: result?.raw?.sfphone
          }
        }
        // Add Website to gtm payload when cause is 'visitWebsite'
        if (cause === 'visitWebsite') {
          gtmEventPayload = {
            ...gtmEventPayload,
            website: result?.raw?.sfwebsite
          }
        }
        // Add Language Preference param to gtm payload when cause is 'viewDealer'
        if (cause === 'viewDealer') {
          gtmEventPayload = {
            ...gtmEventPayload,
            language_preference: $regions.current?.locale?.toLowerCase()
          }
        }
      }

      return {additionalContext, coveoEventPayload, gtmEventPayload, engineState}
    }

    const updateEngineContextAndMetadata = (additionalContext, engineState, dealerAddress) => {
      setEngineAdditionalContext(additionalContext)
      $utils.setUserMetadataStorage({
        ...engineState,
        dealerAddress
      }, productModelField, industryModelField, servicesModelField)
    }

    const logAnalytics = (result, cause) => {
      const {additionalContext, coveoEventPayload, gtmEventPayload, engineState} = createContextAndPayloads(result, cause)

      updateEngineContextAndMetadata(additionalContext, engineState, result.raw.bobc_address)
      logGtmEvents(gtmEventPayload)

      if (cause === 'View Dealer') {
        engine.dispatch(logDocumentOpen(result))
      } else {
        engine.dispatch(logCustomEvent(coveoEventPayload))
      }
    }

    const logMapAnalytics = (cause) => {
      // Log an event for the first map click interaction
      if (isFirstMapEvent.value === false) {
        logGtmEvents({cause: 'map_click_first'})
        isFirstMapEvent.value = true
      }

      // Handle the cause of "search this area" which doesn't have a lot of data to log
      const coveoEventPayload = {
        evt: cause,
        type: eventType
      }
      let gtmEventPayload = {
        cause: gtmEventMapping[cause]
      }

      // If cause of pin click retrieve more data about the result to log
      if (cause === 'pinClick' && props.selectedMarkerIndex > 0) {
        const result = state.value.results[props.selectedMarkerIndex - 1]
        const {additionalContext, gtmEventPayload: updatedGtmEventPayload, engineState} = createContextAndPayloads(result, cause)

        gtmEventPayload = updatedGtmEventPayload

        updateEngineContextAndMetadata(additionalContext, engineState, result.raw.bobc_address)
      }

      logGtmEvents(gtmEventPayload)
      engine.dispatch(logCustomEvent(coveoEventPayload))
    }

    const handleCardClick = (result) => {
      logAnalytics(result, 'View Dealer')
      window.location.href = dynamicHref(result)
    }
    const dynamicHref = (result) => {
      if (process.client && window) {
        return `${window.location.protocol}//${window.location.host}${window.location.pathname}/${result.raw.sfmdm_id__c}`
      }
    }

    const getMapLink = (address) => {
      return `https://www.google.com/maps/search/?api=1&query=${address}`
    }

    watch(state, (newVal) => {
      if (newVal && newVal.hasResults === false) {
        const location = (props.originAddress.includes(zipCode.value) && zipCode.value) ? zipCode.value : props.originAddress
        $ga4.events.dealer_not_found_event(location)
      }
    })

    onMounted(() => {
      const controller = buildResultList(engine)
      controller.subscribe(() => {
        state.value = controller.state.isLoading ? {results: []} : controller.state
      })
    })

    expose({
      logMapAnalytics
    })

    return {
      state,
      logAnalytics,
      getMapLink,
      dynamicHref,
      hoveredIndex,
      onMouseEnter,
      onMouseLeave,
      handleCardClick,
      getValidWebsiteUrl
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.list-wrapper {
  word-break: break-word;
}

.list-root.display-list {
  display: flex;
  flex-direction: column;
}

.result-component {
  cursor: pointer;
}
.result-component.hovered,
.result-component.selected {
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.5);
}
.result-title{
  font-size: 1.125rem;
}
.counter,
.result-title {
  font-family: $headings-font-family;
  font-style: normal;
  line-height: 1.5rem;
}
.list-root.display-list .result-component, .list-root.display-list atomic-result-placeholder {
    width: auto;
    font-family: var(--atomic-font-family);
    font-weight: var(--atomic-font-normal);
    display: block;
    --tw-ring-inset: var(--tw-empty);
    line-height: 1.5;
    -webkit-text-size-adjust: 100%;
    -moz-tab-size: 4;
    -o-tab-size: 4;
    tab-size: 4;
    font-family: var(--atomic-font-family);
    font-feature-settings: normal;
    font-variation-settings: normal;
    -webkit-tap-highlight-color: transparent;
    padding: 1rem;
    border-bottom: 1px solid var(--atomic-neutral);
}
.result-root.with-sections {
    display: grid;
    justify-items: stretch;
}
.result-section-visual {
  grid-area: visual;
  border-radius: var(--atomic-border-radius);
  overflow: hidden;
  text-overflow: ellipsis;
}
.counter {
    display: flex;
    justify-content: center;
    align-items: center;
    background: var(--atomic-primary);
    color: white;
    width: 1.5rem;
    height: 1.5rem;
    border-radius: 1.5rem;
    font-size: 1rem;
}
.result-section-title {
  grid-area: title;
  color: var(--atomic-on-background);
  font-family: $headings-font-family;
  font-size: 1.125rem;
  font-style: normal;
  line-height: 1.5rem !important;
}
.result-section-metadata {
  grid-area: bottom-metadata;
}
.result-link {
  color: var(--atomic-on-background);
}
.title-wrapper {
    display: flex;
    flex-direction: row-reverse;
    justify-content: space-between;
}
.result-link:hover .title-wrapper,
.result-link:active .title-wrapper {
  color: var(--atomic-primary);
}
.result-link:hover svg path,
.result-link:active svg path {
  fill: var(--atomic-primary);
}

.result-link:hover {
  text-decoration: underline;
  text-decoration-color: var(--atomic-primary);
}

.result-link:active {
  text-decoration-color: var(--atomic-primary);
}
.bobc-addr {
  margin-top: 0px !important;
  color: #000;
  font-family: $font-family-base;
  font-size: 1rem;
  font-style: normal;
  line-height: 1.5rem;
}
.field{
  display: flex;
  align-items: center;
  max-width: 100%;
}
.distances-container {
  display: flex;
  margin-top: 0.5rem;
  color: var(--Dark-Grey, #777);
  text-align: center;
  font-family: $font-family-base;
  font-size: 0.875rem;
  font-style: normal;
  line-height: 1.25rem;
  gap: 0.5rem;
}
.distances {
  display: flex;
}
.units-divider {
  padding: 0rem 0.5rem;
}
.cta-label-container {
  display: flex;
}
.cta-label {
  font-family: $headings-font-family;
  font-size: 1.125rem;
  font-style: normal;
  line-height: normal;
}
.cta-icon:hover {
  text-decoration: none;
}
.cta-call-label {
  display: none;
}
.bottom-hyperlinks {
  margin-top: 0.5rem;
  color: var(--atomic-primary);
  font-family: $headings-font-family;
  font-size: 0.875rem;
  font-style: normal;
  line-height: normal;
  display: flex;
  align-items: center;
  align-self: stretch;
}
.hyperlinks-divider {
    width: 1px;
    height: 1.5rem;
    background: #000;
    margin: 0 0.5rem;
}

@media (min-width: 992px) {
    .result-root.with-sections.display-list.image-icon {
        grid-template-areas:
        'badges badges . actions'
        'visual title title title'
        'title-metadata title-metadata title-metadata title-metadata'
        'emphasized emphasized emphasized emphasized'
        'excerpt excerpt excerpt excerpt'
        'bottom-metadata bottom-metadata bottom-metadata bottom-metadata'
        'children children children children' !important;
        grid-template-rows: repeat(7, auto) !important;
        grid-template-columns: minmax(0, min-content) auto 1fr auto;
    }
    .result-root.with-sections.display-list.image-icon .result-section-visual {
        width: 2rem;
        height: 2rem;
    }

    .result-root.with-sections.display-list.image-icon .result-section-title {
      --font-size: var(--atomic-text-xl);
      font-size: var(--font-size);
      --line-height: calc(var(--font-size)* var(--atomic-line-height-ratio));
      line-height: var(--line-height);
    }

    .result-component:not(:last-child){
      border-bottom: 1px solid var(--atomic-neutral);
    }
}

@media not all and (min-width: 992px) {
  .result-root.with-sections.display-list.image-icon {
      grid-template-areas:
      'badges badges'
      'visual title'
      'bottom-metadata bottom-metadata';
      grid-template-columns: auto minmax(0, 1fr);
      grid-template-rows: repeat(2, auto) 1fr;
  }
  .cta-call-label {
    display: block
  }
  .cta-phone-label {
    display: none
  }
  .result-section-metadata {
    margin-top: 0.5rem;
  }
  .distances-container {
    margin: 0.5rem 0;
  }
}
</style>
