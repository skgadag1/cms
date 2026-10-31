<template>
  <a v-if="product"
     :href="url"
     data-test-id="dl-inv-search-results-product-link"
     class="text-black"
     rel="noopener nofollow"
     @click="ga4click"
  >
    <b-card
      border-variant="transparent"
      class="overflow-hidden h-100 transition-base"
      no-body
      footer-tag="footer"
      footer-class="z-index-1"
      data-test-id="dl-inv-search-results-product-card"
    >
      <ImageSet
        v-if="img"
        :src="img.src"
        :alt="img.alt"
        :title="img.title"
        :img-width="img.width"
        :img-height="img.height"
        class="card-img-top card-img rounded-0"
        data-test-id="dl-inv-search-results-product-card-img"
      />
      <div v-if="status"
           data-test-id="dl-inv-search-results-product-card-commingSoon-indicator"
           class="status-indicator py-1 px-2 position-absolute text-white bg-gray-darker font-weight-bold text-uppercase"
      >
        {{ status }}
      </div>

      <div v-else-if="optInStatus && (labels.reserveNow || 'Reserve Online')"
           data-test-id="dl-inv-search-results-product-card-reserve-online-indicator"
           class="status-indicator py-1 px-2 position-absolute text-white bg-primary font-weight-bold text-uppercase"
      >
        {{ labels.reserveNow || 'Reserve Online' }}
      </div>

      <div v-if="product.includeFrontendLoader || product.hasFEL"
           data-test-id="dl-inv-search-results-product-card-includeFrontendLoader-indicator"
           class="frontend-loader-indicator font-weight-bold py-2 px-4 d-flex align-items-center text-white bg-dark text-uppercase"
      >
        {{ labels.frontendLoader || 'INCLUDES FRONT-END LOADER' }}
      </div>

      <b-card-body class="d-flex flex-column px-0"
                   data-test-id="dl-inv-search-results-product-card__content"
                   :class="{
                     'pt-2 pb-0': isProductCarousel && showCTA && ctaLabel,
                     'py-2': !isProductCarousel || !showCTA || !ctaLabel
                   }"
      >
        <div class="px-3">
          <b-card-title v-if="product.name || product.label"
                        data-test-id="dl-inv-search-results-product-card__content-title"
                        :title-tag="'h5'"
                        class="pt-2 mb-2"
          >{{ product.name || product.label }}
          </b-card-title>
          <b-card-text v-if="description"
                       data-test-id="dl-inv-search-results-product-card__content-description"
                       class="pt-2 mt-2 border-top font-weight-bold d-flex align-items-center"
          >
            <div v-if="price" class="h4 mb-0 mr-2 text-primary">${{ price }}</div>
            <small>{{ description }}</small>
          </b-card-text>
        </div>

        <div class="px-3 d-flex flex-column flex-grow-1">
          <b-card-text v-if="dealerName && !isDealerPage" data-test-id="dl-inv-search-results-product-card__content-subtitle" class="d-flex align-items-start font-weight-bold small mb-1 mt-2 pt-3 border-top">
            <span class="text-primary d-flex h4 m-0"><b-icon icon="main-pin" class="ml-n1 mr-1" /></span>
            {{ dealerName }}
          </b-card-text>
          <div v-if="!isDealerPage" class="px-4">
            <b-card-text v-if="product.dealer && product.dealer.distance" data-test-id="dl-inv-search-results-product-card__content-distance" class="d-inline-block small mb-0">{{ product.dealer.distance }} {{ product.dealer.unitOfLength }}</b-card-text>
            <b-card-text v-if="fullAddress" data-test-id="dl-inv-search-results-product-card__content-location" class="d-inline-block small mb-0 text-muted">{{ fullAddress }}</b-card-text>
          </div>

          <div class="flex-grow-1" />

          <b-card-text v-if="product.keyProperties && product.keyProperties.length" data-test-id="dl-inv-search-results-product-card__content-properties" class="mt-2 pt-2 border-top">
            <b-list-group flush>
              <b-list-group-item v-for="(property, i) in product.keyProperties" :key="property.key" :class="{'pb-3' : i === product.keyProperties.length}" class="d-flex border-0 m-0 p-0 py-2 justify-content-between">
                <div class="font-weight-bold flex small">{{ property.name }}</div>
                <div class="text-right small" v-html="`${property.value} ${property.unit}`" />
              </b-list-group-item>
            </b-list-group>
          </b-card-text>
        </div>

        <a
          v-if="showCTA && ctaLabel"
          rel="noopener nofollow"
          :href="url"
          class="d-flex justify-content-center px-5 btn btn-primary text-uppercase"
          data-test-id="cta-button"
          :class="{
            'mt-3': isProductCarousel && showCTA && ctaLabel,
            'my-3': !isProductCarousel || !showCTA || !ctaLabel
          }"
          @click.stop="ga4click"
        >
          {{ ctaLabel }}
        </a>
      </b-card-body>
    </b-card>
  </a>
</template>

<script>
import {ref, computed, useContext, useRouter} from '@nuxtjs/composition-api'
import {COUNTRY_CODES} from '../../../constants/countries'
import {DEALER_INVENTORY_ROUTE} from '~/constants/staticRoutes'
import {STOCK_STATUSES} from '~/constants/dealerInventory'
import {HIDDEN_FIELDS_VALUES} from '~/constants/hiddenFields'
import useDealerLocator from '~/helpers/composables/useDealerLocator'

export default {
  props: {
    product: {
      type: Object,
      required: true
    },
    category: {
      type: Object,
      required: true
    },
    location: {
      type: String
    },
    distance: {
      type: Number
    },
    ctaSettings: {
      type: Object
    },
    isProductCarousel: {
      type: Boolean
    },
    labels: {
      type: Object,
      required: true
    },
    currency: {
      type: String
    },
    isDealerPage: {
      type: Boolean
    }
  },
  setup(props) {
    const {product, category, location, ctaSettings, distance, labels, currency, isDealerPage} = props
    const status = ref(product?.status === STOCK_STATUSES.COMING_SOON.key ? (labels.comingSoon || 'Coming Soon') : null)
    const {$regions, $ga4} = useContext()
    const router = useRouter()
    const {dealerLocatorData} = useDealerLocator()
    const aDetails = product?.dealer?.shippingAddress
    const optInStatus = ref(product?.dealer?.commerceOptIn)
    const url = ref('')
    const fullAddress = ref(`${aDetails.street || ''} ${aDetails.city || ''} ${aDetails.state || ''} ${aDetails.zipcode || ''} ${aDetails.country || ''}`)
    const img = {src: product?.image?.src, alt: product?.image?.imageAlt, title: product?.image?.imageTitle, width: product?.image?.imageWidth || null, height: product?.image?.imageHeight || null}
    const selectedCurrency = currency || (aDetails?.country === COUNTRY_CODES.UnitedStates ? 'USD' : 'CAD')
    const currentPrice = product.pricing?.estimatedNetPrices?.find(x => x.currency === selectedCurrency)?.value
    const price = currentPrice
      ? currentPrice.toLocaleString('en-us', {
        currency: currentPrice.currency,
        currencyDisplay: 'code',
        maximumFractionDigits: 0,
        minimumFractionDigits: 0
      })
      : null
    const dealerName = ref(product?.dealer?.name?.replace(/,([\w])/g, ', $1') || null)
    const ctaLabel = ref(ctaSettings?.ctaLabel)
    const showCTA = ref(ctaSettings?.showCTA === 'true')

    // Computed property that reactively updates based on price and loaded labels
    const description = computed(() => {
      return price
        ? (props.labels.asConfigured || 'as configured')
        : (props.labels.contactDealerForPricing || 'Contact Dealer for pricing')
    })

    const currentUrlParams = router.currentRoute.path.split(DEALER_INVENTORY_ROUTE)[1]
    const segments = currentUrlParams?.split('/').filter(segment => segment?.length > 0)
    const params = [
      `model=${product.name}`,
      `product-type=${category.irwName}`,
      ...(isDealerPage ? [] : [`distance=${product.dealer.distance}`, `location=${location}`, `metric=${product.dealer.unitOfLength}`]),
      `currency=${selectedCurrency}`,
      `level=${category?.level}`,
      `phone=${product?.dealer?.phone}`
    ].join('&')

    if (segments?.length === 1) {
      const [categoryName] = segments
      url.value = `/${$regions.current.name}/${$regions.current.language.name}${DEALER_INVENTORY_ROUTE}/${categoryName}/${product.dealer.arNumber}/${product.serialNumber}?${encodeURI(params)}`
    } else {
      url.value = `/${$regions.current.name}/${$regions.current.language.name}${DEALER_INVENTORY_ROUTE}/${product.dealer.arNumber}/${product.serialNumber}?${encodeURI(params)}`
    }

    const ga4click = (event) => {
      if (isDealerPage) {
        const modelName = product.name || product.label
        const storeName = dealerLocatorData[HIDDEN_FIELDS_VALUES.DEALER_LOCATOR_DEALER_NAME] || dealerName.value
        $ga4.events.dealer_page_click('card', modelName, storeName)
        $ga4.events.dealer_page_product_click(
          event,
          storeName,
          product,
          window.location.host + url.value,
          ctaLabel.value,
          price ? `$${price}` : description.value
        )
      } else if (props.isProductCarousel) {
        $ga4.events.product_click(event, product, {
          item_category: category.categoryName,
          headline: price ? `$${price} ${description.value}` : description.value,
          click_url: window.location.host + url.value,
          click_text: dealerName.value
        })
      } else {
        $ga4.events.search_inventory(category.irwName, product.name || product.label, distance, location)
        $ga4.events.dealer_inventory_product_click(event, category.irwName, product, window.location.host + url.value, price)
      }
    }

    return {
      dealerName,
      fullAddress,
      img,
      price,
      description,
      url,
      status,
      ctaLabel,
      showCTA,
      optInStatus,
      ga4click
    }
  }
}
</script>

<style lang="scss">
a[data-test-id="dl-inv-search-results-product-link"] {
  &:hover {
    text-decoration: none;
    color: unset;
  }

  .status-indicator {
    top: 1rem;
    font-size: .75rem;
  }

  .frontend-loader-indicator {
    font-size: .75rem !important;
  }
}
</style>
