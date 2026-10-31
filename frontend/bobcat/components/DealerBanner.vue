<template>
  <section data-test-id="dealer-locator-banner"
           class="container"
           :class="{'border-top border-color-gray-light' : isMobile}"
  >
    <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
      <div class="row">
        <div v-if="mdmId && !isFetching" class="col-12">
          <div :class="{'partner-heading': isPartner}">
            <component :is="htag"
                       :class="htag"
                       data-test-id="dealer-banner-title"
                       class="mb-0"
            >
              {{ title }}
            </component>
            <div v-if="isPartner"
                 data-test-id="dealer-banner-partner"
                 class="partner-callout text-gray-dark-medium mt-2 mb-3 mt-lg-0 mb-lg-0"
            >
              <p data-test-id="dealer-banner-partner-label" class="h4 mb-1">
                {{ partnerLabel }}
              </p>
              <div v-if="partnerNote"
                   data-test-id="dealer-banner-partner-note"
                   class="partner-note small"
                   v-html="partnerNote"
              />
            </div>
            <p v-if="address" data-test-id="dealer-banner-address" :class="isMobile ? 'h5' : 'h4'" class="mb-0">
              {{
                address
              }}
            </p>
            <p v-if="distance"
               data-test-id="dealer-banner-distance"
               :class="isMobile ? 'text-gray-dark-medium small' : 'h6'"
            >
              {{ distance }}
              <span v-if="distanceByCar"> / {{ distanceByCar }}</span>
            </p>
          </div>
          <div>
            <ul
              class="list-unstyled d-flex align-items-sm-center flex-row flex-wrap align-items-start position-relative mb-5 mb-sm-3"
            >
              <span class="line position-absolute d-block d-sm-none" />
              <li v-for="(item, index) in contactInfo"
                  :key="index"
                  :data-test-id="`dealer-banner-contact-${item.index}`"
                  class="font-weight-bold pb-1 d-flex align-items-center"
              >
                <a v-if="item.label"
                   :href="item.href"
                   target="_blank"
                   rel="noreferrer nofollow"
                   class="d-flex align-items-center text-decoration-none"
                   @click="() => $ga4.events.dealer_page_click('link',item.label, title)"
                >
                  <b-icon :icon="item.icon" class="mr-2" />
                  <span>{{ item.label }}</span>
                </a>
                <span v-if="item.label && item.hasSeparator" class="separator d-block bg-black mx-3" />
              </li>
            </ul>
            <div class="d-flex flex-column flex-md-row border-bottom border-color-gray-lighter pb-2">
              <div class="col-12 px-0" :class="{'col-md-8': !isNotNARegion}">
                <div class="d-flex w-100 flex-column flex-md-row">
                  <button
                    v-if="contactLabel"
                    data-test-id="dealer-banner-contact-button"
                    class="w-100 d-flex justify-content-center btn btn-primary d-flex align-items-center font-weight-bold"
                    @click="() => {$ga4.events.dealer_page_click('button', contactLabel, title); scrollToForm()}"
                  >
                    {{ contactLabel }}
                  </button>
                  <a
                    v-if="checkOtherDealersLabel"
                    data-test-id="dealer-banner-dealers-nearby-button"
                    :class="{'ml-md-3' : contactLabel}"
                    class="w-100 d-flex justify-content-center btn btn-outline-primary d-flex align-items-center font-weight-bold ml-0 mt-3 mt-md-0"
                    @click.prevent="searchDealersNearby"
                  >
                    {{ checkOtherDealersLabel }}
                  </a>
                </div>
                <div ref="mapRef"
                     data-test-id="dealer-banner-map"
                     class="mt-4 mb-5 mb-sm-0"
                     :class="{'w-100' : !isMobile}"
                     :style="{'width: 100vw; margin-left: -15px' : isMobile}"
                     style="height: 26.25rem;"
                />
              </div>
              <div v-if="contractCodes && contractCodes.length && !isNotNARegion"
                   data-test-id="dealer-banner-equipment"
                   class="col-12 col-md-4 px-0 mt-2"
              >
                <div class="d-flex pl-0 pl-sm-3 mt-0 mt-sm-6 pt-4 pt-sm-0"
                     :class="{'border-top border-color-gray-light' : isMobile}"
                >
                  <div>
                    <h2 class="h4 mb-0">{{ equipmentLabel }}</h2>
                    <ul class="mt-3 pl-4">
                      <li v-for="product in contractCodes" :key="product.name" class="equipment-item">
                        <a :href="product.url"
                           class="h5"
                           @click="() => $ga4.events.dealer_page_click('link',product.name, title)"
                        >{{ product.name }}</a>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="isNotNARegion" class="d-flex mt-4 flex-column flex-md-row">
            <div v-if="industriesLabel && industries && industries.length"
                 class="col-md-4 px-0 col-12"
                 data-test-id="dealer-industries"
            >
              <h2 class="h4">{{ industriesLabel }}</h2>
              <ul class="mt-3 pl-4">
                <li v-for="industry in industries" :key="industry">{{ industry }}</li>
              </ul>
            </div>
            <div v-if="equipmentLabel && contractCodes && contractCodes.length"
                 class="col-md-4 px-0 col-12"
                 data-test-id="dealer-equipment"
            >
              <h2 class="h4">{{ equipmentLabel }}</h2>
              <ul class="mt-3 pl-4">
                <li v-for="product in contractCodes" :key="product.name" class="equipment-item">
                  <a :href="product.url"
                     class="h5"
                     @click="() => $ga4.events.dealer_page_click('link',product.name, title)"
                  >{{ product.name }}</a>
                </li>
              </ul>
            </div>
            <div v-if="servicesLabel && services && services.length"
                 class="col-md-4 px-0 col-12"
                 data-test-id="dealer-services"
            >
              <h2 class="h4">{{ servicesLabel }}</h2>
              <ul class="mt-3 pl-4">
                <li v-for="service in services" :key="service">{{ service }}</li>
              </ul>
            </div>
          </div>
        </div>
        <div v-else-if="isFetching" class="col-12 text-center py-6">
          <b-spinner variant="primary" />
        </div>
        <div v-else-if="inEditorEdit"
             data-test-id="dealer-banner-alert"
             class="w-100 d-flex justify-content-center align-items-center"
        >
          <WarningAlert>
            This component can be visible only on public URL
          </WarningAlert>
        </div>
      </div>
    </LazyHydrate>
  </section>
</template>

<script>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, useContext, useRouter} from '@nuxtjs/composition-api'
import {HIDDEN_FIELDS_VALUES} from '../constants/hiddenFields'
import {deriveAddressCountry} from '../constants/addressCountry'
import {cleanValue, convertToPlain, formatPhone} from '../helpers/functional/AppHelpers'
import {getValidWebsiteUrl} from '../plugins/utils/coveoUtils'
import useGoogle from '~/helpers/composables/useGoogle'
import useDealerLocator from '~/helpers/composables/useDealerLocator'
import {DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE} from '~/constants/staticRoutes'
import {inEditorEdit} from '~/helpers/vue-editor'
import {BUSINESS_REGIONS, USER_COVEO_METADATA} from '~/constants'

// Produces a plain-text business name for the LocalBusiness schema:
// decodes HTML entities and strips any HTML tags/special markup.
const toPlainTextName = (value) => {
  if (!value) {
    return ''
  }
  let decoded = String(value)
  if (process.client && typeof document !== 'undefined') {
    const textarea = document.createElement('textarea')
    textarea.innerHTML = decoded
    decoded = textarea.value
  }
  return convertToPlain(decoded, true)
}

export default {
  props: {
    htag: {type: String, default: 'h2'},
    googleApiKey: {type: String},
    websiteLabel: {type: String},
    directionsLabel: {type: String},
    contactLabel: {type: String},
    checkOtherDealersLabel: {type: String},
    equipmentLabel: {type: String},
    servicesLabel: {type: String},
    industriesLabel: {type: String},
    formId: {type: String},
    mappings: {type: Object},
    markerIcon: {type: String},
    partnerLabel: {type: String, default: 'Bobcat Partner'},
    partnerNote: {type: String},
    partnerMarkerIcon: {type: String},
    hourLabel: {type: String},
    hoursLabel: {type: String},
    minuteLabel: {type: String},
    minutesLabel: {type: String}
  },
  setup(props) {
    const {
      googleApiKey,
      formId,
      mappings,
      markerIcon,
      partnerMarkerIcon,
      checkOtherDealersLabel,
      minutesLabel,
      minuteLabel,
      hoursLabel,
      hourLabel
    } = props
    const {$services, $utils, $ga4, $regions, $config} = useContext()
    const router = useRouter()
    const {
      saveData
    } = useDealerLocator()
    const {
      initMapWithCustomMarker,
      convertDistance,
      getAddressFromCoordinates,
      getCoordinates
    } = useGoogle()
    const mdmId = ref('')
    const title = ref('')
    const mailingLabel = ref('')
    const email = ref('')
    const distance = ref('')
    const googlePin = ref('')
    const phone = ref('')
    const address = ref('')
    const website = ref('')
    const latitude = ref(null)
    const longitude = ref(null)
    const currentDealerPath = router.currentRoute.path.substring(0, router.currentRoute.path.lastIndexOf('/'))
    const dealersNearbyURL = ref(currentDealerPath.includes(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)
      ? currentDealerPath
      : `/${$regions.current.name}/${$regions.current.language.name}${DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE}`)

    const contractCodes = ref([])
    const distanceByCar = ref('')
    const isFetching = ref(true)
    const mapRef = ref(null)
    const isNotNARegion = ref(!(BUSINESS_REGIONS.na.includes($regions.current.name)))
    const isEMEARegion = BUSINESS_REGIONS.emea.includes($regions.current.name)
    const isPartner = ref(false)
    const unit = ref(isNotNARegion.value ? 'km' : 'mi')
    const iconUrl = computed(() => $utils.getCloudinaryAsset(
      (isPartner.value && partnerMarkerIcon) || markerIcon, true, {width: 64, height: 64}
    ))
    const isMobile = ref($utils.isMobile())
    const services = ref([])
    const industries = ref([])
    const dealerSchema = ref(null)

    const equipmentMappings = mappings?.['@nodes']?.map(productMappingName => mappings[productMappingName])

    const formatDuration = (hours, minutes) => {
      let durationText = ''
      if (hours > 0) {
        durationText += `${hours} ${hours === 1 ? hourLabel : hoursLabel} `
      }

      if (minutes > 0) {
        durationText += `${minutes} ${minutes === 1 ? minuteLabel : minutesLabel}`
      }

      return durationText.trim()
    }

    const fetchDealerDetails = async () => {
      try {
        if (mdmId.value) {
          const [dealerDetails, codes] = await Promise.all([
            $services.dealerlocator.getDealerDetails(mdmId.value),
            $services.dealerlocator.getDealerContractCodes(mdmId.value, false)
          ])

          const uniqueContractCodes = new Map()

          equipmentMappings.forEach((productMapping) => {
            const productName = productMapping?.name?.toLowerCase()
            if (!productName) {
              return
            }

            const linkField = $utils.getLinkField(productMapping.page, $regions.current.language.name)

            codes.forEach((code) => {
              const fullName = code.fullName?.toLowerCase()
              if (productName === fullName && !uniqueContractCodes.has(fullName)) {
                uniqueContractCodes.set(fullName, {
                  name: (linkField.label && linkField.label !== 'link') ? linkField.label : productMapping.name,
                  url: linkField?.url
                })
              }
            })
          })

          contractCodes.value = Array.from(uniqueContractCodes.values())

          if (dealerDetails) {
            title.value = dealerDetails?.mailingLabel
            mailingLabel.value = dealerDetails?.mailingLabel
            phone.value = dealerDetails?.phone
            email.value = dealerDetails?.email
            website.value = dealerDetails?.website
            latitude.value = dealerDetails?.latitude
            longitude.value = dealerDetails?.longitude
            isPartner.value = isEMEARegion && String(dealerDetails?.isPartner).toLowerCase() === 'true'

            saveData(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_DEALER_NAME, title.value || '')
            saveData(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_MDM_ID, mdmId.value || '')
            saveData(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_OPT_IN, dealerDetails?.commerceOptIn || '')
            saveData(HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_CUSTOMER_NUMBER, dealerDetails?.customerNumber || '')

            if (isNotNARegion.value) {
              const dealerData = await $services.dealerlocator.getDealerIndustriesAndServices(mdmId.value)
              services.value = dealerData?.services
              industries.value = dealerData?.industries
            }

            if (dealerDetails.shippingStreet && dealerDetails.shippingCity) {
              address.value = [
                dealerDetails.shippingStreet,
                dealerDetails.shippingCity,
                isNotNARegion.value ? dealerDetails.shippingPostalCode : '',
                dealerDetails.shippingState,
                isNotNARegion.value ? dealerDetails.shippingCountry : ''
              ].filter(Boolean).join(', ')

              googlePin.value = `https://www.google.com/maps/search/?api=1&query=${address.value}`

              const coveoMetaData = JSON.parse(localStorage.getItem(USER_COVEO_METADATA))
              if (coveoMetaData?.q) {
                const {latitude, longitude} = await getCoordinates(coveoMetaData?.q, googleApiKey, coveoMetaData)
                const searchedAddress = await getAddressFromCoordinates(latitude, longitude, googleApiKey)

                if (searchedAddress) {
                  googlePin.value = `https://www.google.com/maps/dir/?api=1&origin=${searchedAddress}&destination=${address.value}`
                  const distanceByCarData = await $services.dealerlocator.calculateDealerDistanceByCar(searchedAddress, address.value)
                  if (distanceByCarData?.distance) {
                    const calculatedDistance = convertDistance(distanceByCarData.distance, unit.value)
                    distance.value = `${calculatedDistance} ${unit.value}`
                  }
                  if (!!distanceByCarData?.hours || !!distanceByCarData?.minutes) {
                    distanceByCar.value = formatDuration(distanceByCarData.hours, distanceByCarData.minutes)
                  }
                }
              }
            }
            // Official business name as plain text (entities decoded, tags stripped).
            const schemaName = toPlainTextName(dealerDetails.mailingLabel)
            // Primary dealer phone, normalized with country code. '' when absent.
            const schemaTelephone = formatPhone(dealerDetails.phone, !isNotNARegion.value)

            const schemaUrl = mdmId.value
              ? `${$config.PUBLIC_HOST}${router.currentRoute.path}`
              : ''
            const schemaStreet = cleanValue(dealerDetails.shippingStreet)
            const schemaLocality = cleanValue(dealerDetails.shippingCity)
            const schemaRegion = cleanValue(dealerDetails.shippingState)
            const schemaPostalCode = cleanValue(dealerDetails.shippingPostalCode)
            // country code field in SF for NA region is different from other regions
            const explicitCountry = isNotNARegion.value ? dealerDetails.shippingCountryCode : dealerDetails.shippingCountry
            // Use the explicit country when present, otherwise derive it from the
            // region (US state / Canadian province). '' when it can't be determined.
            const schemaCountry = deriveAddressCountry(explicitCountry, schemaRegion)
            
            if (schemaName) {
              dealerSchema.value = {
                '@context': 'https://schema.org',
                '@type': 'LocalBusiness',
                name: schemaName,
                address: {
                  '@type': 'PostalAddress',
                  ...(schemaStreet ? {streetAddress: schemaStreet} : {}),
                  ...(schemaLocality ? {addressLocality: schemaLocality} : {}),
                  ...(schemaRegion ? {addressRegion: schemaRegion} : {}),
                  ...(schemaPostalCode ? {postalCode: schemaPostalCode} : {}),
                  ...(schemaCountry ? {addressCountry: schemaCountry} : {})
                },
                ...(schemaUrl ? {url: schemaUrl} : {}),
                ...(schemaTelephone ? {telephone: schemaTelephone} : {})
              }
            }
          }
        }
        isFetching.value = false
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error)
        isFetching.value = false
      }
    }

    const scrollToForm = () => {
      if (document.activeElement) {
        document.activeElement.blur()
      }
      $utils.scrollToElement(formId)
    }

    const contactInfo = computed(() => [
      {
        icon: 'phone',
        href: `tel:${phone.value}`,
        label: phone.value,
        hasSeparator: true,
        visible: !!phone.value
      },
      {
        icon: 'email',
        href: `mailto:${email.value}`,
        label: email.value,
        hasSeparator: true,
        visible: isNotNARegion.value && !!email.value
      },
      {
        icon: 'launch',
        href: getValidWebsiteUrl(website.value),
        label: props.websiteLabel,
        hasSeparator: true,
        visible: !!website.value
      },
      {
        icon: 'directions',
        href: googlePin.value,
        label: props.directionsLabel,
        hasSeparator: false,
        visible: !!googlePin.value
      }
    ].filter(item => item.visible))

    const setMapSize = () => {
      isMobile.value = $utils.isMobile()
      if (mapRef.value) {
        mapRef.value.style.height = $utils.isMobile() ? '14rem' : '26.25rem'
      }
    }

    onMounted(async () => {
      const urlParam = router.currentRoute.path.split(DEALER_LOCATOR_PAGE_TEMPLATE_ROUTE)[1]?.replace('/', '')
      if (Number.isInteger(parseInt(urlParam))) {
        mdmId.value = urlParam
        await fetchDealerDetails()
        initMapWithCustomMarker(mapRef, address.value, googleApiKey, iconUrl.value, latitude.value, longitude.value)
        setMapSize()
        nextTick(() => {
          window.addEventListener('resize', setMapSize, {passive: true})
        })
      } else {
        isFetching.value = false
      }
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', setMapSize)
    })

    const searchDealersNearby = () => {
      if (process.client && window && dealersNearbyURL.value) {
        const coveoMetaData = JSON.parse(localStorage.getItem(USER_COVEO_METADATA))
        if (coveoMetaData) {
          localStorage.setItem(USER_COVEO_METADATA, JSON.stringify({...coveoMetaData, isDealerLandingPage: true}))
        }
        $ga4.events.dealer_page_click('button', checkOtherDealersLabel, title.value)
        window.location.href = dealersNearbyURL.value
      }
    }

    return {
      title,
      address,
      distance,
      phone,
      mailingLabel,
      email,
      website,
      googlePin,
      mdmId,
      contractCodes,
      isFetching,
      contactInfo,
      scrollToForm,
      $ga4,
      inEditorEdit,
      dealersNearbyURL,
      mapRef,
      searchDealersNearby,
      isMobile,
      distanceByCar,
      isNotNARegion,
      isPartner,
      services,
      industries,
      dealerSchema
    }
  },
  jsonld() {
    return this.dealerSchema
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

section[data-test-id="dealer-locator-banner"] {
  .partner-note p:last-child {
    margin-bottom: 0;
  }

  .partner-heading {
    @include media-breakpoint-up(lg) {
      display: grid;
      grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
      grid-template-rows: auto auto auto 1fr;
      grid-template-areas:
        'title partner'
        'address partner'
        'distance partner'
        '. partner';
      column-gap: 2rem;

      [data-test-id="dealer-banner-title"] {
        grid-area: title;
      }

      [data-test-id="dealer-banner-address"] {
        grid-area: address;
      }

      [data-test-id="dealer-banner-distance"] {
        grid-area: distance;
      }

      .partner-callout {
        grid-area: partner;
        padding-top: 0.5rem;
      }
    }
  }

  ul {
    .line {
      width: 100vw;
      height: 1px;
      background: $gray-200;
      right: -15px;
      bottom: -1rem;
    }

    li {
      &.equipment-item::marker {
        color: $primary
      }

      a:hover {
        span {
          text-decoration: underline;
        }
      }

      .separator {
        width: 1px;
        height: 1.5rem;
      }
    }
  }
}
</style>
