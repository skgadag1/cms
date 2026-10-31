<template>
  <b-overlay :show="$fetchState.pending">
    <Specifications data-test-id="attachment-specifications"
                    :disabled="!selectedModel"
                    :limit-content="limitContentOption"
                    :evolution-button="evolutionButtonOption"
                    :show-units="showUnitsOption"
                    :labels="labels"
                    :button-label="buttonLabel"
                    :title="title"
                    :evolution-title="evolutionTitle"
                    :htag="htag"
                    :uom="uom"
                    :cta-button="ctaButton"
                    :model-type="MODEL_TYPE.ATTACHMENT"
                    @uom-changed="reloadSpecs"
    >
      <template v-if="showExpandAll === 'true'" #expand-collapse>
        <SpecTableToggle :is-expanded-all="isExpandedAll"
                         :labels="labels"
                         :tables-ref="tablesWrapperRef"
                         @expandAll="expandAllToggle"
        />
      </template>

      <div class="container">
        <div ref="tablesWrapperRef" class="row">
          <CompareTableWrapper data-test-id="attachment-specifications-compare-table"
                               class="col-12 my-4"
                               :models="selectedModel"
                               :uom="currentUom"
                               :config="attachmentSpecConfig"
                               :labels="labels"
                               :model-type="MODEL_TYPE.ATTACHMENT"
                               should-sort
                               :expanded="isExpandedAll"
          />
        </div>
      </div>
    </Specifications>
  </b-overlay>
</template>

<script>
import {ref, useFetch, useContext, onServerPrefetch, computed} from '@nuxtjs/composition-api'
import {METRICS, ATTACHMENT_SPECS_REMOVED_PROPERTIES, ATTACHMENT_SPECS_REMOVED_SECTIONS, ATTACHMENT_SPECS_REMOVED_PROPERTIES_ID, MODEL_TYPE} from '~/constants'
import useModels from '~/helpers/composables/useModels'
import useMounted from '~/helpers/composables/useMounted'

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
    htag: {
      type: String,
      default: 'h2'
    },
    limitContent: {
      type: String,
      required: false
    },
    evolutionButton: {
      type: String
    },
    showUnits: {
      type: String,
      required: false
    },
    showExpandAll: {
      type: String
    },
    buttonLabel: {
      type: String,
      default: 'Download & Print Specs'
    },
    showCategoryNames: {
      type: String
    },
    cta: {
      type: Object,
      default: () => {}
    }
  },
  setup(props) {
    const {models, modelVariant} = useModels(MODEL_TYPE.ATTACHMENT)
    const {$services, $utils, $regions} = useContext()
    const {showCategoryNames, limitContent, showUnits} = props
    const limitContentOption = ref(limitContent === 'true')
    const evolutionButtonOption = ref(props.evolutionButton === 'true')
    const showUnitsOption = showUnits === 'true'
    const uom = METRICS
    const currentUom = ref(uom[0])
    const reloadSpecs = (o) => { currentUom.value = o }
    const labels = ref({})
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

    const tablesWrapperRef = ref(null)
    const isExpandedAll = ref(true)
    const expandAllToggle = () => {
      isExpandedAll.value = !isExpandedAll.value
    }

    const {fetch, fetchState} = useFetch(async () => {
      if (!Object.keys(labels.value || {}).length) {
        labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional', '/common/labels/specifications', '/%region%/common/labels/specifications'])
      }
    })

    onServerPrefetch(async () => {
      await fetch()
    })

    const attachmentSpecConfig = {
      filterProps: ATTACHMENT_SPECS_REMOVED_PROPERTIES,
      filterPropsId: ATTACHMENT_SPECS_REMOVED_PROPERTIES_ID,
      filterSections: ATTACHMENT_SPECS_REMOVED_SECTIONS,
      showCategoryNames: showCategoryNames === 'true',
      noBottomMargin: true
    }

    const selectedModel = computed(() => modelVariant.value ? [modelVariant.value] : models)

    return {
      fetchState,
      limitContentOption,
      evolutionButtonOption,
      showUnitsOption,
      selectedModel,
      uom,
      currentUom,
      reloadSpecs,
      labels,
      MODEL_TYPE,
      attachmentSpecConfig,
      isExpandedAll,
      expandAllToggle,
      tablesWrapperRef,
      ctaButton
    }
  }
}
</script>
