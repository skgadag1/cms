<template>
  <section data-test-id="dealer-inventory-model-pricing" class="py-4">
    <div v-if="model" class="container model-pricing">
      <div class="row align-items-stretch">
        <WarningAlert v-if="inEditorEdit" type="info">Dealer Inventory Pricing needs parameters, won't be visible in Edit mode</WarningAlert>
        <LazyHydrate never>
          <div class="col-12 d-lg-none">
            <component :is="!desktop ? htag : 'div'"
                       class="mb-2 mr-md-2 d-block d-md-inline-block d-lg-none"
                       data-test-id="dealer-model-pricing-title__mobile"
            >
              <span class="dealer-model-pricing-title" v-html="model.name" />
              <div v-if="model.productTypeLabel"
                   class="h3 d-block d-md-inline-block text-capitalize"
                   data-test-id="dealer-model-pricing-subtitle__mobile"
                   v-html="model.productTypeLabel"
              />
            </component>

            <div v-if="model.serialNumber" class="d-lg-none">
              <h4 class="dealer-model-serial-number d-inline-block"
                  data-test-id="dealer-model-pricing-production-year__mobile"
              >
                {{ labels.serialNumber || "Serial #:" }} {{ model.serialNumber }}
              </h4>
            </div>

            <div v-if="price" class="d-flex flex-column w-100 align-items-sm-center flex-sm-row">
              <div class="h2 mb-0 mr-2 text-primary">{{ price }}</div> <div class="h4 mb-0">{{ labels.asConfigured || "as configured" }}</div>
            </div>
          </div>
        </LazyHydrate>

        <div v-if="images && images.length" class="col-12 col-md-6 mb-4 mb-md-0">
          <CloudinaryGallery :images="images" />

          <LazyHydrate v-if="priceDisclaimerEnabled && model.hideDisclaimer !== 'true'" never>
            <WarningAlert type="info" :title="generateConceptHint('priceDisclaimer')">{{ labels.priceDisclaimer || "Price may vary depending on configuration and options. Contact dealer for final pricing." }}</WarningAlert>
          </LazyHydrate>
        </div>

        <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
          <div class="col-12 col-md-6">
            <component :is="desktop ? htag : 'div'"
                       data-test-id="dealer-model-pricing-title_desktop"
                       class="d-none d-lg-block mb-lg-2"
            >
              <span class="dealer-model-pricing-title" v-html="model.name" />
              <div v-if="model.productTypeLabel"
                   class="h3 text-capitalize"
                   data-test-id="dealer-model-pricing-subtitle_desktop"
                   v-html="model.productTypeLabel"
              />
            </component>

            <h4 v-if="model.serialNumber" class="dealer-model-serial-number d-none d-lg-block">{{ labels.serialNumber || "Serial #:" }} {{ model.serialNumber }}</h4>

            <div v-if="price" class="d-none d-lg-flex w-100 align-items-center mb-4">
              <div class="h2 mb-0 mr-2 text-primary">{{ price }}</div> <div class="h4 mb-0">{{ labels.asConfigured || "as configured" }}</div>
            </div>

            <div v-if="status" class="mb-4">
              {{ labels.statusLabel || "Status:" }}
              <b-badge pill :variant="status === STOCK_STATUSES.IN_STOCK.value ? 'success' : 'primary'">
                <span>
                  {{ statusName }}
                </span>
              </b-badge>
            </div>

            <div v-else class="bg-gray-lighter p-3 rounded-2 mt-3 border-dark border-2 alert">
              <h5 class="d-flex align-items-center mb-0" :title="generateConceptHint('contactForPrice')"><b-icon icon="info" class="mr-2" />{{ labels.contactForPrice || "Contact for Price" }}</h5>
            </div>

            <a
              v-if="displayReserveBtn"
              data-test-id="reserve-btn"
              :href="reservationUrl"
              target="_blank"
              rel="noopener nofollow"
              class="h5 w-100 mt-4 font-weight-bold"
              @click="(e) => {$ga4.events.product_page_cta(e, model);}"
            >
              {{ labels.reserveNow || "Reserve Now" }} {{ price }} <b-icon icon="chevron-right rot" />
            </a>

            <div v-if="dealerName" class="d-flex flex-column mt-3">
              <div v-if="dealerName" class="d-flex align-items-start font-weight-bold">
                <span class="d-flex h4 mb-0 mt-1 text-primary">
                  <b-icon icon="main-pin" />
                </span>
                <div class="d-flex flex-column ml-2">
                  <h4 class="mb-1">
                    {{ dealerName }}
                  </h4>
                  <p v-if="distance && metric" class="h5 mb-0">
                    {{ distance }} {{ metric }} {{ labels.away || "away" }}
                  </p>
                </div>
              </div>
              <a v-if="phone"
                 :href="`tel:${phone}`"
                 class="ml-5 h5 text-black mt-2 mb-0"
                 @click="$ga4.events.click_to_call($event, model)"
              >
                {{ phone }}
              </a>
            </div>

            <button
              v-if="displayScrollBtn && displayScrollBtn === 'true'"
              data-test-id="view-specifications-btn"
              class="w-100 btn btn-lg btn-primary mt-4 d-flex align-items-center font-weight-bold"
              @click="handleContactFormLink"
            >
              <span class="w-100 align-items-center justify-content-center d-flex pe-none">
                <b-icon icon="email" class="mr-1" />
                {{ labels.contactDealer || "Contact Dealer" }}
              </span>
            </button>

            <div class="d-flex align-items-center mt-3 w-100 flex-lg-row flex-column">
              <button
                v-if="disableDownloadButton === 'false'"
                data-test-id="info-sheet-download-btn"
                :disabled="!componentLoaded || isDownloadInProgress"
                class="w-100 btn btn-lg btn-outline-primary d-flex align-items-center mr-0 mb-3 font-weight-bold"
                @click="(e) => {$ga4.events.product_page_cta(e, model); printPage()}"
              >
                <span class="w-100 align-items-center justify-content-center d-flex">
                  <b-icon v-if="!isDownloadInProgress" icon="list" class="mr-1" />
                  <b-spinner v-if="isDownloadInProgress" variant="primary" class="mr-2" small />
                  {{ labels.infoSheet || "Download Info Sheet" }}
                </span>
              </button>
            </div>
          </div>
        </LazyHydrate>
      </div>
    </div>
    <WarningAlert v-else-if="inEditorEdit">Model not configured in page url</WarningAlert>
  </section>
</template>

<script>
import {computed, inject, onMounted, ref, useContext, useFetch, useRouter} from '@nuxtjs/composition-api'
import {setWithExpiry} from '../helpers/functional/AppHelpers'
import {DEALER_INVENTORY_KEYS} from '../constants/localStorageKeys'
import {STOCK_STATUSES} from '~/constants/dealerInventory'
import useBreakpoints from '~/helpers/composables/useBreakpoints'
import {inEditorEdit} from '~/helpers/vue-editor'
import useConcepts from '~/helpers/composables/useConcepts'
import useMounted from '~/helpers/composables/useMounted'

export default {
  props: {
    nodeId: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    displayScrollBtn: {
      type: String,
      default: 'false'
    },
    disableDownloadButton: {
      type: String,
      default: 'false'
    },
    formId: {
      type: String
    },
    priceDisclaimer: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {generateConceptHint} = useConcepts()
    const model = inject('model') || {}
    const images = model?.gallery?.filter(img => img?.src || img?.tag) || []
    const {desktop} = useBreakpoints()
    const router = useRouter()
    const {route, $services, $utils, $regions, $ga4, $config} = useContext()
    const distance = ref(decodeURI(route.value.query?.distance || ''))
    const metric = ref(decodeURI(route.value.query?.metric || ''))
    const phone = ref(decodeURI(route.value.query?.phone || ''))
    const status = ref(STOCK_STATUSES[model?.status]?.value || '')
    const currency = ref(decodeURI(route.value.query?.currency || 'USD'))
    const labels = ref({})
    const componentLoaded = ref(false)
    const dealerName = ref(model?.dealer?.name?.replace(/,([\w])/g, ', $1') || null)
    const isDownloadInProgress = ref(false)
    const displayReserveBtn = ref(model?.dealer?.commerceOptIn && model?.status !== STOCK_STATUSES.COMING_SOON.key)
    const reservationUrl = ref('')
    useMounted(props.nodeId)

    if (model?.serialNumber) {
      reservationUrl.value = `${$config.BOBCAT_PAYMENT_GATEWAY}${$config.BOBCAT_PAYMENT_GATEWAY_INVENTORY}?serialNumber=${model.serialNumber}`
    } else {
      displayReserveBtn.value = false
    }

    const price = computed({
      get: () => {
        const estimatedNetPrice = currency.value === 'USD' ? (model.pricing?.estimatedNetPrices?.find(x => x.currency === 'USD')) : (model.pricing?.estimatedNetPrices?.find(x => x.currency === 'CAD'))
        if (!estimatedNetPrice) {
          return false
        }
        const price = estimatedNetPrice?.value.toLocaleString('en-us', {currency: estimatedNetPrice.currency, currencyDisplay: 'code', maximumFractionDigits: 0, minimumFractionDigits: 0})
        return `$${price} ${estimatedNetPrice.currency}`
      },
      set: () => {
      }
    })

    const scrollToForm = () => {
      if (document.activeElement) {
        document.activeElement.blur()
      }
      $utils.scrollToElement(props?.formId || 'model-specific-lead-form')
    }

    const printPage = async () => {
      const routeData = router.resolve({
        path: router.currentRoute.path,
        query: {
          ...router.currentRoute.query,
          print: 'true'
        }
      })
      window.open(routeData.href, '_blank')
    }

    const statusName = computed(() => {
      if (status.value === STOCK_STATUSES.IN_STOCK.value) {
        return labels.value.inStock || STOCK_STATUSES.IN_STOCK.value
      }
      if (status.value === STOCK_STATUSES.COMING_SOON.value) {
        return labels.value.comingSoon || STOCK_STATUSES.COMING_SOON.value
      }
      return status.value
    })

    if (process.client) {
      onMounted(() => {
        componentLoaded.value = true

        if (model) {
          setWithExpiry(DEALER_INVENTORY_KEYS.LAST_VISITED_MODEL, {title: model.title, name: model.name, '@name': model['@name']})
        }
      })
    }

    useFetch(async () => {
      if ($regions?.current?.name === 'na') {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory', '/common/labels/functional', '/%region%/labels/dealerinventory/products'])
        const match = Object.keys(labels.value).find(prefix => model?.id?.startsWith(prefix))
        model.pricing = {...model.pricing, reservePrice: labels.value?.[match]}
      } else {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
      }
    })

    const handleContactFormLink = (e) => {
      $ga4?.events?.form_submission_attempt(props?.formId)
      $ga4.events.product_page_cta(e, model)
      scrollToForm()
    }

    return {
      price,
      labels,
      desktop,
      images,
      model,
      distance,
      metric,
      inEditorEdit,
      status,
      statusName,
      STOCK_STATUSES,
      currency,
      generateConceptHint,
      phone,
      dealerName,
      componentLoaded,
      isDownloadInProgress,
      printPage,
      scrollToForm,
      priceDisclaimerEnabled: props.priceDisclaimer === 'true',
      displayReserveBtn,
      reservationUrl,
      handleContactFormLink
    }
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

.dealer-model-pricing-title {
  font-size: 3rem;
}

.dealer-model-serial-number {
  font-size: 1.5rem;
}

.list-unstyled.list-links {
  li:last-child {
    padding-bottom: 0 !important;
  }
}

.model-pricing {
  .cld-gallery {
    @include media-breakpoint-up(md) {
      min-height: calc((50vw - 2rem) * .75 + 1rem);

      &-multi {
        min-height: calc((50vw - 2rem) * .75 + 121px + 1rem);
      }
    }

    @include media-breakpoint-up(xl) {
      min-height: calc((570px - 2rem) * .75 + 1rem);

      &-multi {
        min-height: calc((570px - 2rem) * .75 + 121px + 1rem);
      }
    }
  }
}
</style>
