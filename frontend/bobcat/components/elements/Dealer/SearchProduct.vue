<template>
  <a v-if="product" :href="url" data-test-id="dl-inv-search-results-product-link" class="text-black" rel="noopener nofollow">
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

      <b-card-body class="d-flex flex-column px-0 py-2"
                   data-test-id="dl-inv-search-results-product-card__content"
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
          <b-card-text v-if="dealerName" data-test-id="dl-inv-search-results-product-card__content-subtitle" class="d-flex align-items-start font-weight-bold small mb-1 mt-2 pt-3 border-top">
            <span class="text-primary d-flex h4 m-0"><b-icon icon="main-pin" class="ml-n1 mr-1" /></span>
            {{ dealerName }}
          </b-card-text>
          <div class="px-4">
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
          class="d-flex justify-content-center px-5 my-3 btn btn-primary text-uppercase"
          data-test-id="cta-button"
        >
          {{ ctaLabel }}
        </a>
      </b-card-body>
    </b-card>
  </a>
</template>

<script>
import {ref, useContext} from '@nuxtjs/composition-api'
import {DEALER_INVENTORY_ROUTE} from '~/constants/staticRoutes'
import {STOCK_STATUSES} from '~/constants/dealerInventory'

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
    ctaSettings: {
      type: Object
    },
    labels: {
      type: Object,
      default: () => ({})
    }
  },
  setup(props) {
    const {product, category, location, ctaSettings, labels} = props
    const status = ref(product?.status === STOCK_STATUSES.COMING_SOON.key ? (labels.comingSoon || 'Coming Soon') : null)
    const {$regions} = useContext()
    const aDetails = product?.dealer?.shippingAddress
    const optInStatus = ref(product?.dealer?.commerceOptIn)
    const fullAddress = ref(`${aDetails.street || ''} ${aDetails.city || ''} ${aDetails.state || ''} ${aDetails.zipcode || ''} ${aDetails.country || ''}`)
    const img = {src: product?.image?.src, alt: product?.image?.imageAlt, title: product?.image?.imageTitle, width: product?.image?.imageWidth || null, height: product?.image?.imageHeight || null}
    const currentPrice = aDetails?.country === 'US' ? (product.pricing?.estimatedNetPrices?.find(x => x.currency === 'USD'))?.value : (product.pricing?.estimatedNetPrices?.find(x => x.currency === 'CAD'))?.value
    const price = currentPrice
      ? currentPrice.toLocaleString('en-us', {
        currency: currentPrice.currency,
        currencyDisplay: 'code',
        maximumFractionDigits: 0,
        minimumFractionDigits: 0
      })
      : null
    const description = price ? 'as configured' : 'Contact Dealer for pricing'
    const dealerName = ref(product?.dealer?.name?.replace(/,([\w])/g, ', $1') || null)
    const ctaLabel = ref(ctaSettings?.ctaLabel)
    const showCTA = ref(ctaSettings?.showCTA === 'true')

    const params = `model=${product.name}&product-type=${category?.irwName}&distance=${product.dealer.distance}&location=${location}&metric=${product.dealer.unitOfLength}&currency=${aDetails?.country === 'US' ? 'USD' : 'CAD'}&level=${category?.level}&phone=${product?.dealer?.phone}`
    const url = ref(`/${$regions.current.name}/${$regions.current.language.name}${DEALER_INVENTORY_ROUTE}/${product.dealer.arNumber}/${product.serialNumber}?${encodeURI(params)}`)

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
      optInStatus
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
