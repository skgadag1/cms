<template>
  <section class="transition-base py-4">
    <div v-if="(!model || !model['@path']) && inEditorEdit" class="container">
      <WarningAlert data-test-id="specifications-alert">
        No model defined for page. Please check page template configuration.
      </WarningAlert>
    </div>

    <template v-else-if="hasModel">
      <div class="container">
        <div class="row">
          <LazyHydrate :on-interaction="['click', 'touchstart']">
            <div class="col-12">
              <div class="d-flex flex-column justify-content-between align-items-left">
                <div v-if="evolutionTitle" v-html="eTitle" />
                <component :is="htag" v-else :class="[htag, 'fs-24 mb-1']" data-test-id="product-specifications-title">{{ oTitle }}</component>
              </div>
            </div>
          </LazyHydrate>

          <div class="col-12 d-flex flex-column flex-md-row mb-4">
            <div class="d-flex flex-column">
              <button
                v-if="true"
                role="button"
                class="d-print-none d-flex justify-content-left align-items-center pl-0 pr-3 my-2 bg-transparent border-0 font-weight-bold text-primary"
                data-test-id="product-specifications-download-button"
                @click.prevent="downloadOrPrintPDFSpecsOnly"
              >
                <span class="d-flex align-items-center justify-content-center m-0 mr-2 transition-base"><b-icon icon="download" /></span> {{ buttonLabel }}
              </button>
              <a
                v-if="ctaButton && ctaButton.link && ctaButton.title"
                :href="ctaButton.link"
                :target="ctaButton.target === '_blank_follow' ? '_blank' : ctaButton.target"
                :rel="ctaButton.target === '_blank' ? 'noopener nofollow' : null"
                class="text-primary-red text-decoration-none d-print-none font-weight-bold my-2 mt-2"
                data-test-id="specifications-cta-button"
              >
                {{ ctaButton.title }}
                <b-icon icon="chevron-right" class="ml-1" />
              </a>
            </div>
            <div class="d-flex align-items-center d-print-none ml-md-auto mt-3 mt-md-0" data-test-id="product-specifications-units">
              <div>
                <slot name="compare" />
              </div>
              <SwitchDropdown v-if="showUnits"
                              data-test-id="specifications-units"
                              :label="labels.units || 'Units'"
                              :options="uom"
                              :config="{right:true}"
                              class="ml-2"
                              @changed="$emit('uom-changed', $event)"
              />
            </div>
          </div>
        </div>
      </div>

      <ContentLimiter v-if="hasModel"
                      :evolution-button="evolutionButton"
                      :disabled="!limitContent || disabled || isPrinting"
                      :show-more="labels.showMore"
                      :show-less="labels.showLess"
      >
        <slot />
      </ContentLimiter>
      <slot name="print" />
    </template>
  </section>
</template>

<script>
import {ref, computed} from '@nuxtjs/composition-api'
import {inEditorEdit} from '~/helpers/vue-editor'
import {MODEL_TYPE} from '~/constants'
import usePrintSpecs from '@/helpers/composables/usePrintSpecs'
import useModels from '@/helpers/composables/useModels'
import useBreakpoints from '~/helpers/composables/useBreakpoints'

export default {
  inject: ['model'],
  props: {
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    evolutionTitle: {
      type: String
    },
    limitContent: {
      type: Boolean
    },
    evolutionButton: {
      type: Boolean
    },
    showUnits: {
      type: Boolean
    },
    isCompare: {
      type: Boolean
    },
    buttonLabel: {
      type: String,
      default: 'Download & Print Specs'
    },
    disabled: {
      type: Boolean,
      default: false
    },
    uom: {
      type: Array,
      required: true
    },
    labels: {
      type: Object,
      default: () => { return {} }
    },
    ctaButton: {
      type: Object,
      default: () => { return {} }
    },
    modelType: {
      type: String,
      default: MODEL_TYPE.PRODUCT
    }
  },
  setup(props) {
    const {model, modelVariant} = useModels(props.modelType)
    const hasModel = model && (model.id || model['@path'])
    const {mobile} = useBreakpoints()
    const oTitle = computed(() => {
      const selected = modelVariant.value || model
      return props.title?.replace('%ID%', selected?.id).replace('%NAME%', selected?.['@name']).replace('%TITLE%', selected?.title || selected?.id || selected?.['@name'])
    })
    const eTitle = computed(() => {
      const selected = modelVariant.value || model
      return props.evolutionTitle?.replace('%ID%', selected?.id).replace('%NAME%', selected?.['@name']).replace('%TITLE%', selected?.title || selected?.id || selected?.['@name'])
    })
    const currentUom = ref(props.uom[0])

    const {showPrintOnMobile, downloadOrPrintPDFSpecsOnly, isPrinting} = usePrintSpecs(model, currentUom, props.modelType)

    return {
      oTitle,
      eTitle,
      inEditorEdit,
      currentUom,
      mobile,
      downloadOrPrintPDFSpecsOnly,
      showPrintOnMobile,
      isPrinting,
      hasModel
    }
  }
}
</script>
