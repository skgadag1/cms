<template>
  <div class="mt-0 pt-0"
       data-test-id="evolution-compare-specifications"
  >
    <b-overlay class="mt-0 pt-0 mb-0"
               :show="fetchState.pending && !hasRenderableTable"
               opacity="0.35"
               rounded
               spinner-variant="primary"
    >
      <b-alert v-if="error"
               show
               variant="warning"
               class="mb-0"
               data-test-id="evolution-compare-specifications-error"
      >
        {{ specConceptLabels.compareError || specConceptLabels.compareTableError || 'Unable to load specifications for comparison.' }}
      </b-alert>

      <template v-else-if="hasRenderableTable && tableData">
        <div v-for="sectionKey in sectionKeysWithContent"
             :key="`evolution-compare-spec-section-${sectionKey}`"
             :class="sectionLayoutClass(sectionKey)"
             :data-test-id="`evolution-compare-spec-section-${sectionKey}`"
        >
          <div class="d-flex pb-3 align-items-center">
            <ImageSet v-if="sectionIconDisplay(sectionsForView[sectionKey].title).type === 'image'"
                      :src="sectionIconDisplay(sectionsForView[sectionKey].title).src"
                      :alt="sectionsForView[sectionKey].title"
                      class="flex-shrink-0 mr-2 evolution-compare-spec-section-icon evolution-compare-spec-section-icon--image"
                      :options="{ fluid: false, width: 28, height: 28 }"
            />
            <b-icon v-else-if="sectionIconDisplay(sectionsForView[sectionKey].title).type === 'icon'"
                    :icon="sectionIconDisplay(sectionsForView[sectionKey].title).name"
                    class="flex-shrink-0 mr-2 text-dark evolution-compare-spec-section-icon"
            />
            <span class="text-uppercase mb-0 h4 text-dark font-weight-bold">
              {{ sectionsForView[sectionKey].title }}
            </span>
          </div>
          <EvolutionCompareSpecRows :properties="sectionsForView[sectionKey].properties"
                                    :keys="modelIdKeys"
          />
        </div>
      </template>
    </b-overlay>
  </div>
</template>

<script>
import {computed, onBeforeUnmount, onMounted, reactive, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import {MODEL_TYPE, PRODUCT_SPECS_REMOVED_PROPERTIES, PRODUCT_SPECS_REMOVED_SECTIONS} from '~/constants'
import METRICS from '~/constants/metrics'
import useSpecificationData from '@/helpers/composables/useSpecificationData'
import EvolutionCompareSpecRows from '~/components/EvolutionCompare/EvolutionCompareSpecRows.vue'
import ImageSet from '~/components/elements/ImageSet.vue'
import {
  buildSectionTitleToIconMap,
  coerceSpecSectionIconMappingsList,
  fetchEvolutionCompareSpecSectionIconConcepts,
  filterSectionsForDifferencesOnly,
  mergeSectionTitleConceptImageMaps,
  resolveSectionIconDisplay,
  sliceSpecsToPreview
} from '~/helpers/evolutionCompareSpecsFilter'

const defaultCompareOptions = () => ({
  filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
  filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
  showCategoryNames: true,
  alwaysShowHeader: true,
  noBottomMargin: true
})

export default {
  name: 'EvolutionCompareSpecifications',
  components: {
    EvolutionCompareSpecRows,
    ImageSet
  },
  props: {
    models: {
      type: Array,
      required: true
    },
    labels: {
      type: Object,
      default: () => ({})
    },
    compareConfig: {
      type: Object,
      default: () => ({})
    },
    showOnlyDifferences: {
      type: Boolean,
      default: false
    },
    sectionIconMappings: {
      type: [Array, Object, String],
      default: null
    },
    uom: {
      type: Object,
      default: null
    },
    specificationLevel: {
      type: [Number, String],
      default: 2
    },
    modelType: {
      type: String,
      default: MODEL_TYPE.PRODUCT
    },
    shouldSort: {
      type: Boolean,
      default: false
    },
    expandAllSpecs: {
      type: Boolean,
      default: true
    }
  },
  emits: ['specs-loading-overlay'],
  setup(props, {emit}) {
    const {$services, $regions} = useContext()
    const sectionIconConcepts = ref({})

    const loadSectionIconConcepts = async () => {
      const loaded = await fetchEvolutionCompareSpecSectionIconConcepts(
        $services.bobcatapi,
        $regions.current.name,
        process.env.VUE_APP_SITE_BASENAME || ''
      )
      if (loaded && Object.keys(loaded).length) {
        sectionIconConcepts.value = {
          ...sectionIconConcepts.value,
          ...loaded
        }
      }
    }

    onMounted(() => {
      loadSectionIconConcepts()
    })

    const mergedOptions = {
      ...defaultCompareOptions(),
      ...props.compareConfig
    }

    const specPropsReactive = reactive({
      models: (props.models || []).filter(m => m && m.id),
      uom: props.uom && typeof props.uom === 'object' ? props.uom : METRICS[0],
      config: props.compareConfig || {},
      specificationLevel: props.specificationLevel,
      modelType: props.modelType
    })

    const modelsWithIdCount = computed(() => props.models?.filter(m => m && m.id).length || 0)

    const {
      error,
      specificationFetch,
      tableData
    } = useSpecificationData(specPropsReactive, mergedOptions, props.shouldSort)

    const {fetch, fetchState} = useFetch(async () => {
      if (modelsWithIdCount.value < 2) {
        error.value = false
        tableData.value = null
        return
      }
      await specificationFetch(props.specificationLevel, props.modelType, props.labels)
    })

    watch(
      () => props.models,
      () => {
        specPropsReactive.models = (props.models || []).filter(m => m && m.id)
        if (modelsWithIdCount.value < 2) {
          error.value = false
          tableData.value = null
          return
        }
        fetch()
      },
      {deep: true}
    )

    watch(
      () => props.uom,
      (val) => {
        specPropsReactive.uom = val && typeof val === 'object' ? val : METRICS[0]
        if (modelsWithIdCount.value >= 2) {
          fetch()
        }
      },
      {deep: true}
    )

    watch(
      () => props.compareConfig,
      (val) => {
        specPropsReactive.config = val || {}
      },
      {deep: true}
    )

    watch(
      () => props.specificationLevel,
      (v) => {
        specPropsReactive.specificationLevel = v
      }
    )

    watch(
      () => props.modelType,
      (v) => {
        specPropsReactive.modelType = v
      }
    )

    const labelsRef = computed(() => props.labels || {})

    const hasRenderableTable = computed(() =>
      !!tableData.value?.models?.length && modelsWithIdCount.value >= 2
    )

    const showSpecsLoadingOverlay = computed(() =>
      Boolean(fetchState.value?.pending && !hasRenderableTable.value)
    )

    const bootstrapIconTitleMap = computed(() =>
      buildSectionTitleToIconMap(coerceSpecSectionIconMappingsList(props.sectionIconMappings))
    )

    const conceptImageTitleMap = computed(() => {
      const L = props.labels || {}
      return mergeSectionTitleConceptImageMaps(
        sectionIconConcepts.value,
        L.compareSpecSectionImages,
        L.specCompareSectionImages,
        L.evolutionCompareSpecSectionImages
      )
    })

    const modelIdKeys = computed(() => (tableData.value?.models || []).map(m => m.id))

    const displaySections = computed(() => {
      const sections = tableData.value?.sections
      if (!sections) {
        return {}
      }
      if (!props.showOnlyDifferences) {
        return sections
      }
      return filterSectionsForDifferencesOnly(sections, modelIdKeys.value)
    })

    const sectionsForView = computed(() => {
      const full = displaySections.value
      if (props.expandAllSpecs) {
        return full
      }
      return sliceSpecsToPreview(full, {maxSections: 1, maxRowsPerSection: 1})
    })

    const compareSectionKeyOrder = computed(() => Object.keys(sectionsForView.value || {}))

    const sectionKeysWithContent = computed(() =>
      compareSectionKeyOrder.value.filter(
        sk => Object.keys((sectionsForView.value[sk] || {}).properties || {}).length > 0
      )
    )

    const firstCompareSectionKey = computed(() => {
      const keys = sectionKeysWithContent.value
      return keys.length ? keys[0] : null
    })

    const isLastCompareSectionKey = (sectionKey) => {
      const keys = sectionKeysWithContent.value
      if (!keys.length) {
        return false
      }
      return sectionKey === keys[keys.length - 1]
    }

    const sectionLayoutClass = (sectionKey) => {
      const isFirst = sectionKey === firstCompareSectionKey.value
      const isLast = isLastCompareSectionKey(sectionKey)
      return {
        'mt-0 pt-0': isFirst,
        'mb-0': isLast,
        'mb-7': !isLast && !isFirst,
        'mt-7': !isFirst
      }
    }

    const sectionIconDisplay = title =>
      resolveSectionIconDisplay(title, {
        conceptImageMap: conceptImageTitleMap.value,
        bootstrapIconMap: bootstrapIconTitleMap.value
      })

    watch(
      showSpecsLoadingOverlay,
      (show) => {
        emit('specs-loading-overlay', show)
      },
      {immediate: true}
    )

    onBeforeUnmount(() => {
      emit('specs-loading-overlay', false)
    })

    return {
      mergedOptions,
      specConceptLabels: labelsRef,
      error,
      tableData,
      fetchState,
      hasRenderableTable,
      sectionsForView,
      sectionKeysWithContent,
      sectionLayoutClass,
      isLastCompareSectionKey,
      modelIdKeys,
      sectionIconDisplay
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

/* Match compare key rows: icon reads as H4 on mobile, H3 scale from md up. */
.evolution-compare-spec-section-icon {
  font-size: 1.5rem;
  line-height: 1;

  @include media-breakpoint-up(md) {
    font-size: 1.75rem;
  }

  &--image {
    width: 1.5rem;
    height: 1.5rem;
    object-fit: contain;

    @include media-breakpoint-up(md) {
      width: 1.75rem;
      height: 1.75rem;
    }

    ::v-deep img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
}
</style>
