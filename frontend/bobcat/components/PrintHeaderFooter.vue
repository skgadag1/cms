<template>
  <div class="page-width">
    <!-- Header -->
    <div class="justify-content-between">
      <div class="d-flex justify-content-between print-header page-width ml-3" :class="{ 'pricing-header': showPrice }">
        <div class="align-content-center">
          <ImageSet class="pdf-logo-image" :src="printLogo" />
        </div>
        <div class="d-flex">
          <div class="align-content-center mr-3">
            <ImageSet :src="productImage" class="pdf-product-image" />
          </div>
          <div class="align-content-center">
            <div class="category-name size-20 font-weight-bold">{{ model?.parent?.parentTitle }}</div>
            <div class="model-name font-weight-bold">{{ model.title }}</div>
          </div>
        </div>
        <div v-if="showPrice" class="pricing-title size-28 font-weight-bold mb-3 d-flex justify-content-between mr-3">
          <span>{{ labels.startingCurrency || "Starting at USD" }}: &nbsp;</span>
          <span>{{ productPricing }}</span>
        </div>
        <div class="print-disclaimer print-overview-right flex-shrink-1 mt-3" v-html="model.overview" />
      </div>
      <div class="size-17 date-of-print font-weight-bold d-flex align-content-center ml-3">
        <div class="size-17 font-weight-bold">{{ labels.dateOfPrint || "Date of print" }} {{ currentDate }}</div>
      </div>
      <hr class="ml-3 mr-3">
    </div>

    <!-- Page specific content -->
    <slot />

    <!-- Footer -->
    <div class="d-flex justify-content-between">
      <div class="print-disclaimer print-disclaimer-left font-weight-bold mt-3">
        <p class="size-17">
          <b-icon icon="globe" class="mr-2" />
          {{ labels.printMoreInformation || "For more information, visit us at www.bobcat.com" }}
        </p>
      </div>
      <div class="print-disclaimer print-disclaimer-right">
        {{ labels.printWarningDisclaimer || "warning disclaimer" }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    model: Object,
    labels: Object,
    printLogo: String,
    productImage: String,
    currentDate: String,
    showPrice: {
      type: Boolean,
      default: false
    },
    productPricing: {
      type: String,
      default: ''
    }
  }
}
</script>
