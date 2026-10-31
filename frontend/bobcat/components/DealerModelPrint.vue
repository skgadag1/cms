<template>
  <div v-if="model" id="window-sticker" class="page-width position-relative">
    <LazyHydrate when-visible :on-interaction="['click', 'touchstart']">
      <a class="print-button position-absolute bg-white font-weight-bold cursor-pointer p-3"
         @click="printPage"
      >
        <b-icon icon="print" class="mr-2" />{{ labels.print || "Print" }}
      </a>
    </LazyHydrate>
    <LazyHydrate never>
      <div>
        <div class="d-flex justify-content-between print-header page-width">
          <div class="align-content-center"><img width="330" :src="printLogo" decoding="async"></div>
          <div class="d-flex">
            <div class="align-content-center mr-3">
              <img :src="productImage"
                   class="pdf-product-image"
                   width="132"
                   decoding="async"
              >
            </div>
            <div class="align-content-center">
              <div class="category-name size-20 font-weight-bold">{{ model.productTypeLabel }}</div>
              <div class="model-name font-weight-bold">{{ model.name }}</div>
            </div>
          </div>
          <div class="flex-column">
            <div class="size-20 font-weight-bold pb-3">{{ labels.serialNumber || "SERIAL #" }}: {{ model.serialNumber }}</div>
            <hr>
            <div class="d-flex justify-content-between size-28 configured-price font-weight-bold">
              <div>{{ labels.configuredPrice || "Configured Price" }}</div>
              <div>{{ price }}**</div>
            </div>
          </div>
          <div class="flex-shrink-1">
            <div class="d-flex justify-content-between mb-4">
              <div class="size-20 font-weight-bold">{{ labels.dealerContact || "DEALER CONTACT" }}</div>
              <div class="size-17 font-weight-bold">{{ labels.date || "Date" }} {{ currentDate }}</div>
            </div>
            <div class="dealer-name size-17">
              <div>{{ dealerName }}</div>
              <div>{{ dealerPhone }}</div>
            </div>
          </div>
        </div>
        <div class="size-17 font-weight-bold d-flex align-content-center">
          <b-icon icon="globe" class="mr-2" />
          <div>{{ labels.printMoreInformation || "For more information, visit us at www.bobcat.com" }}</div>
        </div>
        <hr>
      </div>
    </LazyHydrate>
    <div class="page-width row justify-content-between m-0">
      <div :class="model.pricedOptions && model.pricedOptions.length ? 'col-9' : 'col-12 p-0'">
        <LazyHydrate never>
          <div class="data-title size-28 font-weight-bold mb-3">{{ labels.printKeySpecifications || "Key specifications" }}</div>
        </LazyHydrate>
        <div class="data-columns d-flex flex-column">
          <PrintProductSpecifications v-if="labelsLoaded"
                                      :models="models"
                                      :uom="currentUom"
                                      :labels="labels"
          />
        </div>
      </div>

      <div v-if="model.pricedOptions && model.pricedOptions.length" class="col-3">
        <div class="data-title size-28 font-weight-bold mb-3">{{ labels.printPackagesAndOptions || "Packages & Options" }}</div>
        <div v-for="item in model.pricedOptions" :key="item.short" class="size-17 font-weight-bold">{{ item.shortDesc }}</div>
      </div>
    </div>
    <LazyHydrate never>
      <div class="d-flex justify-content-between">
        <div class="print-disclaimer print-disclaimer-left">{{ labels.printPriceDisclaimer || "price disclaimer" }}</div>
        <div class="print-disclaimer print-disclaimer-right">{{ labels.printWarningDisclaimer || "warning disclaimer" }}</div>
      </div>
    </LazyHydrate>
  </div>
</template>

<script>
import {
  computed,
  inject,
  onBeforeUnmount,
  onMounted, onServerPrefetch,
  ref,
  useContext,
  useFetch
} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import {METRICS} from '~/constants'

export default {
  setup() {
    const model = inject('model') || {}
    const images = model?.gallery?.filter(img => img?.src || img?.tag) || []
    const {route, $services, $utils, $regions} = useContext()
    const currency = ref(decodeURI(route.value.query?.currency || 'USD'))
    const uom = METRICS
    const currentUom = ref(uom[0])
    const labels = ref({})
    const labelsLoaded = ref(false)
    const dealerName = ref(model?.dealer?.name?.replace(/,([\w])/g, ', $1') || null)
    const dealerPhone = ref(decodeURI(route.value.query?.phone || ''))

    const models = [{
      id: model.id,
      title: model?.name,
      '@path': $regions.current.language.name
    }]

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

    const printLogo = computed(() => {
      const link = /(https?:\/\/[^" ]*)/i
      const imageSrc = labels.value?.printLogo?.match(link)
      return Array.isArray(imageSrc) ? imageSrc[0] : ''
    })

    const productImage = computed(() => images[0].src.startsWith('cloudinary:') ? $utils.getCloudinaryAsset(images[0].src, true) : '')

    const printPage = () => {
      window.print()
    }

    onBeforeUnmount(() => {
      document.body.classList.remove('window-sticker')
    })

    onMounted(() => {
      if (process.client) {
        document.body.classList.add('window-sticker')
        const printPageStyle = document.createElement('style')
        printPageStyle.textContent = '@page { size: letter landscape !important; margin: none!important; width:1684px; height: 1200px }'
        document.head.appendChild(printPageStyle)
      }
    })

    const isoLangParam = $regions?.current?.locale || `${$regions?.current?.language.name}-us`
    const currentDate = new Date()?.toLocaleDateString(isoLangParam)

    const {fetch} = useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory', '/common/labels/functional', '/common/labels/specifications'])
      labelsLoaded.value = true
      if (!inEditorEdit) {
        printPage()
      }
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    return {
      price,
      models,
      productImage,
      labels,
      labelsLoaded,
      printLogo,
      model,
      currency,
      currentUom,
      dealerName,
      dealerPhone,
      printPage,
      currentDate
    }
  }
}
</script>

<style lang="scss">
@import '~/assets/sass/_colors';

#window-sticker {

  .page-width {
    width: 1684px;
  }

  .print-button {
    right: 0;
    top: 0;
  }

  .print-header.page-width {
    width: 1595px;
  }

  @media print {
    .print-header.page-width {
      width: 1684px;
    }
  }

  .print-header {
    height: 131px;
    overflow: hidden;
  }

  .category-name {
    text-transform: uppercase;
  }

  .product-specification {
    font-size: 12px;
  }

  .print-border-bottom {
    border-bottom: 1px solid $tertiary;
  }

  .model-name {
    font-size: 62px;
    line-height: 100%;
  }

  .size-12 {
    font-size: 12px;
  }

  .size-17 {
    font-size: 17px;
  }

  .size-20 {
    font-size: 20px;
  }

  .size-28 {
    font-size: 28px;
  }

  .dealer-name {
    min-width: 200px;
    max-width: 360px;

    div {
      word-wrap: break-word;
    }
  }

  .configured-price {
    width: 380px;
  }

  hr {
    border-top: 1px solid black;
  }

  .pdf-product-image {
    width: 132px;
  }

  .specifications {
    height: 870px;
    overflow: hidden;

    .specification-row {
      width: 389px;
      margin-right: 34px;
    }
  }

  .print-disclaimer {
    font-size: 12px;
    line-height: 17px;
    color: #333;

    &-right {
      width: 700px;
    }
    &-left {
      width: 900px;
    }
  }

  .key-specifications {
    border: 1px solid black;
    background: #F7F7F7;

    .specification-row {
      width: auto;
      margin-right: 0;
    }

    .product-specification {
      font-size: 17px;
    }
    .print-specification-items  {

      & > div:last-child {
        .print-border-bottom {
          border-bottom: 0;
        }
      }
    }
  }

}

/* Safari 7.1+ only hack to fit in one page */
_::-webkit-full-page-media, _:future, :root .safari_only, #window-sticker .specifications {
  height: 760px;
}

</style>
