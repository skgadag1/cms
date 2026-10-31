<template>
  <section class="container py-4 model-pricing"
           data-test-id="model-pricing"
           :data-model-name="modelName"
           :data-category="categoryName"
  >
    <div class="row align-items-stretch">
      <LazyHydrate never>
        <div class="col-12 d-lg-none">
          <component :is="!desktop ? htag : 'div'"
                     v-if="oTitle"
                     :class="htag"
                     class="mb-2 mr-md-2 d-block d-md-inline-block d-lg-none"
                     data-test-id="model-pricing-title__mobile"
          >
            <span v-html="oTitle" />
            <div v-if="subtitle"
                 class="h2 d-block d-md-inline-block"
                 data-test-id="model-pricing-subtitle__mobile"
                 v-html="subtitle"
            />
          </component>
          <div v-if="historicalContext && productionYears" class="d-lg-none">
            <h5 class="d-inline-block"
                data-test-id="model-pricing-production-year__mobile"
            >
              {{ labels.productionYearsLabel || 'Year(s) of Production:' }}
            </h5>
            <span class="font-weight-light ml-1">{{ productionYears }}</span>
          </div>
        </div>
      </LazyHydrate>

      <div v-if="images.length" class="col-12 col-md-6 mb-4 mb-md-0">
        <CloudinaryGallery :images="images" />
      </div>

      <div class="col-12 col-md-6" data-test-id="model-pricing-bq">
        <div class="row">
          <LazyHydrate never>
            <div :class="isAutomaticMode ? 'col-6' : 'col-12'">
              <component :is="desktop ? htag : 'div'"
                         v-if="oTitle"
                         :class="htag"
                         data-test-id="model-pricing-title__desktop"
                         class="d-none d-lg-block mb-lg-2"
              >
                <span v-html="oTitle" />
                <div v-if="subtitle"
                     class="h2"
                     data-test-id="model-pricing-subtitle__desktop"
                     v-html="subtitle"
                />
              </component>

              <div v-if="historicalContext && productionYears" class="d-none d-lg-block mb-3">
                <h5 data-test-id="model-pricing-production-year__desktop"
                    class="d-inline-block"
                    :title="generateConceptHint('productionYearsLabel')"
                >
                  {{ labels.productionYearsLabel || 'Year(s) of Production:' }}
                </h5>
                <span class="font-weight-light ml-1">{{ productionYears }}</span>
              </div>
            </div>
          </LazyHydrate>

          <LazyHydrate :on-interaction="['click', 'touchstart']">
            <div v-if="isAutomaticMode"
                 class="col-12 col-lg-6 d-flex mb-2 my-md-3 mt-lg-0 pl-lg-0 align-items-center justify-content-md-end"
            >
              <SwitchToggle v-model="currency.selected"
                            label="Select currency:"
                            :options="[currency.cad, currency.us]"
              />
            </div>
          </LazyHydrate>
        </div>

        <BuildAndQuote v-if="isAutomaticMode"
                       :pricing="pricingObject"
                       :currency="currentCurrency.label"
                       :labels="labels"
                       :overview="descriptionAutomatic"
        />
        <div v-else-if="switchable.manualInfo">
          <LazyHydrate never>
            <div v-html="descriptionManual" />
          </LazyHydrate>
        </div>

        <template v-if="!historicalContext && !isHistoricalDetails">
          <div v-if="suggestedPrice || suggestedPrice2" class="h5 mb-4">
            <h5 class="mb-1">{{ labels.startingAt || "Starting at" }}</h5>
            <p v-if="suggestedPrice" class="m-0">{{ suggestedPrice }}</p>
            <p v-if="suggestedPrice2" class="m-0">{{ suggestedPrice2 }}</p>
          </div>

          <div>
            <p v-if="suggestedPriceAdditionalInfo" class="h5">{{ suggestedPriceAdditionalInfo }}</p>
          </div>

          <div v-if="!isAutomaticMode && (specialOfferDetails || specialOfferDetails2)" data-test-id="offers" class="h5">
            <h5 class="mb-1">{{ labels.specialOffers || "Special Offers" }}</h5>
            <p v-if="specialOfferDetails" class="m-0">{{ specialOfferDetails }}</p>
            <p v-if="specialOfferDetails2" class="m-0">{{ specialOfferDetails2 }}</p>
          </div>

          <div>
            <p v-if="specialOffersDetailsAdditionalInfo" class="h5">{{ specialOffersDetailsAdditionalInfo }}</p>
          </div>
        </template>

        <LazyHydrate :on-interaction="['click', 'touchstart']">
          <div v-if="links.length || buildQuoteLink || (brochure && brochure.filePath)" class="row mt-4">
            <div v-if="buildQuoteLink" class="col-12">
              <a :href="buildQuoteLink.url"
                 data-test-id="model-pricing-build-and-quote-link"
                 :target="buildQuoteLink.target === '_blank_follow' ? '_blank' : buildQuoteLink.target"
                 :rel="buildQuoteLink.target === '_blank' ? 'noopener nofollow' : null"
                 class="btn btn-primary btn-lg btn-block"
                 @click="ga4click"
              >{{ buildQuoteLink.label }}</a>
            </div>

            <ul v-if="links.length || (brochure && brochure.filePath)"
                class="col-12 list-unstyled list-links mt-3 font-weight-bold"
                data-test-id="model-pricing-links"
            >
              <li v-for="l of links" :key="`quick-link__${l['id']}`" class="py-3 mr-lg-5">
                <a :href="l.url"
                   :target="l.target === '_blank_follow' ? '_blank' : l.target"
                   :rel="l.target === '_blank' ? 'noopener nofollow' : null"
                   @click="ga4click"
                >{{ l.label }}</a>
              </li>
              <li v-if="(brochure && brochure.filePath)" class="py-3 mr-lg-5">
                <a class="link" @click.prevent="(e) => {ga4click(e); download(brochure.filePath)}">{{
                  brochure.label || 'Download brochure'
                }}</a>
              </li>
            </ul>
            <ul v-if="printButton && printButton.length"
                class="col-12 list-unstyled list-links font-weight-bold"
                data-test-id="model-pdf-links"
            >
              <li v-for="button in printButton" :key="`print-button-${button.title}`" class="py-3 mr-lg-5">
                <b-icon v-if="button.icon" class="icon-link" :icon="button.icon" />
                <a class="link" @click="handlePrint(button)">{{ button.title }}</a>
              </li>
            </ul>
          </div>
        </LazyHydrate>
      </div>
    </div>
  </section>
</template>

<script>
import {computed, inject, onMounted, onServerPrefetch, ref, useContext, useFetch, useRouter} from '@nuxtjs/composition-api'
import useCloudinary from '../helpers/composables/useCloudinary'
import {setWithExpiry, convertToPlain} from '../helpers/functional/AppHelpers'
import {DEALER_INVENTORY_KEYS} from '../constants/localStorageKeys'
import useConcepts from '~/helpers/composables/useConcepts'
import {FIELD_TYPES, MODEL_LIFECYCLE} from '~/constants'
import useBreakpoints from '~/helpers/composables/useBreakpoints'
import useMounted from '~/helpers/composables/useMounted'
import useProductSalesProgram from '~/helpers/composables/useProductSalesProgram'
import useSchema from '~/helpers/composables/useSchema'
import usePrintButtonMapper from '~/helpers/composables/usePrintButtonMapper'
import {resolveLegacyListSource} from '~/helpers/i18nCompat'

function extractCategoryFromPath(modelName, path) {
  if (!modelName || !path) {
    return
  }
  try {
    const [leftPathAtom, rightPathAtom] = path?.split('/').slice(-3, -1)

    if (!modelName.includes(rightPathAtom)) {
      return rightPathAtom
    }

    return leftPathAtom
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error(`Unable to extract category from path ${path}`)
  }
}

export default {
  props: {
    nodeId: {type: String},
    title: {type: String},
    subtitle: {type: String},
    switchable: {type: Object},
    quickLinks: {type: Object},
    bqLink: {type: Object},
    printPDF: {type: Object},
    selectCurrencyLabel: {type: String},
    netPriceLabel: {type: String},
    rebateLabel: {type: String},
    paymentLabel: {type: String},
    financingLabel: {type: String},
    productionYearsLabel: {type: String},
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props, context) {
    const {
      quickLinks,
      bqLink,
      printPDF,
      netPriceLabel,
      rebateLabel,
      paymentLabel,
      financingLabel,
      productionYearsLabel,
      switchable
    } = props

    const model = inject('model')
    const {$utils, $regions, $services, $ga4, route, $config} = useContext()
    const {desktop} = useBreakpoints()
    const {mapCloudinaryGalleryAssets} = useCloudinary()
    const loaded = ref(false)
    const pricingObject = ref({})
    const quickLinksSource = resolveLegacyListSource(
      quickLinks, context.attrs, 'quickLinks', $regions.current.language.name, $config.USE_LEGACY_I18N_STRUCTURE
    )
    const links = quickLinksSource ? $utils.getLinks(quickLinksSource, $regions.current.language.name) || [] : []
    const brochure = ref(null)
    const router = useRouter()
    const {createProductObject} = useSchema({}, $utils, $regions, $config)
    const modelID = model?.id
    const {printButton, handlePrint} = usePrintButtonMapper(printPDF, route, modelID)
    const currency = ref({
      selected: 'us',
      us: {
        value: 'us',
        label: 'USD'
      },
      cad: {
        value: 'cad',
        label: 'CAD'
      }
    })
    const currentCurrency = computed({
      get: () => currency?.value[currency?.value.selected],
      set: () => {
      }
    })
    const buildQuoteLink = bqLink && bqLink.field !== 'none' ? $utils.getLinkField(bqLink, $regions.current.language.name) : null
    const labels = ref({
      netPriceLabel,
      rebateLabel,
      paymentLabel,
      financingLabel,
      productionYearsLabel
    })
    const {generateConceptHint} = useConcepts()
    useMounted(props.nodeId)

    const download = (url, fileName = 'file') => {
      if (process.client) {
        const doc = $utils.getCloudinaryDocument(url)
        $utils.downloadFile(doc, fileName, false, true)
      }
    }

    const pageTemplateId = inject('pageTemplateId')
    const isHistoricalDetails = pageTemplateId?.includes(MODEL_LIFECYCLE.HISTORICAL)
    const historicalContext = ref(model?.lifecycle?.toLowerCase() === MODEL_LIFECYCLE.HISTORICAL)
    const productionYears = model?.yearEnd ? `${model.yearBuilt} - ${model.yearEnd}` : `${model?.yearBuilt || ''}`

    brochure.value = model?.brochure?.field === 'none' ? null : model?.brochure

    const images = mapCloudinaryGalleryAssets(model?.images)
    const modelName = model?.['@name']
    const oTitle = props.title?.replace('%ID%', model?.id).replace('%NAME%', modelName).replace('%TITLE%', model?.title)
    const descriptionManual = props.switchable?.manualInfo?.replace('%DESC%', model?.overview)
    const descriptionAutomatic = props.switchable?.automaticInfo?.replace('%DESC%', model?.overview)
    const preparePricingFromBQ = (obj, labelsData) => {
      const rebates = []
      const netPrices = []
      const netPayment = []
      // calculate rebate for USD
      const rebateValueUS = (obj?.itemRebateUS || 0) + (obj?.itemCashbackUS || 0)
      if (rebateValueUS && parseFloat(rebateValueUS) > 0) {
        rebates.push({value: rebateValueUS, currency: currency.value.us.label})
      }

      // calculate rebate for CAD
      const rebateValueCAD = (obj?.itemRebateCAD || 0) + (obj?.itemCashbackCAD || 0)
      if (rebateValueCAD && parseFloat(rebateValueCAD) > 0) {
        rebates.push({value: rebateValueCAD, currency: currency.value.cad.label})
      }

      if (obj?.itemPriceUS) {
        netPrices.push({value: obj.itemPriceUS, currency: currency.value.us.label})
      }
      if (obj?.itemPriceCAD) {
        netPrices.push({value: obj.itemPriceCAD, currency: currency.value.cad.label})
      }
      if (obj?.itemPriceUS && obj?.itemFinTermMonths) {
        netPayment.push({value: (obj.itemPriceUS / obj.itemFinTermMonths) || 0, currency: currency.value.us.label})
      }
      if (obj?.itemPriceCAD && obj?.itemFinTermMonths) {
        netPayment.push({value: (obj.itemPriceCAD / obj.itemFinTermMonths) || 0, currency: currency.value.us.label})
      }

      let manufacturerFinancing = obj?.itemFinancing
      if (manufacturerFinancing && labelsData) {
        if (labelsData.for) {
          manufacturerFinancing = manufacturerFinancing.replaceAll('for', labelsData.for)
        }
        if (labelsData.months) {
          manufacturerFinancing = manufacturerFinancing.replaceAll('months', labelsData.months)
        }
      }

      return {
        ...(rebates.length && {cashRebate: rebates}),
        ...(manufacturerFinancing && {manufacturerFinancing}),
        ...(obj?.itemPriceUS && netPrices.length && {estimatedNetPrices: netPrices}),
        ...(obj?.itemPriceUS && netPayment.length && {estimatedPayment: netPayment})
      }
    }
    const {fetch} = useFetch(async () => {
      if ($regions?.current?.name === 'na') {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/dealerinventory', '/%region%/common/labels/dealerinventory', '/common/labels/functional'])
      } else {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional'])
      }

      if (model && model.category) {
        const {data} = await $services.buildandquote.getQuote(model.category)
        pricingObject.value = await preparePricingFromBQ(data.modelDetails && data.modelDetails.some(x => x.itemPart === model.id || x.itemId === parseInt(model.id))
          ? data.modelDetails.filter(x => x.itemPart === model.id || x.itemId === parseInt(model.id))[0]
          : {}, labels.value)
      }
      loaded.value = true
    })

    const categoryName = extractCategoryFromPath(modelName, model?.['@path'])
    const isAutomaticMode = ref(switchable.field === FIELD_TYPES.AUTOMATIC && Object.keys(pricingObject).length)

    onServerPrefetch(async () => {
      await fetch()
    })
    onMounted(() => {
      if (model) {
        setWithExpiry(DEALER_INVENTORY_KEYS.LAST_VISITED_MODEL, {title: model.title, name: model.name, '@name': model['@name']})
      }
      if (!loaded.value) {
        fetch()
      }
    })

    const ga4click = (event) => {
      $ga4.events.product_page_cta(event, model)
    }

    const {
      specialOfferDetails,
      specialOfferDetails2,
      specialOffersDetailsAdditionalInfo,
      suggestedPrice,
      suggestedPrice2,
      suggestedPriceAdditionalInfo
    } = useProductSalesProgram(historicalContext || isHistoricalDetails)

    const productSchema = computed(() => {
      const productPrice = suggestedPrice.value || suggestedPrice2.value
      const assetItem = {
        pageUrl: `${$config.PUBLIC_HOST}${router.currentRoute.path}`,
        name: oTitle,
        imageUrl: images[0]?.src || '',
        description: convertToPlain(descriptionManual || descriptionAutomatic || '', true),
        category: categoryName,
        price: productPrice?.replaceAll('*', '')?.replaceAll('$', '')?.replace('USD', '').trim()
      }

      return createProductObject(assetItem)
    })

    return {
      links,
      labels,
      currency,
      currentCurrency,
      buildQuoteLink,
      images,
      FIELD_TYPES,
      oTitle,
      brochure,
      download,
      historicalContext,
      productionYears,
      generateConceptHint,
      desktop,
      modelName,
      categoryName,
      pricingObject,
      descriptionManual,
      descriptionAutomatic,
      isAutomaticMode,
      specialOfferDetails,
      specialOfferDetails2,
      ga4click,
      printButton,
      handlePrint,
      suggestedPrice,
      suggestedPrice2,
      specialOffersDetailsAdditionalInfo,
      suggestedPriceAdditionalInfo,
      isHistoricalDetails,
      productSchema
    }
  },
  jsonld() {
    return [this.productSchema].filter(x => x)
  }
}
</script>

<style lang="scss">
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~/assets/sass/_colors';
@import '[media-breakpoint-up] from ~bootstrap/scss/mixins';

.list-unstyled.list-links {
  li:last-child {
    padding-bottom: 0 !important;
  }
}

.icon-link {
  color: $red
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
