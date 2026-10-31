<template>
  <div class="row build-quote" data-test-id="build-and-quote">
    <LazyHydrate never>
      <div v-if="pricing.itemWebDesc || overview" class="col-12 mb-3" data-test-id="build-quote__webdescription" v-html="overview || pricing.itemWebDesc" />
    </LazyHydrate>

    <LazyHydrate never>
      <div class="col-12">
        <div v-if="hasIndicator && indicatorLabel"
             data-test-id="dinv-model-pricing-includeFrontendLoader-indicator"
             class="font-weight-bold small py-2 px-3 mt-2 d-flex align-items-center text-white bg-dark text-uppercase"
        >
          {{ indicatorLabel }}
        </div>
      </div>
    </LazyHydrate>

    <div v-if="displayReservation && reservePrice" class="col-12 mb-2">
      <div class="col-12 py-3 bg-light">
        <div class="d-flex w-100 justify-content-between align-items-center">
          <h6 class="d-flex align-items-center text-uppercase mb-0">
            <span class="mr-2">
              {{ labels.reserveNow || "Reserve Now" }}
            </span>

            <span
              v-b-tooltip.hover.bottom
              class="cursor-pointer tooltip-danger"
              :title="labels.reserveNowTooltip || `The value of your reservation will be applied to the machine's total price if paying cash, or to the down payment or first payment if financing.`"
            ><b-icon icon="info" class="mt-1" /></span>
          </h6>
          <p class="h4 text-primary mb-0">
            {{ `${reservePrice} ${currency}` }}
          </p>
        </div>
      </div>
    </div>

    <template v-if="Object.keys(pricing).length">
      <div :class="[(estimatedPayment && estimatedPayment.value > 0) || manufacturerFinacing ? 'col-6' : 'col-12']"
           class="d-flex align-items-stretch"
           data-test-id="build-quote-column__left"
      >
        <div class="col-12 py-3 bg-light">
          <template v-if="estimatedNetPrice">
            <h6 class="text-uppercase mb-2"
                data-test-id="model-pricing-netprice__label"
                :title="generateConceptHint('dealerinventory/netPriceLabel')"
            >
              <span class="mr-1">{{ labels.netPriceLabel }}</span><sup :class="disclaimerId ? 'cursor-pointer' : ''"
                                                                       @click.prevent="scrollToDisclaimer"
              >*</sup>
              <span
                v-if="displayReservation"
                v-b-tooltip.hover.bottom
                class="cursor-pointer tooltip-danger"
                :title="labels.netPriceTooltip || 'The total price or full down payment, less the value of your reservation, is due upon receipt of your equipment.'"
              ><b-icon icon="info" class="mt-1" /></span>
            </h6>

            <p class="h4 text-primary mb-3" data-test-id="model-pricing-netprice__value">
              ${{ estimatedNetPrice.value.toLocaleString('en-us', {currency: estimatedNetPrice.currency, currencyDisplay: 'code', maximumFractionDigits: 0, minimumFractionDigits: 0}) }} {{ estimatedNetPrice.currency }}
            </p>
          </template>

          <template v-if="cashRebate">
            <h6 class="text-uppercase mb-2"
                data-test-id="model-pricing-rebate__label"
                :title="generateConceptHint('dealerinventory/rebateLabel')"
            >
              <LazyHydrate never><span class="mr-1">{{ labels.rebateLabel }}</span></LazyHydrate><sup :class="disclaimerId ? 'cursor-pointer' : ''" @click.prevent="scrollToDisclaimer">**</sup>
            </h6>

            <p class="h4 text-primary mb-0" data-test-id="model-pricing-rebate__value">
              ${{ cashRebate.value.toLocaleString('en-us', {currency: cashRebate.currency, currencyDisplay: 'code', maximumFractionDigits: 0, minimumFractionDigits: 0}) }} {{ cashRebate.currency }}
            </p>
          </template>
        </div>
      </div>

      <div v-if="(estimatedPayment && estimatedPayment.value > 0) || manufacturerFinacing"
           class="col-6 d-flex align-items-stretch border-left border-dark position-relative"
           data-test-id="build-quote-column__right"
      >
        <div class="separator position-absolute py-2 bg-white font-weight-bold text-center small">or</div>
        <div class="col-12 py-3 bg-light">
          <template v-if="estimatedPayment && estimatedPayment.value > 0">
            <h6 class="text-uppercase mb-2"
                data-test-id="model-pricing-payment__label"
                :title="generateConceptHint('dealerinventory/paymentLabel')"
            >
              <LazyHydrate never><span class="mr-1">{{ labels.paymentLabel }}</span></LazyHydrate><sup :class="disclaimerId ? 'cursor-pointer' : ''" @click.prevent="scrollToDisclaimer">**</sup>
            </h6>

            <p class="h4 text-primary mb-3" data-test-id="model-pricing-payment__value">
              ${{ estimatedPayment.value.toLocaleString('en-us', {currency: estimatedPayment.currency, currencyDisplay: 'code', maximumFractionDigits: 0, minimumFractionDigits: 0}) }} {{ estimatedPayment.currency }} / {{ labels.month || 'month' }}
            </p>
          </template>

          <template v-if="manufacturerFinacing">
            <h6 class="text-uppercase mb-2"
                data-test-id="model-pricing-financing__label"
                :title="generateConceptHint('dealerinventory/financingLabel')"
            >
              <LazyHydrate never><span class="mr-1">{{ labels.financingLabel }}</span></LazyHydrate><sup :class="disclaimerId ? 'cursor-pointer' : ''" @click.prevent="scrollToDisclaimer">**</sup>
            </h6>
            <LazyHydrate never>
              <p class="h4 text-primary mb-0" data-test-id="model-pricing-financing__value">
                {{ manufacturerFinacing }}
              </p>
            </LazyHydrate>
          </template>
        </div>
      </div>
    </template>

    <div v-else class="col-12">
      <LazyHydrate never>
        <WarningAlert data-test-id="build-quote__errormessage">Error: No pricing data available</WarningAlert>
      </LazyHydrate>
    </div>
  </div>
</template>

<script>
import {computed, ref, useContext} from '@nuxtjs/composition-api'
import useConcepts from '~/helpers/composables/useConcepts'

export default {
  props: {
    pricing: {
      type: Object,
      required: true
    },
    currency: {
      type: String,
      required: true
    },
    labels: {
      type: Object,
      required: true
    },
    overview: {
      type: String,
      required: false
    },
    disclaimerId: {
      type: String
    },
    indicatorLabel: {
      type: String
    },
    hasIndicator: {
      type: Boolean,
      default: false
    },
    displayReservation: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const {generateConceptHint} = useConcepts()
    const {$utils} = useContext()
    const {disclaimerId} = props
    const reservePrice = ref(props.pricing?.reservePrice)

    const scrollToDisclaimer = () => {
      if (process.client && disclaimerId) {
        $utils.scrollToElement(disclaimerId)
      }
    }

    const cashRebate = computed({
      get: () => (props?.currency === 'USD' ? (props.pricing?.cashRebate?.find(x => x.currency === 'USD')) : (props.pricing?.cashRebate?.find(x => x.currency === 'CAD'))),
      set: () => {
      }
    })
    const estimatedPayment = computed({
      get: () => (props?.currency === 'USD' ? (props.pricing?.estimatedPayment?.find(x => x.currency === 'USD')) : (props.pricing?.estimatedPayment?.find(x => x.currency === 'CAD'))),
      set: () => {
      }
    })
    const estimatedNetPrice = computed({
      get: () => (props?.currency === 'USD' ? (props.pricing?.estimatedNetPrices?.find(x => x.currency === 'USD')) : (props.pricing?.estimatedNetPrices?.find(x => x.currency === 'CAD'))),
      set: () => {
      }
    })

    const manufacturerFinacing = computed({
      get: () => props?.pricing?.manufacturerFinancing?.replace(/\*/g, ''),
      set: () => {
      }
    })

    return {
      generateConceptHint,
      estimatedNetPrice,
      estimatedPayment,
      cashRebate,
      manufacturerFinacing,
      reservePrice,
      scrollToDisclaimer
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';

.build-quote {
  .col-6.border-left {

    .separator {
      left: 0;
      top: 50%;
      margin-top: -20px;
      margin-left: -1rem;
      width: 2rem;
    }
  }

  .tooltip-danger:hover {
    color: $danger;
  }
}
</style>
