<template>
  <div v-if="model" id="window-sticker" class="position-relative">
    <LazyHydrate when-visible :on-interaction="['click','touchstart']">
      <a class="print-button position-absolute bg-white font-weight-bold cursor-pointer p-3"
         @click="printPage"
      >
        <b-icon icon="print" class="mr-2" />{{ labels.print || "Print" }}
      </a>
    </LazyHydrate>
    <div v-if="pagesArray.includes('pricingPage')" class="print-page">
      <PrintProductSpecPrice :model="model"
                             :labels="labels"
                             :print-logo="printLogo"
                             :product-image="productImage"
                             :current-date="currentDate"
                             :uom="currentUom"
                             :option-choices="optionChoices"
      />
    </div>
    <div v-if="pagesArray.includes('packagesPage') && optionChoices && optionChoices.length > 0" class="print-page">
      <PrintPackagesOptions
        :model="model"
        :labels="labels"
        :print-logo="printLogo"
        :product-image="productImage"
        :current-date="currentDate"
        :option-choices="optionChoices"
      />
    </div>
    <div v-if="pagesArray.includes('attachmentsPage') && attachmentList && attachmentList.length > 0" class="print-page">
      <PrintCompatibleAttachment
        :model="model"
        :labels="labels"
        :print-logo="printLogo"
        :product-image="productImage"
        :current-date="currentDate"
        :attachment-list="attachmentList"
      />
    </div>
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
import useCloudinary from '../helpers/composables/useCloudinary'
import PrintCompatibleAttachment from './PrintCompatibleAttachment.vue'
import PrintPackagesOptions from './PrintPackagesOptions.vue'
import PrintProductSpecPrice from './PrintProductSpecPrice.vue'
import {MODEL_TYPE, METRICS} from '~/constants'

export default {
  components: {PrintCompatibleAttachment, PrintPackagesOptions, PrintProductSpecPrice},
  props: {
    facet: {
      type: [Object, String],
      default: '/bobcat/na/attachment-facets/attachments'
    }
  },
  setup(props) {
    const model = inject('model') || {}
    const {mapCloudinaryGalleryAssets} = useCloudinary()
    const {route, $services, $utils, $regions} = useContext()
    const labels = ref({})
    const labelsLoaded = ref(false)
    const attachmentList = ref([])
    const optionChoices = ref([])
    const currentUom = ref(METRICS[0])
    const isoLangParam = $regions?.current?.locale || `${$regions?.current?.language.name}-us`
    const currentDate = new Date()?.toLocaleDateString(isoLangParam)
    const images = mapCloudinaryGalleryAssets(model?.images)

    const printLogo = computed(() => {
      const link = /(https?:\/\/[^" ]*)/i
      const imageSrc = labels.value?.printLogo?.match(link)
      return Array.isArray(imageSrc) ? imageSrc[0] : ''
    })

    const pagesArray = computed(() => {
      const pagesParam = route.value.query.pages
      return pagesParam ? pagesParam.split(',') : []
    })

    const productImage = computed(() => images[0]?.src?.startsWith('cloudinary:') ? $utils.getCloudinaryAsset(images[0].src, true) : '')

    const printPage = () => {
      window.print()
    }

    onBeforeUnmount(() => {
      document.body.classList.remove('window-sticker')
    })

    const fetchEquipmentList = async () => {
      try {
        const searchConfig = {
          contentType: MODEL_TYPE.ATTACHMENT,
          facet: typeof props.facet === 'string' ? props.facet : props.facet?.['@path'],
          sort: 'sortRating:DESC',
          page: 0,
          maxItems: 100,
          size: 100,
          displayOnGroupLevel: true,
          model: model.id
        }
        const response = await $services.content.getSearchResultsByType(searchConfig)
        attachmentList.value = response.data?.results || []
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('[EquipmentModelPrint] Unable to fetch equipment list', e)
        attachmentList.value = []
      }
    }

    const fetchQuote = async () => {
      try {
        const quote = await $services.buildandquote.getQuote(model.category)
        if (!quote.data?.modelDetails?.length) {
          // eslint-disable-next-line no-console
          console.warn('No modelDetails found')
          return
        }
        const modelDetails = quote.data.modelDetails.find(item => item.itemPart === model.id)
        if (!modelDetails?.itemId) {
          // eslint-disable-next-line no-console
          console.warn('itemId not found')
          return
        }
        const optionGroupName = await $services.buildandquote.getOptionChoices(modelDetails.itemId)
        const options = optionGroupName.data.find(item => item.optionGroupName === 'Bobcat Packages')
        optionChoices.value = options?.optionChoices || []
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error('[EquipmentModelPrint] Unable to fetch quote data', e)
        optionChoices.value = []
      }
    }

    onMounted(() => {
      if (pagesArray.value.includes('attachmentsPage')) {
        fetchEquipmentList()
      }
      if (pagesArray.value.includes('packagesPage') || pagesArray.value.includes('pricingPage')) {
        fetchQuote()
      }
      if (process.client) {
        document.body.classList.add('window-sticker')
        const printPageStyle = document.createElement('style')
        printPageStyle.textContent = '@page { size: letter landscape !important; margin: none!important; width:1684px; height: 1200px }'
        document.head.appendChild(printPageStyle)
      }
    })

    const {fetch} = useFetch(async () => {
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/common/labels/specifications'])
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    return {
      productImage,
      labels,
      labelsLoaded,
      printLogo,
      model,
      printPage,
      currentUom,
      attachmentList,
      optionChoices,
      currentDate,
      pagesArray
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

  .product-title {
    width: 1232px;
  }

  .print-button {
    right: 0;
    top: 0;
  }

  .print-header {
    height: 140px;
    width: 1300px;
    overflow: hidden;
  }

  .print-header.pricing-header {
    width: 1580px;
  }

  .pricing-title {
    align-items: center;
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

 @media screen {
    .print-page {
      border: 2px dashed $gray-300;
      margin-bottom: 40px;
    }
  }

  @media print {
    .print-page {
      page-break-after: always;
    }

    .print-page:last-child {
      page-break-after: auto;
    }

    .specifications.specifications-spec {
      height: 795px;
    }

    .print-page .print-disclaimer-right,
    .print-page .print-disclaimer-left {
      height: auto;
    }
  }

  .model-name {
    font-size: 62px;
    line-height: 100%;
  }

  .size-12 {
    font-size: 12px;
  }

  .size-14 {
    font-size: 14px;
  }

  .size-17 {
    font-size: 17px;
  }

  .size-18 {
    font-size: 18px;
  }

  .size-20 {
    font-size: 20px;
  }

  .size-28 {
    font-size: 28px;
  }

  hr {
    border-top: 1px solid $black;
  }

  .pdf-product-image {
    width: 132px !important;
  }

  .pdf-logo-image {
    width: 284px !important;
    height: 80px !important;
  }

  .specifications {
    height: 870px;
    font-size: 12px;

    .specification-row-attachment {
      width: 379px;
      margin-right: 34px;
    }

    .specification-row {
      width: 389px;
      margin-right: 34px;
    }

    .package-row {
      display: grid;
      grid-auto-flow: column;
      grid-template-rows: repeat(3, auto);
      grid-template-columns: repeat(auto-fit, 390px);
      gap: 17px;
    }

    .package-card {
      h6 {
        font-weight: 700;
        font-style: normal;
        margin-bottom: 5px;
      }

      p {
        line-height: 17px;
        font-style: normal;
      }
    }
  }

  .print-disclaimer {
    font-size: 12px;
    line-height: 17px;
    color: $gray-800;

    &-right {
      width: 700px;
      height: 50.99px;
    }
    &-left {
      width: 900px;
      height: 50.99px;
    }
  }

  .price-disclaimer {
    width: 700px;
    margin-left: auto;
  }

  .print-check-col {
    width: 40px;
    min-width: 40px;
    flex-shrink: 0;
  }

  .print-check-icon {
    width: 16px;
    height: 16px;
    color: $red;
  }

  .print-overview {
    &-right {
      width: 480px;
      height: 124px;
    }
  }

  .date-of-print {
    width: 379px;
  }

  .key-specifications {
    border: 1px solid $black;
    background: $gray-100;

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
