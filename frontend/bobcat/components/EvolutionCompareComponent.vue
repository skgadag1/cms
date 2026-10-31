<template>
  <section class="bg-white"
           data-test-id="evolution-compare-component"
           :style="compareStickyCssVars"
  >
    <b-overlay :show="fetchState.pending && !flatModels.length">
      <div class="container py-5">
        <header class="evolution-compare-header mb-5" :class="selectedAlignmentVariant">
          <div v-if="title"
               class="mb-0"
               data-test-id="evolution-compare-component-title"
               v-html="addYoutubeNoCookie(title)"
          />
          <div v-if="subtitle"
               class="text-uppercase font-weight-bold h4 mb-3"
               data-test-id="evolution-compare-component-subtitle"
               v-html="addYoutubeNoCookie(subtitle)"
          />
          <div v-if="description"
               class="mb-2"
               data-test-id="evolution-compare-component-description"
               v-html="addYoutubeNoCookie(description)"
          />
          <DynamicButton v-if="sectionCtaResolved && sectionCtaResolved.url"
                         :variant="sectionCtaButtonVariant || BUTTON_NAMES.LINEAR"
                         :color="sectionCtaButtonColor"
                         :href="sectionCtaResolved.url"
                         :target="sectionCtaLinkTarget"
                         class="text-uppercase font-weight-bold"
                         :class="{'p-0 text-left': sectionCtaButtonVariant === BUTTON_NAMES.LINEAR}"
                         data-test-id="evolution-compare-section-cta"
          >
            {{ sectionCtaResolved.label }}
            <b-icon icon="chevron-right" class="ml-1 small" />
          </DynamicButton>
        </header>

        <b-alert v-if="fetchError"
                 show
                 variant="warning"
                 class="mb-0"
                 data-test-id="evolution-compare-error"
        >
          {{ (labels || {}).compareError || 'Unable to load models for comparison.' }}
        </b-alert>

        <template v-else-if="!fetchError && compareSlots.length">
          <b-alert v-if="!flatModels.length"
                   show
                   variant="light"
                   class="border border-gray-light mb-4"
                   data-test-id="evolution-compare-no-models"
          >
            {{ (labels || {}).noModelsToCompare || 'No models are available for comparison in this category.' }}
          </b-alert>
          <div class="evolution-compare-tiles-slider"
               role="region"
               :class="{'evolution-compare-tiles-slider--at-end': tilesSliderAtEnd}"
               :aria-label="(labels || {}).compareSwipeHint || 'Compare models, scroll sideways for more'"
               data-test-id="evolution-compare-tiles-slider"
          >
            <b-row ref="tilesScrollRow"
                   class="evolution-compare-tiles-row evolution-compare-tiles-row--responsive align-items-stretch pt-0 mb-4 pt-md-5"
                   @scroll.passive="updateTilesSliderScrollEnd"
            >
              <b-col v-for="(slotModel, idx) in compareSlots"
                     :key="`evolution-compare-slot-${idx}`"
                     cols="12"
                     sm="4"
                     class="mb-0 mb-sm-4 evolution-compare-slot--stretch d-flex flex-column min-h-0"
              >
                <div class="evolution-compare-slot__inner d-flex flex-column flex-grow-1 w-100 min-h-0">
                  <div class="mb-3 d-flex align-items-center flex-nowrap">
                    <div class="evolution-compare-slot__dropdown">
                      <EvolutionCompareModelDropdown :models="flatModels"
                                                     :value="slotModel"
                                                     :exclude-ids="excludeOtherSlotIds(idx)"
                                                     :placeholder="searchPlaceholder"
                                                     :choose-label="chooseModelLabel"
                                                     :labels="labels"
                                                     :data-test-id="`evolution-compare-slot-dropdown-${idx}`"
                                                     @input="setSlotModel(idx, $event)"
                      />
                    </div>
                    <button v-if="slotModel && canRemoveModel"
                            type="button"
                            class="evolution-compare-remove-model border-0 bg-transparent text-dark d-flex align-items-center justify-content-center ml-2 p-0"
                            :aria-label="`${removeModelLabel} ${slotModel.title || slotModel['@name'] || ''}`"
                            :data-test-id="`evolution-compare-slot-remove-${idx}`"
                            @click="removeSlotModel(idx)"
                    >
                      <b-icon icon="close" />
                    </button>
                  </div>

                  <EvolutionCompareTile v-if="detailedSlots[idx]"
                                        :model="detailedSlots[idx]"
                                        :labels="labels"
                                        :primary-cta-button="primaryCtaButton"
                                        :model-details-label="modelDetailsLabel"
                                        :model-details-fallback-link="modelDetailsFallbackLink"
                                        :model-details-cta-button="modelDetailsCtaButton"
                                        :model-details-target="modelDetailsTarget"
                                        :starting-at-price-label="startingAtPriceLabel"
                                        class="flex-grow-1"
                                        @bp-click="onBpGa"
                                        @details-click="onDetailsGa"
                  />
                  <b-overlay v-else-if="slotModel"
                             show
                             variant="white"
                             spinner-variant="primary"
                             class="evolution-compare-slot-empty flex-grow-1"
                             :data-test-id="`evolution-compare-slot-loading-${idx}`"
                  />
                  <div v-else
                       class="evolution-compare-slot-empty flex-grow-1 d-flex flex-column align-items-center justify-content-center text-center text-muted p-4"
                       :data-test-id="`evolution-compare-slot-empty-${idx}`"
                  >
                    <strong class="evolution-compare-slot-empty__title text-dark text-uppercase mb-1">
                      {{ selectModelPromptLabel }}
                    </strong>
                    <span class="evolution-compare-slot-empty__text">{{ selectModelPromptDescription }}</span>
                  </div>
                </div>
              </b-col>
              <b-col v-if="canAddSlot"
                     cols="12"
                     sm="4"
                     class="mb-3 mb-sm-4 d-flex align-items-start justify-content-sm-end evolution-compare-add-model-col"
                     data-test-id="evolution-compare-add-model-column"
              >
                <DynamicButton type="button"
                               :variant="BUTTON_NAMES.HEXAGONAL"
                               :color="BUTTON_COLORS.PRIMARY"
                               class="text-uppercase font-weight-bold text-nowrap evolution-compare-add-model-btn"
                               data-test-id="evolution-compare-add-model"
                               @click="addModelSlot"
                >
                  <b-icon icon="plus" class="mr-2" />
                  {{ addModelLabel }}
                </DynamicButton>
              </b-col>
            </b-row>
          </div>

          <div v-if="activeCompareModels.length >= 2"
               ref="compareSpecsRegionRef"
               class="position-relative"
               data-test-id="evolution-compare-specs-region"
          >
            <hr class="border-gray-light my-4 my-lg-5 w-100">

            <div ref="compareSpecsStickySentinel"
                 class="pb-4"
                 data-test-id="evolution-compare-specs-sticky-sentinel"
            >
              <b-form-checkbox v-model="showOnlyDifferences"
                               class="custom-checkbox mb-0"
                               data-test-id="evolution-compare-specs-only-differences"
              >
                {{ showOnlyDifferencesLabelResolved }}
              </b-form-checkbox>
            </div>

            <div v-show="specsFullyExpanded && showSpecsStickyDropdowns"
                 class="evolution-compare-specs-sticky-bar"
                 data-test-id="evolution-compare-specs-sticky-dropdowns"
            >
              <div class="ec-compare-hscroll-wrap w-100"
                   data-ec-compare-hscroll-wrap
              >
                <div class="container">
                  <div class="evolution-compare-specs-sticky-bar__scroll"
                       data-ec-compare-hscroll
                  >
                    <div class="evolution-compare-specs-sticky-bar__row row no-gutters align-items-stretch mx-0 flex-nowrap flex-lg-wrap evolution-compare-specs-sticky-bar__inner">
                      <div v-for="(slotModel, idx) in compareSlots"
                           :key="`evolution-compare-specs-sticky-slot-${idx}`"
                           class="evolution-compare-specs-sticky-bar__col d-flex flex-column mb-2 mb-lg-0"
                           :class="stickyColumnClass"
                      >
                        <EvolutionCompareModelDropdown :models="flatModels"
                                                       :value="slotModel"
                                                       :exclude-ids="excludeOtherSlotIds(idx)"
                                                       :placeholder="searchPlaceholder"
                                                       :choose-label="chooseModelLabel"
                                                       :labels="labels"
                                                       :data-test-id="`evolution-compare-specs-sticky-dropdown-${idx}`"
                                                       class="w-100"
                                                       @input="setSlotModel(idx, $event)"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="position-relative">
              <EvolutionCompareSpecifications :models="compareSpecModels"
                                              :labels="labels || {}"
                                              :compare-config="evolutionCompareSpecsConfig"
                                              :show-only-differences="showOnlyDifferences"
                                              :section-icon-mappings="specSectionIconMappingsFromLabels"
                                              :expand-all-specs="specsFullyExpanded"
                                              data-test-id="evolution-compare-specs-panel"
                                              @specs-loading-overlay="onSpecsLoadingOverlay"
              />

              <div v-if="!specsFullyExpanded"
                   class="evolution-compare-specs-preview-fade"
                   aria-hidden="true"
                   data-test-id="evolution-compare-specs-preview-fade"
              />

              <div v-if="!specsFullyExpanded"
                   class="d-flex justify-content-center evolution-compare-specs-show-more-wrap pt-1 pb-2"
                   data-test-id="evolution-compare-specs-show-more-wrap"
              >
                <DynamicButton type="button"
                               :variant="BUTTON_NAMES.HEXAGONAL"
                               :color="BUTTON_COLORS.PRIMARY"
                               class="text-uppercase"
                               data-test-id="evolution-compare-specs-show-more"
                               @click="specsFullyExpanded = true"
                >
                  {{ showMoreSpecsLabelResolved }}
                </DynamicButton>
              </div>
            </div>

            <div v-if="specsFullyExpanded"
                 ref="compareSpecsStickyEndSentinel"
                 class="evolution-compare-specs-sticky-end-sentinel"
                 aria-hidden="true"
                 data-test-id="evolution-compare-specs-sticky-end-sentinel"
            />

            <template v-if="specsFullyExpanded && !specsLoadingOverlayActive">
              <hr class="evolution-compare-specs-footer-hr my-4 my-lg-5 w-100"
                  data-test-id="evolution-compare-specs-footer-divider"
              >

              <div class="evolution-compare-tiles-slider"
                   role="region"
                   :class="{'evolution-compare-tiles-slider--at-end': bottomTilesSliderAtEnd}"
                   :aria-label="(labels || {}).compareBottomCardsHint || 'Model pricing and actions'"
                   data-test-id="evolution-compare-bottom-cards"
              >
                <b-row ref="bottomTilesScrollRow"
                       class="evolution-compare-tiles-row evolution-compare-tiles-row--responsive align-items-stretch pt-0 mb-0"
                       data-ec-compare-hscroll
                       @scroll.passive="onCompareSpecsHScrollPortScroll"
                >
                  <b-col v-for="(slotModel, idx) in compareSlots"
                         :key="`evolution-compare-bottom-slot-${idx}`"
                         cols="12"
                         :sm="comparisonColumnSpan"
                         class="mb-0 mb-sm-4 evolution-compare-slot--stretch d-flex flex-column min-h-0"
                  >
                    <div class="evolution-compare-slot__inner d-flex flex-column flex-grow-1 w-100 min-h-0">
                      <EvolutionCompareTile v-if="detailedSlots[idx]"
                                            footer-only
                                            :model="detailedSlots[idx]"
                                            :labels="labels"
                                            :primary-cta-button="primaryCtaButton"
                                            :model-details-label="modelDetailsLabel"
                                            :model-details-fallback-link="modelDetailsFallbackLink"
                                            :model-details-cta-button="modelDetailsCtaButton"
                                            :model-details-target="modelDetailsTarget"
                                            :starting-at-price-label="startingAtPriceLabel"
                                            class="w-100 flex-grow-1"
                                            @bp-click="onBpGa"
                                            @details-click="onDetailsGa"
                      />
                      <div v-else
                           class="evolution-compare-slot-empty flex-grow-1 d-flex align-items-center justify-content-center text-muted small p-4 border border-gray-light"
                           :data-test-id="`evolution-compare-bottom-slot-empty-${idx}`"
                      >
                        {{ chooseModelLabel }}
                      </div>
                    </div>
                  </b-col>
                </b-row>
              </div>
            </template>

            <div v-if="specsFullyExpanded"
                 class="d-flex justify-content-center evolution-compare-specs-show-more-wrap pt-1 pb-2"
                 data-test-id="evolution-compare-specs-show-more-wrap"
            >
              <DynamicButton type="button"
                             :variant="BUTTON_NAMES.HEXAGONAL"
                             :color="BUTTON_COLORS.PRIMARY"
                             class="text-uppercase"
                             data-test-id="evolution-compare-specs-show-more"
                             @click="specsFullyExpanded = false"
              >
                {{ showLessSpecsLabelResolved }}
              </DynamicButton>
            </div>
          </div>
        </template>
      </div>
    </b-overlay>
  </section>
</template>

<script>
import {
  ref,
  computed,
  watch,
  onMounted,
  ssrRef,
  useContext,
  useRoute
} from '@nuxtjs/composition-api'
import EvolutionCompareTile from '~/components/EvolutionCompare/EvolutionCompareTile.vue'
import EvolutionCompareModelDropdown from '~/components/EvolutionCompare/EvolutionCompareModelDropdown.vue'
import EvolutionCompareSpecifications from '~/components/EvolutionCompare/EvolutionCompareSpecifications.vue'
import DynamicButton from '~/components/DynamicButton.vue'
import {addYoutubeNoCookie} from '~/helpers/functional/AppHelpers'
import useMounted from '~/helpers/composables/useMounted'
import useEvolutionCompareScroll from '~/helpers/composables/useEvolutionCompareScroll'
import {PRODUCT_SPECS_REMOVED_PROPERTIES, PRODUCT_SPECS_REMOVED_SECTIONS} from '~/constants'
import {
  collectCompareModels,
  loadCompareModelDetails,
  resolveExcludedSelectors,
  applyExcludedModels
} from '~/helpers/evolutionCompareModelSource'
import {EVOLUTION_COMPARE_QUERY_KEY, parseCompareQuery, serializeCompareQuery} from '~/helpers/evolutionCompareQuery'
import {BUTTON_NAMES, BUTTON_COLORS} from '~/constants/buttons'
import {ALIGNMENT_CLASSES, LEFT_ALIGNMENT} from '~/constants/variants/sectionTextVariants'
import {coerceSpecSectionIconMappingsList} from '~/helpers/evolutionCompareSpecsFilter'
import {
  MAX_COMPARE_SLOT_COUNT,
  compareModelKey,
  buildCompareSlots,
  addCompareSlot,
  removeCompareSlot
} from '~/helpers/evolutionCompareSlots'

export default {
  components: {
    EvolutionCompareTile,
    EvolutionCompareModelDropdown,
    EvolutionCompareSpecifications,
    DynamicButton
  },
  props: {
    nodeId: {type: String},
    title: {type: String, default: ''},
    subtitle: {type: String, default: ''},
    description: {type: String, default: ''},
    sectionAlignment: {type: String, default: LEFT_ALIGNMENT},
    sectionCtaLink: {type: Object, default: null},
    sectionCtaButton: {type: Object, default: () => ({})},
    dataSource: {type: Object, default: null},
    excludedModels: {type: [Array, Object], default: () => []},
    defaultModel1: {type: String, default: null},
    defaultModel2: {type: String, default: null},
    defaultModel3: {type: String, default: null},
    primaryCtaButton: {type: Object, default: () => ({})},
    modelDetailsLabel: {type: String, default: ''},
    modelDetailsFallbackLink: {type: Object, default: null},
    modelDetailsCtaButton: {type: Object, default: () => ({})},
    modelDetailsTarget: {type: String, default: '_self'},
    startingAtPriceLabel: {type: String, default: ''},
    showMoreSpecsLabel: {type: String, default: ''},
    showLessSpecsLabel: {type: String, default: ''},
    showOnlyDifferencesLabel: {type: String, default: ''}
  },
  setup(props) {
    const {$services, $utils, $regions, $ga4} = useContext()
    const route = useRoute()
    useMounted(props.nodeId)

    const ssrKey = `evolution-compare-${props.nodeId || 'default'}`
    const flatModels = ssrRef([], `${ssrKey}-flat`)
    const fetchError = ssrRef(false, `${ssrKey}-error`)
    const labels = ssrRef({}, `${ssrKey}-labels`)
    const compareSlots = ssrRef([], `${ssrKey}-slots`)
    const specsFullyExpanded = ref(false)
    const showOnlyDifferences = ref(false)
    const specsLoadingOverlayActive = ref(false)

    const onSpecsLoadingOverlay = (show) => {
      specsLoadingOverlayActive.value = Boolean(show)
    }

    const {
      tilesScrollRow,
      tilesSliderAtEnd,
      bottomTilesScrollRow,
      bottomTilesSliderAtEnd,
      compareSpecsStickySentinel,
      compareSpecsStickyEndSentinel,
      compareSpecsRegionRef,
      showSpecsStickyDropdowns,
      compareStickyCssVars,
      updateTilesSliderScrollEnd,
      onCompareSpecsHScrollPortScroll,
      refreshScrollBindings
    } = useEvolutionCompareScroll({specsFullyExpanded, specsLoadingOverlayActive, showOnlyDifferences})

    const activeCompareModels = computed(() => compareSlots.value.filter(Boolean))
    const canAddSlot = computed(() => compareSlots.value.length < MAX_COMPARE_SLOT_COUNT)
    const canRemoveModel = computed(() => activeCompareModels.value.length > 1)
    const comparisonColumnSpan = computed(() => compareSlots.value.length === 2 ? 6 : 4)
    const stickyColumnClass = computed(() =>
      compareSlots.value.length === 2 ? 'col-lg-6' : 'col-lg-4'
    )

    const evolutionCompareSpecsConfig = {
      filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
      filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
      alwaysShowHeader: true,
      noBottomMargin: true
    }

    const showMoreSpecsLabelResolved = computed(() =>
      props.showMoreSpecsLabel?.trim() || 'Show more'
    )

    const showLessSpecsLabelResolved = computed(() =>
      props.showLessSpecsLabel?.trim() || 'Show less'
    )

    const showOnlyDifferencesLabelResolved = computed(() =>
      props.showOnlyDifferencesLabel?.trim() || 'Show only differences'
    )

    const searchPlaceholder = computed(() =>
      labels.value?.compareSearchPlaceholder || labels.value?.searchPlaceholder || 'Search...'
    )

    const chooseModelLabel = computed(() => labels.value?.chooseModel || labels.value?.selectModel || 'Select model')

    const addModelLabel = computed(() => labels.value?.addModel || labels.value?.compareAddModel || 'Add model')

    const removeModelLabel = computed(() => labels.value?.removeModel || labels.value?.compareRemoveModel || 'Remove model')

    const selectModelPromptLabel = computed(() =>
      labels.value?.compareSelectModelPrompt || labels.value?.selectModel || 'Select a model'
    )

    const selectModelPromptDescription = computed(() =>
      labels.value?.compareSelectModelDescription ||
      labels.value?.selectModelDescription ||
      'Add a model to compare specifications and features.'
    )

    const specSectionIconMappingsFromLabels = computed(() => {
      const L = labels.value || {}
      const raw =
        L.compareSpecSectionIcons ??
        L.specCompareSectionIcons ??
        L.evolutionCompareSpecSectionIcons
      return coerceSpecSectionIconMappingsList(raw)
    })

    const sectionCtaResolved = computed(() =>
      $utils.getLinkField?.(props.sectionCtaLink, $regions.current.language?.name)
    )

    const selectedAlignmentVariant = computed(() => ALIGNMENT_CLASSES[props.sectionAlignment] || '')

    const sectionCtaButtonVariant = computed(() => props.sectionCtaButton?.field || null)
    const sectionCtaButtonColor = computed(() => props.sectionCtaButton?.color || null)

    const sectionCtaLinkTarget = computed(() => {
      const t = sectionCtaResolved.value?.target
      return t === '_blank_follow' ? '_blank' : (t || '_self')
    })

    const modelKey = compareModelKey

    const excludeOtherSlotIds = idx => compareSlots.value
      .map((m, i) => (i !== idx && m ? modelKey(m) : null))
      .filter(Boolean)

    const resolveLinkId = (link) => {
      if (!link) {
        return null
      }
      if (typeof link === 'string') {
        return link
      }
      return link['@id'] || link.id || null
    }

    const getConfiguredDefaultModelIds = () => {
      return [props.defaultModel1, props.defaultModel2, props.defaultModel3]
        .map(resolveLinkId)
    }

    const resolveDataSourceNodeId = () => {
      const ds = props.dataSource
      const mode = String(ds?.field || '').toLowerCase()
      if (!mode) {
        return null
      }
      const propName = mode === 'category' ? 'productCategory' : 'productSubcategory'
      const link = ds?.[propName] ?? ds?.[mode]?.[propName]
      return resolveLinkId(link)
    }

    const modelDetailsByKey = ref({})
    const pendingModelDetails = new Map()

    const ensureModelDetails = (model) => {
      const key = modelKey(model)
      if (!key) {
        return Promise.resolve(model)
      }
      if (modelDetailsByKey.value[key]) {
        return Promise.resolve(modelDetailsByKey.value[key])
      }
      if (!pendingModelDetails.has(key)) {
        pendingModelDetails.set(key, loadCompareModelDetails($services, model).then((details) => {
          modelDetailsByKey.value = {...modelDetailsByKey.value, [key]: details}
          pendingModelDetails.delete(key)
          return details
        }))
      }
      return pendingModelDetails.get(key)
    }

    const detailedSlots = computed(() => compareSlots.value.map(model =>
      model ? modelDetailsByKey.value[modelKey(model)] || null : null
    ))

    const compareSpecModels = ref([])

    const syncQuery = () => {
      if (!process.client || typeof window === 'undefined') {
        return
      }
      const url = new URL(window.location.href)
      const ids = activeCompareModels.value.map(modelKey).filter(Boolean)
      const next = serializeCompareQuery(ids)
      const cur = url.searchParams.get(EVOLUTION_COMPARE_QUERY_KEY) || ''
      if (next === cur) {
        return
      }
      if (next) {
        url.searchParams.set(EVOLUTION_COMPARE_QUERY_KEY, next)
      } else {
        url.searchParams.delete(EVOLUTION_COMPARE_QUERY_KEY)
      }
      window.history.replaceState(window.history.state, '', `${url.pathname}${url.search}${url.hash}`)
    }

    const getCompareQueryFromLocation = () => {
      if (process.client && typeof window !== 'undefined') {
        return new URL(window.location.href).searchParams.get(EVOLUTION_COMPARE_QUERY_KEY) || ''
      }
      const q = route.value?.query || {}
      return String(q[EVOLUTION_COMPARE_QUERY_KEY] || '')
    }

    const buildSlotsFromFlatModelsAndRoute = () => {
      const urlIds = parseCompareQuery(getCompareQueryFromLocation())
      const defaultIds = getConfiguredDefaultModelIds()
      compareSlots.value = buildCompareSlots(flatModels.value, urlIds, defaultIds)
    }

    const setSlotModel = (idx, model) => {
      compareSlots.value = compareSlots.value.map((m, i) => i === idx ? model : m)
      if (process.client && model?.['@id']) {
        ensureModelDetails(model).then(details => $ga4?.events?.product_compare_click?.(details))
      }
    }

    const addModelSlot = () => {
      compareSlots.value = addCompareSlot(compareSlots.value)
    }

    const removeSlotModel = (idx) => {
      if (!canRemoveModel.value || !compareSlots.value[idx]) {
        return
      }
      compareSlots.value = removeCompareSlot(compareSlots.value, idx)
    }

    const resolveNaEquipmentLabelPath = () => {
      if ($regions.current.name !== 'na') {
        return null
      }
      const pathname = process.client
        ? window?.location?.pathname
        : route.value?.path
      const equipmentSuffix = pathname?.split('/equipment/')?.[1]
      if (!equipmentSuffix) {
        return null
      }
      const segment = equipmentSuffix.split('?')[0].replace(/\/$/, '')
      return segment ? `/%region%/products/${segment}` : null
    }

    const loadCompareData = async () => {
      fetchError.value = false
      try {
        const labelPaths = ['/common/labels/specifications', '/%region%/common/labels/specifications', '/common/labels/listing']
        const naEquipmentPath = resolveNaEquipmentLabelPath()
        if (naEquipmentPath) {
          labelPaths.push(naEquipmentPath)
        }
        labels.value = await $services.bobcatapi.getConcepts(labelPaths)
        const nodeId = resolveDataSourceNodeId()
        if (!nodeId) {
          fetchError.value = true
          return
        }
        const [compareModels, excludedSelectors] = await Promise.all([
          collectCompareModels($services, nodeId),
          resolveExcludedSelectors($services, props.excludedModels)
        ])
        let models = compareModels
        models = applyExcludedModels(models, excludedSelectors)
        flatModels.value = models
        buildSlotsFromFlatModelsAndRoute()
        syncQuery()
      } catch (err) {
        // eslint-disable-next-line no-console
        console.error('[compare] loadCompareData failed:', err)
        fetchError.value = true
      }
    }
    const fetchState = ref({
      pending: false
    })
    const refetchCompareData = async () => {
      if (process.server) {
        return
      }
      fetchState.value.pending = true
      try {
        await loadCompareData()
      } finally {
        fetchState.value.pending = false
      }
    }

    watch(compareSlots, () => {
      syncQuery()
      if (!process.server) {
        activeCompareModels.value.forEach(ensureModelDetails)
      }
    }, {deep: true})

    let lastCompareSig = ''
    watch(detailedSlots, () => {
      const detailed = activeCompareModels.value.map(model => modelDetailsByKey.value[modelKey(model)])
      if (detailed.some(model => !model)) {
        return
      }
      const sig = detailed.map(modelKey).join(',')
      if (sig === lastCompareSig) {
        return
      }
      lastCompareSig = sig
      compareSpecModels.value = detailed
      if (detailed.length >= 2 && process.client) {
        $ga4?.events?.product_compare_cta?.(detailed)
      }
    })

    watch([compareSlots, flatModels, detailedSlots], refreshScrollBindings, {deep: true})

    watch(() => fetchState.value?.pending, (pending) => {
      if (!pending) {
        refreshScrollBindings()
      }
    })

    onMounted(() => {
      refetchCompareData()
    })

    const onBpGa = (model) => {
      if (process.client) {
        $ga4?.events?.product_compare_buildAndPrice_click?.(model)
      }
    }

    const onDetailsGa = (model) => {
      if (process.client) {
        $ga4?.events?.product_compare_modelDetails_click?.(model)
      }
    }

    return {
      BUTTON_NAMES,
      BUTTON_COLORS,
      addYoutubeNoCookie,
      selectedAlignmentVariant,
      tilesScrollRow,
      tilesSliderAtEnd,
      bottomTilesScrollRow,
      bottomTilesSliderAtEnd,
      updateTilesSliderScrollEnd,
      onCompareSpecsHScrollPortScroll,
      sectionCtaResolved,
      sectionCtaButtonVariant,
      sectionCtaButtonColor,
      sectionCtaLinkTarget,
      flatModels,
      fetchError,
      labels,
      compareSlots,
      detailedSlots,
      activeCompareModels,
      compareSpecModels,
      canAddSlot,
      canRemoveModel,
      comparisonColumnSpan,
      stickyColumnClass,
      excludeOtherSlotIds,
      setSlotModel,
      addModelSlot,
      removeSlotModel,
      onBpGa,
      onDetailsGa,
      fetchState,
      searchPlaceholder,
      chooseModelLabel,
      addModelLabel,
      removeModelLabel,
      selectModelPromptLabel,
      selectModelPromptDescription,
      specsFullyExpanded,
      showOnlyDifferences,
      evolutionCompareSpecsConfig,
      showMoreSpecsLabelResolved,
      showLessSpecsLabelResolved,
      showOnlyDifferencesLabelResolved,
      compareStickyCssVars,
      compareSpecsStickySentinel,
      compareSpecsStickyEndSentinel,
      compareSpecsRegionRef,
      showSpecsStickyDropdowns,
      specsLoadingOverlayActive,
      onSpecsLoadingOverlay,
      specSectionIconMappingsFromLabels
    }
  }
}
</script>

<style lang="scss" src="./EvolutionCompare/evolution-compare.scss"></style>
