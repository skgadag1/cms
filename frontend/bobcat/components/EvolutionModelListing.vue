<template>
  <section class="position-relative" :class="currentBackgroundClass" data-test-id="evolution-model-listing">
    <ModelListingGradient :display-gradient="displayGradient" />
    <EvolutionListingWithFiltering ref="listingComponentRef"
                                   :current-text-color="currentTextColor"
                                   :current-dropdown-background-color="currentDropdownBackgroundColor"
                                   :current-chips-color="currentChipsColor"
                                   :current-divider-class="currentDividerClass"
                                   :show-compare-stripe="showCompareStripe"
                                   :group="group"
                                   :title="title"
                                   :labels="labels"
                                   :hide-selected-variants="hideSelectedVariants"
                                   :hide-filters="hideFiltersOpt"
                                   :show-more-variant="showMoreVariant"
                                   :use-show-more="useShowMoreOpt"
                                   :display-at-group-level="displayAtGroupLevel"
                                   :show-currency-selector="showCurrencySelectorOpt"
                                   :hide-sorting="hideSortDropdown"
                                   :hide-view="hideDropdownView"
                                   :headline="headline"
                                   :additional-c-t-a-headline="additionalCTAHeadline"
                                   :main-c-t-a="mainCTA"
                                   :additional-c-t-a="additionalCTA"
                                   :facet="facet"
                                   :expandable="expandable"
                                   :results-per-page="resultsPerPage"
                                   :floating-filters="floatingFilters === 'true'"
                                   :include-facet-label="true"
                                   :attachment-category="resolvedAttachmentCategory"
                                   @clearCompare="clearAll"
                                   @resultListUpdate="updateResults"
    >
      <template #results>
        <div v-if="loaded" class="row" data-test-id="product-listing__results">
          <div v-for="(itm, i) in allResults"
               :key="`product-listing-result-${itm.name}-${itm.buildQuoteId || itm['@id'] || itm.publishDate}-${i}`"
               class="col-12 mb-5 mb-lg-3 evolution-product-item"
               :class="hideFiltersOpt || floatingFilters === 'true' ? 'col-md-6 col-lg-3' : 'col-md-6 col-lg-4'"
               :data-test-id="`product-listing-result-item__${i}`"
          >
            <PromoModel v-if="campaigns && campaigns.length && itm.isPromo" is-product-listing :promo-campaign="itm" />
            <EvolutionModelListingItem
              v-else-if="group && group.field === LISTING_GROUP.PRODUCTS"
              :horizontal="false"
              :item="itm"
              :labels="labels"
              :is-mobile="isMobile"
              :show-chips="false"
              :show-compare="useCompareOpt"
              :show-indicator="true"
              :inventory-search-variant="inventorySearchVariant"
              :bq-variant="bqVariant"
              :cta-variant="ctaVariant"
              :show-build-price-button="showBuildAndQuoteButtonOpt"
              :show-inventory-search-button="showInventorySearchButtonOpt"
              :hide-description="hideDescriptionOpt"
              :hide-caption="hideCaptionOpt"
              :inventory-search-link="inventorySearchLink"
              :show-view-details-button="showViewDetailsButtonOpt"
              :show-suggested-price="showSuggestedPriceOpt"
              :selected="modelsToCompare.some(x => x['@id'] === itm['@id'])"
              :disabled="modelsToCompare.length === possibleToCompare"
              :show-cta="showCTA"
              :cta-label="ctaLabel"
              :cta-label-url-target="ctaLabelUrlTarget"
              @add="(e, c) => { $ga4.events.product_compare_click(e, c); addToCompare(e)}"
              @remove="removeFromCompare"
            />
            <EvolutionAttachmentListingItem
              v-else-if="group && group.field === LISTING_GROUP.ATTACHMENTS"
              :horizontal="false"
              :item="itm"
              :labels="labels"
              :is-mobile="isMobile"
              :show-chips="false"
              :show-indicator="true"
              :bq-variant="bqVariant"
              :cta-variant="ctaVariant"
              :show-suggested-price="showSuggestedPriceOpt"
              :show-cta="showCTA"
              :cta-label="ctaLabel"
              :cta-label-url-target="ctaLabelUrlTarget"
            />
          </div>
        </div>
      </template>

      <template #extra>
        <transition name="fade">
          <CompareStripe v-show="showCompareStripe && modelsToCompare.length"
                         id="compareTable"
                         data-test-id="product-listing-compare-stripe"
                         class="mt-5"
                         dark
                         :models="modelsToCompare"
                         :labels="labels"
                         :options="compareModelsOptions.filter(x => !modelsToCompare.some(z => x['@id'] === z['@id']))"
                         :disabled="false"
                         :fixed-to-bottom="!showCompareTable"
                         :show-compare-button="!showCompareTable"
                         :show-clear-all-button="showCompareTable"
                         @remove="(e) => { removeFromCompare(e); $emit('remove', e) }"
                         @add="(e) => { $ga4.events.product_compare_click(e); addToCompare(e)}"
                         @compare="handleCompare"
                         @clearCompare="clearAll"
          />
        </transition>
        <div v-if="showCompareTable" class="bg-gray-lighter">
          <div class="container d-flex justify-content-center justify-content-md-end align-content-center">
            <div>
              <EvolutionSwitchDropdown v-if="showUnitsOpt"
                                       data-test-id="product-listing-compare-units"
                                       :label="labels.units || 'Units'"
                                       :options="uom"
                                       :config="{right:true}"
                                       class="mt-4"
                                       fuse-label-and-options-for-mobile
                                       @changed="changeUom"
              />
            </div>
          </div>
          <CompareTableWrapper data-test-id="product-listing-compare-table"
                               class="my-4 product-listing-compare-table"
                               no-bottom-margin
                               :models="modelsToCompare"
                               :uom="currentUom"
                               :config="productCompareConfig"
                               :labels="labels"
                               @data-updated="dataUpdated"
          />
        </div>
      </template>
    </EvolutionListingWithFiltering>
  </section>
</template>

<script>
import {inject, onBeforeUnmount, onMounted, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import EvolutionListingWithFiltering from '~/components/LoadersEvolution/Listings/EvolutionListingWithFiltering.vue'
import EvolutionModelListingItem from '~/components/LoadersEvolution/Listings/EvolutionModelListingItem.vue'
import EvolutionAttachmentListingItem from '~/components/LoadersEvolution/Listings/EvolutionAttachmentListingItem.vue'
import EvolutionSwitchDropdown from '~/components/LoadersEvolution/Listings/EvolutionSwitchDropdown.vue'
import CompareStripe from '~/components/Compare/CompareStripe.vue'
import CompareTableWrapper from '~/components/Compare/CompareTableWrapper.vue'
import ModelListingGradient from '~/components/elements/Shapes/ModelListingGradient.vue'
import useCompare from '~/helpers/composables/useCompare'
import {METRICS, PRODUCT_SPECS_REMOVED_PROPERTIES, PRODUCT_SPECS_REMOVED_SECTIONS} from '~/constants'
import {LISTING_GROUP} from '~/constants/buildAndPrice'
import useMounted from '~/helpers/composables/useMounted'
import {BLACK_GRADIENT_VARIANT, VARIANT_PROPERTIES} from '~/constants/variants/loadersEvolutionVariants'

const MAX_RESULTS_COMPARE_STRIPE = 100 // assumption: there will be less than MAX_RESULTS in selection, otherwise for performance we get MAX_RESULTS (for model dropdown in stripe)

export default {
  components: {
    CompareStripe,
    CompareTableWrapper,
    EvolutionSwitchDropdown,
    EvolutionModelListingItem,
    EvolutionAttachmentListingItem,
    EvolutionListingWithFiltering,
    ModelListingGradient
  },
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    hideFilters: {
      type: String,
      default: 'false'
    },
    hideDescription: {
      type: String,
      default: 'false'
    },
    hideCaption: {
      type: String,
      default: 'false'
    },
    useShowMore: {
      type: String,
      default: 'false'
    },
    useCompare: {
      type: String,
      default: 'false'
    },
    showUnits: {
      type: String,
      default: 'false'
    },
    showCurrencySelector: {
      type: String,
      default: 'false'
    },
    showViewDetailsButton: {
      type: String,
      default: 'false'
    },
    showBuildAndQuoteButton: {
      type: String,
      default: 'true'
    },
    showSuggestedPrice: {
      type: String,
      default: 'false'
    },
    showInventorySearchButton: {
      type: String,
      default: 'false'
    },
    inventorySearchLink: {
      type: Object
    },
    displayAtGroupLevel: {
      type: String,
      default: 'false'
    },
    group: {
      type: [Object, String],
      default: null
    },
    facet: {
      type: [Object, String],
      default: null
    },
    useCollapse: {
      type: String,
      default: 'true'
    },
    hideSorting: {
      type: String,
      default: 'false'
    },
    hideView: {
      type: String,
      default: 'false'
    },
    resultsPerPage: {
      type: String
    },
    showCTA: {
      type: String,
      default: 'false'
    },
    ctaLabel: {
      type: String
    },
    ctaLabelUrlTarget: {
      type: String,
      default: '_parent'
    },
    hideSelectedVariants: {
      type: String,
      default: 'false'
    },
    variant: {
      type: Object
    },
    bqVariant: {
      type: Object
    },
    ctaVariant: {
      type: Object
    },
    showMoreVariant: {
      type: Object
    },
    inventorySearchVariant: {
      type: Object
    },
    additionalCTAHeadline: {
      type: String
    },
    headline: {
      type: String
    },
    mainCTA: {
      type: Object
    },
    additionalCTA: {
      type: Object
    },
    floatingFilters: {
      type: String,
      default: 'false'
    }
  },
  setup(props) {
    const {$services, $ga4, $regions, $utils} = useContext()
    const listingComponentRef = ref(null)
    const campaigns = inject('campaigns')
    const allResults = ref([])
    const loaded = ref(false)
    const hideSortDropdown = props.hideSorting === 'true'
    const hideDropdownView = props.hideView === 'true'
    const hideFiltersOpt = props.hideFilters === 'true'
    const hideDescriptionOpt = props.hideDescription === 'true'
    const hideCaptionOpt = props.hideCaption === 'true'
    const useShowMoreOpt = props.useShowMore === 'true'
    const useCompareOpt = props.useCompare === 'true'
    const showUnitsOpt = props.showUnits === 'true'
    const showCurrencySelectorOpt = props.showCurrencySelector === 'true'
    const showBuildAndQuoteButtonOpt = props.showBuildAndQuoteButton === 'true'
    const showInventorySearchButtonOpt = props.showInventorySearchButton === 'true'
    const showViewDetailsButtonOpt = props.showViewDetailsButton === 'true'
    const showSuggestedPriceOpt = props.showSuggestedPrice === 'true'
    const possibleToCompare = 3
    const compareModelsOptions = ref([])
    const isNA = ref($regions.current.name === 'na')
    const isMobile = ref(false)
    const currentVariant = ref(props.variant?.field)
    const currentBackgroundClass = ref(VARIANT_PROPERTIES[currentVariant.value || BLACK_GRADIENT_VARIANT]?.backgroundClass)
    const displayGradient = ref(VARIANT_PROPERTIES[currentVariant.value || BLACK_GRADIENT_VARIANT]?.displayGradient)
    const currentTextColor = ref(VARIANT_PROPERTIES[currentVariant.value || BLACK_GRADIENT_VARIANT]?.textClass)
    const currentDropdownBackgroundColor = ref(VARIANT_PROPERTIES[currentVariant.value || BLACK_GRADIENT_VARIANT]?.dropdownBackgroundClass)
    const currentChipsColor = ref(VARIANT_PROPERTIES[currentVariant.value || BLACK_GRADIENT_VARIANT]?.chipsVariant)
    const currentDividerClass = ref(VARIANT_PROPERTIES[currentVariant.value || BLACK_GRADIENT_VARIANT]?.dividerClass)
    const resolvedAttachmentCategory = props.group?.attachmentCategory || null

    const expandable = props.useCollapse === 'true'
    const {
      showCompareStripe,
      showCompareTable,
      enableCompareTable,
      modelsToCompare,
      addToCompare,
      removeFromCompare,
      clearAll
    } = useCompare([], 0)
    useMounted(props.nodeId)

    const labels = ref({})

    useFetch(async () => {
      if (!isNA.value) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications', '/common/labels/listing'])
      }
    })

    const compareIndexes = (a, b) => a.positionIndex - b.positionIndex

    const transformResultData = (data) => {
      const maxPropsInResult = data?.reduce((max, current) => max > current.keyProperties?.length ? max : current.keyProperties?.length, 0)
      return data?.map((x) => {
        if (x.keyProperties?.length < maxPropsInResult) {
          x.keyProperties = [...x.keyProperties, ...Array(maxPropsInResult - x.keyProperties?.length).fill({name: ''})]
        }

        return {
          ...x,
          title: x.name,
          description: x.shortDescription,
          ...(x.keyProperties && {
            properties: x.keyProperties.map((p) => {
              return {
                ...p,
                value: p.value ? p.value + '&nbsp;' + p.unit : '&nbsp;'
              }
            })
          })
        }
      }) || []
    }

    const updateResults = async (payload) => {
      try {
        allResults.value = await transformResultData(payload?.results || [])
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(`[ProductListing] transforming results error ${e}`)
      }

      if (process.client && payload.page === 1) {
        clearAll()
        if (useCompareOpt) {
          await fetchModelOptionsToCompare()
        }
      }

      if (campaigns?.value?.length > 0) {
        campaigns.value.sort(compareIndexes).forEach(c => allResults.value?.splice(c?.positionIndex, 0, {
          ...c,
          isPromo: true
        }))
      }

      loaded.value = true
    }

    const fetchModelOptionsToCompare = async () => {
      // get all models to compare with
      // call this as "initial" not to update query params in url [NGB-4252]
      const options = await listingComponentRef.value?.fetchListingResults({
        page: 0,
        perPage: MAX_RESULTS_COMPARE_STRIPE,
        skipCompatibleAttachments: true
      }, true)
      compareModelsOptions.value = await transformResultData(options?.results || [])
    }

    const handleCompare = () => {
      enableCompareTable()

      if (modelsToCompare.value?.length) {
        $ga4.events.product_compare_cta(modelsToCompare.value)
      }
    }

    const dataUpdated = () => {
      if (!process.client || !window?.document) {
        return
      }
      setTimeout(() => {
        const y = document.getElementById('compareTable')?.getBoundingClientRect().top + window.pageYOffset - document.querySelector('header[data-test-id="main-header"]')?.clientHeight - 1
        window.scrollTo({top: y, behavior: 'smooth'})
      }, 200)
    }

    const productCompareConfig = {
      filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
      filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
      alwaysShowHeader: true,
      noBottomMargin: true
    }

    const uom = METRICS
    const currentUom = ref(uom[0])

    const changeUom = (newVal) => {
      currentUom.value = newVal
    }

    const checkSize = () => {
      isMobile.value = $utils.isMobile()
    }

    onMounted(async () => {
      if (useCompareOpt) {
        fetchModelOptionsToCompare()
      }

      if (isNA.value && process.client) {
        const currentEquipmentPath = window?.location?.pathname?.split('/equipment/')?.[1]
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/specifications', '/common/labels/listing', `/%region%/products/${currentEquipmentPath}`])
      }

      if (process.client) {
        checkSize()
        window.addEventListener('resize', checkSize)
        setTimeout(() => {
          window.dispatchEvent(new Event('resize'))
        }, 200)
      }
    })

    onBeforeUnmount(() => window.removeEventListener('resize', checkSize))

    return {
      LISTING_GROUP,
      allResults,
      hideFiltersOpt,
      hideDescriptionOpt,
      hideCaptionOpt,
      useShowMoreOpt,
      useCompareOpt,
      showUnitsOpt,
      showCurrencySelectorOpt,
      showViewDetailsButtonOpt,
      showBuildAndQuoteButtonOpt,
      possibleToCompare,
      compareModelsOptions,
      loaded,
      updateResults,
      showCompareStripe,
      modelsToCompare,
      addToCompare,
      removeFromCompare,
      clearAll,
      labels,
      handleCompare,
      listingComponentRef,
      campaigns,
      productCompareConfig,
      showCompareTable,
      dataUpdated,
      uom,
      currentUom,
      changeUom,
      expandable,
      hideSortDropdown,
      hideDropdownView,
      $ga4,
      isNA,
      showInventorySearchButtonOpt,
      showSuggestedPriceOpt,
      isMobile,
      resolvedAttachmentCategory,
      currentBackgroundClass,
      currentDropdownBackgroundColor,
      currentVariant,
      displayGradient,
      currentTextColor,
      currentChipsColor,
      currentDividerClass
    }
  }
}
</script>
