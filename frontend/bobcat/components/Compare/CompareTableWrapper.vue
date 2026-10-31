<template>
  <section v-cloak
           class="container specification-compare-table transition-base"
           data-test-id="specification-table-compare"
  >
    <LazyHydrate when-visible>
      <b-overlay :show="$fetchState.pending" :class="!$fetchState.pending ? containerClass : 'py-5'">
        <template v-if="!error && tableData">
          <CompareTable v-show="!$fetchState.pending"
                        :table-data="tableData"
                        :show-header="options.alwaysShowHeader"
                        :show-category-header="showCategoryHeader"
                        :expaneded="expanded"
                        :no-bottom-margin="options.noBottomMargin"
          />
        </template>

        <WarningAlert v-else data-test-id="compare-table-error">
          <strong>Error!</strong> Please pick different model and
          try again.
        </WarningAlert>
      </b-overlay>
    </LazyHydrate>
  </section>
</template>

<script>
import {ref, useFetch, watch} from '@nuxtjs/composition-api'
import {MODEL_TYPE, PRODUCT_SPECS_REMOVED_PROPERTIES, PRODUCT_SPECS_REMOVED_SECTIONS} from '~/constants'
import useSpecificationData from '@/helpers/composables/useSpecificationData'

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
    const multipleModels = ref((props.models?.length || props.tableSpecificationData?.models?.length) > 1)
    const options = {
      filterSections: PRODUCT_SPECS_REMOVED_SECTIONS,
      filterProps: PRODUCT_SPECS_REMOVED_PROPERTIES,
      showCategoryNames: true,
      alwaysShowHeader: multipleModels.value,
      customGroupHeader: props.keySpecsTitle,
      noBottomMargin: false,
      ...props.config
    }
    const showCategoryHeader = ref(options.showCategoryNames)

    const {
      error,
      specificationFetch,
      tableData,
      containerClass
    } = useSpecificationData(props, options, props.shouldSort)

    const {fetch} = useFetch(async () => {
      if (!props.tableSpecificationData) {
        await specificationFetch(props.specificationLevel, props.modelType, props.labels)
        emit('data-updated', tableData.value)
      }
    })

    watch(() => props.models, () => {
      multipleModels.value = (props.models?.length || props.tableSpecificationData?.models?.length) > 1
      fetch()
    }, {deep: true})

    watch(() => props.uom, () => {
      fetch()
    })

    watch(() => [options.showCategoryNames, tableData.value], () => {
      showCategoryHeader.value = options.showCategoryNames || (tableData.value.sections > 1 && multipleModels.value)
    })

    return {
      tableData: props.tableSpecificationData || tableData,
      error,
      containerClass,
      multipleModels,
      showCategoryHeader,
      options
    }
  }
}
</script>
