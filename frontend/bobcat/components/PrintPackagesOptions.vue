<template>
  <PrintHeaderFooter
    :model="model"
    :labels="labels"
    :print-logo="printLogo"
    :product-image="productImage"
    :current-date="currentDate"
  >
    <div class="page-width row justify-content-between m-0">
      <div class="col-9">
        <div class="product-title size-28 font-weight-bold mb-4">
          {{ labels.availablePackages || "Available Packages" }}
        </div>
        <div class="data-columns d-flex flex-column">
          <PrintPackagesOptionsTable :option-choices="optionChoices" />
        </div>
      </div>
    </div>
  </PrintHeaderFooter>
</template>

<script>
import PrintHeaderFooter from './PrintHeaderFooter.vue'
export default {
  components: {PrintHeaderFooter},
  props: {
    model: Object,
    labels: Object,
    printLogo: String,
    productImage: String,
    currentDate: String,
    optionChoices: Array
  }
}
</script>
