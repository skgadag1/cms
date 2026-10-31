<template>
  <b-overlay :show="$fetchState.pending">
    <Specifications data-test-id="product-specifications"
                    :disabled="!model"
                    :limit-content="limitContentOption"
                    :evolution-button="evolutionButtonOption"
                    :show-units="showUnitsOption"
                    :is-compare="true"
                    :labels="labels"
                    :button-label="buttonLabel"
                    :title="specificationTitle"
                    :evolution-title="evolutionSpecificationTitle"
                    :htag="htag"
                    :uom="uom"
                    @uom-changed="reloadSpecs"
    >
      <template v-if="showExpandAll === 'true'" #expand-collapse>
        <SpecTableToggle :is-expanded-all="isExpandedAll"
                         :labels="labels"
                         :tables-ref="tablesWrapperRef"
                         @expandAll="expandAllToggle"
        />
      </template>

      <template v-if="renderPrintElements" #print>
        <CompareTableWrapper data-test-id="product-specifications-compare-table-key-spec-print"
                             class="col-12 d-none d-print-block"
                             :models="models"
                             :uom="currentUom"
                             :specification-level="2"
                             :config="productKeySpecConfig"
                             :labels="labels"
        />
        <CompareTableWrapper data-test-id="product-specifications-compare-table-all-spec-print"
                             class="col-12 d-none d-print-block"
                             :models="models"
                             :uom="currentUom"
                             :config="productAllSpecConfig"
                             :labels="labels"
        />
      </template>

      <div v-if="labelsLoaded" class="container">
        <div ref="tablesWrapperRef" class="row">
          <CompareTableWrapper data-test-id="product-specifications-compare-table-key-spec"
                               class="col-12"
                               :models="models"
                               :uom="currentUom"
                               :specification-level="2"
                               :config="productKeySpecConfig"
                               :labels="labels"
                               :expanded="isFirstTableExpanded || isExpandedAll"
                               @data-updated="handleKeySpecData"
          />
          <CompareTableWrapper data-test-id="product-specifications-compare-table-all-spec"
                               class="col-12"
                               :models="models"
                               :uom="currentUom"
                               :config="productAllSpecConfig"
                               :labels="labels"
                               :expanded="isExpandedAll"
                               @data-updated="handleAllSpecData"
          />
          <div class="col-12">
            <WarningAlert v-if="model && inEditorEdit" data-test-id="product-specifications-nospecs-alert">Model not available <b>{{ model }}</b>.</WarningAlert>
            <WarningAlert v-else-if="!model && inEditorEdit" data-test-id="product-specifications-nospecs-alert">Model not defined.</WarningAlert>
          </div>
        </div>
      </div>
    </Specifications>
  </b-overlay>
</template>

<script>
import {inject, onMounted, onServerPrefetch, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useModels from '~/helpers/composables/useModels'
import {METRICS, MODEL_LIFECYCLE, MODEL_TYPE} from '~/constants'
import useMounted from '~/helpers/composables/useMounted'
import {inEditorEdit} from '~/helpers/vue-editor'

export default {
  components: {
    SpecTableToggle: () => import('~/components/Specifications/SpecTableToggle.vue')
  },
  inject: ['model'],
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    evolutionTitle: {
      type: String
    },
    keySpecsTitle: {
      type: String,
      required: true
    },
    limitContent: {
      type: String
    },
    evolutionButton: {
      type: String
    },
    showUnits: {
      type: String
    },
    showExpandAll: {
      type: String
    },
    buttonLabel: {
      type: String,
      default: 'Download & Print Specs'
    },
    htag: {
      type: String,
      default: 'h2'
    }
  },
  setup(props) {
    const pageTemplateId = inject('pageTemplateId')
    const historicalContext = pageTemplateId?.includes(MODEL_LIFECYCLE.HISTORICAL)
    const {model, models: lifecycleModels} = useModels(MODEL_TYPE.PRODUCT, historicalContext)
    const {$services, $regions} = useContext()
    const uom = METRICS
    const currentUom = ref(uom[0])
    const limitContentOption = ref(props.limitContent === 'true')
    const evolutionButtonOption = ref(props.evolutionButton === 'true')
    const showUnitsOption = props.showUnits === 'true'
    const labels = ref({})
    const labelsLoaded = ref(false)
    const commonHorizontalScrollPosition = ref(0)
    const tablesWrapperRef = ref(null)
    useMounted(props.nodeId)

    const specificationTitle = props.title?.replace('%ID%', model?.id).replace('%NAME%', model?.name).replace('%TITLE%', '')
    const evolutionSpecificationTitle = props.evolutionTitle?.replace('%ID%', model?.id).replace('%NAME%', model?.name).replace('%TITLE%', '')

    const selectedModel = lifecycleModels?.[0] || model
    const models = [{
      id: selectedModel?.id,
      title: selectedModel?.name,
      '@path': selectedModel?.['@path'] || $regions.current.name,
      productDetailsDataSource: selectedModel?.productDetailsDataSource
    }]

    const isExpandedAll = ref(false)
    const isFirstTableExpanded = ref(true)
    const renderPrintElements = ref(false)

    const expandAllToggle = () => {
      isExpandedAll.value = !isExpandedAll.value
      isFirstTableExpanded.value = isExpandedAll.value
    }

    const updateScrollPosition = (v) => {
      commonHorizontalScrollPosition.value = v
    }

    const reloadSpecs = (n) => {
      currentUom.value = n
    }

    const keySpecData = ref({})
    const handleKeySpecData = (tableData) => {
      keySpecData.value = tableData
    }

    const allSpecData = ref({})
    const handleAllSpecData = (tableData) => {
      allSpecData.value = tableData
    }

    const {fetch, fetchState} = useFetch(async () => {
      if (model && !Object.keys(labels.value).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/common/labels/specifications'])
      }
      labelsLoaded.value = true
    })

    const productAllSpecConfig = {
      alwaysShowHeader: false,
      customGroupHeader: null
    }

    const productKeySpecConfig = {
      customGroupHeader: props.keySpecsTitle
    }

    onServerPrefetch(async () => {
      await fetch()
    })

    onMounted(() => {
      renderPrintElements.value = true
    })

    return {
      models,
      fetchState,
      limitContentOption,
      evolutionButtonOption,
      showUnitsOption,
      uom,
      currentUom,
      labels,
      labelsLoaded,
      keySpecData,
      allSpecData,
      reloadSpecs,
      handleKeySpecData,
      handleAllSpecData,
      productAllSpecConfig,
      productKeySpecConfig,
      historicalContext,
      inEditorEdit,
      updateScrollPosition,
      commonHorizontalScrollPosition,
      isExpandedAll,
      isFirstTableExpanded,
      expandAllToggle,
      tablesWrapperRef,
      renderPrintElements,
      specificationTitle,
      evolutionSpecificationTitle
    }
  }
}
</script>
