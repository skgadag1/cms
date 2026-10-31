<template>
  <section v-cloak
           ref="compareTable"
           class="container specification-compare-table-old transition-base"
           :class="{'squeezed' : columnSqueezed, 'wide' : isWide}"
           data-test-id="compare-table-wrapper-old"
  >
    <LazyHydrate when-visible>
      <b-overlay :show="$fetchState.pending" :class="!$fetchState.pending ? containerClass : 'py-5'">
        <template v-if="!error && tableData">
          <CompareTableOld v-show="!$fetchState.pending"
                           :table-data="tableData"
                           :show-header="options.alwaysShowHeader"
                           :show-category-header="showCategoryHeader"
                           :expaneded="expanded"
                           :no-bottom-margin="options.noBottomMargin"
          />
        </template>

        <WarningAlert v-else data-test-id="compare-table-old-error"><strong>Error!</strong> Please pick different model and try again.</WarningAlert>
      </b-overlay>
    </LazyHydrate>
  </section>
</template>

<script>
import {onBeforeUnmount, onMounted, ref, useContext, useFetch, watch} from '@nuxtjs/composition-api'
import {MODEL_TYPE, PRODUCT_SPECS_REMOVED_PROPERTIES, PRODUCT_SPECS_REMOVED_SECTIONS} from '~/constants'
import useSpecificationData from '@/helpers/composables/useSpecificationData'

const DESKTOP_TABLE_SCROLL_SIZE_LG = 5

export default {
  props: {
    models: {
      type: Array,
      required: true
    },
    uom: {
      type: Object
    },
    modelType: {
      type: String,
      default: MODEL_TYPE.PRODUCT
    },
    specificationLevel: {
      type: [Number, String],
      default: 3
    },
    expanded: {
      type: Boolean,
      default: true
    },
    shouldSort: {
      type: Boolean,
      default: false
    },
    tableSpecificationData: {
      type: Object
    },
    commonHorizontalScrollPosition: {
      type: Number,
      default: 0
    },
    config: {
      type: Object,
      default: () => {
        return {}
      }
    },
    labels: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  emits: ['data-update', 'updateScrollPosition'],
  setup(props, {emit}) {
    const {models, specificationLevel, modelType, tableSpecificationData, config, labels, shouldSort} = props
    const {$utils} = useContext()
    const compareTable = ref(null)
    const multipleModels = (models?.length || tableSpecificationData?.models?.length) > 2
    const columnSqueezed = ref(false)
    const options = {
      filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
      filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
      showCategoryNames: true,
      alwaysShowHeader: multipleModels,
      customGroupHeader: props.keySpecsTitle,
      noBottomMargin: false,
      ...config
    }
    const showCategoryHeader = ref(options.showCategoryNames)

    const isWide = ref(multipleModels && ($utils.isMobile() || $utils.isTablet()))

    const {
      error,
      specificationFetch,
      tableData,
      containerClass
    } = useSpecificationData(props, options, shouldSort)

    const {fetch} = useFetch(async () => {
      if (!tableSpecificationData) {
        await specificationFetch(specificationLevel, modelType, labels)
        emit('data-updated', tableData.value)
      }
    })

    if (process.client) {
      const horizontalScrollHandler = (e) => {
        columnSqueezed.value = e.target.scrollLeft > 0 && $utils.isMobile()
        emit('updateScrollPosition', e.target.scrollLeft)
      }

      const wideHandler = () => {
        isWide.value = (multipleModels && ($utils.isMobile() || $utils.isTablet())) ||
          (($utils.isDesktopLG() || $utils.isDesktopXL()) && (models?.length || tableSpecificationData?.models?.length) > DESKTOP_TABLE_SCROLL_SIZE_LG)
      }

      onMounted(() => compareTable.value?.addEventListener('scroll', horizontalScrollHandler, {passive: true}))
      onBeforeUnmount(() => compareTable.value?.removeEventListener('scroll', horizontalScrollHandler))
      onMounted(() => {
        wideHandler()
        window.addEventListener('resize', wideHandler)
        emit('data-updated', tableData.value)
      })
      onBeforeUnmount(() => window.removeEventListener('resize', wideHandler))
    }

    watch(() => props.uom, () => {
      fetch()
    })

    watch(() => props.commonHorizontalScrollPosition, (newVal) => {
      if (compareTable.value && newVal) {
        compareTable.value.scrollLeft = newVal
      }
    })

    watch(() => [options.showCategoryNames, tableData.value], () => {
      showCategoryHeader.value = options.showCategoryNames || (tableData.value.sections > 1 && multipleModels)
    })

    return {
      compareTable,
      tableData: tableSpecificationData || tableData,
      error,
      isWide,
      containerClass,
      multipleModels,
      columnSqueezed,
      showCategoryHeader,
      options
    }
  }
}
</script>
