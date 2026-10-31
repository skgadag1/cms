<template>
  <b-overlay :show="$fetchState.pending">
    <Specifications data-test-id="product-specifications"
                    :class="{ 'bg-gray-lighter' : compareOn }"
                    :disabled="!model || !model['@path']"
                    :limit-content="limitContentOption && !compareOn"
                    :evolution-button="evolutionButtonOption"
                    :show-units="showUnitsOption"
                    :is-compare="compareOn"
                    :labels="labels"
                    :button-label="buttonLabel"
                    :title="title"
                    :evolution-title="evolutionTitle"
                    :htag="htag"
                    :uom="uom"
                    :cta-button="ctaButton"
                    @uom-changed="reloadSpecs"
    >
      <template v-if="showExpandAll === 'true'" #expand-collapse>
        <SpecTableToggle :is-expanded-all="isExpandedAll"
                         :labels="labels"
                         :tables-ref="tablesWrapperRef"
                         @expandAll="expandAllToggle"
        />
      </template>

      <template v-if="!historicalContext" #compare>
        <div :title="generateConceptHint('compare')">{{ labels.compare || 'Compare' }}</div>
        <b-button-group>
          <b-button :variant="compareOn ? 'outline-primary' : 'primary'" class="text-capitalize compare-btn" @click.prevent="compareOn = false">{{ labels.off || 'Off' }}</b-button>
          <b-button :variant="compareOn ? 'primary' : 'outline-primary'" class="text-capitalize compare-btn" @click.prevent="compareOn = true">{{ labels.on || 'On' }}</b-button>
        </b-button-group>
      </template>

      <div v-if="labelsLoaded">
        <div v-show="!compareOn" class="container">
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
              <WarningAlert v-if="model && !model['@path'] && inEditorEdit" data-test-id="product-specifications-nospecs-alert">Model not available <b>{{ model }}</b>.</WarningAlert>
              <WarningAlert v-else-if="!model && inEditorEdit" data-test-id="product-specifications-nospecs-alert">Model not defined.</WarningAlert>
            </div>
          </div>
        </div>

        <LazyHydrate never :trigger-hydration="compareOn">
          <keep-alive>
            <ProductCompare v-show="compareOn"
                            :key="`product-compare-specs`"
                            :is-visible="compareOn"
                            :uom="currentUom"
                            :historical="historicalContext"
                            :labels="labels"
            />
          </keep-alive>
        </LazyHydrate>
      </div>
    </Specifications>
  </b-overlay>
</template>

<script>
import {inject, onMounted, onServerPrefetch, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import useModels from '~/helpers/composables/useModels'
import useConcepts from '~/helpers/composables/useConcepts'
import {MODEL_LIFECYCLE, MODEL_TYPE} from '~/constants'
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
    },
    cta: {
      type: Object,
      default: () => {}
    }
  },
  setup(props) {
    const pageTemplateId = inject('pageTemplateId')
    const historicalContext = pageTemplateId?.includes(MODEL_LIFECYCLE.HISTORICAL)
    const {model, models} = useModels(MODEL_TYPE.PRODUCT, historicalContext)
    const {$services, $utils, $regions} = useContext()
    const limitContentOption = ref(props.limitContent === 'true')
    const evolutionButtonOption = ref(props.evolutionButton === 'true')
    const showUnitsOption = props.showUnits === 'true'
    const compareOn = ref(false)
    const labels = ref({})
    const uom = ref([
      {label: 'US', value: 'us'},
      {label: 'Metric', value: 'metric'}
    ])
    const currentUom = ref({label: 'US', value: 'us'})
    const labelsLoaded = ref(false)
    const commonHorizontalScrollPosition = ref(0)
    const {generateConceptHint} = useConcepts()
    const tablesWrapperRef = ref(null)
    useMounted(props.nodeId)

    const ctaButton = (() => {
      const cta = props.cta
      if (!cta?.link) { return null }

      const linkField = $utils.getLinkField(cta, $regions.current.language.name)
      if (!linkField) { return null }

      return {
        link: linkField.url || '',
        title: linkField.label || '',
        target: linkField.target || 'self'
      }
    })()

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
      if (model && model['@path'] && !Object.keys(labels.value).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/common/labels/specifications'])
        if (process.client) { console.log('[DEBUG] us:', labels.value?.us, '| metric:', labels.value?.metric, '| Metric:', labels.value?.Metric) }
        uom.value = [
          {label: labels.value?.us || 'US', value: 'us'},
          {label: labels.value?.metric || 'Metric', value: 'metric'}
        ]
        currentUom.value = uom.value[0]
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
      compareOn,
      labels,
      labelsLoaded,
      keySpecData,
      allSpecData,
      reloadSpecs,
      handleKeySpecData,
      handleAllSpecData,
      productAllSpecConfig,
      productKeySpecConfig,
      generateConceptHint,
      historicalContext,
      inEditorEdit,
      updateScrollPosition,
      commonHorizontalScrollPosition,
      isExpandedAll,
      isFirstTableExpanded,
      expandAllToggle,
      tablesWrapperRef,
      renderPrintElements,
      ctaButton
    }
  }
}
</script>

<style lang="scss" scoped>
.compare-btn {
  height: 2.5rem;
}
</style>
