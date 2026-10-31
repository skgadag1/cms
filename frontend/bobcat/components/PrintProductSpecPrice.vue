<template>
  <PrintHeaderFooter
    :model="model"
    :labels="labels"
    :print-logo="printLogo"
    :product-image="productImage"
    :current-date="currentDate"
    :show-price="true"
    :product-pricing="model.suggestedPrice"
  >
    <div class="page-width row justify-content-between m-0">
      <div class="col-12">
        <div class="product-title size-28 font-weight-bold mb-4">
          {{ labels.printKeySpecifications || "Key specifications" }}
        </div>
        <div class="d-flex flex-column">
          <PrintProductSpecifications :models="models"
                                      :uom="uom"
                                      :labels="labels"
                                      :show-icon="true"
          />
        </div>
      </div>
    </div>
    <div class="print-disclaimer">
      <p class="size-12 price-disclaimer">**{{ labels.priceDisclaimer || "Suggested Price Disclaimer" }}</p>
    </div>
  </PrintHeaderFooter>
</template>

<script>
import {
  useContext,
  toRefs
} from '@nuxtjs/composition-api'
import PrintHeaderFooter from './PrintHeaderFooter.vue'
export default {
  components: {PrintHeaderFooter},
  props: {
    model: Object,
    labels: Object,
    printLogo: String,
    productImage: String,
    currentDate: String,
    uom: Object,
    optionChoices: Array
  },
  setup(props) {
    const {model} = toRefs(props)
    const {$regions} = useContext()
    const models = [{
      id: model.value.id,
      title: model.value?.name,
      price: model.value?.suggestedPrice,
      '@path': $regions.current.language.name
    }]
    return {
      models
    }
  }
}
</script>
